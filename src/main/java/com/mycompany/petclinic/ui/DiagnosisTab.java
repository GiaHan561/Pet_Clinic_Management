package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.DiagnosisDAO;
import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.model.Diagnosis;
import com.mycompany.petclinic.model.MedicalRecord;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class DiagnosisTab extends JPanel {

    private static final String[] SEVERITIES = {"", "Mild", "Moderate", "Severe", "Critical"};

    private final DiagnosisDAO dao = new DiagnosisDAO();
    private final MedicalVisitDAO visitDAO = new MedicalVisitDAO();
    private final MedicalRecord record;
    private DefaultTableModel model;
    private List<Diagnosis> currentDiagnoses = new ArrayList<>();

    private JTextField txtDiagnosisID, txtVisitID, txtName, txtDescription;
    private JComboBox<String> cbSeverity;
    private JButton btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public DiagnosisTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadDiagnoses();
    }

    public void setVisitID(int visitID) {
        if (txtDiagnosisID.getText().isBlank()) {
            txtVisitID.setText(String.valueOf(visitID));
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtDiagnosisID = new JTextField();
        txtVisitID = new JTextField();
        txtName = new JTextField();
        txtDescription = new JTextField();
        cbSeverity = new JComboBox<>(SEVERITIES);

        JLabel[] labels = {
            new JLabel("Mã chẩn đoán"), new JLabel("Mã lần khám (*)"),
            new JLabel("Tên bệnh chẩn đoán (*)"), new JLabel("Mức độ nghiêm trọng"),
            new JLabel("Mô tả chi tiết bệnh")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtDiagnosisID, txtVisitID, txtName, txtDescription}) {
            UiStyle.field(f);
        }
        UiStyle.combo(cbSeverity);
        UiStyle.lock(txtDiagnosisID, true);

        JPanel fields = new JPanel(new GridLayout(3, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtDiagnosisID);
        fields.add(labels[1]); fields.add(txtVisitID);
        fields.add(labels[2]); fields.add(txtName);
        fields.add(labels[3]); fields.add(cbSeverity);
        fields.add(labels[4]); fields.add(txtDescription);
        fields.add(new JLabel()); fields.add(new JLabel());
        UiStyle.card(fields, "Chẩn đoán");

        btnAdd = new JButton("Thêm chẩn đoán");
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
            "Mã chẩn đoán", "Mã lần khám", "Tên bệnh", "Mức độ", "Mô tả chi tiết"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c <= 1 ? Integer.class : String.class;
            }
        };
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);
        int[] widths = {110, 100, 220, 100, 420};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addDiagnosis());
        btnUpdate.addActionListener(e -> updateDiagnosis());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private void loadDiagnoses() {
        try {
            currentDiagnoses = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (Diagnosis d : currentDiagnoses) {
                model.addRow(new Object[]{
                    d.getDiagnosisID(), d.getVisitID(), d.getDiagnosisName(),
                    d.getSeverity(), d.getDescription()
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private Diagnosis readForm(int visitId) {
        String name = txtName.getText().trim();
        if (name.isEmpty() || name.length() > 200) {
            throw new IllegalArgumentException("Tên bệnh chẩn đoán phải có từ 1 đến 200 ký tự.");
        }
        String description = UiStyle.optional(txtDescription.getText());
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("Mô tả tối đa 1000 ký tự.");
        }
        String severity = (String) cbSeverity.getSelectedItem();
        if (severity != null && severity.isEmpty()) {
            severity = null;
        }
        return new Diagnosis(visitId, name, description, severity);
    }

    private void addDiagnosis() {
        if (!txtDiagnosisID.getText().isBlank()) {
            UiStyle.message(this, "Đang xem một chẩn đoán có sẵn. Bấm \"Xóa form\" để nhập mới.");
            return;
        }
        try {
            int visitId = UiStyle.positiveInt(txtVisitID.getText(), "Mã lần khám");
            if (!visitDAO.visitBelongsToRecord(visitId, record.getMedicalRecordID())) {
                UiStyle.message(this, "Mã lần khám không tồn tại hoặc không thuộc hồ sơ này.");
                return;
            }
            int id = dao.insert(readForm(visitId));
            if (id > 0) {
                UiStyle.message(this, "Đã thêm chẩn đoán, mã: " + id);
                loadDiagnoses();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updateDiagnosis() {
        if (txtDiagnosisID.getText().isBlank()) {
            UiStyle.message(this, "Chọn một chẩn đoán trong bảng trước khi cập nhật.");
            return;
        }
        try {
            Diagnosis d = readForm(Integer.parseInt(txtVisitID.getText()));
            d.setDiagnosisID(Integer.parseInt(txtDiagnosisID.getText()));
            if (dao.update(d)) {
                UiStyle.message(this, "Cập nhật chẩn đoán thành công.");
                loadDiagnoses();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy chẩn đoán để cập nhật.");
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
        Diagnosis d = currentDiagnoses.get(tblData.convertRowIndexToModel(view));
        txtDiagnosisID.setText(String.valueOf(d.getDiagnosisID()));
        txtVisitID.setText(String.valueOf(d.getVisitID()));
        txtName.setText(d.getDiagnosisName());
        cbSeverity.setSelectedItem(d.getSeverity() == null ? "" : d.getSeverity());
        txtDescription.setText(UiStyle.nvl(d.getDescription()));
        setSelectedMode(true);
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        UiStyle.lock(txtVisitID, selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        txtDiagnosisID.setText("");
        txtVisitID.setText("");
        txtName.setText("");
        txtDescription.setText("");
        cbSeverity.setSelectedIndex(0);
        setSelectedMode(false);
    }
}