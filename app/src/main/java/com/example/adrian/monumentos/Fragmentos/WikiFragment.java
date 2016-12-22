package com.example.adrian.monumentos.Fragmentos;

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

import com.example.adrian.monumentos.POIListAdapter;

/**
 * Esta clase se encarga de, dada una URL obtenida a través de un objeto Bundle, crear un cliente web y mostrar esa URL.
 *
 * @author Adrián Muñoz Rojo
 * @author Rafael Matamoros Luque
 * @author David Carrancio Aguado
 * @see POIListAdapter
 * @version 1.0
 */
public class WikiFragment extends Fragment {

    /**
     * El constructor por defecto es reemplazado.
     */
    public WikiFragment() {
    }

    /**
     * Método llamado para instanciar el fragmento con su vista asociada (R.wiki_fragment, en este caso).
     *
     * <p>Además, a partir de la URL obtenida en el Bundle, crea un nuevo objeto WebChromeclient para mostrar esa URL.</p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState  If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * Return the View for the fragment's UI, or null.
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(com.example.adrian.monumentos.R.layout.wiki_fragment, container, false);
        WebView webView = (WebView) vista.findViewById(com.example.adrian.monumentos.R.id.webView);

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