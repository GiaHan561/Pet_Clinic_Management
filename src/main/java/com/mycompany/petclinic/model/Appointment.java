
package com.mycompany.petclinic.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private int appointmentID;
    private int customerID;
    private int petID;
    private int doctorID;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String reason;
    private String note;
    private String status;

    // Constructor khi tạo lịch hẹn mới
    public Appointment(int customerID, int petID,
                       int doctorID,
                       LocalDate appointmentDate,
                       LocalTime appointmentTime,
                       String reason, String note) {

        this.customerID = customerID;
        this.petID = petID;
        this.doctorID = doctorID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.note = note;
        this.status = "Pending";
    }

    // Constructor khi lấy dữ liệu từ database
    public Appointment(int appointmentID, int customerID,
                       int petID, int doctorID,
                       LocalDate appointmentDate,
                       LocalTime appointmentTime,
                       String reason, String note,
                       String status) {

        this(customerID, petID, doctorID,
             appointmentDate, appointmentTime,
             reason, note);

        this.appointmentID = appointmentID;
        this.status = status;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public int getPetID() {
        return petID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStatus(String status) {
        if (status == null ||
            !status.matches("Pending|Confirmed|Completed|Cancelled|NoShow")) {
            throw new IllegalArgumentException(
                "Trang thai appointment khong hop le!"
            );
        }

        this.status = status;
    }
}

