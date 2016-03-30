package ar.com.appline.smsradiomanager;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jorge on 7/2/2016.
 */
public class ApiRestAccess extends AsyncTask<Void, Integer, Object> {
    private static final String LOGTAG = "APIRESTACCESS";
    /**
     * DECLARO LAS ACCIONES POSIBLES
     */
    public static final String ACTION_SERVICE_STATUS = "ar.com.appline.smsradiomanager.action.SERVICE_STATUS";
    public static final String ACTION_PUT_SMS = "ar.com.appline.smsradiomanager.action.PUT_SMS";

    /**
     * DECLARO LAS INTERFACES
     */
    public interface ResponseCallback {
        public void onRequestSuccess(JSONObject response, String current_action);
        public void onRequestError(Exception error, String current_action);
    }

    public interface ProgressCallback {
        public void onProgressUpdate(int progress);
    }

    private HttpURLConnection mConnection;
    private String mFormBody;
    private File mUploadFile;
    private String mUploadFileName;
    private String mAction;

    //Activity callbacks. Use WeakReferences to avoid blocking
    // operations causing linked objects to stay in memory
    private WeakReference<ResponseCallback> mResponseCallback;
    private WeakReference<ProgressCallback> mProgressCallback;

    public ApiRestAccess(HttpURLConnection connection, String action) {
        mConnection = connection;
        mAction = action;
    }

    public void setFormBody(String formData) {
        if (formData == null) {
            mFormBody = null;
            return;
        }
        mFormBody = formData;
        Log.i(LOGTAG, mFormBody);
    }

    public void setFormBody(ContentValues formData) {
        if (formData == null) {
            mFormBody = null;
            return;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iterator = formData.valueSet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Object> item = iterator.next();
            sb.append(URLEncoder.encode(item.getKey()) );
            sb.append("=");
            sb.append(URLEncoder.encode(item.getValue().toString()) );
            if(iterator.hasNext()){
                sb.append("&");
            }
        }
        mFormBody = sb.toString();
    }

    public void setUploadFile(File file, String fileName) {
        mUploadFile = file;
        mUploadFileName = fileName;
    }

    public void setResponseCallback(ResponseCallback callback) {
        mResponseCallback = new WeakReference<ResponseCallback>(callback);
    }

    public void setProgressCallback(ProgressCallback callback) {
        mProgressCallback = new WeakReference<ProgressCallback>(callback);
    }

    private void writeMultipart(String boundary, String charset, OutputStream output, boolean writeContent) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(output, Charset.forName(charset)), 8192);
            // Post Form Data Component
            if (mFormBody != null) {
                writer.write("--" + boundary);
                writer.write("\r\n");
                writer.write("Content-Disposition: form-data;" + " name=\"parameters\"");
                writer.write("\r\n");
                writer.write("Content-Type: text/plain; charset=" + charset);
                writer.write("\r\n");
                writer.write("\r\n");
                if (writeContent) {
                    writer.write(mFormBody);
                }
                writer.write("\r\n");
                writer.flush();
            }
            // Send binary file.
            writer.write("--" + boundary);
            writer.write("\r\n");
            writer.write("Content-Disposition: form-data; name=\"" + mUploadFileName + "\"; filename=\"" + mUploadFile.getName() + "\"");
            writer.write("\r\n");
            writer.write("Content-Type: " + URLConnection.guessContentTypeFromName(mUploadFile.getName()));
            writer.write("\r\n");
            writer.write("Content-Transfer-Encoding: binary");
            writer.write("\r\n");
            writer.write("\r\n");
            writer.flush();
            if (writeContent) {
                InputStream input = null;
                try {
                    input = new FileInputStream(mUploadFile);
                    byte[] buffer = new byte[1024];
                    for (int length = 0; (length = input.read(buffer)) > 0; ) {
                        output.write(buffer, 0, length);
                    }
                    // Don't close the OutputStream yet
                    output.flush();
                } catch (IOException e) {
                    Log.w(LOGTAG, e);
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            // This CRLF signifies the end of the binary chunk
            writer.write("\r\n");
            writer.flush();

            // End of multipart/form-data.
            writer.write("--" + boundary + "--");
            writer.write("\r\n");
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeFormData(String charset,OutputStream output) throws IOException {
        try {
            output.write(mFormBody.getBytes(charset));
            output.flush();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Override
    protected Object doInBackground(Void... params) {
        //Generate random string for boundary
        String boundary = Long.toHexString(System.currentTimeMillis());
        String charset = Charset.defaultCharset().displayName();
        try {
            // Set up output if applicable
            if (mUploadFile != null) {
                //We must do a multipart request
                mConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                //Calculate the size of the extra metadata
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                writeMultipart(boundary, charset, bos, false);
                byte[] extra = bos.toByteArray();
                int contentLength = extra.length;
                //Add the file size to the length
                contentLength += mUploadFile.length();
                //Add the form body, if it exists
                if (mFormBody != null) {
                    contentLength += mFormBody.length();
                }
                mConnection.setFixedLengthStreamingMode(contentLength);
            } else if (mFormBody != null) {
                //In this case, it is just form data to post
                mConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
                mConnection.setFixedLengthStreamingMode(mFormBody.length());
            }

            //This is the first call on URLConnection that
            // actually does Network IO.  Even openConnection() is
            // still just doing local operations.
            mConnection.connect();

            // Do output if applicable (for a POST)
            if (mUploadFile != null) {
                OutputStream out = mConnection.getOutputStream();
                writeMultipart(boundary, charset, out, true);
            } else if (mFormBody != null) {
                OutputStream out = mConnection.getOutputStream();
                Log.i(LOGTAG+"-outs",out.toString());
                writeFormData(charset, out);
            }
            // Get response data
            InputStream in = null;
            int status = mConnection.getResponseCode();
            if (status >= 300) {
                String message = mConnection.getResponseMessage();
                Log.e(LOGTAG+"-300",status+" "+message);
                in = mConnection.getErrorStream();
            }
            else {
                in = mConnection.getInputStream();
            }
            String encoding = mConnection.getContentEncoding();
            int contentLength = mConnection.getContentLength();
            if (encoding == null) {
                encoding = "UTF-8";
            }

            byte[] buffer = new byte[1024];

            int length = contentLength > 0 ? contentLength : 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream(length);

            int downloadedBytes = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                downloadedBytes += read;
                publishProgress((downloadedBytes * 100) / contentLength);
                out.write(buffer, 0, read);
            }
            Log.e(LOGTAG+"-out",new String(out.toByteArray()));
            return new JSONObject(new String(out.toByteArray(), encoding));
        } catch (Exception e) {
            Log.w(LOGTAG+"exc", e);
            return e;
        } finally {
            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // Update progress UI
        if (mProgressCallback != null && mProgressCallback.get() != null) {
            mProgressCallback.get().onProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (mResponseCallback != null && mResponseCallback.get() != null) {
            final ResponseCallback cb = mResponseCallback.get();
            if (result instanceof JSONObject) {
                cb.onRequestSuccess((JSONObject) result, mAction);
            } else if (result instanceof Exception) {
                cb.onRequestError((Exception) result, mAction);
            } else {
                cb.onRequestError(new IOException("Unknown Error Contacting Host"), mAction);
            }
        }
    }
}