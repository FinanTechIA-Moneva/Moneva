package com.example.moneva.ui.trilha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity;
import com.example.moneva.R;
import com.example.moneva.ui.home.HomeFragment;
import com.example.moneva.ui.menu.MenuFragment;
import com.example.moneva.ui.perfil.PerfilFragment;
import com.example.moneva.ui.transacoes.TransacoesFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrilhaFragment extends Fragment {

    private LinearLayout nodeBonusTrilha;
    private TextView txtMoedasTrilha;
    private TextView txtSequenciaTrilha;
    private LinearLayout navHome, navGestor, navTrilha, navMenu, layoutBottomNavTrilha;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public TrilhaFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trilha, container, false);

        inicializarFirebase();
        inicializarViews(view);
        configurarEventos();
        carregarDadosUsuario();

        return view;
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void inicializarViews(View view) {
        nodeBonusTrilha = view.findViewById(R.id.nodeBonusTrilha);
        txtMoedasTrilha = view.findViewById(R.id.txtMoedasTrilha);
        txtSequenciaTrilha = view.findViewById(R.id.txtSequenciaTrilha);

        // Identifica o container da barra inferior
        layoutBottomNavTrilha = view.findViewById(R.id.layoutBottomNavTrilha);

        // Inicializa os botões (apenas uma vez é necessário)
        navHome = view.findViewById(R.id.navHome);
        navGestor = view.findViewById(R.id.navGestor);
        navTrilha = view.findViewById(R.id.navTrilha);
        navMenu = view.findViewById(R.id.navMenu);
    }


    private void configurarEventos() {
        navHome.setOnClickListener(v -> navegarPara(new HomeFragment()));
        navGestor.setOnClickListener(v -> navegarPara(new TransacoesFragment()));
        navTrilha.setOnClickListener(v -> Toast.makeText(requireContext(), "Você já está na Trilha", Toast.LENGTH_SHORT).show());
        navMenu.setOnClickListener(v -> navegarPara(new MenuFragment()));

        nodeBonusTrilha.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Fase bônus em breve!", Toast.LENGTH_SHORT).show()
        );

    }

    private void carregarDadosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Moedas baseadas no rendimento ou XP (exemplo)
                        Long moedas = doc.getLong("xp");
                        if (moedas == null) moedas = 0L;
                        txtMoedasTrilha.setText("🪙 " + moedas);

                        // Dias de sequência (mock por enquanto ou campo do banco)
                        txtSequenciaTrilha.setText("🔥 3 dias");
                    }
                });
    }

    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
    }
}
