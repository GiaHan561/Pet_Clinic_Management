package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.dao.VitalSignDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.VitalSign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VitalSignTab extends JPanel {

    private final VitalSignDAO dao = new VitalSignDAO();
    private final MedicalVisitDAO visitDAO = new MedicalVisitDAO();
    private final MedicalRecord record;
    private DefaultTableModel model;
    private List<VitalSign> currentSigns = new ArrayList<>();

    private JTextField txtVitalSignID, txtVisitID, txtWeight, txtHeartRate, txtRespiratoryRate;
    private JButton btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public VitalSignTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadSigns();
    }

    public void setVisitID(int visitID) {
        if (txtVitalSignID.getText().isBlank()) {
            txtVisitID.setText(String.valueOf(visitID));
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtVitalSignID = new JTextField();
        txtVisitID = new JTextField();
        txtWeight = new JTextField();
        txtHeartRate = new JTextField();
        txtRespiratoryRate = new JTextField();

        JLabel[] labels = {
            new JLabel("Mã chỉ số sức khỏe"), new JLabel("Mã lần khám (*)"),
            new JLabel("Cân nặng (kg)"), new JLabel("Nhịp tim (lần/phút)"),
            new JLabel("Tần số hô hấp (lần/phút)")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtVitalSignID, txtVisitID, txtWeight,
            txtHeartRate, txtRespiratoryRate}) {
            UiStyle.field(f);
        }
        UiStyle.lock(txtVitalSignID, true);

        JPanel fields = new JPanel(new GridLayout(3, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtVitalSignID);
        fields.add(labels[1]); fields.add(txtVisitID);
        fields.add(labels[2]); fields.add(txtWeight);
        fields.add(labels[3]); fields.add(txtHeartRate);
        fields.add(labels[4]); fields.add(txtRespiratoryRate);
        fields.add(new JLabel()); fields.add(new JLabel());
        UiStyle.card(fields, "Chỉ số sức khỏe");

        btnAdd = new JButton("Thêm chỉ số");
        btnUpdate = new JButton("Cập nhật");
        btnClear = new JButton("Xóa form");
        UiStyle.button(btnAdd, new Color(39, 163, 98));
        UiStyle.button(btnUpdate, new Color(238, 142, 36));
        UiStyle.button(btnClear, new Color(110, 120, 140));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnClear);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(fields, BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);

        model = new DefaultTableModel(new String[]{
            "Mã chỉ số", "Mã lần khám", "Cân nặng (kg)",
            "Nhịp tim (lần/phút)", "Tần số hô hấp (lần/phút)"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 2 ? BigDecimal.class : Integer.class;
            }
        };
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addSign());
        btnUpdate.addActionListener(e -> updateSign());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private void loadSigns() {
        try {
            currentSigns = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (VitalSign s : currentSigns) {
                model.addRow(new Object[]{
                    s.getVitalSignID(), s.getVisitID(), s.getWeight(),
                    s.getHeartRate(), s.getRespiratoryRate()
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private BigDecimal parseWeight() {
        String s = txtWeight.getText().trim().replace(',', '.');
        if (s.isEmpty()) {
            return null;
        }
        BigDecimal w;
        try {
            w = new BigDecimal(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Cân nặng phải là số (ví dụ 4.5).");
        }
        if (w.compareTo(BigDecimal.ZERO) <= 0 || w.compareTo(new BigDecimal("200")) > 0
                || w.scale() > 2) {
            throw new IllegalArgumentException("Cân nặng phải từ 0.01 đến 200 kg, tối đa 2 số lẻ.");
        }
        return w;
    }

    private VitalSign readForm(int visitId) {
        BigDecimal weight = parseWeight();
        Integer heart = UiStyle.optionalInt(txtHeartRate.getText(), "Nhịp tim", 1, 600);
        Integer resp = UiStyle.optionalInt(txtRespiratoryRate.getText(), "Tần số hô hấp", 1, 300);
        if (weight == null && heart == null && resp == null) {
            throw new IllegalArgumentException("Hãy nhập ít nhất một chỉ số sức khỏe.");
        }
        return new VitalSign(visitId, weight, heart, resp);
    }

    private void addSign() {
        if (!txtVitalSignID.getText().isBlank()) {
            UiStyle.message(this, "Đang xem một chỉ số có sẵn. Bấm \"Xóa form\" để nhập mới.");
            return;
        }
        try {
            int visitId = UiStyle.positiveInt(txtVisitID.getText(), "Mã lần khám");
            if (!visitDAO.visitBelongsToRecord(visitId, record.getMedicalRecordID())) {
                UiStyle.message(this, "Mã lần khám không tồn tại hoặc không thuộc hồ sơ này.");
                return;
            }
            if (dao.existsForVisit(visitId)) {
                UiStyle.message(this, "Lần khám này đã có chỉ số sức khỏe. Hãy chọn dòng trong bảng và cập nhật.");
                return;
            }
            int id = dao.insert(readForm(visitId));
            if (id > 0) {
                UiStyle.message(this, "Đã thêm chỉ số sức khỏe, mã: " + id);
                loadSigns();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updateSign() {
        if (txtVitalSignID.getText().isBlank()) {
            UiStyle.message(this, "Chọn một chỉ số trong bảng trước khi cập nhật.");
            return;
        }
        try {
            VitalSign s = readForm(Integer.parseInt(txtVisitID.getText()));
            s.setVitalSignID(Integer.parseInt(txtVitalSignID.getText()));
            if (dao.update(s)) {
                UiStyle.message(this, "Cập nhật chỉ số sức khỏe thành công.");
                loadSigns();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy chỉ số để cập nhật.");
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void selectRow() {
        int view = tblData.getSelectedRow();
        if (view < 0) {
            return;
        }
        VitalSign s = currentSigns.get(tblData.convertRowIndexToModel(view));
        txtVitalSignID.setText(String.valueOf(s.getVitalSignID()));
        txtVisitID.setText(String.valueOf(s.getVisitID()));
        txtWeight.setText(s.getWeight() == null ? "" : s.getWeight().toPlainString());
        txtHeartRate.setText(s.getHeartRate() == null ? "" : String.valueOf(s.getHeartRate()));
        txtRespiratoryRate.setText(s.getRespiratoryRate() == null ? "" : String.valueOf(s.getRespiratoryRate()));
        setSelectedMode(true);
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        UiStyle.lock(txtVisitID, selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        txtVitalSignID.setText("");
        txtVisitID.setText("");
        txtWeight.setText("");
        txtHeartRate.setText("");
        txtRespiratoryRate.setText("");
        setSelectedMode(false);
    }
}