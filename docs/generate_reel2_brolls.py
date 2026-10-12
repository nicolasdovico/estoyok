import os
from PIL import Image, ImageDraw

def generate_reel2_brolls():
    os.makedirs('docs', exist_ok=True)
    
    reels_w, reels_h = 1080, 1920
    
    assets = [
        {
            "src": "docs/imagen04.jpg",
            "name": "broll_reel2_01_estoy_ok",
            "desc": "Solapa Estoy OK con botón central y contador",
            "bg_color": (11, 20, 26)
        },
        {
            "src": "docs/captura_ajustes_wifi.jpg",
            "name": "broll_reel2_02_wifi_ajustes",
            "desc": "Pantalla de Ajustes con Auto-Checkin por Wi-Fi de Casa",
            "bg_color": (11, 20, 26)
        },
        {
            "src": "docs/captura_alerta_inactividad_whatsapp.jpg",
            "name": "broll_reel2_03_alerta_whatsapp",
            "desc": "Alerta de inactividad de WhatsApp con mapa de rescate",
            "bg_color": (11, 20, 26)
        }
    ]
    
    out_dir = 'docs/reels_assets/reel2_vivir_solo'
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
    generate_reel2_brolls()
