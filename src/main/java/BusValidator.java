import java.time.LocalDate;     // Used to represent the current date when calculating driver age
import java.util.regex.Matcher; // Used to apply a compiled Pattern against a string
import java.util.regex.Pattern;  // Used to compile regular expressions for validation

// Validates Bus objects and driver-bus assignment rules (B1–B5) defined in the specification
// All validation methods return true if the input is valid, false otherwise
public class BusValidator {

    // B1 rule: validates that the busID is exactly 8 characters long and all characters are digits
    public boolean isValidBusIDStructure(String busID) {
        if (busID == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        Pattern pattern = Pattern.compile("\\d{8}"); // Exactly 8 digit characters (0–9)
        Matcher matcher = pattern.matcher(busID); // Apply the pattern to the busID

        return matcher.matches(); // True only if the busID is exactly 8 digits
    }

    // Checks that the fuel type is one of the three allowed values: Diesel, Hybrid, Electricity
    public boolean isValidFuelType(String fuelType) {
        if (fuelType == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        Pattern pattern = Pattern.compile("Diesel|Hybrid|Electricity"); // Only three permitted values
        Matcher matcher = pattern.matcher(fuelType); // Apply the pattern to the fuel type

        return matcher.matches(); // True only if fuelType is Diesel, Hybrid, or Electricity
    }

    // Checks the basic bus details before adding or updating a bus
    // – Validates bus ID structure (B1), capacity, fuel level, and fuel type
    public boolean isValidBus(Bus bus) {
        if (bus == null) {
            return false; // Reject if the bus object itself is null
        }

        if (!isValidBusIDStructure(bus.getBusID())) {
            return false; // Reject if the bus ID violates B1 structural rules
        }

        if (bus.getCapacity() <= 0) {
            return false; // Capacity must be a positive integer
        }

        if (bus.getFuelLevel() < 0) {
            return false; // Fuel level cannot be negative (an empty tank is 0.0)
        }

        if (!isValidFuelType(bus.getFuelType())) {
            return false; // Fuel type must be one of the three permitted values
        }

        return true; // All bus fields passed validation
    }

    // B2 rule: bus capacity cannot increase during update — it may stay the same or decrease
    public boolean canUpdateCapacity(Bus existingBus, Bus updatedBus) {
        if (existingBus == null || updatedBus == null) {
            return false; // Both bus objects must be non-null
        }

        return updatedBus.getCapacity() <= existingBus.getCapacity(); // Updated capacity must not exceed existing
    }

    // B3 rule: drivers older than 50 years cannot drive buses with a capacity of 50 or more
    public boolean passesAgeRestriction(Driver driver, Bus bus, LocalDate currentDate) {
        if (driver == null || bus == null || currentDate == null) {
            return false; // All three inputs must be non-null
        }

        if (!isValidBirthdateStructure(driver.getBirthdate())) {
            return false; // Birthdate must be in DD-MM-YYYY format before computing age
        }

        int age = calculateAge(driver, currentDate); // Calculate the driver's current age

        if (age > 50 && bus.getCapacity() >= 50) {
            return false; // Drivers over 50 may not operate buses with capacity 50 or more (B3)
        }

        return true; // Driver satisfies the age restriction for this bus
    }

    // B4 rule: only drivers with at least 5 years of experience can drive electric buses
    public boolean passesElectricExperienceRestriction(Driver driver, Bus bus) {
        if (driver == null || bus == null) {
            return false; // Both inputs must be non-null
        }

        if ("Electricity".equals(bus.getFuelType()) && driver.getExperienceYears() < 5) {
            return false; // Driver lacks the required experience for an electric bus
        }

        return true; // Driver meets the experience requirement (or bus is not electric)
    }

    // B5 rule: only Heavy or PublicTransport license holders can operate electric and hybrid buses
    public boolean passesElectricHybridLicenseRestriction(Driver driver, Bus bus) {
        if (driver == null || bus == null) {
            return false; // Both inputs must be non-null
        }

        if ("Electricity".equals(bus.getFuelType()) || "Hybrid".equals(bus.getFuelType())) {
            // Only Heavy or PublicTransport license types are permitted for electric and hybrid buses
            if ("Heavy".equals(driver.getLicenseType()) ||
                    "PublicTransport".equals(driver.getLicenseType())) {
                return true; // Driver holds an acceptable license type
            }

            return false; // Driver's license type is not sufficient for this bus
        }

        return true; // Non-electric and non-hybrid buses have no license restriction
    }

    // Combined check for whether a driver is allowed to operate a specific bus
    // – Applies all driver-bus assignment rules: B1, B3, B4, and B5
    public boolean canDriverOperateBus(Driver driver, Bus bus, LocalDate currentDate) {
        if (driver == null || bus == null || currentDate == null) {
            return false; // All three inputs must be non-null
        }

        if (!isValidBus(bus)) {
            return false; // Bus must pass all B1 structural checks
        }

        if (!passesAgeRestriction(driver, bus, currentDate)) {
            return false; // Driver must satisfy the B3 age restriction
        }

        if (!passesElectricExperienceRestriction(driver, bus)) {
            return false; // Driver must have sufficient experience for electric buses (B4)
        }

        if (!passesElectricHybridLicenseRestriction(driver, bus)) {
            return false; // Driver must hold the correct license for electric/hybrid buses (B5)
        }

        return true; // All driver-bus assignment conditions are satisfied
    }

    // Checks that the birthdate string matches the DD-MM-YYYY format before age calculation
    private boolean isValidBirthdateStructure(String birthdate) {
        if (birthdate == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}"); // Exactly DD-MM-YYYY
        Matcher matcher = pattern.matcher(birthdate); // Apply the pattern to the birthdate

        return matcher.matches(); // True only if the birthdate matches DD-MM-YYYY exactly
    }

    // Calculates the driver's current age in full years from their DD-MM-YYYY birthdate
    private int calculateAge(Driver driver, LocalDate currentDate) {
        String[] birthdateParts = driver.getBirthdate().split("-"); // Split DD-MM-YYYY into parts

        int day = Integer.parseInt(birthdateParts[0]);   // Extract the birth day
        int month = Integer.parseInt(birthdateParts[1]); // Extract the birth month
        int year = Integer.parseInt(birthdateParts[2]);  // Extract the birth year

        int age = currentDate.getYear() - year; // Start with the difference between years

        // Subtract one year if the birthday has not yet occurred this calendar year
        if (currentDate.getMonthValue() < month ||
                (currentDate.getMonthValue() == month && currentDate.getDayOfMonth() < day)) {
            age--; // If the birthday has not happened yet this year — reduce age by one
        }

        return age; // Return the driver's age in complete years
    }
}
