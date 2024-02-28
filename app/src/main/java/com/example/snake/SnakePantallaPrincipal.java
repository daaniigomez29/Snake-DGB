package com.example.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SnakePantallaPrincipal extends SurfaceView implements SurfaceHolder.Callback {

    private Paint paint;
    private boolean juegoEmpezado = false;
    private JuegoEmpezadoObserver observer;

    public SnakePantallaPrincipal(Context context, JuegoEmpezadoObserver observer) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setTextSize(100);
        paint.setColor(Color.WHITE);
        juegoEmpezado = false;
        this.observer = observer;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawTitleScreen();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !juegoEmpezado) {
            juegoEmpezado = true;
            notificarObservador();
            Log.d("Booleana", "Ha empezado? " + juegoEmpezado);
        }
        return true;
    }

    public void notificarObservador(){
        observer.onJuegoEmpezadoChanged(juegoEmpezado);
    }

    private void drawTitleScreen() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            // Dibuja "Snake" en la parte superior
            canvas.drawText("Snake", getWidth() / 4, getHeight() / 3, paint);
            // Dibuja "Pulsa para jugar" en la parte inferior
            canvas.drawText("Pulsa para jugar", getWidth() / 5, getHeight() * 2 / 3, paint);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public boolean isJuegoEmpezado() {
        return juegoEmpezado;
    }
}
