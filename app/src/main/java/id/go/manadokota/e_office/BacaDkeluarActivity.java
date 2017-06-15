package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pixplicity.easyprefs.library.Prefs;

import net.colindodd.toggleimagebutton.ToggleImageButton;

import java.util.ArrayList;
import java.util.Map;

public class BacaDkeluarActivity extends AppCompatActivity {


    public SwipeRefreshLayout srlRefresh;

    public ToggleImageButton btnStarred;

    public TextView txPenerima;
    public TextView txTanggalKirim;
    public TextView txInstruksi;
    public TextView txTanggalSelesai;
    public TextView txKeamanan;
    public TextView txKecepatan;
    public TextView txLampiran;
    public TextView txIsiDisposisi;
    public TextView txFollowUp;

    public EditText etFollowUp;

    public Button btnKirimFollowUp;

    public TextView txPerihalSurat;
    public TextView txWaktuKirimSurat;
    public TextView txIsiSurat;

    public TableLayout tlLampiranSurat;
    public TextView txSuratTidakAda;

    public Map pengguna;

    public String id_disposisi;
    public String kode_disposisi;

    public LinearLayout wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_dkeluar);


        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Baca Disposisi");
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Baca Disposisi");
        }

        Config.session_start(BacaDkeluarActivity.this);

        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(BacaDkeluarActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        srlRefresh = (SwipeRefreshLayout)findViewById(R.id.srlRefresh);

        txPenerima = (TextView)findViewById(R.id.txPenerima);
        txTanggalKirim = (TextView)findViewById(R.id.txTanggalKirim);
        txInstruksi = (TextView)findViewById(R.id.txInstruksi);
        txTanggalSelesai = (TextView)findViewById(R.id.txTanggalSelesai);
        txKeamanan = (TextView)findViewById(R.id.txKeamanan);
        txKecepatan = (TextView)findViewById(R.id.txKecepatan);
        txLampiran = (TextView)findViewById(R.id.txLampiran);
        txIsiDisposisi = (TextView)findViewById(R.id.txIsiDisposisi);
        txFollowUp = (TextView)findViewById(R.id.txFollowUp);

        etFollowUp = (EditText)findViewById(R.id.etFollowUp);
        btnKirimFollowUp = (Button)findViewById(R.id.btnKirimFollowUp);

        txPerihalSurat = (TextView)findViewById(R.id.txPerihalSurat);
        txWaktuKirimSurat = (TextView)findViewById(R.id.txWaktuKirimSurat);
        txIsiSurat = (TextView)findViewById(R.id.txIsiSurat);

        tlLampiranSurat = (TableLayout)findViewById(R.id.tlLampiranSurat);
        txSuratTidakAda = (TextView)findViewById(R.id.txSuratTidakAda);


        id_disposisi = getIntent().getStringExtra("id_disposisi");
        kode_disposisi = getIntent().getStringExtra("kode_disposisi");

        wrapper = (LinearLayout)findViewById(R.id.wrapper);

        populate(id_disposisi);

        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populate(id_disposisi);
            }
        });

    }

    private void populate(final String id_disposisi) {

        final ProgressDialog progressDialog = new ProgressDialog(BacaDkeluarActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Ion.with(BacaDkeluarActivity.this)
                .load(Config.API_BASE_URL + "/ambil_satu_disposisi_keluar")
                .setBodyParameter("id_disposisi",id_disposisi)
                .setBodyParameter("kode_disposisi",kode_disposisi)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        progressDialog.dismiss();

                        if(e != null) {
                            Log.d(getPackageName(),e.getMessage());
                            Toast.makeText(BacaDkeluarActivity.this,"Kesalahan saat menghubungi server",Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(getPackageName(),result.toString());
                            JsonArray penerima = result.get("penerima").getAsJsonArray();
                            String daftarPenerima = "";
                            final ArrayList<String> penerimaIds = new ArrayList<String>();
                            for(int i=0;i<penerima.size();i++) {
                                JsonObject itemPenerima = penerima.get(i).getAsJsonObject();
                                daftarPenerima += String.valueOf(i+1) + "." + itemPenerima.get("nama_lengkap").getAsString() + ((itemPenerima.get("dibaca").getAsInt() != 0) ? "(dibaca)" :"");
                                penerimaIds.add(i,itemPenerima.get("ke_user").getAsString());
                            }

                            txPenerima.setText(daftarPenerima);
                            txTanggalKirim.setText(result.get("waktu_kirim").getAsString());
                            txInstruksi.setText(result.get("instruksi_disposisi").getAsString());
                            txTanggalSelesai.setText(result.get("tanggal_selesai").getAsString());
                            if(result.get("lampiran").toString().equals("null")) {
                                txLampiran.setText("Tidak ada lampiran");
                            } else {
                                String lampiranStrArray = result.get("lampiran").getAsString().replace("\\","");
                                JsonArray lampiran = new Gson().fromJson(lampiranStrArray,JsonArray.class);
                                String daftarLampiran = "";
                                for(int i=0;i<lampiran.size();i++) {
                                    JsonObject itemLampiran= lampiran.get(i).getAsJsonObject();
                                    daftarLampiran += String.valueOf(i+1) + "." + itemLampiran.get("judul").getAsString() + "\n";
                                }
                                txLampiran.setText(daftarLampiran);
                            }
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                txIsiDisposisi.setText(Html.fromHtml(result.get("isi_disposisi").getAsString(),Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                txIsiDisposisi.setText(Html.fromHtml(result.get("isi_disposisi").getAsString()));
                            }

                            JsonArray followUp = result.get("follow_up").getAsJsonArray();
                            String daftarFollowUp;

                            if(followUp.size() > 0)
                                daftarFollowUp = "";
                            else
                                daftarFollowUp = "Belum ada follow up";

                            for(int i=0;i<followUp.size();i++) {
                                JsonObject itemFollowUp = followUp.get(i).getAsJsonObject();
                                String nama = (itemFollowUp.get("id_pengguna").getAsString().equals(pengguna.get("id_pengguna").toString())) ? "<b>[Anda]</b>" : "<b>[" + itemFollowUp.get("nama_lengkap").getAsString() + "]</b>";
                                daftarFollowUp += String.valueOf(i+1) + "." + nama + " " + itemFollowUp.get("isi_follow_up").getAsString() + "\n<br>";
                            }
                            txFollowUp.setText(Html.fromHtml(daftarFollowUp));

                            btnKirimFollowUp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String isi_follow_up = etFollowUp.getText().toString();
                                    if(isi_follow_up.equals("")) {
                                        Snackbar.make(v,"Isi detail follow up pada field yang tersedia",Snackbar.LENGTH_LONG).show();
                                    } else {
                                        progressDialog.show();
                                        Ion.with(BacaDkeluarActivity.this)
                                                .load(Config.API_BASE_URL + "/kirim_follow_up")
                                                .setBodyParameter("id_disposisi",id_disposisi)
                                                .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                                                .setBodyParameter("kode_disposisi",kode_disposisi)
                                                .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                                                .setBodyParameter("penerima",penerimaIds.toString())
                                                .setBodyParameter("isi_follow_up",isi_follow_up)
                                                .asString()
                                                .setCallback(new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result) {
                                                        Log.d(getPackageName(),result);
                                                        if(result.equals("true")) {
                                                            etFollowUp.setText("");
                                                            populate(id_disposisi);
                                                            Toast.makeText(BacaDkeluarActivity.this,"Follow up berhasil terkirim",Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(BacaDkeluarActivity.this,"Terjadi kesalahan pada server",Toast.LENGTH_LONG).show();
                                                        }
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                    }
                                }
                            });



                            // For lampiran surat

                            if(result.get("lampiran_surat").toString().equals("null")) {
                                txSuratTidakAda.setVisibility(View.VISIBLE);
                                tlLampiranSurat.setVisibility(View.GONE);
                            } else {
                                JsonObject lampiranSurat = result.get("lampiran_surat").getAsJsonObject();
                                txPerihalSurat.setText(lampiranSurat.get("subjek").getAsString());
                                txWaktuKirimSurat.setText(lampiranSurat.get("waktu_kirim").getAsString());
                                txIsiSurat.setText(lampiranSurat.get("isi_pesan").getAsString());
                                txSuratTidakAda.setVisibility(View.GONE);
                                tlLampiranSurat.setVisibility(View.VISIBLE);
                            }

                            wrapper.setVisibility(View.VISIBLE);

                            if(srlRefresh.isRefreshing())
                                srlRefresh.setRefreshing(false);
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
