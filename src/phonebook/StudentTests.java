package phonebook;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import phonebook.hashes.*;
import phonebook.utils.NoMorePrimesException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;
import static phonebook.hashes.CollisionResolver.*;

//import sun.plugin.perf.PluginRollup;

/**
 * <p> {@link StudentTests} is a place for you to write your tests for {@link Phonebook} and all the various
 * {@link HashTable} instances.</p>
 *
 * @author BRANDON RUBIO
 * @see Phonebook
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see QuadraticProbingHashTable
 */
public class StudentTests {

    private Phonebook pb;
    private CollisionResolver[] resolvers = {SEPARATE_CHAINING, LINEAR_PROBING, ORDERED_LINEAR_PROBING, QUADRATIC_PROBING};
    private HashMap<String, String> testingPhoneBook;
    private static final long SEED = 47;
    private static final Random RNG = new Random(SEED);
    private static final int NUMS = 1000;
    private static final int UPPER_BOUND = 100;

    private String format(String error, CollisionResolver namesToPhones, CollisionResolver phonesToNames) {
        return error + "Collision resolvers:" + namesToPhones + ", " + phonesToNames + ".";
    }


    private String errorData(Throwable t) {
        return "Received a " + t.getClass().getSimpleName() + " with message: " + t.getMessage() + ".";
    }

    @Before
    public void setUp() {
        testingPhoneBook = new HashMap<>();
        testingPhoneBook.put("Arnold", "894-59-0011");
        testingPhoneBook.put("Tiffany", "894-59-0011");
        testingPhoneBook.put("Jessie", "705-12-7500");
        testingPhoneBook.put("Mary", "888-1212-3340");
    }

    @After
    public void tearDown() {
        testingPhoneBook.clear();
    }


    // Make sure that all possible phonebooks we can create will report empty when beginning.
    @Test
    public void testBehaviorWhenEmpty() {
        for (CollisionResolver namesToPhones : resolvers) {
            for (CollisionResolver phonesToNames : resolvers) {
                pb = new Phonebook(namesToPhones, phonesToNames);
                assertTrue(format("Phonebook should be empty", namesToPhones, phonesToNames), pb.isEmpty());
            }
        }
    }

    // See if all of our hash tables cover the simple example from the writeup.
    @Test
    public void testOpenAddressingResizeWhenInsert() {
        SeparateChainingHashTable sc = new SeparateChainingHashTable();
        LinearProbingHashTable lp = new LinearProbingHashTable(false);
        QuadraticProbingHashTable qp = new QuadraticProbingHashTable(false);
        assertEquals("Separate Chaining hash should have a capacity of 7 at startup.", 7, sc.capacity());
        assertEquals("Linear Probing hash should have a capacity of 7 at startup.", 7, lp.capacity());
        assertEquals("Quadratic Probing hash should have a capacity of 7 at startup.", 7, qp.capacity());
        for (Map.Entry<String, String> entry : testingPhoneBook.entrySet()) { // https://docs.oracle.com/javase/10/docs/api/java/util/Map.Entry.html
            sc.put(entry.getKey(), entry.getValue());
            lp.put(entry.getKey(), entry.getValue());
            qp.put(entry.getKey(), entry.getValue());
        }
        assertEquals("Separate Chaining hash should have a capacity of 7 after inserting 4 elements.", 7, sc.capacity());
        assertEquals("Linear Probing hash should have a capacity of 7 after inserting 4 elements.", 7, lp.capacity());
        assertEquals("Quadratic Probing hash should have a capacity of 7 after inserting 4 elements.", 7, qp.capacity());

        sc.put("DeAndre", "888-1212-3340");
        assertEquals("Separate Chaining hash should still have a capacity of 7 after inserting 5 elements.", 7, sc.capacity());
        sc.enlarge();
        assertEquals("Separate Chaining hash should have a capacity of 13 after first call to enlarge().", 13, sc.capacity());
        sc.enlarge();
        assertEquals("Separate Chaining hash should have a capacity of 23 after second call to enlarge().", 23, sc.capacity());
        sc.shrink();
        assertEquals("Separate Chaining hash should have a capacity of 13 after two calls to enlarge() and one to shrink().",
                13, sc.capacity());
        sc.shrink();
        assertEquals("Separate Chaining hash should have a capacity of 7 after two calls to enlarge() and two to shrink().",
                7, sc.capacity());
        lp.put("DeAndre","888-1212-3340" );
        assertEquals("Linear Probing hash should have a capacity of 13 after inserting 5 elements.",
                13, lp.capacity());
        qp.put("DeAndre","888-1212-3340" );
        assertEquals("Quadratic Probing hash should have a capacity of 13 after inserting 5 elements.",
                13, qp.capacity());

        // The following two deletions should both fail and thus not affect capacity.

        lp.remove("Thomas");
        assertEquals("Linear Probing hash with starting capacity of 7 should have a capacity of 13 after " +
                "five insertions and a failed deletion.", 13, lp.capacity());
        qp.remove("Thomas" );
        assertEquals("Quadratic Probing hash with starting capacity of 7 should have a capacity of 13 after " +
                "five insertions and a failed deletion.", 13, qp.capacity());
    }

