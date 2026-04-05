package com.example.moneva.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneva.MainActivity;
import com.example.moneva.R;
import com.example.moneva.ui.questionario.QuestionarioActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button btnEntrar, btnCriarConta;
    private TextView tabEntrar, tabCriarConta;

    private LinearLayout layoutLogin, layoutCriarConta, layoutRodapeInfo;

    private TextView txtTituloPrincipal, txtSubtituloPrincipal, txtTituloPortal;
    private TextView txtEsqueceuSenha, txtEmojiInfo, txtDescricaoInfo;

    private EditText edtEmail, edtSenha;
    private EditText edtNomeCadastro, edtEmailCadastro, edtSenhaCadastro, edtConfirmarSenhaCadastro;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarComponentes();
        inicializarFirebase();
        configurarEventos();

        mostrarAbaLogin();
    }

    private void inicializarFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void inicializarComponentes() {
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);

        tabEntrar = findViewById(R.id.tabEntrar);
        tabCriarConta = findViewById(R.id.tabCriarConta);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutCriarConta = findViewById(R.id.layoutCriarConta);
        layoutRodapeInfo = findViewById(R.id.layoutRodapeInfo);

        txtTituloPrincipal = findViewById(R.id.txtTituloPrincipal);
        txtSubtituloPrincipal = findViewById(R.id.txtSubtituloPrincipal);
        txtTituloPortal = findViewById(R.id.txtTituloPortal);
        txtEsqueceuSenha = findViewById(R.id.txtEsqueceuSenha);
        txtEmojiInfo = findViewById(R.id.txtEmojiInfo);
        txtDescricaoInfo = findViewById(R.id.txtDescricaoInfo);

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        edtNomeCadastro = findViewById(R.id.edtNomeCadastro);
        edtEmailCadastro = findViewById(R.id.edtEmailCadastro);
        edtSenhaCadastro = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenhaCadastro = findViewById(R.id.edtConfirmarSenhaCadastro);
    }

    private void configurarEventos() {
        tabEntrar.setOnClickListener(v -> mostrarAbaLogin());
        tabCriarConta.setOnClickListener(v -> mostrarAbaCriarConta());

        btnEntrar.setOnClickListener(v -> realizarLogin());
        btnCriarConta.setOnClickListener(v -> criarConta());
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        verificarOnboardingERedirecionar();
                    } else {
                        Toast.makeText(this,
                                "Erro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarOnboardingERedirecionar() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::redirecionarAposLogin)
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erro ao verificar onboarding: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void redirecionarAposLogin(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Usuário não encontrado no banco.", Toast.LENGTH_SHORT).show();
            return;
        }

        Boolean completedOnboarding = doc.getBoolean("completedOnboarding");

        if (completedOnboarding != null && completedOnboarding) {
            Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            Toast.makeText(this, "Complete seu questionário inicial.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, QuestionarioActivity.class));
        }

        finish();
    }

    private void criarConta() {
        String nome = edtNomeCadastro.getText().toString().trim();
        String email = edtEmailCadastro.getText().toString().trim();
        String senha = edtSenhaCadastro.getText().toString().trim();
        String confirmarSenha = edtConfirmarSenhaCadastro.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCriarConta.setEnabled(false);
        btnCriarConta.setText("Criando...");

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser() == null) {
                            restaurarBotaoCriarConta();
                            Toast.makeText(this, "Erro ao obter usuário criado.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("displayName", nome);
                        user.put("email", email);
                        user.put("gender", "");
                        user.put("monthlyIncome", 0);
                        user.put("financialGoals", "");
                        user.put("dreams", "");
                        user.put("currentStatus", "");
                        user.put("futureVision5Years", "");
                        user.put("completedOnboarding", false);
                        user.put("createdAt", Timestamp.now());
                        user.put("updatedAt", Timestamp.now());

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, QuestionarioActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    restaurarBotaoCriarConta();
                                    Toast.makeText(this,
                                            "Erro ao salvar usuário: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {
                        restaurarBotaoCriarConta();
                        Toast.makeText(this,
                                "Erro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void restaurarBotaoCriarConta() {
        btnCriarConta.setEnabled(true);
        btnCriarConta.setText("Criar conta");
    }

    private void mostrarAbaLogin() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutCriarConta.setVisibility(View.GONE);

        tabEntrar.setBackgroundResource(R.drawable.bg_login_tab_selected);
        tabCriarConta.setBackgroundResource(R.drawable.bg_login_tab_unselected);

        txtTituloPrincipal.setText("Comece sua jornada financeira!");
        txtSubtituloPrincipal.setText("Da teoria à prática: sua evolução financeira em um só lugar.");
        txtTituloPortal.setText("Portal do Aluno");
        txtEmojiInfo.setText("💰");
        txtDescricaoInfo.setText("Além de conteúdos interativos, você conta com um gestor inteligente para aplicar o que aprendeu no dia a dia.");

        txtEsqueceuSenha.setVisibility(View.VISIBLE);
        layoutRodapeInfo.setVisibility(View.VISIBLE);
    }

    private void mostrarAbaCriarConta() {
        layoutLogin.setVisibility(View.GONE);
        layoutCriarConta.setVisibility(View.VISIBLE);

        tabEntrar.setBackgroundResource(R.drawable.bg_login_tab_unselected);
        tabCriarConta.setBackgroundResource(R.drawable.bg_login_tab_selected);

        txtTituloPrincipal.setText("Crie sua conta e evolua com o Moneva!");
        txtSubtituloPrincipal.setText("Organize sua vida financeira e aprenda na prática.");
        txtTituloPortal.setText("Portal do Aluno");
        txtEmojiInfo.setText("🐷");
        txtDescricaoInfo.setText("Cadastre-se para começar sua jornada com metas, conquistas e gestão financeira inteligente.");

        txtEsqueceuSenha.setVisibility(View.GONE);
        layoutRodapeInfo.setVisibility(View.VISIBLE);
    }
}