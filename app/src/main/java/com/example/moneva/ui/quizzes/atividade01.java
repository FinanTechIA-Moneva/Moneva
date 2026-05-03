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

public class atividade01 extends Fragment {

    private TextView btnFecharAtividade, txtPergunta, txtOp1, txtOp2, txtOp3;
    private ProgressBar progressAtividade;
    private LinearLayout opcao1, opcao2, opcao3;
    private Button btnVerificarAtividade;

    private int opcaoSelecionada = 0;
    private int questaoAtualIndex = 0;
    private List<Questao> listaQuestoes = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public atividade01() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atividade01, container, false);

        inicializarFirebase();
        configurarQuestoes();
        inicializarViews(view);
        configurarEventos();
        carregarQuestao();

        return view;
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void configurarQuestoes() {
        listaQuestoes.add(new Questao("O que é uma Reserva de Emergência?",
                "Dinheiro guardado para viagens.", "Valor para gastos imprevistos e urgentes.", "O limite do seu cartão.", 2));

        listaQuestoes.add(new Questao("Qual o primeiro passo para organizar as finanças?",
                "Comprar um carro.", "Registrar todos os ganhos e gastos.", "Investir em ações arriscadas.", 2));

        listaQuestoes.add(new Questao("O que são gastos variáveis?",
                "Gastos que nunca mudam.", "Gastos que mudam conforme o consumo (ex: luz).", "Impostos anuais fixos.", 2));

        listaQuestoes.add(new Questao("Por que é importante ter um orçamento?",
                "Para saber para onde seu dinheiro vai.", "Para gastar mais do que ganha.", "Para dificultar o uso do cartão.", 1));

        listaQuestoes.add(new Questao("O que acontece se você gasta mais do que ganha?",
                "Seu patrimônio cresce.", "Você acumula dívidas e paga juros.", "O banco te dá um prêmio.", 2));
    }

    private void inicializarViews(View view) {
        btnFecharAtividade = view.findViewById(R.id.btnFecharAtividade);
        progressAtividade = view.findViewById(R.id.progressAtividade);
        txtPergunta = view.findViewById(R.id.txtPergunta);
        txtOp1 = view.findViewById(R.id.txtOpcao1);
        txtOp2 = view.findViewById(R.id.txtOpcao2);
        txtOp3 = view.findViewById(R.id.txtOpcao3);
        opcao1 = view.findViewById(R.id.opcao1);
        opcao2 = view.findViewById(R.id.opcao2);
        opcao3 = view.findViewById(R.id.opcao3);
        btnVerificarAtividade = view.findViewById(R.id.btnVerificarAtividade);
    }

    private void carregarQuestao() {
        Questao q = listaQuestoes.get(questaoAtualIndex);
        txtPergunta.setText(q.pergunta);
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
            Toast.makeText(requireContext(), "Correto! 🥳", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Ops! Tente novamente. 🤔", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Módulo concluído! +10 XP", Toast.LENGTH_LONG).show();
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
                .update("xp", FieldValue.increment(10), "faseAtual", 2);
    }

    private void fecharAtividade() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onBackPressed();
        }
    }

    private static class Questao {
        String pergunta, op1, op2, op3;
        int correta;
        Questao(String p, String o1, String o2, String o3, int c) {
            pergunta = p; op1 = o1; op2 = o2; op3 = o3; correta = c;
        }
    }
}