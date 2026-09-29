import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_gradient(width, height, top_color, bottom_color):
    """Creates a vertical linear gradient image."""
    top_r, top_g, top_b = top_color
    bot_r, bot_g, bot_b = bottom_color
    
    gradient = Image.new('RGB', (1, height))
    for y in range(height):
        ratio = y / float(height - 1)
        r = int(top_r + (bot_r - top_r) * ratio)
        g = int(top_g + (bot_g - top_g) * ratio)
        b = int(top_b + (bot_b - top_b) * ratio)
        gradient.putpixel((0, y), (r, g, b))
    
    return gradient.resize((width, height), Image.Resampling.BILINEAR)

def round_corners(image, radius):
    """Rounds the corners of an RGBA image."""
    mask = Image.new('L', image.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), image.size], radius=radius, fill=255)
    result = image.copy()
    result.putalpha(mask)
    return result

def add_glow(base_img, cx, cy, radius, color, alpha=40):
    """Draws a subtle radial ambient glow."""
    glow = Image.new('RGBA', base_img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    for r in range(radius, 0, -20):
        current_alpha = int(alpha * (1.0 - (r / radius) ** 0.5))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(color[0], color[1], color[2], current_alpha))
    
    glow = glow.filter(ImageFilter.GaussianBlur(35))
    return Image.alpha_composite(base_img.convert('RGBA'), glow).convert('RGB')

def render_emoji(emoji_char, target_size):
    """Renders a crisp emoji using NotoColorEmoji downscaled with Lanczos."""
    try:
        font_emoji = ImageFont.truetype('/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf', 109)
        temp = Image.new('RGBA', (220, 220), (0, 0, 0, 0))
        d = ImageDraw.Draw(temp)
        d.text((20, 20), emoji_char, font=font_emoji, embedded_color=True)
        bbox = temp.getbbox()
        if bbox:
            cropped = temp.crop(bbox)
            aspect = cropped.width / cropped.height
            new_h = target_size
            new_w = max(1, int(target_size * aspect))
            return cropped.resize((new_w, new_h), Image.Resampling.LANCZOS)
    except Exception as e:
        print(f"Emoji render error for {emoji_char}: {e}")
    return None

