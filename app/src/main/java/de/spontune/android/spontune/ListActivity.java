package de.spontune.android.spontune;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import de.spontune.android.spontune.Fragments.EventFragment;
import de.spontune.android.spontune.Fragments.TodayFragment;
import de.spontune.android.spontune.Fragments.TomorrowFragment;
import de.spontune.android.spontune.Fragments.WeekFragment;

public class ListActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{

    private ViewPager mViewPager;

    private ImageButton mButtonCreative;
    private ImageButton mButtonParty;
    private ImageButton mButtonHappening;
    private ImageButton mButtonSports;

    private boolean mCreativeActivated = false;
    private boolean mPartyActivated = false;
    private boolean mHappeningActivated = false;
    private boolean mSportsActivated = false;

    private EventFragment todayFragment;
    private EventFragment tomorrowFragment;
    private EventFragment weekFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.END);
        getWindow().setExitTransition(slide);
        slide.setSlideEdge(Gravity.START);
        getWindow().setEnterTransition(slide);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        EventPagerAdapter mEventPagerAdapter = new EventPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mEventPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);    //to prevent the ViewPager from having to reload fragments

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setUpCategoryButtons();
    }

    /**
     * Sets up the logic for the category buttons on the bottom of the screen.
     * If all categories are deactivated (un-selected), the system acts like all categories are selected (for logic purposes).
     * By default, all categories are deactivated.
     */
    private void setUpCategoryButtons(){

        mButtonCreative = findViewById(R.id.action_category_food_and_drink);
        mButtonParty = findViewById(R.id.action_category_party);
        mButtonHappening = findViewById(R.id.action_category_music);
        mButtonSports = findViewById(R.id.action_category_sports);

        mButtonCreative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreativeActivated = !mCreativeActivated;
                /*If all categories are selected after the button is pressed, all categories have to be set
                 *to false to keep the logic consistent.
                 */
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateList(mViewPager.getCurrentItem(), 1, mCreativeActivated);
            }
        });

        mButtonParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPartyActivated = !mPartyActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateList(mViewPager.getCurrentItem(), 2, mPartyActivated);
            }
        });

        mButtonHappening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHappeningActivated = !mHappeningActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateList(mViewPager.getCurrentItem(), 3, mHappeningActivated);
            }
        });

        mButtonSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSportsActivated = ! mSportsActivated;
                if(everyCategoryActivated()) setAllButtonsFalse();
                toggleButtonsGreyed();
                updateList(mViewPager.getCurrentItem(), 4, mSportsActivated);
            }
        });

    }


    private void updateList(int position, int category, boolean selected){
        switch(position){
            case 0:
                todayFragment.onCategorySelected(mCreativeActivated, mPartyActivated, mHappeningActivated, mSportsActivated);
                break;
            case 1:
                todayFragment.onCategorySelected(mCreativeActivated, mPartyActivated, mHappeningActivated, mSportsActivated);
                break;
            default:
                todayFragment.onCategorySelected(mCreativeActivated, mPartyActivated, mHappeningActivated, mSportsActivated);
                break;
        }
    }


    /**
     * Greys out category buttons based on which categories are selected.
     */
    private void toggleButtonsGreyed(){
        Drawable foodAndDrinkIcon = this.getResources().getDrawable(R.drawable.category_creative_light);
        Drawable partyIcon = this.getResources().getDrawable(R.drawable.category_party_light);
        Drawable musicIcon = this.getResources().getDrawable(R.drawable.category_happening_light);
        Drawable sportsIcon = this.getResources().getDrawable(R.drawable.category_sports_light);
        Drawable foodAndDrinkIconDeactivated = this.getResources().getDrawable(R.drawable.category_creative_deactivated);
        Drawable partyIconDeactivated = this.getResources().getDrawable(R.drawable.category_party_deactivated);
        Drawable musicIconDeactivated = this.getResources().getDrawable(R.drawable.category_happening_deactivated);
        Drawable sportsIconDeactivated = this.getResources().getDrawable(R.drawable.category_sports_deactivated);
        if(noCategoryActivated()){
            mButtonCreative.setImageDrawable(foodAndDrinkIcon);
            mButtonParty.setImageDrawable(partyIcon);
            mButtonHappening.setImageDrawable(musicIcon);
            mButtonSports.setImageDrawable(sportsIcon);
        }else{
            Drawable newIcon = mCreativeActivated ? foodAndDrinkIcon : foodAndDrinkIconDeactivated;
            mButtonCreative.setImageDrawable(newIcon);

            newIcon = mPartyActivated ? partyIcon : partyIconDeactivated;
            mButtonParty.setImageDrawable(newIcon);

            newIcon = mHappeningActivated ? musicIcon : musicIconDeactivated;
            mButtonHappening.setImageDrawable(newIcon);

            newIcon = mSportsActivated ? sportsIcon : sportsIconDeactivated;
            mButtonSports.setImageDrawable(newIcon);
        }

    }


    /**
     * Checks whether no category is selected.
     * @return true if no category is selected
     */
    private boolean noCategoryActivated(){
        return (!mCreativeActivated && !mPartyActivated && !mHappeningActivated && !mSportsActivated);
    }


    /**
     * Checks whether all categories are selected.
     * @return true if all categories are selected
     */
    private boolean everyCategoryActivated(){
        return (mCreativeActivated && mPartyActivated && mHappeningActivated && mSportsActivated);
    }


    /**
     * Un-selects all categories and thus puts category system back in idle mode.
     */
    private void setAllButtonsFalse(){
        mCreativeActivated = false;
        mPartyActivated = false;
        mHappeningActivated = false;
        mSportsActivated = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }




    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class EventPagerAdapter extends FragmentPagerAdapter {

        public EventPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    todayFragment = new TodayFragment();
                    return todayFragment;
                case 1:
                    tomorrowFragment = new TomorrowFragment();
                    return tomorrowFragment;
                default:
                    weekFragment = new WeekFragment();
                    return weekFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.today);
                case 1:
                    return getString(R.string.tomorrow);
                case 2:
                    return getString(R.string.week);
            }
            return null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        }
        return false;
    }
}
