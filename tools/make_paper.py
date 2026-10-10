"""Makes a printable PDF of one practice test, laid out like the paper contest:
a cover page, the 20 questions with answer blanks and room to work, and an
answer key with worked solutions on separate pages at the end (so it can be
pulled off before the test is handed out).

Usage (from the project folder):
    pip install reportlab
    python tools/make_paper.py 1                  # writes build/Mathlete_Prep_Practice_Test_1.pdf
    python tools/make_paper.py 3 my_test.pdf      # test 3, saved as my_test.pdf
"""
import html
import json
import math
import os
import sys

from reportlab.graphics.shapes import Circle, Drawing, Line, Polygon, Rect, String
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import inch
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (Flowable, KeepTogether, PageBreak, Paragraph, SimpleDocTemplate, Spacer,
                                Table, TableStyle)

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BANK = os.path.join(ROOT, "core", "src", "main", "resources", "banks", "grade4_fall")
# The free DejaVu fonts ship next to this script (see fonts/LICENSE-DejaVu.txt), so it
# works the same on Windows, Mac and Linux. They cover symbols like −, ×, ÷, □ and ▲.
FONT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "fonts")

INK = colors.HexColor("#1E1B16")
TEAL = colors.HexColor("#2F6F73")
SOFT = colors.HexColor("#E8F2F1")
GREY = colors.HexColor("#8A8A8A")
LIGHT = colors.HexColor("#D9D9D9")
RED = colors.HexColor("#D64545")
BLUE = colors.HexColor("#3A6FD8")
RIM = colors.HexColor("#C9901E")
GOLD = colors.HexColor("#F5C542")
BROWN = colors.HexColor("#6B4508")

pdfmetrics.registerFont(TTFont("Sans", os.path.join(FONT_DIR, "DejaVuSans.ttf")))
pdfmetrics.registerFont(TTFont("Sans-Bold", os.path.join(FONT_DIR, "DejaVuSans-Bold.ttf")))
pdfmetrics.registerFont(TTFont("Mono", os.path.join(FONT_DIR, "DejaVuSansMono.ttf")))
pdfmetrics.registerFontFamily("Sans", normal="Sans", bold="Sans-Bold", italic="Sans", boldItalic="Sans-Bold")

QUESTION = ParagraphStyle("q", fontName="Sans", fontSize=12, leading=17, textColor=INK, leftIndent=26, firstLineIndent=-26)
ANSWER = ParagraphStyle("a", fontName="Sans", fontSize=12, leading=16, textColor=INK, leftIndent=26, spaceBefore=8)
KEY = ParagraphStyle("k", fontName="Sans", fontSize=10.5, leading=14.5, textColor=INK, leftIndent=22, firstLineIndent=-22, spaceAfter=7)
HEADING = ParagraphStyle("h", fontName="Sans-Bold", fontSize=17, leading=22, textColor=INK, alignment=1, spaceAfter=4)
SUBHEADING = ParagraphStyle("sh", fontName="Sans", fontSize=11, leading=15, textColor=GREY, alignment=1, spaceAfter=12)


def text(s):
    """Question text as Paragraph markup: escaped, with line breaks kept."""
    return html.escape(s).replace("\n", "<br/>")


# ---------------------------------------------------------------- figures

def centered(d):
    d.hAlign = "CENTER"
    return d


def fig_grid(f):
    cell = 28
    rows, cols = f["rows"], f["cols"]
    d = Drawing(cols * cell + 4, rows * cell + 4)
    for i in f.get("shaded", []):
        r, c = divmod(i, cols)
        d.add(Rect(2 + c * cell, 2 + (rows - 1 - r) * cell, cell, cell, fillColor=LIGHT, strokeColor=None))
    for r in range(rows + 1):
        d.add(Line(2, 2 + r * cell, 2 + cols * cell, 2 + r * cell, strokeColor=INK, strokeWidth=1.5))
    for c in range(cols + 1):
        d.add(Line(2 + c * cell, 2, 2 + c * cell, 2 + rows * cell, strokeColor=INK, strokeWidth=1.5))
    return centered(d)


