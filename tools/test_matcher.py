import json
import re

with open('app/src/main/assets/product_icon_catalog.json', 'r', encoding='utf-8') as f:
    catalog = json.load(f)

# Helper: Levenshtein distance
def levenshtein(s1, s2):
    if len(s1) < len(s2):
        return levenshtein(s2, s1)
    if len(s2) == 0:
        return len(s1)
    prev = range(len(s2) + 1)
    for i, c1 in enumerate(s1):
        curr = [i + 1]
        for j, c2 in enumerate(s2):
            insertions = prev[j + 1] + 1
            deletions = curr[j] + 1
            substitutions = prev[j] + (c1 != c2)
            curr.append(min(insertions, deletions, substitutions))
        prev = curr
    return prev[-1]

UNIT_REGEX = re.compile(r'\b\d+(\.\d+)?\s*(g|gm|gms|gram|grams|kg|kgs|kilo|kilos|ml|l|ltr|ltrs|liter|liters|litre|litres|pc|pcs|piece|pieces|pack|packs|pk|pkt|pkts|bundle|combo|family\s*pack|super\s*saver|pouch|can|tin|bottle|box|sachet|unit|units|doz|dozen|bunch|meter|m|cm|mm|pages|page|w|watt|watts|v|volt|volts|mg|tablet|tablets|capsule|capsules)\b', re.I)
NUM_REGEX = re.compile(r'\b\d+\b')
SYMBOLS_REGEX = re.compile(r'[\-_/\\·,.:;!|~`#\'"()\[\]{}+*?@$%^&<>=]')

def normalize(name):
    if not name:
        return ''
    s = name.lower()
    s = UNIT_REGEX.sub(' ', s)
    s = SYMBOLS_REGEX.sub(' ', s)
    s = NUM_REGEX.sub(' ', s)
    s = re.sub(r'\s+', ' ', s).strip()
    return s

def stem_word(w):
    if len(w) > 4:
        if w.endswith('ies'): return w[:-3] + 'y'
        if w.endswith('es') and not w.endswith('tes'): return w[:-2]
        if w.endswith('s') and not w.endswith('ss'): return w[:-1]
    return w

def match_product(name, category=None):
    norm = normalize(name)
    if not norm:
        return None, 0.0
    
    tokens = [t for t in norm.split() if len(t) > 0]
    if not tokens:
        return None, 0.0

    best_key = None
    best_score = 0.0
    
    cat_dept = None
    if category:
        c = category.lower()
        if 'fruit' in c: cat_dept = 'FRUITS'
        elif 'veg' in c: cat_dept = 'VEGETABLES'
        elif 'dairy' in c: cat_dept = 'DAIRY'
        elif 'bev' in c or 'drink' in c or 'tea' in c: cat_dept = 'BEVERAGES'
        elif 'snack' in c or 'biscuit' in c: cat_dept = 'SNACKS'
        elif 'sweet' in c: cat_dept = 'SWEETS'
        elif 'staple' in c or 'grain' in c: cat_dept = 'STAPLES'
        elif 'spice' in c or 'oil' in c: cat_dept = 'SPICES_OILS'

    for item in catalog:
        key = item['icon_key']
        kws = item['keywords']
        names = [item['name_en'].lower(), item['name_hi'].lower(), item['name_ta'].lower(), key.replace('_', ' ')]
        
        score = 0.0
        
        # 1. Multi-word phrase exact match in keywords
        for kw in kws:
            kw_clean = kw.lower().strip()
            if not kw_clean:
                continue
            if ' ' in kw_clean:
                if f' {kw_clean} ' in f' {norm} ' or norm == kw_clean:
                    # Specificity bonus for multi-word match ("coconut oil" gets oil)
                    s = 0.95 + min(len(kw_clean) * 0.005, 0.04)
                    if s > score: score = s
            else:
                # Single word kw match
                for t in tokens:
                    if t == kw_clean:
                        s = 0.90 + min(len(kw_clean) * 0.002, 0.03)
                        if s > score: score = s
                    elif stem_word(t) == stem_word(kw_clean):
                        s = 0.82
                        if s > score: score = s
                    elif len(t) >= 5 and len(kw_clean) >= 5 and levenshtein(t, kw_clean) == 1:
                        s = 0.72
                        if s > score: score = s

        # Boost if department matches category
        if cat_dept and item['department'] == cat_dept and score > 0:
            score += 0.05
            
        if score > best_score:
            best_score = score
            best_key = key

    return best_key, min(best_score, 1.0)

