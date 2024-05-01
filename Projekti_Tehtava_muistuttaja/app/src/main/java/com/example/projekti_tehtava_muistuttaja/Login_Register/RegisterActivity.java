package com.example.projekti_tehtava_muistuttaja.Login_Register;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

// Luokka, joka käsittelee rekisteröintinäkymän toiminnan, perii luokan AppCompatActivity
public class RegisterActivity extends AppCompatActivity {

    // Näkymän elementit ja FirebaseAuth muuttuja
    TextInputEditText editTextRegisterEmail;
    TextInputEditText editTextRegisterPassword;
    TextView textViewLoginHere;
    Button buttonRegister;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asetetaan sovellusmuoto (night mode) pois päältä
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Lukitaan screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Käytetään layoutia activity_register
        setContentView(R.layout.activity_register);

        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        textViewLoginHere = findViewById(R.id.textViewLoginHere);
        buttonRegister = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();

        buttonRegister.setOnClickListener(view ->{
            createUser();
        });

        // Kirjautumistekstiä painettaessa aloitetaan aktiviteetti LoginActivity
        textViewLoginHere.setOnClickListener(view ->{
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    // Metodi uuden käyttäjän luomiseen
    private void createUser(){
        String email = Objects.requireNonNull(editTextRegisterEmail.getText()).toString();
        String password = Objects.requireNonNull(editTextRegisterPassword.getText()).toString();

        // Tarkistetaan, että syötetty sähköpostiosoite ja salasana eivät ole tyhjiä
        if (TextUtils.isEmpty(email)){
            editTextRegisterEmail.setError("Sähköposti ei voi olla tyhjä");
            editTextRegisterEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)){
            editTextRegisterPassword.setError("Salasana ei voi olla tyhjä");
            editTextRegisterPassword.requestFocus();
        } else {
            // Yritetään rekisteröidä uusi käyttäjä Firebase Autentikointi-instanssilla
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Käyttäjä rekisteröity onnistuneesti", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        Log.d(MainActivity.TAG, Objects.requireNonNull(task.getException()).toString());
                        Toast.makeText(RegisterActivity.this, "Käyttäjän rekisteröinnissä virhe", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
