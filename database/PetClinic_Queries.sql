USE PetClinic;
GO


-- 1.1 Tong so khach hang
SELECT COUNT(*) AS totalCustomers
FROM CUSTOMERS;

-- 1.2 Tong so thu cung
SELECT COUNT(*) AS totalPets
FROM PETS;

-- 1.3 So lich hen trong hom nay (khong tinh lich da huy)
SELECT COUNT(*) AS appointmentsToday
FROM APPOINTMENT
WHERE appointmentDate = CAST(GETDATE() AS DATE)
  AND status <> N'Cancelled';

-- 1.4 Doanh thu thuc thu trong thang hien tai
SELECT COALESCE(SUM(amount), 0) AS revenueThisMonth
FROM PAYMENT
WHERE status = N'Success'
  AND paymentDate >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
  AND paymentDate < DATEADD(MONTH, 1,
        DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1));

-- 1.5. Lich hen sap toi
SELECT
    a.appointmentID,
    a.appointmentDate,
    a.appointmentTime,
    c.fullName AS customerName,
    c.phone,
    p.name AS petName,
    d.fullName AS doctorName,
    a.reason,
    a.status
FROM APPOINTMENT AS a
INNER JOIN CUSTOMERS AS c ON c.customerID = a.customerID
INNER JOIN PETS AS p ON p.petID = a.petID
INNER JOIN DOCTORS AS d ON d.doctorID = a.doctorID
WHERE
    (a.appointmentDate > CAST(GETDATE() AS DATE)
     OR (a.appointmentDate = CAST(GETDATE() AS DATE)
         AND a.appointmentTime >= CAST(GETDATE() AS TIME)))
    AND a.status IN (N'Pending', N'Confirmed')
ORDER BY a.appointmentDate, a.appointmentTime;

-- 1.6 Lich hen hom nay
SELECT
    a.appointmentID, a.appointmentTime,
    c.fullName AS customerName, p.name AS petName,
    d.fullName AS doctorName, a.reason, a.status
FROM APPOINTMENT AS a
JOIN CUSTOMERS AS c ON c.customerID = a.customerID
JOIN PETS AS p ON p.petID = a.petID
JOIN DOCTORS AS d ON d.doctorID = a.doctorID
WHERE a.appointmentDate = CAST(GETDATE() AS DATE)
ORDER BY a.appointmentTime;

-- 1.7 Doanh thu tung ngay trong 7 ngay gan nhat
SELECT
    CAST(paymentDate AS DATE) AS revenueDate,
    SUM(amount) AS dailyRevenue,
    COUNT(*) AS successfulTransactions
FROM PAYMENT
WHERE status = N'Success'
  AND paymentDate >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE))
  AND paymentDate < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
GROUP BY CAST(paymentDate AS DATE)
ORDER BY revenueDate;

-- 1.8 Hoa don chua thanh toan
SELECT
    i.invoiceID, i.visitID, i.total, i.discount, i.status,
    c.customerID, c.fullName AS customerName, c.phone
FROM INVOICE AS i
JOIN CUSTOMERS AS c ON c.customerID = i.customerID
WHERE i.status = N'Unpaid'
ORDER BY i.invoiceID DESC;

-- 1.9 Thuoc sap het han trong 30 ngay toi
SELECT medicineID, name, quantity, expiryDay
FROM MEDICINE
WHERE expiryDay >= CAST(GETDATE() AS DATE)
  AND expiryDay < DATEADD(DAY, 31, CAST(GETDATE() AS DATE))
ORDER BY expiryDay;

-- 1.1 Thuoc da het han
SELECT medicineID, name, quantity, expiryDay
FROM MEDICINE
WHERE expiryDay < CAST(GETDATE() AS DATE)
ORDER BY expiryDay;


-- 2.1 Danh sach bac si va chuyen mon
SELECT
    d.doctorID, d.fullName, d.phone, d.email,
    d.licenseNumber, d.status,
    STRING_AGG(ds.specialization, N', ') AS specializations
FROM DOCTORS AS d
LEFT JOIN DOCTOR_SPECIALIZATION AS ds ON ds.doctorID = d.doctorID
GROUP BY d.doctorID, d.fullName, d.phone, d.email,
         d.licenseNumber, d.status
ORDER BY d.fullName;

-- 2.2 Tim bac si theo ma, ten hoac so dien thoai
-- Thay N'%Dung%' bang tu khoa can tim.
SELECT doctorID, fullName, phone, email, licenseNumber, status
FROM DOCTORS
WHERE CAST(doctorID AS NVARCHAR(20)) LIKE N'%1%'
   OR fullName LIKE N'%Dung%'
   OR phone LIKE '%0938%';

-- 2.3 Xem chi tiet mot bac si
DECLARE @DoctorID_Detail INT = 1;
SELECT d.*, ds.specialization
FROM DOCTORS AS d
LEFT JOIN DOCTOR_SPECIALIZATION AS ds ON ds.doctorID = d.doctorID
WHERE d.doctorID = @DoctorID_Detail;

-- 2.4 Them bac si 
INSERT INTO DOCTORS(fullName, phone, email, licenseNumber, status)
VALUES (N'BS. Ten moi', '0900000000', 'doctor@example.com',
       'GP-NEW-0001', N'Active');

-- 2.5 Cap nhat thong tin bac si
UPDATE DOCTORS
SET fullName = 'Đặng Hoàng Nam',
    phone = '0938555444', --phone mới
    email = 'nam.dang@petclinic.vn'
WHERE doctorID = 1;

-- 2.6. Ngung hoat dong / cho nghi phep bac si (giu lich su)
UPDATE DOCTORS SET status = N'Inactive' WHERE doctorID = 1;
UPDATE DOCTORS SET status = N'OnLeave' WHERE doctorID = 1;
UPDATE DOCTORS SET status = N'Active' WHERE doctorID = 1;

-- 2.7 Them chuyen mon cho bac si
INSERT INTO DOCTOR_SPECIALIZATION(doctorID, specialization)
VALUES (1, N'Noi khoa');

-- 2.8 Xoa mot chuyen mon cua bac si
DELETE FROM DOCTOR_SPECIALIZATION
WHERE doctorID = 1 AND specialization = N'Noi khoa';


-- 3.1 Xem lich lam viec cua tat ca bac si
SELECT
    s.scheduleID, d.doctorID, d.fullName,
    CASE s.day_of_week
        WHEN 2 THEN N'Thu 2'
        WHEN 3 THEN N'Thu 3'
        WHEN 4 THEN N'Thu 4'
        WHEN 5 THEN N'Thu 5'
        WHEN 6 THEN N'Thu 6'
        WHEN 7 THEN N'Thu 7'
        WHEN 8 THEN N'Chu nhat'
    END AS dayName,
    s.startTime, s.endTime
