/*

IMPORTANT NOTE: The code we have developed as a team is the result of extensive study of the Software Engineering Fundamentals (for IT)
ISYS3413/ISYS1118/ISYS3475 Course Notes such as:
Week 10 - Lecture - Part 2: Software Testing II: https://rmit.instructure.com/courses/158444/pages/week-10-learning-materials-and-activities-lectures?module_item_id=8263036
Week 10 - Lectorial: https://rmit.instructure.com/courses/158444/pages/week-10-lectorial?module_item_id=8263037
Week 11 - Lecture - Part 4: Software Evolution and Maintenance: https://rmit.instructure.com/courses/158444/pages/week-11-learning-materials-and-activities-lectures?module_item_id=8263040

We have taken the guidance of the examples provided in each one of these course
notes to guide us in writing the JUnit test code

We also used the Tutorial/Practical 10 solution as guidance on Week 11 – Software Testing II
Solution Code that was provided for the question; "Part B: Unit and Integration Testing for the Online Bookstore Platform (80 mins)"

Canvas Link: https://rmit.instructure.com/courses/158444/pages/week-11-tutorial-slash-practical?module_item_id=8263042

The solution source code was highly helpful for us in providing an example
on how to approach the Assignment 4: Team-based, and also on how the code needs
to be written for unit and integration tests and also the code required under src/main/java and src/test/java

The solutions of Tutorial/Practical 9 on Week 10 – Software Testing I helped us write the Unit Test Cases.

Canvas Link: https://rmit.instructure.com/courses/158444/pages/week-10-tutorial-slash-practical?module_item_id=8263038

*/

/*

The first two import statements are based on the Week 10 - Lecture - Part 2: Software Testing II
Course Notes of the subject Software Engineering Fundamentals (for IT) ISYS3413/ISYS1118/ISYS3475

Canvas Link: https://rmit.instructure.com/courses/158444/pages/week-10-learning-materials-and-activities-lectures?module_item_id=8263036

The notes clearly mention that we need these two import statements for JUnit.

*/

// Import all JUnit 5 assertion methods (assertTrue, assertFalse, assertNull, assertNotNull, assertEquals)
import static org.junit.jupiter.api.Assertions.*;
// Import the @Test annotation so JUnit 5 recognises each method as a test case
import org.junit.jupiter.api.Test;

// Import File to allow test methods to delete real TXT files before and after each test
import java.io.File;

/**
 * Integration tests for Bus-related CRUD operations.
 * These tests use real TXT files and real BusRepository/BusValidator instances
 * to verify that the full stack — validation, file write, and file read — works correctly together.
 *
 * Each test creates a dedicated TXT file, performs operations, makes assertions,
 * and then deletes the file to leave a clean environment.
 */
public class BusIntegrationTest {

    // Helper method that constructs a Bus object from individual parameters
    private Bus bus(String busID, int capacity, double fuelLevel, String fuelType) {
        return new Bus(busID, capacity, fuelLevel, fuelType);
    }

    // Helper method that deletes a TXT file if it exists, used to isolate each test
    private void deleteFile(String fileName) {
        File file = new File(fileName); // Create a reference to the file at the given path

        if (file.exists()) {
            file.delete(); // Delete the file only if it actually exists on disk
        }
    }

    // -------------------------------------------------------------------------
    // Integration Test 1 – valid buses are stored and retrieved correctly
    // Verifies: addBus() accepts a valid bus and retrieveBus() reads it back from the TXT file
    // -------------------------------------------------------------------------

    /*
     * Integration Test 1 – Valid buses are stored and retrieved correctly.
     *
     * Verifies that:
     * - A valid bus is accepted by addBus()
     * - The bus is written to the real TXT file
     * - retrieveBus() reads the TXT file and returns a matching object
     * - All field values are preserved exactly through write and read
     */

