package selliott.circularpaging.views;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;

import selliott.circularpaging.RotatingPagerAdapter;

public class CircularLinkedList<E> extends AbstractList<E> implements List<E> {

    transient int size = 0;

    transient Link<E> voidLink;

    private static final class Link<ET> {
        ET data;
        Link<ET> left, right;

        Link(final ET o, final Link<ET> l, final Link<ET> r) {
            data = o;
            left = l;
            right = r;
        }
    }

    public static class LinkIterator<ET> {
        protected final CircularLinkedList<ET> list;
        protected int pos, expectedModCount;

        protected Link<ET> currentLink;

        public LinkIterator(@NonNull final CircularLinkedList<ET> object, final int location) {
            list = object;
            expectedModCount = list.modCount;
            if (isValidLocation(location, list)) {
                setupPositionAndLink(location);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void add(final ET object) {
            if (expectedModCount == list.modCount) {
                final Link<ET> next = currentLink.right;
                final Link<ET> newLink = new Link<>(object, currentLink, next);
                final Link<ET> oldCurrent = currentLink;
                currentLink.right = newLink;
                next.left = newLink;
                currentLink = newLink;
                pos++;
                expectedModCount++;
                list.size++;
                list.modCount++;
                if (oldCurrent != list.voidLink) {
                    if (next == list.voidLink.right) {
                        list.voidLink.left = newLink;
                    }
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public void addAll(@NonNull final Collection<? extends ET> collection) {
            if (expectedModCount == list.modCount) {
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
                if (oldCurrent != list.voidLink) {
                    if (next == list.voidLink.right) {
                        list.voidLink.left = currentLink;
                    }
                }
                pos += collectionSize;
                expectedModCount++;
                list.size += collectionSize;
                list.modCount++;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasNext(final boolean withoutLooping) {
            if (withoutLooping) {
                return currentLink.right != null && nextIndex() < list.size();
            }
            return currentLink.right != null;
        }

        public boolean hasPrevious(final boolean withoutLooping) {
            if (withoutLooping) {
                return currentLink.left != null && previousIndex() >= 0;
            }
            return currentLink.left != null;
        }

        protected void incrementPosAndEnsureRange() {
            pos = (pos + 1) % list.size();
        }

        protected void decrementPosAndEnsureRange() {
            pos = (pos - 1 + list.size()) % list.size();
        }

        public ET next(final boolean withoutLooping) {
            if (expectedModCount == list.modCount) {
                if (hasNext(withoutLooping)) {
                    currentLink = currentLink.right;
                    incrementPosAndEnsureRange();
                    return currentLink.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public int nextIndex() {
            return pos + 1;
        }

        public ET previous(final boolean withoutLooping) {
            if (expectedModCount == list.modCount) {
                if (hasPrevious(withoutLooping)) {
                    currentLink = currentLink.left;
                    decrementPosAndEnsureRange();
                    return currentLink.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public int previousIndex() {
            return pos - 1;
        }

        public void remove() {
            if (expectedModCount == list.modCount) {
                final Link<ET> next = currentLink.right;
                final Link<ET> previous = currentLink.left;
                final Link<ET> oldCurrent = currentLink;
                next.left = previous;
                previous.right = next;
                currentLink = previous;
                expectedModCount++;
                list.size--;
                list.modCount++;
                if (oldCurrent != list.voidLink) {
                    if (oldCurrent == list.voidLink.left) {
                        list.voidLink.left = previous;
                        pos--;
                    } else if (oldCurrent == list.voidLink.right) {
                        list.voidLink.right = next;
                    }
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public final int indexOf(final Object object) {
//            Log.d("hi", "indexOf");
            int currentPos = pos;
            Link<ET> link = currentLink;
            boolean startedSearch = false;
            if (object != null) {
                while (!startedSearch || link != currentLink) {
                    startedSearch = true;
                    if (indexOfComparator(object, link.data)) {
//                        Log.d("hi", "foundPos = " + currentPos);
                        return currentPos;
                    }
                    link = link.right;
                    currentPos = (currentPos + 1) % list.size();
                }
            } else {
                while (!startedSearch || link != currentLink) {
                    startedSearch = true;
                    if (link.data == null) {
//                        Log.d("hi", "foundPos = " + currentPos);
                        return currentPos;
                    }
                    link = link.right;
                    currentPos = (currentPos + 1) % list.size();
                }
            }
            return -1;
        }

        public boolean indexOfComparator(final Object comparisonObject, final ET data) {
            return comparisonObject.equals(data);
        }

        public final ET get() {
//            Log.d(CircularLinkedList.class.getSimpleName(), "data: " + currentLink.data + ", left: " + currentLink.left + ", right: " + currentLink.right);
            if (currentLink.data instanceof RotatingPagerAdapter.ViewHolder) {
//                Log.d(CircularLinkedList.class.getSimpleName(), "view: " + ((TextView)((RotatingPagerAdapter.ViewHolder) currentLink.data).getView().findViewById(R.id.text)).getText());
            }
            return currentLink.data;
        }

        public final void set(final ET object) {
            if (expectedModCount == list.modCount) {
                currentLink.data = object;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        private void setupPositionAndLink(final int location) {
            currentLink = list.voidLink;
            if (location < list.size() / 2) {
                for (pos = -1; pos < location; pos++) {
                    currentLink = currentLink.right;
                }
//                Log.d(CircularLinkedList.class.getSimpleName(), "pos: " + pos);
            } else {
                for (pos = list.size(); pos > location; pos--) {
                    currentLink = currentLink.left;
                }
//                Log.d(CircularLinkedList.class.getSimpleName(), "pos: " + pos);
            }
        }
    }

    public static class ReverseLinkIterater<ET> extends LinkIterator<ET> {
        public ReverseLinkIterater(@NonNull final CircularLinkedList<ET> object, final int location) {
            super(object, location);
        }

        @Override
        public void add(final ET object) {
            if (expectedModCount == list.modCount) {
                final Link<ET> next = currentLink.left;
                final Link<ET> newLink = new Link<>(object, next, currentLink);
                final Link<ET> oldCurrent = currentLink;
                currentLink.left = newLink;
                next.right = newLink;
                currentLink = newLink;
                // position stays the same
                expectedModCount++;
                list.size++;
                list.modCount++;
                if (oldCurrent != list.voidLink) {
                    if (next == list.voidLink.left) {
                        list.voidLink.right = newLink;
                    }
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void addAll(@NonNull final Collection<? extends ET> collection) {
            if (expectedModCount == list.modCount) {
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
                if (oldCurrent != list.voidLink) {
                    if (next == list.voidLink.left) {
                        list.voidLink.right = currentLink;
                    }
                }
                // position stays the same
                expectedModCount++;
                list.size += collectionSize;
                list.modCount++;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext(final boolean withoutLooping) {
            if (withoutLooping) {
                return currentLink.left != null && nextIndex() >= 0;
            }
            return currentLink.left != null;
        }

        @Override
        public boolean hasPrevious(final boolean withoutLooping) {
            if (withoutLooping) {
                return currentLink.right != null && previousIndex() < list.size();
            }
            return currentLink.right != null;
        }

        @Override
        public ET next(final boolean withoutLooping) {
            if (expectedModCount == list.modCount) {
                if (hasNext(withoutLooping)) {
                    currentLink = currentLink.left;
                    decrementPosAndEnsureRange();
                    return currentLink.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public int nextIndex() {
            return pos - 1;
        }

        @Override
        public ET previous(final boolean withoutLooping) {
            if (expectedModCount == list.modCount) {
                if (hasPrevious(withoutLooping)) {
                    currentLink = currentLink.right;
                    incrementPosAndEnsureRange();
                    return currentLink.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public int previousIndex() {
            return pos + 1;
        }

        @Override
        public void remove() {
            if (expectedModCount == list.modCount) {
                final Link<ET> next = currentLink.left;
                final Link<ET> previous = currentLink.right;
                final Link<ET> oldCurrent = currentLink;
                next.right = previous;
                previous.left = next;
                expectedModCount++;
                list.size--;
                list.modCount++;
                if (oldCurrent != list.voidLink) {
                    if (oldCurrent == list.voidLink.left) {
                        list.voidLink.left = next;
                        pos--;
                    } else if (oldCurrent == list.voidLink.right) {
                        list.voidLink.right = previous;
                    }
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    public LinkIterator<E> linkIterator(final int location) {
        return new LinkIterator<>(this, location);
    }

    private static boolean isValidLocation(final int location, final CircularLinkedList list) {
        return location >= 0 && location <= list.size();
    }

    public int size() {
        return size;
    }

    public CircularLinkedList() {
        voidLink = new Link<>(null, null, null);
        // TODO: check if previous and next should have defaults
    }

    public CircularLinkedList(@NonNull final Collection<? extends E> collection) {
        this();

        final int collectionSize = collection.size();
        if (collectionSize == 0) {
            return;
        }
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
            starterLink.right.left = runningLink;
            runningLink.right = starterLink.right;
            voidLink.left = runningLink;
            voidLink.right = runningLink.right;
        }
        size += collectionSize;
        modCount++;
    }

    public void rotate(final int amount) {
        int sign = (int) Math.signum(amount);
        int unsignedSmallestRotationAmount = Math.abs(amount) % size();
        if (Math.abs(unsignedSmallestRotationAmount) > size() / 2) {
            sign = -1 * sign;
            unsignedSmallestRotationAmount = size() - unsignedSmallestRotationAmount;
        }
        final int smallestRotationAmount = sign * unsignedSmallestRotationAmount;
        if (sign < 0) {
            for (int i = 0; i > smallestRotationAmount; i--) {
                voidLink.left = voidLink.left.left;
                voidLink.right = voidLink.right.left;
            }
        } else if (sign > 0) {
            for (int i = 0; i < smallestRotationAmount; i++) {
                voidLink.right = voidLink.right.right;
                voidLink.left = voidLink.left.right;
            }
        }
    }

    public void add(final int location, final E object) {
        final LinkIterator<E> iterator = linkIterator(location);
        iterator.add(object);
    }

    public boolean add(final E object) {
        add(size() - 1, object);
        return true;
    }

    public boolean addAll(final int location, @NonNull final Collection<? extends E> collection) {
        if (collection.size() == 0) {
            return false;
        }
        final LinkIterator<E> iterator = linkIterator(location);
        iterator.addAll(collection);
        return true;
    }

    public boolean addAll(@NonNull final Collection<? extends E> collection) {
        return addAll(size() - 1, collection);
    }

    public void addFirst(final E object) {
        add(0, object);
    }

    public void addLast(final E object) {
        add(object);
    }

    public void clear() {
        if (size() > 0) {
            size = 0;
            voidLink.right = null;
            voidLink.left = null;
            modCount++;
        }
    }

    // TODO: clone

    // TODO: contains

    public E get(final int location) {
//        Log.d("hi", "get(" + location + ")");
        final LinkIterator<E> iterator = linkIterator(location);
        return iterator.get();
    }

    public E getFirst() {
        return get(0);
    }

    public E getLast() {
        return get(size() - 1);
    }

    public int indexOf(final Object object) {
        final LinkIterator<E> iterator = linkIterator(0);
        return iterator.indexOf(object);
    }
}
