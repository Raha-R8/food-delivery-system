package Entities;

public class Address {
    private int id;
    private String city;
    private String address;
    private String mapLocation;
    private boolean isDefault;

    public Address(int id, String city, String address, String mapLocation, boolean isDefault) {
        this.id = id;
        this.city = city;
        this.address = address;
        this.mapLocation = mapLocation;
        this.isDefault = isDefault;
    }

    public int getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return city + ", " + address + " (Default: " + isDefault + ")";
    }
}

