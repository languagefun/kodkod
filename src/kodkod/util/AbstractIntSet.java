package kodkod.util;

import java.util.AbstractSet;
import java.util.NoSuchElementException;

/**
 * A skeletal implementation of the IntSet interface.  
 * @author Emina Torlak
 */
public abstract class AbstractIntSet extends AbstractSet<Integer> implements IntSet {
	
	/**
	 * Constructs an empty int set.
	 * @effects no this.ints'
	 */
	protected AbstractIntSet() {}
	
	/**
	 * Throws a NoSuchElementException if this is an empty set.
	 * @throws NoSuchElementException - this.isEmpty()
	 */
	final void checkNonEmpty() {
		if  (isEmpty()) throw new NoSuchElementException("no this.ints");
	}
	
	/**
	 * Returns an ascending iterator over all elements in this set.
	 * This method calls this.iterator(Integer.MIN_VALUE, Integer.MAX_VALUE).
	 * @return an ascending iterator over all elements in this set.
	 */
	public IntIterator iterator() {
		return iterator(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns an iterator over the elements of this set that
	 * are in the closed range [from..to].  If from < to, 
	 * the elements are returned in the ascending order.  
	 * Otherwise, they are returned in the descending order.
	 * @return an iterator over the elements in this sequence
	 * that are in the closed range [from..to]. 
	 */
	public abstract IntIterator iterator(int from, int to);
	
	/**
	 * Iterates through this.ints and returns true if it
	 * finds i, otherwise returns false.
	 * @return i in this.ints
	 */
	public boolean contains(int i) {
		for(IntIterator iter = iterator(); iter.hasNext(); ) {
			if (i==iter.nextInt()) return true;
		}
		return false;
	}
	
	/**
	 * Returns the first element returned by this.iterator().
	 * If this set is empty, throws a NoSuchElementException().
	 * @return min(this.ints)
	 * @throws NoSuchElementException - no this.ints
	 */
	public int min() {
		return iterator().nextInt();
	}
	
	/**
	 * Returns the last element returned by this.iterator().
	 * If this set is empty, throws a NoSuchElementException().
	 * @return max(this.ints)
	 * @throws NoSuchElementException - no this.ints
	 */
	public int max() {
		final IntIterator iter = iterator();
		int max;
		do {
			max = iter.nextInt();
		} while(iter.hasNext());
		return max;
	}
	
	/**
	 * Throws an UnsupportedOperationException.
	 * @throws UnsupportedOperationException
	 */
	public boolean add(int i) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Iterates through the elements of this, removes
	 * i if it finds it and returns true.  Otherwise
	 * returns false.  Throws an UnsupportedOperationException
	 * if this.intIterator() does not support removal.
	 * @effects this.ints' = this.ints - i
	 * @return i in this.ints
	 * @throws UnsupportedOperationException - this.intIterator() does not support removal
	 */
	public boolean remove(int i) {
		for(IntIterator iter = iterator(); iter.hasNext(); ) {
			if (i==iter.nextInt()) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if o is in this set.
	 * The method calls this.contains(((Integer)o).intValue()).
	 * @return o in this.ints
	 * @throws ClassCastException - o is not an Integer
	 * @throws NullPointerException - o = null
	 */
	public boolean contains(Object o) {
		return contains(((Integer)o).intValue());	
	}
	
	/**
	 * Adds the given integer to this set if not already present
	 * and returns true.  Otherwise does nothing and returns false.
	 * This method calls this.add(o.intValue()).
	 * @effects this.ints' = this.ints + i
	 * @return this.ints != this.ints'
	 * @throws NullPointerException - o = null
	 */
	public boolean add(Integer o) {
		return add(o.intValue());
	}
	
	/**
	 * Removes the given integer from this set if already present and
	 * returns true.  Otherwise does nothing and returns false.
	 * This method calls this.remove(((Integer)o).intValue()).
	 * @effects this.ints' = this.ints - i
	 * @return this.ints != this.ints'
	 * @throws NullPointerException - o = null
	 * @throws ClassCastException - o is not an integer
	 */
	public boolean remove(Object o) {
		return remove(((Integer)o).intValue());
	}
}
