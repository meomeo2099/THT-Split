# Kiểm tra 0.2.0-test — 05/10/2026

## 0.3.7-test — ADB phản hồi ngay và liên hệ06/10
Code fe384fcf725e0ff722708c53e619d0de5af97903; versionCode19.
CI37423152988/job112136801691 completed/success:
https://github.com/meomeo2099/THT-Split/actions/runs/37423152988
Android unit/lint/assemble đạt; Node8 tests, QR ngân hàng/contact và Wrangler
 dry-run đạt. Hai regression tests mới: context/endpoint còn khi cause lồng
ECONNREFUSED; secret trong cả context/cause được ẩn. QR Zalo decode gốc và
kích thước nhỏ136px cùng payload; không sửa QR hoặc dùng URL QR để gửi mạng.

APK SHA256: 34f391be0151730973566cfea121842c926480b84928b332883a7fb955b6735e
Cert SHA256: 0a7da9cdbb44034105fdff4c19ff6ed585d4deb253785acfd92f48321aa3f9b9
apksigner đạt; aapt package com.tht.split/code19/0.3.7-test/min26/target32.
Asset contact-zalo.png byte-identical ảnh chủ dự án gửi, SHA256:
2e7c31d9cecc61f05bc7ef5aaece643121bcd21a062ad5897146603379a025a6
QR MB và launcher foreground/background APK byte-identical0.3.6. Quét toàn bộ
dex có FB/contact asset/networkIo/ConnectionFailure/Worker origin/api/config;
không api/device/android_id/deviceId/offlineSeconds. Worker/policy không đổi.
Signer CI khác0.3.6 nên không cài đè bản đó. Không tự gỡ app để giữ dữ liệu.

Không thử UI/ADB trên LDPlayer/xe: không khẳng định ECONNREFUSED trên máy người
dùng đã hết. Kết nối TCP/RSA/timeouts, bootstrap chỉ khi ConnectException,
Bridge6/UID/token/Off/session/task/Voice giữ nguyên; chỉ tách hàng chờ mạng,
phản hồi tiến trình/dedup và giữ context lỗi. Cổng thực tế vẫn cần bật/chọn đúng.

## 0.3.6-test — bỏ đăng ký, dùng ngay06/10
Code c36f6f0341c7339b5b408cd2f3ebd26cdd2bc25c; versionCode18.
CI37421410376/job112131367276 completed/success:
https://github.com/meomeo2099/THT-Split/actions/runs/37421410376
Android unit/lint/assemble đạt; Node8 tests local/CI, QR và Wrangler dry-run đạt.
Public GET không auth/body, không ghi/sinh records. APIs enrollment/admin-device/
reenroll cũ410. Admin secret vẫn chặn sửa config, public GET không bỏ khóa chung.
Legacy device records không dùng và không xóa; lease/autoApprove không vào response.
Java4 regression tests: dùng ngay thiếu config/Worker, không lease/đăng ký, global
lock/minversion đã nhận vẫn chặn và Stop/Home/Hide luôn thoát được.

APK SHA256: c953819ff31dd6af1298500e4bc59ff99df5014897fc1bfce19eaab7ed40f344
Cert SHA256: 2dc79be82a285e6181f93aa3ad3fa8f172e2c9f00335064640aa9af827ff4e36
apksigner verify đạt; aapt com.tht.split/code18/0.3.6-test/min26/target32.
Quét toàn bộ dex: origin đúng/GET api/config, không api/device/android_id/deviceId/
offlineSeconds/commandDenial. Icon foreground/background và QR APK byte-identical
0.3.5. Worker ZIP SHA256: 2b552e3e1c50507a8f8d44592fd5f931a99b77ec99e1eeb0c5d34cc46475cc06
Gói Worker có README/config/module, không có key/admin/signing credentials.

Static scope: backend/Bridge6/RSA/Voice/Store/SplitService/SplitEngine/overlay/Ui/
AppUi/icon/QR và ApkUpdates kiểm APK giữ nguyên0.3.5. RemoteControl namespace mới
tht_public_policy không đọc khóa/lease per-device cũ và require không gọi mạng.
Chưa chạy APK trên LDPlayer/xe hoặc deploy Cloudflare; các kết quả trên không
chứng minh trạng thái surface, quyền firmware hay layout thực trên xe.
Signer test khác0.3.5, không cài đè được bản đó. Keystore ổn định vẫn chưa cấu hình.

