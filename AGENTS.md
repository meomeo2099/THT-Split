# Làm việc với THT Split

Luôn trả lời bằng tiếng Việt. Đọc theo thứ tự:
1. `docs/HANDOFF.md` (trạng thái thực tế và bước tiếp theo).
2. `docs/REQUIREMENTS.md` (quyết định của chủ dự án).
3. `docs/ARCHITECTURE.md`, `docs/REFERENCE_NOTES.md`.
4. Khi sửa Voice: `docs/VOICE_API.md`. Khi sửa backend: `docs/DEVICE_TEST.md`.

Không làm lại phần đã xong. Kiểm tra `git log -5`, `git status` trước khi sửa.
Quy tắc giữ ổn định Bridge (chốt 06/10/2026): giữ `sleep 1; echo THT_BOOTSTRAP_SENT`
bên trong shell ADB khi bootstrap, rồi 12 lần PING cách 250ms như 0.3.4/0.3.8.
Không bỏ/di chuyển khoảng giữ shell này sang sleep/poll ở phía app: vòng đời
shell khác dù tổng thời gian bằng nhau. Chỉ thay khi có bằng chứng chạy trên
LDPlayer 9 sau reboot, tự mở Bridge mà không chạy lệnh dự phòng trước, so cùng
cấu hình với bản cũ. CI/unit test không thay thế phép thử đó. PING ban đầu nếu
Bridge đã sẵn sàng vẫn trả ngay. Xem HANDOFF/DEVICE_TEST trước tối ưu kết nối.
0.3.4 được chủ dự án xác nhận chạy; 0.3.8 mới rollback/build đạt, chưa xác nhận
chạy trên LDPlayer/xe. Không biến giả thuyết race nohup/PTY thành nguyên nhân đã chứng minh.
Commit backup sau mỗi mốc hoàn chỉnh; cập nhật HANDOFF trong cùng commit.
Không ghi token bridge/Voice, khóa ký hoặc thông tin đăng nhập tài khoản vào git.
STK và QR ủng hộ công khai được nhúng trong APK khi chủ dự án đã yêu cầu rõ ràng.
Không gọi bản chạy trên xe thành công nếu chỉ build hoặc chạy unit test.
Không xóa dữ liệu/force-stop app bên thứ ba để chia màn hình.
X là tắt chia; Off chỉ dừng điều khiển/overlay vì không có quyền sửa task. Chevron là ẩn/hiện thanh. Popup thuộc THT Voice, không dành chỗ cố định.
Ưu tiên EX2 Android 9; firmware khác cần kiểm tra khả năng API trước.
Ứng dụng chia màn hình độc lập, không cần APK chia màn bên ngoài; không sao chép nguồn không rõ giấy phép.
