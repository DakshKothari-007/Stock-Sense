import os
import re
import glob
import json
import shutil
import subprocess

def parse_color(c):
    c = c.strip()
    if c.startswith('#'):
        if len(c) == 4:
            return '#' + ''.join([x*2 for x in c[1:]]).upper()
        return c.upper()
    named = {
        'white': '#FFFFFF',
        'black': '#000000',
        'none': 'none',
        'transparent': 'none',
        'red': '#FF0000',
        'green': '#008000',
        'blue': '#0000FF',
        'yellow': '#FFFF00'
    }
    return named.get(c.lower(), c)

def flatten_svg(content):
    # 1. Parse linearGradient and radialGradient
    grad_pattern = re.compile(r'<(?:linearGradient|radialGradient)[^>]*id="([^"]+)"[^>]*>(.*?)</(?:linearGradient|radialGradient)>', re.DOTALL | re.IGNORECASE)
    gradients = {}

    for match in grad_pattern.finditer(content):
        gid = match.group(1)
        body = match.group(2)
        stops = re.findall(r'<stop[^>]*>', body, re.IGNORECASE)
        colors = []
        for stop in stops:
            m_col = re.search(r'stop-color="([^"]+)"', stop, re.IGNORECASE)
            if not m_col:
                m_col = re.search(r'stop-color:([^;"\s]+)', stop, re.IGNORECASE)
            if m_col:
                col = parse_color(m_col.group(1))
                if col != 'none':
                    colors.append(col)
        if colors:
            mid = colors[len(colors) // 2]
            gradients[gid] = mid

    # Replace gradient references
    for gid, col in gradients.items():
        content = re.sub(rf'fill="url\(#{re.escape(gid)}\)"', f'fill="{col}"', content, flags=re.IGNORECASE)
        content = re.sub(rf'fill:url\(#{re.escape(gid)}\)', f'fill:{col}', content, flags=re.IGNORECASE)
        content = re.sub(rf'stroke="url\(#{re.escape(gid)}\)"', f'stroke="{col}"', content, flags=re.IGNORECASE)
        content = re.sub(rf'stroke:url\(#{re.escape(gid)}\)', f'stroke:{col}', content, flags=re.IGNORECASE)

    # 2. Convert style="..." to XML attributes
    def style_to_attrs(match):
        style_val = match.group(1)
        attrs = []
        for part in style_val.split(';'):
            if ':' in part:
                k, v = part.split(':', 1)
                k = k.strip().lower()
                v = v.strip()
                if k in ['fill', 'stroke', 'stroke-width', 'stroke-linecap', 'stroke-linejoin', 'opacity', 'fill-opacity', 'stroke-opacity']:
                    if k in ['fill', 'stroke']:
                        v = parse_color(v)
                    attrs.append(f'{k}="{v}"')
        return ' ' + ' '.join(attrs) if attrs else ''

    content = re.sub(r'style="([^"]*)"', style_to_attrs, content, flags=re.IGNORECASE)

    # 3. Strip gradient definitions
    content = grad_pattern.sub('', content)
    content = re.sub(r'<(?:linearGradient|radialGradient)[^>]*/>', '', content, flags=re.IGNORECASE)

    # 4. Strip symbols, masks, filters
    content = re.sub(r'<symbol[\s\S]*?</symbol>', '', content, flags=re.IGNORECASE)
    content = re.sub(r'<mask[\s\S]*?</mask>', '', content, flags=re.IGNORECASE)
    content = re.sub(r'<mask[^>]*/>', '', content, flags=re.IGNORECASE)
    content = re.sub(r'mask="[^"]*"', '', content, flags=re.IGNORECASE)
    content = re.sub(r'<filter[\s\S]*?</filter>', '', content, flags=re.IGNORECASE)
    content = re.sub(r'<filter[^>]*/>', '', content, flags=re.IGNORECASE)
    content = re.sub(r'filter="[^"]*"', '', content, flags=re.IGNORECASE)

    return content

def main():
    flat_dir = 'tools/flattened_svgs'
    conv_dir = 'tools/converted_xmls'
    os.makedirs(flat_dir, exist_ok=True)
    os.makedirs(conv_dir, exist_ok=True)

    svg_files = glob.glob('tools/svg_repo/*.svg')
    print(f'Flattening {len(svg_files)} SVGs...')
    for f in svg_files:
        with open(f, 'r', encoding='utf-8') as fp:
            data = fp.read()
        flat = flatten_svg(data)
        out_path = os.path.join(flat_dir, os.path.basename(f))
        with open(out_path, 'w', encoding='utf-8') as fp:
            fp.write(flat)

    print('Flattening complete. Running svg2vectordrawable via npx...')
    res = subprocess.run(
        f'npx -y svg2vectordrawable -f {flat_dir} -o {conv_dir}',
        shell=True,
        capture_output=True,
        text=True
    )
    print('STDOUT:', res.stdout)
    print('STDERR:', res.stderr)
    print('Return code:', res.returncode)

if __name__ == '__main__':
    main()