FROM DOCTOR_SCHEDULE AS s
INNER JOIN DOCTORS AS d ON d.doctorID = s.doctorID
ORDER BY d.fullName, s.day_of_week, s.startTime;

-- 3.2 Xem lich lam viec cua mot bac si
DECLARE @DoctorID_Schedule INT = 1;
SELECT scheduleID, doctorID, day_of_week, startTime, endTime
FROM DOCTOR_SCHEDULE
WHERE doctorID = @DoctorID_Schedule
ORDER BY day_of_week, startTime;

-- 3.3 Them lich lam viec
-- day_of_week: 2=Thu 2, ..., 7=Thu 7, 8=Chu nhat.
INSERT INTO DOCTOR_SCHEDULE(doctorID, day_of_week, startTime, endTime)
VALUES (1, 2, '08:00', '12:00');

-- 3.4 Cap nhat lich lam viec
UPDATE DOCTOR_SCHEDULE
SET day_of_week = 3, startTime = '13:00', endTime = '17:00'
WHERE scheduleID = 1;

-- 3.5 Xoa lich lam viec
DELETE FROM DOCTOR_SCHEDULE WHERE scheduleID = 1;


-- 04.1. Danh sach lich hen kem khach hang, thu cung, bac si
SELECT
    a.appointmentID, a.appointmentDate, a.appointmentTime,
    c.fullName AS customerName, c.phone,
    p.name AS petName, pt.typeName,
    d.fullName AS doctorName,
    a.reason, a.note, a.status
FROM APPOINTMENT AS a
JOIN CUSTOMERS AS c ON c.customerID = a.customerID
JOIN PETS AS p ON p.petID = a.petID
JOIN PET_TYPES AS pt ON pt.typeID = p.typeID
JOIN DOCTORS AS d ON d.doctorID = a.doctorID
ORDER BY a.appointmentDate DESC, a.appointmentTime DESC;

-- 4.2. Xem chi tiet mot lich hen
DECLARE @AppointmentID_Detail INT = 1;
SELECT
    a.*, c.fullName AS customerName, c.phone,
    p.name AS petName, d.fullName AS doctorName
FROM APPOINTMENT AS a
JOIN CUSTOMERS AS c ON c.customerID = a.customerID
JOIN PETS AS p ON p.petID = a.petID
JOIN DOCTORS AS d ON d.doctorID = a.doctorID
WHERE a.appointmentID = @AppointmentID_Detail;

-- 4.3. Tim lich hen theo ma khach hang
DECLARE @CustomerID_Appointment INT = 1;
SELECT a.*
FROM APPOINTMENT AS a
WHERE a.customerID = @CustomerID_Appointment
ORDER BY a.appointmentDate DESC, a.appointmentTime DESC;

-- 4.4. Tim lich hen theo thu cung
DECLARE @PetID_Appointment INT = 1;
SELECT a.*, d.fullName AS doctorName
FROM APPOINTMENT AS a
JOIN DOCTORS AS d ON d.doctorID = a.doctorID
WHERE a.petID = @PetID_Appointment
ORDER BY a.appointmentDate DESC;

-- 4.5. Tim lich hen theo bac si
DECLARE @DoctorID_Appointment INT = 1;
SELECT a.*, p.name AS petName, c.fullName AS customerName
FROM APPOINTMENT AS a
JOIN PETS AS p ON p.petID = a.petID
JOIN CUSTOMERS AS c ON c.customerID = a.customerID
WHERE a.doctorID = @DoctorID_Appointment
ORDER BY a.appointmentDate, a.appointmentTime;

-- 4.6. Loc lich hen theo khoang ngay
DECLARE @FromDate DATE = '2026-10-01';
DECLARE @ToDate DATE = '2026-10-31';
SELECT *
FROM APPOINTMENT
WHERE appointmentDate >= @FromDate
  AND appointmentDate < DATEADD(DAY, 1, @ToDate)
ORDER BY appointmentDate, appointmentTime;

-- 4.7. Loc lich hen theo trang thai
DECLARE @AppointmentStatus NVARCHAR(20) = N'Confirmed';
SELECT *
FROM APPOINTMENT
WHERE status = @AppointmentStatus
ORDER BY appointmentDate, appointmentTime;

-- 4.8. Sap xep lich hen moi nhat
SELECT *
FROM APPOINTMENT
ORDER BY appointmentDate DESC, appointmentTime DESC;

-- 04.9. Tao lich hen moi
INSERT INTO APPOINTMENT 
(customerID, petID, doctorID, appointmentDate, appointmentTime,
reason, note, status)
VALUES (1, 1, 1, '2026-11-10', '09:00',
        N'Kham tong quat', NULL, N'Pending');

-- 4.10 Cap nhat lich hen
UPDATE APPOINTMENT
SET appointmentDate = '2026-11-11',
    appointmentTime = '10:00',
    doctorID = 2,
     reason = N'Tai kham'
WHERE appointmentID = 1;

-- 4.11. Huy lich hen: cap nhat trang thai, khong xoa ban ghi
UPDATE APPOINTMENT
SET status = N'Cancelled'
WHERE appointmentID = 1
AND status IN (N'Pending', N'Confirmed');

-- 4.12. Xac nhan / hoan thanh / khach khong den
UPDATE APPOINTMENT SET status = N'Confirmed' WHERE appointmentID = 1;
UPDATE APPOINTMENT SET status = N'Completed' WHERE appointmentID = 1;
UPDATE APPOINTMENT SET status = N'NoShow' WHERE appointmentID = 1;


-- 5.1. Danh sach dich vu
SELECT serviceID, serviceName, description, price, status
FROM SERVICES
ORDER BY serviceName;

-- 5.2. Chi xem dich vu dang kinh doanh
SELECT *
FROM SERVICES
WHERE status = N'Active'
ORDER BY serviceName;

-- 5.3. Tim dich vu theo ten
SELECT *
FROM SERVICES
WHERE serviceName LIKE N'%kham%';

-- 5.4 Sap xep dich vu theo gia tang dan
SELECT *
FROM SERVICES
ORDER BY price ASC;

-- 5.5. Them dich vu
INSERT INTO SERVICES(serviceName, description, price, status)
VALUES (N'Kham tai mui hong', N'Mo ta dich vu', 200000, N'Active');

-- 5.6. Cap nhat dich vu
UPDATE SERVICES
SET serviceName = N'Ten dich vu moi',
    description = N'Mo ta moi',
    price = 200000
WHERE serviceID = 1;

-- 5.7. Ngung kinh doanh dich vu (giu lich su gia)
UPDATE SERVICES SET status = N'Inactive' WHERE serviceID = 1;
UPDATE SERVICES SET status = N'Active' WHERE serviceID = 1;


-- 6.1. Danh sach dich vu cua mot lich hen
DECLARE @AppointmentID_Service INT = 1;
SELECT
    aps.appointmentID, s.serviceID, s.serviceName,
    aps.quantity, aps.unitPrice,
    aps.quantity * aps.unitPrice AS lineTotal
