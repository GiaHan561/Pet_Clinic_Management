package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Invoice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    /**
     * Chi phí phát sinh của một lần khám.
     * Tổng chi phí = tiền dịch vụ + tiền vaccine + tiền thuốc.
     */
    public static class Charges {

        private final BigDecimal serviceCost;
        private final BigDecimal vaccineCost;
        private final BigDecimal medicineCost;

        public Charges(BigDecimal serviceCost, BigDecimal vaccineCost,
                       BigDecimal medicineCost) {
            this.serviceCost = serviceCost;
            this.vaccineCost = vaccineCost;
            this.medicineCost = medicineCost;
        }

        public BigDecimal getServiceCost() {
            return serviceCost;
        }

        public BigDecimal getVaccineCost() {
            return vaccineCost;
        }

        public BigDecimal getMedicineCost() {
            return medicineCost;
        }

        public BigDecimal getTotal() {
            return serviceCost.add(vaccineCost).add(medicineCost);
        }
    }

    // Câu SELECT dùng chung cho danh sách / tìm kiếm.
    // INVOICE không có customerID nên suy ra qua:
    // INVOICE -> MEDICAL_VISIT -> MEDICAL_RECORD -> PETS -> CUSTOMERS
    // paidAmount chỉ cộng các khoản thanh toán thành công.
    private static final String SQL_SELECT = """
        SELECT i.invoiceID, i.visitID, i.discount, i.total, i.status,
               c.customerID, c.fullName, p.name AS petName,
               ISNULL((SELECT SUM(pay.amount)
                       FROM PAYMENT pay
                       WHERE pay.invoiceID = i.invoiceID
                         AND pay.status = N'Success'), 0) AS paidAmount
        FROM INVOICE i
        JOIN MEDICAL_VISIT  mv ON mv.visitID   = i.visitID
        JOIN MEDICAL_RECORD mr ON mr.recordID  = mv.recordID
        JOIN PETS           p  ON p.petID      = mr.petID
        JOIN CUSTOMERS      c  ON c.customerID = p.customerID
        """;

    // 1. Tính chi phí của một lần khám (trả null nếu không có lần khám)
    public Charges calculateCharges(int visitID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return calculateCharges(conn, visitID);
        }
    }

    private Charges calculateCharges(Connection conn, int visitID)
            throws SQLException {

        String sql = """
            SELECT
                ISNULL((SELECT SUM(s.quantity * s.unitPrice)
                        FROM APPOINTMENT_SERVICE s
                        WHERE s.appointmentID = mv.appointmentID), 0) AS serviceCost,
                ISNULL((SELECT SUM(v.price)
                        FROM VACCINATION vc
                        JOIN VACCINE v ON v.vaccinID = vc.vaccinID
                        WHERE vc.visitID = mv.visitID
                          AND vc.injectionDate <= CAST(GETDATE() AS DATE)), 0) AS vaccineCost,
                ISNULL((SELECT SUM(d.quantity * d.unitPrice)
                        FROM PRESCRIPTION pr
                        JOIN PRESCRIPTION_DETAIL d
                             ON d.prescriptionID = pr.prescriptionID
                        WHERE pr.visitID = mv.visitID), 0) AS medicineCost
            FROM MEDICAL_VISIT mv
            WHERE mv.visitID = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, visitID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Charges(
                    rs.getBigDecimal("serviceCost"),
                    rs.getBigDecimal("vaccineCost"),
                    rs.getBigDecimal("medicineCost")
                );
            }
        }
    }

    // 2. Lập hóa đơn, trả về ID tự tăng.
    // Tổng tiền được tính lại ở đây: total = chi phí - discount
    // (giá trị total truyền vào bị bỏ qua). Vi phạm quy tắc nghiệp vụ
    // sẽ ném IllegalArgumentException kèm câu thông báo tiếng Việt.
    public int insert(Invoice invoice) throws SQLException {

        BigDecimal discount = invoice.getDiscount() == null
                ? BigDecimal.ZERO : invoice.getDiscount();

        if (discount.signum() < 0) {
            throw new IllegalArgumentException("Giảm giá không được âm");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Lần khám phải tồn tại và chưa bị hủy
            String visitStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status FROM MEDICAL_VISIT WHERE visitID = ?")) {
                ps.setInt(1, invoice.getVisitID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        visitStatus = rs.getString("status");
                    }
                }
            }
            if (visitStatus == null) {
                throw new IllegalArgumentException("Không tìm thấy lần khám");
            }
            if ("Cancelled".equals(visitStatus)) {
                throw new IllegalArgumentException("Lần khám đã bị hủy");
            }

            // Mỗi lần khám chỉ có một hóa đơn (visitID là UNIQUE)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM INVOICE WHERE visitID = ?")) {
                ps.setInt(1, invoice.getVisitID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException(
                                "Lần khám này đã có hóa đơn");
                    }
                }
            }

            Charges charges = calculateCharges(conn, invoice.getVisitID());
            BigDecimal subtotal = charges.getTotal();

            if (discount.compareTo(subtotal) > 0) {
                throw new IllegalArgumentException(
                        "Giảm giá không được lớn hơn tổng chi phí");
            }

            BigDecimal total = subtotal.subtract(discount);

            // Hóa đơn 0 đồng không thể thanh toán (amount phải > 0)
            if (total.signum() == 0) {
                throw new IllegalArgumentException(
                        "Tổng tiền bằng 0, không cần lập hóa đơn");
            }

            String sql = """
                INSERT INTO INVOICE(visitID, discount, total, status)
                VALUES (?, ?, ?, N'Unpaid')
                """;

            try (PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, invoice.getVisitID());
                ps.setBigDecimal(2, discount);
                ps.setBigDecimal(3, total);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    int id = rs.next() ? rs.getInt(1) : -1;

                    invoice.setInvoiceID(id);
                    invoice.setDiscount(discount);
                    invoice.setTotal(total);
                    invoice.setStatus(Invoice.STATUS_UNPAID);

                    return id;
                }
            }
        }
    }

    // 3. Xem tất cả hóa đơn (mới nhất trước)
    public List<Invoice> findAll() throws SQLException {
        return query(null, null);
    }

    // 4. Tìm theo mã hóa đơn (trả null nếu không có)
    public Invoice findById(int invoiceID) throws SQLException {
        List<Invoice> list = query("i.invoiceID = ?", invoiceID);
        return list.isEmpty() ? null : list.get(0);
    }

    // 5. Tìm theo mã khách hàng
    public List<Invoice> findByCustomerId(int customerID)
            throws SQLException {
        return query("c.customerID = ?", customerID);
    }

    // 6. Hủy hóa đơn: chỉ khi đang chưa thanh toán và chưa có khoản
    // thanh toán thành công nào. Một câu UPDATE duy nhất nên an toàn.
    public boolean cancel(int invoiceID) throws SQLException {

        String sql = """
            UPDATE INVOICE
            SET status = N'Cancelled'
            WHERE invoiceID = ?
              AND status = N'Unpaid'
              AND NOT EXISTS (SELECT 1 FROM PAYMENT
                              WHERE invoiceID = ?
                                AND status = N'Success')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);
            ps.setInt(2, invoiceID);

            return ps.executeUpdate() == 1;
        }
    }

    // 7. Cập nhật trạng thái hóa đơn trên một Connection có sẵn,
    // để PaymentDAO dùng chung transaction khi thanh toán.
    boolean updateStatus(Connection conn, int invoiceID, String status)
            throws SQLException {

        String sql = "UPDATE INVOICE SET status = ? WHERE invoiceID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, invoiceID);

            return ps.executeUpdate() == 1;
        }
    }

    // Chạy SQL_SELECT với điều kiện WHERE tùy chọn (một tham số int).
    // Chuỗi where chỉ là hằng số trong code này, không nối dữ liệu người dùng.
    private List<Invoice> query(String where, Integer param)
            throws SQLException {

        List<Invoice> list = new ArrayList<>();

        String sql = SQL_SELECT
                + (where == null ? "" : " WHERE " + where)
                + " ORDER BY i.invoiceID DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setInt(1, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    // Chuyển một dòng ResultSet thành đối tượng Invoice
    private Invoice map(ResultSet rs) throws SQLException {
        return new Invoice(
            rs.getInt("invoiceID"),
            rs.getInt("visitID"),
            rs.getBigDecimal("discount"),
            rs.getBigDecimal("total"),
            rs.getString("status"),
            rs.getInt("customerID"),
            rs.getString("fullName"),
            rs.getString("petName"),
            rs.getBigDecimal("paidAmount")
        );
    }
}
