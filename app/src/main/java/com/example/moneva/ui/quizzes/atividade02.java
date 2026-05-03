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

public class atividade02 extends Fragment {

    private TextView btnFecharAtividade, txtPergunta, txtOp1, txtOp2, txtOp3;
    private ProgressBar progressAtividade;
    private LinearLayout opcao1, opcao2, opcao3;
    private Button btnVerificarAtividade;

    private int opcaoSelecionada = 0;
    private int questaoAtualIndex = 0;
    private List<Questao> listaQuestoes = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public atividade02() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atividade02, container, false);

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
        listaQuestoes.add(new Questao("Qual destes é considerado um GASTO ESSENCIAL?",
                "Assinatura de Streaming", "Aluguel / Moradia", "Jantar fora no final de semana", 2));
        
        listaQuestoes.add(new Questao("O que é um gasto supérfluo?",
                "Coisas que não são estritamente necessárias.", "Moradia e saúde.", "Comida básica do dia a dia.", 1));
        
        listaQuestoes.add(new Questao("Qual a regra 50-30-20 das finanças?",
                "50% desejos, 30% necessidades, 20% dívidas.", "50% necessidades, 30% desejos, 20% poupança.", "50% poupança, 30% necessidades, 20% desejos.", 2));
        
        listaQuestoes.add(new Questao("O que significa a sigla ROI?",
                "Retorno sobre Investimento.", "Reserva de Ouro Interna.", "Registro de Operações Imobiliárias.", 1));
        
        listaQuestoes.add(new Questao("Qual a vantagem de pagar à vista?",
                "Pagar o valor total parcelado.", "Conseguir descontos e evitar dívidas.", "Acumular juros sobre o valor.", 2));
    }

    private void inicializarViews(View view) {
        btnFecharAtividade = view.findViewById(R.id.btnFecharAtividade02);
        progressAtividade = view.findViewById(R.id.progressAtividade02);
        txtPergunta = view.findViewById(R.id.txtPerguntaAtividade02);
        txtOp1 = view.findViewById(R.id.txtOpcao2_1);
        txtOp2 = view.findViewById(R.id.txtOpcao2_2);
        txtOp3 = view.findViewById(R.id.txtOpcao2_3);
        opcao1 = view.findViewById(R.id.opcao2_1);
        opcao2 = view.findViewById(R.id.opcao2_2);
        opcao3 = view.findViewById(R.id.opcao2_3);
        btnVerificarAtividade = view.findViewById(R.id.btnVerificarAtividade02);
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
            Toast.makeText(requireContext(), "Correto! ✨", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Ops! Tente novamente. 🧐", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Módulo concluído! +15 XP", Toast.LENGTH_LONG).show();
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
                .update("xp", FieldValue.increment(15), "faseAtual", 3);
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
