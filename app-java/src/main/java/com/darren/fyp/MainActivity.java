package com.darren.fyp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.neurosky.app.R;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;
import butterknife.BindView;
import butterknife.OnClick;

import static com.darren.fyp.MyNeurosky.LOG_TAG;
import static com.darren.fyp.TelloController.DRONE_SOCKET_ACTIVE;
import static com.darren.fyp.TelloController.EXCEPTION_ERROR_CLIENT;
import static com.darren.fyp.TelloController.EXCEPTION_ERROR_SERVER;
import static com.github.pwittchen.neurosky.app.R.id.btn_connect;
import static com.github.pwittchen.neurosky.app.R.id.btn_disconnect;
import static com.github.pwittchen.neurosky.app.R.id.btn_start_monitoring;
import static com.github.pwittchen.neurosky.app.R.id.btn_stop_monitoring;

public class MainActivity extends AppCompatActivity implements Observer {

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

    static String TEXT_RESPONSE;
    private boolean performingActions;
    private MyNeurosky neuroSky;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        try {
            xgbTest();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "cat_dog.pb");
//        classifier = new TensorflowClassifier(inferenceInterface);
//        ButterKnife.bind(this);
//        neuroSky = new MyNeurosky(this);
//        neuroSky.registerObserver(this);
//        telloController = new TelloController();
//
//        telloController.setmCmdArray(getResources().getStringArray(R.array.Commands));
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_list_item_1, telloController.getmCmdArray());

    }

    @Override
    protected void onResume() {
//        if (telloController.isExceptionErrorInetAddress()) {
//            TEXT_RESPONSE = "Exception error creating InetAddress!";
//            Button button = findViewById(R.id.btnTakeOff);
//            button.setEnabled(false);
//        }
        super.onResume();
//        if (neuroSky != null && neuroSky.isConnected()) {
//            neuroSky.start();
//        }
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

    @OnClick(btn_connect)
    public void connect() {
        try {
            neuroSky.connect();
        } catch (BluetoothNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    @OnClick(btn_disconnect)
    public void disconnect() {
        neuroSky.disconnect();
        Intent emailIntent = neuroSky.sendEmailFile();
        startActivity(Intent.createChooser(emailIntent, "Choose Email client :"));
    }

    @OnClick(btn_start_monitoring)
    public void startMonitoring() {
        neuroSky.start();
    }


   public void onClickPerformAction(View view) {
        performingActions = !performingActions;
       System.out.println("nowo performing actionzzz");
    }

    @OnClick(btn_stop_monitoring)
    public void stopMonitoring() {
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
        if (performingActions) {
            chosenCommand(classReturned);
            System.out.println("Doing the stuff: performing: " + classReturned);

        }

    }

    private void chosenCommand(String classReturned) {
        String cmd = "rest";
        if (classReturned.equals("forward"))
            cmd = "forward 40";
        else if (classReturned.equals("back"))
            cmd = "back 40";
        telloController.setUdpClientMessage(cmd);
        telloController.udpClientSendMessage();
    }

    public void onClickConnect(View view) {
        telloController.setUdpClientMessage("command");
        telloController.udpClientSendMessage();
    }

    public void onClickLand(View view) {
        if(EXCEPTION_ERROR_CLIENT) {
            TEXT_RESPONSE = "Exception error in UDP client.";
        } else if(EXCEPTION_ERROR_SERVER) {
            TEXT_RESPONSE = "Exception error in UDP server.";
        } else {
            if(DRONE_SOCKET_ACTIVE) {
                TEXT_RESPONSE = "";
            }
            telloController.setUdpClientMessage("land");
            telloController.udpClientSendMessage();
            Toast.makeText(this, "Sent: (land)", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickTakeOff(View view) {
        if(EXCEPTION_ERROR_CLIENT) {
            System.out.println("Exception error in UDP client.");
        } else if(EXCEPTION_ERROR_SERVER) {
            TEXT_RESPONSE = "Exception error in UDP server.";
        } else {
            if(DRONE_SOCKET_ACTIVE) {
                TEXT_RESPONSE = "";
            }
            telloController.setUdpClientMessage("takeoff");
            telloController.udpClientSendMessage();
            Toast.makeText(this, "Sent: (takeoff)", Toast.LENGTH_SHORT).show();
        }
    }

    void xgbTest() throws IOException {
        // If you want to use faster exp() calculation, uncomment the line below
        // ObjFunction.useFastMathExp(true);

        InputStream pathTOModel = getAssets().open("xgb/xgb_77.dat");
        FileInputStream inputStream = (FileInputStream) pathTOModel;

        String path = pathTOModel.toString();


//        System.out.println(pathTOModel);
        // Load model and create Predictor
        Predictor predictor = new Predictor(inputStream);
        // Create feature vector from dense representation by array
        double[] denseArray = {0, 0, 32, 0, 0, 16, -8, 0, 0, 0};
        FVec fVecDense = FVec.Transformer.fromArray(
                denseArray,
                true /* treat zero element as N/A */);

        // Create feature vector from sparse representation by map
        FVec fVecSparse = FVec.Transformer.fromMap(
                new HashMap<Integer, Double>() {{
                    put(2, 32.);
                    put(5, 16.);
                    put(6, -8.);
                }});

        // Predict probability or classification
        double[] prediction = predictor.predict(fVecDense);

        // prediction[0] has
        //    - probability ("binary:logistic")
        //    - class label ("multi:softmax")

        // Predict leaf index of each tree
        int[] leafIndexes = predictor.predictLeaf(fVecDense);

        // leafIndexes[i] has a leaf index of i-th tree
    }

}