## 0.3.5-test — Worker thật / icon / giảm chờ06/10
Code4f1b98dac048e9a5e6bbbed5f292a639935ba699; versionCode17.
CI37420223825/job112127693172 completed/success:
https://github.com/meomeo2099/THT-Split/actions/runs/37420223825
Android testDebugUnitTest/lintDebug/assembleDebug đạt; Node9 tests đạt (local/CI),
QR/profile/CRC và Wrangler deploy --dry-run đạt, không deploy tài khoản thật.
Tests cache: cache hợp lệ không gọi mạng; unknown/hết lease chỉ chạy sau refresh;
lỗi mạng không bỏ khóa/min version/unknown/hết hạn; Stop/Home/Hide vẫn thoát.
Worker health không lộ secret, thiếu admin/storage503; module đóng gói giữ lock.

APK SHA256: 1f76a4c28fc68a491df246dd2ce9153e9566e6cb0ce7689d2e383e0587510598
Cert SHA256: 024e0a7c6d349cf251d11f92cb9fe566c6fe829bf0f948ef0638d50ccb794249
apksigner verify đạt; aapt package com.tht.split/code17/0.3.5-test/min26/target32.
Quét toàn bộ6 dex tìm đúng origin chiamanhinh.mavoice.workers.dev; resource
adaptive trỏ launcher_background/foreground mới, bytes QR giữ nguyên repo.
Đã xem vector preview mặt vuông/tròn theo mẫu THT; chưa xem launcher Android.
Worker ZIP SHA256: 2fcd101f4629b5bb31246453de1c121daf4c824dcff7c1bc0b6e67c749d22859
Gói3 tệp worker.mjs/wrangler.jsonc/README, không có admin/signing/CF credentials.

Static scope: ShellServer v6/ShellClient/LocalAdb/PairController/DirectWindows/
TransportPolicy/Store/VoiceReceiver/DonationProfile/QR/Ui giữ nguyên0.3.4.
Bỏ1s chờ cố định, giữ≈4s PING budget; executor network riêng, ratio giữ overlay
window, bỏ UI render/component lookup dư. Chưa đo benchmark hoặc thử LDPlayer/xe;
unit/layout check không chứng minh surface app đã vẽ xong hay mượt hơn thực tế.
Worker live hiện GET / và POST /api/device {} trả200 Hello World text/plain;
chưa deploy mã quản trị. Bản mới cần Worker đúng để đăng ký lần đầu. CI signer
khác0.3.4 nên không cài đè bản đó; chưa có chuỗi cập nhật cùng signer ổn định.

## Mốc0.3.4 — nền hộp thoại và chữ cài đặt,06/10/2026
Code27bbd74229c75ee42393c519a493c1fcf1fab77d; CI hoàn tất success:
https://github.com/meomeo2099/THT-Split/actions/runs/37345958636.
Build/unit/lint, Node tests, QR đúng profile/CRC, Wrangler dry-run đều đạt.
git diff --check đạt; Ui.Icon đối chiếu nguyên trạng. Service/engine/Store,
backend/bridge/core/remote/Voice/server/assets giữ nguyên. Tất cả dialog
callsites trong MainActivity/AppPicker/SplitService đi qua Ui.dialog/AppUi.dialog;
factory sửa opaque background/dim/width tại create không thay OnShowListener.
APK artifact11361025097: com.tht.split /code16 /0.3.4-test /min26 /target32;
apksigner verify thành công; QR asset trong APK trùng bytes repo. SHA256 APK:
`53069499ba8318652d1e42a787b675d7dec5b8b2f2d5792eafd5d08d48e0ee28`.
Cert SHA256 `48eac7f7f5b29b961fc18e6be36ae6ec6d4302b7c23f116a2bcbe8ea16ed968d`
khác0.3.3 đã gửi, không cài đè APK đó. Không tự gỡ app/xóa cấu hình để vượt signer.
Chưa có ảnh UI/emulator/xe của0.3.4; CI không chứng minh đã hết lỗi trên firmware.
Cần kiểm freeform dialog trong ảnh phản hồi và các dialog overlay/token/đổi token/
transport/ADB/px/edit/delete/kết quả/update/lỗi/picker/toolbar: nền đặc/viền/dim,
nút không lẫn phía sau; title/list/form đọc rõ, cuộn tới cuối, keyboard không giấu
Lưu/Hủy, nhập sai vẫn báo lỗi và không lưu; Hủy không đổi cấu hình. Kiểm dropdown
và nhãn dài ở từng tab/độ rộng/fontScale, toolbar trái/phải/grip giữ chức năng cũ.

