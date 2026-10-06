# Worker THT Split — tự cập nhật qua GitHub API

Worker: https://chiamanhinh.mavoice.workers.dev
Repo APK: meomeo2099/THT-Split (private). App vẫn dùng ngay, không đăng ký/key/VIN.
Worker quản lý khóa/mở chung, cập nhật tự nguyện/bắt buộc và chuyển APK từ
GitHub cho app. Source repo không được công khai; chỉ APK đã duyệt metadata
được phục vụ công khai để mọi người cài/sử dụng theo yêu cầu chủ dự án.

## Cấu hình Cloudflare

| Loại | Tên | Giá trị |
| --- | --- | --- |
| Variable | GITHUB_REPO | meomeo2099/THT-Split |
| Variable | GITHUB_INCLUDE_PRERELEASE | true (nhận bản -test; false chỉ nhận bản chính thức) |
| Variable | RELEASE_CERT_SHA256 | e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2 |
| Secret | GITHUB_TOKEN | Fine-grained PAT chỉ chọn repo THT-Split, Contents: Read |
| Secret | ADMIN_TOKEN | Mật khẩu quản trị riêng, từ 8 đến 256 ký tự |

GITHUB_REPO/RELEASE_CERT_SHA256/GITHUB_INCLUDE_PRERELEASE đã có trong wrangler.jsonc.
ADMIN_PASSWORD được hỗ trợ làm tên thay thế ADMIN_TOKEN như mẫu THTVoice.
Chỉ đặt một trong hai; nếu có cả hai, ADMIN_TOKEN được ưu tiên.
Không cần LICENSE_KEY_ID, LICENSE_SIGNING_KEY, VIN_KEY hoặc ADMIN_UI.
Không dán GitHub token/mật khẩu vào source, APK, GitHub repository hoặc gửi qua chat.

## Triển khai gói một tệp

Giải nén, mở terminal trong thư mục có worker.mjs và wrangler.jsonc:

```sh
npx --yes wrangler@4 login
npx --yes wrangler@4 secret put ADMIN_TOKEN
npx --yes wrangler@4 secret put GITHUB_TOKEN
npx --yes wrangler@4 deploy
```

Nếu đã có ADMIN_TOKEN đúng, không cần nhập lại. Nhập Secrets trực tiếp khi lệnh
hỏi. Nếu dùng ADMIN_PASSWORD trên dashboard, bỏ bước ADMIN_TOKEN và giữ một
mật khẩu hợp lệ. CONTROL SQLite/class/namespace giữ nguyên khi nâng, không xóa
dữ liệu hoặc tạo Worker tên khác. Nếu copy mã trong editor web, cần thay bằng
worker.mjs đã đóng gói VÀ giữ binding CONTROL, thêm Variables/Secrets như bảng.
Chỉ copy mã mà thiếu CONTROL chưa đủ. Gói không chứa token hay keystore.

## Nâng cấp giữ dữ liệu

Bản này chấp nhận mật khẩu ngắn hơn (tối thiểu 8 ký tự). Mật khẩu cũ dài hơn
vẫn dùng được, không bắt buộc đổi. Muốn dùng mật khẩu ngắn, sửa Value của Secret
ADMIN_TOKEN hoặc ADMIN_PASSWORD trên Worker đang dùng, rồi Deploy.

Trên dashboard: mở Worker chiamanhinh hiện tại → Edit code → thay nội dung
bằng worker.mjs trong gói → Deploy. Giữ nguyên CONTROL binding, class Control,
namespace đang có và mọi Variables/Secrets. Không tạo Worker/namespace mới,
không xóa kho dữ liệu. Gói không có migration/reset và không ghi đè cấu hình
đã lưu: trạng thái khóa/mở, mandatory, nguồn cập nhật và cache vẫn giữ nguyên.
GITHUB_TOKEN và mật khẩu admin là Secrets của Cloudflare, không nằm trong ZIP.

## Kiểm tra

1. GET /api/health: service=tht-split-control, protocol=2, registration=false,
   storage=true, adminConfigured=true.
2. Mở trang Worker, đăng nhập; Nguồn cập nhật = Tự lấy từ GitHub Releases.
   Chọn Nhận cả bản thử nghiệm nếu đang dùng 0.3.9-test; Lưu cấu hình.
3. Kiểm tra GitHub ngay: các ô phiên bản/URL/SHA APK tự điền. Lỗi token/thiếu
   release/khác cert được báo tại đây; lỗi GitHub không tự mở khóa chung.
4. GET /api/config không cần đăng nhập, latest.apkUrl là link /api/apk/<id>
   trên Worker. App không nhận GitHub token hoặc signed URL CDN.
5. Cài bản ký keystore mới một lần, rồi thử một bản có versionCode cao hơn
   cùng khóa. Chỉ tăng version tối thiểu sau khi tải và cài đè đã thử thành công.

## Phát hành từ GitHub

Workflow release buộc đúng khóa ký cố định và kiểm APK thực, tạo 3 assets:
THT-Split.apk, THT-Split.apk.sha256, update.json. Metadata lấy package,
versionCode/versionName, chứng chỉ, kích thước và SHA từ chính APK đã build.
Release chỉ được publish sau khi đã upload đủ assets (ban đầu là draft).
Worker kiểm manifest/schema/package/tag/cert/kích thước/checksum/GitHub digest,
cache 5 phút. Kiểm tra GitHub ngay bỏ cache thời gian. Lỗi GitHub giữ bản tốt
đã cache; không thay state/minimum. Chỉ 5 APK đã xác minh gần nhất được proxy.
Nếu tag đã tồn tại, không tự ghi đè; dùng bản mới hoặc xử lý draft hỏng riêng.

Bản 0.3.9-test/code21 đã phát hành thành công ngày06/10/2026, có đầy đủ 3 assets
và đúng khóa cố định. Lỗi Cannot recover key của lần build trước đã hết trong
lần chạy mới. Giữ 4 Secrets ký APK đã hoạt động cho các bản tiếp theo.
GitHub Token ở Worker chỉ đọc Releases, khác các Secrets keystore ở GitHub.

## Hai SHA khác nhau

- RELEASE_CERT_SHA256 là SHA của chứng chỉ: cố định cho mọi APK dùng cùng khóa.
  THTVoice trong ảnh dùng chứng chỉ khác, không copy SHA của Voice sang Split.
  Dạng chữ hoa có dấu : và chữ thường không dấu : là tương đương; Worker chấp nhận cả hai.
- latest.sha256 / THT-Split.apk.sha256 là SHA của toàn bộ file APK: thay đổi theo
  bản build. Worker tự lấy, không điền SHA chứng chỉ vào trường này.

Worker pin chứng chỉ từ manifest được CI tạo sau khi xác minh chữ ký APK thực.
Worker không giải mã/xác minh chứng chỉ bên trong luồng binary APK; app tiếp tục
kiểm SHA file, package/version/minSDK và chữ ký APK thực trước khi cho cài.
Khác chữ ký debug cũ không cài đè được, app không tự gỡ để đổi khóa.
GITHUB_TOKEN lỗi/hết hạn: quản trị vẫn khóa/mở được; bản tốt đã cache giữ nguyên.

Hướng dẫn chi tiết: docs/REMOTE_CONTROL.md trong repository.
