package com.example.pairs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by usuwi on 08/07/2017.
 */

public class Play extends Activity implements RoomStatusUpdateListener, RoomUpdateListener,
        RealTimeMessageReceivedListener, OnTurnBasedMatchUpdateReceivedListener {
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
    String savedGameName;
    private byte[] savedGameData;

    String mRoomId = null;
    ArrayList<Participant> mParticipants = null;
    String mMyId = null;
    final static int RC_WAITING_ROOM = 10002;
    int localPlayer = 1;

    final static int RC_LOOK_AT_MATCHES = 10001;
    private AlertDialog mAlertDialog;
    public TurnBasedMatch mMatch;
    private Turn mTurnData;
    private int turnTurnBasedMatch;

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
            case "REAL":
                Log.d("OnCreate", "Juego en tiempo real");
                startRealTimeGame();
                break;
            case "TURNO":
                startTurnBasedMatch();
                break;
        }
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        Log.d("ONREALTIMEMESSAGERECIVE", "ENTRA");
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        if (buf[0] == 'A') {
            Log.d("REAL", "ENVIANDO TABLERO DE INICIO");
            int x = buf[1];
            int y = buf[2];
            int valor = buf[3];
            Game.boxes[x][y] = valor;
        }
        if (buf[0] == 'C') {
            Log.d("REAL", "OTRO PLAYER PRESIONANDO CASILLA");
            int x = buf[1];
            int y = buf[2];
            descubrirBox(x, y);
        }


    }

    @Override
    public void onRoomConnecting(Room room) {
        actualizeRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        actualizeRoom(room);

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        actualizeRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        actualizeRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        actualizeRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(Game.mGoogleApiClient));
        if (mRoomId == null) mRoomId = room.getRoomId();

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        actualizeRoom(room);

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        actualizeRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError();
            return;
        }
        mRoomId = room.getRoomId();
        showWaitingRoom(room);

    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError();
            return;
        }
        showWaitingRoom(room);

    }

    @Override
    public void onLeftRoom(int statusCode, String roomId) {

    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError();
            return;
        }
        actualizeRoom(room);
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {

    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {

    }

    class actualizaBoxs extends Handler {
        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                checkBoxes();
            }
        }
    }

    public void checkBoxes() {
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
                if (Game.matchType == "REAL") {
                    int points;
                    if (localPlayer == 1) {
                        points = Game.pointsJ1;
                    } else {
                        points = Game.pointsJ2;
                    }
                    Games.Leaderboards.submitScore(Game.mGoogleApiClient, getString(R.string.realTime_leaderboard_id), points);
                }


                ((TextView) findViewById(R.id.player)).setText("GANADOR JUGADOR " + (Game.turn) + "");
                if (Game.matchType == "TURNO") {
                    mTurnData.pointsP1 = Game.pointsJ1;
                    mTurnData.pointsP2 = Game.pointsJ2;
                    mTurnData.playerTurn = Game.turn;
                    mTurnData.boxes = Game.boxes;
                    Games.TurnBasedMultiplayer.finishMatch(Game.mGoogleApiClient, mMatch.getMatchId());
                    Toast.makeText(getApplicationContext(), "Fin de la Game.", Toast.LENGTH_LONG).show();
                    mTurnData = null;
                }
            }
        } else {
            //FALLO
            secondBox.button.setBackgroundDrawable(hiddenImage);
            firstBox.button.setBackgroundDrawable(hiddenImage);
            if (Game.turn == 1) {
                Game.turn = 2;
            } else {
                Game.turn = 1;
            }
            if (Game.matchType == "TURNO") {
                mTurnData.pointsP1 = Game.pointsJ1;
                mTurnData.pointsP2 = Game.pointsJ2;
                mTurnData.playerTurn = Game.turn;
                mTurnData.boxes = Game.boxes;
                String nextParticipantId = getNextPlayerId();
                Games.TurnBasedMultiplayer.takeTurn(Game.mGoogleApiClient, mMatch.getMatchId(), mTurnData.persist(), nextParticipantId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                    }
                });
                Toast.makeText(getApplicationContext(), "Fin de tu turno.", Toast.LENGTH_LONG).show();
                mTurnData = null;
            }
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
                if (Game.matchType == "REAL") {
                    if (Game.turn != localPlayer) {
                        Toast.makeText(getApplicationContext(), "No es tu turno.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (firstBox != null && secondBox != null) {
                    return;
                }
                int id = v.getId();
                int x = id / 100;
                int y = id % 100;
                descubrirBox(x, y);
                if (Game.matchType == "REAL") {
                    byte[] mensaje;
                    mensaje = new byte[3];
                    mensaje[0] = (byte) 'C';
                    mensaje[1] = (byte) x;
                    mensaje[2] = (byte) y;
                    for (Participant p : mParticipants) {
                        if (!p.getParticipantId().equals(mMyId)) {
                            Games.RealTimeMultiplayer.sendReliableMessage(Game.mGoogleApiClient, null, mensaje, mRoomId, p.getParticipantId());
                        }
                    }
                }
            }
        }


       /* @Override
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
        }*/
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
        Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(Game.mGoogleApiClient, "Games guardadas", true, true, maxNumberOfSavedGamesToShow);
        startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case RC_SAVED_GAMES:
                if (intent != null) {
                    if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                        SnapshotMetadata snapshotMetadata = (SnapshotMetadata) intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
                        savedGameName = snapshotMetadata.getUniqueName();
                        loadSnapshotSavedGame();
                        return;
                    } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                        newSnapshotSavedGame();
                    }
                } else {
                    finish();
                }
                break;
            case RC_WAITING_ROOM:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("OnResult", "Todo Ok");
                    localPlayerNumber();
                    sendOpponentBoard();
                    mostrarTablero();
                } else {
                    Log.d("OnResult", "RequestCode" + requestCode);
                    finish();
                }
                break;
            case RC_LOOK_AT_MATCHES:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
                if (match != null) {
                    manageTurnBasedMatch(match);
                }
                break;
        }
    }

    void encodeSavedGame() {
        savedGameData = new byte[Game.ROWS * Game.COLUMNS];
        int k = 0;
        for (int i = 0; i < Game.ROWS; i++) {
            for (int j = 0; j < Game.COLUMNS; j++) {
                savedGameData[k] = (byte) Game.boxes[i][j];
                k++;
            }
        }
    }

    void decodedSavedGame() {
        int i = 0;
        int j = 0;
        for (int k = 0; k < Game.ROWS * Game.COLUMNS; k++) {
            Game.boxes[i][j] = (int) savedGameData[k];
            if (j < Game.COLUMNS - 1) {
                j++;
            } else {
                j = 0;
                if (i < Game.ROWS - 1) {
                    i++;
                } else {
                    i = 0;
                }
            }
        }
    }

    void newSnapshotSavedGame() {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                String unique = new BigInteger(281, new Random()).toString(13);
                savedGameName = "Parejas-" + unique;
              /*  savedGameData = new byte[Game.ROWS * Game.COLUMNS];
                savedGameData[0] = (byte)Game.turn;
                savedGameData[1] = (byte)Game.pointsJ1;
                savedGameData[2] = (byte)Game.pointsJ2;*/
                Snapshots.OpenSnapshotResult open = Games.Snapshots.open(Game.mGoogleApiClient, savedGameName, true).await();
                if (!open.getStatus().isSuccess()) {
                    return 0;
                }
                encodeSavedGame();
                Snapshot snapshot = open.getSnapshot();
                snapshot.getSnapshotContents().writeBytes(savedGameData);
                Date d = new Date();
                SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                        .fromMetadata(snapshot.getMetadata())
                        .setDescription("Parejas " + DateFormat.format("yyyy.MM.dd", d.getTime()).toString())
                        .build();
                Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(Game.mGoogleApiClient, snapshot, metadataChange).await();
                return -1;
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (status == -1) {
                    mostrarTablero();
                }
            }
        };
        task.execute();
    }

    @Override
    public void onBackPressed() {
        if (Game.matchType == "GUARDADA") {
            saveSavedGame();
        }
        Play.this.finish();
    }

    public void saveSavedGame() {
        encodeSavedGame();
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult open = Games.Snapshots.open(Game.mGoogleApiClient, savedGameName, false).await();
                if (open.getStatus().isSuccess()) {
                    Snapshot snapshot = open.getSnapshot();
                    saveSnapshotSavedGame(snapshot, savedGameData, "Partida de Parejas");
                    return 1;
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer status) {
            }
        };
        task.execute();
    }

    private PendingResult<Snapshots.CommitSnapshotResult> saveSnapshotSavedGame(Snapshot
                                                                                        snapshot, byte[] data, String desc) {
        snapshot.getSnapshotContents().writeBytes(data);
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder().setDescription(desc).build();
        return Games.Snapshots.commitAndClose(Game.mGoogleApiClient, snapshot, metadataChange);
    }

    void loadSnapshotSavedGame() {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult result = Games.Snapshots.open(Game.mGoogleApiClient, savedGameName, true).await();
                if (result.getStatus().isSuccess()) {
                    Snapshot snapshot = result.getSnapshot();
                    try {
                        savedGameData = new byte[0];
                        savedGameData = snapshot.getSnapshotContents().readFully();
                    } catch (IOException e) {
                    }
                }
                return result.getStatus().getStatusCode();
            }

            @Override
            protected void onPostExecute(Integer status) {
                decodedSavedGame();
                mostrarTablero();
            }
        };
        task.execute();

    }

    private void startRealTimeGame() {
        Log.d("REAL", "HA ENTRADO");
        final int NUMERO_MINIMO_OPONENTES = 1, NUMERO_MAXIMO_OPONENTES = 1;
        Bundle criterioPartidaRapida = RoomConfig.createAutoMatchCriteria(NUMERO_MINIMO_OPONENTES, NUMERO_MAXIMO_OPONENTES, 0);
        RoomConfig.Builder roomConfiguradorConstructor = RoomConfig.builder(this);
        roomConfiguradorConstructor.setMessageReceivedListener(this);
        roomConfiguradorConstructor.setRoomStatusUpdateListener(this);
        roomConfiguradorConstructor.setAutoMatchCriteria(criterioPartidaRapida);
        Games.RealTimeMultiplayer.create(Game.mGoogleApiClient, roomConfiguradorConstructor.build());
    }

    private void localPlayerNumber() {
        localPlayer = 1;
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) continue;
            if (p.getStatus() != Participant.STATUS_JOINED) continue;
            if (p.getParticipantId().compareTo(mMyId) < 0) localPlayer = 2;
        }
    }


    public void sendOpponentBoard() {
        if (localPlayer == 1) {
            for (int fila = 0; fila < Game.ROWS; fila++) {
                for (int columna = 0; columna < Game.COLUMNS; columna++) {
                    byte[] mensaje;
                    mensaje = new byte[4];
                    mensaje[0] = (byte) 'A';
                    mensaje[1] = (byte) fila;
                    mensaje[2] = (byte) columna;
                    mensaje[3] = (byte) Game.boxes[fila][columna];
                    for (Participant p : mParticipants) {
                        if (!p.getParticipantId().equals(mMyId)) {
                            Games.RealTimeMultiplayer.sendReliableMessage(Game.mGoogleApiClient, null, mensaje, mRoomId, p.getParticipantId());
                        }
                    }
                }
            }
        }

    }

    void actualizeRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
    }

    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, "Oops! Ha ocurrido un error.");
        finish();
    }

    void showWaitingRoom(Room room) {
        Log.d("showWaitingRoom", "Entra");
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(Game.mGoogleApiClient, room, MIN_PLAYERS);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    public void startTurnBasedMatch() {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(Game.mGoogleApiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }

    public void manageTurnBasedMatch(TurnBasedMatch match) {
        mMatch = match;
        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();
        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Cancelada!", "Este partida ha sido cancelada!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expirada!", "Esta partida ha expirado!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Esperando a jugadores aleatorios...", "Todavía estamos esperando a jugadores aleatorios.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    showWarning("Completada!", "Esta partida ha finalizado! No hay nada que hacer");
                    break;
                }
        }
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = Turn.unpersist(mMatch.getData());
                if (match.getData() == null) {
                    initializeTurnBasedMatch(mMatch);
                }
                initializeTurnBasedMatch(mMatch);
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                showWarning("Turno...", "No es tu turno.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Esperando!", "Esperando a que respondan a las invitaciones!");
        }
        mTurnData = null;
    }

    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title).setMessage(message);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    public void initializeTurnBasedMatch(TurnBasedMatch match) {
        mTurnData = new Turn();
        mTurnData.level = 1;
        mTurnData.rows = 4;
        mTurnData.columns = 4;
        mTurnData.boxes = new int[mTurnData.columns][mTurnData.rows];
        mTurnData.pointsP1 = 0;
        mTurnData.pointsP2 = 0;
        mTurnData.playerTurn = 1;
        try {
            int size = mTurnData.rows * mTurnData.columns;
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int j = 0; j < size; j++) {
                list.add(new Integer(j));
            }
            Random r = new Random();
            for (int i = size - 1; i >= 0; i--) {
                int t = 0;
                if (i > 0) {
                    t = r.nextInt(i);
                }
                t = list.remove(t).intValue();
                mTurnData.boxes[i % mTurnData.rows][i / mTurnData.columns] = 1 + (t % (size / 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMatch = match;
        String playerId = Games.Players.getCurrentPlayerId(Game.mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);
        Games.TurnBasedMultiplayer.takeTurn(Game.mGoogleApiClient, match.getMatchId(), mTurnData.persist(), myParticipantId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
            }
        });
    }

    public void showTurnBasedMatch(TurnBasedMatch match) {
        mTurnData.unpersist(match.getData());
        Game.ROWS = mTurnData.rows;
        Game.COLUMNS = mTurnData.columns;
        Game.pointsJ1 = mTurnData.pointsP1;
        Game.pointsJ2 = mTurnData.pointsP2;
        Game.turn = mTurnData.playerTurn;
        turnTurnBasedMatch = mTurnData.playerTurn;
        Game.boxes = new int[mTurnData.columns][mTurnData.rows];
        Game.boxes = mTurnData.boxes;
        mostrarTablero();
    }

    public String getNextPlayerId() {
        String playerId = Games.Players.getCurrentPlayerId(Game.mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);
        ArrayList<String> participantIds = mMatch.getParticipantIds();
        int desiredIndex = -1;
        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }
        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }
        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            return participantIds.get(0);
        } else {
            return null;
        }

    }
}

