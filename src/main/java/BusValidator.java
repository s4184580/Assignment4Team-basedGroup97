import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusValidator {

    // B1 rule: busID must be exactly 8 digits
    public boolean isValidBusIDStructure(String busID) {
        if (busID == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\d{8}");
        Matcher matcher = pattern.matcher(busID);

        return matcher.matches();
    }

    // Checks that fuel type is one of the allowed fuel types
    public boolean isValidFuelType(String fuelType) {
        if (fuelType == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("Diesel|Hybrid|Electricity");
        Matcher matcher = pattern.matcher(fuelType);

        return matcher.matches();
    }

    // Checks the basic bus details before adding or updating a bus
    public boolean isValidBus(Bus bus) {
        if (bus == null) {
            return false;
        }

        if (!isValidBusIDStructure(bus.getBusID())) {
            return false;
        }

        if (bus.getCapacity() <= 0) {
            return false;
        }

        if (bus.getFuelLevel() < 0) {
            return false;
        }

        if (!isValidFuelType(bus.getFuelType())) {
            return false;
        }

        return true;
    }

    // B2 rule: bus capacity cannot increase during update
    public boolean canUpdateCapacity(Bus existingBus, Bus updatedBus) {
        if (existingBus == null || updatedBus == null) {
            return false;
        }

        return updatedBus.getCapacity() <= existingBus.getCapacity();
    }

    // B3 rule: drivers older than 50 cannot drive buses with capacity 50 or more
    public boolean passesAgeRestriction(Driver driver, Bus bus, LocalDate currentDate) {
        if (driver == null || bus == null || currentDate == null) {
            return false;
        }

        if (!isValidBirthdateStructure(driver.getBirthdate())) {
            return false;
        }

        int age = calculateAge(driver, currentDate);

        if (age > 50 && bus.getCapacity() >= 50) {
            return false;
        }

        return true;
    }

    // B4 rule: only drivers with at least 5 years of experience can drive electric buses
    public boolean passesElectricExperienceRestriction(Driver driver, Bus bus) {
        if (driver == null || bus == null) {
            return false;
        }

        if ("Electricity".equals(bus.getFuelType()) && driver.getExperienceYears() < 5) {
            return false;
        }

        return true;
    }

    // B5 rule: only Heavy or PublicTransport licence holders can operate electric and hybrid buses
    public boolean passesElectricHybridLicenceRestriction(Driver driver, Bus bus) {
        if (driver == null || bus == null) {
            return false;
        }

        if ("Electricity".equals(bus.getFuelType()) || "Hybrid".equals(bus.getFuelType())) {
            if ("Heavy".equals(driver.getLicenseType()) ||
                    "PublicTransport".equals(driver.getLicenseType())) {
                return true;
            }

            return false;
        }

        return true;
    }

    // Combined check for whether a driver is allowed to operate a bus
    public boolean canDriverOperateBus(Driver driver, Bus bus, LocalDate currentDate) {
        if (driver == null || bus == null || currentDate == null) {
            return false;
        }

        if (!isValidBus(bus)) {
            return false;
        }

        if (!passesAgeRestriction(driver, bus, currentDate)) {
            return false;
        }

        if (!passesElectricExperienceRestriction(driver, bus)) {
            return false;
        }

        if (!passesElectricHybridLicenceRestriction(driver, bus)) {
            return false;
        }

        return true;
    }

    // Checks that birthdate follows DD-MM-YYYY before calculating age
    private boolean isValidBirthdateStructure(String birthdate) {
        if (birthdate == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
        Matcher matcher = pattern.matcher(birthdate);

        return matcher.matches();
    }

    // Calculates driver's age from birthdate in DD-MM-YYYY format
    private int calculateAge(Driver driver, LocalDate currentDate) {
        String[] birthdateParts = driver.getBirthdate().split("-");

        int day = Integer.parseInt(birthdateParts[0]);
        int month = Integer.parseInt(birthdateParts[1]);
        int year = Integer.parseInt(birthdateParts[2]);

        int age = currentDate.getYear() - year;

        if (currentDate.getMonthValue() < month ||
                (currentDate.getMonthValue() == month && currentDate.getDayOfMonth() < day)) {
            age--;
        }

        return age;
    }
}