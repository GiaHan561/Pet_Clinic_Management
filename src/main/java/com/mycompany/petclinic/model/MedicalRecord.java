package com.mycompany.petclinic.model;

import java.time.LocalDate;

public class MedicalRecord{
    private int petID;
    private LocalDate createdDate;
    private String note;
    private String allergy;
    private String bloodType;
    private int medicalRecordID;
    
    public MedicalRecord(int petID, LocalDate createdDate, String note, String allergy, String bloodType){
        this.allergy=allergy;
        this.bloodType=bloodType;
        this.createdDate=createdDate;
        this.note=note;
        this.petID=petID;
        this.medicalRecordID=medicalRecordID;
    }
    
    public int getPetID(){
        return petID;
    }
    public void setPetID(int petID){
        this.petID=petID;
    }
    
    public LocalDate getCreatedDate(){
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate){
        this.createdDate=createdDate;
    }
    
    public String getNote(){
        return note;
    }
    public void setNote(String note){
        this.note=note;
    }
    
    public String getBloodType(){
        return bloodType;
    }
    public void setBloodType(String bloodType){
        this.bloodType=bloodType;
    }
    
    public String getAllergy(){
        return allergy;
    }
    public void setAllergy(String allergy){
        this.allergy=allergy;
    }
    
    public int getMedicalRecordID(){
        return medicalRecordID;
    }
    public void setMedicalRecordID(int medicalRecordID){
        this.medicalRecordID=medicalRecordID;
    }
}
