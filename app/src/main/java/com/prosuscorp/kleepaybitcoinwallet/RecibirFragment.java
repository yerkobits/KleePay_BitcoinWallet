package com.prosuscorp.kleepaybitcoinwallet;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.params.MainNetParams;
import android.content.SharedPreferences;
import android.content.Context;

/*
public class RecibirFragment extends Fragment {

    private ImageView qrCodeImageView;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_recibir, container, false);

    qrCodeImageView = view.findViewById(R.id.qr_code_image_view);

    // Genera una dirección de Bitcoin
    String bitcoinAddress = generateBitcoinAddress();

    // Genera un código QR a partir de la dirección de Bitcoin
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    try {
        BitMatrix bitMatrix = qrCodeWriter.encode(bitcoinAddress, BarcodeFormat.QR_CODE, 200, 200);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        qrCodeImageView.setImageBitmap(bmp);
    } catch (WriterException e) {
        e.printStackTrace();
    }

    return view;
}

    private String generateBitcoinAddress() {
        // Aquí debes implementar la lógica para generar una dirección de Bitcoin
        // Este es solo un placeholder
        return "bitcoinAddress";
    }
}
*/

public class RecibirFragment extends Fragment {

    private ImageView qrCodeImageView;
    private TextView bitcoinAddressTextView;
    private Button buttonGenerar;
    private Button buttonVolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recibir, container, false);

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

        // Inicializa los elementos de la vista
        qrCodeImageView = view.findViewById(R.id.qr_code_image_view);
        bitcoinAddressTextView = view.findViewById(R.id.bitcoin_address_text_view);
        buttonGenerar = view.findViewById(R.id.button_generar);

        // Carga las preferencias compartidas
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        // Comprueba si ya se ha generado una dirección Bitcoin
        String bitcoinAddress = sharedPreferences.getString("bitcoinAddress", null);
        String privateKey = sharedPreferences.getString("privateKey", null);

        if (bitcoinAddress != null && privateKey != null) {
            // Si ya se ha generado una dirección Bitcoin, muestra la dirección y la clave privada en el TextView
            bitcoinAddressTextView.setText("Dirección Bitcoin: \n" + bitcoinAddress + "\n\nClave privada: \n" + privateKey);

            // Genera el código QR de la dirección Bitcoin
            try {
                BitMatrix bitMatrix = new MultiFormatWriter().encode(bitcoinAddress, BarcodeFormat.QR_CODE, 200, 200);
                Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
                for (int x = 0; x < 200; x++) {
                    for (int y = 0; y < 200; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                // Muestra el código QR en el ImageView
                qrCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            // Si no se ha generado una dirección Bitcoin, muestra el botón "Generar"
            buttonGenerar.setVisibility(View.VISIBLE);
        }

        // Agrega un listener al botón buttonGenerar
        buttonGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Genera una nueva clave ECKey
                ECKey key = new ECKey();

                // Obtiene la dirección Bitcoin y la clave privada
                NetworkParameters params = MainNetParams.get();
                String bitcoinAddress = SegwitAddress.fromKey(params, key).toBech32();
                String privateKey = key.getPrivateKeyAsWiF(params);

                // Muestra la dirección Bitcoin en el TextView
                bitcoinAddressTextView.setText("Dirección Bitcoin: \n" + bitcoinAddress + "\n\nClave privada: \n" + privateKey);

                // Genera el código QR de la dirección Bitcoin
                try {
                    BitMatrix bitMatrix = new MultiFormatWriter().encode(bitcoinAddress, BarcodeFormat.QR_CODE, 200, 200);
                    Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
                    for (int x = 0; x < 200; x++) {
                        for (int y = 0; y < 200; y++) {
                            bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    // Muestra el código QR en el ImageView
                    qrCodeImageView.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                // Guarda la dirección Bitcoin y la clave privada en las preferencias compartidas
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("bitcoinAddress", bitcoinAddress);
                editor.putString("privateKey", privateKey);
                editor.apply();

                // Oculta el botón "Generar"
                buttonGenerar.setVisibility(View.GONE);

            }
        });

        return view;
    }
}
