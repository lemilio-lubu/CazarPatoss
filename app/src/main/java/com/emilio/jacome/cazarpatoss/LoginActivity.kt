package com.emilio.jacome.cazarpatoss

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonNewUser: Button
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        // Inicialización de variables
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonNewUser = findViewById(R.id.buttonNewUser)

        // Reproduce música
        mediaPlayer = MediaPlayer.create(this, R.raw.title_screen)
        mediaPlayer.start()

        // Evento clic de login
        buttonLogin.setOnClickListener {
            if (!validateRequiredData()) return@setOnClickListener

            val email = editTextEmail.text.toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_LOGIN, email)
            startActivity(intent)
            finish()
        }

        // Evento clic de nuevo usuario (sin acción definida aún)
        buttonNewUser.setOnClickListener {
            // Aquí puedes abrir un registro o mostrar un mensaje
        }
    }

    private fun validateRequiredData(): Boolean {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty()) {
            editTextEmail.error = getString(R.string.error_email_required)
            editTextEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.error_password_required)
            editTextPassword.requestFocus()
            return false
        }

        if (password.length < 3) {
            editTextPassword.error = getString(R.string.error_password_min_length)
            editTextPassword.requestFocus()
            return false
        }

        return true
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_LOGIN = "email"
    }
}
