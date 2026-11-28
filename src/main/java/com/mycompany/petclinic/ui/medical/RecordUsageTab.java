package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.PrescriptionDetailDAO;
import com.mycompany.petclinic.dao.RecordLookupDAO;
import com.mycompany.petclinic.dao.VaccinationDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.Vaccination;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

public class RecordUsageTab extends JPanel {

    private final RecordLookupDAO lookupDAO = new RecordLookupDAO();
    private final PrescriptionDetailDAO detailDAO = new PrescriptionDetailDAO();
    private final VaccinationDAO vaccinationDAO = new VaccinationDAO();

    private ReadOnlyModel recordModel, medicineModel, vaccineModel;
    private List<MedicalRecord> currentRecords = new ArrayList<>();

    private JTextField txtRecordID, txtPetID;
    private JButton btnSearch, btnAll, btnClear, btnManage;
    private JTable tblRecords, tblMedicines, tblVaccines;
    private JScrollPane scrollRecords, scrollMedicines, scrollVaccines;

    public RecordUsageTab() {
        initComponents();
        bindEvents();
        loadRecords(null, null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel lblRecord = new JLabel("Mã hồ sơ khám bệnh");
        JLabel lblPet = new JLabel("Mã thú cưng");
        UiStyle.label(lblRecord);
        UiStyle.label(lblPet);

        txtRecordID = new JTextField();
        txtPetID = new JTextField();
        UiStyle.field(txtRecordID);
        UiStyle.field(txtPetID);
        txtRecordID.setPreferredSize(new Dimension(150, 38));
        txtPetID.setPreferredSize(new Dimension(150, 38));

        btnSearch = new JButton("Tìm kiếm");
        btnAll = new JButton("Xem tất cả");
        btnClear = new JButton("Xóa form");
        btnManage = new JButton("Kê đơn thuốc & vaccine");
        UiStyle.button(btnSearch, UiStyle.PRIMARY);
        UiStyle.button(btnAll, new Color(26, 148, 168));
        UiStyle.button(btnClear, new Color(110, 120, 140));
        UiStyle.button(btnManage, new Color(125, 87, 194));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        inputRow.setOpaque(false);
        inputRow.add(lblRecord);
        inputRow.add(txtRecordID);
        inputRow.add(lblPet);
        inputRow.add(txtPetID);
        
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        buttonRow.setOpaque(false);
        buttonRow.add(btnSearch);
        buttonRow.add(btnAll);
        buttonRow.add(btnClear);
        buttonRow.add(btnManage);
        
        JPanel search = new JPanel(new GridLayout(2, 1, 0, 4));
        search.add(inputRow);
        search.add(buttonRow);
        UiStyle.card(search, "Tìm hồ sơ khám bệnh");

        recordModel = new ReadOnlyModel(new String[]{
            "Mã hồ sơ", "Mã thú cưng", "Ngày tạo", "Nhóm máu", "Ghi chú dị ứng", "Ghi chú"
        }, Integer.class, Integer.class, LocalDate.class, String.class, String.class, String.class);
        tblRecords = new JTable(recordModel);
        tblRecords.setAutoCreateRowSorter(true);
        tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollRecords = new JScrollPane(tblRecords);
        UiStyle.table(tblRecords, scrollRecords);

        medicineModel = new ReadOnlyModel(new String[]{
            "Mã đơn", "Ngày kê", "Thuốc", "Liều lượng", "Tần suất",
            "Thời gian uống", "Số lượng", "Đơn vị"
        }, Integer.class, LocalDateTime.class, String.class, String.class, String.class,
                String.class, Integer.class, String.class);
        tblMedicines = new JTable(medicineModel);
        tblMedicines.setAutoCreateRowSorter(true);
        scrollMedicines = new JScrollPane(tblMedicines);
        UiStyle.table(tblMedicines, scrollMedicines);
        scrollMedicines.setBorder(titledBorder("Thuốc đang dùng / đã kê trong hồ sơ"));

        vaccineModel = new ReadOnlyModel(new String[]{
            "Mã lần khám", "Vaccine", "Ngày tiêm", "Tiêm nhắc lại", "Trạng thái"
        }, Integer.class, String.class, LocalDate.class, LocalDate.class, String.class);
        tblVaccines = new JTable(vaccineModel);
        tblVaccines.setAutoCreateRowSorter(true);
        scrollVaccines = new JScrollPane(tblVaccines);
        UiStyle.table(tblVaccines, scrollVaccines);
        scrollVaccines.setBorder(titledBorder("Vaccine đã tiêm / cần tiêm"));

        JPanel usage = new JPanel(new GridLayout(1, 2, 12, 0));
        usage.setOpaque(false);
        usage.add(scrollMedicines);
        usage.add(scrollVaccines);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollRecords, usage);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.35);
        split.setDividerSize(10);

