package com.example.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends AppCompatActivity {
    Snake snake;
    SnakePantallaPrincipal snakePantallaPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        Point tamano = new Point();
        display.getSize(tamano);

        snake = new Snake(this, tamano);
        //snakePantallaPrincipal = new SnakePantallaPrincipal(this);

        setContentView(snake);
    }

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