def fig_pyramid(f):
    box = 30
    rows = f["rows"]
    width = len(rows[-1]) * box
    d = Drawing(width + 4, len(rows) * box + 4)
    for r, row in enumerate(rows):
        y = 2 + (len(rows) - 1 - r) * box
        x0 = 2 + (width - len(row) * box) / 2
        for k, cell in enumerate(row):
            x = x0 + k * box
            d.add(Rect(x, y, box, box, fillColor=LIGHT if cell == "?" else colors.white, strokeColor=INK, strokeWidth=1.3))
            if cell:
                d.add(String(x + box / 2, y + 10, cell, fontName="Sans-Bold" if cell == "?" else "Sans", fontSize=13,
                             textAnchor="middle", fillColor=INK))
    return centered(d)


def inside(poly, px, py):
    result, j = False, len(poly) - 1
    for i in range(len(poly)):
        xi, yi = poly[i]
        xj, yj = poly[j]
        if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
            result = not result
        j = i
    return result


def fig_shape(f):
    pts = [(p["x"], p["y"]) for p in f["points"]]
    min_x, max_x = min(p[0] for p in pts), max(p[0] for p in pts)
    min_y, max_y = min(p[1] for p in pts), max(p[1] for p in pts)
    w, h = max_x - min_x, max_y - min_y
    scale = min(190 / w, 140 / h, 32)
    m = 34
    d = Drawing(w * scale + 2 * m, h * scale + 2 * m)

    def at(x, y):  # grid units (y down) to drawing points (y up)
        return m + (x - min_x) * scale, m + (max_y - y) * scale

    flat = [v for p in pts for v in at(*p)]
    d.add(Polygon(flat, fillColor=SOFT, strokeColor=None))
    if f.get("showGrid"):
        for gx in range(int(min_x), int(max_x)):
            for gy in range(int(min_y), int(max_y)):
                if inside(pts, gx + 0.5, gy + 0.5):
                    x, y = at(gx, gy + 1)
                    d.add(Rect(x, y, scale, scale, fillColor=None, strokeColor=GREY, strokeWidth=0.6))
    d.add(Polygon(flat, fillColor=None, strokeColor=INK, strokeWidth=1.8))

    # Side labels sit just outside each side and slide along it if they would overlap.
    placed = []
    for key, label in f.get("sideLabels", {}).items():
        i = int(key)
        (ax, ay), (bx, by) = pts[i], pts[(i + 1) % len(pts)]
        length = math.hypot(bx - ax, by - ay)
        tx, ty = (bx - ax) / length, (by - ay) / length
        nx, ny = ty, -tx
        mx, my = (ax + bx) / 2, (ay + by) / 2
        if inside(pts, mx + nx * 0.05, my + ny * 0.05):
            nx, ny = -nx, -ny
        cx, cy = at(mx, my)
        dnx, dny, dtx, dty = nx, -ny, tx, -ty  # flip y for drawing space
        lw, lh = pdfmetrics.stringWidth(label, "Sans", 10), 10
        push = abs(dnx) * lw / 2 + abs(dny) * lh / 2 + 5
        best = None
        for k in [0, 1, -1, 2, -2, 3, -3, 4, -4]:
            slide = k * 4
            if abs(slide) > length * scale / 2:
                continue
            x = cx + dnx * push + dtx * slide
            y = cy + dny * push + dty * slide
            box = (x - lw / 2, y - lh / 2, x + lw / 2, y + lh / 2)
            if all(box[2] < o[0] or box[0] > o[2] or box[3] < o[1] or box[1] > o[3] for o in placed):
                best = box
                break
        if best is None:
            x, y = cx + dnx * push, cy + dny * push
            best = (x - lw / 2, y - lh / 2, x + lw / 2, y + lh / 2)
        placed.append(best)
        d.add(String((best[0] + best[2]) / 2, best[1] + 1.5, label, fontName="Sans", fontSize=10,
                     textAnchor="middle", fillColor=INK))
    return centered(d)


def fig_venn(f):
    d = Drawing(300, 150)
    r, cy = 52, 66
    left, right = (150 - 33, cy), (150 + 33, cy)
    for c in (left, right):
        d.add(Circle(c[0], c[1], r, fillColor=None, strokeColor=INK, strokeWidth=1.5))
    d.add(String(left[0] - 18, cy + r + 8, f["leftLabel"], fontName="Sans", fontSize=11, textAnchor="middle"))
    d.add(String(right[0] + 18, cy + r + 8, f["rightLabel"], fontName="Sans", fontSize=11, textAnchor="middle"))
    for value, x in ((f.get("left", ""), left[0] - 26), (f.get("both", ""), 150), (f.get("right", ""), right[0] + 26)):
        if value:
            d.add(String(x, cy - 5, value, fontName="Sans-Bold", fontSize=14, textAnchor="middle"))
    return centered(d)


