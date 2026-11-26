package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.PetOwnerInfoDAO;
import com.mycompany.petclinic.model.MedicalRecord;
import com.mycompany.petclinic.model.PetOwnerInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class MedicalRecordDetailFrame extends JFrame {

    private static final int INFO_ROWS = 6;

    private final MedicalRecord record;
    private final PetOwnerInfoDAO infoDAO = new PetOwnerInfoDAO();

    public MedicalRecordDetailFrame(MedicalRecord record, Component owner) {
        super("Chi tiết hồ sơ khám bệnh #" + record.getMedicalRecordID());
        this.record = record;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiStyle.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("CHI TIẾT HỒ SƠ KHÁM BỆNH", SwingConstants.CENTER);
        title.setFont(new Font(UiStyle.FONT, Font.BOLD, 26));
        title.setForeground(UiStyle.PRIMARY_DARK);

        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(buildInfoRow(), BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        VisitTab visitTab = new VisitTab(record);
        VitalSignTab vitalSignTab = new VitalSignTab(record);
        DiagnosisTab diagnosisTab = new DiagnosisTab(record);
        visitTab.setOnVisitSelected(id -> {
            vitalSignTab.setVisitID(id);
            diagnosisTab.setVisitID(id);
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(UiStyle.FONT, Font.BOLD, 14));
        tabs.setBackground(UiStyle.BG);
        tabs.addTab("Chi tiết lần khám", visitTab);
        tabs.addTab("Chỉ số sức khỏe", vitalSignTab);
        tabs.addTab("Chẩn đoán", diagnosisTab);
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    private PetOwnerInfo loadInfo() {
        try {
            return infoDAO.findByPetId(record.getPetID());
        } catch (SQLException ex) {
            UiStyle.error(this, ex);
            return null;
        }
    }

    private JPanel buildInfoRow() {
        PetOwnerInfo info = loadInfo();

        String dob = "";
        String weight = "";
        if (info != null) {
            dob = info.getDateOfBirth() == null ? "" : info.getDateOfBirth().format(UiStyle.DATE_FMT);
            weight = info.getWeight() == null ? "" : info.getWeight().toPlainString() + " kg";
        }

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);

        row.add(infoCard("Thông tin thú cưng",
                new String[]{"Tên", "Loài", "Giới tính", "Ngày sinh", "Cân nặng", "Ghi chú"},
                info == null ? new String[0] : new String[]{
                    info.getPetName(), info.getTypeName(), UiStyle.nvl(info.getGender()),
                    dob, weight, UiStyle.nvl(info.getPetNote())}));

        row.add(infoCard("Chủ sở hữu (tóm tắt)",
                new String[]{"Họ tên", "Số điện thoại", "Email", "Địa chỉ"},
                info == null ? new String[0] : new String[]{
                    info.getOwnerName(), info.getOwnerPhone(),
                    UiStyle.nvl(info.getOwnerEmail()), UiStyle.nvl(info.getOwnerAddress())}));

        row.add(infoCard("Sức khỏe tổng quát",
                new String[]{"Mã phiếu khám", "Mã thú cưng", "Ngày tạo", "Nhóm máu", "Dị ứng", "Ghi chú"},
                new String[]{
                    String.valueOf(record.getMedicalRecordID()),
                    String.valueOf(record.getPetID()),
                    record.getCreatedDate() == null ? "" : record.getCreatedDate().format(UiStyle.DATE_FMT),
                    UiStyle.nvl(record.getBloodType()),
                    UiStyle.nvl(record.getAllergy()),
                    UiStyle.nvl(record.getNote())}));

        return row;
    }

    private JPanel infoCard(String title, String[] labels, String[] values) {
        JPanel card = new JPanel(new GridLayout(INFO_ROWS, 2, 10, 4));
        UiStyle.card(card, title);

        for (int i = 0; i < INFO_ROWS; i++) {
            JLabel name = new JLabel(i < labels.length ? labels[i] : "");
            UiStyle.label(name);

            String text = i < values.length ? UiStyle.nvl(values[i]) : "";
            JLabel value = new JLabel(text);
            value.setFont(new Font(UiStyle.FONT, Font.PLAIN, 13));
            value.setForeground(UiStyle.TEXT);
            value.setToolTipText(text.isEmpty() ? null : text);

            card.add(name);
            card.add(value);
        }
        return card;
    }
}
