package com.sonitasv.sonitasvapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    WebView myWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        myWeb.getSettings().setJavaScriptEnabled(true);

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
}
