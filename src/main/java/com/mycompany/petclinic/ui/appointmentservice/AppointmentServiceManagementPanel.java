package com.mycompany.petclinic.ui.appointmentservice;

import com.mycompany.petclinic.dao.AppointmentServiceDAO;
import com.mycompany.petclinic.model.AppointmentService;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện quản lý dịch vụ của lịch hẹn.
 *
 * Cấu trúc đồng bộ với AppointmentManagementPanel / ServiceManagementPanel:
 * initComponents -> initTable -> applyStyle -> bindEvents.
 * Phần xử lý nghiệp vụ (tải / thêm / cập nhật / xóa / tính tổng) giữ nguyên.
 */
public class AppointmentServiceManagementPanel extends JPanel {

    // ----- Bảng màu & font (giống các panel khác) -----
    private static final String FONT = "Segoe UI";
    private static final Color BG = new Color(244, 247, 252);
    private static final Color PRIMARY = new Color(46, 98, 196);
    private static final Color PRIMARY_DARK = new Color(28, 52, 102);
    private static final Color TEXT = new Color(33, 42, 60);
    private static final Color LABEL = new Color(88, 98, 120);
    private static final Color LINE = new Color(205, 212, 226);
    private static final Color ZEBRA = new Color(246, 249, 255);
    private static final Color SELECT = new Color(213, 228, 252);
    private static final Color DISABLED = new Color(208, 213, 223);

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private final AppointmentServiceDAO dao = new AppointmentServiceDAO();

    // ----- Thành phần giao diện -----
    private JPanel pnlTop;
    private JPanel pnlFields;
    private JPanel pnlActions;
    private JPanel pnlBottom;
    private JScrollPane scrollTable;
    private JLabel lblTitle;

    private JLabel lblAppointmentID, lblServiceID, lblQuantity, lblUnitPrice;
    private JTextField txtAppointmentID, txtServiceID, txtQuantity, txtUnitPrice;
    private JLabel lblTotal;

    private JButton btnLoad, btnAdd, btnUpdate, btnDelete, btnClear;

    private JTable table;
    private DefaultTableModel tableModel;

