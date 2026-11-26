package com.mycompany.petclinic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicine {

    private final int medicineID;
    private final String categories;
    private final String name;
    private final String genericName;
    private final String description;
    private final String dosage;
    private final String unit;
    private final int quantity;
    private final BigDecimal price;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;

    public Medicine(int medicineID, String categories, String name, String genericName,
                    String description, String dosage, String unit, int quantity,
                    BigDecimal price, LocalDate manufactureDate, LocalDate expiryDate) {
        this.medicineID = medicineID;
        this.categories = categories;
        this.name = name;
        this.genericName = genericName;
        this.description = description;
        this.dosage = dosage;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
    }

    public int getMedicineID() { return medicineID; }
    public String getCategories() { return categories; }
    public String getName() { return name; }
    public String getGenericName() { return genericName; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public String getUnit() { return unit; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
}