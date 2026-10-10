import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_map_telemetry_broll():
    width = 720
    height = 1600
    
    # 1. Base image from imagen01.jpg (spacious open map + collapsed bottom card)
    base_img = Image.open('docs/imagen01.jpg').convert('RGB')
    draw = ImageDraw.Draw(base_img)
    
    # Fonts
    font_path_regular = '/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf'
    font_path_bold = '/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf'
    font_path_emoji = '/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf'
    
    font_clock = ImageFont.truetype(font_path_bold, 24)
    font_name = ImageFont.truetype(font_path_bold, 22)
    font_telemetry = ImageFont.truetype(font_path_bold, 17)
    font_label = ImageFont.truetype(font_path_bold, 15)
    font_sub = ImageFont.truetype(font_path_regular, 13)
    
    # Colors
    primary_teal = (0, 229, 255)       # Electric Cyan / PrimaryTeal
    primary_emerald = (0, 230, 153)    # Emerald green
    battery_yellow = (255, 214, 0)     # Battery yellow
    card_bg = (34, 34, 36)             # Collapsed card bg (#222224)
    dark_surface = (22, 34, 42)
    text_primary = (255, 255, 255)
    text_muted = (160, 174, 192)
    
    # Helper to render emoji via NotoColorEmoji
    def get_emoji(char, size):
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

    # Pre-render emojis
    car_emoji_18 = get_emoji('🚗', 18)
    zap_emoji_16 = get_emoji('⚡', 16)
    car_emoji_20 = get_emoji('🚗', 20)
    zap_emoji_18 = get_emoji('⚡', 18)
    
    # 2. Update Status Bar Clock to 18:43 (consistent with Reel 1 timeline)
    draw.rectangle([15, 10, 115, 52], fill=(15, 15, 15))
    draw.text((28, 17), "18:43", font=font_clock, fill=(255, 255, 255))
    
    # 3. Create High-Definition Custom Pin with 2x Supersampling for anti-aliasing
    scale = 2
    pin_w, pin_h = 76 * scale, 90 * scale
    pin_img = Image.new('RGBA', (pin_w, pin_h), (0, 0, 0, 0))
    d_pin = ImageDraw.Draw(pin_img)
    
    teal_rgba = (0, 229, 255, 255)
    center_x = pin_w // 2
    
    # Pointer triangle at bottom
    tip_y = pin_h - (4 * scale)
    base_tri_y = pin_h - (24 * scale)
    d_pin.polygon([
        (center_x - (11 * scale), base_tri_y),
        (center_x, tip_y),
        (center_x + (11 * scale), base_tri_y)
    ], fill=teal_rgba)
    
    # Outer rounded box
    outer_box_size = 68 * scale
    box_x1 = (pin_w - outer_box_size) // 2
    box_y1 = 2 * scale
    box_x2 = box_x1 + outer_box_size
    box_y2 = box_y1 + outer_box_size
    
    d_pin.rounded_rectangle([box_x1, box_y1, box_x2, box_y2], radius=22 * scale, fill=teal_rgba)
    
    # Inner dark frame
    inner_pad = 4 * scale
    d_pin.rounded_rectangle([box_x1 + inner_pad, box_y1 + inner_pad, box_x2 - inner_pad, box_y2 - inner_pad],
                            radius=18 * scale, fill=(18, 28, 36, 255))
    
    # Paste Lucas's avatar inside
    av_size = outer_box_size - (8 * scale)
    av_src = Image.open('docs/reels_assets/reel1_padres_auto/avatar_hijo.jpg').convert('RGBA').resize((av_size, av_size), Image.Resampling.LANCZOS)
    
    mask_av = Image.new('L', (av_size, av_size), 0)
    ImageDraw.Draw(mask_av).rounded_rectangle([0, 0, av_size, av_size], radius=15 * scale, fill=255)
    
    av_x = box_x1 + (4 * scale)
    av_y = box_y1 + (4 * scale)
    pin_img.paste(av_src, (av_x, av_y), mask_av)
    
    # Movement Badge at bottom right: circular badge with car emoji 🚗
    badge_r = 15 * scale
    bx = box_x2 - (10 * scale)
    by = box_y2 - (10 * scale)
    
    d_pin.ellipse([bx - badge_r, by - badge_r, bx + badge_r, by + badge_r],
                  fill=(0, 200, 160, 255), outline=(255, 255, 255, 255), width=2 * scale)
    
    # Render car emoji on badge
    car_ico = get_emoji('🚗', 19 * scale)
    if car_ico:
        pin_img.paste(car_ico, (bx - int(9.5 * scale), by - int(9.5 * scale)), car_ico)
    
    # Downsample pin to target 1x size (crisp smooth edges)
    final_pin = pin_img.resize((pin_w // scale, pin_h // scale), Image.Resampling.LANCZOS)
    
    # 4. Position Pin on the Road (Colectora Nte. around x=420, y=780)
    pin_target_x = 420
    pin_target_y = 780
    
    # Draw soft GPS accuracy radar halo on the street
    halo = Image.new('RGBA', base_img.size, (0, 0, 0, 0))
    d_halo = ImageDraw.Draw(halo)
    halo_r = 65
    d_halo.ellipse([pin_target_x - halo_r, pin_target_y - halo_r, pin_target_x + halo_r, pin_target_y + halo_r],
                   fill=(0, 229, 255, 32), outline=(0, 229, 255, 140), width=2)
    
    # Directional arrow showing trajectory along avenue (down-left along Colectora)
    d_halo.line([(pin_target_x, pin_target_y), (pin_target_x - 45, pin_target_y + 32)], fill=(0, 229, 255, 220), width=4)
    d_halo.polygon([(pin_target_x - 49, pin_target_y + 35), (pin_target_x - 35, pin_target_y + 34), (pin_target_x - 43, pin_target_y + 22)], fill=(0, 229, 255, 220))
    
    base_img = Image.alpha_composite(base_img.convert('RGBA'), halo).convert('RGB')
    draw = ImageDraw.Draw(base_img)
    
    # Paste pin (anchor is bottom tip: x = pin_target_x, y = pin_target_y)
    pin_paste_x = pin_target_x - (final_pin.width // 2)
    pin_paste_y = pin_target_y - final_pin.height + 4
    base_img.paste(final_pin, (pin_paste_x, pin_paste_y), final_pin)
    
    # 5. Floating Callout Tag Attached to Pin on Map:
    # [ 🚗 58 km/h • ⚡ 42% ]
    tag_w = 216
    tag_h = 36
    tag_x = pin_paste_x + final_pin.width - 10
    tag_y = pin_paste_y + 4
    
    draw.rounded_rectangle([tag_x, tag_y, tag_x + tag_w, tag_y + tag_h],
                           radius=10, fill=dark_surface, outline=primary_teal, width=2)
    
    # Car emoji + speed
    if car_emoji_18:
        base_img.paste(car_emoji_18, (tag_x + 12, tag_y + 9), car_emoji_18)
    draw.text((tag_x + 36, tag_y + 7), "58 km/h", font=font_label, fill=primary_teal)
    draw.text((tag_x + 114, tag_y + 7), "•", font=font_label, fill=text_muted)
    
    # Zap emoji + battery
    if zap_emoji_16:
        base_img.paste(zap_emoji_16, (tag_x + 128, tag_y + 10), zap_emoji_16)
    draw.text((tag_x + 148, tag_y + 7), "42%", font=font_label, fill=battery_yellow)
    
    # 6. Update Bottom Sheet Card (y = 1192 to 1340) to Highlight Lucas with Telemetry
    # Clean member area in collapsed bottom card without clipping header
    draw.rectangle([70, 1192, 580, 1330], fill=card_bg)
    
    # Avatar in Bottom Card
    card_av_size = 64
    card_av = Image.open('docs/avatar_hijo.jpg').convert('RGBA').resize((card_av_size, card_av_size), Image.Resampling.LANCZOS)
    card_mask = Image.new('L', (card_av_size, card_av_size), 0)
    ImageDraw.Draw(card_mask).ellipse([0, 0, card_av_size, card_av_size], fill=255)
    
    # Avatar border
    draw.ellipse([80, 1196, 80 + card_av_size + 4, 1196 + card_av_size + 4], fill=primary_teal)
    base_img.paste(card_av, (82, 1198), card_mask)
    
    # Member Name: Lucas
    name_x = 164
    name_y = 1200
    draw.text((name_x, name_y), "Lucas", font=font_name, fill=text_primary)
    
    # Telemetry Row: 🚗 58 km/h • ⚡ 42%
    telemetry_y = 1236
    if car_emoji_20:
        base_img.paste(car_emoji_20, (name_x, telemetry_y + 2), car_emoji_20)
    draw.text((name_x + 26, telemetry_y), "58 km/h", font=font_telemetry, fill=primary_teal)
    draw.text((name_x + 112, telemetry_y), "•", font=font_telemetry, fill=text_muted)
    
    if zap_emoji_18:
        base_img.paste(zap_emoji_18, (name_x + 128, telemetry_y + 3), zap_emoji_18)
    draw.text((name_x + 150, telemetry_y), "42%", font=font_telemetry, fill=battery_yellow)
    
    # Subtitle: En movimiento por Colectora Nte. • Hace 5 s
    draw.text((name_x, 1268), "En movimiento por Colectora Nte. • Hace 5 s", font=font_sub, fill=text_muted)
    
    # Save Native 720x1600 image
    out_dir = 'docs/reels_assets/reel1_padres_auto'
    os.makedirs(out_dir, exist_ok=True)
    native_out = f'{out_dir}/captura_mapa_bateria_velocidad.jpg'
    base_img.save(native_out, quality=95)
    print(f"Saved native screenshot: {native_out}")
    
    # 7. Generate 1080x1920 Full HD Vertical (Reels format 9:16)
    reels_w = 1080
    reels_h = 1920
    reels_canvas = Image.new('RGB', (reels_w, reels_h), (11, 20, 26))
    
    scaled_w = int(width * (reels_h / height)) # 864
    scaled_h = reels_h                         # 1920
    scaled_img = base_img.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)
    
    mask_frame = Image.new('L', (scaled_w, scaled_h), 0)
    d_mask = ImageDraw.Draw(mask_frame)
    d_mask.rounded_rectangle([0, 0, scaled_w, scaled_h], radius=32, fill=255)
    
    offset_x = (reels_w - scaled_w) // 2
    reels_canvas.paste(scaled_img, (offset_x, 0), mask_frame)
    
    reels_out = f'{out_dir}/broll_mapa_telemetria_9x16.png'
    reels_canvas.save(reels_out, quality=95)
    print(f"Saved 9:16 Reels asset: {reels_out}")
    
    # Full bleed 1080x1920
    full_bleed = base_img.resize((reels_w, reels_h), Image.Resampling.LANCZOS)
    full_bleed_out = f'{out_dir}/broll_mapa_telemetria_fullbleed_9x16.jpg'
    full_bleed.save(full_bleed_out, quality=95)
    print(f"Saved full-bleed 9:16 asset: {full_bleed_out}")

if __name__ == '__main__':
    create_map_telemetry_broll()
