package com.hexblade.userpanel

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var overlay: WebView? = null
    private lateinit var params: WindowManager.LayoutParams
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (overlay != null) return START_NOT_STICKY
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val web = WebView(this)
        overlay = web
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val assetLoader = androidx.webkit.WebViewAssetLoader.Builder().addPathHandler("/assets/", androidx.webkit.WebViewAssetLoader.AssetsPathHandler(this)).build()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest) = assetLoader.shouldInterceptRequest(request.url)
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript("document.getElementById('drag').addEventListener('touchstart',e=>AndroidDrag.start(e.touches[0].screenX,e.touches[0].screenY));document.getElementById('drag').addEventListener('touchmove',e=>{AndroidDrag.move(e.touches[0].screenX,e.touches[0].screenY);e.preventDefault()},{passive:false});", null)
            }
        }
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(object { @JavascriptInterface fun closeOverlay() { web.post { stopSelf() } } }, "AndroidOverlay")
        web.addJavascriptInterface(object { var sx=0f; var sy=0f; var ox=0; var oy=0
            @JavascriptInterface fun start(x:Float,y:Float) { sx=x; sy=y; ox=params.x; oy=params.y }
            @JavascriptInterface fun move(x:Float,y:Float) { web.post { params.x=ox+((x-sx)/resources.displayMetrics.density).toInt(); params.y=oy+((y-sy)/resources.displayMetrics.density).toInt(); try { wm.updateViewLayout(web,params) } catch (_:Exception) {} } }
        }, "AndroidDrag")
        params = WindowManager.LayoutParams( if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START; params.x = 18; params.y = 100; params.width = dp(280); params.height = dp(465)
        web.loadUrl("https://appassets.androidplatform.net/assets/overlay.html")
        try { wm.addView(web, params) } catch (e: Exception) { Toast.makeText(this, "Could not display floating panel", Toast.LENGTH_SHORT).show(); stopSelf() }
        return START_NOT_STICKY
    }
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    override fun onDestroy() { try { overlay?.let { wm.removeView(it); it.destroy() } } catch (_:Exception) {}; overlay=null; super.onDestroy() }
}
