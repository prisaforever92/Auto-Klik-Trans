package com.autoklik.android.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*

class AutoClickAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    // 1. Eksekusi Tap pada koordinat (x, y)
    @RequiresApi(Build.VERSION_CODES.N)
    fun executeTap(x: Float, y: Float, durationMs: Long = 50L, callback: (() -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { callback?.invoke() }
            override fun onCancelled(gestureDescription: GestureDescription?) { callback?.invoke() }
        }, null)
    }

    // 2. Eksekusi Swipe (Ke Atas / Ke Bawah)
    @RequiresApi(Build.VERSION_CODES.N)
    fun executeSwipe(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        durationMs: Long = 300L,
        callback: (() -> Unit)? = null
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { callback?.invoke() }
            override fun onCancelled(gestureDescription: GestureDescription?) { callback?.invoke() }
        }, null)
    }

    // 3. Eksekusi Paste Teks (Tap kolom terlebih dahulu, lalu paste)
    fun executePaste(
        x: Float, y: Float,
        textToPaste: String,
        autoTapToFocus: Boolean = true,
        callback: (() -> Unit)? = null
    ) {
        val performPaste = {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("AutoKlikPaste", textToPaste)
            clipboard.setPrimaryClip(clip)

            val rootNode = rootInActiveWindow
            val focusedNode = rootNode?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

            if (focusedNode != null && focusedNode.isEditable) {
                val arguments = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToPaste)
                }
                focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                focusedNode.recycle()
            }
            rootNode?.recycle()
            callback?.invoke()
        }

        if (autoTapToFocus) {
            executeTap(x, y, 50L) {
                serviceScope.launch {
                    delay(80)
                    performPaste()
                }
            }
        } else {
            performPaste()
        }
    }
}
