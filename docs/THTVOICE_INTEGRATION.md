# Hướng dẫn triển khai trong dự án THT Voice

Repo: https://github.com/meomeo2099/THTVoice — package `com.tht.voice`.
Split: https://github.com/meomeo2099/THT-Split — package `com.tht.split`, từ 0.2.0-test.

## Trạng thái cần hiểu trước
Phía Split đã có ba phương thức: ADB nội bộ tự mở bridge, Off mở trực tiếp và quyền
hệ thống có điều kiện. API v2 và callback đã có. Công việc này không sửa repo Voice.
Không viết lại các backend phía Split khi chỉ làm client Voice.

Đọc AGENTS.md của Voice → docs/HANDOFF_WORKFLOW.md (phần cập nhật mới nhất, mục 2/4)
→ docs/AGENT_PROMPT.md → docs/VOICE_WORKFLOW.md. Kiểm tra lịch sử commit trước sửa.
Đọc [VOICE_API.md](VOICE_API.md) trong Split trước khi ghép.

## Phần cần làm trong Voice
1. Client SplitApi: kiểm tra đã cài Split, gửi explicit broadcast, theo dõi requestId,
   nhận callback, timeout và chống gửi lặp. Khai báo queries package com.tht.split.
2. Kiểm tra chứng thư ký thực tế. Cùng signer thì khai báo quyền CONTROL_SPLIT và dùng
   SignatureVoiceReceiver. Khác signer thì thêm ô nhập/lưu token trong Voice và dùng
   VoiceReceiver. Không ghi token vào git/TTS/log, không đổi khóa ký để chạy thử.
3. Receiver kết quả explicit không exported. Chỉ nhận requestId còn đang chờ, hủy
   PendingIntent khi timeout, không để response cũ điều khiển yêu cầu mới.
4. Parser local nhận start/replace/swap/ratio/fullscreen/restore/hide/show/stop/home.
   Resolve package đúng app đã cài, chỉ chọn một biến thể YouTube theo ưu tiên hiện có.
   Không chọn app mơ hồ và không tự fallback sang Chrome.
5. Popup nghe do Voice quản lý, vẫn di chuyển/tự tắt. HVAC chạy độc lập. Tìm đường
   Vietmap cần luồng nhận điểm đến riêng, không đồng nhất với mở app trong ô chia.
6. Voice không cần biết token bridge hoặc tự chạy shell; Split chuẩn bị quyền theo
   phương thức được người dùng chọn. Không gọi Telnet/Shizuku.

## Mẫu gửi Java (chuyển sang Kotlin theo cấu trúc repo nếu cần)
Receiver SplitResultReceiver dưới đây là lớp cần tạo trong repo Voice.
Đăng ký manifest android:exported="false"; PendingIntent của Voice cho phép Split gọi
receiver này dù không công khai receiver cho app khác.

```java
String requestId = java.util.UUID.randomUUID().toString();
Intent resultIntent = new Intent(context, SplitResultReceiver.class)
    .setAction("com.tht.voice.SPLIT_RESULT")
    .setData(android.net.Uri.parse("thtvoice://split/" + requestId));
int flags = PendingIntent.FLAG_ONE_SHOT;
if (android.os.Build.VERSION.SDK_INT >= 31) flags |= PendingIntent.FLAG_MUTABLE;
PendingIntent result = PendingIntent.getBroadcast(
    context, nextUniqueRequestCode(), resultIntent, flags);

Intent request = new Intent("com.tht.split.COMMAND")
    .setComponent(new ComponentName("com.tht.split",
        sameSigner ? "com.tht.split.SignatureVoiceReceiver"
                   : "com.tht.split.VoiceReceiver"))
    .putExtra("requestId", requestId)
    .putExtra("result", result)
    .putExtra("command", "start")
    .putExtra("left", resolvedLeftPackage)
    .putExtra("right", resolvedRightPackage)
    .putExtra("ratio", 50);
if (!sameSigner) request.putExtra("token", savedSplitVoiceToken);
// Ghi pending trước khi gửi, lên lịch timeout và giữ result để hủy khi timeout.
context.sendBroadcast(request);
```

sameSigner phải dựa trên chứng thư đã kiểm, không dựa trên việc APK cài được.
Nếu dùng signature, manifest Voice thêm:
```xml
<uses-permission android:name="com.tht.split.permission.CONTROL_SPLIT" />
<queries><package android:name="com.tht.split" /></queries>
```

Callback đọc requestId/status/code/message/verified. success: có thể nói đã thực hiện;
requested: chỉ nói đã gửi yêu cầu; error/timeout: nói lỗi phù hợp, không báo thành công.
Không coi sendBroadcast() trả về hoặc ordered-broadcast resultCode là hoàn tất chia.

## Lệnh nói cần hỗ trợ
| Lời nói | Command |
|---|---|
| Chia Vietmap với YouTube / chia Google Maps với YouTube | start |
| Mở chia màn hình | start với cặp trước, hoặc mở UI khi chưa có cặp |
| Đổi bên | swap |
| Bên trái bốn mươi phần trăm | ratio=40 |
| Đổi bên phải sang Spotify | replace, side=right |
| YouTube toàn màn hình | fullscreen, side xác định theo cặp đang dùng |
| Khôi phục chia màn hình | restore |
| Ẩn / hiện thanh chia màn hình | hide / show |
| Tắt chia màn hình | stop |

API v2 chưa trả toàn bộ danh sách preset hoặc cặp hiện tại cho Voice. Client cần theo
cặp đã gửi thành công; nếu trạng thái không rõ thì hỏi bên trái/phải, không đoán side.

## Kiểm tra và bàn giao
- Test parser câu gần giống lệnh HVAC/media, packages thiếu và biến thể YouTube.
- Test callback thành công/lỗi/requested/BUSY/timeout, token sai và chữ ký khác.
- Test khi chưa cấp overlay, chưa bật API, lần đầu xác nhận khóa ADB, reboot, đổi Wi-Fi.
- Test Off không báo thành công giả; ratio từ0.2.1 phải trả requested, không nói đã đổi bố cục thành công.
- Build/lint kiểm tra repo Voice; giữ package và khóa ký đang phát hành.
- Commit bằng tiếng Việt; cập nhật HANDOFF Voice và đẩy GitHub.
- Ghi rõ chưa test xe nếu chỉ chạy máy phát triển. Không thay đổi firmware/OTA.
