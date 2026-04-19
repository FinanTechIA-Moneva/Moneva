package com.example.moneva.ui.transacoes;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity;
import com.example.moneva.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransacoesFragment extends Fragment {

    private TextView txtMesAtualTransacoes;
    private TextView txtEntradasValor;
    private TextView txtSaidasValor;
    private TextView txtSaldoValor;

    private TextView btnMesAnterior;
    private TextView btnMesProximo;
    private TextView btnFiltro;
    private TextView btnBusca;
    private TextView btnLixeira;

    private TextView btnNovaDespesaFixo;
    private TextView btnNovaReceitaFixo;
    private TextView btnNovaDespesaFlutuante;
    private TextView btnNovaReceitaFlutuante;

    private LinearLayout containerCategorias;
    private LinearLayout layoutAcoesFixas;
    private LinearLayout layoutAcoesFlutuantes;
    private LinearLayout layoutBuscaAtiva;

    private TextView txtBuscaAtiva;
    private TextView btnLimparBusca;

    private ScrollView scrollGestor;
    private PieChart pieChartGestor;

    private View navHome;
    private View navGestor;
    private View navTrilha;
    private View navMenu;

    private final Calendar calendarioAtual = Calendar.getInstance();

    private final List<RegistroFinanceiro> registros = new ArrayList<>();
    private final List<RegistroFinanceiro> registrosLixeira = new ArrayList<>();
    private final List<RegistroFinanceiro> registrosFiltrados = new ArrayList<>();

    private String filtroTipoAtivo = "Todos";
    private String filtroMeioAtivo = "Todos";
    private String filtroCategoriaAtiva = "Todas";

    private String buscaTextoAtiva = "";
    private String buscaValorAtivo = "";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public TransacoesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transacoes, container, false);

        inicializarViews(view);
        inicializarFirebase();
        configurarEventos();
        atualizarCabecalhoMes();
        configurarGrafico();
        configurarScrollAcoesFlutuantes();
        carregarTransacoesDoFirebase();

        return view;
    }

    private void inicializarViews(View view) {
        txtMesAtualTransacoes = view.findViewById(R.id.txtMesAtualTransacoes);
        txtEntradasValor = view.findViewById(R.id.txtEntradasValor);
        txtSaidasValor = view.findViewById(R.id.txtSaidasValor);
        txtSaldoValor = view.findViewById(R.id.txtSaldoValor);

        btnMesAnterior = view.findViewById(R.id.btnMesAnterior);
        btnMesProximo = view.findViewById(R.id.btnMesProximo);
        btnFiltro = view.findViewById(R.id.btnFiltro);
        btnBusca = view.findViewById(R.id.btnBusca);
        btnLixeira = view.findViewById(R.id.btnLixeira);

        btnNovaDespesaFixo = view.findViewById(R.id.btnNovaDespesaFixo);
        btnNovaReceitaFixo = view.findViewById(R.id.btnNovaReceitaFixo);
        btnNovaDespesaFlutuante = view.findViewById(R.id.btnNovaDespesaFlutuante);
        btnNovaReceitaFlutuante = view.findViewById(R.id.btnNovaReceitaFlutuante);

        containerCategorias = view.findViewById(R.id.containerCategorias);
        layoutAcoesFixas = view.findViewById(R.id.layoutAcoesFixas);
        layoutAcoesFlutuantes = view.findViewById(R.id.layoutAcoesFlutuantes);
        layoutBuscaAtiva = view.findViewById(R.id.layoutBuscaAtiva);

        txtBuscaAtiva = view.findViewById(R.id.txtBuscaAtiva);
        btnLimparBusca = view.findViewById(R.id.btnLimparBusca);

        scrollGestor = view.findViewById(R.id.scrollGestor);
        pieChartGestor = view.findViewById(R.id.pieChartGestor);

        navHome = view.findViewById(R.id.navHome);
        navGestor = view.findViewById(R.id.navGestor);
        navTrilha = view.findViewById(R.id.navTrilha);
        navMenu = view.findViewById(R.id.navMenu);
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void configurarEventos() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.home.HomeFragment()));
        }

        if (navGestor != null) {
            navGestor.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Você já está no Gestor", Toast.LENGTH_SHORT).show()
            );
        }

        if (navTrilha != null) {
            navTrilha.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.trilha.TrilhaFragment()));
        }

        if (navMenu != null) {
            navMenu.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.menu.MenuFragment()));
        }

        btnMesAnterior.setOnClickListener(v -> {
            calendarioAtual.add(Calendar.MONTH, -1);
            atualizarCabecalhoMes();
            carregarTransacoesDoFirebase();
        });

        btnMesProximo.setOnClickListener(v -> {
            calendarioAtual.add(Calendar.MONTH, 1);
            atualizarCabecalhoMes();
            carregarTransacoesDoFirebase();
        });

        btnNovaDespesaFixo.setOnClickListener(v -> abrirDialogMovimentacao(null, "saida"));
        btnNovaReceitaFixo.setOnClickListener(v -> abrirDialogMovimentacao(null, "entrada"));
        btnNovaDespesaFlutuante.setOnClickListener(v -> abrirDialogMovimentacao(null, "saida"));
        btnNovaReceitaFlutuante.setOnClickListener(v -> abrirDialogMovimentacao(null, "entrada"));

        btnFiltro.setOnClickListener(v -> abrirDialogFiltro());
        btnBusca.setOnClickListener(v -> abrirDialogBusca());
        btnLixeira.setOnClickListener(v -> abrirDialogLixeira());

        btnLimparBusca.setOnClickListener(v -> limparBuscaEFiltro());
    }

    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
    }

    private void configurarScrollAcoesFlutuantes() {
        if (scrollGestor == null) return;

        scrollGestor.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollGestor.getScrollY();

            if (scrollY > 420) {
                layoutAcoesFlutuantes.setVisibility(View.VISIBLE);
                layoutAcoesFixas.setVisibility(View.INVISIBLE);
            } else {
                layoutAcoesFlutuantes.setVisibility(View.GONE);
                layoutAcoesFixas.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configurarGrafico() {
        if (pieChartGestor == null) return;

        pieChartGestor.setUsePercentValues(false);
        pieChartGestor.setDrawHoleEnabled(true);
        pieChartGestor.setHoleRadius(68f);
        pieChartGestor.setTransparentCircleRadius(72f);
        pieChartGestor.setDrawEntryLabels(false);
        pieChartGestor.setRotationEnabled(false);
        pieChartGestor.setHighlightPerTapEnabled(false);
        pieChartGestor.setHoleColor(ContextCompat.getColor(requireContext(), R.color.moneva_bg));

        Description description = new Description();
        description.setText("");
        pieChartGestor.setDescription(description);

        Legend legend = pieChartGestor.getLegend();
        legend.setEnabled(false);

        pieChartGestor.setNoDataText("Sem dados para este mês");
    }

    private void atualizarCabecalhoMes() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
        String mes = sdf.format(calendarioAtual.getTime());

        if (!mes.isEmpty()) {
            mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        }

        txtMesAtualTransacoes.setText(mes);
    }

    private String obterMonthKeyAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(calendarioAtual.getTime());
    }

    private void carregarTransacoesDoFirebase() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        registros.clear();
        registrosLixeira.clear();
        registrosFiltrados.clear();

        String uid = usuarioAtual.getUid();
        String monthKey = obterMonthKeyAtual();

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .whereEqualTo("monthKey", monthKey)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        RegistroFinanceiro registro = converterDocumentoParaRegistro(doc);
                        if (registro == null) continue;

                        if (registro.deletedAt != null) {
                            registrosLixeira.add(registro);
                        } else {
                            registros.add(registro);
                        }
                    }

                    aplicarFiltrosEBusca();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao carregar transações: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private RegistroFinanceiro converterDocumentoParaRegistro(DocumentSnapshot doc) {
        String id = doc.getId();
        String tipo = valorOuPadrao(doc.getString("tipo"), "");
        String categoria = valorOuPadrao(doc.getString("categoria"), "Outros");
        String meio = valorOuPadrao(doc.getString("meio"), "outro");
        String obs = valorOuPadrao(doc.getString("obs"), "");

        Double valorDouble = doc.getDouble("valor");
        double valor = valorDouble != null ? valorDouble : 0.0;

        Timestamp dateTimestamp = doc.getTimestamp("date");
        Timestamp deletedAt = doc.getTimestamp("deletedAt");

        String dataFormatada = formatarData(dateTimestamp);

        RegistroFinanceiro registro = new RegistroFinanceiro(
                id,
                tipo,
                categoria,
                meio,
                dataFormatada,
                obs,
                valor,
                obterIconeCategoria(categoria)
        );

        registro.dateTimestamp = dateTimestamp;
        registro.deletedAt = deletedAt;
        registro.editado = false;

        return registro;
    }

    private void aplicarFiltrosEBusca() {
        registrosFiltrados.clear();

        for (RegistroFinanceiro registro : registros) {
            boolean passouTipo = filtroTipoAtivo.equals("Todos") || registro.tipo.equalsIgnoreCase(mapearTipoFiltroParaBanco(filtroTipoAtivo));
            boolean passouMeio = filtroMeioAtivo.equals("Todos") || registro.meio.equalsIgnoreCase(mapearMeioUiParaBanco(filtroMeioAtivo));
            boolean passouCategoria = filtroCategoriaAtiva.equals("Todas") || registro.categoria.equalsIgnoreCase(filtroCategoriaAtiva);

            boolean passouBuscaTexto = buscaTextoAtiva.isEmpty()
                    || registro.observacao.toLowerCase().contains(buscaTextoAtiva.toLowerCase());

            boolean passouBuscaValor = buscaValorAtivo.isEmpty()
                    || String.valueOf((int) registro.valor).equals(buscaValorAtivo);

            if (passouTipo && passouMeio && passouCategoria && passouBuscaTexto && passouBuscaValor) {
                registrosFiltrados.add(registro);
            }
        }

        atualizarResumoFinanceiro();
        renderizarGrafico();
        renderizarLista();
        atualizarChipBusca();
    }

    private void atualizarResumoFinanceiro() {
        double totalEntradas = 0;
        double totalSaidas = 0;

        for (RegistroFinanceiro registro : registrosFiltrados) {
            if ("entrada".equals(registro.tipo)) {
                totalEntradas += registro.valor;
            } else if ("saida".equals(registro.tipo)) {
                totalSaidas += registro.valor;
            }
        }

        txtEntradasValor.setText(formatarMoeda(totalEntradas));
        txtSaidasValor.setText(formatarMoeda(totalSaidas));
        txtSaldoValor.setText(formatarMoeda(totalEntradas - totalSaidas));
    }

    private void renderizarGrafico() {
        if (pieChartGestor == null) return;

        Map<String, Double> categorias = new LinkedHashMap<>();

        for (RegistroFinanceiro registro : registrosFiltrados) {
            double acumulado = categorias.containsKey(registro.categoria) ? categorias.get(registro.categoria) : 0.0;
            categorias.put(registro.categoria, acumulado + registro.valor);
        }

        if (categorias.isEmpty()) {
            pieChartGestor.clear();
            pieChartGestor.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> item : categorias.entrySet()) {
            entries.add(new PieEntry(item.getValue().floatValue(), item.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(0f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#D8B15A"));
        colors.add(Color.parseColor("#3E2F7D"));
        colors.add(Color.parseColor("#8E245C"));
        colors.add(Color.parseColor("#0D5E9E"));
        colors.add(Color.parseColor("#374046"));
        colors.add(Color.parseColor("#8A4D35"));
        colors.add(Color.parseColor("#00796B"));
        colors.add(Color.parseColor("#6D6F73"));
        colors.add(Color.parseColor("#B71C1C"));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChartGestor.setData(data);
        pieChartGestor.invalidate();
    }

    private void renderizarLista() {
        containerCategorias.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (RegistroFinanceiro registro : registrosFiltrados) {
            View item = inflater.inflate(R.layout.item_resumo_categoria, containerCategorias, false);

            TextView txtIcone = item.findViewById(R.id.txtIconeCategoriaItem);
            TextView txtTitulo = item.findViewById(R.id.txtTituloCategoriaItem);
            TextView txtTipo = item.findViewById(R.id.txtTipoItem);
            TextView txtDescricao = item.findViewById(R.id.txtDescricaoCategoriaItem);
            TextView txtValor = item.findViewById(R.id.txtValorCategoriaItem);
            TextView btnEditar = item.findViewById(R.id.btnEditarItem);
            TextView btnExcluir = item.findViewById(R.id.btnExcluirItem);
            View viewEditadoDot = item.findViewById(R.id.viewEditadoDot);

            txtIcone.setText(registro.icone);
            txtTitulo.setText(registro.categoria);
            txtTipo.setText(capitalizarTipoUi(registro.tipo));
            txtDescricao.setText("• " + capitalizarMeioUi(registro.meio) + " • " + registro.data + " • " + registro.observacao);
            viewEditadoDot.setVisibility(registro.editado ? View.VISIBLE : View.GONE);

            if ("entrada".equals(registro.tipo)) {
                txtValor.setText("+ " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_green));
            } else {
                txtValor.setText("- " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_danger));
            }

            btnEditar.setOnClickListener(v -> abrirConfirmacaoEdicao(registro));
            btnExcluir.setOnClickListener(v -> abrirConfirmacaoExclusao(registro));

            containerCategorias.addView(item);
        }
    }

    private void abrirDialogMovimentacao(RegistroFinanceiro registroEdicao, String tipoInicial) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_movimentacao, null);

        TextView txtTitulo = dialogView.findViewById(R.id.txtTituloDialogMovimentacao);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipoMovimentacao);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoriaMovimentacao);
        Spinner spinnerMeio = dialogView.findViewById(R.id.spinnerMeioMovimentacao);
        EditText edtData = dialogView.findViewById(R.id.edtDataMovimentacao);
        EditText edtValor = dialogView.findViewById(R.id.edtValorMovimentacao);
        EditText edtObs = dialogView.findViewById(R.id.edtObsMovimentacao);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarDialogMovimentacao);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvarDialogMovimentacao);

        String[] tiposUi = {"Entrada", "Saída"};
        String[] categorias = {"Salário", "Depósitos", "Moradia", "Mercado", "Transporte", "Entretenimento", "Saúde", "Contas", "Outros"};
        String[] meiosUi = {"Dinheiro", "Pix", "Débito", "Crédito", "Boleto", "Outro"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposUi);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        ArrayAdapter<String> adapterMeio = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, meiosUi);
        adapterMeio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeio.setAdapter(adapterMeio);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        boolean modoEdicao = registroEdicao != null;

        txtTitulo.setText(modoEdicao
                ? "Editar movimentação"
                : ("entrada".equals(tipoInicial) ? "Nova receita" : "Nova despesa"));

        if (modoEdicao) {
            selecionarSpinner(spinnerTipo, "entrada".equals(registroEdicao.tipo) ? "Entrada" : "Saída");
            selecionarSpinner(spinnerCategoria, registroEdicao.categoria);
            selecionarSpinner(spinnerMeio, capitalizarMeioUi(registroEdicao.meio));
            edtData.setText(registroEdicao.data);
            edtValor.setText(String.valueOf((int) registroEdicao.valor));
            edtObs.setText(registroEdicao.observacao);
        } else {
            selecionarSpinner(spinnerTipo, "entrada".equals(tipoInicial) ? "Entrada" : "Saída");
            edtData.setText(formatarDataCurta(new Date()));
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnSalvar.setOnClickListener(v -> {
            String tipoUi = spinnerTipo.getSelectedItem().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String meioUi = spinnerMeio.getSelectedItem().toString();
            String dataTexto = edtData.getText().toString().trim();
            String valorTexto = edtValor.getText().toString().trim();
            String obs = edtObs.getText().toString().trim();

            if (TextUtils.isEmpty(dataTexto) || TextUtils.isEmpty(valorTexto) || TextUtils.isEmpty(obs)) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            Date dataConvertida = converterTextoParaData(dataTexto);
            if (dataConvertida == null) {
                Toast.makeText(requireContext(), "Data inválida. Use o formato dd/MM.", Toast.LENGTH_SHORT).show();
                return;
            }

            double valor = Double.parseDouble(valorTexto);
            String tipoBanco = "Entrada".equalsIgnoreCase(tipoUi) ? "entrada" : "saida";
            String meioBanco = mapearMeioUiParaBanco(meioUi);
            String monthKey = formatarMonthKey(dataConvertida);

            if (modoEdicao) {
                atualizarMovimentacaoNoFirebase(registroEdicao.id, tipoBanco, categoria, meioBanco, dataConvertida, monthKey, valor, obs, dialog);
            } else {
                criarMovimentacaoNoFirebase(tipoBanco, categoria, meioBanco, dataConvertida, monthKey, valor, obs, dialog);
            }
        });

        dialog.show();
    }

    private void criarMovimentacaoNoFirebase(String tipo,
                                             String categoria,
                                             String meio,
                                             Date data,
                                             String monthKey,
                                             double valor,
                                             String obs,
                                             AlertDialog dialog) {

        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) return;

        String uid = usuarioAtual.getUid();

        Map<String, Object> novaMovimentacao = new LinkedHashMap<>();
        novaMovimentacao.put("uid", uid);
        novaMovimentacao.put("tipo", tipo);
        novaMovimentacao.put("categoria", categoria);
        novaMovimentacao.put("meio", meio);
        novaMovimentacao.put("date", new Timestamp(data));
        novaMovimentacao.put("monthKey", monthKey);
        novaMovimentacao.put("valor", valor);
        novaMovimentacao.put("obs", obs);
        novaMovimentacao.put("deletedAt", null);
        novaMovimentacao.put("createdAt", FieldValue.serverTimestamp());
        novaMovimentacao.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .add(novaMovimentacao)
                .addOnSuccessListener(documentReference -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Movimentação criada com sucesso.", Toast.LENGTH_SHORT).show();
                    carregarTransacoesDoFirebase();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao criar movimentação: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void atualizarMovimentacaoNoFirebase(String id,
                                                 String tipo,
                                                 String categoria,
                                                 String meio,
                                                 Date data,
                                                 String monthKey,
                                                 double valor,
                                                 String obs,
                                                 AlertDialog dialog) {

        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) return;

        String uid = usuarioAtual.getUid();

        Map<String, Object> atualizacao = new LinkedHashMap<>();
        atualizacao.put("tipo", tipo);
        atualizacao.put("categoria", categoria);
        atualizacao.put("meio", meio);
        atualizacao.put("date", new Timestamp(data));
        atualizacao.put("monthKey", monthKey);
        atualizacao.put("valor", valor);
        atualizacao.put("obs", obs);
        atualizacao.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .document(id)
                .update(atualizacao)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Movimentação atualizada com sucesso.", Toast.LENGTH_SHORT).show();
                    carregarTransacoesDoFirebase();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao editar movimentação: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void abrirConfirmacaoEdicao(RegistroFinanceiro registro) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Editar movimentação")
                .setMessage("Deseja editar esta movimentação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Editar", (dialog, which) -> abrirDialogMovimentacao(registro, registro.tipo))
                .show();
    }

    private void abrirConfirmacaoExclusao(RegistroFinanceiro registro) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar exclusão")
                .setMessage("Deseja realmente enviar esta movimentação para a lixeira?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> enviarParaLixeira(registro))
                .show();
    }

    private void enviarParaLixeira(RegistroFinanceiro registro) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) return;

        String uid = usuarioAtual.getUid();

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .document(registro.id)
                .update(
                        "deletedAt", FieldValue.serverTimestamp(),
                        "updatedAt", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Movimentação enviada para a lixeira.", Toast.LENGTH_SHORT).show();
                    carregarTransacoesDoFirebase();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao excluir movimentação: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void abrirDialogFiltro() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filtro, null);

        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerFiltroTipo);
        Spinner spinnerMeio = dialogView.findViewById(R.id.spinnerFiltroMeio);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerFiltroCategoria);
        Button btnFechar = dialogView.findViewById(R.id.btnFecharDialogFiltro);
        Button btnAplicar = dialogView.findViewById(R.id.btnAplicarDialogFiltro);

        String[] tiposUi = {"Todos", "Entrada", "Saída"};
        String[] meiosUi = {"Todos", "Dinheiro", "Pix", "Débito", "Crédito", "Boleto", "Outro"};
        String[] categorias = {"Todas", "Salário", "Depósitos", "Moradia", "Mercado", "Transporte", "Entretenimento", "Saúde", "Contas", "Outros"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposUi);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        ArrayAdapter<String> adapterMeio = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, meiosUi);
        adapterMeio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeio.setAdapter(adapterMeio);

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        selecionarSpinner(spinnerTipo, filtroTipoAtivo);
        selecionarSpinner(spinnerMeio, filtroMeioAtivo);
        selecionarSpinner(spinnerCategoria, filtroCategoriaAtiva);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnFechar.setOnClickListener(v -> dialog.dismiss());

        btnAplicar.setOnClickListener(v -> {
            filtroTipoAtivo = spinnerTipo.getSelectedItem().toString();
            filtroMeioAtivo = spinnerMeio.getSelectedItem().toString();
            filtroCategoriaAtiva = spinnerCategoria.getSelectedItem().toString();
            aplicarFiltrosEBusca();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void abrirDialogBusca() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_busca, null);

        EditText edtBuscaTexto = dialogView.findViewById(R.id.edtBuscaTexto);
        EditText edtBuscaValor = dialogView.findViewById(R.id.edtBuscaValor);
        Button btnFechar = dialogView.findViewById(R.id.btnFecharDialogBusca);
        Button btnAplicar = dialogView.findViewById(R.id.btnAplicarDialogBusca);

        edtBuscaTexto.setText(buscaTextoAtiva);
        edtBuscaValor.setText(buscaValorAtivo);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnFechar.setOnClickListener(v -> dialog.dismiss());

        btnAplicar.setOnClickListener(v -> {
            buscaTextoAtiva = edtBuscaTexto.getText().toString().trim();
            buscaValorAtivo = edtBuscaValor.getText().toString().trim();
            aplicarFiltrosEBusca();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void abrirDialogLixeira() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lixeira, null);

        LinearLayout containerLixeiraDialog = dialogView.findViewById(R.id.containerLixeiraDialog);
        Button btnFechar = dialogView.findViewById(R.id.btnFecharDialogLixeira);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        renderizarLixeira(containerLixeiraDialog, dialog);

        btnFechar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void renderizarLixeira(LinearLayout containerLixeiraDialog, AlertDialog dialogPai) {
        containerLixeiraDialog.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (RegistroFinanceiro registro : registrosLixeira) {
            View item = inflater.inflate(R.layout.item_resumo_categoria, containerLixeiraDialog, false);

            TextView txtIcone = item.findViewById(R.id.txtIconeCategoriaItem);
            TextView txtTitulo = item.findViewById(R.id.txtTituloCategoriaItem);
            TextView txtTipo = item.findViewById(R.id.txtTipoItem);
            TextView txtDescricao = item.findViewById(R.id.txtDescricaoCategoriaItem);
            TextView txtValor = item.findViewById(R.id.txtValorCategoriaItem);
            TextView btnEditar = item.findViewById(R.id.btnEditarItem);
            TextView btnExcluir = item.findViewById(R.id.btnExcluirItem);

            txtIcone.setText(registro.icone);
            txtTitulo.setText(registro.categoria);
            txtTipo.setText("Lixeira");
            txtDescricao.setText("• " + capitalizarMeioUi(registro.meio) + " • " + registro.data + " • " + registro.observacao);

            if ("entrada".equals(registro.tipo)) {
                txtValor.setText("+ " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_green));
            } else {
                txtValor.setText("- " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_danger));
            }

            btnEditar.setText("Recuperar");
            btnExcluir.setVisibility(View.GONE);

            btnEditar.setOnClickListener(v -> abrirConfirmacaoRecuperacao(registro, dialogPai));

            containerLixeiraDialog.addView(item);
        }
    }

    private void abrirConfirmacaoRecuperacao(RegistroFinanceiro registro, AlertDialog dialogPai) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Recuperar movimentação")
                .setMessage("Deseja recuperar esta movimentação da lixeira?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Recuperar", (dialog, which) -> recuperarDaLixeira(registro, dialogPai))
                .show();
    }

    private void recuperarDaLixeira(RegistroFinanceiro registro, AlertDialog dialogPai) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) return;

        String uid = usuarioAtual.getUid();

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .document(registro.id)
                .update(
                        "deletedAt", null,
                        "updatedAt", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {
                    dialogPai.dismiss();
                    Toast.makeText(requireContext(), "Movimentação recuperada com sucesso.", Toast.LENGTH_SHORT).show();
                    carregarTransacoesDoFirebase();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erro ao recuperar movimentação: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void atualizarChipBusca() {
        List<String> partes = new ArrayList<>();

        if (!filtroTipoAtivo.equals("Todos")) partes.add("Tipo: " + filtroTipoAtivo);
        if (!filtroMeioAtivo.equals("Todos")) partes.add("Meio: " + filtroMeioAtivo);
        if (!filtroCategoriaAtiva.equals("Todas")) partes.add("Categoria: " + filtroCategoriaAtiva);
        if (!buscaTextoAtiva.isEmpty()) partes.add("Texto: " + buscaTextoAtiva);
        if (!buscaValorAtivo.isEmpty()) partes.add("Valor: " + buscaValorAtivo);

        if (partes.isEmpty()) {
            layoutBuscaAtiva.setVisibility(View.GONE);
        } else {
            layoutBuscaAtiva.setVisibility(View.VISIBLE);
            txtBuscaAtiva.setText(unirPartes(partes));
        }
    }

    private String unirPartes(List<String> partes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.size(); i++) {
            sb.append(partes.get(i));
            if (i < partes.size() - 1) {
                sb.append(" • ");
            }
        }
        return sb.toString();
    }

    private void limparBuscaEFiltro() {
        filtroTipoAtivo = "Todos";
        filtroMeioAtivo = "Todos";
        filtroCategoriaAtiva = "Todas";
        buscaTextoAtiva = "";
        buscaValorAtivo = "";

        aplicarFiltrosEBusca();
        Toast.makeText(requireContext(), "Busca e filtros limpos.", Toast.LENGTH_SHORT).show();
    }

    private void selecionarSpinner(Spinner spinner, String valor) {
        if (valor == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String mapearTipoFiltroParaBanco(String tipoUi) {
        if ("Entrada".equalsIgnoreCase(tipoUi)) return "entrada";
        if ("Saída".equalsIgnoreCase(tipoUi)) return "saida";
        return tipoUi;
    }

    private String mapearMeioUiParaBanco(String meioUi) {
        switch (meioUi.toLowerCase()) {
            case "dinheiro":
                return "dinheiro";
            case "pix":
                return "pix";
            case "débito":
            case "debito":
                return "debito";
            case "crédito":
            case "credito":
                return "credito";
            case "boleto":
                return "boleto";
            default:
                return "outro";
        }
    }

    private String capitalizarTipoUi(String tipoBanco) {
        if ("entrada".equals(tipoBanco)) return "Receita";
        if ("saida".equals(tipoBanco)) return "Despesa";
        return capitalizar(tipoBanco);
    }

    private String capitalizarMeioUi(String meioBanco) {
        switch (meioBanco) {
            case "dinheiro":
                return "Dinheiro";
            case "pix":
                return "Pix";
            case "debito":
                return "Débito";
            case "credito":
                return "Crédito";
            case "boleto":
                return "Boleto";
            default:
                return "Outro";
        }
    }

    private String obterIconeCategoria(String categoria) {
        switch (categoria) {
            case "Salário":
                return "💰";
            case "Depósitos":
                return "🏦";
            case "Moradia":
                return "🏠";
            case "Mercado":
                return "🛒";
            case "Transporte":
                return "🚗";
            case "Entretenimento":
                return "🍿";
            case "Saúde":
                return "💊";
            case "Contas":
                return "🧾";
            default:
                return "🪙";
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    private String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private String formatarData(Timestamp timestamp) {
        if (timestamp == null) return "--/--";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", new Locale("pt", "BR"));
        return sdf.format(timestamp.toDate());
    }

    private String formatarDataCurta(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", new Locale("pt", "BR"));
        return sdf.format(date);
    }

    private String formatarMonthKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(date);
    }

    private Date converterTextoParaData(String texto) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            Calendar cal = Calendar.getInstance();
            int anoAtualSelecionado = calendarioAtual.get(Calendar.YEAR);
            return sdf.parse(texto + "/" + anoAtualSelecionado);
        } catch (Exception e) {
            return null;
        }
    }

    private String valorOuPadrao(String valor, String padrao) {
        if (valor == null || valor.trim().isEmpty()) return padrao;
        return valor;
    }

    private static class RegistroFinanceiro {
        String id;
        String tipo;
        String categoria;
        String meio;
        String data;
        String observacao;
        String icone;
        double valor;
        boolean editado;
        Timestamp dateTimestamp;
        Timestamp deletedAt;

        RegistroFinanceiro(String id, String tipo, String categoria, String meio, String data, String observacao, double valor, String icone) {
            this.id = id;
            this.tipo = tipo;
            this.categoria = categoria;
            this.meio = meio;
            this.data = data;
            this.observacao = observacao;
            this.valor = valor;
            this.icone = icone;
            this.editado = false;
        }
    }
}