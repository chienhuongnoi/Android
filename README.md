# NoteApp – Ứng dụng ghi chú và quản lý công việc

NoteApp là ứng dụng giúp người dùng tạo ghi chú, quản lý công việc (task) kèm deadline và tự động nhắc nhở khi đến hạn. Ứng dụng có giao diện đơn giản, dễ sử dụng và phù hợp cho sinh viên lẫn người đi làm.

---

## 1. Tính năng chính

### 1.1. Ghi chú (Notes)
- Tạo ghi chú mới.
- Chỉnh sửa ghi chú.
- Xóa ghi chú.
- Phân loại ghi chú theo danh mục.
- Tìm kiếm ghi chú.
- Ghim ghi chú
### 1.2. Quản lý danh mục(category)
- Tạo mới danh mục
- Xoá danh mục

### 1.3. Quản lý Task
- Thêm công việc cần làm.
- Đặt deadline (ngày và giờ).
- Đánh dấu hoàn thành.
- Sắp xếp theo deadline (ưu tiên việc gần nhất).
- Đổi màu task khi quá hạn.
- Thông báo nhắc nhở khi đến deadline.

### 1.4. Nhắc nhở Deadline
- Khi task có deadline, ứng dụng tự lên lịch nhắc nhở.
- Đến thời điểm deadline, hệ thống gửi thông báo trên thanh trạng thái Android.
- Nếu task đã được đánh dấu hoàn thành thì sẽ không gửi thông báo nữa.

### 1.4. Giao diện
- Chuyển giữa Notes và Tasks bằng ViewPager2.
- Điều hướng bằng Bottom Navigation.
- Icon và giao diện tùy chỉnh.

---

## 2. Hướng dẫn sử dụng

### 2.1. Màn hình chính
Gồm hai phần:
- Notes (Ghi chú)
- Tasks (Công việc)

Người dùng có thể vuốt hoặc nhấn vào Bottom Navigation để chuyển trang.

### 2.2. Thêm ghi chú
1. Nhấn nút thêm.
2. Nhập tiêu đề và nội dung.
3. Nhấn Lưu.

### 2.3. Thêm task
1. Chuyển sang tab Tasks.
2. Nhấn nút thêm.
3. Nhập tên công việc.
4. Chọn Deadline nếu muốn nhắc nhở.
5. Nhấn Thêm Task.

Nếu không chọn deadline, task sẽ hiển thị là "Chưa hoàn thành" và không có thông báo.

### 2.4. Đánh dấu hoàn thành
- Tick vào checkbox.
- Task chuyển sang trạng thái "Đã hoàn thành", hiển thị mờ và gạch ngang.

### 2.5. Thông báo khi đến hạn
- Đến đúng giờ hệ thống sẽ gửi thông báo dạng Notification.
- Thông báo hiển thị ngay cả khi app không mở.

---

## 3. Yêu cầu hệ thống
- Android 8.0 trở lên (API 26+).
- Quyền thông báo đối với Android 13 trở lên.

---

## 4. Cài đặt và chạy dự án (dành cho lập trình viên)

### 4.1. Clone dự án
https://github.com/chienhuongnoi/Android.git

### 4.2. Mở bằng Android Studio
- File → Open.
- Chọn thư mục dự án.
### 4.3. Công nghệ sử dụng
- Kotlin
- ViewPager2
- RecyclerView
- AlarmManager + BroadcastReceiver
- NotificationChannel
- SQLite hoặc Room (tùy phiên bản)
- ViewBinding

---

## 5. Ghi chú thêm
- Nhắc nhở hoạt động ngay cả khi tắt ứng dụng.
- Cần kiểm tra quyền POST_NOTIFICATIONS với Android 13+
- Cần đảm bảo icon thông báo nằm trong thư mục drawable.

---
