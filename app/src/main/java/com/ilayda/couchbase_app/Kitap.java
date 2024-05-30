package com.ilayda.couchbase_app;

import java.util.Date;

public class Kitap {
    private String baslik;
    private String yazar;
    private String kategori;
    private String orijinaldili;
    private String yayimtarihi;
    private String aciklama;

    public Kitap(){}

    public Kitap(String bas, String yaz, String kat, String od, String yt, String aciklama){
        setAciklama(aciklama);
        setBaslik(bas);
        setYazar(yaz);
        setKategori(kat);
        setOrijinaldili(od);
        setYayimtarihi(yt);
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setYayimtarihi(String yayimtarihi) {
        this.yayimtarihi = yayimtarihi;
    }

    public void setOrijinaldili(String orijinaldili) {
        this.orijinaldili = orijinaldili;
    }
    public void setBaslik(String bas){
        this.baslik = bas;
    }
    public void setYazar(String yaz){
        this.yazar = yaz;
    }

    public String getYazar(){
        return this.yazar;
    }

    public String getBaslik(){
        return this.baslik;
    }

    public String getYayimtarihi() {
        return yayimtarihi;
    }

    public String getKategori() {
        return kategori;
    }

    public String getOrijinaldili() {
        return orijinaldili;
    }

    public String getAciklama() {
        return aciklama;
    }
}
