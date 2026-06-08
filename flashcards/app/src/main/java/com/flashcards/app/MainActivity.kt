package com.flashcards.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

/**
 * Ships the exact Shine website (bundled assets) for 1:1 feature parity.
 * Insets are injected as CSS variables (Capacitor / Ionic pattern) so layout fits every screen.
 */
class MainActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var container: FrameLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        container = root
        root.addView(buildWebView())
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            applyInsets(insets)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
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
                databaseEnabled = true
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
            loadUrl("file:///android_asset/www/index.html")
        }
        return view
    }

    private fun applyInsets(insets: WindowInsetsCompat) {
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val top = bars.top
        val left = bars.left
        val right = bars.right
        val bottom = max(bars.bottom, ime.bottom)
        val js = """
            (function(){
              var r=document.documentElement;
              r.style.setProperty('--sat','${top}px');
              r.style.setProperty('--sal','${left}px');
              r.style.setProperty('--sar','${right}px');
              r.style.setProperty('--sab','${bottom}px');
              r.classList.add('shine-android');
            })();
        """.trimIndent()
        webView?.evaluateJavascript(js, null)
    }

    fun reloadWebApp() {
        val root = container ?: return
        webView?.destroy()
        webView = null
        root.removeAllViews()
        root.addView(buildWebView())
        ViewCompat.requestApplyInsets(root)
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
            ViewCompat.requestApplyInsets(container ?: return)
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
