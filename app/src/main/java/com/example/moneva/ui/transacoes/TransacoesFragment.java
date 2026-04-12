package com.example.moneva.ui.transacoes;

import android.app.AlertDialog;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moneva.MainActivity; // Importante para a navegação
import com.example.moneva.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransacoesFragment extends Fragment {

    // Views do Cabeçalho e Resumo
    private TextView txtMesAtualTransacoes, txtEntradasValor, txtSaidasValor, txtSaldoValor;
    private TextView btnMesAnterior, btnMesProximo, btnFiltro, btnBusca, btnLixeira;

    // Views de Ação (Botões flutuantes e fixos)
    private TextView btnNovaDespesaFixo, btnNovaReceitaFixo, btnNovaDespesaFlutuante, btnNovaReceitaFlutuante;
    private LinearLayout containerCategorias, layoutAcoesFixas, layoutAcoesFlutuantes, layoutBuscaAtiva;
    private TextView txtBuscaAtiva, btnLimparBusca;
    private ScrollView scrollGestor;

    // Navegação (Barra inferior, se existir no XML deste fragmento)
    private View navHome, navGestor, navTrilha, navMenu;

    private final Calendar calendarioAtual = Calendar.getInstance();
    private final List<RegistroFinanceiro> registros = new ArrayList<>();
    private final List<RegistroFinanceiro> registrosLixeira = new ArrayList<>();
    private final List<RegistroFinanceiro> registrosFiltrados = new ArrayList<>();

    private String filtroTipoAtivo = "Todos", filtroMeioAtivo = "Todos", filtroCategoriaAtiva = "Todas";
    private String buscaTextoAtiva = "", buscaValorAtivo = "";

    public TransacoesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transacoes, container, false);

        inicializarViews(view);
        carregarDadosMock();
        configurarEventos();
        atualizarCabecalhoMes();
        aplicarFiltrosEBusca();
        configurarScrollAcoesFlutuantes();

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

        // Views de navegação (IDs devem existir no XML para não dar erro)
        navHome = view.findViewById(R.id.navHome);
        navGestor = view.findViewById(R.id.navGestor);
        navTrilha = view.findViewById(R.id.navTrilha);
        navMenu = view.findViewById(R.id.navMenu);
    }

    private void configurarEventos() {
        // Navegação (Check de segurança para evitar NullPointerException)
        if (navHome != null) navHome.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.home.HomeFragment()));
        if (navGestor != null) navGestor.setOnClickListener(v -> Toast.makeText(getContext(), "Você já está no Gestor", Toast.LENGTH_SHORT).show());
        if (navTrilha != null) navTrilha.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.trilha.TrilhaFragment()));
        if (navMenu != null) navMenu.setOnClickListener(v -> navegarPara(new com.example.moneva.ui.menu.MenuFragment()));

        // Eventos do Mês
        if (btnMesAnterior != null) btnMesAnterior.setOnClickListener(v -> { calendarioAtual.add(Calendar.MONTH, -1); atualizarCabecalhoMes(); });
        if (btnMesProximo != null) btnMesProximo.setOnClickListener(v -> { calendarioAtual.add(Calendar.MONTH, 1); atualizarCabecalhoMes(); });

        // Movimentações
        if (btnNovaDespesaFixo != null) btnNovaDespesaFixo.setOnClickListener(v -> abrirDialogMovimentacao(null, "despesa"));
        if (btnNovaReceitaFixo != null) btnNovaReceitaFixo.setOnClickListener(v -> abrirDialogMovimentacao(null, "receita"));
        if (btnNovaDespesaFlutuante != null) btnNovaDespesaFlutuante.setOnClickListener(v -> abrirDialogMovimentacao(null, "despesa"));
        if (btnNovaReceitaFlutuante != null) btnNovaReceitaFlutuante.setOnClickListener(v -> abrirDialogMovimentacao(null, "receita"));

        // Filtros e Busca
        if (btnFiltro != null) btnFiltro.setOnClickListener(v -> abrirDialogFiltro());
        if (btnBusca != null) btnBusca.setOnClickListener(v -> abrirDialogBusca());
        if (btnLixeira != null) btnLixeira.setOnClickListener(v -> abrirDialogLixeira());
        if (btnLimparBusca != null) btnLimparBusca.setOnClickListener(v -> limparBuscaEFiltro());
    }

    // MÉTODO DE NAVEGAÇÃO CENTRALIZADO
    private void navegarPara(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).substituirFragment(fragment, true);
        }
    }

    private void configurarScrollAcoesFlutuantes() {
        if (scrollGestor == null) return;
        scrollGestor.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollGestor.getScrollY();
            if (layoutAcoesFlutuantes != null && layoutAcoesFixas != null) {
                if (scrollY > 420) {
                    layoutAcoesFlutuantes.setVisibility(View.VISIBLE);
                    layoutAcoesFixas.setVisibility(View.INVISIBLE);
                } else {
                    layoutAcoesFlutuantes.setVisibility(View.GONE);
                    layoutAcoesFixas.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void atualizarCabecalhoMes() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
        String mes = sdf.format(calendarioAtual.getTime());
        if (!mes.isEmpty()) mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        if (txtMesAtualTransacoes != null) txtMesAtualTransacoes.setText(mes);
    }

    private void carregarDadosMock() {
        registros.clear();
        registros.add(new RegistroFinanceiro("receita", "Salário", "Dinheiro", "17/03", "pagamento mensal", 3500, "💰"));
        registros.add(new RegistroFinanceiro("despesa", "Moradia", "Débito", "20/03", "internet e água", 350, "🏠"));
        registros.add(new RegistroFinanceiro("despesa", "Transporte", "Boleto", "17/03", "combustível", 350, "🚗"));
        registros.add(new RegistroFinanceiro("despesa", "Mercado", "Boleto", "17/03", "compras do mês", 300, "🛒"));
        registros.add(new RegistroFinanceiro("despesa", "Moradia", "Dinheiro", "17/03", "aluguel", 900, "🏠"));
    }


    private void aplicarFiltrosEBusca() {
        registrosFiltrados.clear();
        for (RegistroFinanceiro registro : registros) {
            boolean passouTipo = filtroTipoAtivo.equals("Todos") || registro.tipo.equalsIgnoreCase(filtroTipoAtivo);
            boolean passouMeio = filtroMeioAtivo.equals("Todos") || registro.meio.equalsIgnoreCase(filtroMeioAtivo);
            boolean passouCategoria = filtroCategoriaAtiva.equals("Todas") || registro.categoria.equalsIgnoreCase(filtroCategoriaAtiva);
            boolean passouBuscaTexto = buscaTextoAtiva.isEmpty() || registro.observacao.toLowerCase().contains(buscaTextoAtiva.toLowerCase());
            boolean passouBuscaValor = buscaValorAtivo.isEmpty() || String.valueOf((int) registro.valor).equals(buscaValorAtivo);

            if (passouTipo && passouMeio && passouCategoria && passouBuscaTexto && passouBuscaValor) {
                registrosFiltrados.add(registro);
            }
        }
        atualizarResumoFinanceiro();
        renderizarLista();
        atualizarChipBusca();
    }

    private void atualizarResumoFinanceiro() {
        double totalEntradas = 0, totalSaidas = 0;
        for (RegistroFinanceiro r : registrosFiltrados) {
            if ("receita".equals(r.tipo)) totalEntradas += r.valor;
            else totalSaidas += r.valor;
        }
        if (txtEntradasValor != null) txtEntradasValor.setText(formatarMoeda(totalEntradas));
        if (txtSaidasValor != null) txtSaidasValor.setText(formatarMoeda(totalSaidas));
        if (txtSaldoValor != null) txtSaldoValor.setText(formatarMoeda(totalEntradas - totalSaidas));
    }

    private void renderizarLista() {
        if (containerCategorias == null) return;
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
            txtTipo.setText(capitalizar(registro.tipo));
            txtDescricao.setText("• " + registro.meio + " • " + registro.data + " • " + registro.observacao);
            viewEditadoDot.setVisibility(registro.editado ? View.VISIBLE : View.GONE);

            if ("receita".equals(registro.tipo)) {
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

        String[] tipos = {"receita", "despesa"};
        String[] categorias = {"Salário", "Depósitos", "Moradia", "Mercado", "Transporte", "Entretenimento", "Saúde", "Contas", "Outros"};
        String[] meios = {"Dinheiro", "Pix", "Débito", "Crédito", "Boleto", "Outros"};

        spinnerTipo.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tipos));
        spinnerCategoria.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias));
        spinnerMeio.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, meios));

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        boolean modoEdicao = registroEdicao != null;

        txtTitulo.setText(modoEdicao ? "Editar movimentação" : ("receita".equals(tipoInicial) ? "Nova receita" : "Nova despesa"));

        if (modoEdicao) {
            selecionarSpinner(spinnerTipo, registroEdicao.tipo);
            selecionarSpinner(spinnerCategoria, registroEdicao.categoria);
            selecionarSpinner(spinnerMeio, registroEdicao.meio);
            edtData.setText(registroEdicao.data);
            edtValor.setText(String.valueOf((int) registroEdicao.valor));
            edtObs.setText(registroEdicao.observacao);
        } else {
            selecionarSpinner(spinnerTipo, tipoInicial);
            edtData.setText("17/03");
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnSalvar.setOnClickListener(v -> {
            String tipo = spinnerTipo.getSelectedItem().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String meio = spinnerMeio.getSelectedItem().toString();
            String data = edtData.getText().toString().trim();
            String valorTexto = edtValor.getText().toString().trim();
            String obs = edtObs.getText().toString().trim();

            if (data.isEmpty() || valorTexto.isEmpty() || obs.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            double valor = Double.parseDouble(valorTexto);
            String icone = obterIconeCategoria(categoria);

            if (modoEdicao) {
                registroEdicao.tipo = tipo;
                registroEdicao.categoria = categoria;
                registroEdicao.meio = meio;
                registroEdicao.data = data;
                registroEdicao.observacao = obs;
                registroEdicao.valor = valor;
                registroEdicao.icone = icone;
                registroEdicao.editado = true;
            } else {
                registros.add(0, new RegistroFinanceiro(tipo, categoria, meio, data, obs, valor, icone));
            }

            aplicarFiltrosEBusca();
            dialog.dismiss();
        });

        dialog.show();
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
                .setPositiveButton("Excluir", (dialog, which) -> {
                    registros.remove(registro);
                    registrosLixeira.add(0, registro);
                    aplicarFiltrosEBusca();
                    Toast.makeText(requireContext(), "Movimentação enviada para a lixeira.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogFiltro() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filtro, null);

        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerFiltroTipo);
        Spinner spinnerMeio = dialogView.findViewById(R.id.spinnerFiltroMeio);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerFiltroCategoria);
        Button btnFechar = dialogView.findViewById(R.id.btnFecharDialogFiltro);
        Button btnAplicar = dialogView.findViewById(R.id.btnAplicarDialogFiltro);

        spinnerTipo.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Todos", "receita", "despesa"}));
        spinnerMeio.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Todos", "Dinheiro", "Pix", "Débito", "Crédito", "Boleto", "Outros"}));
        spinnerCategoria.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Todas", "Salário", "Depósitos", "Moradia", "Mercado", "Transporte", "Entretenimento", "Saúde", "Contas", "Outros"}));

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

        LinearLayout containerLixeira = dialogView.findViewById(R.id.containerLixeiraDialog);
        Button btnFechar = dialogView.findViewById(R.id.btnFecharDialogLixeira);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        renderizarLixeira(containerLixeira, dialog);
        btnFechar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void renderizarLixeira(LinearLayout containerLixeira, AlertDialog dialogPai) {
        containerLixeira.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (RegistroFinanceiro registro : registrosLixeira) {
            View item = inflater.inflate(R.layout.item_resumo_categoria, containerLixeira, false);

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
            txtDescricao.setText("• " + registro.meio + " • " + registro.data + " • " + registro.observacao);

            if ("receita".equals(registro.tipo)) {
                txtValor.setText("+ " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_green));
            } else {
                txtValor.setText("- " + formatarMoeda(registro.valor));
                txtValor.setTextColor(ContextCompat.getColor(requireContext(), R.color.moneva_danger));
            }

            btnEditar.setText("Recuperar");
            btnExcluir.setVisibility(View.GONE);

            btnEditar.setOnClickListener(v -> abrirConfirmacaoRecuperacao(registro, dialogPai));

            containerLixeira.addView(item);
        }
    }

    private void abrirConfirmacaoRecuperacao(RegistroFinanceiro registro, AlertDialog dialogPai) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Recuperar movimentação")
                .setMessage("Deseja recuperar esta movimentação da lixeira?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Recuperar", (dialog, which) -> {
                    registrosLixeira.remove(registro);
                    registros.add(0, registro);
                    aplicarFiltrosEBusca();
                    dialogPai.dismiss();
                    Toast.makeText(requireContext(), "Movimentação recuperada com sucesso.", Toast.LENGTH_SHORT).show();
                })
                .show();
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
        filtroTipoAtivo = "Todos"; filtroMeioAtivo = "Todos"; filtroCategoriaAtiva = "Todas";
        buscaTextoAtiva = ""; buscaValorAtivo = "";
        aplicarFiltrosEBusca();
        Toast.makeText(requireContext(), "Filtros limpos", Toast.LENGTH_SHORT).show();
    }

    private void atualizarChipBusca() {
        if (layoutBuscaAtiva == null) return;
        List<String> partes = new ArrayList<>();
        if (!filtroTipoAtivo.equals("Todos")) partes.add(filtroTipoAtivo);
        if (!filtroMeioAtivo.equals("Todos")) partes.add(filtroMeioAtivo);
        if (!buscaTextoAtiva.isEmpty()) partes.add(buscaTextoAtiva);

        if (partes.isEmpty()) {
            layoutBuscaAtiva.setVisibility(View.GONE);
        } else {
            layoutBuscaAtiva.setVisibility(View.VISIBLE);
            txtBuscaAtiva.setText(String.join(" • ", partes));
        }
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

    private static class RegistroFinanceiro {
        String tipo, categoria, meio, data, observacao, icone;
        double valor;
        boolean editado;

        RegistroFinanceiro(String tipo, String categoria, String meio, String data, String observacao, double valor, String icone) {
            this.tipo = tipo; this.categoria = categoria; this.meio = meio;
            this.data = data; this.observacao = observacao; this.valor = valor;
            this.icone = icone; this.editado = false;
        }
    }
}