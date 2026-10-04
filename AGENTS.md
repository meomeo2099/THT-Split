# Làm việc với THT Split

Luôn trả lời bằng tiếng Việt. Đọc theo thứ tự:
1. `docs/HANDOFF.md` (trạng thái thực tế và bước tiếp theo).
2. `docs/REQUIREMENTS.md` (quyết định của chủ dự án).
3. `docs/ARCHITECTURE.md`, `docs/REFERENCE_NOTES.md`.
4. Khi sửa Voice: `docs/VOICE_API.md`. Khi sửa backend: `docs/DEVICE_TEST.md`.

Không làm lại phần đã xong. Kiểm tra `git log -5`, `git status` trước khi sửa.
Commit backup sau mỗi mốc hoàn chỉnh; cập nhật HANDOFF trong cùng commit.
Không ghi token bridge/Voice, khóa ký hoặc tài khoản vào git.
Không gọi bản chạy trên xe thành công nếu chỉ build hoặc chạy unit test.
Không xóa dữ liệu/force-stop app bên thứ ba để chia màn hình.
X là tắt chia; chevron là ẩn/hiện thanh. Popup thuộc THT Voice, không dành chỗ cố định.
Ưu tiên EX2 Android 9; firmware khác cần kiểm tra khả năng API trước.
Ứng dụng chia màn hình độc lập, không cần APK chia màn bên ngoài; không sao chép nguồn không rõ giấy phép.
