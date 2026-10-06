# Bàn giao THT Split — 0.3.9-test, 06/10/2026

## Mốc0.3.9 — bắt đầu khóa ký cố định
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
GitHub API tự lấy latest trong Worker chưa được triển khai; chưa deploy Worker.
Đang chờ CI/release0.3.9. Chưa kiểm trên LDPlayer9/xe, chưa xác nhận hết lỗi ADB.
Bản debug cũ khác signer không cài đè. Người dùng sao lưu cấu hình cần giữ trước
khi tự chuyển một lần; app không tự gỡ bản cũ. Giữ khóa này cho mọi bản sau.

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
