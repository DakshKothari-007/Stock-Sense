import glob, os

svgs = glob.glob('tools/svg_repo/*.svg')
print(f'Total SVGs: {len(svgs)}')

counts = {
    'linearGradient': 0,
    'radialGradient': 0,
    'style_attr': 0,
    'style_tag': 0,
    'use': 0,
    'symbol': 0,
    'clipPath': 0,
    'mask': 0
}

files_with_symbol = []
files_with_clip = []
files_with_use = []
files_with_mask = []

for s in svgs:
    with open(s, 'r', encoding='utf-8') as f:
        c = f.read()
    if '<linearGradient' in c:
        counts['linearGradient'] += 1
    if '<radialGradient' in c:
        counts['radialGradient'] += 1
    if 'style="' in c or "style='" in c:
        counts['style_attr'] += 1
    if '<style' in c:
        counts['style_tag'] += 1
    if '<use' in c: 
        counts['use'] += 1
        files_with_use.append(os.path.basename(s))
    if '<symbol' in c: 
        counts['symbol'] += 1
        files_with_symbol.append(os.path.basename(s))
    if '<clipPath' in c: 
        counts['clipPath'] += 1
        files_with_clip.append(os.path.basename(s))
    if '<mask' in c: 
        counts['mask'] += 1
        files_with_mask.append(os.path.basename(s))

print('Feature counts across 265 SVGs:')
for k, v in counts.items():
    print(f'  {k}: {v}')

print('Files with symbol:', files_with_symbol)
print('Files with mask:', files_with_mask)
print('Files with clipPath:', len(files_with_clip), files_with_clip[:10])
print('Files with use:', len(files_with_use), files_with_use[:10])
