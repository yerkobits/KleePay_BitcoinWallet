package com.prosuscorp.kleepaybitcoinwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

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
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.SendRequest;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.concurrent.ExecutionException;

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


public class EnviarFragment extends Fragment {
    private static final NetworkParameters params = TestNet3Params.get();
    private WalletAppKit kit;
    public void enviarBitcoinTestnet(String address, Coin amount) {
        Log.d("ykb","enviarBitcointestnet entrando: " + address + " " + amount );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    // Obtener el directorio de almacenamiento interno privado de la aplicación
                    File directory = getActivity().getApplicationContext().getFilesDir();

                    // Inicializar el kit de la billetera
                    kit = new WalletAppKit(params, directory, "walletappkit-example");
                    // Agregar un Listener para manejar los errores
                    kit.addListener(new Service.Listener() {
                        @Override
                        public void failed(Service.State from, Throwable failure) {
                            Log.e("ykb", "Error al iniciar WalletAppKit: ", failure);
                        }
                    }, MoreExecutors.directExecutor());

                    Log.d("ykb", "enviarBitcoinTestnet kit: " + kit);


                    // Iniciar el kit de forma asíncrona
                    kit.startAsync();
                    kit.awaitRunning();
                    Log.d("ykb","enviarBitcoinTestnet iniciado: " );


                    // Verificar si WalletAppKit se ha conectado correctamente
                    if (kit.isRunning()) {
                        Log.d("ykb", "WalletAppKit se ha conectado correctamente.");
                    } else {
                        Log.d("ykb", "WalletAppKit no se ha conectado correctamente.");
                    }

                    // Conectar a los nodos de la red Testnet
                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("178.128.251.37", 18333)));
                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("64.190.113.17", 18333)));
//                    27.148.206.140
//                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("node.testnet.bitcoin.sprovoost.nl", 18333)));
//                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("bitcoin.testnet3.schildbach.de", 18333)));
//                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("testnet-seed.bitcoin.jonasschnelli.ch", 18333)));
//                    kit.peerGroup().addAddress(new PeerAddress(params, new InetSocketAddress("seed.tbtc.petertodd.org", 18333)));

                    Log.d("ykb", "enviarBitcoinTestnet conectar: ");

                    // Crear la dirección de envío
                    Address sendToAddress = Address.fromString(params, address);
                    Log.d("ykb", "WalletAppKit address: " + sendToAddress);

                    // Obtener la dirección
                    Address miAddress = kit.wallet().currentReceiveAddress();
                    Log.d("ykb", "WalletAppKit address: " + miAddress);
                    // Obtener la clave privada
