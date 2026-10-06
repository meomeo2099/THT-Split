# Quản lý THT Split không cần nhập key — 0.3.5

## Worker chủ dự án đã tạo06/10/2026
https://chiamanhinh.mavoice.workers.dev đã được đặt mặc định trong APK và
CI/release. Không phải nhập URL/key ở máy khách. THT_CONTROL_URL chỉ dùng khi
chủ dự án chủ động override lúc build. Kiểm tra thực tế trước lần sửa này:
GET / và POST /api/device với {} đều200 text/plain Hello World, chưa có mã Split.
Chưa đăng nhập/triển khai vào tài khoản Cloudflare của chủ dự án.
Phải triển khai mã server dưới đây trước khi máy mới đăng ký và chia được.

## Chức năng đã viết
- Bỏ ba nút trong app: tài liệu Voice, hướng dẫn kiểm thử và mở trang phát hành.
  Tài liệu tích hợp vẫn được giữ trên GitHub cho dự án THTVoice.
- App tự đăng ký, hiển thị mã thiết bị và được sử dụng ngay, không cần duyệt.
- Trang quản trị khóa/mở thiết bị, đặt lý do, xem model/version/lần kết nối.
- Cập nhật tự kiểm tra khi mở app và lúc đang chia; có nút kiểm tra thủ công.
  `minimumVersionCode` chặn thao tác chia trên bản cũ, không tự cài im lặng.
- APK tải trong app, giới hạn64MB, HTTPS kể cả redirect, SHA256, package/version/
  signer phải khớp. Android vẫn yêu cầu người dùng xác nhận cài đặt.
- Mục Ủng hộ: profile ngân hàng/chủ tài khoản/STK và QR đã nhúng vào APK.
  Sao chép STK, QR hiển thị offline, không phụ thuộc Worker/khóa.

## Triển khai Worker riêng
Thư mục `server/` là Worker ES module và trang quản trị, không chứa mật khẩu.
Yêu cầu Node hiện hành và Wrangler hỗ trợ `exports` Durable Objects SQLite.
Tài liệu Cloudflare: https://developers.cloudflare.com/durable-objects/reference/durable-objects-migrations/
và https://developers.cloudflare.com/workers/configuration/secrets/.

```sh
cd server
npx --yes wrangler@4 login
npx --yes wrangler@4 secret put ADMIN_TOKEN
npx --yes wrangler@4 deploy
```
Nhập mật khẩu ngẫu nhiên dài ít nhất32 ký tự khi được hỏi. Không gửi mật khẩu này
cho khách, không cho vào APK/GitHub/source. Mở URL Worker, đăng nhập bằng mật khẩu.
Worker dùng Durable Object SQLite + transaction: đăng ký/heartbeat không ghi đè
trạng thái khóa do admin đặt. Không dùng KV eventual consistency cho lệnh khóa.
Wrangler name đã đổi thành `chiamanhinh`; đăng nhập tài khoản có subdomain
`mavoice` để thay mã Worker Hello World hiện có. CONTROL được tạo/bind bằng
Durable Object exports SQLite. Không dùng tên khác hoặc xóa namespace cũ.
`server/pack.mjs`/CI có gói Worker.zip gồm module một tệp, wrangler.jsonc và README.
Giải nén và chạy ba lệnh trên trong thư mục có wrangler.jsonc cũng được.
Đừng chỉ dán source trong web editor mà bỏ binding CONTROL.
Sau deploy `/api/health` phải có service=tht-split-control, protocol=1,
storage=true, adminConfigured=true; API thiết bị trả503 nếu thiếu storage/admin.

Trong GitHub repository Settings → Secrets and variables → Actions → Variables,
`THT_CONTROL_URL` chỉ cần nếu đổi origin, **không có dấu / cuối**. Để trống dùng
chiamanhinh.mavoice.workers.dev. Worker trả Hello World thì app báo chưa chạy API;
không bỏ chặn đăng ký lần đầu. Mục ủng hộ vẫn dùng ngay, kể cả offline.

## Khóa ký ổn định và phát hành
Khóa test của runner cũ thay đổi giữa build, không thể bảo đảm cập nhật đè0.2.9.
Phải chọn một keystore ổn định; ưu tiên keystore đã ký APK đang cài nếu còn giữ.
Không đưa `.jks` hoặc mật khẩu vào git. Thêm GitHub Actions Secrets:
`THT_KEYSTORE_B64`, `THT_STORE_PASSWORD`, `THT_KEY_ALIAS`, `THT_KEY_PASSWORD`.
Test CI sẽ dùng khóa này nếu đã cấu hình; Release buộc đủ tất cả secrets.

Workflow “Phát hành APK cùng khóa ký” chạy thủ công với tag phiên bản. Workflow
build/unit/lint trước, đính kèm `THT-Split.apk` và SHA256 vào GitHub Releases.
Tăng `versionCode`/`versionName` trong app/build.gradle trước mỗi bản.
Repo riêng tư: link Releases không tải công khai được; đặt APK trên HTTPS công khai
do chủ dự án quản lý. Không đưa GitHub PAT vào APK để tải private repo.