FROM APPOINTMENT_SERVICE AS aps
JOIN SERVICES AS s ON s.serviceID = aps.serviceID
WHERE aps.appointmentID = @AppointmentID_Service;

-- 6.2. Tong chi phi dich vu cua mot lich hen
SELECT
    appointmentID,
    SUM(quantity * unitPrice) AS serviceTotal
FROM APPOINTMENT_SERVICE
WHERE appointmentID = 1
GROUP BY appointmentID;

-- 6.3. Tong chi phi dich vu cua tat ca lich hen
SELECT
    a.appointmentID,
    COALESCE(SUM(aps.quantity * aps.unitPrice), 0) AS serviceTotal
FROM APPOINTMENT AS a
LEFT JOIN APPOINTMENT_SERVICE AS aps
    ON aps.appointmentID = a.appointmentID
GROUP BY a.appointmentID
ORDER BY a.appointmentID;

-- 6.4. Them dich vu vao lich hen
INSERT INTO APPOINTMENT_SERVICE(appointmentID, serviceID, quantity, unitPrice)
SELECT 1, serviceID, 1, price
FROM SERVICES
WHERE serviceID = 1 AND status = N'Active';

-- 6.5. Cap nhat so luong dich vu
UPDATE APPOINTMENT_SERVICE
SET quantity = 2
WHERE appointmentID = 1 AND serviceID = 1;

-- 6.6. Xoa dich vu khoi lich hen
DELETE FROM APPOINTMENT_SERVICE
WHERE appointmentID = 1 AND serviceID = 1;


-- 7.1. Danh sach khach hang
SELECT customerID, fullName, phone, email, address, createdTime
FROM CUSTOMERS
ORDER BY customerID DESC;

-- 7.2. Tim khach hang theo so dien thoai
DECLARE @CustomerPhone VARCHAR(15) = '0901234567';
SELECT *
FROM CUSTOMERS
WHERE phone = @CustomerPhone;

-- 7.3. Tim khach hang theo ten
SELECT *
FROM CUSTOMERS
WHERE fullName LIKE N'%Nguyen%';

-- 7.4. Tim khach hang theo email
SELECT *
FROM CUSTOMERS
WHERE email LIKE '%gmail.com%';

-- 7.5. Chi tiet khach hang
DECLARE @CustomerID_Detail INT = 1;
SELECT *
FROM CUSTOMERS
WHERE customerID = @CustomerID_Detail;

-- 7.6. Danh sach thu cung cua khach hang
SELECT
    c.customerID, c.fullName AS customerName,
    p.petID, p.name AS petName, pt.typeName,
    p.gender, p.date_of_birth, p.weight
FROM CUSTOMERS AS c
LEFT JOIN PETS AS p ON p.customerID = c.customerID
LEFT JOIN PET_TYPES AS pt ON pt.typeID = p.typeID
WHERE c.customerID = 1
ORDER BY p.name;

-- 7.7. Lich su lich hen cua khach hang
SELECT
    a.appointmentID, a.appointmentDate, a.appointmentTime,
    p.name AS petName, d.fullName AS doctorName, a.status
FROM APPOINTMENT AS a
JOIN PETS AS p ON p.petID = a.petID
JOIN DOCTORS AS d ON d.doctorID = a.doctorID
WHERE a.customerID = 1
ORDER BY a.appointmentDate DESC, a.appointmentTime DESC;

-- 7.8. Lich su hoa don va thanh toan cua khach hang
SELECT
    i.invoiceID, i.visitID, i.total, i.discount, i.status AS invoiceStatus,
    COALESCE(SUM(CASE WHEN pay.status = N'Success'
                      THEN pay.amount ELSE 0 END), 0) AS paidAmount
FROM INVOICE AS i
LEFT JOIN PAYMENT AS pay ON pay.invoiceID = i.invoiceID
WHERE i.customerID = 1
GROUP BY i.invoiceID, i.visitID, i.total, i.discount, i.status
ORDER BY i.invoiceID DESC;

-- 7.9. Them khach hang
INSERT INTO CUSTOMERS(fullName, phone, email, address)
VALUES (N'Khach hang moi', '0900000000',
         'customer@example.com', N'Dia chi moi');

-- 7.10. Cap nhat thong tin khach hang
UPDATE CUSTOMERS
SET fullName = N'Nguyen Van A',
     phone = '0900000001',
     email = 'a@example.com',
     address = N'Dia chi cap nhat'
WHERE customerID = 1;


-- 8.1. Danh sach thu cung kem chu va loai
SELECT
    p.petID, p.name AS petName, p.gender,
    p.date_of_birth, p.weight, p.note,
    c.customerID, c.fullName AS ownerName, c.phone,
    pt.typeID, pt.typeName
FROM PETS AS p
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
JOIN PET_TYPES AS pt ON pt.typeID = p.typeID
ORDER BY p.petID;

-- 8.2. Tim thu cung theo ten
SELECT p.*, c.fullName AS ownerName, c.phone, pt.typeName
FROM PETS AS p
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
JOIN PET_TYPES AS pt ON pt.typeID = p.typeID
WHERE p.name LIKE N'%Milo%';

-- 8.3. Xem thu cung cua mot khach hang
SELECT *
FROM PETS
WHERE customerID = 1;

-- 8.4. Chi tiet thu cung va ho so suc khoe
SELECT
    p.petID, p.name, p.gender, p.date_of_birth, p.weight, p.note,
    c.fullName AS ownerName, c.phone,
    pt.typeName,
    mr.recordID, mr.createdTime, mr.generalNote, mr.allergyNote, mr.bloodType
FROM PETS AS p
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
JOIN PET_TYPES AS pt ON pt.typeID = p.typeID
LEFT JOIN MEDICAL_RECORD AS mr ON mr.petID = p.petID
WHERE p.petID = 1;

-- 8.5. Them thu cung
INSERT INTO PETS(customerID, typeID, name, gender, date_of_birth, weight, note)
VALUES (1, 1, N'Ten thu cung', N'Duc', '2024-01-01', 5.50, N'Ghi chu');

-- 8.6. Cap nhat thong tin / can nang / ghi chu thu cung
UPDATE PETS
SET name = N'Milo', weight = 13.00,
     note = N'Cap nhat ghi chu suc khoe'
WHERE petID = 1;

-- 8.7. Danh sach loai thu cung
SELECT typeID, typeName, description
FROM PET_TYPES
ORDER BY typeName;

-- 8.8. Them loai thu cung
INSERT INTO PET_TYPES(typeName, description)
VALUES (N'Rùa', N'Rùa cảnh');

-- 8.9. Cap nhat loai thu cung
UPDATE PET_TYPES
SET typeName = N'Chim canh', description = N'Cac loai chim canh'
WHERE typeID = 4;

