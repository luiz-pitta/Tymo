package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.FeedCardFragment;
import io.development.tymo.fragments.FeedListFragment;


public class FeedFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"list", "card"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = FeedListFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = FeedCardFragment.newInstance(TABS[position]);
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
