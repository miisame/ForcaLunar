package com.example.forcalunar;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Gerenciador de áudio do aplicativo.
 * Controla a música de fundo e efeitos sonoros.
 */
public class AudioManager {

    private MediaPlayer mediaPlayer;          // Música de fundo
    private MediaPlayer mediaPlayerEfeito;    // Efeitos sonoros

    /**
     * Toca a música de fundo em loop.
     */
    public void tocarMusicaFundo(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.musica_fundo);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.5f, 0.5f);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * Pausa a música de fundo (mantém a posição).
     */
    public void pausarMusica() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * Retoma a música de fundo após pausa.
     */
    public void retomarMusica() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * Para a música de fundo e libera os recursos.
     */
    public void pararMusica() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // EFEITOS SONOROS

    /**
     * Toca o efeito sonoro de vitória.
     * A música de fundo é pausada durante o efeito.
     */
    public void tocarVitoria(Context context) {
        tocarEfeito(context, R.raw.vitoria);
    }

    /**
     * Toca o efeito sonoro de derrota.
     * A música de fundo é pausada durante o efeito.
     */
    public void tocarDerrota(Context context) {
        tocarEfeito(context, R.raw.derrota);
    }

    /**
     * PARA o efeito sonoro imediatamente.
     * Útil quando o diálogo é fechado antes do efeito terminar.
     */
    public void pararEfeito() {
        if (mediaPlayerEfeito != null) {
            if (mediaPlayerEfeito.isPlaying()) {
                mediaPlayerEfeito.stop();
            }
            mediaPlayerEfeito.release();
            mediaPlayerEfeito = null;
        }
        // Retoma a música se estava pausada
        retomarMusica();
    }

    /**
     * Toca um efeito sonoro e pausa a música de fundo.
     * A música retorna automaticamente após o efeito terminar.
     */
    private void tocarEfeito(Context context, int rawId) {
        // ===== 1. PARA EFEITO ANTERIOR =====
        pararEfeito();

        // ===== 2. PAUSA A MÚSICA DE FUNDO =====
        pausarMusica();

        // ===== 3. TOCA O EFEITO =====
        mediaPlayerEfeito = MediaPlayer.create(context, rawId);
        mediaPlayerEfeito.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayerEfeito = null;
            // ===== 4. RETOMA A MÚSICA APÓS O EFEITO =====
            retomarMusica();
        });
        mediaPlayerEfeito.start();
    }

    /**
     * Ajusta o volume da música.
     */
    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }
}