package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicineDAO;
import com.mycompany.petclinic.model.Medicine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class MedicineCatalogTab extends JPanel {

    private final MedicineDAO dao = new MedicineDAO();
    private ReadOnlyModel model;
    private JTextField txtKeyword;
    private JButton btnSearch, btnAll;
    private JTable tblData;
    private JScrollPane scrollTable;

    public MedicineCatalogTab() {
        initComponents();
        bindEvents();
        load("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel lbl = new JLabel("Mã, tên thuốc, hoạt chất hoặc loại thuốc");
        UiStyle.label(lbl);

        txtKeyword = new JTextField();
        UiStyle.field(txtKeyword);
        txtKeyword.setPreferredSize(new Dimension(360, 38));

        btnSearch = new JButton("Tìm kiếm");
        btnAll = new JButton("Xem tất cả");
        UiStyle.button(btnSearch, UiStyle.PRIMARY);
        UiStyle.button(btnAll, new Color(26, 148, 168));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        search.add(lbl);
        search.add(txtKeyword);
        search.add(btnSearch);
        search.add(btnAll);
        UiStyle.card(search, "Tìm kiếm danh mục thuốc");

        model = new ReadOnlyModel(new String[]{
            "Mã thuốc", "Loại thuốc", "Tên thuốc", "Tên hoạt chất", "Mô tả",
            "Liều lượng dùng", "Giá", "Ngày sản xuất", "Đơn vị"
        }, Integer.class, String.class, String.class, String.class, String.class,
                String.class, String.class, LocalDate.class, String.class);

        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);

        int[] widths = {80, 150, 170, 140, 260, 120, 100, 120, 80};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(search, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
    }

    private void bindEvents() {
        btnSearch.addActionListener(e -> load(txtKeyword.getText()));
        txtKeyword.addActionListener(e -> load(txtKeyword.getText()));
        btnAll.addActionListener(e -> {
            txtKeyword.setText("");
            load("");
        });
    }

    private void load(String keyword) {
        try {
            List<Medicine> list = dao.search(keyword);
            model.setRowCount(0);
            for (Medicine m : list) {
                model.addRow(new Object[]{
                    m.getMedicineID(), UiStyle.nvl(m.getCategories()), m.getName(),
                    UiStyle.nvl(m.getGenericName()), UiStyle.nvl(m.getDescription()),
                    UiStyle.nvl(m.getDosage()), UiFormat.money(m.getPrice()),
                    m.getManufactureDate(), UiStyle.nvl(m.getUnit())
                });
            }
            if (list.isEmpty() && !keyword.isBlank()) {
                UiStyle.message(this, "Không tìm thấy thuốc phù hợp.");
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }
}
