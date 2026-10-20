# Bàn giao THT Split — 0.3.16-test, 10/10/2026

## Xác nhận Release Coolray2025 —20/10/2026
Commit mã2cb76dbea338ee579db4387641544288abdff9d4 đã được build/ký/phát hành.
CI39407617760/job118577273157 completed/success: testCoolrayReleaseUnitTest,
lintCoolrayRelease, assembleCoolrayRelease, apksigner/aapt cert/package/code31/
name0.3.19-coolray2025, no sharedUID/nondebuggable đều đạt. UI Android9 emulator:
1920×720/160dpi font1.0 và1.3,1920×600/font1.0 đều UI OK. Đã xem ảnh home720/1.0
và settings720/1.3: nội dung/nút rõ, các mục đủ; đây chưa là test trên Coolray.
Release418273160/v0.3.19-coolray2025 đã publish/prerelease/not draft:
https://github.com/meomeo2099/THT-Split/releases/tag/v0.3.19-coolray2025
APK659005898:275860 bytes, SHA256
123a8a0f94bff17745c733209d15c7fa997c33de5816f86c75b113f4f03236ae.
Cert vẫn c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8.
Đủ APK/checksum/guide9ảnhPNG; không update.json/chuyểnWorker. Artifact12257529224
đã upload thực sự; tải/kiểm SHA ZIP và APK khớp Release, DEX chứa side_left_px/
draggedRatio/nhãn lề trái mới. Hai lần trước không cấp runner là lịch sử; lần
mới đã chạy đạt, không tiếp tục yêu cầu chủ dự án gửi Annotations cũ.
Bước tiếp: cài đè bản platform cùng signer, kiểm quyền thực Coolray, chỉnh lề
trái160px và trên/dưới theo xe rồi thử cặp/tỷ lệ/kéo/full/restore/reboot. Không
kết luận đã hết lỗi trên xe từ UI smoke/ảnh giả lập. EX2/main giữ nhánh cũ.


## Bổ sung thanh hệ thống bên trái —20/10/2026
Đã đọc ảnh gconnect-screenshot.png (1920×720): dock trái≈120px, nội dung app
bắt đầu gần x160px. Đây là số đo ảnh, chưa phải inset WindowManager đã xác minh.
Thêm side_left_px mặc định thử160 cho Coolray (flag/model/product); EX2 vẫn0,
giá trị đã lưu được ưu tiên. UI chỉnh0..600px; snapshot allowlist/validation giữ
backup/restore mới, không đổi token/khóa/policy. Cài đè không ghi đè tùy chỉnh.
LayoutMath có constructor nhận inset trái, overload cũ vẫn0. Tỷ lệ/kéo tính theo
vùng còn lại:1920−160=1760, tâm50:50 x1040. SplitEngine gửi task bounds cùng vùng.
Toolbar/loading/mini và preview divider cùng offset; session capture/rollback/
cache fingerprint gồm side_left_px. Không sửa Bridge/bootstrap/signer/API Voice,
không thay firmware hoặc đoán giao thức ecarx_splitscreen. Fullscreen vẫn do
Android quản lý như bản trước; read-back báo chưa xác minh nếu OEM bỏ bounds.
Không tự thêm padding160 vào Activity vì Android có thể đã trừ dock khỏi window.
Top72/bottom112 vẫn thử nghiệm, không suy số đo trên/dưới từ hình; chỉnh trên xe.
Đã javac47 Java main với Android34/stub R/BuildConfig và chạy harness thật:
5 tỷ lệ720p, tâm1040, kéo176px=10%, clamp30/70, invalid inset, snapshot trái160
và từ chối sai kiểu/phạm vi; hồi quyEX2 lề0/tâm960 đạt. git diff --check đạt.
Thêm unit tests nhưng JUnit/lint/UI giả lập/APK chưa xác minh vì lần CI trước
không cấp runner. Giữ nhánh riêng Coolray, chưa merge main/chuyển OTA EX2.


## Coolray 2025 —20/10/2026
Yêu cầu làm thêm bản cấu hình vì giao diện bản trước lỗi. Dựa file người dùng
Coolray2025-Voice-Spec.md: SX11-LA/IHU730P, Android9/API28,1920×720/160dpi.
Nhánh release/coolray2025-0.3.19 từ ddab015 (0.3.18 platform), thêm coolrayRelease:
package com.tht.split/code31/name0.3.19-coolray2025/cert AOSP platform như0.3.18.
Không shared UID. Không merge main, không thay Secrets signer tht-split.
MainActivity/ScreenProfile: chiều cao khả dụng<=720dp và rộng>=900dp dùng header52,
thẻ chọn app gọn, settings tabs ngang cuộn, menu chính cuộn và nút tăng theo font.
Không đổi density/resolution, SplitEngine/geometry/overlay/Bridge/Voice/Worker.
Khoảng chừa72/112px giữ cấu hình cũ, chưa có số đo HVAC Coolray trong spec;
không tự scale/ghi đè insets đã chỉnh. Chỉ có tên service ecarx_splitscreen trong
spec, chưa có giao thức Binder nên không gọi theo transaction code phỏng đoán.
Cert platform Coolray chưa xác minh, không suy từ ảnh EX2 thành hỗ trợ mọi xe.
Workflow riêng test/lint/build, ký/kiểm cert-package-code-manifest, UI smoke
giả lập Android9 tại1920×720/160dpi/font1.0 và1.3,1920×600/font1.0. Publish
APK+checksum+guide+ảnh giả lập, không update.json/chuyển OTA EX2. CI chưa chạy
tại mốc commit này; cần ghi bổ sung kết quả thực. Chưa thử trên Coolray thật.
Xem docs/COOLRAY_2025.md; cần ảnh/log trên xe để xác minh lỗi UI cụ thể đã hết.

Kết quả20/10: commit09f7298, workflow39405081467 đã thử2 lần; cả hai kết thúc
failure trước cấp runner (runner_id0, steps[]), không log build/artifact/APK.
Job cuối118569657276. Connector không đọc được endpoint annotations, chưa biết
nguyên nhân hệ thống GitHub; cần mở trang Actions xem Annotations trước sửa.
Không gọi đây là lỗi Java/SDK, không phát hành APK hoặc đánh dấu test/lint/UI đạt.
Đã kiểm cục bộ bằng JDK17 javac -source8/target8: toàn bộ47 Java main + stub
Unsafe biên dịch với Android34 android.jar; R/BuildConfig tạm sinh chỉ để kiểm
kiểu nên chưa thay được Gradle/AAPT/lint. Harness thực chạy đạt nhận diện model/
product, viewport720/600 vsEX2/narrow, font1.3, LayoutMath năm tỷ lệ tại1920×720.
Python smoke script py_compile và git diff --check đạt. JUnit/giả lập chưa chạy.
Bước tiếp: mở https://github.com/meomeo2099/THT-Split/actions/runs/39405081467
xem Annotations, khắc phục điều kiện runner rồi Re-run failed jobs. Chỉ gửi link
Release/APK sau khi job chạy test/lint/sign/emulator/publish thành công thực tế.


## Release 0.3.18-platform — thử cập nhật trực tiếp từ 0.3.17-platform (16/10/2026)
Yêu cầu chủ dự án: build bản 0.3.18-platform package chính để kiểm tra cài đè.
Nhánh release/platform-update-0.3.18 xuất phát release/platform-original;
chưa merge main hay thay release/worker cũ. Chỉ nâng app/build.gradle
versionCode 29→30, versionName 0.3.17→0.3.18; variant platformRelease
giữ versionNameSuffix "-platform", applicationId com.tht.split, không shared UID
hay UID1000, cert SHA256 AOSP9 c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8.
Không thay Java/Bridge/Voice/UI. Workflow riêng trên branch này kiểm chứng
build test/lint, signer/package/code30/name 0.3.18-platform/manifest no shared
UID/nondebuggable, metadata update.json từ APK đã ký. Bản UID1000 trước đó ở
test/uid1000-platform-0.3.18 không thuộc kênh cập nhật này.
CI các lần đầu gặp 403 khi tạo Release; mốc cuối cùng 38779199155 /
job116574809305 completed/success, test/lint/build/apksigner/aapt/publish OK.
Release v0.3.18-platform published/prerelease/not draft:
https://github.com/meomeo2099/THT-Split/releases/tag/v0.3.18-platform
THT-Split.apk 275860 bytes, SHA APK
5ef6fbfce9471d687ee0038270953a245a886573e45eff6a5b60b3ae296bc6c5;
đủ checksum, update.json và HUONG_DAN_PLATFORM.md. Artifact upload step báo
success nhưng API liệt kê artifacts rỗng sau run; GitHub Release asset đã xác nhận.
Đã bổ sung docs/PLATFORM_UPDATE_0318.md: thử adb install -r 0.3.17→0.3.18,
không tự gỡ/dọn app, sao lưu trước, kiểm dữ liệu và reboot trên xe.
Server Github Worker hiện normalizeCertSha256 chỉ chấp nhận 1 SHA, giá trị
RELEASE_CERT_SHA256 gồm dấu phẩy là lỗi, không phải allowlist. Chưa deploy/sửa
Worker/chuyển SHA/mandatory/minimum để tránh ảnh hưởng khách Legacy; in-app OTA
cần thay đổi kênh có kiểm tra tương thích trước khi thử. Chưa xác nhận update
hoạt động trên xe chỉ từ CI.


