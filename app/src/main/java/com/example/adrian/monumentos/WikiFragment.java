package com.example.adrian.monumentos;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Esta clase muestra un fragmento con la informacion de la wikipedia de cada POI
 *
 * @author Adrian Munoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 */
public class WikiFragment extends Fragment {

    /**
     * Constructor por defecto
     */
    public WikiFragment() {
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.wiki_fragment, container, false);
        WebView webView = (WebView) vista.findViewById(R.id.webView);

        Bundle params = getArguments();
        String url = params.getString(POIListAdapter.POI_URL);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        WebSettings opciones = webView.getSettings();
        opciones.setJavaScriptEnabled(true);

        webView.loadUrl(url);
        return vista;
    }

}