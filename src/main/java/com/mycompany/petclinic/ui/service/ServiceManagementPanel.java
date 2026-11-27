package com.mycompany.petclinic.ui.service;

import com.mycompany.petclinic.dao.ServiceDAO;
import com.mycompany.petclinic.model.Service;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện quản lý dịch vụ.
 *
 * Cấu trúc đồng bộ với AppointmentManagementPanel / CustomerPanel:
 * initComponents -> initTable -> applyStyle -> bindEvents.
 * Phần xử lý nghiệp vụ (thêm / sửa / ngừng / tìm kiếm) giữ nguyên.
 */
public class ServiceManagementPanel extends JPanel {

    // ----- Bảng màu & font (giống các panel khác) -----
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

    // Trạng thái khớp với CHECK constraint của bảng SERVICES
    private static final String ACTIVE = "Active";
    private static final String INACTIVE = "Inactive";

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    // ----- Thành phần giao diện -----
    private JPanel pnlTop;
    private JPanel pnlFields;
    private JPanel pnlActions;
    private JScrollPane scrollTable;
    private JLabel lblTitle;

    private JLabel lblID, lblStatus, lblName, lblPrice, lblDuration, lblDescription;

    private JTextField txtID, txtName, txtPrice, txtDuration, txtDescription, txtSearch;
    private JComboBox<String> cboStatus;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnShowAll;

    private JTable tblData;
    private DefaultTableModel model;

    private final ServiceDAO serviceDAO = new ServiceDAO();

