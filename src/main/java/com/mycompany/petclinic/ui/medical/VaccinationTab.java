package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.dao.VaccinationDAO;
import com.mycompany.petclinic.dao.VaccineDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.Vaccination;
import com.mycompany.petclinic.model.Vaccine;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class VaccinationTab extends JPanel {

    private final VaccinationDAO dao = new VaccinationDAO();
    private final VaccineDAO vaccineDAO = new VaccineDAO();
    private final MedicalVisitDAO visitDAO = new MedicalVisitDAO();
    private final MedicalRecord record;
    private ReadOnlyModel model;
    private List<Vaccination> currentVaccinations = new ArrayList<>();
    private Vaccine selectedVaccine;

    private JTextField txtVaccinationID, txtVisitID, txtVaccine, txtInjectionDate, txtNextDate;
    private JLabel lblVaccineInfo;
    private JButton btnPickVisit, btnPickVaccine, btnAdd, btnUpdate, btnClear;
    private JTable tblData;
    private JScrollPane scrollTable;

    public VaccinationTab(MedicalRecord record) {
        this.record = record;
        initComponents();
        bindEvents();
        clearFields();
        loadVaccinations();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        txtVaccinationID = new JTextField();
        txtVisitID = new JTextField();
        txtVaccine = new JTextField();
        txtInjectionDate = new JTextField();
        txtNextDate = new JTextField();
        txtNextDate.setToolTipText("Để trống nếu không cần tiêm nhắc lại");
        lblVaccineInfo = new JLabel(" ");
        btnPickVisit = new JButton("Chọn");
        btnPickVaccine = new JButton("Chọn");

        JLabel[] labels = {
            new JLabel("Mã lượt tiêm"), new JLabel("Mã lần khám - VisitID (*)"),
            new JLabel("Vaccine - mã hoặc tên (*)"), new JLabel("Thông tin vaccine"),
            new JLabel("Ngày tiêm (dd/MM/yyyy) (*)"), new JLabel("Ngày tiêm nhắc lại (dd/MM/yyyy)")
        };
        for (JLabel l : labels) {
            UiStyle.label(l);
        }
        for (JTextField f : new JTextField[]{txtVaccinationID, txtVisitID, txtVaccine,
            txtInjectionDate, txtNextDate}) {
            UiStyle.field(f);
        }
        UiStyle.lock(txtVaccinationID, true);
        UiStyle.button(btnPickVisit, UiStyle.PRIMARY);
        UiStyle.button(btnPickVaccine, UiStyle.PRIMARY);
        UiStyle.label(lblVaccineInfo);

        JPanel fields = new JPanel(new GridLayout(3, 4, 16, 12));
        fields.add(labels[0]); fields.add(txtVaccinationID);
        fields.add(labels[1]); fields.add(PickerDialog.fieldWithButton(txtVisitID, btnPickVisit));
        fields.add(labels[2]); fields.add(PickerDialog.fieldWithButton(txtVaccine, btnPickVaccine));
        fields.add(labels[3]); fields.add(lblVaccineInfo);
        fields.add(labels[4]); fields.add(txtInjectionDate);
        fields.add(labels[5]); fields.add(txtNextDate);
        UiStyle.card(fields, "Vaccine cần tiêm / đã tiêm");

        btnAdd = new JButton("Thêm vaccine");
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
            "Mã lượt tiêm", "Mã lần khám", "Mã vaccine", "Tên vaccine",
            "Ngày tiêm", "Tiêm nhắc lại", "Trạng thái"
        }, Integer.class, Integer.class, Integer.class, String.class,
                LocalDate.class, LocalDate.class, String.class);
        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);
        int[] widths = {100, 100, 100, 260, 120, 130, 190};
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
        btnPickVaccine.addActionListener(e -> pickVaccine());
        txtVaccine.addActionListener(e -> pickVaccine());
        btnAdd.addActionListener(e -> addVaccination());
        btnUpdate.addActionListener(e -> updateVaccination());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private String label(Vaccine v) {
        return v.getVaccineID() + " - " + v.getName();
    }

    private void pickVaccine() {
        String text = txtVaccine.getText().trim();
        String keyword = selectedVaccine != null && text.equals(label(selectedVaccine)) ? "" : text;
        Vaccine v = Pickers.vaccine(this, keyword);
        if (v != null) {
            applyVaccine(v, true);
        }
    }

    private void applyVaccine(Vaccine v, boolean suggestNext) {
        selectedVaccine = v;
        txtVaccine.setText(label(v));
        String info = "Nhà sản xuất: " + UiStyle.nvl(v.getManufacturer())
                + "  |  Giá: " + UiFormat.money(v.getPrice())
                + "  |  Nhắc lại sau: " + UiFormat.days(v.getRcmInterval());
        lblVaccineInfo.setText(info);
        lblVaccineInfo.setToolTipText(info);

        if (suggestNext && v.getRcmInterval() != null && txtNextDate.getText().isBlank()) {
            try {
                LocalDate injection = UiFormat.parseDate(txtInjectionDate.getText(), "Ngày tiêm");
                txtNextDate.setText(injection.plusDays(v.getRcmInterval()).format(UiStyle.DATE_FMT));
            } catch (IllegalArgumentException ex) {
                txtNextDate.setText("");
            }
        }
    }

    private Vaccine resolveVaccine() throws SQLException {
        String text = txtVaccine.getText().trim();
        if (selectedVaccine != null && text.equals(label(selectedVaccine))) {
            return selectedVaccine;
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập mã hoặc tên vaccine rồi bấm \"Chọn\".");
        }
        List<Vaccine> found = vaccineDAO.search(text);
        if (found.size() == 1) {
            applyVaccine(found.get(0), false);
            return found.get(0);
        }
        throw new IllegalArgumentException(found.isEmpty()
                ? "Không tìm thấy vaccine phù hợp."
                : "Có nhiều vaccine phù hợp, hãy bấm \"Chọn\" để chọn đúng vaccine.");
    }

    private Vaccination readForm(int visitId, int vaccineId) {
        LocalDate injection = UiFormat.parseDate(txtInjectionDate.getText(), "Ngày tiêm");
        LocalDate next = UiFormat.optionalDate(txtNextDate.getText(), "Ngày tiêm nhắc lại");
        if (next != null && next.isBefore(injection)) {
            throw new IllegalArgumentException("Ngày tiêm nhắc lại không được trước ngày tiêm.");
        }
        return new Vaccination(visitId, vaccineId, injection, next);
    }

    private void loadVaccinations() {
        try {
            currentVaccinations = dao.findByRecordId(record.getMedicalRecordID());
            model.setRowCount(0);
            for (Vaccination v : currentVaccinations) {
                model.addRow(new Object[]{
                    v.getVaccinationID(), v.getVisitID(), v.getVaccineID(),
                    UiStyle.nvl(v.getVaccineName()), v.getInjectionDate(),
                    v.getNextInjectionDate(), v.getStatus()
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private void addVaccination() {
        if (!txtVaccinationID.getText().isBlank()) {
            UiStyle.message(this, "Đang xem một lượt tiêm có sẵn. Bấm \"Xóa form\" để nhập mới.");
            return;
        }
        try {
            int visitId = UiStyle.positiveInt(txtVisitID.getText(), "Mã lần khám");
            if (!visitDAO.visitBelongsToRecord(visitId, record.getMedicalRecordID())) {
                UiStyle.message(this, "Mã lần khám không tồn tại hoặc không thuộc hồ sơ này.");
                return;
            }
            Vaccine vaccine = resolveVaccine();
            if (dao.existsForVisit(visitId, vaccine.getVaccineID())) {
                UiStyle.message(this, "Vaccine này đã được thêm cho lần khám đó. Hãy chọn dòng trong bảng để cập nhật.");
                return;
            }
            int id = dao.insert(readForm(visitId, vaccine.getVaccineID()));
            if (id > 0) {
                UiStyle.message(this, "Đã thêm vaccine, mã lượt tiêm: " + id);
                loadVaccinations();
                clearFields();
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private void updateVaccination() {
        if (txtVaccinationID.getText().isBlank() || selectedVaccine == null) {
            UiStyle.message(this, "Chọn một lượt tiêm trong bảng trước khi cập nhật.");
            return;
        }
        try {
            Vaccination v = readForm(Integer.parseInt(txtVisitID.getText()), selectedVaccine.getVaccineID());
            v.setVaccinationID(Integer.parseInt(txtVaccinationID.getText()));
            if (dao.update(v)) {
                UiStyle.message(this, "Cập nhật lượt tiêm thành công.");
                loadVaccinations();
                clearFields();
            } else {
                UiStyle.message(this, "Không tìm thấy lượt tiêm để cập nhật.");
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
        Vaccination v = currentVaccinations.get(tblData.convertRowIndexToModel(view));
        try {
            Vaccine vaccine = vaccineDAO.findById(v.getVaccineID());
            if (vaccine != null) {
                applyVaccine(vaccine, false);
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
        txtVaccinationID.setText(String.valueOf(v.getVaccinationID()));
        txtVisitID.setText(String.valueOf(v.getVisitID()));
        txtInjectionDate.setText(v.getInjectionDate().format(UiStyle.DATE_FMT));
        txtNextDate.setText(v.getNextInjectionDate() == null ? "" : v.getNextInjectionDate().format(UiStyle.DATE_FMT));
        setSelectedMode(true);
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnPickVisit.setEnabled(!selected);
        btnPickVaccine.setEnabled(!selected);
        UiStyle.lock(txtVisitID, selected);
        UiStyle.lock(txtVaccine, selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        selectedVaccine = null;
        txtVaccinationID.setText("");
        txtVisitID.setText("");
        txtVaccine.setText("");
        lblVaccineInfo.setText(" ");
        lblVaccineInfo.setToolTipText(null);
        txtInjectionDate.setText(LocalDate.now().format(UiStyle.DATE_FMT));
        txtNextDate.setText("");
        setSelectedMode(false);
    }
}