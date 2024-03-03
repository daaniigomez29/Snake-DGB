package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PantallaPuntuaciones extends AppCompatActivity {

    Button btnVolver;
    MediaPlayer pantalla;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puntuaciones);
        btnVolver = findViewById(R.id.btn_volver);
        pantalla = MediaPlayer.create(this, R.raw.pantalla_puntucaciones);
        pantalla.setLooping(true);
        pantalla.start();
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