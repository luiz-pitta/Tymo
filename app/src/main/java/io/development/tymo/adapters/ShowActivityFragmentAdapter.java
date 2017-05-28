package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.WhatShowFragment;
import io.development.tymo.fragments.WhenShowFragment;
import io.development.tymo.fragments.WhoShowFragment;

public class ShowActivityFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"Feed", "Plans", "Wrong"};

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = WhatShowFragment.newInstance(TABS[position]);
                break;
            case 1:
                mFragment = WhenShowFragment.newInstance(TABS[position]);
                break;
            case 2:
                mFragment = WhoShowFragment.newInstance(TABS[position]);
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
