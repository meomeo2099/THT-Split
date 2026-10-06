# Nguồn tham khảo và giới hạn

## Launcher/Worker0.3.5
Mẫu icon đã đọc pixels: "THT chia đôi trên nền xanh đêm.png" (04/10 15:31 UTC)
và "THT Split ô tô, giao diện gọn.png". Icon mới dựng vector từ mẫu THT/vạch chia,
không dùng ảnh icon hai ô trống ban đầu. Adaptive icon hai layer108dp, phần
chính đặt trong vùng an toàn theo Android:
https://developer.android.com/develop/ui/compose/system/icon_design_adaptive
Cloudflare Wrangler exports/CONTROL SQLite cho Worker hiện có:
https://developers.cloudflare.com/workers/wrangler/configuration/
https://developers.cloudflare.com/durable-objects/get-started/

## GSplit
https://github.com/Salat39/GSplit
Đã đọc nguồn tại commit `b85dacc02103e91fe9d35b77fc1e6ebc9c5a66ec`.
- Freeform dùng windowing mode 5, launch bounds và shell resize task.
- Kết nối quyền shell có ADB/Telnet/Shizuku. Bản Geely không chứng minh mọi firmware EX2 chạy.
- Native split experimental qua Accessibility; no-caption/no-restart có điều kiện phiên bản Android.
- THT triển khai nguồn riêng. Không copy source GSplit: checkout tham khảo không thấy LICENSE.

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

## Thiết kế backend THT Split
- Native Android 9 dùng IActivityManager, windowing mode 3/4 và resizeDockedStack.
- Bridge app_process chạy dưới UID shell, gọi API bằng tên; không hard-code Binder transaction code.
- Không yêu cầu chữ ký platform, không sao chép mã decompile.
- Khoảng chừa mặc định trên 72px/dưới 112px là cấu hình thử nghiệm; phải đo và chỉnh theo firmware xe thực tế.

## Bổ sung 05/10/2026
- Off tham khảo hành vi GSplit: ActivityOptions + launch windowing mode/bounds;
  không có helper không có nghĩa app được cấp quyền shell. THT không copy source GSplit.
- ADB wire/RSA tham khảo giao thức AOSP; client được viết riêng, có kiểm tra framing
  và RSA Android public key/challenge.
  https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/main/OVERVIEW.TXT
  https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/main/crypto/rsa_2048_key.cpp
- Scoped hidden API library LSPosed v6.1 được tích hợp dưới Apache-2.0;
  xem THIRD_PARTY.md. Thư viện không cấp permission quản lý task hoặc chữ ký platform.

## Yêu cầu bounds Off0.2.1
AOSP Android9 ActivityStarter.updateBounds áp dụng bounds qua stack/configuration khi
launch cho phép; không suy ra mọi lần reuse task trên firmware đều đổi được.
Nguồn: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r54/services/core/java/com/android/server/am/ActivityStarter.java
ActivityOptions.setLaunchBounds có thể bị bỏ qua khi hệ thống không hỗ trợ cửa sổ tự do.
Nguồn: https://android.googlesource.com/platform/frameworks/base/+/HEAD/core/java/android/app/ActivityOptions.java

## Native và task reuse0.2.6
AOSP Android9 ActivityManagerService: setTaskWindowingMode đổi stack chứa task;
mode3 gọi split primary với showRecents=true. Bản0.2.6 isolate task qua
createStackOnDisplay/moveTaskToStack, gọi primary với showRecents=false và focus
app phải cuối cùng. Không suy mode3/4 là app đã vẽ đúng.
ActivityStarter: FLAG_ACTIVITY_LAUNCH_ADJACENT có nhánh dùng stack đang focus;
Off bỏ flag này, giữ NEW_TASK/RESET_TASK_IF_NEEDED để reuse, không MULTIPLE_TASK.
Nguồn: https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r54/services/core/java/com/android/server/am/ActivityManagerService.java
https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-9.0.0_r54/services/core/java/com/android/server/am/ActivityStarter.java

## Conscrypt / khóa ADB Android9
Log LDPlayer do người dùng cung cấp: provider trả OpenSSLRSAPrivateKey, không có
RSAPrivateCrtKey; cast cũ ném ClassCastException trước kết nối mạng.
Nguồn API chuẩn kiểm tra khi sửa:
https://android.googlesource.com/platform/external/conscrypt/+/refs/tags/android-9.0.0_r54/common/src/main/java/org/conscrypt/OpenSSLRSAKeyFactory.java
https://android.googlesource.com/platform/external/conscrypt/+/refs/tags/android-9.0.0_r54/common/src/main/java/org/conscrypt/OpenSSLRSAKeyPairGenerator.java
KeyFactory hỗ trợ RSAPrivateKeySpec cho key không có CRT; generator mặc định e65537.
THT không import private key ngoài app; sinh mới dùng RSAKeyGenParameterSpec2048/F4.

## Dialog nền rõ0.3.4
Android AlertDialog.Builder: create không show; show gọi create rồi show. Set
window background trong theme và window thật tại create dùng cho cả hai đường,
không thay OnShowListener riêng của form validation. setInverseBackgroundForced
không dành cho Material; nguồn API chỉ dẫn windowBackground trong theme.
https://developer.android.com/reference/android/app/AlertDialog.Builder
https://android.googlesource.com/platform/prebuilts/fullsdk/sources/android-29/+/refs/heads/main/android/app/AlertDialog.java
Ảnh của người dùng là bằng chứng lớp nền chưa tách rõ; không kết luận OEM hay
LDPlayer là nguyên nhân duy nhất. Sửa nền/insets/style cần kiểm ảnh thiết bị tiếp.
