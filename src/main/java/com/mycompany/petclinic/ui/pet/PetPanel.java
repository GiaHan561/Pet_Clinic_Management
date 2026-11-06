package com.mycompany.petclinic.ui.pet;

import com.mycompany.petclinic.dao.CustomerDAO;
import com.mycompany.petclinic.dao.PetDAO;
import com.mycompany.petclinic.dao.PetTypeDAO;
import com.mycompany.petclinic.model.Pet;
import com.mycompany.petclinic.model.PetType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện quản lý thú cưng, loài và thống kê (NetBeans GUI Builder).
 *
 * Cấu trúc form: BorderLayout -> pnlTop (tiêu đề + lưới nhập liệu + hàng nút)
 * ở phía trên, bảng dữ liệu ở giữa. Phần làm đẹp (màu, font, viền, bảng)
 * nằm trong applyStyle() nên Form Editor không ghi đè.
 *
 * @author DELL
 */
public class PetPanel extends javax.swing.JPanel {

    // ----- Bảng màu & font (đồng bộ với CustomerPanel) -----
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

    // Giá trị giới tính lưu vào DB - đổi tại đây nếu CSDL dùng giá trị khác.
    private static final String[] GENDERS = {"", "Đực", "cái"};

    private final PetDAO petDAO = new PetDAO();
    private final PetTypeDAO typeDAO = new PetTypeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private DefaultTableModel model;
    private List<Pet> currentPets = new ArrayList<>();      // song song với các dòng của model
    private final Map<Integer, String> typeNames = new HashMap<>();
    private Pet selectedPet = null;

