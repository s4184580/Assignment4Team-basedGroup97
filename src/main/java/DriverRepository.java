/*

IMPORTANT NOTE: The code we had written below is entirely
by us and is a result of extensive study of several resources
that helped us write code for TXT File Handling in Java

We mainly used Oracle Java Documentation (Java™ Platform
Standard Ed. 8) to help us write the code that is required
alongside several other websites and YouTube Videos.

References:

    Oracle Documentation:

        https://docs.oracle.com/javase/8/docs/api/java/io/File.html
        https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html
        https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html
        https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html
        https://docs.oracle.com/javase/8/docs/api/java/lang/String.html
        https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html
        https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html
        https://docs.oracle.com/javase/8/docs/api/java/io/IOException.html


    Other Sources:

        https://web.stanford.edu/class/archive/cs/cs108/cs108.1082/106a-java-handouts/HO53Files.pdf
        https://www.youtube.com/watch?v=Pg0aoSbrqOE
        https://www.geeksforgeeks.org/java/file-handling-in-java/
        https://www.youtube.com/watch?v=C_vOMUDWV-8&t=2s
        https://medium.com/@liezelshaw_45834/file-handling-in-java-a-beginners-guide-1a54442a9c08
        https://www.youtube.com/watch?v=aU4kYFkC3r0
        https://www.youtube.com/watch?v=vZm0lHciFsQ
        https://www.youtube.com/watch?v=5pNUhouyN1Q
        https://www.youtube.com/watch?v=ScUJx4aWRi0
        https://gist.github.com/Davi-K-Silva/9edb3cb3b31e13fb19d1c34b30f53b19
        https://www.cs.swarthmore.edu/~newhall/unixhelp/Java_files.html
        https://www.codewithharry.com/tutorial/java-read-write-operations

*/


import java.io.File;       // Used to reference and check the TXT file on disk
import java.io.FileWriter; // Used to write/overwrite the contents of the TXT file
import java.io.IOException; // Checked exception thrown by file I/O operations
import java.util.ArrayList; // Used as an in-memory list of Driver objects during read/write
import java.util.Scanner;   // Used to read the TXT file line by line

// Handles all CRUD operations (Add, Retrieve, Update, Count) for Driver records
// Data is stored in a human-readable TXT file, with a header on line 1 followed
// by one driver per line in CSV format: driverID, name, experienceYears, licenseType, address, birthdate
public class DriverRepository {

    private String fileName;          // Path or name of the TXT file used to store driver records
    private DriverValidator validator; // Validator used to enforce Driver Conditions D1–D5

    // Constructor that sets up the repository and creates the TXT file if it does not already exist
    public DriverRepository(String fileName) {
        this.fileName = fileName;               // Store the TXT file name for later use
        this.validator = new DriverValidator(); // Create a new validator for all operations
        createFileIfNotExists();                // Ensure the TXT file exists before any read/write
    }

    // Add operation: validates and stores a new driver in the TXT file
    // – Returns false if validation fails (D1–D3) or if the driverID already exists (D1)
    public boolean addDriver(Driver driver) {
        if (!validator.isValidDriver(driver)) {
            return false; // Invalid driver — do not write to the TXT file
        }

        if (retrieveDriver(driver.getDriverID()) != null) {
            return false; // Duplicate driverID — do not write to the TXT file (D1 uniqueness)
        }

        ArrayList<Driver> drivers = readAllDrivers(); // Load all existing drivers into memory
        drivers.add(driver);                          // Append the new valid driver to the list
        writeAllDrivers(drivers);                     // Write the updated list back to the TXT file

        return true; // Driver was successfully added
    }

    // Retrieve operation: finds and returns a driver by their driverID
    // – Returns null if no driver with the given ID exists in the TXT file
    public Driver retrieveDriver(String driverID) {
        ArrayList<Driver> drivers = readAllDrivers(); // Load all driver records for searching

        for (Driver driver : drivers) {
            if (driver.getDriverID().equals(driverID)) {
                return driver; // Return the matching driver immediately
            }
        }

        return null; // No driver with the given ID was found
    }

