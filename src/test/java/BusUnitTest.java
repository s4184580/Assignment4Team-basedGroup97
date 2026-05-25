import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;

public class BusUnitTest {
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 5, 25);

    private BusValidator validator = new BusValidator();

    private Bus bus(String busID, int capacity, double fuelLevel, String fuelType) {
        return new Bus(busID, capacity, fuelLevel, fuelType);
    }

    private Driver driver(int experienceYears, String licenseType, String birthdate) {
        return new Driver(
                "29ab#$xyAB",
                "Test Driver",
                experienceYears,
                licenseType,
                "10|Swanston Street|Melbourne|VIC|Australia",
                birthdate
        );
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
    }

    // B1 Bus ID Rules

    @Test
    void b1ValidBusIdShouldBeAccepted() {
        boolean result = validator.isValidBusIDStructure("12345678");

        assertTrue(result);
    }

    @Test
    void b1InvalidBusIdShouldBeRejected() {
        boolean shortBusIdResult = validator.isValidBusIDStructure("1234567");
        boolean letterBusIdResult = validator.isValidBusIDStructure("1234567A");

        assertFalse(shortBusIdResult);
        assertFalse(letterBusIdResult);
    }

    @Test
    void b1DuplicateBusIdShouldBeRejected() {
        String fileName = "bus-unit-duplicate-test.txt";
        deleteFile(fileName);

        BusRepository repository = new BusRepository(fileName);

        Bus firstBus = bus("12345678", 45, 70.5, "Diesel");
        Bus duplicateBus = bus("12345678", 35, 60.0, "Hybrid");

        boolean firstResult = repository.addBus(firstBus);
        boolean duplicateResult = repository.addBus(duplicateBus);

        assertTrue(firstResult);
        assertFalse(duplicateResult);

        deleteFile(fileName);
    }

    // B2 Capacity Update Restriction

    @Test
    void b2CapacityDecreaseShouldBeAccepted() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel");
        Bus updatedBus = bus("12345678", 40, 70.0, "Diesel");

        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertTrue(result);
    }

    @Test
    void b2CapacityIncreaseShouldBeRejected() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel");
        Bus updatedBus = bus("12345678", 51, 70.0, "Diesel");

        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertFalse(result);
    }

    @Test
    void b2SameCapacityShouldBeAcceptedAsEdgeCase() {
        Bus existingBus = bus("12345678", 50, 70.0, "Diesel");
        Bus updatedBus = bus("12345678", 50, 65.0, "Diesel");

        boolean result = validator.canUpdateCapacity(existingBus, updatedBus);

        assertTrue(result);
    }

    // B3 Driver Age Restriction

    @Test
    void b3DriverOlderThanFiftyCannotDriveBusWithCapacityFiftyOrMore() {
        Driver olderDriver = driver(20, "Heavy", "24-05-1975");
        Bus largeBus = bus("12345678", 50, 80.0, "Diesel");

        boolean result = validator.passesAgeRestriction(olderDriver, largeBus, TEST_DATE);

        assertFalse(result);
    }

    @Test
    void b3DriverExactlyFiftyCanDriveBusWithCapacityFifty() {
        Driver exactlyFiftyDriver = driver(20, "Heavy", "25-05-1976");
        Bus largeBus = bus("12345678", 50, 80.0, "Diesel");

        boolean result = validator.passesAgeRestriction(exactlyFiftyDriver, largeBus, TEST_DATE);

        assertTrue(result);
    }

    @Test
    void b3DriverOlderThanFiftyCanDriveBusWithCapacityBelowFifty() {
        Driver olderDriver = driver(20, "Heavy", "24-05-1975");
        Bus smallerBus = bus("12345678", 49, 80.0, "Diesel");

        boolean result = validator.passesAgeRestriction(olderDriver, smallerBus, TEST_DATE);

        assertTrue(result);
    }

    // B4 Electric Bus Restriction

    @Test
    void b4DriverWithLessThanFiveYearsCannotDriveElectricBus() {
        Driver inexperiencedDriver = driver(4, "Heavy", "15-06-1990");
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity");

        boolean result = validator.passesElectricExperienceRestriction(inexperiencedDriver, electricBus);

        assertFalse(result);
    }

    @Test
    void b4DriverWithExactlyFiveYearsCanDriveElectricBus() {
        Driver experiencedDriver = driver(5, "Heavy", "15-06-1990");
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity");

        boolean result = validator.passesElectricExperienceRestriction(experiencedDriver, electricBus);

        assertTrue(result);
    }

    @Test
    void b4DriverWithLessThanFiveYearsCanDriveNonElectricBus() {
        Driver inexperiencedDriver = driver(2, "Medium", "15-06-1990");
        Bus dieselBus = bus("12345678", 45, 90.0, "Diesel");

        boolean result = validator.passesElectricExperienceRestriction(inexperiencedDriver, dieselBus);

        assertTrue(result);
    }

    // B5 Driver Licence Restriction

    @Test
    void b5HeavyLicenceCanOperateElectricBus() {
        Driver heavyDriver = driver(7, "Heavy", "15-06-1990");
        Bus electricBus = bus("12345678", 45, 90.0, "Electricity");

        boolean result = validator.passesElectricHybridLicenceRestriction(heavyDriver, electricBus);

        assertTrue(result);
    }

    @Test
    void b5PublicTransportLicenceCanOperateHybridBus() {
        Driver publicTransportDriver = driver(7, "PublicTransport", "15-06-1990");
        Bus hybridBus = bus("12345678", 45, 90.0, "Hybrid");

        boolean result = validator.passesElectricHybridLicenceRestriction(publicTransportDriver, hybridBus);

        assertTrue(result);
    }

    @Test
    void b5MediumLicenceCannotOperateHybridBus() {
        Driver mediumDriver = driver(7, "Medium", "15-06-1990");
        Bus hybridBus = bus("12345678", 45, 90.0, "Hybrid");

        boolean result = validator.passesElectricHybridLicenceRestriction(mediumDriver, hybridBus);

        assertFalse(result);
    }
}