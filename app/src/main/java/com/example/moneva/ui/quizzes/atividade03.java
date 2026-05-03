package com.example.moneva.ui.quizzes;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity;
import com.example.moneva.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class atividade03 extends Fragment {

    private TextView btnFecharAtividade, txtPergunta, txtOp1, txtOp2, txtOp3, txtIconeCentral;
    private ProgressBar progressAtividade;
    private LinearLayout opcao1, opcao2, opcao3;
    private Button btnVerificarAtividade;

    private int opcaoSelecionada = 0;
    private int questaoAtualIndex = 0;
    private List<Questao> listaQuestoes = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atividade03, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configurarQuestoes();
        inicializarViews(view);
        configurarEventos();
        carregarQuestao();

        return view;
    }

    private void configurarQuestoes() {
        listaQuestoes.add(new Questao("O que acontece quando a INFLAÇÃO aumenta?",
                "Seu dinheiro vale MAIS.", "O poder de compra DIMINUI.", "Os preços dos produtos CAEM.", 2, "💸"));
        listaQuestoes.add(new Questao("Qual desses investimentos é considerado de BAIXO risco?",
                "Tesouro Direto Selic", "Ações de empresas novas", "Criptomoedas", 1, "🛡️"));
        listaQuestoes.add(new Questao("O que é o CDI nas finanças?",
                "Um tipo de imposto federal.", "Uma taxa de juros entre bancos.", "Um consórcio de imóveis.", 2, "🏦"));
        listaQuestoes.add(new Questao("Para que serve a DIVERSIFICAÇÃO?",
                "Para gastar em várias lojas.", "Para reduzir riscos nos investimentos.", "Para concentrar todo dinheiro em um só lugar.", 2, "🎯"));
        listaQuestoes.add(new Questao("Qual a principal característica da Poupança?",
                "Altíssima rentabilidade.", "Segurança e liquidez imediata.", "Imposto de renda altíssimo.", 2, "🐷"));
    }

    private void inicializarViews(View view) {
        btnFecharAtividade = view.findViewById(R.id.btnFecharAtividade03);
        progressAtividade = view.findViewById(R.id.progressAtividade03);
        txtPergunta = view.findViewById(R.id.txtPerguntaAtividade03);
        txtIconeCentral = view.findViewById(R.id.txtIconeCentral03);
        txtOp1 = view.findViewById(R.id.txtOpcao3_1);
        txtOp2 = view.findViewById(R.id.txtOpcao3_2);
        txtOp3 = view.findViewById(R.id.txtOpcao3_3);
        opcao1 = view.findViewById(R.id.opcao3_1);
        opcao2 = view.findViewById(R.id.opcao3_2);
        opcao3 = view.findViewById(R.id.opcao3_3);
        btnVerificarAtividade = view.findViewById(R.id.btnVerificarAtividade03);
    }

    private void carregarQuestao() {
        Questao q = listaQuestoes.get(questaoAtualIndex);
        txtPergunta.setText(q.pergunta);
        txtIconeCentral.setText(q.icone);
        txtOp1.setText(q.op1);
        txtOp2.setText(q.op2);
        txtOp3.setText(q.op3);

        opcaoSelecionada = 0;
        resetarEstilosOpcoes();

        int progresso = (int) (((float) questaoAtualIndex / listaQuestoes.size()) * 100);
        progressAtividade.setProgress(progresso);

        btnVerificarAtividade.setText("VERIFICAR");
        btnVerificarAtividade.setEnabled(false);
        btnVerificarAtividade.setAlpha(0.5f);
        btnVerificarAtividade.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.moneva_green_dark)));
    }

    private void configurarEventos() {
        btnFecharAtividade.setOnClickListener(v -> fecharAtividade());
        opcao1.setOnClickListener(v -> selecionarOpcao(1));
        opcao2.setOnClickListener(v -> selecionarOpcao(2));
        opcao3.setOnClickListener(v -> selecionarOpcao(3));
        btnVerificarAtividade.setOnClickListener(v -> verificarResposta());
    }

    private void selecionarOpcao(int id) {
        opcaoSelecionada = id;
        resetarEstilosOpcoes();
        LinearLayout selecionada = (id == 1) ? opcao1 : (id == 2 ? opcao2 : opcao3);
        selecionada.setBackgroundResource(R.drawable.bg_home_highlight);
        btnVerificarAtividade.setEnabled(true);
        btnVerificarAtividade.setAlpha(1.0f);
    }

    private void verificarResposta() {
        Questao q = listaQuestoes.get(questaoAtualIndex);
        if (opcaoSelecionada == q.correta) {
            btnVerificarAtividade.setText("CONTINUAR");
            btnVerificarAtividade.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.moneva_green)));
            btnVerificarAtividade.setOnClickListener(v -> proximaQuestao());
            Toast.makeText(requireContext(), "Incrível! 🥳", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Tente novamente! A educação financeira é a chave. 🗝️", Toast.LENGTH_SHORT).show();
        }
    }

    private void proximaQuestao() {
        questaoAtualIndex++;
        if (questaoAtualIndex < listaQuestoes.size()) {
            carregarQuestao();
            btnVerificarAtividade.setOnClickListener(v -> verificarResposta());
        } else {
            progressAtividade.setProgress(100);
            atualizarProgressoNoBanco();
            Toast.makeText(requireContext(), "Trilha concluída! +20 XP", Toast.LENGTH_LONG).show();
            fecharAtividade();
        }
    }

    private void resetarEstilosOpcoes() {
        opcao1.setBackgroundResource(R.drawable.bg_home_card);
        opcao2.setBackgroundResource(R.drawable.bg_home_card);
        opcao3.setBackgroundResource(R.drawable.bg_home_card);
    }

    private void atualizarProgressoNoBanco() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .update("xp", FieldValue.increment(20), "faseAtual", 4);
    }

    private void fecharAtividade() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackPressed();
        }
    }

    private static class Questao {
        String pergunta, op1, op2, op3, icone;
        int correta;
        Questao(String p, String o1, String o2, String o3, int c, String i) {
            pergunta = p; op1 = o1; op2 = o2; op3 = o3; correta = c; icone = i;
        }
    }
}