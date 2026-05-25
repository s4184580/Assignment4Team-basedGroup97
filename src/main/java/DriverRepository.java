import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DriverRepository {
    private String fileName;
    private DriverValidator validator;

    public DriverRepository(String fileName) {
        this.fileName = fileName;
        this.validator = new DriverValidator();
        createFileIfNotExists();
    }

    // Add operation: adds a valid and unique driver to the TXT file
    public boolean addDriver(Driver driver) {
        if (!validator.isValidDriver(driver)) {
            return false;
        }

        if (retrieveDriver(driver.getDriverID()) != null) {
            return false;
        }

        ArrayList<Driver> drivers = readAllDrivers();
        drivers.add(driver);
        writeAllDrivers(drivers);

        return true;
    }

    // Retrieve operation: finds a driver using driverID
    public Driver retrieveDriver(String driverID) {
        ArrayList<Driver> drivers = readAllDrivers();

        for (Driver driver : drivers) {
            if (driver.getDriverID().equals(driverID)) {
                return driver;
            }
        }

        return null;
    }

    // Update operation: updates an existing driver if the update is valid
    public boolean updateDriver(String driverID, Driver updatedDriver) {
        ArrayList<Driver> drivers = readAllDrivers();

        for (int i = 0; i < drivers.size(); i++) {
            Driver existingDriver = drivers.get(i);

            if (existingDriver.getDriverID().equals(driverID)) {
                if (!validator.isValidDriver(updatedDriver)) {
                    return false;
                }

                if (!validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver)) {
                    return false;
                }

                if (!validator.canUpdateLicense(existingDriver, updatedDriver)) {
                    return false;
                }

                drivers.set(i, updatedDriver);
                writeAllDrivers(drivers);

                return true;
            }
        }

        return false;
    }

    // Count operation: returns the number of drivers stored in the TXT file
    public int countDrivers() {
        return readAllDrivers().size();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(fileName);

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            System.out.println("Error creating driver file.");
        }
    }

    private ArrayList<Driver> readAllDrivers() {
        ArrayList<Driver> drivers = new ArrayList<>();

        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                String line = reader.nextLine();

                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",", -1);

                    Driver driver = new Driver(
                            parts[0],
                            parts[1],
                            Integer.parseInt(parts[2]),
                            parts[3],
                            parts[4],
                            parts[5]
                    );

                    drivers.add(driver);
                }
            }

            reader.close();
        } catch (Exception exception) {
            System.out.println("Error reading driver file.");
        }

        return drivers;
    }

    private void writeAllDrivers(ArrayList<Driver> drivers) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Driver driver : drivers) {
                writer.write(
                        driver.getDriverID() + "," +
                        driver.getName() + "," +
                        driver.getExperienceYears() + "," +
                        driver.getLicenseType() + "," +
                        driver.getAddress() + "," +
                        driver.getBirthdate() + "\n"
                );
            }

            writer.close();
        } catch (IOException exception) {
            System.out.println("Error writing driver file.");
        }
    }
}