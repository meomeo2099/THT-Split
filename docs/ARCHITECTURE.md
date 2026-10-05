# Kiến trúc THT Split 0.2

```mermaid
flowchart TD
  U["Giao diện / preset"] --> S["SplitService: một thao tác mỗi lần"]
  V["Voice: token hoặc signature"] --> R["Receiver: lọc lệnh và callback"]
  R --> S
  S --> E["SplitEngine: session và vùng chia"]
  E --> B["Backend theo cài đặt"]
  B --> A["ADB nội bộ: RSA TCP"]
  A --> H["Bridge shell: token và UID"]
  B --> O["Off: ActivityOptions"]
  B --> P["Hệ thống: kiểm quyền"]
  H --> W["Android task / cửa sổ"]
  O --> W
  P --> W
  S --> C["PendingIntent kết quả → Voice"]
```

- Session giữ transport/mode/insets/cặp độc lập bản chọn UI. Không chuyển phương thức
  khi phiên đang chạy hoặc thao tác đang xử lý. Backend không tự fallback sang cách khác.
- LocalAdb: TCP tới loopback/địa chỉ chính thiết bị, RSA2048, AUTH/SHA1 digest đã băm,
  khung ADB/checksum/stream ID/deadline/giới hạn output. Không có API shell tùy ý.
- ConnectionManager: serialized bootstrap, ping và kiểm UID/version, không kill listener
  lạ. Cổng ADB mặc định5555 khác bridge39871; chỉ bridge bind127.0.0.1.
- Bridge cần được tạo lại sau reboot, nhưng app chuẩn bị lúc boot hoặc trước lệnh.
  Khóa RSA và token vẫn được giữ; lần đầu có thể cần xác nhận khóa trên màn.
- Socket ADB đóng sau bootstrap. Chuyển Off không mở/gửi lệnh shell; daemon bridge
  đã mở trước đó có thể còn sống nhưng không được Off sử dụng. Không tự kill tiến trình.
- Off luôn freeform, geometry không trừ toolbar vì không resize được khi ẩn/hiện;
  toolbar là overlay tạm. Ratio/drag gửi lại launch với bounds mới; không resizeTask, không xác minh được bounds.
  Launch/ratio trả requested, verified=false. Màn chờ overlay chỉ che vùng app,
  worker chờ thêm tối đa3s; không chặn main thread hoặc khẳng định app đã tải xong.
- System dùng MANAGE_ACTIVITY_STACKS được Android cấp; không giả mạo UID/certificates.
  Bản thường không dùng sharedUserId, chỉ cho chọn system nếu quyền thực đã có.
- Scoped hidden API exemptions chỉ trong process ứng dụng; không sửa hidden_api_policy
  toàn hệ thống và không cấp thêm permission. Source thư viện được ghi tại THIRD_PARTY.md.
- API result dùng PendingIntent do Voice/Dashboard tạo; kiểm creator package và requestId,
  không gửi broadcast phản hồi công khai hoặc theo callbackPackage tùy ý.
- Khi overlay hỏng trả error; khi gửi Off trả requested. Unit tests không chứng minh
  nội dung app/insets/cửa sổ trên firmware xe.

## Tệp chính
| Tệp | Trách nhiệm |
|---|---|
| MainActivity / Store | UI/preset/cài đặt và secrets private |
| SplitService / SplitEngine | Tuần tự hóa lệnh, session, overlay và geometry |
| connection/Backend | Chọn một backend, không fallback âm thầm |
| connection/LocalAdb | Khóa RSA private AtomicFile, xác thực/stream TCP |
| core/AdbWire / AdbRsa | Framing, public key Android, chữ ký challenge |
| connection/ConnectionManager | Kiểm tra và bootstrap ShellServer |
| connection/BootReceiver / ConnectionService | Chuẩn bị quyền lúc boot/cập nhật, không tự mở hai app |
| connection/DirectWindows / HiddenAccess | Off và truy cập scoped hidden APIs |
| bridge/ShellClient / ShellServer | Giao thức bridge/token và API task Android9 |
| VoiceReceiver / SignatureVoiceReceiver / VoiceReplies | API token/signature/callback v2 |

## Mốc tiếp tục
Mã cho ADB/Off/system có điều kiện và API phía Split đã có. Còn thử firmware xe,
chữ ký platform nếu có, ghép client trong Voice và ký phát hành ổn định ngoài git.

## Phục hồi task từ0.2.5
Bridge v3 / system dùng PairController: dọn primary màn chính, dựng cặp có thứ tự,
kiểm mode trước resize. RESIZE cần launcher components để mở lại app đã đóng.
Native resize gửi bounds cuối, không dùng temporary task/inset bounds. Off giữ
ActivityOptions và trả requested; không có khả năng quan sát/sửa task tương đương.

## Từ0.2.6 / bridge v4
PairController kiểm hai stack khác nhau, isolate task nếu stack chứa nhiều task
trước đổi mode (Android9 setTaskWindowingMode đổi cả stack). Service dọn AppTask
cấu hình của chính THT trước mở cặp; không đóng task bên thứ ba. Off bỏ adjacent
flag; chỉ mở + overlay. X không chuyển task sang fullscreen; policy chặn điều khiển
task cả UI và Voice. Các mô tả Off ratio/drag0.2.1 ở trên là lịch sử, không còn áp dụng.

## Bounds từ0.2.7
Native: resizeDockedStack trái rồi resizeStack phải với allowResizeInDockedMode.
Read-back getTaskBounds cho cả hai, đối chiếu vùng chia sai số2px. Backend giữ
verified=false của bridge nếu bounds chưa đúng. Log hỗ trợ có expected/actual
bounds; không coi mode3/4 là chứng minh khoảng chừa đã được áp dụng.

## Fullscreen từ0.2.8 / bridge v6
PairController dọn primary, mode1/resetBounds(null) cả hai task còn tồn tại,
sau đó focus task đã có. Reset dùng resizeStack allowResizeInDockedMode=true,
preserveWindows=false, animate=false; không resizeTask native hoặc force-stop.
Full/stop đọc getTaskBounds + mode của task được chọn, đối chiếu display thực.
Đọc đúng bounds vẫn không xác minh surface/OpenGL/nội dung app; log giữ kết quả.

## Khóa ADB từ0.2.9
Decode PKCS8 trả PrivateKey thay cast CRT. Public modulus lấy bằng KeyFactory
RSAPrivateKeySpec, public exponent F4 cho provider non-CRT (khóa app-only, generator
cũ Conscrypt mặc định65537; generator mới pin2048/F4). AUTH Cipher dùng PrivateKey.
Không tạo khóa khác khi file có lỗi, không đổi khóa cũ hay xác thực vượt hộp thoại.
UI/boot hiện nguyên nhân gốc có lọc token/encoded keys, log lỗi Kiểm tra hỗ trợ chẩn đoán.
