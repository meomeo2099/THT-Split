# THT Split dùng ngay, cập nhật và khóa chung — 0.3.6

## Quyết định mới06/10/2026
Bỏ đăng ký thiết bị để ai cài cũng sử dụng ngay. Không nhập key, không duyệt,
không sinh/gửi mã Android ID, model hay credential đăng ký. Bản mới chạy được
ngay khi cài offline, Worker chưa deploy hoặc đang lỗi. Quyền overlay/ADB/
freeform của Android vẫn cần theo phương thức chia đã chọn.

Worker vẫn giữ cập nhật tự nguyện/bắt buộc và khóa/mở toàn ứng dụng đã yêu cầu.
Không còn khóa một máy riêng, danh sách thiết bị, heartbeat hoặc thời hạn ngoại
tuyến. Mặc định state=enabled, minimumVersionCode=0. Khi app đã nhận khóa chung
hoặc yêu cầu cập nhật bắt buộc, cache đó vẫn có hiệu lực qua lỗi mạng/reboot.
Máy chưa từng nhận cấu hình dùng ngay; đây không phải khóa chống cài lại tuyệt đối.

## API công khai
App gọi GET https://chiamanhinh.mavoice.workers.dev/api/config, không có body,
mã thiết bị, credential hay Authorization. Phản hồi chung:

```json
{"state":"enabled","reason":"","minimumVersionCode":0,"latest":null}
```

latest có versionCode/versionName/apkUrl HTTPS/sha256/notes. App kiểm nội dung
trước lưu. Cache mới tht_public_policy không đọc state/lease của đăng ký cũ trong
tht_remote; khóa từng thiết bị cũ không chặn phiên bản công khai. Preset, token
Voice/Bridge và khóa RSA được giữ nguyên khi cập nhật cùng signer giữ dữ liệu.

Lệnh chia không chờ mạng; kiểm cache chung tại service/boot/UI. Service dùng
executor mạng riêng, tick ngay khi tạo/mỗi60s, request tối đa5 phút; UI lúc mở
hoặc kiểm tra thủ công. Khi nhận khóa chung, dừng overlay, không force-stop app
khác. Stop/Home/Hide luôn thoát được. Không push khi app đã dừng/force-stop.

Worker GET không tạo/sửa bản ghi thiết bị. Các route /api/device,
/api/admin/device và /api/admin/reenroll trả410. Records cũ được giữ nguyên trong
storage nhưng không đọc/liệt kê/sử dụng; không xóa namespace khi nâng Worker.
Config cũ thiếu state chuyển enabled, bỏ offlineSeconds/autoApprove khỏi phản hồi.
CONTROL chỉ lưu cấu hình chung. Health protocol2, registration=false.

## Triển khai Worker của chủ dự án
URL đã gắn mặc định trong APK/CI/release: https://chiamanhinh.mavoice.workers.dev.
THT_CONTROL_URL lúc build chỉ cần khi chủ dự án đổi origin (HTTPS, không dấu / cuối).
Worker tại lần kiểm tra0.3.5 đang trả Hello World; chưa deploy mã quản trị thật.
Việc này chỉ làm kiểm tra cập nhật báo lỗi, không chặn người mới dùng0.3.6.

Giải nén Worker.zip hoặc mở terminal ở thư mục server của repo:

```sh
npx --yes wrangler@4 login
npx --yes wrangler@4 secret put ADMIN_TOKEN
npx --yes wrangler@4 deploy
```

Đăng nhập đúng tài khoản subdomain mavoice. Nhập mật khẩu ngẫu nhiên ít nhất32 ký
tự khi secret put hỏi; không gửi cho khách/đưa vào APK/source/GitHub. Wrangler
name=chiamanhinh thay mã Worker hiện có, CONTROL SQLite được tạo/bind qua exports.
Giữ tên class/namespace khi nâng, không dùng tên mới hoặc xóa dữ liệu.

GET /api/health sau deploy: service=tht-split-control, protocol=2,
registration=false, storage=true, adminConfigured=true. GET /api/config không
cần admin secret. Thiếu CONTROL trả503, app giữ config đã lưu hoặc dùng mặc định.
Trang chính đăng nhập mật khẩu quản trị, chọn Mở cho mọi người/Tạm khóa toàn ứng
dụng và cấu hình cập nhật. API admin vẫn yêu cầu mật khẩu.

## Ký APK và cập nhật
APK tải qua HTTPS, tối đa64MB/5redirect; kiểm SHA256/package/version/signer/minSDK,
Android vẫn yêu cầu xác nhận cài đặt. Không gỡ app để đổi chữ ký.

CI debug runner thay signer giữa build, không cài đè được chuỗi bản test trước.
Cấu hình keystore ổn định (ưu tiên key đã ký APK đang cài nếu còn giữ) qua Secrets:
THT_KEYSTORE_B64/THT_STORE_PASSWORD/THT_KEY_ALIAS/THT_KEY_PASSWORD. Keystore và
mật khẩu không ở git. Workflow phát hành buộc đủ signer secrets. Repo private:
đặt APK trên HTTPS công khai, không đưa PAT vào app. Không dùng link artifact
cần đăng nhập cho cập nhật.

Quản trị nhập versionCode/tên bản/URL APK/SHA256/nội dung. Tối thiểu0 = tự nguyện.
Chỉ tăng minimumVersionCode sau khi APK cùng signer đã tải và cài đè thử được.
Checksum Windows: Get-FileHash .\THT-Split.apk -Algorithm SHA256.

## Ủng hộ
DonationProfile và donation-qr.jpg công khai được chủ dự án cung cấp, đã nhúng
offline: MB Bank, 67777777778, TRAN HUU TAI. Bytes QR/profile không đổi; Worker
không thay metadata/QR. Sao chép STK giữ nguyên. Tên holder do chủ dự án cung cấp.

## Kiểm thử tiếp
1. Cài mới offline hoặc Worker Hello World: dùng ngay, không có mã/đăng ký.
2. Cập nhật cùng signer giữ preset/RSA/token; legacy lock/lease không được áp vào
   cache chung mới. Không gỡ bản đang dùng khi chưa giữ cấu hình.
3. GET /api/config nhiều lần không sinh hồ sơ. POST đăng ký cũ410. Admin chỉ có
   cấu hình chung, sai/mất mật khẩu không sửa được.
4. Khóa chung → kiểm tra mạng → service/UI/Voice chặn; Stop/Home/Hide thoát được.
   Mất mạng giữ khóa đã nhận. Admin mở → Kiểm tra cập nhật → dùng lại.
5. Cập nhật đúng signer trước, rồi mới thử mandatory. SHA/signer/package sai,
   HTTP redirect/link private/hủy cài phải giữ bản hiện tại.
6. Icon0.3.5/QR/chia/kéo/ratio/Off giữ nguyên; thử LDPlayer/xe thật.

Chưa triển khai vào tài khoản Cloudflare hay thử xe thật. Build/unit/lint không
thay thế thao tác thiết bị. Không còn phụ thuộc Worker để đăng ký lần đầu.
