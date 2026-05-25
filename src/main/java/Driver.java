public class Driver {
    private String driverID;
    private String name;
    private int experienceYears;
    private String licenseType; // Light, Medium, Heavy, PublicTransport
    private String address;     // Street Number|Street Name|City|State|Country
    private String birthdate;   // DD-MM-YYYY

    // Constructor used to create a Driver object with all required driver details
    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;
        this.name = name;
        this.experienceYears = experienceYears;
        this.licenseType = licenseType;
        this.address = address;
        this.birthdate = birthdate;
    }

    public String getDriverID() {
        return driverID;
    }

    public String getName() {
        return name;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthdate() {
        return birthdate;
    }
}