-- 8.10. So luong thu cung theo loai (bao gom ca loai chua co thu cung)
SELECT
    pt.typeID, pt.typeName,
    COUNT(p.petID) AS petCount
FROM PET_TYPES AS pt
LEFT JOIN PETS AS p ON p.typeID = pt.typeID
GROUP BY pt.typeID, pt.typeName
ORDER BY petCount DESC, pt.typeName;



-- 9.1 Danh sach ho so benh an
SELECT
    mr.recordID, mr.createdTime,
    p.petID, p.name AS petName,
    c.fullName AS ownerName, c.phone,
    mr.generalNote, mr.allergyNote, mr.bloodType
FROM MEDICAL_RECORD AS mr
JOIN PETS AS p ON p.petID = mr.petID
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
ORDER BY mr.createdTime DESC;

-- 9.2 Tim ho so theo ma thu cung
SELECT *
FROM MEDICAL_RECORD
WHERE petID = 1;

-- 9.3 Xem toan bo lich su kham cua mot thu cung
SELECT
    mr.recordID, mv.visitID, mv.reason, mv.note AS visitNote,
    mv.status, d.fullName AS doctorName,
    a.appointmentDate, a.appointmentTime,
    vs.weight, vs.temperature, vs.respiratoryRate
FROM MEDICAL_RECORD AS mr
INNER JOIN MEDICAL_VISIT AS mv ON mv.recordID = mr.recordID
INNER JOIN DOCTORS AS d ON d.doctorID = mv.doctorID
LEFT JOIN APPOINTMENT AS a ON a.appointmentID = mv.appointmentID
LEFT JOIN VITAL_SIGN AS vs ON vs.visitID = mv.visitID
WHERE mr.petID = 1
ORDER BY a.appointmentDate DESC, mv.visitID DESC;

-- 9.4. Tao ho so benh an cho thu cung chua co ho so
INSERT INTO MEDICAL_RECORD(petID, generalNote, allergyNote, bloodType)
SELECT 1, N'Ghi chu tong quat', NULL, NULL
WHERE NOT EXISTS (
     SELECT 1 FROM MEDICAL_RECORD WHERE petID = 1
);

-- 9.5. Cap nhat ghi chu ho so va thong tin di ung
UPDATE MEDICAL_RECORD
SET generalNote = N'Ghi chu suc khoe moi',
     allergyNote = N'Di ung da duoc cap nhat',
     bloodType = N'Unknown'
WHERE recordID = 1;



-- 10.1 Danh sach lan kham
SELECT
    mv.visitID, mv.recordID, mv.petID,
    p.name AS petName, c.fullName AS ownerName,
    d.fullName AS doctorName,
    mv.appointmentID, mv.reason, mv.note, mv.status
FROM MEDICAL_VISIT AS mv
JOIN PETS AS p ON p.petID = mv.petID
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
JOIN DOCTORS AS d ON d.doctorID = mv.doctorID
ORDER BY mv.visitID DESC;

-- 10.2 Chi tiet mot lan kham, bao gom chi so va chan doan
DECLARE @VisitID_Detail INT = 1;
SELECT
    mv.visitID, mv.reason, mv.note, mv.status,
    p.name AS petName, d.fullName AS doctorName,
    vs.weight, vs.temperature, vs.respiratoryRate, vs.note AS vitalNote,
    dg.diagnosisID, dg.diagnosisName, dg.severity, dg.description
FROM MEDICAL_VISIT AS mv
INNER JOIN PETS AS p ON p.petID = mv.petID
INNER JOIN DOCTORS AS d ON d.doctorID = mv.doctorID
LEFT JOIN VITAL_SIGN AS vs ON vs.visitID = mv.visitID
LEFT JOIN DIAGNOSIS AS dg ON dg.visitID = mv.visitID
WHERE mv.visitID = @VisitID_Detail;

-- 10.3 Loc lan kham theo bac si
SELECT mv.*, p.name AS petName
FROM MEDICAL_VISIT AS mv
INNER JOIN PETS AS p ON p.petID = mv.petID
WHERE mv.doctorID = 1
ORDER BY mv.visitID DESC;

-- 10.4 Loc lan kham theo trang thai
SELECT *
FROM MEDICAL_VISIT
WHERE status = N'Completed';

-- 10.5 Tao lan kham moi (can co recordID va petID hop le)
INSERT INTO MEDICAL_VISIT
(recordID, petID, doctorID, appointmentID, vaccinID, reason, note, status)
VALUES (1, 1, 1, 1, NULL, N'Kham tai kham', NULL, N'InProgress');

-- 10.6 Cap nhat trang thai lan kham
UPDATE MEDICAL_VISIT
SET status = N'Completed'
WHERE visitID = 1;


-- 10A.1 Xem chi so suc khoe cua mot lan kham
SELECT *
FROM VITAL_SIGN
WHERE visitID = 1;

-- 10A.2 Xem chi so suc khoe kem ten thu cung va ngay hen
SELECT
    vs.*, mv.petID, p.name AS petName,
    a.appointmentDate, a.appointmentTime
FROM VITAL_SIGN AS vs
JOIN MEDICAL_VISIT AS mv ON mv.visitID = vs.visitID
JOIN PETS AS p ON p.petID = mv.petID
LEFT JOIN APPOINTMENT AS a ON a.appointmentID = mv.appointmentID
ORDER BY vs.vitalSignID DESC;

-- 10A.3 Them chi so suc khoe
INSERT INTO VITAL_SIGN(visitID, weight, temperature, respiratoryRate, note)
VALUES (1, 12.60, 38.5, 25, N'Chi so on dinh');

-- 10A.4 Cap nhat chi so suc khoe
UPDATE VITAL_SIGN
SET weight = 12.70, temperature = 38.6,
     respiratoryRate = 26, note = N'Theo doi them'
WHERE visitID = 1;


-- 10B.1. Danh sach chan doan cua mot lan kham
SELECT diagnosisID, visitID, diagnosisName, severity, description
FROM DIAGNOSIS
WHERE visitID = 1;

-- 10B.2. Tim cac lan kham co chan doan theo ten benh
SELECT
    dg.diagnosisID, dg.diagnosisName, dg.severity,
    mv.visitID, p.name AS petName, d.fullName AS doctorName
FROM DIAGNOSIS AS dg
JOIN MEDICAL_VISIT AS mv ON mv.visitID = dg.visitID
JOIN PETS AS p ON p.petID = mv.petID
JOIN DOCTORS AS d ON d.doctorID = mv.doctorID
WHERE dg.diagnosisName LIKE N'%viem%';

-- 10B.3. Them chan doan
INSERT INTO DIAGNOSIS(visitID, diagnosisName, severity, description)
VALUES (1, N'Viêm da', N'Moderate', N'Mo ta chan doan');

