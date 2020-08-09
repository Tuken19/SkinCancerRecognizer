package com.example.skincancerrecognizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.skincancerrecognizer.ui.home.HomeFragment;
import com.example.skincancerrecognizer.ui.info.InfoFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    // ===== Variable declarations =====
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int PERMISSIONS_COUNT = PERMISSIONS.length;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_CROP_PHOTO = 3;
    public static String DIRECTORY_NAME = "/Skin_Cancer_Recognizer";
    private static String currentPhotoPath;
    GoogleSignInClient mGoogleSignInClient;
    private AppBarConfiguration mAppBarConfiguration;
    Toolbar toolbar;
    FloatingActionButton fab;
    DrawerLayout drawer;
    NavigationView navigationView;
    NavController navController;
    public static FirebaseUser user;

    private ProgressDialog mProgressDialog;

    ImageView userImage;
    TextView userName;
    TextView userEmail;

    TextView textViewTitle;
    TextView textViewDescription;

    ConstraintLayout constraintLayoutCroppedImg;
    ImageView imageViewCameraPhoto;

    TextView textViewImgName;
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
    public static Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ===== User init =====
        user = FirebaseAuth.getInstance().getCurrentUser();

        // ===== Permission handling =====
        getPermission();

        // ===== Check if directory exists =====
        check();

        // ===== Toolbar =====
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ===== Classifier =====
        classifier = load();

        // ===== Camera button =====
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take a photo.
                performCrop();
            }
        });

        // ===== Drawer =====
        drawer = findViewById(R.id.drawer_layout);

        // ===== Navigation View =====
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_info)
                .setDrawerLayout(drawer)
                .build();

        // ===== Nav Controller =====
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.nav_home) {
                    Snackbar.make(findViewById(R.id.app_bar_main), "Home", Snackbar.LENGTH_SHORT).show();
                    fab.setVisibility(View.VISIBLE);
                }
                if (destination.getId() == R.id.nav_gallery) {
                    Snackbar.make(findViewById(R.id.app_bar_main), "Gallery", Snackbar.LENGTH_SHORT).show();
                    fab.setVisibility(View.INVISIBLE);
                }
                if (destination.getId() == R.id.nav_info) {
                    Snackbar.make(findViewById(R.id.app_bar_main), "Info", Snackbar.LENGTH_SHORT).show();
                    fab.setVisibility(View.INVISIBLE);
                }
            }
        });

        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_log_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LogOut();
                return true;
            }
        });

        // ===== User info =====
        View headView = navigationView.getHeaderView(0);
        userImage = headView.findViewById(R.id.imageViewUserImage);
        userName = headView.findViewById(R.id.textViewUserName);
        userEmail = headView.findViewById(R.id.textViewUserEmail);

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            downloadPhoto(userImage, photoUrl, this);
            userName.setText(name);
            userEmail.setText(email);
        }
    }

    // ===== Permissions handling =====
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (arePermissionDenied()) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION);
            }
        }
    }

    private boolean arePermissionDenied() {
        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "Req Code: " + requestCode);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted!", LENGTH_SHORT).show();
            } else {
                LogOut();
            }
        }
    }

    // ===== Menu navigation handling =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Logout method
     */
    private void LogOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, "Signed out!", LENGTH_SHORT).show();
                        // Signed out successfully.
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        // loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loginIntent);
                        finish();
                    }
                });
    }

    /**
     * Info action
     * TODO
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){

        }
        return super.onOptionsItemSelected(item);
    }

    // ===== Methods for downloading users photo =====

    /**
     * @param imageView -
     * @param uri       -
     * @param context   -
     */
    private void downloadPhoto(ImageView imageView, Uri uri, Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(imageView);
            downloadImageTask.setUri(uri);
            downloadImageTask.execute();
        }
    }

    /**
     *
     */
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView bmImage;
        private Uri uri;

        private DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(uri.toString()).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        private void setUri(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bmImage.setImageResource(R.drawable.custom_account_icon);
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    // ===== Camera and photo handling =====

    private String provideNameAlert(){
        final String[] fileName = {"" };
        final EditText input = new EditText(this);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // this Title Fails to display
        alert.setTitle("Enter file name.");

        //  this Message Fails to display
        alert.setView(input);

        alert.setCancelable(false);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName[0] = input.getText().toString();
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

        return fileName[0];
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // User is returning from cropping the photo
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            textViewTitle = findViewById(R.id.text_title);
            textViewDescription = findViewById(R.id.text_description);
            constraintLayoutCroppedImg = findViewById(R.id.constraint_layout_cropped_img);
            imageViewCameraPhoto = findViewById(R.id.image_view_camera_photo);
            textViewImgName = findViewById(R.id.image_name_home);
            textViewPercAK = findViewById(R.id.text_view_perc_AK_home);
            textViewPercBCC = findViewById(R.id.text_view_perc_BCC_home);
            textViewPercBKL = findViewById(R.id.text_view_perc_BKL_home);
            textViewPercDF = findViewById(R.id.text_view_perc_DF_home);
            textViewPercMEL = findViewById(R.id.text_view_perc_MEL_home);
            textViewPercNV = findViewById(R.id.text_view_perc_NV_home);
            textViewPercSCC = findViewById(R.id.text_view_perc_SCC_home);
            textViewPercVASC = findViewById(R.id.text_view_perc_VASC_home);
            progressBarAK = findViewById(R.id.progressBar_AK_home);
            progressBarBCC = findViewById(R.id.progressBar_BCC_home);
            progressBarBKL = findViewById(R.id.progressBar_BKL_home);
            progressBarDF = findViewById(R.id.progressBar_DF_home);
            progressBarMEL = findViewById(R.id.progressBar_MEL_home);
            progressBarNV = findViewById(R.id.progressBar_NV_home);
            progressBarSCC = findViewById(R.id.progressBar_SCC_home);
            progressBarVASC = findViewById(R.id.progressBar_VASC_home);

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    constraintLayoutCroppedImg.setVisibility(View.VISIBLE);
                    textViewTitle.setVisibility(View.INVISIBLE);
                    textViewDescription.setVisibility(View.INVISIBLE);

                    Uri photoURI = result.getUri();
                    // Change URI to Bitmap
                    InputStream input = getContentResolver().openInputStream(photoURI);
                    Bitmap photoBitmap = BitmapFactory.decodeStream(input);
                    // Save Photo Bitmap
                    String imgPath = storeImage(photoBitmap);
                    // Show photo on home fragment after cropping
                    imageViewCameraPhoto.setImageURI(photoURI);
                    // ToDo: Perform calculations here.
                    calculate(classifier, photoBitmap, imgPath);
                }
                catch (Exception e){
                    Log.e(TAG, e.getMessage());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private JSONObject createJSONFile(String imgName, String predicted, float[] scores){
        JSONObject infoFile = new JSONObject();
        JSONObject scoresJSON = new JSONObject();
        try {
            scoresJSON.put("AK", scores[0]*100);
            scoresJSON.put("BCC", scores[1]*100);
            scoresJSON.put("BKL", scores[2]*100);
            scoresJSON.put("DF", scores[3]*100);
            scoresJSON.put("MEL", scores[4]*100);
            scoresJSON.put("NV", scores[5]*100);
            scoresJSON.put("SCC", scores[6]*100);
            scoresJSON.put("VASC", scores[7]*100);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            infoFile.put("Name", imgName);
            infoFile.put("Predicted", predicted);
            infoFile.put("Scores", scoresJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Writer output = null;
            String name = getNameFromFileName(imgName);
            File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + DIRECTORY_NAME + "/" + user.getUid());
            File file = new File(storageDir.getPath() + "/" + name + ".json");
            output = new BufferedWriter(new FileWriter(file));
            output.write(infoFile.toString());
            output.close();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return infoFile;
    }

    private void performCrop() {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }

    private String storeImage(Bitmap image) {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(TAG, "Error occurred while creating the File");
        }
        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return photoFile.getAbsolutePath();
    }

    /**
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + DIRECTORY_NAME + "/" + user.getUid());
        File image = File.createTempFile(
                imageFileName,       /* prefix */
                ".jpg",       /* suffix */
                storageDir          /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Managing multiple full-sized images can be tricky with limited memory.
     * If you find your application running out of memory after displaying just a few images,
     * you can dramatically reduce the amount of dynamic heap used by expanding the JPEG
     * into a memory array that's already scaled to match the size of the destination view.
     * The following example method demonstrates this technique.
     */
    public void setPic(ImageView imageView, String photoPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = 1;//Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Method which checks if directory exists and if not it is created.
     */
    private void check() {
        // ===== SetUp a directory =====
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + DIRECTORY_NAME;
        path = path + "/" + user.getUid();

        File dir = new File(path);
        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir();
            if (isDirectoryCreated) {
                Toast.makeText(this, "Directory created!", LENGTH_SHORT).show();
            }
        }
        if (isDirectoryCreated) {
            Log.d("Folder", "Already Created");
        } else {
            Toast.makeText(this, "Directory creation failed! " + path, LENGTH_SHORT).show();
        }
    }

    public static String getNameFromPath(String path){
        String name = "";
        Pattern pattern = Pattern.compile("[^/]+$");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find())
        {
            name = matcher.group();
        }
        return name;
    }

    private String getNameFromFileName(String fileName){
        String name = "";
        Pattern pattern = Pattern.compile(".*[^\\.jpg]");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find())
        {
            name = matcher.group();
        }
        return name;
    }

    private Classifier load() {
        String path1 = Utils.assetFilePath(this,"ResNet-18_mobile.pt");
        String path2 = Utils.assetFilePath(this,"ResNet-18_mobile.pt");
        String path3 = Utils.assetFilePath(this,"ResNet-18_mobile.pt");
        String path4 = Utils.assetFilePath(this,"ResNet-18_mobile.pt");
        String path5 = Utils.assetFilePath(this,"ResNet-18_mobile.pt");

        return new Classifier(path1, path2, path3, path4, path5);
    }

    private void calculate(final Classifier classifier, final Bitmap bitmap, final String imgPath) {
        mProgressDialog = ProgressDialog.show(this, "Please wait","Calculating scores...", true);
        new Thread() {
            @Override
            public void run() {
                //Perform calculations
                classifier.calculateScores(bitmap);
                final float[] scores = classifier.getScores();
                final String pred = classifier.predict();

                try {
                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void run() {
                            //Update view
                            String imgName = getNameFromPath(imgPath);
                            textViewImgName.setText(imgName);
                            textViewPercAK.setText(String.format("%.1f", scores[0]*100));
                            textViewPercBCC.setText(String.format("%.1f", scores[1]*100));
                            textViewPercBKL.setText(String.format("%.1f", scores[2]*100));
                            textViewPercDF.setText(String.format("%.1f", scores[3]*100));
                            textViewPercMEL.setText(String.format("%.1f", scores[4]*100));
                            textViewPercNV.setText(String.format("%.1f", scores[5]*100));
                            textViewPercSCC.setText(String.format("%.1f", scores[6]*100));
                            textViewPercVASC.setText(String.format("%.1f", scores[7]*100));
                            progressBarAK.setProgress((int) (scores[0]*100));
                            progressBarBCC.setProgress((int) (scores[1]*100));
                            progressBarBKL.setProgress((int) (scores[2]*100));
                            progressBarDF.setProgress((int) (scores[3]*100));
                            progressBarMEL.setProgress((int) (scores[4]*100));
                            progressBarNV.setProgress((int) (scores[5]*100));
                            progressBarSCC.setProgress((int) (scores[6]*100));
                            progressBarVASC.setProgress((int) (scores[7]*100));

                            // Save info to JSON file
                            if(imgPath != null){
                                createJSONFile(imgName, pred, scores);
                            }
                            mProgressDialog.dismiss();
                        }
                    });
                } catch (final Exception ex) {
                    Log.i("---","Exception in thread");
                }
            }
        }.start();
    }

}