## Mốc0.3.3 — nhóm nút trái/phải và Sửa/Xóa rõ hơn
Code e1dd80bad24ef73b447507c7bb49faf52fd97cc1; CI success:
https://github.com/meomeo2099/THT-Split/actions/runs/37343461485.
Build/unit/lint, Node tests, QR đúng profile/CRC và Wrangler dry-run đều đạt.
git diff --check đạt. SplitService chỉ đổi3 dòng hiển thị căn nhóm nút trong render;
đã đối chiếu xử lý onStartCommand và phần mini/grip/dialog toolbar giữ nguyên.
SplitEngine/Ui/Store/connection/bridge/core/remote/Voice/res/server không đổi;
không thay bounds/task/Bridge v6. Không bổ sung unit test mô phỏng style/layout.
APK artifact11358899820: com.tht.split /code15 /0.3.3-test /min26 /target32;
apksigner verify thành công. SHA256:
`2f3549fb93a3eaa120ee980374d1883587d19f558aca0ae44650f574f722ec17`.
QR trong APK trùng bytes repo, thông tin ngân hàng giữ nguyên.
Cert SHA256 `914b65e63a3fe8996f8559b426836ca1408ed50cb89a400baebc2d84c87c6d26`
khác0.3.2 đã gửi; không cài đè bản0.3.2 với signer đó. Không tự gỡ hoặc hứa giữ
token/RSA/config khi gỡ cài lại; cần khóa ký ổn định cho bản phát hành lâu dài.
Chưa chạy bản sửa trên LDPlayer/xe. Cần chọn trái/phải rồi ẩn/hiện lại toolbar,
kiểm đủ tỷ lệ/icon, X/ẩn/Home/Off lock, cặp Sửa → Lưu/Hủy, Xóa → Hủy/Xác nhận;
Hủy không đổi preset và Xóa không gỡ hai app. Kiểm tương phản/cắt chữ/vùng chạm.

## Mốc0.3.2 — giao diện bên trong và ủng hộ
Code9c62c0400a0f181c0ccf62f769cdbae6b3ebc9e7; CI hoàn tất success:
https://github.com/meomeo2099/THT-Split/actions/runs/37340679665.
Build/unit/lint, Node regression tests, QR đúng profile/CRC và Wrangler dry-run
đều đạt. Không deploy Worker. Máy phát triển không có zxingcpp ở mốc này;
kiểm decode/CRC là bằng chứng CI, không ghi thành test local đã đạt.
git diff --check đạt. Đối chiếu25 hàm xử lý trong MainActivity giữ nguyên sau
chuẩn hóa alias style Ui→AppUi. Diff từ0.3.1 chỉ MainActivity/AppUi/build version
và tài liệu; Ui/SplitService/Store/bridge/connection/core/remote/Voice/res/server
giữ nguyên. Đặc biệt toolbar và grip khi chia không dùng AppUi.
APK artifact11358341729: com.tht.split /code14 /0.3.2-test /min26 /target32;
apksigner verify thành công. SHA256 APK:
`73dbf90e5c431b4e5f09dc5dda20b6f7955e3f79181e2e55ebe862606730ffda`.
Ảnh assets/donation-qr.jpg trong APK trùng bytes repo và giữ nguyên từ0.3.1.
Cert SHA256 `a5e091df853a25e46aa22f79707c3d94ad5ecfef6b64b7b768b440aee3555155`
khác0.3.1; không cài đè được APK0.3.1 đã gửi nếu dùng chữ ký đó. Không tự gỡ
bản cũ hoặc nói sẽ giữ token/RSA/config khi phải gỡ cài lại.
Chưa chạy UI bản0.3.2 trên LDPlayer/xe. Cần thử từng danh mục cài đặt, chữ/dropdown,
chọn app/swap/ratio/lưu/sửa/xóa/mở cặp, copy STK và quét QR bằng app ngân hàng;
kiểm cuộn và bố cục khi DPI/độ rộng khác. Build không chứng minh pixels/xe.

