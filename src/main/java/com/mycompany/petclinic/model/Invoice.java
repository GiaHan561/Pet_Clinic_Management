
package com.mycompany.petclinic.model;

import java.math.BigDecimal;

public class Invoice {
    public static final String STATUS_UNPAID = "Unpaid";
    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_CANCELLED = "Cancelled";
    // Cot cua bang INVOICE
    private int invoiceID;
    private int visitID;
    private BigDecimal discount;
    private BigDecimal total;
    private String status;
    // Chi de hien thi( JOIN)
    private int customerID;
    private String customerName;
    private String petName;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    // Lap hoa don moi
    public Invoice(int visitID, BigDecimal discount, BigDecimal total){
       this.visitID = visitID;
       this.discount = discount;
       this.total = total;
       this.status = STATUS_UNPAID;
    }
    // ?
    public Invoice(int invoiceID, int visitID, BigDecimal discount, BigDecimal total, String status, int customerID, String customerName, String petName, BigDecimal paidAmount){
        this.invoiceID = invoiceID;
        this.visitID = visitID;
        this.discount = discount;
        this.total = total;
        this.status = status;
        this.customerID = customerID;
        this.customerName = customerName;
        this.petName = petName;
        this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
    }
    
    public BigDecimal getRemaining(){
        BigDecimal t = total == null ? BigDecimal.ZERO : total;
        BigDecimal remaining = t.subtract(paidAmount);
        return remaining.signum() < 0 ? BigDecimal.ZERO : remaining;
    }
    // Tổng chi phí trước giảm giá = total + discount
    public BigDecimal getSubtotal() {
        BigDecimal t = total == null ? BigDecimal.ZERO : total;
        BigDecimal d = discount == null ? BigDecimal.ZERO : discount;
        return t.add(d);
    }

    public boolean isUnpaid() {
        return STATUS_UNPAID.equals(status);
    }

    // Đổi status trong DB sang chữ hiển thị trên giao diện
    public static String statusToText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case STATUS_UNPAID:
                return "Chưa thanh toán";
            case STATUS_PAID:
                return "Đã thanh toán";
            case STATUS_CANCELLED:
                return "Đã hủy";
            default:
                return status;
        }
    }

    public String getStatusText() {
        return statusToText(status);
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public int getVisitID() {
        return visitID;
    }

    public void setVisitID(int visitID) {
        this.visitID = visitID;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
    }
}
