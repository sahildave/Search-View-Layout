Implementation of **Lollipop+ Dialer** and **Google Maps**.

#### Add in View
Add to your layout by using the include tag.
``` xml
<include layout="@layout/widget_search_bar"/>
```
---
#### API
This overlays the full activity and shows the **fragment** which you have assigned by using `setExpandedContentFragment`.

``` java
searchViewLayout.setExpandedContentFragment(this, new SearchStaticFragment());
```
---
If you want to **animate your Toolbar** too like the demo gif, you can enable it by using `handleToolbarAnimation`.

``` java
searchViewLayout.handleToolbarAnimation(toolbar);
```
---
Setting **Background colors for Transition**. Default should also work just fine:

``` java
// Create Drawable for collapsed state. Default color is android.R.color.transparent
ColorDrawable collapsed = new ColorDrawable(
    ContextCompat.getColor(this, R.color.colorPrimary));

// Create Drawable for expanded state. Default color is #F0F0F0
ColorDrawable expanded = new ColorDrawable(
    ContextCompat.getColor(this, R.color.default_color_expanded));

// Send both colors to searchViewLayout
searchViewLayout.setTransitionDrawables(collapsed, expanded);
```
---
**Listen to search** complete by:
``` java
searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
    @Override
    public void onFinished(String searchKeyword) {
        searchViewLayout.collapse();
        Snackbar.make(searchViewLayout, "Search Done - " + searchKeyword, Snackbar.LENGTH_LONG).show();
    }
});
```
---    
**Listen to collapse/expand animation** by using `setOnToggleAnimationListener`. For eg the FAB in demo hides on expanded and shows on collapse.
``` java
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
```
---
**Setting Hints**

If you want to set hints in the view, there are three APIs. `setCollapsedHint` would come up in the default/collapsed state. `setExpandedHint` would work for expanded state i.e. after click the view and the keyboard is up. `setHint` would set both the hints in one go, use this you want to show the same hint in both the states.

``` java
searchViewLayout.setCollapsedHint("Collapsed Hint");
searchViewLayout.setExpandedHint("Expanded Hint");
searchViewLayout.setHint("Global Hint");
```
**Setting Icons**
Use `setCollapsedIcon`, `setExpandedBackIcon`, `setExpandedSearchIcon` to setup icons according to your choice. The argument should be a DrawableRes
### NOTES

1. If you want to add a scrolling widget in your `setExpandedContentFragment`, add a `onTouchListener` and disallow the parent intercept by using`v.getParent().requestDisallowInterceptTouchEvent(true);`Check out fragments in sample for the implement of ListView, RecyclerView and ScrollView.

    ``` java
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
        // Setting on Touch Listener for handling the touch inside ScrollView
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Disallow the touch request for parent scroll on touch of child view
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        }
    });
    
    ```

2. The default height of the view is `120dp` which is also present in the dimens.xml file as
    ``` xml     
    <dimen name="search_view_layout_approx_height">120dp</dimen>
    ```
    You can use it for adding margin on top of your main content layout.
    

## DEMO

![Screenshot](/demo.gif?raw=true)

### GET

Available at jCenter and mavenCentral.

``` groovy
dependencies {
    compile 'xyz.sahildave:searchviewlayout:0.2'
}
```
    
### CHANGELOG

### 0.2
* Added APIs for setting icons
* Improved animations by using `onAnimationUpdate`
### 0.1
* Added hints API.
* Added `search_view_layout_approx_height` 
#### 0.0.2
* Added API `setTransitionDrawables` which solves crashes in < API 19

#### Contribute

Contribute by creating issues (tagged enhancement, bugs) in the repo or create a pull request.