    // An example of a stress test to catch any insertion errors that you might get.
    @Test
    public void insertionStressTest() {
        HashTable sc = new SeparateChainingHashTable();
        HashTable lp = new LinearProbingHashTable(false);
        HashTable qp = new QuadraticProbingHashTable(false);
        for (int i = 0; i < NUMS; i++) {
            String randomNumber = Integer.toString(RNG.nextInt(UPPER_BOUND));
            String randomNumber2 = Integer.toString(RNG.nextInt(UPPER_BOUND));
            try {
                sc.put(randomNumber, randomNumber2);
            } catch (NoMorePrimesException ignored) {
                // To have this exception thrown is not a problem; we have a finite #primes to generate resizings for.
            } catch (Throwable t) {
                fail("Separate Chaining hash failed insertion #" + i + ". Error message: " + errorData(t));
            }

            try {
                lp.put(randomNumber, randomNumber2);
            } catch (NoMorePrimesException ignored) {
                // To have this exception thrown is not a problem; we have a finite #primes to generate resizings for.
            } catch (Throwable t) {
                fail("Linear Probing hash failed insertion #" + i + ". Error message: " + errorData(t));
            }


            try {
                qp.put(randomNumber, randomNumber2);
            } catch (NoMorePrimesException ignored) {
                // To have this exception thrown is not a problem; we have a finite #primes to generate resizings for.
            } catch (Throwable t) {
                fail("Quadratic Probing hash failed insertion #" + i + ". Error message: " + errorData(t));
            }
        }

    }

    @Test
    public void testSCProbes() {
        SeparateChainingHashTable sc = new SeparateChainingHashTable();

        sc.put("Arnold", "894-59-0011");
        sc.put("Tiffany", "894-59-0011");
        sc.put("Jessie", "705-12-7500");
        sc.put("Mary", "888-1212-3340");

        int idx = sc.hash("Arnold");
        assertEquals(1, sc.get(idx).getValue("Arnold").getProbes());
        assertEquals("894-59-0011", sc.get("Arnold"));
        idx = sc.hash("Tiffany");
        assertEquals(1, sc.get(idx).getValue("Tiffany").getProbes());
        idx = sc.hash("Jessie");
        assertEquals(2, sc.get(idx).getValue("Jessie").getProbes());
        idx = sc.hash("Mary");
        assertEquals(1, sc.get(idx).getValue("Mary").getProbes());

        // Search fail
        idx = sc.hash("Jerry");
        assertEquals(2, sc.get(idx).getKey("Jerry").getProbes());
        assertEquals(null, sc.remove("Jerry"));

        idx = sc.hash("Arnold");
        assertEquals(1, sc.get(idx).getValue("Arnold").getProbes());
        sc.remove("Arnold");

        idx = sc.hash("Tiffany");
        assertEquals(1, sc.get(idx).getValue("Tiffany").getProbes());
        sc.remove("Tiffany");

        idx = sc.hash("Jessie");
        assertEquals(1, sc.get(idx).getValue("Jessie").getProbes());
        sc.remove("Jessie");
        
        idx = sc.hash("Mary");
        assertEquals(1, sc.get(idx).getValue("Mary").getProbes());
        sc.remove("Mary");
    }


