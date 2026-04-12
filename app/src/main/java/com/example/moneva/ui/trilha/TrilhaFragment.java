package com.example.moneva.ui.trilha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity; // Importar a MainActivity
import com.example.moneva.R;
import com.example.moneva.ui.menu.MenuFragment;
import com.example.moneva.ui.perfil.PerfilFragment;
import com.example.moneva.ui.transacoes.TransacoesFragment;

public class TrilhaFragment extends Fragment {

    private LinearLayout nodeBonusTrilha;
    private TextView txtMoedasTrilha;
    private TextView txtSequenciaTrilha;
    private View btnPerfilHome, navHome, navGestor, navTrilha, navMenu;

    public TrilhaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        // Inicializando os botões de navegação
        btnPerfilHome = view.findViewById(R.id.btnPerfilHome);
        navHome = view.findViewById(R.id.navHome);
        navGestor = view.findViewById(R.id.navGestor);
        navTrilha = view.findViewById(R.id.navTrilha);
        navMenu = view.findViewById(R.id.navMenu);
    }

    private void configurarEventos() {
        // Função auxiliar para encurtar a chamada
        btnPerfilHome.setOnClickListener(v -> navegarPara(new PerfilFragment()));

        navHome.setOnClickListener(v -> {
            // Se você quiser voltar para a home, pode carregar o fragmento
            navegarPara(new com.example.moneva.ui.home.HomeFragment());
        });

        navGestor.setOnClickListener(v -> navegarPara(new TransacoesFragment()));

        navTrilha.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Você já está na Trilha", Toast.LENGTH_SHORT).show()
        );

        navMenu.setOnClickListener(v -> navegarPara(new MenuFragment()));

        nodeBonusTrilha.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fase bônus!", Toast.LENGTH_SHORT).show()
        );
    }

    // METODO MÁGICO: Ele faz o cast para a MainActivity e chama o método de troca
    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
    }

    private void carregarDadosMock() {
        txtMoedasTrilha.setText("🪙 27");
        txtSequenciaTrilha.setText("🔥 3 dias");
    }
}