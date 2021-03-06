package io.development.tymo.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private boolean showTitle = true;

    public ViewPagerAdapter(FragmentManager manager, boolean showTitle) {
        super(manager);
        this.showTitle = showTitle;
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

    public void setPageTitle(int position, String title) {
        mFragmentTitleList.remove(position);
        mFragmentTitleList.add(position, title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(showTitle)
            return mFragmentTitleList.get(position);
        else
            return null;
    }
}