package com.stargroup.merchantorder.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stargroup.merchantorder.R
import com.stargroup.merchantorder.data.model.response.Transaction
import com.stargroup.merchantorder.databinding.ItemOrderBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OrderAdapter( private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, OrderAdapter.ViewHolder>(DiffCallback()) {

    private val originalList = mutableListOf<Transaction>()

    private var currentStatus = "ALL"

    fun setOrders(orders: List<Transaction>) {
        originalList.clear()
        originalList.addAll(orders)

        filterByStatus(currentStatus)
    }

    fun filterByStatus(status: String) {
        currentStatus = status

        val result = when (status.uppercase()) {
            "ALL" -> originalList.toList()
            else -> originalList.filter {
                it.orderStatus.equals(status, ignoreCase = true)
            }
        }

        submitList(result)
    }

    inner class ViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) {
            binding.tvOrderId.text = item.invoiceNumber
            binding.tvCustomer.text = item.customer
            binding.tvTable.text = item.table

            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                .withZone(ZoneOffset.UTC)

            val time = formatter.format(Instant.parse(item.addTime))

            binding.tvTimeOrder.text = time

            binding.btDetail.setOnClickListener {
                onItemClick(item)
            }

            when (item.orderStatus.uppercase()) {

                "UNPAID" -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.red_600)
                    )

                    binding.tvStatus.text = "BELUM BAYAR"
                }

                "PAID" -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.orange_700)
                    )

                    binding.tvStatus.text = "SUDAH BAYAR"
                }

                "PROCESSING" -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.blue_600)
                    )

                    binding.tvStatus.text = "DI PROSES"
                }

                "COMPLETED" -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.green_600)
                    )

                    binding.tvStatus.text = "SELESAI"
                }

                "CANCELLED" -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.red_600)
                    )

                    binding.tvStatus.text = "DI BATALKAN"
                }

                else -> {
                    binding.tvStatus.setTextColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.black)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {

        override fun areItemsTheSame(
            oldItem: Transaction,
            newItem: Transaction
        ): Boolean {
            return oldItem.transactionId == newItem.transactionId
        }

        override fun areContentsTheSame(
            oldItem: Transaction,
            newItem: Transaction
        ): Boolean {
            return oldItem == newItem
        }
    }
}