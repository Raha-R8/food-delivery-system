package Entities;

public class Restaurant {
    private int id;
    private String name;
    private String city;
    private String address;
    private float minPurchase;
    private String mapLocation; // VARCHAR for map location
    private byte[] image; // MEDIUMBLOB for image

    public Restaurant(int id, String name, String city, String address, float minPurchase) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.minPurchase = minPurchase;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getMinPurchase() {
        return minPurchase;
    }

    public void setMinPurchase(float minPurchase) {
        this.minPurchase = minPurchase;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(String mapLocation) {
        this.mapLocation = mapLocation;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
