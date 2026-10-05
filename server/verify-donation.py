"""Verify the actual bundled payment QR against the public recipient profile."""
from pathlib import Path
import re
from PIL import Image
import zxingcpp

root = Path(__file__).resolve().parents[1]
source = (root / "app/src/main/java/com/tht/split/DonationProfile.java").read_text()

def field(name):
    return re.search(rf'\b{name}\s*=\s*"([^"]+)"', source).group(1)

def tlv(value):
    result = {}
    at = 0
    while at < len(value):
        tag, size = value[at:at + 2], int(value[at + 2:at + 4])
        at += 4
        assert at + size <= len(value), "Truncated QR field"
        assert tag not in result, "Duplicate QR field"
        result[tag] = value[at:at + size]
        at += size
    assert at == len(value)
    return result

image = Image.open(root / "app/src/main/assets" / field("QR_ASSET"))
decoded = zxingcpp.read_barcode(image)
assert decoded is not None, "Cannot decode the bundled QR"
payload = decoded.text
data = tlv(payload)
merchant = tlv(data["38"])
recipient = tlv(merchant["01"])
assert merchant["00"] == "A000000727" and merchant["02"] == "QRIBFTTA"
assert recipient["00"] == field("BANK_BIN"), "QR bank differs from the profile"
assert recipient["01"] == field("ACCOUNT"), "QR account differs from the profile"
assert data["53"] == "704" and data["58"] == "VN"
assert payload[-8:-4] == "6304"
crc = 0xffff
for byte in payload[:-4].encode():
    crc ^= byte << 8
    for _ in range(8):
        crc = ((crc << 1) ^ 0x1021 if crc & 0x8000 else crc << 1) & 0xffff
assert data["63"] == f"{crc:04X}", "Invalid payment QR checksum"
print("Payment QR recipient and EMV checksum match the bundled profile")
