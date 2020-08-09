package com.example.skincancerrecognizer;

import android.graphics.Bitmap;
import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;

public class Classifier {
    private Module model1;
    private Module model2;
    private Module model3;
    private Module model4;
    private Module model5;
    private float[] mean = {0.485f, 0.456f, 0.406f};
    private float[] std = {0.229f, 0.224f, 0.225f};

    private float[] scores = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    public Classifier(String modelPath1, String modelPath2, String modelPath3, String modelPath4, String modelPath5) {
        model1 = Module.load(modelPath1);
        model2 = Module.load(modelPath2);
        model3 = Module.load(modelPath3);
        model4 = Module.load(modelPath4);
        model5 = Module.load(modelPath5);
        System.out.println("Załadowano");
    }

    public void setMeanAndStd(float[] mean, float[] std){
        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size){
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, this.mean, this.std);
    }

    public int argMax(float[] inputs){
        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++){
            if(inputs[i] > maxvalue) {
                maxIndex = i;
                maxvalue = inputs[i];
            }
        }
        return maxIndex;
    }

    public float[] getScores(){
        return this.scores;
    }

    public String predict(){
        int classIndex = argMax(scores);
        return Constants.CLASSES[classIndex];
    }

    /**
     * Calculates scores and saves them to local variable
     * @param bitmap
     * @return
     */
    public void calculateScores(Bitmap bitmap){
        Tensor tensor = preprocess(bitmap,224);
        IValue inputs = IValue.from(tensor);
        Tensor outputs1 = model1.forward(inputs).toTensor();
        Tensor outputs2 = model2.forward(inputs).toTensor();
        Tensor outputs3 = model3.forward(inputs).toTensor();
        Tensor outputs4 = model4.forward(inputs).toTensor();
        Tensor outputs5 = model5.forward(inputs).toTensor();
        float[] scores1 = outputs1.getDataAsFloatArray();
        float[] scores2 = outputs2.getDataAsFloatArray();
        float[] scores3 = outputs3.getDataAsFloatArray();
        float[] scores4 = outputs4.getDataAsFloatArray();
        float[] scores5 = outputs5.getDataAsFloatArray();

        for(int i = 0; i < Constants.CLASSES.length; i++){
            this.scores[i] = (scores1[i] + scores2[i] + scores3[i] + scores4[i] + scores5[i])/5;
        }
    }

}
