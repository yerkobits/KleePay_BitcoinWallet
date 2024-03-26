package com.prosuscorp.kleepaybitcoinwallet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.prosuscorp.kleepaybitcoinwallet.R;

public class EscanearQRFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_escanear_qr, container, false);

        // Inicia el escaneo de QR cuando se muestra el fragmento
        IntentIntegrator.forSupportFragment(this).initiateScan();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // El escaneo fue cancelado, vuelve a HomeFragment
                EnviarFragment enviarFragment = new EnviarFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, enviarFragment);
                transaction.commit();
/*                new AlertDialog.Builder(getActivity())
                        .setTitle("Resultado del Escaneo")
                        .setMessage("Escaneo cancelado")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();*/
            } else {
                // El escaneo fue exitoso, el contenido del QR está en result.getContents()
                // Aquí puedes manejar la dirección de Bitcoin escaneada
                String bitcoinAddress = result.getContents();
                ((MainActivity)getActivity()).changeToEnviarFragment(bitcoinAddress);
/*                new AlertDialog.Builder(getActivity())
                        .setTitle("Resultado del Escaneo")
                        .setMessage("Contenido del QR: " + result.getContents())
                        .setPositiveButton(android.R.string.ok, null)
                        .show(); */
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}

