package com.oo.skinsync.data

import com.oo.skinsync.domain.Profile

/** Pure prompt construction so it is unit-testable. */
object PromptBuilder {

    private fun hex(argb: Int): String =
        "#%06X".format(0xFFFFFF and argb)

    fun build(profile: Profile, location: String): String {
        val c = profile.faceColors
        return buildString {
            append("You are a fashion color analyst. Given a person's features and a ")
            append("photoshoot location, recommend 4 flattering outfit colors.\n")
            append("Location: $location\n")
            append("Gender: ${profile.gender.name.lowercase()}\n")
            append("Age: ${profile.age ?: "unknown"}\n")
            if (c != null) {
                append("Skin: ${hex(c.skin)}\n")
                append("Lip: ${hex(c.lip)}\n")
                append("Eyes: ${hex(c.leftEye)}\n")
            }
            append("Also classify the person's seasonal color type ")
            append("(e.g. \"Soft Autumn\").\n")
            append("Return ONLY JSON of this exact shape:\n")
            append("""{"seasonalType":"<type>","palette":[""")
            append("""{"hexColor":"#RRGGBB","name":"<color name>",""")
            append(""""reason":"<one concise sentence>",""")
            append(""""outfitQueries":["<search phrase>","<search phrase>"]}]}""")
            append("\nProvide exactly 4 palette entries. ")
            append("outfitQueries must be realistic shopping search phrases ")
            append("for the given gender and age.")
        }
    }
}
