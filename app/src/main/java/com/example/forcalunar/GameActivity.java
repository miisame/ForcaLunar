package com.example.forcalunar;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

/**
 * Activity principal do jogo da forca.
 * Gerencia a lógica do jogo, teclado virtual, timer e diálogos de resultado.
 */

public class GameActivity extends AppCompatActivity {

    // ===================== BANCO DE PALAVRAS =====================
    // Lista inicial de 10 palavras (requisito do projeto)
    private String[] palavras = {
            "LUA", "CAVALO", "MARTELO", "ESTRELA", "ROBÔ",
            "CAVEIRA", "FOGO", "DRAGÃO", "CAVERNA", "LANÇA"
    };

    // ===================== VARIÁVEIS DO JOGO =====================
    private String palavraSecretaOriginal;      // Palavra escolhida para a partida
    private StringBuilder palavraOculta;        // Palavra com _ para letras não reveladas
    private int erros = 0;                      // Número de erros (max 6)
    private ArrayList<Character> letrasTentadas = new ArrayList<>(); // Letras já usadas

    // ===================== COMPONENTES DA INTERFACE =====================
    private TextView txtPalavraOculta;          // Exibe a palavra com _
    private ImageView imgForca;                 // Imagem da forca
    private TextView txtTempo;                  // Exibe o tempo restante
    private ArrayList<Button> botoesTeclado = new ArrayList<>(); // Lista de botões do teclado

    // ===================== CONTROLE DE TEMPO =====================
    private int tempoSegundos = 180;            // 3 minutos (180 segundos)
    private Handler timerHandler = new Handler(); // Gerencia o timer
    private Runnable timerRunnable;             // Ação executada a cada segundo

    // ===================== CICLO DE VIDA DA ACTIVITY =====================

    /**
     * Chamado quando a Activity é criada.
     * Inicializa componentes, carrega o fragment e cria o teclado.
     * @param savedInstanceState Estado salvo anteriormente (se houver)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // ===== 1. RECUPERA O NICK DO JOGADOR =====
        // O nick foi passado pela MainActivity via Intent
        String nick = getIntent().getStringExtra("nick");
        TextView txtNick = findViewById(R.id.txtNick);
        txtNick.setText(getString(R.string.jogador_label) + nick);

        // ===== 2. VINCULA OS COMPONENTES DO LAYOUT =====
        txtTempo = findViewById(R.id.txtTempo);
        txtPalavraOculta = findViewById(R.id.txtPalavraOculta);
        imgForca = findViewById(R.id.imgForca);

        // Define descrição acessível para a imagem da forca
        imgForca.setContentDescription(getString(R.string.content_desc_forca, 0));

        // ===== 3. INICIALIZA O JOGO =====
        iniciarJogo();

        // ===== 4. CARREGA O FRAGMENT DA FORCA =====
        // O fragment é exibido no FrameLayout com ID fragmentContainer
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ForcaFragment())
                    .commit();
        }

        // ===== 5. CRIA O TECLADO VIRTUAL =====
        criarTecladoVirtual();
    }

    /**
     * Cria o teclado virtual com as letras A-Z.
     * Organiza em 5 colunas e usa LinearLayouts aninhados.
     */
    private void criarTecladoVirtual() {
        LinearLayout tecladoContainer = findViewById(R.id.tecladoContainer);
        tecladoContainer.removeAllViews();  // Limpa o teclado anterior
        botoesTeclado.clear();              // Limpa a lista de botões

        // Array com todas as letras do alfabeto
        String[] letras = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
                "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

        int colunas = 5;  // Número de colunas do teclado
        int linhaAtual = 0;

        // Percorre todas as letras
        for (int i = 0; i < letras.length; i++) {
            // A cada 5 letras, cria uma nova linha
            if (i % colunas == 0) {
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

            // Infla o layout do botão a partir do XML
            Button btn = (Button) getLayoutInflater().inflate(R.layout.item_teclado, null);
            btn.setText(letras[i]);

            // Configura o botão para ocupar espaço igual na linha (weight = 1)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,    // Largura 0 (usa weight)
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);                // Peso igual para todos
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);

            // Define o que acontece quando o botão é clicado
            btn.setOnClickListener(v -> {
                String letraSelecionada = btn.getText().toString();
                verificarLetra(letraSelecionada.charAt(0));
                btn.setEnabled(false);  // Desabilita após o clique
            });

            linhaAtualView.addView(btn);
            botoesTeclado.add(btn);  // Adiciona à lista de botões
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
        Random rand = new Random();
        palavraSecretaOriginal = palavras[rand.nextInt(palavras.length)].toUpperCase();

        // ===== 2. CRIA A PALAVRA OCULTA COM UNDERSCORES =====
        // Exemplo: "CAVALO" vira "_ _ _ _ _ _"
        palavraOculta = new StringBuilder();
        for (int i = 0; i < palavraSecretaOriginal.length(); i++) {
            palavraOculta.append("_ ");
        }
        txtPalavraOculta.setText(palavraOculta.toString().trim());

        // ===== 3. RESETA OS DADOS DO JOGO =====
        erros = 0;
        atualizarImagemForca();          // Mostra a forca com 0 erros
        letrasTentadas.clear();          // Limpa letras usadas
        reiniciarTeclado();              // Reativa todos os botões

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
            }
        }

        // ===== SE ERROU, INCREMENTA O CONTADOR =====
        if (!acertou) {
            erros++;
            atualizarImagemForca();
        }

        // Atualiza a exibição da palavra oculta
        txtPalavraOculta.setText(palavraOculta.toString().trim());

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

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_victory);
        dialog.setCancelable(false);  // Impede fechar com botão voltar

        // Exibe a palavra secreta no diálogo
        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraVitoria);
        txtPalavra.setText(palavraSecretaOriginal);

        // Configura botão "Sair"
        Button btnSair = dialog.findViewById(R.id.btnSairVitoria);
        btnSair.setOnClickListener(v -> {
            dialog.dismiss();
            finish();  // Volta para a tela inicial
        });

        // Configura botão "Nova Partida"
        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaVitoria);
        btnNovaPartida.setOnClickListener(v -> {
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

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_defeat);
        dialog.setCancelable(false);

        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraDerrota);
        txtPalavra.setText(palavraSecretaOriginal);

        Button btnSair = dialog.findViewById(R.id.btnSairDerrota);
        btnSair.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaDerrota);
        btnNovaPartida.setOnClickListener(v -> {
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

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_timeout);
        dialog.setCancelable(false);

        TextView txtPalavra = dialog.findViewById(R.id.txtPalavraTempo);
        txtPalavra.setText(palavraSecretaOriginal);

        Button btnSair = dialog.findViewById(R.id.btnSairTempo);
        btnSair.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        Button btnNovaPartida = dialog.findViewById(R.id.btnNovaPartidaTempo);
        btnNovaPartida.setOnClickListener(v -> {
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
    }
}