    @Test
    void validBusShouldBeStoredAndRetrievedFromTxtFile() {
        String fileName = "bus-integration-test-1.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a BusRepository backed by the real TXT file
        BusRepository repository = new BusRepository(fileName);

        // Build a valid bus that satisfies all Bus Conditions (B1: 8-digit ID, positive capacity, valid fuel)
        Bus validBus = bus("12345678", 45, 80.5, "Diesel");

        // Add the bus — should succeed because all fields are valid
        boolean result = repository.addBus(validBus);

        // Retrieve the bus from the TXT file using its busID
        Bus retrievedBus = repository.retrieveBus("12345678");

        // The add operation must have returned true (bus was accepted and written)
        assertTrue(result);

        // The retrieved bus must not be null (bus was written to the TXT file)
        assertNotNull(retrievedBus);

        // Verify each field is preserved exactly through the TXT file write and read
        assertEquals("12345678", retrievedBus.getBusID());  // busID unchanged
        assertEquals(45, retrievedBus.getCapacity());        // capacity unchanged
        assertEquals(80.5, retrievedBus.getFuelLevel());     // fuelLevel unchanged
        assertEquals("Diesel", retrievedBus.getFuelType());  // fuelType unchanged

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 2 – invalid buses are rejected and not stored
    // Verifies: addBus() rejects a B1-violating bus and writes nothing to the TXT file
    // -------------------------------------------------------------------------

    /*
     * Integration Test 2 – Invalid buses are rejected and not stored.
     *
     * Verifies that:
     * - A bus with an invalid busID (contains a letter) is rejected by addBus()
     * - Nothing is written to the TXT file
     * - retrieveBus() returns null (bus was never stored)
     * - countBuses() returns 0 (TXT file remains empty)
     */

    @Test
    void invalidBusShouldBeRejectedAndNotStored() {
        String fileName = "bus-integration-test-2.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a BusRepository backed by the real TXT file
        BusRepository repository = new BusRepository(fileName);

        // Build a bus with an invalid busID — "1234567A" contains a letter, which violates B1
        Bus invalidBus = bus("1234567A", 45, 80.5, "Diesel"); // Last character 'A' is not a digit

        // Attempt to add the invalid bus — should be rejected due to B1 violation
        boolean result = repository.addBus(invalidBus);

        // Attempt to retrieve the rejected bus — should not be found in the TXT file
        Bus retrievedBus = repository.retrieveBus("1234567A");

        // The add operation must have returned false (bus was rejected)
        assertFalse(result);

        // The retrieved bus must be null (nothing was written to the TXT file)
        assertNull(retrievedBus);

        // The count must be 0 (no records exist in the TXT file)
        assertEquals(0, repository.countBuses());

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 3 – valid capacity decrease updates are persisted correctly
    // Verifies: updateBus() saves the reduced capacity and retrieveBus() reads it back
    // -------------------------------------------------------------------------

    /*
     * Integration Test 3 – Valid capacity decrease updates are persisted correctly.
     *
     * Verifies that:
     * - A valid bus can be added successfully
     * - A valid update (capacity decrease and fuel level change) is accepted (B2 allows decrease)
     * - The updated values are written to and correctly read back from the TXT file
     */
    
    @Test
    void validCapacityDecreaseShouldBeSavedInTxtFile() {
        String fileName = "bus-integration-test-3.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a BusRepository backed by the real TXT file
        BusRepository repository = new BusRepository(fileName);

        // Build the original bus with capacity 60
        Bus originalBus = bus("12345678", 60, 80.5, "Diesel");

        // Build the updated bus with a reduced capacity of 45 — a decrease is allowed by B2
        Bus updatedBus = bus("12345678", 45, 75.0, "Diesel"); // Capacity decreased from 60 to 45

        // Step 1: Add the original bus — should succeed
        boolean addResult = repository.addBus(originalBus);

        // Step 2: Update the stored bus with the new details — should succeed (capacity decreased)
        boolean updateResult = repository.updateBus("12345678", updatedBus);

        // Step 3: Retrieve the bus from the TXT file to check the update was persisted
        Bus retrievedBus = repository.retrieveBus("12345678");

        // Both operations must have succeeded
        assertTrue(addResult);
        assertTrue(updateResult);

        // The retrieved bus must not be null (was written to the TXT file after update)
        assertNotNull(retrievedBus);

        // The retrieved bus must reflect the updated capacity and fuel level values
        assertEquals(45, retrievedBus.getCapacity());    // Capacity reduced from 60 to 45
        assertEquals(75.0, retrievedBus.getFuelLevel()); // Fuel level updated from 80.5 to 75.0

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 4 – record counts update correctly after adds and duplicate adds
    // Verifies: countBuses() reflects only valid unique records written to the TXT file
    // -------------------------------------------------------------------------

    /*
     * Integration Test 4 – Record counts update correctly after add and duplicate add operations.
     *
     * Verifies that:
     * - Adding two unique buses increases the count to 2
     * - Adding a duplicate bus (same busID) is rejected and does not change the count
     * - countBuses() reflects the exact number of valid records in the TXT file
     */

    @Test
    void recordCountShouldUpdateCorrectly() {
        String fileName = "bus-integration-test-4.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a BusRepository backed by the real TXT file
        BusRepository repository = new BusRepository(fileName);

        // First bus — unique busID "12345678"
        Bus firstBus = bus("12345678", 45, 80.5, "Diesel");

        // Second bus — unique busID "87654321"
        Bus secondBus = bus("87654321", 35, 90.0, "Hybrid");

        // Third bus — same busID as firstBus (duplicate — should be rejected by B1 uniqueness rule)
        Bus duplicateBus = bus("12345678", 30, 60.0, "Electricity"); // duplicate busID

        // Add the first bus — should succeed
        boolean firstResult = repository.addBus(firstBus);

        // Add the second bus — should succeed (different busID)
        boolean secondResult = repository.addBus(secondBus);

        // Add the duplicate — should be rejected (same busID as firstBus)
        boolean duplicateResult = repository.addBus(duplicateBus);

        // First and second adds must succeed; duplicate add must be rejected
        assertTrue(firstResult);
        assertTrue(secondResult);
        assertFalse(duplicateResult);

        // Only 2 valid records should exist in the TXT file (duplicate was not written)
        assertEquals(2, repository.countBuses());

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }
}
