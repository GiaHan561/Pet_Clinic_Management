package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.model.MedicalRecord;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class PrescriptionManagerFrame extends JFrame {

    public PrescriptionManagerFrame(MedicalRecord record, Component owner) {
        super("Đơn thuốc & vaccine - hồ sơ #" + record.getMedicalRecordID());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiStyle.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("ĐƠN THUỐC & VACCINE", SwingConstants.CENTER);
        title.setFont(new Font(UiStyle.FONT, Font.BOLD, 26));
        title.setForeground(UiStyle.PRIMARY_DARK);

        JLabel subtitle = new JLabel(
                "Hồ sơ #" + record.getMedicalRecordID() + " - Thú cưng mã " + record.getPetID(),
                SwingConstants.CENTER);
        subtitle.setFont(new Font(UiStyle.FONT, Font.PLAIN, 14));
        subtitle.setForeground(UiStyle.LABEL);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        PrescriptionTab prescriptionTab = new PrescriptionTab(record);
        PrescriptionDetailTab detailTab = new PrescriptionDetailTab(record);
        VaccinationTab vaccinationTab = new VaccinationTab(record);
        prescriptionTab.setOnPrescriptionSelected(detailTab::setPrescriptionID);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(UiStyle.FONT, Font.BOLD, 14));
        tabs.setBackground(UiStyle.BG);
        tabs.addTab("Đơn thuốc", prescriptionTab);
        tabs.addTab("Chi tiết đơn thuốc", detailTab);
        tabs.addTab("Vaccine", vaccinationTab);
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }
}