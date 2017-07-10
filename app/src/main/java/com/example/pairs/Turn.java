package com.example.pairs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by usuwi on 10/07/2017.
 */

public class Turn {
    public int[][] boxes;
    public int level = 0;
    public int rows = 0;
    public int columns = 0;
    public int pointsP1 = 0;
    public int pointsP2 = 0;
    public int playerTurn = 0;

    public Turn() {
    }

    public byte[] persist() {
        JSONObject retVal = new JSONObject();
        try {
            retVal.put("nivel", level);
            retVal.put("filas", rows);
            retVal.put("columnas", columns);
            retVal.put("puntosJ1", pointsP1);
            retVal.put("puntosJ2", pointsP2);
            retVal.put("turnoJugador", playerTurn);
            String tablero = "";
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (boxes[i][j] <= 9) {
                        tablero = tablero + "0" + boxes[i][j];
                    } else {
                        tablero = tablero + boxes[i][j];
                    }
                }
            }
            retVal.put("tablero", tablero);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String st = retVal.toString();
        return st.getBytes(Charset.forName("UTF-8"));
    }

    static public Turn unpersist(byte[] byteArray) {
        if (byteArray == null) {
            return new Turn();
        }
        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }
        Turn retVal = new Turn();
        try {
            JSONObject obj = new JSONObject(st);
            if (obj.has("nivel")) {
                retVal.level = obj.getInt("nivel");
            }
            if (obj.has("filas")) {
                retVal.rows = obj.getInt("filas");
            }
            if (obj.has("columnas")) {
                retVal.columns = obj.getInt("columnas");
            }
            if (obj.has("puntosJ1")) {
                retVal.pointsP1 = obj.getInt("puntosJ1");
            }
            if (obj.has("puntosJ2")) {
                retVal.pointsP2 = obj.getInt("puntosJ2");
            }
            if (obj.has("turnoJugador")) {
                retVal.playerTurn = obj.getInt("turnoJugador");
            }
            if (obj.has("tablero")) {
                retVal.boxes = new int[retVal.columns][retVal.rows];
                int k = 0;
                for (int i = 0; i < retVal.rows; i++) {
                    for (int j = 0; j < retVal.columns; j++) {
                        retVal.boxes[i][j] = Integer.parseInt(obj.getString("tablero").substring(k, k + 2));
                        k = k + 2;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}

