package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class PrescriptionDetail {

    private int prescriptionDetailID;
    private int prescriptionID;
    private int medicineID;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;
    private String dosage;
    private String frequency;
    private String duration;
    private String instruction;

    public PrescriptionDetail(int prescriptionID, int medicineID, int quantity,
                              BigDecimal unitPrice, String dosage, String frequency,
                              String duration, String instruction) {
        this.prescriptionID = prescriptionID;
        this.medicineID = medicineID;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instruction = instruction;
    }

    public int getPrescriptionDetailID() { return prescriptionDetailID; }
    public void setPrescriptionDetailID(int prescriptionDetailID) { this.prescriptionDetailID = prescriptionDetailID; }
    public int getPrescriptionID() { return prescriptionID; }
    public int getMedicineID() { return medicineID; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getInstruction() { return instruction; }
}