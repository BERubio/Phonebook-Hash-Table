package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**<p>{@link SeparateChainingHashTable} is a {@link HashTable} that implements <b>Separate Chaining</b>
 * as its collision resolution strategy, i.e the collision chains are implemented as actual
 * Linked Lists. These Linked Lists are <b>not assumed ordered</b>. It is the easiest and most &quot; natural &quot; way to
 * implement a hash table and is useful for estimating hash function quality. In practice, it would
 * <b>not</b> be the best way to implement a hash table, because of the wasted space for the heads of the lists.
 * Open Addressing methods, like those implemented in {@link LinearProbingHashTable} and {@link QuadraticProbingHashTable}
 * are more desirable in practice, since they use the original space of the table for the collision chains themselves.</p>
 *
 * @author YOUR NAME HERE!
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see OrderedLinearProbingHashTable
 * @see CollisionResolver
 */
public class SeparateChainingHashTable implements HashTable{

    /* ****************************************************************** */
    /* ***** PRIVATE FIELDS / METHODS PROVIDED TO YOU: DO NOT EDIT! ***** */
    /* ****************************************************************** */

    private KVPairList[] table;
    private int count;
    private PrimeGenerator primeGenerator;

    // We mask the top bit of the default hashCode() to filter away negative values.
    // Have to copy over the implementation from OpenAddressingHashTable; no biggie.
    public int hash(String key){
        return (key.hashCode() & 0x7fffffff) % table.length;
    }
    
    /* **************************************** */
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  */
    /* **************************************** */
    /**
     *  Default constructor. Initializes the internal storage with a size equal to the default of {@link PrimeGenerator}.
     */
    public SeparateChainingHashTable(){
        this.count = 0;
        this.primeGenerator = new PrimeGenerator();
        this.table = new KVPairList[primeGenerator.getCurrPrime()];
        for(int i = 0; i < this.table.length; i++) {
        	this.table[i] = new KVPairList();
        }
    }

    @Override
    public String put(String key, String value) {
    	System.out.println("Insertion SC--> Key: " + key + ", Value: " + value);
    	
        if(key == null || value == null) {
        	return null;
        }
        int target_index = this.hash(key);
        
        //add KVPair to end of list at the target index
        this.table[target_index].addBack(key, value);
        //increment count
        this.count++;
        //return value associated with key
		return value;
        
    }

    @Override
    public String get(String key) {
    	
    	if(key == null) {
    		return null;
    	}
    	
        return this.table[this.hash(key)].getValue(key).getValue();
    }

    @Override
    public String remove(String key) {
    
    	System.out.println("Removal SC--> Key: " + key);
    	
    	if(key == null) {
    		return null;
    	}
    	
    	int target_index = this.hash(key);
    	// use KVPair List removeByKey to remove key 
        Probes removed = this.table[target_index].removeByKey(key);
        //if successful then decrement count
        this.count--;
        //return value associated with key used for deletion
        return removed.getValue();
    }

    @Override
    public boolean containsKey(String key) {
    	if(key == null) {
    		return false;
    	}
    	
        int target_index = this.hash(key);
        //search specified list for key
        return this.table[target_index].containsKey(key);
    }

    @Override
    public boolean containsValue(String value) {
    	if(value == null) {
    		return false;
    	}
    	// Must iterate through table: Searching for a value
    	for(int i = 0; i < this.table.length; i++) {
    		//search each bucket-list for value
    		if(this.table[i].containsValue(value)) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public int capacity() {
        return table.length; // Or the value of the current prime.
    }

    public KVPairList get(int idx) throws IndexOutOfBoundsException {
    	return table[idx];
    }

    /**
     * Enlarges this hash table. At the very minimum, this method should increase the <b>capacity</b> of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the enlargement heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     * @see PrimeGenerator#getNextPrime()
     */
    public void enlarge() {
        //set up array list to use for re-insertion
    	ArrayList<KVPair> new_table = new ArrayList<>();
    	for(int i = 0; i < this.table.length; i++) {
    		for(KVPair pairs : this.table[i]) {
    			new_table.add(pairs);
    		}
    	}
    	//create new hash table with next prime available;
    	int capacity_resize = primeGenerator.getNextPrime();
    	this.table = new KVPairList[capacity_resize];
    	//reset count
    	this.count = 0;
    	//make each bucket have a new list for separate chaining
    	for(int i = 0; i < this.table.length; i++) {
    		this.table[i] = new KVPairList();
    	}
    	
    	//transfer KV pairs if array list has any 
    	if(new_table.size() > 0) {
    		for(KVPair pairs : new_table) {
    			this.put(pairs.getKey(), pairs.getValue());
    		}
    	}
    }

    /**
     * Shrinks this hash table. At the very minimum, this method should decrease the size of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the shrinking heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     *
     * @see PrimeGenerator#getPreviousPrime()
     */
    public void shrink(){
    	//set up array list to use for re-insertion
    	ArrayList<KVPair> new_table = new ArrayList<>();
    	for(int i = 0; i < this.table.length; i++) {
    		for(KVPair pairs : this.table[i]) {
    			new_table.add(pairs);
    		}
    	}
    	//create new hash table with next prime available;
    	int capacity_resize = primeGenerator.getPreviousPrime();
    	this.table = new KVPairList[capacity_resize];
    	//reset count
    	this.count = 0;
    	//make each bucket have a new list for separate chaining
    	for(int i = 0; i < this.table.length; i++) {
    		this.table[i] = new KVPairList();
    	}
    	
    	//transfer KV pairs if array list has any 
    	if(new_table.size() > 0) {
    		for(KVPair pairs : new_table) {
    			this.put(pairs.getKey(), pairs.getValue());
    		}
    	}
    }
}
