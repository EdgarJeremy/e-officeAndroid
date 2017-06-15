package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Map;

public class PilihPenerimaSurat extends AppCompatActivity {
    ArrayList<String> daftarNamaTerpilih = new ArrayList<>();
    ArrayList<String> daftarIdTerpilih = new ArrayList<>();
    ArrayList<String> daftarNama = new ArrayList<>();
    ArrayList<String> daftarId = new ArrayList<>();
    public ListView lvDaftarPengguna;
    FloatingActionButton btnSelesaiPilih;
    Map pengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_penerima_surat);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setTitle("Pilih Penerima");
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setTitle("Pilih Penerima");
        }

        Config.session_start(PilihPenerimaSurat.this);

        if(!Prefs.getBoolean("loggedIn",false)) {
            startActivity(new Intent(PilihPenerimaSurat.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        final ProgressDialog progressDialog = new ProgressDialog(PilihPenerimaSurat.this);
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Ion.with(PilihPenerimaSurat.this)
                .load(Config.API_BASE_URL + "/ambil_pengguna")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        progressDialog.cancel();
                        if(e != null) {
                            Log.d(getPackageName(),e.getMessage());
                            Toast.makeText(PilihPenerimaSurat.this,"Server bermasalah!",Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(getPackageName(),result.toString());
                            for(int i=0;i<result.size();i++){
                                JsonObject itemObject = result.get(i).getAsJsonObject();
                                daftarNama.add(itemObject.get("nama_lengkap").getAsString());
                                daftarId.add(itemObject.get("id_pengguna").getAsString());
                            }
                            buatListPilihan(daftarNama);
                        }
                    }
                });

        btnSelesaiPilih = (FloatingActionButton)findViewById(R.id.btnSelesaiPilih);
        btnSelesaiPilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(daftarIdTerpilih.size() > 0) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("daftarIdPenerima",daftarIdTerpilih);
                    resultIntent.putExtra("daftarNamaPenerima",daftarNamaTerpilih);
                    setResult(RESULT_OK,resultIntent);
                    finish();
                } else {
                    Snackbar.make(v,"Pilih penerima surat terlebih dahulu!",Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    protected void buatListPilihan(ArrayList<String> daftarNama) {
        lvDaftarPengguna = (ListView)findViewById(R.id.lvDaftarPengguna);
        lvDaftarPengguna.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PilihPenerimaSurat.this,R.layout.list_item_penerima,R.id.chkItemPenerima,daftarNama);
        lvDaftarPengguna.setAdapter(adapter);
        lvDaftarPengguna.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String namaTerpilih = ((TextView)view).getText().toString();
                String idTerpilih = daftarId.get(position);

                if(daftarNamaTerpilih.contains(namaTerpilih))
                    daftarNamaTerpilih.remove(namaTerpilih);
                else
                    daftarNamaTerpilih.add(namaTerpilih);

                if(daftarIdTerpilih.contains(idTerpilih))
                    daftarIdTerpilih.remove(idTerpilih);
                else
                    daftarIdTerpilih.add(idTerpilih);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}