    public PetPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        setSelectedMode(false);
        loadTypes();
        loadPets();
    }

    // ------------------------------------------------------------------
    // Khởi tạo bảng & sự kiện
    // ------------------------------------------------------------------
    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã pet", "Mã KH", "Loài", "Tên", "Giới tính",
            "Ngày sinh", "Cân nặng (kg)", "Ghi chú"
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
                    case 5:
                        return LocalDate.class;
                    case 6:
                        return BigDecimal.class;
                    default:
                        return String.class;
                }
            }
        };
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {70, 70, 110, 150, 90, 110, 110, 260};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        cbGender.setModel(new DefaultComboBoxModel<>(GENDERS));
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addPet());
        btnSearch.addActionListener(e -> searchPets());
        btnUpdate.addActionListener(e -> updatePet());
        btnAll.addActionListener(e -> loadPets());
        btnType.addActionListener(e -> addType());
        btnStats.addActionListener(e -> showStats());
        btnClear.addActionListener(e -> clearFields());
        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        
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
                " Thông tin thú cưng ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblID, lblCustomer, lblName, lblType,
            lblGender, lblBirth, lblWeight, lblNote}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtID, txtCustomer, txtName,
            txtBirth, txtWeight, txtNote}) {
            styleField(f);
        }
        txtID.setBackground(READONLY);
        txtCustomer.setToolTipText("Nhập mã khách hàng để đăng ký thú cưng mới hoặc tìm thú cưng theo chủ");

        for (JComboBox<?> cb : new JComboBox<?>[]{cbType, cbGender}) {
            cb.setFont(new Font(FONT, Font.PLAIN, 14));
            cb.setBackground(Color.WHITE);
            cb.setForeground(TEXT);
        }

        // Nút bấm
        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnSearch, PRIMARY);
        styleButton(btnUpdate, new Color(238, 142, 36));
        styleButton(btnAll, new Color(26, 148, 168));
        styleButton(btnType, new Color(125, 87, 194));
        styleButton(btnStats, new Color(69, 90, 120));
        styleButton(btnClear, new Color(110, 120, 140));

        // Bảng
        styleTable(tblData);
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
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
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

    /** Áp dụng kiểu bảng chung (dùng cho bảng chính và bảng thống kê). */
    private void styleTable(JTable t) {
        t.setFont(new Font(FONT, Font.PLAIN, 14));
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(232, 236, 244));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new CellRenderer());
        t.setDefaultRenderer(Number.class, new CellRenderer());

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
    }

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(value instanceof Number ? CENTER : LEFT);
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
            setHorizontalAlignment(LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Tải dữ liệu
    // ------------------------------------------------------------------
    private void loadTypes() {
        try {
            cbType.removeAllItems();
            typeNames.clear();
            for (PetType t : typeDAO.findAll()) {
                cbType.addItem(t);
                typeNames.put(t.getTypeID(), t.getTypeName());
            }
            cbType.setSelectedIndex(-1);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadPets() {
        try {
            showPets(petDAO.findAll());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showPets(List<Pet> pets) {
        currentPets = pets;
        model.setRowCount(0);
        for (Pet p : pets) {
            model.addRow(new Object[]{
                p.getPetID(), p.getCustomerID(),
                typeNames.getOrDefault(p.getTypeID(), "#" + p.getTypeID()),
                p.getName(), p.getGender(), p.getDateOfBirth(),
                p.getWeight(), p.getNote()
            });
        }
    }

    // ------------------------------------------------------------------
    // Nghiệp vụ
    // ------------------------------------------------------------------
    private void addPet() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty() || name.length() > 100) {
                message("Tên thú cưng phải có từ 1 đến 100 ký tự.");
                txtName.requestFocus();
                return;
            }
            int customerId = parseCustomerId();
            if (!customerDAO.existsById(customerId)) {
                message("Mã khách hàng không tồn tại.");
                txtCustomer.requestFocus();
                return;
            }
            PetType type = (PetType) cbType.getSelectedItem();
            if (type == null) {
                message("Chọn loài hoặc thêm loài mới trước.");
                return;
            }
            Pet p = new Pet(customerId, type.getTypeID(), name,
                    gender(), parseDate(), parseWeight(), optional(txtNote.getText()));
            if (petDAO.insert(p)) {
                message("Đăng ký thú cưng thành công.");
                loadPets();
                clearFields();
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }
    public void openForCustomer(int customerId) {
        clearFields();
        txtCustomer.setText(String.valueOf(customerId));
        txtCustomer.setEditable(false);
        txtCustomer.setBackground(READONLY);
        showPets(List.of());

        try {
            showPets(petDAO.findByCustomerId(customerId));
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void searchPets() {
        try {
            int id = parseCustomerId();
            List<Pet> list = petDAO.findByCustomerId(id);
            showPets(list);
            if (list.isEmpty()) {
                message("Khách hàng chưa có thú cưng.");
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void updatePet() {
        if (selectedPet == null || txtID.getText().isBlank()) {
            message("Chọn một thú cưng trong bảng trước khi cập nhật.");
            return;
        }
        try {
            // Schema hiện tại chỉ cho cập nhật cân nặng và ghi chú.
            selectedPet.setWeight(parseWeight());
            selectedPet.setNote(optional(txtNote.getText()));
            if (petDAO.update(selectedPet)) {
                message("Đã cập nhật cân nặng và ghi chú.");
                loadPets();
                clearFields();
            } else {
                message("Không tìm thấy thú cưng.");
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void addType() {
        JTextField name = new JTextField(24);
        JTextField description = new JTextField(24);
        styleField(name);
        styleField(description);

        JLabel l1 = new JLabel("Tên loài (tối đa 50 ký tự):");
        JLabel l2 = new JLabel("Mô tả (tối đa 255 ký tự):");
        for (JLabel l : new JLabel[]{l1, l2}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(l1);
        form.add(name);
        form.add(l2);
        form.add(description);

        if (JOptionPane.showConfirmDialog(this, form, "Thêm loài mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String n = name.getText().trim();
        String d = description.getText().trim();
        if (n.isEmpty() || n.length() > 50 || d.length() > 255) {
            message("Tên loài: 1–50 ký tự; mô tả: tối đa 255 ký tự.");
            return;
        }
        try {
            if (typeDAO.insert(new PetType(n, d.isEmpty() ? null : d))) {
                message("Đã thêm loài.");
                loadTypes();
                for (int i = 0; i < cbType.getItemCount(); i++) {   // chọn sẵn loài vừa thêm
                    if (cbType.getItemAt(i).getTypeName().equals(n)) {
                        cbType.setSelectedIndex(i);
                        break;
                    }
                }
                loadPets();   // cập nhật tên loài hiển thị trong bảng
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showStats() {
        try {
            Map<String, Integer> counts = petDAO.countByType();
            DefaultTableModel stats = new DefaultTableModel(
                    new String[]{"Tên loài", "Số thú cưng"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int c) {
                    return c == 1 ? Integer.class : String.class;
                }
            };
            for (Map.Entry<String, Integer> item : counts.entrySet()) {
                stats.addRow(new Object[]{item.getKey(), item.getValue()});
            }
            JTable table = new JTable(stats);
            styleTable(table);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(LINE, 1, true));
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setPreferredSize(new Dimension(420, 240));

            JLabel total = new JLabel("Tổng số thú cưng: " + petDAO.countAll());
            total.setFont(new Font(FONT, Font.BOLD, 16));
            total.setForeground(PRIMARY_DARK);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.add(total, BorderLayout.NORTH);
            panel.add(scroll, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, panel, "Thống kê theo loài",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------
    // Chọn dòng / xóa form
    // ------------------------------------------------------------------
    private void selectRow() {
        int view = tblData.getSelectedRow();
        if (view < 0) {
            return;
        }
        int r = tblData.convertRowIndexToModel(view);
        selectedPet = currentPets.get(r);

        txtID.setText(String.valueOf(selectedPet.getPetID()));
        txtCustomer.setText(String.valueOf(selectedPet.getCustomerID()));
        txtName.setText(selectedPet.getName());
        txtBirth.setText(selectedPet.getDateOfBirth() == null
                ? "" : selectedPet.getDateOfBirth().toString());
        txtWeight.setText(selectedPet.getWeight() == null
                ? "" : selectedPet.getWeight().toPlainString());
        txtNote.setText(selectedPet.getNote() == null ? "" : selectedPet.getNote());
        selectGender(selectedPet.getGender());

        cbType.setSelectedIndex(-1);
        for (int i = 0; i < cbType.getItemCount(); i++) {
            if (cbType.getItemAt(i).getTypeID() == selectedPet.getTypeID()) {
                cbType.setSelectedIndex(i);
                break;
            }
        }
        setSelectedMode(true);
    }

    private void selectGender(String g) {
        cbGender.setSelectedIndex(0);
        if (g == null) {
            return;
        }
        for (int i = 0; i < cbGender.getItemCount(); i++) {
            if (cbGender.getItemAt(i).equalsIgnoreCase(g)) {
                cbGender.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Đang chọn một thú cưng: chỉ cho sửa cân nặng và ghi chú (đúng với
     * PetDAO.update). Mã khách hàng vẫn nhập được vì dùng làm khóa tìm kiếm.
     */
    private void setSelectedMode(boolean selected) {
        btnAdd.setEnabled(!selected);
        btnUpdate.setEnabled(selected);
        for (JTextField f : new JTextField[]{txtName, txtBirth}) {
            f.setEditable(!selected);
            f.setBackground(selected ? READONLY : Color.WHITE);
        }
        cbType.setEnabled(!selected);
        cbGender.setEnabled(!selected);
    }

    private void clearFields() {
        tblData.clearSelection();
        selectedPet = null;
        txtID.setText("");
        txtCustomer.setText("");
        txtCustomer.setEditable(true);
        txtCustomer.setBackground(Color.WHITE);
        txtName.setText("");
        txtBirth.setText("");
        txtWeight.setText("");
        txtNote.setText("");
        cbGender.setSelectedIndex(0);
        cbType.setSelectedIndex(-1);
        setSelectedMode(false);
        txtCustomer.setFocusable(true);
        txtCustomer.requestFocus();
    }

    // ------------------------------------------------------------------
    // Chuyển đổi dữ liệu (ném IllegalArgumentException khi không hợp lệ)
    // ------------------------------------------------------------------
    private int parseCustomerId() {
        try {
            int id = Integer.parseInt(txtCustomer.getText().trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã khách hàng phải là số nguyên dương.");
        }
    }

    private LocalDate parseDate() {
        String s = txtBirth.getText().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            LocalDate d = LocalDate.parse(s);
            if (d.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
            }
            return d;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày sinh phải có dạng yyyy-MM-dd (ví dụ 2022-05-20).");
        }
    }

    private BigDecimal parseWeight() {
        String s = txtWeight.getText().trim().replace(',', '.');
        if (s.isEmpty()) {
            return null;
        }
        BigDecimal w;
        try {
            w = new BigDecimal(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Cân nặng phải là số (ví dụ 4.5).");
        }
        if (w.compareTo(BigDecimal.ZERO) <= 0 || w.compareTo(new BigDecimal("999.99")) > 0
                || w.scale() > 2) {
            throw new IllegalArgumentException(
                    "Cân nặng phải từ 0.01 đến 999.99 kg, tối đa 2 số lẻ.");
        }
        return w;
    }

    private String gender() {
        String s = (String) cbGender.getSelectedItem();
        return s == null || s.isEmpty() ? null : s;
    }

    private String optional(String s) {
        return s.trim().isEmpty() ? null : s.trim();
    }

    private void message(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(SQLException ex) {
        String msg = ex.getMessage();
        if (ex.getErrorCode() == 547) {
            msg = "Dữ liệu vi phạm ràng buộc của cơ sở dữ liệu "
                    + "(giới tính, cân nặng, mã khách hàng hoặc loài không hợp lệ).\n" + msg;
        } else if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
            msg = "Dữ liệu bị trùng (ví dụ tên loài đã tồn tại).\n" + msg;
        }
        JOptionPane.showMessageDialog(this, msg, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
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
        lblCustomer = new javax.swing.JLabel();
        txtCustomer = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        cbType = new javax.swing.JComboBox<>();
        lblGender = new javax.swing.JLabel();
        cbGender = new javax.swing.JComboBox<>();
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

        setLayout(new java.awt.BorderLayout(0, 12));

        pnlTop.setLayout(new java.awt.BorderLayout(0, 12));

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("QUẢN LÝ THÚ CƯNG");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(4, 4, 16, 12));

        lblID.setText("Mã thú cưng");
        pnlFields.add(lblID);

        txtID.setEditable(false);
        pnlFields.add(txtID);

        lblCustomer.setText("Mã khách hàng (*)");
        pnlFields.add(lblCustomer);
        pnlFields.add(txtCustomer);

        lblName.setText("Tên thú cưng (*)");
        pnlFields.add(lblName);
        pnlFields.add(txtName);

        lblType.setText("Loài (*)");
        pnlFields.add(lblType);

        pnlFields.add(cbType);

        lblGender.setText("Giới tính");
        pnlFields.add(lblGender);

        pnlFields.add(cbGender);

        lblBirth.setText("Ngày sinh (yyyy-MM-dd)");
        pnlFields.add(lblBirth);
        pnlFields.add(txtBirth);

        lblWeight.setText("Cân nặng (kg)");
        pnlFields.add(lblWeight);
        pnlFields.add(txtWeight);

        lblNote.setText("Ghi chú sức khỏe");
        pnlFields.add(lblNote);
        pnlFields.add(txtNote);

        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));

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
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAll;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnStats;
    private javax.swing.JButton btnType;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cbGender;
    private javax.swing.JComboBox<com.mycompany.petclinic.model.PetType> cbType;
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