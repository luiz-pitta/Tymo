package com.aspsine.fragmentnavigator;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.app.Fragment;

import com.aspsine.fragmentnavigator.R;

/**
 * Created by aspsine on 16/3/30.
 */
public class FragmentNavigator {

    private static final String EXTRA_CURRENT_POSITION = "extra_current_position";
    private static final String EXTRA_LAST_POSITION = "extra_last_position";

    private FragmentManager mFragmentManager;

    private FragmentNavigatorAdapter mAdapter;

    @IdRes
    private int mContainerViewId;

    private int mCurrentPosition = -1;

    private int mLastPosition = -1;

    private boolean mWithTransition = false;

    private int mDefaultPosition;

    public FragmentNavigator(FragmentManager fragmentManager, FragmentNavigatorAdapter adapter, @IdRes int containerViewId) {
        this.mFragmentManager = fragmentManager;
        this.mAdapter = adapter;
        this.mContainerViewId = containerViewId;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(EXTRA_CURRENT_POSITION, mDefaultPosition);
            mLastPosition = savedInstanceState.getInt(EXTRA_LAST_POSITION, -1);
            resetFragments();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_CURRENT_POSITION, mCurrentPosition);
        outState.putInt(EXTRA_LAST_POSITION, mLastPosition);
    }

    /**
     * @see #showFragment(int, boolean)
     */
    public void showFragment(int position) {
        showFragment(position, false);
    }

    /**
     * @see #showFragment(int, boolean)
     */
    public void setTransition(boolean transition) {
        mWithTransition = transition;
    }

    /**
     * @see #showFragment(int, boolean, boolean)
     */
    public void showFragment(int position, boolean reset) {
        showFragment(position, reset, false);
    }

    /**
     * Show fragment at given position
     *
     * @param position fragment position
     * @param reset true if fragment in given position need reset otherwise false
     * @param allowingStateLoss true if allowing state loss otherwise false
     */
    public void showFragment(int position, boolean reset, boolean allowingStateLoss) {
        this.mLastPosition = mCurrentPosition;
        this.mCurrentPosition = position;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if(mWithTransition)
            transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (position == i) {
                if (reset) {
                    remove(position, transaction);
                    add(position, transaction);
                } else {
                    show(i, transaction);
                }
            } else {
                hide(i, transaction);
            }
        }
        if (allowingStateLoss) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }

    /**
     * reset all the fragments and show current fragment
     *
     * @see #resetFragments(int)
     */
    public void resetFragments() {
        resetFragments(mCurrentPosition);
    }

    /**
     * @see #resetFragments(int, boolean)
     */
    public void resetFragments(int position) {
        resetFragments(position, false);
    }

    /**
     * reset all the fragment and show given position fragment
     *
     * @param position fragment position
     * @param allowingStateLoss true if allowing state loss otherwise false
     */
    public void resetFragments(int position, boolean allowingStateLoss) {
        this.mCurrentPosition = position;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        removeAll(transaction);
        add(position, transaction);
        if (allowingStateLoss) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }

    /**
     * @see #removeAllFragment(boolean)
     */
    public void removeAllFragment() {
        removeAllFragment(false);
    }

    /**
     * remove all fragment in the {@link FragmentManager}
     *
     * @param allowingStateLoss true if allowing state loss otherwise false
     */
    public void removeAllFragment(boolean allowingStateLoss) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        removeAll(transaction);
        if (allowingStateLoss) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }

    /**
     * @return current showing fragment's position
     */
    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * @return last showing fragment's position
     */
    public int getLastPosition() {
        return mLastPosition;
    }

    /**
     * Also @see #getFragment(int)
     *
     * @return current position fragment
     */
    public Fragment getCurrentFragment() {
        return getFragment(mCurrentPosition);
    }

    /**
     * Get the fragment has been added in the given position. Return null if the fragment
     * hasn't been added in {@link FragmentManager} or has been removed already.
     *
     * @param position position of fragment in {@link FragmentNavigatorAdapter#onCreateFragment(int)}}
     *                 and {@link FragmentNavigatorAdapter#getTag(int)}
     * @return The fragment if found or null otherwise.
     */
    public Fragment getFragment(int position) {
        String tag = mAdapter.getTag(position);
        return mFragmentManager.findFragmentByTag(tag);
    }

    private void show(int position, FragmentTransaction transaction) {
        String tag = mAdapter.getTag(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            add(position, transaction);
        } else {
            transaction.show(fragment);
        }
    }

    private void hide(int position, FragmentTransaction transaction) {
        String tag = mAdapter.getTag(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }

    private void add(int position, FragmentTransaction transaction) {
        Fragment fragment = mAdapter.onCreateFragment(position);
        String tag = mAdapter.getTag(position);
        transaction.add(mContainerViewId, fragment, tag);
    }

    private void removeAll(FragmentTransaction transaction) {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            remove(i, transaction);
        }
    }

    private void remove(int position, FragmentTransaction transaction) {
        String tag = mAdapter.getTag(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            transaction.remove(fragment);
        }
    }

    public void setDefaultPosition(int defaultPosition) {
        this.mDefaultPosition = defaultPosition;
        if (mCurrentPosition == -1) {
            this.mCurrentPosition = defaultPosition;
        }
    }
}
