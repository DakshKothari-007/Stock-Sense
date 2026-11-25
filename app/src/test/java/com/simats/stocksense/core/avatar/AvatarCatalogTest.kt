package com.simats.stocksense.core.avatar

import com.simats.stocksense.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class AvatarCatalogTest {

    // ---------------------------------------------------------
    // 1. URL Builder Tests
    // ---------------------------------------------------------

    @Test
    fun buildUrl_constructsDeterministicDiceBearUrlWithZeroFacialHair() {
        val code = AvatarCode(
            style = "micah",
            seed = "av_abc123_XYZ",
            options = mapOf(
                "shirtColor" to "9287ff",
                "baseColor" to "f9c9b6",
                "hairVariant" to "fonze",
                "backgroundColor" to "fff4dc"
            )
        )

        val url = AvatarUrlBuilder.buildUrl(code, baseUrl = "https://api.dicebear.com/10.x/")
        assertEquals(
            "https://api.dicebear.com/10.x/micah/svg?seed=av_abc123_XYZ&backgroundColor=fff4dc&baseColor=f9c9b6&facialHairProbability=0&hairVariant=fonze&shirtColor=9287ff",
            url
        )

        val personasUrl = AvatarUrlBuilder.buildUrl(AvatarCode(style = "personas", seed = "ps_123"))
        assertEquals(
            "https://api.dicebear.com/10.x/personas/svg?seed=ps_123&facialHairProbability=0",
            personasUrl
        )
    }

    @Test
    fun buildUrl_usesDefaultBuildConfigDicebearBaseUrlAndSuppressesOpenPeepsMaskAndFacialHair() {
        assertEquals("https://api.dicebear.com/10.x", BuildConfig.DICEBEAR_BASE_URL)
        val code = AvatarCode(style = "open-peeps", seed = "op_m4x9k2a1")
        val url = AvatarUrlBuilder.buildUrl(code)
        assertEquals(
            "https://api.dicebear.com/10.x/open-peeps/svg?seed=op_m4x9k2a1&facialHairProbability=0&maskProbability=0",
            url
        )
    }

    @Test
    fun buildUrl_normalizesColorsAndDropsUnknownKeysWhilePreservingValidOnes() {
        val code = AvatarCode(
            style = "open-peeps",
            seed = "op_seed1",
            options = mapOf(
                "skinColor" to "D08B5B", // upper hex normalized
                "eyesVariant" to "smiling", // unsupported on open-peeps
                "evilKey" to "javascript:alert(1)"
            )
        )

        val url = AvatarUrlBuilder.buildUrl(code, baseUrl = "https://custom.dicebear.local/10.x")
        assertEquals(
            "https://custom.dicebear.local/10.x/open-peeps/svg?seed=op_seed1&facialHairProbability=0&maskProbability=0&skinColor=d08b5b",
            url
        )
    }

    // ---------------------------------------------------------
    // 2. Option Whitelist, Friendly-Only & No-Facial-Hair Tests
    // ---------------------------------------------------------

    @Test
    fun optionWhitelist_validatesFriendlyOnlyAndRejectsAllFacialHair() {
        // Supported friendly values per style
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "headVariant", "short1"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "skinColor", "d08b5b"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "expressionVariant", "smile"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "accessoriesVariant", "glasses"))

        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "baseColor", "f9c9b6"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "eyesVariant", "smiling"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "eyebrowsVariant", "up"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "eyebrowsVariant", "eyelashesUp"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "mouthVariant", "laughing"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "glassesVariant", "round"))

        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "skinColor", "e5a07e"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "clothesVariant", "squared"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "mouthVariant", "bigSmile"))
        assertTrue(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "eyesVariant", "wink"))

        // Reject facial hair keys and values across ALL 3 styles
        for (style in AvatarStyle.entries) {
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHair", "beard"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairVariant", "full"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairVariant", "beard"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairVariant", "goatee"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairProbability", "0"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairProbability", "100"))
            assertFalse(AvatarCatalog.isOptionAllowed(style, "facialHairColor", "362c47"))
            assertFalse(AvatarCatalog.areOptionsStrictlyValid(style, mapOf("facialHairProbability" to "0")))
        }

        // Friendly-only rejections (angry/scary/sad/masks/eyepatch/sleep/cross-style)
        val blockedOpenPeepsExpressions = listOf(
            "angryWithFang", "rage", "veryAngry", "monster", "cyclops", "fear",
            "concernedFear", "hectic", "contempt", "suspicious", "tired",
            "solemn", "blank", "old", "concerned", "eyesClosed", "serious"
        )
        for (expr in blockedOpenPeepsExpressions) {
            assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "expressionVariant", expr))
        }
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "accessoriesVariant", "eyepatch"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "maskVariant", "medicalMask"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "maskProbability", "100"))

        for (mouth in listOf("frown", "sad", "nervous", "surprised")) {
            assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "mouthVariant", mouth))
        }
        for (brow in listOf("down", "eyelashesDown")) {
            assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "eyebrowsVariant", brow))
        }

        for (mouth in listOf("frown", "pacifier", "surprise")) {
            assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "mouthVariant", mouth))
        }
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "eyesVariant", "sleep"))

        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "eyesVariant", "smiling"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "accessoriesVariant", "glasses"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "skinColor", "f9c9b6"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.PERSONAS, "hairVariant", "afro"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.OPEN_PEEPS, "backgroundColor", "#fff4dc"))
        assertFalse(AvatarCatalog.isOptionAllowed(AvatarStyle.MICAH, "glassesProbability", "150"))
    }

    @Test
    fun supportedRows_hideFacialHairAndUnsupportedOptionsAcrossAllStyles() {
        for (style in AvatarStyle.entries) {
            val rowTitles = AvatarCatalog.getSupportedRows(style).map { it.title }
            assertFalse(
                "Facial Hair row must not appear in ${style.id}",
                rowTitles.any { it.contains("Facial", ignoreCase = true) }
            )
        }

        val openPeepsRows = AvatarCatalog.getSupportedRows(AvatarStyle.OPEN_PEEPS).map { it.title }
        assertTrue(openPeepsRows.contains("Skin"))
        assertTrue(openPeepsRows.contains("Hair"))
        assertTrue(openPeepsRows.contains("Expression"))
        assertTrue(openPeepsRows.contains("Background Color"))
        assertFalse(openPeepsRows.contains("Eyes"))
        assertFalse(openPeepsRows.contains("Hair Color"))
        assertFalse(openPeepsRows.any { it.contains("Mask", ignoreCase = true) })

        val personasRows = AvatarCatalog.getSupportedRows(AvatarStyle.PERSONAS).map { it.title }
        assertTrue(personasRows.contains("Eyes"))
        assertTrue(personasRows.contains("Mouth"))
        assertTrue(personasRows.contains("Clothes"))
        assertFalse(personasRows.contains("Accessories"))

        val micahRows = AvatarCatalog.getSupportedRows(AvatarStyle.MICAH).map { it.title }
        assertTrue(micahRows.contains("Accessories"))
        assertTrue(micahRows.contains("Eyes"))
        assertTrue(micahRows.contains("Eyebrows"))
        assertTrue(micahRows.contains("Mouth"))
        assertTrue(micahRows.contains("Hair Color"))
    }

    @Test
    fun randomize_and_reset_produceOnlyAllowedFriendlyValuesWithZeroFacialHair() {
        val random = Random(2026L)
        for (style in AvatarStyle.entries) {
            val resetOpts = AvatarCatalog.defaultFriendlyOptions(style)
            assertTrue(resetOpts.keys.none { it.startsWith("facialHair") })

            repeat(50) {
                val generated = AvatarCatalog.randomize(style, random)
                assertEquals(style.id, generated.style)
                assertTrue(AvatarCatalog.isValidSeed(generated.seed))
                assertTrue(
                    "Randomize generated disallowed option for ${style.id}: ${generated.options}",
                    AvatarCatalog.areOptionsStrictlyValid(style, generated.options)
                )
                assertFalse(generated.options.keys.any { it.startsWith("facialHair") })
                assertFalse(generated.options.containsKey("maskVariant"))
                assertFalse(generated.options.containsKey("maskProbability"))
                if (style == AvatarStyle.OPEN_PEEPS) {
                    assertTrue(generated.options["expressionVariant"] in AvatarCatalog.openPeepsExpressions)
                    val acc = generated.options["accessoriesVariant"]
                    if (acc != null) {
                        assertTrue(acc in AvatarCatalog.openPeepsAccessories)
                    }
                }
                if (style == AvatarStyle.MICAH) {
                    assertTrue(generated.options["mouthVariant"] in AvatarCatalog.micahMouthVariants)
                    assertTrue(generated.options["eyesVariant"] in AvatarCatalog.micahEyesVariants)
                    assertTrue(generated.options["eyebrowsVariant"] in AvatarCatalog.micahEyebrowsVariants)
                }
                if (style == AvatarStyle.PERSONAS) {
                    assertTrue(generated.options["mouthVariant"] in AvatarCatalog.personasMouths)
                    assertTrue(generated.options["eyesVariant"] in AvatarCatalog.personasEyesVariants)
                }
            }
        }
    }

    @Test
    fun baseGridPresets_areStrictlyFriendlyAndHaveZeroFacialHair() {
        for (style in AvatarStyle.entries) {
            val presets = AvatarCatalog.getBasePresets(style)
            assertEquals(12, presets.size)
            for (preset in presets) {
                assertTrue(AvatarCatalog.isValidSeed(preset.seed))
                assertTrue(AvatarCatalog.areOptionsStrictlyValid(style, preset.options))
                assertFalse(preset.options.keys.any { it.startsWith("facialHair") })
                when (style) {
                    AvatarStyle.OPEN_PEEPS -> {
                        assertTrue(preset.options["expressionVariant"] in AvatarCatalog.openPeepsExpressions)
                        val acc = preset.options["accessoriesVariant"]
                        if (acc != null) {
                            assertTrue(acc in AvatarCatalog.openPeepsAccessories)
                        }
                    }
                    AvatarStyle.MICAH -> {
                        assertTrue(preset.options["mouthVariant"] in AvatarCatalog.micahMouthVariants)
                        assertTrue(preset.options["eyesVariant"] in AvatarCatalog.micahEyesVariants)
                        assertTrue(preset.options["eyebrowsVariant"] in AvatarCatalog.micahEyebrowsVariants)
                    }
                    AvatarStyle.PERSONAS -> {
                        assertTrue(preset.options["mouthVariant"] in AvatarCatalog.personasMouths)
                        assertTrue(preset.options["eyesVariant"] in AvatarCatalog.personasEyesVariants)
                    }
                }
            }
        }
    }

    @Test
    fun whitelist_rejectsOptionsJsonLargerThan1Kb() {
        val bigOptions = (1..40).associate { "headVariant" + it to "short1" }
        assertFalse(AvatarCatalog.areOptionsStrictlyValid(AvatarStyle.OPEN_PEEPS, bigOptions))

        val oversizeMap = (1..50).associate { "k$it" to "v".repeat(25) }
        assertFalse(AvatarCatalog.areOptionsStrictlyValid(AvatarStyle.OPEN_PEEPS, oversizeMap))
        assertTrue(AvatarCatalog.sanitizeOptions(AvatarStyle.OPEN_PEEPS, oversizeMap).isEmpty())
    }

    // ---------------------------------------------------------
    // 3. Code Parsing & Legacy Facial-Hair / Blocked Fallbacks
    // ---------------------------------------------------------

    @Test
    fun parseAvatarJson_parsesValidJsonAndSanitizesOptions() {
        val json = """
            {
              "style": "personas",
              "seed": "ps_t6n2r9w1",
              "options": {
                "skinColor": "E5A07E",
                "hairVariant": "curly",
                "invalidKey": "foo"
              }
            }
        """.trimIndent()

        val parsed = AvatarCatalog.parseAvatarJson(json)
        assertNotNull(parsed)
        assertEquals("personas", parsed!!.style)
        assertEquals("ps_t6n2r9w1", parsed.seed)
        assertEquals("e5a07e", parsed.options["skinColor"])
        assertEquals("curly", parsed.options["hairVariant"])
        assertFalse(parsed.options.containsKey("invalidKey"))
    }

    @Test
    fun savedAvatarWithFacialHair_stripsFacialHairAndRendersWithoutBeard() {
        val legacyWithBeard = AvatarCode(
            style = "micah",
            seed = "legacy_beard_1",
            options = mapOf(
                "baseColor" to "f9c9b6",
                "facialHair" to "beard",
                "facialHairVariant" to "beard",
                "facialHairProbability" to "100",
                "facialHairColor" to "000000"
            )
        )

        val sanitized = AvatarCatalog.parseAvatarCode(legacyWithBeard)
        assertNotNull(sanitized)
        assertEquals("f9c9b6", sanitized!!.options["baseColor"])
        assertFalse(sanitized.options.keys.any { it.startsWith("facialHair") })

        val url = AvatarUrlBuilder.buildUrl(legacyWithBeard, baseUrl = "https://api.dicebear.com/10.x")
        assertNotNull(url)
        assertFalse(url!!.contains("facialHairVariant"))
        assertFalse(url.contains("facialHair="))
        assertFalse(url.contains("facialHairColor"))
        assertTrue(url.contains("facialHairProbability=0"))
    }

    @Test
    fun existingUserBlockedValues_fallbackToNearestAllowedFriendlyValuesWithoutCrashing() {
        // open-peeps legacy rage + eyepatch + mask + facialHair -> smile + no accessories + no mask/beard
        val legacyOpenPeeps = AvatarCode(
            style = "open-peeps",
            seed = "legacy_op_seed",
            options = mapOf(
                "expressionVariant" to "rage",
                "accessoriesProbability" to "100",
                "accessoriesVariant" to "eyepatch",
                "facialHairVariant" to "full",
                "facialHairProbability" to "100",
                "maskVariant" to "medicalMask",
                "maskProbability" to "100",
                "skinColor" to "d08b5b"
            )
        )
        val parsedOp = AvatarCatalog.parseAvatarCode(legacyOpenPeeps)
        assertNotNull(parsedOp)
        assertEquals("smile", parsedOp!!.options["expressionVariant"])
        assertEquals("0", parsedOp.options["accessoriesProbability"])
        assertNull(parsedOp.options["accessoriesVariant"])
        assertNull(parsedOp.options["facialHairVariant"])
        assertNull(parsedOp.options["facialHairProbability"])
        assertNull(parsedOp.options["maskVariant"])
        assertNull(parsedOp.options["maskProbability"])
        assertEquals("d08b5b", parsedOp.options["skinColor"])

        // micah legacy frown + eyebrows down + facialHair -> smile + up, no facial hair
        val legacyMicah = AvatarCatalog.parseAvatarJson(
            """{"style":"micah","seed":"legacy_mc_1","options":{"mouthVariant":"frown","eyebrowsVariant":"down","facialHairVariant":"scruff","facialHairProbability":"100"}}"""
        )
        assertNotNull(legacyMicah)
        assertEquals("smile", legacyMicah!!.options["mouthVariant"])
        assertEquals("up", legacyMicah.options["eyebrowsVariant"])
        assertNull(legacyMicah.options["facialHairVariant"])
        assertNull(legacyMicah.options["facialHairProbability"])

        // personas legacy pacifier + sleep + facialHair -> smile + open, no facial hair
        val legacyPersonas = AvatarCatalog.parseAvatarJson(
            """{"style":"personas","seed":"legacy_ps_1","options":{"mouthVariant":"pacifier","eyesVariant":"sleep","facialHairVariant":"goatee","facialHairProbability":"100"}}"""
        )
        assertNotNull(legacyPersonas)
        assertEquals("smile", legacyPersonas!!.options["mouthVariant"])
        assertEquals("open", legacyPersonas.options["eyesVariant"])
        assertNull(legacyPersonas.options["facialHairVariant"])
        assertNull(legacyPersonas.options["facialHairProbability"])
    }

    @Test
    fun randomSeed_matchesStrictPatternAndDoesNotLeakPersonalInfo() {
        val seed = AvatarCatalog.generateRandomSeed(Random(42L))
        assertTrue(AvatarCatalog.isValidSeed(seed))
        assertTrue(AvatarCatalog.BASE_SEEDS_BY_STYLE.values.all { seeds ->
            seeds.size == 12 && seeds.all { AvatarCatalog.isValidSeed(it) }
        })
    }

    // ---------------------------------------------------------
    // 4. Null / Invalid Fallback Tests
    // ---------------------------------------------------------

    @Test
    fun parseAndUrl_fallbackGracefullyToNullOnInvalidOrNullInputs() {
        assertNull(AvatarCatalog.parseAvatarCode(null))
        assertNull(AvatarUrlBuilder.buildUrl(null))

        val badStyle = AvatarCode(style = "unknown-style", seed = "seed123")
        assertNull(AvatarCatalog.parseAvatarCode(badStyle))
        assertNull(AvatarUrlBuilder.buildUrl(badStyle))

        val emailSeed = AvatarCode(style = "micah", seed = "owner@example.com")
        val spaceSeed = AvatarCode(style = "micah", seed = "owner name")
        val longSeed = AvatarCode(style = "micah", seed = "a".repeat(65))
        val emptySeed = AvatarCode(style = "micah", seed = "")

        assertNull(AvatarCatalog.parseAvatarCode(emailSeed))
        assertNull(AvatarCatalog.parseAvatarCode(spaceSeed))
        assertNull(AvatarCatalog.parseAvatarCode(longSeed))
        assertNull(AvatarCatalog.parseAvatarCode(emptySeed))
        assertNull(AvatarUrlBuilder.buildUrl(emailSeed))

        assertNull(AvatarCatalog.parseAvatarJson(null))
        assertNull(AvatarCatalog.parseAvatarJson(""))
        assertNull(AvatarCatalog.parseAvatarJson("{broken"))
        assertNull(AvatarCatalog.parseAvatarJson("""{"style":"micah","seed":"abc","options":"not-an-object"}"""))
    }

    @Test
    fun extractInitials_providesCleanLetterBadgeFallback() {
        assertEquals("AR", AvatarCatalog.extractInitials("Ananya Rao"))
        assertEquals("F", AvatarCatalog.extractInitials("Fresh"))
        assertEquals("VM", AvatarCatalog.extractInitials("  Vikram   Malhotra "))
        assertEquals("U", AvatarCatalog.extractInitials(""))
        assertEquals("U", AvatarCatalog.extractInitials(null))
    }
}
