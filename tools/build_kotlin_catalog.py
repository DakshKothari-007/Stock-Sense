import json
import os

def escape_str(s):
    return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', ' ')

def main():
    with open('tools/compiled_items_list.json', 'r', encoding='utf-8') as f:
        items = json.load(f)

    with open('node_modules/cldr-annotations-modern/annotations/en/annotations.json', 'r', encoding='utf-8') as f:
        en_cldr = json.load(f)['annotations']['annotations']
    with open('node_modules/cldr-annotations-modern/annotations/hi/annotations.json', 'r', encoding='utf-8') as f:
        hi_cldr = json.load(f)['annotations']['annotations']
    with open('node_modules/cldr-annotations-modern/annotations/ta/annotations.json', 'r', encoding='utf-8') as f:
        ta_cldr = json.load(f)['annotations']['annotations']

    catalog_data = []

    for key, char, cp, fname, dept, extra_kw in items:
        en_info = en_cldr.get(char, {})
        hi_info = hi_cldr.get(char, {})
        ta_info = ta_cldr.get(char, {})

        name_en = en_info.get('tts', [key.replace('_', ' ')])[0]
        name_hi = hi_info.get('tts', [name_en])[0]
        name_ta = ta_info.get('tts', [name_en])[0]

        en_kws = [k.lower().strip() for k in en_info.get('default', [])]
        hi_kws = [k.lower().strip() for k in hi_info.get('default', [])]
        ta_kws = [k.lower().strip() for k in ta_info.get('default', [])]
        user_kws = [k.lower().strip() for k in extra_kw]

        # Combine, prioritize specific user keywords first, then CLDR
        combined = []
        seen = set()
        for kw in user_kws + en_kws + hi_kws + ta_kws:
            if kw and kw not in seen:
                seen.add(kw)
                combined.append(kw)

        catalog_data.append({
            'icon_key': key,
            'drawable': f'ic_prod_{key}',
            'department': dept,
            'name_en': name_en,
            'name_hi': name_hi,
            'name_ta': name_ta,
            'keywords': combined
        })

    # Save JSON to assets
    json_path = 'app/src/main/assets/product_icon_catalog.json'
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(catalog_data, f, ensure_ascii=False, indent=2)
    print(f'Wrote {json_path} with {len(catalog_data)} items')

    # Generate ProductIconCatalog.kt
    kt_path = 'app/src/main/java/com/simats/stocksense/feature/inventory/icon/ProductIconCatalog.kt'
    
    kt_code = ["""package com.simats.stocksense.feature.inventory.icon

import androidx.compose.ui.graphics.Color
import com.simats.stocksense.R

/**
 * Supermarket product departments with soft tinted background colors
 * and fallback icons.
 */
enum class ProductDepartment(
    val id: String,
    val displayName: String,
    val defaultIconRes: Int,
    val lightBg: Color,
    val darkBg: Color,
    val badgeTextColor: Color
) {
    FRUITS("fruits", "Fruits", R.drawable.ic_prod_apple, Color(0xFFFFF1F2), Color(0xFF4C1D24), Color(0xFFE11D48)),
    VEGETABLES("vegetables", "Vegetables", R.drawable.ic_prod_potato, Color(0xFFF0FDF4), Color(0xFF14532D), Color(0xFF16A34A)),
    DAIRY("dairy", "Dairy & Eggs", R.drawable.ic_prod_milk_bottle, Color(0xFFF0F9FF), Color(0xFF164E63), Color(0xFF0284C7)),
    BAKERY("bakery", "Bakery", R.drawable.ic_prod_bread, Color(0xFFFFFBEB), Color(0xFF451A03), Color(0xFFD97706)),
    SNACKS("snacks", "Snacks", R.drawable.ic_prod_cookie, Color(0xFFFFF7ED), Color(0xFF431407), Color(0xFFEA580C)),
    SWEETS("sweets", "Sweets", R.drawable.ic_prod_chocolate, Color(0xFFFDF2F8), Color(0xFF500724), Color(0xFFDB2777)),
    STAPLES("staples", "Grains & Staples", R.drawable.ic_prod_sheaf_of_rice, Color(0xFFFEFCE8), Color(0xFF422006), Color(0xFFCA8A04)),
    SPICES_OILS("spices_oils", "Spices & Oils", R.drawable.ic_prod_oil_drop, Color(0xFFFEF3C7), Color(0xFF3F2D06), Color(0xFFB45309)),
    BEVERAGES("beverages", "Beverages", R.drawable.ic_prod_tea_cup, Color(0xFFECFDF5), Color(0xFF064E3B), Color(0xFF059669)),
    MEAT_FISH("meat_fish", "Meat & Seafood", R.drawable.ic_prod_poultry_leg, Color(0xFFFFF1F2), Color(0xFF4C0519), Color(0xFFE11D48)),
    CLEANING("cleaning", "Cleaning", R.drawable.ic_prod_soap_bar, Color(0xFFEEF2FF), Color(0xFF1E1B4B), Color(0xFF4F46E5)),
    PERSONAL_CARE("personal_care", "Personal Care", R.drawable.ic_prod_toothbrush, Color(0xFFF5F3FF), Color(0xFF2E1065), Color(0xFF7C3AED)),
    BABY_CARE("baby_care", "Baby Care", R.drawable.ic_prod_baby_face, Color(0xFFEFF6FF), Color(0xFF172554), Color(0xFF2563EB)),
    PET_CARE("pet_care", "Pet Supplies", R.drawable.ic_prod_dog, Color(0xFFFDF8F6), Color(0xFF382319), Color(0xFF9A3412)),
    STATIONERY("stationery", "Stationery", R.drawable.ic_prod_ballpoint_pen, Color(0xFFF8FAFC), Color(0xFF1E293B), Color(0xFF475569)),
    KITCHENWARE("kitchenware", "Kitchenware", R.drawable.ic_prod_kitchen_knife, Color(0xFFF4F4F5), Color(0xFF27272A), Color(0xFF52525B)),
    ELECTRONICS("electronics", "Electronics", R.drawable.ic_prod_battery, Color(0xFFEFF6FF), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    HEALTH("health", "Health & Medicine", R.drawable.ic_prod_pill, Color(0xFFF0FDF4), Color(0xFF064E3B), Color(0xFF15803D)),
    TOYS_GIFTS("toys_gifts", "Toys & Gifts", R.drawable.ic_prod_gift_box, Color(0xFFFAF5FF), Color(0xFF3B0764), Color(0xFF9333EA)),
    GENERAL("general", "General & Packaging", R.drawable.ic_prod_package, Color(0xFFF3F4F6), Color(0xFF1F2937), Color(0xFF4B5563));

    companion object {
        fun fromCategory(category: String?): ProductDepartment {
            if (category.isNullOrBlank()) return GENERAL
            val clean = category.trim().lowercase()
            return when {
                clean.contains("fruit") || clean.contains("berry") || clean.contains("apple") -> FRUITS
                clean.contains("veg") || clean.contains("greens") || clean.contains("herb") -> VEGETABLES
                clean.contains("dairy") || clean.contains("milk") || clean.contains("cheese") || clean.contains("egg") || clean.contains("curd") || clean.contains("paneer") -> DAIRY
                clean.contains("bake") || clean.contains("bread") || clean.contains("roti") || clean.contains("bun") -> BAKERY
                clean.contains("snack") || clean.contains("biscuit") || clean.contains("chip") || clean.contains("namkeen") || clean.contains("cookie") -> SNACKS
                clean.contains("sweet") || clean.contains("choc") || clean.contains("dessert") || clean.contains("candy") -> SWEETS
                clean.contains("staple") || clean.contains("grain") || clean.contains("rice") || clean.contains("flour") || clean.contains("atta") || clean.contains("dal") || clean.contains("pulse") -> STAPLES
                clean.contains("spice") || clean.contains("oil") || clean.contains("masala") || clean.contains("sauce") -> SPICES_OILS
                clean.contains("bev") || clean.contains("drink") || clean.contains("tea") || clean.contains("coffee") || clean.contains("juice") || clean.contains("soda") -> BEVERAGES
                clean.contains("meat") || clean.contains("fish") || clean.contains("poultry") || clean.contains("chicken") || clean.contains("seafood") -> MEAT_FISH
                clean.contains("clean") || clean.contains("wash") || clean.contains("detergent") || clean.contains("house") -> CLEANING
                clean.contains("person") || clean.contains("care") || clean.contains("cosmetic") || clean.contains("shampoo") || clean.contains("hygiene") -> PERSONAL_CARE
                clean.contains("baby") || clean.contains("infant") || clean.contains("diaper") -> BABY_CARE
                clean.contains("pet") || clean.contains("dog") || clean.contains("cat") -> PET_CARE
                clean.contains("station") || clean.contains("office") || clean.contains("pen") || clean.contains("book") -> STATIONERY
                clean.contains("kitchen") || clean.contains("utensil") || clean.contains("cook") || clean.contains("plate") -> KITCHENWARE
                clean.contains("electr") || clean.contains("battery") || clean.contains("gadget") || clean.contains("appliance") -> ELECTRONICS
                clean.contains("health") || clean.contains("med") || clean.contains("pharma") || clean.contains("pill") -> HEALTH
                clean.contains("toy") || clean.contains("gift") || clean.contains("game") || clean.contains("puja") || clean.contains("festiv") -> TOYS_GIFTS
                else -> GENERAL
            }
        }
    }
}

/**
 * Metadata for a single product icon in the catalog.
 */
data class ProductIconItem(
    val iconKey: String,
    val drawableRes: Int,
    val department: ProductDepartment,
    val nameEn: String,
    val nameHi: String,
    val nameTa: String,
    val keywords: List<String>
)

/**
 * Versatile product icon catalog covering 300+ supermarket retail products.
 * Includes official Google Noto Emoji vectors and multi-lingual keywords (English, Hindi, Tamil, Hinglish, Tanglish).
 */
object ProductIconCatalog {

    val ITEMS: List<ProductIconItem> = listOf("""]

    for item in catalog_data:
        k = item['icon_key']
        d = item['department']
        ne = escape_str(item['name_en'])
        nh = escape_str(item['name_hi'])
        nt = escape_str(item['name_ta'])
        kws_str = ", ".join(f'"{escape_str(kw)}"' for kw in item['keywords'])
        kt_code.append(f"""        ProductIconItem(
            iconKey = "{k}",
            drawableRes = R.drawable.ic_prod_{k},
            department = ProductDepartment.{d},
            nameEn = "{ne}",
            nameHi = "{nh}",
            nameTa = "{nt}",
            keywords = listOf({kws_str})
        ),""")

    kt_code.append("""    )

    val BY_KEY: Map<String, ProductIconItem> = ITEMS.associateBy { it.iconKey }

    val BY_DEPARTMENT: Map<ProductDepartment, List<ProductIconItem>> = ITEMS.groupBy { it.department }

    val DEFAULT_PACKAGE_ICON: Int = R.drawable.ic_prod_package

    /**
     * Looks up an icon item by its exact icon_key.
     */
    fun get(iconKey: String?): ProductIconItem? {
        if (iconKey.isNullOrBlank()) return null
        return BY_KEY[iconKey.trim().lowercase()]
    }

    /**
     * Returns the default icon resource for a category or department name.
     */
    fun getDepartmentIcon(category: String?): Int {
        val dept = ProductDepartment.fromCategory(category)
        return dept.defaultIconRes
    }

    /**
     * Searches the catalog across English, Hindi, and Tamil names and keywords.
     */
    fun search(query: String, department: ProductDepartment? = null): List<ProductIconItem> {
        val clean = query.trim().lowercase()
        val pool = if (department != null) BY_DEPARTMENT[department].orEmpty() else ITEMS
        if (clean.isEmpty()) return pool

        return pool.filter { item ->
            item.nameEn.lowercase().contains(clean) ||
            item.nameHi.lowercase().contains(clean) ||
            item.nameTa.lowercase().contains(clean) ||
            item.iconKey.contains(clean) ||
            item.keywords.any { it.contains(clean) }
        }
    }
}
""")

    with open(kt_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(kt_code))
    print(f'Wrote {kt_path} successfully!')

if __name__ == '__main__':
    main()

