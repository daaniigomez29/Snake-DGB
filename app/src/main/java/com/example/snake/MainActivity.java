package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    Button btnStart;
    Button btnPuntuaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.startGame);
        btnPuntuaciones = findViewById(R.id.btn_puntuaciones);
        mediaPlayer = MediaPlayer.create(this, R.raw.pantalla_inicio);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                Intent intent = new Intent(MainActivity.this, SnakeGame.class);
                startActivity(intent);
            }
        });

        btnPuntuaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                Intent intent = new Intent(MainActivity.this, PantallaPuntuaciones.class);
                startActivity(intent);
            }
        });
    }
}