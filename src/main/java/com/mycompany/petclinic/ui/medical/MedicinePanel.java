package com.mycompany.petclinic.ui.medical;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class MedicinePanel extends JPanel {

    public MedicinePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("QUẢN LÝ THUỐC & VACCINE", SwingConstants.CENTER);
        title.setFont(new Font(UiStyle.FONT, Font.BOLD, 26));
        title.setForeground(UiStyle.PRIMARY_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(UiStyle.FONT, Font.BOLD, 14));
        tabs.setBackground(UiStyle.BG);
        tabs.addTab("Danh mục thuốc", new MedicineCatalogTab());
        tabs.addTab("Danh mục vaccine", new VaccineCatalogTab());
        tabs.addTab("Thuốc & vaccine theo hồ sơ", new RecordUsageTab());

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}