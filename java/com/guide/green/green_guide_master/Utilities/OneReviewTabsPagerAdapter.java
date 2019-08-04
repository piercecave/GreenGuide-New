package com.guide.green.green_guide_master.Utilities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.guide.green.green_guide_master.Fragments.AirReviewFragment;
import com.guide.green.green_guide_master.Fragments.GeneralReviewFragment;
import com.guide.green.green_guide_master.Fragments.WasteReviewFragment;
import com.guide.green.green_guide_master.Fragments.WaterReviewFragment;

public class OneReviewTabsPagerAdapter extends FragmentPagerAdapter {

    public OneReviewTabsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return GeneralReviewFragment.newInstance();
            case 1: return WaterReviewFragment.newInstance();
            case 2: return AirReviewFragment.newInstance();
            case 3: return WasteReviewFragment.newInstance();
            default: return  GeneralReviewFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "General";
            case 1: return "Water";
            case 2: return "Air";
            case 3: return "Waste";
            default: return "";
        }
    }


}
