package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameEt = findViewById(R.id.playerNameEt);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameBtn);

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //obteniendo el nombre del EditText hacia una variable tipo String
                final String getPlayerName = playerNameEt.getText().toString();

                //viendo si el jugador ha introducido su nombre
                if(getPlayerName.isEmpty()){
                    Toast.makeText(PlayerName.this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show();
                }
                else{

                    //creando intento de abrir el MainActivity
                    Intent intent = new Intent (PlayerName.this, MainActivity.class);

                    //añadiendo nombre del jugador con intent
                    intent.putExtra("playerName", getPlayerName);

                    //abriendo MainActiviy
                    startActivity(intent);

                    //destruir el nombre del jugador de la actividad
                    finish();
                }
            }
        });
    }
}