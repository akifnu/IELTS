package com.flashcards.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Hosts the bundled Shine web app with a native viewport bridge.
 * Android lays out the WebView in the safe visible rect; exact pixel dimensions
 * are pushed to CSS so the shell always fits — no vh/dvh guessing.
 */
class MainActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var container: FrameLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            fitsSystemWindows = true
        }
        container = root
        root.addView(buildWebView())
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            syncViewport()
            insets
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildWebView(): WebView {
        val view = WebView(this).apply {
            webView = this
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                useWideViewPort = true
                loadWithOverviewMode = false
                textZoom = 100
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = WebSettings.LOAD_DEFAULT
                @Suppress("DEPRECATION")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = true
                }
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = WebView.OVER_SCROLL_NEVER
            addJavascriptInterface(ShineBridge(this@MainActivity), "ShineAndroid")
            webViewClient = ShineWebViewClient()
            webChromeClient = WebChromeClient()
            addOnLayoutChangeListener(layoutChangeListener)
            loadUrl("file:///android_asset/www/index.html")
        }
        return view
    }

    private val layoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> syncViewport() }

    fun syncViewport() {
        val wv = webView ?: return
        val w = wv.width
        val h = wv.height
        if (w <= 0 || h <= 0) return
        // WebView is already laid out inside the safe area (decorFitsSystemWindows).
        val js = "window.__shineViewport&&window.__shineViewport.set($w,$h,0,0,0,0);"
        wv.evaluateJavascript(js, null)
    }

    fun reloadWebApp() {
        val root = container ?: return
        webView?.removeOnLayoutChangeListener(layoutChangeListener)
        webView?.destroy()
        webView = null
        root.removeAllViews()
        root.addView(buildWebView())
        syncViewport()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val wv = webView
        if (wv == null) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
            return
        }
        wv.evaluateJavascript(
            "(function(){return !!(window.shineHandleBack&&window.shineHandleBack());})();",
        ) { result ->
            if (result == "true") return@evaluateJavascript
            if (wv.canGoBack()) wv.goBack()
            else {
                @Suppress("DEPRECATION")
                super.onBackPressed()
            }
        }
    }

    private inner class ShineWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            view?.evaluateJavascript(
                "document.documentElement.classList.add('shine-android');",
                null,
            )
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            syncViewport()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.startsWith("file:///android_asset/")) return false
            if (url.startsWith("https://accounts.google.com") ||
                url.startsWith("https://oauth") ||
                url.startsWith("mailto:")
            ) {
                try {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, request.url))
                } catch (_: Exception) {
                }
                return true
            }
            return false
        }

        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            reloadWebApp()
            return true
        }
    }
}
