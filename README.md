# 🎓 UTE Capstone Management (Hệ thống Quản lý Đề tài Sinh viên)

Hệ thống quản lý toàn diện quy trình đăng ký, xét duyệt, phân công phản biện và chấm điểm đề tài tốt nghiệp / nghiên cứu khoa học cho sinh viên Khoa CNTT - HCMUTE.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

* **Ngôn ngữ:** Java 21 (LTS)
* **Framework:** Spring Boot 3.3.4 (Spring MVC, Thymeleaf, Spring Security, Spring Data JPA)
* **Cơ sở dữ liệu:** MySQL 8.x (Local) / H2 In-Memory (Test tự động)
* **Quản lý DB Migration:** Flyway
* **Công cụ Build:** Apache Maven
* **CI/CD:** GitHub Actions (Build & Test tự động trên Java 21)

---

## 🚀 Hướng dẫn cài đặt & chạy Local

### 1. Yêu cầu môi trường
* Đã cài đặt **JDK 21** trở lên.
* Đã cài đặt **Maven 3.9+** (hoặc dùng Maven Wrapper).
* MySQL Server đang chạy trên cổng `3306`.

### 2. Cấu hình cơ sở dữ liệu
* Tạo database MySQL:
  ```sql
  CREATE DATABASE ute_capstone_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
* Kiểm tra tài khoản và mật khẩu trong file `src/main/resources/application.properties`:
  ```properties
  spring.datasource.username=root
  spring.datasource.password=root
  ```

### 3. Chạy ứng dụng
* Sử dụng Maven:
  ```bash
  mvn spring-boot:run
  ```
* Mở trình duyệt và truy cập: `http://localhost:8080`

---

## 👥 Phân quyền người dùng (Roles)
1. **Trưởng khoa (Dean/Admin):** Quản lý đợt đăng ký, thành lập hội đồng phản biện, duyệt kết quả cuối cùng.
2. **Bộ môn (Department Head):** Xét duyệt danh sách đề tài của bộ môn trước khi công bố.
3. **Giảng viên (Lecturer):** Đề xuất đề tài, hướng dẫn sinh viên, chấm điểm phản biện / hội đồng.
4. **Sinh viên (Student):** Lập nhóm (tối đa 3 SV), đăng ký đề tài, nộp báo cáo tiến độ và theo dõi kết quả.

---

## 🔄 Quy trình làm việc nhóm (Git Flow)
* **Không push trực tiếp lên nhánh `main`**.
* Mỗi tính năng mới tạo một nhánh riêng: `feature/<ten-tinh-nang>`.
* Khi hoàn thành, tạo **Pull Request (PR)** vào `main` để GitHub Actions tự động kiểm tra code trước khi merge.
