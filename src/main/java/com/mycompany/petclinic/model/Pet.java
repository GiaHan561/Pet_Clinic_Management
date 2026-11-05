package com.mycompany.petclinic.model;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Pet {

    private int petID;
    private int customerID;
    private int typeID;
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private BigDecimal weight;
    private String note;

    // Constructor khi thêm mới
    public Pet(int customerID, int typeID, String name,
               String gender, LocalDate dateOfBirth,
               BigDecimal weight, String note) {

        this.customerID = customerID;
        this.typeID = typeID;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.note = note;
    }

    // Constructor khi đọc từ database
    public Pet(int petID, int customerID, int typeID,
               String name, String gender,
               LocalDate dateOfBirth,
               BigDecimal weight, String note) {

        this(customerID, typeID, name, gender,
             dateOfBirth, weight, note);

        this.petID = petID;
    }

    public int getPetID() {
        return petID;
    }

    public void setPetID(int petID) {
        this.petID = petID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}