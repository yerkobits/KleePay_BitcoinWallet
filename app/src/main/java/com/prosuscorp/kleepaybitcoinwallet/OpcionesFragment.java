package com.prosuscorp.kleepaybitcoinwallet;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class OpcionesFragment extends Fragment {
    private Button btnClavePrivada;
    private Button buttonVolver ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_opciones, container, false);

        Log.e("ykb", "opciones 01.1");
        // Carga las preferencias compartidas
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        Log.d("ykb", "opciones 01.2");

        // Comprueba si ya se ha generado una dirección Bitcoin
        String bitcoinAddress = sharedPreferences.getString("bitcoinAddress", null);
        String clavePrivada = sharedPreferences.getString("privateKey", null);
        Log.d("ykb", "opciones 01.3");

        btnClavePrivada = view.findViewById(R.id.btn_clave_privada);
        btnClavePrivada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("ykb", "opciones 01.4");

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Private Key")
                            .setMessage("Tu clave privada es: \n\n" + clavePrivada +
                                    "\n\n\nY corresponde a la dirección: \n\n" + bitcoinAddress)
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            .setPositiveButton("Volver", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Compartir", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Tu clave privada es: \n\n" + clavePrivada +
                                            "\n\n\nY corresponde a la dirección: \n\n" + bitcoinAddress);
                                    startActivity(Intent.createChooser(shareIntent, "Compartir con"));
                                }
                            })
                            .show();
                } catch (Exception e) {
                    // Aquí puedes manejar la excepción
Log.d("ykb", "opciones 01.5", e);
                }
            }

        });


        // Agrega un listener al botón buttonVolver
        buttonVolver = view.findViewById(R.id.button_volver);
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.commit();
            }
        });


        return view;
    }
}