## Máy phát triển
Đã chạy clean build rồi kiểm tra lại bản cuối:
`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` — BUILD SUCCESSFUL.
17 tests, 0 failures, 0 errors; lint 0 errors / 10 warnings.

11 bài trước kiểm tra geometry/preset bounds và parser task.
6 bài mới kiểm tra protocol ADB bằng fixture little-endian độc lập, checksum sai,
độ dài/magic sai, cấu trúc RSA2048 Android public key/Montgomery và chữ ký challenge
đã băm SHA1 (không băm lại). Không coi đây là test kết nối adbd thật.

Manifest: com.tht.split / versionCode2 / 0.2.0-test / min26 / target32.
Apksigner: v2 hợp lệ, một signer. Không thay khóa debug của máy phát triển.
Đã kiểm class_defs trong DEX: không đóng gói sun.misc.Unsafe stub compileOnly.
License LSPosed đầy đủ có trong assets APK.
SHA256 APK máy phát triển:
`85aa02946cee78d3d275c28945617d5fed877727844e402562d10a8da0497a02`

Lint ProtectedPermissions chỉ được giải thích/suppress ở permission MANAGE_ACTIVITY_STACKS:
backend system có guard runtime, không tự xin/cấp quyền cho APK thường. Không tắt lint
ProtectedPermissions toàn dự án. ExpiredTargetSdkVersion vẫn là ngoại lệ sideload đã có.

## Chưa kiểm tra trên Android thật
Chưa chạy UI/bridge/local ADB/Binder trên EX2 hoặc emulator/điện thoại.
Chưa xác minh boot/authorize RSA/insets/resize/fullscreen/Off/receiver callback thật.
Phía THTVoice chưa sửa client. Cần chạy DEVICE_TEST.md và THTVOICE_INTEGRATION.md.
Không gọi kết quả build hoặc success từ request launch là đã test xe thành công.

## GitHub Actions
Workflow main tiếp tục build/unit/lint và lưu THT-Split-test-apk 7 ngày.
Xem run tương ứng commit mới; run phiên bản0.1 không chứng minh phiên bản0.2.
APK CI debug có thể khác signer/checksum máy phát triển.

## Bản sửa0.2.1 sau phản hồi LDPlayer
Người dùng báo0.2.0 Off chia được trên LDPlayer; bản đó khóa tỷ lệ/không vạch kéo.
0.2.1 thêm yêu cầu launch bounds cho ratio/drag, vùng chạm48×80dp, màn chờ có
khoảng ổn định cấu hình. Chưa chạy bản sửa trên LDPlayer/xe; không xác nhận hết lag.
`git diff --check` đạt. Build local bị chặn do cache AGP8.7.3 không còn và không
truy cập kho phụ thuộc; cần xem workflow tương ứng commit0.2.1, không dùng17tests
bản0.2.0 làm kết quả build bản sửa. Package giữ nguyên, versionCode3.

CI bản0.2.1 đã đạt build/unit/lint tại commit d871475a25f725cfa381935d25dca40b2abbbfa8:
https://github.com/meomeo2099/THT-Split/actions/runs/37261177497
APK tải từ artifact: package com.tht.split, versionCode3,0.2.1-test,min26,target32.
SHA256: e34bc1e70d87650b0c400188f5c1c65c9e8826d0ed3795bd24ffee1fe55cd968.
APK CI dùng khóa debug runner; khóa ký máy phát triển cũ không còn trong workspace,
không đảm bảo cập nhật đè APK0.2.0. Không gỡ bản cũ để vượt lỗi chữ ký khi chưa giữ cấu hình.

## Mốc0.2.2
Sửa nền launcher sau start/preset, handle nhỏ, mặc định chờ0ms và gesture touch slop.
Chưa chạy UI trên thiết bị; cần workflow commit0.2.2 xác minh build/unit/lint.

CI0.2.2 build/unit/lint đạt tại commit8083a19c8135ca1504c8723ad01e4c614e55a2ec:
https://github.com/meomeo2099/THT-Split/actions/runs/37262534677
APK artifact: com.tht.split / versionCode4 /0.2.2-test/min26/target32.
SHA256: 3eb9758bc98f7e119cfa09b88bcb7c1c1935f05a54fe2df00cc8110c99ff6a24.
Cert v2 APK CI0.2.2 khác bản0.2.1 đã gửi; không cài đè được bản đó.

