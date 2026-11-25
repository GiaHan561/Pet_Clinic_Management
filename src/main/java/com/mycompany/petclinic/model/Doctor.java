
package com.mycompany.petclinic.model;

public class Doctor {

    private int doctorID;
    private String fullName;
    private String phone;
    private String email;
    private String specialization;
    private String licenseNumber;
    private String status;

    // Constructor thêm bác sĩ
    public Doctor(String fullName, String phone, String email,
                  String specialization, String licenseNumber,
                  String status) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.status = status;
    }

    // Constructor lấy bác sĩ từ database
    public Doctor(int doctorID, String fullName, String phone,
                  String email, String specialization,
                  String licenseNumber, String status) {
        this.doctorID = doctorID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.status = status;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setDoctorID(int doctorID) {
        this.doctorID = doctorID;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

