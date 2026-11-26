package com.mycompany.petclinic.ui.payment;

import com.mycompany.petclinic.dao.InvoiceDAO;
import com.mycompany.petclinic.dao.PaymentDAO;
import com.mycompany.petclinic.dao.PaymentMethodDAO;
import com.mycompany.petclinic.model.Invoice;
import com.mycompany.petclinic.model.Payment;
import com.mycompany.petclinic.model.Payment_Method;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
 * Giao diện thanh toán: thu tiền cho một hóa đơn (tiền mặt / chuyển khoản)
 * và tra cứu lịch sử thanh toán. Hỗ trợ thanh toán từng phần.
 *
 * Cùng phong cách với DashboardPanel / CustomerPanel.
 */
public class PaymentPanel extends JPanel {

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

    private static final Color STATUS_OK = new Color(30, 130, 70);
    private static final Color STATUS_WAIT = new Color(200, 110, 0);
    private static final Color STATUS_BAD = new Color(190, 60, 60);

    private static final Pattern MONEY_REGEX = Pattern.compile("^\\d{1,13}$");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentMethodDAO methodDAO = new PaymentMethodDAO();

    private DefaultTableModel model;
    // Hóa đơn đang thanh toán (null nếu chưa tải)
    private Invoice currentInvoice;

