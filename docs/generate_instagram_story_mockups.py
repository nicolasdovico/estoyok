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

def add_glow(base_img, cx, cy, radius, color, alpha=35):
    """Draws a subtle radial ambient glow."""
    glow = Image.new('RGBA', base_img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    for r in range(radius, 0, -25):
        current_alpha = int(alpha * (1.0 - (r / radius) ** 0.5))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(color[0], color[1], color[2], current_alpha))
    
    glow = glow.filter(ImageFilter.GaussianBlur(40))
    return Image.alpha_composite(base_img.convert('RGBA'), glow).convert('RGB')

def draw_arrow(draw, start, end, color=(0, 229, 255, 255), width=6, arrow_size=18):
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

def apply_annotations(app_img, annotation_type):
    """Draws sleek focus callout boxes and directional arrows on the screenshot."""
    if not annotation_type:
        return app_img
    
    img = app_img.convert('RGBA')
    overlay = Image.new('RGBA', img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    font = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 30)
    font_bold = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 25)
    font_med = ImageFont.truetype('android-native/app/src/main/res/font/outfit_medium.ttf', 23)

    if annotation_type == "login_google_register":
        cyan = (0, 229, 255, 255)
        green = (16, 185, 129, 255)
        # 1. Google Button Focus
        d.rounded_rectangle([(45, 1055), (675, 1148)], radius=20, outline=cyan, width=6)
        d.rounded_rectangle([(60, 975), (410, 1030)], radius=16, fill=(15, 23, 42, 245), outline=cyan, width=3)
        d.text((80, 986), "Opción 1: Con Google", font=font, fill=(255, 255, 255))
        draw_arrow(d, (230, 1030), (230, 1055), color=cyan, width=6, arrow_size=16)

        # 2. Register Link Focus
        d.rounded_rectangle([(375, 1230), (615, 1282)], radius=12, outline=green, width=4)
        d.rounded_rectangle([(60, 1165), (410, 1220)], radius=16, fill=(15, 23, 42, 245), outline=green, width=3)
        d.text((80, 1176), "Opción 2: Crear Cuenta", font=font, fill=(255, 255, 255))
        draw_arrow(d, (350, 1215), (375, 1235), color=green, width=6, arrow_size=16)

    elif annotation_type == "nucleo_code":
        cyan = (0, 229, 255, 255)
        # Focus on copy code button at top right [3FIRL5LDLO]
        d.rounded_rectangle([(435, 250), (710, 335)], radius=18, outline=cyan, width=6)
        d.rounded_rectangle([(60, 260), (410, 315)], radius=16, fill=(15, 23, 42, 245), outline=cyan, width=3)
        d.text((80, 271), "Tu Código Familiar", font=font, fill=(255, 255, 255))
        draw_arrow(d, (410, 287), (435, 287), color=cyan, width=6, arrow_size=16)

    elif annotation_type == "estoy_ok_button":
        green = (16, 185, 129, 255)
        # Focus on central green button with badge and arrow pointing directly to it
        button_font = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 28)
        bbox = button_font.getbbox("Tocá 1 vez al día")
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        pill_box = [(190, 752), (530, 804)]
        d.rounded_rectangle(pill_box, radius=14, fill=(15, 23, 42, 245), outline=green, width=3)
        tx = 360 - tw // 2
        ty = 752 + (52 - th) // 2 - bbox[1]
        d.text((tx, ty), "Tocá 1 vez al día", font=button_font, fill=(255, 255, 255))
        draw_arrow(d, (360, 804), (360, 831), color=green, width=5, arrow_size=14)

    elif annotation_type == "sos_button":
        red = (239, 68, 68, 255)
        # SOS button
        d.rounded_rectangle([(550, 100), (690, 180)], radius=16, outline=red, width=5)
        d.rounded_rectangle([(160, 110), (530, 165)], radius=16, fill=(15, 23, 42, 245), outline=red, width=3)
        d.text((180, 121), "Botón de Pánico SOS", font=font, fill=(255, 255, 255))
        draw_arrow(d, (530, 137), (550, 137), color=red, width=6, arrow_size=16)

    elif annotation_type == "battery_monitoring":
        cyan = (0, 229, 255, 255)
        green = (16, 185, 129, 255)
        yellow = (245, 158, 11, 255)

        # 1. Stay indicator on map: 'En Casa desde hace 3 h 49 min'
        d.rounded_rectangle([(312, 390), (617, 441)], radius=18, outline=cyan, width=4)

        emoji_sleep = render_emoji('💤', 28)
        text1 = "Inmóvil = GPS en Reposo (0% Gasto)"
        bbox1 = font_med.getbbox(text1)
        tw1 = bbox1[2] - bbox1[0]
        th1 = bbox1[3] - bbox1[1]

        ew1 = emoji_sleep.width if emoji_sleep else 0
        pill1_w = ew1 + 10 + tw1 + 36
        pill1_h = max(th1, 28) + 22
        p1_x = 175
        p1_y = 300

        d.rounded_rectangle([(p1_x, p1_y), (p1_x + pill1_w, p1_y + pill1_h)], radius=14, fill=(15, 23, 42, 245), outline=cyan, width=3)
        if emoji_sleep:
            overlay.paste(emoji_sleep, (p1_x + 16, p1_y + (pill1_h - emoji_sleep.height) // 2), emoji_sleep)
        d.text((p1_x + 16 + ew1 + 10, p1_y + (pill1_h - th1) // 2 - bbox1[1]), text1, font=font_med, fill=(255, 255, 255))
        draw_arrow(d, (p1_x + pill1_w // 2 + 30, p1_y + pill1_h), (465, 388), color=cyan, width=5, arrow_size=14)

        # 2. Battery indicator in member list
        d.rounded_rectangle([(290, 960), (352, 994)], radius=8, outline=yellow, width=3)

        emoji_bat = render_emoji('🔋', 28)
        text2 = "Batería en Vivo"
        bbox2 = font_bold.getbbox(text2)
        tw2 = bbox2[2] - bbox2[0]
        th2 = bbox2[3] - bbox2[1]

        ew2 = emoji_bat.width if emoji_bat else 0
        pill2_w = ew2 + 10 + tw2 + 30
        pill2_h = max(th2, 28) + 20
        p2_x = 380
        p2_y = 952

        d.rounded_rectangle([(p2_x, p2_y), (p2_x + pill2_w, p2_y + pill2_h)], radius=14, fill=(15, 23, 42, 245), outline=green, width=3)
        if emoji_bat:
            overlay.paste(emoji_bat, (p2_x + 14, p2_y + (pill2_h - emoji_bat.height) // 2), emoji_bat)
        d.text((p2_x + 14 + ew2 + 10, p2_y + (pill2_h - th2) // 2 - bbox2[1]), text2, font=font_bold, fill=(255, 255, 255))
        draw_arrow(d, (p2_x, p2_y + pill2_h // 2), (354, 977), color=green, width=5, arrow_size=12)

    elif annotation_type == "privacy_terms":
        cyan = (0, 229, 255, 255)
        green = (16, 185, 129, 255)

        # 1. Focus on Punto 2: 'Permisos y Batería / Ubicación todo el tiempo'
        d.rounded_rectangle([(115, 515), (605, 655)], radius=14, outline=cyan, width=4)

        emoji_shield = render_emoji('🛡️', 26)
        text1 = "Alertas con Celular Bloqueado"
        bbox1 = font_bold.getbbox(text1)
        tw1 = bbox1[2] - bbox1[0]
        th1 = bbox1[3] - bbox1[1]

        ew1 = emoji_shield.width if emoji_shield else 0
        pill1_w = ew1 + 10 + tw1 + 32
        pill1_h = max(th1, 26) + 20
        p1_x = (720 - pill1_w) // 2
        p1_y = 438

        d.rounded_rectangle([(p1_x, p1_y), (p1_x + pill1_w, p1_y + pill1_h)], radius=14, fill=(15, 23, 42, 250), outline=cyan, width=3)
        if emoji_shield:
            overlay.paste(emoji_shield, (p1_x + 14, p1_y + (pill1_h - emoji_shield.height) // 2), emoji_shield)
        d.text((p1_x + 14 + ew1 + 10, p1_y + (pill1_h - th1) // 2 - bbox1[1]), text1, font=font_bold, fill=(255, 255, 255))
        draw_arrow(d, (360, p1_y + pill1_h), (360, 513), color=cyan, width=5, arrow_size=12)

        # 2. Focus on Punto 6: 'Privacidad y No Monitoreo / NUNCA serán vendidos'
        d.rounded_rectangle([(115, 1045), (605, 1185)], radius=14, outline=green, width=4)

        emoji_lock = render_emoji('🔒', 26)
        text2 = "100% Cifrado • Sin Publicidad"
        bbox2 = font_bold.getbbox(text2)
        tw2 = bbox2[2] - bbox2[0]
        th2 = bbox2[3] - bbox2[1]

        ew2 = emoji_lock.width if emoji_lock else 0
        pill2_w = ew2 + 10 + tw2 + 32
        pill2_h = max(th2, 26) + 20
        p2_x = (720 - pill2_w) // 2
        p2_y = 1350

        d.rounded_rectangle([(p2_x, p2_y), (p2_x + pill2_w, p2_y + pill2_h)], radius=14, fill=(15, 23, 42, 250), outline=green, width=3)
        if emoji_lock:
            overlay.paste(emoji_lock, (p2_x + 14, p2_y + (pill2_h - emoji_lock.height) // 2), emoji_lock)
        d.text((p2_x + 14 + ew2 + 10, p2_y + (pill2_h - th2) // 2 - bbox2[1]), text2, font=font_bold, fill=(255, 255, 255))
        draw_arrow(d, (360, p2_y), (360, 1335), color=green, width=5, arrow_size=12)

    return Image.alpha_composite(img, overlay).convert('RGB')

def build_instagram_story(
    output_path,
    app_image_path,
    accent_color=(16, 185, 129),
    annotation_type=None
):
    """
    Generates a 1080x1920 Instagram Story template for the "FaceCam / Bubble Presenter" format:
    - The phone mockup occupies 85% of screen height and 70% width (720x1600 native capture),
      making every button, map street, and label gigantic and ultra-legible.
    - Zero cropping or cutoffs at the edges.
    - Clean background with ambient glow, providing ample room for placing the circular avatar bubble in Kdenlive.
    """
    W, H = 1080, 1920
    
    # 1. Base gradient: Deep Navy -> Slate Navy
    bg = create_gradient(W, H, (10, 17, 38), (18, 28, 52))
    bg = add_glow(bg, W // 2, H // 2, 750, accent_color, alpha=32)
    
    # 2. Native resolution smartphone frame (720 x 1600 screen)
    phone_w = 720
    phone_h = 1600
    phone_x = (W - phone_w) // 2 # 180 px
    phone_y = (H - phone_h) // 2 # 160 px
    
    border_thick = 14
    screen_radius = 44
    frame_radius = screen_radius + border_thick
    
    frame_w = phone_w + border_thick * 2
    frame_h = phone_h + border_thick * 2
    frame_x = phone_x - border_thick # 166 px
    frame_y = phone_y - border_thick # 146 px
    
    # Drop shadow
    shadow_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [(frame_x - 14, frame_y + 14), (frame_x + frame_w + 14, frame_y + frame_h + 24)],
        radius=frame_radius + 12,
        fill=(0, 0, 0, 200)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(36))
    bg = Image.alpha_composite(bg.convert('RGBA'), shadow_layer).convert('RGB')
    
    # 3. Apply annotations on the screenshot
    app_raw = Image.open(app_image_path)
    app_img = apply_annotations(app_raw, annotation_type)
    
    # Phone Frame
    frame_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    f_draw = ImageDraw.Draw(frame_layer)
    
    # Titanium Bezel with accent highlight
    f_draw.rounded_rectangle(
        [(frame_x, frame_y), (frame_x + frame_w, frame_y + frame_h)],
        radius=frame_radius,
        fill=(26, 36, 52, 255),
        outline=(accent_color[0], accent_color[1], accent_color[2], 160),
        width=3
    )
    
    # Screen
    app_rounded = round_corners(app_img.convert('RGBA'), screen_radius)
    frame_layer.paste(app_rounded, (phone_x, phone_y), app_rounded)
    
    # Front camera punch hole
    f_draw.ellipse([(W // 2 - 9, phone_y + 16), (W // 2 + 9, phone_y + 34)], fill=(10, 10, 10, 255))
    
    # Screen inner bevel
    f_draw.rounded_rectangle(
        [(phone_x, phone_y), (phone_x + phone_w, phone_y + phone_h)],
        radius=screen_radius,
        outline=(255, 255, 255, 30),
        width=2
    )
    
    bg = Image.alpha_composite(bg.convert('RGBA'), frame_layer).convert('RGB')
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    bg.save(output_path, 'PNG', quality=95)
    print(f"Generated: {output_path}")

def main():
    dest_dir = "docs/instagram_stories_assets"
    
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)
    os.makedirs(dest_dir, exist_ok=True)
    
    stories = [
        # CARPETA 1: 🚀 EMPEZÁ ACÁ
        {
            "num": "01",
            "name": "destacada1_h1_login",
            "image": "docs/imagen06.jpg",
            "accent": (0, 229, 255),
            "annotation": "login_google_register"
        },
        {
            "num": "02",
            "name": "destacada1_h2_nucleo",
            "image": "docs/captura_crear_nucleo.jpg",
            "accent": (0, 229, 255),
            "annotation": "nucleo_code"
        },
        {
            "num": "03",
            "name": "destacada1_h3_mapa",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 2: 🟢 EL BOTÓN "ESTOY OK"
        {
            "num": "04",
            "name": "destacada2_h1_estoy_ok",
            "image": "docs/imagen02.jpg",
            "accent": (16, 185, 129),
            "annotation": "estoy_ok_button"
        },
        {
            "num": "05",
            "name": "destacada2_h2_alerta_whatsapp",
            "image": "docs/captura_alerta_inactividad_whatsapp.jpg",
            "accent": (245, 158, 11),
            "annotation": None
        },
        {
            "num": "05b",
            "name": "destacada2_h2_wifi_ajustes",
            "image": "docs/captura_ajustes_wifi.jpg",
            "accent": (0, 229, 255),
            "annotation": None
        },
        
        # CARPETA 3: 📍 ZONAS SEGURAS
        {
            "num": "06",
            "name": "destacada3_h1_zonas_crear",
            "image": "docs/zoom.jpg",
            "accent": (0, 229, 255),
            "annotation": None
        },
        {
            "num": "07",
            "name": "destacada3_h2_zonas_notif",
            "image": "docs/captura_notif_zona.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 4: 🚨 CONTACTOS SOS & WHATSAPP
        {
            "num": "08",
            "name": "destacada4_h1_contactos_sos",
            "image": "docs/captura_modal_contactos.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        {
            "num": "09",
            "name": "destacada4_h2_rescate_web",
            "image": "docs/captura_rescate_web.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        
        # CARPETA 5: 🚗 EN EL AUTO (SEGURIDAD VIAL)
        {
            "num": "10",
            "name": "destacada5_h1_conduccion",
            "image": "docs/imagen03.jpg",
            "accent": (245, 158, 11),
            "annotation": None
        },
        {
            "num": "11",
            "name": "destacada5_h2_choques",
            "image": "docs/crash_alert_screen.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        
        # CARPETA 6: 🔋 BATERÍA & PRIVACIDAD
        {
            "num": "12",
            "name": "destacada6_h1_bateria",
            "image": "docs/captura_bateria_app.jpg",
            "accent": (16, 185, 129),
            "annotation": "battery_monitoring"
        },
        {
            "num": "13",
            "name": "destacada6_h2_privacidad",
            "image": "docs/condiciones_servicio.jpg",
            "accent": (168, 85, 247),
            "annotation": "privacy_terms"
        }
    ]
    
    print(f"Generando las {len(stories)} plantillas definitivas en formato FaceCam / Burbuja...")
    for s in stories:
        long_filename = f"{dest_dir}/imagen{s['num']}_{s['name']}.png"
        short_filename = f"{dest_dir}/imagen{s['num']}.png"
        legacy_filename = f"{dest_dir}/{s['name']}.png"
        
        build_instagram_story(
            output_path=long_filename,
            app_image_path=s["image"],
            accent_color=s["accent"],
            annotation_type=s.get("annotation")
        )
        shutil.copy2(long_filename, short_filename)
        shutil.copy2(long_filename, legacy_filename)
    print("\n¡Catálogo definitivo generado con éxito en docs/instagram_stories_assets/!")

if __name__ == "__main__":
    main()
