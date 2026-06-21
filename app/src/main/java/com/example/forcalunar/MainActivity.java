package com.example.forcalunar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentResultListener;

public class MainActivity extends AppCompatActivity {

    private ImageView imgAvatarSelecionado;
    private int avatarEscolhidoId = android.R.drawable.star_big_on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            intent.putExtra("AVATAR_JOGADOR", avatarEscolhidoId);

            startActivity(intent);
        });

        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWordActivity.class);
            startActivity(intent);
        });

        // Seleção de avatar
        imgAvatarSelecionado = findViewById(R.id.imgAvatarSelecionado);

        // Configura o clique para abrir a seleção de avatar
        imgAvatarSelecionado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarSelectionFragment dialog = new AvatarSelectionFragment();
                dialog.show(getSupportFragmentManager(), "AvatarDialog");
            }
        });

        // Escuta o resultado enviado pelo Fragment
        getSupportFragmentManager().setFragmentResultListener("chaveAvatar", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Recupera o ID do drawable selecionado no Fragment
                avatarEscolhidoId = result.getInt("avatarResId");

                // Atualiza a imagem na tela de login
                if (avatarEscolhidoId != 0) {
                    imgAvatarSelecionado.setImageResource(avatarEscolhidoId);
                }
            }
        });



    }
}