
package com.mycompany.petclinic.ui.doctor;

import com.mycompany.petclinic.dao.DoctorDAO;
import com.mycompany.petclinic.model.Doctor;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DoctorManagementPanel extends JPanel {

    private static final String FONT = "Segoe UI";
    private static final Color BG = new Color(244, 247, 252);
    private static final Color PRIMARY = new Color(46, 98, 196);
    private static final Color PRIMARY_DARK = new Color(28, 52, 102);
    private static final Color TEXT = new Color(33, 42, 60);
    private static final Color LABEL = new Color(88, 98, 120);
    private static final Color LINE = new Color(205, 212, 226);
    private static final Color READONLY = new Color(237, 240, 246);
    private static final Color ZEBRA = new Color(246, 249, 255);
    private static final Color SELECT = new Color(213, 228, 252);
    private static final Color DISABLED = new Color(208, 213, 223);

    private JPanel pnlTop, pnlFields, pnlActions;
    private JLabel lblTitle;

    private JTextField txtID, txtName, txtPhone,
            txtEmail, txtSpecialization, txtLicense, txtSearch;

    private JComboBox<String> cboStatus;

    private JButton btnAdd, btnUpdate, btnDelete,
            btnClear, btnSearch;

    private JTable tblData;
    private DefaultTableModel model;

    public DoctorManagementPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false);
        loadDoctors();
    }

    // ================== GIAO DIỆN ==================

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));

        pnlTop = new JPanel(new BorderLayout(0, 12));

        lblTitle = new JLabel(
                "QUẢN LÝ BÁC SĨ", SwingConstants.CENTER);

        pnlTop.add(lblTitle, BorderLayout.NORTH);

        pnlFields = new JPanel(new GridLayout(4, 4, 16, 12));

        txtID = new JTextField();
        txtName = new JTextField();
        txtPhone = new JTextField();
        txtEmail = new JTextField();
        txtSpecialization = new JTextField();
        txtLicense = new JTextField();

        cboStatus = new JComboBox<>(new String[]{
            "Đang làm việc",
            "Nghỉ phép",
            "Nghỉ việc"
        });

        txtID.setEditable(false);

        addField("Mã bác sĩ", txtID);
        addField("Họ và tên (*)", txtName);
        addField("Số điện thoại (*)", txtPhone);
        addField("Email", txtEmail);
        addField("Chuyên môn (*)", txtSpecialization);
        addField("Số giấy phép", txtLicense);
        addField("Trạng thái", cboStatus);

        pnlTop.add(pnlFields, BorderLayout.CENTER);

        pnlActions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0));

        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Xóa form");

        txtSearch = new JTextField(18);
        btnSearch = new JButton("Tìm kiếm");

        pnlActions.add(btnAdd);
        pnlActions.add(btnUpdate);
        pnlActions.add(btnDelete);
        pnlActions.add(btnClear);
        pnlActions.add(txtSearch);
        pnlActions.add(btnSearch);

        pnlTop.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        tblData = new JTable();
        JScrollPane scroll = new JScrollPane(tblData);

        add(scroll, BorderLayout.CENTER);
    }

    private void addField(String label, Component field) {
        JLabel lbl = new JLabel(label);
        pnlFields.add(lbl);
        pnlFields.add(field);
    }

    // ================== KHỞI TẠO BẢNG ==================

    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã bác sĩ",
            "Họ và tên",
            "Số điện thoại",
            "Email",
            "Chuyên môn",
            "Số giấy phép",
            "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {90, 170, 120, 180, 160, 140, 120};

        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }
    }

    // ================== SỰ KIỆN ==================

    private void bindEvents() {
        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearFields());

        btnSearch.addActionListener(e -> searchDoctors());
        txtSearch.addActionListener(e -> searchDoctors());

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    // ================== TẢI DỮ LIỆU ==================

    private void loadDoctors() {
        try {
            DoctorDAO dao = new DoctorDAO();
            List<Doctor> list = dao.findAll();

            model.setRowCount(0);

            for (Doctor d : list) {
                addDoctorRow(d);
            }

        } catch (Exception e) {
            showError("Không thể tải danh sách bác sĩ: "
                    + e.getMessage());
        }
    }

    private void addDoctorRow(Doctor d) {
        model.addRow(new Object[]{
            d.getDoctorID(),
            d.getFullName(),
            d.getPhone(),
            d.getEmail(),
            d.getSpecialization(),
            d.getLicenseNumber(),
            d.getStatus()
        });
    }

    // ================== THÊM BÁC SĨ ==================

    private void addDoctor() {
        try {
            Doctor doctor = getDoctorFromForm();

            int id = new DoctorDAO().insert(doctor);

            if (id > 0) {
                message("Thêm bác sĩ thành công! Mã: " + id);
                loadDoctors();
                clearFields();
            }

        } catch (Exception e) {
            showError("Không thể thêm bác sĩ: " + e.getMessage());
        }
    }

    // ================== CẬP NHẬT ==================

    private void updateDoctor() {
        try {
            if (txtID.getText().trim().isEmpty()) {
                message("Vui lòng chọn bác sĩ cần cập nhật!");
                return;
            }

            Doctor doctor = getDoctorFromForm();
            doctor.setDoctorID(
                    Integer.parseInt(txtID.getText().trim()));

            boolean updated = new DoctorDAO().update(doctor);

            if (updated) {
                message("Cập nhật bác sĩ thành công!");
                loadDoctors();
                clearFields();
            } else {
                message("Không tìm thấy bác sĩ cần cập nhật!");
            }

        } catch (Exception e) {
            showError("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // ================== XÓA BÁC SĨ ==================

    private void deleteDoctor() {
        try {
            if (txtID.getText().trim().isEmpty()) {
                message("Vui lòng chọn bác sĩ cần xóa!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa bác sĩ này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            int id = Integer.parseInt(txtID.getText().trim());

            boolean deleted = new DoctorDAO().delete(id);

            if (deleted) {
                message("Xóa bác sĩ thành công!");
                loadDoctors();
                clearFields();
            } else {
                message("Không tìm thấy bác sĩ!");
            }

        } catch (Exception e) {
            showError("Không thể xóa bác sĩ: " + e.getMessage()
                    + "\nNếu bác sĩ đã có lịch hẹn, hãy chuyển trạng thái "
                    + "thay vì xóa.");
        }
    }

    // ================== TÌM KIẾM ==================

    private void searchDoctors() {
        try {
            String keyword = txtSearch.getText().trim();

            if (keyword.isEmpty()) {
                loadDoctors();
                return;
            }

            List<Doctor> list =
                    new DoctorDAO().findByKeyword(keyword);

            model.setRowCount(0);

            for (Doctor d : list) {
                addDoctorRow(d);
            }

            if (model.getRowCount() == 0) {
                message("Không tìm thấy bác sĩ phù hợp!");
            }

            clearFields();

        } catch (Exception e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // ================== LẤY DỮ LIỆU FORM ==================

    private Doctor getDoctorFromForm() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String specialization =
                txtSpecialization.getText().trim();
        String license = txtLicense.getText().trim();
        String status = cboStatus.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty()
                || specialization.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ họ tên, số điện thoại và chuyên môn!");
        }

        return new Doctor(
                name, phone, email,
                specialization, license, status);
    }

    // ================== CHỌN DÒNG ==================

    private void selectRow() {
        int selected = tblData.getSelectedRow();

        if (selected == -1) {
            return;
        }

        int row = tblData.convertRowIndexToModel(selected);

        txtID.setText(cell(row, 0));
        txtName.setText(cell(row, 1));
        txtPhone.setText(cell(row, 2));
        txtEmail.setText(cell(row, 3));
        txtSpecialization.setText(cell(row, 4));
        txtLicense.setText(cell(row, 5));
        cboStatus.setSelectedItem(cell(row, 6));

        setSelectedMode(true);
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnDelete.setEnabled(selected);
    }

    private void clearFields() {
        tblData.clearSelection();

        txtID.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtSpecialization.setText("");
        txtLicense.setText("");

        cboStatus.setSelectedIndex(0);

        setSelectedMode(false);
        txtName.requestFocus();
    }

    // ================== LÀM ĐẸP GIAO DIỆN ==================

    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        pnlTop.setOpaque(false);
        pnlActions.setOpaque(false);

        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);

        pnlFields.setBackground(Color.WHITE);

        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin bác sĩ ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13),
                PRIMARY);

        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled,
                BorderFactory.createEmptyBorder(8, 14, 12, 14)));

        for (Component c : pnlFields.getComponents()) {
            if (c instanceof JLabel) {
                c.setFont(new Font(FONT, Font.BOLD, 13));
                c.setForeground(LABEL);
            }

            if (c instanceof JTextField) {
                styleField((JTextField) c);
            }
        }

        txtID.setBackground(READONLY);

        cboStatus.setFont(new Font(FONT, Font.PLAIN, 14));

        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnDelete, new Color(210, 70, 70));
        styleButton(btnClear, new Color(110, 120, 140));
        styleButton(btnSearch, PRIMARY);
        styleField(txtSearch);

        tblData.setFont(new Font(FONT, Font.PLAIN, 14));
        tblData.setRowHeight(32);
        tblData.setShowVerticalLines(false);
        tblData.setShowHorizontalLines(true);
        tblData.setGridColor(new Color(232, 236, 244));
        tblData.setSelectionBackground(SELECT);
        tblData.setSelectionForeground(TEXT);
        tblData.setFillsViewportHeight(true);

        tblData.setDefaultRenderer(
                Object.class, new CellRenderer());

        tblData.setDefaultRenderer(
                Number.class, new CellRenderer());

        JTableHeader header = tblData.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(
                new Dimension(header.getPreferredSize().width, 38));
    }

    private void styleField(JTextField f) {
        f.setFont(new Font(FONT, Font.PLAIN, 14));
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY);

        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    private void styleButton(JButton b, Color base) {
        b.setUI(new BasicButtonUI());
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(base);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean sel,
                boolean focus, int row, int col) {

            super.getTableCellRendererComponent(
                    t, value, sel, false, row, col);

            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setForeground(TEXT);
            setBackground(sel ? SELECT
                    : (row % 2 == 0 ? Color.WHITE : ZEBRA));

            return this;
        }
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean sel,
                boolean focus, int row, int col) {

            super.getTableCellRendererComponent(
                    t, value, false, false, row, col);

            setOpaque(true);
            setBackground(PRIMARY_DARK);
            setForeground(Color.WHITE);
            setFont(new Font(FONT, Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            return this;
        }
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(
                this, text, "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String text) {
        JOptionPane.showMessageDialog(
                this, text, "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}
