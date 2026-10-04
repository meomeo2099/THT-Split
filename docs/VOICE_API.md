# API THT Voice → THT Split (v1)

Package: `com.tht.split`; receiver: `com.tht.split.VoiceReceiver`.
Action: `com.tht.split.COMMAND`. Intent explicit, không broadcast mơ hồ.
Người dùng bật Cài đặt → THT Voice và sao chép token vào cấu hình Voice.
Token 64 hex riêng từng máy, khác token bridge. Không đưa token vào git/log/phản hồi TTS.

```java
Intent i = new Intent("com.tht.split.COMMAND");
i.setComponent(new ComponentName("com.tht.split", "com.tht.split.VoiceReceiver"));
i.putExtra("token", savedSplitToken);
i.putExtra("command", "start");
i.putExtra("left", resolvedVietmapPackage);
i.putExtra("right", resolvedYoutubePackage);
i.putExtra("ratio", 50);
context.sendBroadcast(i);
```

| command | Extra | Ý nghĩa |
|---|---|---|
| start | left, right; ratio tùy chọn | Mở cặp; thiếu app lấy cấu hình lần trước |
| preset | presetId | Mở cặp theo ID ổn định trong Store |
| replace | side=left/right, package | Đổi một bên, giữ app bên kia |
| ratio | ratio (int) | Tỷ lệ trái 30–70, clamp ngoài khoảng |
| swap | Không | Đổi bên và giữ tỷ lệ |
| fullscreen | side=left/right | App được chọn toàn màn, vẫn lưu cặp |
| restore | Không | Khôi phục cặp và tỷ lệ |
| hide | Không | Ẩn thanh, vẫn chia |
| show | Không | Hiện thanh |
| stop | Không | Tắt chia về app trái / app còn lại |
| home | Không | Mở launcher xe, bỏ overlay |

Receiver từ chối token sai, API chưa bật, command lạ, thiếu quyền nổi. Chỉ forward
các extra đã biết. Backend báo lỗi bằng toast và last_error; v1 chưa gửi ACK về Voice.
Voice không được nói “đã chia thành công” chỉ vì sendBroadcast trả về.

Ví dụ test sau khi chạy `adb shell` (thay TOKEN/PACKAGE theo máy):
```sh
am broadcast -n com.tht.split/.VoiceReceiver -a com.tht.split.COMMAND   --es token TOKEN --es command start --es left PACKAGE_TRAI --es right PACKAGE_PHAI --ei ratio 50
am broadcast -n com.tht.split/.VoiceReceiver -a com.tht.split.COMMAND   --es token TOKEN --es command ratio --ei ratio 40
am broadcast -n com.tht.split/.VoiceReceiver -a com.tht.split.COMMAND   --es token TOKEN --es command hide
```

## Mapping lời nói dự kiến trong THTVoice (chưa sửa repo Voice)
- “Chia Vietmap với YouTube” → resolve packages trên máy, start.
- “Đổi bên” → swap.
- “Bên trái bốn mươi phần trăm” → ratio40.
- “Đổi bên phải sang Spotify” → replace/right.
- “YouTube toàn màn hình” → fullscreen/side tương ứng.
- “Khôi phục chia màn hình” → restore.
- “Ẩn thanh chia màn hình” → hide; “Tắt chia màn hình” → stop.

Chỉ map ý định local rõ ràng. Tìm đường Vietmap, HVAC và popup vẫn do THTVoice quản lý.
Trước ghép Voice phải thêm ACK cùng requestId và caller package/callback whitelist;
đọc AGENTS của THTVoice riêng, không tự ghép ngoài scope repo này.
