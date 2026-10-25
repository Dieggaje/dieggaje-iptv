package com.iptvtv.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CategoryAdapter(
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var items: List<String> = emptyList()
    private var selected: String = ""

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val t: TextView = v.findViewById(R.id.tvCat)
    }

    fun submit(list: List<String>, sel: String) {
        items = list
        selected = sel
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val c = items[position]
        h.t.text = c
        h.t.isActivated = c == selected
        h.t.setOnClickListener {
            val old = items.indexOf(selected)
            selected = c
            if (old >= 0) notifyItemChanged(old)
            notifyItemChanged(h.bindingAdapterPosition)
            onSelect(c)
        }
    }
}

class ChannelAdapter(
    private val isFav: (Channel) -> Boolean,
    private val onPlay: (List<Channel>, Int) -> Unit,
    private val onToggleFav: (Channel, Int) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.VH>() {

    private var items: List<Channel> = emptyList()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val logo: ImageView = v.findViewById(R.id.ivLogo)
        val name: TextView = v.findViewById(R.id.tvName)
        val star: TextView = v.findViewById(R.id.tvStar)
    }

    fun submit(list: List<Channel>) {
        items = list
        notifyDataSetChanged()
    }

    fun refresh(pos: Int) = notifyItemChanged(pos)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val c = items[position]
        h.name.text = c.name
        h.star.visibility = if (isFav(c)) View.VISIBLE else View.GONE
        h.logo.load(c.logo) {
            size(96)
            placeholder(R.drawable.ic_tv)
            error(R.drawable.ic_tv)
            fallback(R.drawable.ic_tv)
        }
        h.itemView.setOnClickListener {
            val pos = h.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onPlay(items, pos)
        }
        h.itemView.setOnLongClickListener {
            val pos = h.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onToggleFav(items[pos], pos)
            true
        }
    }
}
