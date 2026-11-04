package com.example.vanzzyos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vanzzyos.databinding.ItemKostBinding
import com.example.vanzzyos.models.Kost

class KostAdapter(
    private val onClick: (Kost) -> Unit
) : RecyclerView.Adapter<KostAdapter.VH>() {
    private var list: List<Kost> = emptyList()
    class VH(val binding: ItemKostBinding) : RecyclerView.ViewHolder(binding.root)

    fun submit(items: List<Kost>) {
        list = items
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            ivFotoKost.setImageResource(item.fotoUtama)
            tvNamaKost.text = item.nama
            tvAlamatKost.text = "⌖  ${item.kota} · ${item.alamat}"
            tvHargaKost.text = "${item.harga} / bln"
            tvRatingKost.text = "★  %.1f".format(item.rating)
            root.setOnClickListener { onClick(item) }
        }
    }
    override fun getItemCount(): Int = list.size
}
