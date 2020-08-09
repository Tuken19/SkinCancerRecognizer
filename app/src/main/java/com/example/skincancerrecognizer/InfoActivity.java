package com.example.skincancerrecognizer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoActivity extends AppCompatActivity {

    ImageView image;
    TextView textViewImageName;
    TextView textViewAK;
    TextView textViewBCC;
    TextView textViewBKL;
    TextView textViewDF;
    TextView textViewMEL;
    TextView textViewNV;
    TextView textViewSCC;
    TextView textViewVASC;

    TextView textViewPercAK;
    TextView textViewPercBCC;
    TextView textViewPercBKL;
    TextView textViewPercDF;
    TextView textViewPercMEL;
    TextView textViewPercNV;
    TextView textViewPercSCC;
    TextView textViewPercVASC;

    ProgressBar progressBarAK;
    ProgressBar progressBarBCC;
    ProgressBar progressBarBKL;
    ProgressBar progressBarDF;
    ProgressBar progressBarMEL;
    ProgressBar progressBarNV;
    ProgressBar progressBarSCC;
    ProgressBar progressBarVASC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        initialise();

        String imgPath = getIntent().getStringExtra("FILE_PATH");

        getData(imgPath);
    }

    private void initialise(){
        image = findViewById(R.id.image);
        textViewImageName = findViewById(R.id.image_name);
        textViewAK = findViewById(R.id.textViewAK);
        textViewBCC = findViewById(R.id.textViewBCC);
        textViewBKL = findViewById(R.id.textViewBKL);
        textViewDF = findViewById(R.id.textViewDF);
        textViewMEL = findViewById(R.id.textViewMEL);
        textViewNV = findViewById(R.id.textViewNV);
        textViewSCC = findViewById(R.id.textViewSCC);
        textViewVASC = findViewById(R.id.textViewVASC);
        textViewPercAK = findViewById(R.id.textViewPercAK);
        textViewPercBCC = findViewById(R.id.textViewPercBCC);
        textViewPercBKL = findViewById(R.id.textViewPercBKL);
        textViewPercDF = findViewById(R.id.textViewPercDF);
        textViewPercMEL = findViewById(R.id.textViewPercMEL);
        textViewPercNV = findViewById(R.id.textViewPercNV);
        textViewPercSCC = findViewById(R.id.textViewPercSCC);
        textViewPercVASC = findViewById(R.id.textViewPercVASC);
        progressBarAK = findViewById(R.id.progressBarAK);
        progressBarBCC = findViewById(R.id.progressBarBCC);
        progressBarBKL = findViewById(R.id.progressBarBKL);
        progressBarDF = findViewById(R.id.progressBarDF);
        progressBarMEL = findViewById(R.id.progressBarMEL);
        progressBarNV = findViewById(R.id.progressBarNV);
        progressBarSCC = findViewById(R.id.progressBarSCC);
        progressBarVASC = findViewById(R.id.progressBarVASC);
    }

    @SuppressLint("DefaultLocale")
    private void getData(String imgPath){
        // Open json file
        String jsonPath = imgPath.replace(".jpg", ".json");
        File file = new File(jsonPath);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            // This response will have Json Format String
            String jsonFile = stringBuilder.toString();
            JSONObject reader = new JSONObject(jsonFile);

            // Load Image
            Glide.with(this).load(imgPath).centerCrop().into(image);
            System.out.println(imgPath);

            // Load Image Name
            String imgName = MainActivity.getNameFromPath(imgPath);
            textViewImageName.setText(imgName);

            // Load Values
            JSONObject scores = reader.getJSONObject("Scores");
            textViewPercAK.setText(String.format("%.1f", scores.getDouble("AK")));
            textViewPercBCC.setText(String.format("%.1f", scores.getDouble("BCC")));
            textViewPercBKL.setText(String.format("%.1f", scores.getDouble("BKL")));
            textViewPercDF.setText(String.format("%.1f", scores.getDouble("DF")));
            textViewPercMEL.setText(String.format("%.1f", scores.getDouble("MEL")));
            textViewPercNV.setText(String.format("%.1f", scores.getDouble("NV")));
            textViewPercSCC.setText(String.format("%.1f", scores.getDouble("SCC")));
            textViewPercVASC.setText(String.format("%.1f", scores.getDouble("VASC")));
            progressBarAK.setProgress((int) scores.getDouble("AK"));
            progressBarBCC.setProgress((int) scores.getDouble("BCC"));
            progressBarBKL.setProgress((int) scores.getDouble("BKL"));
            progressBarDF.setProgress((int) scores.getDouble("DF"));
            progressBarMEL.setProgress((int) scores.getDouble("MEL"));
            progressBarNV.setProgress((int) scores.getDouble("NV"));
            progressBarSCC.setProgress((int) scores.getDouble("SCC"));
            progressBarVASC.setProgress((int) scores.getDouble("VASC"));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
