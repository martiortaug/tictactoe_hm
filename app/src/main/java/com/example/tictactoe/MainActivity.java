package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Partida");

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;

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
        firebaseInit();
    }

    private void firebaseInit() {
        // Create a map to represent the new data
        Map<String, Object> partidaData = new HashMap<>();
        partidaData.put("A", Arrays.asList(null, "", "", ""));
        partidaData.put("B", Arrays.asList(null, "", "", ""));
        partidaData.put("C", Arrays.asList(null, "", "", ""));
        partidaData.put("Fecha", "");
        partidaData.put("Jugador1", "");
        partidaData.put("Jugador2", "");

        // Write the new data to the "Partida" node
        myRef.setValue(partidaData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        // TODO: Do something with the success (like showing a success message to the user)
                        Log.d("MainActivity", "Write to database was successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        // TODO: Do something with the failure (like showing an error message to the user)
                        Log.w("MainActivity", "Failed to write to database", e);
                    }
                });
    }
}