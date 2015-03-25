package selliott.circularpaging.adpaters;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CircularPagerAdapter extends PagerAdapter {
    private List<View> items = new ArrayList<>();

    public CircularPagerAdapter(@NonNull final List<View> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public int moveLeft(final int movedToPosition) {
        return moveDirection(1, movedToPosition);
    }

    public int moveRight(final int movedToPosition) {
        return moveDirection(-1, movedToPosition);
    }

    private int moveDirection(final int rotationAmount, final int movedToPosition) {
        Collections.rotate(items, rotationAmount);
        notifyDataSetChanged();
        return movedToPosition + rotationAmount;
    }

    @Override
    public int getItemPosition(final Object object) {
        final View v = (View) object;
        final int foundPos = items.indexOf(v);
        return (foundPos >= 0) ? foundPos : POSITION_NONE;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View v = items.get(position);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object o) {
        return view == o;
    }
}
