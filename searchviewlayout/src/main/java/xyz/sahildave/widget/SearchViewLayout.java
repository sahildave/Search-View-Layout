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
import android.widget.ImageView;
import android.widget.TextView;

public class SearchViewLayout extends FrameLayout {
    public static int ANIMATION_DURATION = 1500;
    private static final String LOG_TAG = SearchViewLayout.class.getSimpleName();

    private boolean mIsExpanded = false;

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
    private TextView mCollapsedHintView;

    /***
     * Interface for listening to animation start and finish.
     * expanding and expanded tell the current state of animation.
     */
    public interface OnToggleAnimationListener {
        void onStart(boolean expanding);

        void onFinish(boolean expanded);
    }

    /***
     * Interface for listening to search finish call.
     * Called on clicking of search button on keyboard and {@link #mExpandedSearchIcon}
     */

    public interface SearchListener {
        void onFinished(String searchKeyword);
    }

    public void setOnToggleAnimationListener(OnToggleAnimationListener listener) {
        mOnToggleAnimationListener = listener;
    }

    public void setSearchListener(SearchListener listener) {
        mSearchListener = listener;
    }

    public SearchViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        ANIMATION_DURATION = context.getResources().getInteger(R.integer.animation_duration);
    }

    @Override
    protected void onFinishInflate() {
        mCollapsed = (ViewGroup) findViewById(R.id.search_box_collapsed);
        mSearchIcon = findViewById(R.id.search_magnifying_glass);
        mCollapsedSearchBox = findViewById(R.id.search_box_start_search);
        mCollapsedHintView = (TextView) findViewById(R.id.search_box_collapsed_hint);

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

        mCollapsedDrawable = new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent));
        mExpandedDrawable = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.default_color_expanded));
        mBackgroundTransition = new TransitionDrawable(new Drawable[]{mCollapsedDrawable, mExpandedDrawable});
        mBackgroundTransition.setCrossFadeEnabled(true);

        setBackground(mBackgroundTransition);
        Utils.setPaddingAll(SearchViewLayout.this, 8);

        super.onFinishInflate();
    }

    /***
     * Should toolbar be animated, y position.
     * @param toolbar current toolbar which needs to be animated.
     */

    public void handleToolbarAnimation(Toolbar toolbar) {
        this.mToolbar = toolbar;
    }

    /***
     * Set the fragment which would be shown in the expanded state
     * @param activity to get fragment manager
     * @param contentFragment fragment which needs to be shown.
     */

    public void setExpandedContentFragment(Activity activity, Fragment contentFragment) {
        mExpandedContentFragment = contentFragment;
        mFragmentManager = activity.getFragmentManager();
        mExpandedHeight = Utils.getSizeOfScreen(activity).y;
    }

    /***
     * Set the background colours of the searchview.
     * @param collapsedDrawable drawable for collapsed state, default transparent
     * @param expandedDrawable drawable for expanded state, default color.default_color_expanded
     */
    public void setTransitionDrawables(Drawable collapsedDrawable, Drawable expandedDrawable) {
        this.mCollapsedDrawable = collapsedDrawable;
        this.mExpandedDrawable = expandedDrawable;

        mBackgroundTransition = new TransitionDrawable(new Drawable[]{mCollapsedDrawable, mExpandedDrawable});
        mBackgroundTransition.setCrossFadeEnabled(true);
        setBackground(mBackgroundTransition);
        Utils.setPaddingAll(SearchViewLayout.this, 8);
    }

    /***
     * Set hint in the collapsed state
     *
     * Also see {@link #setHint(String)}
     * @param searchViewHint
     */
    public void setCollapsedHint(String searchViewHint) {
        if (searchViewHint != null) {
            mCollapsedHintView.setHint(searchViewHint);
        }
    }

    /***
     * Set hint in the expanded state
     *
     * Also see {@link #setHint(String)}
     * @param searchViewHint
     */
    public void setExpandedHint(String searchViewHint) {
        if (searchViewHint != null) {
            mSearchEditText.setHint(searchViewHint);
        }
    }

    /***
     * Overrides both, {@link #setCollapsedHint(String)} and {@link #setExpandedHint(String)},
     * and sets hint for both the views.
     *
     * Use this if you don't want to show different hints in both the states
     * @param searchViewHint
     */
    public void setHint(String searchViewHint) {
        if (searchViewHint != null) {
            mCollapsedHintView.setHint(searchViewHint);
            mSearchEditText.setHint(searchViewHint);
        }
    }

    public void expand(boolean requestFocus) {
        mCollapsedHeight = getHeight();
        toggleToolbar(true);
        if (mBackgroundTransition != null)
            mBackgroundTransition.startTransition(ANIMATION_DURATION);
        mIsExpanded = true;

        animateStates(true, 1f, 0f);
        Utils.crossFadeViews(mExpanded, mCollapsed, ANIMATION_DURATION);

        if (requestFocus) {
            mSearchEditText.requestFocus();
        }
    }

    public void collapse() {
        toggleToolbar(false);
        if (mBackgroundTransition != null)
            mBackgroundTransition.reverseTransition(ANIMATION_DURATION);
        mSearchEditText.setText(null);
        mIsExpanded = false;

        animateStates(false, 0f, 1f);
        Utils.crossFadeViews(mCollapsed, mExpanded, ANIMATION_DURATION);

        hideContentFragment();
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    /**
     * Allow user to set a search icon in the collapsed view
     *
     * @param iconResource resource id of icon
     */
    public void setCollapsedIcon(@DrawableRes int iconResource) {
        ((ImageView)mSearchIcon).setImageResource(iconResource);

    }

    /**
     * Allow user to set a back icon in the expanded view
     *
     * @param iconResource resource id of icon
     */
    public void setExpandedBackIcon(@DrawableRes int iconResource) {
        ((ImageView)mBackButtonView).setImageResource(iconResource);
    }

    /**
     * Allow user to set a search icon in the expanded view
     *
     * @param iconResource resource id of icon
     */
    public void setExpandedSearchIcon(@DrawableRes int iconResource) {
        ((ImageView)mExpandedSearchIcon).setImageResource(iconResource);
    }

    private void showContentFragment() {
        final FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
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

    private void animateStates(final boolean expand, final float start, final float end) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.cancel();

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (expand) {
                    Utils.setPaddingAll(SearchViewLayout.this, 0);
                    showContentFragment();

                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.height = mExpandedHeight;
                    setLayoutParams(params);
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
                int padding = (int) (8 * animation.getAnimatedFraction());
                if (expand) padding = 8 - padding;
                Utils.setPaddingAll(SearchViewLayout.this, padding);
            }
        });

        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
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

}