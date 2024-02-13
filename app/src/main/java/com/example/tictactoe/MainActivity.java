package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;

    // combinaciones ganadoras
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<>();

    private String playerUniqueId = "0";

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe-hm-default-rtdb.europe-west1.firebasedatabase.app/");


    private boolean opponentFound = false;
    private String opponentUniqueId = "0";
    private String status = "matching";
    private String playerTurn = "";
    private String connectionId = "";
    ValueEventListener turnsEventListener, wonEventListener;
    private final String[] boxesSelectedBy = {"", "", "", "", "", "", "", "", ""};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);

        //obteniendo el PlayerName del archivo PlayerName.class
        final String getPlayerName = getIntent().getStringExtra("playerName");

        // generando las combinaciones ganadoras
        combinationsList.add(new int[]{0, 1, 2});
        combinationsList.add(new int[]{3, 4, 5});
        combinationsList.add(new int[]{6, 7, 8});
        combinationsList.add(new int[]{0, 3, 6});
        combinationsList.add(new int[]{1, 4, 7});
        combinationsList.add(new int[]{2, 5, 8});
        combinationsList.add(new int[]{0, 4, 8});
        combinationsList.add(new int[]{2, 4, 6});

        // mostrar progressDialog mientras se espera al otro jugador
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Esperando al otro jugador...");
        progressDialog.show();

        // generar un id único para el jugador
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        // mostrar el nombre del jugador en el layout
        player1TV.setText(getPlayerName);

        databaseReference.child("conexiones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!opponentFound){
                    if (snapshot.hasChildren()) {

                        for (DataSnapshot conexiones: snapshot.getChildren()) {

                            String conId = conexiones.getKey();

                            int getPlayersCount = (int)conexiones.getChildrenCount();

                            if (status.equals("waiting")) {
                                if (getPlayersCount == 2) {
                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);

                                    boolean playerFound = false;

                                    for (DataSnapshot players : conexiones.getChildren())
                                    {
                                        String getPlayerUniqueId = players.getKey();

                                        if (getPlayerUniqueId.equals(playerUniqueId)) {
                                            playerFound = true;
                                        }
                                        else if (playerFound) {
                                            String getOpponentName = players.child("player_name").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            player2TV.setText(getOpponentName);

                                            connectionId = conId;
                                            opponentFound = true;

                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            databaseReference.child("conexiones").removeEventListener(this);
                                        }
                                    }
                                }
                            }
                            else {
                                if (getPlayersCount == 1) {
                                    conexiones.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);
                                    for (DataSnapshot players : conexiones.getChildren()) {
                                        String getOpponentName = players.child("player_name").getValue(String.class);
                                        opponentUniqueId = players.getKey();

                                        playerTurn = opponentUniqueId;
                                        applyPlayerTurn(playerTurn);

                                        player2TV.setText(getOpponentName);

                                        connectionId = conId;
                                        opponentFound = true;

                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        databaseReference.child("conexiones").removeEventListener(this);
                                        break;

                                    }
                                }
                            }
                        }

                        if(!opponentFound && !status.equals("waiting")) {
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                            status = "waiting";
                        }

                    }

                    else {
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                        status = "waiting";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (dataSnapshot.getChildrenCount() == 2) {

                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));

                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);

                        if (!doneBoxes.contains(String.valueOf(getBoxPosition))) {
                            doneBoxes.add(String.valueOf(getBoxPosition));
                            if (getBoxPosition == 1) {
                                selectBox(image1, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 2) {
                                selectBox(image2, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 3) {
                                selectBox(image3, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 4) {
                                selectBox(image4, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 5) {
                                selectBox(image5, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 6) {
                                selectBox(image6, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 7) {
                                selectBox(image7, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 8) {
                                selectBox(image8, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 9) {
                                selectBox(image9, getBoxPosition, getPlayerId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("player_id")) {
                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);

                    final WinDialog windDialog;

                    if (getWinPlayerId.equals(playerUniqueId)) {
                        windDialog = new WinDialog(MainActivity.this, "¡Felicidades! Has ganado");
                    }
                    else {
                        windDialog = new WinDialog(MainActivity.this, "¡Lo siento! Has perdido");
                    }

                    windDialog.setCancelable(false);
                    windDialog.show();

                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("1") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("2") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("3") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("4") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("5") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("6") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("7") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("8") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doneBoxes.contains("9") && playerTurn.equals(playerUniqueId)) {
                    ((ImageView) v).setImageResource(R.drawable.cruz);
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    playerTurn = opponentUniqueId;
                }
            }
        });

    }

    private void applyPlayerTurn(String playerUniqueId2) {
        if (playerUniqueId2.equals(playerUniqueId)) {
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
        else {
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
    }

    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer) {
        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;
        if(selectedByPlayer.equals(playerUniqueId)) {
            imageView.setImageResource(R.drawable.cruz);
            playerTurn = opponentUniqueId;
        }
        else {
            imageView.setImageResource(R.drawable.circulo);
            playerTurn = playerUniqueId;
        }

        applyPlayerTurn(playerTurn);

        if (checkPlayerWin(selectedByPlayer)) {
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }

        if (doneBoxes.size() == 9) {
            final WinDialog winDialog = new WinDialog(MainActivity.this, "¡Empate!");
            winDialog.setCancelable(false);
            winDialog.show();
        }
    }

    private boolean checkPlayerWin(String playerId) {

        boolean playerWon = false;
        for (int i = 0; i < combinationsList.size(); i++) {
            final int[] combination = combinationsList.get(i);
            if (boxesSelectedBy[combination[0]].equals(playerId) && boxesSelectedBy[combination[1]].equals(playerId) && boxesSelectedBy[combination[2]].equals(playerId)) {
                playerWon = true;
                break;
            }
        }
        return playerWon;
    }
}