    public AppointmentServiceManagementPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false);
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------
    private void initComponents() {
        setLayout(new BorderLayout(0, 12));

        pnlTop = new JPanel(new BorderLayout(0, 12));

        // Tiêu đề
        lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ LỊCH HẸN", SwingConstants.CENTER);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        // Lưới nhập liệu: 4 cột (nhãn, ô, nhãn, ô)
        pnlFields = new JPanel(new GridLayout(2, 4, 16, 12));

        lblAppointmentID = new JLabel("Mã lịch hẹn (*)");
        lblServiceID = new JLabel("Mã dịch vụ (*)");
        lblQuantity = new JLabel("Số lượng thú cưng (*)");
        lblUnitPrice = new JLabel("Đơn giá (VNĐ) - tự động");

        txtAppointmentID = new JTextField();
        txtServiceID = new JTextField();
        txtQuantity = new JTextField();
        txtUnitPrice = new JTextField();
        txtUnitPrice.setEditable(false);   // đơn giá tự lấy từ bảng dịch vụ

        pnlFields.add(lblAppointmentID);
        pnlFields.add(txtAppointmentID);
        pnlFields.add(lblServiceID);
        pnlFields.add(txtServiceID);

        pnlFields.add(lblQuantity);
        pnlFields.add(txtQuantity);
        pnlFields.add(lblUnitPrice);
        pnlFields.add(txtUnitPrice);

        pnlTop.add(pnlFields, BorderLayout.CENTER);

        // Hàng nút
        pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        btnLoad = new JButton("Tải dịch vụ");
        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Xóa form");

        pnlActions.add(btnLoad);
        pnlActions.add(btnAdd);
        pnlActions.add(btnUpdate);
        pnlActions.add(btnDelete);
        pnlActions.add(btnClear);

        pnlTop.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        // Bảng (chiếm toàn bộ phần giữa)
        table = new JTable();
        scrollTable = new JScrollPane(table);
        add(scrollTable, BorderLayout.CENTER);

        // Tổng tiền (góc dưới bên phải)
        lblTotal = new JLabel("Tổng tiền dịch vụ: 0 VNĐ");
        pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.add(lblTotal, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(new String[]{
            "Mã lịch hẹn", "Mã dịch vụ", "Tên dịch vụ",
            "Số lượng thú cưng", "Đơn giá (VNĐ)", "Thành tiền (VNĐ)"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                switch (c) {
                    case 0:
                    case 1:
                    case 3:
                        return Integer.class;   // sắp xếp theo số
                    case 4:
                    case 5:
                        return BigDecimal.class;
                    default:
                        return String.class;
                }
            }
        };
        table.setModel(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {100, 100, 260, 90, 140, 150};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // ------------------------------------------------------------------
    // Sự kiện
    // ------------------------------------------------------------------
    private void bindEvents() {
        btnLoad.addActionListener(e -> loadServices());
        btnAdd.addActionListener(e -> addService());
        btnUpdate.addActionListener(e -> updateService());
        btnDelete.addActionListener(e -> deleteService());
        btnClear.addActionListener(e -> clearForm());

        // Nhập mã dịch vụ -> tự hiện đơn giá lấy từ bảng SERVICES
        txtServiceID.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showServicePrice();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showServicePrice();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showServicePrice();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (table.getSelectedRow() >= 0) {
                    selectRow();
                } else {
                    setSelectedMode(false);
                }
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
        pnlBottom.setOpaque(false);

        // Tiêu đề
        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        // Khung nhập liệu dạng "thẻ" trắng có tiêu đề
        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin dịch vụ của lịch hẹn ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblAppointmentID, lblServiceID,
            lblQuantity, lblUnitPrice}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtAppointmentID, txtServiceID,
            txtQuantity, txtUnitPrice}) {
            styleField(f);
        }

        // Nút bấm
        styleButton(btnLoad, PRIMARY);
        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnDelete, new Color(210, 70, 70));
        styleButton(btnClear, new Color(110, 120, 140));

        // Tổng tiền
        lblTotal.setFont(new Font(FONT, Font.BOLD, 16));
        lblTotal.setForeground(PRIMARY_DARK);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        // Bảng
        table.setFont(new Font(FONT, Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(232, 236, 244));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECT);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        // Cột 0, 1, 3 căn giữa; cột 4, 5 (tiền) căn phải; cột tên căn trái
        int[] aligns = {
            SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT,
            SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.RIGHT
        };
        for (int i = 0; i < aligns.length; i++) {
            table.getColumnModel().getColumn(i)
                    .setCellRenderer(new CellRenderer(aligns[i]));
        }

        JTableHeader header = table.getTableHeader();
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
                    (col == 0 || col == 1 || col == 3) ? CENTER
                    : (col >= 4 ? RIGHT : LEFT));
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Tải dữ liệu
    // ------------------------------------------------------------------
    private void loadServices() {
        try {
            int appointmentID = Integer.parseInt(txtAppointmentID.getText().trim());

            List<AppointmentService> list = dao.findByAppointmentId(appointmentID);

            tableModel.setRowCount(0);

            for (AppointmentService item : list) {
                tableModel.addRow(new Object[]{
                    item.getAppointmentID(),
                    item.getServiceID(),
                    item.getServiceName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
                });
            }

            updateTotal(appointmentID);

        } catch (NumberFormatException e) {
            showError("Vui lòng nhập mã lịch hẹn hợp lệ!");
        }
    }

    // ------------------------------------------------------------------
    // Thêm
    // ------------------------------------------------------------------
    private void addService() {
        try {
            int appointmentID = Integer.parseInt(txtAppointmentID.getText().trim());
            int serviceID = Integer.parseInt(txtServiceID.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            if (quantity <= 0) {
                showError("Số lượng thú cưng phải lớn hơn 0!");
                return;
            }

            if (dao.getServicePrice(serviceID) == null) {
                showError("Mã dịch vụ không tồn tại hoặc đã ngừng cung cấp!");
                return;
            }

            // Đơn giá được lấy tự động từ bảng SERVICES trong DAO
            if (dao.insert(appointmentID, serviceID, quantity)) {
                message("Thêm dịch vụ thành công!");
                loadServices();
            } else {
                showError("Không thể thêm dịch vụ! Kiểm tra lại mã lịch hẹn "
                        + "(có thể không tồn tại hoặc dịch vụ đã được thêm).");
            }

        } catch (NumberFormatException e) {
            showError("Vui lòng kiểm tra lại dữ liệu nhập!");
        }
    }

    // ------------------------------------------------------------------
    // Cập nhật
    // ------------------------------------------------------------------
    private void updateService() {
        try {
            int appointmentID = Integer.parseInt(txtAppointmentID.getText().trim());
            int serviceID = Integer.parseInt(txtServiceID.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            if (quantity <= 0) {
                showError("Số lượng thú cưng phải lớn hơn 0!");
                return;
            }

            if (dao.updateQuantity(appointmentID, serviceID, quantity)) {
                message("Cập nhật thành công!");
                loadServices();
            } else {
                showError("Không tìm thấy dịch vụ cần cập nhật!");
            }

        } catch (NumberFormatException e) {
            showError("Vui lòng nhập dữ liệu hợp lệ!");
        }
    }

    // ------------------------------------------------------------------
    // Xóa
    // ------------------------------------------------------------------
    private void deleteService() {
        try {
            int appointmentID = Integer.parseInt(txtAppointmentID.getText().trim());
            int serviceID = Integer.parseInt(txtServiceID.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa dịch vụ này?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.delete(appointmentID, serviceID)) {
                    message("Xóa dịch vụ thành công!");
                    loadServices();
                } else {
                    showError("Không thể xóa dịch vụ!");
                }
            }

        } catch (NumberFormatException e) {
            showError("Vui lòng nhập mã lịch hẹn và mã dịch vụ!");
        }
    }

    // ------------------------------------------------------------------
    // Chọn dòng / tổng tiền / xóa form
    // ------------------------------------------------------------------
    private void selectRow() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return;
        }
        // Bảng có sorter nên phải đổi chỉ số dòng hiển thị sang chỉ số model
        int r = table.convertRowIndexToModel(selected);

        txtAppointmentID.setText(cell(r, 0));
        txtServiceID.setText(cell(r, 1));
        txtQuantity.setText(cell(r, 3));

        Object price = tableModel.getValueAt(r, 4);
        txtUnitPrice.setText(price instanceof BigDecimal
                ? ((BigDecimal) price).stripTrailingZeros().toPlainString()
                : cell(r, 4));

        setSelectedMode(true);
    }

    private String cell(int row, int col) {
        Object value = tableModel.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void updateTotal(int appointmentID) {
        BigDecimal total = dao.getTotalByAppointmentId(appointmentID);
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        lblTotal.setText("Tổng tiền dịch vụ: " + MONEY.format(total) + " VNĐ");
    }

    // Hiện đơn giá của dịch vụ đang nhập (lấy từ bảng SERVICES)
    private void showServicePrice() {
        try {
            int serviceID = Integer.parseInt(txtServiceID.getText().trim());
            BigDecimal price = dao.getServicePrice(serviceID);
            txtUnitPrice.setText(price == null ? "" : MONEY.format(price));
        } catch (NumberFormatException e) {
            txtUnitPrice.setText("");
        }
    }

    private void clearForm() {
        txtAppointmentID.setText("");
        txtServiceID.setText("");
        txtQuantity.setText("");
        txtUnitPrice.setText("");

        tableModel.setRowCount(0);
        lblTotal.setText("Tổng tiền dịch vụ: 0 VNĐ");

        table.clearSelection();
        setSelectedMode(false);
        txtAppointmentID.requestFocus();
    }

    /**
     * Cập nhật / Xóa chỉ dùng được khi đang chọn một dòng. Nút Thêm luôn bật
     * vì người dùng cần thêm nhiều dịch vụ liên tiếp cho cùng một lịch hẹn.
     */
    private void setSelectedMode(boolean selected) {
        btnUpdate.setEnabled(selected);
        btnDelete.setEnabled(selected);
    }

    private void message(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo",
                JOptionPane.ERROR_MESSAGE);
    }
}
