
IF DB_ID(N'PetClinic') IS NULL
    CREATE DATABASE PetClinic;
GO
USE PetClinic;
GO


CREATE TABLE CUSTOMERS (
    customerID  INT IDENTITY(1,1) CONSTRAINT PK_CUSTOMERS PRIMARY KEY,
    fullName    NVARCHAR(100) NOT NULL,
    phone       VARCHAR(15)   NOT NULL,
    email       VARCHAR(100)  NULL,
    address     NVARCHAR(255) NULL,
    createdTime DATETIME2     NOT NULL CONSTRAINT DF_CUSTOMERS_created DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_CUSTOMERS_phone UNIQUE (phone)
);
GO

CREATE TABLE PET_TYPES (
    typeID      INT IDENTITY(1,1) CONSTRAINT PK_PET_TYPES PRIMARY KEY,
    typeName    NVARCHAR(50)  NOT NULL CONSTRAINT UQ_PET_TYPES_name UNIQUE,
    description NVARCHAR(255) NULL
);
GO

CREATE TABLE PETS (
    petID       INT IDENTITY(1,1) CONSTRAINT PK_PETS PRIMARY KEY,
    customerID  INT           NOT NULL,
    typeID      INT           NOT NULL,
    name        NVARCHAR(100) NOT NULL,
    gender      NVARCHAR(10)  NULL,
    date_of_birth DATE        NULL,
    weight      DECIMAL(5,2)  NULL,
    note        NVARCHAR(500) NULL,
    CONSTRAINT FK_PETS_CUSTOMERS FOREIGN KEY (customerID) REFERENCES CUSTOMERS(customerID),
    CONSTRAINT FK_PETS_TYPES     FOREIGN KEY (typeID)     REFERENCES PET_TYPES(typeID),
    CONSTRAINT CK_PETS_gender CHECK (gender IN (N'Đực', N'Cái', N'Male', N'Female', N'Unknown')),
    CONSTRAINT CK_PETS_weight CHECK (weight IS NULL OR weight > 0)
);
GO

CREATE TABLE DOCTORS (
    doctorID      INT IDENTITY(1,1) CONSTRAINT PK_DOCTORS PRIMARY KEY,
    fullName      NVARCHAR(100) NOT NULL,
    phone         VARCHAR(20)   NOT NULL,
    email         VARCHAR(100)  NULL,
    licenseNumber VARCHAR(50)   NOT NULL,
    status        NVARCHAR(20)  NOT NULL CONSTRAINT DF_DOCTORS_status DEFAULT N'Active',
    CONSTRAINT UQ_DOCTORS_license UNIQUE (licenseNumber),
    CONSTRAINT CK_DOCTORS_status CHECK (status IN (N'Active', N'Inactive', N'OnLeave'))
);
GO

--Thuộc tính đa trị
CREATE TABLE DOCTOR_SPECIALIZATION (
    doctorID       INT           NOT NULL,
    specialization NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_DOCTOR_SPECIALIZATION PRIMARY KEY (doctorID, specialization),
    CONSTRAINT FK_DOCSPEC_DOCTORS FOREIGN KEY (doctorID) REFERENCES DOCTORS(doctorID) ON DELETE CASCADE
);
GO

CREATE TABLE DOCTOR_SCHEDULE (
    scheduleID  INT IDENTITY(1,1) CONSTRAINT PK_DOCTOR_SCHEDULE PRIMARY KEY,
    doctorID    INT     NOT NULL,
    day_of_week TINYINT NOT NULL,  -- 8 = Chủ nhật
    startTime   TIME    NOT NULL,
    endTime     TIME    NOT NULL,
    CONSTRAINT FK_SCHEDULE_DOCTORS FOREIGN KEY (doctorID) REFERENCES DOCTORS(doctorID),
    CONSTRAINT CK_SCHEDULE_day  CHECK (day_of_week BETWEEN 2 AND 8),
    CONSTRAINT CK_SCHEDULE_time CHECK (endTime > startTime)
);
GO

CREATE TABLE MEDICAL_RECORD (
    recordID    INT IDENTITY(1,1) CONSTRAINT PK_MEDICAL_RECORD PRIMARY KEY,
    petID       INT           NOT NULL  UNIQUE,
    createdTime DATETIME2     NOT NULL CONSTRAINT DF_RECORD_created DEFAULT SYSDATETIME(),
    generalNote NVARCHAR(1000) NULL,
    allergyNote NVARCHAR(500)  NULL,
    bloodType   NVARCHAR(10)   NULL,
    CONSTRAINT FK_RECORD_PETS FOREIGN KEY (petID) REFERENCES PETS(petID)
);
GO

