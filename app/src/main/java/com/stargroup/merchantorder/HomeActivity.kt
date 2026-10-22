
package com.stargroup.merchantorder

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.material.card.MaterialCardView
import com.stargroup.merchantorder.data.repository.TransactionRepository
import com.stargroup.merchantorder.data.repository.UserRepository
import com.stargroup.merchantorder.databinding.ActivityHomeBinding
import com.stargroup.merchantorder.ui.adapter.OrderAdapter
import com.stargroup.merchantorder.utils.DeviceUtils
import com.stargroup.merchantorder.utils.NotificationEvent
import com.stargroup.merchantorder.utils.SessionManager
import com.stargroup.merchantorder.utils.countByStatus
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: OrderAdapter

    private val repository = TransactionRepository()
    private val repositoryUser = UserRepository()

    private var token: String = ""
    private var selectedStatus: String = "ALL"

    // =========================================================
    // ORDER CARDS
    // =========================================================

    private val cards by lazy {
        listOf(
            binding.cardTotalOrder,
            binding.cardBelumBayar,
            binding.cardBelumDiProses,
            binding.cardProses,
            binding.cardSelesai,
            binding.cardCancel
        )
    }

    private val strokeColors by lazy {
        listOf(
            Color.parseColor("#6A1B9A"), // Total Order
            Color.parseColor("#C62828"), // Belum Bayar
            Color.parseColor("#F57C00"), // Belum Di Proses
            Color.parseColor("#1976D2"), // Proses
            Color.parseColor("#2E7D32"), // Selesai
            Color.parseColor("#616161")  // Cancel
        )
    }

    // =========================================================
    // ACTIVITY RESULT LAUNCHERS
    // =========================================================

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                Log.d("NOTIFICATION", "Permission diberikan")
            } else {
                Log.d("NOTIFICATION", "Permission ditolak")
            }
        }

    private val detailOrderLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                loadHistory()
            }
        }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = SessionManager(this).getToken()

        setupWindowInsets()
        setupHeader()
        setupRecyclerView()
        setupRefresh()
        setupCardFilters()

        requestNotificationPermission()
        loadHistory()
        registerFcmToken()
        listenNotification()
    }

    // =========================================================
    // UI SETUP
    // =========================================================

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.home) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun setupHeader() {
        binding.tvUsername.text = SessionManager(this).getUsername()

        val formatter = DateTimeFormatter.ofPattern(
            "EEEE, dd MMMM yyyy",
            Locale("id", "ID")
        )

        binding.tvDateNow.text = LocalDate.now().format(formatter)
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter { transaction ->

            val intent = Intent(
                this@HomeActivity,
                DetailActivity::class.java
            )

            intent.putExtra("transaction", transaction)

            detailOrderLauncher.launch(intent)
        }

        binding.rvOrder.layoutManager = LinearLayoutManager(this)
        binding.rvOrder.adapter = adapter
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadHistory()
        }
    }

    // =========================================================
    // CARD FILTER
    // =========================================================

    private fun setupCardFilters() {

        binding.cardTotalOrder.setOnClickListener {
            selectCard(0, "ALL")
        }

        binding.cardBelumBayar.setOnClickListener {
            selectCard(1, "UNPAID")
        }

        binding.cardBelumDiProses.setOnClickListener {
            selectCard(2, "PAID")
        }

        binding.cardProses.setOnClickListener {
            selectCard(3, "PROCESSING")
        }

        binding.cardSelesai.setOnClickListener {
            selectCard(4, "COMPLETED")
        }

        binding.cardCancel.setOnClickListener {
            selectCard(5, "CANCELLED")
        }

        // Filter default saat halaman dibuka
        selectCard(0, "ALL")
    }

    private fun selectCard(
        selectedIndex: Int,
        status: String
    ) {
        selectedStatus = status

        cards.forEachIndexed { index, card ->

            val selected = index == selectedIndex

            card.strokeWidth = if (selected) dpToPx(3) else 0

            card.strokeColor = strokeColors[index]

            card.cardElevation = dpToPx(
                if (selected) 6 else 2
            ).toFloat()
        }

        filterOrder(status)
    }

    private fun filterOrder(status: String) {
        adapter.filterByStatus(status)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // =========================================================
    // LOAD ORDER HISTORY
    // =========================================================

    private fun loadHistory() {
        lifecycleScope.launch {

            binding.swipeRefresh.isRefreshing = true

            try {
                val response = repository.getTransactions(token)

                if (!response.isSuccessful) {

                    if (response.code() == 401) {
                        handleUnauthorized()
                    } else {
                        Log.e(
                            "ORDER",
                            "Gagal mengambil order: HTTP ${response.code()}"
                        )
                    }

                    return@launch
                }

                val body = response.body()

                if (body?.responseCode != "0") {
                    Log.e(
                        "ORDER",
                        "Response order tidak berhasil"
                    )
                    return@launch
                }

                val list = body.data ?: emptyList()

                // Update jumlah order pada setiap card
                binding.tvTotal.text = list.size.toString()

                binding.tvBelumBayar.text =
                    list.countByStatus("UNPAID").toString()

                binding.tvPayment.text =
                    list.countByStatus("PAID").toString()

                binding.tvProses.text =
                    list.countByStatus("PROCESSING").toString()

                binding.tvSelesai.text =
                    list.countByStatus("COMPLETED").toString()

                binding.tvCancel.text =
                    list.countByStatus("CANCELLED").toString()

                // Update data adapter.
                // Filter yang aktif tetap dipertahankan.
                adapter.setOrders(list)
                adapter.filterByStatus(selectedStatus)

            } catch (e: Exception) {

                Log.e(
                    "ORDER",
                    "Terjadi kesalahan saat mengambil order",
                    e
                )

            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun handleUnauthorized() {
        SessionManager(this).logout()

        val intent = Intent(
            this@HomeActivity,
            LoginActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    // =========================================================
    // FIREBASE CLOUD MESSAGING
    // =========================================================

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e(
                        "FCM",
                        "Gagal mendapatkan FCM token",
                        task.exception
                    )
                    return@addOnCompleteListener
                }

                val fcmToken = task.result

                val userId = SessionManager(this)
                    .getUserId()
                    .toString()

                Log.d("FCM TOKEN", fcmToken)

                updateFcm(userId, fcmToken)
            }
    }

    private fun updateFcm(
        userId: String,
        fcmToken: String
    ) {
        lifecycleScope.launch {
            try {
                val response = repositoryUser.setFcmToken(
                    userId,
                    fcmToken,
                    DeviceUtils.getDeviceName(),
                    token
                )

                if (response.isSuccessful &&
                    response.body()?.responseCode == "0"
                ) {
                    Log.d("FCM", "Token berhasil diperbarui")
                } else {
                    Log.e("FCM", "Gagal memperbarui token")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error update FCM token", e)
            }
        }
    }

    // =========================================================
    // NOTIFICATION PERMISSION
    // =========================================================

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    // =========================================================
    // LISTEN NEW ORDER NOTIFICATION
    // =========================================================

    private fun listenNotification() {
        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                NotificationEvent.newOrder.collect { transactionId ->

                    Log.d(
                        "HOME",
                        "Ada order baru: $transactionId"
                    )

                    animateBelumDiProses()
                    loadHistory()
                }
            }
        }
    }

    // =========================================================
    // ANIMATION
    // =========================================================

    private fun animateBelumDiProses() {

        val card: MaterialCardView = binding.cardBelumDiProses

        val animator = ObjectAnimator.ofPropertyValuesHolder(
            card,
            PropertyValuesHolder.ofFloat(
                "scaleX",
                1f, 1.05f, 1f
            ),
            PropertyValuesHolder.ofFloat(
                "scaleY",
                1f, 1.05f, 1f
            )
        )

        animator.duration = 700
        animator.repeatCount = 10
        animator.start()
    }
}
