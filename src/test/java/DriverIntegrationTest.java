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

/*
 * Integration tests for Driver-related CRUD operations.
 * These tests use real TXT files and real DriverRepository/DriverValidator instances
 * to verify that the full stack — validation, file write, and file read — works correctly together.
 *
 * Each test creates a dedicated TXT file, performs operations, makes assertions,
 * and then deletes the file to leave a clean environment.
 */
public class DriverIntegrationTest {

    // Helper method that constructs a Driver object from individual parameters
    private Driver driver(String driverID, String name, int experienceYears,
                          String licenseType, String address, String birthdate) {
        return new Driver(driverID, name, experienceYears, licenseType, address, birthdate);
    }

    // Helper method that deletes a TXT file if it exists, used to isolate each test
    private void deleteFile(String fileName) {
        File file = new File(fileName); // Create a reference to the file at the given path

        if (file.exists()) {
            file.delete(); // Delete the file only if it actually exists on disk
        }
    }

    // -------------------------------------------------------------------------
    // Integration Test 1 – valid drivers are stored and retrieved correctly
    // Verifies: addDriver() accepts a valid driver and retrieveDriver() reads it back
    // -------------------------------------------------------------------------

    /**
     * Integration Test 1 – Valid drivers are stored and retrieved correctly.
     *
     * Verifies that:
     * - A valid driver is accepted by addDriver()
     * - The driver is written to the real TXT file
     * - retrieveDriver() reads the TXT file and returns a matching object
     * - All field values are preserved exactly through write and read
     */
    @Test
    void validDriverShouldBeStoredAndRetrievedFromTxtFile() {
        String fileName = "driver-integration-test-1.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a DriverRepository backed by the real TXT file
        DriverRepository repository = new DriverRepository(fileName);

        // Build a valid driver that satisfies all Driver Conditions (D1–D3)
        Driver validDriver = driver(
                "29ab#$xyAB",                               // Valid driverID (D1)
                "Alice Driver",                              // Non-empty name
                6,                                           // 6 years of experience
                "Heavy",                                     // Valid license type
                "10|Swanston Street|Melbourne|VIC|Australia", // Valid address (D2)
                "15-06-1985"                                 // Valid birthdate (D3)
        );

        // Add the driver — should succeed because all fields are valid
        boolean result = repository.addDriver(validDriver);

        // Retrieve the driver from the TXT file using their driverID
        Driver retrievedDriver = repository.retrieveDriver("29ab#$xyAB");

        // The add operation must have returned true (driver was accepted)
        assertTrue(result);

        // The retrieved driver must not be null (driver was written to the TXT file)
        assertNotNull(retrievedDriver);

        // Verify each field is preserved exactly through the TXT file write and read
        assertEquals("29ab#$xyAB", retrievedDriver.getDriverID());   // driverID unchanged
        assertEquals("Alice Driver", retrievedDriver.getName());      // name unchanged
        assertEquals(6, retrievedDriver.getExperienceYears());        // experience unchanged
        assertEquals("Heavy", retrievedDriver.getLicenseType());      // license unchanged

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 2 – invalid drivers are rejected and not stored
    // Verifies: addDriver() rejects a D1-violating driver and writes nothing to the TXT file
    // -------------------------------------------------------------------------

    /**
     * Integration Test 2 – Invalid drivers are rejected and not stored.
     *
     * Verifies that:
     * - A driver with an invalid driverID is rejected by addDriver()
     * - Nothing is written to the TXT file
     * - retrieveDriver() returns null (driver was never stored)
     * - countDrivers() returns 0 (TXT file remains empty)
     */
    @Test
    void invalidDriverShouldBeRejectedAndNotStored() {
        String fileName = "driver-integration-test-2.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a DriverRepository backed by the real TXT file
        DriverRepository repository = new DriverRepository(fileName);

        // Build a driver with an invalid driverID — first digit is 1, not in range 2–9 (violates D1)
        Driver invalidDriver = driver(
                "19ab#$xyAB",                               // Invalid driverID — first digit is 1 (D1 violation)
                "Invalid Driver",
                4,
                "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        // Attempt to add the invalid driver — should be rejected
        boolean result = repository.addDriver(invalidDriver);

        // Attempt to retrieve the rejected driver — should not be found in the TXT file
        Driver retrievedDriver = repository.retrieveDriver("19ab#$xyAB");

        // The add operation must have returned false (driver was rejected)
        assertFalse(result);

        // The retrieved driver must be null (nothing was written to the TXT file)
        assertNull(retrievedDriver);

        // The count must be 0 (no records exist in the TXT file)
        assertEquals(0, repository.countDrivers());

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 3 – valid updates are persisted correctly to the TXT file
    // Verifies: updateDriver() saves the new field values and retrieveDriver() reads them back
    // -------------------------------------------------------------------------

    /**
     * Integration Test 3 – Valid updates are persisted correctly to the TXT file.
     *
     * Verifies that:
     * - A valid driver can be added successfully
     * - A valid update (license and address change) is accepted
     * - The updated values are written to and correctly read back from the TXT file
     */
    @Test
    void validUpdateShouldBeSavedInTxtFile() {
        String fileName = "driver-integration-test-3.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a DriverRepository backed by the real TXT file
        DriverRepository repository = new DriverRepository(fileName);

        // Build the original driver with exactly 10 years of experience and a Medium license
        Driver originalDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                10,                                       // Exactly 10 years — D4 restriction does NOT apply (> 10 required)
                "Medium",                                     // Original license type
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        // Build the updated driver changing license (allowed at exactly 10 years) and address
        Driver updatedDriver = driver(
                "29ab#$xyAB",   // driverID unchanged (D5)
                "Alice Driver",    // name unchanged (D5)
                10,
                "Heavy",        // License upgraded from Medium to Heavy (allowed — experience not > 10)
                "20|Collins Street|Melbourne|VIC|Australia", // Address changed
                "15-06-1985"
        );

        // Step 1: Add the original driver — should succeed
        boolean addResult = repository.addDriver(originalDriver);

        // Step 2: Update the stored driver with the new details — should succeed
        boolean updateResult = repository.updateDriver("29ab#$xyAB", updatedDriver);

        // Step 3: Retrieve the driver from the TXT file to check the update was persisted
        Driver retrievedDriver = repository.retrieveDriver("29ab#$xyAB");

        // Both operations must have succeeded
        assertTrue(addResult);
        assertTrue(updateResult);

        // The retrieved driver must not be null (was written to TXT file)
        assertNotNull(retrievedDriver);

        // The retrieved driver must reflect the updated license and address values
        assertEquals("Heavy", retrievedDriver.getLicenseType());                               // Updated license
        assertEquals("20|Collins Street|Melbourne|VIC|Australia", retrievedDriver.getAddress()); // Updated address

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // Integration Test 4 – record counts update correctly after adds and duplicate adds
    // Verifies: countDrivers() reflects only valid unique records written to the TXT file
    // -------------------------------------------------------------------------

    /**
     * Integration Test 4 – Record counts update correctly after add and duplicate add operations.
     *
     * Verifies that:
     * - Adding two unique drivers increases the count to 2
     * - Adding a duplicate driver (same driverID) is rejected and does not change the count
     * - countDrivers() reflects the exact number of valid records in the TXT file
     */
    @Test
    void recordCountShouldUpdateCorrectly() {
        String fileName = "driver-integration-test-4.txt"; // Unique TXT file for this test
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run, 
        // so the test always starts with no existing data

        // Create a DriverRepository backed by the real TXT file
        DriverRepository repository = new DriverRepository(fileName);

        // First driver — unique driverID "29ab#$xyAB"
        Driver firstDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                6,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        // Second driver — unique driverID "38cd@$uvCD"
        Driver secondDriver = driver(
                "38cd@$uvCD",
                "Bob Driver",
                4,
                "Medium",
                "20|Collins Street|Melbourne|VIC|Australia",
                "16-07-1986"
        );

        // Third driver — same driverID as firstDriver (duplicate — should be rejected by D1)
        Driver duplicateDriver = driver(
                "29ab#$xyAB",   // duplicate driverID — should be rejected
                "Duplicate Driver",
                3,
                "Light",
                "30|King Street|Melbourne|VIC|Australia",
                "17-08-1987"
        );

        // Add the first driver — should succeed
        boolean firstResult = repository.addDriver(firstDriver);

        // Add the second driver — should succeed (different driverID)
        boolean secondResult = repository.addDriver(secondDriver);

        // Add the duplicate — should be rejected (same driverID as firstDriver)
        boolean duplicateResult = repository.addDriver(duplicateDriver);

        // First and second adds must succeed; duplicate add must be rejected
        assertTrue(firstResult);
        assertTrue(secondResult);
        assertFalse(duplicateResult);

        // Only 2 valid records should exist in the TXT file (duplicate was not written)
        assertEquals(2, repository.countDrivers());

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }
}
