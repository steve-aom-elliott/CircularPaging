package selliott.circularpaging;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.List;

import selliott.circularpaging.views.CircularLinkedList;
import selliott.circularpaging.views.CircularViewPager;

public class RotatingPagerAdapter<T extends RotatingPagerAdapter.ViewHolder> extends PagerAdapter {
    private final ExtendedCircularLinkedList items;

    public static interface ViewHolder {
        public View getView();
    }

    private final class ExtendedCircularLinkedList extends CircularLinkedList<T> {
        public ExtendedCircularLinkedList(@NonNull final Collection<? extends T> collection) {
            super(collection);
        }
        private final class CustomLinkIterator extends LinkIterator<T> {
            private CustomLinkIterator(@NonNull final CircularLinkedList<T> object, final int location) {
                super(object, location);
            }

            @Override
            public boolean indexOfComparator(final Object comparisonObject, final T data) {
                if (comparisonObject instanceof View) {
                    if (data != null) {
                        if (comparisonObject.equals(data.getView())) {
                            return true;
                        }
                    }
                }
                return super.indexOfComparator(comparisonObject, data);
            }
        }

        @Override
        public LinkIterator<T> linkIterator(final int location) {
            return new CustomLinkIterator(this, location);
        }
    }

    public RotatingPagerAdapter(@NonNull final List<T> items) {
        this.items = new ExtendedCircularLinkedList(items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final T vh = items.get(position);
        setupView(vh);
        container.addView(vh.getView());
        return vh.getView();
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(final Object object) {
        final View v = (View) object;
        final int index = items.indexOf(v);
        if (index >= 0) {
            return index;
        }
        return POSITION_NONE;
    }

    protected void setupView(@NonNull final T vh) {}

    public void moveDirection(final int rotationAmount, final CircularViewPager parent) {
//        Log.i("blah", "Printing");
//        for(T t : items) {
//            Log.i("blah", "" + ((TextView)t.getView().findViewById(R.id.text)).getText());
//        }
//        Log.i("blah", "-----------------------");
        items.rotate(rotationAmount);
//        Log.i("blah", "Printing");
//        for(T t : items) {
//            Log.i("blah", "" + ((TextView)t.getView().findViewById(R.id.text)).getText());
//        }
//        Log.i("blah", "+++++++++++++++++++++++");
        parent.removeAllViews();
        notifyDataSetChanged();
    }
}
