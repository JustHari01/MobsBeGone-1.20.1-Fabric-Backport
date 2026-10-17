from __future__ import annotations

import math
import struct
import zlib
from pathlib import Path


SIZE = 64
SCALE = 8
COLORS = {
    "background": (13, 29, 39, 255),
    "panel": (22, 49, 60, 255),
    "border": (71, 195, 173, 255),
    "mob": (220, 234, 225, 255),
    "mob_shadow": (111, 146, 146, 255),
    "eye": (17, 37, 47, 255),
    "block": (44, 81, 88, 255),
    "ban": (245, 91, 111, 255),
}


pixels = [[COLORS["background"] for _ in range(SIZE)] for _ in range(SIZE)]


def put(x: int, y: int, color: tuple[int, int, int, int]) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        pixels[y][x] = color


def rect(x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int, int]) -> None:
    for y in range(y0, y1):
        for x in range(x0, x1):
            put(x, y, color)


# Rounded square frame with a dark inset.
for y in range(3, 61):
    for x in range(3, 61):
        dx = max(0, 8 - x, x - 55)
        dy = max(0, 8 - y, y - 55)
        if dx * dx + dy * dy <= 25:
            put(x, y, COLORS["border"])
for y in range(6, 58):
    for x in range(6, 58):
        dx = max(0, 11 - x, x - 52)
        dy = max(0, 11 - y, y - 52)
        if dx * dx + dy * dy <= 25:
            put(x, y, COLORS["panel"])

# Small square glints make the field read as pixel art without copying game assets.
for x, y in ((14, 14), (49, 16), (13, 48), (50, 46)):
    rect(x, y, x + 2, y + 2, COLORS["block"])

# A generic blocky creature silhouette, drawn from simple shapes.
rect(21, 13, 27, 19, COLORS["mob_shadow"])
rect(37, 13, 43, 19, COLORS["mob_shadow"])
rect(19, 17, 45, 33, COLORS["mob"])
rect(17, 31, 47, 48, COLORS["mob_shadow"])
rect(12, 33, 18, 44, COLORS["mob_shadow"])
rect(46, 33, 52, 44, COLORS["mob_shadow"])
rect(21, 47, 29, 54, COLORS["mob_shadow"])
rect(35, 47, 43, 54, COLORS["mob_shadow"])
rect(24, 22, 29, 27, COLORS["eye"])
rect(35, 22, 40, 27, COLORS["eye"])
rect(29, 29, 35, 31, COLORS["eye"])

# A prohibition ring and slash mark the entity as blocked.
cx = cy = 32
for y in range(SIZE):
    for x in range(SIZE):
        distance = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
        if 25 <= distance <= 27:
            put(x, y, COLORS["ban"])
        if abs((x + y) - 64) <= 2 and 9 <= x <= 55 and 9 <= y <= 55:
            put(x, y, COLORS["ban"])


def png_chunk(kind: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)


rgba = bytearray()
for row in pixels:
    rgba.append(0)
    for color in row:
        rgba.extend(color)

raw = bytearray()
for y in range(SIZE):
    row = pixels[y]
    raw.append(0)
    for color in row:
        raw.extend(bytes(color))

scaled = bytearray()
for y in range(SIZE):
    row = raw[y * (1 + SIZE * 4) + 1 : (y + 1) * (1 + SIZE * 4)]
    enlarged = bytearray()
    for x in range(SIZE):
        pixel = row[x * 4 : x * 4 + 4]
        enlarged.extend(pixel * SCALE)
    for _ in range(SCALE):
        scaled.append(0)
        scaled.extend(enlarged)

width = height = SIZE * SCALE
png = bytearray(b"\x89PNG\r\n\x1a\n")
png.extend(png_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
png.extend(png_chunk(b"IDAT", zlib.compress(bytes(scaled), level=9)))
png.extend(png_chunk(b"IEND", b""))

destination = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "assets" / "mobsbegone" / "icon.png"
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_bytes(png)
print(f"Wrote {destination} ({width}x{height}, {len(png)} bytes)")
