package com.example.moneva.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity; // Importante para a navegação
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
    private LinearLayout navHome, navGestor, navTrilha, navMenu;
    private TextView txtSaudacaoHome, txtReceitasHome, txtDespesasHome, txtSaldoHome, txtResumoDashboardHome, txtConquistasHome;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public HomeFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        // Usando o método de navegação centralizado na MainActivity
        btnPerfilHome.setOnClickListener(v -> navegarPara(new PerfilFragment()));

        navHome.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Você já está na Home", Toast.LENGTH_SHORT).show()
        );

        navGestor.setOnClickListener(v -> navegarPara(new TransacoesFragment()));

        navTrilha.setOnClickListener(v -> navegarPara(new TrilhaFragment()));

        navMenu.setOnClickListener(v -> navegarPara(new MenuFragment()));
    }

    // METODO AUXILIAR PARA NAVEGAÇÃO
    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
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
                                "Erro ao carregar dados: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void preencherDadosNaHome(DocumentSnapshot doc) {
        if (!doc.exists()) {
            txtSaudacaoHome.setText("Bem-vindo!");
            return;
        }

        String nome = doc.getString("displayName");
        Double rendaMensal = doc.getDouble("monthlyIncome");

        txtSaudacaoHome.setText("Fala aí, " + obterPrimeiroNome(nome) + "!");
        txtReceitasHome.setText(formatarMoeda(rendaMensal));

        // Valores fixos por enquanto ou vindo do banco
        txtDespesasHome.setText("R$ 0,00");
        txtSaldoHome.setText(formatarMoeda(rendaMensal));
        txtResumoDashboardHome.setText("Resumo rápido com base no seu perfil");
        txtConquistasHome.setText("27 / 200 desbloqueadas");
    }

    private String obterPrimeiroNome(String nomeCompleto) {
        if (TextUtils.isEmpty(nomeCompleto)) return "Usuário";
        String nomeTratado = nomeCompleto.trim();
        return nomeTratado.contains(" ") ? nomeTratado.split(" ")[0] : nomeTratado;
    }

    private String formatarMoeda(Double valor) {
        if (valor == null) return "R$ 0,00";
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatoMoeda.format(valor);
    }
}