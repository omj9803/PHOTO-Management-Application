package com.example.testapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.testapp.utils.CategoryFolder;
import com.example.testapp.utils.PicHolder;
import com.example.testapp.utils.folderAdapter;
import com.example.testapp.utils.imageFolder;
import com.example.testapp.utils.itemClickListener;
import com.example.testapp.utils.pictureFacer;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements itemClickListener{
    private Boolean isPermission = true;
    private static final int PICK_FROM_ALBUM = 1;
    private String photoDate;

    RecyclerView folderRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        tedPermission();


        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light
        );


        //toolbar ??????
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // ?????? ????????? ???????????? ????????? ?????? ???????????? ????????????.
//                if (isPermission) goToAlbum();
//                else
//                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
//            }
//        });

        // Map ?????? ????????? ???
        Button mapButton = (Button) findViewById(R.id.btnMap);
        mapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), MapWithPhoto.class);
                v.getContext().startActivity(intent);
            }
        });


        //?????? ????????? ??????????????? ?????????
        getPicturePath();

    }

    //?????? ?????? ??????
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //?????? ?????? ?????? ?????????
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){

            //?????? - ????????????
            case R.id.btnGallery:
                if(isPermission) goToAlbum();
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                return true;


            //?????? - ???????????? ??? ?????? ??????
            case R.id.btnRename:
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("????????? ?????? ????????? ???????????????.");

                File[] storageDir = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/")};
                if(!storageDir[0].exists()) storageDir[0].mkdirs();
                File[] locationFoldersNames = storageDir[0].listFiles();

                final String[] rename = {null};

                final int[] number = {locationFoldersNames.length};

                String[] localArray = new String[number[0]];

                for(int i = 0; i < number[0]; i++){
                    localArray[i] = locationFoldersNames[i].getName();
                }

                //????????????
                String[] selectedItem = {localArray[0]};

                dlg.setSingleChoiceItems(localArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem[0] = localArray[which];
                    }
                });

                dlg.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editText = new EditText(MainActivity.this);
                        AlertDialog.Builder renameDLG = new AlertDialog.Builder(MainActivity.this);
                        renameDLG.setTitle("????????? ????????? ???????????????.");
                        renameDLG.setView(editText);
                        renameDLG.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        renameDLG.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rename[0] = editText.getText().toString();

                                File newFolderDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + rename[0]);
                                if(!newFolderDir.exists()) newFolderDir.mkdir();
                                File chooseFolder = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0]);

                                boolean success = chooseFolder.renameTo(newFolderDir);
                            }
                        });
                        renameDLG.show();
                    }
                });

                dlg.show();
                return true;


            //?????? - ???????????? ??? ?????? ??????
            case R.id.btnRemove:
                AlertDialog.Builder dlg_ = new AlertDialog.Builder(MainActivity.this);
                dlg_.setTitle("????????? ?????? ????????? ???????????????.");

                File[] storageDir_ = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/")};
                if (!storageDir_[0].exists()) storageDir_[0].mkdirs();
                File[] locationFoldersNamess = storageDir_[0].listFiles();

                final int[] numbers = {locationFoldersNamess.length};

                String[] localArrays = new String[numbers[0]];

                for(int i = 0; i < numbers[0]; i++){
                    localArrays[i]= locationFoldersNamess[i].getName();
                }

                //????????????
                String[] selectedItem_ = {localArrays[0]};

                dlg_.setSingleChoiceItems(localArrays, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem_[0] = localArrays[which];
                    }
                });

                dlg_.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder dlgSecond = new AlertDialog.Builder(MainActivity.this);
                        dlgSecond.setTitle("????????? ????????? ???????????????.");

                        String[] humanLabel = new String[]{"??????", "??????","??????" ,"????????????"};
                        String[] selectedItemsSecond = {"??????"};

                        dlgSecond.setSingleChoiceItems(humanLabel, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedItemsSecond[0] = humanLabel[which];
                            }
                        });

                        dlgSecond.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File[] checkDir = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem_[0]+"/"+ selectedItemsSecond[0])};

                                if(selectedItemsSecond[0].equals("????????????")){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem_[0]);
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                                else if(selectedItemsSecond[0].equals("??????")&& checkDir[0].exists()){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem_[0] + "/??????");
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, selectedItem_[0] + "??? " + selectedItemsSecond[0] + "?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                                else if(selectedItemsSecond[0].equals("??????")&& checkDir[0].exists()){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem_[0] + "/??????");
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, selectedItem_[0] + "??? " + selectedItemsSecond[0] + "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                                else if(selectedItemsSecond[0].equals("??????")&& checkDir[0].exists()){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem_[0] + "/??????");
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, selectedItem_[0] + "??? " + selectedItemsSecond[0] + "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dlgSecond.show();
                    }
                });
                dlg_.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void DeleteDir(String path) {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //?????? ???????????? ??????
            }
            else {
                childFile.delete();    //?????? ????????????
            }
        }
        file.delete();    //root ??????
    }


    private void getPicturePath(){

        folderRecycler = findViewById(R.id.folderRecycler);
        folderRecycler.hasFixedSize();

        ArrayList<imageFolder> folds = new ArrayList<>();

        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
        if (!storageDir.exists()) storageDir.mkdirs();
        File[] locationFoldersNames = storageDir.listFiles();
        for(int i = 0 ; i < locationFoldersNames.length; i++){
            imageFolder fold = new imageFolder();

            String filePath = Environment.getExternalStorageDirectory() + "/addPhoto/" + locationFoldersNames[i].getName();
            String fileName = locationFoldersNames[i].getName();
            File f = locationFoldersNames[i];
            File[] files = f.listFiles();

            fold.setPath(filePath);
            fold.setFolderName(fileName);
            fold.setNumberOfPics(files.length);
            folds.add(fold);
        }

        RecyclerView.Adapter folderAdapter = new folderAdapter(folds,MainActivity.this,this);
        folderRecycler.setAdapter(folderAdapter);

    }

    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<pictureFacer> pics) {
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        Intent move = new Intent(MainActivity.this, CategoryFolder.class);
        move.putExtra("folderPath",pictureFolderPath);
        move.putExtra("folderName",folderName);


        startActivity(move);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath = null;
        String photoLocation = null;


        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) { //???????????? ????????? ????????? ?????? ??????
            } else {
                if (data.getClipData() == null) {
                    Toast.makeText(this, "??????????????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                } else { //?????? ??????
                    ClipData clipData = data.getClipData();
                    Log.i("clipData", String.valueOf(clipData.getItemCount())); // ???????????? ?????? ????????????

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        imagePath = getRealPathFromURI(clipData.getItemAt(i).getUri());

                        if (imagePath == null) {
                            Toast.makeText(this, "????????? ???????????????.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            File photoFile = new File(imagePath);
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                            try {

                                checkDate(photoFile);
                                createDirectory();
                                photoLocation = checkLocation(photoFile); //?????? ????????????

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            initDetector(bitmap, image, photoFile, photoLocation); //?????? + ?????? ???????????? ????????? ??????

                        }
                    }

                }
            }
        }
    }


    private void initDetector(Bitmap bitmap, FirebaseVisionImage image, File photoFile, String photoLocation) {
        //???????????? ??????. ????????? ????????? ?????? ??????.
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)

                .build();
        FirebaseVisionFaceDetector faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);

        faceDetector.detectInImage(image).addOnSuccessListener(
                faces -> {
                    if (!faces.isEmpty()) { //??????????????????
                        System.out.println("??????!");
                        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/" + "??????/");
                        if (!storageDir.exists()) storageDir.mkdirs(); // ????????? ???????????? ???????????? ??????
                        File savedImageFile = new File(storageDir, photoFile.getName());

                        try {
                            copy(photoFile, savedImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else { //?????? ????????? ?????? ??????
                        System.out.println("?????? x!");
                        Bitmap.createScaledBitmap(bitmap, 20, 20, true);

                        int batchNum = 0;
                        float[][][][] input = new float[1][20][20][3];
                        for (int x = 0; x < 20; x++) {
                            for (int y = 0; y < 20; y++) {
                                int pixel = bitmap.getPixel(x, y);

                                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                            }
                        }

                        Interpreter tf_lite = getTfliteInterpreter("food_model.tflite");
                        float[][] output = new float[1][1];
                        tf_lite.run(input, output);

                        System.out.println(output[0][0]);
                        if (Math.round(output[0][0]) == 1) {
                            File storageFoodDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/" + "??????/");
                            if (!storageFoodDir.exists())
                                storageFoodDir.mkdirs(); // ????????? ???????????? ???????????? ??????
                            File savedFoodImageFile = new File(storageFoodDir, photoFile.getName());

                            try {
                                copy(photoFile, savedFoodImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            File storageBackDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/" + "??????/");
                            if (!storageBackDir.exists())
                                storageBackDir.mkdirs(); // ????????? ???????????? ???????????? ??????
                            File savedBackImageFile = new File(storageBackDir, photoFile.getName());

                            try {
                                copy(photoFile, savedBackImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                });

    }



    // ?????? URI ???????????? ?????? : 2016????????? ? ??????
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // ???????????? ?????? ??????
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK); //?????? ??????
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE); //????????? ??????
        intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE, true); // ?????? ?????? ??????
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // ???????????? ??????
    private void createDirectory() throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
        if (!storageDir.exists())
            System.out.println(storageDir);
        storageDir.mkdirs(); // ????????? ???????????? ???????????? ??????
    }

    // ?????? ??????
    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    // ?????? ??????
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // ?????? ?????? ??????
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }

            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // ?????? ?????? ??????
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    private String checkLocation(File photoFile) {
        String photoLocation = null;

        String fileName = photoFile.getPath();
        Log.d("fileName", fileName);
        try {
            ExifInterface exif = new ExifInterface(fileName);
            photoLocation = showExif(exif);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "????????????????????????!", Toast.LENGTH_LONG).show();
        }
        return photoLocation;
    }

    private String showExif(ExifInterface exif) throws IOException {

        String photoLocation = null;

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;
        String latitude = getTagString(ExifInterface.TAG_GPS_LATITUDE, exif); // ??????
        latitude = latitude.substring(14); // GPSLatitude : null -> null
        String longitude = getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif); // ??????
        longitude = longitude.substring(15); // GPSLongitude : null -> null
        Log.d("latitude", latitude);
        Log.d("longitude", longitude);
        if (latitude.contains("null") || longitude.contains("null")) {
            photoLocation = "noLocation";
        } else {
            double d1 = changeGPS(latitude);
            double d2 = changeGPS(longitude);
            try {
                list = geocoder.getFromLocation(d1, d2, 10);
            } catch (IOException e) {
                e.printStackTrace();
                list = geocoder.getFromLocation(d1, d2, 10);
                Toast.makeText(this, "??????????????????!", Toast.LENGTH_LONG).show();
            }

            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(this, "??????????????????!", Toast.LENGTH_LONG).show();
                } else {
                    photoLocation = list.get(0).getLocality();
                    if (photoLocation == null) {
                        photoLocation = list.get(0).getAdminArea();
                        if (photoLocation == null) {
                            photoLocation = "noLocation";
                        }
                    }
                    Log.d("listAdminArea", list.get(0).getAdminArea());
                    Log.d("list", list.get(0).toString());
                    Log.d("listA", list.get(0).getAddressLine(0));
                }
            }
        }
        return photoLocation;
    }

    private double changeGPS(String gpsInfo) {
        String splitGPSInfo[] = gpsInfo.split(",");
        Log.d("splitGPSInfo0", splitGPSInfo[0]);
        Log.d("splitGPSInfo1", splitGPSInfo[1]);
        Log.d("splitGPSInfo2", splitGPSInfo[2]);
        Double changeGPSInfo[] = new Double[3];
        for (int i = 0; i < splitGPSInfo.length; i++) {
            int idx = splitGPSInfo[i].indexOf("/");
            double num1 = Double.parseDouble(splitGPSInfo[i].substring(0, idx));
            double num2 = Double.parseDouble(splitGPSInfo[i].substring(idx + 1));
            changeGPSInfo[i] = num1 / num2;
        }
        double d1 = changeGPSInfo[0];
        double d2 = changeGPSInfo[1];
        double d3 = changeGPSInfo[2];
        double changedGPSInfo = d1 + d2 / 60 + d3 / 3600;
        return changedGPSInfo;
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }


    private void checkDate(File photoFile) throws IOException {
        String fileName = photoFile.getPath();
        ExifInterface exif = new ExifInterface(fileName);
        photoDate = getTagString(ExifInterface.TAG_DATETIME, exif);
        Log.d("photoDate", photoDate);
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

