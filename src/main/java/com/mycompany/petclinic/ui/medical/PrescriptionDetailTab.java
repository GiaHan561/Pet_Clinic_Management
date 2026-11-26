package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicineDAO;
import com.mycompany.petclinic.dao.PrescriptionDAO;
import com.mycompany.petclinic.dao.PrescriptionDetailDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.Medicine;
import com.mycompany.petclinic.model.PrescriptionDetail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
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

public class PrescriptionDetailTab extends JPanel {

    private static final String[] DOSAGES = {
        "", "1/2 viên", "1 viên", "2 viên", "1 gói", "2 gói", "1-2 giọt", "2-3 xịt"
    };
    private static final String[] FREQUENCIES = {
        "", "1 lần/ngày", "2 lần/ngày", "3 lần/ngày", "1 lần/tuần"
    };

    private final PrescriptionDetailDAO dao = new PrescriptionDetailDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final MedicalRecord record;
    private ReadOnlyModel model;
    private List<PrescriptionDetail> currentDetails = new ArrayList<>();
    private Medicine selectedMedicine;

    private JTextField txtDetailID, txtPrescriptionID, txtMedicine, txtDuration,
            txtQuantity, txtInstruction;
    private JLabel lblMedicineInfo;
    private JComboBox<String> cbDosage, cbFrequency;
    private JButton btnPickPrescription, btnPickMedicine, btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public PrescriptionDetailTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadDetails();
    }

    public void setPrescriptionID(int prescriptionID) {
        if (txtDetailID.getText().isBlank()) {
            txtPrescriptionID.setText(String.valueOf(prescriptionID));
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtDetailID = new JTextField();
        txtPrescriptionID = new JTextField();
        txtMedicine = new JTextField();
        txtDuration = new JTextField();
        txtQuantity = new JTextField();
        txtInstruction = new JTextField();
        lblMedicineInfo = new JLabel(" ");
        cbDosage = new JComboBox<>(DOSAGES);
        cbFrequency = new JComboBox<>(FREQUENCIES);
        cbDosage.setEditable(true);
        cbFrequency.setEditable(true);
        btnPickPrescription = new JButton("Chọn");
        btnPickMedicine = new JButton("Chọn");

        JLabel[] labels = {
            new JLabel("Mã chi tiết đơn thuốc"), new JLabel("Mã đơn thuốc (*)"),
            new JLabel("Thuốc - mã hoặc tên (*)"), new JLabel("Thông tin thuốc"),
            new JLabel("Liều lượng"), new JLabel("Tần suất"),
            new JLabel("Thời gian uống"), new JLabel("Số lượng cấp phát (*)"),
            new JLabel("Hướng dẫn sử dụng")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtDetailID, txtPrescriptionID, txtMedicine,
            txtDuration, txtQuantity, txtInstruction}) {
            UiStyle.field(f);
        }
        UiStyle.combo(cbDosage);
        UiStyle.combo(cbFrequency);
        UiStyle.lock(txtDetailID, true);
        UiStyle.button(btnPickPrescription, UiStyle.PRIMARY);
        UiStyle.button(btnPickMedicine, UiStyle.PRIMARY);
        UiStyle.label(lblMedicineInfo);

        JPanel fields = new JPanel(new GridLayout(5, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtDetailID);
        fields.add(labels[1]); fields.add(PickerDialog.fieldWithButton(txtPrescriptionID, btnPickPrescription));
        fields.add(labels[2]); fields.add(PickerDialog.fieldWithButton(txtMedicine, btnPickMedicine));
        fields.add(labels[3]); fields.add(lblMedicineInfo);
        fields.add(labels[4]); fields.add(cbDosage);
        fields.add(labels[5]); fields.add(cbFrequency);
        fields.add(labels[6]); fields.add(txtDuration);
        fields.add(labels[7]); fields.add(txtQuantity);
        fields.add(labels[8]); fields.add(txtInstruction);
        fields.add(new JLabel()); fields.add(new JLabel());
        UiStyle.card(fields, "Chi tiết đơn thuốc");

        btnAdd = new JButton("Thêm thuốc vào đơn");
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
            "Mã chi tiết", "Mã đơn", "Mã thuốc", "Tên thuốc", "Liều lượng", "Tần suất",
            "Thời gian uống", "Số lượng", "Đơn giá", "Hướng dẫn sử dụng"
        }, Integer.class, Integer.class, Integer.class, String.class, String.class,
                String.class, String.class, Integer.class, String.class, String.class);
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);
        int[] widths = {90, 70, 80, 190, 100, 110, 120, 80, 100, 260};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnPickPrescription.addActionListener(e -> {
            Integer id = Pickers.prescription(this, record.getMedicalRecordID());
            if (id != null) {
                txtPrescriptionID.setText(String.valueOf(id));
            }
        });
        btnPickMedicine.addActionListener(e -> pickMedicine());
        txtMedicine.addActionListener(e -> pickMedicine());
        btnAdd.addActionListener(e -> addDetail());
        btnUpdate.addActionListener(e -> updateDetail());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private String label(Medicine m) {
        return m.getMedicineID() + " - " + m.getName();
    }

    private void pickMedicine() {
        String text = txtMedicine.getText().trim();
        String keyword = selectedMedicine != null && text.equals(label(selectedMedicine)) ? "" : text;
        Medicine m = Pickers.medicine(this, keyword);
        if (m != null) {
            applyMedicine(m, true);
        }
    }

    private void applyMedicine(Medicine m, boolean fillDosage) {
        selectedMedicine = m;
        txtMedicine.setText(label(m));
        String info = "Đơn vị: " + UiStyle.nvl(m.getUnit())
                + "  |  Giá: " + UiFormat.money(m.getPrice())
                + "  |  Tồn kho: " + m.getQuantity()
                + "  |  HSD: " + (m.getExpiryDate() == null ? "" : m.getExpiryDate().format(UiStyle.DATE_FMT));
        lblMedicineInfo.setText(info);
        lblMedicineInfo.setToolTipText(info);
        if (fillDosage && m.getDosage() != null && !m.getDosage().isBlank()) {
            cbDosage.setSelectedItem(m.getDosage());
        }
    }

    private Medicine resolveMedicine() throws SQLException {
        String text = txtMedicine.getText().trim();
        if (selectedMedicine != null && text.equals(label(selectedMedicine))) {
            return selectedMedicine;
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập mã hoặc tên thuốc rồi bấm \"Chọn\".");
        }
        List<Medicine> found = medicineDAO.search(text);
        if (found.size() == 1) {
            applyMedicine(found.get(0), true);
            return found.get(0);
        }
        throw new IllegalArgumentException(found.isEmpty()
                ? "Không tìm thấy thuốc phù hợp."
                : "Có nhiều thuốc phù hợp, hãy bấm \"Chọn\" để chọn đúng thuốc.");
    }

    private String comboText(JComboBox<String> cb, String name, int max) {
        Object item = cb.getEditor().getItem();
        String s = item == null ? "" : item.toString().trim();
        if (s.length() > max) {
            throw new IllegalArgumentException(name + " tối đa " + max + " ký tự.");
        }
        return s.isEmpty() ? null : s;
    }

    private PrescriptionDetail readForm(Medicine medicine, int prescriptionId) {
        int quantity = UiStyle.positiveInt(txtQuantity.getText(), "Số lượng cấp phát");
        String dosage = comboText(cbDosage, "Liều lượng", 100);
        String frequency = comboText(cbFrequency, "Tần suất", 100);
        String duration = UiStyle.optional(txtDuration.getText());
        if (duration != null && duration.length() > 100) {
            throw new IllegalArgumentException("Thời gian uống tối đa 100 ký tự.");
        }
        String instruction = UiStyle.optional(txtInstruction.getText());
        if (instruction != null && instruction.length() > 500) {
            throw new IllegalArgumentException("Hướng dẫn sử dụng tối đa 500 ký tự.");
        }
        return new PrescriptionDetail(prescriptionId, medicine.getMedicineID(), quantity,
                medicine.getPrice(), dosage, frequency, duration, instruction);
    }

    private void loadDetails() {
        try {
            currentDetails = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (PrescriptionDetail d : currentDetails) {
                model.addRow(new Object[]{
                    d.getPrescriptionDetailID(), d.getPrescriptionID(), d.getMedicineID(),
                    UiStyle.nvl(d.getMedicineName()), UiStyle.nvl(d.getDosage()),
                    UiStyle.nvl(d.getFrequency()), UiStyle.nvl(d.getDuration()),
                    d.getQuantity(), UiFormat.money(d.getUnitPrice()),
                    UiStyle.nvl(d.getInstruction())
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private void addDetail() {
        if (!txtDetailID.getText().isBlank()) {
            UiStyle.message(this, "Đang xem một chi tiết có sẵn. Bấm \"Xóa form\" để nhập mới.");
            return;
        }
        try {
            int prescriptionId = UiStyle.positiveInt(txtPrescriptionID.getText(), "Mã đơn thuốc");
            if (!prescriptionDAO.belongsToRecord(prescriptionId, record.getMedicalRecordID())) {
                UiStyle.message(this, "Mã đơn thuốc không tồn tại hoặc không thuộc hồ sơ này.");
                return;
            }
            Medicine medicine = resolveMedicine();
            if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
                UiStyle.message(this, "Thuốc này đã hết hạn sử dụng, không thể kê đơn.");
                return;
            }
            if (dao.existsForPrescription(prescriptionId, medicine.getMedicineID())) {
                UiStyle.message(this, "Thuốc này đã có trong đơn. Hãy chọn dòng trong bảng để cập nhật.");
                return;
            }
            PrescriptionDetail detail = readForm(medicine, prescriptionId);
            if (detail.getQuantity() > medicine.getQuantity()) {
                UiStyle.message(this, "Số lượng cấp phát vượt quá tồn kho (" + medicine.getQuantity() + ").");
                return;
            }
            int id = dao.insert(detail);
            if (id > 0) {
                UiStyle.message(this, "Đã thêm thuốc vào đơn, mã chi tiết: " + id);
                loadDetails();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updateDetail() {
        if (txtDetailID.getText().isBlank() || selectedMedicine == null) {
            UiStyle.message(this, "Chọn một chi tiết trong bảng trước khi cập nhật.");
            return;
        }
        try {
            int prescriptionId = Integer.parseInt(txtPrescriptionID.getText());
            PrescriptionDetail detail = readForm(selectedMedicine, prescriptionId);
            detail.setPrescriptionDetailID(Integer.parseInt(txtDetailID.getText()));
            if (dao.update(detail)) {
                UiStyle.message(this, "Cập nhật chi tiết đơn thuốc thành công.");
                loadDetails();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy chi tiết để cập nhật.");
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
        PrescriptionDetail d = currentDetails.get(tblData.convertRowIndexToModel(view));
        try {
            Medicine m = medicineDAO.findById(d.getMedicineID());
            if (m != null) {
                applyMedicine(m, false);
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
        txtDetailID.setText(String.valueOf(d.getPrescriptionDetailID()));
        txtPrescriptionID.setText(String.valueOf(d.getPrescriptionID()));
        cbDosage.setSelectedItem(UiStyle.nvl(d.getDosage()));
        cbFrequency.setSelectedItem(UiStyle.nvl(d.getFrequency()));
        txtDuration.setText(UiStyle.nvl(d.getDuration()));
        txtQuantity.setText(String.valueOf(d.getQuantity()));
        txtInstruction.setText(UiStyle.nvl(d.getInstruction()));
        setSelectedMode(true);
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnPickPrescription.setEnabled(!selected);
        btnPickMedicine.setEnabled(!selected);
        UiStyle.lock(txtPrescriptionID, selected);
        UiStyle.lock(txtMedicine, selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        selectedMedicine = null;
        txtDetailID.setText("");
        txtPrescriptionID.setText("");
        txtMedicine.setText("");
        lblMedicineInfo.setText(" ");
        lblMedicineInfo.setToolTipText(null);
        cbDosage.setSelectedIndex(0);
        cbFrequency.setSelectedIndex(0);
        txtDuration.setText("");
        txtQuantity.setText("");
        txtInstruction.setText("");
        setSelectedMode(false);
    }
}