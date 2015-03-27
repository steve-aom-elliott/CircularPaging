package selliott.circularpaging.views;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;

// Screw it, not going to extend/implement these other things
public class CircularLinkedList2<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Queue<E>, Cloneable, Serializable {
    protected transient int size = 0;
    protected transient Link<E> voidLink;

    public static interface ExtendedListIteratorA<ET> extends ListIterator<ET> {
        public void addWhenEmpty(final ET object);
        public void addAll(final Collection<? extends ET> collection);
        public int indexOf(final Object object);
        public boolean equalsComparator(final Object object, final ET data);
        public ET get();
    }

    public static interface CircularListIteratorA<ET> extends ListIterator<ET> {
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

    protected static class LinkIteratorA<ET> implements ExtendedListIteratorA<ET>, CircularListIteratorA<ET> {
        protected final CircularLinkedList2<ET> list;
        protected int pos, expectedModCount;
        protected Link<ET> currentLink;
        protected boolean defaultShouldLoop = false;

        public LinkIteratorA(@NonNull final CircularLinkedList2<ET> objectList, final int location) throws IndexOutOfBoundsException {
            list = objectList;
            expectedModCount = list.modCount;
            setupPositionAndCurrentLink(location);
        }

        @Override
        public void add(final ET object) throws ConcurrentModificationException {
            concurrencyCheck();
            if (list.size() == 0) {
                addWhenEmpty(object);
                return;
            }
            final Link<ET> next = currentLink.right;
            final Link<ET> newLink = new Link<>(object, currentLink, next);
            currentLink.right = newLink;
            next.left = newLink;
            currentLink = newLink;
            if (next == list.voidLink.right) {
                list.voidLink.left = newLink;
            }
            pos++;
            expectedModCount++;
            list.size++;
            list.modCount++;
        }

        // Only used on iterator that has been emptied
        @Override
        public final void addWhenEmpty(final ET object) throws ConcurrentModificationException, UnsupportedOperationException {
            concurrencyCheck();
            emptyCheck();
            final Link<ET> newLink = new Link<>(object, null, null);
            newLink.left = newLink;
            newLink.right = newLink;
            list.voidLink.left = newLink;
            list.voidLink.right = newLink;
            pos++;
            expectedModCount++;
            list.size++;
            list.modCount++;
        }

        @Override
        public void addAll(final Collection<? extends ET> collection) throws ConcurrentModificationException {
            concurrencyCheck();
            final int collectionSize = collection.size();
            if (collectionSize == 0) {
                return;
            }
            final Link<ET> next = currentLink.right;
            final Link<ET> oldCurrent = currentLink;
            for (ET e : collection) {
                currentLink.right = new Link<>(e, currentLink, null);
                currentLink = currentLink.right;
            }
            if (currentLink != oldCurrent) {
                currentLink.right = next;
                next.left = currentLink;
            }
            if (next == list.voidLink.right) {
                list.voidLink.left = currentLink;
            }
            pos += collectionSize;
            expectedModCount++;
            list.size += collectionSize;
            list.modCount++;
        }

        @Override
        public boolean hasNext(final boolean withoutLooping) {
            return currentLink.right != null && (withoutLooping || nextIndex() < list.size());
        }

        @Override
        public final boolean hasNext() {
            return hasNext(defaultShouldLoop);
        }

        @Override
        public boolean hasPrevious(final boolean withoutLooping) {
            return currentLink.left != null && (withoutLooping || previousIndex() >= 0);
        }

        @Override
        public final boolean hasPrevious() {
            return hasPrevious(defaultShouldLoop);
        }

        protected final void moveNextAndEnsureRange() {
            pos = (nextIndex() + list.size()) % list.size();
        }

        protected final void movePreviousAndEnsureRange() {
            pos = (previousIndex() + list.size()) % list.size();
        }

        @Override
        public ET next(final boolean withoutLooping) throws ConcurrentModificationException, NoSuchElementException {
            concurrencyCheck();
            if (hasNext(withoutLooping)) {
                currentLink = currentLink.right;
                moveNextAndEnsureRange();
                return currentLink.data;
            }
            throw new NoSuchElementException();
        }

        @Override
        public final ET next() throws ConcurrentModificationException, NoSuchElementException {
            return next(defaultShouldLoop);
        }

        @Override
        public int nextIndex() {
            return pos + 1;
        }

        @Override
        public ET previous(final boolean withoutLooping) throws ConcurrentModificationException, NoSuchElementException {
            concurrencyCheck();
            if (hasPrevious(withoutLooping)) {
                currentLink = currentLink.left;
                movePreviousAndEnsureRange();
                return currentLink.data;
            }
            throw new NoSuchElementException();
        }

