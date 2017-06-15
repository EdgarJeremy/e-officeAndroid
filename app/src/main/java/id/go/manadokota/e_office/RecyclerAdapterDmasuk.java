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

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.colindodd.toggleimagebutton.ToggleImageButton;

import java.util.List;

/**
 * Created by edgar on 5/4/17.
 */

class RecyclerAdapterDmasuk extends RecyclerView.Adapter<RecyclerAdapterDmasuk.RecyclerViewHolder> {

    private static List<JsonObject> daftar_disposisi;
    private static Context ctx;

    RecyclerAdapterDmasuk(List<JsonObject> daftar_disposisi, Context ctx) {
        this.daftar_disposisi = daftar_disposisi;
        this.ctx = ctx;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dmasuk,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        // TODO: 5/4/17 Populate textview dengan JsonObject
        JsonObject item = daftar_disposisi.get(position);

        holder.txPerihal.setText((item.get("subjek").toString().equals("null")) ? "Tidak ada surat terlampir" : item.get("subjek").getAsString());

        holder.txNamaPengirim.setText((item.get("nama_lengkap").getAsString()));
        holder.txInstruksiDisposisi.setText(item.get("instruksi_disposisi").getAsString());
        holder.txWaktuKirim.setText(item.get("waktu_kirim").getAsString());

        if(item.get("dibaca").getAsInt() == 0) {
            holder.cardView.setBackgroundColor(Color.parseColor("#ffe78a"));
        }

        if(item.get("starred").getAsInt() == 0)
            holder.btnStarred.setChecked(false);
        else
            holder.btnStarred.setChecked(true);

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
        TextView txNamaPengirim;
        public TextView txInstruksiDisposisi;
        public TextView txWaktuKirim;
        public Button btnBaca;
        public Button btnHapus;
        public CardView cardView;
        public ToggleImageButton btnStarred;

        public RecyclerViewHolder(View view) {
            super(view);
            cardView = (CardView)view.findViewById(R.id.card_view);
            txPerihal = (TextView)view.findViewById(R.id.txPerihal);
            txNamaPengirim = (TextView)view.findViewById(R.id.txNamaPengirim);
            txInstruksiDisposisi = (TextView)view.findViewById(R.id.txInstruksiDisposisi);
            txWaktuKirim = (TextView)view.findViewById(R.id.txWaktuKirim);
            btnBaca = (Button)view.findViewById(R.id.btnBaca);
            btnHapus = (Button)view.findViewById(R.id.btnHapus);
            btnStarred = (ToggleImageButton)view.findViewById(R.id.btnStarred);

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
            btnStarred.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBintang(btnStarred.isChecked(), daftar_disposisi.get(getAdapterPosition()).get("id_relasi_disposisi").getAsInt());
                }
            });
        }

        private void handleBaca(JsonObject itemData) {
            Intent intent = new Intent(ctx,BacaDmasukActivity.class);
            intent.putExtra("id_disposisi",itemData.get("id_disposisi").getAsString());
            intent.putExtra("kode_disposisi",itemData.get("kode_disposisi").getAsString());
            ctx.startActivity(intent);
        }

        private void handleHapus() {

        }

        private void updateBintang(final Boolean checked,int id_relasi_disposisi) {
            Ion.with(ctx)
                    .load(Config.API_BASE_URL + "/update_bintang_disposisi")
                    .setBodyParameter("id_relasi_disposisi",Integer.toString(id_relasi_disposisi))
                    .setBodyParameter("starred",(checked) ? "1" : "0")
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if(e != null) {
                                Log.d(ctx.getPackageName(),e.getMessage());
                                btnStarred.setChecked(!checked);
                                Toast.makeText(ctx,"Terjadi kesalahan!",Toast.LENGTH_LONG).show();
                            } else {
                                Log.d(ctx.getPackageName(),result);
                                Toast.makeText(ctx,"Disposisi diupdate",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

}
