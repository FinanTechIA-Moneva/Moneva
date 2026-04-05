package com.example.moneva.ui.trilha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneva.R;

public class TrilhaFragment extends Fragment {

    private LinearLayout nodeBonusTrilha;
    private TextView txtMoedasTrilha;
    private TextView txtSequenciaTrilha;

    public TrilhaFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trilha, container, false);

        inicializarViews(view);
        configurarEventos();
        carregarDadosMock();

        return view;
    }

    private void inicializarViews(View view) {
        nodeBonusTrilha = view.findViewById(R.id.nodeBonusTrilha);
        txtMoedasTrilha = view.findViewById(R.id.txtMoedasTrilha);
        txtSequenciaTrilha = view.findViewById(R.id.txtSequenciaTrilha);
    }

    private void configurarEventos() {
        nodeBonusTrilha.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fase bônus selecionada!", Toast.LENGTH_SHORT).show()
        );
    }

    private void carregarDadosMock() {
        txtMoedasTrilha.setText("🪙 27");
        txtSequenciaTrilha.setText("🔥 3 dias");
    }
}