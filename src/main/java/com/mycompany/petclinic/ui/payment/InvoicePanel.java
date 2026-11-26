package com.mycompany.petclinic.ui.invoice;

import com.mycompany.petclinic.dao.InvoiceDAO;
import com.mycompany.petclinic.model.Invoice;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện quản lý hóa đơn: lập hóa đơn từ một lần khám, xem danh sách,
 * tìm theo mã hóa đơn / mã khách hàng, hủy hóa đơn và chuyển sang thanh toán.
 *
 * Cùng phong cách với DashboardPanel / CustomerPanel.
 */
public class InvoicePanel extends JPanel {

    // ----- Bảng màu & font (đồng bộ với các panel khác) -----
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

    private static final Color STATUS_UNPAID = new Color(200, 110, 0);
    private static final Color STATUS_PAID = new Color(30, 130, 70);
    private static final Color STATUS_CANCELLED = new Color(120, 128, 145);

    // Số tiền: chỉ gồm chữ số (đồng), tối đa 13 chữ số
    private static final Pattern MONEY_REGEX = Pattern.compile("^\\d{1,13}$");

    private final InvoiceDAO dao = new InvoiceDAO();
    private final IntConsumer openPaymentForInvoice;

    private DefaultTableModel model;
    // Danh sách hóa đơn đang hiển thị, cùng thứ tự với các dòng của model
    private List<Invoice> rows = new ArrayList<>();
    // Chi phí vừa tính (chỉ dùng khi đang lập hóa đơn mới)
    private InvoiceDAO.Charges charges;
    // true: đang xem một hóa đơn đã chọn trong bảng; false: đang nhập mới
    private boolean selectedMode;

