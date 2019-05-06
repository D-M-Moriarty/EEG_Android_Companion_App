package com.darren.fyp;

import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

public class TensorflowClassifier {

    private TensorFlowInferenceInterface inferenceInterface;

    TensorflowClassifier(TensorFlowInferenceInterface inferenceInterface) {
        this.inferenceInterface = inferenceInterface;
    }

    public float[] getPrediction(double[] input) {
        float[] output = predictFace(input);
        Log.d(getClass().getSimpleName(),Arrays.toString(input)+" -> "+Arrays.toString(output));
        return output;
    }

    private float[] predict(double[] input) {
        // model has only 1 output neuron
        float output[] = new float[3];
        float[] in = new float[8];
        for (int i = 0; i < in.length; i++) {
            in[i] = (float) input[i];
        }

        inferenceInterface.feed("dense_21_input", in,
                1, in.length);
        inferenceInterface.run(new String[]{"output_1"});
        inferenceInterface.fetch("output_1", output);

        return output;
    }

    private float[] predictFace(double[] input) {
        // model has only 1 output neuron
        float output[] = new float[3];
        float[] in = new float[8];
        for (int i = 0; i < in.length; i++) {
            in[i] = (float) input[i];
        }

        inferenceInterface.feed("dense_55_input", in,
                1, in.length);
        inferenceInterface.run(new String[]{"output_1"});
        inferenceInterface.fetch("output_1", output);

        return output;
    }

    double[] normalizeArray(int[] waves) {
        double[] normalized = new double[8];
        double max = 16777208.0;
        double min = 92.0;

        for (int i = 0; i < waves.length; i++) {
            normalized[i] = adjustValue((double) waves[i], min, max);
        }
        return normalized;
    }

    double[] normalizeFaceArray(int[] waves) {
        double[] normalized = new double[8];
        double max = 16777204.0;
        double min = 48.0;

        for (int i = 0; i < waves.length; i++) {
            normalized[i] = adjustValue((double) waves[i], min, max);
        }
        return normalized;
    }

    private double adjustValue(double wave, double min, double max) {
        double std = (wave - min) / (max - min);
        double scaled = std * (1.0 - 0.0) + 0.0;
        return scaled;
    }

    private int getLargest(float[] a) {
        if (a[0] > a[1] && a[0] > a[2])
            return 0;
        else if (a[1] > a[2])
            return 1;
        return 2;
    }

    String getClass(float[] a) {
        int index = getLargest(a);
        if (index == 2)
            return "rest";
        else if (index == 1)
            return "forward";
        return "back";
    }

    String getFaceClass(float[] a) {
        int index = getLargest(a);
        if (index == 2)
            return "forward";
        else if (index == 1)
            return "rest";
        return "back";
    }
}
