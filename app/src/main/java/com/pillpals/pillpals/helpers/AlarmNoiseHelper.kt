package com.pillpals.pillpals.helpers

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator

class AlarmNoiseHelper(context: Context) {
    private var context: Context
    private var mediaPlayer: MediaPlayer?
    private var audioAttributes: AudioAttributes
    private var vibrator: Vibrator
    private var vibrationEffect: VibrationEffect

    init {
        this.context = context

        mediaPlayer = null

        audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        //The VibrationEffect pattern (LongArray) needs to be a total of >5 seconds or vibration will be subject to user's touch vibration settings
        //You will not find this in the documentation
        vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrationEffect = VibrationEffect.createWaveform(longArrayOf(0, 1000, 2000, 1000, 2000),1)
    }

    fun startNoise() {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.setAudioAttributes(audioAttributes)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
        vibrator.vibrate(vibrationEffect, audioAttributes)
    }

    fun stopNoise() {
        releaseMediaPlayer()
        vibrator.cancel()
    }

    private fun releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}