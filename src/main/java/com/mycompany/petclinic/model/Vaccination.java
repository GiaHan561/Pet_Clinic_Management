package com.mycompany.petclinic.model;

import java.time.LocalDate;

public class Vaccination {

    private int vaccinationID;
    private int visitID;
    private int vaccineID;
    private String vaccineName;
    private LocalDate injectionDate;
    private LocalDate nextInjectionDate;

    public Vaccination(int visitID, int vaccineID,
                       LocalDate injectionDate, LocalDate nextInjectionDate) {
        this.visitID = visitID;
        this.vaccineID = vaccineID;
        this.injectionDate = injectionDate;
        this.nextInjectionDate = nextInjectionDate;
    }

    public int getVaccinationID() { return vaccinationID; }
    public void setVaccinationID(int vaccinationID) { this.vaccinationID = vaccinationID; }
    public int getVisitID() { return visitID; }
    public int getVaccineID() { return vaccineID; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public LocalDate getInjectionDate() { return injectionDate; }
    public LocalDate getNextInjectionDate() { return nextInjectionDate; }

    public String getStatus() {
        LocalDate today = LocalDate.now();
        if (injectionDate.isAfter(today)) {
            return "Cần tiêm";
        }
        if (nextInjectionDate != null && !nextInjectionDate.isAfter(today)) {
            return "Đến hạn tiêm nhắc lại";
        }
        return "Đã tiêm";
    }
}