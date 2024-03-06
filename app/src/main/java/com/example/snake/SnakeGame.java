package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeGame extends AppCompatActivity {
    private Snake snake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Obtiene el tamaño de la pantalla
        Display display = getWindowManager().getDefaultDisplay();

        //Asignamos a tamano el tamaño de la pantalla
        Point tamano = new Point();
        display.getSize(tamano);

        snake = new Snake(this, tamano); //Instancio el juego pasándole el tamaño de la pantalla
        setContentView(snake); //La vista será la del SurfaceView
    }

    //Hilo correspondiente al resumir de la aplicación que se ejecutr
    @Override
    protected void onResume() {
        super.onResume();
        snake.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snake.pause();
    }
}
