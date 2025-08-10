package tn.amin.keyboard_gpt.external;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import tn.amin.keyboard_gpt.ui.UiInteractor;

public class WebSearchDialogBox extends DialogBox {
    public WebSearchDialogBox(DialogBoxManager dialogManager, Activity parent,
                              Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected Dialog build() {
        String title = getInput().getString(UiInteractor.EXTRA_WEBVIEW_TITLE);
        if (title == null) {
            title = "Untitled";
        }

        String url = getInput().getString(UiInteractor.EXTRA_WEBVIEW_URL);
        if (url == null) {
            throw new NullPointerException(UiInteractor.EXTRA_WEBVIEW_URL + " cannot be null");
        }

        WebView webView = new WebView(getContext());
        webView.setWebViewClient(new WebViewClient()); // Ensures links open in the WebView
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript (optional)
        webView.loadUrl(url); // Replace with your URL
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(webView)
                .create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 100, 200, 100, 200);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(inset);
        }

        return dialog;
    }
}
