package com.example.forcalunar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BootReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "canal_forca_lunar";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Verifica se o sinal recebido é realmente o de Boot Concluído
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            gerarNotificacao(context);
        }
    }

    private void gerarNotificacao(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        // 1. Criar o Canal de Notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificações do Jogo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription("Avisos para jogar a Forca Lunar");
            notificationManager.createNotificationChannel(canal);
        }

        // 2. Configurar o clique da notificação
        Intent intentAbrirApp = new Intent(context, MainActivity.class);
        intentAbrirApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intentAbrirApp,
                PendingIntent.FLAG_IMMUTABLE // Obrigatório em APIs modernas
        );

        // 3. Construir a Notificação em si
        NotificationCompat.Builder construtor = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Forca Lunar 🌙")
                .setContentText("Que tal uma partida?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Vincula o clique
                .setAutoCancel(true); // Some quando o usuário clica

        // 4. Disparar a notificação na barra de status
        notificationManager.notify(NOTIFICATION_ID, construtor.build());
    }
}