## Release platform package gốc —15/10
Chủ dự án gửi platform.pk8/platform.x509.pem, xác minh bằng openssl: publickey
khớp, certSHA c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8,
trùng cặp thử AOSP. Yêu cầu rõ build Release package gốc com.tht.split, thay
cho đề xuất cài đè package thử. Đây là chuyển signer được chủ dự án chỉ định,
không xoay khóa vì lỗi update. Giữ Secrets/khóa tht-split của main không đổi.
Nhánh release/platform-original từ66f4f79, buildType platformRelease/init release,
versionCode29/versionName0.3.17-platform, không suffix applicationId/shared UID.
PLATFORM_TEST=true để tái dùng gate system-only, PLATFORM_RELEASE=true để hiện
UI cập nhật thường và giữ Worker CONTROL_URL. Không tự nối Bridge hoặc fallback.
PING khi systemContext trả sdk/uid/manager/systemPermission, bỏ bridgeVersion/
commandIo/context pipe không đúng; đường daemon PING v7 giữ nguyên.
Giữ Java/native UI/QR/icon/layout/Voice receiver/permission package gốc. Không
sửa THTVoice; nếu gỡ bản signer cũ, token đổi và Voice cần nhập lại token.
Ảnh13/10 xác nhận platformtest trùng cert/được cấp MANAGE_ACTIVITY_STACKS trên
Android9 UID10036/context platform_app; chưa có xác nhận đầy đủ split/reboot.
Workflow riêng chạy unit/lint/build, ký AOSP đúng pin và kiểm package29/label/
không debuggable/không sharedUID; tạo metadata từ APK thực, publish tag riêng.
Worker vẫn pin signer cũ: muốn OTA nhánh này cần chủ dự án đổi RELEASE_CERT_SHA256
và kiểm ngay; chưa deploy/sửa Worker/minimum/khóa chung. Không bỏ signer checks.
docs/PLATFORM_RELEASE.md ghi sao lưu/gỡ-cài một lần nếu khác signer; không tự
xóa app/dữ liệu. Bản platformtest là package khác không cập nhật đè được.
Workspace đã dọn nguồn cũ; chỉ khôi phục tệp sửa, CI checkout base tree đầy đủ.
CI38764095446/job116522674568 completed/success: testPlatformReleaseUnitTest,
lintPlatformRelease/assemblePlatformRelease; apksigner pin mới đúng, aapt
com.tht.split/code29/0.3.17-platform/labelTHT Split, không debuggable/sharedUID.
Metadata tạo từ APK thực với signer mới đạt. Codee7da1777f6f4f48bbe7dff8e52fe5ad22c5105e5;
release413997367/v0.3.17-platform draft=false/prerelease=true/latest=false.
APK644363704/275860bytes/SHAad9e338a9db1340b54d0f045a52d12e64e47ffde6457dfbc655ef97567700471;
checksum644363702/metadata644363703/hướng dẫn644363705. Artifact optional vẫn
lỗi quota nên không có binary để tải local apksigner lại. Không xóa artifact/
release cũ. Bản sẵn sàng tải từ Releases; chưa xác minh cài/quyền/chia/reboot
trên xe với package gốc/signer mới. Chưa thay Worker pin hoặc dữ liệu/Secrets.


## Bản thử AOSP platform riêng —13/10
Chủ dự án gửi SIGNING_AND_VOICE_KEY.md rồi yêu cầu build dùng chứng thư đó.
Nhánh test/aosp-platform-split dựa baa5827, buildType platformTest/code28/
0.3.16-test-platformtest1/applicationId com.tht.split.platformtest. Chứng thư
pin c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8;
khóa thử công khai AOSP android9_r1 tải tạm vào CI, không git/Secrets bản thường.
Không đổi khóa release tht-split hoặc phiên bản APK thường; chưa merge main.
Manifest permission CONTROL_SPLIT theo applicationId tránh duplicate permission
khác signer khi cài song song; bản thường tên quyền/package giữ nguyên.
PLATFORM_TEST ép transport system trong Store/Backend; hiển thị đối chiếu cert
android/quyền thực/UID. Chưa có quyền thì lỗi, không fallbackBridge/Off.
Không sharedUserId; CONTROL_URL rỗng ở bản thử để tránh luồng update của bản
thường. Update UI chỉ diagnostic/exportlog. Bỏ nút freeform shell settings ở
bản thử vì quyền MANAGE_ACTIVITY_STACKS không cấp WRITE_SECURE_SETTINGS.
Giữ Bridge/resize/UI/QR/icon/API; Voice phải gửi explicit package thử, chưa sửa
repoVoice. Release riêng không update.json nên Worker thường không chọn.
Nguồn local khôi phục văn bản qua connector sau dọn workspace; assets binary/
wrapper jar vẫn giữ trong base tree GitHub, CI checkout đầy đủ để build.
CI38362762170/job115180631927 completed/success: testPlatformTestUnitTest,
lintPlatformTest/assemblePlatformTest, pin cert và apksigner APK thực đạt.
Package com.tht.split.platformtest/code28/0.3.16-test-platformtest1 được aapt
kiểm; permission theo package thử, không sharedUserId. Khóa thử tạm được dọn.
Code d87fe941b5665f1d662bc9d28ad7cdf7decf1618; release411404594/tag
platform-test-0.3.16-1 draft=false/prerelease=true/latest=false. APK636255350/
275860 bytes/SHA2c554090a18e5b61a6cba04743aabde1d2e2b7f24dfc08a9c920172ccf84eab9;
checksum636255351/hướng dẫn636255352. Artifact optional vẫn quota (log ghi
Failed to CreateArtifact), không có artifact để tải binary local verify lại;
không coi step kết luận success sau continue-on-error là upload thành công.
Cài/quyền/Binder/reboot trên xe chưa xác minh. docs/PLATFORM_TEST.md có cách
kiểm quyền TRƯỚC pm grant và thử chia sau reboot thật. Main vẫn baa5827.


## Lỗi gọi activity sau reboot trên EX2 —0.3.16/code28
Chủ dự án xác nhận cài lần đầu dùng được, reboot màn rồi Failed transaction
(2147483646). File thiết bị: IHU629G Android9/SDK28/incremental121, ADB máy tính
uid0/context u:r:su:s0; chưa có log AVC/Bridge context sau reboot. Không kết luận
race bootstrap hoặc SELinux là nguyên nhân đã chứng minh. Giả thuyết cần thử:
ProcessBuilder truyền su-owned fifo descriptors tới system_server bị firmware
từ chối. Bridge v7 với UID0 dùng stdin /dev/null, stdout+stderr qua tệp mới
0600 trong /data/local/tmp; truyền argv trực tiếp, không ghép shell/không retry
lệnh có side effect. UID2000 giữ pipe, đóng stdin EOF, giới hạn đọc24KB.
Timeout12s, output file cap256KB kiểm mỗi50ms, dọn capture trong finally; lỗi
gồm tên chương trình/UID/context, PING/UI thêm context/commandIo. Không setenforce,
không đổi context/UID, root màn, pm grant, framework, bố cục hoặc dữ liệu app khác.
Client bắt buộc v7 để không dùng Bridge v6 còn chạy: cần reboot màn một lần
sau cập nhật; boot tự bootstrap vẫn sleep1/echo +12 PING250ms, RSA/token giữ.
Java compile và7 tests runner local đạt: stderr/stdout, stdin EOF hai mode,
exit failure không retry, Binder lỗi exit0, timeout, file0600/cap và output lớn.
CI38068116109/job114259814881 unitRelease/lint/build/signer/metadata đạt,
nhưng upload-artifact lỗi Artifact storage quota has been hit, publish bị skip.
Cho bản sao artifact của release continue-on-error; kiểm build/signature/metadata
và publish Releases vẫn bắt buộc. Không xóa artifact/release hay đổi signer.
Lần mới38068315132/job114260388387 completed/success: unitRelease/lint/build/
signer/metadata và publish đạt; artifact vẫn quota nhưng bản sao optional.
Debug38068315133/job114260388120 unitDebug/lint/build/signer đạt, job failure
chỉ ở artifact quota (không che là toàn debug workflow xanh).
Code893625db5cf2391ba2e32066a4748fc2bbecc169; workflowfix0c9271281fc8890f2e869165b517ce863111a91f.
Release409069183/v0.3.16-test draft=false/prerelease=true; APK628452707 size269029,
SHA5269085309ffca4e0ddea32bb7a0d4d52420d7e2809cdb9dcb94596cc1391020;
checksum628452710/manifest628452705. Signer cố định được CI kiểm pin; chưa tải
binary về local để apksigner lại vì không có artifact. Giao qua Releases/app
Worker update; chưa sửa/deploy Worker hay xác nhận cache đã nhận bản mới.
Test JVM/CI không chứng minh đã sửa reboot trên xe; cần DEVICE_TESTv7.


