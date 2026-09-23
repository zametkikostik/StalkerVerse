package org.web3browser.dpi

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class DpiProxyService : Service() {
    companion object {
        const val CHANNEL_ID = "web3_dpi_proxy"
        const val NOTIFICATION_ID = 42001
        const val EXTRA_PORT = "port"
        const val EXTRA_MODE = "mode"
        const val DEFAULT_PORT = 8080
        const val ACTION_START = "org.web3browser.dpi.START"
        const val ACTION_STOP = "org.web3browser.dpi.STOP"
        @Volatile var isRunning: Boolean = false
            private set
        fun start(context: Context, port: Int = DEFAULT_PORT, mode: String = "both") {
            val intent = Intent(context, DpiProxyService::class.java).apply {
                action = ACTION_START; putExtra(EXTRA_PORT, port); putExtra(EXTRA_MODE, mode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }
        fun stop(context: Context) {
            context.startService(Intent(context, DpiProxyService::class.java).apply { action = ACTION_STOP })
        }
    }

    private val executor = Executors.newCachedThreadPool()
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var listenPort = DEFAULT_PORT
    private var fragmentMode = "both"

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate(); createNotificationChannel() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopProxy(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY }
            else -> {
                listenPort = intent?.getIntExtra(EXTRA_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
                fragmentMode = intent?.getStringExtra(EXTRA_MODE) ?: "both"
                startForeground(NOTIFICATION_ID, buildNotification("Starting on $listenPort"))
                startProxy()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() { stopProxy(); super.onDestroy() }

    private fun startProxy() {
        if (running.getAndSet(true)) return
        isRunning = true
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Web3Browser:DpiProxy").apply { acquire(60 * 60 * 1000L) }
        executor.execute {
            try {
                serverSocket = ServerSocket().apply { reuseAddress = true; bind(InetSocketAddress("127.0.0.1", listenPort)) }
                updateNotification("HTTP CONNECT :$listenPort ($fragmentMode)")
                while (running.get()) {
                    val client = try { serverSocket?.accept() } catch (e: IOException) { null } ?: continue
                    executor.execute { handleClient(client) }
                }
            } catch (e: Exception) {
                updateNotification("Error: ${e.message}")
            }
        }
    }

    private fun stopProxy() {
        running.set(false); isRunning = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        try { wakeLock?.release() } catch (_: Exception) {}
        wakeLock = null
    }

    private fun handleClient(client: Socket) {
        try {
            client.soTimeout = 30000
            val input = client.getInputStream(); val output = client.getOutputStream()
            val buffer = ByteArray(8192)
            val n = input.read(buffer)
            if (n <= 0) { client.close(); return }
            val header = String(buffer, 0, n, Charsets.ISO_8859_1)
            if (!header.startsWith("CONNECT ")) { output.write("HTTP/1.1 405\r\n\r\n".toByteArray()); client.close(); return }
            val target = header.lineSequence().first().split(" ")[1]
            val host: String; val port: Int
            if (target.contains(":")) { val p = target.split(":"); host = p[0]; port = p[1].toIntOrNull() ?: 443 }
            else { host = target; port = 443 }
            val remote = Socket(); remote.connect(InetSocketAddress(host, port), 12000)
            output.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray()); output.flush()
            val t1 = Thread { forward(client, remote, true) }
            val t2 = Thread { forward(remote, client, false) }
            t1.start(); t2.start(); t1.join(); t2.join(); remote.close()
        } catch (_: Exception) {
        } finally { try { client.close() } catch (_: Exception) {} }
    }

    private fun forward(src: Socket, dst: Socket, fragmentFirst: Boolean) {
        try {
            val input = src.getInputStream(); val output = dst.getOutputStream()
            val buf = ByteArray(65536); var first = fragmentFirst
            while (true) {
                val n = input.read(buf); if (n <= 0) break
                if (first && n > 5 && buf[0] == 0x16.toByte()) {
                    val mid = (n / 3).coerceAtLeast(16)
                    output.write(buf, 0, mid); output.flush(); Thread.sleep(8)
                    if (mid < n) {
                        val mid2 = ((n + mid) / 2).coerceAtMost(n)
                        output.write(buf, mid, mid2 - mid); output.flush(); Thread.sleep(8)
                        if (mid2 < n) { output.write(buf, mid2, n - mid2); output.flush() }
                    }
                    first = false
                } else { output.write(buf, 0, n); output.flush(); first = false }
            }
        } catch (_: Exception) {
        } finally { try { src.shutdownInput() } catch (_: Exception) {} }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "DPI Protection Proxy", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification {
        val stopPi = PendingIntent.getService(this, 0,
            Intent(this, DpiProxyService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Web3 Browser — Anti-DPI")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .addAction(0, "Stop", stopPi)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(text))
    }
}
