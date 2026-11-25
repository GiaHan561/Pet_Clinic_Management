
package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class AppointmentService {

    private int appointmentID;
    private int serviceID;
    private String serviceName;
    private int quantity;
    private BigDecimal unitPrice;

    // Constructor thêm mới
    public AppointmentService(
            int appointmentID,
            int serviceID,
            int quantity,
            BigDecimal unitPrice) {

        this.appointmentID = appointmentID;
        this.serviceID = serviceID;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Constructor đầy đủ để hiển thị dữ liệu
    public AppointmentService(
            int appointmentID,
            int serviceID,
            String serviceName,
            int quantity,
            BigDecimal unitPrice) {

        this.appointmentID = appointmentID;
        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public int getServiceID() {
        return serviceID;
    }

    public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Tính thành tiền
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}

