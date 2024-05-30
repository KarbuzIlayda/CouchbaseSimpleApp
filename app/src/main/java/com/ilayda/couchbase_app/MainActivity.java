package com.ilayda.couchbase_app;

import static android.content.ContentValues.TAG;
import static com.couchbase.lite.internal.CouchbaseLiteInternal.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CollectionConfiguration;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.Expression;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.ReplicatorType;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.URLEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String kulad, kulsifre;
    Button kitapekle;
    RecyclerView kitapliste;
    String yazar, kategori, baslik, orijinaldil, aciklama, tarih;
    Date basimtarih;
    private KitapAdapter kitapadapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kitapekle = findViewById(R.id.kitapekle);
        kitapliste = findViewById(R.id.kitapliste);

        kitapliste.setHasFixedSize(true);
        kitapliste.setLayoutManager(new LinearLayoutManager(this));

        Bundle mevcutkul = getIntent().getBundleExtra("kullanicibilgileri");
        if(mevcutkul != null){
            kulad = mevcutkul.getString("kullaniciad");
            kulsifre = mevcutkul.getString("kullanicisifre");
        }
        CouchbaseLite.init(this);

        kitapekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle kulbilgileri = new Bundle();
                kulbilgileri.putString("kullaniciad", kulad);
                kulbilgileri.putString("kullanicisifre", kulsifre);
                Intent intent = new Intent(MainActivity.this, KitapEkle.class);
                intent.putExtra("kullanicibilgileri", kulbilgileri);
                startActivity(intent);
            }
        });

        getir();
        if(kitapadapt != null){
            kitapliste.setAdapter(kitapadapt);
        }
        /*
        DatabaseConfiguration config = new DatabaseConfiguration();

        File dbDirectory = new File(getFilesDir(), "notes");
        if (!dbDirectory.exists()) {
            dbDirectory.mkdirs(); // Create the directory if it does not exist
        }
        config.setDirectory(dbDirectory.getAbsolutePath());

        Database database = null;
        try {
            database = new Database("booksdb", config);
        } catch (CouchbaseLiteException e) {
            throw new RuntimeException(e);
        }

        URI uri = null;
        try {
            uri = new URI("ws://10.0.2.2:4984/booksdb");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Endpoint endpoint = new URLEndpoint(uri);
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(database, endpoint);

        replConfig.setType(ReplicatorType.PUSH_AND_PULL)
                .setContinuous(true);
        replConfig.setAuthenticator(new BasicAuthenticator("ilayda", "password".toCharArray()));
        Log.d(TAG, "Authenticator set");

        // Create the replicator
        Replicator replicator = new Replicator(replConfig);
        Log.d(TAG, "Replicator created"+replicator.getStatus().toString());

        // Start the replicator
        replicator.start();
        Log.d(TAG, "Replicator started");

        replicator.addChangeListener(change -> {
            CouchbaseLiteException err = change.getStatus().getError();
            if (err != null) {
                Log.e(TAG, "Replication Error", err);
            } else {
                Log.d(TAG, "Replication Status: " + change.getStatus().getActivityLevel());
            }
        });
        */
    }
    private void getir() {
        new Thread(() -> {
            try {
                List<Kitap> bookList = kitapBilgileriniAl();

                runOnUiThread(() -> {
                    kitapadapt = new KitapAdapter(bookList);
                    kitapliste.setAdapter(kitapadapt);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public List<Kitap> kitapBilgileriniAl() throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:4985/booksdb/_design/books/_view/byType")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            return bilgileriAyristir(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private List<Kitap> bilgileriAyristir(String json) throws IOException {
        List<Kitap> kitaplar = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray rows = jsonObject.getJSONArray("rows");

            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                JSONObject doc = row.getJSONObject("value");

                if(doc.has("yazar"))
                    yazar = doc.getString("yazar");
                else
                    yazar ="";
                if(doc.has("kategori"))
                    kategori = doc.getString("kategori");
                else
                    kategori = "";
                if(doc.has("baslik"))
                    baslik = doc.getString("baslik");
                else
                    baslik = "";
                if(doc.has("orijinal_dil"))
                    orijinaldil = doc.getString("orijinal_dil");
                else
                    orijinaldil = "";
                if(doc.has("aciklama"))
                    aciklama = doc.getString("aciklama");
                else
                    aciklama = "";
                if(doc.has("basim_tarihi"))
                    tarih = doc.getString("basim_tarihi");
                else
                    tarih = "";


                Kitap book = new Kitap(baslik, yazar, kategori, orijinaldil, tarih, aciklama);
                kitaplar.add(book);
            }
        } catch (Exception e) {
            throw new IOException("Error parsing JSON", e);
        }

        return kitaplar;
    }
}