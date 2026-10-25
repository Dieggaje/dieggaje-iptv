package com.iptvtv.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val editing = intent.getBooleanExtra("edit", false)
        if (!editing && prefs.isConfigured()) {
            openChannels()
            return
        }

        setContentView(R.layout.activity_login)
        val etServer = findViewById<EditText>(R.id.etServer)
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etM3u = findViewById<EditText>(R.id.etM3u)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val tvError = findViewById<TextView>(R.id.tvError)

        etServer.setText(prefs.server)
        etUser.setText(prefs.user)
        etPass.setText(prefs.pass)
        etM3u.setText(prefs.m3u)

        btnSave.setOnClickListener {
            val server = etServer.text.toString().trim()
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val m3u = etM3u.text.toString().trim()

            if (m3u.isEmpty() && (server.isEmpty() || user.isEmpty() || pass.isEmpty())) {
                tvError.text = "Rellena servidor, usuario y contraseña, o una URL M3U."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            prefs.server = server
            prefs.user = user
            prefs.pass = pass
            prefs.m3u = m3u
            openChannels()
        }
    }

    private fun openChannels() {
        startActivity(Intent(this, ChannelsActivity::class.java))
        finish()
    }
}
