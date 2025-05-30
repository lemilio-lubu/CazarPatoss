package com.emilio.jacome.cazarpatoss

import android.animation.ValueAnimator
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var textViewUser: TextView
    private lateinit var textViewCounter: TextView
    private lateinit var textViewTime: TextView
    private lateinit var imageViewDuck: ImageView

    private lateinit var soundPool: SoundPool
    private val handler = Handler(Looper.getMainLooper())

    private var counter = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var soundId: Int = 0
    private var isLoaded = false
    private var gameOver = false

    private lateinit var countDownTimer: CountDownTimer

    companion object {
        const val EXTRA_LOGIN = "EXTRA_LOGIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de vistas
        textViewUser = findViewById(R.id.textViewUser)
        textViewCounter = findViewById(R.id.textViewCounter)
        textViewTime = findViewById(R.id.textViewTime)
        imageViewDuck = findViewById(R.id.imageViewDuck)

        // Usuario desde Login
        val usuario = intent.extras?.getString(EXTRA_LOGIN) ?: "Unknown"
        textViewUser.text = usuario

        initializeScreen()
        initializeCountdown()

        // Configurar sonido
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(10)
            .build()

        soundId = soundPool.load(this, R.raw.gunshot, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ -> isLoaded = true }

        imageViewDuck.setOnClickListener {
            if (gameOver) return@setOnClickListener

            counter++
            if (isLoaded) {
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            }

            textViewCounter.text = counter.toString()
            imageViewDuck.setImageResource(R.drawable.duck_clicked)

            handler.postDelayed({
                imageViewDuck.setImageResource(R.drawable.duck)
            }, 500)

            moveDuck()
        }
    }

    private fun initializeScreen() {
        val display = resources.displayMetrics
        screenWidth = display.widthPixels
        screenHeight = display.heightPixels
    }

    private fun initializeCountdown() {
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                textViewTime.text = "${secondsRemaining}s"
            }

            override fun onFinish() {
                textViewTime.text = "0s"
                gameOver = true
                showGameOverDialog()
            }
        }
        countDownTimer.start()
    }

    private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)
        builder
            .setMessage(getString(R.string.dialog_message_congratulations, counter))
            .setTitle(getString(R.string.dialog_title_game_end))
            .setPositiveButton(getString(R.string.button_restart)) { _, _ ->
                restartGame()
            }
            .setNegativeButton(getString(R.string.button_close), null)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun restartGame() {
        counter = 0
        gameOver = false
        textViewCounter.text = counter.toString()
        countDownTimer.cancel()
        initializeCountdown()
        moveDuck()
    }

    private fun moveDuck() {
        when (Random.nextInt(3)) {
            0 -> moveDuckRandomly()
            1 -> moveDuckRandomlyWithAnimation()
            2 -> moveDuckRandomlyWithParabola()
        }
    }

    private fun moveDuckRandomly() {
        val min = imageViewDuck.width / 2
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height
        val randomX = Random.nextInt(min, maxX)
        val randomY = Random.nextInt(min, maxY)
        imageViewDuck.x = randomX.toFloat()
        imageViewDuck.y = randomY.toFloat()
    }

    private fun moveDuckRandomlyWithAnimation() {
        val min = imageViewDuck.width / 2
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height
        val randomX = Random.nextInt(min, maxX)
        val randomY = Random.nextInt(min, maxY)
        imageViewDuck.animate()
            .x(randomX.toFloat())
            .y(randomY.toFloat())
            .setDuration(300)
            .start()
    }

    private fun moveDuckRandomlyWithParabola() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height

        if (maxX > 0 && maxY > 0) {
            val startX = imageViewDuck.x
            val startY = imageViewDuck.y
            val endX = Random.nextInt(0, maxX).toFloat()
            val endY = Random.nextInt(0, maxY).toFloat()
            val duration = 300L

            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    val t = animation.animatedValue as Float
                    val currentX = startX + (endX - startX) * t
                    val arcHeight = 300f
                    val midT = t * (1 - t) * 4
                    val currentY = startY + (endY - startY) * t - arcHeight * midT
                    imageViewDuck.x = currentX
                    imageViewDuck.y = currentY
                }
            }
            animator.start()
        }
    }

    override fun onStop() {
        Log.w(EXTRA_LOGIN, "Play canceled")
        countDownTimer.cancel()
        textViewTime.text = "0s"
        gameOver = true
        soundPool.stop(soundId)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
