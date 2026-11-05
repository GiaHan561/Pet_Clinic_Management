package com.mycompany.petclinic.model;

import java.time.LocalDateTime;

public class Customer {

    private int customerID;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdTime;

    // Constructor khi thêm khách hàng mới
    public Customer(String fullName, String phone,
                    String email, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // Constructor khi lấy dữ liệu từ database
    public Customer(int customerID, String fullName,
                    String phone, String email,
                    String address, LocalDateTime createdTime) {
        this.customerID = customerID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdTime = createdTime;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}