-- 10B.4. Cap nhat chan doan
UPDATE DIAGNOSIS
SET diagnosisName = N'Viêm da dị ứng',
     severity = N'Moderate',
     description = N'Mo ta cap nhat'
WHERE diagnosisID = 1;

-- 10B.5 Xoa chan doan
DELETE FROM DIAGNOSIS WHERE diagnosisID = 1;


-- 10C.1 Danh sach vaccine
SELECT vaccinID, name, manufacturer, description, price, rcmInterval
FROM VACCINE
ORDER BY name;

-- 10C.2 Tim vaccine theo ten
SELECT *
FROM VACCINE
WHERE name LIKE N'%dại%';

-- 10C.3 Lich su vaccine duoc ghi nhan trong cac lan kham
SELECT
    mv.visitID, p.name AS petName, c.fullName AS ownerName,
    v.vaccinID, v.name AS vaccineName, v.manufacturer,
    v.price, mv.note
FROM MEDICAL_VISIT AS mv
JOIN VACCINE AS v ON v.vaccinID = mv.vaccinID
JOIN PETS AS p ON p.petID = mv.petID
JOIN CUSTOMERS AS c ON c.customerID = p.customerID
ORDER BY mv.visitID DESC;

-- 10C.4 Them vaccine
INSERT INTO VACCINE(name, manufacturer, description, price, rcmInterval)
VALUES (N'Vaccine moi', N'Nha san xuat', N'Mo ta', 200000, 365);

-- 10C.5 Cap nhat vaccine
UPDATE VACCINE
SET price = 220000, rcmInterval = 365
WHERE vaccinID = 1;


-- 11.1 Danh sach thuoc va danh muc
SELECT
    m.medicineID, m.name, m.genericName, m.description,
    m.unit, m.quantity, m.price, m.expiryDay,
    STRING_AGG(mc.category, N', ') AS categories
FROM MEDICINE AS m
LEFT JOIN MEDICINE_CATEGORY AS mc ON mc.medicineID = m.medicineID
GROUP BY m.medicineID, m.name, m.genericName, m.description,
         m.unit, m.quantity, m.price, m.expiryDay
ORDER BY m.name;

-- 11.2 Tim thuoc theo ten/hoat chat
SELECT *
FROM MEDICINE
WHERE name LIKE N'%amoxicillin%'
   OR genericName LIKE N'%amoxicillin%';

-- 11.3 Loc thuoc theo danh muc
SELECT m.*, mc.category
FROM MEDICINE AS m
INNER JOIN MEDICINE_CATEGORY AS mc ON mc.medicineID = m.medicineID
WHERE mc.category = N'Kháng sinh';

-- 11.4 Thuoc sap het hang (nguong mau 50 don vi)
SELECT medicineID, name, quantity, unit, price
FROM MEDICINE
WHERE quantity <= 50
ORDER BY quantity;

-- 11.5 Thuoc sap het han trong 90 ngay
SELECT medicineID, name, quantity, expiryDay
FROM MEDICINE
WHERE expiryDay >= CAST(GETDATE() AS DATE)
  AND expiryDay < DATEADD(DAY, 91, CAST(GETDATE() AS DATE))
ORDER BY expiryDay;

-- 11.6 Them thuoc
INSERT INTO MEDICINE(name, genericName, description, unit, quantity, price, expiryDay)
VALUES (N'Thuoc moi', N'Hoat chat', N'Mo ta', N'Vien', 100, 5000, '2027-12-31');

-- 11.7 Cap nhat thong tin thuoc
UPDATE MEDICINE
SET name = N'Ten thuoc moi', price = 6000,
     description = N'Mo ta moi'
WHERE medicineID = 1;

-- 11.8. Nhap them kho
UPDATE MEDICINE
SET quantity = quantity + 100
WHERE medicineID = 1;

-- 11.9 Dieu chinh ton kho (chi dung khi kiem ke)
UPDATE MEDICINE SET quantity = 450 WHERE medicineID = 1;

-- 11.10 Them danh muc cho thuoc
INSERT INTO MEDICINE_CATEGORY(medicineID, category)
VALUES (1, N'Thuoc chong di ung');

-- 11.11 Xoa danh muc thuoc
DELETE FROM MEDICINE_CATEGORY
WHERE medicineID = 1 AND category = N'Thuoc chong di ung';


-- 12.1 Danh sach don thuoc va bac si ke
SELECT
    pr.prescriptionID, pr.visitID, pr.prescriptionDate,
    d.fullName AS doctorName, pr.note
FROM PRESCRIPTION AS pr
JOIN DOCTORS AS d ON d.doctorID = pr.doctorID
ORDER BY pr.prescriptionDate DESC;

-- 12.2 Chi tiet mot don thuoc
SELECT
    pr.prescriptionID, pr.prescriptionDate,
    m.medicineID, m.name AS medicineName, m.unit,
    pd.quantity, pd.unitPrice,
    pd.quantity * pd.unitPrice AS lineTotal,
    pd.dosage, pd.frequency, pd.duration, pd.instruction
FROM PRESCRIPTION AS pr
JOIN PRESCRIPTION_DETAIL AS pd ON pd.prescriptionID = pr.prescriptionID
JOIN MEDICINE AS m ON m.medicineID = pd.medicineID
WHERE pr.prescriptionID = 1;

-- 12.3 Tong tien thuoc cua tung don
SELECT
    pr.prescriptionID,
    COALESCE(SUM(pd.quantity * pd.unitPrice), 0) AS medicineTotal
FROM PRESCRIPTION AS pr
LEFT JOIN PRESCRIPTION_DETAIL AS pd
    ON pd.prescriptionID = pr.prescriptionID
GROUP BY pr.prescriptionID
ORDER BY pr.prescriptionID;

-- 12.4 Lich su don thuoc cua mot thu cung
SELECT
    p.petID, p.name AS petName,
    mv.visitID, pr.prescriptionID, pr.prescriptionDate,
    m.name AS medicineName, pd.quantity,
    pd.dosage, pd.frequency, pd.duration, pd.instruction
FROM PETS AS p
JOIN MEDICAL_VISIT AS mv ON mv.petID = p.petID
JOIN PRESCRIPTION AS pr ON pr.visitID = mv.visitID
JOIN PRESCRIPTION_DETAIL AS pd ON pd.prescriptionID = pr.prescriptionID
JOIN MEDICINE AS m ON m.medicineID = pd.medicineID
WHERE p.petID = 1
ORDER BY pr.prescriptionDate DESC;

-- 12.5. Tao don thuoc cho lan kham (moi lan kham chi co mot don)
INSERT INTO PRESCRIPTION(visitID, doctorID, note)
VALUES (1, 1, N'Uong thuoc theo huong dan');

