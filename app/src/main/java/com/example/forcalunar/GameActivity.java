package com.example.forcalunar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.UserDictionary;
import android.view.ViewGroup;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

/**
 * Activity principal do jogo da forca.
 * Gerencia a lógica do jogo, teclado virtual, timer e diálogos de resultado.
 */

public class GameActivity extends AppCompatActivity{

    // ================ Banco de Dados de palavras =================
    private WordsDatabase wordsDatabase;

    // ===================== SENSORES =====================
    private SensorsManager sensorManager;
    private AudioManager audioManager;      // Gerenciador de áudio

    // ===================== BANCO DE PALAVRAS =====================
    // Lista inicial de 10 palavras (requisito do projeto)
    private String[] palavras = {
            "LUA", "CAVALO", "MARTELO", "ESTRELA", "ROBÔ",
            "CAVEIRA", "FOGO", "DRAGÃO", "CAVERNA", "LANÇA"
    };

    // ===================== VARIÁVEIS DO JOGO =====================
    private String palavraSecretaOriginal;      // Palavra escolhida para a partida
    private StringBuilder palavraOculta;        // Palavra com _ para letras não reveladas
    private String categoriaDaPalavra;
    private int erros = 0;                      // Número de erros (max 6)
    private ArrayList<Character> letrasTentadas = new ArrayList<>(); // Letras já usadas
    private int pontuacao = 0;
    private int letrasAcertadas = 0;            // Conta letras acertadas na partida atual

    // ===================== COMPONENTES DA INTERFACE =====================
    private TextView txtPalavraOculta;          // Exibe a palavra com _
    private ImageView imgForca;                 // Imagem da forca
    private TextView txtTempo;                  // Exibe o tempo restante
    private ArrayList<Button> botoesTeclado = new ArrayList<>(); // Lista de botões do teclado
    private ImageButton btnVoltarGame;          // Botão para voltar à tela inicial
    private TextView txtPontuacao;              // TextView para exibir a pontuação
    private ImageButton btnAlternarTeclado;     // Botão para alternar entre teclados
    private EditText editTecladoNativo;         // Campo para o teclado nativo
    private TextView txtLetrasTentadas;         // Exibe as letras já tentadas (teclado nativo)
    private LinearLayout layoutTecladoNativo;   // Container do teclado nativo

    // ===================== CONTROLE DE TEMPO =====================
    private int tempoSegundos = 180;            // 3 minutos (180 segundos)
    private Handler timerHandler = new Handler(); // Gerencia o timer
    private Runnable timerRunnable;             // Ação executada a cada segundo

    // ===================== CONTROLE DE TECLADO =====================
    private boolean usandoTecladoVirtual = true;  // true = teclado virtual, false = teclado nativo

    // ===================== CONSTANTES DE PONTUAÇÃO =====================
    private static final int PONTOS_POR_LETRA = 100;      // 100 pontos por letra acertada
    private static final int BONUS_PALAVRA_COMPLETA = 500; // Bônus por palavra completa

    // ===================== CICLO DE VIDA DA ACTIVITY =====================