def fig_clock(f):
    size, r = 140, 62
    c = size / 2
    d = Drawing(size, size)
    d.add(Circle(c, c, r, fillColor=colors.white, strokeColor=INK, strokeWidth=2))

    def at(turn, radius):
        return c + radius * math.sin(2 * math.pi * turn), c + radius * math.cos(2 * math.pi * turn)

    for i in range(1, 13):
        x1, y1 = at(i / 12, r * 0.9)
        x2, y2 = at(i / 12, r)
        d.add(Line(x1, y1, x2, y2, strokeColor=INK, strokeWidth=1.2))
        x, y = at(i / 12, r * 0.74)
        d.add(String(x, y - 4, str(i), fontName="Sans", fontSize=10, textAnchor="middle"))
    hour = ((f["hour"] % 12) + f["minute"] / 60) / 12
    hx, hy = at(hour, r * 0.45)
    mx, my = at(f["minute"] / 60, r * 0.7)
    d.add(Line(c, c, hx, hy, strokeColor=INK, strokeWidth=4, strokeLineCap=1))
    d.add(Line(c, c, mx, my, strokeColor=INK, strokeWidth=2.5, strokeLineCap=1))
    d.add(Circle(c, c, 3, fillColor=INK, strokeColor=None))
    return centered(d)


def fig_balance(f):
    w = 320
    item_h = 18
    stack = max(len(f["left"]), len(f["right"]))
    beam_y = 40
    d = Drawing(w, beam_y + 10 + stack * (item_h + 4) + 4)
    d.add(Line(20, beam_y, w - 20, beam_y, strokeColor=INK, strokeWidth=3))
    d.add(Polygon([w / 2, beam_y, w / 2 - 18, 2, w / 2 + 18, 2], fillColor=INK, strokeColor=None))
    for side, center in ((f["left"], w * 0.27), (f["right"], w * 0.73)):
        for k, item in enumerate(side):
            tw = pdfmetrics.stringWidth(item, "Sans", 10) + 14
            y = beam_y + 6 + k * (item_h + 4)
            d.add(Rect(center - tw / 2, y, tw, item_h, rx=8, ry=8, fillColor=SOFT, strokeColor=INK, strokeWidth=0.8))
            d.add(String(center, y + 5, item, fontName="Sans", fontSize=10, textAnchor="middle"))
    return centered(d)


def fig_bars(f):
    w, h = 330, 200
    left, bottom, top = 34, 26, h - 24
    values, step = f["values"], f.get("step", 1)
    max_v = max(step, math.ceil(max(values) / step) * step)
    d = Drawing(w, h)
    d.add(String(w / 2, h - 12, f["title"], fontName="Sans-Bold", fontSize=11, textAnchor="middle"))

    def y_of(v):
        return bottom + (top - bottom) * v / max_v

    v = 0
    while v <= max_v:
        d.add(Line(left, y_of(v), w - 6, y_of(v), strokeColor=LIGHT, strokeWidth=0.8))
        d.add(String(left - 6, y_of(v) - 3.5, str(v), fontName="Sans", fontSize=9, textAnchor="end"))
        v += step
    slot = (w - 6 - left) / len(values)
    for i, (label, value) in enumerate(zip(f["labels"], values)):
        x = left + slot * i + slot * 0.2
        d.add(Rect(x, bottom, slot * 0.6, y_of(value) - bottom, fillColor=GREY, strokeColor=INK, strokeWidth=0.8))
        d.add(String(x + slot * 0.3, bottom - 14, label, fontName="Sans", fontSize=10, textAnchor="middle"))
    d.add(Line(left, bottom, w - 6, bottom, strokeColor=INK, strokeWidth=1.3))
    d.add(Line(left, bottom, left, top, strokeColor=INK, strokeWidth=1.3))
    return centered(d)


def fig_table(f):
    data = [f["header"]] + f["rows"]
    t = Table(data, colWidths=[0.9 * inch] * len(f["header"]), rowHeights=0.32 * inch)
    style = [
        ("FONT", (0, 0), (-1, -1), "Sans", 12),
        ("FONT", (0, 0), (-1, 0), "Sans-Bold", 12),
        ("BACKGROUND", (0, 0), (-1, 0), LIGHT),
        ("GRID", (0, 0), (-1, -1), 1, INK),
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ]
    for r, row in enumerate(data):
        for c, cell in enumerate(row):
            if cell == "?":
                style += [("BACKGROUND", (c, r), (c, r), LIGHT), ("FONT", (c, r), (c, r), "Sans-Bold", 12)]
    t.setStyle(TableStyle(style))
    t.hAlign = "CENTER"
    return t


