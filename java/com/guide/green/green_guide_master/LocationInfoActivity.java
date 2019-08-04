package com.guide.green.green_guide_master;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.guide.green.green_guide_master.Fragments.GeneralInfoFragment;
import com.guide.green.green_guide_master.Fragments.ReviewsInfoFragment;
import com.guide.green.green_guide_master.Utilities.LocationInfoFetchReviews;
import com.guide.green.green_guide_master.Utilities.LocationInfoTabsPagerAdapter;

public class LocationInfoActivity extends AppCompatActivity implements
        GeneralInfoFragment.OnGeneralFragmentListener,
        ReviewsInfoFragment.OnReviewsFragmentListener {

    private String location;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.location_info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TabLayout tabs = (TabLayout) findViewById(R.id.location_info_tabs);

        ViewPager pager = (ViewPager) findViewById(R.id.location_info_pager);
        LocationInfoTabsPagerAdapter adapter =
                new LocationInfoTabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        tabs.setupWithViewPager(pager);

        location = getIntent().getStringExtra("location");
        longitude = getIntent().getDoubleExtra("longitude", 0);
        latitude = getIntent().getDoubleExtra("latitude", 0);

        getSupportActionBar().setTitle(location);

        loadInformation();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof GeneralInfoFragment) {
            GeneralInfoFragment frag = (GeneralInfoFragment) fragment;
            frag.setOnGeneralFragmentListener(this);
        } else if (fragment instanceof  ReviewsInfoFragment) {
            ReviewsInfoFragment frag = (ReviewsInfoFragment) fragment;
            frag.setOnReviewsFragmentListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadInformation() {
        LocationInfoFetchReviews fetchReviewHandler =
                new LocationInfoFetchReviews(this, location, longitude, latitude);
    }

    public void onGeneralFragmentInteraction () {

    }

    public void onReviewsFragmentInteraction () {

    }
}
