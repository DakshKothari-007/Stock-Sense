package com.simats.stocksense.feature.inventory.icon

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit test suite for [ProductIconMatcher] and [ProductIconCatalog].
 * Covers 70+ test cases across English, Hindi, Tamil, Hinglish, Tanglish, FMCG brands,
 * typos (Levenshtein distance 1), specificity scoring, edge cases (empty, symbols, numbers),
 * and fallback resolution.
 */
class ProductIconMatcherTest {

    data class TestCase(
        val input: String,
        val expectedIconKey: String,
        val description: String,
        val category: String? = null
    )

    private val positiveTestCases = listOf(
        // 1. English Supermarket Staples & Produce
        TestCase("Shimla Red Apples 1kg", "apple", "English fruit - Apple"),
        TestCase("Cavendish Bananas 1 dozen", "banana", "English fruit - Banana"),
        TestCase("Sweet Green Grapes 500g", "grapes", "English fruit - Grapes"),
        TestCase("Fresh Red Tomatoes 1kg", "tomato", "English vegetable - Tomato"),
        TestCase("Crisp Fresh Broccoli", "broccoli", "English vegetable - Broccoli"),
        TestCase("White Farm Eggs 6 pack", "egg", "English grocery - Egg"),
        TestCase("Fresh Cow Milk 1L", "milk_bottle", "English dairy - Milk"),
        TestCase("Salted Table Butter 100g", "butter", "English dairy - Butter"),
        TestCase("Cheddar Cheese Block 200g", "cheese", "English dairy - Cheese"),
        TestCase("Plain White Bread 400g", "bread", "English bakery - Bread"),
        TestCase("Rich Chocolate Cookies 120g", "cookie", "English snack - Cookie/Biscuit"),
        TestCase("Pure Dark Chocolate Bar", "chocolate", "English confectionery - Chocolate"),
        TestCase("Organic Green Tea 25 bags", "green_tea", "English beverage - Green Tea"),
        TestCase("Dark Roast Ground Coffee 200g", "tea_cup", "English beverage - Coffee"),
        TestCase("Cold Pressed Sunflower Oil 1L", "oil_drop", "English pantry - Cooking Oil"),
        TestCase("Whole Wheat Bread", "bread", "English bakery - Bread"),
        TestCase("Fresh Lemon Citrus 4 pcs", "lemon", "English fruit - Lemon"),
        TestCase("Juicy Strawberries 250g", "strawberry", "English fruit - Strawberry"),
        TestCase("Crispy Potato Chips 90g", "potato_chips", "English snack - Chips"),
        TestCase("Fresh Whole Chicken", "chicken_whole", "Meat - Chicken"),
        TestCase("Fresh Cut Fish Fillet", "fish_cake", "Meat - Fish"),

        // 2. Popular Indian FMCG Brands
        TestCase("Amul Taaza Toned Milk 500ml", "milk_bottle", "FMCG Brand - Amul Milk"),
        TestCase("Amul Pasteurised Butter 100g", "butter", "FMCG Brand - Amul Butter"),
        TestCase("Britannia Good Day Butter Cookies", "cookie", "FMCG Brand - Britannia Good Day"),
        TestCase("Britannia Treat Jim Jam", "cookie", "FMCG Brand - Britannia Jim Jam"),
        TestCase("Parle-G Glucose Biscuits 250g", "cookie", "FMCG Brand - Parle-G"),
        TestCase("Aashirvaad Superior Sharbati Atta 5kg", "bowl_flour", "FMCG Brand - Aashirvaad Atta"),
        TestCase("Tata Salt Vacuum Evaporated 1kg", "salt", "FMCG Brand - Tata Salt"),
        TestCase("Tata Tea Premium 500g", "tea_cup", "FMCG Brand - Tata Tea"),
        TestCase("Red Label Natural Care Tea 250g", "tea_cup", "FMCG Brand - Red Label Tea"),
        TestCase("Nescafe Classic Instant Coffee 50g", "tea_cup", "FMCG Brand - Nescafe Coffee"),
        TestCase("Bru Instant Coffee 100g", "tea_cup", "FMCG Brand - Bru Coffee"),
        TestCase("Maggi 2-Minute Masala Noodles 280g", "noodles", "FMCG Brand - Maggi Noodles"),
        TestCase("Cadbury Dairy Milk Silk 150g", "chocolate", "FMCG Brand - Cadbury Dairy Milk"),
        TestCase("Fortune Sunlite Refined Sunflower Oil 1L", "oil_drop", "FMCG Brand - Fortune Oil"),
        TestCase("Saffola Gold Pro Healthy Lifestyle Oil 1L", "oil_drop", "FMCG Brand - Saffola Oil"),
        TestCase("India Gate Super Basmati Rice 1kg", "sheaf_of_rice", "FMCG Brand - India Gate Rice"),
        TestCase("Colgate Total Dental Cream 150g", "toothbrush", "Personal Care - Colgate"),
        TestCase("Dettol Antiseptic Liquid 250ml", "bath_soap", "Personal Care - Dettol"),
        TestCase("Surf Excel Easy Wash Detergent Powder 1kg", "soap_bar", "Cleaning - Surf Excel"),
        TestCase("Vim Dishwash Liquid Gel 500ml", "lotion_bottle", "Cleaning - Vim Gel"),
        TestCase("Ariel Matic Liquid Detergent 1L", "lotion_bottle", "Cleaning - Ariel Liquid"),
        TestCase("Tide Plus Extra Power Detergent Powder 1kg", "soap_bar", "Cleaning - Tide Powder"),
        TestCase("Scotch Brite Scrub Sponge", "sponge", "Cleaning - Scrub Sponge"),
        TestCase("White Crystal Sugar 1kg", "honey", "Staple - Sugar"),
        TestCase("Cheeni packet 1kg", "honey", "Hinglish - Cheeni"),
        TestCase("Sakkarai 1kg", "honey", "Tanglish - Sakkarai"),
        TestCase("Pampers All Round Protection Baby Diapers", "baby_face", "Baby - Pampers"),
        TestCase("Pedigree Adult Chicken & Vegetables Dog Food 1.2kg", "dog", "Pet - Pedigree"),
        TestCase("Whiskas Ocean Fish Dry Cat Food 1kg", "cat", "Pet - Whiskas"),

        // 3. Hinglish / Indian Staples in Romanized Script
        TestCase("Doodh packet 500ml", "milk_bottle", "Hinglish - Doodh (Milk)"),
        TestCase("Chawal 5kg bag", "cooked_rice", "Hinglish - Chawal (Rice)"),
        TestCase("Chakki fresh Atta 10kg", "bowl_flour", "Hinglish - Atta (Flour)"),
        TestCase("Desi Cow Ghee 500ml", "ghee_jar", "Hinglish - Ghee"),
        TestCase("Safed Makhan 200g", "butter", "Hinglish - Makhan (Butter)"),
        TestCase("Chai patti 250g", "tea_cup", "Hinglish - Chai (Tea)"),
        TestCase("Tata Namak 1kg", "salt", "Hinglish - Namak (Salt)"),
        TestCase("Kala Til 100g", "sheaf_of_rice", "Hinglish - Til"),
        TestCase("Haldi Powder 200g", "curry_rice", "Hinglish - Haldi (Turmeric)"),
        TestCase("Garam Masala 100g", "curry_rice", "Hinglish - Masala"),

        // 4. Tanglish / Tamil Staples in Romanized Script
        TestCase("Aavin Paal 500ml", "milk_bottle", "Tanglish - Paal (Milk)"),
        TestCase("Ponni Arisi 25kg bag", "sheaf_of_rice", "Tanglish - Arisi (Rice)"),
        TestCase("Nattu Vellam 1kg", "honey", "Tanglish - Vellam (Jaggery)"),
        TestCase("Idhayam Nallennai 500ml", "oil_drop", "Tanglish - Nallennai (Sesame Oil)"),
        TestCase("Kozhi Muttai 6 nos", "egg", "Tanglish - Muttai (Egg)"),
        TestCase("Theylai 250g", "tea_cup", "Tanglish - Theylai (Tea)"),
        TestCase("Vazhaipazham 1 dozen", "banana", "Tanglish - Vazhaipazham (Banana)"),
        TestCase("Uppu packet 1kg", "salt", "Tanglish - Uppu (Salt)"),

        // 5. Native Hindi Script (Devanagari)
        TestCase("सेब 1 किलो", "apple", "Hindi script - सेब (Apple)"),
        TestCase("ताजा दूध 500ml", "milk_bottle", "Hindi script - दूध (Milk)"),
        TestCase("मक्खन 100g", "butter", "Hindi script - मक्खन (Butter)"),
        TestCase("चाय पत्ती 250g", "tea_cup", "Hindi script - चाय (Tea)"),
        TestCase("बासमती चावल 5kg", "sheaf_of_rice", "Hindi script - चावल (Rice)"),
        TestCase("सरसों का तेल 1L", "oil_drop", "Hindi script - तेल (Oil)"),
        TestCase("अंडा 6 पीस", "egg", "Hindi script - अंडा (Egg)"),
        TestCase("केला 1 दर्जन", "banana", "Hindi script - केला (Banana)"),

        // 6. Native Tamil Script
        TestCase("ஆப்பிள் 1 கிலோ", "apple", "Tamil script - ஆப்பிள் (Apple)"),
        TestCase("ஆவின் பால் 500ml", "milk_bottle", "Tamil script - பால் (Milk)"),
        TestCase("வெண்ணெய் 100g", "butter", "Tamil script - வெண்ணெய் (Butter)"),
        TestCase("தேநீர் தூள் 250g", "tea_cup", "Tamil script - தேநீர் (Tea)"),
        TestCase("பொன்னி அரிசி 5kg", "sheaf_of_rice", "Tamil script - அரிசி (Rice)"),
        TestCase("நல்லெண்ணெய் 1L", "oil_drop", "Tamil script - எண்ணெய் (Oil)"),
        TestCase("முட்டை 6", "egg", "Tamil script - முட்டை (Egg)"),
        TestCase("வாழைப்பழம் 1 டஜன்", "banana", "Tamil script - வாழைப்பழம் (Banana)"),

        // 7. Typo Tolerance (Levenshtein distance 1 on words 5+ letters)
        TestCase("choclate bar 50g", "chocolate", "Typo - choclate -> chocolate"),
        TestCase("bannana yellow", "banana", "Typo - bannana -> banana"),
        TestCase("coffe powder instant", "tea_cup", "Typo - coffe -> coffee"),
        TestCase("tamato ketchup 200g", "tomato", "Typo - tamato -> tomato"),
        TestCase("fresh orrange", "orange", "Typo - orrange -> orange"),
        TestCase("fresh strawbery", "strawberry", "Typo - strawbery -> strawberry")
    )

