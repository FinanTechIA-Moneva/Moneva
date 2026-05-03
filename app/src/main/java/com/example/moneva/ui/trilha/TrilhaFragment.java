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
import com.example.moneva.ui.quizzes.atividade01;
import com.example.moneva.ui.quizzes.atividade02;
import com.example.moneva.ui.quizzes.atividade03;
import com.example.moneva.ui.transacoes.TransacoesFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrilhaFragment extends Fragment {

    private LinearLayout nodeFase1, nodeFase2, nodeBonusTrilha, nodeFase3, nodeFase4;
    private TextView txtMoedasTrilha, txtSequenciaTrilha;
    private LinearLayout navHome, navGestor, navTrilha, navMenu, layoutBottomNavTrilha;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private int faseAtualUsuario = 1; // Padrão inicial

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
        nodeFase1 = view.findViewById(R.id.nodeFase1);
        nodeFase2 = view.findViewById(R.id.nodeFase2);
        nodeBonusTrilha = view.findViewById(R.id.nodeBonusTrilha);
        nodeFase3 = view.findViewById(R.id.nodeFase3);
        nodeFase4 = view.findViewById(R.id.nodeFase4);

        txtMoedasTrilha = view.findViewById(R.id.txtMoedasTrilha);
        txtSequenciaTrilha = view.findViewById(R.id.txtSequenciaTrilha);

        layoutBottomNavTrilha = view.findViewById(R.id.layoutBottomNavTrilha);
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

        // Eventos de clique nas fases usando lógica de gamificação
        nodeFase1.setOnClickListener(v -> lidarCliqueFase(1));
        nodeFase2.setOnClickListener(v -> lidarCliqueFase(2));
        nodeFase3.setOnClickListener(v -> lidarCliqueFase(3));
        nodeFase4.setOnClickListener(v -> lidarCliqueFase(4));

        nodeBonusTrilha.setOnClickListener(v -> {
            if (faseAtualUsuario > 2) {
                Toast.makeText(requireContext(), "Fase bônus em construção! 🐷", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Complete a Fase 2 para liberar o Bônus!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lidarCliqueFase(int faseClicada) {
        if (faseClicada < faseAtualUsuario) {
            Toast.makeText(requireContext(), "Fase concluída! Você pode revisar o conteúdo.", Toast.LENGTH_SHORT).show();
            abrirAtividade(faseClicada);
        } else if (faseClicada == faseAtualUsuario) {
            abrirAtividade(faseClicada);
        } else {
            Toast.makeText(requireContext(), "Fase bloqueada! Complete as anteriores para liberar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirAtividade(int fase) {
        Fragment atividade;
        switch (fase) {
            case 1: atividade = new atividade01(); break;
            case 2: atividade = new atividade02(); break;
            case 3: atividade = new atividade03(); break;
            default: atividade = new atividade01(); break;
        }
        navegarPara(atividade);
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Moedas (XP)
                        Long moedas = doc.getLong("xp");
                        txtMoedasTrilha.setText("🪙 " + (moedas != null ? moedas : 0));

                        // Fase Atual do Banco
                        Long fase = doc.getLong("faseAtual");
                        faseAtualUsuario = (fase != null) ? fase.intValue() : 1;

                        // Sequência (Streak)
                        Long streak = doc.getLong("streak");
                        txtSequenciaTrilha.setText("🔥 " + (streak != null ? streak : 1) + (streak != null && streak == 1 ? " dia" : " dias"));

                        atualizarVisualTrilha();
                    }
                });
    }

    private void atualizarVisualTrilha() {
        // Aplica o estilo visual para cada nó baseado no progresso
        aplicarEstiloNo(nodeFase1, 1, "✔");
        aplicarEstiloNo(nodeFase2, 2, "2"); // Ícone original era cadeado, mostramos o número se estiver ativo
        aplicarEstiloNo(nodeFase3, 3, "3");
        aplicarEstiloNo(nodeFase4, 4, "🏆");

        // Opacidade do bônus
        nodeBonusTrilha.setAlpha(faseAtualUsuario > 2 ? 1.0f : 0.5f);
    }

    private void aplicarEstiloNo(LinearLayout node, int faseNo, String iconeAtivo) {
        TextView txt = (TextView) node.getChildAt(0);
        if (faseNo < faseAtualUsuario) {
            // Fase Concluída
            node.setBackgroundResource(R.drawable.bg_trilha_node_bonus); // Usa o fundo verde/ativo
            txt.setText("✔");
        } else if (faseNo == faseAtualUsuario) {
            // Fase Atual (Ativa)
            node.setBackgroundResource(R.drawable.bg_trilha_node_bonus);
            txt.setText(iconeAtivo);
        } else {
            // Fase Bloqueada
            node.setBackgroundResource(R.drawable.bg_trilha_node_locked);
            txt.setText("🔒");
        }
    }

    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
    }
}