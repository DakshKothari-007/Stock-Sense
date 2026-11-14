package com.simats.stocksense.feature.inventory.icon

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

    val ITEMS: List<ProductIconItem> = listOf(
        ProductIconItem(
            iconKey = "apple",
            drawableRes = R.drawable.ic_prod_apple,
            department = ProductDepartment.FRUITS,
            nameEn = "red apple",
            nameHi = "लाल सेब, सेवफल, लाल, फल",
            nameTa = "ஆப்பிள்",
            keywords = listOf("seb", "sev", "सेब", "ஆப்பிள்", "safarchand", "kashmiri apple", "shimla apple", "red apple", "fresh apple", "apple", "fruit", "red", "फल", "लाल", "लाल सेब", "लाल सेब, सेवफल, लाल, फल", "सेवफल", "உடல்நலம்", "உணவு", "சிவப்பு")
        ),
        ProductIconItem(
            iconKey = "green_apple",
            drawableRes = R.drawable.ic_prod_green_apple,
            department = ProductDepartment.FRUITS,
            nameEn = "green apple",
            nameHi = "हरा सेब, सेव, हरा, फल",
            nameTa = "பச்சை ஆப்பிள்",
            keywords = listOf("granny smith", "green apple", "sour apple", "kacha seb", "apple", "fruit", "green", "फल", "सेव", "हरा", "हरा सेब", "हरा सेब, सेव, हरा, फल", "ஆப்பிள்", "பச்சை ஆப்பிள்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "banana",
            drawableRes = R.drawable.ic_prod_banana,
            department = ProductDepartment.FRUITS,
            nameEn = "banana",
            nameHi = "केला, फल",
            nameTa = "வாழைப்பழம்",
            keywords = listOf("kela", "केला", "வாழைப்பழம்", "vazhaipazham", "yelakki", "robusta", "nendran", "cavendish", "raw banana", "pazham", "banana", "fruit", "केला, फल", "फल", "உடல்நலம்", "உணவு", "பழம்")
        ),
        ProductIconItem(
            iconKey = "orange",
            drawableRes = R.drawable.ic_prod_orange,
            department = ProductDepartment.FRUITS,
            nameEn = "tangerine",
            nameHi = "संतरा, फल",
            nameTa = "ஆரஞ்சு",
            keywords = listOf("santra", "narangi", "संतरा", "ஆரஞ்சு", "kamala orange", "nagpur santra", "kinnow", "tangerine", "mandarin", "aarancu", "fruit", "orange", "नारंगी", "फल", "संतरा, फल", "உடல்நலம்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "lemon",
            drawableRes = R.drawable.ic_prod_lemon,
            department = ProductDepartment.FRUITS,
            nameEn = "lemon",
            nameHi = "नीबू",
            nameTa = "எலுமிச்சை",
            keywords = listOf("nimbu", "नींबू", "எலுமிச்சை", "elumichai", "lime", "citrus", "yellow lemon", "sour lemon", "nimbu paani", "fruit", "lemon", "चकोतरा", "नीबू", "फल", "பழம்", "புளிப்பு", "மஞ்சள் வண்ணம்")
        ),
        ProductIconItem(
            iconKey = "watermelon",
            drawableRes = R.drawable.ic_prod_watermelon,
            department = ProductDepartment.FRUITS,
            nameEn = "watermelon",
            nameHi = "तरबूज़",
            nameTa = "தர்பூசணி",
            keywords = listOf("tarbooz", "तरबूज", "தர்பூசணி", "tharpusani", "water melon", "tarbuj", "red melon", "summer fruit", "fruit", "watermelon", "तरबूज़", "फल", "கோடைகாலம்", "தாகம்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "grapes",
            drawableRes = R.drawable.ic_prod_grapes,
            department = ProductDepartment.FRUITS,
            nameEn = "grapes",
            nameHi = "अंगूर, फल",
            nameTa = "திராட்சை",
            keywords = listOf("angoor", "अंगूर", "திராட்சை", "dhrakshai", "thompson", "kishmish", "black grapes", "green grapes", "sonaka", "fruit", "grape", "grapes", "अंगूर, फल", "फल", "ஒயின்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "strawberry",
            drawableRes = R.drawable.ic_prod_strawberry,
            department = ProductDepartment.FRUITS,
            nameEn = "strawberry",
            nameHi = "स्ट्रॉबेरी",
            nameTa = "ஸ்ட்ராபெர்ரி",
            keywords = listOf("strawberries", "berry", "fresh strawberry", "mahableshwar strawberry", "fruit", "strawberry", "फल", "बेरी", "स्ट्रॉबेरी", "हिसालू", "உணவு", "பழம்", "ஸ்ட்ராபெர்ரி")
        ),
        ProductIconItem(
            iconKey = "melon",
            drawableRes = R.drawable.ic_prod_melon,
            department = ProductDepartment.FRUITS,
            nameEn = "melon",
            nameHi = "खरबूजा, फल",
            nameTa = "மெலன்",
            keywords = listOf("kharbooja", "खरबूजा", "kirni pazham", "muskmelon", "cantaloupe", "honeydew", "sweet melon", "fruit", "melon", "खरबूजा, फल", "फल", "தாகம்", "பழம்", "மெலன்")
        ),
        ProductIconItem(
            iconKey = "cherries",
            drawableRes = R.drawable.ic_prod_cherries,
            department = ProductDepartment.FRUITS,
            nameEn = "cherries",
            nameHi = "चेरी",
            nameTa = "செர்ரி பழம்",
            keywords = listOf("cherry", "red cherry", "kashmir cherry", "fresh cherry", "berries", "cherries", "fruit", "red", "आलूबालू", "चेरी", "चैरी", "फल", "சிவப்பு", "செர்ரி பழம்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "peach",
            drawableRes = R.drawable.ic_prod_peach,
            department = ProductDepartment.FRUITS,
            nameEn = "peach",
            nameHi = "आड़ू",
            nameTa = "பீச் பழம்",
            keywords = listOf("aadu", "fresh peach", "stone fruit", "fruit", "peach", "आड़ू", "फल", "உணவு", "பழம்", "பீச் பழம்")
        ),
        ProductIconItem(
            iconKey = "mango",
            drawableRes = R.drawable.ic_prod_mango,
            department = ProductDepartment.FRUITS,
            nameEn = "mango",
            nameHi = "आम",
            nameTa = "மாம்பழம்",
            keywords = listOf("aam", "आम", "மாம்பழம்", "mambazham", "alphonso", "kesar", "banganapalli", "totapuri", "dasheri", "langra", "ratnagiri", "fruit", "mango", "tropical", "ट्रॉपिकल", "फल", "ட்ராபிகல்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "pineapple",
            drawableRes = R.drawable.ic_prod_pineapple,
            department = ProductDepartment.FRUITS,
            nameEn = "pineapple",
            nameHi = "अन्नानास, फल",
            nameTa = "அன்னாசிப்பழம்",
            keywords = listOf("ananas", "अनानास", "அன்னாசி", "annasi pazham", "rani pineapple", "sweet pineapple", "fruit", "pineapple", "अन्नानास", "अन्नानास, फल", "फल", "அன்னாசிப்பழம்", "உடல்நலம்", "உணவு", "பழம்")
        ),
        ProductIconItem(
            iconKey = "coconut",
            drawableRes = R.drawable.ic_prod_coconut,
            department = ProductDepartment.FRUITS,
            nameEn = "coconut",
            nameHi = "नारियल",
            nameTa = "தேங்காய்",
            keywords = listOf("nariyal", "नारियल", "தேங்காய்", "thengai", "fresh coconut", "tender coconut", "elaneer", "copra", "dry coconut", "coconut", "palm", "piña colada", "ताड़", "पीना कोलाडा", "தென்னை", "பனை", "மரம்")
        ),
        ProductIconItem(
            iconKey = "kiwi",
            drawableRes = R.drawable.ic_prod_kiwi,
            department = ProductDepartment.FRUITS,
            nameEn = "kiwi fruit",
            nameHi = "कीवी फल",
            nameTa = "கிவிப்பழம்",
            keywords = listOf("kiwifruit", "green kiwi", "zespri", "food", "fruit", "kiwi", "कीवी", "फल", "भोजन", "உணவு", "கிவி", "கிவிப்பழம்", "பழம்")
        ),
        ProductIconItem(
            iconKey = "pear",
            drawableRes = R.drawable.ic_prod_pear,
            department = ProductDepartment.FRUITS,
            nameEn = "pear",
            nameHi = "नाशपाती, फल",
            nameTa = "பேரிக்காய்",
            keywords = listOf("nashpati", "नाशपाती", "berikai", "green pear", "babu gosha", "fruit", "pear", "नाशपाती, फल", "फल", "உணவு", "பேரிக்காய்")
        ),
        ProductIconItem(
            iconKey = "avocado",
            drawableRes = R.drawable.ic_prod_avocado,
            department = ProductDepartment.FRUITS,
            nameEn = "avocado",
            nameHi = "अवोकाडो",
            nameTa = "வெண்ணைப் பழம்",
            keywords = listOf("butter fruit", "hass avocado", "guacamole", "avocado", "food", "fruit", "अवोकाडो", "फल", "भोजन", "உணவு", "பழம்", "வெண்ணைப் பழம்", "வெண்ணைப்பழம்")
        ),
        ProductIconItem(
            iconKey = "tomato",
            drawableRes = R.drawable.ic_prod_tomato,
            department = ProductDepartment.FRUITS,
            nameEn = "tomato",
            nameHi = "टमाटर",
            nameTa = "தக்காளி",
            keywords = listOf("tamatar", "टमाटर", "தக்காளி", "thakkali", "desi tomato", "hybrid tomato", "red tomato", "fruit", "tomato", "vegetable", "सब्जी", "உணவு", "காய்கறி")
        ),
        ProductIconItem(
            iconKey = "blueberries",
            drawableRes = R.drawable.ic_prod_blueberries,
            department = ProductDepartment.FRUITS,
            nameEn = "blueberries",
            nameHi = "ब्लूबेरी",
            nameTa = "ப்ளூபெர்ரிகள்",
            keywords = listOf("blueberry", "fresh blueberry", "imported berries", "berry", "bilberry", "blue", "blueberries", "नीला", "बिलबेरी", "बेरी", "ब्लूबेरी", "பில்பெர்ரி", "பெர்ரி", "ப்ளூ", "ப்ளூபெர்ரி", "ப்ளூபெர்ரிகள்")
        ),
        ProductIconItem(
            iconKey = "olive",
            drawableRes = R.drawable.ic_prod_olive,
            department = ProductDepartment.FRUITS,
            nameEn = "olive",
            nameHi = "जैतून",
            nameTa = "ஆலிவ்",
            keywords = listOf("jaitun", "black olive", "green olive", "spanish olive", "figaro", "food", "olive", "खाना", "जैतून", "ஆலிவ்", "உணவு")
        ),
        ProductIconItem(
            iconKey = "chestnut",
            drawableRes = R.drawable.ic_prod_chestnut,
            department = ProductDepartment.FRUITS,
            nameEn = "chestnut",
            nameHi = "चेस्टनट",
            nameTa = "செஸ்ட்நட்",
            keywords = listOf("singhada", "water chestnut", "dry chestnut", "chestnut", "plant", "चेस्टनट", "शाहबलूत", "செஸ்ட்நட்", "வறுத்த செஸ்ட்நட்")
        ),
        ProductIconItem(
            iconKey = "potato",
            drawableRes = R.drawable.ic_prod_potato,
            department = ProductDepartment.VEGETABLES,
            nameEn = "potato",
            nameHi = "आलू",
            nameTa = "உருளைக்கிழங்கு",
            keywords = listOf("aloo", "आलू", "உருளைக்கிழங்கு", "urulaikizhangu", "batata", "pahadi aloo", "baby potato", "jyoti potato", "potatoe", "food", "potato", "vegetable", "भोजन", "सब्जी", "உணவு", "உருளை", "காய்கறி")
        ),
        ProductIconItem(
            iconKey = "carrot",
            drawableRes = R.drawable.ic_prod_carrot,
            department = ProductDepartment.VEGETABLES,
            nameEn = "carrot",
            nameHi = "गाजर",
            nameTa = "கேரட்",
            keywords = listOf("gajar", "गाजर", "கேரட்", "karat", "red carrot", "orange carrot", "desi gajar", "carrot", "food", "vegetable", "भोजन", "सब्जी", "உணவு", "காய்கறி")
        ),
        ProductIconItem(
            iconKey = "onion",
            drawableRes = R.drawable.ic_prod_onion,
            department = ProductDepartment.VEGETABLES,
            nameEn = "onion",
            nameHi = "प्याज़",
            nameTa = "வெங்காயம்",
            keywords = listOf("pyaz", "प्याज़", "प्याज", "வெங்காயம்", "vengayam", "kanda", "sambhar onion", "shallots", "red onion", "spring onion", "white onion", "flavoring", "onion", "मसाला", "சுவை")
        ),
        ProductIconItem(
            iconKey = "garlic",
            drawableRes = R.drawable.ic_prod_garlic,
            department = ProductDepartment.VEGETABLES,
            nameEn = "garlic",
            nameHi = "लहसुन",
            nameTa = "பூண்டு",
            keywords = listOf("lahsun", "लहसुन", "பூண்டு", "poondu", "lasun", "peeled garlic", "desi lahsun", "garlic pods", "flavoring", "garlic", "मसाला", "சுவை")
        ),
        ProductIconItem(
            iconKey = "cucumber",
            drawableRes = R.drawable.ic_prod_cucumber,
            department = ProductDepartment.VEGETABLES,
            nameEn = "cucumber",
            nameHi = "खीरा",
            nameTa = "வெள்ளரி",
            keywords = listOf("kheera", "खीरा", "வெள்ளரிக்காய்", "vellarikkai", "kakdi", "salad cucumber", "english cucumber", "cucumber", "food", "pickle", "vegetable", "अचार", "भोजन", "सब्जी", "உணவு", "காய்கறி", "வெள்ளரி")
        ),
        ProductIconItem(
            iconKey = "broccoli",
            drawableRes = R.drawable.ic_prod_broccoli,
            department = ProductDepartment.VEGETABLES,
            nameEn = "broccoli",
            nameHi = "ब्रोकली",
            nameTa = "ப்ரொக்கோலி",
            keywords = listOf("hari gobhi", "green broccoli", "fresh broccoli", "broccoli", "wild cabbage", "जंगली पत्ता गोभी", "ब्रोकली", "ப்ரொக்கோலி", "முட்டைகோஸ்")
        ),
        ProductIconItem(
            iconKey = "leafy_greens",
            drawableRes = R.drawable.ic_prod_leafy_greens,
            department = ProductDepartment.VEGETABLES,
            nameEn = "leafy green",
            nameHi = "हरी पत्तेदार",
            nameTa = "பச்சை காய்கறி",
            keywords = listOf("palak", "पालक", "கீரை", "keerai", "spinach", "sarson ka saag", "methi", "fenugreek", "lettuce", "salad greens", "bok choy", "cabbage", "kale", "leafy green", "गोभी", "पत्ता गोभी", "बोक चॉय", "सलाद पत्ता", "हरी पत्तेदार", "கேபேஜ்", "பச்சை காய்கறி", "போக் சோய்", "முட்டைகோஸ்", "லெட்யூஸ்")
        ),
        ProductIconItem(
            iconKey = "eggplant",
            drawableRes = R.drawable.ic_prod_eggplant,
            department = ProductDepartment.VEGETABLES,
            nameEn = "eggplant",
            nameHi = "बैंगन",
            nameTa = "கத்தரிக்காய்",
            keywords = listOf("baingan", "बैंगन", "கத்தரிக்காய்", "kathirikai", "brinjal", "aubergine", "bharta baingan", "small brinjal", "eggplant", "vegetable", "बेंगन", "सब्जी", "உணவு", "காய்கறி")
        ),
        ProductIconItem(
            iconKey = "corn",
            drawableRes = R.drawable.ic_prod_corn,
            department = ProductDepartment.VEGETABLES,
            nameEn = "ear of corn",
            nameHi = "भुट्टा, मकई का भुट्टा",
            nameTa = "சோளக்கதிர்",
            keywords = listOf("makka", "मक्का", "மக்காச்சோளம்", "bhutta", "cholam", "sweet corn", "corn cobs", "american corn", "corn", "ear", "ear of corn", "maize", "maze", "भुट्टा", "भुट्टा, मकई का भुट्टा", "मकई का भुट्टा", "சோளக்கதிர்", "பாப்கார்ன்")
        ),
        ProductIconItem(
            iconKey = "hot_pepper",
            drawableRes = R.drawable.ic_prod_hot_pepper,
            department = ProductDepartment.VEGETABLES,
            nameEn = "hot pepper",
            nameHi = "तीखी मिर्च",
            nameTa = "மிளகாய் பெப்பர்",
            keywords = listOf("mirchi", "मिर्ची", "மிளகாய்", "milagai", "green chilli", "red chilli", "hari mirch", "lal mirch", "guntur mirchi", "hot", "pepper", "तीखी", "पौधा", "मिर्च", "காரம்", "சிவப்பு மிளகாய்", "பெப்பர்")
        ),
        ProductIconItem(
            iconKey = "bell_pepper",
            drawableRes = R.drawable.ic_prod_bell_pepper,
            department = ProductDepartment.VEGETABLES,
            nameEn = "bell pepper",
            nameHi = "शिमला मिर्च",
            nameTa = "பெல் பெப்பர்",
            keywords = listOf("shimla mirch", "शिमला मिर्च", "குடை மிளகாய்", "kuda milagai", "capsicum", "green capsicum", "red capsicum", "yellow capsicum", "bell pepper", "pepper", "vegetable", "पहाड़ी मिर्च", "मिर्च", "सब्ज़ी", "காய்", "பெல் பெப்பர்", "மிளகாய்")
        ),
        ProductIconItem(
            iconKey = "sweet_potato",
            drawableRes = R.drawable.ic_prod_sweet_potato,
            department = ProductDepartment.VEGETABLES,
            nameEn = "roasted sweet potato",
            nameHi = "सिके शकरकंद",
            nameTa = "வறுத்த உருளைக்கிழங்கு",
            keywords = listOf("shakarkandi", "शकरकंद", "sarkaraivalli kizhangu", "yam", "sweet yam", "potato", "roasted", "sweet", "मीठा", "रेस्टोरेंट", "सिका हुआ", "सिके आलू", "सिके शकरकंद", "உணவு", "கிழங்கு", "சர்க்கரைவள்ளிக் கிழங்கு", "வறுத்த உருளைக்கிழங்கு", "வறுத்தது")
        ),
        ProductIconItem(
            iconKey = "mushroom",
            drawableRes = R.drawable.ic_prod_mushroom,
            department = ProductDepartment.VEGETABLES,
            nameEn = "mushroom",
            nameHi = "मशरूम",
            nameTa = "காளான்",
            keywords = listOf("kukurmutta", "मशरूम", "காளான்", "kaalan", "button mushroom", "oyster mushroom", "fresh mushroom", "mushroom", "toadstool", "पौधा", "உணவு", "சூப்பர் மாரியோ", "மஷ்ரூம்")
        ),
        ProductIconItem(
            iconKey = "peanuts",
            drawableRes = R.drawable.ic_prod_peanuts,
            department = ProductDepartment.VEGETABLES,
            nameEn = "peanuts",
            nameHi = "मूँगफली",
            nameTa = "வேர்க்கடலை",
            keywords = listOf("mungfali", "मूंगफली", "வேர்க்கடலை", "kadalai", "groundnut", "raw peanuts", "shengdana", "roasted peanuts", "food", "nut", "peanut", "peanuts", "vegetable", "अखरोट", "खाना", "मूँगफली", "सब्जी", "உணவு", "கடலை", "காய்கறி")
        ),
        ProductIconItem(
            iconKey = "pumpkin",
            drawableRes = R.drawable.ic_prod_pumpkin,
            department = ProductDepartment.VEGETABLES,
            nameEn = "jack-o-lantern",
            nameHi = "जैक–ओ–लैंटर्न, कंदील",
            nameTa = "பூசணிக்காய் முகம்",
            keywords = listOf("kaddu", "कद्दू", "பூசணிக்காய்", "poosanikai", "red pumpkin", "yellow pumpkin", "sitaphal", "celebration", "halloween", "jack", "jack-o-lantern", "lantern", "कंदील", "जैक", "जैक–ओ–लैंटर्न", "जैक–ओ–लैंटर्न, कंदील", "लालटेन", "समारोह", "हैलोवीन", "கொண்டாட்டம்", "பூசணிக்காய் முகம்", "ஹேலோவின்")
        ),
        ProductIconItem(
            iconKey = "herbs",
            drawableRes = R.drawable.ic_prod_herbs,
            department = ProductDepartment.VEGETABLES,
            nameEn = "herb",
            nameHi = "जड़ी–बूटी, पत्ती",
            nameTa = "மூலிகை",
            keywords = listOf("dhaniya", "धनिया", "கொத்தமல்லி", "kothamalli", "coriander", "mint", "पुदीना", "pudina", "curry leaves", "kariveppilai", "herb", "leaf", "जड़ी–बूटी", "जड़ी–बूटी, पत्ती", "पत्ती", "இலைகள்", "செடி", "மருந்து", "மூலிகை")
        ),
        ProductIconItem(
            iconKey = "seedling",
            drawableRes = R.drawable.ic_prod_seedling,
            department = ProductDepartment.VEGETABLES,
            nameEn = "seedling",
            nameHi = "अंकुर",
            nameTa = "முளைக்கும் செடி",
            keywords = listOf("sprouts", "ankurit dal", "microgreens", "moong sprouts", "peas", "seedling", "young", "अंकुर", "युवा", "युवा पौधा", "புதிய தொடக்கம்", "முளைக்கும் செடி", "வளர்ச்சி")
        ),
        ProductIconItem(
            iconKey = "potted_plant",
            drawableRes = R.drawable.ic_prod_potted_plant,
            department = ProductDepartment.VEGETABLES,
            nameEn = "potted plant",
            nameHi = "गमले में लगा पौधा",
            nameTa = "பூந்தொட்டியில் உள்ள தாவரம்",
            keywords = listOf("kitchen garden", "pudina plant", "tulsi plant", "boring", "grow", "house", "nurturing", "plant", "potted plant", "useless", "उगना", "उबाऊ", "गमले में लगा पौधा", "पालन-पोषण", "पौधा", "बेकार", "செடி", "துளைத்தல்", "பயனற்றது", "பூந்தொட்டியில் உள்ள தாவரம்", "வளர்", "வளர்ப்பது", "வீடு")
        ),
        ProductIconItem(
            iconKey = "milk_bottle",
            drawableRes = R.drawable.ic_prod_milk_bottle,
            department = ProductDepartment.DAIRY,
            nameEn = "glass of milk",
            nameHi = "दूध का गिलास",
            nameTa = "பால்",
            keywords = listOf("doodh", "dudh", "दूध", "பால்", "paal", "pasum paal", "cow milk", "buffalo milk", "toned milk", "full cream milk", "amul taaza", "amul gold", "nandini", "mother dairy", "aavin", "drink", "glass", "glass of milk", "milk", "ग्लास", "दूध का गिलास", "पेय", "பானம்")
        ),
        ProductIconItem(
            iconKey = "cheese",
            drawableRes = R.drawable.ic_prod_cheese,
            department = ProductDepartment.DAIRY,
            nameEn = "cheese wedge",
            nameHi = "पनीर का टुकड़ा",
            nameTa = "பாலாடைக் கட்டி",
            keywords = listOf("paneer", "पनीर", "பன்னீர்", "cheese cubes", "cheese slice", "mozzarella", "cheddar", "amul cheese", "britannia cheese", "cottage cheese", "cheese", "cheese wedge", "पनीर का टुकड़ा", "சீஸ்", "பாலாடை", "பாலாடைக் கட்டி", "பாலேடு")
        ),
        ProductIconItem(
            iconKey = "butter",
            drawableRes = R.drawable.ic_prod_butter,
            department = ProductDepartment.DAIRY,
            nameEn = "butter",
            nameHi = "मक्खन",
            nameTa = "வெண்ணெய்",
            keywords = listOf("makhan", "मक्खन", "வெண்ணெய்", "vennai", "ghee", "घी", "நெய்", "ney", "amul butter", "table butter", "white butter", "salted butter", "unsalted butter", "delicious butter", "butter", "dairy", "दूध से बने पदार्थ", "பால் பொருட்கள்")
        ),
        ProductIconItem(
            iconKey = "egg",
            drawableRes = R.drawable.ic_prod_egg,
            department = ProductDepartment.DAIRY,
            nameEn = "egg",
            nameHi = "अंडा",
            nameTa = "முட்டை",
            keywords = listOf("anda", "अंडा", "முட்டை", "muttai", "farm eggs", "brown eggs", "white eggs", "desi eggs", "egg tray", "poultry eggs", "breakfast", "egg", "food", "भोजन", "உணவு")
        ),
        ProductIconItem(
            iconKey = "ice_cream",
            drawableRes = R.drawable.ic_prod_ice_cream,
            department = ProductDepartment.DAIRY,
            nameEn = "ice cream",
            nameHi = "आइसक्रीम",
            nameTa = "ஐஸ்கிரீம்",
            keywords = listOf("ice cream tub", "vanilla ice cream", "chocolate ice cream", "amul ice cream", "kwality walls", "havmor", "vadilal", "kulfi", "cream", "dessert", "ice", "sweet", "आइस", "आइसक्रीम", "क्रीम", "रेस्टोरेंट", "உணவகம்", "உணவு", "ஐஸ்கிரீம்", "ஐஸ்க்ரீம்")
        ),
        ProductIconItem(
            iconKey = "soft_ice_cream",
            drawableRes = R.drawable.ic_prod_soft_ice_cream,
            department = ProductDepartment.DAIRY,
            nameEn = "soft ice cream",
            nameHi = "सॉफ़्ट आइसक्रीम",
            nameTa = "கோன் ஐஸ்",
            keywords = listOf("cone ice cream", "cornetto", "chocobar", "soft serve", "ice cream cone", "cream", "dessert", "ice", "icecream", "soft", "sweet", "आइसक्रीम", "क्रीम", "नरम आइसक्रीम", "रेस्टोरेंट", "सॉफ़्ट आइसक्रीम", "सॉफ़्ट सर्व", "உணவகம்", "உணவு", "ஐஸ்க்ரீம்", "கோன் ஐஸ்")
        ),
        ProductIconItem(
            iconKey = "shaved_ice",
            drawableRes = R.drawable.ic_prod_shaved_ice,
            department = ProductDepartment.DAIRY,
            nameEn = "shaved ice",
            nameHi = "पीसी बर्फ़",
            nameTa = "சிரப் ஊற்றிய ஐஸ்கிரீம்",
            keywords = listOf("gola", "baraf gola", "crushed ice", "ice slush", "frozen treat", "dessert", "ice", "shaved", "sweet", "चूरा", "पीसी बर्फ़", "रेस्टोरेंट", "உணவகம்", "உணவு", "ஐஸ்க்ரீம்", "சிரப் ஊற்றிய ஐஸ்கிரீம்", "ஷேவ்ட் ஐஸ்க்ரீம்")
        ),
        ProductIconItem(
            iconKey = "baby_bottle",
            drawableRes = R.drawable.ic_prod_baby_bottle,
            department = ProductDepartment.DAIRY,
            nameEn = "baby bottle",
            nameHi = "बच्चे के दूध की बोतल",
            nameTa = "ஃபீடிங் பாட்டில்",
            keywords = listOf("baby milk", "infant milk", "nan pro", "lactogen", "similac", "feeding milk", "baby", "bottle", "drink", "milk", "दूध", "पेय", "बच्चे के दूध की बोतल", "बेबी बोतल", "बोतल", "ஃபீடிங் பாட்டில்", "சிறுபிள்ளைத்தனம்", "பாட்டில்", "பானம்")
        ),
        ProductIconItem(
            iconKey = "fondue",
            drawableRes = R.drawable.ic_prod_fondue,
            department = ProductDepartment.DAIRY,
            nameEn = "fondue",
            nameHi = "फ़ॉनडू",
            nameTa = "ஃபாண்டூ",
            keywords = listOf("cheese fondue", "curd", "dahi", "दही", "தயிர்", "thayir", "amul masti dahi", "yogurt", "greek yogurt", "cheese", "chocolate fondue", "fondue", "melted", "pot", "swiss", "पनीर", "पिघला हुआ", "फ़ॉनडू", "बर्तन", "स्विस", "உருகியது", "ஃபாண்டூ", "குவளை", "சுவிஸ்", "பாலாடைக்கட்டி")
        ),
        ProductIconItem(
            iconKey = "bread",
            drawableRes = R.drawable.ic_prod_bread,
            department = ProductDepartment.BAKERY,
            nameEn = "bread",
            nameHi = "ब्रेड",
            nameTa = "பிரெட்",
            keywords = listOf("bread", "ब्रेड", "ரொட்டி", "white bread", "brown bread", "wheat bread", "atta bread", "milk bread", "sandwich bread", "britannia bread", "modern bread", "harvest gold", "loaf", "ब्रेड लोफ़", "रेस्टोरेंट", "உணவகம்", "உணவு", "பிரெட்")
        ),
        ProductIconItem(
            iconKey = "croissant",
            drawableRes = R.drawable.ic_prod_croissant,
            department = ProductDepartment.BAKERY,
            nameEn = "croissant",
            nameHi = "क्रोइसैन",
            nameTa = "மாவுப்பண்டம்",
            keywords = listOf("butter croissant", "chocolate croissant", "french pastry", "puff", "bread", "breakfast", "croissant", "food", "french", "roll", "crescent रोल", "क्रोइसैन", "खाना", "फ्रेंच", "रोटी", "உணவு", "மாவுப் பண்டம்", "மாவுப்பண்டம்")
        ),
        ProductIconItem(
            iconKey = "baguette",
            drawableRes = R.drawable.ic_prod_baguette,
            department = ProductDepartment.BAKERY,
            nameEn = "baguette bread",
            nameHi = "बीगट ब्रेड",
            nameTa = "ரொட்டி உணவு",
            keywords = listOf("french bread", "garlic bread", "bread stick", "long bread", "baguette", "bread", "food", "french", "फ्रेंच", "बीगट ब्रेड", "भोजन", "रोटी", "உணவு", "ரொட்டி")
        ),
        ProductIconItem(
            iconKey = "flatbread",
            drawableRes = R.drawable.ic_prod_flatbread,
            department = ProductDepartment.BAKERY,
            nameEn = "flatbread",
            nameHi = "फ़्लैटब्रेड",
            nameTa = "ஃபிளாட் பிரட்",
            keywords = listOf("roti", "रोटी", "chapati", "चपाती", "naan", "नान", "kulcha", "rumali roti", "phulka", "paratha", "idiyappam", "arepa", "flatbread", "lavash", "pita", "अरेपा", "पीता", "फ़्लैटब्रेड", "लवाश", "அரேபா", "ஃபிளாட் பிரட்", "நான்", "பிடா", "லாவாஷ்")
        ),
        ProductIconItem(
            iconKey = "bagel",
            drawableRes = R.drawable.ic_prod_bagel,
            department = ProductDepartment.BAKERY,
            nameEn = "bagel",
            nameHi = "बैगल",
            nameTa = "பேகல்",
            keywords = listOf("sesame bagel", "bread ring", "donut bread", "bagel", "bakery", "breakfast", "schmear", "बेकरी", "बैगल", "श्मियर", "பேகல்", "பேக்கரி", "ஸ்மியர்")
        ),
        ProductIconItem(
            iconKey = "pretzel",
            drawableRes = R.drawable.ic_prod_pretzel,
            department = ProductDepartment.BAKERY,
            nameEn = "pretzel",
            nameHi = "प्रेटज़ेल",
            nameTa = "ப்ரெஸெல்",
            keywords = listOf("salted pretzel", "twisted bread", "baked pretzel", "pretzel", "twisted", "प्रेटज़ेल", "ப்ரெஸெல்", "முறுக்கு")
        ),
        ProductIconItem(
            iconKey = "pancake",
            drawableRes = R.drawable.ic_prod_pancake,
            department = ProductDepartment.BAKERY,
            nameEn = "pancakes",
            nameHi = "पैनकेक",
            nameTa = "பேன்கேக்",
            keywords = listOf("hotcake", "crepe", "dosa", "தோசை", "cheela", "batter cake", "breakfast", "crêpe", "food", "pancake", "pancakes", "क्रेप", "पैनकेक", "भोजन", "हॉटकेक", "உணவு", "பேன்கேக்")
        ),
        ProductIconItem(
            iconKey = "waffle",
            drawableRes = R.drawable.ic_prod_waffle,
            department = ProductDepartment.BAKERY,
            nameEn = "waffle",
            nameHi = "वफ़ल",
            nameTa = "வாஃபிள்",
            keywords = listOf("belgian waffle", "chocolate waffle", "waffle crisp", "breakfast", "indecisive", "iron", "waffle", "वफ़ल", "இரும்பு", "உறுதியற்றது", "வாஃபிள்")
        ),
        ProductIconItem(
            iconKey = "sandwich",
            drawableRes = R.drawable.ic_prod_sandwich,
            department = ProductDepartment.BAKERY,
            nameEn = "sandwich",
            nameHi = "सैंडविच",
            nameTa = "சாண்ட்விச்",
            keywords = listOf("club sandwich", "veg sandwich", "cheese sandwich", "grilled sandwich", "jumbo sandwich", "bread", "sandwich", "ब्रेड", "सैंडविच", "சாண்ட்விச்", "பிரெட்")
        ),
        ProductIconItem(
            iconKey = "pie",
            drawableRes = R.drawable.ic_prod_pie,
            department = ProductDepartment.BAKERY,
            nameEn = "pie",
            nameHi = "पाई",
            nameTa = "பை",
            keywords = listOf("apple pie", "veg pie", "tart", "baked pie", "filling", "pastry", "pie", "पाई", "पेस्ट्री", "இனிப்பு", "ஃபில்லிங்", "கேக்", "பை")
        ),
        ProductIconItem(
            iconKey = "burger_bun",
            drawableRes = R.drawable.ic_prod_burger_bun,
            department = ProductDepartment.BAKERY,
            nameEn = "hamburger",
            nameHi = "हैमबर्गर",
            nameTa = "பர்கர்",
            keywords = listOf("bun", "बर्गर", "burger bun", "pav", "ladi pav", "sweet bun", "cream bun", "burger", "hamburger", "रेस्टोरेंट", "हैमबर्गर", "உணவகம்", "உணவு", "பர்கர்", "ஹம்பர்கர்")
        ),
        ProductIconItem(
            iconKey = "hotdog_bun",
            drawableRes = R.drawable.ic_prod_hotdog_bun,
            department = ProductDepartment.BAKERY,
            nameEn = "hot dog",
            nameHi = "हॉट डॉग",
            nameTa = "ஹாட் டாக்",
            keywords = listOf("long bun", "roll", "frankie roll", "kathi roll bun", "frankfurter", "hot dog", "hotdog", "sausage", "सासीज", "सॉस", "हॉट डॉग", "இறைச்சி உணவு", "கொத்துக்கறி உணவு", "ஹாட் டாக்")
        ),
        ProductIconItem(
            iconKey = "cookie",
            drawableRes = R.drawable.ic_prod_cookie,
            department = ProductDepartment.SNACKS,
            nameEn = "cookie",
            nameHi = "कुकी",
            nameTa = "குக்கீ",
            keywords = listOf("cookie", "cookies", "chocolate cookie", "chocolate cookies", "biscuit", "biscuits", "बिस्कुट", "பிஸ்கட்", "parle-g", "parle g", "good day", "britannia", "marie gold", "monaco", "krackjack", "hide and seek", "bourbon", "dark fantasy", "oreo", "tiger", "dessert", "sweet", "कुकी", "रेस्टोरेंट", "உணவகம்", "உணவு", "குக்கீ", "ரொட்டி")
        ),
        ProductIconItem(
            iconKey = "cracker",
            drawableRes = R.drawable.ic_prod_cracker,
            department = ProductDepartment.SNACKS,
            nameEn = "beverage box",
            nameHi = "जूस का डब्बा",
            nameTa = "குடிபானப் பெட்டி",
            keywords = listOf("salted biscuit", "cream cracker", "snack cracker", "50-50", "time pass", "salt biscuits", "beverage", "box", "juice", "straw", "sweet", "जूस का डब्बा", "जूस बॉक्स", "குடிபானப் பெட்டி", "ஜூஸ் டப்பா", "ஜூஸ் பாக்ஸ்", "ஜூஸ் பெட்டி")
        ),
        ProductIconItem(
            iconKey = "popcorn",
            drawableRes = R.drawable.ic_prod_popcorn,
            department = ProductDepartment.SNACKS,
            nameEn = "popcorn",
            nameHi = "पॉपकॉर्न",
            nameTa = "பாப் காரன்",
            keywords = listOf("act ii", "act 2", "butter popcorn", "cheese popcorn", "caramel popcorn", "salted popcorn", "corn snack", "popcorn", "पॉपकॉर्न", "பாப் காரன்", "பொருட்காட்சி")
        ),
        ProductIconItem(
            iconKey = "potato_chips",
            drawableRes = R.drawable.ic_prod_potato_chips,
            department = ProductDepartment.SNACKS,
            nameEn = "french fries",
            nameHi = "फ़्रेंच फ़्राइस",
            nameTa = "ஃபிரென்ச் ஃப்ரைஸ்",
            keywords = listOf("potato chips", "crisps", "potato wafers", "chips", "wafers", "lays", "bingo", "pringles", "uncle chipps", "kurkure", "aloo wafers", "salted chips", "banana chips", "french", "fries", "फ़्राइस", "फ़्रेंच फ़्राइस", "रेस्टोरेंट", "உணவகம்", "உணவு", "ஃபிரென்ச் ஃப்ரைஸ்")
        ),
        ProductIconItem(
            iconKey = "samosa",
            drawableRes = R.drawable.ic_prod_samosa,
            department = ProductDepartment.SNACKS,
            nameEn = "dumpling",
            nameHi = "भरावनयुक्त मीठी या नमकीन पकौड़ियाँ",
            nameTa = "டம்ப்ளிங்",
            keywords = listOf("dumpling", "समौसा", "samosa", "aloo samosa", "punjabi samosa", "momo", "gujiya", "potli", "snack patty", "empanada", "gyōza", "jiaozi", "pierogi", "potsticker", "भरावनयुक्त मीठी या नमकीन पकौड़ियाँ", "க்யோஸா", "டம்ப்ளிங்", "பாட்ஸ்டிக்கர்", "ஜியாஸி")
        ),
        ProductIconItem(
            iconKey = "taco",
            drawableRes = R.drawable.ic_prod_taco,
            department = ProductDepartment.SNACKS,
            nameEn = "taco",
            nameHi = "टैको",
            nameTa = "டாகோ",
            keywords = listOf("nachos", "tortilla chips", "doritos", "corn crisps", "tacos", "mexican", "taco", "टैको", "मैक्सिकन", "உணவு", "டாகோ", "மெக்சிகன் உணவு")
        ),
        ProductIconItem(
            iconKey = "burrito",
            drawableRes = R.drawable.ic_prod_burrito,
            department = ProductDepartment.SNACKS,
            nameEn = "burrito",
            nameHi = "बरिटो",
            nameTa = "புரிட்டோ",
            keywords = listOf("wrap", "kathi roll", "veg roll", "spring roll", "frankie", "burrito", "mexican", "बरिटो", "मैक्सिकन", "உணவு", "புரிட்டோ", "மெக்சிகன் உணவு")
        ),
        ProductIconItem(
            iconKey = "stuffed_flatbread",
            drawableRes = R.drawable.ic_prod_stuffed_flatbread,
            department = ProductDepartment.SNACKS,
            nameEn = "stuffed flatbread",
            nameHi = "भरा हुआ पराठा",
            nameTa = "ரொட்டி வகை உணவு",
            keywords = listOf("aloo paratha", "stuffed paratha", "kulcha", "paneer paratha", "thepla", "khakhra", "falafel", "flatbread", "food", "gyro", "kebab", "stuffed", "कबाब", "गायरो", "चपाती", "फलाफल", "भरवां", "भरा हुआ पराठा", "भोजन", "உணவு", "ரொட்டி", "ரொட்டி வகை உணவு")
        ),
        ProductIconItem(
            iconKey = "falafel",
            drawableRes = R.drawable.ic_prod_falafel,
            department = ProductDepartment.SNACKS,
            nameEn = "falafel",
            nameHi = "फ़लाफ़ेल",
            nameTa = "வறுத்த கொண்டைக்கடலை உருண்டை",
            keywords = listOf("vada", "வடை", "medu vada", "dal vada", "tikki", "aloo tikki", "cutlet", "pakoda", "bhajji", "chickpea", "falafel", "meatball", "फ़लाफ़ेल", "கறி உருண்டை", "கொண்டைக்கடலை", "வறுத்த கொண்டைக்கடலை உருண்டை")
        ),
        ProductIconItem(
            iconKey = "sev_bhujia",
            drawableRes = R.drawable.ic_prod_sev_bhujia,
            department = ProductDepartment.SNACKS,
            nameEn = "bowl with spoon",
            nameHi = "चम्मच के साथ कटोरी",
            nameTa = "ஸ்பூன் உள்ள பாத்திரம்",
            keywords = listOf("bhujia", "sev", "भुजिया", "सेव", "namkeen", "नमकीन", "haldiram bhujia", "bikanervala", "ratlami sev", "mixture namkeen", "chivda", "bowl with spoon", "breakfast", "cereal", "congee", "अनाज", "चम्मच के साथ कटोरी", "चावल की दलिया", "नाश्ता", "காலை உணவு", "ப்ரேக்ஃபாஸ்ட்", "ஸ்பூன் உள்ள பாத்திரம்")
        ),
        ProductIconItem(
            iconKey = "chocolate",
            drawableRes = R.drawable.ic_prod_chocolate,
            department = ProductDepartment.SWEETS,
            nameEn = "chocolate bar",
            nameHi = "चॉकलेट बार",
            nameTa = "சாக்லேட்",
            keywords = listOf("chocolate", "चॉकलेट", "choclate", "cadbury", "dairy milk", "kitkat", "5 star", "munch", "perk", "snickers", "amul chocolate", "dark chocolate", "silk", "bar", "dessert", "sweet", "बार", "रेस्टोरेंट", "இனிப்பு", "உணவகம்", "சாக்லேட்", "சாக்லேட் பார்")
        ),
        ProductIconItem(
            iconKey = "candy",
            drawableRes = R.drawable.ic_prod_candy,
            department = ProductDepartment.SWEETS,
            nameEn = "candy",
            nameHi = "कैंडी",
            nameTa = "மிட்டாய்",
            keywords = listOf("candy", "toffee", "टाॅफी", "pulse", "alpenliebe", "melody", "kopiko", "mango bite", "eclairs", "mithai", "dessert", "sweet", "कैंडी", "रेस्टोरेंट", "இனிப்பு", "கேன்டி", "மிட்டாய்")
        ),
        ProductIconItem(
            iconKey = "lollipop",
            drawableRes = R.drawable.ic_prod_lollipop,
            department = ProductDepartment.SWEETS,
            nameEn = "lollipop",
            nameHi = "लॉलीपॉप",
            nameTa = "குச்சிமிட்டாய்",
            keywords = listOf("chupa chups", "lollypop", "candy stick", "sweet pop", "candy", "dessert", "lollipop", "sweet", "कैंडी", "रेस्टोरेंट", "लॉलीपॉप", "இனிப்பு", "குச்சிமிட்டாய்", "மிட்டாய்", "லாலிபாப்")
        ),
        ProductIconItem(
            iconKey = "cupcake",
            drawableRes = R.drawable.ic_prod_cupcake,
            department = ProductDepartment.SWEETS,
            nameEn = "cupcake",
            nameHi = "कपकेक",
            nameTa = "கப்கேக்",
            keywords = listOf("muffin", "winkies", "bar cake", "britannia cake", "cup cake", "bakery", "cupcake", "sweet", "कपकेक", "बेकरी", "मिठाई", "मिष्ठान्न", "இனிப்பு", "கப்கேக்", "பேக்கரி")
        ),
        ProductIconItem(
            iconKey = "birthday_cake",
            drawableRes = R.drawable.ic_prod_birthday_cake,
            department = ProductDepartment.SWEETS,
            nameEn = "birthday cake",
            nameHi = "जन्मदिन का केक",
            nameTa = "பிறந்தநாள் கேக்",
            keywords = listOf("cake", "केक", "black forest", "chocolate cake", "pineapple cake", "pastry cake", "birthday cake", "birthday", "celebration", "dessert", "pastry", "sweet", "जन्मदिन", "जन्मदिन का केक", "समारोह", "கேக்", "கொண்டாட்டம்", "பிறந்தநாள்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "shortcake",
            drawableRes = R.drawable.ic_prod_shortcake,
            department = ProductDepartment.SWEETS,
            nameEn = "shortcake",
            nameHi = "शॉर्टकेक",
            nameTa = "கேக் துண்டு",
            keywords = listOf("pastry", "पेस्ट्री", "strawberry cake", "cream slice", "sponge cake", "cake", "dessert", "shortcake", "slice", "sweet", "केक", "केक स्लाइस", "रेस्टोरेंट", "शॉर्टकेक", "உணவகம்", "கேக்", "கேக் துண்டு", "கேக் பீஸ்")
        ),
        ProductIconItem(
            iconKey = "pudding",
            drawableRes = R.drawable.ic_prod_pudding,
            department = ProductDepartment.SWEETS,
            nameEn = "custard",
            nameHi = "कस्टर्ड",
            nameTa = "கஸ்டர்டு",
            keywords = listOf("custard", "kheer", "खीर", "payasam", "பாயாசம்", "caramel pudding", "falooda", "dessert", "pudding", "sweet", "कस्टर्ड", "पुडिंग", "रेस्टोरेंट", "இனிப்பு", "உணவகம்", "கஸ்டர்டு")
        ),
        ProductIconItem(
            iconKey = "donut",
            drawableRes = R.drawable.ic_prod_donut,
            department = ProductDepartment.SWEETS,
            nameEn = "doughnut",
            nameHi = "डोनट",
            nameTa = "டோனட்",
            keywords = listOf("doughnut", "chocolate donut", "glazed donut", "modak", "मोदक", "breakfast", "dessert", "donut", "sweet", "डोनट", "रेस्टोरेंट", "இனிப்பு", "உணவகம்", "உணவு", "டோனட்")
        ),
        ProductIconItem(
            iconKey = "honey",
            drawableRes = R.drawable.ic_prod_honey,
            department = ProductDepartment.SWEETS,
            nameEn = "honey & sugar",
            nameHi = "शहद और चीनी",
            nameTa = "தேன் மற்றும் சர்க்கரை",
            keywords = listOf("sugar", "cheeni", "चीनी", "sakkarai", "சர்க்கரை", "vellam", "வெல்லம்", "jaggery", "gur", "गुड़", "crystal sugar", "white crystal sugar", "white sugar", "cane sugar", "brown sugar", "sugar packet", "shahad", "शहद", "தேன்", "then", "dabur honey", "patanjali honey", "pure honey", "raw honey", "wild honey", "honey", "honeypot", "pot", "sweet", "रेस्टोरेंट", "शहद पात्र", "हनीपॉट", "இனிப்பு", "உணவகம்", "தேன் பானை", "ஹனிபாட்")
        ),
        ProductIconItem(
            iconKey = "ladoo",
            drawableRes = R.drawable.ic_prod_ladoo,
            department = ProductDepartment.SWEETS,
            nameEn = "dango",
            nameHi = "डैंगो",
            nameTa = "டேங்கோ",
            keywords = listOf("mithai", "मिठाई", "motichoor ladoo", "लड्डू", "besan ladoo", "gulab jamun", "गुलाब जामुन", "rasgulla", "रसगुल्ला", "peda", "barfi", "haldiram sweets", "jalebi", "ஜிலேபி", "dango", "dessert", "japanese", "skewer", "stick", "sweet", "डैंगो", "मोची", "मोची बॉल", "मोची बॉल स्टिक", "रेस्टोरेंट", "உணவகம்", "உணவு", "குச்சியில் உருண்டைகள்", "டேங்கோ", "ஜப்பானிய உணவு")
        ),
        ProductIconItem(
            iconKey = "moon_cake",
            drawableRes = R.drawable.ic_prod_moon_cake,
            department = ProductDepartment.SWEETS,
            nameEn = "moon cake",
            nameHi = "मून केक",
            nameTa = "மூன் கேக்",
            keywords = listOf("sweet cake", "baklava", "traditional sweet", "sandesh", "autumn", "festival", "moon cake", "yuèbǐng", "त्यौहार", "पतझड़", "मून केक", "मूनकेक", "இலையுதிர்காலம்", "கேக்", "திருவிழா", "மூன் கேக்")
        ),
        ProductIconItem(
            iconKey = "fortune_cookie",
            drawableRes = R.drawable.ic_prod_fortune_cookie,
            department = ProductDepartment.SWEETS,
            nameEn = "fortune cookie",
            nameHi = "फॉर्चून कुकी",
            nameTa = "ஃபார்ச்சூன் குக்கீ",
            keywords = listOf("crisp sweet", "cookie treat", "fortune cookie", "prophecy", "फॉर्चून कुकी", "ஃபார்ச்சூன் குக்கீ", "குக்கீ", "ஜோசியம்")
        ),
        ProductIconItem(
            iconKey = "cooked_rice",
            drawableRes = R.drawable.ic_prod_cooked_rice,
            department = ProductDepartment.STAPLES,
            nameEn = "cooked rice",
            nameHi = "पके हुए चावल",
            nameTa = "சோறு",
            keywords = listOf("chawal", "चावल", "அரிசி", "arisi", "bhat", "steamed rice", "boiled rice", "biryani rice", "rice", "cooked", "पके हुए चावल", "रेस्टोरेंट", "உணவகம்", "உணவு", "சோறு")
        ),
        ProductIconItem(
            iconKey = "sheaf_of_rice",
            drawableRes = R.drawable.ic_prod_sheaf_of_rice,
            department = ProductDepartment.STAPLES,
            nameEn = "sheaf of rice",
            nameHi = "चावल का पौधा",
            nameTa = "நெற்கதிர்",
            keywords = listOf("बासमती चावल", "பொன்னி அரிசி", "til", "sesame", "kala til", "ellu", "எள்ளு", "basmati", "raw rice", "sona masoori", "kolam", "ponni arisi", "daawat", "india gate", "fortune rice", "brown rice", "idli rice", "dal", "दाल", "பருப்பு", "paruppu", "toor dal", "moong dal", "urad dal", "chana dal", "rajma", "ear", "grain", "rice", "sheaf of rice", "चावल", "चावल का पौधा", "चावल की बाली", "बाली", "அரிசி", "அறுவடை", "நெற்கதிர்", "வயல்")
        ),
        ProductIconItem(
            iconKey = "bowl_flour",
            drawableRes = R.drawable.ic_prod_bowl_flour,
            department = ProductDepartment.STAPLES,
            nameEn = "bowl with spoon",
            nameHi = "चम्मच के साथ कटोरी",
            nameTa = "ஸ்பூன் உள்ள பாத்திரம்",
            keywords = listOf("atta", "आटा", "maida", "मैदा", "sooji", "सूजी", "rava", "ரவை", "besan", "बेसन", "aashirvaad", "wheat flour", "गोहूं का आटा", "poha", "पोहा", "aval", "dalia", "cornflour", "gehu ka atta", "bowl with spoon", "breakfast", "cereal", "congee", "अनाज", "चम्मच के साथ कटोरी", "चावल की दलिया", "नाश्ता", "காலை உணவு", "ப்ரேக்ஃபாஸ்ட்", "ஸ்பூன் உள்ள பாத்திரம்")
        ),
        ProductIconItem(
            iconKey = "noodles",
            drawableRes = R.drawable.ic_prod_noodles,
            department = ProductDepartment.STAPLES,
            nameEn = "steaming bowl",
            nameHi = "भाप वाली बाउल",
            nameTa = "ஆவி பறக்கும் உணவு",
            keywords = listOf("noodles", "maggi noodles", "masala noodles", "maggi", "मैगी", "yippee", "top ramen", "hakka noodles", "instant noodles", "chowmein", "cup noodles", "wai wai", "ramen", "sevai", "semiya", "bowl", "noodle", "steaming", "उबले नूडल", "नूडल", "बाउल", "भाप", "भाप वाली बाउल", "रेस्टोरेंट", "रैमन", "ஆவி பறக்கும் உணவு", "உணவகம்", "உணவு", "சூப்", "நூடூல்ஸ்")
        ),
        ProductIconItem(
            iconKey = "spaghetti",
            drawableRes = R.drawable.ic_prod_spaghetti,
            department = ProductDepartment.STAPLES,
            nameEn = "spaghetti",
            nameHi = "स्पैगेटी",
            nameTa = "ஸ்பாகெட்டி",
            keywords = listOf("pasta", "பாஸ்தா", "macaroni", "penne", "fusilli", "maggi pazzta", "durum wheat pasta", "spaghetti", "पास्ता", "रेस्टोरेंट", "सुधिरा", "स्पैगेटी", "உணவகம்", "உணவு", "தக்காளி நூடூல்ஸ்", "ஸ்பாகெட்டி")
        ),
        ProductIconItem(
            iconKey = "canned_food",
            drawableRes = R.drawable.ic_prod_canned_food,
            department = ProductDepartment.STAPLES,
            nameEn = "canned food",
            nameHi = "डिब्बा बंद खाद्य सामग्री",
            nameTa = "கேனில் அடைக்கப்பட்ட உணவு",
            keywords = listOf("tin can", "canned beans", "canned sweet corn", "canned pineapple", "canned mushroom", "can", "canned food", "कैन", "टिन", "डिब्बा", "डिब्बा बंद खाद्य सामग्री", "உணவு", "கேனில் அடைக்கப்பட்ட உணவு", "கேன்", "பதப்படுத்திய உணவு")
        ),
        ProductIconItem(
            iconKey = "takeout_box",
            drawableRes = R.drawable.ic_prod_takeout_box,
            department = ProductDepartment.STAPLES,
            nameEn = "takeout box",
            nameHi = "टेकआउट बॉक्स",
            nameTa = "சாப்பாடுப் பெட்டி",
            keywords = listOf("meal box", "packed food", "ready to eat", "mtr", "kitchens of india", "oyster pail", "takeout box", "टेकआउट बॉक्स", "கேரியர்", "சாப்பாடுப் பெட்டி", "டிபன் கேரியர்")
        ),
        ProductIconItem(
            iconKey = "bento_box",
            drawableRes = R.drawable.ic_prod_bento_box,
            department = ProductDepartment.STAPLES,
            nameEn = "bento box",
            nameHi = "बैंटो, बैंटो बॉक्स",
            nameTa = "பென்டோ பாக்ஸ்",
            keywords = listOf("thali", "lunch pack", "combo meal", "tiffin", "bento", "box", "बैंटो", "बैंटो, बैंटो बॉक्स", "बॉक्स", "रेस्टोरेंट", "உணவகம்", "பெட்டி", "பென்டோ பாக்ஸ்")
        ),
        ProductIconItem(
            iconKey = "salt",
            drawableRes = R.drawable.ic_prod_salt,
            department = ProductDepartment.SPICES_OILS,
            nameEn = "salt",
            nameHi = "नमक",
            nameTa = "உப்பு",
            keywords = listOf("namak", "नमक", "உப்பு", "uppu", "tata salt", "iodized salt", "rock salt", "sendha namak", "black salt", "kala namak", "table salt", "condiment", "salt", "shaker", "मसाला", "शेकर", "சுவையூட்டி", "ஷேக்கர்")
        ),
        ProductIconItem(
            iconKey = "oil_drop",
            drawableRes = R.drawable.ic_prod_oil_drop,
            department = ProductDepartment.SPICES_OILS,
            nameEn = "drop of blood",
            nameHi = "ख़ून की बूँद",
            nameTa = "இரத்தத் துளி",
            keywords = listOf("oil", "cooking oil", "tel", "तेल", "எண்ணெய்", "ennai", "nallennai", "நல்லெண்ணெய்", "sesame oil", "gingelly oil", "gingelly", "idhayam", "sunlite", "refined oil", "mustard oil", "sarson tel", "सरसों का तेल", "sunflower oil", "fortune oil", "dhara", "saffola", "gemini", "coconut oil", "nariyal tel", "தேங்காய் எண்ணெய்", "parachute oil", "bleed", "blood donation", "drop of blood", "injury", "medicine", "menstruation", "ख़ून", "ख़ून की बूँद", "चिकित्सा", "रक्त", "रक्तदान", "இரத்த தானம்", "இரத்தத் துளி", "மருந்துவம்", "மாதவிடாய்")
        ),
        ProductIconItem(
            iconKey = "water_drop",
            drawableRes = R.drawable.ic_prod_water_drop,
            department = ProductDepartment.SPICES_OILS,
            nameEn = "droplet",
            nameHi = "ठंडा पसीना",
            nameTa = "வியர்வை",
            keywords = listOf("groundnut oil", "kadugu ennai", "sesame oil", "gingelly oil", "til oil", "deepam oil", "cold", "comic", "drop", "droplet", "sweat", "कॉमिक", "ठंडा पसीना", "पसीना", "पसीनेदार", "குளிர்", "துளி", "வியர்வை")
        ),
        ProductIconItem(
            iconKey = "curry_rice",
            drawableRes = R.drawable.ic_prod_curry_rice,
            department = ProductDepartment.SPICES_OILS,
            nameEn = "curry rice",
            nameHi = "करी–चावल",
            nameTa = "சோறும் குழம்பும்",
            keywords = listOf("haldi", "हल्दी", "manjal", "மஞ்சள்", "turmeric", "masala", "मसाला", "மசாலா", "curry powder", "garam masala", "गरम मसाला", "everest masala", "mdh", "catch spices", "sambar powder", "rasam powder", "biryani masala", "chana masala", "curry", "rice", "कड़ी", "कड़ी–चावल", "करी–चावल", "चावल", "रेस्टोरेंट", "உணவகம்", "குழம்பு", "சாப்பாடு", "சோறு", "சோறும் குழம்பும்")
        ),
        ProductIconItem(
            iconKey = "tea_cup",
            drawableRes = R.drawable.ic_prod_tea_cup,
            department = ProductDepartment.BEVERAGES,
            nameEn = "hot beverage",
            nameHi = "कॉफ़ी, भाप वाला गर्म पेय",
            nameTa = "சூடான பானம்",
            keywords = listOf("tea", "chai", "चाय", "டீ", "theeneer", "theylai", "தேயிலை", "தேநீர்", "தேநீர் தூள்", "tata tea", "red label", "taj mahal", "wagh bakri", "brooke bond", "society tea", "tea powder", "chai patti", "चाय पत्ती", "டீ தூள்", "leaf tea", "dust tea", "coffee", "कॉफ़ी", "காபி", "kaapi", "bru", "nescafe", "filter coffee", "beverage", "drink", "hot", "steaming", "कॉफ़ी, भाप वाला गर्म पेय", "गर्म पेय", "भाप वाला गर्म पेय", "சூடான காபி", "சூடான பானம்", "பானம்")
        ),
        ProductIconItem(
            iconKey = "green_tea",
            drawableRes = R.drawable.ic_prod_green_tea,
            department = ProductDepartment.BEVERAGES,
            nameEn = "teacup without handle",
            nameHi = "चाय का प्याला, चाय",
            nameTa = "கைப்பிடி இல்லாத தேனீர் கோப்பை",
            keywords = listOf("green tea", "green tea bags", "tea bags", "tea bag", "tetley", "lipton green tea", "herbal tea", "chamomile", "tulsi tea", "organic india", "lemon tea", "beverage", "cup", "drink", "tea", "teacup", "teacup without handle", "चाय", "चाय का प्याला", "चाय का प्याला, चाय", "पेय", "கைப்பிடி இல்லாத தேனீர் கோப்பை", "டீ", "தேனீர்", "தேனீர்க்கோப்பை", "பானம்")
        ),
        ProductIconItem(
            iconKey = "teapot",
            drawableRes = R.drawable.ic_prod_teapot,
            department = ProductDepartment.BEVERAGES,
            nameEn = "teapot",
            nameHi = "चाय की केतली",
            nameTa = "தேநீர்க் குவளை",
            keywords = listOf("tea leaves", "brewed tea", "assam tea", "darjeeling tea", "kettle", "drink", "pot", "tea", "teapot", "चाय", "चाय की केतली", "पीने की चीज़", "बर्तन", "குவளை", "தேநீர்", "தேநீர்க் குவளை", "பானம்")
        ),
        ProductIconItem(
            iconKey = "mate_drink",
            drawableRes = R.drawable.ic_prod_mate_drink,
            department = ProductDepartment.BEVERAGES,
            nameEn = "mate",
            nameHi = "मेट",
            nameTa = "மேட்",
            keywords = listOf("herbal infusion", "ayurvedic drink", "kashayam", "kadha", "drink", "mate", "पेय पदार्थ", "मेट", "குளிர்பானம்", "பானம்", "மேட்", "ஜூஸ்")
        ),
        ProductIconItem(
            iconKey = "bubble_tea",
            drawableRes = R.drawable.ic_prod_bubble_tea,
            department = ProductDepartment.BEVERAGES,
            nameEn = "bubble tea",
            nameHi = "बुलबुला चाय",
            nameTa = "குமிழித் தேநீர்",
            keywords = listOf("boba", "milk tea", "shake", "thick shake", "horlicks", "bournvita", "boost", "complan", "bubble", "milk", "pearl", "tea", "चाय", "दूध", "बुलबुला", "குமிழி", "குமிழித் தேநீர்", "தேநீர்", "பால்", "முத்து")
        ),
        ProductIconItem(
            iconKey = "cup_with_straw",
            drawableRes = R.drawable.ic_prod_cup_with_straw,
            department = ProductDepartment.BEVERAGES,
            nameEn = "cup with straw",
            nameHi = "स्ट्रॉ के साथ कप",
            nameTa = "ஸ்ட்ரா உள்ள கோப்பை",
            keywords = listOf("soft drink", "coca cola", "pepsi", "thums up", "sprite", "fanta", "mirinda", "limca", "frooti", "maaza", "slice", "real juice", "tropicana", "sting", "red bull", "cup with straw", "juice", "soda", "जूस", "सोडा", "स्ट्रॉ के साथ कप", "சோடா", "பழச்சாறு", "ஜூஸ்", "ஸ்ட்ரா உள்ள கோப்பை")
        ),
        ProductIconItem(
            iconKey = "beer_mug",
            drawableRes = R.drawable.ic_prod_beer_mug,
            department = ProductDepartment.BEVERAGES,
            nameEn = "beer mug",
            nameHi = "बियर, बियर मग",
            nameTa = "பீர் குவளை",
            keywords = listOf("beer", "lager", "draught beer", "malt beverage", "bar", "drink", "mug", "बार", "बियर", "बियर, बियर मग", "मग", "रेस्टोरेंट", "உணவகம்", "குவளை", "பீர்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "beer_bottles",
            drawableRes = R.drawable.ic_prod_beer_bottles,
            department = ProductDepartment.BEVERAGES,
            nameEn = "clinking beer mugs",
            nameHi = "बियर के दो मग",
            nameTa = "பீர் குவளைகள்",
            keywords = listOf("craft beer", "chilled beer", "bottled beer", "bar", "beer", "clink", "clinking beer mugs", "drink", "mug", "दो मग", "बार", "बियर", "बियर के दो मग", "बियर के मग", "मग", "रेस्टोरेंट", "உணவகம்", "குவளைகள்", "பீர்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "wine_glass",
            drawableRes = R.drawable.ic_prod_wine_glass,
            department = ProductDepartment.BEVERAGES,
            nameEn = "wine glass",
            nameHi = "शराब का गिलास",
            nameTa = "மதுக்கோப்பை",
            keywords = listOf("red wine", "white wine", "sula", "grape wine", "bar", "beverage", "drink", "glass", "wine", "गिलास", "पेय", "बार", "वाइन", "शराब", "शराब का गिलास", "உணவகம்", "கோப்பை", "பானம்", "மது", "மதுக்கோப்பை")
        ),
        ProductIconItem(
            iconKey = "champagne",
            drawableRes = R.drawable.ic_prod_champagne,
            department = ProductDepartment.BEVERAGES,
            nameEn = "bottle with popping cork",
            nameHi = "कॉक खुलती हुई बोतल",
            nameTa = "பீச்சடிக்கும் பாட்டில்",
            keywords = listOf("sparkling drink", "champagne", "celebration drink", "fizzy wine", "bar", "bottle", "bottle with popping cork", "cork", "drink", "popping", "कॉक खुलती हुई बोतल", "जश्न", "पार्टी", "கார்க்", "பீச்சடிக்கும் பாட்டில்", "பீச்சியடி")
        ),
        ProductIconItem(
            iconKey = "cocktail",
            drawableRes = R.drawable.ic_prod_cocktail,
            department = ProductDepartment.BEVERAGES,
            nameEn = "cocktail glass",
            nameHi = "कॉकटेल गिलास",
            nameTa = "காக்டெயில் கிளாஸ்",
            keywords = listOf("mocktail", "fruit punch", "mixed juice", "syrup drink", "bar", "cocktail", "drink", "glass", "कॉकटेल", "कॉकटेल गिलास", "बार", "रेस्टोरेंट", "உணவகம்", "காக்டெயில்", "காக்டெயில் கிளாஸ்", "பானம்", "மதுக்கலவை")
        ),
        ProductIconItem(
            iconKey = "tropical_drink",
            drawableRes = R.drawable.ic_prod_tropical_drink,
            department = ProductDepartment.BEVERAGES,
            nameEn = "tropical drink",
            nameHi = "ट्रॉपिकल पेय",
            nameTa = "பழச்சாறு",
            keywords = listOf("lemonade", "nimbu soda", "rooh afza", "rasna", "glucon-d", "tang", "sharbat", "bar", "drink", "tropical", "ऊष्णकटिबंधीय", "ट्रॉपिकल पेय", "पेय", "बार", "रेस्टोरेंट", "உணவகம்", "பழச்சாறு", "பானம்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "ice_cube",
            drawableRes = R.drawable.ic_prod_ice_cube,
            department = ProductDepartment.BEVERAGES,
            nameEn = "ice",
            nameHi = "आइस क्यूब",
            nameTa = "ஐஸ் கட்டி",
            keywords = listOf("ice pack", "mineral water", "drinking water", "bisleri", "kinley", "aquafina", "bailley", "cold", "ice", "ice cube", "iceberg", "आइस क्यूब", "आइसबर्ग", "ठंडा", "बर्फ़", "ஐஸ்", "ஐஸ் கட்டி")
        ),
        ProductIconItem(
            iconKey = "poultry_leg",
            drawableRes = R.drawable.ic_prod_poultry_leg,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "poultry leg",
            nameHi = "चिकन लेग",
            nameTa = "சிக்கன் லெக்பீஸ்",
            keywords = listOf("chicken", "चिकन", "சிக்கன்", "kozhi", "murgh", "chikken", "curry cut", "chicken curry cut", "chicken drumstick", "chicken breast", "boneless chicken", "broiler", "bone", "drumstick", "leg", "poultry", "चिकन लेग", "मुर्गी की टाँग", "मुर्गीपालन", "எலும்புத்துண்டு", "கோழிக்கறி", "சிக்கன் லெக்பீஸ்", "லெக் பீஸ்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "cut_of_meat",
            drawableRes = R.drawable.ic_prod_cut_of_meat,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "cut of meat",
            nameHi = "गोश्त का टुकड़ा",
            nameTa = "இறைச்சி",
            keywords = listOf("mutton", "मटन", "gosht", "aattu iraichi", "goat meat", "lamb", "buff", "pork", "beef", "keema", "minced meat", "chop", "cut of meat", "lambchop", "porkchop", "steak", "गोश्त का टुकड़ा", "चॉप", "पोर्कचॉप", "लैम्बचॉप", "स्टीक", "ஆட்டிறைச்சி", "இறைச்சி", "இறைச்சித் துண்டு", "பன்றி இறைச்சி")
        ),
        ProductIconItem(
            iconKey = "bacon",
            drawableRes = R.drawable.ic_prod_bacon,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "bacon",
            nameHi = "बेकन",
            nameTa = "பன்றி இறைச்சி",
            keywords = listOf("cured meat", "ham", "sausage", "salami", "bacon", "breakfast", "food", "meat", "बेकन", "भोजन", "मांस", "அசைவம்", "இறைச்சி", "உணவு", "பன்றி")
        ),
        ProductIconItem(
            iconKey = "fish",
            drawableRes = R.drawable.ic_prod_fish,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "fish",
            nameHi = "मछली",
            nameTa = "மீன்",
            keywords = listOf("machli", "मछली", "மீன்", "meen", "rohu", "katla", "hilsa", "pomfret", "surmai", "seer fish", "salmon", "fresh fish", "fish", "pisces", "zodiac", "पशु", "நீர்வாழ் உயிரினம்", "விலங்கினம்")
        ),
        ProductIconItem(
            iconKey = "fish_cake",
            drawableRes = R.drawable.ic_prod_fish_cake,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "fish cake with swirl",
            nameHi = "चक्करदार फ़िश केक",
            nameTa = "சுருள் வடிவ ஃபிஷ் கேக்",
            keywords = listOf("fish fingers", "fish cutlet", "fish fillet", "cake", "fish", "fish cake with swirl", "pastry", "swirl", "चक्करदार फ़िश केक", "फ़िश केक", "मछली", "रेस्टोरेंट", "உணவகம்", "ஃபிஷ் கேக்", "கடல் உணவு", "சுருள் வடிவ ஃபிஷ் கேக்")
        ),
        ProductIconItem(
            iconKey = "shrimp",
            drawableRes = R.drawable.ic_prod_shrimp,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "shrimp",
            nameHi = "झींगा",
            nameTa = "கூனிறால்",
            keywords = listOf("prawns", "झींगा", "இறால்", "jheenga", "eraal", "tiger prawns", "white prawns", "cleaned prawns", "food", "shellfish", "shrimp", "small", "छोटा", "भोजन", "शंख", "உணவு", "கூனிறால்", "மீன்")
        ),
        ProductIconItem(
            iconKey = "crab",
            drawableRes = R.drawable.ic_prod_crab,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "crab",
            nameHi = "कर्कट",
            nameTa = "நண்டு",
            keywords = listOf("kankda", "நண்டு", "nandu", "fresh crab", "mud crab", "cancer", "crab", "zodiac", "कर्क राशि", "कर्कट", "केकड़ा", "கடக ராசி", "கடகம்")
        ),
        ProductIconItem(
            iconKey = "lobster",
            drawableRes = R.drawable.ic_prod_lobster,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "lobster",
            nameHi = "झींगा मछली",
            nameTa = "கடல் நண்டு",
            keywords = listOf("king lobster", "crayfish", "seafood", "bisque", "claws", "lobster", "झींगा मछली", "पंजा", "शोरबा", "समुद्री भोजन", "கடல் உணவு", "கடல் நண்டு", "நகங்கள்", "பிஸ்கே")
        ),
        ProductIconItem(
            iconKey = "squid",
            drawableRes = R.drawable.ic_prod_squid,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "squid",
            nameHi = "स्क्विड",
            nameTa = "கணவாய்",
            keywords = listOf("calamari", "kanava", "octopus", "squid rings", "food", "molusc", "squid", "भोजन", "सीप", "स्क्विड", "உணவு", "கணவாய்")
        ),
        ProductIconItem(
            iconKey = "oyster",
            drawableRes = R.drawable.ic_prod_oyster,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "oyster",
            nameHi = "सीप",
            nameTa = "சிப்பி உணவு வகை",
            keywords = listOf("clams", "mussels", "shellfish", "diving", "oyster", "pearl", "गोताखोरी", "घोंघा", "मोती", "सीप", "கடல் வாழ் உயிரினம்", "சிப்பி உணவு வகை", "முத்து")
        ),
        ProductIconItem(
            iconKey = "bone",
            drawableRes = R.drawable.ic_prod_bone,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "bone",
            nameHi = "हड्डी",
            nameTa = "எலும்புத்துண்டு",
            keywords = listOf("soup bone", "marrow bone", "meat bones", "bone", "skeleton", "कंकाल", "हड्डी", "எலும்புக்கூடு", "எலும்புத்துண்டு")
        ),
        ProductIconItem(
            iconKey = "chicken_whole",
            drawableRes = R.drawable.ic_prod_chicken_whole,
            department = ProductDepartment.MEAT_FISH,
            nameEn = "chicken",
            nameHi = "मुर्गी",
            nameTa = "கோழிக்குஞ்சு",
            keywords = listOf("whole chicken", "desi murgi", "country chicken", "naatu kozhi", "bird", "chicken", "पशु", "मुर्गी", "கோழிக்குஞ்சு", "பறவை")
        ),
        ProductIconItem(
            iconKey = "broom",
            drawableRes = R.drawable.ic_prod_broom,
            department = ProductDepartment.CLEANING,
            nameEn = "broom",
            nameHi = "झाड़ू",
            nameTa = "துடைப்பம்",
            keywords = listOf("jhadu", "झाड़ू", "துடைப்பம்", "thodappam", "phool jhadu", "grass broom", "floor broom", "wiper", "mop", "broom", "cleaning", "sweeping", "witch", "जादूगरनी", "झाडू निकालना", "सफ़ाई करना", "சுத்தம்", "சூனியக்காரி", "துப்புரவு")
        ),
        ProductIconItem(
            iconKey = "soap_bar",
            drawableRes = R.drawable.ic_prod_soap_bar,
            department = ProductDepartment.CLEANING,
            nameEn = "soap & detergent bar",
            nameHi = "साबुन और डिटर्जेंट बार",
            nameTa = "சோப்பு மற்றும் சோப் பார்",
            keywords = listOf("detergent", "surf", "vim", "ariel", "tide", "washing powder", "detergent powder", "tide plus", "tide powder", "soap", "sabun", "साबुन", "சோப்பு", "surf excel", "vim bar", "rin soap", "surf excel bar", "dishwash bar", "detergent cake", "exo bar", "tide bar", "ariel bar", "bar", "bathing", "cleaning", "lather", "soapdish", "नहाना", "बार", "सफ़ाई करना", "साबुन का झाग", "सोपडिश", "குளியல்", "சுத்தம்", "சோப்", "சோப்பு டப்பா", "நுரை")
        ),
        ProductIconItem(
            iconKey = "lotion_bottle",
            drawableRes = R.drawable.ic_prod_lotion_bottle,
            department = ProductDepartment.CLEANING,
            nameEn = "liquid detergent & lotion",
            nameHi = "लिक्विड डिटर्जेंट और लोशन",
            nameTa = "திரவ சோப்பு மற்றும் லோஷன்",
            keywords = listOf("detergent", "surf", "vim", "ariel", "tide", "washing powder", "liquid detergent", "surf excel liquid", "ariel liquid", "vim liquid", "vim gel", "vim dishwash", "dishwash liquid", "dishwash gel", "pril", "lizol", "harpic", "colin", "comfort", "floor cleaner", "toilet cleaner", "dettol liquid", "lotion", "lotion bottle", "moisturizer", "shampoo", "sunscreen", "मॉइश्चरॉइज़र", "लोशन", "लोशन की बोतल", "शैंपू", "सनस्क्रीन", "சன்ஸ்கிரீன்", "மாய்ஸ்சரைசர்", "லோஷன்", "லோஷன் பாட்டில்", "ஷாம்பு")
        ),
        ProductIconItem(
            iconKey = "sponge",
            drawableRes = R.drawable.ic_prod_sponge,
            department = ProductDepartment.CLEANING,
            nameEn = "sponge & scrubber",
            nameHi = "स्पंज और स्क्रबर",
            nameTa = "ஸ்பான்ஞ் மற்றும் ஸ்க்ரப்பர்",
            keywords = listOf("scrub pad", "scotch brite", "dishwash sponge", "steel scrubber", "cleaning scrub", "foam pad", "sponge", "scrubber", "absorbing", "cleaning", "porous", "छिद्रयुक्त", "सफ़ाई", "सोखना", "स्पंज", "உறிஞ்சுதல்", "சுத்தம்", "துளைகள்", "ஸ்பான்ஞ்")
        ),
        ProductIconItem(
            iconKey = "bucket",
            drawableRes = R.drawable.ic_prod_bucket,
            department = ProductDepartment.CLEANING,
            nameEn = "bucket",
            nameHi = "बाल्टी",
            nameTa = "வாளி",
            keywords = listOf("balti", "बाल्टी", "plastic bucket", "mug", "cleaning pail", "water bucket", "bucket", "cask", "pail", "vat", "टंकी", "डोल", "पीपा", "பக்கெட்", "பீப்பாய்", "வாளி")
        ),
        ProductIconItem(
            iconKey = "toilet_paper",
            drawableRes = R.drawable.ic_prod_toilet_paper,
            department = ProductDepartment.CLEANING,
            nameEn = "roll of paper",
            nameHi = "पेपर का रोल",
            nameTa = "பேப்பர் ரோல்",
            keywords = listOf("tissue paper", "kitchen roll", "toilet roll", "paper napkin", "serviette", "face tissue", "origami", "paper towels", "roll of paper", "toilet paper", "टॉयलेट पेपर", "पेपर का रोल", "पेपर टॉवेल", "டாய்லெட் பேப்பர்", "பேப்பர் டவல்", "பேப்பர் ரோல்")
        ),
        ProductIconItem(
            iconKey = "wastebasket",
            drawableRes = R.drawable.ic_prod_wastebasket,
            department = ProductDepartment.CLEANING,
            nameEn = "wastebasket",
            nameHi = "कचरा पेटी",
            nameTa = "குப்பைத் தொட்டி",
            keywords = listOf("dustbin", "garbage bags", "trash bags", "waste bin", "wastebasket", "कचरा पेटी", "रद्दी की टोकरी", "குப்பைக் கூடை", "குப்பைத் தொட்டி")
        ),
        ProductIconItem(
            iconKey = "plunger",
            drawableRes = R.drawable.ic_prod_plunger,
            department = ProductDepartment.CLEANING,
            nameEn = "window",
            nameHi = "खिड़की",
            nameTa = "ஜன்னல்",
            keywords = listOf("drain cleaner", "sink cleaner", "toilet plunger", "frame", "fresh air", "opening", "transparent", "view", "window", "खिड़की", "खोलना", "ताज़ी हवा", "दृश्य", "पारदर्शी", "फ़्रेम", "ஒளிப்புகும் தன்மை", "காட்சி", "சட்டம்", "திறப்பு", "தூய்மையான காற்று", "ஜன்னல்")
        ),
        ProductIconItem(
            iconKey = "fire_extinguisher",
            drawableRes = R.drawable.ic_prod_fire_extinguisher,
            department = ProductDepartment.CLEANING,
            nameEn = "fire extinguisher",
            nameHi = "अग्निशामक",
            nameTa = "தீ அணைப்பான்",
            keywords = listOf("fire safety", "extinguisher cylinder", "extinguish", "fire", "fire extinguisher", "quench", "अग्नि", "अग्निशमन", "अग्निशामक", "आग", "बुझाना", "அணைத்தல்", "தணித்தல்", "தீ", "தீ அணைப்பான்")
        ),
        ProductIconItem(
            iconKey = "toothbrush",
            drawableRes = R.drawable.ic_prod_toothbrush,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "toothbrush",
            nameHi = "टूथब्रश",
            nameTa = "டூத்பிரஷ்",
            keywords = listOf("colgate brush", "oral b", "toothpaste", "colgate", "pepsodent", "close up", "sensodyne", "dant kanti", "dabur red", "brush", "bathroom", "clean", "dental", "hygiene", "teeth", "toothbrush", "टूथब्रश", "डेंटल", "दाँत", "बाथरूम", "ब्रश", "सफ़ाई", "हाइजीन", "குளியலறை", "சுகாதாரம்", "சுத்தம்", "டூத்பிரஷ்", "டெண்ட்டல்", "பற்கள்", "பிரஷ்")
        ),
        ProductIconItem(
            iconKey = "razor",
            drawableRes = R.drawable.ic_prod_razor,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "razor",
            nameHi = "उस्तरा",
            nameTa = "ரேஸர்",
            keywords = listOf("gillette", "shaving blade", "shaving razor", "shaving foam", "shaving cream", "aftershave", "ustara", "razor", "sharp", "shave", "उस्तरा", "छुरा", "छुरी", "ब्लेड", "रेज़र", "शेव", "शेविंग", "கூர்மென்கத்தி", "சவரக்கத்தி", "ரேஸர்")
        ),
        ProductIconItem(
            iconKey = "comb",
            drawableRes = R.drawable.ic_prod_comb,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "person getting haircut",
            nameHi = "बाल कटवाता हुआ व्यक्ति",
            nameTa = "முடித்திருத்தம்",
            keywords = listOf("kanghi", "seepu", "hair comb", "hair brush", "hair care", "barber", "beauty", "haircut", "parlor", "person getting haircut", "पार्लर", "बाल कटवाता हुआ व्यक्ति", "ब्यूटी पार्लर", "सुंदरता", "அழகு நிலையம்", "கூந்தல் பராமரிப்பு", "சிகை அலங்காரம்", "பார்லர்", "பியூட்டி பார்லர்", "முடித்திருத்தம்")
        ),
        ProductIconItem(
            iconKey = "lipstick",
            drawableRes = R.drawable.ic_prod_lipstick,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "lipstick",
            nameHi = "लिपस्टिक",
            nameTa = "லிப்ஸ்டிக்",
            keywords = listOf("lip balm", "vaseline", "lakme", "maybelline", "cosmetics", "lip gloss", "chapstick", "lipstick", "makeup", "कॉस्मेटिक", "मेकअप", "लिपस्टिक", "அலங்காரம்", "உதட்டுச்சாயம்", "மேக்-அப்", "லிப்ஸ்டிக்")
        ),
        ProductIconItem(
            iconKey = "nail_polish",
            drawableRes = R.drawable.ic_prod_nail_polish,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "nail polish",
            nameHi = "नेल पॉलिश",
            nameTa = "நகச்சாயம்",
            keywords = listOf("nail paint", "nail enamel", "nail polish remover", "manicure", "care", "cosmetics", "nail", "polish", "कॉस्मेटिक", "नाखून", "नाखूनों की देखरेख", "नेल पॉलिश", "मेनीक्योर", "அழகுப்பொருட்கள்", "நகச்சாயம்", "நகப்பூச்சு", "நகம்", "பராமரிப்பு", "மெனிக்கியூர்")
        ),
        ProductIconItem(
            iconKey = "mirror",
            drawableRes = R.drawable.ic_prod_mirror,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "mirror",
            nameHi = "आईना",
            nameTa = "கண்ணாடி",
            keywords = listOf("sheesha", "pocket mirror", "shaving mirror", "mirror", "reflection", "reflector", "speculum", "आईना", "दर्पण", "परावर्तक", "प्रतिबिंब", "கண்ணாடி", "பிரதிபலிப்பான்", "பிரதிபலிப்பு", "மெருகேற்றப்பட்ட உலோகம்")
        ),
        ProductIconItem(
            iconKey = "scissors",
            drawableRes = R.drawable.ic_prod_scissors,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "scissors",
            nameHi = "कैंची",
            nameTa = "கத்திரிக்கோல்",
            keywords = listOf("nail cutter", "hair scissors", "grooming scissor", "cutting", "scissors", "tool", "कैंची", "टूल", "கத்திரி", "கத்திரிக்கோல்", "தையல்", "வெட்டு", "வெட்டுதல்")
        ),
        ProductIconItem(
            iconKey = "safety_pin",
            drawableRes = R.drawable.ic_prod_safety_pin,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "safety pin",
            nameHi = "सेफ़्टी पिन",
            nameTa = "ஊக்கு",
            keywords = listOf("sanitary pads", "whisper", "stayfree", "sofy", "cotton buds", "ear buds", "cotton roll", "bandage tape", "diaper", "punk rock", "safety pin", "डायपर", "पंक रॉक", "सेफ़्टी पिन", "ஊக்கு", "டயபர்", "பங்க் ராக்")
        ),
        ProductIconItem(
            iconKey = "bath_soap",
            drawableRes = R.drawable.ic_prod_bath_soap,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "soap",
            nameHi = "साबुन",
            nameTa = "சோப்பு",
            keywords = listOf("dettol", "dettol antiseptic", "dettol soap", "lifebuoy", "lux", "pears", "cinthol", "santoor", "fiama", "body wash", "handwash", "dettol handwash", "bar", "bathing", "cleaning", "lather", "soap", "soapdish", "नहाना", "बार", "सफ़ाई करना", "साबुन", "साबुन का झाग", "सोपडिश", "குளியல்", "சுத்தம்", "சோப்", "சோப்பு", "சோப்பு டப்பா", "நுரை")
        ),
        ProductIconItem(
            iconKey = "baby_face",
            drawableRes = R.drawable.ic_prod_baby_face,
            department = ProductDepartment.BABY_CARE,
            nameEn = "baby",
            nameHi = "शिशु",
            nameTa = "குழந்தை",
            keywords = listOf("pampers", "huggies", "mamy poko pants", "baby diapers", "baby wipes", "johnsons baby", "himalaya baby", "baby", "young", "नवजात", "शिशु", "குட்டிப்பாப்பா", "குட்டிப்பையன்", "குழந்தை", "பாப்பா", "மக்கள்")
        ),
        ProductIconItem(
            iconKey = "teddy_bear",
            drawableRes = R.drawable.ic_prod_teddy_bear,
            department = ProductDepartment.BABY_CARE,
            nameEn = "teddy bear",
            nameHi = "टेडी बेअर",
            nameTa = "டெடிபியர்",
            keywords = listOf("soft toy", "baby toy", "plush toy", "rattle", "plaything", "plush", "stuffed", "teddy bear", "toy", "खिलौना", "टेडी बेअर", "टेडीबियर", "टॉय", "भव्य", "स्टफ़्ड", "டெடிபியர்", "பஞ்சு", "பட்டு", "பொம்மை", "விளையாட்டுப்பொருள்")
        ),
        ProductIconItem(
            iconKey = "baby_crib",
            drawableRes = R.drawable.ic_prod_baby_crib,
            department = ProductDepartment.BABY_CARE,
            nameEn = "person in bed",
            nameHi = "बिस्तर में सोता व्यक्ति",
            nameTa = "படுக்கையில் படுத்திருப்பவர்",
            keywords = listOf("jhoola", "baby cot", "baby bed", "pram", "good night", "hotel", "person in bed", "sleep", "बिस्तर में सोता व्यक्ति", "होटल", "தூக்கம்", "படுக்கையில் படுத்திருப்பவர்", "படுத்திருப்பவர்", "விடுதி")
        ),
        ProductIconItem(
            iconKey = "dog",
            drawableRes = R.drawable.ic_prod_dog,
            department = ProductDepartment.PET_CARE,
            nameEn = "dog",
            nameHi = "कुत्ता",
            nameTa = "நாய்",
            keywords = listOf("dog food", "pedigree", "drools", "royal canin", "puppy food", "dog biscuits", "dog treats", "meat up", "dog", "pet", "कुत्ता", "पपी", "पशु", "செல்லப்பிராணி", "நாய்", "நாய்க்குட்டி", "விலங்கு")
        ),
        ProductIconItem(
            iconKey = "cat",
            drawableRes = R.drawable.ic_prod_cat,
            department = ProductDepartment.PET_CARE,
            nameEn = "cat",
            nameHi = "बिल्ली",
            nameTa = "பூனை",
            keywords = listOf("cat food", "whiskas", "me-o", "purepet", "kitten food", "cat treats", "cat", "pet", "पशु", "बिल्ली", "பூனை", "பூனைக்குட்டி", "விலங்கு")
        ),
        ProductIconItem(
            iconKey = "dog_face",
            drawableRes = R.drawable.ic_prod_dog_face,
            department = ProductDepartment.PET_CARE,
            nameEn = "dog face",
            nameHi = "कुत्ते का चेहरा",
            nameTa = "நாய் முகம்",
            keywords = listOf("pet care", "canine", "dog", "face", "pet", "कुत्ता", "कुत्ते का चेहरा", "चेहरा", "पशु", "செல்லப்பிராணி", "நாய்", "முகம்", "வளர்ப்புப் பிராணி")
        ),
        ProductIconItem(
            iconKey = "cat_face",
            drawableRes = R.drawable.ic_prod_cat_face,
            department = ProductDepartment.PET_CARE,
            nameEn = "cat face",
            nameHi = "बिल्ली का चेहरा",
            nameTa = "பூனை முகம்",
            keywords = listOf("feline", "pet care", "cat", "face", "pet", "चेहरा", "पशु", "बिल्ली", "बिल्ली का चेहरा", "செல்லப்பிராணி", "பூனை", "பூனைக்குட்டி", "முகம்", "விலங்கு")
        ),
        ProductIconItem(
            iconKey = "paw_prints",
            drawableRes = R.drawable.ic_prod_paw_prints,
            department = ProductDepartment.PET_CARE,
            nameEn = "paw prints",
            nameHi = "पंजे की छाप",
            nameTa = "விலங்கின் பாதத்தடம்",
            keywords = listOf("pet grooming", "pet shampoo", "pet accessories", "pet care", "feet", "paw", "paw prints", "print", "छाप", "पंजा", "पंजे की छाप", "पशु", "தடம்", "பாதத் தடம்", "பாதம்", "விலங்கின் பாதத்தடம்", "விலங்கு")
        ),
        ProductIconItem(
            iconKey = "bird",
            drawableRes = R.drawable.ic_prod_bird,
            department = ProductDepartment.PET_CARE,
            nameEn = "bird",
            nameHi = "चिड़िया",
            nameTa = "பறவை",
            keywords = listOf("bird food", "seeds", "parrot food", "lovebirds food", "bird", "चिड़िया", "पशु", "பறவை", "விலங்கினம்")
        ),
        ProductIconItem(
            iconKey = "rabbit",
            drawableRes = R.drawable.ic_prod_rabbit,
            department = ProductDepartment.PET_CARE,
            nameEn = "rabbit",
            nameHi = "खरगोश",
            nameTa = "முயல்",
            keywords = listOf("rabbit food", "hay", "bunny pellets", "bunny", "pet", "rabbit", "खरगोश", "पशु", "बन्नी", "முயல்", "முயல் குட்டி", "மொசக் குட்டி", "விலங்கு")
        ),
        ProductIconItem(
            iconKey = "hamster",
            drawableRes = R.drawable.ic_prod_hamster,
            department = ProductDepartment.PET_CARE,
            nameEn = "hamster",
            nameHi = "चूहे का चेहरा",
            nameTa = "வெள்ளெலி முகம்",
            keywords = listOf("hamster food", "guinea pig food", "face", "hamster", "pet", "चूहा", "चूहे का चेहरा", "चेहरा", "पशु", "முகம்", "விலங்கு", "வெள்ளெலி")
        ),
        ProductIconItem(
            iconKey = "ballpoint_pen",
            drawableRes = R.drawable.ic_prod_ballpoint_pen,
            department = ProductDepartment.STATIONERY,
            nameEn = "pen",
            nameHi = "बॉलपेन",
            nameTa = "பேனா",
            keywords = listOf("pen", "reynolds", "cello", "flair", "ball pen", "gel pen", "trimax", "uniball", "hauser", "ink pen", "ballpoint", "पेन", "बॉलपेन", "பால்பாய்ண்ட் பேனா", "பேனா")
        ),
        ProductIconItem(
            iconKey = "pencil",
            drawableRes = R.drawable.ic_prod_pencil,
            department = ProductDepartment.STATIONERY,
            nameEn = "pencil",
            nameHi = "पेंसिल",
            nameTa = "பென்சில்",
            keywords = listOf("apsara pencil", "natraj", "doms", "lead pencil", "drawing pencil", "eraser", "sharpener", "pencil", "पेंसिल", "ஆரம்பப்பள்ளி", "பென்சில்")
        ),
        ProductIconItem(
            iconKey = "fountain_pen",
            drawableRes = R.drawable.ic_prod_fountain_pen,
            department = ProductDepartment.STATIONERY,
            nameEn = "fountain pen",
            nameHi = "फ़ाउंटन पेन",
            nameTa = "பவுண்டைன் பேனா",
            keywords = listOf("ink pen", "camlin", "parker", "fountain pen", "ink bottle", "fountain", "pen", "कलम", "पेन", "फ़ाउंटन", "பவுண்டைன் பேனா", "பேனா")
        ),
        ProductIconItem(
            iconKey = "marker",
            drawableRes = R.drawable.ic_prod_marker,
            department = ProductDepartment.STATIONERY,
            nameEn = "crayon",
            nameHi = "क्रेयॉन",
            nameTa = "கிரேயன்",
            keywords = listOf("highlighter", "whiteboard marker", "permanent marker", "sketch pen", "camlin marker", "crayons", "color pencils", "crayon", "क्रेयॉन", "मोम के रंग", "கிரேயன்", "கிரேயான்ஸ்", "வண்ணம் தீட்டுதல்")
        ),
        ProductIconItem(
            iconKey = "spiral_notebook",
            drawableRes = R.drawable.ic_prod_spiral_notebook,
            department = ProductDepartment.STATIONERY,
            nameEn = "notebook",
            nameHi = "नोटबुक",
            nameTa = "நோட்டுப்புத்தகம்",
            keywords = listOf("classmate notebook", "copy", "rough notebook", "long notebook", "spiral book", "navneet", "notebook", "नोटबुक", "நோட்டுப்புத்தகம்", "நோட்புக்")
        ),
        ProductIconItem(
            iconKey = "ruled_notebook",
            drawableRes = R.drawable.ic_prod_ruled_notebook,
            department = ProductDepartment.STATIONERY,
            nameEn = "notebook with decorative cover",
            nameHi = "सजावटी कवर वाली नोटबुक",
            nameTa = "அழகான கவர் போடப்பட்ட நோட்டு",
            keywords = listOf("single line notebook", "four line notebook", "square line", "school notebook", "book", "cover", "decorated", "notebook", "notebook with decorative cover", "कवर", "किताब", "नोटबुक", "सजावट", "सजावटी कवर वाली नोटबुक", "அட்டை", "அழகான கவர் போடப்பட்ட நோட்டு", "அழகான நோட்டு", "வேலைப்பாடான அட்டை")
        ),
        ProductIconItem(
            iconKey = "ledger",
            drawableRes = R.drawable.ic_prod_ledger,
            department = ProductDepartment.STATIONERY,
            nameEn = "closed book",
            nameHi = "बंद किताब",
            nameTa = "மூடிய புத்தகம்",
            keywords = listOf("khata book", "bahi khata", "register", "attendance register", "cash book", "bill book", "voucher pad", "book", "closed", "पुस्तक", "बंद किताब", "புத்தகம்", "மூடிய புத்தகம்")
        ),
        ProductIconItem(
            iconKey = "books",
            drawableRes = R.drawable.ic_prod_books,
            department = ProductDepartment.STATIONERY,
            nameEn = "books",
            nameHi = "किताबें",
            nameTa = "புத்தகங்கள்",
            keywords = listOf("textbook", "guide book", "drawing book", "chart paper", "book", "books", "किताबें", "पुस्तक", "புத்தகங்கள்", "புத்தகம்")
        ),
        ProductIconItem(
            iconKey = "paperclip",
            drawableRes = R.drawable.ic_prod_paperclip,
            department = ProductDepartment.STATIONERY,
            nameEn = "paperclip",
            nameHi = "पेपरक्लिप",
            nameTa = "பேப்பர் கிளிப்",
            keywords = listOf("gems clip", "binder clip", "u clip", "paper clips", "stapler pins", "paperclip", "क्लिप", "पेपरक्लिप", "கிளிப்", "பேப்பர் கிளிப்")
        ),
        ProductIconItem(
            iconKey = "straight_ruler",
            drawableRes = R.drawable.ic_prod_straight_ruler,
            department = ProductDepartment.STATIONERY,
            nameEn = "straight ruler",
            nameHi = "स्ट्रेट रूलर",
            nameTa = "அளவுகோல்",
            keywords = listOf("scale", "plastic scale", "steel ruler", "30cm scale", "15cm scale", "ruler", "straight edge", "straight ruler", "पैमाना", "सीधा रूलर", "स्ट्रेट रूलर", "அடிஸ்கேல்", "அளவுகோல்", "ஸ்கேல்")
        ),
        ProductIconItem(
            iconKey = "triangle_ruler",
            drawableRes = R.drawable.ic_prod_triangle_ruler,
            department = ProductDepartment.STATIONERY,
            nameEn = "triangular ruler",
            nameHi = "त्रिकोणीय रूलर",
            nameTa = "முக்கோணமானி",
            keywords = listOf("geometry box", "set square", "compass", "protractor", "camlin flora", "ruler", "set", "triangle", "triangular ruler", "गुनिया", "तिकोना रूलर", "त्रिकोण", "त्रिकोणीय रूलर", "पैमाना", "அளவுகோல்", "செட் ஸ்கொயர்", "முக்கோணமானி", "முக்கோணம்")
        ),
        ProductIconItem(
            iconKey = "envelope",
            drawableRes = R.drawable.ic_prod_envelope,
            department = ProductDepartment.STATIONERY,
            nameEn = "envelope",
            nameHi = "लिफ़ाफ़ा",
            nameTa = "கடித உறை",
            keywords = listOf("post envelope", "brown envelope", "office envelope", "cover", "shagun lifafa", "email", "envelope", "letter", "ईमेल", "मेल", "लिफ़ाफ़ा", "என்வெலப்", "என்வெலப் கவர்", "கடித உறை")
        ),
        ProductIconItem(
            iconKey = "memo",
            drawableRes = R.drawable.ic_prod_memo,
            department = ProductDepartment.STATIONERY,
            nameEn = "memo",
            nameHi = "मेमो",
            nameTa = "மெமோ",
            keywords = listOf("post it", "sticky notes", "notepad", "scratch pad", "scribble pad", "memo", "pencil", "पेंसिल", "मेमो", "குறிப்பு", "குறிப்புப் புத்தகம்", "நோட்", "மெமோ")
        ),
        ProductIconItem(
            iconKey = "pushpin",
            drawableRes = R.drawable.ic_prod_pushpin,
            department = ProductDepartment.STATIONERY,
            nameEn = "pushpin",
            nameHi = "पुशपिन",
            nameTa = "பின்",
            keywords = listOf("thumb pin", "board pin", "soft board pin", "tack", "pin", "pushpin", "पिन", "पुशपिन", "ஊசி", "பின்")
        ),
        ProductIconItem(
            iconKey = "calendar",
            drawableRes = R.drawable.ic_prod_calendar,
            department = ProductDepartment.STATIONERY,
            nameEn = "calendar",
            nameHi = "कैलेंडर",
            nameTa = "நாள்காட்டி",
            keywords = listOf("panchang", "desk calendar", "wall calendar", "planner", "calendar", "date", "कैलेंडर", "तारीख़", "तिथि", "दिनांक", "காலண்டர்", "காலேண்டர்", "நாட்காட்டி", "நாள்காட்டி")
        ),
        ProductIconItem(
            iconKey = "clipboard",
            drawableRes = R.drawable.ic_prod_clipboard,
            department = ProductDepartment.STATIONERY,
            nameEn = "clipboard",
            nameHi = "क्लिपबोर्ड",
            nameTa = "பரீட்சை அட்டை",
            keywords = listOf("exam pad", "writing pad", "clip board", "clipboard", "क्लिपबोर्ड", "கிளிப்", "கிளிப் போர்டு", "பரீட்சை அட்டை")
        ),
        ProductIconItem(
            iconKey = "file_folder",
            drawableRes = R.drawable.ic_prod_file_folder,
            department = ProductDepartment.STATIONERY,
            nameEn = "file folder",
            nameHi = "फ़ाइल फ़ोल्डर",
            nameTa = "பைல் ஃபோல்டர்",
            keywords = listOf("cobra file", "display folder", "ring binder", "plastic file", "document folder", "file", "folder", "फ़ाइल", "फ़ोल्डर", "ஃபோல்டர்", "கோப்பு", "பைல்")
        ),
        ProductIconItem(
            iconKey = "backpack",
            drawableRes = R.drawable.ic_prod_backpack,
            department = ProductDepartment.STATIONERY,
            nameEn = "backpack",
            nameHi = "स्कूल का बस्ता",
            nameTa = "பள்ளிக்கூட பை",
            keywords = listOf("school bag", "tuition bag", "pencil pouch", "pencil box", "backpack", "bag", "rucksack", "satchel", "school", "बस्ता", "विद्यालय", "स्कूल का बस्ता", "स्कूल बैग", "பள்ளிக்கூட பை", "பள்ளிக்கூடம்", "பை", "மாணவர்கள்")
        ),
        ProductIconItem(
            iconKey = "glue_tape",
            drawableRes = R.drawable.ic_prod_glue_tape,
            department = ProductDepartment.STATIONERY,
            nameEn = "bookmark",
            nameHi = "बुकमार्क",
            nameTa = "புக்மார்க்",
            keywords = listOf("fevistick", "fevicol", "glue", "cello tape", "brown tape", "masking tape", "double side tape", "bookmark", "mark", "निशान", "बुकमार्क", "பக்க அடையாளம்", "பட்டி", "புக்மார்க்", "புத்தகக்குறி")
        ),
        ProductIconItem(
            iconKey = "abacus",
            drawableRes = R.drawable.ic_prod_abacus,
            department = ProductDepartment.STATIONERY,
            nameEn = "abacus",
            nameHi = "अबैकस",
            nameTa = "அபாக்கஸ்",
            keywords = listOf("abacus", "calculator", "math tool", "counting frame", "calculation", "अबैकस", "गणना", "அபாக்கஸ்", "கணக்கு")
        ),
        ProductIconItem(
            iconKey = "paintbrush",
            drawableRes = R.drawable.ic_prod_paintbrush,
            department = ProductDepartment.STATIONERY,
            nameEn = "paintbrush",
            nameHi = "पेंटब्रश",
            nameTa = "பெயின்ட் பிரஷ்",
            keywords = listOf("art brush", "water color brush", "painting brush", "paintbrush", "painting", "पेंटब्रश", "पेंटिंग", "பெயின்ட் பிரஷ்", "பெயின்ட்டிங்")
        ),
        ProductIconItem(
            iconKey = "kitchen_knife",
            drawableRes = R.drawable.ic_prod_kitchen_knife,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "kitchen knife",
            nameHi = "खाना बनाने का चाकू",
            nameTa = "சமையல் கத்தி",
            keywords = listOf("chaku", "knife", "vegetable knife", "chopping knife", "kitchen cutter", "cooking", "hocho", "kitchen knife", "tool", "weapon", "खाना बनाने का चाकू", "चाकू", "கத்தி", "காய்கறி வெட்டுதல்", "சமையல் கத்தி", "வெட்டு கத்தி")
        ),
        ProductIconItem(
            iconKey = "fork_and_knife",
            drawableRes = R.drawable.ic_prod_fork_and_knife,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "fork and knife",
            nameHi = "फ़ोर्क और छुरी–चाकू",
            nameTa = "முள்கரண்டியும் கத்தியும்",
            keywords = listOf("cutlery", "fork", "table knife", "spoon fork set", "cooking", "fork and knife", "knife", "छुरी–चाकू", "नाइफ़", "फ़ोर्क", "फ़ोर्क और छुरी–चाकू", "रेस्टोरेंट", "உணவகம்", "உணவு", "உணவு நேரம்", "முள்கரண்டியும் கத்தியும்")
        ),
        ProductIconItem(
            iconKey = "spoon",
            drawableRes = R.drawable.ic_prod_spoon,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "spoon",
            nameHi = "चम्मच",
            nameTa = "கரண்டி",
            keywords = listOf("chamach", "steel spoon", "tablespoon", "teaspoon", "soup spoon", "serving spoon", "spoon", "tableware", "चम्मच", "मेज के बर्तन", "கரண்டி")
        ),
        ProductIconItem(
            iconKey = "plate",
            drawableRes = R.drawable.ic_prod_plate,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "fork and knife with plate",
            nameHi = "प्लेट के साथ काँटा और छुरी",
            nameTa = "தட்டுடன் முள்கரண்டியும் கத்தியும்",
            keywords = listOf("thali", "dinner plate", "steel plate", "melamine plate", "quarter plate", "cooking", "fork", "fork and knife with plate", "knife", "plate", "काँटा", "खाना बनाना", "चाकू", "प्लेट", "प्लेट के साथ काँटा और छुरी", "உணவு", "கத்தி", "சமையல்", "தட்டு", "தட்டுடன் முள்கரண்டியும் கத்தியும்", "முள்கரண்டி")
        ),
        ProductIconItem(
            iconKey = "cooking_pot",
            drawableRes = R.drawable.ic_prod_cooking_pot,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "pot of food",
            nameHi = "स्ट्यू, भोजन पात्र",
            nameTa = "பாத்திரம் நிறைய உணவு",
            keywords = listOf("patila", "bhagona", "degchi", "cooker", "pressure cooker", "saucepan", "milk pan", "handi", "pot", "pot of food", "stew", "भोजन पात्र", "रेस्टोरेंट", "स्ट्यू", "स्ट्यू, भोजन पात्र", "உணவகம்", "உணவு", "சூடான உணவு", "பாத்திரத்தில் உணவு", "பாத்திரம் நிறைய உணவு")
        ),
        ProductIconItem(
            iconKey = "frying_pan",
            drawableRes = R.drawable.ic_prod_frying_pan,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "cooking",
            nameHi = "भोजन पकाना",
            nameTa = "சமையல்",
            keywords = listOf("kadhai", "tawa", "dosa tawa", "roti tawa", "fry pan", "non stick pan", "prestige", "hawkins", "breakfast", "cooking", "egg", "frying", "pan", "फ़्राइंग पैन", "भोजन पकाना", "रेस्टोरेंट", "உணவு", "காஃப் பாயில்", "காஃப் பாயில் முட்டை", "சமையல்", "முட்டை")
        ),
        ProductIconItem(
            iconKey = "chopsticks",
            drawableRes = R.drawable.ic_prod_chopsticks,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "chopsticks",
            nameHi = "चॉपस्टिक",
            nameTa = "சாப்ஸ்டிக்",
            keywords = listOf("wooden chopsticks", "noodle sticks", "chopsticks", "hashi", "चॉपस्टिक", "हाशी", "சாப்ஸ்டிக்", "சீன உணவு", "ஜப்பானிய உணவு", "ஹஷி")
        ),
        ProductIconItem(
            iconKey = "thermos",
            drawableRes = R.drawable.ic_prod_thermos,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "sake",
            nameHi = "सेक, सेक बोतल और कप",
            nameTa = "மது பாட்டிலும் கோப்பையும்",
            keywords = listOf("milton flask", "thermos flask", "hot water bottle", "insulated flask", "water bottle", "school bottle", "bar", "beverage", "bottle", "cup", "drink", "sake", "कप", "पीना", "पेय", "बार", "बोतल", "साकी", "सेक", "सेक बोतल और कप", "सेक, सेक बोतल और कप", "உணவகம்", "சேக்", "சேக் பாட்டிலும் கோப்பையும்", "மது பாட்டிலும் கோப்பையும்", "மதுபானம்")
        ),
        ProductIconItem(
            iconKey = "battery",
            drawableRes = R.drawable.ic_prod_battery,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "battery",
            nameHi = "बैटरी",
            nameTa = "பேட்டரி",
            keywords = listOf("duracell", "everyeady", "aa cell", "aaa battery", "remote cell", "nippo", "panasonic", "battery", "बैटरी", "பேட்டரி", "மின்கலம்")
        ),
        ProductIconItem(
            iconKey = "electric_plug",
            drawableRes = R.drawable.ic_prod_electric_plug,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "electric plug",
            nameHi = "बिजली का प्लग",
            nameTa = "பிளக்",
            keywords = listOf("plug", "socket", "extension board", "adapter", "usb cable", "charging cable", "mobile charger", "electric", "electricity", "बिजली", "बिजली का प्लग", "பிளக்", "பிளக் பாயின்ட்")
        ),
        ProductIconItem(
            iconKey = "light_bulb",
            drawableRes = R.drawable.ic_prod_light_bulb,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "light bulb",
            nameHi = "लाइट बल्ब, विचार",
            nameTa = "மின்விளக்கு",
            keywords = listOf("led bulb", "philips bulb", "wipro bulb", "cfl", "tube light", "9w bulb", "night bulb", "bulb", "comic", "electric", "idea", "light", "कॉमिक", "प्रकाश", "बल्ब", "बिजली", "लाइट बल्ब", "लाइट बल्ब, विचार", "विचार", "ஐடியா", "ஒளி", "நகைச்சுவை", "பல்பு", "மின்சாரம்", "மின்விளக்கு")
        ),
        ProductIconItem(
            iconKey = "flashlight",
            drawableRes = R.drawable.ic_prod_flashlight,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "flashlight",
            nameHi = "टॉर्च",
            nameTa = "டார்ச் லைட்",
            keywords = listOf("torch", "led torch", "emergency light", "rechargeable torch", "electric", "flashlight", "light", "tool", "टॉर्च", "प्रकाश", "फ़्लैशलाइट", "बिजली", "டார்ச்", "பிளாஷ்", "லைட்", "விளக்கு")
        ),
        ProductIconItem(
            iconKey = "phone",
            drawableRes = R.drawable.ic_prod_phone,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "mobile phone",
            nameHi = "मोबाइल फ़ोन",
            nameTa = "மொபைல்",
            keywords = listOf("mobile phone", "screen guard", "phone cover", "sim card", "tempered glass", "cell", "mobile", "phone", "telephone", "चलभाष", "टेलीफ़ोन", "दूरभाष", "फ़ोन", "मोबाइल", "செல்பேசி", "செல்போன்", "மொபைல்")
        ),
        ProductIconItem(
            iconKey = "headphones",
            drawableRes = R.drawable.ic_prod_headphones,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "headphone",
            nameHi = "हेडफ़ोन",
            nameTa = "ஹெட்ஃபோன்",
            keywords = listOf("earphones", "earbuds", "boat", "noise", "bluetooth earphones", "wired headset", "earbud", "headphone", "ईयरबड", "हेडफ़ोन", "செய்தி", "பாடல்", "பாட்டு கேட்டல்", "பொழுதுபோக்கு", "ஹெட்ஃபோன்")
        ),
        ProductIconItem(
            iconKey = "laptop",
            drawableRes = R.drawable.ic_prod_laptop,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "laptop",
            nameHi = "लैपटॉप",
            nameTa = "லேப்டாப் கணினி",
            keywords = listOf("computer", "mouse", "keyboard", "pendrive", "usb drive", "san disk", "laptop", "pc", "personal", "कंप्यूटर", "पर्सनल कंप्यूटर", "पीसी", "लैपटॉप", "கணிப்பொறி", "கணினி", "தனிநபர் கணினி", "லேப்டாப் கணினி")
        ),
        ProductIconItem(
            iconKey = "tablet",
            drawableRes = R.drawable.ic_prod_tablet,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "mobile phone with arrow",
            nameHi = "तीर के साथ मोबाइल फ़ोन",
            nameTa = "அம்புக்குறியுடன் செல்போன்",
            keywords = listOf("ipad", "tab", "display tablet", "arrow", "cell", "mobile", "mobile phone with arrow", "phone", "receive", "तीर", "तीर के साथ फ़ोन", "तीर के साथ मोबाइल फ़ोन", "मोबाइल", "सेल", "அம்புக்குறி", "அம்புக்குறியுடன் செல்போன்", "ஃபோன்", "செல்ஃபோன்", "பெறுதல்", "மொபைல்")
        ),
        ProductIconItem(
            iconKey = "alarm_clock",
            drawableRes = R.drawable.ic_prod_alarm_clock,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "alarm clock",
            nameHi = "अलार्म घड़ी, समय",
            nameTa = "அலாரம்",
            keywords = listOf("ghadi", "wall clock", "table clock", "ajanta clock", "alarm", "clock", "अलार्म", "अलार्म घड़ी, समय", "घड़ी", "समय", "அலார கடிகாரம்", "அலாரம்", "கடிகாரம்")
        ),
        ProductIconItem(
            iconKey = "wrist_watch",
            drawableRes = R.drawable.ic_prod_wrist_watch,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "watch",
            nameHi = "घड़ी, समय",
            nameTa = "வாட்ச்",
            keywords = listOf("watch", "titan", "fastrack", "smart watch", "wrist watch", "clock", "घड़ी", "घड़ी, समय", "समय", "हाथघड़ी", "கடிகாரம்", "நேரம்", "வாட்ச்")
        ),
        ProductIconItem(
            iconKey = "lock",
            drawableRes = R.drawable.ic_prod_lock,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "locked",
            nameHi = "बंद ताला",
            nameTa = "பூட்டு",
            keywords = listOf("tala", "padlock", "godrej lock", "brass lock", "door lock", "closed", "locked", "ताला", "बंद ताला", "பூட்டியது", "பூட்டு")
        ),
        ProductIconItem(
            iconKey = "key",
            drawableRes = R.drawable.ic_prod_key,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "key",
            nameHi = "चाबी",
            nameTa = "சாவி",
            keywords = listOf("chabi", "keys", "spare key", "key", "lock", "password", "चाबी", "पासवर्ड", "கடவுச்சொல்", "சாவி", "பாஸ்வேர்ட்")
        ),
        ProductIconItem(
            iconKey = "hammer",
            drawableRes = R.drawable.ic_prod_hammer,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "hammer",
            nameHi = "हथौड़ा",
            nameTa = "சுத்தி",
            keywords = listOf("hathoda", "nail hammer", "hardware tool", "hammer", "tool", "औज़ार", "हथौड़ा", "கருவி", "சுத்தி", "சுத்தியல்")
        ),
        ProductIconItem(
            iconKey = "screwdriver",
            drawableRes = R.drawable.ic_prod_screwdriver,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "screwdriver",
            nameHi = "स्क्रूड्राइवर",
            nameTa = "திருப்புளி",
            keywords = listOf("tester", "screw driver", "hardware kit", "screw", "screwdriver", "tool", "औजार", "स्क्रू", "स्क्रूड्राइवर", "கருவி", "திருப்புளி")
        ),
        ProductIconItem(
            iconKey = "television",
            drawableRes = R.drawable.ic_prod_television,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "television",
            nameHi = "टेलीविज़न",
            nameTa = "தொலைக்காட்சி",
            keywords = listOf("tv", "led screen", "television set", "television", "video", "टीवी", "टेलीविज़न", "वीडियो", "டிவி", "டெலிவிஷன்", "தொலைக்காட்சி")
        ),
        ProductIconItem(
            iconKey = "radio",
            drawableRes = R.drawable.ic_prod_radio,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "radio",
            nameHi = "रेडियो",
            nameTa = "ரேடியோ",
            keywords = listOf("transistor", "fm radio", "radio", "video", "रेडियो", "वीडियो", "எப்எம்", "ரேடியோ", "வானொலி")
        ),
        ProductIconItem(
            iconKey = "camera",
            drawableRes = R.drawable.ic_prod_camera,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "camera",
            nameHi = "कैमरा",
            nameTa = "கேமரா",
            keywords = listOf("dslr", "digital camera", "camera", "video", "कैमरा", "वीडियो", "கேமரா", "படம்", "புகைப்படம்")
        ),
        ProductIconItem(
            iconKey = "pill",
            drawableRes = R.drawable.ic_prod_pill,
            department = ProductDepartment.HEALTH,
            nameEn = "pill",
            nameHi = "गोली",
            nameTa = "மாத்திரை",
            keywords = listOf("tablet", "capsule", "paracetamol", "crocin", "dolo 650", "pain killer", "saridon", "disprin", "combiflam", "gelusil", "pudina hara", "eno", "vitamin c", "becosules", "doctor", "medicine", "pill", "sick", "गोली", "डॉक्टर", "दवाई", "உடல் நலக் குறைவு", "மருத்துவம்", "மருத்துவர்", "மருந்து", "மாத்திரை")
        ),
        ProductIconItem(
            iconKey = "bandage",
            drawableRes = R.drawable.ic_prod_bandage,
            department = ProductDepartment.HEALTH,
            nameEn = "adhesive bandage",
            nameHi = "चिपकाने वाली पट्‍टी",
            nameTa = "ஒட்டக்கூடிய பேண்டேஜ்",
            keywords = listOf("band aid", "plaster", "hansaplast", "crepe bandage", "first aid tape", "adhesive bandage", "bandage", "चिपकाने वाली पट्‍टी", "पट्टी", "बैंड एड", "बैंडेज", "ஒட்டக்கூடிய பேண்டேஜ்", "காயம்", "பேண்டேஜ்")
        ),
        ProductIconItem(
            iconKey = "syringe",
            drawableRes = R.drawable.ic_prod_syringe,
            department = ProductDepartment.HEALTH,
            nameEn = "syringe",
            nameHi = "सिरींज, सूई",
            nameTa = "ஊசி",
            keywords = listOf("dispo van", "injection", "insulin syringe", "medicine", "needle", "shot", "sick", "syringe", "दवाई", "बीमार", "मरीज़", "सिरिंज", "सिरींज", "सिरींज, सूई", "सुई", "सूई", "ஊசி", "காய்ச்சல்", "மருத்துவம்", "மருந்து", "வைத்தியம்")
        ),
        ProductIconItem(
            iconKey = "stethoscope",
            drawableRes = R.drawable.ic_prod_stethoscope,
            department = ProductDepartment.HEALTH,
            nameEn = "stethoscope",
            nameHi = "स्टेथोस्कोप",
            nameTa = "ஸ்டெதாஸ்கோப்",
            keywords = listOf("bp monitor", "blood pressure check", "health tracker", "doctor", "heart", "medicine", "stethoscope", "चिकित्सा", "डॉक्टर", "ब्लड प्रेशर", "रक्तचाप", "स्टेथोस्कोप", "இதயத்துடிப்பு", "இதயம்", "மருத்துவம்", "மருத்துவர்", "ஸ்டெதாஸ்கோப்")
        ),
        ProductIconItem(
            iconKey = "thermometer",
            drawableRes = R.drawable.ic_prod_thermometer,
            department = ProductDepartment.HEALTH,
            nameEn = "thermometer",
            nameHi = "थर्मामीटर",
            nameTa = "வெப்பநிலைமானி",
            keywords = listOf("digital thermometer", "fever check", "temperature", "thermometer", "weather", "थर्मामीटर", "मौसम", "உடல்நிலை", "வெப்பநிலைமானி")
        ),
        ProductIconItem(
            iconKey = "face_mask",
            drawableRes = R.drawable.ic_prod_face_mask,
            department = ProductDepartment.HEALTH,
            nameEn = "face with medical mask",
            nameHi = "चिकित्सा मास्क वाला चेहरा",
            nameTa = "மருத்துவ மாஸ்க் அணிந்த முகம்",
            keywords = listOf("surgical mask", "n95 mask", "cotton mask", "protective mask", "cold", "doctor", "face", "face with medical mask", "mask", "sick", "चिकित्सा मास्क", "चिकित्सा मास्क वाला चेहरा", "चेहरा", "ठंड", "नकाब", "मास्क वाला चेहरा", "குளிர்", "நோயாளி", "மருத்துவ மாஸ்க் அணிந்த முகம்", "மருத்துவ முகமூடி", "மருத்துவர்", "முகம்")
        ),
        ProductIconItem(
            iconKey = "first_aid",
            drawableRes = R.drawable.ic_prod_first_aid,
            department = ProductDepartment.HEALTH,
            nameEn = "rescue worker’s helmet",
            nameHi = "सफेद क्रॉस वाला हेलमेट",
            nameTa = "வெள்ளை நிற கிராஸ் வரையப்பட்ட ஹெல்மெட்",
            keywords = listOf("first aid kit", "burnol", "savlon", "betadine", "moov", "iodex", "volini", "fast relief", "vicks vaporub", "amrutanjan", "aid", "cross", "face", "hat", "helmet", "rescue worker’s helmet", "सफेद क्रॉस वाला हेलमेट", "सहायता", "வெள்ளை நிற கிராஸ் உள்ள ஹெல்மெட்", "வெள்ளை நிற கிராஸ் வரையப்பட்ட ஹெல்மெட்")
        ),
        ProductIconItem(
            iconKey = "gift_box",
            drawableRes = R.drawable.ic_prod_gift_box,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "wrapped gift",
            nameHi = "उपहार, पैक किया हुआ उपहार",
            nameTa = "மூடப்பட்ட பரிசு",
            keywords = listOf("gift pack", "present", "gift wrapper", "cadbury celebrations", "gift hamper", "box", "celebration", "gift", "wrapped", "उपहार", "उपहार, पैक किया हुआ उपहार", "गिफ़्ट", "पैक किया हुआ उपहार", "लपेटा हुआ", "समारोह", "கொண்டாட்டம்", "பரிசு", "மூடப்பட்ட பரிசு")
        ),
        ProductIconItem(
            iconKey = "balloon",
            drawableRes = R.drawable.ic_prod_balloon,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "balloon",
            nameHi = "गुब्बारा",
            nameTa = "பலூன்",
            keywords = listOf("birthday balloon", "metallic balloon", "party decoration", "air balloon", "balloon", "celebration", "गुब्बारा", "समारोह", "குழந்தைகள்", "கொண்டாட்டம்", "திருவிழா", "பலூன்", "ஹீலியம்")
        ),
        ProductIconItem(
            iconKey = "party_popper",
            drawableRes = R.drawable.ic_prod_party_popper,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "party popper",
            nameHi = "पार्टी पॉपर",
            nameTa = "வண்ண வேடிக்கை",
            keywords = listOf("party blast", "birthday popper", "celebration spray", "snow spray", "celebration", "party", "popper", "tada", "पार्टी", "पॉपर", "समारोह", "கொண்டாட்டம்", "வண்ண வேடிக்கை", "விருந்து", "வெற்றி அறிவிப்பு")
        ),
        ProductIconItem(
            iconKey = "ribbon",
            drawableRes = R.drawable.ic_prod_ribbon,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "ribbon",
            nameHi = "फ़ीता",
            nameTa = "ரிப்பன்",
            keywords = listOf("gift ribbon", "satin ribbon", "decoration ribbon", "celebration", "ribbon", "उत्सव", "फ़ीता", "रिबन", "समारोह", "கொண்டாட்டம்", "திறப்பு விழா", "ரிப்பன்", "விருந்து")
        ),
        ProductIconItem(
            iconKey = "confetti",
            drawableRes = R.drawable.ic_prod_confetti,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "confetti ball",
            nameHi = "कॉनफ़ेटी बॉल",
            nameTa = "வண்ணக் காகிதங்களைத் தூவும் பந்து",
            keywords = listOf("party confetti", "sparkle balls", "decoration", "ball", "celebration", "confetti", "कॉनफ़ेटी", "कॉनफ़ेटी बॉल", "गेंद", "समारोह", "கொண்டாட்டம்", "திருமணம்", "பிறந்தநாள்", "வண்ணக் காகிதங்களைத் தூவும் பந்து", "வாழ்த்துகள்")
        ),
        ProductIconItem(
            iconKey = "candle",
            drawableRes = R.drawable.ic_prod_candle,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "candle",
            nameHi = "मोमबत्ती",
            nameTa = "மெழுகுவர்த்தி",
            keywords = listOf("birthday candle", "wax candle", "tealight candle", "fragrance candle", "white candle", "candle", "light", "प्रकाश", "मोमबत्ती", "ஒளி", "கேண்டில் பார்ட்டி", "மெழுகுவர்த்தி")
        ),
        ProductIconItem(
            iconKey = "diya_lamp",
            drawableRes = R.drawable.ic_prod_diya_lamp,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "diya lamp",
            nameHi = "दीपक",
            nameTa = "அகல் விளக்கு",
            keywords = listOf("deepak", "mitti diya", "puja diya", "brass diya", "diwali diya", "agarbatti", "dhoop", "kapoor", "camphor", "cotton wicks", "cycle agarbatti", "moksh", "diya", "lamp", "oil", "दिया", "दीपक", "प्रदीप", "बाती", "அகல்", "எண்ணெய்", "விளக்கு")
        ),
        ProductIconItem(
            iconKey = "bell",
            drawableRes = R.drawable.ic_prod_bell,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "bell",
            nameHi = "घंटी",
            nameTa = "மணி",
            keywords = listOf("puja ghanti", "brass bell", "temple bell", "bell", "घंटी", "ஒலி", "மணி", "மணியொலி")
        ),
        ProductIconItem(
            iconKey = "playing_cards",
            drawableRes = R.drawable.ic_prod_playing_cards,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "joker",
            nameHi = "जोकर कार्ड",
            nameTa = "ஜோக்கர்",
            keywords = listOf("taash", "cards", "rummy cards", "uno cards", "card", "game", "joker", "wildcard", "कार्ड", "जोकर", "ताश का पत्ता", "கருப்பு ஜோக்கர்", "சீட்டு அட்டையில் கருப்பு ஜோக்கர்", "சீட்டுக்கட்டு", "ஜோக்கர்")
        ),
        ProductIconItem(
            iconKey = "dice",
            drawableRes = R.drawable.ic_prod_dice,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "game die",
            nameHi = "पासे, डाइस",
            nameTa = "பகடை விளையாட்டு",
            keywords = listOf("ludo", "snakes and ladders", "board game", "game die", "dice", "die", "game", "गेम", "डाइस", "डाई", "पासे", "पासे, डाइस", "தாயம்", "தாயம் உருட்டுதல்", "பகடை", "விளையாட்டு")
        ),
        ProductIconItem(
            iconKey = "chess_pawn",
            drawableRes = R.drawable.ic_prod_chess_pawn,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "chess pawn",
            nameHi = "शतरंज का प्यादा",
            nameTa = "சதுரங்கச் சிப்பாய்",
            keywords = listOf("shatranj", "chess board", "strategy game", "chess", "chess pawn", "dupe", "expendable", "प्यादा", "शतरंज का प्यादा", "சதுரங்கச் சிப்பாய்", "சதுரங்கம்", "செஸ்", "தியாகம்")
        ),
        ProductIconItem(
            iconKey = "puzzle",
            drawableRes = R.drawable.ic_prod_puzzle,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "puzzle piece",
            nameHi = "पज़ल गेम",
            nameTa = "ஜிக்ஸா",
            keywords = listOf("jigsaw puzzle", "rubiks cube", "educational toy", "clue", "interlocking", "jigsaw", "piece", "puzzle", "इंटरलॉकिंग", "जिग्सॉ", "पज़ल", "पज़ल गेम", "संकेत", "சேர்த்தல்", "துண்டு", "துப்பு", "புதிர்", "ஜிக்ஸா")
        ),
        ProductIconItem(
            iconKey = "video_game",
            drawableRes = R.drawable.ic_prod_video_game,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "video game",
            nameHi = "वीडियो गेम, कंट्रोलर",
            nameTa = "வீடியோ கேம்",
            keywords = listOf("game controller", "joystick", "toy console", "controller", "game", "video game", "कंट्रोलर", "गेम", "वीडियो गेम", "वीडियो गेम, कंट्रोलर", "கன்ட்ரோலர்", "கேம்", "சாதனம்", "வீடியோ கேம்")
        ),
        ProductIconItem(
            iconKey = "yo_yo",
            drawableRes = R.drawable.ic_prod_yo_yo,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "yo-yo",
            nameHi = "यो-यो",
            nameTa = "யோ யோ",
            keywords = listOf("spinning toy", "yoyo", "fluctuate", "toy", "yo-yo", "खिलौना", "खेल", "यो यो", "यो-यो", "लट्टू", "ஏற்ற இறக்கம்", "பொம்மை", "யோ யோ")
        ),
        ProductIconItem(
            iconKey = "kite",
            drawableRes = R.drawable.ic_prod_kite,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "kite",
            nameHi = "पतंग",
            nameTa = "பட்டம்",
            keywords = listOf("patang", "manjha", "sankranti kite", "fly", "kite", "soar", "उड़ान", "पतंग", "உயரப்பறத்தல்", "பட்டம்", "பறப்பது")
        ),
        ProductIconItem(
            iconKey = "nesting_dolls",
            drawableRes = R.drawable.ic_prod_nesting_dolls,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "nesting dolls",
            nameHi = "नेस्टिंग गुड़ियों का समूह",
            nameTa = "அம்மா பொம்மைகள்",
            keywords = listOf("doll", "wooden doll", "barbie doll", "action figure", "nesting", "nesting dolls", "russia", "गुड़िया", "नेस्टिंग", "नेस्टिंग गुड़ियों का समूह", "रूस", "அம்மா", "அம்மா பொம்மைகள்", "பொம்மை", "ரஷியா")
        ),
        ProductIconItem(
            iconKey = "firecracker",
            drawableRes = R.drawable.ic_prod_firecracker,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "firecracker",
            nameHi = "पटाखे",
            nameTa = "வெடி",
            keywords = listOf("patakhe", "diwali crackers", "sparklers", "fuljhadi", "anaar", "chakri", "dynamite", "explosive", "firecracker", "fireworks", "आतिशबाजी", "पटाखे", "बारूद", "विस्फोटक", "டைனமைட்", "வானவேடிக்கை", "வெடி")
        ),
        ProductIconItem(
            iconKey = "sparkler",
            drawableRes = R.drawable.ic_prod_sparkler,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "sparkler",
            nameHi = "फुलझड़ी, आतिशबाजी",
            nameTa = "மத்தாப்பு",
            keywords = listOf("fuljhadi", "sparklers", "sparkler stick", "celebration", "fireworks", "sparkle", "sparkler", "आतिशबाजी", "फुलझड़ी", "फुलझड़ी पटाखा", "फुलझड़ी, आतिशबाजी", "समारोह", "ஒளி", "கொண்டாட்டம்", "பண்டிகை", "மத்தாப்பு")
        ),
        ProductIconItem(
            iconKey = "greeting_card",
            drawableRes = R.drawable.ic_prod_greeting_card,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "love letter",
            nameHi = "प्रेमपत्र",
            nameTa = "காதல் கடிதம்",
            keywords = listOf("birthday card", "greeting card", "invitation card", "heart", "letter", "love", "mail", "दिल", "दिल के चिह्न वाला पत्र", "पत्र", "प्यार", "प्रेमपत्र", "இதயம்", "கடிதம்", "காதல்", "காதல் தூது")
        ),
        ProductIconItem(
            iconKey = "t_shirt",
            drawableRes = R.drawable.ic_prod_t_shirt,
            department = ProductDepartment.GENERAL,
            nameEn = "t-shirt",
            nameHi = "टी–शर्ट",
            nameTa = "டி-சர்ட்",
            keywords = listOf("t shirt", "tee", "cotton t shirt", "banian", "vest", "round neck", "polo", "clothing", "shirt", "t-shirt", "tshirt", "टी–शर्ट", "पोषाक", "शर्ट", "ஆடை அணிகலன்", "சர்ட்", "டி-சர்ட்")
        ),
        ProductIconItem(
            iconKey = "jeans",
            drawableRes = R.drawable.ic_prod_jeans,
            department = ProductDepartment.GENERAL,
            nameEn = "jeans",
            nameHi = "जींस",
            nameTa = "ஜீன்ஸ்",
            keywords = listOf("denim", "trousers", "pants", "cotton pants", "clothing", "jeans", "जींस", "ट्राउज़र", "पैंट्स", "पोषाक", "ஆடைகள்", "காற்சட்டை", "டிரவுசர்ஸ்", "பேன்ட்", "ஜீன்ஸ்")
        ),
        ProductIconItem(
            iconKey = "dress",
            drawableRes = R.drawable.ic_prod_dress,
            department = ProductDepartment.GENERAL,
            nameEn = "dress",
            nameHi = "पोषाक",
            nameTa = "ஆடை",
            keywords = listOf("frock", "kurti", "ladies wear", "salwar", "clothing", "dress", "ड्रेस", "पोषाक", "वन पीस", "ஆடை", "உடை", "பெண்கள் உடை")
        ),
        ProductIconItem(
            iconKey = "socks",
            drawableRes = R.drawable.ic_prod_socks,
            department = ProductDepartment.GENERAL,
            nameEn = "socks",
            nameHi = "मोज़े",
            nameTa = "சாக்ஸ்",
            keywords = listOf("cotton socks", "school socks", "ankle socks", "socks", "stocking", "जुराबें", "मोज़े", "காலுறை", "சாக்ஸ்", "ஸ்டாக்கிங்ஸ்", "ஸ்டைல்")
        ),
        ProductIconItem(
            iconKey = "shoe",
            drawableRes = R.drawable.ic_prod_shoe,
            department = ProductDepartment.GENERAL,
            nameEn = "man’s shoe",
            nameHi = "पुरुषों का जूता",
            nameTa = "ஆண்களுக்கான ஷூ",
            keywords = listOf("formal shoe", "school shoe", "leather shoes", "clothing", "man", "man’s shoe", "shoe", "जूते", "पुरुषों का जूता", "पुरुषों के जूते", "पोषाक", "ஆடை அணிகலன்", "ஆண்களுக்கான ஷூ", "ஆண்கள்", "ஷூ")
        ),
        ProductIconItem(
            iconKey = "sneaker",
            drawableRes = R.drawable.ic_prod_sneaker,
            department = ProductDepartment.GENERAL,
            nameEn = "running shoe",
            nameHi = "एथलेटिक जूता",
            nameTa = "தடகள வீரர்களுக்கான ஷூ",
            keywords = listOf("sports shoe", "running shoe", "canvas shoes", "athletic", "clothing", "shoe", "sneaker", "एथलेटिक जूता", "एथलेटिक जूते", "जूते", "पोषाक", "स्नीकर", "ஆடை அணிகலன்", "தடகள வீரர்களுக்கான ஷூ", "மிருதுவான ஷூ", "ஷூ")
        ),
        ProductIconItem(
            iconKey = "sandal",
            drawableRes = R.drawable.ic_prod_sandal,
            department = ProductDepartment.GENERAL,
            nameEn = "woman’s sandal",
            nameHi = "महिलाओं का सैंडल",
            nameTa = "பெண்கள் செருப்பு",
            keywords = listOf("slippers", "chappal", "flip flop", "hawai chappal", "clothing", "sandal", "shoe", "woman", "woman’s sandal", "पोषाक", "महिला", "महिलाओं का सैंडल", "महिलाओं के सैंडल", "सैंडल", "ஆடை அணிகலன்", "சாண்டல்", "செருப்பு", "பெண்கள்")
        ),
        ProductIconItem(
            iconKey = "hat",
            drawableRes = R.drawable.ic_prod_hat,
            department = ProductDepartment.GENERAL,
            nameEn = "billed cap",
            nameHi = "बिल कैप",
            nameTa = "சின்னமுள்ள தொப்பி",
            keywords = listOf("cap", "sun cap", "cricket cap", "baseball cap", "billed cap", "बिल कैप", "बेसबॉल कैप", "சின்னமுள்ள தொப்பி", "பேஸ்பால் தொப்பி")
        ),
        ProductIconItem(
            iconKey = "sunglasses",
            drawableRes = R.drawable.ic_prod_sunglasses,
            department = ProductDepartment.GENERAL,
            nameEn = "sunglasses",
            nameHi = "धूप का चश्मा",
            nameTa = "சன்கிளாசஸ்",
            keywords = listOf("goggles", "shades", "sun glasses", "dark", "eye", "eyewear", "glasses", "sunglasses", "अंधेरा", "आईवियर", "आंख", "चश्मा", "धूप का चश्मा", "கருப்புக் கண்ணாடி", "சன்கிளாசஸ்", "சன்கிளாஸ்கள்")
        ),
        ProductIconItem(
            iconKey = "glasses",
            drawableRes = R.drawable.ic_prod_glasses,
            department = ProductDepartment.GENERAL,
            nameEn = "glasses",
            nameHi = "चश्मा",
            nameTa = "மூக்குக் கண்ணாடிகள்",
            keywords = listOf("spectacles", "chashma", "reading glasses", "clothing", "eye", "eyeglasses", "eyewear", "glasses", "चश्मा", "पोषाक", "ஆடை அணிகலன்", "கண்ணாடி", "கண்ணாடிகள்", "மூக்குக் கண்ணாடி", "மூக்குக் கண்ணாடிகள்")
        ),
        ProductIconItem(
            iconKey = "umbrella",
            drawableRes = R.drawable.ic_prod_umbrella,
            department = ProductDepartment.GENERAL,
            nameEn = "umbrella",
            nameHi = "छाता",
            nameTa = "குடை",
            keywords = listOf("chatri", "rain umbrella", "folding umbrella", "clothing", "rain", "umbrella", "खुला छाता", "छाता", "मौसम", "குடை", "மழை", "வெயில்")
        ),
        ProductIconItem(
            iconKey = "handbag",
            drawableRes = R.drawable.ic_prod_handbag,
            department = ProductDepartment.GENERAL,
            nameEn = "handbag",
            nameHi = "हैंडबैग",
            nameTa = "ஹேண்ட் பேக்",
            keywords = listOf("purse", "shoulder bag", "ladies bag", "wallet", "bag", "clothing", "handbag", "पोषाक", "बैग", "हैंडबैग", "ஆடை அணிகலன்", "கைப்பை", "பேக்", "பை", "ஹேண்ட் பேக்")
        ),
        ProductIconItem(
            iconKey = "shopping_cart",
            drawableRes = R.drawable.ic_prod_shopping_cart,
            department = ProductDepartment.GENERAL,
            nameEn = "shopping cart",
            nameHi = "शॉपिंग कार्ट",
            nameTa = "ஷாப்பிங் கூடை",
            keywords = listOf("supermarket cart", "trolley", "grocery cart", "dukaan cart", "cart", "shopping", "कार्ट", "ख़रीदारी", "ट्राली", "शॉपिंग कार्ट", "கூடை", "ஷாப்பிங்")
        ),
        ProductIconItem(
            iconKey = "shopping_bags",
            drawableRes = R.drawable.ic_prod_shopping_bags,
            department = ProductDepartment.GENERAL,
            nameEn = "shopping bags",
            nameHi = "शॉपिंग बैग",
            nameTa = "ஷாப்பிங் பேக்ஸ்",
            keywords = listOf("carry bag", "cloth bag", "paper bag", "jute bag", "shopping bag", "bag", "hotel", "shopping", "shopping bags", "खरीदारी", "बैग", "शॉपिंग बैग", "होटल", "பிக் ஷாப்பர் பேக்", "ஷாப்பிங்", "ஷாப்பிங் பேக்குகள்", "ஷாப்பிங் பேக்ஸ்")
        ),
        ProductIconItem(
            iconKey = "package",
            drawableRes = R.drawable.ic_prod_package,
            department = ProductDepartment.GENERAL,
            nameEn = "package",
            nameHi = "पैकेज",
            nameTa = "பேக்கேஜ்",
            keywords = listOf("parcel", "carton", "box", "delivery box", "courier pack", "package", "पार्सल", "पैकेज", "बॉक्स", "பாக்ஸ்", "பார்சல்", "பேக்", "பேக்கேஜ்")
        ),
        ProductIconItem(
            iconKey = "storefront",
            drawableRes = R.drawable.ic_prod_storefront,
            department = ProductDepartment.GENERAL,
            nameEn = "convenience store",
            nameHi = "सुविधा स्टोर",
            nameTa = "சிறிய மார்க்கெட்",
            keywords = listOf("dukaan", "shop", "kirana", "supermarket", "store", "retail", "convenience", "दुकान", "भवन", "सुविधा", "सुविधा स्टोर", "கட்டிடம்", "சிறிய மார்க்கெட்", "சூப்பர் மார்க்கெட்", "ஸ்டோர்")
        ),
        ProductIconItem(
            iconKey = "price_tag",
            drawableRes = R.drawable.ic_prod_price_tag,
            department = ProductDepartment.GENERAL,
            nameEn = "label",
            nameHi = "लेबल",
            nameTa = "லேபிள்",
            keywords = listOf("label", "barcode tag", "offer tag", "discount label", "price sticker", "लेबल", "அட்டை", "மஞ்சள்", "லேபிள்")
        ),
        ProductIconItem(
            iconKey = "receipt",
            drawableRes = R.drawable.ic_prod_receipt,
            department = ProductDepartment.GENERAL,
            nameEn = "receipt",
            nameHi = "रसीद",
            nameTa = "ரசீது",
            keywords = listOf("bill", "cash receipt", "invoice", "payment slip", "accounting", "bookkeeping", "evidence", "proof", "receipt", "प्रमाण", "बहीखाता", "रसीद", "लेखांकन", "साक्ष्य", "ஆதாரம்", "கணக்குவழக்கு", "சாட்சியம்", "ரசீது", "வரவு செலவு")
        ),
        ProductIconItem(
            iconKey = "coin",
            drawableRes = R.drawable.ic_prod_coin,
            department = ProductDepartment.GENERAL,
            nameEn = "coin",
            nameHi = "सिक्का",
            nameTa = "நாணயம்",
            keywords = listOf("rupee", "change", "sikka", "currency", "coin", "gold", "metal", "money", "silver", "treasure", "खजाना", "धन", "धातु", "रजत", "सिक्का", "सोना", "உலோகம்", "தங்கம்", "நாணயம்", "பணம்", "புதையல்", "வெள்ளி")
        ),
        ProductIconItem(
            iconKey = "banknote",
            drawableRes = R.drawable.ic_prod_banknote,
            department = ProductDepartment.GENERAL,
            nameEn = "dollar banknote",
            nameHi = "डॉलर नोट, बैंकनोट",
            nameTa = "டாலர் நோட்டு",
            keywords = listOf("cash", "rupee note", "currency note", "money", "banknote", "bill", "currency", "dollar", "note", "डॉलर", "डॉलर नोट, बैंकनोट", "नोट", "पैसे", "बिल", "बैंकनोट", "அமெரிக்கா", "டாலர்", "டாலர் நோட்டு", "பணம்", "ரூபாய் நோட்டு", "ரூபாய் நோட்டுக் கட்டு")
        ),
        ProductIconItem(
            iconKey = "shopping_basket",
            drawableRes = R.drawable.ic_prod_shopping_basket,
            department = ProductDepartment.GENERAL,
            nameEn = "basket",
            nameHi = "बास्केट",
            nameTa = "கூடை",
            keywords = listOf("tokri", "plastic basket", "hand basket", "basket", "farming", "laundry", "picnic", "किसानी", "पिकनिक", "बास्केट", "लॉन्ड्री", "கூடை", "சலவை", "சுற்றுலா", "விவசாயம்")
        ),
        ProductIconItem(
            iconKey = "delivery_truck",
            drawableRes = R.drawable.ic_prod_delivery_truck,
            department = ProductDepartment.GENERAL,
            nameEn = "delivery truck",
            nameHi = "डिलीवरी ट्रक",
            nameTa = "சரக்கு வண்டி",
            keywords = listOf("home delivery", "tempo", "transport", "logistics", "delivery", "truck", "ट्रक", "डिलीवरी ट्रक", "वाहन", "சரக்கு லாரி", "சரக்கு வண்டி", "டிரக்")
        ),
        ProductIconItem(
            iconKey = "thread",
            drawableRes = R.drawable.ic_prod_thread,
            department = ProductDepartment.GENERAL,
            nameEn = "thread",
            nameHi = "धागा",
            nameTa = "நூல்",
            keywords = listOf("dhaga", "sewing thread", "reel", "needle", "sewing", "spool", "string", "thread", "डोर", "धागा", "रील", "सिलना", "सुई", "ஊசி", "சரம்", "தையல்", "நூல்", "ஸ்பூல்")
        ),
        ProductIconItem(
            iconKey = "yarn",
            drawableRes = R.drawable.ic_prod_yarn,
            department = ProductDepartment.GENERAL,
            nameEn = "yarn",
            nameHi = "सूत",
            nameTa = "நூல்கண்டு",
            keywords = listOf("wool", "knitting yarn", "una", "ball", "crochet", "knit", "yarn", "क्रोशिया", "गेंद", "बुनाई", "सूत", "கொக்கிப்பின்னல்", "நூல்கண்டு", "பந்து", "பின்னல்")
        ),
        ProductIconItem(
            iconKey = "wrench",
            drawableRes = R.drawable.ic_prod_wrench,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "wrench",
            nameHi = "पाना",
            nameTa = "இடுக்கி",
            keywords = listOf("pana", "spanner", "wrench", "hardware", "tool", "औज़ार", "पाना", "रेंच", "रैंच", "இடுக்கி", "கருவி", "டூல்")
        ),
        ProductIconItem(
            iconKey = "nut_and_bolt",
            drawableRes = R.drawable.ic_prod_nut_and_bolt,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "nut and bolt",
            nameHi = "नट और बोल्ट",
            nameTa = "நட்டு மற்றும் போல்ட்",
            keywords = listOf("bolt", "nut", "screw", "washer", "nut and bolt", "tool", "औज़ार", "नट", "नट और बोल्ट", "बोल्ट", "கருவி", "நட்டு", "நட்டு மற்றும் போல்ட்", "போல்ட்", "போல்ட்டும் நட்டும்")
        ),
        ProductIconItem(
            iconKey = "gear",
            drawableRes = R.drawable.ic_prod_gear,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "gear",
            nameHi = "गियर",
            nameTa = "கியர்",
            keywords = listOf("spare part", "machinery", "cog", "cogwheel", "gear", "tool", "गियर", "टूल", "இயந்திரம்", "கருவி", "கியர்", "பொறியியல்")
        ),
        ProductIconItem(
            iconKey = "mouse",
            drawableRes = R.drawable.ic_prod_mouse,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "computer mouse",
            nameHi = "माउस",
            nameTa = "கணினி மவுஸ்",
            keywords = listOf("computer mouse", "optical mouse", "wireless mouse", "computer", "कंप्यूटर माउस", "माउस", "கணினி மவுஸ்", "சுட்டி", "மவுஸ்")
        ),
        ProductIconItem(
            iconKey = "printer",
            drawableRes = R.drawable.ic_prod_printer,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "printer",
            nameHi = "प्रिंटर",
            nameTa = "பிரிண்டர்",
            keywords = listOf("bill printer", "printer", "paper printer", "computer", "कंप्यूटर", "प्रिंटर", "கணினி உபகரணம்", "பிரிண்டர்")
        ),
        ProductIconItem(
            iconKey = "monitor",
            drawableRes = R.drawable.ic_prod_monitor,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "desktop computer",
            nameHi = "डेस्कटॉप",
            nameTa = "டெஸ்க்டாப் கணினி",
            keywords = listOf("desktop", "computer screen", "monitor", "computer", "कंप्यूटर", "डेस्कटॉप", "संगणक", "கணினி", "டெஸ்க்டாப்")
        ),
        ProductIconItem(
            iconKey = "keyboard",
            drawableRes = R.drawable.ic_prod_keyboard,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "keyboard",
            nameHi = "कीबोर्ड",
            nameTa = "விசைப்பலகை",
            keywords = listOf("typing keyboard", "usb keyboard", "computer", "keyboard", "कंप्यूटर", "कीबोर्ड", "கீபோர்டு", "விசைப்பலகை")
        ),
        ProductIconItem(
            iconKey = "satellite_antenna",
            drawableRes = R.drawable.ic_prod_satellite_antenna,
            department = ProductDepartment.ELECTRONICS,
            nameEn = "satellite antenna",
            nameHi = "सैटेलाइट एंटेना",
            nameTa = "சாட்டிலைட் ஆன்டனா",
            keywords = listOf("dish antenna", "dth", "tata play", "antenna", "dish", "satellite", "उपग्रह", "डिश", "सैटेलाइट एंटेना", "सैटेलाइट डिश", "ஆன்டனா", "சாட்டிலைட் ஆன்டனா", "டிஷ்")
        ),
        ProductIconItem(
            iconKey = "document_page",
            drawableRes = R.drawable.ic_prod_document_page,
            department = ProductDepartment.STATIONERY,
            nameEn = "page facing up",
            nameHi = "ऊपरी पृष्ठ",
            nameTa = "நேர் ஆவணம்",
            keywords = listOf("a4 paper", "print paper", "xerox paper", "paper sheet", "document", "page", "page facing up", "ऊपरी पृष्ठ", "दस्तावेज़", "पृष्ठ", "सामने की ओर पृष्ठ", "திறந்த ஆவணம்", "நேர் ஆவணம்")
        ),
        ProductIconItem(
            iconKey = "bar_chart",
            drawableRes = R.drawable.ic_prod_bar_chart,
            department = ProductDepartment.STATIONERY,
            nameEn = "bar chart",
            nameHi = "बार चार्ट",
            nameTa = "பட்டை வரைபடம்",
            keywords = listOf("accounts", "sales chart", "ledger", "bar", "chart", "graph", "चार्ट", "बार ग्राफ़", "बार चार्ट", "பட்டை வரைபடம்", "பார் கிராப்", "பார் சார்ட்", "பார் வரைபடம்")
        ),
        ProductIconItem(
            iconKey = "bookmark",
            drawableRes = R.drawable.ic_prod_bookmark,
            department = ProductDepartment.STATIONERY,
            nameEn = "bookmark",
            nameHi = "बुकमार्क",
            nameTa = "புக்மார்க்",
            keywords = listOf("ribbon marker", "page marker", "bookmark", "mark", "निशान", "बुकमार्क", "பக்க அடையாளம்", "பட்டி", "புக்மார்க்", "புத்தகக்குறி")
        ),
        ProductIconItem(
            iconKey = "magnifying_glass",
            drawableRes = R.drawable.ic_prod_magnifying_glass,
            department = ProductDepartment.STATIONERY,
            nameEn = "magnifying glass tilted left",
            nameHi = "बाईं ओर झुका आवर्धक काँच",
            nameTa = "இடது சாய்வு லென்ஸ்",
            keywords = listOf("lens", "magnifier", "reading glass", "glass", "magnifying", "magnifying glass tilted left", "search", "tool", "आवर्धक काँच", "औज़ार", "काँच", "खोज", "बाईं ओर झुका आवर्धक काँच", "இடது சாய்வு லென்ஸ்", "தேடல்", "தேடு", "லென்ஸ்")
        ),
        ProductIconItem(
            iconKey = "scroll",
            drawableRes = R.drawable.ic_prod_scroll,
            department = ProductDepartment.STATIONERY,
            nameEn = "scroll",
            nameHi = "स्क्रॉल, पेपर स्क्रॉल",
            nameTa = "சுருள்",
            keywords = listOf("parchment", "certificate", "roll", "paper", "scroll", "काग़ज़", "पेपर स्क्रॉल", "स्क्रॉल", "स्क्रॉल, पेपर स्क्रॉल", "स्क्रोल", "சுருட்டு", "சுருள்", "பேப்பரை சுருட்டு")
        ),
        ProductIconItem(
            iconKey = "briefcase",
            drawableRes = R.drawable.ic_prod_briefcase,
            department = ProductDepartment.STATIONERY,
            nameEn = "briefcase",
            nameHi = "ब्रीफ़केस",
            nameTa = "சூட்கேஸ்",
            keywords = listOf("office bag", "attache", "document briefcase", "briefcase", "ब्रीफ़केस", "सूटकेस", "சூட்கேஸ்", "பெட்டி")
        ),
        ProductIconItem(
            iconKey = "postal_horn",
            drawableRes = R.drawable.ic_prod_postal_horn,
            department = ProductDepartment.STATIONERY,
            nameEn = "postal horn",
            nameHi = "पोस्टल हॉर्न",
            nameTa = "போஸ்டல் ஹார்ன்",
            keywords = listOf("post", "courier", "speed post", "horn", "postal", "पोस्टल हॉर्न", "हॉर्न", "போஸ்டல்", "போஸ்டல் ஹார்ன்")
        ),
        ProductIconItem(
            iconKey = "incoming_envelope",
            drawableRes = R.drawable.ic_prod_incoming_envelope,
            department = ProductDepartment.STATIONERY,
            nameEn = "incoming envelope",
            nameHi = "इनकमिंग लिफ़ाफ़ा",
            nameTa = "பெற்ற மெயில்",
            keywords = listOf("mail", "letter", "dak", "e-mail", "email", "envelope", "incoming", "receive", "आवक", "इनकमिंग", "इनकमिंग लिफ़ाफ़ा", "ईमेल", "पत्र", "मेल", "लिफाफा", "संचार", "இ-கடிதம்", "கடிதம்", "பெற்ற மெயில்", "வந்த மெயில்")
        ),
        ProductIconItem(
            iconKey = "outbox_tray",
            drawableRes = R.drawable.ic_prod_outbox_tray,
            department = ProductDepartment.STATIONERY,
            nameEn = "outbox tray",
            nameHi = "आउटबॉक्स ट्रे",
            nameTa = "அவுட்பாக்ஸ் தட்டு",
            keywords = listOf("paper tray", "desk organizer", "box", "letter", "mail", "outbox", "sent", "tray", "आउटबॉक्स", "चिट्ठी", "ट्रे", "बॉक्स", "मेल", "அஞ்சல் பெட்டி", "அவுட்பாக்ஸ்", "தட்டு", "பெட்டி")
        ),
        ProductIconItem(
            iconKey = "inbox_tray",
            drawableRes = R.drawable.ic_prod_inbox_tray,
            department = ProductDepartment.STATIONERY,
            nameEn = "inbox tray",
            nameHi = "इनबॉक्स ट्रे",
            nameTa = "இன்பாக்ஸ் தட்டு",
            keywords = listOf("in tray", "document tray", "box", "inbox", "letter", "mail", "receive", "tray", "इनबॉक्स", "ट्रे", "प्राप्त", "बॉक्स", "मेल", "संचार", "அஞ்சல் பெட்டி", "இன்பாக்ஸ்", "தட்டு", "பெட்டி")
        ),
        ProductIconItem(
            iconKey = "matches_fire",
            drawableRes = R.drawable.ic_prod_matches_fire,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "fire",
            nameHi = "आग",
            nameTa = "தீ",
            keywords = listOf("maachis", "matchbox", "matchstick", "safety matches", "fire", "flame", "tool", "आग", "लपट", "தீ", "நெருப்பு")
        ),
        ProductIconItem(
            iconKey = "sparkles",
            drawableRes = R.drawable.ic_prod_sparkles,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "sparkles",
            nameHi = "चमकदार तारे",
            nameTa = "ஒளிரும் நட்சத்திரம்",
            keywords = listOf("glitter", "sparkle", "festive", "*", "sparkles", "star", "चमक", "चमकदार तारे", "चमचम", "तारे", "ஒளிரும் நட்சத்திரம்", "கொண்டாட்டம்", "நட்சத்திரம்")
        ),
        ProductIconItem(
            iconKey = "holiday_tree",
            drawableRes = R.drawable.ic_prod_holiday_tree,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "Christmas tree",
            nameHi = "क्रिसमस ट्री",
            nameTa = "கிறிஸ்துமஸ் மரம்",
            keywords = listOf("christmas tree", "festival tree", "decoration", "celebration", "christmas", "tree", "उत्सव", "क्रिसमस ट्री", "पेड़", "समारोह", "கிறிஸ்துமஸ் காலம்", "கிறிஸ்துமஸ் மரம்", "பசுமை", "மரம்")
        ),
        ProductIconItem(
            iconKey = "jack_o_lantern",
            drawableRes = R.drawable.ic_prod_jack_o_lantern,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "jack-o-lantern",
            nameHi = "जैक–ओ–लैंटर्न, कंदील",
            nameTa = "பூசணிக்காய் முகம்",
            keywords = listOf("pumpkin", "halloween", "celebration", "jack", "jack-o-lantern", "lantern", "कंदील", "कद्दू", "जैक", "जैक–ओ–लैंटर्न", "जैक–ओ–लैंटर्न, कंदील", "लालटेन", "समारोह", "हैलोवीन", "கொண்டாட்டம்", "பூசணிக்காய்", "பூசணிக்காய் முகம்", "ஹேலோவின்")
        ),
        ProductIconItem(
            iconKey = "red_envelope",
            drawableRes = R.drawable.ic_prod_red_envelope,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "red envelope",
            nameHi = "लाल लिफ़ाफ़ा",
            nameTa = "சிவப்பு உறை",
            keywords = listOf("shagun packet", "cash envelope", "lifafa", "gift", "good luck", "hóngbāo", "lai see", "money", "red envelope", "उपहार", "धन", "लाई सी", "लाल लिफ़ाफ़ा", "शुभेच्छा", "होंगबाओ", "சன்மானம்", "சிவப்பு உறை", "பணம்", "பரிசு", "பரிசு கவர்", "வாழ்த்து")
        ),
        ProductIconItem(
            iconKey = "firecracker_roll",
            drawableRes = R.drawable.ic_prod_firecracker_roll,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "firecracker",
            nameHi = "पटाखे",
            nameTa = "வெடி",
            keywords = listOf("ladi", "diwali patakha", "crackers", "dynamite", "explosive", "firecracker", "fireworks", "आतिशबाजी", "पटाखे", "बारूद", "विस्फोटक", "டைனமைட்", "வானவேடிக்கை", "வெடி")
        ),
        ProductIconItem(
            iconKey = "party_hat",
            drawableRes = R.drawable.ic_prod_party_hat,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "party popper",
            nameHi = "पार्टी पॉपर",
            nameTa = "வண்ண வேடிக்கை",
            keywords = listOf("birthday cap", "party hat", "celebration", "party", "popper", "tada", "पार्टी", "पॉपर", "समारोह", "கொண்டாட்டம்", "வண்ண வேடிக்கை", "விருந்து", "வெற்றி அறிவிப்பு")
        ),
        ProductIconItem(
            iconKey = "crystal_ball",
            drawableRes = R.drawable.ic_prod_crystal_ball,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "crystal ball",
            nameHi = "क्रिस्टल बॉल, भविष्य देखना",
            nameTa = "கண்ணாடி பந்து",
            keywords = listOf("magic ball", "toy", "ball", "crystal", "fairy tale", "fantasy", "fortune", "tool", "क्रिस्टल", "क्रिस्टल बॉल", "क्रिस्टल बॉल, भविष्य देखना", "गेंद", "भविष्य", "भविष्य देखना", "கண்ணாடி பந்து", "கிறிஸ்டல் பால்", "பந்து")
        ),
        ProductIconItem(
            iconKey = "magic_wand",
            drawableRes = R.drawable.ic_prod_magic_wand,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "magic wand",
            nameHi = "जादू की छड़ी",
            nameTa = "மந்திரக் கோல்",
            keywords = listOf("toy wand", "fairy stick", "magic", "magic wand", "witch", "wizard", "जादू", "जादू की छड़ी", "जादूगर", "जादूगरनी", "சூனியக்காரி", "மந்திரக் கோல்", "மந்திரம்", "மந்திரவாதி")
        ),
        ProductIconItem(
            iconKey = "teddy_toy",
            drawableRes = R.drawable.ic_prod_teddy_toy,
            department = ProductDepartment.TOYS_GIFTS,
            nameEn = "teddy bear",
            nameHi = "टेडी बेअर",
            nameTa = "டெடிபியர்",
            keywords = listOf("plushie", "stuffed animal", "plaything", "plush", "stuffed", "teddy bear", "toy", "खिलौना", "टेडी बेअर", "टेडीबियर", "टॉय", "भव्य", "स्टफ़्ड", "டெடிபியர்", "பஞ்சு", "பட்டு", "பொம்மை", "விளையாட்டுப்பொருள்")
        ),
        ProductIconItem(
            iconKey = "kitchen_pot",
            drawableRes = R.drawable.ic_prod_kitchen_pot,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "pot of food",
            nameHi = "स्ट्यू, भोजन पात्र",
            nameTa = "பாத்திரம் நிறைய உணவு",
            keywords = listOf("handi", "stew pot", "soup pot", "pot", "pot of food", "stew", "भोजन पात्र", "रेस्टोरेंट", "स्ट्यू", "स्ट्यू, भोजन पात्र", "உணவகம்", "உணவு", "சூடான உணவு", "பாத்திரத்தில் உணவு", "பாத்திரம் நிறைய உணவு")
        ),
        ProductIconItem(
            iconKey = "frying_pan_shallow",
            drawableRes = R.drawable.ic_prod_frying_pan_shallow,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "cooking",
            nameHi = "भोजन पकाना",
            nameTa = "சமையல்",
            keywords = listOf("omelette pan", "skillet", "tawa", "breakfast", "cooking", "egg", "frying", "pan", "फ़्राइंग पैन", "भोजन पकाना", "रेस्टोरेंट", "உணவு", "காஃப் பாயில்", "காஃப் பாயில் முட்டை", "சமையல்", "முட்டை")
        ),
        ProductIconItem(
            iconKey = "tea_kettle",
            drawableRes = R.drawable.ic_prod_tea_kettle,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "teapot",
            nameHi = "चाय की केतली",
            nameTa = "தேநீர்க் குவளை",
            keywords = listOf("water kettle", "tea pot", "chai kettle", "drink", "pot", "tea", "teapot", "चाय", "चाय की केतली", "पीने की चीज़", "बर्तन", "குவளை", "தேநீர்", "தேநீர்க் குவளை", "பானம்")
        ),
        ProductIconItem(
            iconKey = "katori_bowl",
            drawableRes = R.drawable.ic_prod_katori_bowl,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "bowl with spoon",
            nameHi = "चम्मच के साथ कटोरी",
            nameTa = "ஸ்பூன் உள்ள பாத்திரம்",
            keywords = listOf("soup bowl", "cereal bowl", "steel katori", "bowl with spoon", "breakfast", "cereal", "congee", "अनाज", "चम्मच के साथ कटोरी", "चावल की दलिया", "नाश्ता", "காலை உணவு", "ப்ரேக்ஃபாஸ்ட்", "ஸ்பூன் உள்ள பாத்திரம்")
        ),
        ProductIconItem(
            iconKey = "cutlery_set",
            drawableRes = R.drawable.ic_prod_cutlery_set,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "fork and knife",
            nameHi = "फ़ोर्क और छुरी–चाकू",
            nameTa = "முள்கரண்டியும் கத்தியும்",
            keywords = listOf("spoon and fork", "eating utensils", "cooking", "cutlery", "fork", "fork and knife", "knife", "छुरी–चाकू", "नाइफ़", "फ़ोर्क", "फ़ोर्क और छुरी–चाकू", "रेस्टोरेंट", "உணவகம்", "உணவு", "உணவு நேரம்", "முள்கரண்டியும் கத்தியும்")
        ),
        ProductIconItem(
            iconKey = "disposable_cup",
            drawableRes = R.drawable.ic_prod_disposable_cup,
            department = ProductDepartment.KITCHENWARE,
            nameEn = "cup with straw",
            nameHi = "स्ट्रॉ के साथ कप",
            nameTa = "ஸ்ட்ரா உள்ள கோப்பை",
            keywords = listOf("paper cup", "chai glass", "disposable glass", "cup with straw", "juice", "soda", "जूस", "सोडा", "स्ट्रॉ के साथ कप", "சோடா", "பழச்சாறு", "ஜூஸ்", "ஸ்ட்ரா உள்ள கோப்பை")
        ),
        ProductIconItem(
            iconKey = "shaving_blade",
            drawableRes = R.drawable.ic_prod_shaving_blade,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "razor",
            nameHi = "उस्तरा",
            nameTa = "ரேஸர்",
            keywords = listOf("gillette mach 3", "shaving cartridge", "razor", "sharp", "shave", "उस्तरा", "छुरा", "छुरी", "ब्लेड", "रेज़र", "शेव", "शेविंग", "கூர்மென்கத்தி", "சவரக்கத்தி", "ரேஸர்")
        ),
        ProductIconItem(
            iconKey = "cotton_swab",
            drawableRes = R.drawable.ic_prod_cotton_swab,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "safety pin",
            nameHi = "सेफ़्टी पिन",
            nameTa = "ஊக்கு",
            keywords = listOf("q tips", "ear cleaning buds", "diaper", "punk rock", "safety pin", "डायपर", "पंक रॉक", "सेफ़्टी पिन", "ஊக்கு", "டயபர்", "பங்க் ராக்")
        ),
        ProductIconItem(
            iconKey = "perfume_drop",
            drawableRes = R.drawable.ic_prod_perfume_drop,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "droplet",
            nameHi = "ठंडा पसीना",
            nameTa = "வியர்வை",
            keywords = listOf("attar", "fragrance", "cologne", "cold", "comic", "drop", "droplet", "sweat", "कॉमिक", "ठंडा पसीना", "पसीना", "पसीनेदार", "குளிர்", "துளி", "வியர்வை")
        ),
        ProductIconItem(
            iconKey = "eye_glasses",
            drawableRes = R.drawable.ic_prod_eye_glasses,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "glasses",
            nameHi = "चश्मा",
            nameTa = "மூக்குக் கண்ணாடிகள்",
            keywords = listOf("spectacle frame", "zero power", "clothing", "eye", "eyeglasses", "eyewear", "glasses", "चश्मा", "पोषाक", "ஆடை அணிகலன்", "கண்ணாடி", "கண்ணாடிகள்", "மூக்குக் கண்ணாடி", "மூக்குக் கண்ணாடிகள்")
        ),
        ProductIconItem(
            iconKey = "sun_glasses",
            drawableRes = R.drawable.ic_prod_sun_glasses,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "sunglasses",
            nameHi = "धूप का चश्मा",
            nameTa = "சன்கிளாசஸ்",
            keywords = listOf("uv sunglasses", "polaroid", "dark", "eye", "eyewear", "glasses", "sunglasses", "अंधेरा", "आईवियर", "आंख", "चश्मा", "धूप का चश्मा", "கருப்புக் கண்ணாடி", "சன்கிளாசஸ்", "சன்கிளாஸ்கள்")
        ),
        ProductIconItem(
            iconKey = "footwear_chappal",
            drawableRes = R.drawable.ic_prod_footwear_chappal,
            department = ProductDepartment.PERSONAL_CARE,
            nameEn = "woman’s sandal",
            nameHi = "महिलाओं का सैंडल",
            nameTa = "பெண்கள் செருப்பு",
            keywords = listOf("bata chappal", "paragon", "relaxo", "clothing", "sandal", "shoe", "woman", "woman’s sandal", "पोषाक", "महिला", "महिलाओं का सैंडल", "महिलाओं के सैंडल", "सैंडल", "ஆடை அணிகலன்", "சாண்டல்", "செருப்பு", "பெண்கள்")
        ),
        ProductIconItem(
            iconKey = "adhesive_tape",
            drawableRes = R.drawable.ic_prod_adhesive_tape,
            department = ProductDepartment.HEALTH,
            nameEn = "adhesive bandage",
            nameHi = "चिपकाने वाली पट्‍टी",
            nameTa = "ஒட்டக்கூடிய பேண்டேஜ்",
            keywords = listOf("micropore", "medical tape", "cotton gauze", "adhesive bandage", "bandage", "चिपकाने वाली पट्‍टी", "पट्टी", "बैंड एड", "बैंडेज", "ஒட்டக்கூடிய பேண்டேஜ்", "காயம்", "பேண்டேஜ்")
        ),
        ProductIconItem(
            iconKey = "dropper",
            drawableRes = R.drawable.ic_prod_dropper,
            department = ProductDepartment.HEALTH,
            nameEn = "drop of blood",
            nameHi = "ख़ून की बूँद",
            nameTa = "இரத்தத் துளி",
            keywords = listOf("eye drops", "nasal drops", "ear drops", "bleed", "blood donation", "drop of blood", "injury", "medicine", "menstruation", "ख़ून", "ख़ून की बूँद", "चिकित्सा", "रक्त", "रक्तदान", "இரத்த தானம்", "இரத்தத் துளி", "மருந்துவம்", "மாதவிடாய்")
        ),
        ProductIconItem(
            iconKey = "stethoscope_tool",
            drawableRes = R.drawable.ic_prod_stethoscope_tool,
            department = ProductDepartment.HEALTH,
            nameEn = "stethoscope",
            nameHi = "स्टेथोस्कोप",
            nameTa = "ஸ்டெதாஸ்கோப்",
            keywords = listOf("health check", "heart monitor", "doctor", "heart", "medicine", "stethoscope", "चिकित्सा", "डॉक्टर", "ब्लड प्रेशर", "रक्तचाप", "स्टेथोस्कोप", "இதயத்துடிப்பு", "இதயம்", "மருத்துவம்", "மருத்துவர்", "ஸ்டெதாஸ்கோப்")
        ),
        ProductIconItem(
            iconKey = "fever_thermometer",
            drawableRes = R.drawable.ic_prod_fever_thermometer,
            department = ProductDepartment.HEALTH,
            nameEn = "thermometer",
            nameHi = "थर्मामीटर",
            nameTa = "வெப்பநிலைமானி",
            keywords = listOf("mercury thermometer", "fever meter", "thermometer", "weather", "थर्मामीटर", "मौसम", "உடல்நிலை", "வெப்பநிலைமானி")
        ),
        ProductIconItem(
            iconKey = "blood_drop",
            drawableRes = R.drawable.ic_prod_blood_drop,
            department = ProductDepartment.HEALTH,
            nameEn = "drop of blood",
            nameHi = "ख़ून की बूँद",
            nameTa = "இரத்தத் துளி",
            keywords = listOf("blood test", "glucometer strips", "sugar check", "bleed", "blood donation", "drop of blood", "injury", "medicine", "menstruation", "ख़ून", "ख़ून की बूँद", "चिकित्सा", "रक्त", "रक्तदान", "இரத்த தானம்", "இரத்தத் துளி", "மருந்துவம்", "மாதவிடாய்")
        ),
        ProductIconItem(
            iconKey = "tea_toast",
            drawableRes = R.drawable.ic_prod_tea_toast,
            department = ProductDepartment.BAKERY,
            nameEn = "bread",
            nameHi = "ब्रेड",
            nameTa = "பிரெட்",
            keywords = listOf("rusk", "suji rusk", "britannia toastea", "milk toast", "cake rusk", "bread", "loaf", "ब्रेड", "ब्रेड लोफ़", "रेस्टोरेंट", "உணவகம்", "உணவு", "பிரெட்", "ரொட்டி")
        ),
        ProductIconItem(
            iconKey = "pav_bun",
            drawableRes = R.drawable.ic_prod_pav_bun,
            department = ProductDepartment.BAKERY,
            nameEn = "hamburger",
            nameHi = "हैमबर्गर",
            nameTa = "பர்கர்",
            keywords = listOf("vada pav bun", "pav bhaji bun", "dinner rolls", "burger", "hamburger", "बर्गर", "रेस्टोरेंट", "हैमबर्गर", "உணவகம்", "உணவு", "பர்கர்", "ஹம்பர்கர்")
        ),
        ProductIconItem(
            iconKey = "paratha_bread",
            drawableRes = R.drawable.ic_prod_paratha_bread,
            department = ProductDepartment.BAKERY,
            nameEn = "flatbread",
            nameHi = "फ़्लैटब्रेड",
            nameTa = "ஃபிளாட் பிரட்",
            keywords = listOf("malabar parotta", "frozen paratha", "laccha paratha", "arepa", "flatbread", "lavash", "naan", "pita", "अरेपा", "नान", "पीता", "फ़्लैटब्रेड", "लवाश", "அரேபா", "ஃபிளாட் பிரட்", "நான்", "பிடா", "லாவாஷ்")
        ),
        ProductIconItem(
            iconKey = "nacho_chips",
            drawableRes = R.drawable.ic_prod_nacho_chips,
            department = ProductDepartment.SNACKS,
            nameEn = "taco",
            nameHi = "टैको",
            nameTa = "டாகோ",
            keywords = listOf("cheese nachos", "corn crisps", "tortilla", "mexican", "taco", "टैको", "मैक्सिकन", "உணவு", "டாகோ", "மெக்சிகன் உணவு")
        ),
        ProductIconItem(
            iconKey = "kathi_roll",
            drawableRes = R.drawable.ic_prod_kathi_roll,
            department = ProductDepartment.SNACKS,
            nameEn = "burrito",
            nameHi = "बरिटो",
            nameTa = "புரிட்டோ",
            keywords = listOf("paneer roll", "chicken roll", "egg roll", "burrito", "mexican", "wrap", "बरिटो", "मैक्सिकन", "உணவு", "புரிட்டோ", "மெக்சிகன் உணவு")
        ),
        ProductIconItem(
            iconKey = "tea_biscuit",
            drawableRes = R.drawable.ic_prod_tea_biscuit,
            department = ProductDepartment.SNACKS,
            nameEn = "cookie",
            nameHi = "कुकी",
            nameTa = "குக்கீ",
            keywords = listOf("chai biscuit", "tea snack", "rusk", "cookie", "dessert", "sweet", "कुकी", "रेस्टोरेंट", "உணவகம்", "உணவு", "குக்கீ", "ரொட்டி")
        ),
        ProductIconItem(
            iconKey = "salted_peanuts",
            drawableRes = R.drawable.ic_prod_salted_peanuts,
            department = ProductDepartment.SNACKS,
            nameEn = "peanuts",
            nameHi = "मूँगफली",
            nameTa = "வேர்க்கடலை",
            keywords = listOf("haldiram salted peanuts", "roasted chana", "snack nuts", "food", "nut", "peanut", "peanuts", "vegetable", "अखरोट", "खाना", "मूँगफली", "सब्जी", "உணவு", "கடலை", "காய்கறி", "வேர்க்கடலை")
        ),
        ProductIconItem(
            iconKey = "mango_frooti",
            drawableRes = R.drawable.ic_prod_mango_frooti,
            department = ProductDepartment.BEVERAGES,
            nameEn = "cup with straw",
            nameHi = "स्ट्रॉ के साथ कप",
            nameTa = "ஸ்ட்ரா உள்ள கோப்பை",
            keywords = listOf("frooti", "maaza", "mango drink", "paper boat", "cup with straw", "juice", "soda", "जूस", "सोडा", "स्ट्रॉ के साथ कप", "சோடா", "பழச்சாறு", "ஜூஸ்", "ஸ்ட்ரா உள்ள கோப்பை")
        ),
        ProductIconItem(
            iconKey = "cola_soft_drink",
            drawableRes = R.drawable.ic_prod_cola_soft_drink,
            department = ProductDepartment.BEVERAGES,
            nameEn = "cup with straw",
            nameHi = "स्ट्रॉ के साथ कप",
            nameTa = "ஸ்ட்ரா உள்ள கோப்பை",
            keywords = listOf("cold drink", "soda", "carbonated drink", "cup with straw", "juice", "जूस", "सोडा", "स्ट्रॉ के साथ कप", "சோடா", "பழச்சாறு", "ஜூஸ்", "ஸ்ட்ரா உள்ள கோப்பை")
        ),
        ProductIconItem(
            iconKey = "energy_drink_can",
            drawableRes = R.drawable.ic_prod_energy_drink_can,
            department = ProductDepartment.BEVERAGES,
            nameEn = "cup with straw",
            nameHi = "स्ट्रॉ के साथ कप",
            nameTa = "ஸ்ட்ரா உள்ள கோப்பை",
            keywords = listOf("red bull", "sting energy", "monster", "cup with straw", "juice", "soda", "जूस", "सोडा", "स्ट्रॉ के साथ कप", "சோடா", "பழச்சாறு", "ஜூஸ்", "ஸ்ட்ரா உள்ள கோப்பை")
        ),
        ProductIconItem(
            iconKey = "mineral_water_pack",
            drawableRes = R.drawable.ic_prod_mineral_water_pack,
            department = ProductDepartment.BEVERAGES,
            nameEn = "ice",
            nameHi = "आइस क्यूब",
            nameTa = "ஐஸ் கட்டி",
            keywords = listOf("packaged water", "20l jar", "water bottle", "cold", "ice", "ice cube", "iceberg", "आइस क्यूब", "आइसबर्ग", "ठंडा", "बर्फ़", "ஐஸ்", "ஐஸ் கட்டி")
        ),
        ProductIconItem(
            iconKey = "badam_milk",
            drawableRes = R.drawable.ic_prod_badam_milk,
            department = ProductDepartment.BEVERAGES,
            nameEn = "glass of milk",
            nameHi = "दूध का गिलास",
            nameTa = "பால்",
            keywords = listOf("almond milk", "kesar milk", "amul kool", "drink", "glass", "glass of milk", "milk", "ग्लास", "दूध", "दूध का गिलास", "पेय", "பால்", "பானம்")
        ),
        ProductIconItem(
            iconKey = "chaas_buttermilk",
            drawableRes = R.drawable.ic_prod_chaas_buttermilk,
            department = ProductDepartment.BEVERAGES,
            nameEn = "glass of milk",
            nameHi = "दूध का गिलास",
            nameTa = "பால்",
            keywords = listOf("masala chaas", "amul masti chaas", "mattha", "moru", "drink", "glass", "glass of milk", "milk", "ग्लास", "दूध", "दूध का गिलास", "पेय", "பால்", "பானம்")
        ),
        ProductIconItem(
            iconKey = "paneer_pack",
            drawableRes = R.drawable.ic_prod_paneer_pack,
            department = ProductDepartment.DAIRY,
            nameEn = "cheese wedge",
            nameHi = "पनीर का टुकड़ा",
            nameTa = "பாலாடைக் கட்டி",
            keywords = listOf("fresh paneer", "malai paneer", "amul paneer", "gowardhan paneer", "cheese", "cheese wedge", "पनीर", "पनीर का टुकड़ा", "சீஸ்", "பாலாடை", "பாலாடைக் கட்டி", "பாலேடு")
        ),
        ProductIconItem(
            iconKey = "ghee_jar",
            drawableRes = R.drawable.ic_prod_ghee_jar,
            department = ProductDepartment.DAIRY,
            nameEn = "butter",
            nameHi = "मक्खन",
            nameTa = "வெண்ணெய்",
            keywords = listOf("amul ghee", "cow ghee", "shudh desi ghee", "patanjali ghee", "aashirvaad svasti ghee", "mother dairy ghee", "grb ghee", "butter", "dairy", "दूध से बने पदार्थ", "मक्खन", "பால் பொருட்கள்", "வெண்ணெய்")
        ),
    )

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