    @Test
    fun testPositiveMatchesAndReportMatchRate() {
        var matchedCount = 0
        val failedCases = mutableListOf<String>()

        for (case in positiveTestCases) {
            val result = ProductIconMatcher.match(case.input, case.category)
            if (result != null && result.iconKey == case.expectedIconKey) {
                matchedCount++
            } else {
                val actual = result?.iconKey ?: "null"
                failedCases.add("FAIL [${case.description}]: input='${case.input}' expected='${case.expectedIconKey}' actual='$actual'")
            }
        }

        val matchRate = (matchedCount.toDouble() / positiveTestCases.size.toDouble()) * 100.0
        val matchRateStr = "%.1f".format(matchRate)
        println("=== PRODUCT ICON MATCHER REPORT ===")
        println("Total Test Cases: ${positiveTestCases.size}")
        println("Matched: $matchedCount")
        println("Match Rate: $matchRateStr%")

        if (failedCases.isNotEmpty()) {
            println("Failed Cases:\n" + failedCases.joinToString("\n"))
        }

        assertTrue("Match rate must be at least 95%, was $matchRateStr%", matchRate >= 95.0)
    }

    @Test
    fun testSpecificity_coconutOilGetsOilNotCoconut() {
        // "Coconut oil" should match oil_drop rather than coconut because oil phrase is more specific
        val result = ProductIconMatcher.match("Parachute Pure Coconut Oil 500ml")
        assertNotNull(result)
        assertEquals("oil_drop", result?.iconKey)
    }

