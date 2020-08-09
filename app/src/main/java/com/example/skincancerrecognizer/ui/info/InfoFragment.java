package com.example.skincancerrecognizer.ui.info;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.skincancerrecognizer.R;

public class InfoFragment extends Fragment {

    private InfoViewModel mViewModel;

    TextView textViewTitleInfo;
    TextView textViewDescriptionInfo;

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_info, container, false);
        textViewTitleInfo = root.findViewById(R.id.text_title_info);
        textViewDescriptionInfo = root.findViewById(R.id.text_description_info);

        textViewTitleInfo.setText("Information");
        textViewDescriptionInfo.setText("The purpose of this application is to conduct a preliminary diagnosis of skin lesions. " +
                                        "Remember, this is not a substitute for medical examinations! \n\n" +
                                        "Follow the steps written below. \n" +
                                        "1. Take a photo of a lesion by clicking button in the bottom left corner on the home screen. \n" +
                                        "2. Crop the photo so that it shows particular skin change. \n" +
                                        "3. Specially trained neural network will perform necessary calculations to classify your lesion. \n\n" +
                                        "It can belong to one of the eight classes: \n" +
                                        " - AK   - Actinic keratosis \n" +
                                        " - BCC  - Basal cell carcinoma \n" +
                                        " - BKL  - Benign keratosis \n" +
                                        " - DF   - Dermatofibroma \n" +
                                        " - MEL  - Melanoma \n" +
                                        " - NV   - Melanocytic nevus\n" +
                                        " - SCC  - Squamous cell carcinoma \n" +
                                        " - VASC - Vascular lesion \n\n" +
                                        "After receiving a negative diagnosis, you should immediately visit a doctor, to set a further treatment.");

        return root;
    }

}
