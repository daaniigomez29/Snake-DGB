package com.example.snake;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesSnake {
    private static final String nombrePreferencia = "preferencias";
    private static final String key = "lista";

    public SharedPreferencesSnake(){

    }

    public static void guardarDatos(Context context, List<Integer> list) {
        SharedPreferences preferences = context.getSharedPreferences(nombrePreferencia, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Convierte la lista de Integer a una cadena
        String listaString = TextUtils.join(",", list);

        // Guarda la cadena en SharedPreferences
        editor.putString(key, listaString);
        editor.apply();
    }

    public static List<Integer> recuperarDatos(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(nombrePreferencia, Context.MODE_PRIVATE);

        // Recupera la cadena desde SharedPreferences
        String listaString = preferences.getString(key, "");

        // Convierte la cadena a una lista de Integer utilizando el delimitador
        List<Integer> listaRecuperada = new ArrayList<>();
        String[] elementos = listaString.split(",");
        for (String elemento : elementos) {
            listaRecuperada.add(Integer.valueOf(elemento));
        }

        return listaRecuperada;
    }

    public static boolean hayDatos(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(nombrePreferencia, Context.MODE_PRIVATE);
        return preferences.contains(key);
    }
}
