package com.prosuscorp.kleepaybitcoinwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
//import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.SendRequest;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.SendRequest;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.RequestBody;
//import org.apache.commons.codec.binary.Hex;


public class EnviarFragment extends Fragment {

    private EditText inputDireccion;
    private Button buttonEscanear;
    private EditText inputMonto;
    private Button buttonEnviar;
    private Button buttonVolver;
    private String bitcoinAddressTestnet;
    private String privateKeyTestnet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enviar, container, false);

        // Inicializa los campos de entrada y los botones
        inputDireccion = view.findViewById(R.id.bitcoin_address_enviar);
        buttonEscanear = view.findViewById(R.id.button_escanear);
        inputMonto = view.findViewById(R.id.input_monto);
        buttonEnviar = view.findViewById(R.id.button_enviar);
        buttonVolver = view.findViewById(R.id.button_volver);

        boolean useTestNet = true;
        if (useTestNet) {
//            mostrarTestnet();
        }

        // Agrega un listener al botón buttonEscanear
        buttonEscanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EscanearQRFragment escanearQRFragment = new EscanearQRFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, escanearQRFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Agrega un listener al botón buttonVolver
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.commit();
            }
        });

        // Obtener la dirección de Bitcoin de los argumentos del fragmento
        String bitcoinAddressEnviar = "";
        if (getArguments() != null) {
            bitcoinAddressEnviar = getArguments().getString("bitcoinAddressEnviar");
            Log.d("ykb", "bitcoinAddressEnviar NO ES null");
        } else {
            bitcoinAddressEnviar = "";
            Log.d("ykb", "bitcoinAddressEnviar es null");
        }

        // Establecer la dirección de Bitcoin en el recuadro de dirección de Bitcoin
        EditText bitcoinAddressEditText = view.findViewById(R.id.bitcoin_address_enviar);
        bitcoinAddressEditText.setText(bitcoinAddressEnviar);

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ykb", "DEBUG: montoEnviar 00: " + inputMonto.getText().toString());
                String montoEscrito = inputMonto.getText().toString();
                Log.d("ykb", "DEBUG: montoEnviar 01");
                String direccionEscrito = inputDireccion.getText().toString();

                // Carga las preferencias compartidas
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                // Recupera la dirección Bitcoin
                String miDireccionBitcoin = sharedPreferences.getString("bitcoinAddress", null);
                String miClavePrivada = sharedPreferences.getString("privateKey", null);

                if (direccionEscrito.isEmpty()) {
                    Log.d("ykb", "DEBUG: direccion 02");
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Campo vacío")
                            .setMessage("Por favor, completa el campo 'dirección'.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
//                                    bitcoinAddressEditText.setText(miDireccionBitcoin); //test
                                }
                            })
                            .show();
                    autocompletarDireccionMonto(miDireccionBitcoin);
                    Log.d("ykb", "DEBUG: direccion 03");
                } else if (montoEscrito.isEmpty() || !montoEscrito.matches("\\d+(\\.\\d+)?")) {
                    Log.d("ykb", "DEBUG: montoEnviar 02");
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Campo vacío")
                            .setMessage("Por favor, escribe un monto válido..")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    Log.d("ykb", "DEBUG: montoEnviar 03");
                } else {
                    Log.d("ykb", "DEBUG: montoEnviar 04");
                    // Procesa el monto a enviar
                    new Thread(new Runnable() {
                        public void run() {
//                            enviarTransaccion(miClavePrivada);
                            enviarTransaccionJavascript(miClavePrivada);
                        }
                    }).start();

                }


            }
        });


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // Anular el retroceso con el botón del dispositivo
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // Aquí puedes hacer lo que quieras cuando se presiona el botón de retroceso
                    return true; // Consumir el evento
                }
                return false;
            }
        });
    }


    public static void enviarTransaccion(String clavePrivada) {
        try {
            NetworkParameters params = MainNetParams.get();
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, clavePrivada);
            ECKey key = dumpedPrivateKey.getKey();

            // Crear una cartera con la clave privada
            List<ECKey> keys = new ArrayList<>();
            keys.add(key);
            Wallet wallet = Wallet.fromKeys(params, keys);
            Log.d("ykb", "crea cartera desde privateKey: " + wallet);

            // Obtiene la dirección desde la clave privada
            SegwitAddress miAddress = SegwitAddress.fromKey(params, key);
            Log.d("ykb", "dirección desde privateKey: " + miAddress);

            // Obtener el balance
            EnviarFragment bt = new EnviarFragment();
            String balanceStr = bt.balance(miAddress.toString());
            Log.d("ykb", "balance remitente: " + balanceStr);
            BigDecimal balance = new BigDecimal(balanceStr);
            BigDecimal amountToSend = balance.multiply(new BigDecimal("0.07"));  // 7% del balance

            // Convertir BigDecimal a Coin
            Coin amountToSendCoin = Coin.valueOf(amountToSend.longValue());

            // Crear la transacción
            Transaction transaction = new Transaction(params);
            transaction.addOutput(amountToSendCoin, miAddress);

            // Aquí deberías obtener las UTXOs y añadirlas como entradas a la transacción
            JSONObject utxoData = bt.jsonBlockchair(miAddress.toString()).get();
            JSONArray utxos;
            try {
                utxos = utxoData.getJSONObject("data").getJSONObject(miAddress.toString()).getJSONArray("utxo");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < utxos.length(); i++) {
                JSONObject utxo;
                try {
                    utxo = utxos.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
                TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getLong("index"), Sha256Hash.wrap(utxo.getString("transaction_hash")));
                TransactionInput input = new TransactionInput(params, null, new byte[]{}, outPoint);
                transaction.addInput(input);
                Log.d("ykb", "outPoint: " + outPoint);
                Log.d("ykb", "input: " + outPoint);
                Log.d("ykb", "transaction: " + transaction);
            }

            // Firmar la transacción
            SendRequest sendRequest = SendRequest.forTx(transaction);
//            wallet.completeTx(sendRequest);  // Esto firma la transacción

            // Aquí deberías enviar la transacción utilizando el servicio de terceros
            // ...
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }



    public CompletableFuture<JSONObject> jsonBlockchair(String address) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();

        // Crea un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Cambia la URL para usar la API de Blockchair
        String url = "https://api.blockchair.com/bitcoin/dashboards/address/" + address  + "?key=A___GKSHlrRDzkAcIKbPuYoaAHLBcLO6" ;

        // Crea una solicitud GET
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Realiza la solicitud y obtén la respuesta
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    // Aquí puedes parsear la respuesta JSON
                    try {
                        JSONObject json = new JSONObject(myResponse);
                        future.complete(json);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                }
            }
        });

        return future;
    }

    public String balance(String address) {
        CompletableFuture<String> futureBalance = jsonBlockchair(address).thenApply(json -> {
            try {
                JSONObject data = json.getJSONObject("data");
                JSONObject addressInfo = data.getJSONObject(address);
                JSONObject balanceInfo = addressInfo.getJSONObject("address");
                long satoshis = balanceInfo.getLong("balance");
                double bitcoins = satoshis / 1e8;
                String montoFormateado = String.format("%.8f", bitcoins);
                String montoConPunto = montoFormateado.replace(',', '.');
                return montoConPunto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Bloquea el hilo actual hasta que se complete el CompletableFuture
        return futureBalance.join();
    }

    private void autocompletarDireccionMonto(String address) {
        try {
            // Llama a la función balance() y asigna su resultado a una variable String
            String balanceStr = balance(address);
            inputMonto.setText(balanceStr);
            inputDireccion.setText(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private WebView webView;
    private String resultadoTransaccion;

    public void enviarTransaccionJavascript(String clavePrivada) {
        // Aquí puedes usar 'clavePrivada' para hacer algo
        // Guarda el resultado en 'resultadoTransaccion'
        resultadoTransaccion = clavePrivada;
        Log.d("ykb","Resultado transacción: " + resultadoTransaccion);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(getActivity());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "Android");
        webView.loadUrl("file:///android_asset/funcion.js");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("ykb", consoleMessage.message() + " -- Desde línea "
                        + consoleMessage.lineNumber() + " de "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });


        // Usa 'resultadoTransaccion' para pasar los datos a tu función JavaScript
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.loadUrl("javascript:miFuncionJS('" + resultadoTransaccion + "')");
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.evaluateJavascript("javascript:(function() { " +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        "script.innerHTML = window.atob('" + encodeAssetToBase64("funcion.js") + "');" +
                        "parent.appendChild(script)" +
                        "})()", null);
            }
        });
    }

    private String encodeAssetToBase64(String assetName) {
        String base64 = "";
        try {
            InputStream input = getContext().getAssets().open(assetName);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            base64 = Base64.encodeToString(buffer, Base64.NO_WRAP);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void mostrarResultado(final String resultado) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // Aquí puedes manejar el resultado. Por ejemplo, imprimirlo en Logcat:
                    Log.d("ykb","Resultado JS: " + resultado);
                }
            });
        }
    }

}




