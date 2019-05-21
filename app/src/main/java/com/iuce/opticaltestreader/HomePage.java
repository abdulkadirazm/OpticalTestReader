package com.iuce.opticaltestreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iuce.opticaltestreader.omr.Scanner;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private TextView textEmail;
    private Button logOut, btn, buttonAnswer;
    private ImageView imageView;
    private Uri imageUri;

    private Mat originalImage;
    private String answerList;
    private static final String IMAGE_DIRECTORY = "/OMR Sheets";
    private int GALLERY = 1, CAMERA = 2;

    private String path;

    private FirebaseAuth firebaseAuth;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    originalImage = new Mat();
                } break;

                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home_page);

        requestMultiplePermissions();

        buttonAnswer = findViewById(R.id.buttonAnswer);
        logOut = findViewById(R.id.logOut);
        btn =  findViewById(R.id.btn);
        imageView = findViewById(R.id.imageView);
        textEmail = findViewById(R.id.textEmail);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        textEmail.setText("Welcome " + user.getEmail());

        buttonAnswer.setOnClickListener(v -> startActivity(new Intent(HomePage.this,AnswerKeyActivity.class)));

        logOut.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(HomePage.this,Login.class));
            finish();
        });

        imageView.setOnClickListener(v -> Toast.makeText(HomePage.this,
                "The favorite list would appear on clicking this icon " + path,
                Toast.LENGTH_LONG).show());

        btn.setOnClickListener(v -> showPictureDialog());

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem in OpenCV", Toast.LENGTH_SHORT).show();
        } else {
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                //Uri contentURI = data.getData();
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);

                imageUri = data.getData();

                path = getPath(imageUri);

                originalImage = Imgcodecs.imread(path);

                Scanner scanner = new Scanner(originalImage, 20);

                scanner.setLogging(true);

                final TextView textViewToChange = findViewById(R.id.rate);

                try {
                    answerList = String.valueOf(scanner.scan());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.RGB_565);

                // Convert mat to bitmap
                Utils.matToBitmap(originalImage, bitmap);

                saveImage(bitmap);

                textViewToChange.setText(answerList);

                Toast.makeText(HomePage.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                layoutParams.width = bitmap.getWidth();
                layoutParams.height = bitmap.getHeight();

                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
            }

        } else if (requestCode == CAMERA) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            path = saveImage(bitmap);

            originalImage = Imgcodecs.imread(path);

            Scanner scanner = new Scanner(originalImage, 20);

            scanner.setLogging(true);

            final TextView textViewToChange = findViewById(R.id.rate);

            try {
                answerList = String.valueOf(scanner.scan());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Convert mat to bitmap
            Utils.matToBitmap(originalImage, bitmap);

            saveImage(bitmap);

            textViewToChange.setText(answerList);

            Toast.makeText(HomePage.this, "Image Saved!", Toast.LENGTH_SHORT).show();

            android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = bitmap.getWidth();
            layoutParams.height = bitmap.getHeight();

            imageView.setLayoutParams(layoutParams);
            imageView.setImageBitmap(bitmap);
        }
    }

    public String saveImage(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                return cursor.getString(col_index);
            }
        }

        return uri.getPath();
    }

    private void showPictureDialog(){
        android.app.AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
}
