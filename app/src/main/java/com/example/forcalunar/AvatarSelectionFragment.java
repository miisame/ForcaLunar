package com.example.forcalunar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AvatarSelectionFragment extends DialogFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_selection, container, false);

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
        if (v.getId() == R.id.avatar1) resIdEscolhido = android.R.drawable.star_big_on;
        else if (v.getId() == R.id.avatar2) resIdEscolhido = android.R.drawable.ic_menu_compass;
        else if (v.getId() == R.id.avatar3) resIdEscolhido = android.R.drawable.ic_menu_agenda;
        else if (v.getId() == R.id.avatar4) resIdEscolhido = android.R.drawable.ic_menu_gallery;

        // Envia o resultado de volta para a Activity usando a Fragment Result API
        Bundle resultado = new Bundle();
        resultado.putInt("avatarResId", resIdEscolhido);
        getParentFragmentManager().setFragmentResult("chaveAvatar", resultado);

        // Fecha o diálogo automaticamente
        dismiss();
    }
}
