package com.example.moneva.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.moneva.R;
import com.example.moneva.ui.auth.LoginActivity;
import com.example.moneva.ui.home.HomeFragment;
import com.example.moneva.ui.perfil.PerfilFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MenuFragment extends Fragment {

    private Button btnIrPerfil;
    private Button btnVoltarHome;
    private Button btnSairConta;

    public MenuFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnIrPerfil = view.findViewById(R.id.btnIrPerfil);
        btnVoltarHome = view.findViewById(R.id.btnVoltarHome);
        btnSairConta = view.findViewById(R.id.btnSairContaMenu);

        btnIrPerfil.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PerfilFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnVoltarHome.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit()
        );

        btnSairConta.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}