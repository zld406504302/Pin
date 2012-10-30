package pin.util;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * User: zhongyuan
 * Date: 12-8-20
 * Time: 上午10:35
 */
public class ConcurrentArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {


    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();


    private transient volatile int modCount = 0;
    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient Object[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public ConcurrentArrayList(int initialCapacity) {
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " +
                    initialCapacity);
        this.elementData = new Object[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ConcurrentArrayList() {
        this(10);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public ConcurrentArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        size = elementData.length;
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        modCount = modCount + 1;
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            int oldCapacity = elementData.length;
            if (size < oldCapacity) {
                elementData = Arrays.copyOf(elementData, size);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity) {
        if (minCapacity > 0)
            ensureCapacityInternal(minCapacity);
    }

    private void ensureCapacityInternal(int minCapacity) {

        modCount = modCount + 1;
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            // overflow-conscious code
            if (minCapacity - elementData.length > 0)
                grow(minCapacity);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        modCount = modCount + 1;
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            // overflow-conscious code
            int oldCapacity = elementData.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            // minCapacity is usually close to size, so this is a win:
            elementData = Arrays.copyOf(elementData, newCapacity);
        } finally {
            writeLock.unlock();
        }
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    public int indexOf(Object o) {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            if (o == null) {
                for (int i = 0; i < size; i++)
                    if (elementData[i] == null)
                        return i;
            } else {
                for (int i = 0; i < size; i++)
                    if (o.equals(elementData[i]))
                        return i;
            }
            return -1;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    public int lastIndexOf(Object o) {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            if (o == null) {
                for (int i = size - 1; i >= 0; i--)
                    if (elementData[i] == null)
                        return i;
            } else {
                for (int i = size - 1; i >= 0; i--)
                    if (o.equals(elementData[i]))
                        return i;
            }
            return -1;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    public Object clone() {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            @SuppressWarnings("unchecked")
            ConcurrentArrayList<E> v = (ConcurrentArrayList<E>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     * <p/>
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     * <p/>
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */
    public Object[] toArray() {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            return Arrays.copyOf(elementData, size);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.  If the list fits in the
     * specified array, it is returned therein.  Otherwise, a new array is
     * allocated with the runtime type of the specified array and the size of
     * this list.
     * <p/>
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element in
     * the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            if (a.length < size)
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(elementData, size, a.getClass());
            System.arraycopy(elementData, 0, a, 0, size);
            if (a.length > size)
                a[size] = null;
            return a;
        } finally {
            readLock.unlock();
        }
    }

    // Positional Access Operations

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        rangeCheck(index);
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            E oldValue = elementData(index);
            elementData[index] = element;
            return oldValue;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            ensureCapacityInternal(size + 1);  // Increments modCount!!
            elementData[size++] = e;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            rangeCheckForAdd(index);

            ensureCapacityInternal(size + 1);  // Increments modCount!!
            System.arraycopy(elementData, index, elementData, index + 1,
                    size - index);
            elementData[index] = element;
            size++;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            rangeCheck(index);

            modCount = modCount + 1;
            E oldValue = elementData(index);

            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index + 1, elementData, index,
                        numMoved);
            elementData[--size] = null; // Let gc do its work

            return oldValue;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(Object o) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            if (o == null) {
                for (int index = 0; index < size; index++)
                    if (elementData[index] == null) {
                        fastRemove(index);
                        return true;
                    }
            } else {
                for (int index = 0; index < size; index++)
                    if (o.equals(elementData[index])) {
                        fastRemove(index);
                        return true;
                    }
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            modCount = modCount + 1;
            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index + 1, elementData, index,
                        numMoved);
            elementData[--size] = null; // Let gc do its work
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            modCount = modCount + 1;

            // Let gc do its work
            for (int i = 0; i < size; i++)
                elementData[i] = null;

            size = 0;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            Object[] a = c.toArray();
            int numNew = a.length;
            ensureCapacityInternal(size + numNew);  // Increments modCount
            System.arraycopy(a, 0, elementData, size, numNew);
            size += numNew;
            return numNew != 0;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c     collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException      if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            rangeCheckForAdd(index);

            Object[] a = c.toArray();
            int numNew = a.length;
            ensureCapacityInternal(size + numNew);  // Increments modCount

            int numMoved = size - index;
            if (numMoved > 0)
                System.arraycopy(elementData, index, elementData, index + numNew,
                        numMoved);

            System.arraycopy(a, 0, elementData, index, numNew);
            size += numNew;
            return numNew != 0;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *                                   {@code toIndex} is out of range
     *                                   ({@code fromIndex < 0 ||
     *                                   fromIndex >= size() ||
     *                                   toIndex > size() ||
     *                                   toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            modCount = modCount + 1;
            int numMoved = size - toIndex;
            System.arraycopy(elementData, toIndex, elementData, fromIndex,
                    numMoved);

            // Let gc do its work
            int newSize = size - (toIndex - fromIndex);
            while (size != newSize)
                elementData[--size] = null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * A version of rangeCheck used by add and addAll.
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException   if the class of an element of this list
     *                              is incompatible with the specified collection
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *                              specified collection does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, false);
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all
     * of its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException   if the class of an element of this list
     *                              is incompatible with the specified collection
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *                              specified collection does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, true);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] elementData = this.elementData;
            int r = 0, w = 0;
            boolean modified = false;
            try {
                for (; r < size; r++)
                    if (c.contains(elementData[r]) == complement)
                        elementData[w++] = elementData[r];
            } finally {
                // Preserve behavioral compatibility with AbstractCollection,
                // even if c.contains() throws.
                if (r != size) {
                    System.arraycopy(elementData, r,
                            elementData, w,
                            size - r);
                    w += size - r;
                }
                if (w != size) {
                    for (int i = w; i < size; i++)
                        elementData[i] = null;
                    modCount = modCount + size - w;
                    size = w;
                    modified = true;
                }
            }
            return modified;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     * instance is emitted (int), followed by all of its elements
     * (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        final Lock readLock = this.readLock;
        readLock.lock();
        try {
            // Write out element count, and any hidden stuff
            int expectedModCount = modCount;
            s.defaultWriteObject();

            // Write out array length
            s.writeInt(elementData.length);

            // Write out all elements in the proper order.
            for (int i = 0; i < size; i++)
                s.writeObject(elementData[i]);

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            // Read in size, and any hidden stuff
            s.defaultReadObject();

            // Read in array length and allocate array
            int arrayLength = s.readInt();
            Object[] a = elementData = new Object[arrayLength];

            // Read in all elements in the proper order.
            for (int i = 0; i < size; i++)
                a[i] = s.readObject();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     * <p/>
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     * <p/>
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * <p/>
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            final Lock readLock = ConcurrentArrayList.this.readLock;
            readLock.lock();
            try {
                checkForComodification();
                int i = cursor;
                if (i >= size)
                    throw new NoSuchElementException();
                Object[] elementData = ConcurrentArrayList.this.elementData;
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return (E) elementData[lastRet = i];
            } finally {
                readLock.unlock();
            }
        }

        public void remove() {
            final Lock writeLock = ConcurrentArrayList.this.writeLock;
            writeLock.lock();
            try {
                if (lastRet < 0)
                    throw new IllegalStateException();
                checkForComodification();

                try {
                    ConcurrentArrayList.this.remove(lastRet);
                    cursor = lastRet;
                    lastRet = -1;
                    expectedModCount = modCount;
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            } finally {
                writeLock.unlock();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            final Lock readLock = ConcurrentArrayList.this.readLock;
            readLock.lock();
            try {
                checkForComodification();
                int i = cursor - 1;
                if (i < 0)
                    throw new NoSuchElementException();
                Object[] elementData = ConcurrentArrayList.this.elementData;
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i;
                return (E) elementData[lastRet = i];
            } finally {
                readLock.unlock();
            }
        }

        public void set(E e) {
            final Lock writeLock = ConcurrentArrayList.this.writeLock;
            writeLock.lock();
            try {
                if (lastRet < 0)
                    throw new IllegalStateException();
                checkForComodification();

                try {
                    ConcurrentArrayList.this.set(lastRet, e);
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            } finally {
                writeLock.unlock();
            }
        }

        public void add(E e) {
            final Lock writeLock = ConcurrentArrayList.this.writeLock;
            writeLock.lock();
            try {
                checkForComodification();

                try {
                    int i = cursor;
                    ConcurrentArrayList.this.add(i, e);
                    cursor = i + 1;
                    lastRet = -1;
                    expectedModCount = modCount;
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            } finally {
                writeLock.unlock();
            }
        }
    }
}