CREATE TABLE APPOINTMENT (
    appointmentID   INT IDENTITY(1,1) CONSTRAINT PK_APPOINTMENT PRIMARY KEY,
    customerID      INT           NOT NULL,
    petID           INT           NOT NULL,
    doctorID        INT           NOT NULL,
    appointmentDate DATE          NOT NULL,
    appointmentTime TIME          NOT NULL,
    reason          NVARCHAR(500) NULL,
    note            NVARCHAR(500) NULL,
    status          NVARCHAR(20)  NOT NULL CONSTRAINT DF_APPOINTMENT_status DEFAULT N'Pending',
    CONSTRAINT FK_APPT_CUSTOMERS FOREIGN KEY (customerID) REFERENCES CUSTOMERS(customerID),
    CONSTRAINT FK_APPT_PETS      FOREIGN KEY (petID)      REFERENCES PETS(petID),
    CONSTRAINT FK_APPT_DOCTORS   FOREIGN KEY (doctorID)   REFERENCES DOCTORS(doctorID),
    CONSTRAINT CK_APPT_status CHECK (status IN (N'Pending', N'Confirmed', N'Completed', N'Cancelled', N'NoShow'))
);
GO

CREATE TABLE SERVICES (
    serviceID   INT IDENTITY(1,1) CONSTRAINT PK_SERVICES PRIMARY KEY,
    serviceName NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,
    price       DECIMAL(18,2) NOT NULL,
    status      NVARCHAR(20)  NOT NULL CONSTRAINT DF_SERVICES_status DEFAULT N'Active',
    CONSTRAINT CK_SERVICES_price  CHECK (price >= 0),
    CONSTRAINT CK_SERVICES_status CHECK (status IN (N'Active', N'Inactive'))
);
GO

CREATE TABLE APPOINTMENT_SERVICE (
    appointmentID INT           NOT NULL,
    serviceID     INT           NOT NULL,
    quantity      INT           NOT NULL CONSTRAINT DF_APPTSVC_qty DEFAULT 1,
    unitPrice     DECIMAL(18,2) NOT NULL,
    CONSTRAINT PK_APPOINTMENT_SERVICE PRIMARY KEY (appointmentID, serviceID),
    CONSTRAINT FK_APPTSVC_APPT FOREIGN KEY (appointmentID) REFERENCES APPOINTMENT(appointmentID) ON DELETE CASCADE,
    CONSTRAINT FK_APPTSVC_SVC  FOREIGN KEY (serviceID)     REFERENCES SERVICES(serviceID),
    CONSTRAINT CK_APPTSVC_qty   CHECK (quantity > 0),
    CONSTRAINT CK_APPTSVC_price CHECK (unitPrice >= 0)
);
GO

CREATE TABLE VACCINE (
    vaccinID     INT IDENTITY(1,1) CONSTRAINT PK_VACCINE PRIMARY KEY,
    name         NVARCHAR(100) NOT NULL,
    manufacturer NVARCHAR(100) NULL,
    description  NVARCHAR(500) NULL,
    price        DECIMAL(18,2) NOT NULL,
    rcmInterval  INT           NULL,   -- khoảng cách tiêm nhắc lại (ngày)
    CONSTRAINT CK_VACCINE_price    CHECK (price >= 0),
    CONSTRAINT CK_VACCINE_interval CHECK (rcmInterval IS NULL OR rcmInterval > 0)
);
GO

CREATE TABLE MEDICAL_VISIT (
    visitID       INT IDENTITY(1,1) CONSTRAINT PK_MEDICAL_VISIT PRIMARY KEY,
    recordID      INT           NOT NULL,
    petID         INT           NOT NULL,
    doctorID      INT           NOT NULL,
    appointmentID INT           NULL,
    vaccinID      INT           NULL,
    reason        NVARCHAR(500) NULL,
    note          NVARCHAR(1000) NULL,
    status        NVARCHAR(20)  NOT NULL CONSTRAINT DF_VISIT_status DEFAULT N'InProgress',
    CONSTRAINT FK_VISIT_RECORD  FOREIGN KEY (recordID)      REFERENCES MEDICAL_RECORD(recordID),
    CONSTRAINT FK_VISIT_PETS    FOREIGN KEY (petID)         REFERENCES PETS(petID),
    CONSTRAINT FK_VISIT_DOCTORS FOREIGN KEY (doctorID)      REFERENCES DOCTORS(doctorID),
    CONSTRAINT FK_VISIT_APPT    FOREIGN KEY (appointmentID) REFERENCES APPOINTMENT(appointmentID),
    CONSTRAINT FK_VISIT_VACCINE FOREIGN KEY (vaccinID)      REFERENCES VACCINE(vaccinID),
    CONSTRAINT CK_VISIT_status CHECK (status IN (N'InProgress', N'Completed', N'Cancelled'))
);
GO

