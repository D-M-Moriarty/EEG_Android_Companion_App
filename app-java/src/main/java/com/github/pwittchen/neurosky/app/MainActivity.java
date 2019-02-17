package com.github.pwittchen.neurosky.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.github.pwittchen.neurosky.app.MyNeurosky.LOG_TAG;
import static com.github.pwittchen.neurosky.app.TelloController.DRONE_SOCKET_ACTIVE;
import static com.github.pwittchen.neurosky.app.TelloController.EXCEPTION_ERROR_CLIENT;
import static com.github.pwittchen.neurosky.app.TelloController.EXCEPTION_ERROR_SERVER;

public class MainActivity extends AppCompatActivity implements Observer {

    private MyNeurosky neuroSky;

    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_attention)
    TextView tvAttention;
    @BindView(R.id.tv_meditation)
    TextView tvMeditation;
    @BindView(R.id.tv_blink)
    TextView tvBlink;

    private TensorFlowInferenceInterface inferenceInterface;
    private TensorflowClassifier classifier;
    private TelloController telloController;


    private AutoCompleteTextView textCommand;
    static TextView TEXT_RESPONSE;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "cat_dog.pb");
        classifier = new TensorflowClassifier(inferenceInterface);
        ButterKnife.bind(this);
        neuroSky = new MyNeurosky(this);
        neuroSky.registerObserver(this);
        telloController = new TelloController();

        telloController.setmCmdArray(getResources().getStringArray(R.array.Commands));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, telloController.getmCmdArray());

//        textCommand.setThreshold(1);//will start working from first character
//        textCommand.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        if (telloController.isExceptionErrorInetAddress()) {
            TEXT_RESPONSE.setText("Exception error creating InetAddress!");
            Button button = findViewById(R.id.btnTakeOff);
            button.setEnabled(false);
        }
        super.onResume();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.start();
        }
    }

    @Override
    protected void onPause() {
        // If the app loses focus, or phone is locked, then land!
        telloController.setUdpClientMessage("land");
        telloController.udpClientSendMessage();
        super.onPause();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.stop();
        }
    }

    @OnClick(R.id.btn_connect)
    void connect() {
        try {
            neuroSky.connect();
        } catch (BluetoothNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    @OnClick(R.id.btn_disconnect)
    void disconnect() {
        neuroSky.disconnect();
        Intent emailIntent = neuroSky.sendEmailFile();
        startActivity(Intent.createChooser(emailIntent, "Choose Email client :"));
    }

    @OnClick(R.id.btn_start_monitoring)
    void startMonitoring() {
        neuroSky.start();
    }

    @OnClick(R.id.btn_stop_monitoring)
    void stopMonitoring() {
        neuroSky.stop();
    }

    @Override
    protected void onDestroy() {
        telloController.stopUdpServer();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                Toast.makeText(this, "App version: " + versionName + "\nBlue Spectrum Software", Toast.LENGTH_LONG).show();
            } catch(PackageManager.NameNotFoundException e) {}
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateObserver(int[] waves) {
        System.out.println(Arrays.toString(waves));
        double[] normalizedWaves = classifier.normalizeArray(waves);
        System.out.println("normalized " + Arrays.toString(normalizedWaves));
        float[] prediction = classifier.getPrediction(normalizedWaves);
        String classReturned = classifier.getClass(prediction);
        System.out.println(classReturned);
    }

    public void onClickLand(View view) {

        if(EXCEPTION_ERROR_CLIENT) {
            TEXT_RESPONSE.setText("Exception error in UDP client.");
        } else if(EXCEPTION_ERROR_SERVER) {
            TEXT_RESPONSE.setText("Exception error in UDP server.");
        } else {
            String cmd = textCommand.getText().toString().trim();

            textCommand.setText("land");
//            textCommand.clearFocus();
            if(DRONE_SOCKET_ACTIVE) {
                TEXT_RESPONSE.setText("");
            }
            // If user presses button with no command entered, land immediately!
            if (cmd.isEmpty()) {
                cmd = "land";
            }

            telloController.setUdpClientMessage(cmd);
            telloController.udpClientSendMessage();
            Toast.makeText(this, "Sent: (" + cmd + ")", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickTakeOff(View view) {
        if(EXCEPTION_ERROR_CLIENT) {
            TEXT_RESPONSE.setText("Exception error in UDP client.");
        } else if(EXCEPTION_ERROR_SERVER) {
            TEXT_RESPONSE.setText("Exception error in UDP server.");
        } else {
//            String cmd = textCommand.getText().toString().trim();
//
//            textCommand.setText("takeoff");
//            textCommand.clearFocus();
            if(DRONE_SOCKET_ACTIVE) {
                TEXT_RESPONSE.setText("");
            }
//            // If user presses button with no command entered, land immediately!
//            if (cmd.isEmpty()) {
//                cmd = "land";
//            }

            telloController.setUdpClientMessage("takeoff");
            telloController.udpClientSendMessage();
            Toast.makeText(this, "Sent: (takeoff)", Toast.LENGTH_SHORT).show();
        }
    }
}
