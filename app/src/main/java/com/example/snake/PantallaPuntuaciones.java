package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PantallaPuntuaciones extends AppCompatActivity {

    Button btnVolver;
    MediaPlayer pantalla;
    List<Integer> listaPuntuaciones;
    TextView puntuacion1;
    TextView puntuacion2;
    TextView puntuacion3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puntuaciones);
        btnVolver = findViewById(R.id.btn_volver);
        puntuacion1 = findViewById(R.id.puntuacion1);
        puntuacion2 = findViewById(R.id.puntuacion2);
        puntuacion3 = findViewById(R.id.puntuacion3);
        pantalla = MediaPlayer.create(this, R.raw.pantalla_puntucaciones);
        pantalla.setLooping(true);
        pantalla.start();

        if(SharedPreferencesSnake.hayDatos(this)) {
            listaPuntuaciones = SharedPreferencesSnake.recuperarDatos(this);
            if (!listaPuntuaciones.isEmpty()) {
                Collections.sort(listaPuntuaciones, Collections.reverseOrder());

                puntuacion1.setText(String.valueOf(listaPuntuaciones.get(0)));
                if (listaPuntuaciones.size() > 1) {
                    puntuacion2.setText(String.valueOf(listaPuntuaciones.get(1)));

                }
                if (listaPuntuaciones.size() > 2) {
                    puntuacion3.setText(String.valueOf(listaPuntuaciones.get(2)));
                }
            }
        }



        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pantalla.stop();
                Intent intent = new Intent(PantallaPuntuaciones.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}