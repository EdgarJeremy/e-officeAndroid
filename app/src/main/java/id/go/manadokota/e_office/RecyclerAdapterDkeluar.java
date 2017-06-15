package id.go.manadokota.e_office;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.colindodd.toggleimagebutton.ToggleImageButton;

import java.util.List;

/**
 * Created by edgar on 5/4/17.
 */

class RecyclerAdapterDkeluar extends RecyclerView.Adapter<RecyclerAdapterDkeluar.RecyclerViewHolder> {

    private static List<JsonObject> daftar_disposisi;
    private static Context ctx;

    RecyclerAdapterDkeluar(List<JsonObject> daftar_disposisi, Context ctx) {
        this.daftar_disposisi = daftar_disposisi;
        this.ctx = ctx;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dkeluar,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        // TODO: 5/4/17 Populate textview dengan JsonObject
        JsonObject item = daftar_disposisi.get(position);

        holder.txPerihal.setText((item.get("subjek").toString().equals("null")) ? "Tidak ada surat terlampir" : item.get("subjek").getAsString());

        JsonArray penerima = item.get("penerima").getAsJsonArray();
        String daftarPenerima = "";
        for(int i=0;i<penerima.size();i++) {
            JsonObject siPenerima = penerima.get(i).getAsJsonObject();
            daftarPenerima += String.valueOf(i+1) + "." + siPenerima.get("nama_lengkap").getAsString() + ((siPenerima.get("dibaca").getAsInt() == 1) ? "(dibaca)\n" : "\n");
        }
        holder.txPenerima.setText(daftarPenerima);

        holder.txInstruksiDisposisi.setText(item.get("instruksi_disposisi").getAsString());
        holder.txWaktuKirim.setText(item.get("waktu_kirim").getAsString());

    }

    public String stripTags(String html) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }


    public void clear() {
        daftar_disposisi.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<JsonObject> listBaru) {
        daftar_disposisi.addAll(listBaru);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return daftar_disposisi.size();
    }


    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView txPerihal;
        TextView txPenerima;
        public TextView txInstruksiDisposisi;
        public TextView txWaktuKirim;
        public Button btnBaca;
        public Button btnHapus;
        public CardView cardView;

        public RecyclerViewHolder(View view) {
            super(view);
            cardView = (CardView)view.findViewById(R.id.card_view);
            txPerihal = (TextView)view.findViewById(R.id.txPerihal);
            txPenerima = (TextView)view.findViewById(R.id.txPenerima);
            txInstruksiDisposisi = (TextView)view.findViewById(R.id.txInstruksiDisposisi);
            txWaktuKirim = (TextView)view.findViewById(R.id.txWaktuKirim);
            btnBaca = (Button)view.findViewById(R.id.btnBaca);
            btnHapus = (Button)view.findViewById(R.id.btnHapus);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBaca(daftar_disposisi.get(getAdapterPosition()));
                }
            });
            btnBaca.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBaca(daftar_disposisi.get(getAdapterPosition()));
                }
            });
        }

        private void handleBaca(JsonObject itemData) {
            Intent intent = new Intent(ctx,BacaDkeluarActivity.class);
            intent.putExtra("id_disposisi",itemData.get("id_disposisi").getAsString());
            intent.putExtra("kode_disposisi",itemData.get("kode_disposisi").getAsString());
            ctx.startActivity(intent);
        }

        private void handleHapus() {

        }


    }

}
