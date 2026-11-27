package com.mycompany.petclinic.model;

import java.time.LocalDateTime;

public class MedicalVisit {

    private int visitID;
    private int medicalRecordID;
    private int doctorID;
    private Integer appointmentID;
    private int petID;
    private LocalDateTime visitTime;
    private String reason;
    private String status;

    public MedicalVisit(int medicalRecordID, int doctorID, Integer appointmentID,
                        int petID, LocalDateTime visitTime, String reason, String status) {
        this.medicalRecordID = medicalRecordID;
        this.doctorID = doctorID;
        this.appointmentID = appointmentID;
        this.petID = petID;
        this.visitTime = visitTime;
        this.reason = reason;
        this.status = status;
    }

    public int getVisitID() { return visitID; }
    public void setVisitID(int visitID) { this.visitID = visitID; }
    public int getMedicalRecordID() { return medicalRecordID; }
    public int getDoctorID() { return doctorID; }
    public Integer getAppointmentID() { return appointmentID; }
    public int getPetID() { return petID; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
}