## Gộp hình tay nắm với thanh hệ thống — 0.3.15-test/code27
Đã đọc ảnh image(20261007-040145).png: chủ dự án chỉ tay nắm trắng nhỏ trên
divider đen của Android, không phải grip THT;0.3.14 trước đó hiểu nhầm.
Bỏ rail/mũi tên/dots riêng, chỉ pill trắng6×72dp. Native dùng tâm ngang từ
actualLeftBounds/actualRightBounds khi hợp lệ, fallback geometry nếu thiếu/
overlap/gap>40dp. Tâm dọc native là giữa display như layout AOSP landscape;
ẩn/hiện toolbar không làm pill lệch dọc. Freeform giữ tâm vùng app.
Native MOVE chỉ hiện tỷ lệ, không di chuyển pill khỏi thanh hệ thống hiện
tại để tránh hai pill; UP áp dụng resize rồi căn theo read-back. Freeform
vẫn preview MOVE vì không có thanh hệ thống. Touch strip48dp toàn chiều cao
vùng app, cancel/busy/Off/full/X giữ quy tắc cũ; mode vào overlay fingerprint.
Đây là ghép bằng overlay, không sửa/hook SystemUI hay đổi resource hệ thống.
Không thay backend/Bridge/Voice/Worker/signer/QR/icon hoặc thêm quyền/root.
44 Java nguồn compile local đạt;10 tests DividerPlacement/LayoutMath đạt
(4 mới: native center không đổi theo toolbar, read-back seam, dữ liệu sai,
insets lớn). diff --check sạch. Release37570002943/job112626257925 và debug
37570002893/job112626257422 completed/success: unit/lint/build/signer/QR/Node/
dry-run/metadata đạt. Codecefc660f0438d43fe65c0df0c7df6919122475bb;
release405369307/v0.3.15-test prerelease=true/draft=false, APK617522634/
checksum617522639/manifest617522636. Artifact11460222912 kiểm APK thực
267413 bytes/com.tht.split/code27/min26/target32; SHA021dec9caf0fc17bba33f6f30401024d9f96941abec01456ebb17e22214be230
khớp checksum/manifest/GitHub digest, apksigner v2 cùng cert cố định. Assets
nguyên0.3.14. Chưa kiểm vị trí/gesture OEM thực; cần thử LDPlayer/xe theo DEVICE_TEST.


## Tay nắm trắng dài hơn — 0.3.14-test/code26
Chủ dự án yêu cầu phần kéo đồng bộ với thanh trắng giữa hai app, dài hơn một
chút. Tay nắm overlay trắng52→72dp cao,10dp rộng, vạch dọc đổi trắng đồng màu;
không chuyển cyan khi chạm, giữ badge tỷ lệ cyan. Vùng kéo48dp toàn chiều cao
và tay nắm cùng tâm g.splitX; preview/cancel/restore cập nhật cùng view như0.3.13.
Chỉ SplitDividerView và version đổi; không sửa thanh phân cách do Android/OEM
vẽ, backend/resize/Bridge/timing hoặc layout loading. Giữ signer/QR/icon/Worker.
Java compile/diff --check đạt. Release37568930544/job112622945467 và debug
37568930468/job112622944778 completed/success: unit/lint/build/signer/QR/Node/
dry-run/metadata đạt. Code8bfb75d6e0518531e62cf2d771c93581fd68b83c;
release405361117/v0.3.14-test prerelease=true/draft=false, APK617497379/
checksum617497383/manifest617497382. Artifact11460330695 kiểm APK thực
266965 bytes/com.tht.split/code26/min26/target32; SHA46e813a21c2beed3eea917638bcd985458082c7f38d210cf38a98997e42af2dc
khớp checksum/manifest/GitHub digest, apksigner v2 cùng cert tht-split. Assets
nguyên0.3.13. Chưa xác minh vị trí divider thật trên LDPlayer/xe.


## Màn chờ và thanh kéo toàn chiều cao — 0.3.13-test/code25
Đã đọc ảnh image(20261007-034042).png thực: vòng tải giữa màn, chữ lệch trái.
Chủ dự án yêu cầu màn chờ đẹp hơn và kéo từ bất kỳ điểm trên thanh dọc.
SplitLoadingView gom badge/logo chia/title/progress cyan trong thẻ giữa, nền
gradient navy; vùng thấp<240dp dùng bản gọn. Thanh tải indeterminate, không
phần trăm giả/khẳng định app khác đã vẽ xong; thời gian/backend vẫn giữ.
SplitDividerView vạch cyan mảnh/tay nắm nhỏ, mũi tên ngang; touch strip48dp
trải từ g.left.top tới bottom, không chạm toolbar/điều hòa/khoảng chừa. Chạm ở
mọi độ cao, kéo ngang preview/tỷ lệ, thả tay gửi một resize như cơ chế cũ.
Không resize backend từng MOVE. Multi-touch/cancel trả view về geometry thật,
tap không resize, busy bỏ gesture; read LayoutParams hiện tại khi MOVE.
Off/fullscreen không hiện divider; hide/show cập nhật chiều cao; X/Home dọn.
Giữ key/backend/PairController/Bridge sleep1+12poll/Worker/Voice/QR/icon và
trang0.3.12; chưa làm3 app.43 Java nguồn compile đạt, diff --check sạch;
release37568347356/job112621121563 và debug37568347320/job112621121154
completed/success, unit/lint/build/QR/Node/dry-run/signer/metadata đạt.
Code109d266f0208863428a901081140ab63af85943d; release405356429/v0.3.13-test
prerelease=true/draft=false. APK617481831/checksum617481836/manifest617481830.
Artifact11459209371 đã kiểm APK thực267093 bytes/com.tht.split/code25/min26/
target32; SHA11651116e9dbd52769836eb0da64b283cb5aa4dc9f7fdbbf7d6deb56d5c89192
khớp checksum/manifest/GitHub digest, signer v2 cùng cert tht-split. Assets
nguyên bytes0.3.12. Chưa quan sát animation/touch thực trên LDPlayer/xe.


## Ủng hộ và liên hệ gọn hơn — 0.3.12-test/code24
Chủ dự án yêu cầu thiết kế lại trang ủng hộ/liên hệ gọn gàng hơn. Gom thành hai
thẻ cân nhau với QR160dp, chữ/nút ngắn, padding16/gap16; tính phần rộng nội dung
sau navigation/padding, đủ760dp mới đặt hai thẻ cạnh nhau. Thẻ<384dp xếp nội
dung dọc tránh ép STK/nút; có cuộn. QR chạm mở dialog ảnh lớn theo chiều màn,
FIT_CENTER giữ toàn ảnh/nền trắng; lỗi đọc vẫn còn thông tin/nút liên hệ.
Giữ nguyên profile/STK/link/bytes QR, icon app, backend/overlay/ADB/Worker/Voice
và khóa ký. Không làm chế độ3 app: chủ dự án muốn test chia đôi trước.
Java compile tại máy và git diff --check đạt. Release37567753293/
job112619263096 và Debug37567753325/job112619262891 completed/success:
unit/lint/build/QR/Node/Worker dry-run/signer đều đạt. Local QR script thiếu
zxingcpp, đã được CI chạy đạt; không gọi lỗi dependency là lỗi QR.
Code d662fa963b4a13eba2f06a67d29823ff1584c041. Release405352571/v0.3.12-test
prerelease=true/draft=false, APK617468897/checksum617468896/manifest617468898.
Artifact11458969014 kiểm APK thực com.tht.split/code24/min26/target32,
264845 bytes, SHA b769fd4591b9aeacc21ebcba0d221cdccdae80087c88c309b43b6eb8391361ca
khớp checksum/manifest/GitHub digest. apksigner v2 cùng cert tht-split đã pin;
assets nguyên bytes0.3.11. Chưa có ảnh kiểm UI thật LDPlayer/xe: cần xem card
trên màn rộng/hẹp, quét QR160 và popup lớn, bấm copy STK/link và mở Facebook.


## Tối ưu và sao lưu — bản 0.3.11-test/code23
Chủ dự án xác nhận cập nhật đã OK, yêu cầu làm cả 5 phần tối ưu đã đề xuất.
Picker đọc danh sách/icon ở worker, tái dùng hàng và cache icon có giới hạn;
mỗi lần mở vẫn đọc danh sách mới. Tải APK có tiến trình/%/dung lượng/giai đoạn,
cho ẩn và mở lại; giữ nguyên kiểm SHA, package, phiên bản và cùng signer.
Cài đặt > Sao lưu xuất/nhập JSON schema1, tối đa1MB/100 cặp, có xem trước và
xác nhận. Chỉ thay allowlist cài đặt/cặp; không xuất/ghi token, RSA, policy,
session hoặc dữ liệu rollback freeform. Khôi phục yêu cầu đã tắt chia, dùng
một commit và chặn lệnh mở chia trong lúc ghi; không tự cài app còn thiếu.
Overlay tái dùng toolbar/mini/grip khi cấu hình không đổi, cập nhật geometry
và trạng thái nút; đổi density/kích thước/vùng sử dụng/side/opacity thì dựng lại.
Ghi tối đa20 thời gian thao tác cục bộ, xem trong Kết nối và Xuất log; không
ghi lệnh/token hoặc tự gửi lên mạng. Đây là thời gian xử lý, không phải FPS.
Không đổi PairController/SplitEngine, Off, Voice API, icon/QR, Worker, keystore
hoặc bootstrap shell: giữ sleep1/echo và12 PING250ms đúng quy tắc ổn định.
Local: toàn bộ41 Java nguồn biên dịch với Android35 và58 kiểm thử lõi đạt;
git diff --check đạt. Gradle offline thiếu AGP8.7.3, chưa chạy được full build
tại máy. Thêm4 kiểm thử đọc JSON thực với org.json chỉ trong test dependency;
CI đã chạy thành công unitRelease/lintRelease/assembleRelease và unitDebug/
lintDebug/assembleDebug, cả4 JSON tests mới không còn bị chặn dependency.
Codecad1cb502bdb2766e5b88a0b162b5914217ed0ee; release37462993653/
job112266943255 completed/success; debug37462993629/job112266942634
completed/success, unit/lint/build/signer và post-job đạt.
Release404678577/v0.3.11-test prerelease=true/draft=false, APK615451040/
checksum615451042/manifest615451043. Artifact11413716018 tải và kiểm APK thực:
com.tht.split/code23/0.3.11-test/min26/target32,264609 bytes; SHA
03b1014c5e6fff2689dabc589a647afb93ea4cee07f183c48fbe7fe5d8328aa6 khớp
checksum/manifest/GitHub digest; apksigner v2 đạt cùng cert tht-split đã pin.
Assets byte-identical0.3.10, không đổi Worker/icon/QR. Chưa thử cập nhật
0.3.10→0.3.11 hoặc benchmark UI/LDPlayer/xe; cần DEVICE_TEST.


