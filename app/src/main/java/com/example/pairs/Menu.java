package com.example.pairs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;

import java.util.ArrayList;
import java.util.Random;

import static com.example.pairs.Game.mGoogleApiClient;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Menu extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener {
    private Button btnPlay;
    private static final int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;
    private com.google.android.gms.common.SignInButton btnConnect;
    private Button btnDisconnect;
    private Button btnSavedGames;
    private Button btnRealTimeGame;
    private Button btnTurnBasedMatch;

    String mIncomingInvitationId = null;
    final static int RC_SELECT_PLAYERS = 10000;
    private Button btnInvite;

    private Button btnLeaderBoard;
    final static int REQUEST_LEADERBOARD = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnConnect = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        btnConnect.setOnClickListener(btnConnect_Click);
        btnDisconnect = (Button) findViewById(R.id.sign_out_button);
        btnDisconnect.setOnClickListener(btnDisconnect_Click);
        Game.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        SharedPreferences prefs = getSharedPreferences("Parejas", MODE_PRIVATE);
        int conectado = prefs.getInt("conectado", 0);
        if (conectado != 0) {
            Game.mGoogleApiClient.connect();
        }
        btnSavedGames = (Button) findViewById(R.id.btnSavedGames);
        btnRealTimeGame = (Button) findViewById(R.id.btnRealTimeGame);
        btnInvite = (Button) findViewById(R.id.btnInvite);
        btnTurnBasedMatch = (Button) findViewById(R.id.btnTurnBasedMatch);
        btnLeaderBoard = (Button) findViewById(R.id.btnLeaderboard);
    }

    public void btnPlay_Click(View v) {
        Game.matchType = "LOCAL";
        newGame(4, 4);
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
    }

    public void btnRealTimeGame_Click(View v) {
        Game.matchType = "REAL";
        newGame(4, 4);
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
    }

    public void btnSavedGames_Click(View v) {
        Game.matchType = "GUARDADA";
        newGame(4, 4);
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
    }

    public void btnTurnBasedMatch_Click(View v) {
        Game.matchType = "TURNO";
        newGame(4, 4);
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
    }

    public void btnInvite_Click(View v) {
        final int NUMERO_MINIMO_OPONENTES = 1, NUMERO_MAXIMO_OPONENTES = 1;
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(Game.mGoogleApiClient, NUMERO_MINIMO_OPONENTES, NUMERO_MAXIMO_OPONENTES, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    public void btnLeaderboard_Click(View v) {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(Game.mGoogleApiClient, getString(R.string.realTime_leaderboard_id)), REQUEST_LEADERBOARD);
    }


    private View.OnClickListener btnConnect_Click = new View.OnClickListener() {
        public void onClick(View v) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    };

    private View.OnClickListener btnDisconnect_Click = new View.OnClickListener() {
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

    void newGame(int col, int fil) {
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
    public void onConnected(Bundle bundle) {
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
            case RC_SELECT_PLAYERS:
                if (responseCode != Activity.RESULT_OK) {
                    return;
                }
                final ArrayList<String> invitees = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                Bundle autoMatchCriteria = null;
                int minAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
                int maxAutoMatchPlayers = intent.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
                if (minAutoMatchPlayers > 0) {
                    autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
                } else {
                    autoMatchCriteria = null;
                }
                TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder().addInvitedPlayers(invitees).setAutoMatchCriteria(autoMatchCriteria).build();
                Games.TurnBasedMultiplayer.createMatch(Game.mGoogleApiClient, tbmc);
                break;
        }
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        mIncomingInvitationId = invitation.getInvitationId();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
            mIncomingInvitationId = null;
        }
    }
}