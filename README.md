# PetClinic — Quản lý phòng khám thú y

Ứng dụng desktop hỗ trợ quản lý khách hàng, thú cưng và hoạt động khám chữa bệnh tại phòng khám thú y, được xây dựng bằng **Java Swing** và **SQL Server**.

## 1. Giới thiệu

PetClinic là đồ án xây dựng hệ thống quản lý phòng khám thú y, kết nối các công việc từ tiếp nhận chủ nuôi, đăng ký thú cưng, đặt lịch hẹn đến khám bệnh, điều trị và thanh toán.

Chương trình phục vụ nhân viên tiếp nhận, bác sĩ và người quản lý phòng khám. Dữ liệu được lưu trữ tập trung để thuận tiện cho việc tra cứu thông tin và theo dõi lịch sử chăm sóc của từng thú cưng.

## 2. Mục tiêu

- Quản lý thống nhất thông tin chủ nuôi, thú cưng, bác sĩ và dịch vụ.
- Hỗ trợ quy trình đặt lịch, khám bệnh, kê đơn và tiêm phòng.
- Theo dõi hóa đơn, các khoản thanh toán và doanh thu.
- Giảm việc ghi chép thủ công, hạn chế dữ liệu nhập sai và hỗ trợ tra cứu nhanh.
- Vận dụng lập trình hướng đối tượng, thiết kế giao diện Java Swing và thao tác cơ sở dữ liệu qua JDBC.

## 3. Chức năng chính

| Phân hệ | Chức năng |
| --- | --- |
| Trang chủ | Hiển thị tổng khách hàng, tổng thú cưng, lịch hẹn trong ngày, doanh thu tháng và các lịch hẹn gần đây. |
| Khách hàng | Thêm mới, tìm theo số điện thoại, cập nhật thông tin và xem danh sách. Kiểm tra số điện thoại, email trùng; email được phép để trống. |
| Thú cưng | Đăng ký, tìm theo chủ nuôi và cập nhật thông tin thú cưng. Quản lý tên, loài, giới tính, ngày sinh, cân nặng và ghi chú. |
| Loài thú cưng | Thêm, cập nhật tên và mô tả loài; kiểm tra tên loài trùng; thống kê số thú cưng theo loài. |
| Bác sĩ | Quản lý thông tin liên hệ, chuyên môn, giấy phép hành nghề và trạng thái làm việc; hỗ trợ thêm, cập nhật và tìm kiếm. |
| Dịch vụ | Quản lý tên, mô tả, đơn giá và trạng thái dịch vụ; lựa chọn dịch vụ, số lượng và theo dõi chi phí cho lịch hẹn. |
| Lịch hẹn | Tạo, cập nhật, tìm kiếm và hủy lịch hẹn có lý do; kiểm tra chủ nuôi, thú cưng, bác sĩ và ngăn trùng lịch cùng thời điểm. |
| Hồ sơ khám bệnh | Lưu hồ sơ sức khỏe, nhóm máu, dị ứng và lịch sử khám; ghi nhận chỉ số sức khỏe, chẩn đoán và thông tin điều trị của từng lần khám. |
| Thuốc và đơn thuốc | Tra cứu danh mục thuốc, lập đơn thuốc, ghi nhận số lượng, liều dùng và hướng dẫn sử dụng; điều chỉnh tồn kho theo đơn thuốc. |
| Tiêm phòng | Tra cứu vaccine, ghi nhận lịch sử tiêm phòng, ngày tiêm và ngày tiêm tiếp theo của thú cưng. |
| Hóa đơn và thanh toán | Lập, tra cứu và theo dõi hóa đơn, khoản phí, giảm giá; ghi nhận thanh toán tiền mặt hoặc chuyển khoản, hỗ trợ thanh toán nhiều lần và cập nhật trạng thái khi thanh toán đủ. |

### Một số xử lý nghiệp vụ