-- 12.6 Them thuoc vao don (lay gia hien tai lam gia tai thoi diem ke)
INSERT INTO PRESCRIPTION_DETAIL
(prescriptionID, medicineID, quantity, unitPrice, dosage, frequency, duration, instruction)
SELECT 1, medicineID, 10, price,
        N'1 vien', N'2 lan/ngay', N'5 ngay', N'Uong sau an'
FROM MEDICINE
WHERE medicineID = 1 AND quantity >= 10;

-- 12.7 Cap nhat huong dan su dung
UPDATE PRESCRIPTION_DETAIL
SET dosage = N'1/2 vien', frequency = N'2 lan/ngay',
     duration = N'7 ngay', instruction = N'Uong sau an'
WHERE prescriptionDetailID = 1;

-- 12.8 Xoa thuoc khoi don
DELETE FROM PRESCRIPTION_DETAIL
WHERE prescriptionDetailID = 1;



-- 13.1 Danh sach hoa don kem khach hang va thu cung
SELECT
    i.invoiceID, i.visitID, i.customerID,
    c.fullName AS customerName, c.phone,
    p.name AS petName, i.discount, i.total, i.status
FROM INVOICE AS i
JOIN CUSTOMERS AS c ON c.customerID = i.customerID
JOIN MEDICAL_VISIT AS mv ON mv.visitID = i.visitID
JOIN PETS AS p ON p.petID = mv.petID
ORDER BY i.invoiceID DESC;

-- 13.2 Chi tiet hoa don: thong tin chung
SELECT
    i.invoiceID, i.visitID, i.discount, i.total, i.status,
    c.fullName AS customerName, c.phone, c.address,
    p.name AS petName, d.fullName AS doctorName,
    mv.reason, mv.note AS visitNote
FROM INVOICE AS i
JOIN CUSTOMERS AS c ON c.customerID = i.customerID
JOIN MEDICAL_VISIT AS mv ON mv.visitID = i.visitID
JOIN PETS AS p ON p.petID = mv.petID
JOIN DOCTORS AS d ON d.doctorID = mv.doctorID
WHERE i.invoiceID = 1;

-- 13.3 Chi tiet dich vu thuoc lan kham cua hoa don
SELECT
    i.invoiceID, s.serviceName,
    aps.quantity, aps.unitPrice,
    aps.quantity * aps.unitPrice AS lineTotal
FROM INVOICE AS i
JOIN MEDICAL_VISIT AS mv ON mv.visitID = i.visitID
JOIN APPOINTMENT_SERVICE AS aps
    ON aps.appointmentID = mv.appointmentID
JOIN SERVICES AS s ON s.serviceID = aps.serviceID
WHERE i.invoiceID = 1;

-- 13.4 Chi tiet thuoc thuoc lan kham cua hoa don
SELECT
    i.invoiceID, m.name AS medicineName,
    pd.quantity, pd.unitPrice,
    pd.quantity * pd.unitPrice AS lineTotal,
    pd.dosage, pd.frequency, pd.duration
FROM INVOICE AS i
JOIN PRESCRIPTION AS pr ON pr.visitID = i.visitID
JOIN PRESCRIPTION_DETAIL AS pd ON pd.prescriptionID = pr.prescriptionID
JOIN MEDICINE AS m ON m.medicineID = pd.medicineID
WHERE i.invoiceID = 1;

-- 13.5 Tinh tong dich vu + thuoc + giam gia cho tung lan kham
-- Truy van nay tinh lai tu chi tiet, khong bi nhan dong do JOIN hai bang chi tiet.
WITH ServiceTotals AS (
    SELECT
        mv.visitID,
        SUM(aps.quantity * aps.unitPrice) AS serviceTotal
    FROM MEDICAL_VISIT AS mv
    JOIN APPOINTMENT_SERVICE AS aps
        ON aps.appointmentID = mv.appointmentID
    GROUP BY mv.visitID
),
MedicineTotals AS (
    SELECT
        pr.visitID,
        SUM(pd.quantity * pd.unitPrice) AS medicineTotal
    FROM PRESCRIPTION AS pr
    JOIN PRESCRIPTION_DETAIL AS pd
        ON pd.prescriptionID = pr.prescriptionID
    GROUP BY pr.visitID
)
SELECT
    i.invoiceID, i.visitID,
    COALESCE(st.serviceTotal, 0) AS serviceTotal,
    COALESCE(mt.medicineTotal, 0) AS medicineTotal,
    COALESCE(st.serviceTotal, 0) + COALESCE(mt.medicineTotal, 0) AS subtotal,
    i.discount,
    COALESCE(st.serviceTotal, 0) + COALESCE(mt.medicineTotal, 0)
        - i.discount AS calculatedTotal,
    i.total AS storedInvoiceTotal
FROM INVOICE AS i
LEFT JOIN ServiceTotals AS st ON st.visitID = i.visitID
LEFT JOIN MedicineTotals AS mt ON mt.visitID = i.visitID
ORDER BY i.invoiceID;

-- 13.6 Tim hoa don theo ma
SELECT * FROM INVOICE WHERE invoiceID = 1;

-- 13.7 Tim hoa don theo khach hang
SELECT i.*
FROM INVOICE AS i
WHERE i.customerID = 1
ORDER BY i.invoiceID DESC;

-- 13.8 Loc hoa don theo trang thai
SELECT *
FROM INVOICE
WHERE status = N'Unpaid'
ORDER BY invoiceID DESC;

-- 13.9 Tao hoa don cho lan kham chua co hoa don
-- Can tinh tong chi phi truoc khi tao hoa don.
INSERT INTO INVOICE(customerID, visitID, discount, total, status)
SELECT p.customerID, mv.visitID, 0, 0, N'Unpaid'
FROM MEDICAL_VISIT AS mv
JOIN PETS AS p ON p.petID = mv.petID
WHERE mv.visitID = 1
      AND NOT EXISTS (SELECT 1 FROM INVOICE i WHERE i.visitID = mv.visitID);

-- 13.10 Cap nhat muc giam gia
UPDATE INVOICE SET discount = 20000 WHERE invoiceID = 1;

-- 13.11 Cap nhat tong hoa don sau khi da tinh chi phi
-- Vi du nay tinh tong dich vu va thuoc, tru discount.
WITH ServiceTotals AS (
    SELECT mv.visitID, SUM(aps.quantity * aps.unitPrice) AS serviceTotal
    FROM MEDICAL_VISIT mv
    JOIN APPOINTMENT_SERVICE aps ON aps.appointmentID = mv.appointmentID
    GROUP BY mv.visitID
),
MedicineTotals AS (
     SELECT pr.visitID, SUM(pd.quantity * pd.unitPrice) AS medicineTotal
     FROM PRESCRIPTION pr
     JOIN PRESCRIPTION_DETAIL pd ON pd.prescriptionID = pr.prescriptionID
     GROUP BY pr.visitID
)
UPDATE i
SET total = CASE
     WHEN COALESCE(st.serviceTotal, 0) + COALESCE(mt.medicineTotal, 0) - i.discount < 0
     THEN 0
     ELSE COALESCE(st.serviceTotal, 0) + COALESCE(mt.medicineTotal, 0) - i.discount
