package com.mycompany.petclinic.ui.medical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PickerDialog extends JDialog {

    public interface Searcher {
        List<Object[]> search(String keyword) throws SQLException;
    }

    private final Searcher searcher;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch;
    private List<Object[]> rows = new ArrayList<>();
    private Object[] selected;

    private PickerDialog(Window owner, String title, String[] columns,
                         Searcher searcher, String keyword) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.searcher = searcher;

        model = new ReadOnlyModel(columns);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        UiStyle.table(table, scroll);

        txtSearch = new JTextField(keyword);
        UiStyle.field(txtSearch);
        JLabel lblSearch = new JLabel("Tìm theo mã hoặc tên:");
        UiStyle.label(lblSearch);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.add(lblSearch, BorderLayout.WEST);
        searchRow.add(txtSearch, BorderLayout.CENTER);

        JButton btnChoose = new JButton("Chọn");
        JButton btnCancel = new JButton("Hủy");
        UiStyle.button(btnChoose, new Color(39, 163, 98));
        UiStyle.button(btnCancel, new Color(110, 120, 140));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(btnChoose);
        buttons.add(btnCancel);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiStyle.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.add(searchRow, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        setSize(780, 460);
        setLocationRelativeTo(owner);

        btnChoose.addActionListener(e -> choose());
        btnCancel.addActionListener(e -> dispose());
        txtSearch.addActionListener(e -> choose());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
                        && table.rowAtPoint(e.getPoint()) >= 0) {
                    choose();
                }
            }
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reload();
            }
        });

        reload();
    }

    public static Object[] pick(Component parent, String title, String[] columns,
                                Searcher searcher, String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            try {
                List<Object[]> found = searcher.search(kw);
                if (found.size() == 1) {
                    return found.get(0);
                }
            } catch (SQLException ex) {
                UiStyle.error(parent, ex);
                return null;
            }
        }
        PickerDialog dialog = new PickerDialog(
                SwingUtilities.getWindowAncestor(parent), title, columns, searcher, kw);
        dialog.setVisible(true);
        return dialog.selected;
    }

    public static JPanel fieldWithButton(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void reload() {
        try {
            rows = searcher.search(txtSearch.getText().trim());
            model.setRowCount(0);
            for (Object[] row : rows) {
                model.addRow(row);
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }

    private void choose() {
        int view = table.getSelectedRow();
        if (view < 0 && rows.size() == 1) {
            view = 0;
        }
        if (view < 0) {
            UiStyle.message(this, "Hãy chọn một dòng trong danh sách.");
            return;
        }
        selected = rows.get(table.convertRowIndexToModel(view));
        dispose();
    }
}