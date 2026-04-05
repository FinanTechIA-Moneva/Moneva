package com.example.moneva.ui.questionario;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneva.R;

public class QuestionarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionario);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.questionario_container, new QuestionarioFragment())
                    .commit();
        }
    }
}