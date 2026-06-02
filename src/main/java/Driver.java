// Driver class representing a bus driver in the Intelligent Bus Driver Guidance System
// All fields must satisfy Driver Conditions D1–D5 defined in the specification
public class Driver {

    private String driverID;        // Unique 10-character driver ID — must follow D1 format rules
    private String name;            // Full name of the driver — immutable after creation (D5)
    private int experienceYears;    // Years of driving experience — used in D4 license restriction
    private String licenseType;     // Driving license category — Light, Medium, Heavy, PublicTransport
    private String address;         // Residential address — Street Number|Street Name|City|State|Country (D2)
    private String birthdate;       // Date of birth — must follow DD-MM-YYYY format (D3)

    // Constructor used to create a Driver object with all required driver details
    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;               // Assign the driver's unique ID
        this.name = name;                       // Assign the driver's full name
        this.experienceYears = experienceYears; // Assign years of experience
        this.licenseType = licenseType;         // Assign the license category
        this.address = address;                 // Assign the formatted address
        this.birthdate = birthdate;             // Assign the date of birth
    }

    // Returns the driver's unique ID
    public String getDriverID() {
        return driverID;
    }

    // Returns the driver's full name
    public String getName() {
        return name;
    }

    // Returns the number of years of driving experience
    public int getExperienceYears() {
        return experienceYears;
    }

    // Returns the driver's license type (Light, Medium, Heavy, or PublicTransport)
    public String getLicenseType() {
        return licenseType;
    }

    // Returns the driver's residential address in Street Number|Street Name|City|State|Country format
    public String getAddress() {
        return address;
    }

    // Returns the driver's date of birth in DD-MM-YYYY format
    public String getBirthdate() {
        return birthdate;
    }
}
