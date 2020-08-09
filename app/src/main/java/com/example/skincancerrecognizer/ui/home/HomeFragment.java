package com.example.skincancerrecognizer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.skincancerrecognizer.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TextView textViewTitle = root.findViewById(R.id.text_title);
        textViewTitle.setText("Take a picture of a skin lesion.");

        TextView textViewDescription = root.findViewById(R.id.text_description);
        textViewDescription.setText("The purpose of this application is to conduct a preliminary diagnosis. " +
                        "Neural network will perform necessary calculations to classify your lesion. " +
                        "Remember, this is not a substitute for medical examinations! ");
        return root;
    }

}