def build_screenshot(
    output_path,
    title_lines,
    subtitle_text,
    callout_emoji,
    callout_text,
    app_image_path,
    accent_color=(16, 185, 129)
):
    W, H = 1080, 2400
    
    # 1. Base gradient: Deep Navy -> Slate Navy
    bg = create_gradient(W, H, (10, 17, 38), (18, 28, 52))
    
    # Ambient glows
    bg = add_glow(bg, W // 2, 280, 500, accent_color, alpha=38)
    bg = add_glow(bg, W // 2, 1600, 680, accent_color, alpha=24)
    
    draw = ImageDraw.Draw(bg)
    
    # Fonts
    font_title = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 58)
    font_sub = ImageFont.truetype('android-native/app/src/main/res/font/outfit_medium.ttf', 32)
    font_callout = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 30)
    
    # 2. Render Header (Title Lines)
    margin_top = 115
    for t_line in title_lines:
        t_bbox = draw.textbbox((0, 0), t_line, font=font_title)
        t_w = t_bbox[2] - t_bbox[0]
        draw.text(((W - t_w) // 2, margin_top), t_line, font=font_title, fill=(255, 255, 255))
        margin_top += (t_bbox[3] - t_bbox[0]) + 12
    
    margin_top += 12
    
    # Wrap subtitle
    words = subtitle_text.split()
    sub_lines = []
    curr_line = ""
    for w in words:
        test = curr_line + (" " if curr_line else "") + w
        bbox = draw.textbbox((0, 0), test, font=font_sub)
        if (bbox[2] - bbox[0]) < 900:
            curr_line = test
        else:
            sub_lines.append(curr_line)
            curr_line = w
    if curr_line:
        sub_lines.append(curr_line)
        
    for line in sub_lines:
        s_bbox = draw.textbbox((0, 0), line, font=font_sub)
        s_w = s_bbox[2] - s_bbox[0]
        draw.text(((W - s_w) // 2, margin_top), line, font=font_sub, fill=(203, 213, 225))
        margin_top += 44
        
    # 3. Callout Badge Pill
    margin_top += 20
    c_bbox = draw.textbbox((0, 0), callout_text, font=font_callout)
    text_w = c_bbox[2] - c_bbox[0]
    text_h = c_bbox[3] - c_bbox[1]
    
    emoji_img = render_emoji(callout_emoji, 36)
    emoji_w = emoji_img.width if emoji_img else 0
    emoji_gap = 12 if emoji_img else 0
    
    content_w = emoji_w + emoji_gap + text_w
    pad_x = 36
    pad_y = 14
    pill_w = content_w + pad_x * 2
    pill_h = max(text_h, 36) + pad_y * 2
    pill_x = (W - pill_w) // 2
    pill_y = margin_top
    
    pill_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    p_draw = ImageDraw.Draw(pill_layer)
    
    # Pill background with glowing border
    p_draw.rounded_rectangle(
        [(pill_x, pill_y), (pill_x + pill_w, pill_y + pill_h)],
        radius=pill_h // 2,
        fill=(15, 23, 42, 235),
        outline=(accent_color[0], accent_color[1], accent_color[2], 220),
        width=3
    )
    
    # Draw emoji inside pill
    if emoji_img:
        emoji_x = pill_x + pad_x
        emoji_y = pill_y + (pill_h - emoji_img.height) // 2
        pill_layer.paste(emoji_img, (emoji_x, emoji_y), emoji_img)
        text_x = emoji_x + emoji_w + emoji_gap
    else:
        text_x = pill_x + pad_x
        
    text_y = pill_y + (pill_h - text_h) // 2 - 3
    p_draw.text((text_x, text_y), callout_text, font=font_callout, fill=(255, 255, 255))
    
    bg = Image.alpha_composite(bg.convert('RGBA'), pill_layer).convert('RGB')
    
    # 4. Device Mockup Frame
    app_img = Image.open(app_image_path)
    
    phone_w = 830
    phone_h = int(phone_w * (app_img.size[1] / app_img.size[0]))
    
    phone_x = (W - phone_w) // 2
    phone_y = pill_y + pill_h + 38
    
    border_thick = 16
    screen_radius = 48
    frame_radius = screen_radius + border_thick
    
    frame_w = phone_w + border_thick * 2
    frame_h = phone_h + border_thick * 2
    frame_x = (W - frame_w) // 2
    frame_y = phone_y - border_thick
    
    # Drop shadow
    shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [(frame_x - 12, frame_y + 12), (frame_x + frame_w + 12, frame_y + frame_h + 30)],
        radius=frame_radius + 12,
        fill=(0, 0, 0, 160)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(38))
    bg = Image.alpha_composite(bg.convert('RGBA'), shadow_layer).convert('RGB')
    
    # Device frame
    frame_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    f_draw = ImageDraw.Draw(frame_layer)
    
    # Graphite / Titanium frame
    f_draw.rounded_rectangle(
        [(frame_x, frame_y), (frame_x + frame_w, frame_y + frame_h)],
        radius=frame_radius,
        fill=(28, 38, 56, 255),
        outline=(60, 75, 98, 255),
        width=3
    )
    
    # Screen image
    app_resized = app_img.resize((phone_w, phone_h), Image.Resampling.LANCZOS).convert('RGBA')
    app_rounded = round_corners(app_resized, screen_radius)
    frame_layer.paste(app_rounded, (phone_x, phone_y), app_rounded)
    
    # Camera punch hole
    punch_w, punch_h = 24, 24
    punch_x = (W - punch_w) // 2
    punch_y = phone_y + 18
    f_draw.ellipse([(punch_x, punch_y), (punch_x + punch_w, punch_y + punch_h)], fill=(10, 10, 10, 255))
    
    # Inner bevel highlight
    f_draw.rounded_rectangle(
        [(phone_x, phone_y), (phone_x + phone_w, phone_y + phone_h)],
        radius=screen_radius,
        outline=(255, 255, 255, 25),
        width=2
    )
    
    bg = Image.alpha_composite(bg.convert('RGBA'), frame_layer).convert('RGB')
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    bg.save(output_path, 'PNG', quality=95)
    print(f"Generated: {output_path} ({W}x{H})")

def main():
    dest_dir = "docs/play_store_screenshots"
    os.makedirs(dest_dir, exist_ok=True)
    
    screenshots = [
        {
            "filename": f"{dest_dir}/screenshot_01_mapa.png",
            "title_lines": ["Tranquilidad Familiar", "en Tiempo Real"],
            "subtitle": "Localizá a tu núcleo en un mapa interactivo de alta precisión",
            "callout_emoji": "🟢",
            "callout_text": "Familia Conectada en Vivo",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129) # Emerald
        },
        {
            "filename": f"{dest_dir}/screenshot_02_estoy_ok.png",
            "title_lines": ["Confirmá que estás bien", "en un solo toque"],
            "subtitle": "Si no confirmás en tu horario habitual, la app avisa por vos",
            "callout_emoji": "📲",
            "callout_text": "Aviso Automático a Contactos SOS",
            "image": "docs/imagen02.jpg",
            "accent": (16, 185, 129) # Emerald
        },
        {
            "filename": f"{dest_dir}/screenshot_03_sos.png",
            "title_lines": ["Alerta SOS con Audio", "y Coordinación de Rescate"],
            "subtitle": "Envía tu ubicación exacta por WhatsApp a tus contactos en segundos",
            "callout_emoji": "🚨",
            "callout_text": "Alerta Inmediata a WhatsApp",
            "image": "docs/crash_alert_screen.jpg",
            "accent": (239, 68, 68) # Red Alert
        },
        {
            "filename": f"{dest_dir}/screenshot_04_zonas_seguras.png",
            "title_lines": ["Llegadas y Salidas", "Automáticas"],
            "subtitle": "Creá Zonas Seguras para Hogar, Escuela y Trabajo",
            "callout_emoji": "📍",
            "callout_text": "Llegada a Zona Segura Confirmada",
            "image": "docs/zoom.jpg",
            "accent": (16, 185, 129) # Emerald
        },
        {
            "filename": f"{dest_dir}/screenshot_05_conduccion.png",
            "title_lines": ["Conducción Segura", "y Hábitos Viales"],
            "subtitle": "Monitoreo de velocidad, frenadas bruscas y uso de celular",
            "callout_emoji": "⭐",
            "callout_text": "Puntuación de Manejo: 98 pts",
            "image": "docs/imagen03.jpg",
            "accent": (245, 158, 11) # Amber
        },
        {
            "filename": f"{dest_dir}/screenshot_06_login_google.png",
            "title_lines": ["Registro Rápido", "y Privacidad Total"],
            "subtitle": "Continuá con Google en un toque. Tus datos nunca se venden.",
            "callout_emoji": "🔒",
            "callout_text": "Cifrado de Datos y Batería Inteligente",
            "image": "docs/imagen06.jpg",
            "accent": (6, 182, 212) # Cyan/Teal
        }
    ]
    
    for s in screenshots:
        build_screenshot(
            output_path=s["filename"],
            title_lines=s["title_lines"],
            subtitle_text=s["subtitle"],
            callout_emoji=s["callout_emoji"],
            callout_text=s["callout_text"],
            app_image_path=s["image"],
            accent_color=s["accent"]
        )

if __name__ == '__main__':
    main()
