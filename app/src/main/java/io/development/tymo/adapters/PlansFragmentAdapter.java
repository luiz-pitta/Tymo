package io.development.tymo.adapters;

import android.app.Fragment;

import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;

import io.development.tymo.fragments.CommitmentFragment;
import io.development.tymo.fragments.FreeFragment;
import io.development.tymo.fragments.FeedFragment;

public class PlansFragmentAdapter implements FragmentNavigatorAdapter {

    private static final String TABS[] = {"comit", "free"};
    private int screen;

    public PlansFragmentAdapter(int screen){
        this.screen = screen;
    }

    @Override
    public Fragment onCreateFragment(int position) {
        Fragment mFragment = null;
        switch (position){
            case 0:
                mFragment = CommitmentFragment.newInstance(screen);
                break;
            case 1:
                mFragment = FreeFragment.newInstance(screen);
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
