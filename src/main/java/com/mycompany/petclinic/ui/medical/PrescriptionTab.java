package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.dao.PrescriptionDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.Prescription;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class PrescriptionTab extends JPanel {

    private final PrescriptionDAO dao = new PrescriptionDAO();
    private final MedicalVisitDAO visitDAO = new MedicalVisitDAO();
    private final MedicalRecord record;
    private ReadOnlyModel model;
    private List<Prescription> currentPrescriptions = new ArrayList<>();
    private IntConsumer onPrescriptionSelected;

    private JTextField txtPrescriptionID, txtVisitID, txtDoctorID, txtDate, txtNote;
    private JButton btnPickVisit, btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public PrescriptionTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadPrescriptions();
    }

    public void setOnPrescriptionSelected(IntConsumer onPrescriptionSelected) {
        this.onPrescriptionSelected = onPrescriptionSelected;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtPrescriptionID = new JTextField();
        txtVisitID = new JTextField();
        txtDoctorID = new JTextField();
        txtDate = new JTextField();
        txtNote = new JTextField();
        btnPickVisit = new JButton("Chọn");

        JLabel[] labels = {
            new JLabel("Mã đơn thuốc"), new JLabel("Mã lần khám - VisitID (*)"),
            new JLabel("Mã bác sĩ (*)"), new JLabel("Ngày kê đơn (dd/MM/yyyy HH:mm)"),
            new JLabel("Ghi chú")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtPrescriptionID, txtVisitID,
            txtDoctorID, txtDate, txtNote}) {
            UiStyle.field(f);
        }
        UiStyle.lock(txtPrescriptionID, true);
        UiStyle.button(btnPickVisit, UiStyle.PRIMARY);

        JPanel fields = new JPanel(new GridLayout(3, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtPrescriptionID);
        fields.add(labels[1]); fields.add(PickerDialog.fieldWithButton(txtVisitID, btnPickVisit));
        fields.add(labels[2]); fields.add(txtDoctorID);
        fields.add(labels[3]); fields.add(txtDate);
        fields.add(labels[4]); fields.add(txtNote);
        fields.add(new JLabel()); fields.add(new JLabel());
        UiStyle.card(fields, "Thông tin đơn thuốc");

        btnAdd = new JButton("Thêm đơn thuốc");
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

        model = new ReadOnlyModel(new String[]{
            "Mã đơn thuốc", "Mã lần khám", "Mã bác sĩ", "Ngày kê đơn", "Ghi chú"
        }, Integer.class, Integer.class, Integer.class, LocalDateTime.class, String.class);
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);
        int[] widths = {110, 110, 100, 150, 420};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnPickVisit.addActionListener(e -> {
            Integer id = Pickers.visit(this, record.getMedicalRecordID());
            if (id != null) {
                txtVisitID.setText(String.valueOf(id));
            }
        });
        btnAdd.addActionListener(e -> addPrescription());
        btnUpdate.addActionListener(e -> updatePrescription());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private void loadPrescriptions() {
        try {
            currentPrescriptions = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (Prescription p : currentPrescriptions) {
                model.addRow(new Object[]{
                    p.getPrescriptionID(), p.getVisitID(), p.getDoctorID(),
                    p.getPrescriptionDate(), UiStyle.nvl(p.getNote())
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private Prescription readForm(int visitId) throws SQLException {
        int doctorId = UiStyle.positiveInt(txtDoctorID.getText(), "Mã bác sĩ");
        if (!visitDAO.doctorExists(doctorId)) {
            throw new IllegalArgumentException("Mã bác sĩ không tồn tại.");
        }
        LocalDateTime date = UiFormat.parseDateTime(txtDate.getText(), "Ngày kê đơn");
        String note = UiStyle.optional(txtNote.getText());
        if (note != null && note.length() > 500) {
            throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự.");
        }
        return new Prescription(visitId, doctorId, date, note);
    }

    private void addPrescription() {
        if (!txtPrescriptionID.getText().isBlank()) {
            UiStyle.message(this, "Đang xem một đơn thuốc có sẵn. Bấm \"Xóa form\" để nhập mới.");
            return;
        }
        try {
            int visitId = UiStyle.positiveInt(txtVisitID.getText(), "Mã lần khám");
            if (!visitDAO.visitBelongsToRecord(visitId, record.getMedicalRecordID())) {
                UiStyle.message(this, "Mã lần khám không tồn tại hoặc không thuộc hồ sơ này.");
                return;
            }
            if (dao.existsForVisit(visitId)) {
                UiStyle.message(this, "Lần khám này đã có đơn thuốc. Hãy chọn đơn trong bảng để cập nhật.");
                return;
            }
            int id = dao.insert(readForm(visitId));
            if (id > 0) {
                UiStyle.message(this, "Đã thêm đơn thuốc, mã: " + id);
                loadPrescriptions();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updatePrescription() {
        if (txtPrescriptionID.getText().isBlank()) {
            UiStyle.message(this, "Chọn một đơn thuốc trong bảng trước khi cập nhật.");
            return;
        }
        try {
            Prescription p = readForm(Integer.parseInt(txtVisitID.getText()));
            p.setPrescriptionID(Integer.parseInt(txtPrescriptionID.getText()));
            if (dao.update(p)) {
                UiStyle.message(this, "Cập nhật đơn thuốc thành công.");
                loadPrescriptions();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy đơn thuốc để cập nhật.");
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
        Prescription p = currentPrescriptions.get(tblData.convertRowIndexToModel(view));
        txtPrescriptionID.setText(String.valueOf(p.getPrescriptionID()));
        txtVisitID.setText(String.valueOf(p.getVisitID()));
        txtDoctorID.setText(String.valueOf(p.getDoctorID()));
        txtDate.setText(p.getPrescriptionDate() == null ? "" : p.getPrescriptionDate().format(UiStyle.TIME_FMT));
        txtNote.setText(UiStyle.nvl(p.getNote()));
        setSelectedMode(true);
        if (onPrescriptionSelected != null) {
            onPrescriptionSelected.accept(p.getPrescriptionID());
        }
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnPickVisit.setEnabled(!selected);
        UiStyle.lock(txtVisitID, selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        txtPrescriptionID.setText("");
        txtVisitID.setText("");
        txtDoctorID.setText("");
        txtDate.setText(LocalDateTime.now().format(UiStyle.TIME_FMT));
        txtNote.setText("");
        setSelectedMode(false);
    }
}