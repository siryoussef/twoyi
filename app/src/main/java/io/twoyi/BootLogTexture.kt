package io.twoyi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.TextureView
import android.view.View
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import io.twoyi.utils.ShellUtil
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BootLogTexture @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : TextureView(context, attrs, defStyleAttr, defStyleRes), TextureView.SurfaceTextureListener {

    private val rendering = AtomicBoolean(false)
    private val logMessages = LimitedQueue<String>(160)
    private val snapshot = LinkedList<String>()
    private val paints = SparseArray<Paint>()
    private val defaultPaint = Paint()

    companion object {
        private val COLOR_MAP = SparseIntArray().apply {
            put('V'.code, 0xBBBBBB)
            put('D'.code, 0x5EBB1E)
            put('I'.code, 0x4CBBA2)
            put('W'.code, 0xFFD21C)
            put('E'.code, 0xFF6B68)
            put('F'.code, Color.RED)
            put('S'.code, Color.WHITE)
        }
    }

    init {
        surfaceTextureListener = this
        
        for (i in 0 until COLOR_MAP.size()) {
            val key = COLOR_MAP.keyAt(i)
            val value = COLOR_MAP.valueAt(i)
            Paint().apply {
                setPaint(this, value)
                paints.put(key, this)
            }
        }
        
        setPaint(defaultPaint, Color.WHITE)
    }

    private fun setPaint(paint: Paint, color: Int) {
        paint.apply {
            this.color = color
            isAntiAlias = true
            textSize = 16f
            alpha = 128
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        rendering.set(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rendering.set(false)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        rendering.set(visibility == VISIBLE)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Shell.EXECUTOR.execute {
            val callbackList = object : CallbackList<String>() {
                override fun onAddElement(s: String) {
                    if (TextUtils.isEmpty(s)) return
                    synchronized(logMessages) {
                        logMessages.add(s)
                    }
                }
            }

            val shell = ShellUtil.newSh()
            shell.newJob().add("timeout -s 9 30 logcat -v brief *I").to(callbackList).submit()

            while (rendering.get()) {
                render()
                SystemClock.sleep(16)
            }

            try {
                shell.waitAndClose(1, TimeUnit.SECONDS)
            } catch (ignored: Throwable) {
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        rendering.set(false)
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun render() {
        val canvas = lockCanvas() ?: return
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            snapshot.clear()
            synchronized(logMessages) {
                snapshot.addAll(logMessages)
            }

            var count = 0
            for (log in snapshot) {
                val chr = log[0]
                val paint = paints.get(chr.code) ?: defaultPaint
                canvas.drawText(log, 0f, (count++ * 20).toFloat(), paint)
            }
        } finally {
            unlockCanvasAndPost(canvas)
        }
    }
}