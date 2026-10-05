"""Draws the Mathlete Prep gold-medal launcher icons and Fire TV banner.

Shapes live in a 108x108 grid, the same grid as the adaptive icon in
app/src/main/res/drawable/ic_launcher_foreground.xml. Keep the two in sync.

Usage: pip install pillow && python tools/make_art.py
"""
import os
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
OUT = os.path.join(ROOT, "build")
TEAL = (47, 111, 115)
RED = (214, 69, 69)
BLUE = (58, 111, 216)
RIM = (201, 144, 30)
GOLD = (245, 197, 66)
INK = (107, 69, 8)

# Shared shape data (also written into the vector drawable below).
LEFT_STRAP = [(38, 26), (48, 26), (60, 50), (51, 54)]
RIGHT_STRAP = [(60, 26), (70, 26), (57, 54), (48, 50)]
MEDAL_C, MEDAL_R, FACE_R = (54, 62), 19, 15.5
FOUR = [(55, 52), (59.5, 52), (59.5, 64), (63, 64), (63, 67.5), (59.5, 67.5), (59.5, 72),
        (55, 72), (55, 67.5), (45, 67.5), (45, 64)]
FOUR_HOLE = [(55, 58), (55, 64), (50, 64)]


def draw_medal(d, scale, ox=0.0, oy=0.0):
    def p(pts):
        return [(ox + x * scale, oy + y * scale) for x, y in pts]

    def circle(c, r, fill):
        cx, cy = ox + c[0] * scale, oy + c[1] * scale
        d.ellipse([cx - r * scale, cy - r * scale, cx + r * scale, cy + r * scale], fill=fill)

    d.polygon(p(RIGHT_STRAP), fill=BLUE)
    d.polygon(p(LEFT_STRAP), fill=RED)
    circle(MEDAL_C, MEDAL_R, RIM)
    circle(MEDAL_C, FACE_R, GOLD)
    d.polygon(p(FOUR), fill=INK)
    d.polygon(p(FOUR_HOLE), fill=GOLD)


def icon(size, crop=76, rounded=True):
    """Legacy square icon: the middle `crop` units of the 108 grid, teal tile with rounded corners."""
    ss = 8
    big = size * ss
    scale = big / crop
    off = -(108 - crop) / 2 * scale
    img = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    radius = int(big * 0.2) if rounded else 0
    d.rounded_rectangle([0, 0, big - 1, big - 1], radius=radius, fill=TEAL)
    draw_medal(d, scale, off, off)
    return img.resize((size, size), Image.LANCZOS)


def banner():
    ss = 4
    w, h = 320 * ss, 180 * ss
    img = Image.new("RGBA", (w, h), TEAL + (255,))
    d = ImageDraw.Draw(img)
    # Medal art spans x 38-73 and y 26-81 in the 108 grid; fit it into the left 120 px with a margin.
    scale = 2.75 * ss
    draw_medal(d, scale, ox=18 * ss - 38 * scale, oy=14 * ss - 26 * scale)
    bold = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
    regular = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    left, room = 132 * ss, (320 - 132 - 14) * ss
    size = 40
    while ImageFont.truetype(bold, size * ss).getlength("Mathlete") > room:
        size -= 1
    title = ImageFont.truetype(bold, size * ss)
    d.text((left, 40 * ss), "Mathlete", font=title, fill="white")
    d.text((left, 40 * ss + size * 1.15 * ss), "Prep", font=title, fill=GOLD)
    d.text((left + 2 * ss, 132 * ss), "Grade 4 math contest", font=ImageFont.truetype(regular, 12 * ss), fill=(205, 232, 230))
    return img.resize((320, 180), Image.LANCZOS)


def main():
    for folder, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        path = f"{RES}/mipmap-{folder}/ic_launcher.png"
        os.makedirs(os.path.dirname(path), exist_ok=True)
        icon(size).save(path, optimize=True)
    os.makedirs(f"{RES}/drawable-xhdpi", exist_ok=True)
    b = banner()
    b.convert("RGB").save(f"{RES}/drawable-xhdpi/tv_banner.png", optimize=True)

    # Preview sheet for the product owner: big icon, small icons, TV banner.
    sheet = Image.new("RGB", (1120, 620), (247, 242, 234))
    big = icon(400)
    sheet.paste(big, (40, 80), big)
    for i, s in enumerate([96, 48]):
        im = icon(s)
        sheet.paste(im, (470 + i * 120, 80 + (96 - s) // 2), im)
    sheet.paste(b.resize((640, 360), Image.LANCZOS), (460, 240))
    d = ImageDraw.Draw(sheet)
    f = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 26)
    d.text((40, 30), "App icon", font=f, fill=(30, 27, 22))
    d.text((470, 30), "Small sizes", font=f, fill=(30, 27, 22))
    d.text((460, 200), "Fire TV banner", font=f, fill=(30, 27, 22))
    os.makedirs(OUT, exist_ok=True)
    sheet.save(f"{OUT}/mathlete_prep_preview.png")
    print("ok")


if __name__ == "__main__":
    main()
