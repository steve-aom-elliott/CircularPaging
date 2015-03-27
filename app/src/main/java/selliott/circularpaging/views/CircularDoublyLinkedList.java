package selliott.circularpaging.views;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

public class CircularDoublyLinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Queue<E>, Cloneable, Serializable {
    protected transient int size = 0;
    protected transient Link<E> voidLink;

    public static interface ExtendedListIterator<ET> extends ListIterator<ET> {
        public void addAll(final Collection<? extends ET> collection);
        public int indexOf(final Object object);
        public boolean equalsComparator(final Object object, final ET data);
        public ET get();
    }

    public static interface CircularListIterator<ET> extends ListIterator<ET> {
        public boolean hasNext(final boolean withoutLooping);
        public boolean hasPrevious(final boolean withoutLooping);
        public ET next(final boolean withoutLooping);
        public ET previous(final boolean withoutLooping);
    }

    protected static final class Link<ET> {
        public ET data;
        public Link<ET> left, right;

        public Link(final ET o, final Link<ET> l, final Link<ET> r) {
            data = o;
            left = l;
            right = r;
        }
    }

    protected static class LinkIterator<ET> implements ExtendedListIterator<ET>, CircularListIterator<ET> {
        protected final CircularDoublyLinkedList<ET> list;
        protected int pos, expectedModCount;
        protected Link<ET> currentLink, lastLink;
        protected boolean defaultShouldLoop = false;
        
        public LinkIterator(@NonNull final CircularDoublyLinkedList<ET> objectList, final int location) {
            list = objectList;
            expectedModCount = list.modCount;
            setupPositionAndCurrentLink(location);
        }

        private void setupPositionAndCurrentLink(final int location) {
            if (location >= 0 && location <= list.size) {
                currentLink = list.voidLink;

            }
        }

    }
}
