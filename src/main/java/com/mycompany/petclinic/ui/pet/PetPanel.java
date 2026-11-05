
package com.mycompany.petclinic.ui.pet;

import com.mycompany.petclinic.dao.CustomerDAO;
import com.mycompany.petclinic.dao.PetDAO;
import com.mycompany.petclinic.dao.PetTypeDAO;
import com.mycompany.petclinic.model.Pet;
import com.mycompany.petclinic.model.PetType;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/** Giao diện quản lý thú cưng, loài và thống kê (NetBeans GUI Builder). */
public class PetPanel extends javax.swing.JPanel {
    private final PetDAO petDAO = new PetDAO();
    private final PetTypeDAO typeDAO = new PetTypeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private DefaultTableModel model;

    public PetPanel() {
        initComponents();
        model = new DefaultTableModel(new String[]{
            "Mã pet", "Mã KH", "Mã loài", "Tên", "Giới tính",
            "Ngày sinh", "Cân nặng", "Ghi chú"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData.setModel(model);
        tblData.setRowHeight(27);
        tblData.setAutoCreateRowSorter(true);
        cbGender.setModel(new DefaultComboBoxModel<>(new String[]{
            "","Đực", "cái"
        }));
        btnAdd.addActionListener(e -> addPet());
        btnSearch.addActionListener(e -> searchPets());
        btnUpdate.addActionListener(e -> updatePet());
        btnAll.addActionListener(e -> loadPets());
        btnType.addActionListener(e -> addType());
        btnStats.addActionListener(e -> showStats());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectRow();
        });
        loadTypes();
        loadPets();
    }

    private void loadTypes() {
        try {
            cbType.removeAllItems();
            for (PetType t : typeDAO.findAll()) cbType.addItem(t);
            cbType.setSelectedIndex(-1);
        } catch (SQLException ex) { showError(ex); }
    }

    private void loadPets() {
        try { showPets(petDAO.findAll()); }
        catch (SQLException ex) { showError(ex); }
    }

    private void showPets(List<Pet> pets) {
        model.setRowCount(0);
        for (Pet p : pets) model.addRow(new Object[]{
            p.getPetID(), p.getCustomerID(), p.getTypeID(), p.getName(),
            p.getGender(), p.getDateOfBirth(), p.getWeight(), p.getNote()
        });
    }

