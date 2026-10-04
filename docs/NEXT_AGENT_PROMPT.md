Bạn làm việc trên repo meomeo2099/THT-Split, package com.tht.split.
Luôn trả lời bằng tiếng Việt. Đọc AGENTS.md → docs/HANDOFF.md → REQUIREMENTS.md
→ ARCHITECTURE.md → REFERENCE_NOTES.md. Sửa Voice đọc VOICE_API.md; backend đọc DEVICE_TEST.md.

Kiểm tra git log -5, git status và Actions mới nhất. Không làm lại UI/preset/API đã có.
Chạy ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug trước commit.
Tiếp tục từ danh sách ưu tiên trong HANDOFF. Commit backup theo từng mốc và cập nhật
HANDOFF trong cùng commit. Phân biệt build/test máy phát triển với kết quả trên EX2.
Không phụ thuộc EX2VNControl, không sao chép source không rõ giấy phép.
Không sửa repo THTVoice trước khi được giao phần tích hợp và đọc AGENTS riêng.
Không đưa khóa ký/token vào git. Nút X tắt chia, nút chevron ẩn/hiện thanh.
