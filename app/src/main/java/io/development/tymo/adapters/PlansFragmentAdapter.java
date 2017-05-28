package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.CommitmentFragment;
import io.development.tymo.fragments.FreeFragment;

public class PlansFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"comit", "free"};

    public PlansFragmentAdapter(){
    }

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = CommitmentFragment.newInstance();
                break;
            case 1:
                mFragment = FreeFragment.newInstance();
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
