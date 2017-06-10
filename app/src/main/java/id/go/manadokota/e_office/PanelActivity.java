package id.go.manadokota.e_office;

import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Handler;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pl.droidsonroids.gif.GifImageView;

public class PanelActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public TextView tvNamaLengkap;
    public TextView tvJabatanDinas;
    public ImageView ivFotoProfil;
    public GifImageView anim;

    public Map pengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(!SocketService.SERVICE_RUNNING)
            startService(new Intent(PanelActivity.this,SocketService.class));

        if(getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getSupportActionBar().setIcon(R.drawable.ic_logo);
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            getActionBar().setIcon(R.drawable.ic_logo);
        }

        Config.session_start(PanelActivity.this);

        if(!Prefs.getBoolean("loggedIn",false)) {
            startActivity(new Intent(PanelActivity.this,LoginActivity.class));
            finish();
        } else {
            pengguna = Prefs.getAll();
        }

        // TODO: 4/2/17 lempar ke LoginActivity kalau Prefs kosong

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!pengguna.get("id_jabatan").toString().equals("1"))
            sembunyiMenuAdmin(navigationView);

        View hView = navigationView.getHeaderView(0);
        ivFotoProfil = (ImageView)hView.findViewById(R.id.fotoProfil);
        tvNamaLengkap = (TextView)hView.findViewById(R.id.tvNamaLengkap);
        tvJabatanDinas = (TextView)hView.findViewById(R.id.tvJabatanDinas);

        tvNamaLengkap.setText(pengguna.get("nama_lengkap").toString());
        tvJabatanDinas.setText(pengguna.get("nama_jabatan").toString() + " " + pengguna.get("nama_dinas").toString());

        anim = (GifImageView)findViewById(R.id.anim);

        anim.setImageResource(R.drawable.bubbles);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.panel, menu);
        return true;
    }

    private void sembunyiMenuAdmin(NavigationView nView) {
        Menu menu = nView.getMenu();
        menu.findItem(R.id.administrator_items).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.keluar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PanelActivity.this);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Apa anda yakin ingin keluar?");
            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.clear();
                    stopService(new Intent(PanelActivity.this,SocketService.class));
                    startActivity(new Intent(PanelActivity.this,LoginActivity.class));
                    finish();
                }
            });
            builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        } else if(id == R.id.profil) {

        } else if(id == R.id.pengaturan) {
            startActivity(new Intent(PanelActivity.this,PengaturanActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_panel) {

        } else if (id == R.id.menu_compose) {
            startActivity(new Intent(PanelActivity.this,ComposeActivity.class));
        } else if (id == R.id.menu_inbox) {
            startActivity(new Intent(PanelActivity.this,InboxActivity.class));
        } else if (id == R.id.menu_outbox) {

        } else if (id == R.id.menu_dmasuk) {

        } else if (id == R.id.menu_dkeluar) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