CREATE TABLE VITAL_SIGN (
    vitalSignID     INT IDENTITY(1,1) CONSTRAINT PK_VITAL_SIGN PRIMARY KEY,
    visitID         INT           NOT NULL    UNIQUE,
    weight          DECIMAL(6,2)  NULL,
    temperature     DECIMAL(4,1)  NULL,
    respiratoryRate INT           NULL,
    note            NVARCHAR(500) NULL,
    CONSTRAINT FK_VITAL_VISIT FOREIGN KEY (visitID) REFERENCES MEDICAL_VISIT(visitID) ON DELETE CASCADE,
    CONSTRAINT CK_VITAL_weight CHECK (weight IS NULL OR weight > 0),
    CONSTRAINT CK_VITAL_temp   CHECK (temperature IS NULL OR temperature BETWEEN 30 AND 45),
    CONSTRAINT CK_VITAL_resp   CHECK (respiratoryRate IS NULL OR respiratoryRate > 0)
);
GO

CREATE TABLE DIAGNOSIS (
    diagnosisID   INT IDENTITY(1,1) CONSTRAINT PK_DIAGNOSIS PRIMARY KEY,
    visitID       INT           NOT NULL,
    diagnosisName NVARCHAR(200) NOT NULL,
    severity      NVARCHAR(20)  NULL,
    description   NVARCHAR(1000) NULL,
    CONSTRAINT FK_DIAGNOSIS_VISIT FOREIGN KEY (visitID) REFERENCES MEDICAL_VISIT(visitID) ON DELETE CASCADE,
    CONSTRAINT CK_DIAGNOSIS_severity CHECK (severity IS NULL OR severity IN (N'Mild', N'Moderate', N'Severe', N'Critical'))
);
GO

CREATE TABLE MEDICINE (
    medicineID  INT IDENTITY(1,1) CONSTRAINT PK_MEDICINE PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    genericName NVARCHAR(100) NULL,
    description NVARCHAR(500) NULL,
    unit        NVARCHAR(30)  NULL,
    quantity    INT           NOT NULL CONSTRAINT DF_MEDICINE_qty DEFAULT 0,  -- tồn kho
    price       DECIMAL(18,2) NOT NULL,
    expiryDay   DATE          NULL,
    CONSTRAINT CK_MEDICINE_qty   CHECK (quantity >= 0),
    CONSTRAINT CK_MEDICINE_price CHECK (price >= 0)
);
GO

/* Thuộc tính đa trị */
CREATE TABLE MEDICINE_CATEGORY (
    medicineID INT           NOT NULL,
    category   NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_MEDICINE_CATEGORY PRIMARY KEY (medicineID, category),
    CONSTRAINT FK_MEDCAT_MEDICINE FOREIGN KEY (medicineID) REFERENCES MEDICINE(medicineID) ON DELETE CASCADE
);
GO

CREATE TABLE PRESCRIPTION (
    prescriptionID   INT IDENTITY(1,1) CONSTRAINT PK_PRESCRIPTION PRIMARY KEY,
    visitID          INT           NOT NULL    UNIQUE,
    doctorID         INT           NOT NULL,
    prescriptionDate DATETIME2     NOT NULL CONSTRAINT DF_PRESC_date DEFAULT SYSDATETIME(),
    note             NVARCHAR(500) NULL,
    CONSTRAINT FK_PRESC_VISIT   FOREIGN KEY (visitID)  REFERENCES MEDICAL_VISIT(visitID),
    CONSTRAINT FK_PRESC_DOCTORS FOREIGN KEY (doctorID) REFERENCES DOCTORS(doctorID)
);
GO

