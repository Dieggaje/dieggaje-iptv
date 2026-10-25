package com.iptvtv.player

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logo: String?,
    val group: String,
    /** URL alternativa (por ejemplo .ts si .m3u8 falla). */
    val alt: String? = null
)

/** Estado compartido sencillo entre pantallas. */
object Store {
    var playlist: List<Channel> = emptyList()
    var index: Int = 0
}
