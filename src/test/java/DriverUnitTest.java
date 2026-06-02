import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.File;

public class DriverUnitTest {

    // Shared validator instance used across all test methods in this class
    private DriverValidator validator = new DriverValidator();

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
    // D1 Driver ID Rules
    // D1: driverID must be exactly 10 chars, first two digits 2–9, at least two
    //     special chars in positions 3–8, last two uppercase letters A–Z.
    // -------------------------------------------------------------------------

    /**
     * D1 – Normal case: a correctly structured driverID should pass validation.
     * "29ab#$xyAB" → digits 2,9 | special chars #,$ | uppercase A,B
     */
    @Test
    void d1ValidDriverIdShouldBeAccepted() {
        // Pass a driverID that satisfies all D1 rules
        boolean result = validator.isValidDriverIDStructure("29ab#$xyAB");

        // Expect the validator to return true for a valid ID
        assertTrue(result);
    }

    /**
     * D1 – Invalid case: three different structural violations should each be rejected.
     * - First digit is 1 (must be 2–9)
     * - Middle section has no special characters (only has alphanumeric)
     * - Last two characters are not both uppercase
     */
    @Test
    void d1InvalidDriverIdStructureShouldBeRejected() {
        // First two digits start with 1, which is outside the allowed range 2–9
        boolean invalidFirstDigitResult = validator.isValidDriverIDStructure("19ab#$xyAB");

        // Middle positions (3–8) contain only alphanumeric characters — no special chars
        boolean missingSpecialCharactersResult = validator.isValidDriverIDStructure("29ab12xyAB");

        // Last character is lowercase 'b' instead of required uppercase letter
        boolean lowercaseEndingResult = validator.isValidDriverIDStructure("29ab#$xyAb");

        // Only 9 characters long — one character short of the required 10
        boolean tooShortResult = validator.isValidDriverIDStructure("29ab#$xAB");

        // 11 characters long — one character over the required 10
        boolean tooLongResult = validator.isValidDriverIDStructure("29ab#$xyzAB");

        // All five should be rejected because they violate D1
        assertFalse(invalidFirstDigitResult);
        assertFalse(missingSpecialCharactersResult);
        assertFalse(lowercaseEndingResult);
        assertFalse(tooShortResult);   // 9-character ID must be rejected
        assertFalse(tooLongResult);    // 11-character ID must be rejected
    }

    /**
     * D1 – Edge/integration case: adding two drivers with the same driverID should reject the second.
     * Uses a real DriverRepository and a temporary TXT file to test the uniqueness constraint.
     */
    @Test
    void d1DuplicateDriverIdShouldBeRejected() {
        String fileName = "driver-unit-duplicate-test.txt"; // Define a temporary TXT file name
        deleteFile(fileName); // This line of code deletes the TXT file if it already exists from a previous run,
        // so the test always starts with no existing data

        // Create a fresh repository backed by the temporary TXT file
        DriverRepository repository = new DriverRepository(fileName);

        // First driver with a valid, unique driverID
        Driver firstDriver = driver(
                "29ab#$xyAB",                              // valid driverID
                "Alice Driver",                             // name
                5,                                          // 5 years experience
                "Heavy",                                    // valid license type
                "10|Swanston Street|Melbourne|VIC|Australia", // valid address
                "15-06-1985"                                // valid birthdate
        );

        // Second driver attempting to use the same driverID as the first
        Driver duplicateDriver = driver(
                "29ab#$xyAB",                              // duplicate driverID — should be rejected
                "Bob Driver",
                4,
                "Medium",
                "20|Collins Street|Melbourne|VIC|Australia",
                "16-06-1986"
        );

        // Add the first driver — should succeed
        boolean firstResult = repository.addDriver(firstDriver);
        // Attempt to add the duplicate — should be rejected due to D1 uniqueness rule
        boolean duplicateResult = repository.addDriver(duplicateDriver);

        assertTrue(firstResult);      // First add must succeed
        assertFalse(duplicateResult); // Duplicate add must be rejected

        // deleteFile(fileName); // This line of code deletes the TXT file that was just created by this test,
        // so it does not affect the next run
    }

    // -------------------------------------------------------------------------
    // D2 Address Format
    // D2: address must follow Street Number|Street Name|City|State|Country
    // -------------------------------------------------------------------------

    /**
     * D2 – Normal case: a correctly formatted address should pass validation.
     */
    @Test
    void d2ValidAddressShouldBeAccepted() {
        // Address with all five pipe-separated sections and a numeric street number
        boolean result = validator.isValidAddress("10|Swanston Street|Melbourne|VIC|Australia");

        // Expect the validator to return true for a properly formatted address
        assertTrue(result);
    }

    /**
     * D2 – Invalid case: an address missing the Country section should be rejected.
     * Only four sections are present instead of the required five.
     */
    @Test
    void d2AddressMissingCountryShouldBeRejected() {
        // Address with only four pipe-separated sections — Country is missing
        boolean result = validator.isValidAddress("10|Swanston Street|Melbourne|VIC");

        // Expect the validator to return false because Country is required
        assertFalse(result);
    }

