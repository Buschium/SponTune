package de.spontune.android.spontune;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

import de.spontune.android.spontune.Fragments.LoginFragment;
import de.spontune.android.spontune.Fragments.NonSwipeableViewPager;
import de.spontune.android.spontune.Fragments.PasswordResetFragment;
import de.spontune.android.spontune.Fragments.SignupFragment;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private View background;
    private View slogan;
    private View logo;

    private NonSwipeableViewPager viewPager;
    private PagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
    }


    public void setCurrentItem (int item) {
        viewPager.setCurrentItem(item, true);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(1);
        }
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 1: return new LoginFragment();
                case 0: return new PasswordResetFragment();
                default: return new SignupFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}