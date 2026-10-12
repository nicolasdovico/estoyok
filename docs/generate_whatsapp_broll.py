import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_whatsapp_chat():
    width = 720
    height = 1600
    
    # 1. Base image with WhatsApp Dark theme background (#0b141a)
    chat_bg_color = (11, 20, 26)
    img = Image.new('RGB', (width, height), chat_bg_color)
    draw = ImageDraw.Draw(img)
    
    # Fonts
    font_path_regular = '/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf'
    font_path_bold = '/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf'
    font_path_emoji = '/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf'
    
    font_msg = ImageFont.truetype(font_path_regular, 21)
    font_time = ImageFont.truetype(font_path_regular, 13)
    font_date = ImageFont.truetype(font_path_bold, 13)
    font_name = ImageFont.truetype(font_path_bold, 24)
    font_status = ImageFont.truetype(font_path_regular, 15)
    font_clock = ImageFont.truetype(font_path_bold, 24)
    font_call = ImageFont.truetype(font_path_regular, 15)
    
    # Helper: render emoji using NotoColorEmoji
    def get_emoji_img(char, size=24):
        try:
            fe = ImageFont.truetype(font_path_emoji, 109)
            tmp = Image.new('RGBA', (220, 220), (0, 0, 0, 0))
            td = ImageDraw.Draw(tmp)
            td.text((20, 20), char, font=fe, embedded_color=True)
            bb = tmp.getbbox()
            if bb:
                tmp = tmp.crop(bb)
            return tmp.resize((size, size), Image.Resampling.LANCZOS)
        except Exception:
            return None

    # Helper: draw double blue ticks
    def draw_blue_ticks(d, x, y):
        blue = (83, 189, 235)
        # Check 1
        d.line([(x, y + 6), (x + 4, y + 11), (x + 12, y)], fill=blue, width=2)
        # Check 2
        d.line([(x + 6, y + 6), (x + 10, y + 11), (x + 18, y)], fill=blue, width=2)

    # 2. Add Status Bar (top 0 to 65)
    orig_img = Image.open('docs/captura_alerta_inactividad_whatsapp.jpg')
    status_bar = orig_img.crop((0, 0, width, 65))
    img.paste(status_bar, (0, 0))
    
    # Repaint clock to 18:42
    draw.rectangle([15, 10, 115, 52], fill=(15, 15, 15))
    draw.text((28, 17), "18:42", font=font_clock, fill=(255, 255, 255))
    
    # 3. Add Header Bar (65 to 185)
    header_bg = (31, 44, 52)
    draw.rectangle([0, 65, width, 185], fill=header_bg)
    
    # Clean area behind back arrow and avatar
    back_arrow = orig_img.crop((20, 80, 56, 165))
    img.paste(back_arrow, (20, 80))
    
    # Contact Avatar (circular photo of teenage son)
    avatar_size = 58
    avatar_x = 75
    avatar_y = 94
    
    avatar_src = Image.open('docs/reels_assets/reel1_padres_auto/avatar_hijo.jpg').convert('RGBA')
    avatar_src = avatar_src.resize((avatar_size, avatar_size), Image.Resampling.LANCZOS)
    
    # Mask circle
    mask = Image.new('L', (avatar_size, avatar_size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.ellipse([0, 0, avatar_size - 1, avatar_size - 1], fill=255)
    
    img.paste(avatar_src, (avatar_x, avatar_y), mask)
    
    # Contact Name: "Lucas" + heart emoji
    name_x = 148
    draw.text((name_x, 97), "Lucas", font=font_name, fill=(233, 237, 239))
    name_bbox = font_name.getbbox("Lucas")
    heart_x = name_x + (name_bbox[2] - name_bbox[0]) + 8
    heart_img = get_emoji_img("❤️", size=22)
    if heart_img:
        img.paste(heart_img, (heart_x, 102), heart_img)
        
    draw.text((name_x, 134), "últ. vez hoy a las 15:10", font=font_status, fill=(134, 150, 160))
    
    # Header Action Icons (Video, Call, 3 Dots)
    right_icons = orig_img.crop((600, 85, 715, 165))
    img.paste(right_icons, (600, 85))
    
    # Add video call icon at x=540, y=110
    cam_color = (160, 175, 185)
    # camera body
    draw.rounded_rectangle([538, 114, 568, 138], radius=5, fill=cam_color)
    # camera lens triangle
    draw.polygon([(568, 121), (581, 113), (581, 139), (568, 131)], fill=cam_color)
    
    # 4. Add Bottom Input Bar (from 1450 to 1600)
    bottom_bar = orig_img.crop((0, 1445, width, height))
    img.paste(bottom_bar, (0, 1445))
    
    # 5. Date Pill: "HOY"
    pill_w = 90
    pill_h = 32
    pill_x = (width - pill_w) // 2
    pill_y = 205
    draw.rounded_rectangle([pill_x, pill_y, pill_x + pill_w, pill_y + pill_h], radius=10, fill=(24, 34, 41))
    draw.text((pill_x + 29, pill_y + 6), "HOY", font=font_date, fill=(134, 150, 160))
    
    # 6. Render Messages
    messages = [
        {
            "lines": ["¿Saliste del cole, Lu?", "Avisame cuando llegues a casa."],
            "time": "14:35",
            "type": "msg"
        },
        {
            "lines": ["¿Llegaste bien? Avisame."],
            "time": "15:15",
            "type": "msg"
        },
        {
            "lines": ["Lucas, ¿estás en casa?", "Me quedo intranquilo."],
            "time": "16:02",
            "type": "msg"
        },
        {
            "lines": ["¿Hola? ¿Estás bien?", "Clavaste el visto hace hora y media..."],
            "time": "16:48",
            "type": "msg"
        },
        {
            "lines": ["¿Te quedaste sin batería?", "Por favor respondeme aunque sea un pulgar"],
            "emoji": "👍",
            "time": "17:35",
            "type": "msg"
        },
        {
            "lines": ["Lucas atendé el teléfono por favor"],
            "emoji": "🙏",
            "time": "18:10",
            "type": "msg"
        },
        {
            "type": "call",
            "text": "Llamada de voz sin responder",
            "time": "18:25"
        },
        {
            "lines": ["Llamame ya.", "Me tenés con el corazón en la boca."],
            "time": "18:38",
            "type": "msg"
        }
    ]
    
    current_y = 258
    bubble_bg = (0, 92, 75)       # WhatsApp Dark Outgoing green
    text_color = (233, 237, 239)   # Near-white
    time_color = (141, 161, 143)   # WhatsApp muted green
    
    for item in messages:
        if item["type"] == "call":
            # Missed call pill (system style)
            call_w = 400
            call_h = 44
            call_x = (width - call_w) // 2
            
            draw.rounded_rectangle([call_x, current_y, call_x + call_w, current_y + call_h], radius=12, fill=(24, 34, 41))
            
            # Draw red missed call icon (arrow angled pointing down-left and handset)
            red_icon = (241, 92, 105)
            # Arrow
            draw.line([(call_x + 22, current_y + 16), (call_x + 36, current_y + 28)], fill=red_icon, width=2)
            draw.line([(call_x + 22, current_y + 16), (call_x + 22, current_y + 24)], fill=red_icon, width=2)
            draw.line([(call_x + 22, current_y + 16), (call_x + 30, current_y + 16)], fill=red_icon, width=2)
            
            # Small handset icon next to arrow
            phone_em = get_emoji_img("📞", size=18)
            if phone_em:
                img.paste(phone_em, (call_x + 42, current_y + 13), phone_em)
            
            draw.text((call_x + 68, current_y + 12), f"{item['text']} • {item['time']}", font=font_call, fill=red_icon)
            
            current_y += call_h + 16
            continue
            
        lines = item["lines"]
        # Calculate width
        max_line_w = 0
        for l in lines:
            bbox = font_msg.getbbox(l)
            w = bbox[2] - bbox[0]
            if w > max_line_w:
                max_line_w = w
        
        # Add emoji width if present
        if "emoji" in item:
            max_line_w += 32
            
        # Extra space for timestamp and blue ticks
        time_ticks_w = 78
        bubble_w = max(max_line_w + 36, 220)
        
        # If last line is wide, expand bubble or add extra line for timestamp
        last_line_w = font_msg.getbbox(lines[-1])[2] - font_msg.getbbox(lines[-1])[0]
        if "emoji" in item:
            last_line_w += 32
            
        needs_extra_time_line = (last_line_w + time_ticks_w + 36 > bubble_w)
        if not needs_extra_time_line:
            bubble_w = max(bubble_w, last_line_w + time_ticks_w + 36)
            
        bubble_w = min(bubble_w, 545)
        
        line_height = 29
        content_h = len(lines) * line_height + (20 if needs_extra_time_line else 6)
        bubble_h = content_h + 18
        
        # Right aligned
        bubble_x2 = width - 24
        bubble_x1 = bubble_x2 - bubble_w
        
        # Draw bubble
        draw.rounded_rectangle([bubble_x1, current_y, bubble_x2, current_y + bubble_h], radius=13, fill=bubble_bg)
        
        # Outgoing tail at top right
        tail = [
            (bubble_x2 - 3, current_y),
            (bubble_x2 + 7, current_y),
            (bubble_x2 - 3, current_y + 12)
        ]
        draw.polygon(tail, fill=bubble_bg)
        
        # Draw text lines
        text_y = current_y + 9
        for idx, l in enumerate(lines):
            draw.text((bubble_x1 + 16, text_y), l, font=font_msg, fill=text_color)
            if idx == len(lines) - 1 and "emoji" in item:
                em = get_emoji_img(item["emoji"], size=22)
                if em:
                    text_bbox = font_msg.getbbox(l)
                    em_x = bubble_x1 + 16 + (text_bbox[2] - text_bbox[0]) + 6
                    img.paste(em, (em_x, text_y + 2), em)
            text_y += line_height
            
        # Draw timestamp & ticks
        time_x = bubble_x2 - 74
        time_y = current_y + bubble_h - 24
        draw.text((time_x, time_y), item["time"], font=font_time, fill=time_color)
        draw_blue_ticks(draw, time_x + 40, time_y + 3)
        
        current_y += bubble_h + 14

    out_dir = 'docs/reels_assets/reel1_padres_auto'
    os.makedirs(out_dir, exist_ok=True)
    native_out = f'{out_dir}/captura_chat_whatsapp_visto.jpg'
    img.save(native_out, quality=95)
    print(f"Saved native screenshot: {native_out}")
    
    # 7. Also generate 1080x1920 Full HD Vertical (Exact 9:16 for Reels timeline)
    reels_w = 1080
    reels_h = 1920
    reels_canvas = Image.new('RGB', (reels_w, reels_h), (11, 20, 26))
    
    # Center scaled version on 1080x1920
    scaled_w = int(width * (reels_h / height)) # 864
    scaled_h = reels_h                         # 1920
    scaled_img = img.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)
    
    # Subtle rounded border with sleek phone frame
    mask_frame = Image.new('L', (scaled_w, scaled_h), 0)
    d_mask = ImageDraw.Draw(mask_frame)
    d_mask.rounded_rectangle([0, 0, scaled_w, scaled_h], radius=32, fill=255)
    
    offset_x = (reels_w - scaled_w) // 2
    reels_canvas.paste(scaled_img, (offset_x, 0), mask_frame)
    
    reels_out = f'{out_dir}/broll_chat_whatsapp_visto_9x16.png'
    reels_canvas.save(reels_out, quality=95)
    print(f"Saved 9:16 Reels asset: {reels_out}")
    
    # Also save full-bleed 1080x1920 version (scaled edge-to-edge covering 100% of screen)
    full_bleed = img.resize((reels_w, reels_h), Image.Resampling.LANCZOS)
    full_bleed_out = f'{out_dir}/broll_chat_whatsapp_visto_fullbleed_9x16.jpg'
    full_bleed.save(full_bleed_out, quality=95)
    print(f"Saved full-bleed 9:16 asset: {full_bleed_out}")

if __name__ == '__main__':
    create_whatsapp_chat()