## Bản thử cập nhật cùng khóa — chốt18:32
Tăng versionCode21→22, versionName0.3.9-test→0.3.10-test để chủ dự án thử
cập nhật trong app. Không đổi app/src/assets, keystore, Worker hoặc Bridge.
Giữ pin chứng chỉ tht-split và 4 Secrets đã ký0.3.9 thành công; release tự chạy
khi đẩy main, chỉ publish sau unit/lint/signature/metadata và đủ3 assets.
Ảnh18:29 báo APK khác chữ ký giữa bản cài trên máy và APK cập nhật; chưa lấy
chứng chỉ bản đang cài nên chưa xác định chính xác phiên bản/khóa trên máy.
Test phải bắt đầu từ APK0.3.9-test/code21 ký đúng tht-split. Nếu bản cũ ký khóa
khác, không cài đè bằng khóa mới; chủ dự án ghi lại cấu hình trước khi tự gỡ/cài
một lần. Không bỏ kiểm chữ ký hoặc tự gỡ app để che lỗi. Worker nguồn GitHub
cần nhận prerelease và Kiểm tra GitHub ngay để bỏ cache5 phút. Giữ minimum21
hoặc thấp hơn khi thử cập nhật tự nguyện; không buộc22 trước khi test thành công.
Bước thử: cài0.3.9 đúng signer → lưu preset/cài đặt → Worker nhận0.3.10/code22 →
trong app Kiểm tra/Tải và cài → xác nhận0.3.10, preset/cài đặt còn và chia/ADB.
Code1bfbc994a3242cf6f4fa80c549adce3b48e24de3. Release CI37457248132/
job112247751402 completed/success: Node24 tests, QR, dry-run, unitRelease,
lintRelease, signer, metadata và publish đạt. Debug37457248053/job112247750859
completed/success: unitDebug/lintDebug/build/signer và kiểm tra kèm theo đạt.
Release404636315/v0.3.10-test prerelease=true/draft=false, đủ APK615337000/
checksum615337001/manifest615337005. Artifact11410125645 đã tải; APK thực
com.tht.split/code22/0.3.10-test/min26/target32, kích thước252701 bytes.
SHA371ac9ffb73ae2fb4a5fee48fec24414c367cec87ca3e2ecc3d6b518d679d32d
khớp checksum/manifest/GitHub digest. apksigner verify hai APK0.3.9 và0.3.10
cùng cert e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2.
Toàn bộ assets byte-identical0.3.9; chưa thử cập nhật cài đè Android/LDPlayer/xe,
chưa xác minh Worker thật đã nhận bản mới. Bản sẵn sàng cho chủ dự án thử.


## Worker mật khẩu ngắn hơn — chốt14:43
ADMIN_TOKEN/ADMIN_PASSWORD chấp nhận8..256 ký tự, health/admin gate cùng kiểm;
8 ký tự dùng được,7 và257 bị từ chối. UI ghi rõ8..256. Mật khẩu cũ≥32 vẫn dùng.
Không đổi CONTROL binding/class/namespace/wrangler vars hoặc schema/key/defaults,
không migration/reset/delete data; thay code trong Worker chiamanhinh hiện tại.
State/minimum/source/cache/records cũ/Secrets được giữ. Không đổi APK/code21,
keystore/ADB/Bridge/Voice/UI Android. README có cách nâng cùng Worker và giữ binding.
Code0bf463983c357e6145208faa77b81e5ba4a66338.24 Node tests local và Worker CI
37431737965/job112163937103 completed/success, dry-run/bundle đạt.
Artifact11397351102 khớp3 tệp bundle nguồn; CONTROL/config byte-identical bản
trước. Đã kiểm cận7/8/32/256/257, mật khẩu sai bị401, alias8 ký tự và snapshot
config khóa/min/source/cache/legacy records không bị ghi/đổi khi đăng nhập.
Chưa deploy vào tài khoản CF của chủ dự án; không khẳng định dữ liệu tài khoản
thật đã kiểm. Hướng dẫn nâng đúng Worker/binding/namespace trong ZIP.

## Worker GitHub API — chốt14:17
Đã bổ sung server/github.mjs, cache Durable Object riêng và proxy /api/apk/<id>.
Vars GITHUB_REPO/GITHUB_INCLUDE_PRERELEASE/RELEASE_CERT_SHA256; Secret GITHUB_TOKEN
Contents: Read đúng repo private. ADMIN_TOKEN vẫn giữ, ADMIN_PASSWORD hỗ trợ alias.
Pin cert cố định tht-split; nhận dạng hex hoa/dấu : hoặc thường. Không có khóa/PAT
trong git hoặc APK. update.json được workflow tạo từ APK/signature thực cùng
checksum và upload 3 assets vào draft trước publish; Worker đọc Releases list
để chọn cả prerelease khi bật, bỏ draft và bản legacy thiếu manifest.
Metadata/cache không ghi đè global lock/minimum; refresh xong đọc lại policy.
Cache5 phút/persistent + single flight; lỗi GitHub giữ bản tốt. Endpoint APK chỉ
cho 5 assets được duyệt trong đúng repo/cert/channel; API auth không chuyển sang
CDN, không trả redirect/signed URL/header token, stream≤64MB/đúng kích thước.
Worker kiểm cert manifest từ CI, app kiểm cert APK thật; không gọi pin metadata
là xác minh binary chứng chỉ phía Worker. Manual source còn dùng được; admin
có chọn GitHub/nhận -test/kiểm ngay/mandatory. Không đăng ký hoặc license/VIN.
Worker tương thích app≥0.3.6; không đổi app/src, Bridge sleep1/12poll/UI/Off/Voice.
CI Worker riêng để Node tests/dry-run/ZIP không phụ thuộc lỗi ký Android.
22 Node tests local đạt (14 GitHub/security/cache/race/stream mới +8 cũ).
release-metadata.py đã thử bằng bản sao APK0.3.8 ký khóa tht-split thật: package/
code20/name/SHA/size/cert đúng, pin sai bị từ chối. APK ký thử xóa sau kiểm tra,
không phải một bản phát hành0.3.9 hoặc thử cập nhật Android trên thiết bị.
Code c48179f6a0520855f0ffe429ff948077287140b6. Worker CI37430158450/
job112158886834 completed/success: Node22 tests, Wrangler dry-run, bundle/ZIP đạt.
Android debug37430158472/job112158886529 cũng completed/success; signer pin đạt.
Release37430158461/job112158887289 completed/success: unit/lint/signature/metadata/
publish đạt. Lỗi key password đã hết ở lần chạy mới, không đọc Secret thực từ API.
Release https://github.com/meomeo2099/THT-Split/releases/tag/v0.3.9-test,
id404436462/prerelease=true/draft=false, đủ APK/sha/update.json. APK code21/min26/
target32 đã aapt/apksigner verify và đối chiếu hash/metadata/GitHub digest.
APK SHA256:5ae67af5ad1a047757b41be4a61cb595d65c0a59da7d5d57f875858171d8635a.
Cert SHA256:e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2.
QR assets byte-identical0.3.8; app/src không đổi, dex có remote sleep1/API config/
Worker origin. Release tối ưu tên resource nên không so tên resource như debug.
Worker ZIP giao kèm README cập nhật trạng thái phát hành (mã/config đúng artifact
đã CI); chưa deploy Cloudflare vì chưa có credential ở phiên làm việc. Chưa thử
private GitHub API bằng token Worker thật hoặc cập nhật Android/LDPlayer/xe.

