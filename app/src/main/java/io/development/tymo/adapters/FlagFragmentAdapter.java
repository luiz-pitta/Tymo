package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.fragments.FlagEditFragment;
import io.development.tymo.fragments.FlagShowFragment;


public class FlagFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"Show", "Edit"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = FlagEditFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = FlagShowFragment.newInstance(TABS[position]);
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
