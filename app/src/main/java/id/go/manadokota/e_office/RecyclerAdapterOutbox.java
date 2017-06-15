package id.go.manadokota.e_office;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by edgar on 5/4/17.
 */

public class RecyclerAdapterOutbox extends RecyclerView.Adapter<RecyclerAdapterOutbox.RecyclerViewHolder> {

    private static List<JsonObject> daftar_surat;
    public static Context ctx;

    RecyclerAdapterOutbox(List<JsonObject> daftar_surat, Context ctx) {
        this.daftar_surat = daftar_surat;
        this.ctx = ctx;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_outbox,parent,false);
        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
        return recyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        // TODO: 5/4/17 Populate textview dengan JsonObject
        JsonObject item = daftar_surat.get(position);
        holder.txPerihal.setText(item.get("subjek").getAsString());
        holder.txWaktuKirim.setText(item.get("waktu_kirim").getAsString());
        holder.txRingkasan.setText(item.get("isi_pesan").getAsString());


    }

    public String stripTags(String html) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }


    public void clear() {
        daftar_surat.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<JsonObject> listBaru) {
        daftar_surat.addAll(listBaru);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return daftar_surat.size();
    }


    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView txPerihal;
        public TextView txWaktuKirim;
        public TextView txRingkasan;
        public Button btnBaca;
        public Button btnHapus;
        public CardView cardView;

        public RecyclerViewHolder(View view) {
            super(view);
            cardView = (CardView)view.findViewById(R.id.card_view);
            txPerihal = (TextView)view.findViewById(R.id.txPerihal);
            txWaktuKirim = (TextView)view.findViewById(R.id.txWaktuKirim);
            txRingkasan = (TextView)view.findViewById(R.id.txRingkasan);
            btnBaca = (Button)view.findViewById(R.id.btnBaca);
            btnHapus = (Button)view.findViewById(R.id.btnHapus);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBaca(daftar_surat.get(getAdapterPosition()));
                }
            });
            btnBaca.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBaca(daftar_surat.get(getAdapterPosition()));
                }
            });
        }

        private void handleBaca(JsonObject itemData) {
            Intent intent = new Intent(ctx,BacaOutboxActivity.class);
            intent.putExtra("id_pesan",itemData.get("id_pesan").getAsString());
            ctx.startActivity(intent);
        }


    }

}
