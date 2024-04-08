package com.prosuscorp.kleepaybitcoinwallet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.content.SharedPreferences;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.android.volley.Request;
//import com.android.volley.Response;
import com.google.gson.Gson;

public class HomeFragment extends Fragment {

    private Button buttonRecibir;
    private Button buttonEnviar;
    private TextView textSaldo;
    private LinearLayout buttonLayout;
    private Button historialTransacciones;
    private ListView listaFrases;
    private boolean isListVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializa los botones y el texto del saldo
        buttonRecibir = view.findViewById(R.id.button_recibir);
        buttonEnviar = view.findViewById(R.id.button_enviar);
        textSaldo = view.findViewById(R.id.text_saldo);
        buttonLayout = view.findViewById(R.id.button_layout);
        historialTransacciones = view.findViewById(R.id.historialTransacciones);
        listaFrases = view.findViewById(R.id.listaFrases);

        historialTransacciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListVisible) {
                    hideList();
                    moveButtonsAndSaldoToCenter();
                    textSaldo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                } else {
                    animateButtonsAndSaldoToTop();
                    textSaldo.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_saldo_small_text_size));
                }
                isListVisible = !isListVisible;
            }
        });

        // Carga las preferencias compartidas
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        // Recupera la dirección Bitcoin
        String miDireccionBitcoin = sharedPreferences.getString("bitcoinAddress", null);


        // Agrega un listener al botón buttonRecibir
        buttonRecibir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecibirFragment recibirFragment = new RecibirFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, recibirFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Agrega un listener al botón buttonEnviar
        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarFragment fragmentEnviar = new EnviarFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragmentEnviar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        Button buttonOpciones = view.findViewById(R.id.button_opciones);
        buttonOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpcionesFragment opcionesFragment = new OpcionesFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, opcionesFragment);
                transaction.commit();
            }
        });


        muestraSaldo(miDireccionBitcoin);


        return view;
    }

    private void animateButtonsAndSaldoToTop() {
        float newY = -buttonLayout.getY() + buttonLayout.getHeight()/2 + textSaldo.getHeight();
        buttonLayout.animate()
                .translationY(newY)
                .setDuration(1000)
                .start();
        textSaldo.animate()
                .translationY(newY)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                          mostrarHistorial();

                    }
                })
                .start();
    }
    private void moveButtonsAndSaldoToCenter() {
        buttonLayout.animate()
                .translationY(0)
                .setDuration(500)
                .start();
        textSaldo.animate()
                .translationY(0)
                .setDuration(500)
                .start();
    }


    private void hideList() {
        listaFrases.setVisibility(View.GONE);
    }


    private void muestraSaldo(String address) {

        // Crea un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Actualiza la URL para usar la API de Blockchair
        String url = "https://api.blockchair.com/bitcoin/dashboards/address/" + address + "?key=A___GKSHlrRDzkAcIKbPuYoaAHLBcLO6";

        // Crea una solicitud GET
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Realiza la solicitud y obtén la respuesta
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    // Aquí puedes parsear la respuesta JSON para obtener el saldo
                    // y luego mostrarlo en textSaldo
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(myResponse);
                                long satoshis = json.getJSONObject("data").getJSONObject(address).getJSONObject("address").getLong("balance");
                                double bitcoins = satoshis / 1e8;
                                String montoFormateado = String.format("%.8f", bitcoins);
                                String montoConPunto = montoFormateado.replace(',', '.');
                                textSaldo.setText(montoConPunto + " BTC");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }



    private void mostrarHistorial() {
        // Carga las preferencias compartidas
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        // Recupera la dirección Bitcoin
        String miDireccionBitcoin = sharedPreferences.getString("bitcoinAddress", null);

        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.blockchair.com/bitcoin/dashboards/address/" + miDireccionBitcoin + "?key=A___GKSHlrRDzkAcIKbPuYoaAHLBcLO6" )
                .build();

        // Muestra un ProgressDialog durante la carga
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando transacciones...");
        progressDialog.show();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray transactions = jsonObject.getJSONObject("data").getJSONObject(miDireccionBitcoin).getJSONArray("transactions");

                    ArrayList<String> links_hash = new ArrayList<>();
                    String tx_hash;
                    for (int i = 0; i < transactions.length(); i++) {
                        tx_hash = transactions.getString(i);
                        links_hash.add("https://blockstream.info/tx/" + tx_hash);
                    }

                    jsonBlockcypher(miDireccionBitcoin).thenAccept(json -> {
                        try {
                            JSONArray txrefs = json.getJSONArray("txrefs");
                            for (int n = 0; n < txrefs.length(); n++) {
                                JSONObject txref = txrefs.getJSONObject(n);
                                String fecha = txref.getString("confirmed");
                                String hash = txref.getString("tx_hash");
                                String monto = txref.getString("value");

                                // Formatea la fecha
                                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                SimpleDateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("es", "ES"));
                                try {
                                    Date date = originalFormat.parse(fecha);
                                    String formattedDate = targetFormat.format(date);
                                    Log.d("ykb", "Blockcypher: " + formattedDate +" "+ monto +" "+ hash);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });


                    // Actualizar la ListView en el hilo principal de la UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, R.id.tx_hash, links_hash) {
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView txAmountView = view.findViewById(R.id.tx_amount);
                                    TextView txFechaView = view.findViewById(R.id.tx_fecha);
                                    TextView txHashView = view.findViewById(R.id.tx_hash);

                                    // Aquí debes obtener el monto de la transacción y determinar si es de entrada o salida
                                    // Esto dependerá de cómo estén estructurados los datos de tu API
                                    double tx_amount = 0.00010000;  // Reemplaza esto con el monto de la transacción
                                    boolean is_incoming = true;  // Reemplaza esto con un booleano que indique si la transacción es de entrada
                                        if (is_incoming) {
                                            txAmountView.setTextColor(Color.parseColor("#006400"));
                                            txAmountView.setText("+" + String.format("%.8f", tx_amount));
                                        } else {
                                            txAmountView.setTextColor(Color.parseColor("#8B0000"));
                                            txAmountView.setText("-" + String.format("%.8f", tx_amount));
                                        }

                                    // fecha
                               /*     if (!links_fecha.isEmpty()) {
                                        String tx_fecha = links_fecha.get(0);
                                        txFechaView.setText("fecha: " + tx_fecha);
                                    } else {
                                        Log.d("ykb", "La lista está vacía");
                                    }*/


                                    // hash
                                    String textoHash = links_hash.get(position).substring(29);  // Solo muestra el hash
                                    String textoHashCompacto = textoHash.substring(0, 8) + " ... " + textoHash.substring(textoHash.length() - 8);
                                    txHashView.setText("hash: " + textoHashCompacto);

                                    return view;
                                }

                            };
                            listaFrases.setAdapter(adapter);
                            listaFrases.setAlpha(0f);
                            listaFrases.setVisibility(View.VISIBLE);

                            float newY = -buttonLayout.getY()*3 + textSaldo.getHeight();
                            listaFrases.animate()
                                    .alpha(1f)
                                    .translationY(newY)
                                    .setDuration(500)
                                    .start();

                            listaFrases.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String url = links_hash.get(position);
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            });

                            // Cierra el ProgressDialog después de cargar los datos
                            progressDialog.dismiss();
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
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


    public CompletableFuture<JSONObject> jsonBlockchairTransaction(String hash) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();

        // Crea un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Cambia la URL para usar la API de Blockchair
        String url = "https://api.blockchair.com/bitcoin/dashboards/transaction/" + hash  + "?key=A___GKSHlrRDzkAcIKbPuYoaAHLBcLO6" ;

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


    public CompletableFuture<JSONObject> jsonBlockcypher(String address) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();

        // Crea un cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Cambia la URL para usar la API de Blockcypher
        String url = "https://api.blockcypher.com/v1/btc/main/addrs/" + address;

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







}