# 65 Test Cases across categories, Indian languages, brands, typos, odd names, empty
TEST_CASES = [
    # English standard & FMCG brands
    ('Amul Butter 100g', 'Dairy', 'butter'),
    ('Fresh Red Apples', 'Fruits', 'apple'),
    ('Cow Milk 1L', 'Dairy', 'milk_bottle'),
    ('Whole Wheat Bread', 'Bakery', 'bread'),
    ('Tata Salt 1kg', 'Staples', 'salt'),
    ('Potato Chips', 'Snacks', 'potato_chips'),
    ('Lipton Green Tea', 'Beverages', 'green_tea'),
    ('Coca Cola Can', 'Beverages', 'cup_with_straw'),
    ('Basmati Rice 5kg', 'Staples', 'sheaf_of_rice'),
    ('Dove Soap 75g', 'Personal Care', 'soap_bar'),
    ('Colgate Toothpaste', 'Personal Care', 'toothbrush'),
    ('Surf Excel Detergent', 'Cleaning', 'soap_bar'),
    ('Vim Dishwash Bar', 'Cleaning', 'soap_bar'),
    ('Aashirvaad Shudh Chakki Atta', 'Staples', 'bowl_flour'),
    ('Parle-G Biscuits', 'Snacks', 'cookie'),
    ('Britannia Good Day Cookies', 'Snacks', 'cookie'),
    ('Maggi 2-Minute Noodles', 'Staples', 'noodles'),
    ('Dabur Honey 250g', 'Sweets', 'honey'),
    ('Everest Garam Masala', 'Spices', 'curry_rice'),
    ('Fortune Sunflower Oil 1L', 'Oils', 'oil_drop'),
    ('Haldiram Bhujia Sev', 'Snacks', 'sev_bhujia'),
    ('Cadbury Dairy Milk Chocolate', 'Sweets', 'chocolate'),
    ('Nestle KitKat', 'Sweets', 'chocolate'),
    ('Dettol Antiseptic Soap', 'Personal Care', 'soap_bar'),
    ('Pampers Baby Diapers', 'Baby Care', 'baby_face'),
    ('Pedigree Adult Dog Food', 'Pet Care', 'dog'),
    ('Whiskas Cat Food', 'Pet Care', 'cat'),
    ('Reynolds Ball Pen Blue', 'Stationery', 'ballpoint_pen'),
    ('Classmate Notebook 172 Pages', 'Stationery', 'spiral_notebook'),
    ('Stainless Steel Kitchen Knife', 'Kitchen', 'kitchen_knife'),
    ('Philips LED Bulb 9W', 'Electronics', 'light_bulb'),
    ('Duracell AA Batteries', 'Electronics', 'battery'),
    ('Paracetamol Tablets 500mg', 'Health', 'pill'),
    ('Band-Aid Adhesive Bandage', 'Health', 'bandage'),
    ('Coconut Oil 500ml', 'Oils', 'oil_drop'),
    # Hindi script & Hinglish
    ('दूध 500ml', 'Dairy', 'milk_bottle'),
    ('ताजा सेब', 'Fruits', 'apple'),
    ('आलू 1kg', 'Vegetables', 'potato'),
    ('टमाटर', 'Vegetables', 'tomato'),
    ('प्याज़', 'Vegetables', 'onion'),
    ('चावल 5kg', 'Staples', 'cooked_rice'),
    ('आटा', 'Staples', 'bowl_flour'),
    ('घी 1kg', 'Dairy', 'butter'),
    ('चाय पत्ती', 'Beverages', 'tea_cup'),
    ('बिस्कुट', 'Snacks', 'cookie'),
    ('doodh 500 ml', 'Dairy', 'milk_bottle'),
    ('atta 5 kg', 'Staples', 'bowl_flour'),
    ('makhan 100g', 'Dairy', 'butter'),
    # Tamil script & Tanglish
    ('பால் 500ml', 'Dairy', 'milk_bottle'),
    ('ஆப்பிள்', 'Fruits', 'apple'),
    ('வெங்காயம்', 'Vegetables', 'onion'),
    ('உருளைக்கிழங்கு', 'Vegetables', 'potato'),
    ('அரிசி', 'Staples', 'cooked_rice'),
    ('எண்ணெய்', 'Oils', 'oil_drop'),
    ('நெய்', 'Dairy', 'butter'),
    ('டீ தூள்', 'Beverages', 'tea_cup'),
    ('paal 1 litre', 'Dairy', 'milk_bottle'),
    ('arisi 10kg', 'Staples', 'cooked_rice'),
    # Typos (edit distance 1 for 5+ letters)
    ('biscut 100g', 'Snacks', 'cookie'),
    ('choclate bar', 'Sweets', 'chocolate'),
    ('coffe 50g', 'Beverages', 'tea_cup'),
    ('tomaoto 1kg', 'Vegetables', 'tomato'),
    ('potatoe', 'Vegetables', 'potato'),
    ('chikken curry cut', 'Meat', 'poultry_leg'),
    # Odd / empty / symbols
    ('', None, None),
    ('   ', None, None),
    ('!@#$%^&*', None, None),
    ('123456', None, None),
    ('Random Nonexistent Product 999', None, None)
]

correct = 0
total = len(TEST_CASES)
for name, cat, exp in TEST_CASES:
    matched, conf = match_product(name, cat)
    is_ok = (matched == exp) if exp else (matched is None or conf < 0.5)
    if is_ok:
        correct += 1
    else:
        print(f'FAIL: "{name}" -> matched: {matched} ({conf:.2f}), expected: {exp}')

pct = (correct / total) * 100
print(f'\nResult: {correct} / {total} tests passed ({pct:.1f}%)')

