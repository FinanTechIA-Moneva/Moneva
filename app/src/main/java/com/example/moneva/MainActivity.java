package com.example.moneva;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.moneva.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajuste de padding para as barras do sistema (status bar/navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Carrega o fragmento inicial apenas na primeira criação
        if (savedInstanceState == null) {
            substituirFragment(new HomeFragment(), false);
        }
    }

    /**

     Metodo central para trocar de fragmentos.
     @param fragment O novo fragmento a ser exibido.
     @param adicionarNaPilha Se true, permite voltar ao fragmento anterior com o botão "voltar".*/
    public void substituirFragment(Fragment fragment, boolean adicionarNaPilha) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment);

        if (adicionarNaPilha) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}