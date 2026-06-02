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

// Import all JUnit 5 assertion methods (assertTrue, assertFalse, assertEquals, etc.)
import static org.junit.jupiter.api.Assertions.*;
// Import the @Test annotation so JUnit 5 recognises each method as a test case
import org.junit.jupiter.api.Test;

// Import File to allow test methods to delete temporary TXT files after each test
import java.io.File;
// Import LocalDate to supply a fixed reference date for driver age calculations (B3)
import java.time.LocalDate;

/**
 * Unit tests for Bus-related validation rules (B1–B5).
 * Each condition has at least three test cases covering normal, invalid, and edge cases.
 * Tests are isolated: any TXT files created during a test are deleted after the test.
 */
public class BusUnitTest {

    // Fixed test date used for all age-related checks (B3) to ensure consistent results
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 5, 25);

    // Shared validator instance used across all test methods in this class
    private BusValidator validator = new BusValidator();

    // Helper method that constructs a Bus object from individual parameters
    private Bus bus(String busID, int capacity, double fuelLevel, String fuelType) {
        return new Bus(busID, capacity, fuelLevel, fuelType);
    }

    // Helper method that constructs a Driver object with a fixed driverID, name, and address
    // Only the experience years, license type, and birthdate vary across test cases
    private Driver driver(int experienceYears, String licenseType, String birthdate) {
        return new Driver(
                "29ab#$xyAB",                               // A valid driverID (D1 format)
                "Test Driver",                               // A placeholder name
                experienceYears,                             // Variable: years of experience
                licenseType,                                 // Variable: license type
                "10|Swanston Street|Melbourne|VIC|Australia", // A valid address (D2 format)
                birthdate                                    // Variable: birthdate in DD-MM-YYYY
        );
    }

    // Helper method that deletes a TXT file if it exists, used to isolate each test
    private void deleteFile(String fileName) {
        File file = new File(fileName); // Create a reference to the file at the given path

        if (file.exists()) {
            file.delete(); // Delete the file only if it actually exists on disk
        }
    }

    // -------------------------------------------------------------------------
    // B1 Bus ID Rules
    // B1: busID must be exactly 8 characters long and all characters must be digits.
    // -------------------------------------------------------------------------

    /**
     * B1 – Normal case: a busID that is exactly 8 digits should pass validation.
     */
    @Test
    void b1ValidBusIdShouldBeAccepted() {
        // Pass a busID that is exactly 8 numeric characters — satisfies B1
        boolean result = validator.isValidBusIDStructure("12345678");

        // Expect the validator to return true for a valid 8-digit bus ID
        assertTrue(result);
    }

    /**
     * B1 – Invalid case: a busID that is too short or contains letters should be rejected.
     * - 7-digit ID is too short
     * - 8-character ID with a trailing letter is not all digits
     */
    @Test
    void b1InvalidBusIdShouldBeRejected() {
        // Only 7 digits — one character short of the required 8
        boolean shortBusIdResult = validator.isValidBusIDStructure("1234567");

        // 7 digits followed by the letter 'A' — not all digits
        boolean letterBusIdResult = validator.isValidBusIDStructure("1234567A");

        // 9 digits — one character over the required 8
        boolean longBusIdResult = validator.isValidBusIDStructure("123456789");

        // All three should be rejected because they violate the B1 structure rule
        assertFalse(shortBusIdResult);
        assertFalse(letterBusIdResult);
        assertFalse(longBusIdResult);  // 9-digit ID must be rejected
    }

    /**
     * B1 – Edge/integration case: adding two buses with the same busID should reject the second.
     * Uses a real BusRepository and a temporary TXT file to test the uniqueness constraint.
     */
    @Test
    void b1DuplicateBusIdShouldBeRejected() {
        String fileName = "bus-unit-duplicate-test.txt"; // Define a temporary TXT file name
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a fresh repository backed by the temporary TXT file
        BusRepository repository = new BusRepository(fileName);

        // First bus with a valid, unique busID
        Bus firstBus = bus("12345678", 45, 70.5, "Diesel");

        // Second bus attempting to use the same busID as the first
        Bus duplicateBus = bus("12345678", 35, 60.0, "Hybrid"); // duplicate busID

        // Add the first bus — should succeed
        boolean firstResult = repository.addBus(firstBus);
        // Attempt to add the duplicate — should be rejected due to B1 uniqueness rule
        boolean duplicateResult = repository.addBus(duplicateBus);

        assertTrue(firstResult);      // First add must succeed
        assertFalse(duplicateResult); // Duplicate add must be rejected

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // B2 Capacity Update Restriction
    // B2: busCapacity cannot increase during update operations; it can decrease or stay the same.
    // -------------------------------------------------------------------------

    /**
     * B2 – Normal case: reducing capacity during an update should be accepted.
     * Capacity is allowed to decrease (from 50 to 40).
     */
    @Test
    void b2CapacityDecreaseShouldBeAccepted() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel"); // Currently stored bus with capacity 50
        Bus updatedBus  = bus("12345678", 40, 70.0, "Diesel"); // Updated bus with capacity 40 (decreased)

        // The validator should allow a capacity decrease
        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertTrue(result); // Capacity decrease must be permitted
    }

    /**
     * B2 – Invalid case: increasing capacity during an update should be rejected.
     * Capacity cannot increase from 50 to 51.
     */
    @Test
    void b2CapacityIncreaseShouldBeRejected() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel"); // Currently stored bus with capacity 50
        Bus updatedBus  = bus("12345678", 51, 70.0, "Diesel"); // Updated bus with capacity 51 (increased)

        // The validator should reject a capacity increase
        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertFalse(result); // Capacity increase must be rejected
    }

    /**
     * B2 – Edge case: keeping capacity exactly the same during an update should be accepted.
     * The restriction says capacity cannot *increase* — staying the same is permitted.
     */
    @Test
    void b2SameCapacityShouldBeAcceptedAsEdgeCase() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel"); // Currently stored bus with capacity 50
        Bus updatedBus  = bus("12345678", 50, 65.0, "Diesel"); // Updated bus with same capacity (fuel level changed)

        // The validator should allow the update because the capacity has not increased
        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertTrue(result); // Same capacity must be permitted (boundary value)
    }

    // -------------------------------------------------------------------------
    // B3 Driver Age Restriction
    // B3: Drivers older than 50 years cannot drive buses with a capacity of 50 or more.
    // -------------------------------------------------------------------------

    /**
     * B3 – Normal case: a driver older than 50 should be blocked from driving a bus
     * with capacity >= 50. Driver born 24-05-1975 is 51 years old as of TEST_DATE (25-05-2026).
     */
    @Test
    void b3DriverOlderThanFiftyCannotDriveBusWithCapacityFiftyOrMore() {
        Driver olderDriver = driver(20, "Heavy", "24-05-1975"); // Driver aged 51 on TEST_DATE
        Bus largeBus = bus("12345678", 50, 80.0, "Diesel");     // Bus with capacity exactly 50

        // The age restriction should be violated (age > 50 AND capacity >= 50)
        boolean result = validator.passesAgeRestriction(olderDriver, largeBus, TEST_DATE);

        assertFalse(result); // Driver must be blocked from this bus
    }

    /**
     * B3 – Edge case: a driver who turns exactly 50 on TEST_DATE should still be permitted.
     * The restriction triggers only when age is *strictly greater than* 50.
     * Driver born 25-05-1976 turns exactly 50 on TEST_DATE (25-05-2026).
     */
    @Test
    void b3DriverExactlyFiftyCanDriveBusWithCapacityFifty() {
        Driver exactlyFiftyDriver = driver(20, "Heavy", "25-05-1976"); // Driver exactly 50 on TEST_DATE
        Bus largeBus = bus("12345678", 50, 80.0, "Diesel");            // Bus with capacity exactly 50

        // Age is exactly 50, which does not trigger the restriction (> 50 required)
        boolean result = validator.passesAgeRestriction(exactlyFiftyDriver, largeBus, TEST_DATE);

        assertTrue(result); // Driver aged exactly 50 must be permitted
    }

    /**
     * B3 – Normal case: a driver older than 50 is still permitted to drive smaller buses.
     * The restriction only applies when capacity is 50 or more.
     */
    @Test
    void b3DriverOlderThanFiftyCanDriveBusWithCapacityBelowFifty() {
        Driver olderDriver = driver(20, "Heavy", "24-05-1975"); // Driver aged 51 on TEST_DATE
        Bus smallerBus = bus("12345678", 49, 80.0, "Diesel");   // Bus with capacity 49 (below 50)

        // Age is over 50 but capacity is below 50, so the restriction does not apply
        boolean result = validator.passesAgeRestriction(olderDriver, smallerBus, TEST_DATE);

        assertTrue(result); // Driver must be permitted for buses with capacity below 50
    }

    // -------------------------------------------------------------------------
    // B4 Electric Bus Restriction
    // B4: Only drivers with at least 5 years of experience can drive electric buses.
    // -------------------------------------------------------------------------

    /**
     * B4 – Invalid case: a driver with only 4 years of experience cannot drive an electric bus.
     * The minimum required experience for electric buses is exactly 5 years.
     */
    @Test
    void b4DriverWithLessThanFiveYearsCannotDriveElectricBus() {
        Driver inexperiencedDriver = driver(4, "Heavy", "15-06-1990"); // Only 4 years experience
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity");    // Electric bus

        // The experience restriction should be violated (< 5 years AND electric bus)
        boolean result = validator.passesElectricExperienceRestriction(inexperiencedDriver, electricBus);

        assertFalse(result); // Driver must be blocked from the electric bus
    }

    /**
     * B4 – Edge case: a driver with exactly 5 years of experience should be permitted.
     * The restriction requires *at least* 5 years, so exactly 5 is sufficient.
     */
    @Test
    void b4DriverWithExactlyFiveYearsCanDriveElectricBus() {
        Driver experiencedDriver = driver(5, "Heavy", "15-06-1990"); // Exactly 5 years experience (boundary)
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity");  // Electric bus

        // Exactly 5 years meets the minimum — the restriction should not apply
        boolean result = validator.passesElectricExperienceRestriction(experiencedDriver, electricBus);

        assertTrue(result); // Driver with exactly 5 years must be permitted
    }

    /**
     * B4 – Normal case: a driver with fewer than 5 years can still drive non-electric buses.
     * The B4 restriction only applies to buses with fuelType "Electricity".
     */
    @Test
    void b4DriverWithLessThanFiveYearsCanDriveNonElectricBus() {
        Driver inexperiencedDriver = driver(2, "Medium", "15-06-1990"); // Only 2 years experience
        Bus dieselBus = bus("12345678", 45, 90.0, "Diesel");            // Diesel bus (not electric)

        // B4 does not apply to non-electric buses — the check should pass
        boolean result = validator.passesElectricExperienceRestriction(inexperiencedDriver, dieselBus);

        assertTrue(result); // Driver must be permitted for non-electric buses
    }

    // -------------------------------------------------------------------------
    // B5 Driver License Restriction
    // B5: Only Heavy or PublicTransport license holders may operate electric and hybrid buses.
    // -------------------------------------------------------------------------

    /**
     * B5 – Normal case: a driver with a Heavy license should be permitted to drive an electric bus.
     * Heavy is one of the two accepted license types for electric/hybrid buses.
     */
    @Test
    void b5HeavyLicenseCanOperateElectricBus() {
        Driver heavyDriver = driver(7, "Heavy", "15-06-1990"); // Heavy license holder
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity"); // Electric bus

        // Heavy license satisfies B5 for electric buses
        boolean result = validator.passesElectricHybridLicenseRestriction(heavyDriver, electricBus);

        assertTrue(result); // Heavy license must be permitted for electric buses
    }

    /**
     * B5 – Normal case: a driver with a PublicTransport license should be permitted to drive a hybrid bus.
     * PublicTransport is the other accepted license type for electric/hybrid buses.
     */
    @Test
    void b5PublicTransportLicenseCanOperateHybridBus() {
        Driver publicTransportDriver = driver(7, "PublicTransport", "15-06-1990"); // PublicTransport license
        Bus hybridBus = bus("12345678", 45, 90.0, "Hybrid"); // Hybrid bus

        // PublicTransport license satisfies B5 for hybrid buses
        boolean result = validator.passesElectricHybridLicenseRestriction(publicTransportDriver, hybridBus);

        assertTrue(result); // PublicTransport license must be permitted for hybrid buses
    }

    /**
     * B5 – Invalid case: a driver with a Medium license should be blocked from driving a hybrid bus.
     * Only Heavy and PublicTransport licenses are permitted for electric/hybrid buses.
     */
    @Test
    void b5MediumLicenseCannotOperateHybridBus() {
        Driver mediumDriver = driver(7, "Medium", "15-06-1990"); // Medium license holder
        Bus hybridBus = bus("12345678", 45, 90.0, "Hybrid");     // Hybrid bus

        // Medium license does not satisfy B5 for hybrid buses
        boolean result = validator.passesElectricHybridLicenseRestriction(mediumDriver, hybridBus);

        assertFalse(result); // Medium license must be blocked from hybrid buses
    }
}