## Mốc0.2.5 — phục hồi cửa sổ
Commit38712989f64cddf324cd82640c64a8b219a4b4c2; GitHub Actions build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37269596828
Thêm7 regression tests PairController cho thứ tự swap/rebuild và task/mode thiếu.
Test dùng fake task manager, không chứng minh Binder/LDPlayer/xe thực tế.
APK artifact: com.tht.split / versionCode7 /0.2.5-test/min26/target32.
SHA256: 7ae9e2577c6081658b3f06103cd685c16c66970faa666a6b269bcfa3beb9bf8d.
Bridge v3 cần reboot/kết nối lại để không dùng code v2 còn sống; không tự kill listener.
APK CI dùng debug signer runner, không đảm bảo cập nhật đè bản test trước.
Chưa kiểm bản mới trên LDPlayer/xe; Off không có quyền quản lý task.

## Mốc0.2.6 — stack riêng và Off
Commit e0856f96cd3ada4ccfa5e9fee2a5d0e62c3e7df4; CI build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37271611577
Fake task manager mô hình hóa setMode thay cả stack và launch reuse bỏ qua mode.
Thêm3 tests shared/native/freeform/reuse,2 tests policy Off; sửa test thứ tự có focus.
Đây là kiểm logic, chưa kiểm Binder/LDPlayer/xe thực tế. Không gọi Off mở lại đã đúng.
APK: com.tht.split/versionCode8/0.2.6-test/min26/target32.
SHA256: 79d53b3b1263f12408a7b904a6fc2d1386ad999519ba7395d425df6e961afbe2.
Bridge v4 cần reboot/kết nối lại; APK CI có thể khác signer bản test cũ.
Cửa sổ Off sai từ phiên cũ phải đóng bằng Android một lần; X THT chỉ dừng overlay.

## Mốc0.2.7 — khoảng chừa secondary native
CI đầu bắt JSONException chưa khai báo trong bounds(JSONArray); đã sửa.
Bản cuối5012d4f24ae8adb8de4f0c82f6ee0157e4cb861a build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37272679706
Thêm3 tests bounds: secondary tràn display, snapped ratio khác yêu cầu, sai số pixel.
Không chứng minh resizeStack/getTaskBounds trên LDPlayer/xe thực tế.
APK: com.tht.split/versionCode9/0.2.7-test/min26/target32.
SHA256: 12eccd8bb789296f147d37624fa9041c2c87ca2049a3388518c8337221eb88b6.
Bridge v5 cần reboot/kết nối lại. APK CI có thể khác signer bản cũ.
Nếu read-back khác yêu cầu, verified=false/callback requested và log có expected/actual.

## Mốc0.2.8 — chuyển toàn màn hình
Commit c391b83124fa58be2df00d74ab0b03ecd1e6587a; CI build/unit/lint đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37275295920
Thêm5 tests: reset bounds cả hai trước focus, bounds cũ dù mode đã1, stop fallback,
target đóng không làm thay đổi app còn lại, cả hai đóng không ảnh hưởng task khác.
Fake tests chỉ kiểm thứ tự/trạng thái; chưa kiểm Binder/configuration/Maps renderer
trên LDPlayer hoặc GPU xe. Ảnh UI Maps toàn màn nhưng lớp bản đồ vẽ thiếu chưa có
log bounds, không đủ chứng minh nguyên nhân là LDPlayer hoặc task bounds.
APK: com.tht.split/versionCode10/0.2.8-test/min26/target32.
SHA256: 24db6193704236693a0dc0b78502be13796e6caac6b5067597ce2640f7041b11.
Bridge v6 cần reboot/kết nối lại. APK debug CI có thể khác signer bản trước;
không gỡ app khi chưa giữ cấu hình. Cần khóa ký ổn định ngoài git trước release.
Full read-back đúng chỉ xác nhận mode/bounds, không chứng minh lớp bản đồ đã vẽ hết.

