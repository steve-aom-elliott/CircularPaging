package selliott.circularpaging.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import selliott.circularpaging.RotatingPagerAdapter;

public class CircularViewPager extends ViewPager {
    private int currentPosition;
    private boolean ignoreUpdate = true;

    public CircularViewPager(final Context context) {
        super(context);
        initPager();
    }

    public CircularViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initPager();
    }

    @Override
    public void setCurrentItem(final int item) {
        ignoreUpdate = false;
        if (item < 0 || item >= getAdapter().getCount()) {
            return;
        }
        final int diff = item - currentPosition;
        if (diff >= 0) {
            ((RotatingPagerAdapter) getAdapter()).moveDirection(diff - 1, CircularViewPager.this);
            ignoreUpdate = false;
            super.setCurrentItem(currentPosition + 1);
        } else if (diff < 0) {
            ((RotatingPagerAdapter) getAdapter()).moveDirection(diff + 1, CircularViewPager.this);
            ignoreUpdate = false;
            super.setCurrentItem(currentPosition - 1);
        }
    }

    @Override
    public void setCurrentItem(final int item, final boolean smoothScroll) {
        ignoreUpdate = false;
        if (item < 0 || item >= getAdapter().getCount()) {
            return;
        }
        final int diff = item - currentPosition;
        if (diff >= 0) {
            ((RotatingPagerAdapter) getAdapter()).moveDirection(diff - 1, CircularViewPager.this);
            ignoreUpdate = false;
            super.setCurrentItem(currentPosition + 1, smoothScroll);
        } else if (diff < 0) {
            ((RotatingPagerAdapter) getAdapter()).moveDirection(diff + 1, CircularViewPager.this);
            ignoreUpdate = false;
            super.setCurrentItem(currentPosition - 1, smoothScroll);
        }
    }

    @Override
    public void setAdapter(final PagerAdapter adapter) {
        if (adapter instanceof RotatingPagerAdapter){
            super.setAdapter(adapter);
            setCurrentItem(0, true);
        }
    }

    private void initPager() {
        setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {}

            @Override
            public void onPageSelected(final int position) {
                if (currentPosition != position) {
                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        ((RotatingPagerAdapter) getAdapter()).moveDirection(position - currentPosition, CircularViewPager.this);
                    } else {
                        currentPosition = position;
                        ignoreUpdate = true;
                    }
                } else {
                    if (!ignoreUpdate) {
                        ignoreUpdate = true;
                        getAdapter().instantiateItem(CircularViewPager.this, position);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {}
        });
    }
}