- Sau khi thêm khách hàng thành công, chương trình chuyển sang màn hình thú cưng và điền sẵn mã chủ nuôi.
- Mỗi thú cưng liên kết với một chủ nuôi và một loài. Khi cập nhật, mã thú cưng và mã chủ nuôi được giữ nguyên.
- Nếu trùng chủ, tên, loài và ngày sinh, chương trình cảnh báo để người dùng kiểm tra và quyết định có tiếp tục lưu hay không.
- Loài chưa có thú cưng vẫn xuất hiện trong thống kê với số lượng bằng 0.
- Mã định danh được cơ sở dữ liệu tự cấp; mã có thể không liên tiếp khi việc thêm dữ liệu thất bại hoặc dữ liệu đã bị xóa.

## 4. Công nghệ sử dụng

| Công nghệ | Vai trò |
| --- | --- |
| Java | Xây dựng các lớp dữ liệu và xử lý nghiệp vụ theo hướng đối tượng. |
| Java Swing | Xây dựng cửa sổ, biểu mẫu nhập liệu, bảng danh sách và hộp thoại. |
| JDBC | Kết nối và thực hiện truy vấn, thêm, cập nhật dữ liệu. |
| SQL Server | Lưu trữ dữ liệu và quản lý các ràng buộc, quan hệ giữa các bảng. |
| Maven | Quản lý thư viện và quá trình biên dịch dự án. |
| Git, GitHub | Quản lý phiên bản mã nguồn và phối hợp làm việc nhóm. |

## 5. Cấu trúc dự án

Cấu trúc mã nguồn, tệp thiết kế giao diện, script SQL và các tệp dự án:

```text
CÂY THƯ MỤC                                               | CHÚ THÍCH
----------------------------------------------------------+------------------------------------------------
Pet_Clinic_Management/                                    |
├── database/                                             | # Các script cơ sở dữ liệu
│   ├── PetClinic_Create.sql                              | # Tạo cơ sở dữ liệu và các bảng
│   ├── PetClinic_Data.sql                                | # Dữ liệu mẫu
│   └── PetClinic_Queries.sql                             | # Các câu truy vấn
├── src/main/java/com/mycompany/petclinic/                | # Mã nguồn Java và thiết kế giao diện
│   ├── config/                                           | # Kết nối cơ sở dữ liệu
│   │   ├── DatabaseConnection.java                       |
│   │   └── TestConnection.java                           |
│   ├── dao/                                              | # Thao tác cơ sở dữ liệu qua JDBC
│   │   ├── AppointmentDAO.java                           |
│   │   ├── AppointmentServiceDAO.java                    |
│   │   ├── CustomerDAO.java                              |
│   │   ├── DaoUtil.java                                  |
│   │   ├── DashboardDAO.java                             |
│   │   ├── DiagnosisDAO.java                             |
│   │   ├── DoctorDAO.java                                |
│   │   ├── InvoiceDAO.java                               |
│   │   ├── MedicalRecordDAO.java                         |
│   │   ├── MedicalVisitDAO.java                          |
│   │   ├── MedicineDAO.java                              |
│   │   ├── PaymentDAO.java                               |
│   │   ├── PaymentMethodDAO.java                         |
│   │   ├── PetDAO.java                                   |
│   │   ├── PetOwnerInfoDAO.java                          |
│   │   ├── PetTypeDAO.java                               |
│   │   ├── PrescriptionDAO.java                          |
│   │   ├── PrescriptionDetailDAO.java                    |
│   │   ├── RecordLookupDAO.java                          |
│   │   ├── ServiceDAO.java                               |
│   │   ├── VaccinationDAO.java                           |
│   │   ├── VaccineDAO.java                               |
│   │   └── VitalSignDAO.java                             |
│   ├── model/                                            | # Các lớp biểu diễn dữ liệu
│   │   ├── Appointment.java                              |
│   │   ├── AppointmentService.java                       |
│   │   ├── Customer.java                                 |
│   │   ├── Diagnosis.java                                |
│   │   ├── Doctor.java                                   |
│   │   ├── Invoice.java                                  |
│   │   ├── MedicalRecord.java                            |
│   │   ├── MedicalVisit.java                             |
│   │   ├── Medicine.java                                 |
│   │   ├── Payment_Method.java                           |
│   │   ├── Payment.java                                  |
│   │   ├── Pet.java                                      |
│   │   ├── PetOwnerInfo.java                             |
│   │   ├── PetType.java                                  |
│   │   ├── Prescription.java                             |
│   │   ├── PrescriptionDetail.java                       |
│   │   ├── Service.java                                  |
│   │   ├── Vaccination.java                              |
│   │   ├── Vaccine.java                                  |
│   │   └── VitalSign.java                                |
│   └── ui/                                               | # Giao diện Java Swing
│       ├── appointment/                                  | # Quản lý lịch hẹn
│       │   ├── AppointmentManagementPanel.java           |
│       │   ├── AppointmentPanel.form                     | # Thiết kế giao diện NetBeans
│       │   ├── AppointmentPanel.java                     |
│       │   ├── DoctorPanel.form                          | # Thiết kế giao diện NetBeans
│       │   └── DoctorPanel.java                          |
│       ├── appointmentservice/                           | # Dịch vụ của lịch hẹn
│       │   └── AppointmentServiceManagementPanel.java    |
│       ├── customer/                                     | # Quản lý khách hàng
│       │   ├── CustomerPanel.form                        | # Thiết kế giao diện NetBeans
│       │   └── CustomerPanel.java                        |
│       ├── doctor/                                       | # Quản lý bác sĩ
│       │   └── DoctorManagementPanel.java                |
│       ├── medical/                                      | # Khám bệnh, thuốc và tiêm phòng
│       │   ├── DiagnosisTab.java                         |
│       │   ├── MedicalRecordDetailFrame.java             |
│       │   ├── MedicalRecordPanel.java                   |
│       │   ├── MedicineCatalogTab.java                   |
│       │   ├── MedicinePanel.form                        | # Thiết kế giao diện NetBeans
│       │   ├── MedicinePanel.java                        |
│       │   ├── PickerDialog.java                         |
│       │   ├── Pickers.java                              |
│       │   ├── PrescriptionDetailTab.java                |
│       │   ├── PrescriptionManagerFrame.java             |
│       │   ├── PrescriptionTab.java                      |
│       │   ├── ReadOnlyModel.java                        |
│       │   ├── RecordUsageTab.java                       |
│       │   ├── UiFormat.java                             |
│       │   ├── UiStyle.java                              |
│       │   ├── VaccinationTab.java                       |
│       │   ├── VaccineCatalogTab.java                    |
│       │   ├── VisitTab.java                             |
│       │   └── VitalSignTab.java                         |
│       ├── payment/                                      | # Hóa đơn và thanh toán
│       │   ├── InvoicePanel.form                         | # Thiết kế giao diện NetBeans
│       │   ├── InvoicePanel.java                         |
│       │   ├── PaymentPanel.form                         | # Thiết kế giao diện NetBeans
│       │   └── PaymentPanel.java                         |
│       ├── pet/                                          | # Quản lý thú cưng và loài
│       │   ├── PetPanel.form                             | # Thiết kế giao diện NetBeans
│       │   └── PetPanel.java                             |
│       ├── service/                                      | # Quản lý dịch vụ
│       │   └── ServiceManagementPanel.java               |
│       ├── DashboardPanel.java                           | # Trang chủ và thống kê
│       └── MainFrame.java                                | # Cửa sổ chính và điều hướng
├── DashboardPanel.java                                   | # Tệp Java ở thư mục gốc
├── MainFrame.java                                        | # Tệp Java ở thư mục gốc
├── PetClinic.bak                                         | # Tệp sao lưu cơ sở dữ liệu
├── pom.xml                                               | # Khai báo dự án Maven và thư viện
├── nbactions.xml                                         | # Các thao tác chạy trong NetBeans
├── README.md                                             | # Giới thiệu dự án
└── .gitignore                                            | # Quy tắc bỏ qua tệp khi quản lý bằng Git
```

