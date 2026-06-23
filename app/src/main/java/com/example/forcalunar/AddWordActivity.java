package com.example.forcalunar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity responsável pelo cadastro de novas palavras no jogo.
 * Permite que o usuário insira uma palavra e o seu respectivo tema/categoria.
 */

public class AddWordActivity extends AppCompatActivity {

    private AudioManager audioManager;  // Gerenciador de áudio

    // ===================== DECLARAÇÃO DE VARIÁVEIS =====================
    private WordsDatabase wordsDatabase;
    private EditText editPalavra; // Campo de texto para digitar a palavra
    private EditText editTema; // Campo de texto para digitar o tema da palavra

    // ===================== CICLO DE VIDA DA ACTIVITY =====================

    /**
     * Chamado quando a Activity é criada.
     * Inicializa a interface e configura os eventos dos botões.
     * @param savedInstanceState Estado salvo anteriormente (se houver)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===================== INSTÂNCIA DO DATABASE =====================
        wordsDatabase = new WordsDatabase(this);

        // Define o layout XML que será exibido nesta tela
        setContentView(R.layout.activity_add_word);

        // ===== INICIA A MÚSICA DE FUNDO =====
        audioManager = new AudioManager();
        audioManager.tocarMusicaFundo(this);

        // ===================== VINCULAÇÃO DOS COMPONENTES =====================

        // Busca os componentes do layout pelos seus IDs e os associa às variáveis
        editPalavra = findViewById(R.id.editPalavra);
        editTema = findViewById(R.id.editTema);
        // Botão que salva a palavra cadastrada
        Button btnSalvar = findViewById(R.id.btnSalvar);
        // Botão para voltar à tela inicial
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        // ===================== CONFIGURAÇÃO DO BOTÃO VOLTAR =====================
        // Define o comportamento quando o botão "Voltar" for clicado
        btnVoltar.setOnClickListener(v -> {
            finish(); // Fecha a Activity e volta para a tela anterior
        });

        // ===================== CONFIGURAÇÃO DO BOTÃO SALVAR =====================

        // Define o comportamento quando o botão "Salvar" for clicado
        btnSalvar.setOnClickListener(v -> {
            // Obtém o texto digitado e remove espaços extras no início/fim
            String palavra = editPalavra.getText().toString().trim();
            String tema = editTema.getText().toString().trim();

            // ===================== VALIDAÇÃO DOS CAMPOS =====================
            // Verifica se algum campo está vazio
            if (palavra.isEmpty() || tema.isEmpty()) {
                // Exibe mensagem de erro usando Toast
                Toast.makeText(this,
                        "Preencha palavra e tema",
                        Toast.LENGTH_SHORT).show();
            } else {
                // ===================== SALVAMENTO DA PALAVRA =====================
                boolean salvouComSucesso = wordsDatabase.inserirNovaPalavra(palavra, tema);

                if(salvouComSucesso)
                {
                    Toast.makeText(this,
                            "Palavra cadastrada: " + palavra,
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this,
                            "Erro ao cadastrar a palavra: " + palavra,
                            Toast.LENGTH_SHORT).show();
                }


                // ===================== LIMPEZA DOS CAMPOS =====================
                // Após cadastrar, limpa os campos para facilitar novo cadastro
                editPalavra.setText("");
                editTema.setText("");
            }
        });
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