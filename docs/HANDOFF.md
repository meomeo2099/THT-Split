# Bàn giao THT Split — 0.3.0-test, 05/10/2026

## Mốc mới nhất — quản lý/cập nhật/ủng hộ
Người dùng đã xác nhận0.2.9 kết nối ADB và test OK trên LDPlayer9; chưa xác nhận
reboot tự kết nối lại hoặc EX2 thật. Giữ nguyên bridge v6 và cơ chế cửa sổ.
0.3.0 bỏ ba nút tài liệu/phát hành trong app, giữ tài liệu tích hợp trong repo.
RemoteControl + AccessRules áp quyền dùng ở UI/service/Voice/toolbar, cache lock,
lease ngoại tuyến7 ngày; máy mới pending. APK HTTPS/SHA256/package/version/signer
trước PackageInstaller; không tự cài im lặng hoặc gỡ bản cũ. Ủng hộ lấy QR/STK từ
Worker. `server/` có quản trị duyệt/khóa/cập nhật/ủng hộ và transaction chống ghi
đè lock. Xem REMOTE_CONTROL.md cho triển khai/cấu hình và giới hạn bảo mật.
Chưa deploy Worker, chưa có URL/ngân hàng/QR thật/keystore ổn định. Test build khi
thiếu CONTROL_URL hiện rõ chưa cấu hình và vẫn dùng bộ chia để kiểm thử.
Release workflow bắt buộc endpoint/secrets ký; không coi test APK là bản đã khóa từ xa.
Node server regression đạt6 tests local/CI. CI cuối build/unit/lint + Wrangler
dry-run đạt: https://github.com/meomeo2099/THT-Split/actions/runs/37290015077.
Code commit a12dd9be583537cfb653dca5c85e200ed63b352a; APK0.3.0-test code12.
APK test không chứa endpoint: UI mới dùng được, các chức năng quản trị/ủng hộ chưa
hoạt động thật. Chưa thử APK mới trên LDPlayer/xe, chưa deploy Cloudflare.
Tách khóa identity khỏi khóa mạng để UI không đợi HTTP khi hiện mã thiết bị;
không dựng lại UI nếu policy không đổi, serialize APK download/install qua Activity recreate.

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
