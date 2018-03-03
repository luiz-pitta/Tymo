package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.fragments.PlansFragment;
import io.development.tymo.fragments.ProfileFragment;
import io.development.tymo.fragments.SearchFragment;

public class MainFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"Feed_main", "Plans_main", "Wrong", "Profile", "Search"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = FeedFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = PlansFragment.newInstance(TABS[position]);
                break;
            case 2:
                mFragment = ProfileFragment.newInstance(TABS[position]);
                break;
            case 3:
                mFragment = SearchFragment.newInstance(TABS[position]);
                break;
        }
        return mFragment;
    }

    @Override
    public String getTag(int position) {
        return TABS[position];
    }

    @Override
    public int getCount() {
        return TABS.length;
    }
}
