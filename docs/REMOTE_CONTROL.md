# Quản lý THT Split không cần nhập key — 0.3.0

## Chức năng đã viết
- Bỏ ba nút trong app: tài liệu Voice, hướng dẫn kiểm thử và mở trang phát hành.
  Tài liệu tích hợp vẫn được giữ trên GitHub cho dự án THTVoice.
- App tự đăng ký, hiển thị mã thiết bị. Mặc định chờ chủ dự án duyệt; có thể bật tự duyệt.
- Trang quản trị duyệt/khóa/mở thiết bị, đặt lý do, xem model/version/lần kết nối.
- Cập nhật tự kiểm tra khi mở app và lúc đang chia; có nút kiểm tra thủ công.
  `minimumVersionCode` chặn thao tác chia trên bản cũ, không tự cài im lặng.
- APK tải trong app, giới hạn64MB, HTTPS kể cả redirect, SHA256, package/version/
  signer phải khớp. Android vẫn yêu cầu người dùng xác nhận cài đặt.
- Mục Ủng hộ: ngân hàng/chủ tài khoản/STK, sao chép STK, QR ảnh công khai.
  Đổi thông tin qua web quản trị; ủng hộ không ảnh hưởng duyệt/khóa.

## Triển khai Worker riêng
Thư mục `server/` là Worker ES module và trang quản trị, không chứa mật khẩu.
Yêu cầu Node hiện hành và Wrangler hỗ trợ `exports` Durable Objects SQLite.
Tài liệu Cloudflare: https://developers.cloudflare.com/durable-objects/reference/durable-objects-migrations/
và https://developers.cloudflare.com/workers/configuration/secrets/.

```sh
cd server
npx wrangler login
npx wrangler deploy
npx wrangler secret put ADMIN_TOKEN
```
Nhập mật khẩu ngẫu nhiên dài ít nhất32 ký tự khi được hỏi. Không gửi mật khẩu này
cho khách, không cho vào APK/GitHub/source. Mở URL Worker, đăng nhập bằng mật khẩu.
Worker dùng Durable Object SQLite + transaction: đăng ký/heartbeat không ghi đè
trạng thái khóa do admin đặt. Không dùng KV eventual consistency cho lệnh khóa.
URL phải là origin HTTPS, ví dụ `https://tht-split-control.<subdomain>.workers.dev`.

Trong GitHub repository Settings → Secrets and variables → Actions → Variables,
thêm `THT_CONTROL_URL` bằng URL Worker **không có dấu / cuối**.
Không có endpoint thật thì APK test hiện rõ “chưa cấu hình”; chưa kích hoạt khóa,
cập nhật từ xa hay thông tin ủng hộ. Workflow phát hành từ chối thiếu endpoint.

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
Trang quản trị nhập ngân hàng, tên chủ tài khoản, số tài khoản nguyên văn (giữ số0
đầu), URL QR PNG/JPG HTTPS công khai. QR tối đa2MB,2048×2048, không redirect.
Chủ dự án cần cung cấp thông tin/ảnh QR thật; không có tài khoản mặc định hoặc tự
bịa số tài khoản. Không lưu tài khoản ngân hàng thật vào git.

## Hành vi khóa / ngoại tuyến
- Khi online, kiểm tra tối đa mỗi5 phút; service/UI gọi tick mỗi phút, lệnh chia cũng
  kiểm tra nếu đã đến hạn. Không có push nền khi app bị dừng/force-stop.
- Thiết bị mới phải online lần đầu và được duyệt (trừ khi admin bật tự duyệt).
- Đã duyệt: ngoại tuyến mặc định7 ngày kể từ lần xác nhận thành công; admin chỉnh
  từ1 giờ đến30 ngày. Hết hạn phải online lại. Lỗi mạng không tự khóa một máy mới
  được xác nhận; cũng không xóa lệnh khóa/cập nhật bắt buộc đã nhận.
- Khóa hoặc bản quá cũ: chặn mở/khôi phục/đổi tỷ lệ/đổi bên/đổi app/show/status ở
  SplitService, gồm UI, Voice và toolbar. Voice nhận `error/ACCESS_BLOCKED`.
- Vẫn cho Stop/Home/Hide để thoát phiên. Khi service phát hiện khóa, dừng overlay
  điều khiển; không force-stop app khác, không tắt màn xe hay xóa dữ liệu.
- Cài lại cùng signer/user: dùng hash Android ID scoped làm mã thiết bị, không gửi
  serial/VIN. Private credential tự sinh, không hiển thị như key cho khách nhập.
  Nếu cài lại mất credential: admin bấm “Khôi phục cài lại”, app kiểm tra lại rồi
  admin duyệt. Thao tác khôi phục vẫn giữ trạng thái locked; enabled về pending.
- Factory reset, đổi Android user/signer hoặc thiết bị bị can thiệp có thể đổi
  identity. Đây là kiểm soát của app thường, không phải chống sửa APK/root tuyệt đối.
  Mã nguồn công khai có thể bị sửa để bỏ client gate. Không hứa khóa tức thì khi
  thiết bị offline hoặc khóa cả hệ điều hành xe.

## Thử cần làm sau cấu hình
1. App mới hiện pending → admin duyệt → bấm kiểm tra → mở cặp bằng UI/Voice.
2. Admin khóa → kiểm tra thủ công hoặc đợi ≤5 phút online → toolbar dừng, chia bị
   chặn; Stop/Home vẫn thoát được. Mất mạng rồi mở lại vẫn giữ khóa.
3. Admin mở → kiểm tra mạng → dùng lại; thử ngoại tuyến trong/hết lease.
4. Bản mới versionCode lớn hơn, đúng SHA/signer; cập nhật tự nguyện trước, cài đè
   giữ preset/token/RSA; sau đó thử bắt buộc với máy còn bản cũ.
5. Sai SHA/signer/package, HTTP redirect, link private, hủy cài đặt: phải báo lỗi,
   giữ bản hiện tại. Không tự gỡ APK cũ.
6. QR/STK chính xác, tương phản chữ, sao chép giữ nguyên; không có mạng QR tải thất
   bại thì STK đã cache vẫn dùng. Cài lại cần admin khôi phục, khóa không tự mất.

Chưa deploy Worker vào tài khoản Cloudflare của chủ dự án. Chưa có URL, thông tin
ngân hàng/QR thật và khóa ký ổn định. Unit/build không thay thế các bước thử trên.
