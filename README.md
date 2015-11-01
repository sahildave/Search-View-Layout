Implementation of <b>Lollipop+ Dialer</b> and <b>Google Maps.</b>

Added to your layout by

    <include layout="@layout/widget_search_bar"/>

This overlays the full activity and shows the fragment which you have assigned by using `setExpandedContentFragment`.

    searchViewLayout.setExpandedContentFragment(this, new SearchStaticFragment());

If you want to animate your Toolbar too like the demo gif, you can enable it by using `handleToolbarAnimation`.

    searchViewLayout.handleToolbarAnimation(toolbar);

Listen to search complete by:

    searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
        @Override
        public void onFinished(String searchKeyword) {
            searchViewLayout.collapse();
            Snackbar.make(searchViewLayout, "Search Done - " + searchKeyword, Snackbar.LENGTH_LONG).show();
        }
    });
    
Listen to layout collapse/expand by using `setOnToggleVisibilityListener`. For eg the FAB in demo hides on expanded and shows on collapse.

    searchViewLayout.setOnToggleVisibilityListener(new SearchViewLayout.OnToggleVisibilityListener() {
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

### TODO

* Push to maven
* Make view transition background programatically by asking for startColor and endColor
* Add a "Search" button in the expanded form. Currently, relying on keyboard.
* API for settings icons
* Granular setPadding using `onAnimationUpdate`

#### Contribute

Contribute by creating issues (tagged enhancement, bugs) in the repo or create a pull request.
