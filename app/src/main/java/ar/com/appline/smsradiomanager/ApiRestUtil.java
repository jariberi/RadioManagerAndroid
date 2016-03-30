package ar.com.appline.smsradiomanager;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jorge on 7/2/2016.
 */
public class ApiRestUtil {
    private static String LOGTAG = "APIRESTUTIL";
    private static String base_url = "http://192.168.120.13:8000/entrantes/api/";

    public static ApiRestAccess getServiceStatus() throws MalformedURLException, IOException {
        String url = base_url + "service_status/";
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setDoInput(true);
        ApiRestAccess task = new ApiRestAccess(connection, ApiRestAccess.ACTION_SERVICE_STATUS);
        return task;
    }

    public static ApiRestAccess putSMS(Date date, String number, String msj) throws IOException, JSONException {
        String url = base_url + "smss/";
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setDoOutput(true);
        ApiRestAccess task = new ApiRestAccess(connection, ApiRestAccess.ACTION_PUT_SMS);
        Log.i(LOGTAG + "DATE", date.toString());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Log.i(LOGTAG+"DATE",format.format(date));
        JSONObject sms = new JSONObject();
        sms.put("fecha_hora", format.format(date));
        sms.put("origen", number);
        sms.put("texto", msj);
        Log.i(LOGTAG,sms.toString());
        task.setFormBody(sms.toString());
        return task;
    }
//    public static ApiRestAccess obtainGetTask(String url) throws MalformedURLException, IOException {
//        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
//        connection.setReadTimeout(10000);
//        connection.setConnectTimeout(15000);
//        connection.setDoInput(true);
//        ApiRestAccess task = new ApiRestAccess(connection);
//        return task;
//    }
//
//    public static ApiRestAccess obtainFormPostTask(String url,ContentValues formData) throws MalformedURLException, IOException {
//        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
//        connection.setReadTimeout(10000);
//        connection.setConnectTimeout(15000);
//        connection.setDoOutput(true);
//        ApiRestAccess task = new ApiRestAccess(connection);
//        task.setFormBody(formData);
//        return task;
//    }
//
//    public static ApiRestAccess obtainMultipartPostTask(String url, ContentValues formPart, File file, String fileName) throws MalformedURLException, IOException {
//        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
//        connection.setReadTimeout(10000);
//        connection.setConnectTimeout(15000);
//        connection.setDoOutput(true);
//        ApiRestAccess task = new ApiRestAccess(connection);
//        task.setFormBody(formPart);
//        task.setUploadFile(file, fileName);
//        return task;
//    }
}
