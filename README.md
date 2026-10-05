# THT Split

App chia màn hình độc lập cho Geely EX2, package `com.tht.split`, bản `0.2.2-test`.
Chọn hai app, lưu/sửa/xóa cặp, chọn tỷ lệ, đổi app/đổi bên, fullscreen/restore,
Home/X và thanh điều khiển ẩn/hiện. Popup thuộc THT Voice.

## Phương thức trong Cài đặt
| Phương thức | Cơ chế và giới hạn |
|---|---|
| ADB nội bộ (mặc định) | Tự kết nối ADB RSA TCP trên chính màn, tự mở bridge khi cần; tùy chọn chuẩn bị lúc boot |
| Off | Mở cửa sổ trực tiếp; cần freeform bật sẵn, đổi tỷ lệ bằng yêu cầu mở lại với bounds mới, không xác minh bố cục |
| Quyền hệ thống | Chỉ chạy nếu Android đã cấp MANAGE_ACTIVITY_STACKS; APK ký thường không tự có quyền |

Không cần cài app hỗ trợ bên ngoài. Không Telnet/Shizuku. Không tự bật adbd/root/flash OTA.
ADB mặc định127.0.0.1:5555; cổng chỉnh được. Đổi Wi-Fi không đổi loopback.
Lần đầu có thể cần xác nhận khóa ADB của app trên màn xe. ADB TLS pairing chưa hỗ trợ.

## Tích hợp THT Voice
[Hướng dẫn cho dự án THTVoice](docs/THTVOICE_INTEGRATION.md) có mẫu gửi lệnh và callback.
[API v2](docs/VOICE_API.md) có token hoặc permission signature nếu hai app cùng signer.
API phía Split đã có; repo Voice chưa được sửa trong mốc này. Không đổi khóa ký để ghép.

## Tài liệu tiếp tục
- [Bàn giao](docs/HANDOFF.md) và [prompt tiếp tục](docs/NEXT_AGENT_PROMPT.md).
- [Yêu cầu](docs/REQUIREMENTS.md), [kiến trúc](docs/ARCHITECTURE.md), [UI](docs/UI_SPEC.md).
- [Cài/thử xe](docs/DEVICE_TEST.md), [kiểm tra build](docs/VALIDATION.md), [nguồn](docs/REFERENCE_NOTES.md).
- [Thư viện và giấy phép](docs/THIRD_PARTY.md).

JDK17 / SDK35 / Gradle8.9 / AGP8.7.3:
```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
**Chưa thử EX2 thật.** Build/unit/lint không chứng minh bố cục và quyền của firmware xe.
CI build debug; chữ ký runner có thể khác bản test máy phát triển. Cần khóa ký ổn định
ngoài git trước phát hành chính thức. Không ghi token/keystore/khóa ADB vào repo.
