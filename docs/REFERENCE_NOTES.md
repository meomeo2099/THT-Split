# Nguồn tham khảo và giới hạn

## Failed transaction/root ADB —10/10
AOSP cmd.cpp gọi IBinder::shellCommand với stdin/stdout/stderr và báo
FAILED_TRANSACTION; mã lỗi này không chứng minh missing permission.
AOSP system_server policy cho read/write shell_data_file qua fd đã mở,
khác fifo tạo bởi domain su. Firmware user/test-keys có thể khác policy.
https://android.googlesource.com/platform/frameworks/native/+/baa86314946d66f7323443f562e5f66e898e37b2/cmds/cmd/cmd.cpp
https://android.googlesource.com/platform/system/sepolicy/+/refs/heads/android11-dev/private/system_server.te
File thực người dùng chỉ xác nhận phiên ADB root/su, chưa có AVC hoặc context
Bridge sau reboot. File redirect là sửa tương thích để thử, không khẳng định
đã chứng minh nguyên nhân hoặc hết lỗi trên xe. Không thay SELinux policy.


## Tay nắm native Android9 —07/10
Đã đọc AOSP mirror tag android-9.0.0_r54: docked_stack_divider.xml khai báo
DividerHandleView, landscape DockedDividerHandle gravity=center_vertical;
onDraw vẽ pill ở giữa view. DividerWindowManager dùng TYPE_DOCK_DIVIDER riêng,
WindowManagerPolicy đặt nó ở APPLICATION_LAYER trong AOSP9. Overlay không
thay đổi resource/view SystemUI; đây là cách ghép hình và giữ điều khiển THT.
https://github.com/aosp-mirror/platform_frameworks_base/blob/android-9.0.0_r54/packages/SystemUI/res/values-land/styles.xml
https://github.com/aosp-mirror/platform_frameworks_base/blob/android-9.0.0_r54/packages/SystemUI/src/com/android/systemui/stackdivider/DividerHandleView.java
https://github.com/aosp-mirror/platform_frameworks_base/blob/android-9.0.0_r54/packages/SystemUI/src/com/android/systemui/stackdivider/DividerWindowManager.java
OEM/Android khác có thể đổi tâm/layer; cần so ảnh/bounds thật, không hứa
trùng tuyệt đối hoặc thay SystemUI chỉ bằng quyền shell.


## GitHub private Releases và Worker06/10
API Releases/asset hỗ trợ token Contents: Read; download asset trả200 hoặc302,
không chuyển Authorization sang CDN. releases/latest bỏ prerelease nên dùng
danh sách releases và lọc channel rõ ràng. Worker Secret khác Variable và chỉ
được đọc ở runtime env, không nhúng trong client.
https://docs.github.com/en/rest/releases/releases
https://docs.github.com/en/rest/releases/assets
https://developers.cloudflare.com/workers/configuration/secrets/
https://developers.cloudflare.com/workers/runtime-apis/fetch/

## Vòng đời shell Android9 và rollback0.3.8
AOSP classic shell không shell-protocol dùng PTY dù yêu cầu raw; đóng master
PTY gửi SIGHUP. nohup cần được thực thi trước khi shell đóng; vì vậy sleep giữ
remote shell mở khác với polling ở app sau close. Race cụ thể trên LDPlayer9
là suy luận từ nguồn và regression báo bởi người dùng, chưa có log chứng minh.
https://android.googlesource.com/platform/system/core/+/refs/tags/android-9.0.0_r1/adb/shell_service.cpp
Nguồn0.3.4 đối chiếu: commit27bbd74229c75ee42393c519a493c1fcf1fab77d.

## Chẩn đoán socket0.3.7
AOSP IoBridge isConnected đọc SO_ERROR, ECONNREFUSED được bọc ConnectException;
FailureText trước chỉ hiện ErrnoException sâu nhất nên mất endpoint/giai đoạn.
https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/io/IoBridge.java
Không suy ra cổng ADB đang mở chỉ vì ADB máy tính kết nối được; giữ endpoint
nội bộ chỉnh được và không dò các host/cổng khác.

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
