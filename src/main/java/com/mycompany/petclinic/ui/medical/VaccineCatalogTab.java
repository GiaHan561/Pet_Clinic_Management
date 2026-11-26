package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.VaccineDAO;
import com.mycompany.petclinic.model.Vaccine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class VaccineCatalogTab extends JPanel {

    private final VaccineDAO dao = new VaccineDAO();
    private ReadOnlyModel model;
    private JTextField txtKeyword;
    private JButton btnSearch, btnAll;
    private JTable tblData;
    private JScrollPane scrollTable;

    public VaccineCatalogTab() {
        initComponents();
        bindEvents();
        load("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel lbl = new JLabel("Mã, tên vaccine hoặc nhà sản xuất");
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
        UiStyle.card(search, "Tìm kiếm danh mục vaccine");

        model = new ReadOnlyModel(new String[]{
            "Mã vaccine", "Tên vaccine", "Nhà sản xuất", "Mô tả", "Giá", "Thời gian tiêm nhắc lại"
        }, Integer.class, String.class, String.class, String.class, String.class, String.class);

        tblData = new JTable(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollTable = new JScrollPane(tblData);
        UiStyle.table(tblData, scrollTable);

        int[] widths = {90, 220, 150, 380, 110, 160};
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
            List<Vaccine> list = dao.search(keyword);
            model.setRowCount(0);
            for (Vaccine v : list) {
                model.addRow(new Object[]{
                    v.getVaccineID(), v.getName(), UiStyle.nvl(v.getManufacturer()),
                    UiStyle.nvl(v.getDescription()), UiFormat.money(v.getPrice()),
                    UiFormat.days(v.getRcmInterval())
                });
            }
            if (list.isEmpty() && !keyword.isBlank()) {
                UiStyle.message(this, "Không tìm thấy vaccine phù hợp.");
            }
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
        }
    }
}