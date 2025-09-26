package com.sonitasv.sonitasvapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.ProgressBar;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.provider.MediaStore;
import android.os.Build;
import androidx.annotation.Nullable;
import android.webkit.DownloadListener;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Environment;
import android.webkit.URLUtil;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    WebView myWeb;
    ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        progressBar = findViewById(R.id.progressBar);
        myWeb.getSettings().setJavaScriptEnabled(true);
        
        // Optimización inteligente: Limpiar caché cuando sea necesario
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        long lastCacheClear = prefs.getLong("last_cache_clear", 0);
        long currentTime = System.currentTimeMillis();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L; // 7 días en milisegundos
        
        // Limpiar caché si es la primera vez o han pasado más de 7 días
        if (lastCacheClear == 0 || (currentTime - lastCacheClear) > sevenDaysInMillis) {
            myWeb.clearCache(true);
            myWeb.clearHistory();
            myWeb.clearFormData();
            // Guardar la fecha de la última limpieza
            prefs.edit().putLong("last_cache_clear", currentTime).apply();
        }
        
        // Usar caché normal para mejor rendimiento
        myWeb.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);

        // Set a custom WebViewClient to handle external links
        myWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                // Check if the URL should be opened externally
                if (isExternalLink(uri.toString())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true; // Indicates we've handled the URL
                }
                return false; // Load the URL within the WebView
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // For older versions
                if (isExternalLink(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        myWeb.setWebChromeClient(new WebChromeClient() {
            // For Android 5.0+
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        myWeb.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });

        // Load the initial URL
        myWeb.loadUrl("https://sonitasv.com/");
    }

    // Helper method to determine if a URL should open externally
    private boolean isExternalLink(String url) {
        // Add your custom logic for external links here, e.g., PDF, Canva links
        return url.endsWith(".pdf") || url.contains("canva.com") || !url.contains("sonitasv.com");
    }

    @Override
    public void onBackPressed () {
        if (myWeb.canGoBack()) {
          myWeb.goBack();
        }else{
           super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    } else if (data.getClipData() != null) {
                        final int numSelectedFiles = data.getClipData().getItemCount();
                        results = new Uri[numSelectedFiles];
                        for (int i = 0; i < numSelectedFiles; i++) {
                            results[i] = data.getClipData().getItemAt(i).getUri();
                        }
                    }
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
    }
}