    // Update operation: replaces an existing driver record with updated details
    // – Enforces D5 (immutable fields), D4 (license lock for experienced drivers), and full validation
    // – Returns false if any validation rule is violated or if the driverID is not found
    public boolean updateDriver(String driverID, Driver updatedDriver) {
        ArrayList<Driver> drivers = readAllDrivers(); // Load all driver records for in-place modification

        for (int i = 0; i < drivers.size(); i++) {
            Driver existingDriver = drivers.get(i); // Retrieve the current driver at position i

            if (existingDriver.getDriverID().equals(driverID)) {
                if (!validator.isValidDriver(updatedDriver)) {
                    return false; // Updated data contains invalid fields — reject the update
                }

                if (!validator.hasImmutableFieldsUnchanged(existingDriver, updatedDriver)) {
                    return false; // Attempt to change driverID or name — violates D5
                }

                if (!validator.canUpdateLicense(existingDriver, updatedDriver)) {
                    return false; // License change not permitted for drivers with > 10 years experience — violates D4
                }

                drivers.set(i, updatedDriver); // Replace the existing record with the updated driver
                writeAllDrivers(drivers);       // Persist the modified list back to the TXT file

                return true; // Update was successful
            }
        }

        return false; // No driver with the given ID was found — update not performed
    }

    // Count operation: returns the total number of driver records stored in the TXT file
    public int countDrivers() {
        return readAllDrivers().size(); // Load all records and return the total count
    }

    // Creates the TXT file if it does not already exist, including any missing parent directories
    private void createFileIfNotExists() {
        try {
            File file = new File(fileName); // Create a File reference for the given path

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs(); // Create all missing parent directories
            }

            if (!file.exists()) {
                file.createNewFile(); // Create an empty TXT file at the specified path
            }
        } catch (IOException exception) {
            System.out.println("Error creating driver file."); // Print error if file cannot be created
        }
    }

    // Reads all driver records from the TXT file into an in-memory ArrayList
    // – Line 1 is the header and is skipped; each subsequent non-empty line is parsed as:
    //   driverID, name, experienceYears, licenseType, address, birthdate
    private ArrayList<Driver> readAllDrivers() {
        ArrayList<Driver> drivers = new ArrayList<>(); // Empty list to hold the parsed records

        try {
            File file = new File(fileName);     // Reference the TXT file on disk
            Scanner reader = new Scanner(file); // Open the file for line-by-line reading

            boolean isFirstLine = true; // Flag to skip the header line on the first iteration

            while (reader.hasNextLine()) {
                String line = reader.nextLine(); // Read the next line from the file

                if (isFirstLine) {
                    isFirstLine = false; // Mark that the header has been skipped
                    continue;            // Skip the header line and move to data lines
                }

                if (!line.trim().isEmpty()) { // Skip blank or whitespace-only lines
                    String[] parts = line.split(",", -1); // Split the CSV line; -1 keeps empty trailing fields

                    // Reconstruct a Driver object from the six parsed CSV fields
                    Driver driver = new Driver(
                            parts[0].trim(),                   // driverID
                            parts[1].trim(),                   // name
                            Integer.parseInt(parts[2].trim()), // experienceYears (converted from String)
                            parts[3].trim(),                   // licenseType
                            parts[4].trim(),                   // address
                            parts[5].trim()                    // birthdate
                    );

                    drivers.add(driver); // Add the reconstructed driver to the in-memory list
                }
            }

            reader.close(); // Close the file reader to release the file handle
        } catch (Exception exception) {
            System.out.println("Error reading driver file."); // Print error if file cannot be read
        }

        return drivers; // Return the list of all drivers read from the TXT file
    }

    // Writes all driver records from the in-memory ArrayList back to the TXT file
    // – Completely overwrites the existing file contents
    // – Line 1 is always the header: driverID, name, experienceYears, licenseType, address, birthdate
    // – Each subsequent driver is written as: driverID, name, experienceYears, licenseType, address, birthdate
    private void writeAllDrivers(ArrayList<Driver> drivers) {
        try {
            FileWriter writer = new FileWriter(fileName); // Open in overwrite mode (not append)

            writer.write("driverID, name, experienceYears, licenseType, address, birthdate\n"); // Write the header line

            for (Driver driver : drivers) {
                writer.write(
                        driver.getDriverID() + ", " +        // Column 1: driverID
                        driver.getName() + ", " +            // Column 2: name
                        driver.getExperienceYears() + ", " + // Column 3: experienceYears
                        driver.getLicenseType() + ", " +     // Column 4: licenseType
                        driver.getAddress() + ", " +         // Column 5: address
                        driver.getBirthdate() + "\n"         // Column 6: birthdate (newline ends the record)
                );
            }

            writer.close(); // Flush and close the writer to ensure all data is saved to disk
        } catch (IOException exception) {
            System.out.println("Error writing driver file."); // Print error if file cannot be written
        }
    }
}
