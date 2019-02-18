package com.darren.fyp;

import java.io.FileInputStream;
import java.util.HashMap;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;

public class HowToUseXgboostPredictor {
    public static void main(String[] args) throws java.io.IOException {
        // If you want to use faster exp() calculation, uncomment the line below
        // ObjFunction.useFastMathExp(true);

        // Load model and create Predictor
        Predictor predictor = new Predictor(
                new FileInputStream("/xgb_77.dat"));
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
