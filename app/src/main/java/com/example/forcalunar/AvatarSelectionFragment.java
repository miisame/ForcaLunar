package com.example.forcalunar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AvatarSelectionFragment extends DialogFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_selection, container, false);

        // ===== REMOVE O FUNDO BRANCO =====
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setDimAmount(0.7f);  // Escurece o fundo (opcional)
        }

        // Mapeia as opções de avatares
        view.findViewById(R.id.avatar1).setOnClickListener(this);
        view.findViewById(R.id.avatar2).setOnClickListener(this);
        view.findViewById(R.id.avatar3).setOnClickListener(this);
        view.findViewById(R.id.avatar4).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int resIdEscolhido = 0;

        // Identifica qual imagem foi clicada e captura o recurso correspondente
        if (v.getId() == R.id.avatar1) resIdEscolhido = R.drawable.avatar_marc_spector;
        else if (v.getId() == R.id.avatar2) resIdEscolhido = R.drawable.avatar_steven_grant;
        else if (v.getId() == R.id.avatar3) resIdEscolhido = R.drawable.avatar_jake_lockley;
        else if (v.getId() == R.id.avatar4) resIdEscolhido = R.drawable.avatar_konshu;

        // Envia o resultado de volta para a Activity usando a Fragment Result API
        Bundle resultado = new Bundle();
        resultado.putInt("avatarResId", resIdEscolhido);
        getParentFragmentManager().setFragmentResult("chaveAvatar", resultado);

        // Fecha o diálogo automaticamente
        dismiss();
    }
}
