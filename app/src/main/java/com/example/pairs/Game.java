package com.example.pairs;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Game {
    public static GoogleApiClient mGoogleApiClient;
    public static int ROWS = -1;
    public static int COLUMNS = -1;
    public static int[][] boxes;
    public static int turn;
    public static int pointsJ1;
    public static int pointsJ2;
    public static String matchType = "LOCAL";
}
