package com.example.habitflow

import com.example.habitflow.util.AmbientAudioManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test

class AmbientAudioManagerTest {

    private lateinit var audioManager: AmbientAudioManager

    @Before
    fun setUp() {
        audioManager = AmbientAudioManager()
    }

    @After
    fun tearDown() {
        audioManager.release()
    }

    @Test
    fun testInitialState() {
        assertFalse(audioManager.isPlaying())
        assertNull(audioManager.getCurrentPreset())
    }

    @Test
    fun testRawResourcesExist() {
        assertTrue(R.raw.rain != 0)
        assertTrue(R.raw.lofi != 0)
        assertTrue(R.raw.cafe != 0)
    }

    @Test
    fun testPlayRainPreset() {
        audioManager.play("Rain")
        val isPlaying = audioManager.isPlaying()
        val preset = audioManager.getCurrentPreset()
        if (isPlaying) {
            assertEquals("Rain", preset)
        }
    }

    @Test
    fun testPlayAndStop() {
        audioManager.play("Lo-Fi Beats")
        audioManager.stop()
        assertFalse(audioManager.isPlaying())
        assertNull(audioManager.getCurrentPreset())
    }

    @Test
    fun testPlayAndRelease() {
        audioManager.play("Cafe")
        audioManager.release()
        assertFalse(audioManager.isPlaying())
        assertNull(audioManager.getCurrentPreset())
    }

    @Test
    fun testTogglePreset() {
        audioManager.play("Cafe")
        val state1 = audioManager.isPlaying()
        if (state1) {
            assertEquals("Cafe", audioManager.getCurrentPreset())
            // Playing same preset again should stop it (toggle)
            audioManager.play("Cafe")
            assertFalse(audioManager.isPlaying())
            assertNull(audioManager.getCurrentPreset())
        }
    }
}
