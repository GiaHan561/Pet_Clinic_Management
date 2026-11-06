
package com.mycompany.petclinic.ui.customer;

import com.mycompany.petclinic.dao.CustomerDAO;
import com.mycompany.petclinic.model.Customer;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/** Giao diện quản lý khách hàng (NetBeans GUI Builder). */
public class CustomerPanel extends javax.swing.JPanel {
    private final CustomerDAO dao = new CustomerDAO();
    private DefaultTableModel model;

    public CustomerPanel() {
        initComponents();
        model = new DefaultTableModel(new String[]{
            "Mã KH", "Họ tên", "SĐT", "Email", "Địa chỉ", "Ngày đăng ký"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData.setModel(model);
        tblData.setRowHeight(27);
        tblData.setAutoCreateRowSorter(true);
        btnAdd.addActionListener(e -> addCustomer());
        btnSearch.addActionListener(e -> searchCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnAll.addActionListener(e -> loadCustomers());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectRow();
        });
        loadCustomers();
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = dao.findAll();
            model.setRowCount(0);
            for (Customer customer : customers) addRow(customer);
        } catch (SQLException ex) { showError(ex); }
    }

    private void addRow(Customer c) {
        model.addRow(new Object[]{c.getCustomerID(), c.getFullName(),
            c.getPhone(), c.getEmail(), c.getAddress(), c.getCreatedTime()});
    }

    private void addCustomer() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (!valid(name, phone)) return;
        Customer customer = new Customer(name, phone,
            optional(txtEmail.getText()), optional(txtAddress.getText()));
        try {
            int id = dao.insert(customer); // DAO hiện tại trả về mã ID mới
            if (id > 0) {
                message("Đã thêm khách hàng, mã: " + id);
                loadCustomers(); clearFields();
            } else message("Không lấy được mã khách hàng vừa tạo.");
        } catch (SQLException ex) { showError(ex); }
    }

    private void searchCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) { message("Hãy nhập số điện thoại cần tìm."); return; }
        try {
            Customer c = dao.findByPhone(phone);
            model.setRowCount(0);
            if (c == null) { message("Không tìm thấy khách hàng."); return; }
            addRow(c); showCustomer(c);
        } catch (SQLException ex) { showError(ex); }
    }

    private void updateCustomer() {
        if (txtID.getText().isBlank()) {
            message("Chọn khách hàng trong bảng trước khi cập nhật."); return;
        }
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (!valid(name, phone)) return;
        Customer c = new Customer(name, phone,
            optional(txtEmail.getText()), optional(txtAddress.getText()));
        c.setCustomerID(Integer.parseInt(txtID.getText()));
        try {
            if (dao.update(c)) {
                message("Cập nhật thành công."); loadCustomers(); clearFields();
            } else message("Không tìm thấy khách hàng để cập nhật.");
        } catch (SQLException ex) { showError(ex); }
    }

    private boolean valid(String name, String phone) {
        if (name.isEmpty() || phone.isEmpty()) {
            message("Họ tên và số điện thoại không được trống."); return false;
        }
        if (name.length() > 100 || phone.length() > 15
                || txtEmail.getText().trim().length() > 100
                || txtAddress.getText().trim().length() > 255) {
            message("Dữ liệu vượt quá độ dài quy định của SQL Server."); return false;
        }
        return true;
    }

    private void selectRow() {
        int selected = tblData.getSelectedRow();
        if (selected == -1) return;
        int r = tblData.convertRowIndexToModel(selected);
        txtID.setText(cell(r, 0)); txtName.setText(cell(r, 1));
        txtPhone.setText(cell(r, 2)); txtEmail.setText(cell(r, 3));
        txtAddress.setText(cell(r, 4)); txtCreated.setText(cell(r, 5));
    }

    private void showCustomer(Customer c) {
        txtID.setText(String.valueOf(c.getCustomerID()));
        txtName.setText(c.getFullName()); txtPhone.setText(c.getPhone());
        txtEmail.setText(c.getEmail() == null ? "" : c.getEmail());
        txtAddress.setText(c.getAddress() == null ? "" : c.getAddress());
        txtCreated.setText(c.getCreatedTime() == null ? "" : c.getCreatedTime().toString());
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }
    private String optional(String s) { return s.trim().isEmpty() ? null : s.trim(); }
    private void clearFields() {
        tblData.clearSelection(); txtID.setText(""); txtName.setText("");
        txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText("");
        txtCreated.setText("");
    }
    private void message(String m) { JOptionPane.showMessageDialog(this, m); }
    private void showError(SQLException ex) {
        String message = ex.getMessage();

        if (ex.getErrorCode() == 547
                && message != null
                && message.contains("CK_CUSTOMERS_phone")) {
            message = "Số điện thoại phải gồm đúng 10 chữ số.";
        }

        JOptionPane.showMessageDialog(
                this, message, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlTop = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlFields = new javax.swing.JPanel();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblCreated = new javax.swing.JLabel();
        txtCreated = new javax.swing.JTextField();
        pnlActions = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        scrollTable = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(8, 8));
        pnlTop.setLayout(new java.awt.BorderLayout(8, 8));
        lblTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 21));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("QUẢN LÝ KHÁCH HÀNG");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);
        pnlFields.setLayout(new java.awt.GridLayout(3, 4, 8, 8));
        lblID.setText("Mã KH");
        pnlFields.add(lblID);
        txtID.setEditable(false);
        pnlFields.add(txtID);
        lblName.setText("Họ tên");
        pnlFields.add(lblName);
        pnlFields.add(txtName);
        lblPhone.setText("Số điện thoại");
        pnlFields.add(lblPhone);
        pnlFields.add(txtPhone);
        lblEmail.setText("Email");
        pnlFields.add(lblEmail);
        pnlFields.add(txtEmail);
        lblAddress.setText("Địa chỉ");
        pnlFields.add(lblAddress);
        pnlFields.add(txtAddress);
        lblCreated.setText("Ngày đăng ký");
        pnlFields.add(lblCreated);
        txtCreated.setEditable(false);
        pnlFields.add(txtCreated);
        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);
        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        btnAdd.setText("Thêm");
        pnlActions.add(btnAdd);
        btnSearch.setText("Tìm SĐT");
        pnlActions.add(btnSearch);
        btnUpdate.setText("Cập nhật");
        pnlActions.add(btnUpdate);
        btnAll.setText("Xem tất cả");
        pnlActions.add(btnAll);
        btnClear.setText("Xóa form");
        pnlActions.add(btnClear);
        pnlTop.add(pnlActions, java.awt.BorderLayout.SOUTH);
        add(pnlTop, java.awt.BorderLayout.NORTH);
        scrollTable.setViewportView(tblData);
        add(scrollTable, java.awt.BorderLayout.CENTER);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
    }//GEN-END:initComponents
    // </editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblCreated;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlActions;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollTable;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtCreated;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables
}
