package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Invoice;
import com.mycompany.petclinic.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    // Câu SELECT dùng chung cho lịch sử thanh toán
    private static final String SQL_SELECT = """
        SELECT pay.paymentID, pay.invoiceID, pay.paymentMethodID,
               pay.paymentDate, pay.amount, pay.status,
               pm.typeName
        FROM PAYMENT pay
        JOIN PAYMENT_METHOD pm ON pm.paymentMethodID = pay.paymentMethodID
        """;

    // 1. Thực hiện thanh toán, trả về mã thanh toán mới.
    // Toàn bộ nằm trong MỘT transaction: kiểm tra hóa đơn, ghi giao dịch,
    // và (nếu đã trả đủ) chuyển hóa đơn sang "Paid". Lỗi ở bất kỳ bước
    // nào thì rollback, hóa đơn và thanh toán không bao giờ bị lệch nhau.
    // Vi phạm quy tắc nghiệp vụ ném IllegalArgumentException (tiếng Việt).
    public int pay(Payment payment) throws SQLException {

        BigDecimal amount = payment.getAmount();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Số tiền thanh toán phải lớn hơn 0");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Khóa dòng hóa đơn để hai người không cùng thanh toán một lúc
                String lockSql = """
                    SELECT total, status
                    FROM INVOICE WITH (UPDLOCK, ROWLOCK)
                    WHERE invoiceID = ?
                    """;

                BigDecimal total;
                String invoiceStatus;

                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, payment.getInvoiceID());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Không tìm thấy hóa đơn");
                        }
                        total = rs.getBigDecimal("total");
                        invoiceStatus = rs.getString("status");
                    }
                }

                if (Invoice.STATUS_PAID.equals(invoiceStatus)) {
                    throw new IllegalArgumentException(
                            "Hóa đơn đã được thanh toán đủ");
                }
                if (Invoice.STATUS_CANCELLED.equals(invoiceStatus)) {
                    throw new IllegalArgumentException(
                            "Hóa đơn đã bị hủy");
                }

                BigDecimal paid = paidAmount(conn, payment.getInvoiceID());
                BigDecimal remaining = total.subtract(paid);

                if (amount.compareTo(remaining) > 0) {
                    throw new IllegalArgumentException(
                            "Số tiền vượt quá số còn phải trả: "
                            + String.format("%,.0f đ", remaining));
                }

                // paymentDate không truyền: DB tự điền SYSDATETIME()
                String insertSql = """
                    INSERT INTO PAYMENT(invoiceID, paymentMethodID,
                                        amount, status)
                    VALUES (?, ?, ?, ?)
                    """;

                int paymentID;

                try (PreparedStatement ps = conn.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, payment.getInvoiceID());
                    ps.setInt(2, payment.getPaymentMethodID());
                    ps.setBigDecimal(3, amount);
                    ps.setString(4, payment.getStatus());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        paymentID = rs.next() ? rs.getInt(1) : -1;
                    }
                }

                // Chỉ khoản thành công mới được tính; đủ tiền thì chốt "Paid"
                if (Payment.STATUS_SUCCESS.equals(payment.getStatus())
                        && paid.add(amount).compareTo(total) >= 0) {
                    invoiceDAO.updateStatus(conn, payment.getInvoiceID(),
                            Invoice.STATUS_PAID);
                }

                conn.commit();

                payment.setPaymentID(paymentID);
                return paymentID;

            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    // 2. Xem tất cả giao dịch (mới nhất trước)
    public List<Payment> findAll() throws SQLException {
        return query(null, null);
    }

    // 3. Tìm theo mã thanh toán (trả null nếu không có)
    public Payment findById(int paymentID) throws SQLException {
        List<Payment> list = query("pay.paymentID = ?", paymentID);
        return list.isEmpty() ? null : list.get(0);
    }

    // 4. Lịch sử thanh toán của một hóa đơn
    public List<Payment> findByInvoice(int invoiceID) throws SQLException {
        return query("pay.invoiceID = ?", invoiceID);
    }

    // 5. Lịch sử thanh toán của một khách hàng
    // (INVOICE -> MEDICAL_VISIT -> MEDICAL_RECORD -> PETS)
    public List<Payment> findByCustomerId(int customerID)
            throws SQLException {
        return query("""
            pay.invoiceID IN (
                SELECT i.invoiceID
                FROM INVOICE i
                JOIN MEDICAL_VISIT  mv ON mv.visitID  = i.visitID
                JOIN MEDICAL_RECORD mr ON mr.recordID = mv.recordID
                JOIN PETS           p  ON p.petID     = mr.petID
                WHERE p.customerID = ?)
            """, customerID);
    }

    // Tổng tiền đã thanh toán thành công của một hóa đơn
    private BigDecimal paidAmount(Connection conn, int invoiceID)
            throws SQLException {

        String sql = """
            SELECT ISNULL(SUM(amount), 0)
            FROM PAYMENT
            WHERE invoiceID = ? AND status = N'Success'
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    // Chạy SQL_SELECT với điều kiện WHERE tùy chọn (một tham số int).
    // Chuỗi where chỉ là hằng số trong code này, không nối dữ liệu người dùng.
    private List<Payment> query(String where, Integer param)
            throws SQLException {

        List<Payment> list = new ArrayList<>();

        String sql = SQL_SELECT
                + (where == null ? "" : " WHERE " + where)
                + " ORDER BY pay.paymentDate DESC, pay.paymentID DESC";

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

    // Chuyển một dòng ResultSet thành đối tượng Payment
    private Payment map(ResultSet rs) throws SQLException {

        Timestamp time = rs.getTimestamp("paymentDate");

        LocalDateTime paymentDate = null;

        if (time != null) {
            paymentDate = time.toLocalDateTime();
        }

        return new Payment(
            rs.getInt("paymentID"),
            rs.getInt("invoiceID"),
            rs.getInt("paymentMethodID"),
            paymentDate,
            rs.getBigDecimal("amount"),
            rs.getString("status"),
            rs.getString("typeName")
        );
    }
}

