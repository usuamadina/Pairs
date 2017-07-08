package com.example.pairs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.games.Games;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Play extends Activity {
    private Drawable hiddenImage;
    private List<Drawable> images;
    private Box firstBox;
    private Box secondBox;
    private ButtonListener buttonListener;
    private TableLayout table;
    private actualizaBoxs handler;
    private Context context;
    private static Object lock = new Object();
    private Button[][] buttons;
    private ButtonListener btnBox_Click;
    private static final int RC_SAVED_GAMES = 9009;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new actualizaBoxs();
        loadImages();
        setContentView(R.layout.play);
        hiddenImage = getResources().getDrawable(R.drawable.icon);
        table = (TableLayout) findViewById(R.id.TableLayoutBox);
        context = table.getContext();
        btnBox_Click = new ButtonListener();
        switch (Game.matchType) {
            case "LOCAL":
                mostrarTablero();
                break;
            case "GUARDADA":
                showSavedGames();
                break;
        }
    }

    class actualizaBoxs extends Handler {
        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                compruebaBoxs();
            }
        }
    }

    public void compruebaBoxs() {
        if (Game.boxes[secondBox.x][secondBox.y] == Game.boxes[firstBox.x][firstBox.y]) {
            //ACIERTO
            Game.boxes[secondBox.x][secondBox.y] = 0;
            Game.boxes[firstBox.x][firstBox.y] = 0;
            buttons[firstBox.x][firstBox.y].setVisibility(View.INVISIBLE);
            buttons[secondBox.x][secondBox.y].setVisibility(View.INVISIBLE);
            if (Game.turn == 1) {
                Game.pointsJ1 += 2;
            } else {
                Game.pointsJ2 += 2;
            }
            if ((Game.pointsJ1 + Game.pointsJ2) == (Game.ROWS * Game.COLUMNS)) {
                //FIN JUEGO
                ((TextView) findViewById(R.id.player)).setText("GANADOR player " + (Game.turn) + "");
            }
        } else {
            //FALLO
            secondBox.button.setBackgroundDrawable(hiddenImage);
            firstBox.button.setBackgroundDrawable(hiddenImage);
        }
        if (Game.turn == 1) {
            Game.turn = 2;
        } else {
            Game.turn = 1;
        }
        firstBox = null;
        secondBox = null;
    }

    private void loadImages() {
        images = new ArrayList<Drawable>();
        images.add(getResources().getDrawable(R.drawable.card1));
        images.add(getResources().getDrawable(R.drawable.card2));
        images.add(getResources().getDrawable(R.drawable.card3));
        images.add(getResources().getDrawable(R.drawable.card4));
        images.add(getResources().getDrawable(R.drawable.card5));
        images.add(getResources().getDrawable(R.drawable.card6));
        images.add(getResources().getDrawable(R.drawable.card7));
        images.add(getResources().getDrawable(R.drawable.card8));
        images.add(getResources().getDrawable(R.drawable.card9));
        images.add(getResources().getDrawable(R.drawable.card10));
        images.add(getResources().getDrawable(R.drawable.card11));
        images.add(getResources().getDrawable(R.drawable.card12));
        images.add(getResources().getDrawable(R.drawable.card13));
        images.add(getResources().getDrawable(R.drawable.card14));
        images.add(getResources().getDrawable(R.drawable.card15));
        images.add(getResources().getDrawable(R.drawable.card16));
        images.add(getResources().getDrawable(R.drawable.card17));
        images.add(getResources().getDrawable(R.drawable.card18));
        images.add(getResources().getDrawable(R.drawable.card19));
        images.add(getResources().getDrawable(R.drawable.card20));
        images.add(getResources().getDrawable(R.drawable.card21));
    }

    class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            synchronized (lock) {
                if (firstBox != null && secondBox != null) {
                    return;
                }
                int id = v.getId();
                int x = id / 100;
                int y = id % 100;
                descubrirBox(x, y);
            }
        }
    }

    private void descubrirBox(int x, int y) {
        Button button = buttons[x][y];
        button.setBackgroundDrawable(images.get(Game.boxes[x][y]));
        if (firstBox == null) {
            firstBox = new Box(button, x, y);
        } else {
            if (firstBox.x == x && firstBox.y == y) {
                return;
            }
            secondBox = new Box(button, x, y);
            ((TextView) findViewById(R.id.score)).setText("JUGADOR 1= " + (Game.pointsJ1) + " : JUGADOR 2= " + (Game.pointsJ2));
            ((TextView) findViewById(R.id.player)).setText("TURNO JUGADOR " + (Game.turn) + "");
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {

                    try {
                        synchronized (lock) {
                            handler.sendEmptyMessage(0);
                        }
                    } catch (Exception e) {
                        Log.e("E1", e.getMessage());
                    }
                }
            };
            Timer t = new Timer(false);
            t.schedule(tt, 1300);
        }
    }

    private void mostrarTablero() {
        buttons = new Button[Game.COLUMNS][Game.ROWS];
        for (int y = 0; y < Game.ROWS; y++) {
            table.addView(createRow(y));
        }
        ((TextView) findViewById(R.id.score)).setText("JUGADOR 1= " + (Game.pointsJ1) + " : JUGADOR 2= " + (Game.pointsJ2));
        ((TextView) findViewById(R.id.player)).setText("TURNO JUGADOR " + (Game.turn) + "");
    }

    private TableRow createRow(int y) {
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);
        for (int x = 0; x < Game.COLUMNS; x++) {
            row.addView(crearBox(x, y));
            if (Game.boxes[x][y] == 0) {
                buttons[x][y].setVisibility(View.INVISIBLE);
            }
        }
        return row;
    }

    private View crearBox(int x, int y) {
        Button button = new Button(context);
        button.setBackgroundDrawable(hiddenImage);
        button.setId(100 * x + y);
        button.setOnClickListener(btnBox_Click);
        buttons[x][y] = button;
        return button;
    }

    private void showSavedGames() {
        int maxNumberOfSavedGamesToShow = 5;
        Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(Game.mGoogleApiClient, "Partidas guardadas", true, true, maxNumberOfSavedGamesToShow);
        startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
    }
}

