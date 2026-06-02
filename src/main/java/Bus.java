// Bus class representing a bus in the Intelligent Bus Driver Guidance System
// All fields must satisfy Bus Conditions B1–B5 defined in the specification
public class Bus {

    private String busID;    // Unique 8-digit bus identifier — must follow B1 format rules
    private int capacity;    // Maximum passenger capacity — cannot increase during updates (B2)
    private double fuelLevel; // Current fuel level — must be zero or positive
    private String fuelType; // Type of fuel used — Diesel, Hybrid, or Electricity

    // Constructor used to create a Bus object with all required bus details
    public Bus(String busID, int capacity, double fuelLevel, String fuelType) {
        this.busID = busID;         // Assign the unique bus ID
        this.capacity = capacity;   // Assign the passenger capacity
        this.fuelLevel = fuelLevel; // Assign the current fuel level
        this.fuelType = fuelType;   // Assign the fuel type
    }

    // Returns the bus's unique 8-digit ID
    public String getBusID() {
        return busID;
    }

    // Returns the bus's maximum passenger capacity
    public int getCapacity() {
        return capacity;
    }

    // Returns the bus's current fuel level
    public double getFuelLevel() {
        return fuelLevel;
    }

    // Returns the bus's fuel type (Diesel, Hybrid, or Electricity)
    public String getFuelType() {
        return fuelType;
    }
}
