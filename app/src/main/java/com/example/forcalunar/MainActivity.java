package com.example.forcalunar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;  // Gerenciador de áudio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ===== INICIA A MÚSICA DE FUNDO =====
        audioManager = new AudioManager();
        audioManager.tocarMusicaFundo(this);

        EditText editNick = findViewById(R.id.editNick);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);

        btnIniciar.setOnClickListener(v -> {
            String nick = editNick.getText().toString().trim();
            // Valida se o nick não está vazio
            if (nick.isEmpty()) {
                Toast.makeText(MainActivity.this, "Digite um nick para começar", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("nick", nick);
            startActivity(intent);
        });

        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa a música quando a tela não está visível
        if (audioManager != null) {
            audioManager.pausarMusica();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retoma a música quando a tela volta a ficar visível
        if (audioManager != null) {
            audioManager.retomarMusica();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para a música ao destruir a Activity
        if (audioManager != null) {
            audioManager.pararMusica();
        }
    }
}