        add(search, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiStyle.LINE, 1, true),
                " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(UiStyle.FONT, Font.BOLD, 13), UiStyle.PRIMARY);
    }

    private void bindEvents() {
        btnSearch.addActionListener(e -> search());
        txtRecordID.addActionListener(e -> search());
        txtPetID.addActionListener(e -> search());
        btnAll.addActionListener(e -> clearFields());
        btnClear.addActionListener(e -> clearFields());
        btnManage.addActionListener(e -> openManager());

        tblRecords.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadUsage(selectedRecord());
            }
        });
        tblRecords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
                        && tblRecords.rowAtPoint(e.getPoint()) >= 0) {
                    openManager();
                }
            }
        });
    }

    private Integer optionalId(String text, String name) {
        return text.trim().isEmpty() ? null : UiStyle.positiveInt(text, name);
    }

    private void search() {
        try {
            Integer recordId = optionalId(txtRecordID.getText(), "Mã hồ sơ");
            Integer petId = optionalId(txtPetID.getText(), "Mã thú cưng");
            List<MedicalRecord> list = loadRecords(recordId, petId);
            if (list == null) {
                return;
            }
            if (list.isEmpty()) {
                UiStyle.message(this, "Không tìm thấy hồ sơ khám bệnh phù hợp.");
            } else if (list.size() == 1) {
                tblRecords.setRowSelectionInterval(0, 0);
            }
        } catch (IllegalArgumentException ex) {
            UiStyle.message(this, ex.getMessage());
        }
    }

    private List<MedicalRecord> loadRecords(Integer recordId, Integer petId) {
        try {
            List<MedicalRecord> list = lookupDAO.search(recordId, petId);
            currentRecords = list;
            recordModel.setRowCount(0);
            for (MedicalRecord r : list) {
                recordModel.addRow(new Object[]{
                    r.getMedicalRecordID(), r.getPetID(), r.getCreatedDate(),
                    UiStyle.nvl(r.getBloodType()), UiStyle.nvl(r.getAllergy()),
                    UiStyle.nvl(r.getNote())
                });
            }
            medicineModel.setRowCount(0);
            vaccineModel.setRowCount(0);
            return list;
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
            return null;
        }
    }

    private MedicalRecord selectedRecord() {
        int view = tblRecords.getSelectedRow();
        if (view < 0) {
            return null;
        }
        int index = tblRecords.convertRowIndexToModel(view);
        return index < currentRecords.size() ? currentRecords.get(index) : null;
    }

    private void loadUsage(MedicalRecord record) {
        medicineModel.setRowCount(0);
        vaccineModel.setRowCount(0);
        if (record == null) {
            return;
        }
        try {
            for (Object[] row : detailDAO.findUsageByRecordId(record.getMedicalRecordID())) {
                medicineModel.addRow(row);
            }
            for (Vaccination v : vaccinationDAO.findByRecordId(record.getMedicalRecordID())) {
                vaccineModel.addRow(new Object[]{
                    v.getVisitID(), v.getVaccineName(), v.getInjectionDate(),
                    v.getNextInjectionDate(), v.getStatus()
                });
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private void clearFields() {
        txtRecordID.setText("");
        txtPetID.setText("");
        tblRecords.clearSelection();
        loadRecords(null, null);
    }

    private void openManager() {
        MedicalRecord record = selectedRecord();
        if (record == null) {
            UiStyle.message(this, "Hãy chọn một hồ sơ khám bệnh trong danh sách.");
            return;
        }
        PrescriptionManagerFrame frame = new PrescriptionManagerFrame(record, this);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadUsage(record);
            }
        });
        frame.setVisible(true);
    }
}
