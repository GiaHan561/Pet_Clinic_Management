package com.mycompany.petclinic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetOwnerInfo {

    private final String petName;
    private final String typeName;
    private final String gender;
    private final LocalDate dateOfBirth;
    private final BigDecimal weight;
    private final String petNote;
    private final String ownerName;
    private final String ownerPhone;
    private final String ownerEmail;
    private final String ownerAddress;

    public PetOwnerInfo(String petName, String typeName, String gender,
                        LocalDate dateOfBirth, BigDecimal weight, String petNote,
                        String ownerName, String ownerPhone,
                        String ownerEmail, String ownerAddress) {
        this.petName = petName;
        this.typeName = typeName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.petNote = petNote;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
        this.ownerAddress = ownerAddress;
    }

    public String getPetName() { return petName; }
    public String getTypeName() { return typeName; }
    public String getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public BigDecimal getWeight() { return weight; }
    public String getPetNote() { return petNote; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getOwnerAddress() { return ownerAddress; }
}