## Mốc05e7ffe/0.3.9 — bắt đầu khóa ký cố định (lịch sử)
Đã tạo keystore riêng ngoài git, alias tht-split/RSA3072/JKS. Chủ dự án xác nhận
đã thêm 4 Secrets THT_KEYSTORE_B64/THT_STORE_PASSWORD/THT_KEY_ALIAS/THT_KEY_PASSWORD.
SHA256 chứng chỉ công khai:
e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2
Không có keystore/Base64/mật khẩu trong repository. CI test và release buộc đủ
Secrets và đúng chứng chỉ trên; thiếu/sai thì dừng, không quay lại runner debug key.
VersionCode21/0.3.9-test. Runtime/ADB/Bridge/Off/Voice/UI/assets/Worker giữ0.3.8.
Release tự chạy khi main thay app/build.gradle, lấy tag từ versionName; vẫn cho
chạy thủ công nhưng tag phải khớp. Bản có hậu tố là prerelease. Build/unit/lint,
QR ngân hàng/Zalo, Node8 và dry-run phải đạt, xác minh APK signer trước publish.
Có artifact release + APK/SHA trên Releases để tải và cấu hình Worker thủ công.
Ở mốc05e7ffe, Worker chưa tự lấy GitHub; đã được thay bởi mốc Worker GitHub phía trên.
Code05e7ffe8ec6cc43779fcf6bd96f23c85fcac704a. Release run37428056253 /
job112152153719: Node8, QR ngân hàng/Zalo, dry-run, pin chứng chỉ, unitRelease và
lintRelease đạt; packageRelease thất bại "Cannot recover key". Debug run37428056259 /
job112152153353 cũng thất bại packageDebug. Keystore/store password/alias đã mở
được và export đúng cert; key password trên GitHub không mở được PrivateKeyEntry.
Gói tạo ban đầu đã giải mã/ký thử/verify bằng apksigner thành công ngoài git.
Cần chủ dự án cập nhật THT_KEY_PASSWORD bằng nội dung đúng tệp cùng tên trong
gói đã nhận, không thay keystore hay dùng store password cho key password.
Lần chạy đầu chưa tạo APK0.3.9 hoặc Release; lần mới đã đạt theo mốc phía trên.
Không cần bump code/tag hoặc sửa runtime để sửa Secret.
Chưa kiểm trên LDPlayer9/xe, chưa xác nhận hết lỗi ADB.
Bản debug cũ khác signer không cài đè. Người dùng sao lưu cấu hình cần giữ trước
khi tự chuyển một lần; app không tự gỡ bản cũ. Giữ khóa này cho mọi bản sau.
Repo hiện private (đã kiểm metadata06/10); browser_download_url của Releases
không dùng trực tiếp làm apkUrl cho khách. Worker GitHub API/proxy cần triển khai
riêng với credential chỉ ở Worker. Không nhúng PAT trong app hoặc đổi repo public.

## Quy tắc đã lưu theo yêu cầu chủ dự án —06/10 13:48
Đã đưa bài học startup Bridge vào AGENTS.md, REQUIREMENTS và NEXT_AGENT_PROMPT
để các lượt tiếp theo đọc trước khi sửa. Giữ sleep1 trong remote shell +12 poll,
không thay bằng chờ phía app sau close. Mọi tối ưu thay chuỗi này cần phép thử
LDPlayer9 sau reboot mà không mở Bridge thủ công, so cùng cấu hình/bản cũ;
CI không chứng minh tương thích thiết bị.0.3.8 vẫn chưa được người dùng xác nhận
hoạt động; không ghi race nohup/PTY là nguyên nhân chắc chắn. Mốc này chỉ tài
liệu, không thay code/version/APK hay chạy lại CI.

## Mốc0.3.8 — khôi phục khởi động Bridge theo0.3.4
Chủ dự án xác nhận0.3.4 chạy bình thường trên LDPlayer9, bản mới tự mở Bridge
thất bại. Ảnh0.3.7: ADB đã nhận bootstrap marker nhưng listener39871 refused;
log Bridge người dùng nói trống. tht-adb-log.txt gửi lại là log05/10 14:48 của
lỗi CRT đã sửa0.2.9, không dùng làm nguyên nhân lỗi0.3.7.
Đối chiếu GitHub code27bbd74229c75ee42393c519a493c1fcf1fab77d: ShellClient,
ShellServer6/transport/RSA không có thay đổi chức năng. Khác biệt đáng chú ý:
0.3.4 dùng "sleep 1; echo THT_BOOTSTRAP_SENT" bên trong remote shell.0.3.5 bỏ
sleep,0.3.5 bổ sung16 poll sau shell đóng để giữ tổng~4s. Hai cách không tương
đương về vòng đời shell/PTY, dù tổng thời gian gần nhau. AOSP Android9 classic
shell không protocol dùng PTY, master đóng gửi SIGHUP. Race trước khi nohup
ổn định là giả thuyết hợp lý, chưa được xác minh trên LDPlayer9.
0.3.8/code20 khôi phục đúng sleep1 bên trong shell và12×250ms PING như0.3.4;
không tự mở lặp/kill tiến trình/đổi token/RSA/bỏUID/version/checksum. Bridge đã
sẵn sàng vẫn trả ngay qua PING đầu. Không đổi UI0.3.7, liên hệ, donation/icon,
Worker/public policy/Voice/Off/task engine/overlay; giữ progress và networkIo.
CI37425327271 unit/lint/build/Node8/QR/dry-run đạt; code9c2f57f40b5d1a373036fa607b460e2c4acee9a1.
APK/code20/cert kiểm xong, remote grace có trong dex; QR/icon giữ nguyên0.3.7.
Signer khác0.3.7 nên không cài đè, xem VALIDATION. Cần thử LDPlayer9 sau reboot
mà không mở bridge thủ công, so cùng cấu hình0.3.4. Không khẳng định mọi lỗi
Bridge đã hết từ diff/build.

## Mốc mới0.3.7 — phản hồi ADB chậm và liên hệ
Ảnh06/10 báo ErrnoException/isConnected ECONNREFUSED; ảnh không cho biết
endpoint. Không khẳng định lỗi thiếu ADB hay token chỉ từ ảnh. AOSP IoBridge
bọc errno bằng ConnectException, đường bootstrap hiện tại đã xử lý dạng này.
Lỗi trước bị FailureText unwrap mất giai đoạn/cổng. ConnectionFailure giữ context
kết nối TCP, xác thực và bootstrap; root cause vẫn giữ và secrets được redacted.
LocalAdb báo đúng host:port + hướng bật ADB TCP/cổng nội bộ khác cổng máy tính.
Không tự bật adbd, dò cổng, đổi token/RSA hoặc bootstrap khi lỗi token/version.
MainActivity tách networkIo cho refresh/tải cập nhật khỏi executor kết nối;
kiểm tra hiện dialog tiến trình ngay, cập nhật stage500ms, chặn bấm lặp và đổi
endpoint/method trong lúc kiểm tra. Nút Ẩn chỉ giấu dialog, không hủy xác thực.
Giữ timeout TCP3,5s/auth20s/read12s/bootstrap20s/PING~4s, cho phép RSA lần đầu.
Không thay Bridge v6/engine/overlay/Off/Voice/Worker/policy dùng ngay0.3.6.
Ủng hộ thêm Liên hệ THT: QR Zalo PNG gốc do chủ dự án gửi, không sửa/crop;
FB đúng https://www.facebook.com/100004637940665, mở/copy khi người dùng bấm.
QR offline/nền trắng/FIT_CENTER; màn rộng hai card, hẹp xếp dọc/cuộn.
VersionCode19/0.3.7-test/code fe384fcf725e0ff722708c53e619d0de5af97903.
CI37423152988 unit/lint/build/Node8/QR/dry-run đạt; APK/cert/code kiểm xong,
Zalo asset nguyên bytes, đọc được136px; QR MB/icon giữ nguyên0.3.6.
Chưa test LDPlayer/xe, không khẳng định cổng ADB thật đã hoạt động.
Signer khác0.3.6, không cài đè. Xem VALIDATION và DEVICE_TEST.

## Mốc mới — bỏ đăng ký để ai cài cũng dùng ngay
Chốt mới của chủ dự án bỏ đăng ký từng thiết bị.0.3.6/code18 dùng GET /api/config
công khai; không đọc Android ID/model hay sinh/gửi credential. Không có mã thiết
bị/UI đăng ký/API heartbeat/lease. Máy mới offline/Worker Hello World vẫn chạy.
Cache tht_public_policy mới không dùng khóa/lease per-device cũ; giữ preset,
Voice/Bridge token/RSA và signer requirements. Không xóa hồ sơ server cũ nhưng
không đọc/liệt kê hoặc dùng chúng. Route đăng ký/admin-device/reenroll trả410.
Giữ yêu cầu trước về cập nhật bắt buộc và khóa/mở chung toàn app: mặc định
enabled/min0. Config đã nhận locked/min vẫn giữ khi lỗi mạng; Stop/Home/Hide
thoát được. GET config không ghi storage; admin writes có secret. CONTROL/name
chiamanhinh giữ namespace, health protocol2/registrationfalse. README/REMOTE_CONTROL
đã viết lại cho cơ chế dùng ngay. Các mốc đăng ký bên dưới là lịch sử đã bỏ.
Node8 tests local/CI đạt; Java4 tests public policy cùng unit/lint/build Android
đạt. CI: https://github.com/meomeo2099/THT-Split/actions/runs/37421410376
Code c36f6f0341c7339b5b408cd2f3ebd26cdd2bc25c/code18; QR/Wrangler dry-run đạt.
APK đã kiểm package/version/cert, origin/API công khai, không còn id/đăng ký/lease
trong toàn bộ dex; icon/QR bytes giữ nguyên0.3.5. Signer test khác0.3.5, không cài
đè bản đó. Xem VALIDATION. Chưa deploy CF/test xe; chia/backend/overlay không đổi.


