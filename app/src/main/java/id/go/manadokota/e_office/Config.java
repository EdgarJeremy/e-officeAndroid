package id.go.manadokota.e_office;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.pixplicity.easyprefs.library.Prefs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by edgar on 4/1/17.
 */

public class Config {

    // API Configuration
    public static String BASE_HOST = "http://192.168.137.71";
    public static String API_BASE_URL = BASE_HOST + "/disposisi/api";
    public static int SOCKET_PORT = 7008;


    public static String PREFS_NAME = "e-Office";

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Buat MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Buat Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void session_start(Context ctx) {
        new Prefs.Builder()
                .setContext(ctx)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(Config.PREFS_NAME)
                .setUseDefaultSharedPreference(true)
                .build();
    }

}
