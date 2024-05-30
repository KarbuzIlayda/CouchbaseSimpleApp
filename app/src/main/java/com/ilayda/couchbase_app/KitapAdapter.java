package com.ilayda.couchbase_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class KitapAdapter extends RecyclerView.Adapter<KitapAdapter.BookViewHolder> {

    private List<Kitap> kitaplistesi;

    public KitapAdapter(List<Kitap> bookList) {
        this.kitaplistesi = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kitap, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Kitap kitap = kitaplistesi.get(position);

        if(kitap.getBaslik() != null && !kitap.getBaslik().isEmpty()) {
            holder.basliktw.setVisibility(View.VISIBLE);
            holder.basliktw.setText(kitap.getBaslik());
        } else {
            holder.basliktw.setVisibility(View.GONE);
        }
        if(kitap.getYazar() != null && !kitap.getYazar().isEmpty()) {
            holder.yazartw.setVisibility(View.VISIBLE);
            holder.yazartw.setText(kitap.getYazar());
        } else {
            holder.yazartw.setVisibility(View.GONE);
        }
        if(kitap.getKategori() != null && !kitap.getKategori().isEmpty()) {
            holder.kategoritw.setVisibility(View.VISIBLE);
            holder.kategoritw.setText(kitap.getKategori());
        } else {
            holder.kategoritw.setVisibility(View.GONE);
        }
        if(kitap.getOrijinaldili() != null && !kitap.getOrijinaldili().isEmpty()) {
            holder.oridiltw.setVisibility(View.VISIBLE);
            holder.oridiltw.setText(kitap.getOrijinaldili());
        } else {
            holder.oridiltw.setVisibility(View.GONE);
        }
        if(kitap.getAciklama() != null && !kitap.getAciklama().isEmpty()) {
            holder.aciklamatw.setVisibility(View.VISIBLE);
            holder.aciklamatw.setText(kitap.getAciklama());
        } else {
            holder.aciklamatw.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return kitaplistesi.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView yazartw, basliktw, kategoritw, aciklamatw, oridiltw, tarihtw;

        public BookViewHolder(View view) {
            super(view);
            yazartw = view.findViewById(R.id.yazar);
            kategoritw = view.findViewById(R.id.kategori);
            basliktw = view.findViewById(R.id.baslik);
            aciklamatw = view.findViewById(R.id.aciklama);
            oridiltw = view.findViewById(R.id.orijinaldil);
            tarihtw = view.findViewById(R.id.yayimtarihi);
        }
    }
}
