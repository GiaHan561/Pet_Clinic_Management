package com.mycompany.petclinic.ui.appointment;

import com.mycompany.petclinic.dao.AppointmentDAO;
import com.mycompany.petclinic.model.Appointment;
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
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Giao diện quản lý lịch hẹn.
 *
 * Cấu trúc đồng bộ với CustomerPanel: BorderLayout -> pnlTop (tiêu đề + lưới
 * nhập liệu + hàng nút) ở phía trên, bảng dữ liệu ở giữa. Phần làm đẹp nằm
 * trong applyStyle().
 */
public class AppointmentManagementPanel extends JPanel {

    // ----- Bảng màu & font (giống CustomerPanel) -----
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

    // ----- Thành phần giao diện -----
    private JPanel pnlTop;
    private JPanel pnlFields;
    private JPanel pnlActions;
    private JScrollPane scrollTable;
    private JLabel lblTitle;

    private JLabel lblID, lblStatus, lblCustomerID, lblPetID, lblDoctorID,
            lblDate, lblTime, lblReason, lblNote;

    private JTextField txtID, txtStatus, txtCustomerID, txtPetID, txtDoctorID,
            txtDate, txtTime, txtReason, txtNote;

    private JButton btnAdd, btnUpdate, btnCancel, btnClear;
    private JLabel lblSearch;
    private JPanel pnlSearch;
    private JTextField txtSearch;
    private JButton btnSearch;

    private JTable tblData;
    private DefaultTableModel model;

