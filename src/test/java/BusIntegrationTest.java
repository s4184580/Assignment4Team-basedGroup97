import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.File;

public class BusIntegrationTest {

    private Bus bus(String busID, int capacity, double fuelLevel, String fuelType) {
        return new Bus(busID, capacity, fuelLevel, fuelType);
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void validBusShouldBeStoredAndRetrievedFromTxtFile() {
        String fileName = "bus-integration-test-1.txt";
        deleteFile(fileName);

        BusRepository repository = new BusRepository(fileName);

        Bus validBus = bus("12345678", 45, 80.5, "Diesel");

        boolean result = repository.addBus(validBus);

        Bus retrievedBus = repository.retrieveBus("12345678");

        assertTrue(result);
        assertNotNull(retrievedBus);
        assertEquals("12345678", retrievedBus.getBusID());
        assertEquals(45, retrievedBus.getCapacity());
        assertEquals(80.5, retrievedBus.getFuelLevel());
        assertEquals("Diesel", retrievedBus.getFuelType());

        deleteFile(fileName);
    }

    @Test
    void invalidBusShouldBeRejectedAndNotStored() {
        String fileName = "bus-integration-test-2.txt";
        deleteFile(fileName);

        BusRepository repository = new BusRepository(fileName);

        Bus invalidBus = bus("1234567A", 45, 80.5, "Diesel");

        boolean result = repository.addBus(invalidBus);

        Bus retrievedBus = repository.retrieveBus("1234567A");

        assertFalse(result);
        assertNull(retrievedBus);
        assertEquals(0, repository.countBuses());

        deleteFile(fileName);
    }

    @Test
    void validCapacityDecreaseShouldBeSavedInTxtFile() {
        String fileName = "bus-integration-test-3.txt";
        deleteFile(fileName);

        BusRepository repository = new BusRepository(fileName);

        Bus originalBus = bus("12345678", 60, 80.5, "Diesel");
        Bus updatedBus = bus("12345678", 45, 75.0, "Diesel");

        boolean addResult = repository.addBus(originalBus);
        boolean updateResult = repository.updateBus("12345678", updatedBus);

        Bus retrievedBus = repository.retrieveBus("12345678");

        assertTrue(addResult);
        assertTrue(updateResult);
        assertNotNull(retrievedBus);
        assertEquals(45, retrievedBus.getCapacity());
        assertEquals(75.0, retrievedBus.getFuelLevel());

        deleteFile(fileName);
    }

    @Test
    void recordCountShouldUpdateCorrectly() {
        String fileName = "bus-integration-test-4.txt";
        deleteFile(fileName);

        BusRepository repository = new BusRepository(fileName);

        Bus firstBus = bus("12345678", 45, 80.5, "Diesel");
        Bus secondBus = bus("87654321", 35, 90.0, "Hybrid");
        Bus duplicateBus = bus("12345678", 30, 60.0, "Electricity");

        boolean firstResult = repository.addBus(firstBus);
        boolean secondResult = repository.addBus(secondBus);
        boolean duplicateResult = repository.addBus(duplicateBus);

        assertTrue(firstResult);
        assertTrue(secondResult);
        assertFalse(duplicateResult);
        assertEquals(2, repository.countBuses());

        deleteFile(fileName);
    }
}