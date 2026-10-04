# THT Split

Ứng dụng Android độc lập cho Geely EX2: chọn 2 app, đổi tỷ lệ/kéo divider, cặp đã lưu, toolbar ẩn/hiện và API điều khiển từ THT Voice. Package `com.tht.split`.

Không phụ thuộc GSplit APK hoặc EX2VNControl. Quyền điều khiển task dùng bridge ADB riêng, không cần root; ADB phải được mở sẵn.

- [Bàn giao](docs/HANDOFF.md)
- [Yêu cầu đã chốt](docs/REQUIREMENTS.md)
- [Tài liệu nguồn](docs/REFERENCE_NOTES.md)

Build: JDK 17, Android SDK 35, `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
APK test: `app/build/outputs/apk/debug/app-debug.apk`. Chưa kiểm thử xe thật.