END
FROM INVOICE i
LEFT JOIN ServiceTotals st ON st.visitID = i.visitID
LEFT JOIN MedicineTotals mt ON mt.visitID = i.visitID
WHERE i.invoiceID = 1;



-- 14.1 Danh sach phuong thuc thanh toan
SELECT paymentMethodID, typeName, description
FROM PAYMENT_METHOD
ORDER BY typeName;

-- 14.2 Danh sach giao dich
SELECT
    p.paymentID, p.invoiceID, p.paymentDate,
    p.amount, p.status,
    pm.typeName AS paymentMethod,
    c.fullName AS customerName
FROM PAYMENT AS p
JOIN PAYMENT_METHOD AS pm ON pm.paymentMethodID = p.paymentMethodID
JOIN INVOICE AS i ON i.invoiceID = p.invoiceID
JOIN CUSTOMERS AS c ON c.customerID = i.customerID
ORDER BY p.paymentDate DESC;

-- 14.3 Lich su giao dich cua mot hoa don
SELECT
    p.paymentID, p.invoiceID, p.paymentDate, p.amount,
    p.status, pm.typeName AS paymentMethod
FROM PAYMENT AS p
JOIN PAYMENT_METHOD AS pm ON pm.paymentMethodID = p.paymentMethodID
WHERE p.invoiceID = 1
ORDER BY p.paymentDate DESC;

-- 14.4 Tong da thanh toan va so tien con thieu cua tung hoa don
SELECT
    i.invoiceID, i.total, i.status,
    COALESCE(SUM(CASE WHEN p.status = N'Success'
                      THEN p.amount ELSE 0 END), 0) AS paidAmount,
    CASE
        WHEN i.total - COALESCE(SUM(CASE WHEN p.status = N'Success'
                                         THEN p.amount ELSE 0 END), 0) > 0
        THEN i.total - COALESCE(SUM(CASE WHEN p.status = N'Success'
                                         THEN p.amount ELSE 0 END), 0)
        ELSE 0
    END AS remainingAmount
FROM INVOICE AS i
LEFT JOIN PAYMENT AS p ON p.invoiceID = i.invoiceID
GROUP BY i.invoiceID, i.total, i.status
ORDER BY i.invoiceID;

-- 14.5 Lich su thanh toan cua mot khach hang
SELECT
    c.customerID, c.fullName,
    i.invoiceID, i.total AS invoiceTotal,
    p.paymentID, p.paymentDate, p.amount, p.status,
    pm.typeName AS paymentMethod
FROM CUSTOMERS AS c
JOIN INVOICE AS i ON i.customerID = c.customerID
JOIN PAYMENT AS p ON p.invoiceID = i.invoiceID
JOIN PAYMENT_METHOD AS pm ON pm.paymentMethodID = p.paymentMethodID
WHERE c.customerID = 1
ORDER BY p.paymentDate DESC;

-- 14.6 Loc giao dich theo khoang ngay
DECLARE @PaymentFrom DATE = '2026-10-01';
DECLARE @PaymentTo DATE = '2026-10-31';
SELECT *
FROM PAYMENT
WHERE paymentDate >= @PaymentFrom
  AND paymentDate < DATEADD(DAY, 1, @PaymentTo)
ORDER BY paymentDate DESC;

-- 14.7 Loc giao dich theo phuong thuc
SELECT p.*, pm.typeName
FROM PAYMENT AS p
JOIN PAYMENT_METHOD AS pm ON pm.paymentMethodID = p.paymentMethodID
WHERE pm.typeName = N'Tiền mặt'
ORDER BY p.paymentDate DESC;

-- 14.8 Tao giao dich thanh toan va cap nhat hoa don
-- Mau transaction: thay invoiceID, paymentMethodID, amount truoc khi chay.
-- Kiem tra da thanh toan du de tranh thu tien vuot tong hoa don.
DECLARE @PayInvoiceID INT = 1;
DECLARE @PayMethodID INT = 1;
DECLARE @PayAmount DECIMAL(18,2) = 100000;
BEGIN TRY
    BEGIN TRANSACTION;
     DECLARE @InvoiceTotal DECIMAL(18,2);
     DECLARE @AlreadyPaid DECIMAL(18,2);
     SELECT @InvoiceTotal = total
     FROM INVOICE WITH (UPDLOCK, HOLDLOCK)
     WHERE invoiceID = @PayInvoiceID AND status = N'Unpaid';
     IF @InvoiceTotal IS NULL
         THROW 50001, N'Khong tim thay hoa don chua thanh toan.', 1;
     SELECT @AlreadyPaid = COALESCE(SUM(amount), 0)
     FROM PAYMENT
     WHERE invoiceID = @PayInvoiceID AND status = N'Success';
     IF @PayAmount <= 0 OR @AlreadyPaid + @PayAmount > @InvoiceTotal
         THROW 50002, N'So tien thanh toan khong hop le.', 1;
     INSERT INTO PAYMENT(invoiceID, paymentMethodID, amount, status)
     VALUES (@PayInvoiceID, @PayMethodID, @PayAmount, N'Success');
     IF @AlreadyPaid + @PayAmount = @InvoiceTotal
         UPDATE INVOICE SET status = N'Paid'
         WHERE invoiceID = @PayInvoiceID;
     COMMIT TRANSACTION; 
END TRY
BEGIN CATCH
     IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
     THROW;
END CATCH;

-- 14.9 Huy/hoan tien giao dich: khong xoa lich su giao dich
UPDATE PAYMENT SET status = N'Refunded' WHERE paymentID = 1;
-- Sau khi hoan tien, can doi soat va cap nhat lai trang thai hoa don
-- theo chinh sach nghiep vu cua phong kham.


-- 15.1 Doanh thu theo ngay
SELECT
    CAST(paymentDate AS DATE) AS revenueDate,
    SUM(amount) AS revenue
FROM PAYMENT
WHERE status = N'Success'
GROUP BY CAST(paymentDate AS DATE)
ORDER BY revenueDate DESC;

-- 15.2 Doanh thu theo thang
SELECT
    YEAR(paymentDate) AS revenueYear,
    MONTH(paymentDate) AS revenueMonth,
    SUM(amount) AS monthlyRevenue
FROM PAYMENT
WHERE status = N'Success'
GROUP BY YEAR(paymentDate), MONTH(paymentDate)
ORDER BY revenueYear DESC, revenueMonth DESC;

-- 15.3 Doanh thu theo nam
SELECT
    YEAR(paymentDate) AS revenueYear,
    SUM(amount) AS yearlyRevenue
FROM PAYMENT
WHERE status = N'Success'
GROUP BY YEAR(paymentDate)
ORDER BY revenueYear DESC;

