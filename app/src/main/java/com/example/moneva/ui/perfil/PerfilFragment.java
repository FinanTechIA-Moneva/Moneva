package com.example.moneva.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneva.R;
import com.example.moneva.ui.auth.LoginActivity;
import com.example.moneva.ui.questionario.QuestionarioFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class PerfilFragment extends Fragment {

    private TextView btnFecharPerfil;
    private TextView btnEditarPerfil;
    private Button btnSairConta;

    private TextView txtInicialAvatar;
    private TextView txtNomePerfil;
    private TextView txtEmailPerfil;

    private TextView txtNomeValor;
    private TextView txtGeneroValor;
    private TextView txtRendaValor;

    private TextView txtObjetivoFinanceiroValor;
    private TextView txtSonhoValor;
    private TextView txtVisao5AnosValor;
    private TextView txtStatusFinanceiroValor;

    private TextView txtConquistasValor;
    private ProgressBar progressConquistas;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public PerfilFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        inicializarViews(view);
        inicializarFirebase();
        configurarEventos();
        carregarDadosPerfil();

        return view;
    }

    private void inicializarViews(View view) {
        btnFecharPerfil = view.findViewById(R.id.btnFecharPerfil);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnSairConta = view.findViewById(R.id.btnSairConta);

        txtInicialAvatar = view.findViewById(R.id.txtInicialAvatar);
        txtNomePerfil = view.findViewById(R.id.txtNomePerfil);
        txtEmailPerfil = view.findViewById(R.id.txtEmailPerfil);

        txtNomeValor = view.findViewById(R.id.txtNomeValor);
        txtGeneroValor = view.findViewById(R.id.txtGeneroValor);
        txtRendaValor = view.findViewById(R.id.txtRendaValor);

        txtObjetivoFinanceiroValor = view.findViewById(R.id.txtObjetivoFinanceiroValor);
        txtSonhoValor = view.findViewById(R.id.txtSonhoValor);
        txtVisao5AnosValor = view.findViewById(R.id.txtVisao5AnosValor);
        txtStatusFinanceiroValor = view.findViewById(R.id.txtStatusFinanceiroValor);

        txtConquistasValor = view.findViewById(R.id.txtConquistasValor);
        progressConquistas = view.findViewById(R.id.progressConquistas);
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void configurarEventos() {
        btnFecharPerfil.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnEditarPerfil.setOnClickListener(v -> abrirDialogEditarPerfil());

        btnSairConta.setOnClickListener(v -> abrirDialogLogout());
    }

    private void abrirDialogEditarPerfil() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Editar informações")
                .setMessage("Deseja editar as informações do seu questionário?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sim", (dialog, which) -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new QuestionarioFragment())
                            .addToBackStack(null)
                            .commit();
                })
                .show();
    }

    private void abrirDialogLogout() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirmar_logout, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarLogoutDialog);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarLogoutDialog);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();
            realizarLogout();
        });

        dialog.show();
    }

    private void realizarLogout() {
        auth.signOut();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void carregarDadosPerfil() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = usuarioAtual.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::preencherDadosNaTela)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao carregar perfil: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void preencherDadosNaTela(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(requireContext(), "Perfil do usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = doc.getString("displayName");
        String email = doc.getString("email");
        String genero = doc.getString("gender");
        String objetivoFinanceiro = doc.getString("financialGoals");
        String sonho = doc.getString("dreams");
        String visao5Anos = doc.getString("futureVision5Years");
        String statusFinanceiro = doc.getString("currentStatus");

        Double rendaMensal = doc.getDouble("monthlyIncome");

        int conquistasAtuais = 27;
        int conquistasTotais = 200;

        txtNomePerfil.setText(valorOuPadrao(nome, "Usuário"));
        txtEmailPerfil.setText(valorOuPadrao(email, "Sem e-mail"));

        txtNomeValor.setText(valorOuPadrao(nome, "-"));
        txtGeneroValor.setText(formatarGenero(genero));
        txtRendaValor.setText(formatarMoeda(rendaMensal));

        txtObjetivoFinanceiroValor.setText(valorOuPadrao(objetivoFinanceiro, "-"));
        txtSonhoValor.setText(valorOuPadrao(sonho, "-"));
        txtVisao5AnosValor.setText(valorOuPadrao(visao5Anos, "-"));
        txtStatusFinanceiroValor.setText(formatarStatusFinanceiro(statusFinanceiro));

        definirInicialAvatar(nome);

        progressConquistas.setMax(conquistasTotais);
        progressConquistas.setProgress(conquistasAtuais);
        txtConquistasValor.setText(conquistasAtuais + " / " + conquistasTotais);
    }

    private void definirInicialAvatar(String nome) {
        if (!TextUtils.isEmpty(nome)) {
            String inicial = nome.substring(0, 1).toUpperCase();
            txtInicialAvatar.setText(inicial);
        } else {
            txtInicialAvatar.setText("U");
        }
    }

    private String valorOuPadrao(String valor, String padrao) {
        if (valor == null || valor.trim().isEmpty()) {
            return padrao;
        }
        return valor;
    }

    private String formatarMoeda(Double valor) {
        if (valor == null) {
            return "-";
        }

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatoMoeda.format(valor);
    }

    private String formatarGenero(String genero) {
        if (genero == null || genero.trim().isEmpty()) {
            return "-";
        }

        switch (genero) {
            case "masculino":
                return "Masculino";
            case "feminino":
                return "Feminino";
            default:
                return genero;
        }
    }

    private String formatarStatusFinanceiro(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "-";
        }

        switch (status) {
            case "endividado":
                return "Endividado";
            case "sem_economia":
                return "Não consegue economizar";
            case "pouca_renda":
                return "Pouca renda";
            case "equilibrado":
                return "Equilibrado";
            case "confortavel":
                return "Confortável";
            default:
                return status;
        }
    }
}