    /**
     * D2 – Invalid case: using commas instead of pipes as the delimiter should be rejected.
     * The required delimiter is the pipe character '|'.
     */
    @Test
    void d2AddressWithWrongDelimiterShouldBeRejected() {
        // Address using commas instead of the required pipe delimiter (matches documented test data exactly)
        boolean result = validator.isValidAddress("10,Swanston Street,Melbourne,VIC,Australia");

        // Expect the validator to return false because the delimiter is incorrect
        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // D3 Birthdate Format
    // D3: birthdate must follow DD-MM-YYYY and represent a real calendar date.
    // -------------------------------------------------------------------------

    /**
     * D3 – Normal case: a valid date in DD-MM-YYYY format should pass validation.
     */
    @Test
    void d3ValidBirthdateShouldBeAccepted() {
        // A real calendar date formatted correctly as DD-MM-YYYY
        boolean result = validator.isValidBirthdate("15-06-1985");

        // Expect the validator to return true for a correctly formatted valid date
        assertTrue(result);
    }

    /**
     * D3 – Invalid case: a date in YYYY-MM-DD format (ISO 8601) should be rejected.
     * The required format is DD-MM-YYYY, not the reverse.
     */
    @Test
    void d3BirthdateWithWrongFormatShouldBeRejected() {
        // Date in YYYY-MM-DD format — wrong order compared to the required DD-MM-YYYY
        boolean result = validator.isValidBirthdate("1985-06-15");

        // Expect the validator to return false because the format is incorrect
        assertFalse(result);
    }

    /**
     * D3 – Edge case: February 31 does not exist on the calendar and should be rejected.
     * February has at most 29 days (in a leap year), so day 31 is always invalid.
     */
    @Test
    void d3ImpossibleCalendarDateShouldBeRejected() {
        // 31-02-YYYY is an impossible date — February never has 31 days
        boolean result = validator.isValidBirthdate("31-02-1985");

        // Expect the validator to return false for a date that cannot exist
        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // D4 License Update Restriction
    // D4: if experienceYears > 10, licenseType cannot be changed during update.
    // -------------------------------------------------------------------------

    /**
     * D4 – Normal case: a driver with more than 10 years of experience cannot change their license.
     * Attempting to change from Heavy to Medium should be rejected.
     */
    @Test
    void d4DriverWithMoreThanTenYearsCannotChangeLicense() {
        // Existing driver with 11 years of experience and a Heavy license
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 11, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver trying to change the license from Heavy to Medium (not allowed by D4)
        Driver updatedDriver = driver(
                "29ab#$xyAB", "Alice Driver", 11, "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // The validator should reject the license change because experience > 10 years
        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertFalse(result); // License change must be rejected
    }

    /**
     * D4 – Edge case: a driver with exactly 10 years of experience CAN still change their license.
     * The restriction only applies when experience is strictly greater than 10.
     */
    @Test
    void d4DriverWithExactlyTenYearsCanChangeLicense() {
        // Existing driver with exactly 10 years of experience (boundary value)
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 10, "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver changing the license from Medium to Heavy (allowed at exactly 10 years)
        Driver updatedDriver = driver(
                "29ab#$xyAB", "Alice Driver", 10, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // The validator should allow the license change because experience == 10 (not > 10)
        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertTrue(result); // License change must be permitted at exactly 10 years
    }

    /**
     * D4 – Normal case: a driver with more than 10 years can still update other fields
     * as long as the license type remains unchanged.
     */
    @Test
    void d4DriverWithMoreThanTenYearsCanUpdateOtherDetailsWhenLicenseUnchanged() {
        // Existing driver with 12 years of experience and a Heavy license
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 12, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver changes only the address — the license type stays as Heavy
        Driver updatedDriver = driver(
                "29ab#$xyAB", "Alice Driver", 12, "Heavy",
                "20|Collins Street|Melbourne|VIC|Australia", // address changed
                "15-06-1985"
        );

        // The validator should allow the update because the license type was not changed
        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertTrue(result); // Update must be permitted when the license type is unchanged
    }

    // -------------------------------------------------------------------------
    // D5 Immutable Fields
    // D5: driverID and name cannot be modified during update operations.
    // -------------------------------------------------------------------------

    /**
     * D5 – Invalid case: attempting to change the driverID during an update should be rejected.
     * The driverID is immutable and must remain the same as the stored record.
     */
    @Test
    void d5ChangedDriverIdShouldBeRejectedDuringUpdate() {
        // Existing driver stored with driverID "29ab#$xyAB"
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 5, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver with a different driverID "39ab#$xyAB" — should be rejected (D5)
        Driver updatedDriver = driver(
                "39ab#$xyAB", // driverID changed — violates D5
                "Alice Driver", 5, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // The validator should detect that the driverID has changed and reject the update
        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertFalse(result); // Changed driverID must be rejected
    }

    /**
     * D5 – Invalid case: attempting to change the name during an update should be rejected.
     * The name is immutable and must remain the same as the stored record.
     */
    @Test
    void d5ChangedNameShouldBeRejectedDuringUpdate() {
        // Existing driver stored with name "Alice Driver"
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 5, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver with a different name "Alice Updated" — should be rejected (D5)
        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Updated", // name changed — violates D5
                5, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // The validator should detect that the name has changed and reject the update
        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertFalse(result); // Changed name must be rejected
    }

    /**
     * D5 – Normal case: an update that keeps both driverID and name unchanged should be accepted.
     * Only mutable fields (experience, address) are changed here.
     */
    @Test
    void d5UnchangedDriverIdAndNameShouldBeAcceptedDuringUpdate() {
        // Existing driver stored with driverID "29ab#$xyAB" and name "Alice Driver"
        Driver existingDriver = driver(
                "29ab#$xyAB", "Alice Driver", 5, "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia", "15-06-1985"
        );

        // Updated driver changes experience and address — driverID and name are unchanged
        Driver updatedDriver = driver(
                "29ab#$xyAB", // driverID unchanged — satisfies D5
                "Alice Driver", // name unchanged — satisfies D5
                6, // experience increased (mutable field)
                "Heavy",
                "20|Collins Street|Melbourne|VIC|Australia", // address changed (mutable field)
                "15-06-1985"
        );

        // The validator should allow the update because neither immutable field changed
        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertTrue(result); // Update must be permitted when driverID and name are unchanged
    }
}