        @Override
        public final ET previous() throws ConcurrentModificationException, NoSuchElementException {
            return previous(defaultShouldLoop);
        }

        @Override
        public int previousIndex() {
            return pos - 1;
        }

        @Override
        public final void remove() throws ConcurrentModificationException, UnsupportedOperationException {
            concurrencyCheck();
            if (list.voidLink.left == list.voidLink.right) {
                throw new UnsupportedOperationException();
            }
            final Link<ET> left = currentLink.left;
            final Link<ET> right = currentLink.right;
            left.right = right;
            right.left = left;
            currentLink = left;
            if (right == list.voidLink.right) {
                list.voidLink.left = left;
                pos--;
            } else if (left == list.voidLink.left) {
                list.voidLink.right = right;
            }
            expectedModCount++;
            list.size--;
            list.modCount++;
        }

        @Override
        public int indexOf(final Object object) {
            int currentPos = pos;
            Link<ET> link = currentLink;
            boolean startedSearch = false;
            while (!startedSearch || link != currentLink) {
                startedSearch = true;
                if (equalsComparator(object, link.data)) {
                    return currentPos;
                }
                link = link.right;
                currentPos = (currentPos + 1) % list.size();
            }
            return -1;
        }

        @Override
        public boolean equalsComparator(final Object object, final ET data) {
            if (object == null) {
                return data == null;
            } else {
                return object.equals(data);
            }
        }

        @Override
        public final ET get() throws ConcurrentModificationException {
            concurrencyCheck();
            return currentLink.data;
        }

        @Override
        public final void set(final ET object) throws ConcurrentModificationException {
            concurrencyCheck();
            currentLink.data = object;
        }

