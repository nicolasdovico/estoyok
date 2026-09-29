import os
import math
import shutil
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

def add_glow(base_img, cx, cy, radius, color, alpha=45):
    """Draws a subtle radial ambient glow."""
    glow = Image.new('RGBA', base_img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    for r in range(radius, 0, -25):
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

def draw_arrow(draw, start, end, color=(0, 229, 255, 255), width=5, arrow_size=16):
    """Draws a directional arrow with a triangular head."""
    x0, y0 = start
    x1, y1 = end
    draw.line([start, end], fill=color, width=width)
    angle = math.atan2(y1 - y0, x1 - x0)
    angle1 = angle + math.pi * 0.82
    angle2 = angle - math.pi * 0.82
    p1 = (x1 + arrow_size * math.cos(angle1), y1 + arrow_size * math.sin(angle1))
    p2 = (x1 + arrow_size * math.cos(angle2), y1 + arrow_size * math.sin(angle2))
    draw.polygon([end, p1, p2], fill=color)

def apply_annotations(app_img, annotation_type):
    """Draws sleek focus callout boxes and directional arrows on the screenshot."""
    if not annotation_type:
        return app_img
    
    img = app_img.convert('RGBA')
    overlay = Image.new('RGBA', img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    font = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 28)

    if annotation_type == "login_google_register":
        cyan = (0, 229, 255, 255)
        green = (16, 185, 129, 255)
        # 1. Google Button Focus
        d.rounded_rectangle([(45, 1055), (675, 1148)], radius=20, outline=cyan, width=5)
        d.rounded_rectangle([(60, 975), (410, 1030)], radius=16, fill=(15, 23, 42, 245), outline=cyan, width=3)
        d.text((80, 986), "Opción 1: Con Google", font=font, fill=(255, 255, 255))
        draw_arrow(d, (230, 1030), (230, 1055), color=cyan, width=5, arrow_size=15)

        # 2. Register Link Focus (in gap between Google button and link)
        d.rounded_rectangle([(375, 1230), (615, 1282)], radius=12, outline=green, width=4)
        d.rounded_rectangle([(60, 1165), (410, 1220)], radius=16, fill=(15, 23, 42, 245), outline=green, width=3)
        d.text((80, 1176), "Opción 2: Crear Cuenta", font=font, fill=(255, 255, 255))
        draw_arrow(d, (350, 1215), (375, 1235), color=green, width=5, arrow_size=15)

    elif annotation_type == "nucleo_code":
        cyan = (0, 229, 255, 255)
        # Focus on copy code button at top right [3FIRL5LDLO]
        d.rounded_rectangle([(435, 250), (710, 335)], radius=18, outline=cyan, width=5)
        d.rounded_rectangle([(60, 260), (410, 315)], radius=16, fill=(15, 23, 42, 245), outline=cyan, width=3)
        d.text((80, 271), "Tu Código Familiar", font=font, fill=(255, 255, 255))
        draw_arrow(d, (410, 287), (435, 287), color=cyan, width=5, arrow_size=15)

    elif annotation_type == "estoy_ok_button":
        green = (16, 185, 129, 255)
        # Focus on central green button
        d.ellipse([(205, 680), (515, 990)], outline=green, width=6)
        d.rounded_rectangle([(160, 600), (560, 655)], radius=16, fill=(15, 23, 42, 245), outline=green, width=3)
        d.text((180, 611), "Tocá acá 1 vez al día", font=font, fill=(255, 255, 255))
        draw_arrow(d, (360, 655), (360, 680), color=green, width=5, arrow_size=16)

    elif annotation_type == "sos_button":
        red = (239, 68, 68, 255)
        # SOS button
        d.rounded_rectangle([(550, 100), (690, 180)], radius=16, outline=red, width=5)
        d.rounded_rectangle([(160, 110), (530, 165)], radius=16, fill=(15, 23, 42, 245), outline=red, width=3)
        d.text((180, 121), "Botón de Pánico SOS", font=font, fill=(255, 255, 255))
        draw_arrow(d, (530, 137), (550, 137), color=red, width=5, arrow_size=15)

    return Image.alpha_composite(img, overlay).convert('RGB')

def build_instagram_story(
    output_path,
    callout_emoji,
    callout_landmark,
    app_image_path,
    accent_color=(16, 185, 129),
    annotation_type=None
):
    """
    Generates a 1080x1920 Instagram Story background:
    - Top 50% (0..950px): Clean background gradient + ambient glow, 100% free of text,
      providing a seamless backdrop for the Google Flow avatar video.
    - Bottom 50% (950..1920px): Floating phone mockup, landmark pill, and focus arrows.
      The entire phone fits on screen with zero clipping at the bottom.
    """
    W, H = 1080, 1920
    
    # 1. Base gradient: Deep Navy -> Slate Navy
    bg = create_gradient(W, H, (10, 17, 38), (18, 28, 52))
    
    # Ambient glows (top for avatar backdrop, bottom for mockup)
    bg = add_glow(bg, W // 2, 450, 480, accent_color, alpha=22)
    bg = add_glow(bg, W // 2, 1450, 580, accent_color, alpha=36)
    
    font_callout = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 26)
    
    # 2. Landmark Pill at y: 955 (transition between video and app mockup)
    landmark_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    l_draw = ImageDraw.Draw(landmark_layer)
    
    emoji_img = render_emoji(callout_emoji, 30)
    emoji_w = emoji_img.width if emoji_img else 0
    emoji_gap = 10 if emoji_img else 0
    
    draw = ImageDraw.Draw(bg)
    l_bbox = draw.textbbox((0, 0), callout_landmark, font=font_callout)
    l_w = l_bbox[2] - l_bbox[0]
    l_h = l_bbox[3] - l_bbox[1]
    
    lm_total_w = emoji_w + emoji_gap + l_w
    pad_x, pad_y = 28, 10
    lm_pill_w = lm_total_w + pad_x * 2
    lm_pill_h = max(l_h, 30) + pad_y * 2
    lm_pill_x = (W - lm_pill_w) // 2
    lm_pill_y = 955
    
    # Glowing pill
    l_draw.rounded_rectangle(
        [(lm_pill_x, lm_pill_y), (lm_pill_x + lm_pill_w, lm_pill_y + lm_pill_h)],
        radius=lm_pill_h // 2,
        fill=(15, 23, 42, 245),
        outline=(accent_color[0], accent_color[1], accent_color[2], 255),
        width=3
    )
    
    if emoji_img:
        ex = lm_pill_x + pad_x
        ey = lm_pill_y + (lm_pill_h - emoji_img.height) // 2
        landmark_layer.paste(emoji_img, (ex, ey), emoji_img)
        tx = ex + emoji_w + emoji_gap
    else:
        tx = lm_pill_x + pad_x
        
    ty = lm_pill_y + (lm_pill_h - l_h) // 2 - 2
    l_draw.text((tx, ty), callout_landmark, font=font_callout, fill=(255, 255, 255))
    
    bg = Image.alpha_composite(bg.convert('RGBA'), landmark_layer).convert('RGB')
    
    # 3. Device Mockup Frame (fits completely inside the canvas, zero pixels clipped)
    app_raw = Image.open(app_image_path)
    app_img = apply_annotations(app_raw, annotation_type)
    
    phone_w = 390
    phone_h = int(phone_w * (app_img.size[1] / app_img.size[0])) # 866 px
    
    phone_x = (W - phone_w) // 2
    phone_y = lm_pill_y + lm_pill_h + 16 # 955 + 50 + 16 = 1021 px
    
    border_thick = 10
    screen_radius = 34
    frame_radius = screen_radius + border_thick
    
    frame_w = phone_w + border_thick * 2
    frame_h = phone_h + border_thick * 2
    frame_x = (W - frame_w) // 2
    frame_y = phone_y - border_thick # 1011 px -> bottom ends at 1897 px (23px margin)
    
    # Drop shadow
    shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [(frame_x - 8, frame_y + 8), (frame_x + frame_w + 8, frame_y + frame_h + 16)],
        radius=frame_radius + 6,
        fill=(0, 0, 0, 180)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(28))
    bg = Image.alpha_composite(bg.convert('RGBA'), shadow_layer).convert('RGB')
    
    # Phone Frame
    frame_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    f_draw = ImageDraw.Draw(frame_layer)
    
    # Bezel
    f_draw.rounded_rectangle(
        [(frame_x, frame_y), (frame_x + frame_w, frame_y + frame_h)],
        radius=frame_radius,
        fill=(26, 36, 52, 255),
        outline=(accent_color[0], accent_color[1], accent_color[2], 140),
        width=2
    )
    
    # Screen image
    app_resized = app_img.resize((phone_w, phone_h), Image.Resampling.LANCZOS).convert('RGBA')
    app_rounded = round_corners(app_resized, screen_radius)
    frame_layer.paste(app_rounded, (phone_x, phone_y), app_rounded)
    
    # Camera punch hole
    f_draw.ellipse([(W // 2 - 8, phone_y + 12), (W // 2 + 8, phone_y + 28)], fill=(10, 10, 10, 255))
    
    # Screen inner bevel
    f_draw.rounded_rectangle(
        [(phone_x, phone_y), (phone_x + phone_w, phone_y + phone_h)],
        radius=screen_radius,
        outline=(255, 255, 255, 25),
        width=2
    )
    
    bg = Image.alpha_composite(bg.convert('RGBA'), frame_layer).convert('RGB')
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    bg.save(output_path, 'PNG', quality=95)
    print(f"Generated: {output_path}")

def main():
    dest_dir = "docs/instagram_stories_assets"
    
    # Clean output directory so only the exact needed images exist
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)
    os.makedirs(dest_dir, exist_ok=True)
    
    stories = [
        # CARPETA 1: 🚀 EMPEZÁ ACÁ
        {
            "filename": f"{dest_dir}/destacada1_h1_login.png",
            "emoji": "👉",
            "landmark": "Tocá 'Google' o 'Registrate aquí'",
            "image": "docs/imagen06.jpg",
            "accent": (0, 229, 255),
            "annotation": "login_google_register"
        },
        {
            "filename": f"{dest_dir}/destacada1_h2_nucleo.png",
            "emoji": "👨‍👩‍👧",
            "landmark": "Pestaña Familia → Compartí tu Código",
            "image": "docs/captura_crear_nucleo.jpg",
            "accent": (0, 229, 255),
            "annotation": "nucleo_code"
        },
        {
            "filename": f"{dest_dir}/destacada1_h3_mapa.png",
            "emoji": "🟢",
            "landmark": "Pestaña Mapa → Conexión en Vivo y Batería",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 2: 🟢 EL BOTÓN "ESTOY OK"
        {
            "filename": f"{dest_dir}/destacada2_h1_estoy_ok.png",
            "emoji": "🟢",
            "landmark": "Pestaña Estoy OK → Botón de Bienestar",
            "image": "docs/imagen02.jpg",
            "accent": (16, 185, 129),
            "annotation": "estoy_ok_button"
        },
        {
            "filename": f"{dest_dir}/destacada2_h2_alerta_whatsapp.png",
            "emoji": "📲",
            "landmark": "Alerta Automática a Contactos SOS",
            "image": "docs/crash_alert_message_whatsapp.jpg",
            "accent": (37, 211, 102),
            "annotation": None
        },
        
        # CARPETA 3: 📍 ZONAS SEGURAS
        {
            "filename": f"{dest_dir}/destacada3_h1_zonas_crear.png",
            "emoji": "📍",
            "landmark": "Pestaña Mapa → Selector de Zonas Seguras",
            "image": "docs/zoom.jpg",
            "accent": (0, 229, 255),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada3_h2_zonas_notif.png",
            "emoji": "🔔",
            "landmark": "Alerta de Perímetro: Llegada a Casa",
            "image": "docs/captura_notif_zona.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 4: 🚨 CONTACTOS SOS & WHATSAPP
        {
            "filename": f"{dest_dir}/destacada4_h1_contactos_sos.png",
            "emoji": "👥",
            "landmark": "Pestaña Estoy OK → Agregar Contacto SOS",
            "image": "docs/captura_modal_contactos.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada4_h2_rescate_web.png",
            "emoji": "🚨",
            "landmark": "Web de Rescate → Botón 'Voy en camino'",
            "image": "docs/captura_rescate_web.jpg",
            "accent": (239, 68, 68),
            "annotation": "sos_button"
        },
        
        # CARPETA 5: 🚗 EN EL AUTO (SEGURIDAD VIAL)
        {
            "filename": f"{dest_dir}/destacada5_h1_conduccion.png",
            "emoji": "⭐",
            "landmark": "Pestaña Vehículo → Score de Manejo (70 pts)",
            "image": "docs/imagen03.jpg",
            "accent": (245, 158, 11),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada5_h2_choques.png",
            "emoji": "⚠️",
            "landmark": "Alerta de Impacto 4.80G y Aviso WhatsApp",
            "image": "docs/crash_alert_screen.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        
        # CARPETA 6: 🔋 BATERÍA & PRIVACIDAD
        {
            "filename": f"{dest_dir}/destacada6_h1_bateria.png",
            "emoji": "🔋",
            "landmark": "Pestaña Mapa → Nivel de Batería en Vivo",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada6_h2_privacidad.png",
            "emoji": "🔒",
            "landmark": "Privacidad Garantizada: Sin Anuncios",
            "image": "docs/imagen06.jpg",
            "accent": (168, 85, 247),
            "annotation": None
        }
    ]
    
    print(f"Generando {len(stories)} piezas definitivas para Historias Destacadas...")
    for s in stories:
        build_instagram_story(
            output_path=s["filename"],
            callout_emoji=s["emoji"],
            callout_landmark=s["landmark"],
            app_image_path=s["image"],
            accent_color=s["accent"],
            annotation_type=s.get("annotation")
        )
    print("\n¡Catálogo definitivo generado con éxito en docs/instagram_stories_assets/!")

if __name__ == "__main__":
    main()
