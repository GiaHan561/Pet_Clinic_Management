package com.mycompany.petclinic.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    // Giá trị status lưu trong database (CHECK constraint của bảng PAYMENT)
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_REFUNDED = "Refunded";

    // ----- Cột của bảng PAYMENT -----
    private int paymentID;
    private int invoiceID;
    private int paymentMethodID;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String status;

    // ----- Trường chỉ để hiển thị (lấy bằng JOIN) -----
    private String methodName;

    // Constructor khi thanh toán mới
    // paymentDate để null: database tự điền SYSDATETIME()
    public Payment(int invoiceID, int paymentMethodID, BigDecimal amount) {
        this.invoiceID = invoiceID;
        this.paymentMethodID = paymentMethodID;
        this.amount = amount;
        this.status = STATUS_SUCCESS;
    }

    // Constructor khi đọc từ database
    public Payment(int paymentID, int invoiceID, int paymentMethodID,
                   LocalDateTime paymentDate, BigDecimal amount,
                   String status, String methodName) {
        this.paymentID = paymentID;
        this.invoiceID = invoiceID;
        this.paymentMethodID = paymentMethodID;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.status = status;
        this.methodName = methodName;
    }

    // Đổi status trong DB sang chữ hiển thị trên giao diện
    public static String statusToText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case STATUS_SUCCESS:
                return "Thành công";
            case STATUS_PENDING:
                return "Đang chờ";
            case STATUS_FAILED:
                return "Thất bại";
            case STATUS_REFUNDED:
                return "Đã hoàn tiền";
            default:
                return status;
        }
    }

    public String getStatusText() {
        return statusToText(status);
    }

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public int getPaymentMethodID() {
        return paymentMethodID;
    }

    public void setPaymentMethodID(int paymentMethodID) {
        this.paymentMethodID = paymentMethodID;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}

