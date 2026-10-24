# THT Split 0.3.18-platform — thử cập nhật đè 0.3.17-platform

**Mục tiêu:** kiểm tra cập nhật giữ dữ liệu/cặp ứng dụng/thiết lập trên màn Geely EX2,
không chuyển UID, không gỡ/cài lại, không đụng khách hàng của kênh Legacy.

| Thông tin | Bản cũ | Bản mới |
| --- | --- | --- |
| Package | com.tht.split | com.tht.split |
| versionCode | 29 | 30 |
| versionName | 0.3.17-platform | 0.3.18-platform |
| SHA-256 chứng thư ký | c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8 | giống hệt |
| sharedUserId | không có | không có |

- Bản này **KHÔNG phải** test `android.uid.system`/UID 1000. Bản UID1000
  `0.3.18-uid1000-test1` nằm trong release riêng; không dùng nó làm nguồn cập nhật.
- Giữ `BuildConfig.PLATFORM_RELEASE=true`, `PLATFORM_TEST=true`, app native
  và backend system trực tiếp có kiểm tra quyền, Worker origin hiện có.
- Không thêm/sửa tính năng chia màn, Voice receiver, key, Bridge hay cấu hình firmware.
- Chữ ký dùng AOSP Android 9 **test key công khai** được pin ở CI để giữ tương thích
  với `0.3.17-platform`; không phải chứng thư riêng an toàn cho sản phẩm thương mại.
- APK không khai báo UID 1000; chữ ký platform khớp cert firmware nếu được xác minh
  nhưng không đồng nghĩa Android cấp UID 1000.
- Bản phát hành `v0.3.18-platform` là pre-release, gồm APK, checksum,
  `update.json` tạo từ APK đã ký và hướng dẫn này.

## Test cập nhật trực tiếp và bảo toàn dữ liệu

1. Trên xe phải đang có **đúng bản 0.3.17-platform** với package
   `com.tht.split` và cùng SHA chứng thư. Sao lưu preset/cặp ứng dụng ra bên
   ngoài ứng dụng để có đường lui; không gỡ app.
2. Ghi lại cấu hình/preset và trạng thái kết nối, thử chia hai ứng dụng.
3. Cài đè bản mới. Nếu dùng Windows PowerShell trong thư mục chứa APK:

   ```powershell
   adb install -r .\THT-Split.apk
   adb shell dumpsys package com.tht.split | findstr /I "versionCode versionName userId"
   ```

4. Đối chiếu versionCode 30, versionName 0.3.18-platform; mở THT Split, kiểm
   cặp/preset/token cũ vẫn còn. Thử chia, đổi bên, kéo 30/50/70, full/restore.
   Khởi động lại màn và kiểm tra lại.
5. Nếu gặp `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, **không tự gỡ hoặc xóa dữ liệu**.
   Xuất log và kiểm lại chữ ký APK đang cài, package, userId và khóa ký.
   Không khẳng định cài đè thành công chỉ vì CI build xanh.

## Thử cập nhật qua Cloudflare Worker

Bản mới có `update.json` nhưng **không tự động xuất hiện trong ứng dụng**:
Worker tại `chiamanhinh.mavoice.workers.dev` đang pin SHA chứng thư Legacy
`e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2`
trong repo. Để kiểm kênh platform, cần cấu hình Worker có allowlist hai chứng
thư nếu code thực sự hỗ trợ, và bật nhận pre-release/đảm bảo Release mới
được chọn. **Không thay đổi Worker sản xuất ở lần build này**, không cập nhật
bắt buộc hoặc tăng minimumVersionCode; việc thay pin đơn có thể làm bản Legacy
mất cập nhật. Chỉ triển khai OTA sau khi kiểm nghiệm với thiết bị thật.

## CI kiểm chứng

GitHub Actions chạy `testPlatformReleaseUnitTest`, `lintPlatformRelease`,
`assemblePlatformRelease`, kiểm chứng thư AOSP được pin, `apksigner verify`,
`aapt dump badging` xác nhận package/code/name, manifest không có sharedUserId,
không debuggable, tạo checksum và update.json từ APK ký thực.
CI **không chứng minh** cập nhật đè/giữ dữ liệu hoặc quyền trên firmware xe.
