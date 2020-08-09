package com.example.skincancerrecognizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.Fragment;

public final class GalleryAdapter extends BaseAdapter {

    private List<String> data = new ArrayList<>();
    private Fragment fragment;

    public GalleryAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setData(List<String> data){
        if(this.data.size() > 0){
            this.data.clear();
        }

        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        View view = null;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);
            imageView = view.findViewById(R.id.img);
        }
        else {
            view = convertView;
            imageView = (ImageView) convertView.findViewById(R.id.img);
        }

        // Open json file
        String imgPath = data.get(position);
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

            // Load image
            Glide.with(fragment).load(data.get(position)).centerCrop().into(imageView);

            // Load image name
            String name = getNameFromPath(imgPath);
            TextView fileName = (TextView) view.findViewById(R.id.file_name);
            fileName.setText(name);

            // Load lesion name
            String pred = reader.getString("Predicted");
            TextView lesionName = (TextView) view.findViewById(R.id.lesion_name);
            lesionName.setText(pred);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public String getPath(int position){
        return data.get(position);
    }

    public String getNameFromPath(String path){
        String name = "";
        Pattern pattern = Pattern.compile("[^/]+$");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find())
        {
            name = matcher.group();
        }
        return name;
    }
}
