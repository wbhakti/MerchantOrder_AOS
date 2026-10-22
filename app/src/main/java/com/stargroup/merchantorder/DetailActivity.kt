package com.stargroup.merchantorder

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.stargroup.merchantorder.data.model.response.Transaction
import com.stargroup.merchantorder.data.repository.TransactionRepository
import com.stargroup.merchantorder.databinding.ActivityDetailBinding
import com.stargroup.merchantorder.ui.adapter.MenuAdapter
import com.stargroup.merchantorder.utils.SessionManager
import com.stargroup.merchantorder.utils.toRupiah
import kotlinx.coroutines.launch
//import coil3.load

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val menuAdapter = MenuAdapter()
    private val repository = TransactionRepository()
    private var token : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = SessionManager(this).getToken()

        val transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("transaction", Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Transaction>("transaction")
        }

        binding.rvMenu.layoutManager = LinearLayoutManager(this)
        binding.rvMenu.adapter = menuAdapter


        ViewCompat.setOnApplyWindowInsetsListener(binding.detail) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Order"
        }

        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_OK)
            finish()
        }


        if (transaction != null) {
            menuAdapter.submitList(transaction.details)

            binding.tvOrderId.text = transaction.invoiceNumber
            binding.tvCustomer.text = transaction.customer
            binding.tvTable.text = "Meja : ${transaction.table}"
            binding.tvDate.text = transaction.addTime
            binding.tvSubtotal.text = transaction.grandTotal.toRupiah()
            binding.tvDiscount.text = transaction.discount.toRupiah()
            binding.tvTotal.text = (transaction.grandTotal - transaction.discount).toRupiah()
            Picasso.get()
                .load(transaction.paymentProof)
                .placeholder(R.drawable.logo_merchant_order)
                .error(R.drawable.logo_merchant_order)
                .into(binding.imgTransfer)

            updateStatusView(transaction.orderStatus)

            binding.btnProcess.setOnClickListener {
                val idTransaksi = transaction.transactionId
                val status = binding.btnProcess.text.toString()

                updateStatus(idTransaksi,status)
            }

            binding.btnCancel.setOnClickListener {
                val idTransaksi = transaction.transactionId
                updateStatus(idTransaksi,"CANCELLED")
            }
        }
    }

    private fun updateStatus(idTransaksi: Int, status : String) {
        lifecycleScope.launch {

            try {

                val response = repository.setStatus(
                    token = token,
                    idTransaksi,
                    status
                )

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body?.responseCode == "0") {
                        body.data?.orderStatus?.let { status ->
                            updateStatusView(status)
                        }
                    } else {
                        Toast.makeText(
                            this@DetailActivity,
                            body?.responseMessage ?: "Update Status gagal",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } else {

                    Toast.makeText(
                        this@DetailActivity,
                        "HTTP Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@DetailActivity,
                    e.localizedMessage ?: "Terjadi kesalahan",
                    Toast.LENGTH_LONG
                ).show()

            }

        }
    }

    private fun updateStatusView(status : String) {


        when (status.uppercase()) {

            "UNPAID" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.red_600)
                )
                binding.tvStatus.text = "BELUM BAYAR"
                binding.btnProcess.text = "PROCESSING"
            }

            "PAID" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.orange_700)
                )
                binding.tvStatus.text = "SUDAH BAYAR"
                binding.btnProcess.text = "PROCESSING"
            }

            "PROCESSING" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.green_600)
                )
                binding.tvStatus.text = "DI PROSES"
                binding.btnProcess.text = "COMPLETED"
            }

            "COMPLETED" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.blue_600)
                )
                binding.tvStatus.text = "SELESAI"
                binding.btnProcess.isEnabled = false
                binding.btnCancel.isEnabled = false
            }

            "CANCELLED" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.blue_600)
                )
                binding.tvStatus.text = "DI BATALKAN"
                binding.btnProcess.isEnabled = false
                binding.btnCancel.isEnabled = false
            }

            else -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
            }
        }
    }
}