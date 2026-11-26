package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalRecordDAO;
import com.mycompany.petclinic.model.MedicalRecord;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MedicalRecordPanel extends JPanel {

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
    private static final Color HOVER = new Color(230, 240, 255);
    private static final Color DISABLED = new Color(208, 213, 223);

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MedicalRecordDAO dao = new MedicalRecordDAO();
    private DefaultTableModel model;
    private List<MedicalRecord> currentRecords = new ArrayList<>();
    private int hoveredRow = -1;

    private JPanel pnlTop, pnlFields, pnlActions;
    private JLabel lblTitle, lblMedicalRecordID, lblPetID, lblCreatedDate,
            lblBloodType, lblAllergy, lblNote;
    private JTextField txtMedicalRecordID, txtPetID, txtCreatedDate,
            txtBloodType, txtAllergy, txtNote;
    private JButton btnAdd, btnSearch, btnClear;
    private JScrollPane scrollTable;
    private JTable tblData;
        private void openDetail(MedicalRecord record) {
            new MedicalRecordDetailFrame(record, this).setVisible(true);
        }
    public MedicalRecordPanel() {
        initComponents();
        initTable();
        applyStyle();
        bindEvents();
        loadRecords();
    }

    private void initComponents() {
        pnlTop = new JPanel(new BorderLayout(0, 12));

        lblTitle = new JLabel("QUẢN LÝ HỒ SƠ KHÁM BỆNH", SwingConstants.CENTER);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        lblMedicalRecordID = new JLabel("Mã phiếu khám");
        lblPetID = new JLabel("Mã thú cưng (*)");
        lblCreatedDate = new JLabel("Ngày tạo phiếu khám");
        lblBloodType = new JLabel("Nhóm máu");
        lblAllergy = new JLabel("Ghi chú dị ứng");
        lblNote = new JLabel("Ghi chú");

        txtMedicalRecordID = new JTextField();
        txtMedicalRecordID.setEditable(false);
        txtPetID = new JTextField();
        txtCreatedDate = new JTextField();
        txtCreatedDate.setEditable(false);
        txtCreatedDate.setToolTipText("Tự động ghi nhận khi đăng ký");
        txtBloodType = new JTextField();
        txtAllergy = new JTextField();
        txtNote = new JTextField();

        pnlFields = new JPanel(new GridLayout(3, 4, 16, 12));
        pnlFields.add(lblMedicalRecordID); pnlFields.add(txtMedicalRecordID);
        pnlFields.add(lblPetID);           pnlFields.add(txtPetID);
        pnlFields.add(lblCreatedDate);     pnlFields.add(txtCreatedDate);
        pnlFields.add(lblBloodType);       pnlFields.add(txtBloodType);
        pnlFields.add(lblAllergy);         pnlFields.add(txtAllergy);
        pnlFields.add(lblNote);            pnlFields.add(txtNote);
        pnlTop.add(pnlFields, BorderLayout.CENTER);

        btnAdd = new JButton("Đăng ký");
        btnSearch = new JButton("Tìm theo mã thú cưng");
        btnClear = new JButton("Xóa form");
        pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlActions.add(btnAdd);
        pnlActions.add(btnSearch);
        pnlActions.add(btnClear);
        pnlTop.add(pnlActions, BorderLayout.SOUTH);

        tblData = new JTable() {
            @Override
            public String getToolTipText(MouseEvent e) {
                int viewRow = rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return null;
                }
                int r = convertRowIndexToModel(viewRow);
                return r < currentRecords.size() ? buildDetail(currentRecords.get(r)) : null;
            }
        };
        scrollTable = new JScrollPane(tblData);

        setLayout(new BorderLayout(0, 12));
        add(pnlTop, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void initTable() {
        model = new DefaultTableModel(new String[]{
            "Mã phiếu", "Mã thú cưng", "Ngày tạo",
            "Nhóm máu", "Ghi chú dị ứng", "Ghi chú"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 1) {
                    return Integer.class;
                }
                return c == 2 ? LocalDate.class : String.class;
            }
        };
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {90, 110, 120, 100, 220, 300};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        ToolTipManager.sharedInstance().registerComponent(tblData);
        ToolTipManager.sharedInstance().setInitialDelay(250);
    }

    private void bindEvents() {
        btnAdd.addActionListener(e -> addRecord());
        btnSearch.addActionListener(e -> searchByPet());
        btnClear.addActionListener(e -> clearFields());

        tblData.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2) {
                    return;
                }
                int viewRow = tblData.rowAtPoint(e.getPoint());
                if (viewRow >= 0) {
                    openDetail(currentRecords.get(tblData.convertRowIndexToModel(viewRow)));
                }
            }
        });
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(tblData.rowAtPoint(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHover(-1);
            }

            private void updateHover(int row) {
                if (hoveredRow != row) {
                    hoveredRow = row;
                    tblData.repaint();
                }
            }
        };
        tblData.addMouseMotionListener(hover);
        tblData.addMouseListener(hover);
    }

    private void loadRecords() {
        try {
            showRecords(dao.findAll());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showRecords(List<MedicalRecord> records) {
        currentRecords = records;
        model.setRowCount(0);
        for (MedicalRecord r : records) {
            model.addRow(new Object[]{
                r.getMedicalRecordID(), r.getPetID(), r.getCreatedDate(),
                r.getBloodType(), r.getAllergy(), r.getNote()
            });
        }
    }

    private void addRecord() {
        if (!txtMedicalRecordID.getText().isBlank()) {
            message("Đang xem một hồ sơ có sẵn. Bấm \"Xóa form\" để nhập hồ sơ mới.");
            return;
        }
        try {
            int petId = parsePetId();
            String bloodType = optional(txtBloodType.getText());
            String allergy = optional(txtAllergy.getText());
            String note = optional(txtNote.getText());

            if ((bloodType != null && bloodType.length() > 10)
                    || (allergy != null && allergy.length() > 500)
                    || (note != null && note.length() > 1000)) {
                message("Dữ liệu quá dài (nhóm máu ≤ 10, dị ứng ≤ 500, ghi chú ≤ 1000 ký tự).");
                return;
            }
            if (!dao.petExists(petId)) {
                message("Mã thú cưng không tồn tại.");
                txtPetID.requestFocus();
                return;
            }
            if (dao.hasRecord(petId)) {
                message("Thú cưng này đã có hồ sơ khám bệnh. Hãy dùng \"Tìm theo mã thú cưng\".");
                return;
            }
            MedicalRecord record = new MedicalRecord(
                    petId, LocalDate.now(), note, allergy, bloodType);
            int id = dao.insert(record);
            if (id > 0) {
                record.setMedicalRecordID(id);
                message("Đã đăng ký hồ sơ, mã phiếu: " + id);
                clearFields();
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void searchByPet() {
        try {
            int petId = parsePetId();
            List<MedicalRecord> list = dao.findByPetId(petId);
            showRecords(list);
            if (list.isEmpty()) {
                message("Thú cưng này chưa có hồ sơ khám bệnh.");
            }
        } catch (SQLException ex) {
            showError(ex);
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage());
        }
    }

    private void selectRow() {
        int view = tblData.getSelectedRow();
        if (view < 0) {
            return;
        }
        MedicalRecord r = currentRecords.get(tblData.convertRowIndexToModel(view));
        txtMedicalRecordID.setText(String.valueOf(r.getMedicalRecordID()));
        txtPetID.setText(String.valueOf(r.getPetID()));
        txtCreatedDate.setText(r.getCreatedDate() == null ? "" : r.getCreatedDate().format(DATE_FMT));
        txtBloodType.setText(nvl(r.getBloodType()));
        txtAllergy.setText(nvl(r.getAllergy()));
        txtNote.setText(nvl(r.getNote()));
    }

    private void clearFields() {
        tblData.clearSelection();
        txtMedicalRecordID.setText("");
        txtPetID.setText("");
        txtCreatedDate.setText("");
        txtBloodType.setText("");
        txtAllergy.setText("");
        txtNote.setText("");
        txtPetID.requestFocus();
        loadRecords();
    }

    private int parsePetId() {
        try {
            int id = Integer.parseInt(txtPetID.getText().trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã thú cưng phải là số nguyên dương.");
        }
    }

    private String optional(String s) {
        return s.trim().isEmpty() ? null : s.trim();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String esc(String s) {
        if (s == null || s.isBlank()) {
            return "<i>(không có)</i>";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String buildDetail(MedicalRecord r) {
        String date = r.getCreatedDate() == null ? "" : r.getCreatedDate().format(DATE_FMT);
        return "<html><body style='width:320px'>"
                + "<b>Phiếu khám #" + r.getMedicalRecordID() + "</b><br>"
                + "Mã thú cưng: " + r.getPetID() + "<br>"
                + "Ngày tạo: " + date + "<br>"
                + "Nhóm máu: " + esc(r.getBloodType()) + "<br>"
                + "Dị ứng: " + esc(r.getAllergy()) + "<br>"
                + "Ghi chú: " + esc(r.getNote())
                + "</body></html>";
    }

    private void message(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(SQLException ex) {
        String msg = ex.getMessage();
        if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
            msg = "Thú cưng này đã có hồ sơ khám bệnh.";
        } else if (ex.getErrorCode() == 547) {
            msg = "Dữ liệu vi phạm ràng buộc (mã thú cưng không hợp lệ).\n" + msg;
        }
        JOptionPane.showMessageDialog(this, msg, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
    }

    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        pnlTop.setOpaque(false);
        pnlActions.setOpaque(false);

        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thông tin hồ sơ khám bệnh ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblMedicalRecordID, lblPetID, lblCreatedDate,
            lblBloodType, lblAllergy, lblNote}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtMedicalRecordID, txtPetID, txtCreatedDate,
            txtBloodType, txtAllergy, txtNote}) {
            styleField(f);
        }
        txtMedicalRecordID.setBackground(READONLY);
        txtCreatedDate.setBackground(READONLY);

        styleButton(btnAdd, new Color(39, 163, 98));
        styleButton(btnSearch, PRIMARY);
        styleButton(btnClear, new Color(110, 120, 140));

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
        b.setUI(new BasicButtonUI());
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
    }

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

    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = value instanceof LocalDate
                    ? ((LocalDate) value).format(DATE_FMT) : value;
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(value instanceof Integer ? CENTER : LEFT);
            setForeground(TEXT);
            if (sel) {
                setBackground(SELECT);
            } else if (row == hoveredRow) {
                setBackground(HOVER);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
            }
            return this;
        }
    }

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
}