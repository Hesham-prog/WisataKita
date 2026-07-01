package com.wisatakita.app

import java.util.Locale

object ProfanityFilterUtil {

    private val BANNED_WORDS = setOf(
        // Indonesian Profanity (Comprehensive)
        "anjing", "anjir", "anj", "ajg", "babi", "bangsat", "bgst",
        "kampret", "keparat", "kontol", "kntl", "memek", "mmk",
        "jembut", "jancok", "jancuk", "cok", "jingan", "bajingan",
        "perek", "pelacur", "lonte", "lont", "jablay", "bego",
        "goblok", "gblk", "tolol", "idiot", "sinting", "gila",
        "pantat", "tai", "telek", "ngentot", "ngewe", "ngehe",
        "setan", "iblis", "dajjal", "asu", "sundel", "banci",
        "bencong", "maho", "homo", "lesbi", "peler", "pler",
        "kentu", "pepek", "pukimak", "kimak", "tetek", "toket",

        // English Profanity (Comprehensive)
        "fuck", "fck", "fucker", "fucking", "shit", "bullshit",
        "bitch", "bastard", "cunt", "dick", "pussy", "asshole",
        "ass", "arsehole", "motherfucker", "mf", "slut", "whore",
        "hooker", "cock", "cocksucker", "prick", "twat", "wanker",
        "nigger", "nigga", "faggot", "fag", "dyke", "tranny",
        "retard", "dumbass", "jackass", "douche", "douchebag",
        "cum", "jizz", "tits", "boobs", "boner", "shitty",
        "crap", "piss", "pissed", "wank", "bollocks", "bugger",
        "chink", "spic", "kike", "gook", "wetback", "raghead"
    )

    /**
     * Checks if a string contains any exact banned word.
     * Uses word boundaries to avoid false positives (e.g. blocking "class" because of "ass").
     */
    fun containsProfanity(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase(Locale.getDefault())
        // Split by non-alphanumeric to get clean words
        val words = normalized.split(Regex("[\\W_]+"))
        for (word in words) {
            if (BANNED_WORDS.contains(word)) {
                return true
            }
        }
        return false
    }
}
