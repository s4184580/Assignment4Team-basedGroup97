import java.util.regex.Matcher; // Used to apply a compiled Pattern against a string
import java.util.regex.Pattern; // Used to compile regular expressions for validation

// Validates Driver objects against the Driver Conditions (D1–D5) defined in the specification
// All validation methods return true if the input is valid, false otherwise
public class DriverValidator {

    // D1 rule: validates that the driverID follows the required 10-character structure
    // – Characters 1–2: digits between 2 and 9
    // – Characters 3–8: at least two special characters (non-alphanumeric)
    // – Characters 9–10: uppercase letters A–Z
    public boolean isValidDriverIDStructure(String driverID) {
        if (driverID == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        // Compile a regex: first two chars [2-9], six any chars, last two uppercase letters
        Pattern pattern = Pattern.compile("[2-9]{2}.{6}[A-Z]{2}");
        Matcher matcher = pattern.matcher(driverID); // Apply the pattern to the driverID

        if (!matcher.matches()) {
            return false; // Reject if the overall structure does not match
        }

        // Count how many special characters appear between positions 3 and 8 (index 2–7)
        int specialCharacterCount = 0;

        // Pattern that matches exactly one special character (not alphanumeric or whitespace)
        Pattern specialPattern = Pattern.compile("[^A-Za-z0-9\\s]");

        for (int i = 2; i <= 7; i++) {
            String character = String.valueOf(driverID.charAt(i)); // Extract single character as string
            Matcher specialMatcher = specialPattern.matcher(character); // Check if it is special

            if (specialMatcher.matches()) {
                specialCharacterCount++; // Increment count if the character is a special character
            }
        }

        return specialCharacterCount >= 2; // At least two special characters must be present in positions 3–8
    }

    // D2 rule: validates that the address follows Street Number|Street Name|City|State|Country format
    // – Street Number must be numeric; all five sections must be non-empty
    public boolean isValidAddress(String address) {
        if (address == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        String[] parts = address.split("\\|", -1); // Split by pipe delimiter; -1 keeps empty trailing parts

        if (parts.length != 5) {
            return false; // Address must contain exactly 5 pipe-separated sections
        }

        Pattern streetNumberPattern = Pattern.compile("\\d+"); // Street number must be all digits
        Matcher streetNumberMatcher = streetNumberPattern.matcher(parts[0]); // Match the first section

        if (!streetNumberMatcher.matches()) {
            return false; // Reject if the street number is not purely numeric
        }

        for (String part : parts) {
            if (part.trim().isEmpty()) {
                return false; // Reject if any section is blank or whitespace only
            }
        }

        return true; // All address checks passed
    }

    // D3 rule: validates that the birthdate follows the DD-MM-YYYY format and represents a real calendar date
    public boolean isValidBirthdate(String birthdate) {
        if (birthdate == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        // Compile regex to match exactly two-digit day, two-digit month, four-digit year
        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
        Matcher matcher = pattern.matcher(birthdate); // Apply the pattern to the birthdate

        if (!matcher.matches()) {
            return false; // Reject if the format does not match DD-MM-YYYY
        }

        String[] parts = birthdate.split("-");

        int day = Integer.parseInt(parts[0]);   // Parse the day portion
        int month = Integer.parseInt(parts[1]); // Parse the month portion
        int year = Integer.parseInt(parts[2]);  // Parse the year portion

        if (month < 1 || month > 12) {
            return false; // Month must be between 1 and 12 inclusive
        }

        if (day < 1 || day > getDaysInMonth(month, year)) {
            return false; // Day must be valid for the given month and year (rejects e.g. 31-02)
        }

        return true; // Birthdate format and value are valid
    }

    // Checks that the license type is one of the four allowed values: Light, Medium, Heavy, PublicTransport
    public boolean isValidLicenseType(String licenseType) {
        if (licenseType == null) {
            return false; // Reject null inputs to avoid NullPointerException
        }

        Pattern pattern = Pattern.compile("Light|Medium|Heavy|PublicTransport"); // Only four permitted values
        Matcher matcher = pattern.matcher(licenseType); // Apply the pattern to the license type

        return matcher.matches(); // True only if the value is exactly one of the allowed types
    }

    // Checks all basic driver details before adding or updating a driver
    // – Validates ID structure (D1), name, experience, license type, address (D2), and birthdate (D3)
    public boolean isValidDriver(Driver driver) {
        if (driver == null) {
            return false; // Reject if the driver object itself is null
        }

        if (!isValidDriverIDStructure(driver.getDriverID())) {
            return false; // Reject if the driver ID violates D1 structural rules
        }

        if (driver.getName() == null || driver.getName().trim().isEmpty()) {
            return false; // Driver must have a non-null and non-blank name
        }

        if (driver.getExperienceYears() < 0) {
            return false; // Experience years must not be negative
        }

        if (!isValidLicenseType(driver.getLicenseType())) {
            return false; // License type must be one of the four permitted values
        }

        if (!isValidAddress(driver.getAddress())) {
            return false; // Address must follow the D2 pipe-separated format
        }

        if (!isValidBirthdate(driver.getBirthdate())) {
            return false; // Birthdate must follow the D3 DD-MM-YYYY format and be a real date
        }

        return true; // All driver fields passed validation
    }

    // D4 rule: if experience is more than 10 years, license type cannot be changed during update
    public boolean canUpdateLicense(Driver existingDriver, Driver updatedDriver) {
        if (existingDriver == null || updatedDriver == null) {
            return false; // Both driver objects must be non-null
        }

        if (existingDriver.getLicenseType() == null || updatedDriver.getLicenseType() == null) {
            return false; // Both license values must be non-null to compare safely
        }

        if (existingDriver.getExperienceYears() > 10) {
            return existingDriver.getLicenseType().equals(updatedDriver.getLicenseType()); // License must remain the same
        }

        return true; // Drivers with 10 or fewer years of experience may change their license
    }

    // D5 rule: driverID and name cannot be changed during update operations
    public boolean hasImmutableFieldsUnchanged(Driver existingDriver, Driver updatedDriver) {
        if (existingDriver == null || updatedDriver == null) {
            return false; // Both driver objects must be non-null
        }

        if (existingDriver.getDriverID() == null || updatedDriver.getDriverID() == null) {
            return false; // Both driver IDs must be non-null to compare safely
        }

        if (existingDriver.getName() == null || updatedDriver.getName() == null) {
            return false; // Both names must be non-null to compare safely
        }

        // The driver ID and name must both be identical to the stored record
        return existingDriver.getDriverID().equals(updatedDriver.getDriverID()) &&
                existingDriver.getName().equals(updatedDriver.getName());
    }

    // Returns the maximum number of days in a given month, accounting for leap years in February
    private int getDaysInMonth(int month, int year) {
        if (month == 2) {
            if (isLeapYear(year)) {
                return 29; // February has 29 days in a leap year
            }

            return 28; // February has 28 days in a non-leap year
        }

        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30; // April, June, September, and November have 30 days
        }

        return 31; // All other months have 31 days
    }

    // Checks whether a year is a leap year
    private boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true; // Divisible by 400 — always a leap year (e.g. 2000)
        }

        if (year % 100 == 0) {
            return false; // Divisible by 100 but not 400 — not a leap year (e.g. 1900)
        }

        return year % 4 == 0; // Divisible by 4 but not 100 — a leap year (e.g. 2024)
    }
}
