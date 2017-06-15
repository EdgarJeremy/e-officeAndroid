package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.pixplicity.easyprefs.library.Prefs;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class BuatDisposisiActivity extends AppCompatActivity {


    public Map pengguna;
    public ArrayList<String> daftarIdTerpilih = null;
    public ArrayList<String> daftarNamaTerpilih = null;
    public EditText etDaftarNama;
    public EditText etTanggalSelesai;
    public EditText etIsiDisposisi;
    public FloatingActionButton btnKirimDisposisi;
    public static int PILIH_PENERIMA_RCODE = 1;
    public static int PILIH_FILE_RCODE = 2;
    public String fileTerpilih = null;
    public Button btnPilihPenerima;
    public Button btnPilihFile;
    public Spinner spnInstruksiDisposisi;
    public String idpesan = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_disposisi);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setTitle("Buat Disposisi");
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setTitle("Buat Disposisi");
        }

        new Prefs.Builder()
                .setContext(BuatDisposisiActivity.this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(Config.PREFS_NAME)
                .setUseDefaultSharedPreference(true)
                .build();


        Config.session_start(BuatDisposisiActivity.this);


        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(BuatDisposisiActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
            Log.d(getPackageName(),pengguna.toString());
        }



        idpesan = getIntent().getStringExtra("idpesan");

        etDaftarNama = (EditText)findViewById(R.id.etDaftarNama);
        etIsiDisposisi = (EditText)findViewById(R.id.etIsiDisposisi);
        etTanggalSelesai = (EditText)findViewById(R.id.etTanggalSelesai);
        etTanggalSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                String hasilPilih = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                                etTanggalSelesai.setText(hasilPilih);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        spnInstruksiDisposisi = (Spinner)findViewById(R.id.spnInstruksiDisposisi);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BuatDisposisiActivity.this,R.array.instruksi_disposisi,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnInstruksiDisposisi.setAdapter(adapter);

        btnKirimDisposisi = (FloatingActionButton)findViewById(R.id.btnKirimDisposisi);
        btnPilihPenerima = (Button)findViewById(R.id.btnPilihPenerima);
        btnPilihFile = (Button)findViewById(R.id.btnPilihFile);

        btnPilihPenerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BuatDisposisiActivity.this,PilihPenerimaSurat.class);
                startActivityForResult(i,PILIH_PENERIMA_RCODE);
            }
        });

        etDaftarNama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BuatDisposisiActivity.this,PilihPenerimaSurat.class);
                startActivityForResult(i,PILIH_PENERIMA_RCODE);
            }
        });

        btnPilihFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(BuatDisposisiActivity.this)
                        .withRequestCode(PILIH_FILE_RCODE)
                        .withFilterDirectories(true)
                        .withRootPath("/storage")
                        .withHiddenFiles(true)
                        .start();
            }
        });

        btnKirimDisposisi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instruksi = spnInstruksiDisposisi.getSelectedItem().toString();
                String isi = etIsiDisposisi.getText().toString();
                String tanggal_selesai = etTanggalSelesai.getText().toString();

                if(instruksi.equals("") || isi.equals("") || tanggal_selesai.equals("") || daftarIdTerpilih == null) {
                    Snackbar.make(v,"Lengkapi field yang ada!",Snackbar.LENGTH_LONG).show();
                } else {
                    kirimDisposisi(instruksi,isi,tanggal_selesai);
                }
            }
        });


    }

    public void kirimDisposisi(String instruksi,String isi,String tanggal_selesai) {
        final ProgressDialog progressDialog = new ProgressDialog(BuatDisposisiActivity.this);
        progressDialog.setMessage("Menghubungi server..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Builders.Any.M mIon;

        if(fileTerpilih == null && idpesan == null) {
            mIon = Ion.with(BuatDisposisiActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_disposisi")
                    .setMultipartParameter("idpengguna",pengguna.get("id_pengguna").toString())
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("instruksi_disposisi",instruksi)
                    .setMultipartParameter("tanggal_selesai",tanggal_selesai)
                    .setMultipartParameter("isi_disposisi",isi);
        } else if(fileTerpilih != null && idpesan != null) {
            mIon = Ion.with(BuatDisposisiActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_disposisi")
                    .setMultipartParameter("idpengguna",pengguna.get("id_pengguna").toString())
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("instruksi_disposisi",instruksi)
                    .setMultipartParameter("tanggal_selesai",tanggal_selesai)
                    .setMultipartParameter("isi_disposisi",isi)
                    .setMultipartParameter("idpesan",idpesan)
                    .setMultipartFile("file",new File(fileTerpilih));
        } else if(fileTerpilih != null && idpesan == null) {
            mIon = Ion.with(BuatDisposisiActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_disposisi")
                    .setMultipartParameter("idpengguna",pengguna.get("id_pengguna").toString())
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("instruksi_disposisi",instruksi)
                    .setMultipartParameter("tanggal_selesai",tanggal_selesai)
                    .setMultipartParameter("isi_disposisi",isi)
                    .setMultipartFile("file",new File(fileTerpilih));
        } else {
            mIon = Ion.with(BuatDisposisiActivity.this)
                    .load(Config.API_BASE_URL + "/kirim_disposisi")
                    .setMultipartParameter("idpengguna",pengguna.get("id_pengguna").toString())
                    .setMultipartParameter("penerima",daftarIdTerpilih.toString())
                    .setMultipartParameter("instruksi_disposisi",instruksi)
                    .setMultipartParameter("tanggal_selesai",tanggal_selesai)
                    .setMultipartParameter("isi_disposisi",isi)
                    .setMultipartParameter("idpesan",idpesan);
        }



        mIon.asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        progressDialog.cancel();
                        if(e != null) {
                            e.printStackTrace();
                            Toast.makeText(BuatDisposisiActivity.this,"Server bermasalah!",Toast.LENGTH_LONG).show();
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BuatDisposisiActivity.this);
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
                                        etTanggalSelesai.setText("");
                                        etIsiDisposisi.setText("");
                                        daftarIdTerpilih.clear();
                                        fileTerpilih = null;
                                        finish();
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
            Toast.makeText(BuatDisposisiActivity.this,fileTerpilih,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}
