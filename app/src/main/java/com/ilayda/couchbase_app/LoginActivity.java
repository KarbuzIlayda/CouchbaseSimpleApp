package com.ilayda.couchbase_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.client.java.manager.user.UserManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    UserManager umanage;
    EditText emailEt, sifreEt;
    Button btn_giris;
    TextView kayitadon;
    CheckBox sifregoster;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.editTextMail);
        sifreEt = findViewById(R.id.editTextSifre);
        btn_giris = findViewById(R.id.buttonGirisYap);
        kayitadon = findViewById(R.id.giristen_kayida);
        sifregoster = findViewById(R.id.checkbox_sifregoster);

        sifregoster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sifreEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else{
                    sifreEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        kayitadon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent giris = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(giris);
            }
        });

        btn_giris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = emailEt.getText().toString();
                String sifre = sifreEt.getText().toString();

                kullaniciKontrol(mail, sifre, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(LoginActivity.this, "Kullanici bilgileri alınamadı veya bulunamadı.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Log.d("gelen sonuç", responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Oturum başarıyla açıldı.\nHOŞGELDİNİZ...", Toast.LENGTH_LONG).show();
                            Bundle kulbilgileri = new Bundle();
                            kulbilgileri.putString("kullaniciad", String.valueOf(responseBody.indexOf("Username")));
                            kulbilgileri.putString("kullanicisifre", sifre);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("kullanicibilgileri", kulbilgileri);
                            startActivity(intent);
                        });
                    }
                });
            }
        });
    }
    public void kullaniciKontrol(String email, String password, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        //String requestBody = "{\"useremail\": \"" + email + "\"}";
        String requestBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";

        Request request = new Request.Builder()
                .url("http://10.0.2.2:4985/booksdb/_session")
                .post(RequestBody.create(mediaType, requestBody))
                .build();

        client.newCall(request).enqueue(callback);
    }
}