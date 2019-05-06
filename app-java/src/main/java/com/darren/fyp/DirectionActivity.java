package com.darren.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.github.pwittchen.neurosky.app.R;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.darren.fyp.MyNeurosky.LOG_TAG;
import static com.github.pwittchen.neurosky.app.R.id.*;


public class DirectionActivity extends AppCompatActivity implements Observer {

    private MyNeurosky neuroSky;
    private String expression;
    private TensorFlowInferenceInterface inferenceInterface;
    private TensorflowClassifier classifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        expression = "";
        ButterKnife.bind(this);
        neuroSky = new MyNeurosky(this);
        neuroSky.registerObserver(this);
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "cat_dog.pb");
        classifier = new TensorflowClassifier(inferenceInterface);

    }

    @OnClick(btnUp)
    public void clickUp(View view) {
        expression = "up";
        neuroSky.setExpression(expression);
    }

    @OnClick(btnRight)
    public void clickRight(View view) {
        expression = "right";
        neuroSky.setExpression(expression);
    }

    @OnClick(btnDown)
    public void clickDown(View view) {
        expression = "down";
        neuroSky.setExpression(expression);
    }

    @OnClick(btnLeft)
    public void clickLeft(View view) {
        expression = "left";
        neuroSky.setExpression(expression);
    }

    @OnClick(btnRest)
    public void clickRest(View view) {
        expression = "rest";
        neuroSky.setExpression(expression);
    }

    @OnClick(btnRecord)
    public void startRecording(View view) {
        try {
            neuroSky.connect();
        } catch (BluetoothNotEnabledException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
        }
        neuroSky.start();
    }

    @OnClick(btnStop)
    public void stopRecording(View view) {
        neuroSky.stop();
        neuroSky.disconnect();
        Intent emailIntent = neuroSky.sendEmailFile();
        startActivity(Intent.createChooser(emailIntent, "Choose Email client :"));
    }

    @Override
    public void updateObserver(int[] waves) {
        System.out.println(Arrays.toString(waves));
        double[] normalizedWaves = classifier.normalizeArray(waves);
        System.out.println("normalized " + Arrays.toString(normalizedWaves));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.stop();
        }
    }
}
