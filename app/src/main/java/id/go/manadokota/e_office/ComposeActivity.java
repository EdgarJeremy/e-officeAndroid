package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ComposeActivity extends AppCompatActivity {

    public Map pengguna;
    public Button btnPilihPenerima;
    public Button btnPilihFile;
    public FloatingActionButton btnKirimSurat;
    public static int PILIH_PENERIMA_RCODE = 1;
    public static int PILIH_FILE_RCODE = 2;
    ArrayList<String> daftarNamaTerpilih;
    ArrayList<String> daftarIdTerpilih = null;
    EditText etDaftarNama;
    EditText etPerihalSurat;
    EditText etIsiSurat;
    public String fileTerpilih = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setTitle("Buat Surat");
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setTitle("Buat Surat");
        }

        Config.session_start(ComposeActivity.this);

        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(ComposeActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        etDaftarNama = (EditText)findViewById(R.id.etDaftarNama);
        etPerihalSurat = (EditText)findViewById(R.id.etPerihalSurat);
        etIsiSurat = (EditText)findViewById(R.id.etIsiSurat);

        btnPilihPenerima = (Button)findViewById(R.id.btnPilihPenerima);
        btnPilihFile = (Button)findViewById(R.id.btnPilihFile);
        btnKirimSurat = (FloatingActionButton)findViewById(R.id.btnKirimSurat);

        btnPilihPenerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ComposeActivity.this,PilihPenerimaSurat.class);
                startActivityForResult(i, PILIH_PENERIMA_RCODE);
            }
        });
        etDaftarNama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ComposeActivity.this,PilihPenerimaSurat.class);
                startActivityForResult(i, PILIH_PENERIMA_RCODE);
            }
        });

        btnPilihFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(ComposeActivity.this)
                        .withRequestCode(PILIH_FILE_RCODE)
                        .withFilterDirectories(true)
                        .withRootPath("/storage")
                        .withHiddenFiles(true)
                        .start();
            }
        });

        btnKirimSurat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String perihal = etPerihalSurat.getText().toString();
                String isi = etIsiSurat.getText().toString();

                if(perihal.equals("") || isi.equals("") || daftarIdTerpilih == null) {
                    Snackbar.make(v,"Lengkapi field yang ada!",Snackbar.LENGTH_LONG).show();
                } else {
                    kirimSurat(perihal,isi);
                }

            }
        });

    }

    public void kirimSurat(String perihal,String isi) {
        final ProgressDialog progressDialog = new ProgressDialog(ComposeActivity.this);
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Builders.Any.M mIon;

        if(fileTerpilih == null) {
            mIon = Ion.with(ComposeActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_surat")
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("subjek",perihal)
                    .setMultipartParameter("isi_pesan",isi);
        } else {
            mIon = Ion.with(ComposeActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_surat")
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("subjek",perihal)
                    .setMultipartParameter("isi_pesan",isi)
                    .setMultipartFile("file",new File(fileTerpilih));
        }



        mIon.asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        progressDialog.cancel();
                        if(e != null) {
                            e.printStackTrace();
                            Toast.makeText(ComposeActivity.this,"Server bermasalah!",Toast.LENGTH_LONG).show();
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ComposeActivity.this);
                            if(result.get("status").getAsInt() == 1) {
                                alertDialog.setTitle("Berhasil");
                                alertDialog.setMessage(result.get("pesan").getAsString());
                            } else {
                                alertDialog.setTitle("Gagal");
                                alertDialog.setMessage(result.get("pesan").getAsString());
                            }
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(result.get("status").getAsInt() == 1) {
                                        etDaftarNama.setText("");
                                        etPerihalSurat.setText("");
                                        etIsiSurat.setText("");
                                        daftarIdTerpilih.clear();
                                        fileTerpilih = null;
                                    }
                                }
                            });
                            alertDialog.show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == PILIH_PENERIMA_RCODE) {
            daftarIdTerpilih = data.getStringArrayListExtra("daftarIdPenerima");
            daftarNamaTerpilih = data.getStringArrayListExtra("daftarNamaPenerima");
            String listNama = "";
            for(String item:daftarNamaTerpilih) {
                listNama += item + ", ";
            }
            etDaftarNama.setText(listNama);
        }

        if(resultCode == RESULT_OK && requestCode == PILIH_FILE_RCODE) {
            fileTerpilih = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            File file = new File(fileTerpilih);
            btnPilihFile.setText(file.getName() + " (" + (file.length()/1024) + "kb)");
            Toast.makeText(ComposeActivity.this,fileTerpilih,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(ComposeActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
