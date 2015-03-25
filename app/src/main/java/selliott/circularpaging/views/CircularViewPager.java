package selliott.circularpaging.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import selliott.circularpaging.RotatingPagerAdapter;

public class CircularViewPager extends ViewPager {
    private int currentPosition;
    private Direction movingDirection = Direction.NONE;
    private boolean ignoreUpdate = true;

    public CircularViewPager(final Context context) {
        super(context);
        initPager();
    }

    public CircularViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initPager();
    }

    private static enum Direction {
        LEFT,
        RIGHT,
        NONE
    }

    public void setCurrentItemNoRotate(final int item) {
        ignoreUpdate = false;
        super.setCurrentItem(item);
    }

    public void setCurrentItemNoRotate(final int item, final boolean smoothScroll) {
        ignoreUpdate = false;
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setAdapter(final PagerAdapter adapter) {
        if (adapter instanceof RotatingPagerAdapter){
            super.setAdapter(adapter);
        }
    }

    private void initPager() {
        setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {}

            @Override
            public void onPageSelected(final int position) {
                Log.d("ViewPager", "currentPosition: " + currentPosition + ", position: " + position);
                if (currentPosition > position) {
                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        movingDirection = Direction.LEFT;
//                        Log.d("ViewPager", "position-a: " + position + ", movingDirection: " + movingDirection);
                        ((RotatingPagerAdapter) getAdapter()).moveBackward(CircularViewPager.this);
//                        Log.d("ViewPager", "position-b: " + position + ", movingDirection: " + movingDirection);
                    } else {
                        currentPosition = position;
                        movingDirection = Direction.LEFT;
//                        Log.d("ViewPager", "position-c: " + position + ", movingDirection: " + movingDirection);
                        ignoreUpdate = true;
                    }
                } else if (currentPosition < position) {
                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        movingDirection = Direction.RIGHT;
//                        Log.d("ViewPager", "position-d: " + position + ", movingDirection: " + movingDirection);
                        ((RotatingPagerAdapter) getAdapter()).moveForward(CircularViewPager.this);
//                        Log.d("ViewPager", "position-e: " + position + ", movingDirection: " + movingDirection);
                    } else {
                        currentPosition = position;
                        movingDirection = Direction.RIGHT;
//                        Log.d("ViewPager", "position-f: " + position + ", movingDirection: " + movingDirection);
                        ignoreUpdate = true;
                    }
                } else {
                    if (ignoreUpdate) {
                        movingDirection = Direction.NONE;
//                        Log.d("ViewPager", "position-g: " + position + ", movingDireciton: " + movingDirection);
                    } else {
                        ignoreUpdate = true;
                        movingDirection = Direction.NONE;
//                        Log.d("ViewPager", "position-h: " + position + ", movingDireciton: " + movingDirection);
                        getAdapter().instantiateItem(CircularViewPager.this, position);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {}
        });
    }
}
