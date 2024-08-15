package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;
/**
 * <p>{@link LinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with <b>Linear Probing</b> as its
 * collision resolution strategy: every key collision is resolved by moving one address over. It is
 * the most famous collision resolution strategy, praised for its simplicity, theoretical properties
 * and cache locality. It <b>does</b>, however, suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author BRANDON RUBIO
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class LinearProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/
	private boolean soft_check;
	private int tombstone_count;
	private double threshold;
    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */
	public void printTable(){
		System.out.println("Soft Deletion: " + this.soft_check);
        System.out.println("***********START*************");
        for(KVPair p : this.table){
            if(p != null){
                System.out.println(p.getKey()+ " : "+ p.getValue());
            }else{
                System.out.println("null");
            }
        }
        System.out.println("************END*************");
    }
    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     *
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *             we want soft deletion, {@code false} otherwise.
     */
    public LinearProbingHashTable(boolean soft) {
        this.soft_check = soft;
        this.tombstone_count = 0;
        this.threshold = 0.5;
        this.count = 0;
        this.softFlag = soft;
        this.primeGenerator = new PrimeGenerator();
        this.table = new KVPair[primeGenerator.getCurrPrime()];
    }

    /**
     * Inserts the pair &lt;key, value&gt; into this. The container should <b>not</b> allow for {@code null}
     * keys and values, and we <b>will</b> test if you are throwing a {@link IllegalArgumentException} from your code
     * if this method is given {@code null} arguments! It is important that we establish that no {@code null} entries
     * can exist in our database because the semantics of {@link #get(String)} and {@link #remove(String)} are that they
     * return {@code null} if, and only if, their key parameter is {@code null}. This method is expected to run in <em>amortized
     * constant time</em>.
     * <p>
     * Instances of {@link LinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     *
     * @param key   The record's key.
     * @param value The record's value.
     * @return The value added.
     * @throws IllegalArgumentException if either argument is {@code null}.
     */
    @Override
    public String put(String key, String value) {
       //Print statement for testing
       System.out.println("Insertion LP--> Key: " + key + ", Value: " + value);
       
       if(key == null || value == null) {
    	   throw new IllegalArgumentException();
       }else {
    	   //If the threshold has been passed, resize the hash table
    	   if(threshold < ((double)(this.count + this.tombstone_count))/((double) this.table.length)){
    		   
    		   ArrayList<KVPair> newTable = new ArrayList<>();
    		   
    		   for(KVPair pairs: this.table) {
    			   if(pairs != null && pairs != TOMBSTONE) {
    				   newTable.add(pairs);
    			   }
    		   }
    		   
    		   this.count = 0;
    		   this.tombstone_count = 0; //No tombstones present after a resize
    		   // make a new table with a capacity of a prime number greater than previous 
    		   this.table = new KVPair[this.primeGenerator.getNextPrime()];
    		   
    		   // if there are any elements to re-insert
    		   if(newTable.size() > 0) {
    			   for(KVPair pairs : newTable) {
    				   //for each KV pair, get the hash value of the key
    				   int newIndex = this.hash(pairs.getKey());
    				   //search for unoccupied entry
    				   while(this.table[newIndex] != null) {
    					   newIndex = (newIndex + 1) % this.table.length;
    				   }
    				   //re-insert the KV Pair
    				   this.table[newIndex] = pairs;
    				   this.count++;
    				   
    				   //recursive call to put may be causing an infinite loop
    				   //this.put(pairs.getKey(), pairs.getValue());
    			   }
    		   }
    	   }
    	   //----------------------------------------------
    	   //Actual Insert, regardless of whether the increase in size was performed
    	   //----------------------------------------------
    	   
    	   //set target_index as hash of key to be inserted
    	   int target_index = this.hash(key);
    	   //int start_check = this.hash(key);
    	   //int collisions = 0;
    	   
    	   //find next available cell. If a cell is occupied, then increment target_index by one a.k.a. to the next cell
    	   while(this.table[target_index] != null) {
    		  /* if(target_index == start_check && collisions > 0) {
    			   return null;
    		   }*/
   			   target_index = (target_index +1) % this.table.length;
   			   //collisions++;
       	   }
    	   this.table[target_index] = new KVPair(key, value);
    	   this.count++; 
    	   
    	   //What do I return???
    	   return value;   
       }
    }

    @Override
    public String get(String key) {
    	if(key == null) {
    		return null;
    	}
    // set target cell
       int target_index = this.hash(key);
       int start_index = this.hash(key);
       int collision_count = 0;
       //search for target cell. If found, return value that is paired with the key
       while(this.table[target_index] != null) {
    	   
    	   if(target_index == start_index && collision_count > 0) {
    		   //visited all relevant cells and ended up at the beginning search --> key not in table
    		   return null;
    	   }
    	   // if not found immediately, loop will not stop until found, or key is not in table 
    	   if(this.table[target_index].getKey().equals(key) && this.table[target_index] != TOMBSTONE) {
    		   return this.table[target_index].getValue();
    	   }
    	   target_index = (target_index + 1) % this.table.length;
    	   collision_count++;
       }
       //key not found in table: returning null
       return null;
    }


    /**
     * <b>Return</b> the value associated with key in the {@link HashTable}, and <b>remove</b> the {@link phonebook.utils.KVPair} from the table.
     * If key does not exist in the database
     * or if key = {@code null}, this method returns {@code null}. This method is expected to run in <em>amortized constant time</em>.
     *
     * @param key The key to search for.
     * @return The associated value. If the key is {@code null}, return {@code null};
     * if the key doesn't exist in the database, return {@code null}.
     */
    @Override
    public String remove(String key) {
    	System.out.println("Removal LP--> Key: " + key);
    	if(key == null) {
    		return null;
    	}
    	
    	int target_index = this.hash(key);
    	int start_index = this.hash(key);
    	int collisions = 0;
    	String ret_val;
    	
    	//soft deletion
    	//-----------------------------------------------
        if(this.soft_check) {
        	//find target
        	while(this.table[target_index] != null) {
        		if(target_index == start_index && collisions > 0) {
        			return null;
        		}
        		if(this.table[target_index].getKey().equals(key) && this.table[target_index] != TOMBSTONE) {
        			ret_val = this.table[target_index].getValue();
        			this.table[target_index] = TOMBSTONE;
        			this.tombstone_count++;
        			this.count--;
        			return ret_val;
        		}
        		//increment target_index with wrap-around
        		target_index = (target_index +1) % this.table.length;
        		collisions++;
        	}
        	//return null if key is not found in table
        	return null;
        }else {
        	//hard deletion
        	//-------------------------------------------
        	while(this.table[target_index] != null) {
        		
        		if(target_index == start_index && collisions > 0 ) {
        			return null;
        		}
        		
        		if(this.table[target_index].getKey().equals(key)) {
        			//record return value
        			ret_val = this.table[target_index].getValue();
        			//set table cell to null
        			this.table[target_index] = null;
        			this.count--;
        			//set up for re-insertion after removal
        			ArrayList<KVPair> new_table = new ArrayList<>();
        			// Move index forward because current cell is null after hard-deletion
        			int ref_target_index = (target_index + 1) % this.table.length;
        			
        			
        			//Does this work for re-insertion after deletion???????
        			// Loop would terminate if an empty cell is visited. Then no re-insertions happen.
        			while(this.table[ref_target_index] != null) {
        				//archive cell for new table
        				new_table.add(this.table[ref_target_index]);
        				//delete cell
        				this.table[ref_target_index] = null;
        				this.count--;
        				//update reference target_index
        				ref_target_index = (ref_target_index + 1) % this.table.length;
        			}
        			//re-insert all cells
        			if(new_table.size() != 0) {
        				for(KVPair pairs : new_table) {
        					this.put(pairs.getKey(), pairs.getValue());
        				}
        			}
        			//return Value associated with key
        			return ret_val;
        		}
        		//not found YET, increment target_index
        		target_index = (target_index + 1) % this.table.length;
        		collisions++;
        	}
        	
        	//not found, return null
        	return null;
        }
    }

    @Override
    public boolean containsKey(String key) {
    	if(key == null) {
    		return false;
    	}
    	
        if(this.get(key) == null) {
        	return false;
        }
        return true;
    }

    @Override
    public boolean containsValue(String value) {
        for(int i =0; i < this.table.length; i++) {
        	if(this.table[i] != TOMBSTONE && this.table[i].getValue().equals(value)) {
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
        return this.table.length;
    }
}