    public InvoicePanel(IntConsumer openPaymentForInvoice) {
        this.openPaymentForInvoice = openPaymentForInvoice;
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false, null);
        loadInvoices();
    }

    // ------------------------------------------------------------------
    // API cho MainFrame
    // ------------------------------------------------------------------
    /* Nạp lại danh sách (ví dụ sau khi thanh toán xong); giữ hóa đơn đang chọn. */
    public void refresh() {
        int selectedId = currentId();
        loadInvoices();
        if (selectedId > 0) {
            selectInvoiceById(selectedId);
        }
    }

    // ------------------------------------------------------------------
    // Bảng & sự kiện
    // ------------------------------------------------------------------
    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã HĐ", "Mã lần khám", "Mã KH", "Khách hàng", "Thú cưng",
            "Giảm giá", "Tổng tiền", "Đã trả", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c <= 2) {
                    return Integer.class;
                }
                if (c >= 5 && c <= 7) {
                    return BigDecimal.class;
                }
                return String.class;
            }
        };
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {70, 100, 70, 180, 130, 110, 120, 120, 130};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void bindEvents() {
        btnCalc.addActionListener(e -> previewCharges());
        btnCreate.addActionListener(e -> createInvoice());
        btnCancel.addActionListener(e -> cancelInvoice());
        btnPay.addActionListener(e -> payInvoice());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchInvoices());
        txtSearch.addActionListener(e -> searchInvoices());
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadInvoices();
        });

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });

        // Đổi mã lần khám => chi phí đã tính không còn đúng
        onChange(txtVisit, () -> {
            if (!selectedMode) {
                charges = null;
                txtSubtotal.setText("");
                txtTotal.setText("");
            }
        });
        // Đổi giảm giá => tính lại tổng tiền ngay
        onChange(txtDiscount, this::refreshPreview);
    }

    private void onChange(JTextField f, Runnable r) {
        f.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                r.run();
            }
        });
    }

    // ------------------------------------------------------------------
    // Nghiệp vụ
    // ------------------------------------------------------------------
    private void loadInvoices() {
        try {
            showInvoices(dao.findAll());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showInvoices(List<Invoice> list) {
        rows = new ArrayList<>(list);
        model.setRowCount(0);
        for (Invoice i : rows) {
            model.addRow(new Object[]{
                i.getInvoiceID(), i.getVisitID(), i.getCustomerID(),
                i.getCustomerName(), i.getPetName(),
                i.getDiscount(), i.getTotal(), i.getPaidAmount(),
                i.getStatusText()});
        }
    }

    /* Nút "Tính chi phí": xem chi phí của lần khám trước khi lập hóa đơn. */
    private void previewCharges() {
        try {
            int visitId = parseId(txtVisit.getText().trim(), "Mã lần khám");
            InvoiceDAO.Charges c = dao.calculateCharges(visitId);
            if (c == null) {
                message("Không tìm thấy lần khám.");
                return;
            }
            charges = c;
            txtSubtotal.setText(money(c.getTotal()));
            refreshPreview();
            message(breakdown(c));
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    /* Tổng tiền hiển thị = tổng chi phí - giảm giá (chỉ để xem trước). */
    private void refreshPreview() {
        if (selectedMode || charges == null) {
            return;
        }
        try {
            BigDecimal discount = parseDiscount();
            BigDecimal subtotal = charges.getTotal();
            if (discount.compareTo(subtotal) > 0) {
                txtTotal.setText("Giảm giá quá lớn");
            } else {
                txtTotal.setText(money(subtotal.subtract(discount)));
            }
        } catch (IllegalArgumentException ex) {
            txtTotal.setText("");
        }
    }

    private void createInvoice() {
        try {
            int visitId = parseId(txtVisit.getText().trim(), "Mã lần khám");
            BigDecimal discount = parseDiscount();

            InvoiceDAO.Charges c = dao.calculateCharges(visitId);
            if (c == null) {
                message("Không tìm thấy lần khám.");
                return;
            }
            BigDecimal subtotal = c.getTotal();
            if (discount.compareTo(subtotal) > 0) {
                message("Giảm giá không được lớn hơn tổng chi phí ("
                        + money(subtotal) + ").");
                txtDiscount.requestFocus();
                return;
            }

            BigDecimal total = subtotal.subtract(discount);
            int ok = JOptionPane.showConfirmDialog(this,
                    breakdown(c) + "\nGiảm giá: " + money(discount)
                    + "\nTổng tiền: " + money(total)
                    + "\n\nLập hóa đơn cho lần khám " + visitId + "?",
                    "Xác nhận lập hóa đơn", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }

            // DAO tự tính lại total; giá trị truyền vào chỉ mang tính tham khảo
            Invoice invoice = new Invoice(visitId, discount, total);
            int id = dao.insert(invoice);

            message("Đã lập hóa đơn, mã: " + id);
            loadInvoices();
            selectInvoiceById(id);
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void cancelInvoice() {
        int id = currentId();
        if (id <= 0) {
            message("Chọn hóa đơn trong bảng trước khi hủy.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Hủy hóa đơn " + id + "? Thao tác này không hoàn tác được.",
                "Xác nhận hủy hóa đơn", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (dao.cancel(id)) {
                message("Đã hủy hóa đơn.");
            } else {
                message("Không thể hủy: hóa đơn đã thanh toán, đã hủy "
                        + "hoặc đã có khoản thanh toán thành công.");
            }
            refresh();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void payInvoice() {
        int id = currentId();
        if (id <= 0) {
            message("Chọn hóa đơn trong bảng trước khi thanh toán.");
            return;
        }
        openPaymentForInvoice.accept(id);
    }

    private void searchInvoices() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            message("Hãy nhập mã cần tìm.");
            txtSearch.requestFocus();
            return;
        }
        boolean byInvoice = cboSearch.getSelectedIndex() == 0;
        try {
            int id = parseId(text, byInvoice ? "Mã hóa đơn" : "Mã khách hàng");
            List<Invoice> list;
            if (byInvoice) {
                Invoice inv = dao.findById(id);
                list = inv == null ? List.of() : List.of(inv);
            } else {
                list = dao.findByCustomerId(id);
            }
            showInvoices(list);
            if (list.isEmpty()) {
                message("Không tìm thấy hóa đơn.");
            } else if (list.size() == 1) {
                tblData.setRowSelectionInterval(0, 0);
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Chọn dòng / chế độ form
    // ------------------------------------------------------------------
    private void selectRow() {
        int selected = tblData.getSelectedRow();
        if (selected == -1) {
            return;
        }
        Invoice inv = rows.get(tblData.convertRowIndexToModel(selected));

        selectedMode = true;   // đặt trước để các DocumentListener không xóa dữ liệu
        charges = null;
        txtID.setText(String.valueOf(inv.getInvoiceID()));
        txtVisit.setText(String.valueOf(inv.getVisitID()));
        txtCustomer.setText(inv.getCustomerID() + " - " + inv.getCustomerName());
        txtPet.setText(inv.getPetName());
        txtSubtotal.setText(money(inv.getSubtotal()));
        txtDiscount.setText(inv.getDiscount()
                .setScale(0, RoundingMode.HALF_UP).toPlainString());
        txtTotal.setText(money(inv.getTotal()));
        txtStatus.setText(inv.getStatusText());
        txtPaid.setText(money(inv.getPaidAmount()));
        txtRemaining.setText(money(inv.getRemaining()));
        setSelectedMode(true, inv);
    }

    /* Đang chọn hóa đơn: khóa phần nhập liệu, chỉ cho hủy / thanh toán. */
    private void setSelectedMode(boolean selected, Invoice inv) {
        selectedMode = selected;

        txtVisit.setEditable(!selected);
        txtVisit.setBackground(selected ? READONLY : Color.WHITE);
        txtDiscount.setEditable(!selected);
        txtDiscount.setBackground(selected ? READONLY : Color.WHITE);

        btnCalc.setEnabled(!selected);
        btnCreate.setEnabled(!selected);
        btnCancel.setEnabled(selected && inv != null && inv.isUnpaid()
                && inv.getPaidAmount().signum() == 0);
        btnPay.setEnabled(selected && inv != null && inv.isUnpaid());
    }

    private void clearFields() {
        selectedMode = false;
        tblData.clearSelection();
        charges = null;
        for (JTextField f : new JTextField[]{txtID, txtVisit, txtCustomer, txtPet,
            txtSubtotal, txtDiscount, txtTotal, txtStatus, txtPaid, txtRemaining}) {
            f.setText("");
        }
        setSelectedMode(false, null);
        txtVisit.requestFocus();
    }

    private void selectInvoiceById(int invoiceId) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getInvoiceID() == invoiceId) {
                int viewRow = tblData.convertRowIndexToView(i);
                if (viewRow >= 0) {
                    tblData.setRowSelectionInterval(viewRow, viewRow);
                    tblData.scrollRectToVisible(tblData.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private int currentId() {
        String s = txtID.getText().trim();
        return s.isEmpty() ? -1 : Integer.parseInt(s);
    }

    // ------------------------------------------------------------------
    // Tiện ích
    // ------------------------------------------------------------------
    private int parseId(String text, String label) {
        try {
            int id = Integer.parseInt(text);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // rơi xuống ném IllegalArgumentException bên dưới
        }
        throw new IllegalArgumentException(label + " không hợp lệ (phải là số nguyên dương).");
    }

    /* Giảm giá: để trống = 0; chỉ nhận chữ số (đồng). */
    private BigDecimal parseDiscount() {
        String s = txtDiscount.getText().trim();
        if (s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (!MONEY_REGEX.matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Giảm giá chỉ gồm chữ số (đơn vị đồng), không nhập dấu phân cách.");
        }
        return new BigDecimal(s);
    }

    private static String money(BigDecimal v) {
        return v == null ? "" : String.format("%,.0f đ", v);
    }

    private String breakdown(InvoiceDAO.Charges c) {
        return "Dịch vụ: " + money(c.getServiceCost())
                + "\nVaccine: " + money(c.getVaccineCost())
                + "\nThuốc: " + money(c.getMedicineCost())
                + "\nTổng chi phí: " + money(c.getTotal());
    }

    private void message(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(SQLException ex) {
        String message = ex.getMessage();
        if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
            message = "Lần khám này đã có hóa đơn.";
        }
        JOptionPane.showMessageDialog(
                this, message, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Làm đẹp giao diện (cùng phong cách CustomerPanel)
    // ------------------------------------------------------------------
    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        pnlTop.setOpaque(false);
        pnlBottom.setOpaque(false);
        pnlActions.setOpaque(false);
        pnlSearch.setOpaque(false);

        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin hóa đơn ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblID, lblVisit, lblCustomer, lblPet,
            lblSubtotal, lblDiscount, lblTotal, lblStatus, lblPaid, lblRemaining, lblSearch}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtID, txtVisit, txtCustomer, txtPet,
            txtSubtotal, txtDiscount, txtTotal, txtStatus, txtPaid, txtRemaining, txtSearch}) {
            styleField(f);
        }
        for (JTextField f : new JTextField[]{txtID, txtCustomer, txtPet,
            txtSubtotal, txtTotal, txtStatus, txtPaid, txtRemaining}) {
            f.setBackground(READONLY);
        }
        txtTotal.setFont(new Font(FONT, Font.BOLD, 15));
        txtStatus.setFont(new Font(FONT, Font.BOLD, 14));

        cboSearch.setFont(new Font(FONT, Font.PLAIN, 14));
        cboSearch.setBackground(Color.WHITE);

        styleButton(btnCalc, new Color(26, 148, 168));
        styleButton(btnCreate, new Color(39, 163, 98));
        styleButton(btnCancel, new Color(205, 70, 70));
        styleButton(btnPay, PRIMARY);
        styleButton(btnClear, new Color(110, 120, 140));
        styleButton(btnSearch, PRIMARY);
        styleButton(btnAll, new Color(26, 148, 168));

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
        b.setUI(new BasicButtonUI());
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

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề, định dạng tiền, tô màu trạng thái. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = value instanceof BigDecimal ? money((BigDecimal) value) : value;
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setFont(t.getFont());
            setHorizontalAlignment(value instanceof Integer ? CENTER
                    : value instanceof BigDecimal ? RIGHT : LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));

            // Cột cuối: trạng thái
            if (t.convertColumnIndexToModel(col) == 8 && value != null) {
                setFont(t.getFont().deriveFont(Font.BOLD));
                setForeground(statusColor(value.toString()));
            }
            return this;
        }
    }

    private static Color statusColor(String statusText) {
        if (Invoice.statusToText(Invoice.STATUS_PAID).equals(statusText)) {
            return STATUS_PAID;
        }
        if (Invoice.statusToText(Invoice.STATUS_CANCELLED).equals(statusText)) {
            return STATUS_CANCELLED;
        }
        return STATUS_UNPAID;
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
            setHorizontalAlignment(col <= 2 ? CENTER : LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------
    private void initComponents() {
        pnlTop = new JPanel();
        lblTitle = new JLabel();
        pnlFields = new JPanel();
        lblID = new JLabel();
        txtID = new JTextField();
        lblVisit = new JLabel();
        txtVisit = new JTextField();
        lblCustomer = new JLabel();
        txtCustomer = new JTextField();
        lblPet = new JLabel();
        txtPet = new JTextField();
        lblSubtotal = new JLabel();
        txtSubtotal = new JTextField();
        lblDiscount = new JLabel();
        txtDiscount = new JTextField();
        lblTotal = new JLabel();
        txtTotal = new JTextField();
        lblStatus = new JLabel();
        txtStatus = new JTextField();
        lblPaid = new JLabel();
        txtPaid = new JTextField();
        lblRemaining = new JLabel();
        txtRemaining = new JTextField();
        pnlBottom = new JPanel();
        pnlActions = new JPanel();
        btnCalc = new JButton();
        btnCreate = new JButton();
        btnCancel = new JButton();
        btnPay = new JButton();
        btnClear = new JButton();
        pnlSearch = new JPanel();
        lblSearch = new JLabel();
        cboSearch = new JComboBox<>();
        txtSearch = new JTextField(14);
        btnSearch = new JButton();
        btnAll = new JButton();
        scrollTable = new JScrollPane();
        tblData = new JTable();

        setLayout(new java.awt.BorderLayout(0, 12));
        pnlTop.setLayout(new java.awt.BorderLayout(0, 12));

        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("QUẢN LÝ HÓA ĐƠN");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(5, 4, 16, 12));
        lblID.setText("Mã hóa đơn");
        pnlFields.add(lblID);
        txtID.setEditable(false);
        pnlFields.add(txtID);
        lblVisit.setText("Mã lần khám");
        pnlFields.add(lblVisit);
        pnlFields.add(txtVisit);
        lblCustomer.setText("Khách hàng");
        pnlFields.add(lblCustomer);
        txtCustomer.setEditable(false);
        pnlFields.add(txtCustomer);
        lblPet.setText("Thú cưng");
        pnlFields.add(lblPet);
        txtPet.setEditable(false);
        pnlFields.add(txtPet);
        lblSubtotal.setText("Tổng chi phí");
        pnlFields.add(lblSubtotal);
        txtSubtotal.setEditable(false);
        pnlFields.add(txtSubtotal);
        lblDiscount.setText("Giảm giá (đ)");
        pnlFields.add(lblDiscount);
        pnlFields.add(txtDiscount);
        lblTotal.setText("Tổng tiền");
        pnlFields.add(lblTotal);
        txtTotal.setEditable(false);
        pnlFields.add(txtTotal);
        lblStatus.setText("Trạng thái");
        pnlFields.add(lblStatus);
        txtStatus.setEditable(false);
        pnlFields.add(txtStatus);
        lblPaid.setText("Đã thanh toán");
        pnlFields.add(lblPaid);
        txtPaid.setEditable(false);
        pnlFields.add(txtPaid);
        lblRemaining.setText("Còn phải trả");
        pnlFields.add(lblRemaining);
        txtRemaining.setEditable(false);
        pnlFields.add(txtRemaining);
        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.GridLayout(2, 1, 0, 8));

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnCalc.setText("Tính chi phí");
        pnlActions.add(btnCalc);
        btnCreate.setText("Lập hóa đơn");
        pnlActions.add(btnCreate);
        btnCancel.setText("Hủy hóa đơn");
        pnlActions.add(btnCancel);
        btnPay.setText("Thanh toán");
        pnlActions.add(btnPay);
        btnClear.setText("Nhập mới");
        pnlActions.add(btnClear);
        pnlBottom.add(pnlActions);

        pnlSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        lblSearch.setText("Tìm theo:");
        pnlSearch.add(lblSearch);
        cboSearch.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Mã hóa đơn", "Mã khách hàng"}));
        pnlSearch.add(cboSearch);
        pnlSearch.add(txtSearch);
        btnSearch.setText("Tìm");
        pnlSearch.add(btnSearch);
        btnAll.setText("Xem tất cả");
        pnlSearch.add(btnAll);
        pnlBottom.add(pnlSearch);

        pnlTop.add(pnlBottom, java.awt.BorderLayout.SOUTH);
        add(pnlTop, java.awt.BorderLayout.NORTH);

        scrollTable.setViewportView(tblData);
        add(scrollTable, java.awt.BorderLayout.CENTER);
    }

    private JButton btnAll;
    private JButton btnCalc;
    private JButton btnCancel;
    private JButton btnClear;
    private JButton btnCreate;
    private JButton btnPay;
    private JButton btnSearch;
    private JComboBox<String> cboSearch;
    private JLabel lblCustomer;
    private JLabel lblDiscount;
    private JLabel lblID;
    private JLabel lblPaid;
    private JLabel lblPet;
    private JLabel lblRemaining;
    private JLabel lblSearch;
    private JLabel lblStatus;
    private JLabel lblSubtotal;
    private JLabel lblTitle;
    private JLabel lblTotal;
    private JLabel lblVisit;
    private JPanel pnlActions;
    private JPanel pnlBottom;
    private JPanel pnlFields;
    private JPanel pnlSearch;
    private JPanel pnlTop;
    private JScrollPane scrollTable;
    private JTable tblData;
    private JTextField txtCustomer;
    private JTextField txtDiscount;
    private JTextField txtID;
    private JTextField txtPaid;
    private JTextField txtPet;
    private JTextField txtRemaining;
    private JTextField txtSearch;
    private JTextField txtStatus;
    private JTextField txtSubtotal;
    private JTextField txtTotal;
    private JTextField txtVisit;
}