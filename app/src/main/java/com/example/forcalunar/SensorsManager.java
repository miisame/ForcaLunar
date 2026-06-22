package com.example.forcalunar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class SensorsManager implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor sensorLuz;
    private Sensor sensorAcelerometro;

    private float valorLuz;
    private float magnitudeAcelerometro;

    // Construtor: Recebe a Activity (Context) para poder rodar funções do sistema
    public SensorsManager(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            sensorLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    // Método para ligar os sensores (Será chamado no onResume da Activity)
    public void iniciarSensores() {
        valorLuz = -1f;
        magnitudeAcelerometro = -1f;

        if (sensorLuz != null) {
            sensorManager.registerListener(this, sensorLuz, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorAcelerometro != null) {
            sensorManager.registerListener(this, sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorLuz == null && sensorAcelerometro == null) {
            Toast.makeText(context, "Sensores indisponíveis neste aparelho.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para desligar os sensores (Será chamado no onPause da Activity)
    public void pararSensores() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            valorLuz = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            magnitudeAcelerometro = (float) Math.sqrt(x * x + y * y + z * z);
        }

        boolean luzPronta = (sensorLuz == null || valorLuz != -1f);
        boolean acelPronto = (sensorAcelerometro == null || magnitudeAcelerometro != -1f);

        if (luzPronta && acelPronto) {
            processarExibirToast();
            pararSensores(); // Para de ouvir imediatamente após a primeira leitura
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Vazio
    }

    private void processarExibirToast() {
        String palavraLuz = "Indisponível";
        String palavraMovimento = "Indisponível";

        if (sensorLuz != null) {
            if (valorLuz < 20) {
                palavraLuz = "Sombrio";
            } else if (valorLuz <= 150) {
                palavraLuz = "Iluminado";
            } else {
                palavraLuz = "Radiante";
            }
        }

        if (sensorAcelerometro != null) {
            float desvioDaGravidade = Math.abs(magnitudeAcelerometro - 9.81f);

            if (desvioDaGravidade < 0.5f) {
                palavraMovimento = "Estável";
            } else if (desvioDaGravidade <= 4.0f) {
                palavraMovimento = "Movimentado";
            } else {
                palavraMovimento = "Agitado";
            }
        }

        String mensagemFinal = "Ambiente " + palavraLuz + " e dispositivo " + palavraMovimento + "!";
        Toast.makeText(context, mensagemFinal, Toast.LENGTH_LONG).show();
    }
}
