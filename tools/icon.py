"""Draws the launcher icon (a compartment window at night: moon, stars, pines, a mahogany frame and a
velvet valance) and docs/logo.png.

python tools/icon.py
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
BG = (14, 22, 48)
SKY_TOP = (6, 10, 24)
SKY_LOW = (38, 56, 98)
WOOD = (107, 53, 35)
WOOD_DARK = (62, 29, 19)
BRASS = (214, 174, 98)
VELVET = (160, 58, 64)
MOON = (240, 232, 205)
PINE = (9, 13, 26)
S = 4  # supersampling


def foreground(size: int, background: bool) -> Image.Image:
    """The icon on a size x size canvas in 108 dp units; the safe zone is the middle 72."""
    W = size * S
    u = W / 108
    im = Image.new("RGBA", (W, W), BG + (255,) if background else (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # The frame: a rounded window, mahogany with a brass bead.
    fx0, fy0, fx1, fy1 = 26 * u, 24 * u, 82 * u, 84 * u
    d.rounded_rectangle((fx0, fy0, fx1, fy1), radius=12 * u, fill=WOOD + (255,))
    d.rounded_rectangle((fx0 + 1.2 * u, fy0 + 1.2 * u, fx1 - 1.2 * u, fy1 - 1.2 * u), radius=11 * u, outline=WOOD_DARK + (255,), width=int(1.4 * u))
    gx0, gy0, gx1, gy1 = 31 * u, 29 * u, 77 * u, 77 * u
    sky = Image.new("RGBA", (W, W))
    sd = ImageDraw.Draw(sky)
    for y in range(int(gy0), int(gy1) + 1):
        t = (y - gy0) / (gy1 - gy0)
        sd.line([(gx0, y), (gx1, y)], fill=tuple(int(SKY_TOP[i] + (SKY_LOW[i] - SKY_TOP[i]) * t) for i in range(3)) + (255,))
    # Moon glow, moon and its bite.
    glow = Image.new("L", (W, W), 0)
    ImageDraw.Draw(glow).ellipse((52 * u, 34 * u, 72 * u, 54 * u), fill=110)
    glow = glow.filter(ImageFilter.GaussianBlur(4 * u))
    sky.alpha_composite(Image.merge("RGBA", (*Image.new("RGB", (W, W), MOON).split(), glow)))
    sd = ImageDraw.Draw(sky)
    sd.ellipse((57 * u, 38 * u, 67 * u, 48 * u), fill=MOON + (255,))
    sd.ellipse((60 * u, 36.6 * u, 70 * u, 46.6 * u), fill=SKY_TOP + (255,))
    for x, y, r in [(37, 35, 0.7), (45, 40, 0.5), (41, 47, 0.6), (50, 33, 0.45), (72, 56, 0.45), (35, 54, 0.5)]:
        sd.ellipse(((x - r) * u, (y - r) * u, (x + r) * u, (y + r) * u), fill=(255, 255, 255, 220))
    # Pines along the bottom.
    sd.rectangle((gx0, 70 * u, gx1, gy1), fill=PINE + (255,))
    for cx, h in [(33, 14), (38, 19), (43, 12), (49, 17), (55, 11), (62, 15), (68, 20), (74, 13)]:
        sd.polygon([(cx * u, (71 - h) * u), ((cx + h * 0.28) * u, 71 * u), ((cx - h * 0.28) * u, 71 * u)], fill=PINE + (255,))
    mask = Image.new("L", (W, W), 0)
    ImageDraw.Draw(mask).rounded_rectangle((gx0, gy0, gx1, gy1), radius=8 * u, fill=255)
    im.paste(sky, (0, 0), mask)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle((gx0, gy0, gx1, gy1), radius=8 * u, outline=BRASS + (255,), width=int(1.1 * u))
    # The velvet valance, scalloped, with brass tassels.
    folds = 6
    fw = (fx1 - fx0) / folds
    for i in range(folds):
        x0 = fx0 + i * fw
        d.rectangle((x0, fy0, x0 + fw, fy0 + 7 * u), fill=VELVET + (255,))
        d.pieslice((x0, fy0 + 3 * u, x0 + fw, fy0 + 11 * u), 0, 180, fill=VELVET + (255,))
        d.ellipse((x0 + fw / 2 - 1 * u, fy0 + 10 * u, x0 + fw / 2 + 1 * u, fy0 + 12 * u), fill=BRASS + (255,))
    # The brass catch.
    d.rounded_rectangle((50 * u, 79 * u, 58 * u, 81 * u), radius=1 * u, fill=BRASS + (255,))
    return im.resize((size, size), Image.LANCZOS)


def main():
    sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for dpi, px in sizes.items():
        out = RES / f"mipmap-{dpi}"
        out.mkdir(parents=True, exist_ok=True)
        fg = 108 * px // 48
        foreground(fg, False).save(out / "ic_launcher_foreground.png")
        full = foreground(fg, True).crop(((fg - px) // 2 - px // 6, (fg - px) // 2 - px // 6, (fg + px) // 2 + px // 6, (fg + px) // 2 + px // 6)).resize((px, px), Image.LANCZOS)
        full.save(out / "ic_launcher.png")
        mask = Image.new("L", full.size, 0)
        ImageDraw.Draw(mask).ellipse((0, 0, px - 1, px - 1), fill=255)
        rnd = Image.new("RGBA", full.size, (0, 0, 0, 0))
        rnd.paste(full, (0, 0), mask)
        rnd.save(out / "ic_launcher_round.png")
    logo = foreground(432, True).crop((54, 54, 378, 378)).resize((256, 256), Image.LANCZOS)
    mask = Image.new("L", logo.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, 255, 255), radius=56, fill=255)
    out = Image.new("RGBA", logo.size, (0, 0, 0, 0))
    out.paste(logo, (0, 0), mask)
    (ROOT / "docs").mkdir(exist_ok=True)
    out.save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
