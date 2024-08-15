package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;

/**
 * <p>{@link QuadraticProbingHashTable} is an Openly Addressed {@link HashTable} which uses <b>Quadratic
 * Probing</b> as its collision resolution strategy. Quadratic Probing differs from <b>Linear</b> Probing
 * in that collisions are resolved by taking &quot; jumps &quot; on the hash table, the length of which
 * determined by an increasing polynomial factor. For example, during a key insertion which generates
 * several collisions, the first collision will be resolved by moving 1^2 + 1 = 2 positions over from
 * the originally hashed address (like Linear Probing), the second one will be resolved by moving
 * 2^2 + 2= 6 positions over from our hashed address, the third one by moving 3^2 + 3 = 12 positions over, etc.
 * </p>
 *
 * <p>By using this collision resolution technique, {@link QuadraticProbingHashTable} aims to get rid of the
 * &quot;key clustering &quot; problem that {@link LinearProbingHashTable} suffers from. Leaving more
 * space in between memory probes allows other keys to be inserted without many collisions. The tradeoff
 * is that, in doing so, {@link QuadraticProbingHashTable} sacrifices <em>cache locality</em>.</p>
 *
 * @author BRANDON RUBIO
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see LinearProbingHashTable
 * @see CollisionResolver
 */
public class QuadraticProbingHashTable extends OpenAddressingHashTable {

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
        int i = 0;
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
    public QuadraticProbingHashTable(boolean soft) {
        this.soft_check = soft;
        this.tombstone_count = 0;
        this.threshold = 0.5;
        this.count = 0;
        this.softFlag = soft;
        this.primeGenerator = new PrimeGenerator();
        this.table = new KVPair[primeGenerator.getCurrPrime()];
    }

    @Override
    public String put(String key, String value) {
    	System.out.println("Insertion QP --> Key: " + key + ", Value: " + value);
    	
        if(key == null || value == null) {
        	return null;
        }else {
        	//
        	//if threshold had been reached, resize and reinsert
        	if(threshold < ((double)(this.count + this.tombstone_count))/((double) this.table.length)){
     		   ArrayList<KVPair> newTable = new ArrayList<>();
     		   for(KVPair pairs: this.table) {
     			   if(pairs != null && pairs != TOMBSTONE) {
     				   newTable.add(pairs);
     			   }
     		   }
     		   this.count = 0;
     		   this.tombstone_count = 0; //No tombstones after a resize
     		   this.table = new KVPair[this.primeGenerator.getNextPrime()];
     		   
     		   int collision_count = 1;
       		   int quad_probing = 0;
     		   
     		   //Re-insert all elements
     		   if(newTable.size() > 0) {
     			   for(KVPair pairs : newTable) {
     				  collision_count = 1;
     	       		  quad_probing = 0;
     	       		   
     				   int newIndex = this.hash(pairs.getKey());
     				   int startIndex = this.hash(pairs.getKey());
     				   
     				   while(this.table[newIndex] != null) {
     					//if a collision occurs, calculate ---> (i^2) + i
     		     	    	quad_probing = (collision_count*collision_count) + collision_count;
     		     	    	//use polynomial (i^2) + i to increment target cells
     		     	    	newIndex = (startIndex + quad_probing) % this.table.length;
     		     	    	//increment collision counter to prepare for another collision
     		     	    	collision_count++;
     				   }
     				   this.table[newIndex] = pairs;
     				   this.count++;
     				   //reset variables for quadratic probing
     			   }
     		   }
     	    }
        	//---------------------------------
        	//The actual insertion: ----------------------------
        	//---------------------------------
        	//calculate target index
     	    int target_index = this.hash(key);
     	    int start_index = this.hash(key);
     	    //int collisions = 0;
     	    // set collision counter to use for probing calculation
     		int collision_count = 1;
     		int quad_probing = 0;
     		
     	    while(this.table[target_index] != null) {
     	    	//Collision occurred, calculate ---> (i^2) + i
     	    	quad_probing = (collision_count*collision_count) + collision_count;
     	    	//use polynomial (i^2) + i to increment target cells
     	    	target_index = (start_index + quad_probing) % this.table.length;
     	    	//increment collision counter to prepare for another collision
     	    	collision_count++;
     	    }
     	    
     	    // found empty cell, now insert
     	    this.table[target_index] = new KVPair(key, value);
     	    this.count++;
     	    //return string associated with key
     	    return value;
     	   
        }
    }


