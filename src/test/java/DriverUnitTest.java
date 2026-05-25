import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

public class DriverUnitTest {
    private DriverValidator validator = new DriverValidator();

    private Driver driver(String driverID, String name, int experienceYears,
                          String licenseType, String address, String birthdate) {
        return new Driver(driverID, name, experienceYears, licenseType, address, birthdate);
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
    }

    // D1 Driver ID Rules

    @Test
    void d1ValidDriverIdShouldBeAccepted() {
        boolean result = validator.isValidDriverIDStructure("29ab#$xyAB");

        assertTrue(result);
    }

    @Test
    void d1InvalidDriverIdStructureShouldBeRejected() {
        boolean invalidFirstDigitResult = validator.isValidDriverIDStructure("19ab#$xyAB");
        boolean missingSpecialCharactersResult = validator.isValidDriverIDStructure("29ab12xyAB");
        boolean lowercaseEndingResult = validator.isValidDriverIDStructure("29ab#$xyAb");

        assertFalse(invalidFirstDigitResult);
        assertFalse(missingSpecialCharactersResult);
        assertFalse(lowercaseEndingResult);
    }

    @Test
    void d1DuplicateDriverIdShouldBeRejected() {
        String fileName = "driver-unit-duplicate-test.txt";
        deleteFile(fileName);

        DriverRepository repository = new DriverRepository(fileName);

        Driver firstDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver duplicateDriver = driver(
                "29ab#$xyAB",
                "Bob Driver",
                4,
                "Medium",
                "20|Collins Street|Melbourne|VIC|Australia",
                "16-06-1986"
        );

        boolean firstResult = repository.addDriver(firstDriver);
        boolean duplicateResult = repository.addDriver(duplicateDriver);

        assertTrue(firstResult);
        assertFalse(duplicateResult);

        deleteFile(fileName);
    }

    // D2 Address Format

    @Test
    void d2ValidAddressShouldBeAccepted() {
        boolean result = validator.isValidAddress("10|Swanston Street|Melbourne|VIC|Australia");

        assertTrue(result);
    }

    @Test
    void d2AddressMissingCountryShouldBeRejected() {
        boolean result = validator.isValidAddress("10|Swanston Street|Melbourne|VIC");

        assertFalse(result);
    }

    @Test
    void d2AddressWithWrongDelimiterShouldBeRejected() {
        boolean result = validator.isValidAddress("10,Swanston Street,Melbourne,VIC,Australia");

        assertFalse(result);
    }

    // D3 Birthdate Format

    @Test
    void d3ValidBirthdateShouldBeAccepted() {
        boolean result = validator.isValidBirthdate("15-06-1985");

        assertTrue(result);
    }

    @Test
    void d3BirthdateWithWrongFormatShouldBeRejected() {
        boolean result = validator.isValidBirthdate("1985-06-15");

        assertFalse(result);
    }

    @Test
    void d3ImpossibleCalendarDateShouldBeRejected() {
        boolean result = validator.isValidBirthdate("31-02-1985");

        assertFalse(result);
    }

    // D4 License Update Restriction

    @Test
    void d4DriverWithMoreThanTenYearsCannotChangeLicense() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                11,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                11,
                "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertFalse(result);
    }

    @Test
    void d4DriverWithExactlyTenYearsCanChangeLicense() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                10,
                "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                10,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertTrue(result);
    }

    @Test
    void d4DriverWithMoreThanTenYearsCanUpdateOtherDetailsWhenLicenseUnchanged() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                12,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                12,
                "Heavy",
                "20|Collins Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.canUpdateLicense(existingDriver, updatedDriver);

        assertTrue(result);
    }

    // D5 Immutable Fields

    @Test
    void d5ChangedDriverIdShouldBeRejectedDuringUpdate() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "39ab#$xyAB",
                "Alice Driver",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertFalse(result);
    }

    @Test
    void d5ChangedNameShouldBeRejectedDuringUpdate() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Updated",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertFalse(result);
    }

    @Test
    void d5UnchangedDriverIdAndNameShouldBeAcceptedDuringUpdate() {
        Driver existingDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                5,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver updatedDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                6,
                "Heavy",
                "20|Collins Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver);

        assertTrue(result);
    }
}