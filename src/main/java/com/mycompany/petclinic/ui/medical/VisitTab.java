package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.MedicalVisit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

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

public class VisitTab extends JPanel {

    private static final String[] STATUSES = {"InProgress", "Completed", "Cancelled"};

    private final MedicalVisitDAO dao = new MedicalVisitDAO();
    private final MedicalRecord record;
    private DefaultTableModel model;
    private List<MedicalVisit> currentVisits = new ArrayList<>();
    private IntConsumer onVisitSelected;

    private JTextField txtVisitID, txtDoctorID, txtAppointmentID, txtPetID, txtReason, txtTime;
    private JComboBox<String> cbStatus;
    private JButton btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public VisitTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadVisits();
    }

    public void setOnVisitSelected(IntConsumer onVisitSelected) {
        this.onVisitSelected = onVisitSelected;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtVisitID = new JTextField();
        txtDoctorID = new JTextField();
        txtAppointmentID = new JTextField();
        txtAppointmentID.setToolTipText("Để trống nếu khám không qua lịch hẹn");
        txtPetID = new JTextField();
        txtReason = new JTextField();
        txtTime = new JTextField();
        txtTime.setToolTipText("dd/MM/yyyy HH:mm");
        cbStatus = new JComboBox<>(STATUSES);

        JLabel[] labels = {
            new JLabel("Mã lần khám"), new JLabel("Mã bác sĩ (*)"),
            new JLabel("Mã lịch hẹn"), new JLabel("Mã thú cưng"),
            new JLabel("Trạng thái khám"), new JLabel("Thời gian (dd/MM/yyyy HH:mm)"),
            new JLabel("Lý do khám")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtVisitID, txtDoctorID, txtAppointmentID,
            txtPetID, txtReason, txtTime}) {
            UiStyle.field(f);
        }
        UiStyle.combo(cbStatus);
        UiStyle.lock(txtVisitID, true);
        UiStyle.lock(txtPetID, true);

        JPanel fields = new JPanel(new GridLayout(4, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtVisitID);
        fields.add(labels[1]); fields.add(txtDoctorID);
        fields.add(labels[2]); fields.add(txtAppointmentID);
        fields.add(labels[3]); fields.add(txtPetID);
        fields.add(labels[4]); fields.add(cbStatus);
        fields.add(labels[5]); fields.add(txtTime);
        fields.add(labels[6]); fields.add(txtReason);
        fields.add(new JLabel()); fields.add(new JLabel());
        UiStyle.card(fields, "Chi tiết lần khám");

        btnAdd = new JButton("Thêm lần khám");
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
            "Mã lần khám", "Mã bác sĩ", "Mã lịch hẹn", "Mã thú cưng",
            "Trạng thái", "Lý do khám", "Thời gian"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c <= 3) {
                    return Integer.class;
                }
                return c == 6 ? LocalDateTime.class : String.class;
            }
        };
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);
        int[] widths = {100, 90, 100, 100, 110, 260, 150};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addVisit());
        btnUpdate.addActionListener(e -> updateVisit());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private void loadVisits() {
        try {
            currentVisits = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (MedicalVisit v : currentVisits) {
                model.addRow(new Object[]{
                    v.getVisitID(), v.getDoctorID(), v.getAppointmentID(), v.getPetID(),
                    v.getStatus(), v.getReason(), v.getVisitTime()
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private MedicalVisit readForm() throws SQLException {
        int doctorId = UiStyle.positiveInt(txtDoctorID.getText(), "Mã bác sĩ");
        if (!dao.doctorExists(doctorId)) {
            throw new IllegalArgumentException("Mã bác sĩ không tồn tại.");
        }

        Integer appointmentId = null;
        String ap = txtAppointmentID.getText().trim();
        if (!ap.isEmpty()) {
            appointmentId = UiStyle.positiveInt(ap, "Mã lịch hẹn");
            if (!dao.appointmentBelongsToPet(appointmentId, record.getPetID())) {
                throw new IllegalArgumentException(
                        "Mã lịch hẹn không tồn tại hoặc không thuộc thú cưng này.");
            }
        }

        LocalDateTime time = parseTime();
        String reason = UiStyle.optional(txtReason.getText());
        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException("Lý do khám tối đa 500 ký tự.");
        }
        String status = (String) cbStatus.getSelectedItem();

        return new MedicalVisit(record.getMedicalRecordID(), doctorId,
                appointmentId, record.getPetID(), time, reason, status);
    }

    private LocalDateTime parseTime() {
        try {
            return LocalDateTime.parse(txtTime.getText().trim(), UiStyle.TIME_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Thời gian phải có dạng dd/MM/yyyy HH:mm (ví dụ 24/11/2026 09:30).");
        }
    }

    private void addVisit() {
        try {
            int id = dao.insert(readForm());
            if (id > 0) {
                UiStyle.message(this, "Đã thêm lần khám, mã: " + id);
                loadVisits();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updateVisit() {
        if (txtVisitID.getText().isBlank()) {
            UiStyle.message(this, "Chọn một lần khám trong bảng trước khi cập nhật.");
            return;
        }
        try {
            MedicalVisit v = readForm();
            v.setVisitID(Integer.parseInt(txtVisitID.getText()));
            if (dao.update(v)) {
                UiStyle.message(this, "Cập nhật lần khám thành công.");
                loadVisits();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy lần khám để cập nhật.");
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
        MedicalVisit v = currentVisits.get(tblData.convertRowIndexToModel(view));
        txtVisitID.setText(String.valueOf(v.getVisitID()));
        txtDoctorID.setText(String.valueOf(v.getDoctorID()));
        txtAppointmentID.setText(v.getAppointmentID() == null ? "" : String.valueOf(v.getAppointmentID()));
        txtPetID.setText(String.valueOf(v.getPetID()));
        cbStatus.setSelectedItem(v.getStatus());
        txtTime.setText(v.getVisitTime() == null ? "" : v.getVisitTime().format(UiStyle.TIME_FMT));
        txtReason.setText(UiStyle.nvl(v.getReason()));
        setSelectedMode(true);
        if (onVisitSelected != null) {
            onVisitSelected.accept(v.getVisitID());
        }
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        txtVisitID.setText("");
        txtDoctorID.setText("");
        txtAppointmentID.setText("");
        txtPetID.setText(String.valueOf(record.getPetID()));
        cbStatus.setSelectedIndex(0);
        txtTime.setText(LocalDateTime.now().format(UiStyle.TIME_FMT));
        txtReason.setText("");
        setSelectedMode(false);
    }
}