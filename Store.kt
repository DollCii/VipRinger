package com.dollcii.vipringer

import android.content.Context

object Store {
    private fun prefs(c: Context) = c.getSharedPreferences("vip", Context.MODE_PRIVATE)

    // نقارن بآخر 9 أرقام عشان +20 و 01 يتطابقوا
    fun key(number: String) = number.filter { it.isDigit() }.takeLast(9)

    fun isEnabled(c: Context) = prefs(c).getBoolean("enabled", true)
    fun setEnabled(c: Context, v: Boolean) = prefs(c).edit().putBoolean("enabled", v).apply()

    // كل عنصر: "الاسم|الرقم"
    fun list(c: Context): List<String> =
        (prefs(c).getStringSet("nums", emptySet()) ?: emptySet()).toList().sorted()

    fun add(c: Context, name: String, number: String) {
        val set = (prefs(c).getStringSet("nums", emptySet()) ?: emptySet()).toMutableSet()
        set.removeAll { key(it.substringAfter("|")) == key(number) }
        set.add("${name.ifBlank { number }}|$number")
        prefs(c).edit().putStringSet("nums", set).apply()
    }

    fun remove(c: Context, entry: String) {
        val set = (prefs(c).getStringSet("nums", emptySet()) ?: emptySet()).toMutableSet()
        set.remove(entry)
        prefs(c).edit().putStringSet("nums", set).apply()
    }

    fun isAllowed(c: Context, number: String): Boolean {
        val k = key(number)
        if (k.isEmpty()) return false
        return list(c).any { key(it.substringAfter("|")) == k }
    }
}
