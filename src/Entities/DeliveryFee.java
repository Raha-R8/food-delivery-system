package Entities;

public class DeliveryFee {
    private int id;
    private int maxDistance;
    private float cost;

    // Constructor
    public DeliveryFee(int id, int maxDistance, float cost) {
        this.id = id;
        this.maxDistance = maxDistance;
        this.cost = cost;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public float getCost() {
        return cost;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    // Override toString for better debugging
    @Override
    public String toString() {
        return "DeliveryFee{" +
                "id=" + id +
                ", maxDistance=" + maxDistance +
                ", cost=" + cost +
                '}';
    }
}
