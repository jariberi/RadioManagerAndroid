package ar.com.appline.smsradiomanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ApiRestAccess.ProgressCallback, ApiRestAccess.ResponseCallback, PopupMenu.OnMenuItemClickListener {
    private static final String POST_URI = "http://httpbin.org/post";
    private TextView mServiceStatus;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mServiceStatus = (TextView) findViewById(R.id._textViewServiceStatus);
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onProgressUpdate(int progress) {

    }

    @Override
    public void onRequestSuccess(JSONObject response, String current_action) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        //Clear progress indicator
        try {
            int l = response.getInt("Service Status");
            if (l < 3) {
                mServiceStatus.setText("MALO");
                mServiceStatus.setTextColor(Color.RED);
            } else if (l > 7) {
                mServiceStatus.setText("CORRECTO");
                mServiceStatus.setTextColor(Color.GREEN);
            } else {
                mServiceStatus.setText("POBRE");
                mServiceStatus.setTextColor(Color.YELLOW);
            }
        } catch (Exception e) {
            mServiceStatus.setText("ERROR");
            mServiceStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public void onRequestError(Exception error, String current_action) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        //Process the response data (here we just display it)
        mServiceStatus.setText("ERROR");
        mServiceStatus.setTextColor(Color.RED);
    }

    public void onRefreshClick(View v) {
        refresh();
    }

    public void refresh() {
        //Create the request
        try {
            //Simple GET
            mProgressDialog = ProgressDialog.show(this, "Espere", "Comprobando conectividad", true);
            ApiRestAccess getServiceStatus = ApiRestUtil.getServiceStatus();
            getServiceStatus.setResponseCallback(this);
            //getServiceStatus.setProgressCallback(this);
            getServiceStatus.execute();
        } catch (Exception e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mServiceStatus.setText("ERROR");
            mServiceStatus.setTextColor(Color.RED);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}