    public PaymentPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        loadMethods();
        setInvoice(null);
        loadAllPayments();
    }

    // ------------------------------------------------------------------
    // API cho MainFrame / InvoicePanel
    // ------------------------------------------------------------------
    /* Mở màn hình thanh toán cho một hóa đơn (gọi từ nút "Thanh toán" bên hóa đơn). */
    public void openForInvoice(int invoiceId) {
        txtInvoice.setText(String.valueOf(invoiceId));
        loadInvoice();
    }

    /* Nạp lại dữ liệu khi quay lại màn hình này. */
    public void refresh() {
        if (currentInvoice != null) {
            txtInvoice.setText(String.valueOf(currentInvoice.getInvoiceID()));
            loadInvoice();
        } else {
            loadAllPayments();
        }
    }

    // ------------------------------------------------------------------
    // Bảng & sự kiện
    // ------------------------------------------------------------------
    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã TT", "Mã HĐ", "Ngày thanh toán", "Số tiền", "Phương thức", "Trạng thái"
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
                        return Integer.class;
                    case 2:
                        return LocalDateTime.class;
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

        int[] widths = {80, 80, 170, 150, 200, 140};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void bindEvents() {
        btnLoad.addActionListener(e -> loadInvoice());
        txtInvoice.addActionListener(e -> loadInvoice());
        btnPay.addActionListener(e -> pay());
        btnPayAll.addActionListener(e -> {
            if (currentInvoice != null) {
                txtAmount.setText(plain(currentInvoice.getRemaining()));
            }
        });
        btnClear.addActionListener(e -> clearAll());
        btnSearch.addActionListener(e -> searchPayments());
        txtSearch.addActionListener(e -> searchPayments());
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadAllPayments();
        });
    }

    // ------------------------------------------------------------------
    // Nghiệp vụ
    // ------------------------------------------------------------------
    private void loadMethods() {
        try {
            cboMethod.setModel(new DefaultComboBoxModel<>(
                    methodDAO.findAll().toArray(new Payment_Method[0])));
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    /* Tải hóa đơn theo mã trong ô nhập + lịch sử thanh toán của hóa đơn đó. */
    private void loadInvoice() {
        try {
            int id = parseId(txtInvoice.getText().trim(), "Mã hóa đơn");
            Invoice inv = invoiceDAO.findById(id);
            if (inv == null) {
                message("Không tìm thấy hóa đơn.");
                setInvoice(null);
                return;
            }
            setInvoice(inv);
            showPayments(paymentDAO.findByInvoice(id));
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void pay() {
        if (currentInvoice == null) {
            message("Hãy nhập mã hóa đơn và bấm Tải hóa đơn trước.");
            txtInvoice.requestFocus();
            return;
        }
        Payment_Method method = (Payment_Method) cboMethod.getSelectedItem();
        if (method == null) {
            message("Chưa có phương thức thanh toán nào.");
            return;
        }
        try {
            BigDecimal amount = parseAmount();
            BigDecimal remaining = currentInvoice.getRemaining();
            if (amount.compareTo(remaining) > 0) {
                message("Số tiền vượt quá số còn phải trả (" + money(remaining) + ").");
                txtAmount.requestFocus();
                return;
            }

            int ok = JOptionPane.showConfirmDialog(this,
                    "Thu " + money(amount) + " bằng \"" + method.getTypeName()
                    + "\" cho hóa đơn " + currentInvoice.getInvoiceID() + "?",
                    "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }

            Payment payment = new Payment(currentInvoice.getInvoiceID(),
                    method.getPaymentMethodID(), amount);
            int paymentId = paymentDAO.pay(payment);

            loadInvoice();   // nạp lại để thấy trạng thái và số còn nợ mới
            String result = "Thanh toán thành công, mã thanh toán: " + paymentId;
            if (currentInvoice != null) {
                result += currentInvoice.isUnpaid()
                        ? "\nCòn phải trả: " + money(currentInvoice.getRemaining())
                        : "\nHóa đơn đã được thanh toán đủ.";
            }
            message(result);
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
            loadInvoice();   // dữ liệu có thể đã đổi (người khác vừa thanh toán)
        }
    }

    private void loadAllPayments() {
        try {
            showPayments(paymentDAO.findAll());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void searchPayments() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            message("Hãy nhập mã cần tìm.");
            txtSearch.requestFocus();
            return;
        }
        int kind = cboSearch.getSelectedIndex();   // 0: hóa đơn, 1: khách hàng, 2: thanh toán
        String[] labels = {"Mã hóa đơn", "Mã khách hàng", "Mã thanh toán"};
        try {
            int id = parseId(text, labels[kind]);
            List<Payment> list;
            if (kind == 0) {
                list = paymentDAO.findByInvoice(id);
            } else if (kind == 1) {
                list = paymentDAO.findByCustomerId(id);
            } else {
                Payment p = paymentDAO.findById(id);
                list = p == null ? List.of() : List.of(p);
            }
            showPayments(list);
            if (list.isEmpty()) {
                message("Không tìm thấy giao dịch thanh toán.");
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void showPayments(List<Payment> list) {
        model.setRowCount(0);
        for (Payment p : list) {
            model.addRow(new Object[]{
                p.getPaymentID(), p.getInvoiceID(), p.getPaymentDate(),
                p.getAmount(), p.getMethodName(), p.getStatusText()});
        }
    }

    // ------------------------------------------------------------------
    // Trạng thái form
    // ------------------------------------------------------------------
    /* Điền thông tin hóa đơn; chỉ cho thanh toán khi chưa thanh toán và còn nợ. */
    private void setInvoice(Invoice inv) {
        currentInvoice = inv;

        if (inv == null) {
            txtCustomer.setText("");
            txtTotal.setText("");
            txtPaid.setText("");
            txtRemaining.setText("");
            txtStatus.setText("");
            txtAmount.setText("");
        } else {
            txtCustomer.setText(inv.getCustomerID() + " - " + inv.getCustomerName());
            txtTotal.setText(money(inv.getTotal()));
            txtPaid.setText(money(inv.getPaidAmount()));
            txtRemaining.setText(money(inv.getRemaining()));
            txtStatus.setText(inv.getStatusText());
        }

        boolean canPay = inv != null && inv.isUnpaid()
                && inv.getRemaining().signum() > 0;
        txtAmount.setText(canPay ? plain(inv.getRemaining()) : "");
        txtAmount.setEditable(canPay);
        txtAmount.setBackground(canPay ? Color.WHITE : READONLY);
        cboMethod.setEnabled(canPay);
        btnPay.setEnabled(canPay);
        btnPayAll.setEnabled(canPay);
    }

    private void clearAll() {
        txtInvoice.setText("");
        setInvoice(null);
        txtSearch.setText("");
        loadAllPayments();
        txtInvoice.requestFocus();
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

    private BigDecimal parseAmount() {
        String s = txtAmount.getText().trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập số tiền thanh toán.");
        }
        if (!MONEY_REGEX.matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Số tiền chỉ gồm chữ số (đơn vị đồng), không nhập dấu phân cách.");
        }
        BigDecimal amount = new BigDecimal(s);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        return amount;
    }

    private static String money(BigDecimal v) {
        return v == null ? "" : String.format("%,.0f đ", v);
    }

    /* Số tiền dạng chữ số thuần để điền vào ô nhập (không dấu phân cách). */
    private static String plain(BigDecimal v) {
        return v.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private void message(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this, ex.getMessage(), "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
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
                " Thanh toán hóa đơn ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblInvoice, lblCustomer, lblTotal, lblPaid,
            lblRemaining, lblStatus, lblMethod, lblAmount, lblSearch}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtInvoice, txtCustomer, txtTotal, txtPaid,
            txtRemaining, txtStatus, txtAmount, txtSearch}) {
            styleField(f);
        }
        for (JTextField f : new JTextField[]{txtCustomer, txtTotal, txtPaid,
            txtRemaining, txtStatus}) {
            f.setBackground(READONLY);
        }
        txtRemaining.setFont(new Font(FONT, Font.BOLD, 15));
        txtStatus.setFont(new Font(FONT, Font.BOLD, 14));

        for (JComboBox<?> c : new JComboBox<?>[]{cboMethod, cboSearch}) {
            c.setFont(new Font(FONT, Font.PLAIN, 14));
            c.setBackground(Color.WHITE);
        }

        styleButton(btnLoad, PRIMARY);
        styleButton(btnPay, new Color(39, 163, 98));
        styleButton(btnPayAll, new Color(238, 142, 36));
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

        scrollTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(LINE, 1, true),
                        " Lịch sử thanh toán ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font(FONT, Font.BOLD, 13), PRIMARY),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        scrollTable.setOpaque(false);
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

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề, định dạng ngày giờ / tiền, tô màu trạng thái. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = value;
            if (value instanceof LocalDateTime) {
                shown = ((LocalDateTime) value).format(TIME_FMT);
            } else if (value instanceof BigDecimal) {
                shown = money((BigDecimal) value);
            }
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setFont(t.getFont());
            setHorizontalAlignment(value instanceof Integer ? CENTER
                    : value instanceof BigDecimal ? RIGHT : LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));

            // Cột cuối: trạng thái
            if (t.convertColumnIndexToModel(col) == 5 && value != null) {
                setFont(t.getFont().deriveFont(Font.BOLD));
                setForeground(statusColor(value.toString()));
            }
            return this;
        }
    }

    private static Color statusColor(String statusText) {
        if (Payment.statusToText(Payment.STATUS_SUCCESS).equals(statusText)) {
            return STATUS_OK;
        }
        if (Payment.statusToText(Payment.STATUS_PENDING).equals(statusText)) {
            return STATUS_WAIT;
        }
        return STATUS_BAD;
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
            setHorizontalAlignment(col <= 1 ? CENTER : LEFT);
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
        lblInvoice = new JLabel();
        txtInvoice = new JTextField();
        lblCustomer = new JLabel();
        txtCustomer = new JTextField();
        lblTotal = new JLabel();
        txtTotal = new JTextField();
        lblPaid = new JLabel();
        txtPaid = new JTextField();
        lblRemaining = new JLabel();
        txtRemaining = new JTextField();
        lblStatus = new JLabel();
        txtStatus = new JTextField();
        lblMethod = new JLabel();
        cboMethod = new JComboBox<>();
        lblAmount = new JLabel();
        txtAmount = new JTextField();
        pnlBottom = new JPanel();
        pnlActions = new JPanel();
        btnLoad = new JButton();
        btnPay = new JButton();
        btnPayAll = new JButton();
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
        lblTitle.setText("THANH TOÁN");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(4, 4, 16, 12));
        lblInvoice.setText("Mã hóa đơn");
        pnlFields.add(lblInvoice);
        pnlFields.add(txtInvoice);
        lblCustomer.setText("Khách hàng");
        pnlFields.add(lblCustomer);
        txtCustomer.setEditable(false);
        pnlFields.add(txtCustomer);
        lblTotal.setText("Tổng tiền");
        pnlFields.add(lblTotal);
        txtTotal.setEditable(false);
        pnlFields.add(txtTotal);
        lblPaid.setText("Đã thanh toán");
        pnlFields.add(lblPaid);
        txtPaid.setEditable(false);
        pnlFields.add(txtPaid);
        lblRemaining.setText("Còn phải trả");
        pnlFields.add(lblRemaining);
        txtRemaining.setEditable(false);
        pnlFields.add(txtRemaining);
        lblStatus.setText("Trạng thái");
        pnlFields.add(lblStatus);
        txtStatus.setEditable(false);
        pnlFields.add(txtStatus);
        lblMethod.setText("Phương thức");
        pnlFields.add(lblMethod);
        pnlFields.add(cboMethod);
        lblAmount.setText("Số tiền thanh toán (đ)");
        pnlFields.add(lblAmount);
        pnlFields.add(txtAmount);
        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.GridLayout(2, 1, 0, 8));

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnLoad.setText("Tải hóa đơn");
        pnlActions.add(btnLoad);
        btnPay.setText("Thanh toán");
        pnlActions.add(btnPay);
        btnPayAll.setText("Trả hết");
        pnlActions.add(btnPayAll);
        btnClear.setText("Nhập mới");
        pnlActions.add(btnClear);
        pnlBottom.add(pnlActions);

        pnlSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        lblSearch.setText("Tra cứu lịch sử theo:");
        pnlSearch.add(lblSearch);
        cboSearch.setModel(new DefaultComboBoxModel<>(
                new String[]{"Mã hóa đơn", "Mã khách hàng", "Mã thanh toán"}));
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
    private JButton btnClear;
    private JButton btnLoad;
    private JButton btnPay;
    private JButton btnPayAll;
    private JButton btnSearch;
    private JComboBox<Payment_Method> cboMethod;
    private JComboBox<String> cboSearch;
    private JLabel lblAmount;
    private JLabel lblCustomer;
    private JLabel lblInvoice;
    private JLabel lblMethod;
    private JLabel lblPaid;
    private JLabel lblRemaining;
    private JLabel lblSearch;
    private JLabel lblStatus;
    private JLabel lblTitle;
    private JLabel lblTotal;
    private JPanel pnlActions;
    private JPanel pnlBottom;
    private JPanel pnlFields;
    private JPanel pnlSearch;
    private JPanel pnlTop;
    private JScrollPane scrollTable;
    private JTable tblData;
    private JTextField txtAmount;
    private JTextField txtCustomer;
    private JTextField txtInvoice;
    private JTextField txtPaid;
    private JTextField txtRemaining;
    private JTextField txtSearch;
    private JTextField txtStatus;
    private JTextField txtTotal;
}
