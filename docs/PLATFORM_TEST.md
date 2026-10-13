# THT Split Platform Test 1 —13/10/2026

APK thử ký đúng chứng thư AOSP platform test key SHA256
`c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`.
Nguồn khóa thử công khai: AOSP platform/build tag android-9.0.0_r1,
target/product/security/platform.pk8 và platform.x509.pem. Chỉ tải tạm trong CI,
kiểm hash chứng thư, không đưa khóa vào git/APK. Đây không phải khóa riêng của Geely.

## Cài và thử trên xe
1. Tải THT-Split-Platform-Test.apk và cài như APK thường hoặc:
   `adb install -r THT-Split-Platform-Test.apk`.
2. Bản riêng `com.tht.split.platformtest`, tên THT Split Platform Test.
   Không cần gỡ bản com.tht.split; dữ liệu/cặp/token không tự chuyển sang bản thử.
3. Cấp quyền hiển thị nổi trong Cài đặt hoặc:
   `adb shell appops set com.tht.split.platformtest SYSTEM_ALERT_WINDOW allow`.
4. Kết nối & quyền hiện đối chiếu chứng chỉ APK với package android, UID và
   MANAGE_ACTIVITY_STACKS. Ghi/chụp kết quả TRƯỚC khi dùng pm grant, để biết
   chữ ký có tự cấp quyền hay không. Nếu CHƯA CẤP thì dừng thử chia và gửi kết quả.
5. Bản thử chỉ chọn Quyền hệ thống, không tự fallback ADB/Off. Khi ĐÃ CẤP:
   Kiểm tra kết nối → chọn hai app → chia, đổi bên/tỷ lệ/fullscreen/khôi phục.
6. Reboot màn thật, mở đúng bản Platform Test và thử lại cùng cặp. Không cần
   bootstrap Bridge. Test chỉ đạt khi hai app/bounds thực đúng sau reboot.
7. Nếu lỗi, Cập nhật → Xuất log hỗ trợ, gửi kèm ảnh quyền và thao tác.

Không sharedUserId/system UID, không sửa firmware/services.jar/SELinux hoặc
thay đổi chữ ký bản thường. Cài được không chứng minh quyền đã được cấp.
Chứng chỉ khớp trên một xe không bảo đảm xe/firmware khác. Khóa thử công khai
không dùng như khóa bí mật của bản thương mại.

## Kết nối Voice cho bản thử
Chưa sửa repo THTVoice. Voice cần gửi explicit component với package
`com.tht.split.platformtest`, class `com.tht.split.SignatureVoiceReceiver`.
Quyền yêu cầu phía Voice: `com.tht.split.platformtest.permission.CONTROL_SPLIT`.
Action vẫn `com.tht.split.COMMAND`. Chỉ Voice cùng signer và được cấp quyền mới
dùng đường signature không token. Voice khác signer dùng class
`com.tht.split.VoiceReceiver` và token riêng của bản thử, bật nhận lệnh trước.
Không gửi broadcast action chung không chỉ định package khi cài song song.

## Phát hành
Release riêng platform-test-0.3.16-1, không có update.json; Worker bản thường
không chọn release này. Bản thử không nhận cập nhật từ Worker của bản thường.
CI kiểm unit/lint/build, apksigner và package/permission trước publish. Chưa
có thử cài/quyền/Binder/gesture trên xe khi build; cần chủ dự án xác nhận.
