"""Check that the owner's original Zalo QR is bundled unchanged and readable."""
from pathlib import Path
from urllib.parse import urlparse
import hashlib
import re
from PIL import Image
import zxingcpp

root = Path(__file__).resolve().parents[1]
source = (root / "app/src/main/java/com/tht/split/ContactProfile.java").read_text()
asset = re.search(r'ZALO_QR_ASSET\s*=\s*"([^"]+)"', source).group(1)
path = root / "app/src/main/assets" / asset
assert hashlib.sha256(path.read_bytes()).hexdigest() == "2e7c31d9cecc61f05bc7ef5aaece643121bcd21a062ad5897146603379a025a6"
original = Image.open(path)
decoded = zxingcpp.read_barcode(original)
assert decoded is not None, "Cannot decode the original contact QR"
url = urlparse(decoded.text)
assert url.scheme == "http" and url.hostname == "zaloapp.com", "QR differs from the supplied image"
# Smallest QR frame: 160dp minus 12dp padding per side; preserve aspect ratio.
small = original.copy()
small.thumbnail((136, 136), Image.Resampling.BILINEAR)
scan = zxingcpp.read_barcode(small)
assert scan is not None and scan.text == decoded.text, "Contact QR unreadable at the smallest displayed size"
assert 'FACEBOOK_URL="https://www.facebook.com/100004637940665"' in source
print("Original Zalo QR bytes, scan at minimum size and Facebook link verified")