Trang quản trị → Cập nhật: nhập versionCode/tên bản/URL APK/SHA256/nội dung.
Để version tối thiểu0 cho cập nhật tự nguyện. Chỉ nâng version tối thiểu sau khi
đã thử APK **cùng signer**, đúng package, tải được và cài được trên thiết bị đích.
Version tối thiểu không được vượt version APK đã cấu hình.
Checksum Windows: `Get-FileHash .\THT-Split.apk -Algorithm SHA256`.

## Ủng hộ
Chủ dự án đã cung cấp thông tin MB Bank và ảnh VietQR. `DonationProfile.java` có
profile công khai; `assets/donation-qr.jpg` giữ nguyên ảnh tải từ link được cung cấp.
Ảnh đã được decode, đối chiếu STK/BIN và kiểm CRC EMV. Tên chủ tài khoản dùng đúng
nội dung chủ dự án gửi, không suy ra tên từ QR. Màn ủng hộ không tải ảnh qua mạng,
không cần Worker và không cho metadata server thay QR/STK. Muốn đổi profile sửa
source/asset và phát hành APK. Chỉ thông tin ủng hộ này được chủ dự án cho phép
nhúng; mọi credential đăng nhập/admin/signing vẫn không nằm trong git.

## Hành vi khóa / ngoại tuyến
- Khi online, kiểm tra tối đa mỗi5 phút; service tick ngay khi tạo và mỗi phút trên
  executor mạng riêng, UI lúc resume/thủ công. Lệnh chia có cache quyền còn hạn
  chạy ngay, không đợi request mạng. Thiếu/hết quyền vẫn phải refresh và bị chặn
  nếu không xác nhận được. Không có push nền khi app bị dừng/force-stop.
- Thiết bị mới online lần đầu tự đăng ký enabled, không có bước duyệt.
- Đang mở: ngoại tuyến mặc định7 ngày kể từ lần xác nhận thành công; admin chỉnh
  từ1 giờ đến30 ngày. Hết hạn phải online lại. Lỗi mạng không tự khóa một máy mới
  được xác nhận; cũng không xóa lệnh khóa/cập nhật bắt buộc đã nhận.
- Khóa hoặc bản quá cũ: chặn mở/khôi phục/đổi tỷ lệ/đổi bên/đổi app/show/status ở
  SplitService, gồm UI, Voice và toolbar. Voice nhận `error/ACCESS_BLOCKED`.
- Vẫn cho Stop/Home/Hide để thoát phiên. Khi service phát hiện khóa, dừng overlay
  điều khiển; không force-stop app khác, không tắt màn xe hay xóa dữ liệu.
- Cài lại cùng signer/user: dùng hash Android ID scoped làm mã thiết bị, không gửi
  serial/VIN. Private credential tự sinh, không hiển thị như key cho khách nhập.
  Nếu cài lại mất credential: API quản trị `/api/admin/reenroll` cho khôi phục
  bằng id, giữ nguyên locked/enabled. Không tạo pending hoặc yêu cầu duyệt lại.
  UI quản trị thiết bị chỉ có Khóa/Mở khóa.
- Factory reset, đổi Android user/signer hoặc thiết bị bị can thiệp có thể đổi
  identity. Đây là kiểm soát của app thường, không phải chống sửa APK/root tuyệt đối.
  Mã nguồn công khai có thể bị sửa để bỏ client gate. Không hứa khóa tức thì khi
  thiết bị offline hoặc khóa cả hệ điều hành xe.

## Thử cần làm sau cấu hình
1. App mới online tự đăng ký enabled → mở cặp bằng UI/Voice, không cần admin duyệt.
2. Admin khóa → kiểm tra thủ công hoặc đợi ≤5 phút online → toolbar dừng, chia bị
   chặn; Stop/Home vẫn thoát được. Mất mạng rồi mở lại vẫn giữ khóa.
3. Admin mở → kiểm tra mạng → dùng lại; thử ngoại tuyến trong/hết lease.
4. Bản mới versionCode lớn hơn, đúng SHA/signer; cập nhật tự nguyện trước, cài đè
   giữ preset/token/RSA; sau đó thử bắt buộc với máy còn bản cũ.
5. Sai SHA/signer/package, HTTP redirect, link private, hủy cài đặt: phải báo lỗi,
   giữ bản hiện tại. Không tự gỡ APK cũ.
6. QR/STK đúng, sao chép nguyên STK, QR hiện ngay khi airplane mode/chưa có Worker.
   Migrate pending cũ → enabled; migrate/khôi phục credential không làm mất locked.

Đã có URL Worker và gắn vào APK; chưa deploy mã quản trị vào tài khoản Cloudflare
của chủ dự án, chưa có khóa ký ổn định. Unit/build không thay thế các bước thử trên.
