package com.mycompany.petclinic.model;

public class PetType {

    private int typeID;
    private String typeName;
    private String description;

    public PetType(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }

    public PetType(int typeID, String typeName,
                   String description) {
        this.typeID = typeID;
        this.typeName = typeName;
        this.description = description;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return typeName;
    }
}