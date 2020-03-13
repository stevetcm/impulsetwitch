package com.orangemuffin.impulse.tabmanagers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.orangemuffin.impulse.R;
import com.orangemuffin.impulse.fragments.BrowseGamesFragment;
import com.orangemuffin.impulse.fragments.ClipsFragment;
import com.orangemuffin.impulse.fragments.FeaturedFragment;
import com.orangemuffin.impulse.fragments.FollowedFragment;
import com.orangemuffin.impulse.fragments.GameStreamsFragment;
import com.orangemuffin.impulse.fragments.LiveFragment;
import com.orangemuffin.impulse.fragments.AllVideosFragment;
import com.orangemuffin.impulse.fragments.RecentsFragment;
import com.orangemuffin.impulse.fragments.TopFragment;

import java.util.ArrayList;
import java.util.List;

/* Created by OrangeMuffin on 2018-03-17 */
 public class MainTabManager extends Fragment {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_manager_main_layout, null);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        SmartTabLayout tabLayout = (SmartTabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setViewPager(viewPager);

        //endless effort to remove indicator when using a single tab page
        if (viewPagerAdapter.getCount() == 1) {
            tabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getContext(), R.color.transparent));
        }

        return rootView;
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        String fragment_type = getArguments().getString("fragment_type");
        if (fragment_type.equals("Featured")) {
            viewPagerAdapter.addFragment(new FeaturedFragment(), "Featured Streams");
        } else if (fragment_type.equals("Top")) {
            viewPagerAdapter.addFragment(new TopFragment(), "Top Streams");
        } else if (fragment_type.equals("Live")) {
            viewPagerAdapter.addFragment(new LiveFragment(), "Live Followed Streams");
            viewPagerAdapter.addFragment(new RecentsFragment(), "Recent Videos");
        } else if (fragment_type.equals("Followed")) {
            viewPagerAdapter.addFragment(new FollowedFragment(), "Followed Channels");
        } else if (fragment_type.equals("Browse Games")) {
            viewPagerAdapter.addFragment(new BrowseGamesFragment(), "Browse Games");
        } else if (fragment_type.equals("Channel Profile")) {
            Bundle bundle = new Bundle();
            bundle.putString("channelId", getArguments().getString("channelId"));
            bundle.putString("channelName", getArguments().getString("channelName"));
            bundle.putString("display_name", getArguments().getString("display_name"));
            bundle.putString("logoUrl", getArguments().getString("logoUrl"));

            AllVideosFragment allVideosFragment = new AllVideosFragment();
            allVideosFragment.setArguments(bundle);

            ClipsFragment clipsFragment = new ClipsFragment();
            clipsFragment.setArguments(bundle);

            viewPagerAdapter.addFragment(allVideosFragment, "All Videos");
            viewPagerAdapter.addFragment(clipsFragment, "Clips");
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("gameName", fragment_type);

            GameStreamsFragment gameStreamsFragment = new GameStreamsFragment();
            gameStreamsFragment.setArguments(bundle);

            viewPagerAdapter.addFragment(gameStreamsFragment, fragment_type);
        }

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
    }
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void refreshRecyclerView() {
        Fragment currentFragment = viewPagerAdapter.getItem(viewPager.getCurrentItem());
        if (currentFragment instanceof BrowseGamesFragment) {
            ((BrowseGamesFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof AllVideosFragment) {
            ((AllVideosFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof FeaturedFragment) {
            ((FeaturedFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof FollowedFragment) {
            ((FollowedFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof GameStreamsFragment) {
            ((GameStreamsFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof LiveFragment) {
            ((LiveFragment) currentFragment).refreshRecyclerView();
        } else if (currentFragment instanceof ClipsFragment) {
            ((ClipsFragment) currentFragment).refreshRecyclerView();
        }
    }

    public void recreateFragment() {
        Fragment currentFragment = viewPagerAdapter.getItem(viewPager.getCurrentItem());
        if (currentFragment instanceof BrowseGamesFragment) {
            ((BrowseGamesFragment) currentFragment).recreateFragment();
        } else if (currentFragment instanceof AllVideosFragment) {
            ((AllVideosFragment) currentFragment).recreateFragment();

            Fragment nextFragment = viewPagerAdapter.getItem(viewPager.getCurrentItem()+1);
            ((ClipsFragment) nextFragment).recreateFragment();
        } else if (currentFragment instanceof FeaturedFragment) {
            ((FeaturedFragment) currentFragment).recreateFragment();
        } else if (currentFragment instanceof FollowedFragment) {
            ((FollowedFragment) currentFragment).recreateFragment();
        } else if (currentFragment instanceof GameStreamsFragment) {
            ((GameStreamsFragment) currentFragment).recreateFragment();
        } else if (currentFragment instanceof LiveFragment) {
            ((LiveFragment) currentFragment).recreateFragment();
        } else if (currentFragment instanceof ClipsFragment) {
            Fragment previousFragment = viewPagerAdapter.getItem(viewPager.getCurrentItem()-1);
            ((AllVideosFragment) previousFragment).recreateFragment();

            ((ClipsFragment) currentFragment).recreateFragment();
        }
    }
}
