package com.iptvtv.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Repo {

    private const val UA = "Mozilla/5.0 (Linux; Android TV) IPTVPlayer/1.0"

    suspend fun load(p: Prefs): List<Channel> = withContext(Dispatchers.IO) {
        if (p.m3u.isNotBlank()) parseM3u(httpGet(p.m3u.trim())) else loadXtream(p)
    }

    private fun httpGet(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 60000
        c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", UA)
        try {
            if (c.responseCode !in 200..299) {
                throw Exception("El servidor respondió HTTP ${c.responseCode}")
            }
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    private fun loadXtream(p: Prefs): List<Channel> {
        var base = p.server.trim().trimEnd('/')
        if (!base.startsWith("http://") && !base.startsWith("https://")) base = "http://$base"
        val user = p.user.trim()
        val pass = p.pass.trim()
        val q = "username=${enc(user)}&password=${enc(pass)}"

        val catsText = httpGet("$base/player_api.php?$q&action=get_live_categories").trim()
        if (!catsText.startsWith("[")) {
            throw Exception("Respuesta inválida del servidor. Revisa URL, usuario y contraseña.")
        }
        val cats = JSONArray(catsText)
        val catMap = HashMap<String, String>()
        for (i in 0 until cats.length()) {
            val o = cats.getJSONObject(i)
            catMap[o.optString("category_id")] = o.optString("category_name")
        }

        val streamsText = httpGet("$base/player_api.php?$q&action=get_live_streams").trim()
        if (!streamsText.startsWith("[")) {
            throw Exception("No se pudieron leer los canales (¿cuenta caducada?).")
        }
        val streams = JSONArray(streamsText)
        val out = ArrayList<Channel>(streams.length())
        for (i in 0 until streams.length()) {
            val o = streams.getJSONObject(i)
            val id = o.optString("stream_id")
            if (id.isEmpty()) continue
            val name = o.optString("name").ifBlank { "Canal $id" }
            val logo = o.optString("stream_icon").takeIf { it.startsWith("http") }
            val group = catMap[o.optString("category_id")] ?: "Otros"
            val root = "$base/live/${enc(user)}/${enc(pass)}/$id"
            out.add(Channel(id, name, "$root.m3u8", logo, group, alt = "$root.ts"))
        }
        return out
    }

    fun parseM3u(text: String): List<Channel> {
        val out = ArrayList<Channel>()
        val attr = Regex("([\\w-]+)=\"([^\"]*)\"")
        var name: String? = null
        var logo: String? = null
        var group = "Otros"
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.startsWith("#EXTINF")) {
                val attrs = attr.findAll(line).associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                logo = attrs["tvg-logo"]?.takeIf { it.startsWith("http") }
                group = attrs["group-title"]?.takeIf { it.isNotBlank() } ?: "Otros"
                name = line.substringAfterLast(",").trim()
            } else if (line.isNotEmpty() && !line.startsWith("#")) {
                out.add(Channel(out.size.toString(), name?.ifBlank { null } ?: line, line, logo, group))
                name = null
                logo = null
                group = "Otros"
            }
        }
        if (out.isEmpty()) throw Exception("La lista M3U no contiene canales.")
        return out
    }
}
