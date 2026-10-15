# THT Split 0.3.17-platform — package gốc, quyền hệ thống

Package: `com.tht.split`, versionCode29, versionName `0.3.17-platform`.
Giao diện Java/Android native, không WebView. APK Release không debuggable,
ký bằng chứng thư platform đã đối chiếu với hai tệp chủ dự án gửi15/10:

`c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`.

Hai tệp khớp nhau, trùng AOSP platform test key công khai. CI lấy đúng cặp
khóa thử từ AOSP android-9.0.0_r1 tạm thời, kiểm chứng thư trước ký và kiểm
APK sau ký. Không đưa khóa vào git/APK hoặc thay Secrets của bản ký tht-split.

## Chuyển từ bản cũ
- Bản thường com.tht.split ký tht-split (e7874f...) khác chữ ký. Không cài đè
  được. Trước khi tự gỡ, vào Sao lưu xuất cấu hình/cặp ra tệp bên ngoài app.
  Gỡ bản thường rồi cài THT-Split.apk, nhập bản sao lưu. App không tự gỡ/xóa data.
- File sao lưu không chứa token Voice/Bridge hoặc khóa RSA. Sau cài mới cần
  lấy token Voice mới và cập nhật phía Voice nếu đang dùng cơ chế token.
- Bản thử com.tht.split.platformtest là package khác, có thể giữ hoặc tự gỡ.
  Bản package gốc không cập nhật đè hay tự nhập dữ liệu của bản thử này.
- Nếu com.tht.split đang ký đúng cùng chứng thư platform này, dùng cài đè
  `adb install -r THT-Split.apk`; versionCode mới29.

## Cài và kiểm trên xe
1. Cài APK, mở THT Split và cấp quyền hiển thị nổi.
2. Kết nối & quyền: xem chứng chỉ trùng hệ thống và MANAGE_ACTIVITY_STACKS ĐÃ CẤP.
3. Kiểm tra kết nối phải báo Quyền hệ thống sẵn sàng, không Bridge v7/pipe.
4. Chọn hai app rồi chia, đổi bên/tỷ lệ/toàn màn/khôi phục; reboot màn thật rồi
   mở cặp lại. Không cần ADB/Bridge để thao tác khi quyền đã được Android cấp.
5. Nếu CHƯA CẤP hoặc lỗi: xuất log/chụp kết quả; APK không tự fallback phương thức.

Ảnh13/10 đã xác nhận bản thử trùng cert/được cấp quyền trên một màn Android9,
UID10036/context platform_app. Chưa có xác nhận đầy đủ chia/reboot của bản mới.
Không thêm sharedUserId/system UID, không sửa firmware/SELinux/framework.

## Voice và cập nhật
- Package gốc và action `com.tht.split.COMMAND` giữ nguyên. Bật nhận lệnh trong
  cài đặt. Token có thể đổi khi gỡ/cài; phía Voice cần nhập lại đúng token.
- Signature API cần Voice cùng signer và khai báo quyền
  `com.tht.split.permission.CONTROL_SPLIT`; ký Split không tự ký lại Voice.
- Giữ Worker origin chiamanhinh.mavoice.workers.dev và kiểm khóa/minimum như
  bản thường. Chưa deploy/thay dữ liệu hoặc cấu hình Worker trong lần build này.
- Release có update.json tạo từ APK thực, signer pin mới. Nếu muốn dùng OTA
  nhánh platform, đổi RELEASE_CERT_SHA256 trên Worker sang SHA platform ở trên,
  giữ GITHUB_REPO/ADMIN_TOKEN/CONTROL và dữ liệu, cho nhận prerelease rồi kiểm
  GitHub ngay. Bản ký cũ không thể tự cập nhật qua đổi signer: phải chuyển
  thủ công một lần như hướng dẫn. Không bỏ kiểm chữ ký/hash/package.
- Chưa bật bắt buộc cập nhật hoặc tăng minimumVersionCode. APK sau này phải
  giữ cùng package và cùng signer platform để cập nhật đè.
