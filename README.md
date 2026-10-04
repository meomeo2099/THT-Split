# THT Split

Ứng dụng Android độc lập cho Geely EX2, package `com.tht.split`. Bản đầu `0.1.0-test`.
Không cần cài ứng dụng chia màn hình bên ngoài. Không cần root; điều khiển task cần bridge
ADB riêng chạy bằng quyền shell. ADB của xe phải được mở sẵn.

## Chức năng đã có mã
- Chọn app trái/phải, tỷ lệ 30:70–70:30, kéo vạch và cập nhật khi thả.
- Đổi từng app, đổi bên, toàn màn hình/khôi phục, Home và X tắt chia.
- Thanh điều khiển nhỏ; nút ẩn riêng, tự ẩn; nút nổi để hiện lại.
- Cặp đã lưu: tạo, mở, sửa tên/app/tỷ lệ, xóa có xác nhận.
- Giao diện tiếng Việt navy/cyan, icon THT có vạch chia giữa.
- Cài đặt khoảng chừa OEM, thanh điều khiển, Voice API, kết nối, cập nhật và log.
- API nhận lệnh THT Voice có token; popup nghe do Voice quản lý.

Native mode 3/4 và freeform mode5 đều có backend. **Chưa xác nhận chia trên EX2 thật.**
Build/unit/lint chỉ xác nhận chương trình được đóng gói và một số logic đã kiểm tra.

## Người tiếp tục công việc
Đọc [AGENTS.md](AGENTS.md), rồi [HANDOFF](docs/HANDOFF.md).
Có thể gửi nguyên [prompt bàn giao](docs/NEXT_AGENT_PROMPT.md) cho người/agent khác.

| Tài liệu | Nội dung |
|---|---|
| [Yêu cầu](docs/REQUIREMENTS.md) | Quyết định đã chốt |
| [Kiến trúc và sơ đồ](docs/ARCHITECTURE.md) | Luồng lệnh, module, mốc triển khai |
| [Nguồn tham khảo](docs/REFERENCE_NOTES.md) | GSplit, hai video, AOSP |
| [UI](docs/UI_SPEC.md) | Giao diện và hành vi nút |
| [Voice API](docs/VOICE_API.md) | Token, extras, lệnh và tích hợp |
| [Cài và test EX2](docs/DEVICE_TEST.md) | Quyền, bridge, ma trận kiểm thử và log |
| [Kết quả kiểm tra](docs/VALIDATION.md) | Bằng chứng build/unit/lint/CI |

## Build và cài bản test
JDK 17, Android SDK 35 và Gradle Wrapper8.9:
```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Windows dùng `gradlew.bat`. Sau cài, cấp quyền nổi, lấy lệnh bridge riêng trong app
và thực hiện [DEVICE_TEST](docs/DEVICE_TEST.md). Bridge cần chạy lại sau reboot/cập nhật.
Actions tự build khi push main và tạo artifact `THT-Split-test-apk`, giữ 7 ngày.
APK CI ký debug của runner; chưa có chuỗi ký phát hành cố định để cập nhật đè mọi bản test.
Cập nhật trong app hiện kiểm tra/mở trang Releases; chưa tự tải và cài thay thế.

Không đưa token, keystore hoặc mã decompile app tham khảo vào git.
