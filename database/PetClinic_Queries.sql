USE PetClinic;
GO

-- KIEM TRA TINH HOP LE CUA DU LIEU (CHUAN 3NF)
-- QUY UOC: Cac truy van o phan nay MONG DOI TRA VE 0 DONG.

-- 1. Khach hang trung so dien thoai
SELECT phone, COUNT(*) AS soLan
FROM CUSTOMERS
GROUP BY phone
HAVING COUNT(*) > 1;

-- 2. Kiem tra xem khach hang dat Lich hen (APPOINTMENT) co khac chu so huu Thu cung (PETS) hay khong
SELECT 
    i.invoiceID, 
    a.customerID AS appointmentCustomerID, 
    p.customerID AS petOwnerID
FROM INVOICE AS i
JOIN MEDICAL_VISIT AS mv ON mv.visitID = i.visitID
JOIN MEDICAL_RECORD AS mr ON mr.recordID = mv.recordID
JOIN PETS AS p ON p.petID = mr.petID
LEFT JOIN APPOINTMENT AS a ON a.appointmentID = mv.appointmentID
WHERE a.customerID IS NOT NULL AND a.customerID <> p.customerID;

-- 3. Lich hen 'Completed' nhung chua co lan kham
SELECT a.appointmentID, a.appointmentDate, a.status
FROM APPOINTMENT AS a
LEFT JOIN MEDICAL_VISIT AS mv ON mv.appointmentID = a.appointmentID
WHERE a.status = N'Completed' AND mv.visitID IS NULL;

-- 4. Lich hen dat ngoai gio lam viec cua bac si (khong tinh lich da huy)
SELECT a.appointmentID, a.doctorID, a.appointmentDate, a.appointmentTime
FROM APPOINTMENT AS a
WHERE a.status <> N'Cancelled'
  AND NOT EXISTS (
      SELECT 1
      FROM DOCTOR_SCHEDULE AS s
      WHERE s.doctorID = a.doctorID
        AND s.day_of_week = 2 + (DATEDIFF(DAY, '19000101', a.appointmentDate) % 7)
        AND a.appointmentTime >= s.startTime
        AND a.appointmentTime < s.endTime
  );


-- THONG KE THEO TRANG THAI / PHAN LOAI

-- So thu cung theo loai (ke ca loai chua co thu cung)
SELECT pt.typeName, COUNT(p.petID) AS petCount
FROM PET_TYPES AS pt
LEFT JOIN PETS AS p ON p.typeID = pt.typeID
GROUP BY pt.typeName
ORDER BY petCount DESC;


-- KIEM TRA DU LIEU THIEU (NULL / BANG MO COI)

-- Thu cung chua co ho so benh an
SELECT p.petID, p.name
FROM PETS AS p
LEFT JOIN MEDICAL_RECORD AS mr ON mr.petID = p.petID
WHERE mr.recordID IS NULL;

-- Bac si chua co chuyen mon hoac chua co lich lam viec
SELECT d.doctorID, d.fullName,
       (SELECT COUNT(*) FROM DOCTOR_SPECIALIZATION ds WHERE ds.doctorID = d.doctorID) AS soChuyenMon,
       (SELECT COUNT(*) FROM DOCTOR_SCHEDULE s WHERE s.doctorID = d.doctorID) AS soLich
FROM DOCTORS AS d
WHERE NOT EXISTS (SELECT 1 FROM DOCTOR_SPECIALIZATION ds WHERE ds.doctorID = d.doctorID)
   OR NOT EXISTS (SELECT 1 FROM DOCTOR_SCHEDULE s WHERE s.doctorID = d.doctorID);

-- Hoa don chua co giao dich thanh toan thanh cong
SELECT i.invoiceID, i.total, i.status
FROM INVOICE AS i
LEFT JOIN PAYMENT AS p ON p.invoiceID = i.invoiceID AND p.status = N'Success'
WHERE p.paymentID IS NULL;


-- KIEM TRA NHANH THUOC HET HAN / SAP HET HAN
-- Thuoc da het han
SELECT medicineID, name, quantity, expiryDay
FROM MEDICINE
WHERE expiryDay < CAST(GETDATE() AS DATE)
ORDER BY expiryDay;

-- Thuoc sap het hang (nguong mau 50 don vi)
SELECT medicineID, name, quantity, unit
FROM MEDICINE
WHERE quantity <= 50
ORDER BY quantity;
GO

-- DEM SO DONG CUA TAT CA CAC BANG
SELECT 'CUSTOMERS' AS tableName, COUNT(*) AS totalRows FROM CUSTOMERS
UNION ALL SELECT 'PET_TYPES', COUNT(*) FROM PET_TYPES
UNION ALL SELECT 'PETS', COUNT(*) FROM PETS
UNION ALL SELECT 'DOCTORS', COUNT(*) FROM DOCTORS
UNION ALL SELECT 'DOCTOR_SPECIALIZATION', COUNT(*) FROM DOCTOR_SPECIALIZATION
UNION ALL SELECT 'DOCTOR_SCHEDULE', COUNT(*) FROM DOCTOR_SCHEDULE
UNION ALL SELECT 'MEDICAL_RECORD', COUNT(*) FROM MEDICAL_RECORD
UNION ALL SELECT 'APPOINTMENT', COUNT(*) FROM APPOINTMENT
UNION ALL SELECT 'SERVICES', COUNT(*) FROM SERVICES
UNION ALL SELECT 'APPOINTMENT_SERVICE', COUNT(*) FROM APPOINTMENT_SERVICE
UNION ALL SELECT 'VACCINE', COUNT(*) FROM VACCINE
UNION ALL SELECT 'MEDICAL_VISIT', COUNT(*) FROM MEDICAL_VISIT
UNION ALL SELECT 'VITAL_SIGN', COUNT(*) FROM VITAL_SIGN
UNION ALL SELECT 'DIAGNOSIS', COUNT(*) FROM DIAGNOSIS
UNION ALL SELECT 'MEDICINE', COUNT(*) FROM MEDICINE
UNION ALL SELECT 'MEDICINE_CATEGORY', COUNT(*) FROM MEDICINE_CATEGORY
UNION ALL SELECT 'PRESCRIPTION', COUNT(*) FROM PRESCRIPTION
UNION ALL SELECT 'PRESCRIPTION_DETAIL', COUNT(*) FROM PRESCRIPTION_DETAIL
UNION ALL SELECT 'PAYMENT_METHOD', COUNT(*) FROM PAYMENT_METHOD
UNION ALL SELECT 'INVOICE', COUNT(*) FROM INVOICE
UNION ALL SELECT 'PAYMENT', COUNT(*) FROM PAYMENT
ORDER BY tableName;
GO