package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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