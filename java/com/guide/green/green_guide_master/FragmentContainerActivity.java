package com.guide.green.green_guide_master;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.guide.green.green_guide_master.Fragments.AboutFragment;
import com.guide.green.green_guide_master.Fragments.GuidelinesFragment;
import com.guide.green.green_guide_master.Fragments.LogInFragment;
import com.guide.green.green_guide_master.Fragments.MyReviewsFragment;
import com.guide.green.green_guide_master.Fragments.SignUpFragment;
import com.guide.green.green_guide_master.Fragments.UserGuideFragment;

public class FragmentContainerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        boolean openedFragment = false;
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            Object layoutId = extras.get("itemId");
            if (layoutId != null && layoutId instanceof Integer) {
                openedFragment = openFragment((Integer) layoutId);
            }
        }
        if (!openedFragment) {
            finish();
        }
    }

    private Fragment getFragmentFromId(int layoutId) {
        switch (layoutId) {
            case R.id.my_reviews: return new MyReviewsFragment();
            case R.id.guidelines: return new GuidelinesFragment();
            case R.id.about: return new AboutFragment();
            case R.id.user_guide: return new UserGuideFragment();
            case R.id.sign_up: return new SignUpFragment();
            //case R.id.log_in_out: return new LogInOutFragment();
            case R.id.view_one_review: return new ViewOneReviewFragment();
            case R.id.drawable_log_in_out: return new LogInFragment();
        }
        return null;
    }

    private boolean openFragment(int layoutId) {
        Fragment fragment = getFragmentFromId(layoutId);
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, fragment);
            transaction.commit();
            return true;
        }
        return false;
    }

    public static void startActivity(Activity act, int layoutId) {
        Intent intent = new Intent(act, FragmentContainerActivity.class);
        intent.putExtra("itemId", layoutId);
        act.startActivity(intent);
    }
}
