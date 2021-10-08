package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements itemClickListener {
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

        findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if (isPermission) goToAlbum();
                else
                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });

        // Map 버튼 눌렀을 때
        Button mapButton = (Button) findViewById(R.id.btnMap);
        mapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), MapWithPhoto.class);
                v.getContext().startActivity(intent);
            }
        });


        //리무브 버튼 눌렀을 때
        Button editButton = (Button) findViewById(R.id.btnRemove);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제할 지역 폴더를 선택하세요.");

                File[] storageDir = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/")};
                if (!storageDir[0].exists()) storageDir[0].mkdirs();
                File[] locationFoldersNames = storageDir[0].listFiles();


                final int[] number = {locationFoldersNames.length};


                String[] localArray = new String[number[0]];

                for(int i = 0; i < number[0]; i++){
                    localArray[i]= locationFoldersNames[i].getName();
                }


                String[] selectedItem= {localArray[0]};


                dlg.setSingleChoiceItems(localArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem[0] = localArray[which];
                    }
                });


                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AlertDialog.Builder dlgSecond = new AlertDialog.Builder(MainActivity.this);
                        dlgSecond.setTitle("삭제할 폴더를 선택하세요.");

                        String[] humanLabel = new String[]{"인물", "그 외 폴더", "전부삭제"};
                        String[] selectedItemsSecond = {"인물"};

                        dlgSecond.setSingleChoiceItems(humanLabel, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedItemsSecond[0] = humanLabel[which];

                            }

                        });

                        dlgSecond.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File[] checkDir = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0] +"/"+ selectedItemsSecond[0])};



                                if(selectedItemsSecond[0].equals("전부삭제")){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0]);
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, "지역폴더 삭제 완료",Toast.LENGTH_SHORT).show();

                                }
                                else if(selectedItemsSecond[0].equals("그 외 폴더") && checkDir[0].exists()){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0]  +"/그 외 폴더");
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, selectedItem[0] + "의 " + selectedItemsSecond[0] +" 삭제 완료",Toast.LENGTH_SHORT).show();

                                }
                                else if(selectedItemsSecond[0].equals("인물") && checkDir[0].exists()){
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0]  +"/인물");
                                    DeleteDir(dir.getPath());
                                    Toast.makeText(MainActivity.this, selectedItem[0] + "의 " + selectedItemsSecond[0] +" 폴더 삭제 완료",Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(MainActivity.this, "잘못된 접근입니다.",Toast.LENGTH_SHORT).show();

                                }



                            }
                        });

                        dlgSecond.show();
                    }
                });
                dlg.show();

            }


        });


        //수정 버튼 눌렀을 때
        Button renameButton = (Button) findViewById(R.id.btnRename);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("수정할 지역 폴더를 선택하세요.");

                File[] storageDir = {new File(Environment.getExternalStorageDirectory() + "/addPhoto/")};
                if (!storageDir[0].exists()) storageDir[0].mkdirs();
                File[] locationFoldersNames = storageDir[0].listFiles();

                final String[] rename = {null};

                final int[] number = {locationFoldersNames.length};


                String[] localArray = new String[number[0]];

                for(int i = 0; i < number[0]; i++){
                    localArray[i]= locationFoldersNames[i].getName();
                }


                String[] selectedItem= {localArray[0]};


                dlg.setSingleChoiceItems(localArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItem[0] = localArray[which];
                    }
                });

                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editText = new EditText(view.getContext());
                        AlertDialog.Builder renameDLG = new AlertDialog.Builder(view.getContext());
                        renameDLG.setTitle("수정할 이름을 입력하세요.");
                        renameDLG.setView(editText);
                        renameDLG.setPositiveButton("입력",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                        renameDLG.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rename[0] = editText.getText().toString();

                                File newFolderDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + rename[0]);
                                if (!newFolderDir.exists()) newFolderDir.mkdir();
                                File chooseFolder = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + selectedItem[0]);

                                boolean success = chooseFolder.renameTo(newFolderDir);


                            }
                        });

                        renameDLG.show();
                    }
                });

                dlg.show();
            }
        });

        //메인 화면에 지역명폴더 띄우기
        getPicturePath();

    }
    
    private void DeleteDir(String path) {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            }
            else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
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
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) { //데이터가 없을때 생기는 오류 방지
            } else {
                if (data.getClipData() == null) {
                    Toast.makeText(this, "다중선택이 불가한 기기입니다.", Toast.LENGTH_LONG).show();
                } else { //사진 선택
                    ClipData clipData = data.getClipData();
                    Log.i("clipData", String.valueOf(clipData.getItemCount())); // 사용자가 몇개 골랐는지

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        imagePath = getRealPathFromURI(clipData.getItemAt(i).getUri());

                        if(imagePath == null){
                            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else{
                            File photoFile = new File(imagePath);
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                            try {

                                checkDate(photoFile);
                                createDirectory();
                                photoLocation = checkLocation(photoFile); //위치 받아오기

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            initDetector(image, photoFile, photoLocation); //위치 + 인물 구별해서 폴더에 저장
                        }
                    }

                }
            }
        }
    }


    private void initDetector(FirebaseVisionImage image, File photoFile, String photoLocation) {
        //얼굴인식 옵션. 정확성 높이는 옵션 추가.
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)

                .build();
        FirebaseVisionFaceDetector faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);

        faceDetector.detectInImage(image).addOnSuccessListener(
                faces -> {
                    if (!faces.isEmpty()) { //인물사진일때
                        System.out.println("인물!");
                        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/" + "인물/");
                        if (!storageDir.exists()) storageDir.mkdirs(); // 폴더가 존재하지 않는다면 생성
                        File savedImageFile = new File(storageDir, photoFile.getName());

                        try {
                            copy(photoFile, savedImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("인물 x!");
                        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/" + "그 외 폴더/");
                        if (!storageDir.exists()) storageDir.mkdirs(); // 폴더가 존재하지 않는다면 생성
                        File savedImageFile = new File(storageDir, photoFile.getName());

                        try {
                            copy(photoFile, savedImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                });

    }



    // 사진 URI 얻어오는 함수 : 2016년부터 ? 변경
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

    // 앨범에서 사진 선택
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK); //앨범 호출
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE); //이미지 파일
        intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 선택 가능
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // 저장폴더 생성
    private void createDirectory() throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
        if (!storageDir.exists())
            System.out.println(storageDir);
        storageDir.mkdirs(); // 폴더가 존재하지 않는다면 생성
    }

    // 파일 복사
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

    // 권한 설정
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
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
            Toast.makeText(this, "위치가져오기오류!", Toast.LENGTH_LONG).show();
        }
        return photoLocation;
    }

    private String showExif(ExifInterface exif) throws IOException {

        String photoLocation = null;

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;
        String latitude = getTagString(ExifInterface.TAG_GPS_LATITUDE, exif); // 위도
        latitude = latitude.substring(14); // GPSLatitude : null -> null
        String longitude = getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif); // 경도
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
                Toast.makeText(this, "위치변환오류!", Toast.LENGTH_LONG).show();
            }

            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(this, "해당주소없음!", Toast.LENGTH_LONG).show();
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

}