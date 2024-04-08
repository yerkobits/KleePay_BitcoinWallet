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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
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
Log.d("ykb", "DEBUG: montoEnviar 00: " + inputMonto.getText().toString() );
                String montoEscrito = inputMonto.getText().toString();
Log.d("ykb", "DEBUG: montoEnviar 01" );
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
                } else if ( montoEscrito.isEmpty() ||  !montoEscrito.matches("\\d+(\\.\\d+)?") ) {
                    Log.d("ykb", "DEBUG: montoEnviar 02" );
                    new AlertDialog.Builder( getActivity() )
                            .setTitle("Campo vacío")
                            .setMessage("Por favor, escribe un monto válido..")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    Log.d("ykb", "DEBUG: montoEnviar 03" );
                } else {
                    Log.d("ykb", "DEBUG: montoEnviar 04" );
                    // Procesa el monto a enviar
//                  enviarTransaccionTestnet(useTestNet);
                    new Thread(new Runnable() {
                        public void run() {
                            enviarTransaccion(miClavePrivada);
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


    public void enviarTransaccion(String clavePrivada) {
        try {
            // Crea una red
            NetworkParameters params = MainNetParams.get();

            // Carga la clave privada
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, clavePrivada);
            ECKey key = dumpedPrivateKey.getKey();

            // Obtiene la dirección desde la clave privada
            String miAddress = SegwitAddress.fromKey(params, key).toBech32();
            Log.d("ykb", "dirección desde privateKey: " + miAddress);

// Llama a la función balance()
            String balanceStr = balance(miAddress);
            Log.d("ykb", "balance1: " + balanceStr );

            double wb = 0; // Inicializa wb aquí
            try {
                // Intenta convertir balanceStr a un número largo
                wb = Double.parseDouble(balanceStr) * 1e8;
                Log.d("ykb", "balance2: " + wb );
            } catch (NumberFormatException e) {
                // Imprime el error si balanceStr no puede ser convertido a un número largo
                Log.d("ykb", "Error al convertir balanceStr a un número largo: " + e.getMessage());
            }

// Calcula el monto a enviar (7% del balance)
            long oa = (long) (wb * 0.07);


            // Crea una transacción
            Transaction tx = new Transaction(params);

            // Aquí debes agregar los inputs de la transacción
            CompletableFuture<JSONObject> futureJson = jsonBlockchair(miAddress);
            JSONObject json = futureJson.get();  // Esto bloqueará hasta que el CompletableFuture se complete
            JSONArray utxos = json.getJSONObject("data").getJSONObject(miAddress).getJSONArray("utxo");
            Log.d("ykb", "utxos: " + utxos );

            try {
                JSONObject data = json.getJSONObject("data");
                JSONObject addressInfo = data.getJSONObject(miAddress);
                String scriptHex = addressInfo.getJSONObject("address").getString("script_hex");

                Coin amount = Coin.valueOf(oa);
                Log.d("ykb", "amount: " + amount);
                Address destination = Address.fromString(params, miAddress);
                Log.d("ykb", "destination: " + destination);
                tx.addOutput(amount, destination);
                Log.d("ykb", "Transaction after adding output: " + tx);

                utxos = json.getJSONObject("data").getJSONObject(miAddress).getJSONArray("utxo");
                Log.d("ykb", "utxos: " + utxos);

                for (int i = 0; i < utxos.length(); i++) {
                    JSONObject utxo = utxos.getJSONObject(i);
                    String txHash = utxo.getString("transaction_hash");
                    int outputIndex = utxo.getInt("index");
                    long value = utxo.getLong("value");

                    // Convierte el script_hex a un array de bytes
                    byte[] scriptBytes = org.apache.commons.codec.binary.Hex.decodeHex(scriptHex.toCharArray());

                    // Crea un nuevo Script a partir del array de bytes
                    Script scriptPubKey = new Script(scriptBytes);

                    Log.d("ykb", "params: " + params);
                    Log.d("ykb", "outputIndex: " + outputIndex);
                    Log.d("ykb", "txHash: " + txHash);
                    Log.d("ykb", "scriptPubKey.getProgram(): " + Arrays.toString(scriptPubKey.getProgram()));
                    Log.d("ykb", "Coin.valueOf(value): " + Coin.valueOf(value));

                    TransactionOutPoint outPoint = new TransactionOutPoint(params, outputIndex, Sha256Hash.wrap(txHash));
                    Log.d("ykb", "TransactionOutPoint: " + outPoint);
                    TransactionInput input = new TransactionInput(params, null, scriptPubKey.getProgram(), outPoint, Coin.valueOf(value));
                    Log.d("ykb", "TransactionInput: " + input);
                    tx.addInput(input);
                }

            } catch (Exception e) {
                // Imprime el error si ocurre una excepción
                Log.d("ykb", "Error: " + e.getMessage());
            }

// Firma la transacción
            for (int i = 0; i < tx.getInputs().size(); i++) {
                try {
                    TransactionInput input = tx.getInput(i);
                    Log.d("ykb", "Procesando input: " + i);
                    if (input != null) {
                        Log.d("ykb", "Revisando outPoint a: " + input.getOutpoint());
                        TransactionOutPoint outPoint = input.getOutpoint();
                        Log.d("ykb", "Revisando outPoint b: " + outPoint );
                        if (outPoint != null) {
                            Log.d("ykb", "Revisando outPoint c: " + outPoint.getConnectedOutput() );
                            TransactionOutput connectedOutput = outPoint.getConnectedOutput();
                            if (connectedOutput != null) {
                                Script scriptPubKey = connectedOutput.getScriptPubKey();
                                if (scriptPubKey != null) {
                                    Log.d("ykb", "Calculando firma para el input: " + i);
                                    tx.calculateSignature(i, key, scriptPubKey, Transaction.SigHash.ALL, false);
                                } else {
                                    Log.d("ykb", "El script de la clave pública del output conectado es nulo para el input: " + i);
                                }
                            } else {
                                Log.d("ykb", "El output conectado es nulo para el input: " + i);
                            }
                        } else {
                            Log.d("ykb", "El OutPoint es nulo para el input: " + i);
                        }
                    } else {
                        Log.d("ykb", "El input es nulo: " + i);
                    }
                } catch (Exception e) {
                    Log.e("ykb", "Error al calcular la firma para el input: " + i, e);
                }
            }








            // Aquí puedes enviar la transacción a la red
// Aquí puedes enviar la transacción a la red
            String apiUrl = "https://api.blockchair.com/bitcoin/push/transaction";

// Convierte la transacción a formato hexadecimal
            String txHex = Hex.toHexString(tx.bitcoinSerialize());
            Log.d("ykb", "txHex: " + txHex);


// Crea un cliente HTTP
            OkHttpClient client = new OkHttpClient();

// Crea una solicitud POST
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), txHex);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

// Realiza la solicitud y obtén la respuesta
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("ykb", "Error al enviar la transacción", e);
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("ykb", "Código inesperado: " + response);
                    } else {
                        // Aquí puedes manejar la respuesta
                        String responseStr = response.body().string();
                        Log.d("ykb", "Respuesta: " + responseStr);
                    }
                }
            });


        } catch (Exception e) {
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


}




