package com.example.snake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Snake extends SurfaceView implements Runnable, SurfaceHolder.Callback, View.OnClickListener {
    private Thread thread = null;
    private Context context;
    private int juegoEmpezado = -1;

    private int screenX;
    private int screenY;

    // Tamaño de la serpiente
    private int snakeLength;

    //Posiciones de la manzana
    private int manzanaX = 0;
    private int manzanaY = 0;

    //Manzana dorada que da doble de puntos
    private int manzanaDorada = 0;

    //Vidas del jugador
    private int vidas = 1;

    //Posiciones escudo
    private int escudoX = -1;
    private int escudoY = -1;

    //Número aleatorio para saber cuantas manzanas tiene que comer el jugador para que aparezca un escudo
    private int cuantoFaltaParaEscudo = 0;

    //Si la serpiente se come el escudo
    private boolean tieneEscudo = false;

    // Tamaño en píxeles del juego
    private int tamanoPixel;

    // El tamaño del area de juego
    private final int numBloquesAncho = 22;
    private int numBloquesAlto;

    public enum Movimiento {UP, RIGHT, DOWN, LEFT}

    // Empieza siempre para la derecha
    private Movimiento movimiento = Movimiento.RIGHT;

    // Controlar actualizaciones de tiempo
    private long siguienteFrame;
    // Actualiza el juego 10 veces por segundo
    //private double FPS = 10;
    // Velocidad de la serpiente por segundo
    private double velocidad = 10.0;
    //private final long milisegundosPorS = 1000;

    private int puntuacion;
    private int[] snakeXs; //Cada posición X de la serpiente
    private int[] snakeYs; //Cada posición Y de la serpiente

    private float dedoX, dedoY; //Posiciones del dedo del jugador al pulsar la pantalla
    private static final float MIN_DISTANCIA_DESLIZAMIENTO = 100; //Distancia mínima para deslizar el dedo y cambiar posición

    private volatile boolean isPlaying; //si el jugador está jugando

    private Canvas canvas;

    private SurfaceHolder surfaceHolder;

    private Paint paint;

    private MediaPlayer comerManzana; //Efecto de sonido al comer manzana
    private MediaPlayer comerEscudo; //Efecto de sonido al comer escudo
    private MediaPlayer juego; // Música del juego
    private MediaPlayer juego2; //Música 2 del juego
    private MediaPlayer juego3; //Música 3 del juego
    private MediaPlayer perder; //Música de muerte
    private Button btnReiniciar; //Botón para reiniciar la partida
    private Button btnVolver; //Botón para volver al menú de inicio
    private boolean flag = true; //Bandera para saber si ya se ha inicializado los botones y no tener errores de ejecución
    private boolean isDead = false; //Bandera para saber si el jugador ha perdido o no y así visualizar los botones

    private List<Integer> listaPuntuaciones = new ArrayList<>();

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

        //Iniciamos los 2 botones
        btnReiniciar = new Button(context);
        btnReiniciar.setText("Jugar de nuevo");
        btnReiniciar.setOnClickListener(this);


        btnVolver = new Button(context);
        btnVolver.setText("Volver al menú");
        btnVolver.setOnClickListener(this);

        if(SharedPreferencesSnake.hayDatos(getContext())){
            listaPuntuaciones = SharedPreferencesSnake.recuperarDatos(getContext());
        }
    }

    // Método que se llama cuando el Surface es creado
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (juegoEmpezado == -1) {
            nuevoJuego();
            juegoEmpezado = 1;
        } else {
            resume();
            juego.start();
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
        Log.d("LLega", "Llega");
        while (isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
        if(isDead) {
            visualizarBotonesMuerte();
        }
    }

    public void visualizarBotonesMuerte(){
        // Configuración inicial de altura, ancho y coordenadas
        ViewGroup.MarginLayoutParams paramsReiniciar = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsReiniciar.setMargins(screenX / 3, screenY / 3, 0, 0);

        ViewGroup.MarginLayoutParams paramsVolver = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsVolver.setMargins(screenX / 3, screenY / 2 - 100, 0, 0);

        // Inicializo los botones en el hilo de la interfaz de usuario
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag) {
                    // Agrega el botón a la vista
                    ((Activity) getContext()).addContentView(btnVolver, paramsVolver);
                    ((Activity) getContext()).addContentView(btnReiniciar, paramsReiniciar);
                    flag = false;
                } else {
                    // Hacer visible el botón con las nuevas coordenadas
                    btnVolver.setVisibility(View.VISIBLE);
                    btnReiniciar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void pause() {
        isPlaying = false;
        if(juego != null){
            juego.pause();
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void onClick(View v){
        perder.release();
        perder = null;

        if(v == btnReiniciar){
            nuevoJuego();
            resume();
            btnReiniciar.setVisibility(View.GONE);
            btnVolver.setVisibility(View.GONE);
        } else {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
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

    public void nuevoJuego() {
        iniciarSonidos();
        snakeLength = 1; //Empieza con 1 de tamaño, un "bloque"
        velocidad = 10.0; //Velocidad inicial de la serpiente
        vidas = 1; //Vidas iniciales
        cuantoFaltaParaEscudo = 0;
        escudoY = -1; //Posición del escudo en el alto
        escudoX = -1; //Posición del escudo en el ancho
        //Dónde aparece la serpiente por primera vez
        snakeXs[0] = numBloquesAncho / 2;
        snakeYs[0] = numBloquesAlto / 2;
        isDead = false;

        spawnManzana(); //Aparece una manzana por primera vez

        puntuacion = 0; //Puntuación reiniciada

        siguienteFrame = System.currentTimeMillis(); //Siguiente frame que ve el jugador
    }

    public void iniciarSonidos(){
        juego = MediaPlayer.create(getContext(), R.raw.game);
        juego.setVolume(0.3f, 0.3f);
        juego2 = MediaPlayer.create(getContext(), R.raw.juego2);
        juego3 = MediaPlayer.create(getContext(), R.raw.modo_serio);
        perder = MediaPlayer.create(getContext(), R.raw.death_song);
        comerManzana = MediaPlayer.create(getContext(), R.raw.eat_apple);
        comerEscudo = MediaPlayer.create(getContext(), R.raw.shield);

        //Lo que se realiza dentro es lo que ocurre después de que se termine la canción
        juego.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                juego2.setLooping(true);
                juego2.start();
            }
        });
        juego.start();
    }

    public void spawnManzana() {
        Random randomManzana = new Random();
        manzanaDorada = randomManzana.nextInt(2);

        Random randomPosicion = new Random();
        manzanaX = randomPosicion.nextInt(numBloquesAncho - 1) + 1;
        manzanaY = randomPosicion.nextInt(numBloquesAlto - 1) + 1;
    }

    public void spawnEscudo(){
        Random randomPosicion = new Random();
        while(escudoX != manzanaX && escudoY != manzanaY){
            escudoX = randomPosicion.nextInt(numBloquesAncho - 1) + 1;
            escudoY = randomPosicion.nextInt(numBloquesAlto - 1) + 1;
        }
    }

    private void comerManzana() {
        if (manzanaDorada == 0) {
            puntuacion = puntuacion + 1;
            if(puntuacion >= 10){
                velocidad = velocidad + 0.5;
            }
            snakeLength++;
        } else if (manzanaDorada == 1) {
            puntuacion = puntuacion + 2;
            if(puntuacion >= 10){
                velocidad++;
            }
            snakeLength = snakeLength + 2;
        }
        if(comerManzana != null){
            comerManzana.start();
        }
        cuantoFaltaParaEscudo--;
        spawnManzana();

        if(puntuacion == 10 || puntuacion == 11){
            Random random = new Random();
            cuantoFaltaParaEscudo = random.nextInt(5) + 1;
        }

        if(cuantoFaltaParaEscudo == 0){
            spawnEscudo();
        }
    }

    private void comerEscudo(){
        comerEscudo.start();
        Random random = new Random();
        cuantoFaltaParaEscudo = random.nextInt(5) + 1;
        vidas++;
        escudoX = -1;
        escudoY = -1;
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

    private boolean detectarMuerte() {
        // Se choca con el borde de la pantalla
        if (snakeXs[0] == -1){
            vidas--;
            snakeXs[0] = numBloquesAncho - 1;
        }
        if (snakeXs[0] >= numBloquesAncho){
            vidas--;
            snakeXs[0] = 0;
        }
        if (snakeYs[0] == -1){
            vidas--;
            snakeYs[0] = numBloquesAlto - 1;
        }
        if (snakeYs[0] == numBloquesAlto){
            vidas--;
            snakeYs[0] = 0;
        }

        // Se come a el mismo
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                vidas--;
            }
        }

        if(vidas == 0){
            isDead = true;
        }
        return isDead;
    }

    public void update() {
        if (snakeXs[0] == manzanaX && snakeYs[0] == manzanaY) {
            comerManzana();
        }

        if(snakeXs[0] == escudoX && snakeYs[0] == escudoY){
            comerEscudo();
        }

        moverSnake();

        if(puntuacion == 40 || puntuacion == 41){
            if(juego2.isPlaying()){
                juego2.stop();
            } else{
                juego.stop();
            }
        }

        if(puntuacion == 45 || puntuacion == 46){
            juego3.start();
        }

        if (detectarMuerte()) {
            isPlaying = false;

            listaPuntuaciones.add(puntuacion);
            SharedPreferencesSnake.guardarDatos(getContext(), listaPuntuaciones);

            perder.setLooping(true);
            perder.start();
        }
    }

    public void liberarAudios(){
        juego.stop();
        comerManzana.stop();
        comerEscudo.stop();
        juego.release();
        juego = null;
        comerManzana.release();
        comerManzana = null;
        comerEscudo.release();
        comerEscudo = null;
        juego2.release();
        juego2 = null;
        juego3.release();
        juego3 = null;

    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            //Fondo del juego
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            paint.setTextSize(70);
            paint.setColor(Color.WHITE);
            if(isPlaying){
                canvas.drawText("Puntuación: " + puntuacion, 10, 70, paint);
            } else{
                paint.setTextSize(90);
                canvas.drawText("Puntuación: " + puntuacion, screenX / 4, screenY / 2 - 500, paint);
            }

            //Color de Snake
            if(vidas > 1){
                paint.setColor(Color.CYAN);
            } else{
                paint.setColor(Color.GREEN);
            }
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
            canvas.drawRect(manzanaX * tamanoPixel,
                    (manzanaY * tamanoPixel),
                    (manzanaX * tamanoPixel) + tamanoPixel,
                    (manzanaY * tamanoPixel) + tamanoPixel,
                    paint);

            //Color del escudo
            paint.setColor(Color.CYAN);
            //Pintar escudo
            canvas.drawRect(escudoX * tamanoPixel,
                    (escudoY * tamanoPixel),
                    (escudoX * tamanoPixel) + tamanoPixel,
                    (escudoY * tamanoPixel) + tamanoPixel,
                    paint);

            // Desbloquea el canvas y actualiza el dibujo
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {
        if (siguienteFrame <= System.currentTimeMillis()) {
            // Actualiza frame
            siguienteFrame = (System.currentTimeMillis() +  (long) (1000 / velocidad));

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
                    if (deltaX > 0 && movimiento != Movimiento.LEFT) {
                        // Deslizamiento hacia la derecha
                        movimiento = Movimiento.RIGHT;
                    } else if(movimiento != Movimiento.RIGHT) {
                        // Deslizamiento hacia la izquierda
                        movimiento = Movimiento.LEFT;
                    }
                } else {
                    // Deslizamiento vertical
                    if (deltaY > 0 && movimiento != Movimiento.UP) {
                        // Deslizamiento hacia abajo
                        movimiento = Movimiento.DOWN;
                    } else if(Math.abs(deltaY) > MIN_DISTANCIA_DESLIZAMIENTO && movimiento != Movimiento.DOWN){
                        // Deslizamiento hacia arriba
                        movimiento = Movimiento.UP;
                    }
                }
                break;
        }
        return true;
    }
}
