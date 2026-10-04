# Bàn giao — mốc triển khai đầu tiên

Đọc AGENTS.md và REQUIREMENTS.md trước. Repo mới, không có mã cũ cần giữ.

Đã tạo: Java Android app; picker 2 app; lưu/sửa/xóa cặp; dark UI và icon THT; cài đặt; shell bridge authenticated; backend native/freeform; foreground overlay; token Voice API; workflow build.

Đang làm: build và unit/lint tests; tài liệu kiến trúc/Voice/kiểm thử xe. Chưa xác nhận chạy trên xe.

Bước tiếp: đọc kết quả build, sửa lỗi, kiểm tra bounds và backend reflection Android 9, hoàn thiện DEVICE_TEST/VOICE_API/ARCHITECTURE. Không thay backend bằng phụ thuộc EX2VNControl.

Giới hạn: freeform có caption OEM, native API phụ thuộc firmware. Bridge ADB cần chạy lại sau reboot. Chưa tích hợp API trong repo THTVoice. Chưa có khóa ký release ổn định.
