package com.example.pairs;

import android.widget.Button;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Box {
    public int x;
    public int y;
    public Button button;

    public Box(Button button, int x, int y) {
        this.x = x;
        this.y = y;
        this.button = button;
    }

}
