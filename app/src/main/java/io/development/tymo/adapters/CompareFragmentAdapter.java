package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.CompareFreeFragment;
import io.development.tymo.fragments.CompareTotalFragment;

public class CompareFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"total", "free2"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = CompareTotalFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = CompareFreeFragment.newInstance(TABS[position]);
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