    private void addPet() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty() || name.length() > 100) {
                message("Tên thú cưng phải có từ 1 đến 100 ký tự."); return;
            }
            int customerId = Integer.parseInt(txtCustomer.getText().trim());
            if (customerId <= 0 || !customerDAO.existsById(customerId)) {
                message("Mã khách hàng không tồn tại."); return;
            }
            PetType type = (PetType) cbType.getSelectedItem();
            if (type == null) { message("Chọn loài hoặc thêm loài mới trước."); return; }
            Pet p = new Pet(customerId, type.getTypeID(), name,
                gender(), parseDate(), parseWeight(), optional(txtNote.getText()));
            if (petDAO.insert(p)) {
                message("Đăng ký thú cưng thành công."); loadPets(); clearFields();
            }
        } catch (SQLException ex) { showError(ex); }
        catch (IllegalArgumentException ex) { message("Dữ liệu không hợp lệ: " + ex.getMessage()); }
    }

    private void searchPets() {
        try {
            int id = Integer.parseInt(txtCustomer.getText().trim());
            if (id <= 0) { message("Mã khách hàng phải lớn hơn 0."); return; }
            List<Pet> list = petDAO.findByCustomerId(id);
            showPets(list);
            if (list.isEmpty()) message("Khách hàng chưa có thú cưng.");
        } catch (SQLException ex) { showError(ex); }
        catch (NumberFormatException ex) { message("Nhập mã khách hàng là số nguyên."); }
    }

    private void updatePet() {
        if (txtID.getText().isBlank()) {
            message("Chọn một thú cưng trong bảng trước khi cập nhật."); return;
        }
        try {
            // Trong schema hiện tại chỉ được cập nhật weight và note.
            // Các giá trị còn lại chỉ dùng để tạo Pet theo constructor sẵn có.
            int id = Integer.parseInt(txtID.getText());
            int customer = Integer.parseInt(txtCustomer.getText());
            PetType type = (PetType) cbType.getSelectedItem();
            if (type == null) { message("Không xác định được loài."); return; }
            Pet p = new Pet(id, customer, type.getTypeID(), txtName.getText().trim(),
                gender(), parseDate(), parseWeight(), optional(txtNote.getText()));
            if (petDAO.update(p)) {
                message("Đã cập nhật cân nặng và ghi chú."); loadPets(); clearFields();
            } else message("Không tìm thấy thú cưng.");
        } catch (SQLException ex) { showError(ex); }
        catch (IllegalArgumentException ex) { message("Dữ liệu không hợp lệ: " + ex.getMessage()); }
    }

    private void addType() {
        JTextField name = new JTextField();
        JTextField description = new JTextField();
        Object[] inputs = {"Tên loài:", name, "Mô tả:", description};
        if (JOptionPane.showConfirmDialog(this, inputs, "Thêm loài mới",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String n = name.getText().trim();
        String d = description.getText().trim();
        if (n.isEmpty() || n.length() > 50 || d.length() > 255) {
            message("Tên loài: 1–50 ký tự; mô tả: tối đa 255 ký tự."); return;
        }
        try {
            if (typeDAO.insert(new PetType(n, d.isEmpty() ? null : d))) {
                message("Đã thêm loài."); loadTypes();
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showStats() {
        try {
            Map<String,Integer> counts = petDAO.countByType();
            DefaultTableModel stats = new DefaultTableModel(
                new String[]{"Tên loài", "Số thú cưng"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Map.Entry<String,Integer> item : counts.entrySet())
                stats.addRow(new Object[]{item.getKey(), item.getValue()});
            JTable table = new JTable(stats);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(420, 220));
            JPanel panel = new JPanel(new BorderLayout(8,8));
            panel.add(new JLabel("Tổng số thú cưng: " + petDAO.countAll()), BorderLayout.NORTH);
            panel.add(scroll, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, panel, "Thống kê theo loài",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) { showError(ex); }
    }

    private void selectRow() {
        int view = tblData.getSelectedRow();
        if (view < 0) return;
        int r = tblData.convertRowIndexToModel(view);
        txtID.setText(cell(r,0)); txtCustomer.setText(cell(r,1));
        txtName.setText(cell(r,3)); txtBirth.setText(cell(r,5));
        txtWeight.setText(cell(r,6)); txtNote.setText(cell(r,7));
        cbGender.setSelectedItem(cell(r,4));
        int typeId = Integer.parseInt(cell(r,2));
        cbType.setSelectedIndex(-1);
        for (int i=0; i<cbType.getItemCount(); i++) {
            if (((PetType) cbType.getItemAt(i)).getTypeID() == typeId) {
                cbType.setSelectedIndex(i); break;
            }
        }
    }

    private String cell(int r, int c) {
        Object x = model.getValueAt(r,c);
        return x == null ? "" : x.toString();
    }
    private LocalDate parseDate() {
        String s = txtBirth.getText().trim();
        if (s.isEmpty()) return null;
        try {
            LocalDate d = LocalDate.parse(s);
            if (d.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
            return d;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày sinh phải có dạng yyyy-MM-dd.");
        }
    }
    private BigDecimal parseWeight() {
        String s = txtWeight.getText().trim();
        if (s.isEmpty()) return null;
        BigDecimal w = new BigDecimal(s);
        if (w.compareTo(BigDecimal.ZERO) <= 0 || w.compareTo(new BigDecimal("999.99")) > 0
                || w.scale()>2)
            throw new IllegalArgumentException("Cân nặng phải từ 0.01 đến 999.99 kg, tối đa 2 số lẻ.");
        return w;
    }
    private String gender() {
        String s = (String) cbGender.getSelectedItem();
        return s == null || s.isEmpty() ? null : s;
    }
    private String optional(String s) { return s.trim().isEmpty() ? null : s.trim(); }
    private void clearFields() {
        tblData.clearSelection(); txtID.setText(""); txtCustomer.setText("");
        txtName.setText(""); txtBirth.setText(""); txtWeight.setText("");
        txtNote.setText(""); cbGender.setSelectedIndex(0); cbType.setSelectedIndex(-1);
    }
    private void message(String msg) { JOptionPane.showMessageDialog(this,msg); }
    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi SQL Server",JOptionPane.ERROR_MESSAGE);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlTop = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlFields = new javax.swing.JPanel();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        lblCustomer = new javax.swing.JLabel();
        txtCustomer = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        cbGender = new javax.swing.JComboBox<>();
        cbType = new javax.swing.JComboBox<>();
        lblGender = new javax.swing.JLabel();
        lblBirth = new javax.swing.JLabel();
        txtBirth = new javax.swing.JTextField();
        lblWeight = new javax.swing.JLabel();
        txtWeight = new javax.swing.JTextField();
        lblNote = new javax.swing.JLabel();
        txtNote = new javax.swing.JTextField();
        pnlActions = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        btnType = new javax.swing.JButton();
        btnStats = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        scrollTable = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(8, 8));
        pnlTop.setLayout(new java.awt.BorderLayout(8, 8));
        lblTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 21));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("QUẢN LÝ THÚ CƯNG");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);
        pnlFields.setLayout(new java.awt.GridLayout(4, 4, 8, 8));
        lblID.setText("Mã thú cưng");
        pnlFields.add(lblID);
        txtID.setEditable(false);
        pnlFields.add(txtID);
        lblCustomer.setText("Mã khách hàng");
        pnlFields.add(lblCustomer);
        pnlFields.add(txtCustomer);
        lblName.setText("Tên thú cưng");
        pnlFields.add(lblName);
        pnlFields.add(txtName);
        lblType.setText("Loài");
        pnlFields.add(lblType);
        pnlFields.add(cbType);
        lblGender.setText("Giới tính");
        pnlFields.add(lblGender);
        pnlFields.add(cbGender);
        lblBirth.setText("Ngày sinh yyyy-MM-dd");
        pnlFields.add(lblBirth);
        pnlFields.add(txtBirth);
        lblWeight.setText("Cân nặng (kg)");
        pnlFields.add(lblWeight);
        pnlFields.add(txtWeight);
        lblNote.setText("Ghi chú sức khỏe");
        pnlFields.add(lblNote);
        pnlFields.add(txtNote);
        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);
        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        btnAdd.setText("Đăng ký");
        pnlActions.add(btnAdd);
        btnSearch.setText("Tìm theo chủ");
        pnlActions.add(btnSearch);
        btnUpdate.setText("Cập nhật");
        pnlActions.add(btnUpdate);
        btnAll.setText("Xem tất cả");
        pnlActions.add(btnAll);
        btnType.setText("Thêm loài");
        pnlActions.add(btnType);
        btnStats.setText("Thống kê");
        pnlActions.add(btnStats);
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
    private javax.swing.JButton btnStats;
    private javax.swing.JButton btnType;
    private javax.swing.JButton btnUpdate;
private javax.swing.JComboBox<String> cbGender;
private javax.swing.JComboBox<PetType> cbType;
    private javax.swing.JLabel lblBirth;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNote;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblWeight;
    private javax.swing.JPanel pnlActions;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollTable;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtBirth;
    private javax.swing.JTextField txtCustomer;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNote;
    private javax.swing.JTextField txtWeight;
    // End of variables declaration//GEN-END:variables
}
