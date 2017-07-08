package com.example.pairs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Menu extends Activity {
    private Button btnPlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        btnPlay = (Button) findViewById(R.id.btnPlay);
    }

    public void btnPlay_Click(View v) {
        Game.matchType = "LOCAL";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
    }

    private void nuevoJuego(int col, int fil) {
        Game.turn = 1;
        Game.ROWS = fil;
        Game.COLUMNS = col;
        Game.boxes = new int[Game.COLUMNS][Game.ROWS];
        try {
            int size = Game.ROWS * Game.COLUMNS;
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < size; i++) {
                list.add(new Integer(i));
            }
            Random r = new Random();
            for (int i = size - 1; i >= 0; i--) {
                int t = 0;
                if (i > 0) {
                    t = r.nextInt(i);
                }
                t = list.remove(t).intValue();
                Game.boxes[i % Game.COLUMNS][i / Game.COLUMNS] = 1 + (t % (size / 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}