    @Test
    public void testLProbes() {

        LinearProbingHashTable lp = new LinearProbingHashTable(false);
        
        lp.put("Arnold", "894-59-0011");
        lp.put("Tiffany", "894-59-0011");
        lp.put("Jessie", "705-12-7500");
        lp.put("Mary", "888-1212-3340");

        assertEquals("Arnold", lp.get(lp.hash("Arnold")).getKey());
        assertEquals("894-59-0011", lp.get("Arnold"));
        assertEquals("Tiffany", lp.get(lp.hash("Tiffany")).getKey());
        assertEquals("Jessie", lp.get( (lp.hash("Jessie")+1)%lp.capacity() ).getKey());
        assertEquals("Mary", lp.get(lp.hash("Mary")).getKey());

        // Search fail
        assertEquals(null, lp.get("Jerry"));
        assertEquals(null, lp.remove("Jerry"));

        assertEquals("705-12-7500", lp.remove("Jessie"));
        assertEquals("894-59-0011", lp.remove("Arnold"));
        assertEquals("894-59-0011", lp.remove("Tiffany"));
        assertEquals("888-1212-3340", lp.remove("Mary"));



    }

    @Test
    public void testResizeSoftLProbes() {

        LinearProbingHashTable lp = new LinearProbingHashTable(true);
        String[] add1 = new String[]{"Tiffany", "Helen", "Alexander", "Paulette", "Jason", "Money", "Nakeesha", "Ray", "Jing", "Amg"};
        String[] remove1 = new String[]{"Helen", "Alexander", "Paulette", "Jason", "Money", "Nakeesha", "Ray", "Jing", "Amg"};
        String[] add2 = new String[]{"Christine", "Carl"};

        for(String s: add1) {
            lp.put(s, s);
        }

        for (String s: remove1) {
            lp.remove(s);
        }

        for(String s: add2) {
            lp.put(s, s);
        }

        assertEquals("After additions and deletions, and additions again, the capacity should be 23, but get " + lp.capacity() + ".", 23, lp.capacity());

        lp.put("Terry", "new");
        assertEquals("After additions and deletions, and additions again, resize should be triggered and the capacity should be 43, but get " + lp.capacity() + ".", 43, lp.capacity());

    }
    
    @Test
    public void testSCdeletion() {
    	pb = new Phonebook(resolvers[0], resolvers[0]);
    	String[] add = new String[]{"Tiffany", "Helen", "Alexander", "Paulette", "Jason", "Money", "Nakeesha", "Ray", "Jing", "Amg"};
    	String[] remove = new String[]{"Tiffany", "Helen", "Alexander", "Paulette", "Jason", "Money", "Nakeesha", "Ray", "Jing", "Amg"};
    	for(String s: add) {
    		pb.addEntry(s, s);
    	}
    	for (String s: remove) {
            pb.deleteEntry(s, s);
        }
    	//System.out.println(pb.size());
    	assertEquals("Checking that phonebook is empty after full deletion", pb.isEmpty(), true);
    }
    
    
    @Test
    public void testPBGetNumber() {
    	for (CollisionResolver namesToPhones : resolvers) {
            for (CollisionResolver phonesToNames : resolvers) {
                pb = new Phonebook(namesToPhones, phonesToNames);
                pb.addEntry("Batman", "9110");
                pb.addEntry("Jason", "900-701-2902");
            	assertEquals("Checking that Jason's number is 900-701-2901", pb.getNumberOf("Jason"), "900-701-2902");
            }
        }
    }
    
