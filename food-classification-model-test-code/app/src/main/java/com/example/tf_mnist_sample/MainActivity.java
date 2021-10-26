package com.example.tf_mnist_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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


            InputStream stream = getContentResolver().openInputStream(data.getData());
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            Bitmap.createScaledBitmap(bmp, 20, 20, true);

            stream.close();

            ImageView iv = findViewById(R.id.photo);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);    // [300, 300]에 꽉 차게 표시
            iv.setImageBitmap(bmp);


            int batchNum = 0;
            float[][][][] input = new float[1][20][20][3];
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 20; y++) {
                    int pixel = bmp.getPixel(x, y);
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                }
            }

//            float[][] bytes_img = new float[1][784];
//            for (int y = 0; y < 28; y++) {
//                for (int x = 0; x < 28; x++) {
//                    int pixel = bmp.getPixel(x, y);
//                    bytes_img[0][y * 28 + x] = (pixel & 0xff) / (float) 255;
//                }
//            }

            Interpreter tf_lite = getTfliteInterpreter("food_model.tflite");
            float[][] output = new float[1][1];
            tf_lite.run(input, output);
            System.out.println(output);
            int[] id_array = {R.id.result_0};

            TextView tv = findViewById(id_array[0]);
            if(output[0][0]==0) {
                tv.setText("음식아님");    // [0] : 2차원 배열의 첫 번째
            }
            else{
                tv.setText("음식임");
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

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}