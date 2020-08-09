package com.example.skincancerrecognizer.ui.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.skincancerrecognizer.GalleryAdapter;
import com.example.skincancerrecognizer.InfoActivity;
import com.example.skincancerrecognizer.MainActivity;
import com.example.skincancerrecognizer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private boolean isGalleryInitialized = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        final TextView textView = root.findViewById(R.id.text_gallery);
        final ListView listView = root.findViewById(R.id.listViewGallery);

        if(!isGalleryInitialized){
            final GalleryAdapter galleryAdapter = new GalleryAdapter(this);
            String dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            final String directoryPath = dirPath.concat(MainActivity.DIRECTORY_NAME).concat("/").concat(MainActivity.user.getUid());
            final List<String>[] filesList = new List[]{loadFilesToGallery(directoryPath)};

            galleryAdapter.setData(filesList[0]);
            listView.setAdapter(galleryAdapter);

            listView.setLongClickable(true);

            // Deleting pictures
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> arg0, View arg1, final int position, long arg3) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                    // this Title Fails to display
                    alert.setTitle("Delete File");

                    //  this Message Fails to display
                    alert.setMessage("Are you sure you want to delete this file?");

                    alert.setCancelable(false);
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String path = galleryAdapter.getPath(position);
                            String jsonFilePath = path.replace("jpg", "json");

                            File imgFile = new File(path);
                            boolean imgDeleted = imgFile.delete();
                            if(imgDeleted){
                                filesList[0] = loadFilesToGallery(directoryPath);
                                galleryAdapter.setData(filesList[0]);
                                listView.setAdapter(galleryAdapter);
                            }

                            // TODO: Nie działa usuwanie plików json...
                            System.out.println(jsonFilePath);
                            File jsonFile = new File(jsonFilePath);
                            boolean jsonDeleted = jsonFile.delete();
                            System.out.println(jsonDeleted);
                        }

                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alert.create();
                    alert.show();
                    return false;
                }
            });

            // Go to info activity
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String filePath = galleryAdapter.getPath(position);
                    Intent infoActivityIntent = new Intent(getContext(), InfoActivity.class);
                    infoActivityIntent.putExtra("FILE_PATH", filePath);
                    startActivity(infoActivityIntent);
                }
            });

            isGalleryInitialized = true;
        }

        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    private List<String> loadFilesToGallery(String directoryPath){
        final File imagesDirectory = new File(directoryPath);
        final File[] files = imagesDirectory.listFiles();
        final int filesCount = files.length;
        final List<String> filesList = new ArrayList<>();

        for(int i = 0; i < filesCount; i++){
            final String path = files[i].getAbsolutePath();
            if(path.endsWith(".jpg") || path.endsWith(".png")){
                filesList.add(path);
            }
        }
        return filesList;
    }

}