## Mốc Worker thật, launcher icon và giảm chờ
Chủ dự án cung cấp https://chiamanhinh.mavoice.workers.dev. GET / và POST
/api/device với {} kiểm06/10 trả200 text/plain Hello World, chưa phải API Split.
0.3.5-test/code17 đã đặt origin này làm mặc định Gradle và CI/release; override
THT_CONTROL_URL vẫn được giữ. Không nhúng admin credential, không bỏ gate để
chạy khi Worker sai. Máy mới cần online đăng ký; phải deploy mã server trước.
Wrangler name=chiamanhinh; CONTROL SQLite giữ dữ liệu khóa. Thiếu storage/admin
secret thì API503; /api/health chỉ báo tình trạng cấu hình, không lộ secret.
server/pack.mjs tạo module Worker một tệp và gói cùng README/wrangler qua CI.
Chưa có quyền đăng nhập Cloudflare để deploy; chưa có keystore ổn định.

Launcher mới là vector từ mẫu "THT chia đôi trên nền xanh đêm.png" đã chọn:
khung bo góc cyan, hai nền xanh đậm, THT trắng/H chuyển cyan, vạch dọc giữa.
Adaptive foreground/background riêng, nội dung chính trong vùng an toàn;
đã xem preview mặt vuông/tròn, chưa xem launcher Android thật. Không sửa icon
toolbar/grip. Notification tiếp tục dùng tài nguyên icon_foreground cũ.

Tối ưu: command dùng policy cache khi còn hợp lệ, không đợi lock/network;
service kiểm Worker ngay khi tạo và mỗi phút trên executor riêng, request mạng
vẫn giới hạn5 phút. Unknown/hết lease/locked/min version phải refresh/check và
vẫn chặn nếu lỗi mạng. Stop/Home/Hide thoát được. Poll không xếp sau lệnh task;
khóa nhận được dừng overlay. Bỏ sleep1s cố định khi bootstrap; vẫn đợi bridge
PING + kiểmUID0/2000/version6/token theo timeout cũ. Không đổi mã Bridge v6.
Đổi ratio giữ window toolbar/mini/grip, cập nhật màu và vị trí; khi kích thước
màn đổi hoặc trạng thái khác vẫn render đầy đủ. Bỏ resolve component đôi trong
SplitEngine và render UI hai lần lúc mở MainActivity. Không sửa luồng task,
mode/bounds/verified/Off policies; chưa đo độ mượt hoặc thử LDPlayer/xe.

Node9 tests local/CI đạt, thêm3 Java tests quyền cache cùng bộ unit/lint/build
Android đạt. CI: https://github.com/meomeo2099/THT-Split/actions/runs/37420223825
Code4f1b98dac048e9a5e6bbbed5f292a639935ba699, code17. QR/Worker dry-run đạt;
đã kiểm APK signer/package/code17/Worker origin trong toàn bộ dex/icon resources
và QR bytes. Signer test khác0.3.4, không cài đè bản đó. Xem VALIDATION.
Bootstrap vẫn có khoảng4s chờ PING như trước, trả ngay nếu bridge sẵn sàng;
không có sleep1s cố định. Chưa có benchmark/LDPlayer/xe hay deploy Cloudflare.

## Mốc sửa nền hộp thoại và chữ cài đặt
Ảnh phản hồi cuối05/10: hộp Bật freeform có chữ lẫn trang phía sau, thiếu nền
opaque rõ ràng. Rà tất cả callsites dùng Ui.dialog/AppUi.dialog, gồm cấp overlay,
bật freeform, đổi transport, token/đổi token, nhập px/ADB, sửa/xóa cặp, kết quả
kết nối, cập nhật/lỗi, chọn app và dialog toolbar.
Ui.dialog tạo window nền navy FF112233 + viền/bo16dp, dim72%, width≤720dp và
chừa20dp hai mép, adjustResize khi bàn phím. Theme có drawable nền/floating/dim
tường minh; không chỉ dựa colorBackground của theme/OEM. Không gắn OnShowListener
chung để tránh ghi đè validation Lưu trong editPair/ADB hay màu đỏ Xóa.
Custom view không có ListView được bọc vùng cuộn, cao tối đa55% màn; message/list
native vẫn giữ cơ chế cuộn. AppPicker giới hạn width/height theo màn, tên/package
chung cột có weight để không tràn ngang. Filter/chọn package giữ nguyên.
Spinner chọn/dropdown sáng trên navy, tối đa2 dòng; bỏ ép control48dp để nhãn
được đo đủ chiều cao. Nút cài đặt nhiều chữ cao theo nội dung, tối thiểu52dp;
dòng setting/opacity co giãn, màn<600dp tách nhãn và ô chọn thành2 hàng.
0.3.4-test/code16: CI build/unit/lint, Node tests, QR/profile/CRC và Wrangler dry-run
đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37345958636.
Code27bbd74229c75ee42393c519a493c1fcf1fab77d. APK kiểm package/code/signature,
QR bytes nguyên trạng; signer khác0.3.3 nên không cài đè bản đó. Xem VALIDATION.
Chưa chạy UI bản sửa trên LDPlayer/xe. Service/engine/
backend/Bridge v6/Voice/Store/remote/server/QR và căn toolbar trái/phải giữ nguyên;
dialog toolbar hưởng style chung, nội dung thanh/grip không đổi.


## Mốc mới — nhóm nút trái/phải và thao tác cặp rõ hơn
Theo ảnh người dùng và chốt chọn Bên trái/Bên phải: thêm toolbar_side trong
Cài đặt → Thanh điều khiển → Vị trí nhóm nút. Giữ thanh ngang phía trên, chỉ
dồn nhóm ratio/icon sang trái hoặc phải; mặc định phải giữ giao diện cũ.
Áp dụng khi hiện lại thanh/mở cặp; key lưu lâu dài trong Store hiện có.
Không chuyển thanh thành cột, không đổi toolbar height/insets/geometry/backend.
Sửa/Xóa/Mở cặp là3 nút riêng52dp ngay trên card, bỏ menu⋮ khó nhìn theo ảnh.
Xóa màu đỏ/nền riêng và vẫn có xác nhận rõ tên cặp; Hủy không đổi dữ liệu,
xóa chỉ xóa preset. Logic editPair/openPair/Store giữ nguyên.
0.3.3-test/code15: CI build/unit/lint, Node tests, kiểm QR và Wrangler dry-run đạt:
https://github.com/meomeo2099/THT-Split/actions/runs/37343461485.
Code e1dd80bad24ef73b447507c7bb49faf52fd97cc1. Đã lấy APK/kiểm package/code/cert
và bytes QR; xem VALIDATION. Chưa thử thao tác và hiển thị trên LDPlayer/xe.
Signer test khác0.3.2 đã gửi, không cài đè bản đó; không gỡ khi chưa giữ cấu hình.
Không nâng bridge v6 hay cần khởi động lại bridge chỉ vì thêm vị trí nhóm nút.


## Mốc giao diện bên trong — theo chốt23:12
0.3.2-test/versionCode14 làm lại MainActivity theo ảnh mẫu đã tìm lại: header giữa,
menu có icon, card chọn app, cặp lưu, danh mục cài đặt bên trái trên màn rộng.
Trang ủng hộ có card thông tin MB Bank/copy STK và card QR nền trắng bên cạnh;
màn nhỏ xếp dọc, nội dung và danh mục có cuộn. Giữ nguyên profile/bytes QR0.3.1.
AppUi mới chỉ MainActivity dùng. Không sửa Ui, SplitService, overlay/grip, backend,
Bridge v6, API Voice, Store hay remote/server; không thay launcher icon.
CI build/unit/lint, Node tests, kiểm QR và Wrangler dry-run đều đạt tại code
9c62c0400a0f181c0ccf62f769cdbae6b3ebc9e7:
https://github.com/meomeo2099/THT-Split/actions/runs/37340679665.
APK đã lấy từ artifact, kiểm package/version/signature và bytes QR; xem VALIDATION.
Chưa chạy UI trên LDPlayer/xe; cần kiểm tương phản/cắt chữ/danh mục/sao chép/QR/chọn app/cặp.
Chữ ký test vẫn phụ thuộc runner nếu chưa cấu hình keystore ổn định; không tự gỡ
app đang dùng hoặc hứa cài đè giữ dữ liệu. Worker thật chưa có URL/deploy.
Rà bố cục màn nhỏ: nút tỷ lệ giữ một dòng, padding2dp và vùng bấm48dp để tránh
nhãn30:70 bị xuống dòng khi năm nút chia đều chiều ngang.

