package com.example.moneva.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneva.R;
import com.example.moneva.ui.menu.MenuFragment;
import com.example.moneva.ui.perfil.PerfilFragment;
import com.example.moneva.ui.transacoes.TransacoesFragment;
import com.example.moneva.ui.trilha.TrilhaFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private LinearLayout btnPerfilHome;

    private LinearLayout navHome;
    private LinearLayout navGestor;
    private LinearLayout navTrilha;
    private LinearLayout navMenu;

    private TextView txtSaudacaoHome;
    private TextView txtReceitasHome;
    private TextView txtDespesasHome;
    private TextView txtSaldoHome;
    private TextView txtResumoDashboardHome;
    private TextView txtConquistasHome;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public HomeFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        inicializarViews(view);
        inicializarFirebase();
        configurarEventos();
        carregarDadosHome();

        return view;
    }

    private void inicializarViews(View view) {
        btnPerfilHome = view.findViewById(R.id.btnPerfilHome);

        navHome = view.findViewById(R.id.navHome);
        navGestor = view.findViewById(R.id.navGestor);
        navTrilha = view.findViewById(R.id.navTrilha);
        navMenu = view.findViewById(R.id.navMenu);

        txtSaudacaoHome = view.findViewById(R.id.txtSaudacaoHome);
        txtReceitasHome = view.findViewById(R.id.txtReceitasHome);
        txtDespesasHome = view.findViewById(R.id.txtDespesasHome);
        txtSaldoHome = view.findViewById(R.id.txtSaldoHome);
        txtResumoDashboardHome = view.findViewById(R.id.txtResumoDashboardHome);
        txtConquistasHome = view.findViewById(R.id.txtConquistasHome);
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void configurarEventos() {
        btnPerfilHome.setOnClickListener(v -> abrirFragment(new PerfilFragment()));

        navHome.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Você já está na Home", Toast.LENGTH_SHORT).show()
        );

        navGestor.setOnClickListener(v -> abrirFragment(new TransacoesFragment()));

        navTrilha.setOnClickListener(v -> abrirFragment(new TrilhaFragment()));

        navMenu.setOnClickListener(v -> abrirFragment(new MenuFragment()));
    }

    private void abrirFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void carregarDadosHome() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = usuarioAtual.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::preencherDadosNaHome)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao carregar dados da Home: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void preencherDadosNaHome(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(requireContext(), "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = doc.getString("displayName");
        Double rendaMensal = doc.getDouble("monthlyIncome");

        txtSaudacaoHome.setText("Fala aí, " + obterPrimeiroNome(nome) + "!");
        txtReceitasHome.setText(formatarMoeda(rendaMensal));

        txtDespesasHome.setText("Aguardando transações");
        txtSaldoHome.setText("Aguardando transações");

        txtResumoDashboardHome.setText("Resumo rápido com base no seu perfil");
        txtConquistasHome.setText("27 / 200 desbloqueadas");
    }

    private String obterPrimeiroNome(String nomeCompleto) {
        if (TextUtils.isEmpty(nomeCompleto)) {
            return "Usuário";
        }

        String nomeTratado = nomeCompleto.trim();

        if (nomeTratado.contains(" ")) {
            return nomeTratado.substring(0, nomeTratado.indexOf(" "));
        }

        return nomeTratado;
    }

    private String formatarMoeda(Double valor) {
        if (valor == null) {
            return "R$ 0,00";
        }

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatoMoeda.format(valor);
    }
}