    @Test
    public void testPBGetOwnerLP() {
    	pb = new Phonebook(LINEAR_PROBING, LINEAR_PROBING);
        pb.addEntry("DeAndre", "367-900-1199");
        pb.addEntry("Charles", "667-093-4567");
        pb.addEntry("Christine", "104-356-2111");
        pb.addEntry("Alexander", "590-260-9001");
        pb.addEntry("Carl", "850-102-8974");
        pb.addEntry("Paulette", "215-334-6807");
        pb.addEntry("Aditya", "890-123-0209");
        pb.addEntry("Arnold", "894-590-0011");    
        pb.addEntry("Jacqueline", "321-990-2801");  
        pb.addEntry("Yi", "921-350-4314"); 
        pb.addEntry("Tiffany", "810-279-0711"); 
        pb.addEntry("Nakeesha", "708-890-2234");
        pb.addEntry("Jason", "900-701-2902");
        pb.addEntry("Jessie", "705-120-7500");
        pb.addEntry("Helen", "810-206-9450");
        pb.addEntry("Mary", "888-121-3340");

        assertEquals("Jason", pb.getOwnerOf("900-701-2902"));
    }
    
    @Test
    public void testPBGetNumberOfOLP() {
    	pb = new Phonebook(ORDERED_LINEAR_PROBING, ORDERED_LINEAR_PROBING);
        pb.addEntry("DeAndre", "367-900-1199");
        pb.addEntry("Charles", "667-093-4567");
        pb.addEntry("Christine", "104-356-2111");
        pb.addEntry("Alexander", "590-260-9001");
        pb.addEntry("Carl", "850-102-8974");
        pb.addEntry("Paulette", "215-334-6807");
        pb.addEntry("Aditya", "890-123-0209");
        pb.addEntry("Arnold", "894-590-0011");    
        pb.addEntry("Jacqueline", "321-990-2801");  
        pb.addEntry("Yi", "921-350-4314"); 
        pb.addEntry("Tiffany", "810-279-0711"); 
        pb.addEntry("Nakeesha", "708-890-2234");
        pb.addEntry("Jason", "900-701-2902");
        pb.addEntry("Jessie", "705-120-7500");
        pb.addEntry("Helen", "810-206-9450");
        pb.addEntry("Mary", "888-121-3340");
    	
        
        assertEquals("900-701-2902", pb.getNumberOf("Jason"));
    }
    
    @Test
    public void testPBGetNumberOfOLP2() {
    	OrderedLinearProbingHashTable olp = new OrderedLinearProbingHashTable(false);
        olp.put("DeAndre", "367-900-1199");
        olp.printTable();
        olp.put("Charles", "667-093-4567");
        olp.printTable();
        olp.put("Christine", "104-356-2111");
        olp.printTable();
        olp.put("Alexander", "590-260-9001");
        olp.printTable();
        olp.put("Carl", "850-102-8974");
        olp.printTable();
        olp.put("Paulette", "215-334-6807");
        olp.printTable();
        olp.put("Aditya", "890-123-0209");
        olp.printTable();
        olp.put("Arnold", "894-590-0011");
        olp.printTable();
        olp.put("Jacqueline", "321-990-2801"); 
        olp.printTable();
        olp.put("Yi", "921-350-4314"); 
        olp.printTable();
        olp.put("Tiffany", "810-279-0711"); 
        olp.printTable();
        olp.put("Nakeesha", "708-890-2234");
        olp.printTable();
        olp.put("Jason", "900-701-2902");
        olp.printTable();
        olp.put("Jessie", "705-120-7500");
        olp.printTable();
        olp.put("Helen", "810-206-9450");
        olp.printTable();
        olp.put("Mary", "888-121-3340");
        olp.printTable();
    	
        
        assertEquals("900-701-2902", olp.get("Jason"));
    }
    
