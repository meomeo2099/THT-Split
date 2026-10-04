# API THT Voice → THT Split v2 — 05/10/2026

API đã có phía Split từ `0.2.0-test`; repo THTVoice chưa được sửa trong công việc này.
Đọc [THTVOICE_INTEGRATION.md](THTVOICE_INTEGRATION.md) để triển khai client.
Không cần cài phần mềm chia màn hoặc dịch vụ quyền bên ngoài.

## Địa chỉ và xác thực
- Package Split: `com.tht.split`.
- Action: `com.tht.split.COMMAND`, luôn dùng ComponentName explicit.
- Khác chữ ký: receiver `com.tht.split.VoiceReceiver`, extra `token` bằng token Voice trong Split.
- Cùng chữ ký: receiver `com.tht.split.SignatureVoiceReceiver`, không gửi token;
  Voice phải khai báo `uses-permission com.tht.split.permission.CONTROL_SPLIT`.
  Android bảo vệ receiver này bằng permission `signature`.
- Cả hai cách đều cần bật Cài đặt → THT Voice → Cho phép nhận lệnh.
- Token Voice được giữ sau tắt màn/reboot, chỉ đổi khi xoay token/xóa dữ liệu/cài lại.
- Token bridge riêng biệt và khóa RSA ADB không được gửi sang Voice hoặc đưa vào git/log.
- Không đổi khóa ký đang phát hành để ghép app. Chỉ sử dụng signature khi chứng thư thực tế khớp.

## Yêu cầu và callback
Mỗi yêu cầu v2 gửi `requestId` duy nhất (1–80 ký tự A–Z/a–z/0–9/_/-) và `result` là
PendingIntent broadcast do chính Voice tạo. Split chỉ chấp nhận creator package
`com.tht.voice` hoặc `com.tht.dashboard` (Dashboard cần kiểm tra package thực tế trước khi tích hợp).
Không dùng extra callbackPackage hay broadcast kết quả công khai.
PendingIntent phải cho phép fill-in extras: mutable từ Android 12, explicit tới receiver
không exported của Voice, FLAG_ONE_SHOT và requestCode riêng cho từng yêu cầu.
Không truyền token trong callback. V1 token không callback vẫn được giữ tương thích.

| command | Extra | Hành vi |
|---|---|---|
| start | left, right; ratio tùy chọn | Mở cặp; thiếu app dùng cấu hình đã nhớ |
| preset | presetId | Mở cặp theo ID đã lưu |
| replace | side=left/right, package | Đổi một bên |
| ratio | ratio int | Tỷ lệ trái clamp 30–70; Off từ chối resize khi đang chạy |
| swap | Không | Đổi bên, giữ tỷ lệ |
| fullscreen | side=left/right | App được chọn toàn màn |
| restore | Không | Khôi phục cặp và tỷ lệ |
| hide / show | Không | Ẩn/hiện thanh, vẫn giữ cặp |
| stop | Không | Tắt chia, ưu tiên app trái |
| home | Không | Về launcher xe, bỏ overlay |
| status | Không | Kiểm tra phương thức; ADB tự mở bridge nếu cần; không cần quyền nổi |

## Kết quả
Callback chứa `apiVersion=2`, `requestId`, `command`, `status`, `code`, `message`.
Khi có kết quả backend còn có `verified`, `transport`, `sdk`.

| status | Ý nghĩa và câu phản hồi Voice |
|---|---|
| success | Backend thực hiện xong; native/freeform kiểm mode lúc mở cặp. Không chứng minh nội dung app đã vẽ đúng/insets đúng. |
| requested | Off gửi yêu cầu ActivityOptions; không xác minh bố cục. Nói “Đã gửi yêu cầu chia màn”, không nói đã chia thành công. |
| error | Hiện/đọc lỗi; không tự đổi sang phương thức khác hoặc mở thêm app |

Mã lỗi: API_DISABLED, INVALID_COMMAND, INVALID_ARGUMENT, OVERLAY_REQUIRED, BUSY,
SERVICE_DENIED, OPERATION_FAILED, OVERLAY_FAILED. LAYOUT_UNVERIFIED đi cùng requested.
READY đi cùng success của status; Off READY chỉ nghĩa backend mở trực tiếp sẵn sàng,
không chứng minh có quyền shell hoặc đã chia màn.
Token sai, permission signature không hợp lệ, requestId/callback không hợp lệ sẽ bị từ chối;
Voice phải xử lý timeout. Thời gian chờ đề xuất 90 giây cho lần kết nối ADB đầu tiên;
không tự gửi lại start khi chưa biết trạng thái để tránh mở trùng.

## Phương thức và reboot
Voice không lựa chọn backend qua intent. Split dùng Cài đặt → Phương thức điều khiển:
- ADB nội bộ: ưu tiên 127.0.0.1:5555, cổng chỉnh được, chỉ kết nối loopback/địa chỉ chính thiết bị.
  Tự xác thực RSA TCP, mở bridge nếu chưa có listener và kiểm UID shell.
  Tùy chọn chuẩn bị lúc boot/cập nhật; nếu boot chưa sẵn sàng sẽ thử khi nhận lệnh.
- Off: yêu cầu mở freeform trực tiếp, không bridge. Freeform cần bật sẵn;
  chọn tỷ lệ trước khi mở, không kéo/resize task đang chạy.
- Quyền hệ thống: chỉ chọn được khi MANAGE_ACTIVITY_STACKS đã được cấp.
  Không tự cấp quyền, không có sharedUserId trong APK thường.

Không triển khai Telnet/Shizuku. ADB TLS/wireless pairing Android 11+ chưa được hỗ trợ.
Popup, STT/TTS, HVAC và tìm đường Vietmap thuộc Voice. Không dành vùng popup cố định trong Split.
