package com.iuce.opticaltestreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.iuce.opticaltestreader.omr.Scanner;

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


public class HomeFragment extends Fragment {

    public Button btnGetImage;
    public ImageView imageView;
    public Uri imageUri;
    public TextView rate2TextView;
    public Mat originalImage;
    public String answerList;
    public static final String IMAGE_DIRECTORY = "/OMR Sheets";
    public int GALLERY = 1, CAMERA = 2;
    public Scanner scanner;

    public String path;
    public FrameLayout content_frame;
    public FirebaseAuth firebaseAuth;

    public HomeFragment() {
        // Required empty public constructor
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        btnGetImage =  rootView.findViewById(R.id.btnGetImage);
        imageView = rootView.findViewById(R.id.imageView);
        rate2TextView = rootView.findViewById(R.id.rate2);
        content_frame = rootView.findViewById(R.id.content_frame);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), Login.class));
            //finish();
        }

        btnGetImage.setOnClickListener(v -> showPictureDialog());

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getContext(), "There is a problem in OpenCV", Toast.LENGTH_SHORT).show();
        } else {
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

        Gson gson = new Gson();

        String answers =  PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("exam1", null);
        if(answers!=null){
            List<String> answerArray = gson.fromJson(answers,new TypeToken<List<String>>(){}.getType());
            String message = "ANSWER KEY \n\n";
            for(int i=0 ; i<answerArray.size(); i++){
                String oneRow = (i+1)+"-"+answerArray.get(i)+"\n";
                message = message + oneRow;
            }
            rate2TextView.setText(message);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                //Uri contentURI = data.getData();
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);

                imageUri = data.getData();

                path = getPath(imageUri);

                originalImage = Imgcodecs.imread(path);

                scanner = new Scanner(originalImage, 20);

                Gson gson = new Gson();

                String answers =  PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("exam1", null);
                if(answers!=null) {
                    List<String> answerArray = gson.fromJson(answers, new TypeToken<List<String>>() {
                    }.getType());
                    scanner.setAnswersBy(answerArray);
                }

                scanner.setLogging(true);

                final TextView textViewToChange = getActivity().findViewById(R.id.rate);
                final TextView examResult = getActivity().findViewById(R.id.examResult);

                StringBuilder builder = new StringBuilder();

                try {
                    answerList = String.valueOf(scanner.scan());
                    builder = scanner.getExamResult();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.RGB_565);

                // Convert mat to bitmap
                Utils.matToBitmap(originalImage, bitmap);

                saveImage(bitmap);

                textViewToChange.setText(answerList);
                examResult.setText(builder);

                Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_SHORT).show();

                android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                layoutParams.width = bitmap.getWidth();
                layoutParams.height = bitmap.getHeight();

                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
            }

        } else if (requestCode == CAMERA) {

            //Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            //path = saveImage(bitmap);

            path = getPath(imageUri);

            originalImage = Imgcodecs.imread(path);

            scanner = new Scanner(originalImage, 20);

            scanner.setLogging(true);

            final TextView textViewToChange = getActivity().findViewById(R.id.rate);

            try {
                answerList = String.valueOf(scanner.scan());
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            // Convert mat to bitmap
            Utils.matToBitmap(originalImage, bitmap);

            saveImage(bitmap);
            */

            Bitmap bitmap = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.RGB_565);

            // Convert mat to bitmap
            Utils.matToBitmap(originalImage, bitmap);

            saveImage(bitmap);

            textViewToChange.setText(answerList);


            Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_SHORT).show();

            android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = 960;
            layoutParams.height = 1280;

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
            MediaScannerConnection.scanFile(getContext(),
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
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                return cursor.getString(col_index);
            }
        }

        return uri.getPath();
    }

    private void showPictureDialog(){
        android.app.AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallary();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
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
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // Camera Intent
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA);
    }
}
