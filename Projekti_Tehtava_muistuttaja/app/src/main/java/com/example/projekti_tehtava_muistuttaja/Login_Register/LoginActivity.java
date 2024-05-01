package com.example.projekti_tehtava_muistuttaja.Login_Register;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
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

// Luokka, joka käsittelee kirjautumisnäkymän toiminnan, perii luokan AppCompatActivity
public class LoginActivity extends AppCompatActivity {

    // Näkymän elementit ja FirebaseAuth muuttuja
    TextInputEditText editTextLoginEmail;
    TextInputEditText editTextLoginPassword;
    TextView textViewRegisterHere;
    Button buttonLogin;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asetetaan sovellusmuoto (night mode) pois päältä
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Lukitaan screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Käytetään layoutia activity_login
        setContentView(R.layout.activity_login);

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        textViewRegisterHere = findViewById(R.id.textViewRegisterHere);
        buttonLogin = findViewById(R.id.buttonLogin);

        mAuth = FirebaseAuth.getInstance();

        buttonLogin.setOnClickListener(view -> {
            loginUser();
        });
        // Rekisteröintitekstiä painettaessa aloitetaan aktiviteetti RegisterActivity
        textViewRegisterHere.setOnClickListener(view ->{
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    // Metodi käyttäjän kirjautumiseen
    private void loginUser(){
        String email = Objects.requireNonNull(editTextLoginEmail.getText()).toString();
        String password = Objects.requireNonNull(editTextLoginPassword.getText()).toString();

        // Tarkistetaan, että syötetty sähköpostiosoite ja salasana eivät ole tyhjiä
        if (TextUtils.isEmpty(email)){
            editTextLoginEmail.setError("Sähköposti ei voi olla tyhjä");
            editTextLoginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)){
            editTextLoginPassword.setError("Salasana ei voi olla tyhjä");
            editTextLoginPassword.requestFocus();
        } else {
            // Yritetään kirjautua sisään Firebase Autentikointi-instanssilla
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Käyttäjä sisäänkirjautunut onnistuneesti", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else{
                        Toast.makeText(LoginActivity.this, "Virhe kirjautuessa: Tarkasta sähköposti ja salasana", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}