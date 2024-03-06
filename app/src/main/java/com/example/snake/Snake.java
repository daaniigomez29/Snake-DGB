package com.example.snake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Snake extends SurfaceView implements Runnable, SurfaceHolder.Callback, View.OnClickListener {
    private Thread thread = null; //Hilo
    private Context context;
    private int juegoEmpezado = -1; //Controlar si el juego ha empezado o no

    private int screenX; //Ancho de la pantalla
    private int screenY; //Alto de la pantalla

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

    private Canvas canvas; //Para dibujar

    private SurfaceHolder surfaceHolder; //Sobre donde dibujamos

    private Paint paint; //Para dibujar

    private MediaPlayer comerManzana; //Efecto de sonido al comer manzana
    private MediaPlayer comerEscudo; //Efecto de sonido al comer escudo
    private MediaPlayer juego; // Música del juego
    private MediaPlayer juego2; //Música 2 del juego
    private MediaPlayer juego3; //Música 3 del juego
    private MediaPlayer perder; //Música de muerte
    private MediaPlayer nuevoRecord; //Música cuando se hace un nuevo top 1
    private Button btnReiniciar; //Botón para reiniciar la partida
    private Button btnVolver; //Botón para volver al menú de inicio
    private boolean flag = true; //Bandera para saber si ya se ha inicializado los botones y no tener errores de ejecución
    private boolean isDead = false; //Bandera para saber si el jugador ha perdido o no y así visualizar los botones
    private int maximaPuntuacion; //Máxima puntuación de la lista

    private List<Integer> listaPuntuaciones = new ArrayList<>(); //Lista de puntuaciones
    private boolean listaExiste = false; //Si la lista existe o no

    public Snake(Context context, Point tamano) {
        super(context);

        context = context;
        //Iniciamos las variables del tamaño de la pantalla obteniéndolas desde tamano
        screenX = tamano.x;
        screenY = tamano.y;

        //Cuántos píxeles tiene cada bloque
        tamanoPixel = screenX / numBloquesAncho;
        //Bloques del mismo tamaño para la altura
        numBloquesAlto = screenY / tamanoPixel;


        surfaceHolder = getHolder(); //Obtenemos el holder del surfaceView
        paint = new Paint(); //Instancia de Paint

        //Máxima puntuación
        snakeXs = new int[200];
        snakeYs = new int[200];

        surfaceHolder.addCallback(this); //Permite las llamadas al surfaceView

        //Iniciamos los 2 botones
        btnReiniciar = new Button(context);
        btnReiniciar.setText("Jugar de nuevo");
        btnReiniciar.setOnClickListener(this);

        btnVolver = new Button(context);
        btnVolver.setText("Volver al menú");
        btnVolver.setOnClickListener(this);
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

    //Método para llamar al surfaceView cuando cambia, en este caso no se necesario
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //Para destruir el surface lo que hacemos es pausar el hilo para mantener la interfaz en pantalla pero sin funcionamiento
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    //Ejecución del hilo
    @Override
    public void run() {
        //Mientras esté jugando se actualiza y dibuja en tiempo real
        while (isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
        //Si ha perdido se muestran los botones para volver a jugar o salir
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

    //Pausar el hilo
    public void pause() {
        isPlaying = false;
        if(juego != null){
            juego.pause(); //Pausa el hilo
        }
        try {
            thread.join(); //Espera a que el hilo termine su ejecución
        } catch (InterruptedException e) {
        }
    }

    //Al pulsar cualquiera de los 2 botones al perder, se realizan 2 acciones, o vuelves a jugar o vuelves al menú
    @Override
    public void onClick(View v){
            //Liberacion de audios para que no haya problemas
            perder.release();
            perder = null;
            nuevoRecord.release();
            nuevoRecord = null;


        if(v == btnReiniciar){
            nuevoJuego(); //Comienza nuevo juego
            resume();   //Resume el hilo, al no estar jugando y empezar de nuevo se crea un nuevo hilo
            //Dejar no visibles los botones de nuevo
            btnReiniciar.setVisibility(View.GONE);
            btnVolver.setVisibility(View.GONE);
        } else {
            liberarAudios(); //Se liberan los demás audios
            Intent intent = new Intent(getContext(), MainActivity.class); //Intent a MainActivity
            getContext().startActivity(intent); //Start de intent
        }
    }

    //Resumir el hilo
    public void resume() {
        //Si está jugando se devuelve así mismo
        if (isPlaying) {
            return; //Así no se crea un nuevo hilo de nuevo
        }
        //Si no está jugando se crea un nuevo hilo para jugar
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    //Reinicia todos los atributos a su valor predeterminado
    public void nuevoJuego() {
        iniciarSonidos();
        snakeLength = 1; //Empieza con 1 de tamaño, un "bloque"
        velocidad = 10.0; //Velocidad inicial de la serpiente
        vidas = 1; //Vidas iniciales
        cuantoFaltaParaEscudo = 0; //Cuántas manzanas tienes que comerte para que aparezca el escudo
        escudoY = -1; //Posición del escudo en el alto
        escudoX = -1; //Posición del escudo en el ancho
        //Dónde aparece la serpiente por primera vez
        snakeXs[0] = numBloquesAncho / 2;
        snakeYs[0] = numBloquesAlto / 2;
        isDead = false;

        spawnManzana(); //Aparece una manzana por primera vez

        puntuacion = 0; //Puntuación reiniciada

        if(SharedPreferencesSnake.hayDatos(getContext())){
            listaPuntuaciones = SharedPreferencesSnake.recuperarDatos(getContext()); //Recupero la lista de guardada
            listaExiste = true;
        }

        siguienteFrame = System.currentTimeMillis(); //Siguiente frame que ve el jugador
    }

    //Inicia todos los sonidos
    public void iniciarSonidos(){
        juego = MediaPlayer.create(getContext(), R.raw.game);
        juego.setVolume(0.3f, 0.3f);
        juego2 = MediaPlayer.create(getContext(), R.raw.juego2);
        juego3 = MediaPlayer.create(getContext(), R.raw.modo_serio);
        perder = MediaPlayer.create(getContext(), R.raw.death_song);
        nuevoRecord = MediaPlayer.create(getContext(), R.raw.nuevo_record);
        comerManzana = MediaPlayer.create(getContext(), R.raw.eat_apple);
        comerEscudo = MediaPlayer.create(getContext(), R.raw.shield);

        //Lo que se realiza dentro es lo que ocurre después de que se termine la canción
        juego.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                if(!isDead){
                    juego2.setLooping(true);
                    juego2.start(); //Comienza la 2 canción
                }
            }
        });
        juego.start();
    }

    //Genera un número aleatorio para saber si colocar una manzana roja o dorada y le añade una posición aleatoria
    public void spawnManzana() {
        Random randomManzana = new Random();
        manzanaDorada = randomManzana.nextInt(2);

        Random randomPosicion = new Random();
        manzanaX = randomPosicion.nextInt(numBloquesAncho - 1) + 1;
        manzanaY = randomPosicion.nextInt(numBloquesAlto - 1) + 1;
    }

    //Genera una posición aleatoria que no esté pillada por una manzana
    public void spawnEscudo(){
        Random randomPosicion = new Random();
        while(escudoX != manzanaX && escudoY != manzanaY){
            escudoX = randomPosicion.nextInt(numBloquesAncho - 1) + 1;
            escudoY = randomPosicion.nextInt(numBloquesAlto - 1) + 1;
        }
    }

    //Si el jugador come una manzana
    private void comerManzana() {
        //Si la manzana es roja, se sumará 1 a la puntuación, la serpiente se alargará y si la puntuación es >= 10, la velocidad irá subiendo 0.5
        if (manzanaDorada == 0) {
            puntuacion = puntuacion + 1;
            if(puntuacion >= 10){
                velocidad = velocidad + 0.5;
            }
            snakeLength++;
            //Si la manzana es dorada, se sumará 2 a la puntuación, la serpiente se alargará x2 y si la puntuación es >= 10, la velocidad irá subiendo 1
        } else if (manzanaDorada == 1) {
            puntuacion = puntuacion + 2;
            if(puntuacion >= 10){
                velocidad++;
            }
            snakeLength = snakeLength + 2;
        }
        if(comerManzana != null){
            comerManzana.start(); //Sonido de comer manzana
        }
        cuantoFaltaParaEscudo--; //Cuántas manzanas faltan por comer para que aparezca el escudo
        spawnManzana(); //Colocar manzana de nuevo

        //Si la puntuación es 10 u 11, se genera un contador de manzanas a comer para que aparezca el escudo
        if(puntuacion == 10 || puntuacion == 11){
            Random random = new Random();
            cuantoFaltaParaEscudo = random.nextInt(5) + 1;
        }

        if(cuantoFaltaParaEscudo == 0){
            spawnEscudo(); //Si el contador es 0, aparece un escudo
        }
    }

    //Controla el comer un escudo
    private void comerEscudo(){
        comerEscudo.start(); //Comienza audio
        Random random = new Random();
        cuantoFaltaParaEscudo = random.nextInt(5) + 1; //Se genera otro contador
        vidas++; //Se suma una vida
        //Se vuelve a esconder
        escudoX = -1;
        escudoY = -1;
    }

    //Controla el movimiento de la serpiente
    private void moverSnake() {
        //Recorre el cuerpo de la serpiente, actualizando su nueva posicion a su anterior
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

    //Controla si el jugador ha chocado y las vidas que tiene, devuelve si el jugador ha perdido o no
    private boolean detectarMuerte() {
        // Se choca con el borde ancho de la pantalla
        if (snakeXs[0] == -1){
            vidas--;
            snakeXs[0] = numBloquesAncho - 1; //Si la vida es mayor a 1, no perderá y aparecerá por la parte derecha
        }
        if (snakeXs[0] >= numBloquesAncho){
            vidas--;
            snakeXs[0] = 0; //Si la vida es mayor a 1, no perderá y aparecerá por la parte izquierda
        }
        // Se choca con el borde alto de la pantalla
        if (snakeYs[0] == -1){
            vidas--;
            snakeYs[0] = numBloquesAlto - 1; //Si la vida es mayor a 1, no perderá y aparecerá por la parte de abajo
        }
        if (snakeYs[0] == numBloquesAlto){
            vidas--;
            snakeYs[0] = 0; //Si la vida es mayor a 1, no perderá y aparecerá por la parte de arriba
        }

        // Se come a el mismo con la condición de que se choque y que mínimo tenga una longitud de 5
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                vidas--; //Se resta una vida
            }
        }

        //Si las vidas = 0, pierde
        if(vidas == 0){
            isDead = true;
        }
        return isDead;
    }

    //Controla las actualizaciones que ocurren en la ejecución del juego
    public void update() {
        //Si la cabeza de la serpiente pasa por encima de una manzana
        if (snakeXs[0] == manzanaX && snakeYs[0] == manzanaY) {
            comerManzana(); //Come manzana
        }

        //Si la cabeza de la serpiente pasa por encima de un escudo
        if(snakeXs[0] == escudoX && snakeYs[0] == escudoY){
            comerEscudo(); //Come escudo
        }

        moverSnake(); //Mueve la serpiente

        //Si la puntuación es 40 o 41, la música se para
        if(puntuacion == 40 || puntuacion == 41){
            if(juego2.isPlaying()){
                juego2.stop();
            } else{
                juego.stop();
            }
        }
        //Si la puntuación es 45 o 46, empieza la música final
        if(puntuacion == 45 || puntuacion == 46){
            juego3.setLooping(true);
            juego3.start();
        }

        //Si el jugador pierde
        if (detectarMuerte()) {
            isPlaying = false; //Deja de jugar para que la ejecución del juego pare

            if(listaExiste){ //Si la lista existe
                maximaPuntuacion = Collections.max(listaPuntuaciones); //Devuelve la puntuación máxima
            }
            //Si la puntuación nueva es mayor a la puntuación máxima existente
            if(puntuacion > maximaPuntuacion){
                nuevoRecord.setLooping(true);
                nuevoRecord.start(); //Suena música de victoria
            } else{ //Si no
                perder.setLooping(true);
                perder.start(); //Suena música triste
            }

            pararAudios(); //Para todos los audios
            listaPuntuaciones.add(puntuacion); //Añade la nueva puntuación a la lista de puntuaciones
            SharedPreferencesSnake.guardarDatos(getContext(), listaPuntuaciones); //Guarda la lista con sharedPreferences
        }
    }

    //Para todos los audios disponibles excepto los de perder, esos se escuchan al perder
    public void pararAudios(){
        juego.stop();
        comerManzana.stop();
        comerEscudo.stop();
        juego2.stop();
        juego3.stop();
    }

    //Libera todos los audios
    public void liberarAudios(){
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

    //Dibuja en pantalla
    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            //Fondo del juego
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            //Dibuja el texto Puntuación, si está jugando arriba a la izquierda, sino en el centro
            paint.setTextSize(70);
            paint.setColor(Color.WHITE);
            if(isPlaying){
                canvas.drawText("Puntuación: " + puntuacion, 10, 70, paint);
            } else{
                paint.setTextSize(90);
                canvas.drawText("Puntuación: " + puntuacion, screenX / 4, screenY / 2 - 500, paint);
            }

            //Color de Snake verde, si obtiene un escudo su color se vuelve azul
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

            // Color de la manzana, si es 0 es roja, si no es dorada
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

    //Maneja los eventos hechos por el jugador mientras juega
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            //Si el jugador pulsa pantalla, se obtienen las coordenadas de donde se ha pulsado
            case MotionEvent.ACTION_DOWN:
                dedoX = motionEvent.getX();
                dedoY = motionEvent.getY();
                break;
             //Al levantar el dedo, se obtienen de nuevo las coordenadas
            case MotionEvent.ACTION_UP:
                float endX = motionEvent.getX();
                float endY = motionEvent.getY();

                //Resultante de la distancia que ha recorrido el dedo del jugador en la pantalla
                float deltaX = endX - dedoX;
                float deltaY = endY - dedoY;

                // Determina la dirección del deslizamiento
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Deslizamiento horizontal, el jugador no podrá volver a moverse al lado contrario mientras esté en el mismo eje
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
