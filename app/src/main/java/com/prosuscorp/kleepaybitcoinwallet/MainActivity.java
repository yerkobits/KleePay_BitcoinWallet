/*
package com.prosuscorp.kleepaybitcoinwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}*/

package com.prosuscorp.kleepaybitcoinwallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crea una nueva instancia de tu HomeFragment
        HomeFragment homeFragment = new HomeFragment();

        // Comienza una transacción de fragmentos
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Reemplaza cualquier fragmento que esté en el contenedor de fragmentos (si existe) con HomeFragment
        transaction.replace(R.id.fragment_container, homeFragment);

        // Completa la transacción
        transaction.commit();
    }

    public void changeToEnviarFragment(String bitcoinAddress) {
        EnviarFragment enviarFragment = new EnviarFragment();

        // Pasar la dirección de Bitcoin al fragmento
        Bundle bundle = new Bundle();
        bundle.putString("bitcoinAddressEnviar", bitcoinAddress);
        enviarFragment.setArguments(bundle);

        // Cambiar al fragmento EnviarFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, enviarFragment)
                .commit();
    }

}

