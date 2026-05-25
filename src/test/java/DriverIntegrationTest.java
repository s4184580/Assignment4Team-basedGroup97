import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

public class DriverIntegrationTest {

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

    @Test
    void validDriverShouldBeStoredAndRetrievedFromTxtFile() {
        String fileName = "driver-integration-test-1.txt";
        deleteFile(fileName);

        DriverRepository repository = new DriverRepository(fileName);

        Driver validDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                6,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = repository.addDriver(validDriver);

        Driver retrievedDriver = repository.retrieveDriver("29ab#$xyAB");

        assertTrue(result);
        assertNotNull(retrievedDriver);
        assertEquals("29ab#$xyAB", retrievedDriver.getDriverID());
        assertEquals("Alice Driver", retrievedDriver.getName());
        assertEquals(6, retrievedDriver.getExperienceYears());
        assertEquals("Heavy", retrievedDriver.getLicenseType());

        deleteFile(fileName);
    }

    @Test
    void invalidDriverShouldBeRejectedAndNotStored() {
        String fileName = "driver-integration-test-2.txt";
        deleteFile(fileName);

        DriverRepository repository = new DriverRepository(fileName);

        Driver invalidDriver = driver(
                "19ab#$xyAB",
                "Invalid Driver",
                4,
                "Medium",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean result = repository.addDriver(invalidDriver);

        Driver retrievedDriver = repository.retrieveDriver("19ab#$xyAB");

        assertFalse(result);
        assertNull(retrievedDriver);
        assertEquals(0, repository.countDrivers());

        deleteFile(fileName);
    }

    @Test
    void validUpdateShouldBeSavedInTxtFile() {
        String fileName = "driver-integration-test-3.txt";
        deleteFile(fileName);

        DriverRepository repository = new DriverRepository(fileName);

        Driver originalDriver = driver(
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
                "20|Collins Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        boolean addResult = repository.addDriver(originalDriver);
        boolean updateResult = repository.updateDriver("29ab#$xyAB", updatedDriver);

        Driver retrievedDriver = repository.retrieveDriver("29ab#$xyAB");

        assertTrue(addResult);
        assertTrue(updateResult);
        assertNotNull(retrievedDriver);
        assertEquals("Heavy", retrievedDriver.getLicenseType());
        assertEquals("20|Collins Street|Melbourne|VIC|Australia", retrievedDriver.getAddress());

        deleteFile(fileName);
    }

    @Test
    void recordCountShouldUpdateCorrectly() {
        String fileName = "driver-integration-test-4.txt";
        deleteFile(fileName);

        DriverRepository repository = new DriverRepository(fileName);

        Driver firstDriver = driver(
                "29ab#$xyAB",
                "Alice Driver",
                6,
                "Heavy",
                "10|Swanston Street|Melbourne|VIC|Australia",
                "15-06-1985"
        );

        Driver secondDriver = driver(
                "38cd@$uvCD",
                "Bob Driver",
                4,
                "Medium",
                "20|Collins Street|Melbourne|VIC|Australia",
                "16-07-1986"
        );

        Driver duplicateDriver = driver(
                "29ab#$xyAB",
                "Duplicate Driver",
                3,
                "Light",
                "30|King Street|Melbourne|VIC|Australia",
                "17-08-1987"
        );

        boolean firstResult = repository.addDriver(firstDriver);
        boolean secondResult = repository.addDriver(secondDriver);
        boolean duplicateResult = repository.addDriver(duplicateDriver);

        assertTrue(firstResult);
        assertTrue(secondResult);
        assertFalse(duplicateResult);
        assertEquals(2, repository.countDrivers());

        deleteFile(fileName);
    }
}