package com.example.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class Snake extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Thread thread = null;
    private Context context;
    private int juegoEmpezado = -1;

    private int screenX;
    private int screenY;

    // Tamaño de la serpiente
    private int snakeLength;

    //Posiciones de la manzana
    private int appleX;
    private int appleY;

    // Tamaño en píxeles del juego
    private int tamanoPixel;

    // El tamaño del area de juego
    private final int numBloquesAncho = 25;
    private int numBloquesAlto;

    public enum Movimiento {UP, RIGHT, DOWN, LEFT}

    // Empieza siempre para la derecha
    private Movimiento movimiento = Movimiento.RIGHT;

    // Controlar actualizaciones de tiempo
    private long siguienteFrame;
    // Actualiza el juego 10 veces por segundo
    private long FPS = 10;
    private final long milisegundosPorS = 1000;

    private int puntuacion;
    private int[] snakeXs;
    private int[] snakeYs;

    private float dedoX, dedoY;
    private static final float MIN_DISTANCIA_DESLIZAMIENTO = 100;


    private int manzanaDorada = 0;

    private volatile boolean isPlaying;

    private Canvas canvas;

    private SurfaceHolder surfaceHolder;

    private Paint paint;

    public Snake(Context context, Point tamano) {
        super(context);

        context = context;

        screenX = tamano.x;
        screenY = tamano.y;

        //Cuántos píxeles tiene cada bloque
        tamanoPixel = screenX / numBloquesAncho;
        //Bloques del mismo tamaño para la altura
        numBloquesAlto = screenY / tamanoPixel;


        surfaceHolder = getHolder();
        paint = new Paint();

        //Máxima puntuación
        snakeXs = new int[100];
        snakeYs = new int[100];

        surfaceHolder.addCallback(this);
        //newGame();
    }

    // Método que se llama cuando el Surface es creado
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (juegoEmpezado == -1) {
            newGame();
            juegoEmpezado = 1;
        } else {
            resume();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        if (isPlaying) {
            return; //Así no se crea un nuevo hilo de nuevo
        }
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        //Empieza con 1 de tamaño, un "bloque"
        snakeLength = 1;
        FPS = 10;
        snakeXs[0] = numBloquesAncho / 2;
        snakeYs[0] = numBloquesAlto / 2;

        spawnApple();

        puntuacion = 0;

        siguienteFrame = System.currentTimeMillis();
    }

    public void spawnApple() {
        Random randomManzana = new Random();
        manzanaDorada = randomManzana.nextInt(2);

        Random randomPosicion = new Random();
        appleX = randomPosicion.nextInt(numBloquesAncho - 1) + 1;
        appleY = randomPosicion.nextInt(numBloquesAlto - 1) + 1;
    }

    private void eatApple() {
        snakeLength++;

        if (manzanaDorada == 0) {
            puntuacion = puntuacion + 1;
            FPS++;
        } else if (manzanaDorada == 1) {
            puntuacion = puntuacion + 2;
            FPS = FPS + 2;
        }
        spawnApple();
    }

    private void moverSnake() {
        for (int i = snakeLength; i > 0; i--) {
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
        }

        // Mueve la cabeza en la dirección adecuada
        switch (movimiento) {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath() {
        boolean dead = false;

        // Hit the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= numBloquesAncho) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBloquesAlto) dead = true;

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        if (snakeXs[0] == appleX && snakeYs[0] == appleY) {
            eatApple();
        }

        moverSnake();

        if (detectDeath()) {
            newGame();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            //Fondo del juego
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            paint.setTextSize(90);
            paint.setColor(Color.WHITE);
            canvas.drawText("Puntuación: " + puntuacion, 10, 70, paint);

            //Color de Snake
            paint.setColor(Color.GREEN);
            //Dibuja a Snake en cada bloque
            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeXs[i] * tamanoPixel,
                        (snakeYs[i] * tamanoPixel),
                        (snakeXs[i] * tamanoPixel) + tamanoPixel,
                        (snakeYs[i] * tamanoPixel) + tamanoPixel,
                        paint);
            }

            // Color de la manzana
            if (manzanaDorada == 0) {
                paint.setColor(Color.argb(255, 255, 0, 0));
            } else {
                paint.setColor(Color.YELLOW);
            }

            // Pintar manzana
            canvas.drawRect(appleX * tamanoPixel,
                    (appleY * tamanoPixel),
                    (appleX * tamanoPixel) + tamanoPixel,
                    (appleY * tamanoPixel) + tamanoPixel,
                    paint);

            // Desbloquea el canvas y actualiza el dibujo
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {
        if (siguienteFrame <= System.currentTimeMillis()) {
            // Actualiza frame
            siguienteFrame = System.currentTimeMillis() + milisegundosPorS / FPS;

            // Devuelve true, la actualización se realiza y se dibuja de nuevo
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dedoX = motionEvent.getX();
                dedoY = motionEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = motionEvent.getX();
                float endY = motionEvent.getY();

                float deltaX = endX - dedoX;
                float deltaY = endY - dedoY;

                // Determina la dirección del deslizamiento
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Deslizamiento horizontal
                    if (deltaX > 0) {
                        // Deslizamiento hacia la derecha
                        movimiento = Movimiento.RIGHT;
                    } else {
                        // Deslizamiento hacia la izquierda
                        movimiento = Movimiento.LEFT;
                    }
                } else {
                    // Deslizamiento vertical
                    if (deltaY > 0) {
                        // Deslizamiento hacia abajo
                        movimiento = Movimiento.DOWN;
                    } else if(Math.abs(deltaY) > MIN_DISTANCIA_DESLIZAMIENTO){
                        // Deslizamiento hacia arriba
                        movimiento = Movimiento.UP;
                    }
                }
                break;
        }
        return true;
    }
}
