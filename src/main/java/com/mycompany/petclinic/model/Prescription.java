package com.mycompany.petclinic.model;

import java.time.LocalDateTime;

public class Prescription {

    private int prescriptionID;
    private int visitID;
    private int doctorID;
    private LocalDateTime prescriptionDate;
    private String note;

    public Prescription(int visitID, int doctorID,
                        LocalDateTime prescriptionDate, String note) {
        this.visitID = visitID;
        this.doctorID = doctorID;
        this.prescriptionDate = prescriptionDate;
        this.note = note;
    }

    public int getPrescriptionID() { return prescriptionID; }
    public void setPrescriptionID(int prescriptionID) { this.prescriptionID = prescriptionID; }
    public int getVisitID() { return visitID; }
    public int getDoctorID() { return doctorID; }
    public LocalDateTime getPrescriptionDate() { return prescriptionDate; }
    public String getNote() { return note; }
}