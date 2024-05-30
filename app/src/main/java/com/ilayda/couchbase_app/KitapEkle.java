package com.ilayda.couchbase_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Objects;

public class KitapEkle extends AppCompatActivity {
    String kulad, kulsifre;
    private Database database;
    EditText kitapad, yazarad, kategori, basimtarih, orijdil, aciklama;
    Button kitapekle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitap_ekle);

        CouchbaseLite.init(getApplicationContext());
        try {
            database = new Database("books", new DatabaseConfiguration());
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        kitapad = findViewById(R.id.kitapadi);
        yazarad = findViewById(R.id.yazaradi);
        kategori = findViewById(R.id.kategori);
        basimtarih = findViewById(R.id.yayimtarihi);
        orijdil = findViewById(R.id.orijinaldil);
        aciklama = findViewById(R.id.aciklama);
        kitapekle = findViewById(R.id.kitapekle);

        Bundle mevcutkul = getIntent().getBundleExtra("kullanicibilgileri");
        if(mevcutkul != null){
            kulad = mevcutkul.getString("kullaniciad");
            kulsifre = mevcutkul.getString("kullanicisifre");
            //Log.d("kullanıcı adı", kulad);
            //Log.d("kullanıcı şifresi", kulsifre);
        }

        CouchbaseLite.init(this);

        kitapekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kad = kitapad.getText().toString();
                String yad = yazarad.getText().toString();
                String kat = kategori.getText().toString();
                String bastar = basimtarih.getText().toString();
                String orijinal = orijdil.getText().toString();
                String acik = aciklama.getText().toString();

                Kitap kitap = new Kitap();
                MutableDocument doc = new MutableDocument();
                doc.setString("type", "book");

                if(!kad.isEmpty()){
                    kitap.setBaslik(kad);
                    doc.setString("baslik", kitap.getBaslik());
                }
                if(!yad.isEmpty()){
                    kitap.setYazar(yad);
                    doc.setString("yazar", kitap.getYazar());
                }
                if(!kat.isEmpty()){
                    kitap.setKategori(kat);
                    doc.setString("kategori", kitap.getKategori());
                }
                if(!bastar.isEmpty()){
                    kitap.setYayimtarihi(bastar);
                    doc.setString("basim_tarihi", kitap.getYayimtarihi());
                }
                if(!orijinal.isEmpty()){
                    kitap.setOrijinaldili(orijinal);
                    doc.setString("orijinal_dil", kitap.getOrijinaldili());
                }
                if(!acik.isEmpty()){
                    kitap.setAciklama(acik);
                    doc.setString("aciklama", kitap.getAciklama());
                }

                try {
                    Objects.requireNonNull(database.getDefaultCollection()).save(doc);
                    Toast.makeText(KitapEkle.this, "Kitap başarıyla yerel alana kaydedildi", Toast.LENGTH_SHORT).show();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    Toast.makeText(KitapEkle.this, "Kitap yerel alana kaydedilemedi", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    URI syncGatewayURI = new URI("ws://10.0.2.2:4984/booksdb");
                    ReplicatorConfiguration config = new ReplicatorConfiguration(database, new URLEndpoint(syncGatewayURI));
                    config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
                    config.setContinuous(true);
                    config.setAuthenticator(new BasicAuthenticator("kitap", kulsifre.toCharArray()));

                    Replicator replicator = new Replicator(config);
                    replicator.start();
                    Toast.makeText(KitapEkle.this, "Veriler yükleniyor...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(KitapEkle.this, MainActivity.class));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    Toast.makeText(KitapEkle.this, "Veri tabanı ile bağlantı problemi oluştu.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database when the activity is destroyed
        if (database != null) {
            try {
                database.close();
            } catch (CouchbaseLiteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}