def fig_text(f):
    lines = f["lines"]
    size, gap = 18, 24
    width = max(pdfmetrics.stringWidth(line, "Mono", size) for line in lines) + 30
    d = Drawing(width, len(lines) * gap + 14)
    d.add(Rect(0, 0, width, len(lines) * gap + 14, fillColor=SOFT, strokeColor=None))
    for i, line in enumerate(lines):
        d.add(String(15, (len(lines) - 1 - i) * gap + 14, line, fontName="Mono", fontSize=size, fillColor=INK))
    return centered(d)


FIGURES = {"grid": fig_grid, "pyramid": fig_pyramid, "shape": fig_shape, "venn": fig_venn, "clock": fig_clock,
           "balance": fig_balance, "bars": fig_bars, "table": fig_table, "text": fig_text}


# ---------------------------------------------------------------- pages

class Cover(Flowable):
    """The front page: title, medal, rules, name line and a score box for the grown-up."""

    def __init__(self, title):
        super().__init__()
        self.title = title

    def wrap(self, aw, ah):
        self.w, self.h = aw, ah - 2
        return self.w, self.h

    def draw(self):
        c, w, top = self.canv, self.w, self.h
        mid = w / 2
        c.setFillColor(INK)
        c.setFont("Sans-Bold", 34)
        c.drawCentredString(mid, top - 70, "Mathlete Prep")
        c.setFont("Sans", 20)
        c.drawCentredString(mid, top - 105, "Math Contest Practice")
        c.setFont("Sans", 16)
        c.setFillColor(GREY)
        c.drawCentredString(mid, top - 132, "Grade 4 · Fall")
        self.medal(mid, top - 160, 2.3)
        c.setFillColor(INK)
        c.setFont("Sans-Bold", 26)
        c.drawCentredString(mid, top - 345, self.title)
        c.setFont("Sans", 14)
        c.drawCentredString(mid, top - 375, "20 questions  •  45 minutes  •  No calculators allowed")
        c.setFont("Sans", 11)
        c.setFillColor(GREY)
        c.drawCentredString(mid, top - 397, "Write each answer on the line. Use the empty space to work things out.")
        c.setFillColor(INK)
        c.setFont("Sans", 14)
        c.drawString(mid - 150, top - 450, "Student Name:")
        c.setLineWidth(1)
        c.line(mid - 36, top - 452, mid + 165, top - 452)
        c.drawString(mid - 150, top - 482, "Date:")
        c.line(mid - 105, top - 484, mid + 20, top - 484)

        box_w, box_h, box_y = 330, 92, top - 610
        c.setDash(2, 3)
        c.setStrokeColor(INK)
        c.rect(mid - box_w / 2, box_y, box_w, box_h)
        c.setDash()
        c.setFont("Sans-Bold", 11)
        c.drawCentredString(mid, box_y + box_h - 22, "For Grown-Ups Only")
        c.setFont("Sans", 11)
        c.drawString(mid - box_w / 2 + 22, box_y + 42, "Number of correct answers: __________")
        c.drawString(mid - box_w / 2 + 22, box_y + 18, "Score (correct answers × 5): __________ / 100")

    def medal(self, cx, top, s):
        """The app icon's gold medal, drawn from the same 108-unit grid as tools/make_art.py."""
        c = self.canv
        ox, oy = cx - 54 * s, top + 26 * s  # the medal art starts at y = 26 in the grid

        def path(points, fill):
            p = c.beginPath()
            x, y = points[0]
            p.moveTo(ox + x * s, oy - y * s)
            for x, y in points[1:]:
                p.lineTo(ox + x * s, oy - y * s)
            p.close()
            c.setFillColor(fill)
            c.drawPath(p, fill=1, stroke=0)

        def circle(x, y, r, fill):
            c.setFillColor(fill)
            c.circle(ox + x * s, oy - y * s, r * s, fill=1, stroke=0)

        path([(60, 26), (70, 26), (57, 54), (48, 50)], BLUE)
        path([(38, 26), (48, 26), (60, 50), (51, 54)], RED)
        circle(54, 62, 19, RIM)
        circle(54, 62, 15.5, GOLD)
        path([(55, 52), (59.5, 52), (59.5, 64), (63, 64), (63, 67.5), (59.5, 67.5), (59.5, 72),
              (55, 72), (55, 67.5), (45, 67.5), (45, 64)], BROWN)
        path([(55, 58), (55, 64), (50, 64)], GOLD)