        private void setupPositionAndCurrentLink(final int location) throws IndexOutOfBoundsException {
            if (isValidLocation(list, location)) {
                if (list.voidLink.left == null || list.voidLink.right == null) {
                    throw new IndexOutOfBoundsException();
                }
                currentLink = list.voidLink;
                if (location < list.size() / 2) {
                    for (pos = -1; pos < location; pos++) {
                        currentLink = currentLink.right;
                    }
                } else {
                    for (pos = list.size(); pos > location; pos--) {
                        currentLink = currentLink.left;
                    }
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        protected final void concurrencyCheck() throws ConcurrentModificationException {
            if (expectedModCount != list.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        protected final void emptyCheck() throws UnsupportedOperationException {
            if (list.size() != 0) {
                throw new UnsupportedOperationException();
            }
        }
    }

    protected static class DescendingLinkIterator<ET> extends LinkIteratorA<ET> {
        public DescendingLinkIterator(@NonNull final CircularLinkedList2<ET> objectList, final int location) throws IndexOutOfBoundsException {
            super(objectList, location);
        }

        @Override
        public void add(final ET object) throws ConcurrentModificationException {
            concurrencyCheck();
            if (list.size() == 0) {
                addWhenEmpty(object);
                return;
            }
            final Link<ET> next = currentLink.left;
            final Link<ET> newLink = new Link<>(object, next, currentLink);
            currentLink.left = newLink;
            next.right = newLink;
            currentLink = newLink;
            if (next == list.voidLink.left) {
                list.voidLink.right = newLink;
            }
            // position stays the same
            expectedModCount++;
            list.size++;
            list.modCount++;
        }

        @Override
        public void addAll(final Collection<? extends ET> collection) throws ConcurrentModificationException {
            concurrencyCheck();
            final int collectionSize = collection.size();
            if (collectionSize == 0) {
                return;
            }
            final Link<ET> next = currentLink.left;
            final Link<ET> oldCurrent = currentLink;
            for (ET e : collection) {
                currentLink.left = new Link<>(e, null, currentLink);
                currentLink = currentLink.left;
            }
            if (currentLink != oldCurrent) {
                currentLink.left = next;
                next.right = currentLink;
            }
            if (next == list.voidLink.left) {
                list.voidLink.right = currentLink;
            }
            // position stays the same
            expectedModCount++;
            list.size += collectionSize;
            list.modCount++;
        }

        @Override
        public boolean hasNext(final boolean withoutLooping) {
            return currentLink.left != null && (withoutLooping || nextIndex() >= 0);
        }

        @Override
        public boolean hasPrevious(final boolean withoutLooping) {
            return currentLink.right != null && (withoutLooping || previousIndex() < list.size());
        }

        @Override
        public ET next(final boolean withoutLooping) throws ConcurrentModificationException, NoSuchElementException {
            concurrencyCheck();
            if (hasNext(withoutLooping)) {
                currentLink = currentLink.left;
                moveNextAndEnsureRange();
                return currentLink.data;
            }
            throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            return pos - 1;
        }

        @Override
        public ET previous(final boolean withoutLooping) throws ConcurrentModificationException, NoSuchElementException {
            concurrencyCheck();
            if (hasNext(withoutLooping)) {
                currentLink = currentLink.right;
                movePreviousAndEnsureRange();
                return currentLink.data;
            }
            throw new NoSuchElementException();
        }

        @Override
        public int previousIndex() {
            return pos + 1;
        }
    }

    public CircularLinkedList2() {
        voidLink = new Link<>(null, null, null);
    }

    public CircularLinkedList2(@NonNull final Collection<? extends E> collection) {
        this();
        addAll(collection);
    }


    @Override
    public void add(final int location, final E object) {
        listIterator(location).add(object);
    }

    @Override
    public boolean add(final E e) {
        try {
            add(size() - 1, e);
        } catch (IndexOutOfBoundsException ex) {
            final Link<E> newLink = new Link<>(e, null, null);
            newLink.left = newLink;
            newLink.right = newLink;
            voidLink.left = newLink;
            voidLink.right = newLink;
            size++;
            modCount++;
        }
        return true;
    }

    @Override
    public boolean offer(final E e) {
        return add(e);
    }

    @Override
    public void addFirst(final E e) {
        try {
            descendingListIterator(0).add(e);
        } catch (IndexOutOfBoundsException ex) {
            add(e);
        }
    }

    @Override
    public boolean offerFirst(final E e) {
        addFirst(e);
        return true;
    }

    @Override
    public void addLast(final E e) {
        add(e);
    }

    @Override
    public boolean offerLast(final E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean addAll(final int location, final Collection<? extends E> collection) {
        final int collectionSize = collection.size();
        if (collectionSize == 0) {
            return false;
        }
        try {
            ((LinkIteratorA<E>)listIterator(location)).addAll(collection);
        } catch (IndexOutOfBoundsException ex) {
            final Link<E> starterLink = new Link<>(null, null, null);
            Link<E> runningLink = null;
            for (E e : collection) {
                if (runningLink == null) {
                    runningLink = new Link<>(e, null, null);
                    starterLink.right = runningLink;
                } else {
                    runningLink.right = new Link<>(e, runningLink, null);
                    runningLink = runningLink.right;
                }
            }
            if (runningLink != null) {
                // attach the first item to the last
                starterLink.right.left = runningLink;
                runningLink.right = starterLink.right;
                voidLink.left = runningLink;
                voidLink.right = runningLink.right;
            }
            size += collectionSize;
            modCount++;
        }
        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> collection) {
        return addAll(size() - 1, collection);
    }

    @Override
    public E remove(final int location) {
        if (size() > 1) {
            final LinkIteratorA<E> iterator = (LinkIteratorA<E>) listIterator(location);
            final E item = iterator.get();
            iterator.remove();
            return item;
        } else {
            if (isValidLocation(this, location)) {
                return removeFirst();
            }
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean remove(final Object object) {
        if (size() == 0) {
            return false;
        }
        final LinkIteratorA<E> iterator = (LinkIteratorA<E>) listIterator(0);
        final int position = iterator.indexOf(object);
        if (position >= 0) {
            remove(position);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public E removeFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        if (size() == 1) {
            final E item = voidLink.right.data;
            voidLink.left = null;
            voidLink.right = null;
            size--;
            modCount++;
            return item;
        } else {
            return remove(0);
        }
    }

    @Override
    public E removeLast() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        if (size() == 1) {
            return removeFirst();
        } else {
            return remove(size() - 1);
        }
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> collection) {
        if (size() == 0 || collection.size() == 0) {
            return false;
        }
        final LinkIteratorA<E> iterator = (LinkIteratorA<E>) listIterator(0);
        do {
            if (collection.contains(iterator.get())) {
                iterator.remove();
            }
        } while (iterator.hasNext(true));
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(final int location) throws IndexOutOfBoundsException {
        return new LinkIteratorA<>(this, location);
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator() throws IndexOutOfBoundsException {
        return listIterator(0);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() throws IndexOutOfBoundsException{
        return listIterator();
    }

    @NonNull
    public ListIterator<E> descendingListIterator(final int location) throws IndexOutOfBoundsException {
        return new DescendingLinkIterator<>(this, location);
    }

    @NonNull
    public ListIterator<E> descendingListIterator() throws IndexOutOfBoundsException {
        return descendingListIterator(0);
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() throws IndexOutOfBoundsException {
        return descendingListIterator();
    }

    // TODO:
    public void rotate(final int amount) {

    }

    protected static boolean isValidLocation(@NonNull final CircularLinkedList2 list, final int location) {
        return location >= 0 && location <= list.size();
    }
}
