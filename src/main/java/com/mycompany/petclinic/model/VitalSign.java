package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class VitalSign {

    private int vitalSignID;
    private int visitID;
    private BigDecimal weight;
    private Integer heartRate;
    private Integer respiratoryRate;

    public VitalSign(int visitID, BigDecimal weight,
                     Integer heartRate, Integer respiratoryRate) {
        this.visitID = visitID;
        this.weight = weight;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
    }

    public int getVitalSignID() { return vitalSignID; }
    public void setVitalSignID(int vitalSignID) { this.vitalSignID = vitalSignID; }
    public int getVisitID() { return visitID; }
    public BigDecimal getWeight() { return weight; }
    public Integer getHeartRate() { return heartRate; }
    public Integer getRespiratoryRate() { return respiratoryRate; }
}
