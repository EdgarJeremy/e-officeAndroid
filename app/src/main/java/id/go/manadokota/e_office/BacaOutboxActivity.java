package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;

import java.util.Map;

public class BacaOutboxActivity extends AppCompatActivity {
    public TextView txPerihal;
    public TextView txNamaPenerima;
    public TextView txWaktuKirim;
    public TextView txIsiPesan;
    public LinearLayout wrapper;

    public Map pengguna;

    public String id_pesan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_outbox);
        id_pesan = getIntent().getStringExtra("id_pesan");

        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Baca Surat");
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Baca Surat");
        }

        Config.session_start(BacaOutboxActivity.this);

        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(BacaOutboxActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        txPerihal = (TextView)findViewById(R.id.txPerihal);
        txNamaPenerima = (TextView)findViewById(R.id.txNamaPenerima);
        txWaktuKirim = (TextView)findViewById(R.id.txWaktuKirim);
        txIsiPesan = (TextView)findViewById(R.id.txIsiPesan);

        wrapper = (LinearLayout)findViewById(R.id.header_surat);
        wrapper.setVisibility(View.INVISIBLE);


        populate(id_pesan);
    }

    private void populate(String id_pesan) {

        final ProgressDialog progressDialog = new ProgressDialog(BacaOutboxActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Ion.with(BacaOutboxActivity.this)
                .load(Config.API_BASE_URL + "/ambil_satu_surat_keluar")
                .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                .setBodyParameter("id_pesan",id_pesan)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        if(e != null) {
                            Log.d(getPackageName(),e.getMessage());
                            Toast.makeText(BacaOutboxActivity.this,"Kesalahan saat menghubungi server",Toast.LENGTH_LONG).show();
                        } else {
                            txPerihal.setText(result.get("subjek").getAsString());
                            String forPenerima = "<b>Penerima : </b>";
                            JsonArray penerima = result.get("dikirim").getAsJsonArray();

                            for(int i=0;i<penerima.size();i++) {
                                JsonObject orang = penerima.get(i).getAsJsonObject();
                                forPenerima += "\n- " + orang.get("nama_lengkap").getAsString();
                                forPenerima += (orang.get("dibaca").getAsInt() == 1) ? " (dibaca)" : "";
                            }

                            txNamaPenerima.setText(Html.fromHtml(forPenerima));
                            txWaktuKirim.setText(Html.fromHtml("<b>Waktu Kirim : </b>".concat(result.get("waktu_kirim").getAsString())));
                            txIsiPesan.setText(Html.fromHtml(result.get("isi_pesan").getAsString()));
                            wrapper.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
