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

import java.io.File;        // Used to reference and check the TXT file on disk
import java.io.FileWriter;  // Used to write/overwrite the contents of the TXT file
import java.io.IOException; // Checked exception thrown by file I/O operations
import java.util.ArrayList; // Used as an in-memory list of Bus objects during read/write
import java.util.Scanner;   // Used to read the TXT file line by line

// Handles all CRUD operations (Add, Retrieve, Update, Count) for Bus records
// Data is stored in a human-readable TXT file, with a header on line 1 followed
// by one bus per line in CSV format: busID, capacity, fuelLevel, fuelType
public class BusRepository {

    private String fileName;        // Path or name of the TXT file used to store bus records
    private BusValidator validator; // Validator used to enforce Bus Conditions B1–B5

    // Constructor that sets up the repository and creates the TXT file if it does not already exist
    public BusRepository(String fileName) {
        this.fileName = fileName;            // Store the TXT file name for later use
        this.validator = new BusValidator(); // Create a new validator for all operations
        createFileIfNotExists();             // Ensure the TXT file exists before any read/write
    }

    // Add operation: validates and stores a new bus in the TXT file
    // – Returns false if validation fails (B1) or if the busID already exists (B1 uniqueness)
    public boolean addBus(Bus bus) {
        if (!validator.isValidBus(bus)) {
            return false; // Invalid bus — do not write to the TXT file
        }

        if (retrieveBus(bus.getBusID()) != null) {
            return false; // Duplicate busID — do not write to the TXT file (B1 uniqueness)
        }

        ArrayList<Bus> buses = readAllBuses(); // Load all existing buses into memory
        buses.add(bus);                        // Append the new valid bus to the list
        writeAllBuses(buses);                  // Write the updated list back to the TXT file

        return true; // Bus was successfully added
    }

    // Retrieve operation: finds and returns a bus by its busID
    // – Returns null if no bus with the given ID exists in the TXT file
    public Bus retrieveBus(String busID) {
        ArrayList<Bus> buses = readAllBuses(); // Load all bus records for searching

        for (Bus bus : buses) {
            if (bus.getBusID().equals(busID)) {
                return bus; // Return the matching bus immediately
            }
        }

        return null; // No bus with the given ID was found
    }

    // Update operation: replaces an existing bus record with updated details
    // – Enforces B2 (capacity cannot increase) and full bus validation
    // – Returns false if any validation rule is violated or if the busID is not found
    public boolean updateBus(String busID, Bus updatedBus) {
        ArrayList<Bus> buses = readAllBuses(); // Load all bus records for in-place modification

        for (int i = 0; i < buses.size(); i++) {
            Bus existingBus = buses.get(i); // Retrieve the current bus at position i

            if (existingBus.getBusID().equals(busID)) {
                if (!validator.isValidBus(updatedBus)) {
                    return false; // Updated data contains invalid fields — reject the update
                }

                if (!existingBus.getBusID().equals(updatedBus.getBusID())) {
                    return false; // busID cannot be changed during an update
                }

                if (!validator.canUpdateCapacity(existingBus, updatedBus)) {
                    return false; // Capacity would increase — violates B2
                }

                buses.set(i, updatedBus); // Replace the existing record with the updated bus
                writeAllBuses(buses);      // Persist the modified list back to the TXT file

                return true; // Update was successful
            }
        }

        return false; // No bus with the given ID was found — update not performed
    }

    // Count operation: returns the total number of bus records stored in the TXT file
    public int countBuses() {
        return readAllBuses().size(); // Load all records and return the total count
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
            System.out.println("Error creating bus file."); // Print error if file cannot be created
        }
    }

    // Reads all bus records from the TXT file into an in-memory ArrayList
    // – Line 1 is the header and is skipped; each subsequent non-empty line is parsed as:
    //   busID, capacity, fuelLevel, fuelType
    private ArrayList<Bus> readAllBuses() {
        ArrayList<Bus> buses = new ArrayList<>(); // Empty list to hold the parsed records

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
                    String[] parts = line.split(","); // Split the CSV line into 4 fields

                    // Reconstruct a Bus object from the four parsed CSV fields
                    Bus bus = new Bus(
                            parts[0].trim(),                     // busID
                            Integer.parseInt(parts[1].trim()),   // capacity (converted from String)
                            Double.parseDouble(parts[2].trim()), // fuelLevel (converted from String)
                            parts[3].trim()                      // fuelType
                    );

                    buses.add(bus); // Add the reconstructed bus to the in-memory list
                }
            }

            reader.close(); // Close the file reader to release the file handle
        } catch (Exception exception) {
            System.out.println("Error reading bus file."); // Print error if file cannot be read
        }

        return buses; // Return the list of all buses read from the TXT file
    }

    // Writes all bus records from the in-memory ArrayList back to the TXT file
    // – Completely overwrites the existing file contents
    // – Line 1 is always the header: busID, capacity, fuelLevel, fuelType
    // – Each subsequent bus is written as: busID, capacity, fuelLevel, fuelType
    private void writeAllBuses(ArrayList<Bus> buses) {
        try {
            FileWriter writer = new FileWriter(fileName); // Open in overwrite mode (not append)

            writer.write("busID, capacity, fuelLevel, fuelType\n"); // Write the header line

            for (Bus bus : buses) {
                writer.write(
                        bus.getBusID() + ", " +     // Column 1: busID
                        bus.getCapacity() + ", " +  // Column 2: capacity
                        bus.getFuelLevel() + ", " + // Column 3: fuelLevel
                        bus.getFuelType() + "\n"    // Column 4: fuelType (newline ends the record)
                );
            }

            writer.close(); // Flush and close the writer to ensure all data is saved to disk
        } catch (IOException exception) {
            System.out.println("Error writing bus file."); // Print error if file cannot be written
        }
    }
}