def decorate(title, cover):
    def draw(canvas, doc):
        canvas.saveState()
        w, h = letter
        canvas.setStrokeColor(TEAL)
        canvas.setLineWidth(2.5)
        canvas.rect(0.4 * inch, 0.4 * inch, w - 0.8 * inch, h - 0.8 * inch)
        canvas.setLineWidth(0.8)
        canvas.rect(0.46 * inch, 0.46 * inch, w - 0.92 * inch, h - 0.92 * inch)
        if not cover:
            canvas.setFont("Sans", 9)
            canvas.setFillColor(GREY)
            canvas.drawString(0.75 * inch, 0.62 * inch, f"Mathlete Prep  ·  Grade 4 Fall  ·  {title}")
            canvas.drawRightString(w - 0.75 * inch, 0.62 * inch, f"Page {doc.page - 1}")
        canvas.restoreState()
    return draw


def answer_line(q):
    blank = "_" * 18
    before = html.escape(q.get("unitBefore", ""))
    unit = html.escape(q.get("unit", ""))
    return Paragraph(f"Answer: &nbsp;{before}{blank}&nbsp; {unit}", ANSWER)


def answer_text(q):
    return f"{q.get('unitBefore', '')}{q['answer']} {q.get('unit', '')}".strip()


def build(set_no, out_path):
    with open(os.path.join(BANK, f"set{set_no:02d}.json"), encoding="utf-8") as fh:
        data = json.load(fh)
    title = data["title"]
    story = [Cover(title), PageBreak()]

    for i, q in enumerate(data["questions"], 1):
        parts = [Paragraph(f"<b>{i})</b>&nbsp; {text(q['prompt'])}", QUESTION)]
        fig = q.get("figure")
        if fig:
            parts += [Spacer(1, 8), FIGURES[fig["type"]](fig)]
        parts.append(answer_line(q))
        # Room to work: more for the harder questions at the end.
        parts.append(Spacer(1, {1: 0.55, 2: 0.8, 3: 1.05}[q["difficulty"]] * inch))
        story.append(KeepTogether(parts))

    story += [Paragraph("~ The End ~", ParagraphStyle("end", parent=HEADING, fontName="Sans", fontSize=15)), PageBreak()]

    # Answer key: a quick grading table, then worked solutions.
    story += [Paragraph(f"{title}: Answers &amp; Solutions", HEADING),
              Paragraph("Grown-ups: remove these pages before the test. Each correct answer is worth 5 points.", SUBHEADING)]
    answers = [f"{i}.  {answer_text(q)}" for i, q in enumerate(data["questions"], 1)]
    rows = [[answers[r], answers[r + 10]] for r in range(10)]
    grid = Table(rows, colWidths=[3.2 * inch, 3.2 * inch], rowHeights=0.27 * inch)
    grid.setStyle(TableStyle([
        ("FONT", (0, 0), (-1, -1), "Sans", 11),
        ("LINEBELOW", (0, 0), (-1, -1), 0.5, LIGHT),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
    ]))
    story += [grid, Spacer(1, 18)]
    for i, q in enumerate(data["questions"], 1):
        story.append(Paragraph(f"<b>{i})</b>&nbsp; <b>({html.escape(answer_text(q))})</b>&nbsp; {text(q['solution'])}", KEY))

    doc = SimpleDocTemplate(out_path, pagesize=letter, leftMargin=0.75 * inch, rightMargin=0.75 * inch,
                            topMargin=0.8 * inch, bottomMargin=0.85 * inch, title=f"Mathlete Prep {title}",
                            author="Mathlete Prep")
    doc.build(story, onFirstPage=decorate(title, cover=True), onLaterPages=decorate(title, cover=False))


def main():
    set_no = int(sys.argv[1]) if len(sys.argv) > 1 else 1
    out = sys.argv[2] if len(sys.argv) > 2 else os.path.join(ROOT, "build", f"Mathlete_Prep_Practice_Test_{set_no}.pdf")
    os.makedirs(os.path.dirname(os.path.abspath(out)), exist_ok=True)
    build(set_no, out)
    print(out)


if __name__ == "__main__":
    main()
