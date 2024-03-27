package com.prosuscorp.kleepaybitcoinwallet;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.SendRequest;



import java.util.concurrent.ExecutionException;


public class EnviarFragment extends Fragment {

    private EditText inputDireccion;
    private Button buttonEscanear;
    private EditText inputMonto;
    private Button buttonEnviar;
    private Button buttonVolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enviar, container, false);

        // Inicializa los campos de entrada y los botones
        inputDireccion = view.findViewById(R.id.bitcoin_address_enviar);
        buttonEscanear = view.findViewById(R.id.button_escanear);
        inputMonto = view.findViewById(R.id.input_monto);
        buttonEnviar = view.findViewById(R.id.button_enviar);
        buttonVolver = view.findViewById(R.id.button_volver);


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
            Log.d("ykb", "NO ES null");
        } else {
            bitcoinAddressEnviar = "";
            Log.d("ykb", "es null");
        }

        // Establecer la dirección de Bitcoin en el recuadro de dirección de Bitcoin
        EditText bitcoinAddressEditText = view.findViewById(R.id.bitcoin_address_enviar);
        bitcoinAddressEditText.setText(bitcoinAddressEnviar);
//      bitcoinAddressEditText.setText("bc1qanfaeqd26j59l9krltpjf3cd9tm3knxtrpfngk"); //test

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
                    inputMonto.setText("0,00005000");

                } else if (direccionEscrito.isEmpty()) {
                    Log.d("ykb", "DEBUG: direccion 02");
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Campo vacío")
                            .setMessage("Por favor, completa el campo 'dirección'.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    Log.d("ykb", "DEBUG: direccion 03");
                } else {
                    Log.d("ykb", "DEBUG: montoEnviar 04" );
                    // Procesa el monto a enviar
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
        Log.d("ykb", "DEBUG: enviar 01" );
        // Obtén la dirección de Bitcoin y el monto a enviar
        String bitcoinAddress = inputDireccion.getText().toString();
        Coin amount = Coin.parseCoin(inputMonto.getText().toString());
        Log.d("ykb", "DEBUG: enviar 02" );

        // Crea una conexión a un nodo externo y público
        NetworkParameters params = MainNetParams.get();
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
            Log.d("ykb", "DEBUG: enviar 05.1" );
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
            Log.d("ykb", "DEBUG: enviar 07b" );
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



}




