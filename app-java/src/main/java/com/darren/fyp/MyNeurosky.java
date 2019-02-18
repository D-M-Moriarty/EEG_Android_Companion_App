package com.darren.fyp;

import android.content.Intent;
import android.util.Log;

import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MyNeurosky implements Subject {

    public final static String LOG_TAG = "NeuroSky";
    private static String waves = "eegRawValue,delta,theta,alphaLow,alphaHigh,betaLow,betaHigh,gammaLow,gammaMid";

    private NeuroSky neuroSky;
    private MainActivity mainActivity;

    private List<Observer> observers;
    private int[] brainWaves;

    MyNeurosky(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.neuroSky = createNeuroSky();
        this.observers = new ArrayList<>();
        brainWaves = new int[8];
    }

    private NeuroSky createNeuroSky() {
        return new NeuroSky(new ExtendedDeviceMessageListener() {
            @Override public void onStateChange(State state) {
                handleStateChange(state);
            }

            @Override public void onSignalChange(Signal signal) {
                handleSignalChange(signal);
            }

            @Override public void onBrainWavesChange(Set<BrainWave> brainWaves) {
                handleBrainWavesChange(brainWaves);
            }
        });
    }

    private void handleStateChange(final State state) {
        if (neuroSky != null && state.equals(State.CONNECTED)) {
            neuroSky.start();
        }

        mainActivity.tvState.setText(state.toString());
        Log.d(LOG_TAG, state.toString());
    }

    private void handleSignalChange(final Signal signal) {
        switch (signal) {
            case ATTENTION:
                mainActivity.tvAttention.setText(getFormattedMessage("attention: %d", signal));
                break;
            case MEDITATION:
                mainActivity.tvMeditation.setText(getFormattedMessage("meditation: %d", signal));
                break;
            case RAW_DATA:
                mainActivity.tvBlink.setText(getFormattedMessage("RAW_DATA: %d", signal));
                break;
//      case HEART_RATE:
//        tvBlink.setText(getFormattedMessage("HEART_RATE: %d", signal));
//        break;
//      case RAW_MULTI:
//        tvBlink.setText(getFormattedMessage("RAW_MULTI: %d", signal));
//        break;
//      case EEG_POWER:
//        tvBlink.setText(getFormattedMessage("EEG_POWER: %d", signal));
//        break;
//      case BLINK:
//        tvBlink.setText(getFormattedMessage("blink: %d", signal));
//        break;
        }

        Log.d(LOG_TAG, String.format("%s: %d", signal.toString(), signal.getValue()));
    }

    private String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
        StringBuilder waveLine= new StringBuilder();
        int[] bv = new int[8];
        findWaves(brainWaves, bv);
        for (Integer w: bv)
            waveLine.append(w + ",");
        waves += ("\n" + waveLine);
    }

    private void findWaves(Set<BrainWave> brainWaves, int[] bv) {
        for (BrainWave brainWave : brainWaves) {
            Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.getValue()));
            switch (brainWave) {
                case DELTA:
                    bv[0] = brainWave.getValue();
                    break;
                case THETA:
                    bv[1] = brainWave.getValue();
                    break;
                case LOW_ALPHA:
                    bv[2] = brainWave.getValue();
                    break;
                case HIGH_ALPHA:
                    bv[3] = brainWave.getValue();
                    break;
                case LOW_BETA:
                    bv[4] = brainWave.getValue();
                    break;
                case HIGH_BETA:
                    bv[5] = brainWave.getValue();
                    break;
                case LOW_GAMMA:
                    bv[6] = brainWave.getValue();
                    break;
                case MID_GAMMA:
                    bv[7] = brainWave.getValue();
                    break;
            }
        }
        this.brainWaves = bv;
        notifyObservers();
    }

    boolean isConnected() {
        return neuroSky.isConnected();
    }

    void connect() {
        neuroSky.connect();
    }

    void disconnect() {
        neuroSky.disconnect();
    }

    void start() {
        neuroSky.start();
    }

    void stop() {
        neuroSky.stop();
    }

    Intent sendEmailFile() {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "dmamprop@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "forward");
        email.putExtra(Intent.EXTRA_TEXT, waves);
        //need this to prompts email client only
        email.setType("message/rfc822");
        return email;
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer: observers)
            observer.updateObserver(brainWaves);
    }
}
