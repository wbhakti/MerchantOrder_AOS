package com.stargroup.merchantorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.stargroup.merchantorder.data.repository.UserRepository
import com.stargroup.merchantorder.databinding.ActivityLoginBinding
import com.stargroup.merchantorder.utils.DeviceUtils
import com.stargroup.merchantorder.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository = UserRepository()
    private var tokenFcm = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val session = SessionManager(this)

        if (session.isLogin()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(binding.login) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(
//                systemBars.left,
//                systemBars.top,
//                systemBars.right,
//                systemBars.bottom
//            )
//            insets
//        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e("FCM", "Failed", task.exception)
                    return@addOnCompleteListener
                }

                tokenFcm = task.result

                Log.d("FCM TOKEN", tokenFcm)
            }

        binding.btnLogin.setOnClickListener {

            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // TODO: Panggil API Login
            if (username.isEmpty()) {
                binding.etUsername.error = "Username wajib diisi"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Password wajib diisi"
                return@setOnClickListener
            }

            lifecycleScope.launch {

                try {

                    val response = repository.login(
                        username,
                        password,
                        tokenFcm,
                        DeviceUtils.getDeviceName()
                    )

                    if (response.isSuccessful) {

                        val body = response.body()

                        if (body?.responseCode == "0") {

                            val login = body.data!!

                            Toast.makeText(
                                this@LoginActivity,
                                body.responseMessage,
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("TOKEN", login.token)

                            // Simpan session di sini
                            val session = SessionManager(this@LoginActivity)
                            session.saveLogin(login)

                            // Pindah ke HOME
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    HomeActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this@LoginActivity,
                                body?.responseMessage ?: "Login gagal",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    } else {

                        Toast.makeText(
                            this@LoginActivity,
                            "HTTP Error ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this@LoginActivity,
                        e.localizedMessage ?: "Terjadi kesalahan",
                        Toast.LENGTH_LONG
                    ).show()

                }

            }
        }
    }
}