package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;

/**
 * <p>{@link OrderedLinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with
 * <b>Ordered Linear Probing</b> as its collision resolution strategy: every key collision is resolved by moving
 * one address over, and the keys in the chain is in order. It suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author YOUR NAME HERE!
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class OrderedLinearProbingHashTable extends OpenAddressingHashTable {

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
        int i =0;
        for(KVPair pairs : this.table){
            if(pairs != null){
                System.out.println("Index: "+ i + "-->"+  pairs.getKey()+ " : "+ pairs.getValue());
            }else{
                System.out.println("null");
            }
            i++;
        }
        System.out.println("************END*************");
    }
    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *               we want soft deletion, {@code false} otherwise.
     */
    public OrderedLinearProbingHashTable(boolean soft){
        this.softFlag = soft;
        this.soft_check = soft;
        this.tombstone_count = 0;
        this.count = 0;
        this.threshold = 0.5;
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
     *
     * Different from {@link LinearProbingHashTable}, the keys in the chain are <b>in order</b>. As a result, we might increase
     * the cost of insertion and reduce the cost on search miss. One thing to notice is that, in soft deletion, we ignore
     * the tombstone during the reordering of the keys in the chain. We will have some example in the writeup.
     *
     * Instances of {@link OrderedLinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     * @param key The record's key.
     * @param value The record's value.
     * @throws IllegalArgumentException if either argument is {@code null}.
     * @return The value added.
     */
    @Override
    public String put(String key, String value) {
    	System.out.println("Insertion OLP--> Key: " + key + ", Value: " + value + ", Hash Value: " + this.hash(key));
    	
        if(key == null || value == null) {
        	return null;
        }else {
        	 //calculate hash value of key
        	 int target_index = this.hash(key);
        	 
        	 //int start_index = this.hash(key);
        	 //int collisions = 0;
             //check if table has reached capacity threshold (alpha value)
        	 if(threshold < ((double)(this.count + this.tombstone_count))/ ((double) this.table.length)) {
        		 //if so, transfer all KV pairs to a list
        		 ArrayList<KVPair> newTable = new ArrayList<>();
        		 for(KVPair pairs : this.table) {
        			 if(pairs != null && pairs  != TOMBSTONE) {
        				 newTable.add(pairs);
        			 }
        		 }
        		 //Re-initilaize the hash table with next prime 
        		 this.count = 0;
        		 this.tombstone_count = 0;
        		 this.table = new KVPair[this.primeGenerator.getNextPrime()];
        		 //As long as list has values, begin placing them 
        		 if(newTable.size() > 0) {
        			 for(KVPair pairs : newTable) {
        				 
        				 put(pairs.getKey(), pairs.getValue());
        				 /*int newIndex = this.hash(pairs.getKey());
        				 
        				 while(this.table[newIndex] != null) {
        					 
        					 //supposed to 'ignore' tombstones when reordering keys
        					 
        					 // Am I ordering keys properly?? Should i insert and then re-order???
        					 if(this.table[newIndex].getKey().compareTo(pairs.getKey()) > 0 && this.table[newIndex] != TOMBSTONE ) {
        						 KVPair temp = this.table[newIndex];
        						 this.table[newIndex] = pairs;
        						 pairs = temp;
        					 }
        					 
        					 newIndex = (newIndex + 1) % this.table.length;
        				 }
        				 this.table[newIndex] = pairs;
        				 this.count++;*/
        			 }
        		 }
        	 }
        	 target_index = this.hash(key);
        	 //if no re-size is needed, place new KV pair
        	 KVPair insert = new KVPair(key, value);
             while(this.table[target_index] != null) {
            	 /*if(target_index == start_index && collisions > 0) {
            		 return null;
            	 }*/
            	 
            	 //iteration is based on comparison rather than equality
            	 if(this.table[target_index].getKey().compareTo(insert.getKey()) >= 0 && this.table[target_index] != TOMBSTONE) {
            		 System.out.println("Swapping Key " + insert.getKey() + " with Key " + this.table[target_index].getKey());
            		 
            		 KVPair pair = this.table[target_index];
            		 this.table[target_index] = insert;
            		 insert = pair;
            	 }
            	 target_index = (target_index + 1) % this.table.length;
            	// collisions++;
             }
             //found available cell, insert, increment count, return...
             this.table[target_index] = insert;
             this.count++;
             
             return value;
        }
       
    }

    @Override
    public String get(String key) {
       String ret_val = null;
       
       if(key == null) {
    	   return null;
       }
       //set target to hashed key value
       int target_index = this.hash(key);
       int start_index = this.hash(key);
       int collisions = 0;
       
       //move through the table, looking for the key that matches
       //return value if found, otherwise iterate
       //return null if never found
       while(this.table[target_index] != null) {
    	   
    	   if(target_index == start_index && collisions > 0) {
    		   return null;
    	   }
    	   
    	   if(this.table[target_index].getKey().compareTo(key) == 0  && this.table[target_index] != TOMBSTONE) {
    		   if(this.table[target_index].getKey().equals(key)) {
    			   ret_val = this.table[target_index].getValue();
    		   }
    	   }
    	   target_index = (target_index + 1) % this.table.length;
    	   collisions++;
       } 
       return ret_val;
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
    	System.out.println("Removal OLP--> Key: " + key);
        if(key == null) {
        	return null;
        }
        int target_index = this.hash(key);
        int start_index = this.hash(key);
        int collisions = 0;
        String ret_val;
        
        //check for deletion method in use
        if(this.soft_check) {
        	//soft deletion
        	while(this.table[target_index] != null) {
        		if(target_index == start_index && collisions > 0) {
        			return null;
        		}
        		
        		if(this.table[target_index].getKey().compareTo(key) == 0 && this.table[target_index] != TOMBSTONE) {
        			ret_val = this.table[target_index].getValue();
        			this.table[target_index] = TOMBSTONE;
        			this.tombstone_count++;
        			this.count--;
        			return ret_val;
        		}
        		
        		target_index = (target_index + 1) % this.table.length;
        		collisions++;
        	}
        }else {
        	//hard deletion
        	while(this.table[target_index] != null) {
        		
        		if(target_index == start_index && collisions > 0) {
        			return null;
        		}
        		
        		if(this.table[target_index].getKey().compareTo(key) == 0 && this.table[target_index] != TOMBSTONE ) {
        			ret_val = this.table[target_index].getValue();
        			//delete node
        			this.table[target_index] = null;
        			this.count--;
        			//set up for re-insertion post-deletion
        			ArrayList<KVPair> new_table = new ArrayList<>();
        			//move forward one index
        			//only keys after the key that was deleted/nullified should be re-inserted. 
        			int nextIndex = (target_index + 1) % this.table.length;
        			// loop will stop once null cell is found ak
        			while(this.table[nextIndex] != null) {
        				// store value in new list and delete
        				new_table.add(this.table[nextIndex]);
        				this.table[nextIndex] = null;
        				this.count--;
        				//iterate by incrementing reference idx with wrap-around
        				nextIndex = (nextIndex + 1) % this.table.length;
        			}
        			//re-insert all cells
        			if(new_table.size() > 0) {
        				for(KVPair pairs : new_table) {
        					this.put(pairs.getKey(), pairs.getValue());
        				}
        			}
        			
        			return ret_val;
        			        			
        		}
        		target_index = (target_index + 1) % this.table.length;
        		collisions++;
        	}
        }
        //key not found, return null
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        if(key == null) {
        	return false;
        }else if(this.get(key) != null) {
        	return true;
        }else {
        	return false;
        }
    }

    @Override
    public boolean containsValue(String value) {
        if(value ==  null) {
        	return false;
        }
        for(int i=0; i < this.table.length; i++) {
        	if(this.table[i].getValue().equals(value) && this.table[i] != TOMBSTONE) {
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
