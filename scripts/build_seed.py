import json
import pathlib
import re

archive = pathlib.Path(__file__).resolve().parent / "archive-data.js"
out = pathlib.Path(__file__).resolve().parent.parent / "backend" / "src" / "main" / "resources" / "data.sql"

src = archive.read_text(encoding="utf-8")
text = src.replace("window.PETS_ARCHIVE =", "", 1).strip()
if text.endswith(";"):
    text = text[:-1]
text = re.sub(r"([{\[,]\s*)([A-Za-z_][A-Za-z0-9_]*)\s*:", r'\1"\2":', text)
text = re.sub(r",\s*([}\]])", r"\1", text)
data = json.loads(text)


def q(value):
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


lines = ["SET NAMES utf8mb4;", ""]
artist = data["artist"]
roles = " / ".join(artist["roles"])
lines.append(
    "INSERT INTO artist (id, name, english_name, born, born_place, roles, tagline, bio) VALUES ("
    + ", ".join(
        [
            "1",
            q(artist["name"]),
            q(artist["english"]),
            q(artist["born"]),
            q(artist["bornPlace"]),
            q(roles),
            q(artist["tagline"]),
            q(artist["bio"]),
        ]
    )
    + ");"
)
lines.append("")

for index, voice in enumerate(data["voices"]):
    lines.append(
        "INSERT INTO voice (id, title, release_year, note, bvid, list_name, autoplay, sort_no) VALUES ("
        + ", ".join(
            [
                q(voice["id"]),
                q(voice["title"]),
                str(voice["year"]),
                q(voice.get("note") or ""),
                q(voice["bvid"]),
                q(voice["list"]),
                "1" if voice.get("autoplay") else "0",
                str(index),
            ]
        )
        + ");"
    )
lines.append("")

for index, quote in enumerate(data["quotes"]):
    lines.append(
        "INSERT INTO quote_item (id, content, origin, quote_year, sort_no) VALUES ("
        + ", ".join(
            [
                q(quote["id"]),
                q(quote["text"]),
                q(quote["source"]),
                str(quote["year"]),
                str(index),
            ]
        )
        + ");"
    )
    for tag_index, tag in enumerate(quote.get("tags") or []):
        lines.append(
            "INSERT INTO quote_tag (quote_id, tag, sort_no) VALUES ("
            + ", ".join([q(quote["id"]), q(tag), str(tag_index)])
            + ");"
        )
lines.append("")

for index, stage in enumerate(data["stages"]):
    lines.append(
        "INSERT INTO stage (id, stage_date, place, venue, title, note, kind, voice_id, sort_no) VALUES ("
        + ", ".join(
            [
                q(stage["id"]),
                q(stage["date"]),
                q(stage["place"]),
                q(stage["venue"]),
                q(stage["title"]),
                q(stage.get("note") or ""),
                q(stage["kind"]),
                q(stage.get("voiceId")),
                str(index),
            ]
        )
        + ");"
    )
lines.append("")

for index, album in enumerate(data["albums"], start=1):
    lines.append(
        "INSERT INTO album (id, release_year, title, label, sort_no) VALUES ("
        + ", ".join(
            [
                str(index),
                str(album["year"]),
                q(album["title"]),
                q(album["label"]),
                str(index),
            ]
        )
        + ");"
    )
    for hit_index, hit in enumerate(album.get("hits") or []):
        lines.append(
            "INSERT INTO album_hit (album_id, title, sort_no) VALUES ("
            + ", ".join([str(index), q(hit), str(hit_index)])
            + ");"
        )

out.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(
    f"OK voices={len(data['voices'])} quotes={len(data['quotes'])} "
    f"stages={len(data['stages'])} albums={len(data['albums'])} -> {out}"
)
