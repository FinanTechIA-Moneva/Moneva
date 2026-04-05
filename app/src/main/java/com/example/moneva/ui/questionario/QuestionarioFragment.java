package com.example.moneva.ui.questionario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity;
import com.example.moneva.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class QuestionarioFragment extends Fragment {

    private TextView btnFecharQuestionario;

    private EditText edtNomeQuestionario;
    private Spinner spinnerGeneroQuestionario;
    private EditText edtRendaMensalQuestionario;

    private Spinner spinnerObjetivoFinanceiroQuestionario;
    private LinearLayout layoutOutroObjetivoFinanceiro;
    private EditText edtOutroObjetivoFinanceiroQuestionario;

    private Spinner spinnerSonhosQuestionario;
    private LinearLayout layoutOutroSonho;
    private EditText edtOutroSonhoQuestionario;

    private Spinner spinnerStatusFinanceiroQuestionario;
    private LinearLayout layoutOutroStatusFinanceiro;
    private EditText edtOutroStatusFinanceiroQuestionario;

    private EditText edtVisao5AnosQuestionario;

    private Button btnSalvarQuestionario;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final Pattern PADRAO_SEM_CARACTERES_ESPECIAIS =
            Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s]+$");

    private final Pattern PADRAO_NOME =
            Pattern.compile("^[a-zA-ZÀ-ÿ\\s]+$");

    public QuestionarioFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_questionario, container, false);

        inicializarViews(view);
        inicializarFirebase();
        configurarSpinners();
        configurarEventos();
        carregarDadosExistentes();

        return view;
    }

    private void inicializarViews(View view) {
        btnFecharQuestionario = view.findViewById(R.id.btnFecharQuestionario);

        edtNomeQuestionario = view.findViewById(R.id.edtNomeQuestionario);
        spinnerGeneroQuestionario = view.findViewById(R.id.spinnerGeneroQuestionario);
        edtRendaMensalQuestionario = view.findViewById(R.id.edtRendaMensalQuestionario);

        spinnerObjetivoFinanceiroQuestionario = view.findViewById(R.id.spinnerObjetivoFinanceiroQuestionario);
        layoutOutroObjetivoFinanceiro = view.findViewById(R.id.layoutOutroObjetivoFinanceiro);
        edtOutroObjetivoFinanceiroQuestionario = view.findViewById(R.id.edtOutroObjetivoFinanceiroQuestionario);

        spinnerSonhosQuestionario = view.findViewById(R.id.spinnerSonhosQuestionario);
        layoutOutroSonho = view.findViewById(R.id.layoutOutroSonho);
        edtOutroSonhoQuestionario = view.findViewById(R.id.edtOutroSonhoQuestionario);

        spinnerStatusFinanceiroQuestionario = view.findViewById(R.id.spinnerStatusFinanceiroQuestionario);
        layoutOutroStatusFinanceiro = view.findViewById(R.id.layoutOutroStatusFinanceiro);
        edtOutroStatusFinanceiroQuestionario = view.findViewById(R.id.edtOutroStatusFinanceiroQuestionario);

        edtVisao5AnosQuestionario = view.findViewById(R.id.edtVisao5AnosQuestionario);

        btnSalvarQuestionario = view.findViewById(R.id.btnSalvarQuestionario);
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void configurarSpinners() {
        String[] opcoesGenero = {
                "Selecione o gênero",
                "Mulher cisgênero",
                "Homem cisgênero",
                "Mulher transgênero",
                "Homem transgênero",
                "Travesti",
                "Pessoa não-binária",
                "Agênero",
                "Gênero fluido",
                "Bigênero",
                "Prefiro não informar",
                "Outro"
        };

        String[] opcoesObjetivoFinanceiro = {
                "Selecione o objetivo financeiro",
                "Economizar dinheiro",
                "Quitar dívidas",
                "Comprar a casa própria",
                "Investir",
                "Aumentar a renda",
                "Outros"
        };

        String[] opcoesSonhos = {
                "Selecione o sonho",
                "Viajar",
                "Celular novo",
                "Comprar um carro",
                "Vestuário",
                "Entretenimento",
                "Outros"
        };

        String[] opcoesStatusFinanceiro = {
                "Selecione a situação financeira",
                "Endividado",
                "Não consegue economizar",
                "Pouca renda",
                "Equilibrado",
                "Confortável",
                "Outros"
        };

        configurarAdapterSpinner(spinnerGeneroQuestionario, opcoesGenero);
        configurarAdapterSpinner(spinnerObjetivoFinanceiroQuestionario, opcoesObjetivoFinanceiro);
        configurarAdapterSpinner(spinnerSonhosQuestionario, opcoesSonhos);
        configurarAdapterSpinner(spinnerStatusFinanceiroQuestionario, opcoesStatusFinanceiro);
    }

    private void configurarAdapterSpinner(Spinner spinner, String[] opcoes) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                opcoes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnFecharQuestionario.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        spinnerObjetivoFinanceiroQuestionario.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selecionado = parent.getItemAtPosition(position).toString();
                layoutOutroObjetivoFinanceiro.setVisibility(selecionado.equals("Outros") ? View.VISIBLE : View.GONE);

                if (!selecionado.equals("Outros")) {
                    edtOutroObjetivoFinanceiroQuestionario.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        spinnerSonhosQuestionario.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selecionado = parent.getItemAtPosition(position).toString();
                layoutOutroSonho.setVisibility(selecionado.equals("Outros") ? View.VISIBLE : View.GONE);

                if (!selecionado.equals("Outros")) {
                    edtOutroSonhoQuestionario.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        spinnerStatusFinanceiroQuestionario.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selecionado = parent.getItemAtPosition(position).toString();
                layoutOutroStatusFinanceiro.setVisibility(selecionado.equals("Outros") ? View.VISIBLE : View.GONE);

                if (!selecionado.equals("Outros")) {
                    edtOutroStatusFinanceiroQuestionario.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnSalvarQuestionario.setOnClickListener(v -> validarQuestionario());
    }

    private void carregarDadosExistentes() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            return;
        }

        String uid = usuarioAtual.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::preencherFormulario)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao carregar dados do questionário: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void preencherFormulario(DocumentSnapshot doc) {
        if (!doc.exists()) {
            return;
        }

        String nome = doc.getString("displayName");
        String genero = doc.getString("gender");
        Double rendaMensal = doc.getDouble("monthlyIncome");
        String objetivoFinanceiro = doc.getString("financialGoals");
        String sonho = doc.getString("dreams");
        String statusFinanceiro = doc.getString("currentStatus");
        String visao5Anos = doc.getString("futureVision5Years");

        if (nome != null) {
            edtNomeQuestionario.setText(nome);
        }

        if (rendaMensal != null) {
            if (rendaMensal % 1 == 0) {
                edtRendaMensalQuestionario.setText(String.valueOf(rendaMensal.intValue()));
            } else {
                edtRendaMensalQuestionario.setText(String.valueOf(rendaMensal));
            }
        }

        if (visao5Anos != null) {
            edtVisao5AnosQuestionario.setText(visao5Anos);
        }

        selecionarGenero(genero);
        selecionarObjetivoFinanceiro(objetivoFinanceiro);
        selecionarSonho(sonho);
        selecionarStatusFinanceiro(statusFinanceiro);
    }

    private void selecionarGenero(String generoSalvo) {
        if (generoSalvo == null) {
            return;
        }

        String valor = generoSalvo.trim().toLowerCase();

        if (valor.equals("masculino")) {
            setSpinnerSelection(spinnerGeneroQuestionario, "Homem cisgênero");
        } else if (valor.equals("feminino")) {
            setSpinnerSelection(spinnerGeneroQuestionario, "Mulher cisgênero");
        } else {
            setSpinnerSelection(spinnerGeneroQuestionario, valor);
        }
    }

    private void selecionarObjetivoFinanceiro(String valorSalvo) {
        if (valorSalvo == null || valorSalvo.trim().isEmpty()) {
            return;
        }

        if (existeNoSpinner(spinnerObjetivoFinanceiroQuestionario, valorSalvo)) {
            setSpinnerSelection(spinnerObjetivoFinanceiroQuestionario, valorSalvo);
        } else {
            setSpinnerSelection(spinnerObjetivoFinanceiroQuestionario, "Outros");
            layoutOutroObjetivoFinanceiro.setVisibility(View.VISIBLE);
            edtOutroObjetivoFinanceiroQuestionario.setText(valorSalvo);
        }
    }

    private void selecionarSonho(String valorSalvo) {
        if (valorSalvo == null || valorSalvo.trim().isEmpty()) {
            return;
        }

        if (existeNoSpinner(spinnerSonhosQuestionario, valorSalvo)) {
            setSpinnerSelection(spinnerSonhosQuestionario, valorSalvo);
        } else {
            setSpinnerSelection(spinnerSonhosQuestionario, "Outros");
            layoutOutroSonho.setVisibility(View.VISIBLE);
            edtOutroSonhoQuestionario.setText(valorSalvo);
        }
    }

    private void selecionarStatusFinanceiro(String valorSalvo) {
        if (valorSalvo == null || valorSalvo.trim().isEmpty()) {
            return;
        }

        String valorMapeado;

        switch (valorSalvo) {
            case "endividado":
                valorMapeado = "Endividado";
                break;
            case "sem_economia":
                valorMapeado = "Não consegue economizar";
                break;
            case "pouca_renda":
                valorMapeado = "Pouca renda";
                break;
            case "equilibrado":
                valorMapeado = "Equilibrado";
                break;
            case "confortavel":
                valorMapeado = "Confortável";
                break;
            default:
                valorMapeado = valorSalvo;
                break;
        }

        if (existeNoSpinner(spinnerStatusFinanceiroQuestionario, valorMapeado)) {
            setSpinnerSelection(spinnerStatusFinanceiroQuestionario, valorMapeado);
        } else {
            setSpinnerSelection(spinnerStatusFinanceiroQuestionario, "Outros");
            layoutOutroStatusFinanceiro.setVisibility(View.VISIBLE);
            edtOutroStatusFinanceiroQuestionario.setText(valorSalvo);
        }
    }

    private boolean existeNoSpinner(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        return false;
    }

    private void setSpinnerSelection(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void validarQuestionario() {
        String nome = edtNomeQuestionario.getText().toString().trim();
        String genero = spinnerGeneroQuestionario.getSelectedItem().toString();
        String rendaMensal = edtRendaMensalQuestionario.getText().toString().trim();

        String objetivoFinanceiro = spinnerObjetivoFinanceiroQuestionario.getSelectedItem().toString();
        String outroObjetivoFinanceiro = edtOutroObjetivoFinanceiroQuestionario.getText().toString().trim();

        String sonho = spinnerSonhosQuestionario.getSelectedItem().toString();
        String outroSonho = edtOutroSonhoQuestionario.getText().toString().trim();

        String statusFinanceiro = spinnerStatusFinanceiroQuestionario.getSelectedItem().toString();
        String outroStatusFinanceiro = edtOutroStatusFinanceiroQuestionario.getText().toString().trim();

        String visao5Anos = edtVisao5AnosQuestionario.getText().toString().trim();

        if (TextUtils.isEmpty(nome) ||
                genero.equals("Selecione o gênero") ||
                TextUtils.isEmpty(rendaMensal) ||
                objetivoFinanceiro.equals("Selecione o objetivo financeiro") ||
                sonho.equals("Selecione o sonho") ||
                statusFinanceiro.equals("Selecione a situação financeira") ||
                TextUtils.isEmpty(visao5Anos)) {

            Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PADRAO_NOME.matcher(nome).matches()) {
            edtNomeQuestionario.setError("O nome não pode conter caracteres especiais.");
            edtNomeQuestionario.requestFocus();
            return;
        }

        if (!rendaMensal.matches("^[0-9]+$")) {
            edtRendaMensalQuestionario.setError("A renda mensal deve conter apenas números.");
            edtRendaMensalQuestionario.requestFocus();
            return;
        }

        if (objetivoFinanceiro.equals("Outros")) {
            if (TextUtils.isEmpty(outroObjetivoFinanceiro)) {
                edtOutroObjetivoFinanceiroQuestionario.setError("Digite o outro objetivo financeiro.");
                edtOutroObjetivoFinanceiroQuestionario.requestFocus();
                return;
            }

            if (!PADRAO_SEM_CARACTERES_ESPECIAIS.matcher(outroObjetivoFinanceiro).matches()) {
                edtOutroObjetivoFinanceiroQuestionario.setError("Não use caracteres especiais.");
                edtOutroObjetivoFinanceiroQuestionario.requestFocus();
                return;
            }
        }

        if (sonho.equals("Outros")) {
            if (TextUtils.isEmpty(outroSonho)) {
                edtOutroSonhoQuestionario.setError("Digite o outro sonho.");
                edtOutroSonhoQuestionario.requestFocus();
                return;
            }

            if (!PADRAO_SEM_CARACTERES_ESPECIAIS.matcher(outroSonho).matches()) {
                edtOutroSonhoQuestionario.setError("Não use caracteres especiais.");
                edtOutroSonhoQuestionario.requestFocus();
                return;
            }
        }

        if (statusFinanceiro.equals("Outros")) {
            if (TextUtils.isEmpty(outroStatusFinanceiro)) {
                edtOutroStatusFinanceiroQuestionario.setError("Digite a outra situação financeira.");
                edtOutroStatusFinanceiroQuestionario.requestFocus();
                return;
            }

            if (!PADRAO_SEM_CARACTERES_ESPECIAIS.matcher(outroStatusFinanceiro).matches()) {
                edtOutroStatusFinanceiroQuestionario.setError("Não use caracteres especiais.");
                edtOutroStatusFinanceiroQuestionario.requestFocus();
                return;
            }
        }

        if (!PADRAO_SEM_CARACTERES_ESPECIAIS.matcher(visao5Anos).matches()) {
            edtVisao5AnosQuestionario.setError("Não use caracteres especiais.");
            edtVisao5AnosQuestionario.requestFocus();
            return;
        }

        salvarQuestionarioNoFirebase(
                nome,
                genero,
                rendaMensal,
                objetivoFinanceiro,
                outroObjetivoFinanceiro,
                sonho,
                outroSonho,
                statusFinanceiro,
                outroStatusFinanceiro,
                visao5Anos
        );
    }

    private void salvarQuestionarioNoFirebase(String nome,
                                              String genero,
                                              String rendaMensal,
                                              String objetivoFinanceiro,
                                              String outroObjetivoFinanceiro,
                                              String sonho,
                                              String outroSonho,
                                              String statusFinanceiro,
                                              String outroStatusFinanceiro,
                                              String visao5Anos) {

        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = usuarioAtual.getUid();

        String objetivoFinanceiroFinal = objetivoFinanceiro.equals("Outros")
                ? outroObjetivoFinanceiro
                : objetivoFinanceiro;

        String sonhoFinal = sonho.equals("Outros")
                ? outroSonho
                : sonho;

        String statusFinanceiroFinal = mapearStatusParaFirestore(statusFinanceiro, outroStatusFinanceiro);

        String generoFinal = mapearGeneroParaFirestore(genero);

        Map<String, Object> dadosQuestionario = new HashMap<>();
        dadosQuestionario.put("displayName", nome);
        dadosQuestionario.put("gender", generoFinal);
        dadosQuestionario.put("monthlyIncome", Double.parseDouble(rendaMensal));
        dadosQuestionario.put("financialGoals", objetivoFinanceiroFinal);
        dadosQuestionario.put("dreams", sonhoFinal);
        dadosQuestionario.put("currentStatus", statusFinanceiroFinal);
        dadosQuestionario.put("futureVision5Years", visao5Anos);
        dadosQuestionario.put("completedOnboarding", true);
        dadosQuestionario.put("updatedAt", new Timestamp(new Date()));

        btnSalvarQuestionario.setEnabled(false);
        btnSalvarQuestionario.setText("Salvando...");

        db.collection("users")
                .document(uid)
                .update(dadosQuestionario)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Questionário salvo com sucesso!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    btnSalvarQuestionario.setEnabled(true);
                    btnSalvarQuestionario.setText("Salvar questionário");
                    Toast.makeText(requireContext(),
                            "Erro ao salvar questionário: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String mapearGeneroParaFirestore(String generoSelecionado) {
        switch (generoSelecionado) {
            case "Homem cisgênero":
                return "masculino";
            case "Mulher cisgênero":
                return "feminino";
            default:
                return generoSelecionado.toLowerCase();
        }
    }

    private String mapearStatusParaFirestore(String statusSelecionado, String outroStatus) {
        if (statusSelecionado.equals("Outros")) {
            return outroStatus;
        }

        switch (statusSelecionado) {
            case "Endividado":
                return "endividado";
            case "Não consegue economizar":
                return "sem_economia";
            case "Pouca renda":
                return "pouca_renda";
            case "Equilibrado":
                return "equilibrado";
            case "Confortável":
                return "confortavel";
            default:
                return statusSelecionado.toLowerCase();
        }
    }
}