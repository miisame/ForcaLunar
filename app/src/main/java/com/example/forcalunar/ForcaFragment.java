package com.example.forcalunar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

/**
 * Fragmento que exibe a imagem da forca no jogo.
 * Este fragmento é usado no container da GameActivity.
 * Atualmente é um fragmento simples que apenas exibe um layout,
 * mas pode ser expandido para mostrar animações ou informações adicionais.
 */

public class ForcaFragment extends Fragment {

    // ===================== CONSTRUTOR =====================

    public ForcaFragment() {
        // Construtor vazio - requerido pelo sistema
    }

    // ===================== CICLO DE VIDA =====================

    /**
     * Chamado quando o fragment é criado.
     * @param savedInstanceState Estado salvo anteriormente (se houver)
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Não há parâmetros sendo passados para esse fragment
    }

    /**
     * Cria a View do fragment.
     * @param inflater Inflador para criar a View a partir do XML
     * @param container ViewGroup pai que conterá este fragment
     * @param savedInstanceState Estado salvo anteriormente (se houver)
     * @return A View criada para este fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // O terceiro parâmetro "false" indica que não queremos anexar
        // a view ao container automaticamente (o sistema fará isso)
        return inflater.inflate(R.layout.fragment_forca, container, false);
    }
}