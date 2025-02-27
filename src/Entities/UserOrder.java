package Entities;

import java.sql.Timestamp;

public class UserOrder {
    private Integer id;
    private long addressId;
    private Timestamp orderTime;
    private boolean isPaid;
    private String orderStatus;
    private boolean isDeleted;

    // Constructor
    public UserOrder(int id, long addressId, Timestamp orderTime, boolean isPaid, String orderStatus, boolean isDeleted) {
        this.id = id;
        this.addressId = addressId;
        this.orderTime = orderTime;
        this.isPaid = isPaid;
        this.orderStatus = orderStatus;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return "UserOrder{" +
                "id=" + id +
                ", addressId=" + addressId +
                ", orderTime=" + orderTime +
                ", isPaid=" + isPaid +
                ", orderStatus='" + orderStatus + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