## Mốc mới nhất — ủng hộ dùng ngay, bỏ duyệt thiết bị
Chủ dự án đã cung cấp STK/holder/link VietQR và yêu cầu nhúng vào app.0.3.1
(versionCode13) dùng DonationProfile công khai + assets/donation-qr.jpg; QR/STK
hiển thị ngay cả khi offline hoặc chưa cấu hình Worker. Giữ nguyên bytes ảnh đã
lấy từ link của chủ dự án (response thực là JPEG490×490). Đã decode QR, đối chiếu
account/BIN và checksum EMV CRC; không tạo/sửa nội dung QR hoặc xác minh tên chủ
qua ngân hàng. Holder dùng đúng nội dung chủ dự án cung cấp.
Worker: thiết bị mới enabled ngay, bỏ autoApprove/duyệt; UI thiết bị chỉ Khóa/Mở
khóa. Pending cũ được migrate thành enabled khi heartbeat, locked giữ nguyên.
Khôi phục credential API reenroll không chuyển enabled về pending và không xóa
locked; vẫn bảo vệ identity không cho credential khác ghi đè. Bỏ cấu hình ủng hộ
trong Worker vì profile/QR nằm trong APK. Giữ cập nhật bắt buộc đã yêu cầu trước.
App chấp nhận policy pending cũ như enabled để không kẹt quy trình duyệt cũ;
unknown state vẫn bị chặn. Stop/Home/Hide thoát được, cache locked không tự xóa.
Người dùng xác nhận0.2.9 ADB và test OK LDPlayer9; chưa xác nhận reboot/xe thật.
Giữ bridge v6 và backend task. Chưa deploy Worker, chưa có URL/signer ổn định.
Node7 regression tests local/CI, kiểm QR đúng profile/CRC, Wrangler dry-run,
build/unit/lint đều đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37301624495.
Code1bc8b14446be4e19797feffc5d449728ac9d43b5; APK0.3.1-test/code13.
APK test đã có STK/QR nhưng chưa cấu hình Worker thật. Signer CI khác0.3.0;
không hứa cài đè hoặc giữ dữ liệu nếu buộc gỡ/cài lại. Chưa thử LDPlayer/xe.

## Đọc trước khi tiếp tục
AGENTS.md → REQUIREMENTS.md → ARCHITECTURE.md → REFERENCE_NOTES.md.
Ghép Voice đọc VOICE_API.md và THTVOICE_INTEGRATION.md; thử xe đọc DEVICE_TEST.md.
Không làm lại UI, preset hoặc bộ chia đã có. Lịch sử commit là nguồn xác định mốc tiếp tục.

## Quyết định mới của chủ dự án
Chỉ giữ ADB nội bộ, Off theo cơ chế mở ActivityOptions và quyền hệ thống có điều kiện.
Không triển khai Telnet/Shizuku. Mỗi phiên chỉ dùng một phương thức; phải tắt phiên
trước chuyển backend/cổng. Mục tiêu không nhập lệnh máy tính mỗi lần lên xe.
Commit tiếng Việt và hướng dẫn trên GitHub để dự án Voice tiếp tục làm client.

## Đã có mã
| Phần | Kết quả |
|---|---|
| UI/preset/overlay | Giữ chọn hai app, CRUD, ẩn/hiện, tỷ lệ, đổi bên, fullscreen, Home/X |
| ADB nội bộ | Client RSA TCP original; khóa private lưu noBackupFiles; localhost mặc định, cổng chỉnh được; chỉ địa chỉ chính thiết bị |
| Bridge v6 | Tự mở khi chưa có listener, token riêng, kiểm UID/version; không tự kill tiến trình hoặc bật adbd |
| Boot/cập nhật | Foreground ConnectionService chuẩn bị bridge nếu tùy chọn bật; khi lệnh tới tiếp tục thử kết nối |
| Off | ActivityOptions freeform với bounds và scoped hidden API access; status=requested; không giả lập resize task |
| Hệ thống | Kiểm MANAGE_ACTIVITY_STACKS trước chọn/chạy; gọi backend trực tiếp, không shared UID trong APK thường |
| Voice API v2 | Token tương thích v1 hoặc receiver bảo vệ signature; requestId + caller-owned PendingIntent; lỗi/busy/timeout ở phía client |
| Hướng dẫn Voice | THTVOICE_INTEGRATION.md có manifest/client Java/mapping/callback và các phần cần sửa ở Voice |

## Giới hạn thật
- Người dùng đã thử 0.2.0 trên LDPlayer bằng Off: chia được, tỷ lệ bị khóa. Bản sửa chưa được thử LDPlayer/xe.
- ADB nội bộ hỗ trợ ADB TCP RSA cổ điển, không ADB TLS/pairing Android 11+.
- Phải xác nhận khóa ADB lần đầu nếu firmware bật xác thực. Không tự vượt hộp thoại.
- ADB phải được firmware bật sau reboot; app không tự mở khóa/bật adbd.
- Boot có thể đến trước adbd/Wi-Fi; sẽ thử lại khi chia. Force-stop app có thể chặn boot receiver.
- Bridge 0.1 cũ đang chạy cần reboot màn để nạp bridge v2; không tự kill theo tên chung.
- Off chỉ yêu cầu hệ thống mở cửa sổ, không xác minh được task của app khác;
  từ 0.2.6 khóa ratio/drag/swap/replace/fullscreen/restore cả UI lẫn lệnh Voice.
  Không force-stop/xóa dữ liệu/tạo MULTIPLE_TASK. Hide/show chỉ đổi overlay.
- Hệ thống vẫn cần chữ ký/quyền phù hợp; không có khóa platform được tạo hoặc đưa vào git.
- Backend quyền hệ thống là nhánh có điều kiện, không phải APK debug tự có quyền.
- Native Android 9 ưu tiên; reflection Android mới/OEM có thể khác và báo lỗi.
- API Voice đã có phía Split; chưa sửa repo THTVoice. Không nói đã tích hợp Voice hoàn chỉnh.
- Popup, HVAC, tìm đường Vietmap thuộc Voice; Split không làm phần đó.
- Đã thêm thư viện LSPosed v6.1 có Apache-2.0, dùng bản source có ghi rõ adaptation và
  license trong APK; không copy mã GSplit không rõ giấy phép.

## Kiểm tra cần tiếp trên xe
1. Cài APK cùng signer bản test trước; cấp overlay, chọn ADB nội bộ/cổng thực tế, Kiểm tra.
2. Xác nhận khóa nếu có → bridge UID2000/version6 → thử native/freeform.
3. Reboot thật, đổi mạng Wi-Fi → gọi chia; ghi log/cổng/ảnh/insets/mode thật.
4. Bật freeform bằng ADB lần đầu → chọn Off → quan sát cả hai app, thử swap/stop/fullscreen.
5. Kiểm certificate/quyền platform trước thử system; không suy ra quyền từ cài được APK.
6. Dự án Voice triển khai client theo THTVOICE_INTEGRATION.md rồi test end-to-end.

