import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverValidator {

    // D1 rule: driverID must follow the required structure
    public boolean isValidDriverIDStructure(String driverID) {
        if (driverID == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("[2-9]{2}.{6}[A-Z]{2}");
        Matcher matcher = pattern.matcher(driverID);

        if (!matcher.matches()) {
            return false;
        }

        int specialCharacterCount = 0;
        Pattern specialPattern = Pattern.compile("[^A-Za-z0-9\\s]");

        for (int i = 2; i <= 7; i++) {
            String character = String.valueOf(driverID.charAt(i));
            Matcher specialMatcher = specialPattern.matcher(character);

            if (specialMatcher.matches()) {
                specialCharacterCount++;
            }
        }

        return specialCharacterCount >= 2;
    }

    // D2 rule: address must follow Street Number|Street Name|City|State|Country
    public boolean isValidAddress(String address) {
        if (address == null) {
            return false;
        }

        String[] parts = address.split("\\|", -1);

        if (parts.length != 5) {
            return false;
        }

        Pattern streetNumberPattern = Pattern.compile("\\d+");
        Matcher streetNumberMatcher = streetNumberPattern.matcher(parts[0]);

        if (!streetNumberMatcher.matches()) {
            return false;
        }

        for (String part : parts) {
            if (part.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // D3 rule: birthdate must follow DD-MM-YYYY and be a valid date
    public boolean isValidBirthdate(String birthdate) {
        if (birthdate == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
        Matcher matcher = pattern.matcher(birthdate);

        if (!matcher.matches()) {
            return false;
        }

        String[] parts = birthdate.split("-");

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        if (month < 1 || month > 12) {
            return false;
        }

        if (day < 1 || day > getDaysInMonth(month, year)) {
            return false;
        }

        return true;
    }

    // Checks that license type is one of the allowed license types
    public boolean isValidLicenseType(String licenseType) {
        if (licenseType == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("Light|Medium|Heavy|PublicTransport");
        Matcher matcher = pattern.matcher(licenseType);

        return matcher.matches();
    }

    // Checks all basic driver details before adding or updating a driver
    public boolean isValidDriver(Driver driver) {
        if (driver == null) {
            return false;
        }

        if (!isValidDriverIDStructure(driver.getDriverID())) {
            return false;
        }

        if (driver.getName() == null || driver.getName().trim().isEmpty()) {
            return false;
        }

        if (driver.getExperienceYears() < 0) {
            return false;
        }

        if (!isValidLicenseType(driver.getLicenseType())) {
            return false;
        }

        if (!isValidAddress(driver.getAddress())) {
            return false;
        }

        if (!isValidBirthdate(driver.getBirthdate())) {
            return false;
        }

        return true;
    }

    // D4 rule: if experience is more than 10 years, license type cannot change
    public boolean canUpdateLicense(Driver existingDriver, Driver updatedDriver) {
        if (existingDriver == null || updatedDriver == null) {
            return false;
        }

        if (existingDriver.getLicenseType() == null || updatedDriver.getLicenseType() == null) {
            return false;
        }

        if (existingDriver.getExperienceYears() > 10) {
            return existingDriver.getLicenseType().equals(updatedDriver.getLicenseType());
        }

        return true;
    }

    // D5 rule: driverID and name cannot be changed during update
    public boolean hasImmutableFieldsUnchanged(Driver existingDriver, Driver updatedDriver) {
        if (existingDriver == null || updatedDriver == null) {
            return false;
        }

        if (existingDriver.getDriverID() == null || updatedDriver.getDriverID() == null) {
            return false;
        }

        if (existingDriver.getName() == null || updatedDriver.getName() == null) {
            return false;
        }

        return existingDriver.getDriverID().equals(updatedDriver.getDriverID()) &&
                existingDriver.getName().equals(updatedDriver.getName());
    }

    // Returns the number of days in a month
    private int getDaysInMonth(int month, int year) {
        if (month == 2) {
            if (isLeapYear(year)) {
                return 29;
            }

            return 28;
        }

        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        }

        return 31;
    }

    // Checks whether a year is a leap year
    private boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        }

        if (year % 100 == 0) {
            return false;
        }

        return year % 4 == 0;
    }
}