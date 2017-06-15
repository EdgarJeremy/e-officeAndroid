package id.go.manadokota.e_office;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pixplicity.easyprefs.library.Prefs;


public class LoginActivity extends AppCompatActivity {

    protected EditText etUsername;
    protected EditText etPassword;
    protected Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        else
            getActionBar().hide();

        Config.session_start(LoginActivity.this);

        if(Prefs.getBoolean("loggedIn",false)) {
            startActivity(new Intent(LoginActivity.this,PanelActivity.class));
            finish();
        }

        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View passedView = v;

                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if(!username.equals("") || !password.equals("")) {

                    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Menghubungi server..");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Ion.with(LoginActivity.this)
                            .load(Config.API_BASE_URL + "/login")
                            .setBodyParameter("username",username)
                            .setBodyParameter("password",password)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    progressDialog.cancel();
                                    if(e != null) {
                                        Log.d(getPackageName(),e.getMessage());
                                        Toast.makeText(LoginActivity.this,R.string.server_error_msg,Toast.LENGTH_LONG).show();
                                    } else{
                                        Log.d(getPackageName(),result.toString());
                                        int stat = result.get("status").getAsInt();
                                        if(stat == 1) {
                                            JsonObject userdata = result.get("userdata").getAsJsonObject();
                                            if(userdata.get("blokir").getAsInt() == 0) {
                                                Prefs.putBoolean("loggedIn",true);
                                                Prefs.putInt("id_pengguna",userdata.get("id_pengguna").getAsInt());
                                                Prefs.putInt("id_jabatan",userdata.get("id_jabatan").getAsInt());
                                                Prefs.putInt("id_dinas",userdata.get("id_dinas").getAsInt());
                                                Prefs.putString("username",userdata.get("username").getAsString());
                                                Prefs.putString("nama_lengkap",userdata.get("nama_lengkap").getAsString());
                                                Prefs.putString("nip",userdata.get("nip").getAsString());
                                                Prefs.putInt("disposisi",userdata.get("disposisi").getAsInt());
                                                Prefs.putString("nama_jabatan",userdata.get("nama_jabatan").getAsString());
                                                Prefs.putString("nama_dinas",userdata.get("nama_dinas").getAsString());
                                                Prefs.putString("password",userdata.get("password").getAsString());
                                                startActivity(new Intent(LoginActivity.this,PanelActivity.class));
                                                finish();
                                            } else {
//                                                Toast.makeText(LoginActivity.this,"Status pengguna anda sedang diblokir! Hubungi admin",Toast.LENGTH_LONG).show();
                                                Snackbar.make(passedView,"Status pengguna anda sedang diblokir! Hubungi admin",Snackbar.LENGTH_LONG).show();
                                            }
                                        } else {
//                                            Toast.makeText(LoginActivity.this,"Login Gagal! Periksa username dan password anda",Toast.LENGTH_LONG).show();
                                            Snackbar.make(passedView,"Login Gagal! Periksa username dan password anda",Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                } else {
                    Snackbar.make(v,"Isi semua field diatas!",Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }




}