    /**
     * Chamado quando a Activity é criada.
     * Inicializa componentes e cria o teclado.
     * @param savedInstanceState Estado salvo anteriormente (se houver)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // ===== 1. RECUPERA OS DADOS DO JOGADOR =====
        // O nick e avatar foram passados pela MainActivity via Intent
        int avatarId = getIntent().getIntExtra("AVATAR_JOGADOR", android.R.drawable.star_big_on);
        // ===== INICIA A MÚSICA DE FUNDO =====
        audioManager = new AudioManager();
        audioManager.tocarMusicaFundo(this);

        // ===== 1. RECUPERA O NICK DO JOGADOR =====
        // O nick foi passado pela MainActivity via Intent
        String nick = getIntent().getStringExtra("nick");

        ImageView imgAvatarGame = findViewById(R.id.imgAvatarGame);
        TextView txtNick = findViewById(R.id.txtNick);

        txtNick.setText(getString(R.string.jogador_label) + nick);
        imgAvatarGame.setImageResource(avatarId);

        // ===== 2. VINCULA OS COMPONENTES DO LAYOUT =====
        txtTempo = findViewById(R.id.txtTempo);
        txtPalavraOculta = findViewById(R.id.txtPalavraOculta);
        imgForca = findViewById(R.id.imgForca);
        btnVoltarGame = findViewById(R.id.btnVoltarGame);
        txtPontuacao = findViewById(R.id.txtPontuacao);
        btnAlternarTeclado = findViewById(R.id.btnAlternarTeclado);
        editTecladoNativo = findViewById(R.id.editTecladoNativo);
        txtLetrasTentadas = findViewById(R.id.txtLetrasTentadas);
        layoutTecladoNativo = findViewById(R.id.layoutTecladoNativo);

        // Define descrição acessível para a imagem da forca
        imgForca.setContentDescription(getString(R.string.content_desc_forca, 0));

        // ===== 3. INICIALIZA O JOGO =====
        // Inicializa o banco de dados
        wordsDatabase = new WordsDatabase(this);

        // ===== 3. CONFIGURA O BOTÃO VOLTAR =====
        // Define o comportamento quando o botão "Voltar" for clicado
        btnVoltarGame.setOnClickListener(v -> {
            pararTimer();  // Para o timer antes de sair
            finish();      // Fecha a Activity e volta para a tela anterior
        });

        // ===== 4. CONFIGURA O BOTÃO ALTERNAR TECLADO =====
        // Define o comportamento quando o botão de alternar teclado for clicado
        btnAlternarTeclado.setOnClickListener(v -> alternarTeclado());

        // ===== 5. CONFIGURA O TECLADO NATIVO =====
        // O teclado nativo será ativado quando o usuário digitar no EditText
        configurarEntradaTecladoNativo();

        // ===== 6. INICIALIZA O JOGO =====
        iniciarJogo();

        // ===== 8. CRIA O TECLADO VIRTUAL =====
        criarTecladoVirtual();

        // ===== 6. Inicializa o gerenciador de sensores =====
        sensorManager = new SensorsManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Manda os sensores ligarem quando o usuário entra/volta pra tela
        if (sensorManager != null) {
            sensorManager.iniciarSensores();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desativa para economizar bateria ao sair da tela
        if (sensorManager != null) {
            sensorManager.pararSensores();
        }
    }

    /**
     * Configura a entrada do teclado nativo (do sistema).
     * Quando o usuário digita uma letra no campo editTecladoNativo,
     * ela é capturada e processada pelo jogo.
     */
    private void configurarEntradaTecladoNativo() {

        // Quando o EditText ganha foco, mostra o teclado nativo
        editTecladoNativo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !usandoTecladoVirtual) {
                showNativeKeyboard();
            }
        });

        editTecladoNativo.setOnEditorActionListener((v, actionId, event) -> {
            String texto = editTecladoNativo.getText().toString().trim();
            if (!texto.isEmpty()) {
                // Pega a última letra digitada
                char letra = texto.charAt(texto.length() - 1);
                verificarLetra(Character.toUpperCase(letra));
                editTecladoNativo.setText("");  // Limpa o campo após a entrada
            }
            return true;
        });

        // Também captura quando o usuário digita letras
        editTecladoNativo.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                // Verifica se é uma letra (A-Z)
                char letra = (char) event.getUnicodeChar();
                if (Character.isLetter(letra)) {
                    // Limpa o campo antes de processar para evitar duplicação
                    String texto = editTecladoNativo.getText().toString().trim();
                    if (!texto.isEmpty()) {
                        char letraDigitada = texto.charAt(texto.length() - 1);
                        verificarLetra(Character.toUpperCase(letraDigitada));
                        editTecladoNativo.setText("");
                    }
                }
            }
            return false;
        });

        // Quando o campo ganha foco, mostra o teclado nativo
        editTecladoNativo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !usandoTecladoVirtual) {
                showNativeKeyboard();
            }
        });

        // Força o EditText a ser clicável e focável
        editTecladoNativo.setFocusable(true);
        editTecladoNativo.setFocusableInTouchMode(true);
    }

    /**
     * Alterna entre teclado virtual (dinâmico) e teclado nativo (do sistema).
     */
    /**
     * Alterna entre teclado virtual (dinâmico) e teclado nativo (do sistema).
     */
    private void alternarTeclado() {
        usandoTecladoVirtual = !usandoTecladoVirtual;

        if (usandoTecladoVirtual) {
            // ===== TECLADO VIRTUAL =====
            findViewById(R.id.tecladoContainer).setVisibility(View.VISIBLE);
            layoutTecladoNativo.setVisibility(View.GONE);
            hideNativeKeyboard();
            editTecladoNativo.setText("");
            btnAlternarTeclado.setImageResource(R.drawable.ic_teclado);
        } else {
            // ===== TECLADO NATIVO =====
            findViewById(R.id.tecladoContainer).setVisibility(View.GONE);

            // Mostra o layout do teclado nativo
            layoutTecladoNativo.setVisibility(View.VISIBLE);

            // Garante que o EditText está visível (caso tenha sido alterado)
            editTecladoNativo.setVisibility(View.VISIBLE);

            // Força o foco no EditText e mostra o teclado
            editTecladoNativo.postDelayed(() -> {
                editTecladoNativo.requestFocus();
                showNativeKeyboard();
            }, 300);

            atualizarLetrasTentadas();
            btnAlternarTeclado.setImageResource(R.drawable.ic_teclado_off);
        }
    }

    /**
     * Atualiza a exibição das letras já tentadas.
     * Usado apenas quando o teclado nativo está ativo.
     */
    private void atualizarLetrasTentadas() {
        if (txtLetrasTentadas == null) return;

        if (letrasTentadas.isEmpty()) {
            txtLetrasTentadas.setText("-");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Character c : letrasTentadas) {
                sb.append(c).append(" ");
            }
            txtLetrasTentadas.setText(sb.toString().trim());
        }
    }

    /**
     * Mostra o teclado nativo (do sistema) no campo da palavra.
     */
    private void showNativeKeyboard() {
        editTecladoNativo.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTecladoNativo, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Esconde o teclado nativo (do sistema).
     */
    private void hideNativeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTecladoNativo.getWindowToken(), 0);
    }

    /**
     * Cria o teclado virtual com as letras A-Z.
     * Organiza em 5 colunas e usa LinearLayouts aninhados.
     */
    private void criarTecladoVirtual() {
        LinearLayout tecladoContainer = findViewById(R.id.tecladoContainer);
        tecladoContainer.removeAllViews(); // Limpa o teclado anterior
        botoesTeclado.clear();              // Limpa a lista de botões

        String[] letras = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
                "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

        int colunas = 5;
        int linhaAtual = 0;
        int margem = 2; // Espaçamento entre os botões

        // Percorre todas as letras do alfabeto
        for (int i = 0; i < letras.length; i++) {
            // A cada 5 letras, cria uma nova linha
            if (i % colunas == 0) {
                // Cria um novo LinearLayout para a linha
                LinearLayout linha = new LinearLayout(this);
                linha.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                linha.setOrientation(LinearLayout.HORIZONTAL);
                tecladoContainer.addView(linha);
                linhaAtual = tecladoContainer.getChildCount() - 1;
            }

            // Pega a linha atual para adicionar o botão
            LinearLayout linhaAtualView = (LinearLayout) tecladoContainer.getChildAt(linhaAtual);

            Button btn = (Button) getLayoutInflater().inflate(R.layout.item_teclado, null);
            btn.setText(letras[i]); // Define a letra do botão
            btn.setTextSize(14);    // Tamanho da fonte

            // Configura o botão para ocupar espaço igual na linha (weight = 1)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(margem, margem, margem, margem);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                String letraSelecionada = btn.getText().toString();
                verificarLetra(letraSelecionada.charAt(0));
                btn.setEnabled(false);  // Desabilita após o clique (letra já usada)
            });

            // Adiciona o botão à linha atual e à lista de botões
            linhaAtualView.addView(btn);
            botoesTeclado.add(btn);
        }
    }

    /**
     * Reabilita todos os botões do teclado para uma nova partida.
     */
    private void reiniciarTeclado() {
        for (Button btn : botoesTeclado) {
            btn.setEnabled(true);
        }
    }

    /**
     * Normaliza um caractere removendo acentos e tratando cedilha.
     * Usado para comparar letras ignorando acentos.
     * Exemplo: "á" vira "a", "ç" vira "c"
     * @param c Caractere a ser normalizado
     * @return Caractere normalizado (sem acentos)
     */

    private char normalizarChar(char c) {
        String letra = String.valueOf(c);
        // Remove acentos usando Normalizer
        String normalizada = java.text.Normalizer.normalize(letra, java.text.Normalizer.Form.NFD);
        normalizada = normalizada.replaceAll("\\p{M}", "");  // Remove diacríticos

        // Trata cedilha manualmente
        if (normalizada.equals("Ç")) normalizada = "C";
        if (normalizada.equals("ç")) normalizada = "c";

        return normalizada.charAt(0);
    }

    /**
     * Inicia uma nova partida.
     * Escolhe uma palavra aleatória, zera erros e reinicia o timer.
     */
    private void iniciarJogo() {
        // ===== 1. ESCOLHE UMA PALAVRA ALEATÓRIA =====
        String[] dadosDaForca = wordsDatabase.obterPalavraAleatoria();
        if (dadosDaForca != null) {
            palavraSecretaOriginal = dadosDaForca[0];
            categoriaDaPalavra = dadosDaForca[1];
        }

        // ===== 2. CRIA A PALAVRA OCULTA COM UNDERSCORES =====
        // Exemplo: "CAVALO" vira "_ _ _ _ _ _"
        palavraOculta = new StringBuilder();
        for (int i = 0; i < palavraSecretaOriginal.length(); i++) {
            palavraOculta.append("_ ");
        }
        txtPalavraOculta.setText(palavraOculta.toString().trim());

        // ===== 3. RESETA OS DADOS DO JOGO =====
        erros = 0;
        letrasAcertadas = 0;                 // Zera contador de letras acertadas
        //pontuacao = 0;                       // Zera pontuação
        atualizarImagemForca();              // Mostra a forca com 0 erros
        letrasTentadas.clear();              // Limpa letras usadas
        reiniciarTeclado();                  // Reativa todos os botões
        atualizarPontuacao();                // Atualiza exibição da pontuação
        atualizarLetrasTentadas();           // Atualiza exibição das letras tentadas

        // Limpa o campo de entrada do teclado nativo
        editTecladoNativo.setText("");

        // ===== 4. REINICIA O TIMER =====
        pararTimer();
        iniciarTimer();
    }

    /**
     * Atualiza a imagem da forca baseada no número de erros.
     * As imagens devem estar nomeadas como: forca_0, forca_1, ..., forca_6
     */
    private void atualizarImagemForca() {
        String nomeImagem = "forca_" + erros;
        int resId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
        if (resId != 0) {
            imgForca.setImageResource(resId);
            // Atualiza a descrição acessível com o número de erros
            imgForca.setContentDescription(getString(R.string.content_desc_forca, erros));
        }
    }

    /**
     * Atualiza o TextView da pontuação.
     */
    private void atualizarPontuacao() {
        txtPontuacao.setText("⭐ " + pontuacao + " pts");
    }

    /**
     * Verifica se a letra escolhida está na palavra secreta.
     * Atualiza a palavra oculta, conta erros e verifica fim de jogo.
     * @param letra Letra escolhida pelo jogador
     */
    private void verificarLetra(char letra) {
        // Normaliza a letra para comparação (remove acentos)
        char letraNormalizada = normalizarChar(letra);

        // Se a letra já foi tentada, ignora
        if (letrasTentadas.contains(letraNormalizada)) return;
        letrasTentadas.add(letraNormalizada);

        // ===== VERIFICA SE A LETRA ESTÁ NA PALAVRA =====
        boolean acertou = false;
        for (int i = 0; i < palavraSecretaOriginal.length(); i++) {
            char originalNormalizado = normalizarChar(palavraSecretaOriginal.charAt(i));
            if (originalNormalizado == letraNormalizada) {
                // Revela a letra na posição correta
                palavraOculta.setCharAt(i * 2, palavraSecretaOriginal.charAt(i));
                acertou = true;
                letrasAcertadas++;              // Conta letra acertada
            }
        }

        // ===== CALCULA PONTUAÇÃO =====
        if (acertou) {
            // 100 pontos por letra acertada
            pontuacao += PONTOS_POR_LETRA;

            // Verifica se a palavra foi completada
            if (palavraOculta.indexOf("_") == -1) {
                // Bônus por palavra completa
                pontuacao += BONUS_PALAVRA_COMPLETA;
            }

        } else {
            erros++;
            atualizarImagemForca();
        }

        // Atualiza a exibição da palavra oculta
        txtPalavraOculta.setText(palavraOculta.toString().trim());
        atualizarPontuacao();                   // Atualiza pontuação na tela

        // ===== SE TECLADO NATIVO, ATUALIZA AS LETRAS TENTADAS =====
        if (!usandoTecladoVirtual) {
            atualizarLetrasTentadas();
        }

        // ===== VERIFICA FIM DE JOGO =====
        // Vitória: não há mais underscores (_) na palavra
        if (palavraOculta.indexOf("_") == -1) {
            mostrarDialogVitoria();
        }
        // Derrota: 6 erros (enforcado)
        else if (erros >= 6) {
            mostrarDialogDerrota();
        }
    }

    // ===================== DIÁLOGOS PERSONALIZADOS =====================

    /**
     * Exibe um diálogo de vitória com a palavra revelada.
     * Oferece opções: "Nova Partida" ou "Sair"
     */
    private void mostrarDialogVitoria() {
        pararTimer();  // Para o timer ao finalizar

        // ===== TOCA EFEITO SONORO DE VITÓRIA =====
        if (audioManager != null) {
            audioManager.tocarVitoria(this);
        }

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_victory);
        dialog.setCancelable(false);  // Impede fechar com botão voltar

        // Exibe a palavra secreta no diálogo
        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraVitoria);
        txtPalavra.setText(palavraSecretaOriginal);

        // Adiciona a pontuação na mensagem de vitória
        TextView txtMensagem = dialog.findViewById(R.id.txtMensagemVitoria);

        // Configura botão "Sair"
        Button btnSair = dialog.findViewById(R.id.btnSairVitoria);
        btnSair.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            finish();  // Volta para a tela inicial
        });

        // Configura botão "Nova Partida"
        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaVitoria);
        btnNovaPartida.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            iniciarJogo();  // Reinicia o jogo
        });

        dialog.show();
    }

    /**
     * Exibe um diálogo de derrota por enforcamento.
     * Mostra a palavra que o jogador não conseguiu adivinhar.
     */
    private void mostrarDialogDerrota() {
        pararTimer();

        // ===== TOCA EFEITO SONORO DE DERROTA =====
        if (audioManager != null) {
            audioManager.tocarDerrota(this);
        }

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_defeat);
        dialog.setCancelable(false);

        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraDerrota);
        txtPalavra.setText(palavraSecretaOriginal);

        // Adiciona a pontuação na mensagem de derrota
        TextView txtMensagem = dialog.findViewById(R.id.txtMensagemDerrota);

        Button btnSair = dialog.findViewById(R.id.btnSairDerrota);
        btnSair.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            finish();
        });

        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaDerrota);
        btnNovaPartida.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            iniciarJogo();
        });

        dialog.show();
    }

    /**
     * Exibe um diálogo quando o tempo acaba.
     * Mostra a palavra que o jogador não conseguiu adivinhar.
     */
    private void mostrarDialogTempoEsgotado() {
        pararTimer();

        // ===== TOCA EFEITO SONORO DE DERROTA =====
        if (audioManager != null) {
            audioManager.tocarDerrota(this);
        }

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_timeout);
        dialog.setCancelable(false);

        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraTempo);
        txtPalavra.setText(palavraSecretaOriginal);

        // Adiciona a pontuação na mensagem de tempo esgotado
        TextView txtMensagem = dialog.findViewById(R.id.txtMensagemTempo);

        Button btnSair = dialog.findViewById(R.id.btnSairTempo);
        btnSair.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            finish();
        });

        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaTempo);
        btnNovaPartida.setOnClickListener(v -> {
            // ===== PARA O EFEITO SONORO =====
            if (audioManager != null) {
                audioManager.pararEfeito();
            }
            dialog.dismiss();
            iniciarJogo();
        });

        dialog.show();
    }

    // ===================== CONTROLE DE TEMPO =====================

    /**
     * Inicia o timer regressivo de 3 minutos (180 segundos).
     * Atualiza o texto do tempo a cada segundo.
     * Se o tempo acabar, exibe o diálogo de tempo esgotado.
     */
    private void iniciarTimer() {
        tempoSegundos = 180;
        atualizarTextoTempo();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (tempoSegundos > 0) {
                    tempoSegundos--;
                    atualizarTextoTempo();
                    timerHandler.postDelayed(this, 1000);  // Repete a cada 1s
                } else {
                    mostrarDialogTempoEsgotado();  // Tempo acabou
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    /**
     * Para o timer, removendo as chamadas pendentes.
     */
    private void pararTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    /**
     * Atualiza o texto do tempo restante no formato MM:SS.
     */
    private void atualizarTextoTempo() {
        int minutos = tempoSegundos / 60;
        int segundos = tempoSegundos % 60;
        String tempoFormatado = String.format("%02d:%02d", minutos, segundos);
        txtTempo.setText(getString(R.string.tempo_label) + tempoFormatado);
    }

    /**
     * Chamado quando a Activity é destruída.
     * Garante que o timer seja parado para evitar vazamento de memória
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararTimer();
        if (audioManager != null) {
            audioManager.pararMusica();  // Garante que para ao fechar
        }
    }
}