CREATE TABLE PRESCRIPTION_DETAIL (
    prescriptionDetailID INT IDENTITY(1,1) CONSTRAINT PK_PRESC_DETAIL PRIMARY KEY,
    prescriptionID INT           NOT NULL,
    medicineID     INT           NOT NULL,
    quantity       INT           NOT NULL,
    unitPrice      DECIMAL(18,2) NOT NULL,
    dosage         NVARCHAR(100) NULL,
    frequency      NVARCHAR(100) NULL,
    duration       NVARCHAR(100) NULL,
    instruction    NVARCHAR(500) NULL,
    CONSTRAINT FK_PRESCDT_PRESC    FOREIGN KEY (prescriptionID) REFERENCES PRESCRIPTION(prescriptionID) ON DELETE CASCADE,
    CONSTRAINT FK_PRESCDT_MEDICINE FOREIGN KEY (medicineID)     REFERENCES MEDICINE(medicineID),
    CONSTRAINT UQ_PRESCDT UNIQUE (prescriptionID, medicineID),
    CONSTRAINT CK_PRESCDT_qty   CHECK (quantity > 0),
    CONSTRAINT CK_PRESCDT_price CHECK (unitPrice >= 0)
);
GO

CREATE TABLE PAYMENT_METHOD (
    paymentMethodID INT IDENTITY(1,1) CONSTRAINT PK_PAYMENT_METHOD PRIMARY KEY,
    typeName        NVARCHAR(50)  NOT NULL CONSTRAINT UQ_PAYMETHOD_name UNIQUE,
    description     NVARCHAR(255) NULL
);
GO

CREATE TABLE INVOICE (
    invoiceID  INT IDENTITY(1,1) CONSTRAINT PK_INVOICE PRIMARY KEY,
    customerID INT           NOT NULL,
    visitID    INT           NOT NULL      UNIQUE,
    discount   DECIMAL(18,2) NOT NULL CONSTRAINT DF_INVOICE_discount DEFAULT 0,
    total      DECIMAL(18,2) NOT NULL CONSTRAINT DF_INVOICE_total DEFAULT 0,
    status     NVARCHAR(20)  NOT NULL CONSTRAINT DF_INVOICE_status DEFAULT N'Unpaid',
    CONSTRAINT FK_INVOICE_CUSTOMERS FOREIGN KEY (customerID) REFERENCES CUSTOMERS(customerID),
    CONSTRAINT FK_INVOICE_VISIT     FOREIGN KEY (visitID)    REFERENCES MEDICAL_VISIT(visitID),
    CONSTRAINT CK_INVOICE_discount CHECK (discount >= 0),
    CONSTRAINT CK_INVOICE_total    CHECK (total >= 0),
    CONSTRAINT CK_INVOICE_status   CHECK (status IN (N'Unpaid', N'Paid', N'Cancelled'))
);
GO

CREATE TABLE PAYMENT (
    paymentID       INT IDENTITY(1,1) CONSTRAINT PK_PAYMENT PRIMARY KEY,
    invoiceID       INT           NOT NULL,
    paymentMethodID INT           NOT NULL,
    paymentDate     DATETIME2     NOT NULL CONSTRAINT DF_PAYMENT_date DEFAULT SYSDATETIME(),
    amount          DECIMAL(18,2) NOT NULL,
    status          NVARCHAR(20)  NOT NULL CONSTRAINT DF_PAYMENT_status DEFAULT N'Success',
    CONSTRAINT FK_PAYMENT_INVOICE FOREIGN KEY (invoiceID)       REFERENCES INVOICE(invoiceID),
    CONSTRAINT FK_PAYMENT_METHOD  FOREIGN KEY (paymentMethodID) REFERENCES PAYMENT_METHOD(paymentMethodID),
    CONSTRAINT CK_PAYMENT_amount CHECK (amount > 0),
    CONSTRAINT CK_PAYMENT_status CHECK (status IN (N'Success', N'Pending', N'Failed', N'Refunded'))
);
GO

--INDEX cho các khóa ngoại thường truy vấn 
CREATE INDEX IX_PETS_customerID       ON PETS(customerID);
CREATE INDEX IX_APPOINTMENT_customer  ON APPOINTMENT(customerID, appointmentDate);
CREATE INDEX IX_APPOINTMENT_doctor    ON APPOINTMENT(doctorID, appointmentDate);
CREATE INDEX IX_VISIT_record          ON MEDICAL_VISIT(recordID);
CREATE INDEX IX_INVOICE_customer      ON INVOICE(customerID);
CREATE INDEX IX_PAYMENT_invoice       ON PAYMENT(invoiceID);
GO