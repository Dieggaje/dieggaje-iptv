package com.iptvtv.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var tvName: TextView
    private lateinit var tvError: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val hideName = Runnable { tvName.visibility = View.GONE }

    private var current: Channel? = null
    private var triedAlt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)
        tvName = findViewById(R.id.tvName)
        tvError = findViewById(R.id.tvError)
    }

    override fun onStart() {
        super.onStart()
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android TV) IPTVPlayer/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
        val p = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(http))
            .build()
        p.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val alt = current?.alt
                if (!triedAlt && alt != null) {
                    triedAlt = true
                    playUrl(alt)
                } else {
                    tvError.visibility = View.VISIBLE
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) tvError.visibility = View.GONE
            }
        })
        player = p
        playerView.player = p
        playIndex(Store.index)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(hideName)
        playerView.player = null
        player?.release()
        player = null
    }

    private fun playIndex(i: Int) {
        val list = Store.playlist
        if (list.isEmpty()) {
            finish()
            return
        }
        val idx = ((i % list.size) + list.size) % list.size
        Store.index = idx
        val ch = list[idx]
        current = ch
        triedAlt = false
        tvError.visibility = View.GONE
        showName()
        playUrl(ch.url)
    }

    private fun playUrl(url: String) {
        val p = player ?: return
        p.setMediaItem(MediaItem.fromUri(url))
        p.prepare()
        p.playWhenReady = true
    }

    private fun showName() {
        val ch = current ?: return
        tvName.text = "${Store.index + 1}. ${ch.name}"
        tvName.visibility = View.VISIBLE
        handler.removeCallbacks(hideName)
        handler.postDelayed(hideName, 3500)
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if (e.action == KeyEvent.ACTION_DOWN) {
            when (e.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_CHANNEL_UP,
                KeyEvent.KEYCODE_PAGE_UP -> {
                    playIndex(Store.index - 1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_CHANNEL_DOWN,
                KeyEvent.KEYCODE_PAGE_DOWN -> {
                    playIndex(Store.index + 1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    showName()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(e)
    }
}
