Implementation of **Lollipop+ Dialer** and **Google Maps**.

**Add** to your layout by

    <include layout="@layout/widget_search_bar"/>
---

This overlays the full activity and shows the **fragment** which you have assigned by using `setExpandedContentFragment`.

    searchViewLayout.setExpandedContentFragment(this, new SearchStaticFragment());
---
If you want to **animate your Toolbar** too like the demo gif, you can enable it by using `handleToolbarAnimation`.

    searchViewLayout.handleToolbarAnimation(toolbar);
---
Setting **Background colors for Transition**. Default should also work just fine:

    // Create Drawable for collapsed state. Default color is android.R.color.transparent
    ColorDrawable collapsed = new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary));
    
    // Create Drawable for expanded state. Default color is #F0F0F0
    ColorDrawable expanded = new ColorDrawable(ContextCompat.getColor(this, R.color.default_color_expanded));
    
    // Send both colors to searchViewLayout
    searchViewLayout.setTransitionDrawables(collapsed, expanded);
---
**Listen to search** complete by:

    searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
        @Override
        public void onFinished(String searchKeyword) {
            searchViewLayout.collapse();
            Snackbar.make(searchViewLayout, "Search Done - " + searchKeyword, Snackbar.LENGTH_LONG).show();
        }
    });
    
**Listen to collapse/expand animation** by using `setOnToggleAnimationListener`. For eg the FAB in demo hides on expanded and shows on collapse.

    searchViewLayout.setOnToggleAnimationListener(new SearchViewLayout.OnToggleAnimationListener() {
        @Override
        public void onStart(boolean expanded) {
            if(expanded) {
                fab.hide();
            } else {
                fab.show();
            }
        }

        @Override
        public void onFinish(boolean expanded) { }
    });

## DEMO

![Screenshot](/demo.gif?raw=true)

### GET

Available at jCenter and mavenCentral.

    dependencies {
        compile 'xyz.sahildave:searchviewlayout:0.0.2'
    }
    
### CHANGELOG

#### 0.0.2

* Added API `setTransitionDrawables`

### TODO

* API for setting icons
* Granular setPadding using `onAnimationUpdate`

#### Contribute

Contribute by creating issues (tagged enhancement, bugs) in the repo or create a pull request.