    public ServiceManagementPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        loadServices();
        setSelectedMode(false);
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------
    private void initComponents() {
        setLayout(new BorderLayout(0, 12));

        pnlTop = new JPanel(new BorderLayout(0, 12));

        // Tiêu đề
        lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ KHÁM VÀ ĐIỀU TRỊ", SwingConstants.CENTER);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        // Lưới nhập liệu: 4 cột (nhãn, ô, nhãn, ô)
        pnlFields = new JPanel(new GridLayout(3, 4, 16, 12));

        lblID = new JLabel("Mã dịch vụ");
        lblStatus = new JLabel("Trạng thái");
        lblName = new JLabel("Tên dịch vụ (*)");
        lblPrice = new JLabel("Giá dịch vụ (VNĐ) (*)");
        lblDuration = new JLabel("Thời gian (phút) (*)");
        lblDescription = new JLabel("Mô tả");

        txtID = new JTextField();
        txtID.setEditable(false);
        txtName = new JTextField();
        txtPrice = new JTextField();
        txtDuration = new JTextField();
        txtDescription = new JTextField();
        txtSearch = new JTextField(16);
        cboStatus = new JComboBox<>(new String[]{ACTIVE, INACTIVE});

        pnlFields.add(lblID);
        pnlFields.add(txtID);
        pnlFields.add(lblStatus);
        pnlFields.add(cboStatus);

        pnlFields.add(lblName);
        pnlFields.add(txtName);
        pnlFields.add(lblPrice);
        pnlFields.add(txtPrice);

        pnlFields.add(lblDuration);
        pnlFields.add(txtDuration);
        pnlFields.add(lblDescription);
        pnlFields.add(txtDescription);

        pnlTop.add(pnlFields, BorderLayout.CENTER);

        // Hàng nút (nút chức năng + ô tìm kiếm cùng một hàng như CustomerPanel)
        pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Ngừng dịch vụ");
        btnClear = new JButton("Xóa form");
        btnSearch = new JButton("Tìm kiếm");
        btnShowAll = new JButton("Xem tất cả");

        pnlActions.add(btnAdd);
        pnlActions.add(btnUpdate);
        pnlActions.add(btnDelete);
        pnlActions.add(btnClear);
        pnlActions.add(txtSearch);
        pnlActions.add(btnSearch);
        pnlActions.add(btnShowAll);

        pnlTop.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        // Bảng
        tblData = new JTable();
        scrollTable = new JScrollPane(tblData);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã dịch vụ", "Tên dịch vụ", "Mô tả",
            "Giá dịch vụ (VNĐ)", "Thời gian (phút)", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                switch (c) {
                    case 0:
                    case 4:
                        return Integer.class;   // sắp xếp theo số
                    case 3:
                        return BigDecimal.class;
                    default:
                        return String.class;
                }
            }
        };
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {90, 180, 280, 140, 120, 100};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblData.getColumnModel().getColumn(0).setMaxWidth(120);
    }

    // ------------------------------------------------------------------
    // Sự kiện
    // ------------------------------------------------------------------
    private void bindEvents() {
        btnAdd.addActionListener(e -> addService());
        btnUpdate.addActionListener(e -> updateService());
        btnDelete.addActionListener(e -> deactivateService());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchServices());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadServices();
        });
        txtSearch.addActionListener(e -> searchServices());

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblData.getSelectedRow() >= 0) {
                selectRow();
            }
        });
    }

    // ------------------------------------------------------------------
    // Làm đẹp giao diện (giống AppointmentManagementPanel)
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
                " Thông tin dịch vụ ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblID, lblStatus, lblName, lblPrice,
            lblDuration, lblDescription}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtID, txtName, txtPrice,
            txtDuration, txtDescription, txtSearch}) {
            styleField(f);
        }
        txtID.setBackground(READONLY);
        styleCombo(cboStatus);

        // Nút bấm
        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnDelete, new Color(210, 70, 70));
        styleButton(btnClear, new Color(110, 120, 140));
        styleButton(btnSearch, PRIMARY);
        styleButton(btnShowAll, new Color(26, 148, 168));

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

        // Cột 0, 4, 5 căn giữa; cột 3 (giá) căn phải; còn lại căn trái
        int[] aligns = {
            SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
            SwingConstants.RIGHT, SwingConstants.CENTER, SwingConstants.CENTER
        };
        for (int i = 0; i < aligns.length; i++) {
            tblData.getColumnModel().getColumn(i)
                    .setCellRenderer(new CellRenderer(aligns[i]));
        }

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

    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font(FONT, Font.PLAIN, 14));
        c.setForeground(TEXT);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createLineBorder(LINE, 1, true));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề theo cột, định dạng tiền. */
    private class CellRenderer extends DefaultTableCellRenderer {

        private final int align;

        CellRenderer(int align) {
            this.align = align;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = (value instanceof BigDecimal) ? MONEY.format(value) : value;
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(align);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));
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
            setHorizontalAlignment(
                    (col == 0 || col == 4 || col == 5) ? CENTER : LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Tải dữ liệu
    // ------------------------------------------------------------------
    private void loadServices() {
        try {
            model.setRowCount(0);
            List<Service> services = serviceDAO.findAll();
            for (Service s : services) {
                addServiceRow(s);
            }
        } catch (Exception e) {
            showError("Không thể tải danh sách dịch vụ: " + e.getMessage());
        }
    }

    private void addServiceRow(Service s) {
        model.addRow(new Object[]{
            s.getServiceId(),
            s.getServiceName(),
            s.getDescription(),
            s.getPrice(),
            s.getDuration(),
            s.getStatus()
        });
    }

    // ------------------------------------------------------------------
    // Lấy dữ liệu từ form
    // ------------------------------------------------------------------
    private Service getServiceFromForm() {
        String name = txtName.getText().trim();
        String priceText = txtPrice.getText().trim();
        String durationText = txtDuration.getText().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên dịch vụ.");
        }
        if (priceText.isEmpty() || durationText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập giá và thời gian dịch vụ.");
        }

        BigDecimal price = new BigDecimal(priceText);
        int duration = Integer.parseInt(durationText);

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá dịch vụ không được âm.");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Thời gian phải lớn hơn 0.");
        }

        return new Service(
                name,
                txtDescription.getText().trim(),
                price,
                duration,
                cboStatus.getSelectedItem().toString()
        );
    }

    // ------------------------------------------------------------------
    // Thêm
    // ------------------------------------------------------------------
    private void addService() {
        try {
            Service service = getServiceFromForm();
            int id = serviceDAO.insert(service);

            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
                clearForm();
            } else {
                showError("Không thể thêm dịch vụ.");
            }
        } catch (NumberFormatException e) {
            showError("Giá và thời gian phải là số hợp lệ.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Cập nhật
    // ------------------------------------------------------------------
    private void updateService() {
        if (txtID.getText().trim().isEmpty()) {
            showError("Vui lòng chọn dịch vụ cần cập nhật.");
            return;
        }
        try {
            Service service = getServiceFromForm();
            service.setServiceId(Integer.parseInt(txtID.getText()));

            if (serviceDAO.update(service)) {
                JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!");
                clearForm();
            }
        } catch (NumberFormatException e) {
            showError("Giá và thời gian phải là số hợp lệ.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Ngừng dịch vụ (xóa mềm: chỉ đổi status)
    // ------------------------------------------------------------------
    private void deactivateService() {
        if (txtID.getText().trim().isEmpty()) {
            showError("Vui lòng chọn dịch vụ cần ngừng.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn ngừng cung cấp dịch vụ này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Service service = serviceDAO.findById(Integer.parseInt(txtID.getText()));
            if (service == null) {
                showError("Không tìm thấy dịch vụ.");
                return;
            }

            service.setStatus(INACTIVE);

            if (serviceDAO.update(service)) {
                JOptionPane.showMessageDialog(this, "Đã ngừng dịch vụ!");
                clearForm();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Tìm kiếm
    // ------------------------------------------------------------------
    private void searchServices() {
        try {
            model.setRowCount(0);
            String keyword = txtSearch.getText().trim();

            List<Service> services = keyword.isEmpty()
                    ? serviceDAO.findAll()
                    : serviceDAO.findByKeyword(keyword);

            for (Service s : services) {
                addServiceRow(s);
            }
        } catch (Exception e) {
            showError("Tìm kiếm thất bại: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Chọn dòng / xóa form
    // ------------------------------------------------------------------
    private void selectRow() {
        int selected = tblData.getSelectedRow();
        if (selected < 0) {
            return;
        }
        // Bảng có sorter nên phải đổi chỉ số dòng hiển thị sang chỉ số model
        int r = tblData.convertRowIndexToModel(selected);

        txtID.setText(cell(r, 0));
        txtName.setText(cell(r, 1));
        txtDescription.setText(cell(r, 2));

        Object price = model.getValueAt(r, 3);
        txtPrice.setText(price instanceof BigDecimal
                ? ((BigDecimal) price).stripTrailingZeros().toPlainString()
                : cell(r, 3));

        txtDuration.setText(cell(r, 4));
        cboStatus.setSelectedItem(cell(r, 5));

        setSelectedMode(true);
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void clearForm() {
        txtID.setText("");
        txtName.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtDuration.setText("");
        txtSearch.setText("");
        cboStatus.setSelectedIndex(0);

        tblData.clearSelection();
        setSelectedMode(false);
        loadServices();
        txtName.requestFocus();
    }
    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnDelete.setEnabled(selected);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo",
                JOptionPane.ERROR_MESSAGE);
    }
}
