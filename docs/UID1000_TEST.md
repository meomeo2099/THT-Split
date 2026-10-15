# THT Split 0.3.18 — thử UID 1000 (không phải bản chính thức)

Đây là bản **thử riêng**, chỉ dành cho người sở hữu màn Geely EX2 Android 9
và đã xác minh chứng thư platform thật. Không phát hành vào kênh cập nhật
thông thường; không sửa Worker, Secrets, các bản Release cũ hoặc firmware.

- Package: `com.tht.split` (giữ gốc theo yêu cầu).
- versionCode: 30; versionName: `0.3.18-uid1000-test1`.
- Android manifest **chỉ** của biến thể `platformSystem` khai báo
  `android:sharedUserId="android.uid.system"`.
- Chữ ký pin SHA256: `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`.
- Workflow lấy cặp khóa **AOSP Android 9 công khai** tương ứng trên runner,
  kiểm chứng thư rồi ký/verify APK. Không lấy mật khẩu hay lưu JKS vào git.
- BuildConfig.PLATFORM_TEST=true: ép phương thức system, không fallback Bridge.
  BuildConfig.PLATFORM_RELEASE=false, CONTROL_URL rỗng: **không** nhận OTA/
  min-version/khóa chung từ Worker trong bản thử; chỉ hiện chẩn đoán và xuất log.
- Giữ nguyên code/UI Java native, giao diện điều khiển và Voice receiver.
- APK cùng tên package với bản cũ, **không cài song song**.

## Trước khi cài

**Rủi ro:** UID 1000 chia quyền với tiến trình hệ thống. Trên xe không dùng
cặp khóa AOSP này làm platform signer, cài có thể báo
`INSTALL_FAILED_SHARED_USER_INCOMPATIBLE`. Cùng fingerprint AOSP của APK
không có nghĩa nó khớp chứng thư của xe. AOSP platform test key là **khóa
công khai**, không an toàn để dùng như bí mật/chữ ký sản phẩm thương mại.

1. Chưa gỡ bản đang hoạt động khi chưa xuất backup cặp app/cấu hình từ trong
   THT Split; sao lưu ra ngoài thư mục app. Token Voice có thể phải nhập lại.
2. Trước khi quyết định cài, so SHA-256 chứng thư firmware với chứng thư APK.
   `adb shell pm path android` thường trả đường dẫn `framework-res.apk`;
   kéo về máy kiểm chứng thư bằng `apksigner verify --print-certs` nếu khả dụng.
3. Dù cùng certificate với APK `0.3.17-platform`, **thêm sharedUserId là
   thay đổi danh tính cài đặt Android**, nên cập nhật đè có thể bị từ chối;
   không tự xóa dữ liệu để vượt lỗi. Sau khi backup và chấp nhận mất data,
   mới thử gỡ/cài thủ công.
4. Không bật chế độ cập nhật bắt buộc, không sửa SELinux/root hay framework.

## Kiểm tra trên xe sau khi cài

```sh
adb shell dumpsys package com.tht.split | grep -E 'userId=|sharedUserId|MANAGE_ACTIVITY_STACKS'
adb shell ps -A -o USER,PID,NAME | grep com.tht.split
```

Ở Windows thay `grep` bằng `findstr`. Phải thấy package được hệ thống gán
**userId=1000**, không chỉ thấy chữ ký hợp lệ. Trong app mở Cập nhật/Chẩn đoán:
xác nhận permission `MANAGE_ACTIVITY_STACKS` được cấp và backend system
trả kết quả thực. Thử chia, kéo tỷ lệ, đổi bên, fullscreen/restore, sau đó
khởi động lại màn và thử lần nữa. Nếu lỗi hãy xuất log; **CI không kiểm được
thiết bị thật**.

Không dùng kết quả này để coi mọi xe EX2/firmware được hỗ trợ. Nếu không nhận
UID 1000 hoặc quyền hệ thống, quay về APK/backup đã lưu. Không thay đổi kênh
update của bản thường cho tới khi người dùng xác nhận test.
