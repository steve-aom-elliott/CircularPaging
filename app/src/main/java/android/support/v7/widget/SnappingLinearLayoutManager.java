package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

public class SnappingLinearLayoutManager extends LinearLayoutManager {
    public SnappingLinearLayoutManager(final Context context) {
        super(context);
    }

    public SnappingLinearLayoutManager(final Context context, final int orientation, final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    private static enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE
    }

    protected RecyclerView.OnScrollListener snappingScrollListener = new RecyclerView.OnScrollListener() {
        private Direction movingDirection = Direction.NONE;
        private boolean isEnoughScroll = true;

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            if (newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                switch (movingDirection) {
                    case LEFT:
                        if (!SnappingLinearLayoutManager.this.getReverseLayout() && isEnoughScroll) {
                            mRecyclerView.smoothScrollToPosition(findFirstVisibleItemPosition());
                        } else {
                            mRecyclerView.smoothScrollToPosition(findLastVisibleItemPosition());
                        }
                        break;
                    case UP:
                        if (isEnoughScroll) {
                            mRecyclerView.smoothScrollToPosition(findFirstVisibleItemPosition());
                        } else {
                            mRecyclerView.smoothScrollToPosition(findLastVisibleItemPosition());
                        }
                        break;
                    case RIGHT:
                        if (!SnappingLinearLayoutManager.this.getReverseLayout() && isEnoughScroll) {
                            mRecyclerView.smoothScrollToPosition(findLastVisibleItemPosition());
                        } else {
                            mRecyclerView.smoothScrollToPosition(findFirstVisibleItemPosition());
                        }
                        break;
                    case DOWN:
                        if (isEnoughScroll) {
                            mRecyclerView.smoothScrollToPosition(findLastVisibleItemPosition());
                        } else {
                            mRecyclerView.smoothScrollToPosition(findFirstVisibleItemPosition());
                        }
                        break;
                    default:
                        break;
                }
            }
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (getOrientation() == HORIZONTAL) {
                if (dx < 0) {
                    movingDirection = Direction.LEFT;
//                    isEnoughScroll = Math.abs(dx) >= SnappingLinearLayoutManager.this.getWidth() / 2;
                } else if (dx > 0) {
                    movingDirection = Direction.RIGHT;
//                    isEnoughScroll = dx >= SnappingLinearLayoutManager.this.getWidth() / 2;
                } else {
                    movingDirection = Direction.NONE;
                }
            } else if (getOrientation() == VERTICAL) {
                if (dy < 0) {
                    movingDirection = Direction.UP;
//                    isEnoughScroll = Math.abs(dy) >= SnappingLinearLayoutManager.this.getHeight() / 2;
                } else if (dy > 0) {
                    movingDirection = Direction.DOWN;
//                    isEnoughScroll = dy >= SnappingLinearLayoutManager.this.getHeight() / 2;
                } else {
                    movingDirection = Direction.NONE;
                }
            }
        }
    };

    @Override
    void setRecyclerView(final RecyclerView recyclerView) {
        super.setRecyclerView(recyclerView);
        if (mRecyclerView != null) {
            mRecyclerView.setOnScrollListener(snappingScrollListener);
        }
    }

    @Override
    public void measureChild(final View child, int widthUsed, int heightUsed) {
        final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;
        final int widthSpec = getChildMeasureSpec(getWidth(), getPaddingLeft() + getPaddingRight() + widthUsed, getWidth(), canScrollHorizontally());
        final int heightSpec = getChildMeasureSpec(getHeight(), getPaddingTop() + getPaddingBottom() + heightUsed, getHeight(), canScrollVertically());
        child.measure(widthSpec, heightSpec);
    }

    @Override
    public void measureChildWithMargins(final View child, int widthUsed, int heightUsed) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;
        final int widthSpec = getChildMeasureSpec(getWidth(), getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed, getWidth(), canScrollHorizontally());
        final int heightSpec = getChildMeasureSpec(getHeight(), getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed, getHeight(), canScrollVertically());
        child.measure(widthSpec, heightSpec);
    }
}
