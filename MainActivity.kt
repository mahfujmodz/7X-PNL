package com.hexblade.userpanel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var permissionRequested = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest) =
                assetLoader.shouldInterceptRequest(request.url)
        }
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(OverlayBridge(), "AndroidOverlay")
        setContentView(web)
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }
    inner class OverlayBridge {
        @JavascriptInterface fun requestOverlay() { runOnUiThread {
            if (Settings.canDrawOverlays(this@MainActivity)) startOverlay()
            else {
                permissionRequested = true
                val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(i)
                Toast.makeText(this@MainActivity, "Allow Display over other apps, then return here", Toast.LENGTH_LONG).show()
            }
        } }
    }
    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) { Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_SHORT).show(); return }
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
        Toast.makeText(this, "Floating panel started", Toast.LENGTH_SHORT).show()
    }
    override fun onResume() { super.onResume(); if (permissionRequested) { permissionRequested = false; if (Settings.canDrawOverlays(this)) startOverlay() } }
    override fun onDestroy() { if (::web.isInitialized) web.destroy(); super.onDestroy() }
}
