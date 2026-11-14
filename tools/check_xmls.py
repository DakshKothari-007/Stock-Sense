import glob, re, os
import xml.etree.ElementTree as ET

xml_files = glob.glob('app/src/main/res/drawable/ic_prod_*.xml')

stats = {
    'total': len(xml_files),
    'with_aapt': 0,
    'with_clip_path': 0,
    'with_group': 0,
    'with_mask': 0,
    'no_fill_color_on_all_paths': 0,
    'paths_outside_viewport': 0,
}

sample_outside = []
no_fill_files = []

for f in xml_files:
    with open(f, 'r', encoding='utf-8') as fp:
        raw = fp.read()
    
    has_aapt = 'aapt:attr' in raw
    has_clip = '<clip-path' in raw
    has_group = '<group' in raw
    
    if has_aapt:
        stats['with_aapt'] += 1
    if has_clip:
        stats['with_clip_path'] += 1
    if has_group:
        stats['with_group'] += 1

    # Check if any path has fillColor or aapt
    try:
        root = ET.fromstring(raw)
        vw = float(root.attrib.get('{http://schemas.android.com/apk/res/android}viewportWidth', '128'))
        vh = float(root.attrib.get('{http://schemas.android.com/apk/res/android}viewportHeight', '128'))
        
        # Check paths
        all_paths = root.findall('.//{http://schemas.android.com/apk/res/android}path') or root.findall('.//path')
        visible_paths = 0
        for p in root.iter():
            if p.tag.endswith('path'):
                fc = p.attrib.get('{http://schemas.android.com/apk/res/android}fillColor')
                sc = p.attrib.get('{http://schemas.android.com/apk/res/android}strokeColor')
                has_aapt_child = any(c.tag.endswith('attr') for c in p)
                if fc or sc or has_aapt_child:
                    visible_paths += 1
        if visible_paths == 0:
            stats['no_fill_color_on_all_paths'] += 1
            no_fill_files.append(f)
            
        # Check coordinates
        # Find all pathData
        all_d = []
        for p in root.iter():
            if p.tag.endswith('path') or p.tag.endswith('clip-path'):
                d = p.attrib.get('{http://schemas.android.com/apk/res/android}pathData', '')
                all_d.append(d)
        all_d_str = " ".join(all_d)
        nums = [float(n) for n in re.findall(r'[-+]?\d*\.?\d+', all_d_str)]
        max_coord = max(nums) if nums else 0
        min_coord = min(nums) if nums else 0
        if max_coord > vw * 1.5 or min_coord < -vw * 0.5:
            stats['paths_outside_viewport'] += 1
            sample_outside.append((f, min_coord, max_coord, vw, vh))
    except Exception as e:
        print(f"Error parsing {f}: {e}")

print('Stats:', stats)
print('Files with no fill/stroke color on any path:', len(no_fill_files))
for f in no_fill_files[:10]:
    print('  ', f)
print('Files with coordinates outside viewport:', len(sample_outside))
for s in sample_outside[:15]:
    print(f"   {os.path.basename(s[0])}: min={s[1]}, max={s[2]}, vw={s[3]}, vh={s[4]}")
