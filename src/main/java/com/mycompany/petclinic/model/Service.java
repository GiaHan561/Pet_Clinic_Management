
package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private BigDecimal price;
    private int duration;
    private String status;

    // Constructor thêm mới
    public Service(String serviceName, String description,
                   BigDecimal price, int duration, String status) {
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.status = status;
    }

    // Constructor đầy đủ
    public Service(int serviceId, String serviceName,
                   String description, BigDecimal price,
                   int duration, String status) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.status = status;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

