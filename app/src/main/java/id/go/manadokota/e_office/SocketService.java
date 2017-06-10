package id.go.manadokota.e_office;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by edgar on 5/3/17.
 */

public class SocketService extends Service {

    public static boolean SERVICE_RUNNING = false;
    public static int NOTIF_ID = 0;
    public Map pengguna;

    public Socket socket; {
        try {
            socket = IO.socket(Config.BASE_HOST + ":" + Config.SOCKET_PORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        socket.connect();
        SERVICE_RUNNING = true;

        new Prefs.Builder()
                .setContext(SocketService.this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(Config.PREFS_NAME)
                .setUseDefaultSharedPreference(true)
                .build();

        pengguna = Prefs.getAll();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("join",pengguna.get("id_pengguna").toString());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        socket.on("notifBaru", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject data = (JSONObject)args[0];

                try {
//                    Intent notifIntent = new Intent(SocketService.this, BacaSuratActivity.class);
//                    notifIntent.putExtra("id_pesan", data.getString("id_pesan"));

                    TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(SocketService.this);
                    taskStackBuilder.addParentStack(BacaSuratActivity.class);
//                    taskStackBuilder.addNextIntent(notifIntent);

//                    PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    Uri soundPath = Uri.parse("android.resource://id.go.manadokota.e_office/" + R.raw.alert);
                    long[] vibrateTone = {10000};

                    Notification notification = new Notification.Builder(SocketService.this)
                            .setSmallIcon(R.drawable.ic_logo)
                            .setContentTitle(data.getString("judul"))
                            .setContentText(data.getString("pesan"))
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_LIGHTS)
//                            .setContentIntent(pendingIntent)
                            .setVibrate(vibrateTone)
                            .setSound(soundPath)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIF_ID, notification);
                    NOTIF_ID++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.emit("leave",pengguna.get("id_pengguna").toString());
        socket.disconnect();
        SERVICE_RUNNING = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