## Mốc0.2.9 — khóa ADB không có CRT trên Android9
Log thực14:48:13: ClassCastException OpenSSLRSAPrivateKey → RSAPrivateCrtKey tại
LocalAdb.loadKey, trước socket.connect. Đã sửa, commit1a20e672c9d6dae2f21179d9a41a118df890918d.
CI build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37280998296.
Thêm2 tests non-CRT/sign/identity/PKCS8 reload và2 tests cause/redaction.
Test RSA dùng provider JVM với wrapper không có CRT, không chứng minh Conscrypt
Android9 hoặc ADB xác thực/socket/bootstrap thật. Cần thử APK trên LDPlayer9/EX2.
APK: com.tht.split/versionCode11/0.2.9-test/min26/target32; bridge vẫn v6.
SHA256: ed58fb8452914290bae0b341411498483c5df2e7a14f0e9e50a2249e0e9c330d.
Không thay khóa ADB đã lưu. Chỉ giữ được khóa/cấu hình khi cài đè cùng signer giữ
app data; APK debug CI có thể khác signer bản trước, cần giữ cấu hình trước cài mới.
Test tự mở bridge:127.0.0.1:5555, Kiểm tra khi chưa chạy dự phòng; thử reboot thật.
Không coi cổng TCP đang nghe hoặc build đạt là xác thực ADB thành công trên xe.


## 0.3.0-test — cập nhật/khóa từ xa/ủng hộ05/10/2026
- Người dùng xác nhận0.2.9 kết nối ADB và test OK trên LDPlayer9. Chưa xác nhận
  reboot tự nối lại, chưa xác nhận xe thật. Không đổi bridge v6/window backend.
- CI code cuối a12dd9be583537cfb653dca5c85e200ed63b352a:
  https://github.com/meomeo2099/THT-Split/actions/runs/37290015077
  build/unit/lint +6 Node tests server + Wrangler4 dry-run đạt. Không deploy.
- APK com.tht.split/versionCode12/versionName0.3.0-test/min26/target32.
- SHA256: f30d0a909691bbdc035d2b1983d9c19d54816788c2c77c106e02d4b9d01652d9.
- APK test không có HTTPS origin CONTROL_URL. Quản trị, cập nhật từ xa và QR/STK
  chưa hoạt động thật; app ghi rõ chưa cấu hình, cho dùng split để test UI.
- Chưa có ngân hàng/QR thật, URL Worker đã deploy hoặc keystore ổn định. Khóa CI
  debug vẫn có thể khác APK0.2.9. Release workflow từ chối thiếu endpoint/secrets.
- Kiểm tra tác vụ/khóa/lease/installer cùng signer theo REMOTE_CONTROL.md sau cấu
  hình; unit test không xác minh hành vi mạng hoặc PackageInstaller trên xe.


## 0.3.1-test — QR ủng hộ nhúng sẵn và không cần duyệt
- CI commit1bc8b14446be4e19797feffc5d449728ac9d43b5:
  https://github.com/meomeo2099/THT-Split/actions/runs/37301624495
  Android build/unit/lint,7 Node tests, giải mã QR kiểm profile/EMV CRC và
  Wrangler dry-run đều đạt. Chưa deploy Worker hoặc thử APK mới trên LDPlayer/xe.
- QR downloaded original JPEG490×490 từ link chủ dự án cung cấp; asset trong APK
  byte-for-byte bằng source. Ngân hàng/STK trong payload khớp profile, CRC hợp lệ.
  Holder hiển thị theo chủ dự án gửi; không xác minh tên qua hệ thống ngân hàng.
- Thiết bị mới enabled ngay; cấu hình cũ autoApprove=false không làm chờ duyệt,
  pending migrate enabled. Locked vẫn giữ qua heartbeat/migrate/reenroll. Admin
  chỉ có Khóa/Mở khóa cho thiết bị; cập nhật bắt buộc vẫn giữ.
- com.tht.split/versionCode13/versionName0.3.1-test/min26/target32.
- SHA256: c0252c65bc6c5829ff4cfc4d9ac54eb7cf8cb6b17c083bcf1beb1109d7b61201.
- Signer SHA256: f15296a430902ad605593b462a224044d04e608ae4eb9955b2d15da01d4a2745.
  Khác0.3.0 debug signer. Chưa có signer phát hành ổn định; cần giữ cấu hình trước
  nếu buộc cài mới. Không hứa token/preset/RSA tồn tại sau gỡ app.
- Mục ủng hộ không cần mạng/Worker. Khóa/cập nhật từ xa còn cần endpoint thật và
  signer ổn định cho APK được phát hành; xem REMOTE_CONTROL.md.
