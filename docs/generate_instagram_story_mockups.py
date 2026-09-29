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

def build_instagram_story(
    output_prefix,
    folder_name,
    story_number,
    story_title,
    callout_emoji,
    callout_landmark,
    app_image_path,
    accent_color=(16, 185, 129),
    export_bottom_slice=True
):
    """
    Generates:
    1. Full 1080x1920 Instagram Story template:
       - Top 50% (0..960px): Reserved for Google Flow video overlay with header badge.
       - Bottom 50% (960..1920px): Floating Phone Mockup + Landmark focus badge.
    2. Optional bottom slice (1080x960px) for direct timeline track import.
    """
    W, H = 1080, 1920
    HALF_H = 960
    
    # 1. Base gradient: Deep Navy -> Slate Navy
    bg = create_gradient(W, H, (10, 17, 38), (18, 28, 52))
    
    # Ambient glows
    bg = add_glow(bg, W // 2, 220, 420, accent_color, alpha=26)
    bg = add_glow(bg, W // 2, 1420, 600, accent_color, alpha=32)
    
    # Fonts
    font_folder = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 24)
    font_title = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 38)
    font_callout = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 28)
    font_guide = ImageFont.truetype('android-native/app/src/main/res/font/outfit_medium.ttf', 20)
    
    # 2. Top Half Branding (y: 60..180)
    draw = ImageDraw.Draw(bg)
    folder_pill_text = f"{folder_name}  •  HISTORIA {story_number}".upper()
    f_bbox = draw.textbbox((0, 0), folder_pill_text, font=font_folder)
    f_w = f_bbox[2] - f_bbox[0]
    f_h = f_bbox[3] - f_bbox[1]
    
    pill_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    p_draw = ImageDraw.Draw(pill_layer)
    
    # Top Folder Pill
    pill_px = (W - (f_w + 48)) // 2
    pill_py = 65
    p_draw.rounded_rectangle(
        [(pill_px, pill_py), (pill_px + f_w + 48, pill_py + f_h + 16)],
        radius=(f_h + 16) // 2,
        fill=(15, 23, 42, 220),
        outline=(accent_color[0], accent_color[1], accent_color[2], 200),
        width=2
    )
    p_draw.text((pill_px + 24, pill_py + 8), folder_pill_text, font=font_folder, fill=(240, 246, 255))
    
    # Story Title below folder pill
    t_bbox = draw.textbbox((0, 0), story_title, font=font_title)
    t_w = t_bbox[2] - t_bbox[0]
    p_draw.text(((W - t_w) // 2, pill_py + f_h + 30), story_title, font=font_title, fill=(255, 255, 255))
    
    # Video Area Guide indicator (Subtle dashed style or guide text)
    p_draw.line([(60, HALF_H), (W - 60, HALF_H)], fill=(255, 255, 255, 40), width=2)
    
    bg = Image.alpha_composite(bg.convert('RGBA'), pill_layer).convert('RGB')
    
    # 3. Bottom Half Landmark Pill (at y: 995)
    landmark_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    l_draw = ImageDraw.Draw(landmark_layer)
    
    emoji_img = render_emoji(callout_emoji, 32)
    emoji_w = emoji_img.width if emoji_img else 0
    emoji_gap = 10 if emoji_img else 0
    
    l_bbox = draw.textbbox((0, 0), callout_landmark, font=font_callout)
    l_w = l_bbox[2] - l_bbox[0]
    l_h = l_bbox[3] - l_bbox[1]
    
    lm_total_w = emoji_w + emoji_gap + l_w
    lm_pad_x = 32
    lm_pad_y = 12
    lm_pill_w = lm_total_w + lm_pad_x * 2
    lm_pill_h = max(l_h, 32) + lm_pad_y * 2
    lm_pill_x = (W - lm_pill_w) // 2
    lm_pill_y = HALF_H + 35
    
    # Pill with neon glowing border
    l_draw.rounded_rectangle(
        [(lm_pill_x, lm_pill_y), (lm_pill_x + lm_pill_w, lm_pill_y + lm_pill_h)],
        radius=lm_pill_h // 2,
        fill=(15, 23, 42, 240),
        outline=(accent_color[0], accent_color[1], accent_color[2], 255),
        width=3
    )
    
    if emoji_img:
        ex = lm_pill_x + lm_pad_x
        ey = lm_pill_y + (lm_pill_h - emoji_img.height) // 2
        landmark_layer.paste(emoji_img, (ex, ey), emoji_img)
        tx = ex + emoji_w + emoji_gap
    else:
        tx = lm_pill_x + lm_pad_x
        
    ty = lm_pill_y + (lm_pill_h - l_h) // 2 - 2
    l_draw.text((tx, ty), callout_landmark, font=font_callout, fill=(255, 255, 255))
    
    bg = Image.alpha_composite(bg.convert('RGBA'), landmark_layer).convert('RGB')
    
    # 4. Device Mockup Frame (y: 1085..1920)
    app_img = Image.open(app_image_path)
    
    # Clean phone width of 480px fits beautifully with ample breathing room
    phone_w = 480
    phone_h = int(phone_w * (app_img.size[1] / app_img.size[0]))
    
    phone_x = (W - phone_w) // 2
    phone_y = lm_pill_y + lm_pill_h + 30
    
    border_thick = 12
    screen_radius = 38
    frame_radius = screen_radius + border_thick
    
    frame_w = phone_w + border_thick * 2
    frame_h = phone_h + border_thick * 2
    frame_x = (W - frame_w) // 2
    frame_y = phone_y - border_thick
    
    # Drop shadow
    shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [(frame_x - 10, frame_y + 10), (frame_x + frame_w + 10, frame_y + frame_h + 20)],
        radius=frame_radius + 8,
        fill=(0, 0, 0, 170)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(30))
    bg = Image.alpha_composite(bg.convert('RGBA'), shadow_layer).convert('RGB')
    
    # Phone Frame
    frame_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    f_draw = ImageDraw.Draw(frame_layer)
    
    # Titanium / Graphite bezel
    f_draw.rounded_rectangle(
        [(frame_x, frame_y), (frame_x + frame_w, frame_y + frame_h)],
        radius=frame_radius,
        fill=(28, 38, 56, 255),
        outline=(accent_color[0], accent_color[1], accent_color[2], 120),
        width=2
    )
    
    # App screen content
    app_resized = app_img.resize((phone_w, phone_h), Image.Resampling.LANCZOS).convert('RGBA')
    app_rounded = round_corners(app_resized, screen_radius)
    frame_layer.paste(app_rounded, (phone_x, phone_y), app_rounded)
    
    # Front camera punch hole
    punch_w, punch_h = 18, 18
    punch_x = (W - punch_w) // 2
    punch_y = phone_y + 14
    f_draw.ellipse([(punch_x, punch_y), (punch_x + punch_w, punch_y + punch_h)], fill=(12, 12, 12, 255))
    
    # Screen inner bevel
    f_draw.rounded_rectangle(
        [(phone_x, phone_y), (phone_x + phone_w, phone_y + phone_h)],
        radius=screen_radius,
        outline=(255, 255, 255, 30),
        width=2
    )
    
    bg = Image.alpha_composite(bg.convert('RGBA'), frame_layer).convert('RGB')
    
    # 5. Export Full Story Template (1080 x 1920)
    full_path = f"{output_prefix}_full.png"
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    bg.save(full_path, 'PNG', quality=95)
    print(f"Generated Full Template: {full_path}")
    
    # 6. Export Bottom Slice (1080 x 960)
    if export_bottom_slice:
        bottom_slice = bg.crop((0, HALF_H, W, H))
        slice_path = f"{output_prefix}_bottom_960.png"
        bottom_slice.save(slice_path, 'PNG', quality=95)
        print(f"Generated Bottom Slice: {slice_path}")

def main():
    dest_dir = "docs/instagram_stories_assets"
    os.makedirs(dest_dir, exist_ok=True)
    
    stories = [
        # CARPETA 1: 🚀 EMPEZÁ ACÁ
        {
            "prefix": f"{dest_dir}/destacada1_h1_login",
            "folder": "🚀 Empezá Acá",
            "number": "1/3",
            "title": "Creá tu Cuenta o Ingresá con Google",
            "emoji": "🔐",
            "landmark": "Acceso Rápido con Google o Registro",
            "image": "docs/imagen06.jpg",
            "accent": (16, 185, 129) # Emerald
        },
        {
            "prefix": f"{dest_dir}/destacada1_h2_nucleo",
            "folder": "🚀 Empezá Acá",
            "number": "2/3",
            "title": "Tu Código de Invitación Familiar",
            "emoji": "👨‍👩‍👧",
            "landmark": "Pestaña Familia → Compartí tu Código",
            "image": "docs/captura_crear_nucleo.jpg",
            "accent": (0, 229, 255) # Electric Teal
        },
        {
            "prefix": f"{dest_dir}/destacada1_h3_mapa",
            "folder": "🚀 Empezá Acá",
            "number": "3/3",
            "title": "Ubicación en Vivo de tu Familia",
            "emoji": "🟢",
            "landmark": "Pestaña Mapa → Conexión en Vivo y Batería",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129)
        },
        
        # CARPETA 2: 🟢 EL BOTÓN "ESTOY OK"
        {
            "prefix": f"{dest_dir}/destacada2_h1_estoy_ok",
            "folder": "🟢 El Botón Estoy OK",
            "number": "1/2",
            "title": "Check-in Diario en 1 Solo Toque",
            "emoji": "🟢",
            "landmark": "Pestaña Estoy OK → Botón de Bienestar",
            "image": "docs/imagen02.jpg",
            "accent": (16, 185, 129)
        },
        {
            "prefix": f"{dest_dir}/destacada2_h2_alerta_whatsapp",
            "folder": "🟢 El Botón Estoy OK",
            "number": "2/2",
            "title": "Aviso Automático por WhatsApp",
            "emoji": "📲",
            "landmark": "Alerta Automática a Contactos SOS",
            "image": "docs/crash_alert_message_whatsapp.jpg",
            "accent": (37, 211, 102) # WhatsApp Green
        },
        
        # CARPETA 3: 📍 ZONAS SEGURAS
        {
            "prefix": f"{dest_dir}/destacada3_h1_zonas_crear",
            "folder": "📍 Zonas Seguras",
            "number": "1/2",
            "title": "Llegadas y Salidas Automáticas",
            "emoji": "📍",
            "landmark": "Pestaña Mapa → Selector de Zonas Seguras",
            "image": "docs/zoom.jpg",
            "accent": (0, 229, 255)
        },
        {
            "prefix": f"{dest_dir}/destacada3_h2_zonas_notif",
            "folder": "📍 Zonas Seguras",
            "number": "2/2",
            "title": "Avisos Inmediatos al Celular",
            "emoji": "🔔",
            "landmark": "Alerta de Perímetro: Llegada a Casa",
            "image": "docs/captura_notif_zona.jpg",
            "accent": (16, 185, 129)
        },
        
        # CARPETA 4: 🚨 CONTACTOS SOS & WHATSAPP
        {
            "prefix": f"{dest_dir}/destacada4_h1_contactos_sos",
            "folder": "🚨 Contactos SOS",
            "number": "1/2",
            "title": "Cargá tus Contactos de Emergencia",
            "emoji": "👥",
            "landmark": "Pestaña Estoy OK → Agregar Contacto SOS",
            "image": "docs/captura_modal_contactos.jpg",
            "accent": (239, 68, 68) # SOS Red
        },
        {
            "prefix": f"{dest_dir}/destacada4_h2_rescate_web",
            "folder": "🚨 Contactos SOS",
            "number": "2/2",
            "title": "Botón de Pánico y Coordinación de Rescate",
            "emoji": "🚨",
            "landmark": "Web de Rescate → Botón 'Voy en camino'",
            "image": "docs/captura_rescate_web.jpg",
            "accent": (239, 68, 68)
        },
        
        # CARPETA 5: 🚗 EN EL AUTO (SEGURIDAD VIAL)
        {
            "prefix": f"{dest_dir}/destacada5_h1_conduccion",
            "folder": "🚗 Seguridad Vial",
            "number": "1/2",
            "title": "Score Semanal y Hábitos al Volante",
            "emoji": "⭐",
            "landmark": "Pestaña Vehículo → Score de Manejo (70 pts)",
            "image": "docs/imagen03.jpg",
            "accent": (245, 158, 11) # Amber
        },
        {
            "prefix": f"{dest_dir}/destacada5_h2_choques",
            "folder": "🚗 Seguridad Vial",
            "number": "2/2",
            "title": "Detección de Choques por Fuerza G",
            "emoji": "⚠️",
            "landmark": "Alerta de Impacto 4.80G y Aviso WhatsApp",
            "image": "docs/crash_alert_screen.jpg",
            "accent": (239, 68, 68)
        },
        
        # CARPETA 6: 🔋 BATERÍA & PRIVACIDAD
        {
            "prefix": f"{dest_dir}/destacada6_h1_bateria",
            "folder": "🔋 Batería & Privacidad",
            "number": "1/2",
            "title": "GPS Inteligente de Ultra Bajo Consumo",
            "emoji": "🔋",
            "landmark": "Pestaña Mapa → Nivel de Batería en Vivo",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129)
        },
        {
            "prefix": f"{dest_dir}/destacada6_h2_privacidad",
            "folder": "🔋 Batería & Privacidad",
            "number": "2/2",
            "title": "Cifrado Total y Cero Publicidad",
            "emoji": "🔒",
            "landmark": "Privacidad Garantizada: Sin Anuncios",
            "image": "docs/imagen06.jpg",
            "accent": (168, 85, 247) # Indigo / Purple
        }
    ]
    
    print(f"Generando {len(stories)} piezas para Historias Destacadas de Instagram...")
    for s in stories:
        build_instagram_story(
            output_prefix=s["prefix"],
            folder_name=s["folder"],
            story_number=s["number"],
            story_title=s["title"],
            callout_emoji=s["emoji"],
            callout_landmark=s["landmark"],
            app_image_path=s["image"],
            accent_color=s["accent"],
            export_bottom_slice=True
        )
    print("\n¡Todas las piezas generadas exitosamente en docs/instagram_stories_assets/!")

if __name__ == "__main__":
    main()
