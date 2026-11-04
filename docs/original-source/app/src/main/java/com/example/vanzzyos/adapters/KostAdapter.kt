package com.example.vanzzyos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vanzzyos.databinding.ItemKostBinding
import com.example.vanzzyos.models.Kost

class KostAdapter(private val list: List<Kost>, private val onClick: (Kost) -> Unit) :
    RecyclerView.Adapter<KostAdapter.VH>() {

    class VH(val b: ItemKostBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemKostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val i = list[position]
        holder.b.tvNamaKost.text = i.nama
        holder.b.tvHargaKost.text = i.harga
        holder.b.tvRatingKost.text = i.rating.toString()
        holder.b.ivFotoKost.setImageResource(i.fotoUtama)
        holder.itemView.setOnClickListener { onClick(i) }
    }

    override fun getItemCount() = list.size
}