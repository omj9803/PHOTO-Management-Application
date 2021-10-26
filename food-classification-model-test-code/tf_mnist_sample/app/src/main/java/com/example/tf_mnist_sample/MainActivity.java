package com.example.tf_mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int FROM_ALBUM = 1;
    private static final int FROM_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FROM_ALBUM);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode != FROM_ALBUM || requestCode != RESULT_OK) {
//            return;
//        }

        try {
            // 선택한 이미지에서 비트맵 생성
            InputStream stream = getContentResolver().openInputStream(data.getData());
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            stream.close();

            ImageView iv = findViewById(R.id.photo);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);    // [300, 300]에 꽉 차게 표시
            iv.setImageBitmap(bmp);

            // 한장에 대해서 예측할 경우 1차원배열로,
            // 여러장을 한번에 전달하여 예측하기 위해서는 2차원 배열로 만들어야 한다.
            float[][] bytes_img = new float[1][400];
            for (int y = 0; y < 20; y++) {
                for (int x = 0; x < 20; x++) {
                    int pixel = bmp.getPixel(x, y);
                    bytes_img[0][y * 20 + x] = (pixel & 0xff) / (float) 255;
                }
            }

            // 파이썬에서 만든 모델 파일 로딩
            Interpreter tf_lite = getTfliteInterpreter("airplane_image_classification_model.tflite");

            // 출력 배열 생성
            float[][] output = new float[1][1];
            tf_lite.run(bytes_img, output);

            System.out.println(Arrays.toString(output[0]));
            int[] id_array = {R.id.result_0};

            for (int i = 0; i < 1; i++) {
                TextView tv = findViewById(id_array[i]);
                tv.setText(String.format("%.5f", output[0][i]));    // [0] : 2차원 배열의 첫 번째
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* 모델을 읽어오는 함수 */
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}