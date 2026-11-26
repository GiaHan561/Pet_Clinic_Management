package com.mycompany.petclinic.ui.medical;

import com.mycompany.petclinic.dao.MedicalVisitDAO;
import com.mycompany.petclinic.dao.MedicineDAO;
import com.mycompany.petclinic.dao.PrescriptionDAO;
import com.mycompany.petclinic.dao.VaccineDAO;
import com.mycompany.petclinic.model.MedicalVisit;
import com.mycompany.petclinic.model.Medicine;
import com.mycompany.petclinic.model.Prescription;
import com.mycompany.petclinic.model.Vaccine;

import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Pickers {

    private Pickers() {
    }

    public static Integer visit(Component parent, int recordID) {
        MedicalVisitDAO dao = new MedicalVisitDAO();
        Object[] row = PickerDialog.pick(parent, "Chọn lần khám",
                new String[]{"Mã lần khám", "Thời gian", "Lý do", "Trạng thái"},
                kw -> {
                    List<Object[]> rows = new ArrayList<>();
                    String key = kw.trim().toLowerCase();
                    for (MedicalVisit v : dao.findByRecordId(recordID)) {
                        String text = (v.getVisitID() + " " + UiStyle.nvl(v.getReason())).toLowerCase();
                        if (key.isEmpty() || text.contains(key)) {
                            rows.add(new Object[]{v.getVisitID(), v.getVisitTime(),
                                UiStyle.nvl(v.getReason()), v.getStatus()});
                        }
                    }
                    return rows;
                }, "");
        return row == null ? null : (Integer) row[0];
    }

    public static Integer prescription(Component parent, int recordID) {
        PrescriptionDAO dao = new PrescriptionDAO();
        Object[] row = PickerDialog.pick(parent, "Chọn đơn thuốc",
                new String[]{"Mã đơn thuốc", "Mã lần khám", "Ngày kê đơn", "Ghi chú"},
                kw -> {
                    List<Object[]> rows = new ArrayList<>();
                    String key = kw.trim().toLowerCase();
                    for (Prescription p : dao.findByRecordId(recordID)) {
                        String text = (p.getPrescriptionID() + " " + UiStyle.nvl(p.getNote())).toLowerCase();
                        if (key.isEmpty() || text.contains(key)) {
                            rows.add(new Object[]{p.getPrescriptionID(), p.getVisitID(),
                                p.getPrescriptionDate(), UiStyle.nvl(p.getNote())});
                        }
                    }
                    return rows;
                }, "");
        return row == null ? null : (Integer) row[0];
    }

    public static Medicine medicine(Component parent, String keyword) {
        MedicineDAO dao = new MedicineDAO();
        Object[] row = PickerDialog.pick(parent, "Chọn thuốc",
                new String[]{"Mã thuốc", "Tên thuốc", "Hoạt chất", "Đơn vị", "Giá", "Tồn kho"},
                kw -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (Medicine m : dao.search(kw)) {
                        rows.add(new Object[]{m.getMedicineID(), m.getName(),
                            UiStyle.nvl(m.getGenericName()), UiStyle.nvl(m.getUnit()),
                            UiFormat.money(m.getPrice()), m.getQuantity()});
                    }
                    return rows;
                }, keyword);
        if (row == null) {
            return null;
        }
        try {
            return dao.findById((Integer) row[0]);
        } catch (SQLException ex) {
            UiStyle.error(parent, ex);
            return null;
        }
    }

    public static Vaccine vaccine(Component parent, String keyword) {
        VaccineDAO dao = new VaccineDAO();
        Object[] row = PickerDialog.pick(parent, "Chọn vaccine",
                new String[]{"Mã vaccine", "Tên vaccine", "Nhà sản xuất", "Giá", "Tiêm nhắc lại"},
                kw -> {
                    List<Object[]> rows = new ArrayList<>();
                    for (Vaccine v : dao.search(kw)) {
                        rows.add(new Object[]{v.getVaccineID(), v.getName(),
                            UiStyle.nvl(v.getManufacturer()), UiFormat.money(v.getPrice()),
                            UiFormat.days(v.getRcmInterval())});
                    }
                    return rows;
                }, keyword);
        if (row == null) {
            return null;
        }
        try {
            return dao.findById((Integer) row[0]);
        } catch (SQLException ex) {
            UiStyle.error(parent, ex);
            return null;
        }
    }
}