    @Test
    fun testSpecificity_chickenCurryCutGetsChicken() {
        val result = ProductIconMatcher.match("Fresh Chicken Curry Cut 500g")
        assertNotNull(result)
        assertEquals("poultry_leg", result?.iconKey)
    }

    @Test
    fun testSpecificity_greenTeaGetsTea() {
        val result = ProductIconMatcher.match("Lipton Pure Green Tea 100 Tea Bags")
        assertNotNull(result)
        assertEquals("green_tea", result?.iconKey)
    }

    @Test
    fun testNormalization_stripsUnitsAndPunctuation() {
        val normalized1 = ProductIconMatcher.normalize("Amul Taaza Milk 500ml Pack (Combo)")
        assertEquals("amul taaza milk", normalized1)

        val normalized2 = ProductIconMatcher.normalize("Aashirvaad Atta 5kg - 100% Whole Wheat!")
        assertEquals("aashirvaad atta whole wheat", normalized2)

        val normalized3 = ProductIconMatcher.normalize("Maggi 2-Minute Masala Noodles (70g x 4 pkts)")
        assertEquals("maggi minute masala noodles x", normalized3)
    }

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, ProductIconMatcher.levenshtein("apple", "apple"))
        assertEquals(1, ProductIconMatcher.levenshtein("coffe", "coffee"))
        assertEquals(1, ProductIconMatcher.levenshtein("choclate", "chocolate"))
        assertEquals(1, ProductIconMatcher.levenshtein("tamato", "tomato"))
        assertEquals(1, ProductIconMatcher.levenshtein("aple", "apple"))
        assertEquals(2, ProductIconMatcher.levenshtein("apple", "applyy"))
    }

    @Test
    fun testEdgeCases_neverCrashOnEmptyOrSymbols() {
        assertNull(ProductIconMatcher.match(""))
        assertNull(ProductIconMatcher.match("   "))
        assertNull(ProductIconMatcher.match(null))
        assertNull(ProductIconMatcher.match("---###$$$"))
        assertNull(ProductIconMatcher.match("12345678 999"))
        assertNull(ProductIconMatcher.match("!@#$%^&*()"))
        assertNull(ProductIconMatcher.match("xyzabcrandomnonexistent98765"))
    }

    @Test
    fun testFallbackHierarchy_savedIconKeyWins() {
        // Even if name is "Fresh Milk", if savedKey is "cookie", savedKey wins
        val drawable = ProductIconMatcher.resolveDrawableRes(
            name = "Fresh Cow Milk",
            category = "Dairy",
            savedIconKey = "cookie"
        )
        val expectedDrawable = ProductIconCatalog.get("cookie")?.drawableRes
        assertNotNull(expectedDrawable)
        assertEquals(expectedDrawable, drawable)
    }

    @Test
    fun testFallbackHierarchy_categoryFallbackWhenNoMatch() {
        // Unknown product name with category "Bakery"
        val drawable = ProductIconMatcher.resolveDrawableRes(
            name = "Super Unknown Brand XYZ123",
            category = "Bakery",
            savedIconKey = null
        )
        val expectedDrawable = ProductIconCatalog.getDepartmentIcon("Bakery")
        assertEquals(expectedDrawable, drawable)
    }

    @Test
    fun testFallbackHierarchy_packageDefaultWhenAllElseFails() {
        val drawable = ProductIconMatcher.resolveDrawableRes(
            name = "RandomNonExistentItem999",
            category = null,
            savedIconKey = null
        )
        assertEquals(ProductIconCatalog.DEFAULT_PACKAGE_ICON, drawable)
    }

    @Test
    fun testDepartmentResolution() {
        val dept1 = ProductIconMatcher.resolveDepartment("Amul Milk", "Dairy")
        assertEquals(ProductDepartment.DAIRY, dept1)

        val dept2 = ProductIconMatcher.resolveDepartment("Britannia Biscuits", null)
        assertEquals(ProductDepartment.SNACKS, dept2)

        val dept3 = ProductIconMatcher.resolveDepartment("Unknown Item", "Household Cleaning")
        assertEquals(ProductDepartment.CLEANING, dept3)
    }

    @Test
    fun testCatalogCompleteness() {
        assertTrue("Catalog must have 300+ items", ProductIconCatalog.ITEMS.size >= 300)
        val totalKeywords = ProductIconCatalog.ITEMS.sumOf { it.keywords.size }
        assertTrue("Catalog must have 1500+ keywords, found $totalKeywords", totalKeywords >= 1500)
    }

    @Test
    fun testAllCatalogIconsHaveNonEmptyVisibleVectorDrawables() {
        val resDir = listOf(
            java.io.File("src/main/res/drawable"),
            java.io.File("app/src/main/res/drawable"),
            java.io.File("../app/src/main/res/drawable")
        ).firstOrNull { it.exists() && it.isDirectory }

        assertNotNull("Drawable resource directory must be accessible from unit test", resDir)

        val brokenIcons = mutableListOf<String>()

        for (item in ProductIconCatalog.ITEMS) {
            val file = java.io.File(resDir, "ic_prod_${item.iconKey}.xml")
            if (!file.exists()) {
                brokenIcons.add("${item.iconKey}: File does not exist (${file.path})")
                continue
            }

            val content = file.readText()

            // 1. Must be valid vector drawable
            if (!content.contains("<vector") || !content.contains("</vector>")) {
                brokenIcons.add("${item.iconKey}: Not a valid <vector> drawable")
                continue
            }

            // 2. Must not contain unsupported AAPT or gradient tags (incompatible with Compose vector renderer at minSdk 24)
            if (content.contains("<aapt:attr") || content.contains("<gradient")) {
                brokenIcons.add("${item.iconKey}: Contains unsupported <aapt:attr> or <gradient>")
                continue
            }

            // 3. Must have at least one path with non-empty pathData
            val paths = Regex("<path[^>]*>", RegexOption.DOT_MATCHES_ALL).findAll(content).toList()
            if (paths.isEmpty()) {
                brokenIcons.add("${item.iconKey}: Has 0 <path> elements")
                continue
            }

            var hasNonEmptyPathData = false
            var hasVisibleFillOrStroke = false

            for (p in paths) {
                val tag = p.value
                val pathDataMatch = Regex("android:pathData=\"([^\"]+)\"").find(tag)
                if (pathDataMatch != null && pathDataMatch.groupValues[1].isNotBlank()) {
                    hasNonEmptyPathData = true
                }

                val fillMatch = Regex("android:fillColor=\"([^\"]+)\"").find(tag)
                val strokeMatch = Regex("android:strokeColor=\"([^\"]+)\"").find(tag)

                val fill = fillMatch?.groupValues?.get(1)
                val stroke = strokeMatch?.groupValues?.get(1)

                val isVisibleColor = { c: String? ->
                    c != null &&
                    c.isNotBlank() &&
                    c != "@android:color/transparent" &&
                    c != "#00000000" &&
                    !c.startsWith("#00")
                }

                if (isVisibleColor(fill) || isVisibleColor(stroke)) {
                    hasVisibleFillOrStroke = true
                }
            }

            if (!hasNonEmptyPathData) {
                brokenIcons.add("${item.iconKey}: Path data is empty or missing")
            }

            if (!hasVisibleFillOrStroke) {
                brokenIcons.add("${item.iconKey}: All fills/strokes are transparent or missing")
            }
        }

        if (brokenIcons.isNotEmpty()) {
            fail("Found ${brokenIcons.size} broken icons in catalog:\n" + brokenIcons.joinToString("\n"))
        }

        assertEquals("All ${ProductIconCatalog.ITEMS.size} icons must be verified", 304, ProductIconCatalog.ITEMS.size)
    }
}
