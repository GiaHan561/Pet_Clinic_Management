package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class Vaccine {

    private final int vaccineID;
    private final String name;
    private final String manufacturer;
    private final String description;
    private final BigDecimal price;
    private final Integer rcmInterval;

    public Vaccine(int vaccineID, String name, String manufacturer,
                   String description, BigDecimal price, Integer rcmInterval) {
        this.vaccineID = vaccineID;
        this.name = name;
        this.manufacturer = manufacturer;
        this.description = description;
        this.price = price;
        this.rcmInterval = rcmInterval;
    }

    public int getVaccineID() { return vaccineID; }
    public String getName() { return name; }
    public String getManufacturer() { return manufacturer; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getRcmInterval() { return rcmInterval; }
}