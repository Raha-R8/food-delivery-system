package Entities;

public class Restaurant {
    private int id;
    private String name;
    private String city;
    private String address;
    private float minPurchase;

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

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public float getMinPurchase() {
        return minPurchase;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMinPurchase(float minPurchase) {
        this.minPurchase = minPurchase;
    }
}
