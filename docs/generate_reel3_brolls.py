import os
from PIL import Image, ImageDraw

def generate_reel3_brolls():
    os.makedirs('docs', exist_ok=True)
    
    reels_w, reels_h = 1080, 1920
    
    assets = [
        {
            "src": "docs/captura_modal_contactos.jpg",
            "name": "broll_reel3_01_contactos_sos",
            "desc": "Modal de Contactos SOS y activación de emergencia",
            "bg_color": (11, 20, 26)
        },
        {
            "src": "docs/captura_rescate_web.jpg",
            "name": "broll_reel3_02_web_rescate_audio",
            "desc": "Web pública de rescate con audio ambiente de 15s y botón Voy en camino",
            "bg_color": (11, 20, 26)
        },
        {
            "src": "docs/crash_alert_screen.jpg",
            "name": "broll_reel3_03_choque_fuerzag",
            "desc": "Pantalla roja de detección de choque vehicular por Fuerza G con cuenta regresiva",
            "bg_color": (15, 23, 42)
        },
        {
            "src": "docs/crash_alert_message_whatsapp.jpg",
            "name": "broll_reel3_04_whatsapp_choque",
            "desc": "Mensaje de WhatsApp de impacto recibido por los contactos con link de rescate",
            "bg_color": (11, 20, 26)
        }
    ]
    
    out_dir = 'docs/reels_assets/reel3_sos_choques'
    os.makedirs(out_dir, exist_ok=True)

    for item in assets:
        src_path = item["src"]
        base_name = item["name"]
        bg_color = item["bg_color"]
        
        if not os.path.exists(src_path):
            print(f"Error: {src_path} not found.")
            continue
            
        img = Image.open(src_path).convert('RGB')
        src_w, src_h = img.size
        
        # 1. Full-Bleed Version (Edge-to-Edge 1080x1920)
        full_bleed = img.resize((reels_w, reels_h), Image.Resampling.LANCZOS)
        full_bleed_path = f"{out_dir}/{base_name}_fullbleed_9x16.jpg"
        full_bleed.save(full_bleed_path, quality=95)
        print(f"Generated: {full_bleed_path}")
        
        # 2. Framed Version (Scaled maintaining smartphone ratio with sleek frame)
        canvas = Image.new('RGB', (reels_w, reels_h), bg_color)
        
        scaled_w = int(src_w * (reels_h / src_h)) # 864 px
        scaled_h = reels_h                         # 1920 px
        scaled_img = img.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)
        
        # Mask with modern smartphone rounded corners (32px radius)
        mask = Image.new('L', (scaled_w, scaled_h), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.rounded_rectangle([0, 0, scaled_w, scaled_h], radius=32, fill=255)
        
        offset_x = (reels_w - scaled_w) // 2
        canvas.paste(scaled_img, (offset_x, 0), mask)
        
        framed_path = f"{out_dir}/{base_name}_9x16.png"
        canvas.save(framed_path, quality=95)
        print(f"Generated: {framed_path}")

if __name__ == '__main__':
    generate_reel3_brolls()
