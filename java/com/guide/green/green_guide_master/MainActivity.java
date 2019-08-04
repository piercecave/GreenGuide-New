package com.guide.green.green_guide_master;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.guide.green.green_guide_master.Dialogs.CityPickerDialog;
import com.guide.green.green_guide_master.Dialogs.CityPickerDialog.OnCitySelectedListener;
import com.guide.green.green_guide_master.Utilities.BaiduMapManager;
import com.guide.green.green_guide_master.Utilities.BaiduSuggestion;
import com.guide.green.green_guide_master.Utilities.BottomSheetManager;
import com.guide.green.green_guide_master.Utilities.CredentialManager;
import com.guide.green.green_guide_master.Utilities.DBReviewSearchManager;
import com.guide.green.green_guide_master.Utilities.FetchReviews;
import com.guide.green.green_guide_master.Utilities.Review;
import com.guide.green.green_guide_master.Utilities.RomanizedLocation;
import com.guide.green.green_guide_master.Utilities.SuggestionSearchManager;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnCitySelectedListener,
        NavigationView.OnNavigationItemSelectedListener, SuggestionSearchManager.DrawerController,
        CredentialManager.OnLoginStateChanged, LocationPreview.onLocationPreviewListener {
    // Main managers
    private BaiduMapManager mMapManager;
    private BottomSheetManager mBtmSheetManager;
    private SuggestionSearchManager mSearchManager;
    private Button mCitySelectionView = null;

    // Terrain selection
    private FloatingActionButton normalMapView;
    private FloatingActionButton satelliteMapView;
    private boolean fabIsOpen = false;

    // Toggle between hamburger icon and back arrow
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mActionBarToggle;
    private boolean mBackButtonDisplaied = false;

    // Logout & login state handler
    private MenuItem mLoginOut;

    // Container for fragments
    private FrameLayout previewFragmentContainer;
    private ArrayList<Review> currentReviews;

    // Image View
    private TextView mDrawerUsername;
    private ImageView mDrawerImage;

    private BaiduSuggestion.Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        findViewById(R.id.writeReview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WriteReviewActivity.open(MainActivity.this);
            }
        });

        initMapManager();
        initBottomSheet();
        initToolsAndWidgets();
        initLocationTracker();
        CredentialManager.addLoginStateChangedListener(this);
        CredentialManager.initialize(this);



        final AppCompatActivity mAct = this;

        previewFragmentContainer = findViewById(R.id.preview_fragment_container);

        // Start the process of showing our location preview
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        LocationPreview newPreviewFragment = LocationPreview.newInstance();

        // Replace the contents of the container with the new LocationPreview
        ft.replace(R.id.preview_fragment_container, newPreviewFragment);

        ft.commit();

        FrameLayout fragContainer = findViewById(R.id.preview_fragment_container);

        fragContainer.setVisibility(View.INVISIBLE);

        mMapManager.setOnLocationClickListener(new BaiduMapManager.OnLocationClickListener() {
            @Override
            public void onLocationClick(BaiduSuggestion.Location location) {
                currentLocation = location;
                mMapManager.moveTo(location.point);
                //mBtmSheetManager.getReviewFor(location);

                FetchReviews fetchReviews = new FetchReviews(mAct, location);

                /*fetchReviews.

                // Start the process of showing our location preview
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                LocationPreview newPreviewFragment = LocationPreview.newInstance();

                // Replace the contents of the container with the new LocationPreview
                ft.replace(R.id.preview_fragment_container, newPreviewFragment);

                ft.commit();*/

            }
        });

        // This will cause the bottom sheet to disappear when the user clicks on the map
        mMapManager.baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                hidePreview();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                hidePreview();
                return false;
            }
        });

        findViewById(R.id.doSomething).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStuff(view);
            }
        });
    }

    /**
     * Insure that the icon on the toolbar is correct.
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarToggle.syncState();
    }

    public void initMapManager() {
        mMapManager = new BaiduMapManager((MapView) findViewById(R.id.map));
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    public void initLocationTracker() {
        FloatingActionButton btnMyLocation = findViewById(R.id.myLocation);

        TrackLocationHandler locationHandler =
                new TrackLocationHandler(this, btnMyLocation, mMapManager.baiduMap);

        btnMyLocation.setOnClickListener(locationHandler);
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    private void initBottomSheet() {
        // Get Relevant Bottom Sheet Views
        BottomSheetManager.Reviews reviews = new BottomSheetManager.Reviews();

        reviews.container = findViewById(R.id.btmSheetReviewsContainer);
        reviews.writeReviewButton = findViewById(R.id.btmSheetWriteReview);

        reviews.peekBar.companyName = findViewById(R.id.previewCompanyName);
        reviews.peekBar.ratingValue = findViewById(R.id.btmSheetRatingValue);
        reviews.peekBar.ratingImage = findViewById(R.id.btmSheetRatingImage);
        reviews.peekBar.ratingCount = findViewById(R.id.btmSheetRatingsCount);

        reviews.body.container = findViewById(R.id.btmSheetReviewBody);
        reviews.body.address = findViewById(R.id.btmSheetAddress);
        reviews.body.city = findViewById(R.id.btmSheetCityName);
        reviews.body.industry = findViewById(R.id.btmSheetIndustry);
        reviews.body.product = findViewById(R.id.btmSheetProduct);
        reviews.body.histogram = findViewById(R.id.btmSheetHistogram);
        reviews.body.reviews = findViewById(R.id.userReviewList);

        ViewGroup container = findViewById(R.id.db_search_results_container);
        ViewGroup childContainer = findViewById(R.id.db_search_results);
        DBReviewSearchManager dbSearchMgr = new DBReviewSearchManager(container, childContainer);

        // Initialize Bottom Sheet Manager
        NestedScrollView btmSheet = findViewById(R.id.btmSheet);

        mBtmSheetManager = new BottomSheetManager(this, btmSheet, reviews, dbSearchMgr,
                mMapManager);
        mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);

        CoordinatorLayout btmSheetContainer = findViewById(R.id.bottom_sheet_container);

        CoordinatorLayout.LayoutParams btmSheetLP = (CoordinatorLayout.LayoutParams) btmSheetContainer.getLayoutParams();
        //btmSheetLP.height = findViewById(R.id.drawer_layout).getHeight() * 2 / 3;
        btmSheetLP.topMargin = 100;
        btmSheetContainer.setLayoutParams(btmSheetLP);
    }

    /**
     * Initializes the tool bar and the floating buttons.
     */
    private void initToolsAndWidgets() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*CoordinatorLayout bottomSheetContainer = findViewById(R.id.bottom_sheet_container);
        int toolbarHeight = toolbar.getLayoutParams().height;
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheetContainer.getLayoutParams();
        layoutParams.topMargin = toolbarHeight;
        bottomSheetContainer.setLayoutParams(layoutParams);*/

        normalMapView = findViewById(R.id.normalfab);
        satelliteMapView = findViewById(R.id.satellitefab);
        FloatingActionButton fab = findViewById(R.id.fab);

        mDrawer = findViewById(R.id.drawer_layout);
        mActionBarToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mActionBarToggle);
        mActionBarToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mLoginOut = navigationView.getMenu().findItem(R.id.drawable_log_in_out);

        mDrawerUsername = navigationView.getHeaderView(0).findViewById(R.id.drawer_user_name);
        mDrawerImage = navigationView.getHeaderView(0).findViewById(R.id.drawer_user_picture);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapTypeFab();
            }
        });
    }

    private void hideMapTypeFabItems() {
        normalMapView.hide();
        satelliteMapView.hide();
        normalMapView.setClickable(false);
        satelliteMapView.setClickable(false);
        fabIsOpen = false;
    }

    private void showMapTypeFabItems() {
        normalMapView.show();
        satelliteMapView.show();
        normalMapView.setClickable(true);
        satelliteMapView.setClickable(true);
        fabIsOpen = true;
    }

    private void toggleMapTypeFab() {
        if (fabIsOpen) {
            hideMapTypeFabItems();
        } else {
            showMapTypeFabItems();
            normalMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.NORMAL);
                    hideMapTypeFabItems();
                }
            });
            satelliteMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.SATELLITE);
                    hideMapTypeFabItems();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (mBtmSheetManager.getBottomSheetState() != BottomSheetBehavior.STATE_HIDDEN) {
            if (mBtmSheetManager.getBottomSheetState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                mBtmSheetManager.removeMarkers();
                if (mSearchManager.hasText()) {
                    mSearchManager.showDropDownOverlay();
                }
            } else {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        } else if (mSearchManager.isDropDownOverlayShowing()) {
            mSearchManager.onBackButtonClick();
        } else if (mSearchManager.hasText()) {
            mSearchManager.showDropDownOverlay();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.searchItem);

        View root = menuItem.getActionView().getRootView();
        ViewGroup.LayoutParams lp = root.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        root.setLayoutParams(lp);

        mCitySelectionView = menuItem.getActionView().findViewById(R.id.city);
        mCitySelectionView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                CityPickerDialog cityPickerDialog = new CityPickerDialog();
                cityPickerDialog.setOnCitySelectedListener(MainActivity.this);
                cityPickerDialog.show(getSupportFragmentManager(), "City Picker");
            }
        });

        EditText searchInput = menuItem.getActionView().findViewById(R.id.searchInput);
        RecyclerView searchDropDown = findViewById(R.id.searchDropDown);

        ViewGroup mapViewContainer = findViewById(R.id.mapViewContainer);
        ViewGroup searchDropDownContainer = findViewById(R.id.searchDropDownContainer);

        /* Not saved Intentionally */
        mSearchManager = new SuggestionSearchManager(this, searchInput, searchDropDown,
                mCitySelectionView, mMapManager, mBtmSheetManager, mapViewContainer,
                searchDropDownContainer, this);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemId = item.getItemId();
        if (itemId == R.id.drawable_log_in_out) {
            LogInOutSignUpActivity.startActivity(this);
        } else {
            FragmentContainerActivity.startActivity(this, itemId);
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapManager.onDestroy();
    }

    @Override
    public void onCitySelected(RomanizedLocation city) {
        mCitySelectionView.setText(city.name);
    }

    @Override
    public void showBackButton(boolean enabled) {
        if (mBackButtonDisplaied == enabled) return;
        if (enabled) {
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START);
            }
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mActionBarToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActionBarToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSearchManager.onBackButtonClick();
                }
            });
        } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActionBarToggle.setDrawerIndicatorEnabled(true);
            mActionBarToggle.setToolbarNavigationClickListener(null);
        }
        mBackButtonDisplaied = enabled;
    }

    /* Temporary Method For Testing Things */
    public void doStuff(View view) {

    }

    @Override
    public void onLoginStateChanged(boolean isLoggedIn) {
        if (isLoggedIn) {
            mDrawerUsername.setText(CredentialManager.getUsername());
            mLoginOut.setTitle(getResources().getString(R.string.log_out_text));
        } else {
            mDrawerUsername.setText("");
            mLoginOut.setTitle(getResources().getString(R.string.log_in_text));
        }
    }

    public void setCurrentReviews() {

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof LocationPreview) {
            LocationPreview lp = (LocationPreview) fragment;
            lp.setOnLocationPreviewListener(this);
        }
    }

    public void onPreviewFragmentClicked() {
        Log.d("MAIN", "Clicked");
        Intent intent = new Intent(this, LocationInfoActivity.class);
        intent.putExtra("location", currentLocation.name);
        intent.putExtra("longitude", currentLocation.point.longitude);
        intent.putExtra("latitude", currentLocation.point.latitude);
        startActivity(intent);
    }

    public void onPreviewFragmentCreated() {

        final AppCompatActivity mAct = this;

        LinearLayout fragContainer = findViewById(R.id.FragmentLayout);

        fragContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mAct, LocationInfoActivity.class);
                intent.putExtra("location", currentLocation.name);
                intent.putExtra("longitude", currentLocation.point.longitude);
                intent.putExtra("latitude", currentLocation.point.latitude);
                mAct.startActivity(intent);
            }
        });
    }

    public void showPreviewFragmentForLocation(BaiduSuggestion.Location location) {
        currentLocation = location;
        mMapManager.moveTo(location.point);

        FetchReviews fetchReviews = new FetchReviews(this, location);

    }

    public void setCurrentLocation(BaiduSuggestion.Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void hidePreview() {
        findViewById(R.id.preview_fragment_container).setVisibility(View.GONE);
        currentLocation = null;
    }
}