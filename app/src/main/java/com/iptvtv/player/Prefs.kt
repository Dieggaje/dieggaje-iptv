package com.iptvtv.player

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("iptv", Context.MODE_PRIVATE)

    var server: String
        get() = sp.getString("server", "") ?: ""
        set(v) = sp.edit().putString("server", v).apply()

    var user: String
        get() = sp.getString("user", "") ?: ""
        set(v) = sp.edit().putString("user", v).apply()

    var pass: String
        get() = sp.getString("pass", "") ?: ""
        set(v) = sp.edit().putString("pass", v).apply()

    var m3u: String
        get() = sp.getString("m3u", "") ?: ""
        set(v) = sp.edit().putString("m3u", v).apply()

    fun isConfigured(): Boolean =
        m3u.isNotBlank() || (server.isNotBlank() && user.isNotBlank() && pass.isNotBlank())

    fun isFav(key: String): Boolean =
        (sp.getStringSet("favs", emptySet()) ?: emptySet()).contains(key)

    fun toggleFav(key: String) {
        val s = HashSet(sp.getStringSet("favs", emptySet()) ?: emptySet())
        if (!s.add(key)) s.remove(key)
        sp.edit().putStringSet("favs", s).apply()
    }
}
