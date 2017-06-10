package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Map;

public class PengaturanActivity extends AppCompatActivity {

    public Map pengguna;
    public EditText etUsername;
    public EditText etPassword;
    public EditText etPasswordLama;
    public Button btnSimpanPengaturan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pengaturan");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getActionBar().setTitle("Pengaturan");
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Config.session_start(PengaturanActivity.this);

        if(!Prefs.getBoolean("loggedIn",true)) {
            startActivity(new Intent(PengaturanActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        etPasswordLama = (EditText)findViewById(R.id.etPasswordLama);
        btnSimpanPengaturan = (Button)findViewById(R.id.btnSimpanPengaturan);

        resetIsiField();

        btnSimpanPengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PengaturanActivity.this);
                alertDialog.setTitle("Apakah anda yakin ingin menyimpan data baru ini?");
                alertDialog.setMessage("Data akan dikirim ke server dan saat anda login lagi, sudah harus menggunakan data baru ini. Lanjutkan?");
                alertDialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(PengaturanActivity.this);
                        progressDialog.setTitle("Loading..");
                        progressDialog.setMessage("Mengirim ke server..");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        final String sUsername = etUsername.getText().toString();
                        final String sPassword = etPassword.getText().toString();
                        String sPasswordLama = etPasswordLama.getText().toString();

                        Ion.with(PengaturanActivity.this)
                                .load(Config.API_BASE_URL + "/edit_data_pengguna")
                                .progressDialog(progressDialog)
                                .setBodyParameter("id_pengguna",pengguna.get("id_pengguna").toString())
                                .setBodyParameter("username",sUsername)
                                .setBodyParameter("password",sPassword)
                                .setBodyParameter("cpassword",sPasswordLama)
                                .setBodyParameter("sesspassword",pengguna.get("password").toString())
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        progressDialog.cancel();
                                        if(e != null) {
                                            Log.d("Salah",e.getMessage());
                                            Toast.makeText(PengaturanActivity.this, R.string.server_error_msg,Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.d("Kembalian",result.toString());
                                            int stat = result.get("statusCode").getAsInt();
                                            if(stat == 3) {
                                                Toast.makeText(PengaturanActivity.this, "Data berhasil disimpan!", Toast.LENGTH_LONG).show();
                                                Prefs.putString("username", sUsername);
                                                Prefs.putString("password",(sPassword.equals("")) ? pengguna.get("password").toString() : Config.md5(sPassword));
                                                pengguna = Prefs.getAll();
                                            } else if(stat == 2) {
                                                Toast.makeText(PengaturanActivity.this,"Password lama tidak cocok!",Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(PengaturanActivity.this,"Terjadi kesalahan saat menyimpan di database!",Toast.LENGTH_LONG).show();
                                            }
                                            resetIsiField();
                                        }
                                    }
                                });

                    }
                });
                alertDialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alertDialog.show();


            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(PengaturanActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resetIsiField() {
        etUsername.setText(pengguna.get("username").toString());
        etPassword.setText("");
        etPasswordLama.setText("");
    }
}
