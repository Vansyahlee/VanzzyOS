package com.example.vanzzyos.adapters

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageSliderAdapter(private val list: List<Int>) : RecyclerView.Adapter<ImageSliderAdapter.VH>() {
    class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = ImageView(parent.context)
        iv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.iv.setImageResource(list[position])
    }

    override fun getItemCount() = list.size
}