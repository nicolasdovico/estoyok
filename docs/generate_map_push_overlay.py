#!/usr/bin/env python3
"""
Generate the Estoy Ok push notification banner and composite it over
docs/broll_mapa_crudo.mp4 with an authentic ease-out Slide Down animation.

Outputs:
- docs/push_notification_banner.png (Clean RGBA asset)
- docs/broll_mapa_con_notificacion.mp4 (576x1280 native 50fps)
- docs/broll_mapa_con_notificacion_9x16.mp4 (1080x1920 Reel 9:16 standard)
"""

import os
import subprocess
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_notification_banner(width=536, scale=1.0):
    """
    Renders an authentic Android notification card matching captura_notif_zona.jpg.
    """
    w = int(width * scale)
    h = int(136 * scale)
    radius = int(24 * scale)
    pad = int(16 * scale)

    total_w = w + pad * 2
    total_h = h + pad * 2

    img = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))

    # 1. Soft Ambient Shadow (Android Material You elevation)
    shadow = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle(
        [pad, pad + int(4 * scale), pad + w, pad + h + int(4 * scale)],
        radius=radius,
        fill=(0, 0, 0, 45)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(int(8 * scale)))
    img.paste(shadow, (0, 0), shadow)

    # 2. Main Card Surface
    card = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    cdraw = ImageDraw.Draw(card)
    cdraw.rounded_rectangle(
        [pad, pad, pad + w, pad + h],
        radius=radius,
        fill=(252, 252, 255, 255) # Clean Material surface
    )

    # 3. Estoy Ok Green Circle Badge with White Map-Pin/Check Logo
    badge_size = int(32 * scale)
    badge = Image.new("RGBA", (badge_size * 4, badge_size * 4), (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(badge)
    # Dark green from Estoy Ok Android notification (#0D7048)
    bdraw.ellipse([0, 0, badge_size * 4 - 1, badge_size * 4 - 1], fill=(13, 112, 72, 255))

    fg_path = "mobile/assets/images/android-icon-foreground.png"
    if os.path.exists(fg_path):
        fg = Image.open(fg_path).convert("RGBA")
        bbox = fg.getbbox()
        if bbox:
            fg_tight = fg.crop(bbox)
            white_logo = Image.new("RGBA", fg_tight.size, (255, 255, 255, 0))
            for x in range(fg_tight.width):
                for y in range(fg_tight.height):
                    r, g, b, a = fg_tight.getpixel((x, y))
                    if a > 30:
                        white_logo.putpixel((x, y), (255, 255, 255, a))
            
            target_h = int(badge_size * 4 * 0.62)
            target_w = int(white_logo.width * (target_h / white_logo.height))
            logo_resized = white_logo.resize((target_w, target_h), Image.Resampling.LANCZOS)
            pos_x = (badge_size * 4 - target_w) // 2
            pos_y = (badge_size * 4 - target_h) // 2
            badge.paste(logo_resized, (pos_x, pos_y), logo_resized)

    badge = badge.resize((badge_size, badge_size), Image.Resampling.LANCZOS)
    badge_x = pad + int(18 * scale)
    badge_y = pad + int(14 * scale)
    card.paste(badge, (badge_x, badge_y), badge)

    # 4. Fonts
    font_reg = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"
    font_bold = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"

    f_header = ImageFont.truetype(font_reg, int(13 * scale))
    f_title = ImageFont.truetype(font_bold, int(16 * scale))
    f_body = ImageFont.truetype(font_reg, int(14 * scale))

    text_x = pad + int(60 * scale)

    # 5. Header row: "Estoy Ok • ahora"
    cdraw.text((text_x, pad + int(19 * scale)), "Estoy Ok  •  ahora", font=f_header, fill=(95, 99, 104, 255))

    # Up/expand chevron on top right
    cdraw.text((pad + w - int(28 * scale), pad + int(16 * scale)), "^", font=f_title, fill=(95, 99, 104, 255))

    # 6. Title row: "Alerta de Perímetro"
    cdraw.text((text_x, pad + int(50 * scale)), "Alerta de Perímetro", font=f_title, fill=(28, 27, 31, 255))

    # 7. Body row: "Lucas ha ingresado a: Colegio"
    cdraw.text((text_x, pad + int(80 * scale)), "Lucas ha ingresado a: Colegio", font=f_body, fill=(73, 69, 79, 255))

    img.paste(card, (0, 0), card)
    return img

def render_videos():
    out_dir = "docs/reels_assets/reel1_padres_auto"
    os.makedirs(out_dir, exist_ok=True)

    # 1. Save banner asset
    banner = create_notification_banner(width=536, scale=1.0)
    banner_path = f"{out_dir}/push_notification_banner.png"
    banner.save(banner_path)
    print(f"1. Saved banner asset: {banner_path}")

    # 2. Render native 576x1280 composited video with ease-out slide-down
    # Slide down starts at t=2.0s, finishes at t=2.4s (resting at y=30px for pad=16 => visible card at y=46px)
    crudo_path = f"{out_dir}/broll_mapa_crudo.mp4"
    native_out = f"{out_dir}/broll_mapa_con_notificacion.mp4"
    reels_out = f"{out_dir}/broll_mapa_con_notificacion_9x16.mp4"

    cmd_native = [
        "ffmpeg", "-i", crudo_path,
        "-i", banner_path,
        "-filter_complex",
        "[0:v][1:v]overlay=x=(W-w)/2:y='if(lt(t,2.0), -h, if(lt(t,2.4), -h + (30 + h)*(1 - pow(1 - (t-2.0)/0.4, 3)), 30))':eval=frame[v]",
        "-map", "[v]", "-map", "0:a?",
        "-c:v", "libx264", "-preset", "fast", "-crf", "18", "-pix_fmt", "yuv420p",
        native_out, "-y"
    ]
    print(f"2. Rendering native video {native_out}...")
    subprocess.run(cmd_native, check=True)
    print("Native video render complete.")

    # 3. Render 1080x1920 Reel format
    cmd_reels = [
        "ffmpeg", "-i", native_out,
        "-vf", "scale=1080:1920:flags=lanczos",
        "-c:v", "libx264", "-preset", "fast", "-crf", "18", "-pix_fmt", "yuv420p",
        "-c:a", "copy",
        reels_out, "-y"
    ]
    print(f"3. Rendering 1080x1920 Reel video {reels_out}...")
    subprocess.run(cmd_reels, check=True)
    print("1080x1920 Reel video render complete.")

if __name__ == "__main__":
    render_videos()
