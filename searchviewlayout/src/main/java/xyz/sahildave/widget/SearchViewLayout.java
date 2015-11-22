/*
 * Copyright (C) 2015 Sahil Dave
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package xyz.sahildave.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchViewLayout extends FrameLayout {
    public static final int ANIMATION_DURATION = 150;
    private static final String LOG_TAG = SearchViewLayout.class.getSimpleName();

    /* Subclass-visible for testing */
    protected boolean mIsExpanded = false;

    private ViewGroup mCollapsed;
    private ViewGroup mExpanded;
    private EditText mSearchEditText;
    private View mSearchIcon;
    private View mCollapsedSearchBox;
    private View mBackButtonView;
    private View mExpandedSearchIcon;

    private int toolbarExpandedHeight = 0;

    private ValueAnimator mAnimator;
    private OnToggleAnimationListener mOnToggleAnimationListener;
    private SearchListener mSearchListener;
    private Fragment mExpandedContentFragment;
    private FragmentManager mFragmentManager;
    private TransitionDrawable mBackgroundTransition;
    private Toolbar mToolbar;

    private Drawable mCollapsedDrawable;
    private Drawable mExpandedDrawable;

    private int mExpandedHeight;
    private int mCollapsedHeight;

    public interface OnToggleAnimationListener {
        void onStart(boolean expanded);

        void onFinish(boolean expanded);
    }

    public interface SearchListener {
        void onFinished(String searchKeyword);
    }

    public SearchViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnToggleAnimationListener(OnToggleAnimationListener listener) {
        mOnToggleAnimationListener = listener;
    }

    public void setSearchListener(SearchListener listener) {
        mSearchListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        mCollapsed = (ViewGroup) findViewById(R.id.search_box_collapsed);
        mSearchIcon = findViewById(R.id.search_magnifying_glass);
        mCollapsedSearchBox = findViewById(R.id.search_box_start_search);

        mExpanded = (ViewGroup) findViewById(R.id.search_expanded_root);
        mSearchEditText = (EditText) mExpanded.findViewById(R.id.search_expanded_edit_text);
        mBackButtonView = mExpanded.findViewById(R.id.search_expanded_back_button);
        mExpandedSearchIcon = findViewById(R.id.search_expanded_magnifying_glass);

        // Convert a long click into a click to expand the search box, and then long click on the
        // search view. This accelerates the long-press scenario for copy/paste.
        mCollapsedSearchBox.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mCollapsedSearchBox.performClick();
                mSearchEditText.performLongClick();
                return false;
            }
        });

        mCollapsed.setOnClickListener(mSearchViewOnClickListener);
        mSearchIcon.setOnClickListener(mSearchViewOnClickListener);
        mCollapsedSearchBox.setOnClickListener(mSearchViewOnClickListener);

        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.showInputMethod(v);
                } else {
                    Utils.hideInputMethod(v);
                }
            }
        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    callSearchListener();
                    Utils.hideInputMethod(v);
                    return true;
                }
                return false;
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSearchEditText.getText().length() > 0) {
                    Utils.fadeIn(mExpandedSearchIcon, ANIMATION_DURATION);
                } else {
                    Utils.fadeOut(mExpandedSearchIcon, ANIMATION_DURATION);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBackButtonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse();
            }
        });

        mExpandedSearchIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callSearchListener();
                Utils.hideInputMethod(v);
            }
        });
        this.mCollapsedDrawable = new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent));
        this.mExpandedDrawable = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.default_color_expanded));
        mBackgroundTransition = new TransitionDrawable(new Drawable[]{mCollapsedDrawable, mExpandedDrawable});
        mBackgroundTransition.setCrossFadeEnabled(true);
        setBackground(mBackgroundTransition);
        Utils.setPaddingAll(SearchViewLayout.this, 8);
        super.onFinishInflate();
    }

    private void callSearchListener() {
        Editable editable = mSearchEditText.getText();
        if (editable != null && editable.length() > 0) {
            if (mSearchListener != null) {
                mSearchListener.onFinished(editable.toString());
            }
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (mSearchEditTextLayoutListener != null) {
            if (mSearchEditTextLayoutListener.onKey(this, event.getKeyCode(), event)) {
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    /**
     * Open the search UI when the user clicks on the search box.
     */
    private final View.OnClickListener mSearchViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mIsExpanded) {
                expand(true);
            }
        }
    };

    /**
     * If the search term is empty and the user closes the soft keyboard, close the search UI.
     */
    private final View.OnKeyListener mSearchEditTextLayoutListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN &&
                    isExpanded()) {
                boolean keyboardHidden = Utils.hideInputMethod(v);
                if (keyboardHidden) return true;
                collapse();
                return true;
            }
            return false;
        }
    };

    public void setExpandedContentFragment(Activity activity, Fragment contentFragment) {
        mExpandedContentFragment = contentFragment;
        mFragmentManager = activity.getFragmentManager();
        mExpandedHeight = Utils.getSizeOfScreen(activity).y;
    }

    public void handleToolbarAnimation(Toolbar toolbar) {
        this.mToolbar = toolbar;
    }

    public void setTransitionDrawables(Drawable collapsedDrawable, Drawable expandedDrawable) {
        this.mCollapsedDrawable = collapsedDrawable;
        this.mExpandedDrawable = expandedDrawable;

        mBackgroundTransition = new TransitionDrawable(new Drawable[]{mCollapsedDrawable, mExpandedDrawable});
        mBackgroundTransition.setCrossFadeEnabled(true);
        setBackground(mBackgroundTransition);
        Utils.setPaddingAll(SearchViewLayout.this, 8);
    }

    public void expand(boolean requestFocus) {
        mCollapsedHeight = getHeight();
        toggleToolbar(true);
        if (mBackgroundTransition != null)
            mBackgroundTransition.startTransition(ANIMATION_DURATION);
        updateVisibility(true /* isExpand */);
        mIsExpanded = true;

        Utils.crossFadeViews(mExpanded, mCollapsed, ANIMATION_DURATION);
        mAnimator = ValueAnimator.ofFloat(1f, 0f);
        prepareAnimator(true);

        if (requestFocus) {
            mSearchEditText.requestFocus();
        }
    }

    public void collapse() {
        toggleToolbar(false);
        if (mBackgroundTransition != null)
            mBackgroundTransition.reverseTransition(ANIMATION_DURATION);
        mSearchEditText.setText(null);
        updateVisibility(false /* isExpand */);
        mIsExpanded = false;

        Utils.crossFadeViews(mCollapsed, mExpanded, ANIMATION_DURATION);
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        prepareAnimator(false);

        hideContentFragment();
    }

    private void showContentFragment() {
        final FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.replace(R.id.search_expanded_content, mExpandedContentFragment);
        mExpandedContentFragment.setHasOptionsMenu(false);
        transaction.commit();
    }

    private void hideContentFragment() {
        if (mFragmentManager == null) {
            Log.e(LOG_TAG, "Fragment Manager is null. Returning");
            return;
        }
        final FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.remove(mExpandedContentFragment).commit();
    }

    private void toggleToolbar(boolean expanding) {
        if (mToolbar == null) return;

        mToolbar.clearAnimation();
        if (expanding) {
            toolbarExpandedHeight = mToolbar.getHeight();
        }

        int toYValue = expanding ? toolbarExpandedHeight * (-1) : 0;

        mToolbar.animate()
                .y(toYValue)
                .setDuration(ANIMATION_DURATION)
                .start();

        Utils.animateHeight(
                mToolbar,
                expanding ? toolbarExpandedHeight : 0,
                expanding ? 0 : toolbarExpandedHeight,
                ANIMATION_DURATION);
    }

    /**
     * Updates the visibility of views depending on whether we will show the expanded or collapsed
     * search view. This helps prevent some jank with the crossfading if we are animating.
     *
     * @param isExpand Whether we are about to show the expanded search box.
     */
    private void updateVisibility(boolean isExpand) {
        int collapsedViewVisibility = isExpand ? View.GONE : View.VISIBLE;
        int expandedViewVisibility = isExpand ? View.VISIBLE : View.GONE;

        mSearchIcon.setVisibility(collapsedViewVisibility);
        mCollapsedSearchBox.setVisibility(collapsedViewVisibility);
        mBackButtonView.setVisibility(expandedViewVisibility);
    }

    private void prepareAnimator(final boolean expand) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (expand) {
                    showContentFragment();

                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.height = mExpandedHeight;
                    setLayoutParams(params);

                    Utils.setPaddingAll(SearchViewLayout.this, 0);
                } else {
                    Utils.setPaddingAll(SearchViewLayout.this, 8);
                }
                if (mOnToggleAnimationListener != null)
                    mOnToggleAnimationListener.onFinish(expand);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (!expand) {
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.height = mCollapsedHeight;
                    setLayoutParams(params);
                }
                if (mOnToggleAnimationListener != null)
                    mOnToggleAnimationListener.onStart(expand);
            }
        });

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });

        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    /**
     * Allow user to set a search icon in the un-expended view
     *
     * @param iconResource resource id of icon
     */
    public void setCollapsedIcon(@DrawableRes int iconResource) {
        ((ImageView)mSearchIcon).setImageResource(iconResource);

    }

    /**
     * Allow user to set a back icon in the expended view
     *
     * @param iconResource resource id of icon
     */
    public void setExpandedBackIcon(@DrawableRes int iconResource) {
        ((ImageView)mBackButtonView).setImageResource(iconResource);
    }

    /**
     * Allow user to set a search icon in the expended view
     *
     * @param iconResource resource id of icon
     */
    public void setExpandedSearchIcon(@DrawableRes int iconResource) {
        ((ImageView)mExpandedSearchIcon).setImageResource(iconResource);
    }

}