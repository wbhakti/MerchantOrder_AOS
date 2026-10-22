package com.stargroup.merchantorder.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stargroup.merchantorder.data.model.entity.DetailItem
import com.stargroup.merchantorder.databinding.ItemMenuBinding
import com.stargroup.merchantorder.utils.toRupiah
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter :
    ListAdapter<DetailItem, MenuAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailItem) {

            binding.tvMenuName.text =  "${item.menuId}  ${item.quantity}x"

            binding.tvPrice.text = item.price.toRupiah()

            binding.tvNote.text =
                if (item.note.isBlank()) "-" else item.note

            if (item.productDiscount > 0) {
                binding.tvDiscount.visibility = View.VISIBLE
                binding.tvDiscount.text = "-${item.productDiscount.toRupiah()}"
            } else {
                binding.tvDiscount.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<DetailItem>() {

        override fun areItemsTheSame(
            oldItem: DetailItem,
            newItem: DetailItem
        ): Boolean {
            return oldItem.menuId == newItem.menuId
        }

        override fun areContentsTheSame(
            oldItem: DetailItem,
            newItem: DetailItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

