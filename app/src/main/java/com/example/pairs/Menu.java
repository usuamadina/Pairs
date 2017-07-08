package com.example.pairs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.ArrayList;
import java.util.Random;

import static com.example.pairs.Game.mGoogleApiClient;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Menu extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Button btnPlay;
    private static final int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;
    private com.google.android.gms.common.SignInButton btnConnect;
    private Button btnDisconnect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnConnect = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        btnConnect.setOnClickListener(btnConnect_Click);
        btnDisconnect = (Button) findViewById(R.id.sign_out_button);
        btnDisconnect.setOnClickListener(btnDisconnect_Click);
        Game.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Games.API).addScope(Games.SCOPE_GAMES).build();
        SharedPreferences prefs = getSharedPreferences("Parejas", MODE_PRIVATE);
        int conectado = prefs.getInt("conectado", 0);
        if (conectado != 0) {
            Game.mGoogleApiClient.connect();
        }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }
        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN, "Hubo un error al conectar, por favor, inténtalo más tarde.")) {
                mResolvingConnectionFailure = false;
            }

        }
    }

    View.OnClickListener btnConnect_Click = new View.OnClickListener() {
        public void onClick(View v) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    };
    View.OnClickListener btnDisconnect_Click = new View.OnClickListener() {
        public void onClick(View v) {
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            SharedPreferences.Editor editor = getSharedPreferences("Parejas", MODE_PRIVATE).edit();
            editor.putInt("conectado", 0);
            editor.commit();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SIGN_IN:
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                    SharedPreferences.Editor editor = getSharedPreferences("Parejas", MODE_PRIVATE).edit();
                    editor.putInt("conectado", 1);
                    editor.commit();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.unknown_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }
}