-- 15.4 So khach hang moi theo thang
SELECT
    YEAR(createdTime) AS createdYear,
    MONTH(createdTime) AS createdMonth,
    COUNT(*) AS newCustomers
FROM CUSTOMERS
GROUP BY YEAR(createdTime), MONTH(createdTime)
ORDER BY createdYear DESC, createdMonth DESC;

-- 15.5 So luot kham theo bac si
SELECT
    d.doctorID, d.fullName,
    COUNT(mv.visitID) AS visitCount
FROM DOCTORS AS d
LEFT JOIN MEDICAL_VISIT AS mv ON mv.doctorID = d.doctorID
GROUP BY d.doctorID, d.fullName
ORDER BY visitCount DESC;

-- 15.6 So luot kham theo thang
SELECT
    YEAR(a.appointmentDate) AS visitYear,
    MONTH(a.appointmentDate) AS visitMonth,
    COUNT(mv.visitID) AS visitCount
FROM MEDICAL_VISIT AS mv
JOIN APPOINTMENT AS a ON a.appointmentID = mv.appointmentID
GROUP BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)
ORDER BY visitYear DESC, visitMonth DESC;

-- 15.7 So lich hen theo trang thai
SELECT status, COUNT(*) AS appointmentCount
FROM APPOINTMENT
GROUP BY status
ORDER BY appointmentCount DESC;

-- 15.8 So hoa don theo trang thai
SELECT status, COUNT(*) AS invoiceCount,
       SUM(total) AS totalInvoiceValue
FROM INVOICE
GROUP BY status;

-- 15.9 Cac dich vu duoc su dung nhieu nhat
SELECT
    s.serviceID, s.serviceName,
    SUM(aps.quantity) AS totalQuantity,
    SUM(aps.quantity * aps.unitPrice) AS totalServiceRevenue
FROM SERVICES AS s
JOIN APPOINTMENT_SERVICE AS aps ON aps.serviceID = s.serviceID
GROUP BY s.serviceID, s.serviceName
ORDER BY totalQuantity DESC;

-- 15.10 Cac chan doan pho bien nhat
SELECT
    diagnosisName,
    COUNT(*) AS diagnosisCount
FROM DIAGNOSIS
GROUP BY diagnosisName
HAVING COUNT(*) >= 1
ORDER BY diagnosisCount DESC, diagnosisName;

-- 15.11 Tong tien thuoc da ke theo tung loai thuoc
SELECT
    m.medicineID, m.name,
    SUM(pd.quantity) AS totalMedicineQuantity,
    SUM(pd.quantity * pd.unitPrice) AS totalMedicineValue
FROM MEDICINE AS m
JOIN PRESCRIPTION_DETAIL AS pd ON pd.medicineID = m.medicineID
GROUP BY m.medicineID, m.name
ORDER BY totalMedicineQuantity DESC;

-- 15.12 Khach hang co tu 2 thu cung tro len
SELECT
    c.customerID, c.fullName,
    COUNT(p.petID) AS petCount
FROM CUSTOMERS AS c
JOIN PETS AS p ON p.customerID = c.customerID
GROUP BY c.customerID, c.fullName
HAVING COUNT(p.petID) >= 2
ORDER BY petCount DESC;



-- 16.1 Thu cung chua co ho so benh an
SELECT p.petID, p.name
FROM PETS AS p
LEFT JOIN MEDICAL_RECORD AS mr ON mr.petID = p.petID
WHERE mr.recordID IS NULL;

-- 16.2 Lich hen chua co lan kham
SELECT a.appointmentID, a.appointmentDate, a.appointmentTime, a.status
FROM APPOINTMENT AS a
LEFT JOIN MEDICAL_VISIT AS mv ON mv.appointmentID = a.appointmentID
WHERE a.status = N'Completed'
  AND mv.visitID IS NULL;

-- 16.3 Hoa don chua co giao dich thanh toan thanh cong
SELECT i.invoiceID, i.total, i.status
FROM INVOICE AS i
LEFT JOIN PAYMENT AS p
    ON p.invoiceID = i.invoiceID AND p.status = N'Success'
WHERE p.paymentID IS NULL;

-- 16.4 Phat hien hoa don co tong thanh toan khac tong hoa don
SELECT
    i.invoiceID, i.total,
    COALESCE(SUM(CASE WHEN p.status = N'Success'
                      THEN p.amount ELSE 0 END), 0) AS paidAmount
FROM INVOICE AS i
LEFT JOIN PAYMENT AS p ON p.invoiceID = i.invoiceID
GROUP BY i.invoiceID, i.total
HAVING COALESCE(SUM(CASE WHEN p.status = N'Success'
                         THEN p.amount ELSE 0 END), 0) <> i.total;

-- 16.5 Kiem tra lich hen co khach hang va thu cung khong dong bo
SELECT
    a.appointmentID, a.customerID,
    p.customerID AS petOwnerID, a.petID
FROM APPOINTMENT AS a
JOIN PETS AS p ON p.petID = a.petID
WHERE a.customerID <> p.customerID;

-- 16.6 Kiem tra cac don thuoc co so luong vuot ton kho hien tai
-- Day la canh bao tham khao, khong phai bang chung ton kho tai thoi diem ke.
SELECT
    pd.prescriptionID, m.medicineID, m.name,
    pd.quantity AS prescribedQuantity,
    m.quantity AS currentStock
FROM PRESCRIPTION_DETAIL AS pd
JOIN MEDICINE AS m ON m.medicineID = pd.medicineID
WHERE pd.quantity > m.quantity;

-- 17.14. Xoa mot thuoc khoi don (khong xoa danh muc thuoc)
DELETE FROM PRESCRIPTION_DETAIL
WHERE prescriptionDetailID = 1;

-- 17.15 Hien thi lich su don thuoc theo tung thu cung
DECLARE @PetID_MedicineHistory INT = 1;
SELECT
    p.petID, p.name AS petName,
    mv.visitID, pr.prescriptionID, pr.prescriptionDate,
    d.fullName AS doctorName,
    m.medicineID, m.name AS medicineName,
    pd.quantity, pd.dosage, pd.frequency, pd.duration,
    pd.instruction, pd.unitPrice
FROM PETS AS p
JOIN MEDICAL_VISIT AS mv ON mv.petID = p.petID
JOIN PRESCRIPTION AS pr ON pr.visitID = mv.visitID
JOIN DOCTORS AS d ON d.doctorID = pr.doctorID
JOIN PRESCRIPTION_DETAIL AS pd ON pd.prescriptionID = pr.prescriptionID
JOIN MEDICINE AS m ON m.medicineID = pd.medicineID
WHERE p.petID = @PetID_MedicineHistory
ORDER BY pr.prescriptionDate DESC, pr.prescriptionID DESC;

-- Dem so dong cua tat ca bang
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