Các tệp `.form` đi kèm một số màn hình lưu thông tin thiết kế giao diện của NetBeans.

### Vai trò các nhóm lớp

| Nhóm | Vai trò |
| --- | --- |
| `config` | Cung cấp kết nối đến SQL Server; có lớp kiểm tra kết nối. |
| `model` | Biểu diễn dữ liệu bằng các đối tượng như `Customer`, `Pet`, `MedicalRecord`, `Invoice`; lưu thuộc tính và cung cấp constructor, getter/setter. |
| `dao` | Thực hiện câu lệnh SQL, truyền tham số bằng JDBC và chuyển kết quả truy vấn thành đối tượng hoặc danh sách dữ liệu. |
| `ui` | Hiển thị biểu mẫu, bảng dữ liệu, tiếp nhận sự kiện, kiểm tra dữ liệu nhập và gọi DAO để thực hiện thao tác. Các màn hình được chia theo chức năng. |
| `database` | Lưu script tạo bảng, dữ liệu mẫu và các câu truy vấn phục vụ dự án. |

Ví dụ, khi thêm khách hàng, `CustomerPanel` tiếp nhận dữ liệu, tạo đối tượng `Customer`, gọi `CustomerDAO` để lưu vào SQL Server rồi nhận mã khách hàng để cập nhật giao diện và chuyển sang màn hình thú cưng.

### Vận dụng lập trình hướng đối tượng

Các lớp model đóng gói thuộc tính bằng `private`, cung cấp getter/setter và constructor để truy cập, khởi tạo dữ liệu. Các lớp giao diện kế thừa `JPanel` hoặc `JFrame`, đồng thời phối hợp với đối tượng model và DAO để thực hiện chức năng. Cách tổ chức này giúp phân chia trách nhiệm giữa dữ liệu, truy cập cơ sở dữ liệu và giao diện.

## 6. Quy trình sử dụng

1. **Tiếp nhận khách hàng:** tìm khách hàng đã có hoặc đăng ký khách hàng mới.
2. **Quản lý thú cưng:** chọn thú cưng của chủ nuôi hoặc đăng ký thú cưng mới.
3. **Đặt lịch hẹn:** chọn bác sĩ, thời gian hẹn và các dịch vụ cần sử dụng.
4. **Khám bệnh:** mở hồ sơ, ghi nhận lần khám, chỉ số sức khỏe và chẩn đoán.
5. **Điều trị:** lập đơn thuốc và ghi nhận tiêm phòng khi có chỉ định.
6. **Lập hóa đơn và thanh toán:** tổng hợp các khoản phí, áp dụng giảm giá nếu có và ghi nhận thanh toán. Khi khách hàng thanh toán đủ, hệ thống cập nhật trạng thái hóa đơn và hoàn tất quy trình.

Thông tin khách hàng, thú cưng, lịch sử khám và thanh toán được lưu lại để phục vụ tra cứu và thống kê.

## 7. Hướng dẫn chạy

1. Mở thư mục dự án có `pom.xml` bằng IDE hỗ trợ Java và Maven.
2. Chuẩn bị cơ sở dữ liệu `PetClinic` trên SQL Server theo các script trong thư mục `database`, thiết lập kết nối phù hợp với máy sử dụng.
3. Biên dịch dự án và chạy lớp `com.mycompany.petclinic.ui.MainFrame`.

---

Mã nguồn: [GiaHan561/Pet_Clinic_Management](https://github.com/GiaHan561/Pet_Clinic_Management) · [Thành viên đóng góp](https://github.com/GiaHan561/Pet_Clinic_Management/graphs/contributors)
