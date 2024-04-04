package com.prosuscorp.kleepaybitcoinwallet;

import android.content.DialogInterface;
import android.os.Bundle;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.content.SharedPreferences;
import android.content.Context;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String tuDireccionBitcoin = sharedPreferences.getString("bitcoinAddress", null);


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


        muestraSaldo(tuDireccionBitcoin);


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
                        showList();
                        mostrarTxHashTestnet();
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

/*    public class TransactionResponse {
        private List<Transaction> txs;

        public List<String> getTransactions() {
            List<String> transactions = new ArrayList<>();
            for (Transaction tx : txs) {
                transactions.add(tx.tx_hash);
            }
            return transactions;
        }

        public class Transaction {
            String tx_hash; // Este campo debe coincidir con el nombre del campo en la respuesta JSON de la API

            @Override
            public String toString() {
                return tx_hash;
            }
        }
    }*/


/*    private void showList() {
        String url = "https://api.blockcypher.com/v1/btc/main/addrs/bc1qanfaeqd26j59l9krltpjf3cd9tm3knxtrpfngk";

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        TransactionResponse transactionResponse = gson.fromJson(response, TransactionResponse.class);
Log.d("ykb", "transactionResponse: " + transactionResponse );

                        // Aquí asumimos que TransactionResponse tiene un método getTransactions() que devuelve una lista de transacciones
                        List<String> transactions = transactionResponse.getTransactions();
                        for (String tx : transactions) {
                            Log.d("ykb", "tx_hash: " + tx);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, transactions);
                        listaFrases.setAdapter(adapter);
                        listaFrases.setAlpha(0f);
                        listaFrases.setVisibility(View.VISIBLE);

                        float newY = -buttonLayout.getY()*3 + textSaldo.getHeight();
                        listaFrases.animate()
                                .alpha(1f)
                                .translationY(newY)
                                .setDuration(500)
                                .start();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Manejo de errores
            }
        });

        Volley.newRequestQueue(getContext()).add(stringRequest);
    }*/
    private void showList() {
        String[] frases = new String[]{
                "transacción de ejemplo 01",
                "transacción de ejemplo 02",
                "transacción de ejemplo 03",
                "transacción de ejemplo 04",
                "transacción de ejemplo 05",
                "transacción de ejemplo 06",
                "transacción de ejemplo 07",
                "transacción de ejemplo 08",
                "transacción de ejemplo 09",
                "transacción de ejemplo 10",
                "transacción de ejemplo 11",
                "transacción de ejemplo 12",
                "transacción de ejemplo 13",
                "transacción de ejemplo 14",
                "transacción de ejemplo 15",
                "transacción de ejemplo 16",
                "transacción de ejemplo 17",
                "transacción de ejemplo 18",
                "transacción de ejemplo 19",
                "transacción de ejemplo 20"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, frases);
        Log.d("ykb", "adapter frases: " + adapter);
        listaFrases.setAdapter(adapter);
        listaFrases.setAlpha(0f);
        listaFrases.setVisibility(View.VISIBLE);

        float newY = -buttonLayout.getY()*3 + textSaldo.getHeight();
        listaFrases.animate()
                .alpha(1f)
                .translationY(newY)
                .setDuration(500)
                .start();
    }
    private void hideList() {
        listaFrases.setVisibility(View.GONE);
    }


    private void muestraSaldo(String address) {

        // Crea un cliente HTTP
        OkHttpClient client = new OkHttpClient();

//        String url = "https://blockchain.info/rawaddr/" + address;
        String url = "https://api.blockcypher.com/v1/btc/main/addrs/" + address;

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
                                long satoshis = json.getLong("final_balance");
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



    private void mostrarTxHashTestnet() {
        String[] frases = new String[]{
                "transacción de ejemplo 01",
                "transacción de ejemplo 02",
                "transacción de ejemplo 03",
                "transacción de ejemplo 04",
                "transacción de ejemplo 05",
        };

        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
              .url("https://api.blockcypher.com/v1/btc/test3/addrs/tb1q5ktghnkphhhvc6eru9wz0a4uarec56l7yyaycc")
//                .url("https://blockstream.info/testnet/address/tb1q5ktghnkphhhvc6eru9wz0a4uarec56l7yyaycc")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray txrefs = jsonObject.getJSONArray("txrefs");

                    ArrayList<String> links = new ArrayList<>();
                    for (int i = 0; i < txrefs.length(); i++) {
                        String tx_hash = txrefs.getJSONObject(i).getString("tx_hash");
                        links.add("https://mempool.space/es/testnet/tx/" + tx_hash);
                        Log.d("ykb", "https://mempool.space/es/testnet/tx/" + tx_hash);
                    }
/*                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < txrefs.length(); i++) {
                        String tx_hash = txrefs.getJSONObject(i).getString("tx_hash");
                        sb.append("https://mempool.space/es/testnet/tx/" + tx_hash + "\n");
                        Log.d("ykb", "https://mempool.space/es/testnet/tx/" + tx_hash);
                    }*/
                    Log.d("ykb", "links: " + links);

                    StringBuilder sb = new StringBuilder();
                    for (String link : links) {
                        Log.d("ykb", "link: " + link);
                        sb.append("\"" + link + "\"," + "\n");
                    }
                    Log.d("ykb", "string builder: " + sb);
                    Log.d("ykb", "frases: " + frases);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, links);
                    Log.d("ykb", "adapter: " + adapter);

                    listaFrases.setAdapter(adapter);
                    listaFrases.setAlpha(0f);
                    listaFrases.setVisibility(View.VISIBLE);

                    float newY = -buttonLayout.getY()*3 + textSaldo.getHeight();
                    listaFrases.animate()
                            .alpha(1f)
                            .translationY(newY)
                            .setDuration(500)
                            .start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



}

