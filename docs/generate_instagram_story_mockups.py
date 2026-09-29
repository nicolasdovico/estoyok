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

def apply_annotations(app_img, annotation_type):
    """Draws sleek focus callout boxes and directional arrows on the screenshot."""
    if not annotation_type:
        return app_img
    
    img = app_img.convert('RGBA')
    overlay = Image.new('RGBA', img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    font = ImageFont.truetype('android-native/app/src/main/res/font/outfit_bold.ttf', 30)

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
        # Focus on central green button
        d.ellipse([(205, 680), (515, 990)], outline=green, width=6)
        d.rounded_rectangle([(160, 600), (560, 655)], radius=16, fill=(15, 23, 42, 245), outline=green, width=3)
        d.text((180, 611), "Tocá acá 1 vez al día", font=font, fill=(255, 255, 255))
        draw_arrow(d, (360, 655), (360, 680), color=green, width=6, arrow_size=16)

    elif annotation_type == "sos_button":
        red = (239, 68, 68, 255)
        # SOS button
        d.rounded_rectangle([(550, 100), (690, 180)], radius=16, outline=red, width=5)
        d.rounded_rectangle([(160, 110), (530, 165)], radius=16, fill=(15, 23, 42, 245), outline=red, width=3)
        d.text((180, 121), "Botón de Pánico SOS", font=font, fill=(255, 255, 255))
        draw_arrow(d, (530, 137), (550, 137), color=red, width=6, arrow_size=16)

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
            "filename": f"{dest_dir}/destacada1_h1_login.png",
            "image": "docs/imagen06.jpg",
            "accent": (0, 229, 255),
            "annotation": "login_google_register"
        },
        {
            "filename": f"{dest_dir}/destacada1_h2_nucleo.png",
            "image": "docs/captura_crear_nucleo.jpg",
            "accent": (0, 229, 255),
            "annotation": "nucleo_code"
        },
        {
            "filename": f"{dest_dir}/destacada1_h3_mapa.png",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 2: 🟢 EL BOTÓN "ESTOY OK"
        {
            "filename": f"{dest_dir}/destacada2_h1_estoy_ok.png",
            "image": "docs/imagen02.jpg",
            "accent": (16, 185, 129),
            "annotation": "estoy_ok_button"
        },
        {
            "filename": f"{dest_dir}/destacada2_h2_alerta_whatsapp.png",
            "image": "docs/crash_alert_message_whatsapp.jpg",
            "accent": (37, 211, 102),
            "annotation": None
        },
        
        # CARPETA 3: 📍 ZONAS SEGURAS
        {
            "filename": f"{dest_dir}/destacada3_h1_zonas_crear.png",
            "image": "docs/zoom.jpg",
            "accent": (0, 229, 255),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada3_h2_zonas_notif.png",
            "image": "docs/captura_notif_zona.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        
        # CARPETA 4: 🚨 CONTACTOS SOS & WHATSAPP
        {
            "filename": f"{dest_dir}/destacada4_h1_contactos_sos.png",
            "image": "docs/captura_modal_contactos.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada4_h2_rescate_web.png",
            "image": "docs/captura_rescate_web.jpg",
            "accent": (239, 68, 68),
            "annotation": "sos_button"
        },
        
        # CARPETA 5: 🚗 EN EL AUTO (SEGURIDAD VIAL)
        {
            "filename": f"{dest_dir}/destacada5_h1_conduccion.png",
            "image": "docs/imagen03.jpg",
            "accent": (245, 158, 11),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada5_h2_choques.png",
            "image": "docs/crash_alert_screen.jpg",
            "accent": (239, 68, 68),
            "annotation": None
        },
        
        # CARPETA 6: 🔋 BATERÍA & PRIVACIDAD
        {
            "filename": f"{dest_dir}/destacada6_h1_bateria.png",
            "image": "docs/imagen01.jpg",
            "accent": (16, 185, 129),
            "annotation": None
        },
        {
            "filename": f"{dest_dir}/destacada6_h2_privacidad.png",
            "image": "docs/imagen06.jpg",
            "accent": (168, 85, 247),
            "annotation": None
        }
    ]
    
    print(f"Generando las {len(stories)} plantillas definitivas en formato FaceCam / Burbuja...")
    for s in stories:
        build_instagram_story(
            output_path=s["filename"],
            app_image_path=s["image"],
            accent_color=s["accent"],
            annotation_type=s.get("annotation")
        )
    print("\n¡Catálogo definitivo generado con éxito en docs/instagram_stories_assets/!")

if __name__ == "__main__":
    main()
