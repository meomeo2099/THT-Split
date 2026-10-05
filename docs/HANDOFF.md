# Bàn giao THT Split — 0.2.2-test, 05/10/2026

## Đọc trước khi tiếp tục
AGENTS.md → REQUIREMENTS.md → ARCHITECTURE.md → REFERENCE_NOTES.md.
Ghép Voice đọc VOICE_API.md và THTVOICE_INTEGRATION.md; thử xe đọc DEVICE_TEST.md.
Không làm lại UI, preset hoặc bộ chia đã có. Lịch sử commit là nguồn xác định mốc tiếp tục.

## Quyết định mới của chủ dự án
Chỉ giữ ADB nội bộ, Off theo cơ chế mở ActivityOptions và quyền hệ thống có điều kiện.
Không triển khai Telnet/Shizuku. Mỗi phiên chỉ dùng một phương thức; phải tắt phiên
trước chuyển backend/cổng. Mục tiêu không nhập lệnh máy tính mỗi lần lên xe.
Commit tiếng Việt và hướng dẫn trên GitHub để dự án Voice tiếp tục làm client.

## Đã có mã
| Phần | Kết quả |
|---|---|
| UI/preset/overlay | Giữ chọn hai app, CRUD, ẩn/hiện, tỷ lệ, đổi bên, fullscreen, Home/X |
| ADB nội bộ | Client RSA TCP original; khóa private lưu noBackupFiles; localhost mặc định, cổng chỉnh được; chỉ địa chỉ chính thiết bị |
| Bridge | Tự mở khi chưa có listener, token riêng, kiểm UID/version; không tự kill tiến trình hoặc bật adbd |
| Boot/cập nhật | Foreground ConnectionService chuẩn bị bridge nếu tùy chọn bật; khi lệnh tới tiếp tục thử kết nối |
| Off | ActivityOptions freeform với bounds và scoped hidden API access; status=requested; không giả lập resize task |
| Hệ thống | Kiểm MANAGE_ACTIVITY_STACKS trước chọn/chạy; gọi backend trực tiếp, không shared UID trong APK thường |
| Voice API v2 | Token tương thích v1 hoặc receiver bảo vệ signature; requestId + caller-owned PendingIntent; lỗi/busy/timeout ở phía client |
| Hướng dẫn Voice | THTVOICE_INTEGRATION.md có manifest/client Java/mapping/callback và các phần cần sửa ở Voice |

## Giới hạn thật
- Người dùng đã thử 0.2.0 trên LDPlayer bằng Off: chia được, tỷ lệ bị khóa. Bản sửa chưa được thử LDPlayer/xe.
- ADB nội bộ hỗ trợ ADB TCP RSA cổ điển, không ADB TLS/pairing Android 11+.
- Phải xác nhận khóa ADB lần đầu nếu firmware bật xác thực. Không tự vượt hộp thoại.
- ADB phải được firmware bật sau reboot; app không tự mở khóa/bật adbd.
- Boot có thể đến trước adbd/Wi-Fi; sẽ thử lại khi chia. Force-stop app có thể chặn boot receiver.
- Bridge 0.1 cũ đang chạy cần reboot màn để nạp bridge v2; không tự kill theo tên chung.
- Off chỉ yêu cầu hệ thống mở cửa sổ, không xác minh được task của app khác;
  từ 0.2.1 ratio/drag gửi lại launch bounds khi thả tay, có thể bị firmware bỏ qua.
  Không force-stop/xóa dữ liệu/tạo MULTIPLE_TASK. Hide/show chỉ đổi overlay.
- Hệ thống vẫn cần chữ ký/quyền phù hợp; không có khóa platform được tạo hoặc đưa vào git.
- Backend quyền hệ thống là nhánh có điều kiện, không phải APK debug tự có quyền.
- Native Android 9 ưu tiên; reflection Android mới/OEM có thể khác và báo lỗi.
- API Voice đã có phía Split; chưa sửa repo THTVoice. Không nói đã tích hợp Voice hoàn chỉnh.
- Popup, HVAC, tìm đường Vietmap thuộc Voice; Split không làm phần đó.
- Đã thêm thư viện LSPosed v6.1 có Apache-2.0, dùng bản source có ghi rõ adaptation và
  license trong APK; không copy mã GSplit không rõ giấy phép.

## Kiểm tra cần tiếp trên xe
1. Cài APK cùng signer bản test trước; cấp overlay, chọn ADB nội bộ/cổng thực tế, Kiểm tra.
2. Xác nhận khóa nếu có → bridge UID2000/version2 → thử native/freeform.
3. Reboot thật, đổi mạng Wi-Fi → gọi chia; ghi log/cổng/ảnh/insets/mode thật.
4. Bật freeform bằng ADB lần đầu → chọn Off → quan sát cả hai app, thử swap/stop/fullscreen.
5. Kiểm certificate/quyền platform trước thử system; không suy ra quyền từ cài được APK.
6. Dự án Voice triển khai client theo THTVOICE_INTEGRATION.md rồi test end-to-end.

## Build
JDK17/SDK35/Gradle8.9/AGP8.7.3. `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
Chi tiết kết quả thật ở VALIDATION.md. Khóa ký release/token/khóa ADB không ở git.
APK CI dùng debug key của runner; không đảm bảo cập nhật đè bản test ký máy khác.

## Bằng chứng máy phát triển ở mốc0.2.0
Build APK0.2, 17 unit tests và lint đạt (0 lỗi). Apksigner v2 hợp lệ;
compile-only Unsafe stub không có trong APK. Xem VALIDATION.md cho giới hạn và checksum.

## Mốc sửa sau phản hồi LDPlayer
- 0.2.1: bỏ khóa tỷ lệ Off; vạch kéo48×80dp, gửi yêu cầu khi thả, không resize liên tục.
- Màn chờ hiển thị trước khi worker mở app, dọn khi thành công/lỗi/hủy service;
  thanh điều khiển chỉ trở lại sau backend và khoảng ổn định tùy chọn.
- Bỏ moveTaskToBack ngay khi bấm; không lộ launcher khi backend chưa mở app.
- Chưa chứng minh bounds task cũ đổi được trong Off. Test theo DEVICE_TEST.
- Cache phụ thuộc Gradle máy phát triển không còn đủ; CI0.2.1 build/unit/lint đạt:
  https://github.com/meomeo2099/THT-Split/actions/runs/37261177497. APK CI khác signer có thể không cài đè bản cũ.

## Phản hồi và sửa0.2.2
- Người dùng thử0.2.1 Off trên LDPlayer: UI Split còn sau hai app, kéo không đổi cửa sổ.
- Khôi phục moveTaskToBack sau khi gửi lệnh start/preset để nền là launcher như bản cũ.
- Bỏ mặc định chờ thêm1,5s; mặc định0ms, vẫn giữ tùy chọn chờ nếu người dùng chọn.
- Dấu kéo hiển thị12×32dp, nền còn lại trong suốt; vùng nhận touch vẫn48×80dp.
- Gesture dùng độ dịch chuyển từ vị trí bắt đầu + touch slop; chạm đơn không gửi ratio.
- Off vẫn chỉ gửi lại launch bounds, không có quyền resize task. Toast khi đổi tỷ lệ
  chỉ dẫn ADB nếu cửa sổ không đổi. Không xác nhận LDPlayer là nguyên nhân.
- Chưa chạy bản0.2.2 trên LDPlayer/xe; cần quan sát nền launcher và handle khi thử.

CI0.2.2 build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37262534677.
APK CI0.2.2 khác signer bản0.2.1; không gỡ bản cũ khi chưa giữ cấu hình.
