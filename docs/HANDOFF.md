# Bàn giao THT Split — bản 0.1.0-test

## Đọc trước khi sửa
AGENTS.md → REQUIREMENTS.md → ARCHITECTURE.md → REFERENCE_NOTES.md.
Sửa backend đọc DEVICE_TEST.md; sửa tích hợp đọc VOICE_API.md.
Prompt gửi cho người/agent mới: NEXT_AGENT_PROMPT.md.

## Trạng thái thực tế
Repo khởi tạo mới ngày 04/10/2026; package `com.tht.split`.
Có mã ứng dụng và APK debug, không còn chỉ là ảnh mockup.
Đã build + 11 unit test + lint thành công tại máy phát triển. APK xác minh chữ ký v2.
GitHub Actions đã build thành công ở commit7c84d3e; xem run mới nhất sau mỗi commit code.
**Chưa chạy trên EX2 hoặc điện thoại/emulator. Không coi bảng chức năng là kết quả test xe.**

| Phần | Đã làm | Cần tiếp |
|---|---|---|
| UI | Trang chính, app picker có tìm, preset CRUD,5 nhóm settings,icon THT | Chụp UI thật, sửa kích thước trên xe/phone |
| Presets | ID ổn định; tên/app/tỷ lệ; sửa/xóa xác nhận; nhớ qua restart | Thử CRUD trên Android thực |
| Native | Shell launch mode 3/4, named Binder methods, resizeDockedStack, kiểm mode sau mở | Kiểm tra IHU629G và đo OEM insets |
| Freeform | Mode 5, resizeTask, bật/khôi phục global settings | Caption và hỗ trợ firmware; chưa NoCaption |
| Overlay | Ratio buttons, swap/replace/full/Home/X, hide/show,auto-hide | Kiểm tra task/overlay lifecycle và focus OEM |
| Divider | Kéo grip, cập nhật bounds khi thả | Live resize theo debounce sau đo performance |
| Voice | Explicit receiver + token + allowlist; commandcontract | ACK/requestId, rồi ghép repo THTVoice |
| Kết nối | Bridge localhost có token, UIDshell, timeout,error | Chạy lại sau reboot; optional Shizuku về sau |
| Cập nhật | Check latest/mở Releases/xuất log | Ký ổn định, tải/cài APK có kiểm chữ ký |
| CI | Build/unit/lint; artifact7ngày | Không dùng khóa debug thay đổi cho phát hành chính thức |

## Bước tiếp theo theo ưu tiên
1. Cài APK lên EX2, cấp overlay, khởi động bridge bằng lệnh lấy trong app. Làm DEVICE_TEST.
   Ghi firmware/API/packages/ảnh và log. Nếu API bị chặn, sửa backend theo log thật;
   không tự mở quyền platform, không copy decompile hoặc đoán binder transaction code.
2. Chốt native/freeform nào đúng trên xe và profile chừa OEM. ShellServer đã kiểm mode
   quan sát được sau layout; còn phải nhìn bounds/insets thật. Nếu không có getAllStackInfos
   đúng firmware, bổ sung parser/adapter, không báo thành công mặc định.
3. Làm divider live resize, coalesce lệnh và kiểm thử không tái tạo task/nhấp nháy.
   Bản đầu không force-stop app; không cam kết “không restart” nếu OEM tự tái tạo Activity.
4. Bổ sung API phản hồi có requestId/status, rồi tích hợp THTVoice theo AGENTS repo đó.
   Split hiện chưa sửa repo Voice, chưa có STT/TTS và popup nghe riêng.
5. Provision khóa ký ngoài git, build bản test ký ổn định, rồi cập nhật tải/cài có xác minh.
   Repo đang riêng tư: unauthenticated ReleasesAPI có thể404; app báo đúng giới hạn.

## Quyết định không đổi
- App độc lập, không cần cài APK chia màn bên ngoài.
- X tắt chia; chevron ẩn/hiện thanh. Tắt mặc định về app trái, nếu trái đã đóng dùng app còn lại.
- Popup Voice do Voice di chuyển/tự tắt; Split không giữ chỗ.
- Không điều khiển HVAC/cửa/kính/camera trong repo này.
- Token Voice và bridge khác nhau; allowBackup=false; không ghi token/keystore vào git/log.
- Bridge chỉ loopback và typed operation allowlist, không API chạy shell tùy ý.
- Các phương thức backend dùng tên API, không transaction number của firmware khác.

## Lệnh kiểm tra
```sh
git status
git log -5 --oneline
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```
APK: app/build/outputs/apk/debug/app-debug.apk. JDK 17 / SDK 35 / Gradle 8.9 / AGP 8.7.3.
CI workflow: .github/workflows/android.yml. Artifact THT-Split-test-apk giữ 7 ngày.
Mỗi mốc sửa hoàn chỉnh phải commit backup và cập nhật HANDOFF cùng commit.

## Các commit mốc ban đầu
- 9f4022a: khởi tạo phạm vi app.
- 5299b10: scaffold Android, lưu yêu cầu và ghi chú nguồn.
- b40b808: app/preset/overlay/backend/Voice API bản đầu.
- 7c84d3e: sửa lint và SDK CI, thêm kiến trúc/Voice/cài-test.
- Xem git log / commit mới nhất cho mốc xác minh và bàn giao cuối, không quay lại làm từ đầu.

Đã tách Store session khỏi draft UI để chọn app/cài đặt chưa áp dụng không điều khiển nhầm cặp.
Callback service không vẽ overlay sau onDestroy; X có xử lý app bên kia đã đóng.

## Cập nhật tài liệu 04/10/2026
Đã làm gọn mô tả ứng dụng độc lập và nguồn tham khảo theo yêu cầu chủ dự án.
Không thay đổi mã ứng dụng hoặc API; kiểm tra diff và rà lại tên/package/link tham chiếu trong toàn bộ tệp được git theo dõi.
