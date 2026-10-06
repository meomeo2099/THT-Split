Bạn làm việc trên meomeo2099/THT-Split, package com.tht.split. Luôn dùng tiếng Việt.

## Bắt buộc giữ startup Bridge đã khôi phục
Chủ dự án yêu cầu lưu bài học06/10:0.3.4 chạy LDPlayer9, bản tối ưu bỏ sleep
trong remote shell gặp lỗi;0.3.8 đã khôi phục, đang chờ thử thực tế.
Không bỏ/di chuyển `sleep 1; echo THT_BOOTSTRAP_SENT` ra khỏi phiên shell ADB,
không thay bằng chờ/poll sau close. Giữ12 PING250ms; PING đầu trả ngay khi sẵn
sàng. Thay chuỗi này cần bằng chứng LDPlayer9 sau reboot tự mở Bridge không
chạy dự phòng trước, so cùng cấu hình bản cũ. CI không thay thế thử thiết bị.
Giả thuyết race nohup/PTY chưa được chứng minh. Đọc AGENTS/HANDOFF trước sửa.

Đọc AGENTS.md → docs/HANDOFF.md → REQUIREMENTS.md → ARCHITECTURE.md → REFERENCE_NOTES.md.
Đọc VOICE_API.md và THTVOICE_INTEGRATION.md khi ghép Voice, DEVICE_TEST.md khi thử xe.

Kiểm tra git log/status và Actions mới nhất. Đã có ADB nội bộ tự mở bridge, Off mở
ActivityOptions, system có kiểm quyền, API v2 PendingIntent/requestId và signature receiver.
Không làm lại UI/preset/backend. Không thêm Telnet/Shizuku. Không đổi khóa ký/package.
Off chỉ yêu cầu mở, không coi layout verified; ratio/drag từ0.2.1 gửi lại launch bounds, có thể bị firmware bỏ qua.
Thử local ADB trên xe, xác nhận khóa/cổng/reboot và ghi bằng chứng firmware/insets.
Phía THTVoice chưa ghép client; dùng THTVOICE_INTEGRATION.md làm hướng dẫn tại repo Voice.
Không nói đã test xe nếu chỉ build. Commit tiếng Việt, cập nhật HANDOFF và push GitHub.
Không đưa token bridge/Voice, khóa ADB hoặc keystore vào git/log.