## Build
JDK17/SDK35/Gradle8.9/AGP8.7.3. `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
Chi tiết kết quả thật ở VALIDATION.md. Khóa ký release/token/khóa ADB không ở git.
APK CI dùng debug key của runner; không đảm bảo cập nhật đè bản test ký máy khác.

## Bằng chứng máy phát triển ở mốc0.2.0
Build APK0.2, 17 unit tests và lint đạt (0 lỗi). Apksigner v2 hợp lệ;
compile-only Unsafe stub không có trong APK. Xem VALIDATION.md cho giới hạn và checksum.

## Mốc sửa sau phản hồi LDPlayer
- 0.2.1: bỏ khóa tỷ lệ Off; vạch kéo48×80dp, gửi yêu cầu khi thả, không resize liên tục.
- Màn chờ hiển thị trước khi worker mở app, dọn khi thành công/lỗi/hủy service;
  thanh điều khiển chỉ trở lại sau backend và khoảng ổn định tùy chọn.
- Bỏ moveTaskToBack ngay khi bấm; không lộ launcher khi backend chưa mở app.
- Chưa chứng minh bounds task cũ đổi được trong Off. Test theo DEVICE_TEST.
- Cache phụ thuộc Gradle máy phát triển không còn đủ; CI0.2.1 build/unit/lint đạt:
  https://github.com/meomeo2099/THT-Split/actions/runs/37261177497. APK CI khác signer có thể không cài đè bản cũ.

## Phản hồi và sửa0.2.2
- Người dùng thử0.2.1 Off trên LDPlayer: UI Split còn sau hai app, kéo không đổi cửa sổ.
- Khôi phục moveTaskToBack sau khi gửi lệnh start/preset để nền là launcher như bản cũ.
- Bỏ mặc định chờ thêm1,5s; mặc định0ms, vẫn giữ tùy chọn chờ nếu người dùng chọn.
- Dấu kéo hiển thị12×32dp, nền còn lại trong suốt; vùng nhận touch vẫn48×80dp.
- Gesture dùng độ dịch chuyển từ vị trí bắt đầu + touch slop; chạm đơn không gửi ratio.
- Off vẫn chỉ gửi lại launch bounds, không có quyền resize task. Toast khi đổi tỷ lệ
  chỉ dẫn ADB nếu cửa sổ không đổi. Không xác nhận LDPlayer là nguyên nhân.
- Chưa chạy bản0.2.2 trên LDPlayer/xe; cần quan sát nền launcher và handle khi thử.

CI0.2.2 build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37262534677.
APK CI0.2.2 khác signer bản0.2.1; không gỡ bản cũ khi chưa giữ cấu hình.

## Điều chỉnh0.2.3
Chừa bên phải thanh40dp thay8dp: nhóm nút dịch trái32dp, giữ kích thước nút.
Theo yêu cầu người dùng để dễ với tay từ ghế lái. Chưa quan sát trên xe.

## Sửa tương phản0.2.4
Ảnh người dùng: dialog Toàn màn hình từ thanh công cụ chữ đen trên nền navy.
- Mọi AlertDialog dùng SplitDialogTheme rõ ràng qua Ui.dialog, gồm cả Service overlay.
- Chữ chính trắng, chữ phụ/hint sáng, nhấn chọn và nút cyan; không phụ thuộc theme OEM.
- Spinner cài đặt và tỷ lệ preset dùng adapter riêng cho cả dòng chọn/dropdown:
  chữ sáng/nền navy, popup cùng palette. Ô nhập ADB/insets/preset/tìm app cùng màu.
- Chưa quan sát bản sửa trên LDPlayer/xe; kiểm title/dòng chọn/ô nhập/button/dropdown.

## Sửa phục hồi cửa sổ0.2.5
Ảnh LDPlayer: đổi bên báo duplicate split-screen-primary; đóng một app rồi đổi tỷ lệ
báo thiếu task; khôi phục fullscreen gây chồng app/ô trống.
- Backend ADB/system dọn primary đang có trên màn chính trước khi dựng cặp.
- Native mở/reuse cả hai app ở fullscreen trước, sau đó đặt trái3/phải4 rồi resize.
- RESIZE kiểm task/mode; app đã đóng hoặc mode bị Android thay đổi thì dựng lại cặp.
- resizeDockedStack dùng bounds cuối, bỏ các task/inset bounds tạm của animation.
- FULLSCREEN/STOP đưa app được chọn ra trước qua launcher intent, giữ dữ liệu/task.
- PairController có regression tests cho swap, task thiếu, fullscreen, thay app,
  resize bình thường, freeform và firmware từ chối mode. Không force-stop/removeTask.
- Bridge nâng v3: cài APK không thay mã bridge đang chạy. Reboot màn/LDPlayer một lần
  rồi kết nối lại; nếu ADB nội bộ vẫn lỗi phải chạy lệnh bridge thủ công sau reboot.
- Off vẫn có giới hạn launch bounds; sửa này không chứng minh Off resize task được.
- Chưa kiểm bản0.2.5 trên LDPlayer/xe. CI build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37269596828.

## Sửa cửa sổ dùng chung stack và giới hạn Off0.2.6
Ảnh người dùng0.2.5: ADB/native có Maps bên trái, cửa sổ YouTube/freeform và trang
cấu hình THT còn ở bên phải. Video7,8s: Off mở lại thành YouTube fullscreen.
- Android9 setTaskWindowingMode thay mode của cả stack. Bản0.2.5 chỉ kiểm mode
  từng task nên chưa bắt được lỗi chung stack. Fake tests cũ cũng chưa mô hình hóa điều này.
- PairController tách từng task vào stack riêng trước setMode; bridge dùng
  createStackOnDisplay + moveTaskToStack khi stack có nhiều task, kiểm hai stack khác nhau.
  Reuse stack riêng nếu đã có, không removeTask/force-stop app bên thứ ba.
- SplitService đóng/remove riêng các AppTask thuộc THT Split khi mở cặp (cả UI/Voice),
  service và cấu hình vẫn giữ. Không để trang cấu hình bị kéo vào stack của app khác.
- Off bỏ LAUNCH_ADJACENT để không dùng stack đang focus thay ActivityOptions.
- Off X chỉ dừng overlay, không gửi fullscreen hai app như trước; giữ cửa sổ cho lần mở tiếp.
  Cửa sổ đã sai từ bản cũ cần đóng bằng Android một lần; Off không sửa được task cũ.
- Off khóa ratio/swap/replace/fullscreen/restore; bỏ grip, không tô tỷ lệ như đã xác minh.
  Giữ mở cặp, hide/show, Home/X. Voice trả UNSUPPORTED_OPERATION cho lệnh bị khóa.
- Native dùng setTaskWindowingModeSplitScreenPrimary với showRecents=false;
  sau dựng cặp focus app phải để không dừng ở màn chọn app gần đây.
- Bridge v4 cần reboot/kết nối lại để nạp code; không kill listener cũ.
- Thêm test stack-wide mode, task reuse bỏ qua launch mode, shared freeform và policy.
- Chưa thử bản sửa trên LDPlayer/xe; CI build/unit/lint đạt:
  https://github.com/meomeo2099/THT-Split/actions/runs/37271611577.

## Sửa khoảng chừa native0.2.7
Ảnh LDPlayer0.2.6: Maps trái có vùng đen trên/dưới; YouTube phải full chiều cao
và bị toolbar đè. Code native chỉ resizeDockedStack với bounds trái; Android tự
tính secondary theo display, chưa áp bounds phải.
- Sau dock resize, resizeStack của app phải với bounds phải/allowResizeInDockedMode
  true/preserveWindows false/animate false. Không gọi resizeTask cho native vì
  API Android9 có thể chuyển task sang freeform hoặc từ chối.
- Đọc getTaskBounds cả hai sau layout/resize, đợi tối đa400ms nếu đang thay đổi.
  So sánh bốn cạnh với sai số2px; mode đúng chưa đủ để verified=true.
- Backend giữ verified từ bridge, không ghi đè true. Nếu firmware không giữ bounds,
  callback requested + note; UI báo và log last_layout lưu requested/actual/stack/mode.
- Tests bắt secondary tràn display, tỷ lệ bị snap khác yêu cầu, sai số pixel.
- Bridge v5 cần reboot/kết nối lại. Không thay Off đã khóa task controls.
- CI lần đầu bắt lỗi thiếu khai báo JSONException ở hàm bounds; đã sửa và chạy lại.
  CI bản cuối build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37272679706.
  Chưa thử bản sửa trên LDPlayer/xe.

## Sửa chuyển toàn màn hình0.2.8
Ảnh người dùng: ADB → Google Maps toàn màn hình, UI tìm kiếm/nút đã trải rộng
nhưng lớp bản đồ vẫn chỉ vẽ bên trái, phần còn lại đen. Ảnh không phân biệt được
bounds/configuration cũ với lỗi surface của Maps/LDPlayer; chưa xác nhận nguyên nhân.
- PairController xử lý fullscreen/stop cùng thứ tự: tìm app được chọn trước,
  dọn primary, mode1 cả hai task còn sống, resizeStack(null) cả hai với
  preserveWindows=false để kế thừa display bounds và cập nhật cấu hình.
- Focus task đã có qua setFocusedTask, bỏ launcher intent fullscreen lần nữa.
  Không force-stop/xóa dữ liệu/restart cưỡng bức app bên thứ ba.
- Fullscreen đọc mode/bounds sau thao tác, so với kích thước display thực gửi từ app;
  verified=false nếu không khớp. last_layout lưu expected/actualFullscreenBounds.
  Bounds đúng không chứng minh layer bản đồ đã vẽ; cần kiểm trực quan cả Maps/YouTube.
- Stop fallback app còn lại khi bên trái đã đóng; hai app đã đóng thì không sửa task khác.
- Thêm5 regression tests logic. Bridge v6 cần reboot/kết nối lại bằng lệnh mới.
  CI build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37275295920.
  Chưa thử bản sửa trên LDPlayer/xe; xem VALIDATION cho checksum và giới hạn.

## Sửa đọc khóa ADB Android9 —0.2.9
Log người dùng05/10 14:48:13, Android9 LDPlayer: LocalAdb.loadKey ném
ClassCastException: OpenSSLRSAPrivateKey cannot be cast to RSAPrivateCrtKey.
Lỗi xảy ra trước socket.connect, không phải bằng chứng ADB/TCP/IPv6 không hỗ trợ.
Nguồn0.2.2 cũng có cast này; khác provider có thể giải thích bản Android khác chạy.
- Giữ PrivateKey chuẩn từ PKCS8, không ép RSAPrivateCrtKey. AUTH dùng Cipher với
  PrivateKey; public ADB lấy modulus qua RSAPrivateKeySpec. Nếu provider không có
  CRT interface, dùng F4=65537 của khóa do app tạo; không có import khóa bên ngoài.
- Sinh mới2048/F4 tường minh. Khóa PKCS8 cũ đọc nguyên bytes, không xoay/xóa/tạo
  lại khi lỗi; identity RSA vẫn giữ nếu cập nhật cài đè cùng chữ ký giữ dữ liệu.
- Giữ bridge v6, không đổi socket/cổng/task backend hay nới checksum để che lỗi.
- ConnectionManager/UI/boot ghi nguyên nhân gốc có lọc secrets; Kiểm tra hiện
  UID/API/bridgeVersion. Có thể thu log chỉ bằng Kiểm tra, không phải đóng UI để chia.
- Thêm2 tests khóa non-CRT/PKCS8 identity và2 tests cause/redaction. Unit provider
  JVM không chứng minh Conscrypt/ADB xác thực trên thiết bị; cần thử Android9 thật.
- Sau cài đặt127.0.0.1:5555, Kiểm tra khi chưa mở bridge thủ công, xác nhận RSA nếu
  có; reboot rồi thử tự mở lại. Không yêu cầu chạy lệnh dự phòng như điều kiện test.
- CI build/unit/lint đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37280998296.
  Chưa thử APK sửa trên LDPlayer/xe; xem VALIDATION cho checksum và giới hạn.

