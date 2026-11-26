package com.mycompany.petclinic.model;

public class Payment_Method {

    private int paymentMethodID;
    private String typeName;
    private String description;

    // Constructor khi thêm mới
    public Payment_Method(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }

    // Constructor khi đọc từ database
    public Payment_Method(int paymentMethodID, String typeName,
                         String description) {
        this.paymentMethodID = paymentMethodID;
        this.typeName = typeName;
        this.description = description;
    }

    public int getPaymentMethodID() {
        return paymentMethodID;
    }

    public void setPaymentMethodID(int paymentMethodID) {
        this.paymentMethodID = paymentMethodID;
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

    // JComboBox hiển thị tên phương thức
    @Override
    public String toString() {
        return typeName;
    }
}