    @Override
    public String get(String key) {
    	
        if(key == null) {
        	return null;
        }
        
        //calculate target index
 	    int target_index = this.hash(key);
 	    int wrap_check = this.hash(key);
 	    // set collision counter to use for probing calculation
 		int collision_count = 0;
 		int quad_probing;
        
 		//search until key is found, while avoiding Tombstones 
        while(this.table[target_index] != null) {
        	 if (target_index == wrap_check && collision_count > 0){ // search fail, wrap around to the start index
                 return null;
             }
        	 if (this.table[target_index].getKey().equals(key) && this.table[target_index] != TOMBSTONE){
        		 String ret_val = this.table[target_index].getValue();
        	       
        	     return ret_val;
             }
        	//if a collision occurred during insertion, calculate ---> (i^2) + i
 	    	quad_probing = ((collision_count*collision_count) + collision_count);
 	    	//use polynomial (i^2) + i to increment target cells
 	    	target_index = (target_index + quad_probing) % this.table.length;
 	    	//increment collision counter to keep up with other previous collisions
 	    	collision_count++;
        }
       
       return null;
    }

    @Override
    public String remove(String key) {
    	
    	System.out.println("Removal QP --> Key: " + key);
    	
    	if(key == null) {
    		return null;
    	}
    	
        //calculate target index
 	    int target_index = this.hash(key);
 	    int wrap_check = this.hash(key);
 	    // set collision counter to use for probing calculation
 		int collision_count = 0;
 		int quad_probing = 0;
 		
 		String ret_val;
 		
 		if(this.soft_check) {
 			//soft deletion
 			while(this.table[target_index] != null) {
 				//if all locations have been visited, return null
 				if(target_index == wrap_check && collision_count > 0) {
 					return null;
 				}
 				
 				if(this.table[target_index].getKey().equals(key) && this.table[target_index] != TOMBSTONE) {
 					ret_val = this.table[target_index].getValue();
 					this.table[target_index] = TOMBSTONE;
 					this.tombstone_count++;
 					this.count--;
 					return ret_val;
 				}
 				//quadratic probing mechanism
 				quad_probing = ((collision_count*collision_count) + collision_count);
 				target_index = (wrap_check + quad_probing) % this.table.length;
 				collision_count++;
 			}
 		}else {
 			//hard deletion
 			while(this.table[target_index] != null) {
 				
 				if(target_index == wrap_check && collision_count > 0) {
 					return null;
 				}
 				//find key
 				if(this.table[target_index].getKey().equals(key)) {
 					//extract value from KV Pair
 					ret_val = this.table[target_index].getValue();
 					//set target to null
 					this.table[target_index] = null;
 					// Re-insert after deletion
 					this.count = 0;
 					ArrayList<KVPair> new_table = new ArrayList<>();
 					for(KVPair pairs : this.table) {
 						if(pairs != null) {
 							new_table.add(pairs);
 						}
 					}
 					// re-instantiation of hash table
 					this.table = new KVPair[primeGenerator.getCurrPrime()];
 					
 					if(new_table.size() > 0) {
 						for(KVPair pairs: new_table) {
 							this.put(pairs.getKey(), pairs.getValue());
 						}
 					}
 					
 					return ret_val;
 				}
 				//quadratic probing
 				quad_probing = (collision_count*collision_count) + collision_count;
 				target_index = (wrap_check + quad_probing) % this.table.length;
 				collision_count++;
 			}
 		}
 		return null;
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
       if(value == null) {
    	   return false;
       }
       
       for(int i = 0; i < this.table.length; i++) {
    	   if(this.table[i].getValue().equals(value) && this.table[i] != TOMBSTONE) {
    		   return true;
    	   }
       }
       
       return false;
    }
    @Override
    public int size(){
        return this.count;
    }

    @Override
    public int capacity() {
        return this.table.length;
    }

}