    public AppointmentManagementPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false);

        loadAppointments();
    }

    // ------------------------------------------------------------------
    // Xử lý nghiệp vụ
    // ------------------------------------------------------------------

    // Tìm kiếm theo mã lịch hẹn
    private void searchAppointments() {
        String keyword = txtSearch.getText().trim();

        // Để trống -> hiện lại tất cả lịch hẹn
        if (keyword.isEmpty()) {
            loadAppointments();
            return;
        }

        try {
            int appointmentID = Integer.parseInt(keyword);

            AppointmentDAO dao = new AppointmentDAO();
            Appointment a = dao.findById(appointmentID);

            model.setRowCount(0);

            if (a == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy lịch hẹn có mã " + appointmentID + "!");
                return;
            }

            model.addRow(new Object[]{
                a.getAppointmentID(),
                a.getCustomerID(),
                a.getPetID(),
                a.getDoctorID(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getReason(),
                a.getStatus(),
                a.getNote()
            });

            clearFields();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Mã lịch hẹn phải là số!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tìm kiếm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hủy lịch hẹn
    private void cancelAppointment() {

        try {
            // Kiểm tra đã chọn lịch hẹn
            if (txtID.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn lịch hẹn cần hủy!");
                return;
            }

            int appointmentID =
                    Integer.parseInt(txtID.getText().trim());

            // Xác nhận hủy
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn hủy lịch hẹn này?",
                    "Xác nhận hủy lịch",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Nhập lý do hủy
            String cancelReason = JOptionPane.showInputDialog(
                    this,
                    "Nhập lý do hủy lịch:"
            );

            if (cancelReason == null) {
                return;
            }

            cancelReason = cancelReason.trim();

            if (cancelReason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập lý do hủy!");
                return;
            }

            // Gọi DAO
            AppointmentDAO dao = new AppointmentDAO();

            boolean cancelled =
                    dao.cancel(appointmentID, cancelReason);

            if (cancelled) {
                JOptionPane.showMessageDialog(this,
                        "Hủy lịch hẹn thành công!");

                loadAppointments();
                clearFields();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể hủy lịch hẹn này!\n"
                        + "Lịch có thể đã hoàn thành, đã hủy "
                        + "hoặc không tồn tại.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hủy lịch: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Cập nhật lịch hẹn
    private void updateAppointment() {

        try {
            // Kiểm tra đã chọn lịch hẹn
            if (txtID.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn lịch hẹn cần cập nhật!");
                return;
            }

            int appointmentID = Integer.parseInt(txtID.getText().trim());
            int customerID = Integer.parseInt(txtCustomerID.getText().trim());
            int petID = Integer.parseInt(txtPetID.getText().trim());
            int doctorID = Integer.parseInt(txtDoctorID.getText().trim());

            LocalDate date = LocalDate.parse(txtDate.getText().trim());
            LocalTime time = LocalTime.parse(txtTime.getText().trim());

            String reason = txtReason.getText().trim();
            String note = txtNote.getText().trim();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập lý do khám!");
                return;
            }

            Appointment appointment = new Appointment(
                    appointmentID,
                    customerID,
                    petID,
                    doctorID,
                    date,
                    time,
                    reason,
                    note,
                    txtStatus.getText().trim()
            );

            AppointmentDAO dao = new AppointmentDAO();

            boolean updated = dao.update(appointment);

            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật lịch hẹn thành công!");

                loadAppointments();
                clearFields();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể cập nhật lịch hẹn. "
                        + "Lịch có thể đã hoàn thành hoặc bị hủy.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Các mã ID phải là số!");

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Ngày nhập yyyy-MM-dd, giờ nhập HH:mm!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi cập nhật: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Thêm lịch hẹn
    private void addAppointment() {

        try {
            // Lấy dữ liệu từ form
            int customerID = Integer.parseInt(txtCustomerID.getText().trim());
            int petID = Integer.parseInt(txtPetID.getText().trim());
            int doctorID = Integer.parseInt(txtDoctorID.getText().trim());

            LocalDate date = LocalDate.parse(txtDate.getText().trim());
            LocalTime time = LocalTime.parse(txtTime.getText().trim());

            String reason = txtReason.getText().trim();
            String note = txtNote.getText().trim();

            // Kiểm tra dữ liệu bắt buộc
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập lý do khám!");
                return;
            }

            // Tạo đối tượng Appointment
            Appointment appointment = new Appointment(
                    customerID,
                    petID,
                    doctorID,
                    date,
                    time,
                    reason,
                    note
            );

            // Gọi DAO để lưu
            AppointmentDAO dao = new AppointmentDAO();
            int newID = dao.insert(appointment);

            if (newID > 0) {
                JOptionPane.showMessageDialog(this,
                        "Thêm lịch hẹn thành công!\nMã lịch hẹn: " + newID);

                loadAppointments();
                clearFields();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Mã khách hàng, thú cưng và bác sĩ phải là số!");

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Ngày nhập theo yyyy-MM-dd và giờ theo HH:mm!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể thêm lịch hẹn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Tải danh sách lịch hẹn từ database
    private void loadAppointments() {

        try {
            AppointmentDAO dao = new AppointmentDAO();

            // Xóa dữ liệu cũ trên bảng
            model.setRowCount(0);

            // Lấy danh sách lịch hẹn
            for (Appointment a : dao.findAll()) {

                model.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getCustomerID(),
                    a.getPetID(),
                    a.getDoctorID(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getReason(),
                    a.getStatus(),
                    a.getNote()
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách lịch hẹn!\n"
                    + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------
    private void initComponents() {
        setLayout(new BorderLayout(0, 12));

        pnlTop = new JPanel(new BorderLayout(0, 12));

        // Tiêu đề
        lblTitle = new JLabel("QUẢN LÝ LỊCH HẸN", SwingConstants.CENTER);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        // Lưới nhập liệu: 4 cột (nhãn, ô, nhãn, ô)
        pnlFields = new JPanel(new GridLayout(5, 4, 16, 12));

        lblID = new JLabel("Mã lịch hẹn");
        lblStatus = new JLabel("Trạng thái");
        lblCustomerID = new JLabel("Mã KH (*)");
        lblPetID = new JLabel("Mã thú cưng (*)");
        lblDoctorID = new JLabel("Mã bác sĩ (*)");
        lblDate = new JLabel("Ngày hẹn (yyyy-MM-dd) (*)");
        lblTime = new JLabel("Giờ hẹn (HH:mm) (*)");
        lblReason = new JLabel("Lý do khám (*)");
        lblNote = new JLabel("Ghi chú");

        txtID = new JTextField();
        txtStatus = new JTextField();
        txtCustomerID = new JTextField();
        txtPetID = new JTextField();
        txtDoctorID = new JTextField();
        txtDate = new JTextField();
        txtTime = new JTextField();
        txtReason = new JTextField();
        txtNote = new JTextField();

        txtID.setEditable(false);
        txtStatus.setEditable(false);

        pnlFields.add(lblID);
        pnlFields.add(txtID);
        pnlFields.add(lblStatus);
        pnlFields.add(txtStatus);

        pnlFields.add(lblCustomerID);
        pnlFields.add(txtCustomerID);
        pnlFields.add(lblPetID);
        pnlFields.add(txtPetID);

        pnlFields.add(lblDoctorID);
        pnlFields.add(txtDoctorID);
        pnlFields.add(lblDate);
        pnlFields.add(txtDate);

        pnlFields.add(lblTime);
        pnlFields.add(txtTime);
        pnlFields.add(lblReason);
        pnlFields.add(txtReason);

        pnlFields.add(lblNote);
        pnlFields.add(txtNote);
        pnlFields.add(filler());
        pnlFields.add(filler());

        pnlTop.add(pnlFields, BorderLayout.CENTER);

        // Hàng nút (bên trái) + ô tìm kiếm (bên phải)
        pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnCancel = new JButton("Hủy lịch");
        btnClear = new JButton("Xóa form");

        pnlActions.add(btnAdd);
        pnlActions.add(btnUpdate);
        pnlActions.add(btnCancel);
        pnlActions.add(btnClear);

        // Tìm kiếm theo mã lịch hẹn: nhãn + khung nhập + nút
        lblSearch = new JLabel("Tìm kiếm theo mã lịch hẹn:");
        txtSearch = new JTextField(10);
        btnSearch = new JButton("Tìm kiếm");

        pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        JPanel pnlBar = new JPanel(new BorderLayout());
        pnlBar.setOpaque(false);
        pnlBar.add(pnlActions, BorderLayout.WEST);
        pnlBar.add(pnlSearch, BorderLayout.EAST);

        pnlTop.add(pnlBar, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        // Bảng
        tblData = new JTable();
        scrollTable = new JScrollPane(tblData);
        add(scrollTable, BorderLayout.CENTER);
    }

    /** Ô trống để lấp chỗ cho hàng cuối của lưới. */
    private JPanel filler() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã lịch hẹn", "Mã KH", "Mã thú cưng", "Mã bác sĩ",
            "Ngày hẹn", "Giờ hẹn", "Lý do khám", "Trạng thái", "Ghi chú"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c <= 3 ? Integer.class : String.class;
            }
        };

        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {100, 80, 100, 100, 110, 90, 220, 110, 200};

        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tblData.getColumnModel().getColumn(0).setMaxWidth(120);
    }

    // ------------------------------------------------------------------
    // Sự kiện
    // ------------------------------------------------------------------
    private void bindEvents() {
        btnAdd.addActionListener(e -> addAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnCancel.addActionListener(e -> cancelAppointment());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchAppointments());
        txtSearch.addActionListener(e -> searchAppointments());

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    // ------------------------------------------------------------------
    // Làm đẹp giao diện (giống CustomerPanel)
    // ------------------------------------------------------------------
    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        pnlTop.setOpaque(false);
        pnlActions.setOpaque(false);
        pnlSearch.setOpaque(false);
        lblSearch.setFont(new Font(FONT, Font.BOLD, 13));
        lblSearch.setForeground(LABEL);

        // Tiêu đề
        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        // Khung nhập liệu dạng "thẻ" trắng có tiêu đề
        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin lịch hẹn ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblID, lblStatus, lblCustomerID, lblPetID,
            lblDoctorID, lblDate, lblTime, lblReason, lblNote}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtID, txtStatus, txtCustomerID,
            txtPetID, txtDoctorID, txtDate, txtTime, txtReason, txtNote}) {
            styleField(f);
        }
        txtID.setBackground(READONLY);
        txtStatus.setBackground(READONLY);

        // Nút bấm
        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnCancel, new Color(210, 70, 70));
        styleButton(btnClear, new Color(110, 120, 140));
        styleField(txtSearch);
        styleButton(btnSearch, PRIMARY);

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

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(value instanceof Integer ? CENTER : LEFT);
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
            setHorizontalAlignment(col == 0 ? CENTER : LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Xử lý form
    // ------------------------------------------------------------------
    private void selectRow() {
        int selected = tblData.getSelectedRow();

        if (selected == -1) {
            return;
        }

        int r = tblData.convertRowIndexToModel(selected);

        txtID.setText(cell(r, 0));
        txtCustomerID.setText(cell(r, 1));
        txtPetID.setText(cell(r, 2));
        txtDoctorID.setText(cell(r, 3));
        txtDate.setText(cell(r, 4));
        txtTime.setText(cell(r, 5));
        txtReason.setText(cell(r, 6));
        txtStatus.setText(cell(r, 7));
        txtNote.setText(cell(r, 8));

        setSelectedMode(true);
    }

    /** Đang chọn một lịch hẹn: chỉ cho cập nhật / hủy, không cho thêm mới. */
    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        btnCancel.setEnabled(selected);
    }

    private String cell(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private void clearFields() {
        tblData.clearSelection();
        txtID.setText("");
        txtStatus.setText("");
        txtCustomerID.setText("");
        txtPetID.setText("");
        txtDoctorID.setText("");
        txtDate.setText("");
        txtTime.setText("");
        txtReason.setText("");
        txtNote.setText("");
        setSelectedMode(false);
        txtCustomerID.requestFocus();
    }

    private void message(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