//        ECKey key = kit.wallet().findKeyFromPubHash(address.getHash160());
//        System.out.println("Private Key: " + key.getPrivateKeyEncoded(params).toString());

                    // Crear una solicitud de envío
                    SendRequest request = SendRequest.to(sendToAddress, amount);

                    // Completar la transacción
                    kit.wallet().completeTx(request);

                    // Enviar la transacción
                    kit.peerGroup().broadcastTransaction(request.tx).broadcast();

                } catch (InsufficientMoneyException e) {
                    System.out.println("Saldo insuficiente para completar la transacción.");
                }

        }
        });
        try {
            future.get(30, TimeUnit.SECONDS);  // Esperar hasta X segundos
        } catch (TimeoutException e) {
            future.cancel(true);  // Cancelar la tarea si se agota el tiempo
            Log.d("ykb", "La conexión a la red Bitcoin Testnet ha tardado demasiado. Por favor, inténtalo de nuevo más tarde.");
        } catch (Exception e) {
            Log.e("ykb", "Se produjo un error: ", e);
        } finally {
            executor.shutdownNow();  // Cerrar el ExecutorService
        }



    }


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


                if (montoEscrito.isEmpty()) {
                    Log.d("ykb", "DEBUG: montoEnviar 02" );
                    new AlertDialog.Builder( getActivity() )
                            .setTitle("Campo vacío")
                            .setMessage("Por favor, completa el campo 'monto a enviar'.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    Log.d("ykb", "DEBUG: montoEnviar 03" );
//                    inputMonto.setText();
                    // Carga las preferencias compartidas
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    // Recupera la dirección Bitcoin
                    String miDireccionBitcoin = sharedPreferences.getString("bitcoinAddress", null);
                    autocompletarDireccionMonto(miDireccionBitcoin);

                } else if (direccionEscrito.isEmpty()) {
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
                    Log.d("ykb", "DEBUG: direccion 03");
                } else {
                    Log.d("ykb", "DEBUG: montoEnviar 04" );
                    // Procesa el monto a enviar
//                  enviarTransaccionTestnet(useTestNet);
                    enviarTransaccion();
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



    private void enviarTransaccion() {
        String montoConComa = inputMonto.getText().toString();
        String montoConPunto = montoConComa.replace(',', '.');

        // Obtén la dirección de Bitcoin y el monto a enviar
        String bitcoinAddress = inputDireccion.getText().toString();
        Coin amount = Coin.parseCoin(montoConPunto);
        Log.d("ykb", "DEBUG: enviar 02" );

        NetworkParameters params;
        params = MainNetParams.get();

        Wallet wallet = new Wallet(params);
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain;
        PeerGroup peerGroup;

        Log.d("ykb", "DEBUG: enviar 03" );
        try {
            chain = new BlockChain(params, blockStore);
            Log.d("ykb", "DEBUG: enviar 04a" );
        } catch (BlockStoreException e) {
            Log.d("ykb", "DEBUG: enviar 04b" );
            e.printStackTrace();
            return;
        }
        peerGroup = new PeerGroup(params, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.start();

        // Crea una transacción Bitcoin
        Transaction tx = new Transaction(params);
        SegwitAddress address = SegwitAddress.fromBech32(params, bitcoinAddress);
        tx.addOutput(amount, address);

        Log.d("ykb", "DEBUG: enviar 05" );
        // Crea un SendRequest y firma la transacción
//                SendRequest request = SendRequest.forTx(tx);
//                wallet.signTransaction(request);
        SendRequest request = SendRequest.forTx(tx);
        try {
            Log.d("ykb", "DEBUG: enviar 05.1 " + request );
            wallet.signTransaction(request);
            Log.d("ykb", "DEBUG: enviar 05.2" );
        } catch (KeyCrypterException e) {
            Log.d("ykb", "DEBUG: enviar 05.3" );
            e.printStackTrace();
            Log.d("ykb", "DEBUG: enviar 05.4" );
            // Aquí puedes manejar la excepción o mostrar un mensaje al usuario
        } catch (Exception e) {
            Log.d("ykb", "DEBUG: Excepción no controlada: "+e );
            e.printStackTrace();
            // Aquí puedes manejar la excepción o mostrar un mensaje al usuario
        }

        Log.d("ykb", "DEBUG: enviar 06" );
        // Firmar y transmitir la transacción
        try {
            Log.d("ykb", "DEBUG: enviar 07a" );
            Wallet.SendResult result = wallet.sendCoins(peerGroup, SendRequest.forTx(tx));
            result.broadcastComplete.get();
        } catch (InsufficientMoneyException e) {
            Log.d("ykb", "DEBUG: enviar 07b_ " + amount );
            // Manejo de la excepción InsufficientMoneyException
            e.printStackTrace();
            new AlertDialog.Builder(getActivity())
                    .setTitle("Error")
                    .setMessage("No tienes suficientes fondos para realizar esta transacción.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (InterruptedException | ExecutionException e) {
            Log.d("ykb", "DEBUG: enviar 07c" );
            e.printStackTrace();
        }

    }



    private void autocompletarDireccionMonto(String address) {

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
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    // Aquí puedes parsear la respuesta JSON para obtener el saldo
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(myResponse);
                                long satoshis = json.getLong("final_balance");
                                double bitcoins = satoshis / 1e8;
                                String montoFormateado = String.format("%.8f", bitcoins);
                                String montoConPunto = montoFormateado.replace(',', '.');
//                                textSaldo.setText(montoConPunto + " BTC");
                                inputMonto.setText(montoConPunto);
                                inputDireccion.setText(address);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }



}




