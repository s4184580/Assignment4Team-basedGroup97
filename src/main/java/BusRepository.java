import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class BusRepository {
    private String fileName;
    private BusValidator validator;

    public BusRepository(String fileName) {
        this.fileName = fileName;
        this.validator = new BusValidator();
        createFileIfNotExists();
    }

    // Add operation: adds a valid and unique bus to the TXT file
    public boolean addBus(Bus bus) {
        if (!validator.isValidBus(bus)) {
            return false;
        }

        if (retrieveBus(bus.getBusID()) != null) {
            return false;
        }

        ArrayList<Bus> buses = readAllBuses();
        buses.add(bus);
        writeAllBuses(buses);

        return true;
    }

    // Retrieve operation: finds a bus using busID
    public Bus retrieveBus(String busID) {
        ArrayList<Bus> buses = readAllBuses();

        for (Bus bus : buses) {
            if (bus.getBusID().equals(busID)) {
                return bus;
            }
        }

        return null;
    }

    // Update operation: updates an existing bus if the update is valid
    public boolean updateBus(String busID, Bus updatedBus) {
        ArrayList<Bus> buses = readAllBuses();

        for (int i = 0; i < buses.size(); i++) {
            Bus existingBus = buses.get(i);

            if (existingBus.getBusID().equals(busID)) {
                if (!validator.isValidBus(updatedBus)) {
                    return false;
                }

                if (!existingBus.getBusID().equals(updatedBus.getBusID())) {
                    return false;
                }

                if (!validator.canUpdateCapacity(existingBus, updatedBus)) {
                    return false;
                }

                buses.set(i, updatedBus);
                writeAllBuses(buses);

                return true;
            }
        }

        return false;
    }

    // Count operation: returns the number of buses stored in the TXT file
    public int countBuses() {
        return readAllBuses().size();
    }

    private void createFileIfNotExists() {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            System.out.println("Error creating bus file.");
        }
    }

    private ArrayList<Bus> readAllBuses() {
        ArrayList<Bus> buses = new ArrayList<>();

        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                String line = reader.nextLine();

                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");

                    Bus bus = new Bus(
                            parts[0],
                            Integer.parseInt(parts[1]),
                            Double.parseDouble(parts[2]),
                            parts[3]
                    );

                    buses.add(bus);
                }
            }

            reader.close();
        } catch (Exception exception) {
            System.out.println("Error reading bus file.");
        }

        return buses;
    }

    private void writeAllBuses(ArrayList<Bus> buses) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (Bus bus : buses) {
                writer.write(
                        bus.getBusID() + "," +
                        bus.getCapacity() + "," +
                        bus.getFuelLevel() + "," +
                        bus.getFuelType() + "\n"
                );
            }

            writer.close();
        } catch (IOException exception) {
            System.out.println("Error writing bus file.");
        }
    }
}