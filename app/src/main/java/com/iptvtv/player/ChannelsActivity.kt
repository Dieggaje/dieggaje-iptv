package com.iptvtv.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ChannelsActivity : AppCompatActivity() {

    companion object {
        const val FAV = "★ Favoritos"
        const val ALL = "Todos"
    }

    private lateinit var prefs: Prefs
    private lateinit var catAdapter: CategoryAdapter
    private lateinit var chAdapter: ChannelAdapter
    private lateinit var progress: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var rvCats: RecyclerView

    private var all: List<Channel> = emptyList()
    private var selectedCat = ALL
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)
        prefs = Prefs(this)

        progress = findViewById(R.id.progress)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvCats = findViewById(R.id.rvCats)
        val rvChannels = findViewById<RecyclerView>(R.id.rvChannels)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        catAdapter = CategoryAdapter { cat ->
            selectedCat = cat
            applyFilter()
        }
        chAdapter = ChannelAdapter(
            isFav = { prefs.isFav(it.url) },
            onPlay = { list, pos ->
                Store.playlist = list
                Store.index = pos
                startActivity(Intent(this, PlayerActivity::class.java))
            },
            onToggleFav = { ch, pos ->
                prefs.toggleFav(ch.url)
                if (selectedCat == FAV) applyFilter() else chAdapter.refresh(pos)
            }
        )

        rvCats.layoutManager = LinearLayoutManager(this)
        rvCats.adapter = catAdapter
        rvChannels.layoutManager = LinearLayoutManager(this)
        rvChannels.adapter = chAdapter

        etSearch.doAfterTextChanged {
            query = it?.toString() ?: ""
            applyFilter()
        }
        findViewById<Button>(R.id.btnReload).setOnClickListener { load() }
        findViewById<Button>(R.id.btnChange).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).putExtra("edit", true))
            finish()
        }

        load()
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        lifecycleScope.launch {
            try {
                all = Repo.load(prefs)
                val groups = all.map { it.group }.distinct()
                catAdapter.submit(listOf(FAV, ALL) + groups, selectedCat)
                applyFilter()
                rvCats.post { rvCats.findViewHolderForAdapterPosition(1)?.itemView?.requestFocus() }
            } catch (e: Exception) {
                tvEmpty.text = "Error al cargar la lista:\n${e.message}"
                tvEmpty.visibility = View.VISIBLE
            }
            progress.visibility = View.GONE
        }
    }

    private fun applyFilter() {
        val base = when (selectedCat) {
            FAV -> all.filter { prefs.isFav(it.url) }
            ALL -> all
            else -> all.filter { it.group == selectedCat }
        }
        val q = query.trim().lowercase()
        val list = if (q.isEmpty()) base else base.filter { it.name.lowercase().contains(q) }
        chAdapter.submit(list)
        if (list.isEmpty() && all.isNotEmpty()) {
            tvEmpty.text = if (selectedCat == FAV && q.isEmpty())
                "Aún no hay favoritos.\nMantén pulsado OK sobre un canal para añadirlo."
            else "Sin resultados"
            tvEmpty.visibility = View.VISIBLE
        } else if (all.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
        }
    }
}
