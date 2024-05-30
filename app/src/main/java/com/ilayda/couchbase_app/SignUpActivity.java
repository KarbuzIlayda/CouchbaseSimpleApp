package com.ilayda.couchbase_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;

import android.content.Intent;
import android.os.Build;
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

import com.couchbase.client.core.deps.io.grpc.InternalChannelz;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.user.UserManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    public static final String email_regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    EditText mailet, kuladet,sifreet, sifretekraret;
    Button kayitol;
    CheckBox sifregoster;
    TextView girisedon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mailet = findViewById(R.id.editTextMail);
        sifreet = findViewById(R.id.editTextSifre);
        kuladet = findViewById(R.id.editTextKulAdi);
        sifretekraret = findViewById(R.id.editTextSifreOnay);
        kayitol = findViewById(R.id.buttonKayitOl);
        girisedon = findViewById(R.id.textViewGirisDon);
        sifregoster = findViewById(R.id.checkbox_sifregoster);

        kayitol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mailet.getText().toString();
                String sifre = sifreet.getText().toString();
                String kullaniciad = kuladet.getText().toString();
                String sifretekrar = sifretekraret.getText().toString();

                if (!email.isEmpty() && !kullaniciad.isEmpty() && !sifre.isEmpty() && !sifretekrar.isEmpty() && !kullaniciad.equals(" ") && email.matches(email_regex) && sifre.length() >= 6) {
                    if (sifre.equals(sifretekrar)) {
                        kullaniciOlustur(kullaniciad, sifre, email);
                    } else {
                        Toast.makeText(SignUpActivity.this, "Şifre uyuşmazlığı!\nŞifreleri kontrol ediniz!", Toast.LENGTH_LONG).show();
                    }
                } else if(!email.matches(email_regex)){
                    mailet.setError("abc@abc.abc şeklinde geçerli bir mail hesabı giriniz!");
                } else if(sifre.length() < 6 || sifretekrar.length() < 6){
                    sifreet.setError("Şifre uzunluğu 6 karakterden fazla olmalıdır!");
                } else {
                    Toast.makeText(SignUpActivity.this, "Lütfen tüm alanları doldurduğunuzdan emin olunuz!", Toast.LENGTH_LONG).show();
                }
            }
        });

        girisedon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        sifregoster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sifretekraret.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    sifreet.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else{
                    sifreet.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    sifretekraret.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }
    public void kullaniciOlustur(String kullaniciad, String sifre, String email) {
        OkHttpClient httpClient = new OkHttpClient();
        String uid = UUID.randomUUID().toString();
        String json = "{\"name\":\"" + kullaniciad + "\",\"password\":\"" + sifre + "\",\"admin_channels\":[\"users\"]," +
                "\"email\":\"" + email + "\",\"uid\":\"" + kullaniciad + "\"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

        //sync gateway üzerinde kullanıcı oluşturur ki replicator oluşturanilsin ve servera erişebilsin
        Request request = new Request.Builder()
                .url("http://10.0.2.2:4985/booksdb/_user/")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(SignUpActivity.this, "Bağlantı Sorunu\nKullanıcı oluşturulamadı!", Toast.LENGTH_LONG).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    //kulBilgisiServeraKaydet(uid, kullaniciad, sifre, email, "users");
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "HOŞGELDİNİZ...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                    });
                } else {
                    Log.d("response body", response.body().string());
                    Log.d("response code", String.valueOf(response.code()));
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "Herhangi bir senepten ötürü kullanıcı oluşturulamadı.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /* errordan kurtaramadım.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void kulBilgisiServeraKaydet(String id, String ad, String sifre, String mail, String channel){
        try {
            Log.d("kullanici bilgi fonksiyonu", "fonksiyona girildi");

            //ClusterOptions options = ClusterOptions.clusterOptions("books", "forbooks")
             //       .environment(env -> env.securityConfig(SecurityConfig.disableTls().enablePlainSaslMechanisms("PLAIN")));
            ClusterEnvironment env = ClusterEnvironment.builder()
                    .securityConfig(SecurityConfig
                            .enableTls(false)
                    )
                    .build();
            ClusterOptions options = ClusterOptions.clusterOptions("books", "forbooks").environment(env);

            Log.d("options durumu","opitons oluşturuldu" + options);
            Cluster cluster = Cluster.connect("couchbase://10.0.2.2", options);
            Log.d("cluster bağlantı", "bağlandı sanki" + cluster.buckets().toString());
            Bucket bucket = cluster.bucket("books-bucket");
            Log.d("bucket ulaşımı", "oldu sanki" + bucket.name());
            bucket.waitUntilReady(Duration.ofSeconds(120));

            JsonObject userDoc = JsonObject.create()
                    .put("type", "user")
                    .put("uid", id)
                    .put("username", ad)
                    .put("password", sifre)
                    .put("email", mail)
                    .put("channels", channel);
            Log.d("try öncesi", userDoc.toString());

            try {
                Log.d("upsert operation", "Attempting to upsert document: " + userDoc);
                MutationResult result = bucket.defaultCollection().upsert(ad, userDoc);
                Log.d("upsert result", "Kullanıcı dökümanı başarıyla oluşturuldu: " + result);

                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Kullanıcı dökümanı başarıyla oluşturuldu", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                });
            } catch (CouchbaseException e) {
                Log.d("couchbase exception", "bağlantısal bir sorun oldu"+ e.context());
                Toast.makeText(SignUpActivity.this, "Kullanıcı bilgilerini yüklemede sorun oluştu.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}