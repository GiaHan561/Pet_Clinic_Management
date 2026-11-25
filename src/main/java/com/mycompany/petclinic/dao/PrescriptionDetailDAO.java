package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.PrescriptionDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDetailDAO {

    public int insert(PrescriptionDetail d) throws SQLException {
        String sql = """
            INSERT INTO PRESCRIPTION_DETAIL
            (prescriptionID, medicineID, quantity, unitPrice,
             dosage, frequency, duration, instruction)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                adjustStock(conn, d.getMedicineID(), d.getQuantity());

                int id;
                try (PreparedStatement ps = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, d.getPrescriptionID());
                    ps.setInt(2, d.getMedicineID());
                    ps.setInt(3, d.getQuantity());
                    ps.setBigDecimal(4, d.getUnitPrice());
                    ps.setString(5, d.getDosage());
                    ps.setString(6, d.getFrequency());
                    ps.setString(7, d.getDuration());
                    ps.setString(8, d.getInstruction());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        id = rs.next() ? rs.getInt(1) : -1;
                    }
                }
                conn.commit();
                return id;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean update(PrescriptionDetail d) throws SQLException {
        String selectSql = """
            SELECT medicineID, quantity
            FROM PRESCRIPTION_DETAIL WITH (UPDLOCK)
            WHERE prescriptionDetailID = ?
            """;
        String updateSql = """
            UPDATE PRESCRIPTION_DETAIL
            SET quantity = ?, dosage = ?, frequency = ?,
                duration = ?, instruction = ?
            WHERE prescriptionDetailID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int medicineId;
                int oldQuantity;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, d.getPrescriptionDetailID());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        medicineId = rs.getInt("medicineID");
                        oldQuantity = rs.getInt("quantity");
                    }
                }

                adjustStock(conn, medicineId, d.getQuantity() - oldQuantity);

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, d.getQuantity());
                    ps.setString(2, d.getDosage());
                    ps.setString(3, d.getFrequency());
                    ps.setString(4, d.getDuration());
                    ps.setString(5, d.getInstruction());
                    ps.setInt(6, d.getPrescriptionDetailID());
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public List<PrescriptionDetail> findByRecordId(int recordID) throws SQLException {
        List<PrescriptionDetail> list = new ArrayList<>();

        String sql = """
            SELECT pd.prescriptionDetailID, pd.prescriptionID, pd.medicineID,
                   m.name AS medicineName, pd.quantity, pd.unitPrice,
                   pd.dosage, pd.frequency, pd.duration, pd.instruction
            FROM PRESCRIPTION_DETAIL pd
            JOIN PRESCRIPTION p ON p.prescriptionID = pd.prescriptionID
            JOIN MEDICAL_VISIT v ON v.visitID = p.visitID
            JOIN MEDICINE m ON m.medicineID = pd.medicineID
            WHERE v.recordID = ?
            ORDER BY pd.prescriptionDetailID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrescriptionDetail d = new PrescriptionDetail(
                        rs.getInt("prescriptionID"),
                        rs.getInt("medicineID"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unitPrice"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        rs.getString("duration"),
                        rs.getString("instruction")
                    );
                    d.setPrescriptionDetailID(rs.getInt("prescriptionDetailID"));
                    d.setMedicineName(rs.getString("medicineName"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    public List<Object[]> findUsageByRecordId(int recordID) throws SQLException {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
            SELECT p.prescriptionID, p.prescriptionDate, m.name, pd.dosage,
                   pd.frequency, pd.duration, pd.quantity, m.unit
            FROM PRESCRIPTION p
            JOIN MEDICAL_VISIT v ON v.visitID = p.visitID
            JOIN PRESCRIPTION_DETAIL pd ON pd.prescriptionID = p.prescriptionID
            JOIN MEDICINE m ON m.medicineID = pd.medicineID
            WHERE v.recordID = ?
            ORDER BY p.prescriptionDate DESC, pd.prescriptionDetailID
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("prescriptionDate");
                    rows.add(new Object[]{
                        rs.getInt("prescriptionID"),
                        t == null ? null : t.toLocalDateTime(),
                        rs.getString("name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        rs.getString("duration"),
                        rs.getInt("quantity"),
                        rs.getString("unit")
                    });
                }
            }
        }
        return rows;
    }

    public boolean existsForPrescription(int prescriptionID, int medicineID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn, """
                SELECT 1 FROM PRESCRIPTION_DETAIL
                WHERE prescriptionID = ? AND medicineID = ?
                """, prescriptionID, medicineID);
        }
    }

    private void adjustStock(Connection conn, int medicineID, int delta) throws SQLException {
        if (delta == 0) {
            return;
        }
        String sql = """
            UPDATE MEDICINE
            SET quantity = quantity - ?
            WHERE medicineID = ? AND quantity - ? >= 0
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, medicineID);
            ps.setInt(3, delta);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("Không đủ thuốc trong kho để cấp phát.");
            }
        }
    }
}