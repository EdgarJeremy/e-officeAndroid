package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisposisiMasuk extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;
    public RecyclerAdapterDmasuk adapter;
    public RecyclerView.LayoutManager layoutManager;
    public Map pengguna;
    public List<JsonObject> daftar_disposisi = new ArrayList<JsonObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disposisi_masuk);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Disposisi Masuk");
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Disposisi Masuk");
        }

        Config.session_start(DisposisiMasuk.this);

        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(DisposisiMasuk.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        populate();

    }

    private void populate() {
        final ProgressDialog progressDialog = new ProgressDialog(DisposisiMasuk.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Ion.with(DisposisiMasuk.this)
                .load(Config.API_BASE_URL + "/ambil_disposisi_masuk")
                .progressDialog(progressDialog)
                .noCache()
                .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        progressDialog.dismiss();
                        if(e != null) {
                            Log.d(getPackageName(),e.getMessage());
                            Toast.makeText(DisposisiMasuk.this,"Kesalahan saat menghubungi server",Toast.LENGTH_LONG).show();
                        } else {
                            int i = 0;
                            for(JsonElement el: result) {
                                daftar_disposisi.add(el.getAsJsonObject());
                            }
                            recyclerView = (RecyclerView)findViewById(R.id.rcvListDmasuk);
                            layoutManager = new LinearLayoutManager(DisposisiMasuk.this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            adapter = new RecyclerAdapterDmasuk(daftar_disposisi,DisposisiMasuk.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Ion.with(DisposisiMasuk.this)
                        .load(Config.API_BASE_URL + "/ambil_disposisi_masuk")
                        .progressDialog(progressDialog)
                        .noCache()
                        .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                        .asJsonArray()
                        .setCallback(new FutureCallback<JsonArray>() {
                            @Override
                            public void onCompleted(Exception e, JsonArray result) {
                                progressDialog.dismiss();
                                if(e != null) {
                                    Log.d(getPackageName(),e.getMessage());
                                    Toast.makeText(DisposisiMasuk.this,"Kesalahan terjadi saat menghubungi server",Toast.LENGTH_LONG).show();
                                    swipeRefreshLayout.setRefreshing(false);
                                } else {
                                    List<JsonObject> surat2_baru = new ArrayList<JsonObject>();
                                    for(JsonElement el: result) {
                                        surat2_baru.add(el.getAsJsonObject());
                                    }
                                    adapter.clear();
                                    adapter.addAll(surat2_baru);
                                    Log.d(getPackageName(),surat2_baru.toString());
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DisposisiMasuk.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
