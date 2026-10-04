# Nguồn tham khảo và giới hạn

## GSplit
https://github.com/Salat39/GSplit
Đã đọc nguồn tại commit `b85dacc02103e91fe9d35b77fc1e6ebc9c5a66ec`.
- Freeform dùng windowing mode 5, launch bounds và shell resize task.
- Kết nối quyền shell có ADB/Telnet/Shizuku. Bản Geely không chứng minh mọi firmware EX2 chạy.
- Native split experimental qua Accessibility; no-caption/no-restart có điều kiện phiên bản Android.
- THT triển khai nguồn riêng. Không copy source GSplit: checkout tham khảo không thấy LICENSE.

## EX2VNControl 1.3.2 và repo public
https://github.com/liemnguyen2107-coder/geely-ex2-vietnam-xe-choi
Repo public chủ yếu tài liệu OTA, không phải đầy đủ mã nguồn app.
APK đã phân tích trong cuộc trao đổi này: NativeSplit dùng IActivityManager, mode 3/4,
setTaskWindowingModeSplitScreenPrimary, resizeDockedStack và overlay kéo vạch riêng.
Một số bản có system shared UID/platform signature; APK ký thường không tự có quyền đó.
THT không copy mã decompile; dùng app_process chạy dưới uid shell để gọi API bằng tên,
không hard-code transaction code binder, không yêu cầu chữ ký platform.
APK tham khảo chừa trên 72px/dưới 112px; đây là mặc định thử nghiệm, chưa đo firmware người dùng.

## Video người dùng gửi
- Chia đôi màn hình cho Geely Ex2.mp4: chọn app hai bên, ratio presets, kéo divider, bản đồ + YouTube.
- Nâng cấp trợ lý ảo và Việt hóa… Coolray: voice mở cặp Vietmap/YouTube hoặc Maps/YouTube;
  điều hòa và tìm đường thuộc Voice, không phải Split.
Chỉ phân tích hình ảnh video; không có transcript âm thanh được kiểm chứng.

## AOSP Android 9 (nguồn gốc API)
https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r1/services/core/java/com/android/server/am/ActivityManagerShellCommand.java
https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r1/services/core/java/com/android/server/am/ActivityManagerService.java
- am start --windowingMode, am task resize, am stack resize-docked-stack là cú pháp Android 9.
- resizeTask yêu cầu MANAGE_ACTIVITY_STACKS; setTaskWindowingMode cũng cần quyền quản lý task.
- API OEM có thể đổi/bị khóa. Cần kiểm tra trên xe; build thành công không chứng minh hỗ trợ.
