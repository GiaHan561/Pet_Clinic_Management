package com.mycompany.petclinic.ui.customer;
import java.util.function.IntConsumer;

import com.mycompany.petclinic.dao.CustomerDAO;
import com.mycompany.petclinic.model.Customer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện quản lý khách hàng (NetBeans GUI Builder).
 *
 * Cấu trúc form: BorderLayout -> pnlTop (tiêu đề + lưới nhập liệu + hàng nút)
 * ở phía trên, bảng dữ liệu ở giữa. Phần làm đẹp (màu, font, viền, bảng)
 * nằm trong applyStyle() nên Form Editor không ghi đè.
 *
 * @author DELL
 */
public class CustomerPanel extends javax.swing.JPanel {

    // ----- Bảng màu & font -----
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
    private int hoveredCustomerRow = -1;

    private static final Pattern PHONE_REGEX = Pattern.compile("^\\d{10}$");
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CustomerDAO dao = new CustomerDAO();
    private DefaultTableModel model;

    private final IntConsumer openPetsForCustomer;

    public CustomerPanel(IntConsumer openPetsForCustomer) {
        this.openPetsForCustomer = openPetsForCustomer;
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false);
        loadCustomers();
    }

    // ------------------------------------------------------------------
    // Khởi tạo bảng & sự kiện
    // ------------------------------------------------------------------
    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã KH", "Họ tên", "SĐT", "Email", "Địa chỉ", "Ngày đăng ký"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0) {
                    return Integer.class;
                }
                return c == 5 ? LocalDateTime.class : String.class;
            }
        };
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {70, 180, 120, 200, 260, 150};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblData.getColumnModel().getColumn(0).setMaxWidth(90);
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addCustomer());
        btnSearch.addActionListener(e -> searchCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnAll.addActionListener(e -> loadCustomers());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblData.rowAtPoint(e.getPoint());
                int column = tblData.columnAtPoint(e.getPoint());

                if (e.getButton() == MouseEvent.BUTTON1
                        && row >= 0
                        && column >= 0
                        && tblData.convertColumnIndexToModel(column) == 0) {
                    int modelRow = tblData.convertRowIndexToModel(row);
                    int customerId = (Integer) model.getValueAt(modelRow, 0);
                    openPetsForCustomer.accept(customerId);
                }
            }
        });
        MouseAdapter customerHover = new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            int row = tblData.rowAtPoint(e.getPoint());
            int column = tblData.columnAtPoint(e.getPoint());

            boolean overCustomerId = row >= 0 && column >= 0
                    && tblData.convertColumnIndexToModel(column) == 0;

            updateHover(overCustomerId ? row : -1);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateHover(-1);
        }

        private void updateHover(int row) {
            if (hoveredCustomerRow == row) {
                return;
            }
            hoveredCustomerRow = row;
            tblData.setCursor(Cursor.getPredefinedCursor(
                    row >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            tblData.repaint();
        }
    };

    tblData.addMouseMotionListener(customerHover);
    tblData.addMouseListener(customerHover);

    }

    // ------------------------------------------------------------------
    // Làm đẹp giao diện
    // ------------------------------------------------------------------
    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        pnlTop.setOpaque(false);
        pnlActions.setOpaque(false);

        // Tiêu đề
        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        // Khung nhập liệu dạng "thẻ" trắng có tiêu đề
        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin khách hàng ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblID, lblCreated, lblName, lblPhone, lblEmail, lblAddress}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtID, txtCreated, txtName, txtPhone, txtEmail, txtAddress}) {
            styleField(f);
        }
        txtID.setBackground(READONLY);
        txtCreated.setBackground(READONLY);

        // Nút bấm
        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnSearch, PRIMARY);
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnAll, new Color(26, 148, 168));
        styleButton(btnClear, new Color(110, 120, 140));

        // Bảng
        tblData.setFont(new Font(FONT, Font.PLAIN, 14));
        tblData.setRowHeight(32);
        tblData.setShowVerticalLines(false);
        tblData.setShowHorizontalLines(true);
        tblData.setGridColor(new Color(232, 236, 244));
        tblData.setIntercellSpacing(new Dimension(0, 1));
        tblData.setSelectionBackground(SELECT);
        tblData.setSelectionForeground(TEXT);
        tblData.setFillsViewportHeight(true);
        tblData.setDefaultRenderer(Object.class, new CellRenderer());
        tblData.setDefaultRenderer(Number.class, new CellRenderer());

        JTableHeader header = tblData.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        scrollTable.setBorder(BorderFactory.createLineBorder(LINE, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);
    }

    private Border fieldBorder(Color line) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    private void styleField(JTextField f) {
        f.setFont(new Font(FONT, Font.PLAIN, 14));
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY);
        f.setBorder(fieldBorder(LINE));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (f.isEditable()) {
                    f.setBorder(fieldBorder(PRIMARY));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(fieldBorder(LINE));
            }
        });
    }

    private void styleButton(JButton b, Color base) {
        b.setUI(new BasicButtonUI());          // vẽ phẳng, giống nhau trên mọi Look&Feel
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(base);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(base.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(b.isEnabled() ? base : DISABLED);
            }
        });
        b.addPropertyChangeListener("enabled",
                e -> b.setBackground(b.isEnabled() ? base : DISABLED));
    }

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề, định dạng ngày giờ. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = value instanceof LocalDateTime
                    ? ((LocalDateTime) value).format(TIME_FMT) : value;
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(value instanceof Integer ? CENTER : LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));
            if (row == hoveredCustomerRow && t.convertColumnIndexToModel(col) == 0) {
            setBackground(new Color(230, 240, 255));
            }
            return this;
        }
    }

    /** Vẽ tiêu đề cột: nền xanh đậm, chữ trắng đậm. */
    private class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setOpaque(true);
            setBackground(PRIMARY_DARK);
            setForeground(Color.WHITE);
            setFont(new Font(FONT, Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(col == 0 ? CENTER : LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Nghiệp vụ
    // ------------------------------------------------------------------
    private void loadCustomers() {
        try {
            List<Customer> customers = dao.findAll();
            model.setRowCount(0);
            for (Customer customer : customers) {
                addRow(customer);
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addRow(Customer c) {
        model.addRow(new Object[]{c.getCustomerID(), c.getFullName(),
            c.getPhone(), c.getEmail(), c.getAddress(), c.getCreatedTime()});
    }

    private void addCustomer() {
        if (!valid()) {
            return;
        }
        Customer customer = new Customer(txtName.getText().trim(),
                txtPhone.getText().trim(),
                optional(txtEmail.getText()), optional(txtAddress.getText()));
        try {
            int id = dao.insert(customer);
            if (id > 0) {
                message("Đã thêm khách hàng, mã: " + id);
                loadCustomers();
                clearFields();
                openPetsForCustomer.accept(id);
            } else {
                message("Không lấy được mã khách hàng vừa tạo.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void searchCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            message("Hãy nhập số điện thoại cần tìm.");
            txtPhone.requestFocus();
            return;
        }
        try {
            Customer c = dao.findByPhone(phone);
            model.setRowCount(0);
            if (c == null) {
                message("Không tìm thấy khách hàng.");
                return;
            }
            addRow(c);
            tblData.setRowSelectionInterval(0, 0);   // tự điền form qua selectRow()
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateCustomer() {
        if (txtID.getText().isBlank()) {
            message("Chọn khách hàng trong bảng trước khi cập nhật.");
            return;
        }
        if (!valid()) {
            return;
        }
        Customer c = new Customer(txtName.getText().trim(),
                txtPhone.getText().trim(),
                optional(txtEmail.getText()), optional(txtAddress.getText()));
        c.setCustomerID(Integer.parseInt(txtID.getText()));
        try {
            if (dao.update(c)) {
                message("Cập nhật thành công.");
                loadCustomers();
                clearFields();
            } else {
                message("Không tìm thấy khách hàng để cập nhật.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private boolean valid() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) {
            message("Họ tên không được để trống.");
            txtName.requestFocus();
            return false;
        }
        if (!PHONE_REGEX.matcher(phone).matches()) {
            message("Số điện thoại phải gồm đúng 10 chữ số.");
            txtPhone.requestFocus();
            return false;
        }
        if (!email.isEmpty() && !EMAIL_REGEX.matcher(email).matches()) {
            message("Email không hợp lệ.");
            txtEmail.requestFocus();
            return false;
        }
        if (name.length() > 100 || email.length() > 100 || address.length() > 255) {
            message("Dữ liệu vượt quá độ dài quy định (họ tên/email ≤ 100, địa chỉ ≤ 255 ký tự).");
            return false;
        }
        return true;
    }

    private void selectRow() {
        int selected = tblData.getSelectedRow();
        if (selected == -1) {
            return;
        }
        int r = tblData.convertRowIndexToModel(selected);
        txtID.setText(cell(r, 0));
        txtName.setText(cell(r, 1));
        txtPhone.setText(cell(r, 2));
        txtEmail.setText(cell(r, 3));
        txtAddress.setText(cell(r, 4));
        txtCreated.setText(cell(r, 5));
        setSelectedMode(true);
    }

    /** Đang chọn một khách hàng: chỉ cho cập nhật, khóa họ tên (DAO không sửa họ tên). */
    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        txtName.setEditable(!selected);
        txtName.setBackground(selected ? READONLY : Color.WHITE);
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(TIME_FMT);
        }
        return value.toString();
    }

    private String optional(String s) {
        return s.trim().isEmpty() ? null : s.trim();
    }

    private void clearFields() {
        tblData.clearSelection();
        txtID.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtCreated.setText("");
        setSelectedMode(false);
        txtName.requestFocus();
    }

    private void message(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(SQLException ex) {
        String message = ex.getMessage();

        if (ex.getErrorCode() == 547
                && message != null
                && message.contains("CK_CUSTOMERS_phone")) {
            message = "Số điện thoại phải gồm đúng 10 chữ số.";
        } else if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
            message = "Số điện thoại hoặc email này đã tồn tại trong hệ thống.";
        }

        JOptionPane.showMessageDialog(
                this, message, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTop = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlFields = new javax.swing.JPanel();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        lblCreated = new javax.swing.JLabel();
        txtCreated = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        pnlActions = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnAll = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        scrollTable = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 12));

        pnlTop.setLayout(new java.awt.BorderLayout(0, 12));

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("QUẢN LÝ KHÁCH HÀNG");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(3, 4, 16, 12));

        lblID.setText("Mã KH");
        pnlFields.add(lblID);

        txtID.setEditable(false);
        pnlFields.add(txtID);

        lblCreated.setText("Ngày đăng ký");
        pnlFields.add(lblCreated);

        txtCreated.setEditable(false);
        pnlFields.add(txtCreated);

        lblName.setText("Họ tên (*)");
        pnlFields.add(lblName);
        pnlFields.add(txtName);

        lblPhone.setText("Số điện thoại (*)");
        pnlFields.add(lblPhone);
        pnlFields.add(txtPhone);

        lblEmail.setText("Email");
        pnlFields.add(lblEmail);
        pnlFields.add(txtEmail);

        lblAddress.setText("Địa chỉ");
        pnlFields.add(lblAddress);
        pnlFields.add(txtAddress);

        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));

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
    }// </editor-fold>//GEN-END:initComponents


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