    @Test
    public void testPBGetOwnerSC() {
    	pb = new Phonebook(SEPARATE_CHAINING, SEPARATE_CHAINING);
        pb.addEntry("DeAndre", "367-900-1199");
        pb.addEntry("Charles", "667-093-4567");
        pb.addEntry("Christine", "104-356-2111");
        pb.addEntry("Alexander", "590-260-9001");
        pb.addEntry("Carl", "850-102-8974");
        pb.addEntry("Paulette", "215-334-6807");
        pb.addEntry("Aditya", "890-123-0209");
        pb.addEntry("Arnold", "894-590-0011");    
        pb.addEntry("Jacqueline", "321-990-2801");  
        pb.addEntry("Yi", "921-350-4314"); 
        pb.addEntry("Tiffany", "810-279-0711"); 
        pb.addEntry("Nakeesha", "708-890-2234");
        pb.addEntry("Jason", "900-701-2902");
        pb.addEntry("Jessie", "705-120-7500");
        pb.addEntry("Helen", "810-206-9450");
        pb.addEntry("Mary", "888-121-3340");

        assertEquals("Jason", pb.getOwnerOf("900-701-2902"));
    }
    
    @Test
    public void testPBGetOwnerQP() {
    	pb = new Phonebook(QUADRATIC_PROBING, QUADRATIC_PROBING);
        pb.addEntry("DeAndre", "367-900-1199");
        pb.addEntry("Charles", "667-093-4567");
        pb.addEntry("Christine", "104-356-2111");
        pb.addEntry("Alexander", "590-260-9001");
        pb.addEntry("Carl", "850-102-8974");
        pb.addEntry("Paulette", "215-334-6807");
        pb.addEntry("Aditya", "890-123-0209");
        pb.addEntry("Arnold", "894-590-0011");    
        pb.addEntry("Jacqueline", "321-990-2801");  
        pb.addEntry("Yi", "921-350-4314"); 
        pb.addEntry("Tiffany", "810-279-0711"); 
        pb.addEntry("Nakeesha", "708-890-2234");
        pb.addEntry("Jason", "900-701-2902");
        pb.addEntry("Jessie", "705-120-7500");
        pb.addEntry("Helen", "810-206-9450");
        pb.addEntry("Mary", "888-121-3340");

        assertEquals("Jason", pb.getOwnerOf("900-701-2902"));
    }
    
    
    @Test
    public void testComplicatedQP() {
    	QuadraticProbingHashTable qp = new QuadraticProbingHashTable(false);
    	qp.put("DeAndre", "0");
    	qp.put("DeAndre", "0");
    	qp.put("Paulette", "1");
    	qp.put("Paulette", "1");
    	qp.printTable();
    	qp.put("Jason", "2");
    	qp.printTable();
    	qp.put("Jason", "2");
    	qp.put("Helen", "3");
    	qp.printTable();
    	qp.put("Helen", "3");
    	qp.put("Tian", "T");
    	qp.put("Tian", "T");
    	System.out.println("********Before Deletion:*******");
    	qp.printTable();
    	qp.remove("Jason");
    	qp.remove("Jason");
    	System.out.println("*******After Deletion:********");
    	qp.printTable();
    	assertEquals(qp.get("Jason"), null);
    }
    
    @Test
    public void testPutOLP() {
    	OrderedLinearProbingHashTable olp = new OrderedLinearProbingHashTable(false);
    	olp.put("DeAndre", "367-900-1199");//DeAndre
    	olp.printTable();
    	olp.put("Charles", "667-093-4567");//Charles
    	olp.printTable();
    	olp.put("Christine", "104-356-2111");//Christine
    	olp.printTable();
    	olp.put("Alexander", "590-260-9001");//Alexander
    	olp.printTable();
    	olp.put("Carl", "850-102-8974");//Carl
    	//resize
    	olp.printTable();
    }
    

}
