package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.fragments.WhatEditFragment;
import io.development.tymo.fragments.WhenEditFragment;
import io.development.tymo.fragments.WhoEditFragment;

public class AddActivityFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"Feed", "Plans", "Wrong"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = WhatEditFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = WhenEditFragment.newInstance(TABS[position]);
                break;
            case 2:
                mFragment = WhoEditFragment.newInstance(TABS[position]);
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
