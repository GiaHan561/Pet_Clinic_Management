package com.mycompany.petclinic.ui.medical;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class UiStyle {

    public static final String FONT = "Segoe UI";
    public static final Color BG = new Color(244, 247, 252);
    public static final Color PRIMARY = new Color(46, 98, 196);
    public static final Color PRIMARY_DARK = new Color(28, 52, 102);
    public static final Color TEXT = new Color(33, 42, 60);
    public static final Color LABEL = new Color(88, 98, 120);
    public static final Color LINE = new Color(205, 212, 226);
    public static final Color READONLY = new Color(237, 240, 246);
    public static final Color ZEBRA = new Color(246, 249, 255);
    public static final Color SELECT = new Color(213, 228, 252);
    public static final Color DISABLED = new Color(208, 213, 223);

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UiStyle() {
    }

    public static Border fieldBorder(Color line) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public static void label(JLabel l) {
        l.setFont(new Font(FONT, Font.BOLD, 13));
        l.setForeground(LABEL);
    }

    public static void field(JTextField f) {
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

    public static void lock(JTextField f, boolean locked) {
        f.setEditable(!locked);
        f.setBackground(locked ? READONLY : Color.WHITE);
    }

    public static void combo(JComboBox<?> cb) {
        cb.setFont(new Font(FONT, Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT);
    }

    public static void button(JButton b, Color base) {
        b.setUI(new BasicButtonUI());
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(base);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
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

    public static void card(JPanel panel, String title) {
        panel.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));
    }

    public static void table(JTable t, JScrollPane scroll) {
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

        scroll.setBorder(BorderFactory.createLineBorder(LINE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
    }

    public static void message(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, SQLException ex) {
        String msg = ex.getMessage();
        if (ex.getErrorCode() == 207) {
            msg = "CSDL chưa có cột cần thiết. Hãy chạy file database/PetClinic_Migration.sql.\n" + msg;
        } else if (ex.getErrorCode() == 547) {
            msg = "Dữ liệu vi phạm ràng buộc của cơ sở dữ liệu.\n" + msg;
        } else if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
            msg = "Dữ liệu bị trùng.\n" + msg;
        }
        JOptionPane.showMessageDialog(parent, msg, "Lỗi SQL Server", JOptionPane.ERROR_MESSAGE);
    }

    public static int positiveInt(String text, String name) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v <= 0) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " phải là số nguyên dương.");
        }
    }

    public static Integer optionalInt(String text, String name, int min, int max) {
        String s = text.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            int v = Integer.parseInt(s);
            if (v < min || v > max) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " phải là số nguyên từ " + min + " đến " + max + ".");
        }
    }

    public static String optional(String s) {
        return s.trim().isEmpty() ? null : s.trim();
    }

    public static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Object shown = value;
            if (value instanceof LocalDateTime) {
                shown = ((LocalDateTime) value).format(TIME_FMT);
            } else if (value instanceof LocalDate) {
                shown = ((LocalDate) value).format(DATE_FMT);
            }
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(value instanceof Number ? CENTER : LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {

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