package com.mycompany.petclinic.model;

public class Diagnosis {

    private int diagnosisID;
    private int visitID;
    private String diagnosisName;
    private String description;
    private String severity;

    public Diagnosis(int visitID, String diagnosisName,
                     String description, String severity) {
        this.visitID = visitID;
        this.diagnosisName = diagnosisName;
        this.description = description;
        this.severity = severity;
    }

    public int getDiagnosisID() { return diagnosisID; }
    public void setDiagnosisID(int diagnosisID) { this.diagnosisID = diagnosisID; }
    public int getVisitID() { return visitID; }
    public String getDiagnosisName() { return diagnosisName; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
}