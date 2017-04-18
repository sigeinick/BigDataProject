package edu.umkc.anonymous.cs5542project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiFileImage;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageClassificationActivity extends AppCompatActivity {

    TextView outputTextView;
    ImageView image;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // not best practice; should set up asynchronous task on api call
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        Bitmap photoImage;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_classification);
        outputTextView = (TextView) findViewById(R.id.text_output);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        image = (ImageView) findViewById(R.id.image_to_analyze);

        Intent intent = getIntent();
        photoImage = intent.getParcelableExtra("Image");
        if (photoImage != null) {
            image.setImageBitmap(photoImage);
        } else {
            image.setImageResource(R.drawable.nichols);
        }
    }

    public void onClickClarifai(View v) {
        final ClarifaiClient client = new ClarifaiBuilder("gkRbcRqTrqFxXIWg8oWqZf8FpnkHLXw81V_skWzY", "TsqyYSMhocLidZ1s-Q-wtFVmWEnP4PAt8p9O1iSI")
                .client(new OkHttpClient()).buildSync();

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.nichols);

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "drawable");
        boolean doSave = true;
        if (!dir.exists()) {
            doSave = dir.mkdirs();
        }

        if (doSave) {
            saveBitmapToFile(dir,"nichols.png",bm,Bitmap.CompressFormat.PNG,100);
        }
        else {
            Log.e("app","Couldn't create target directory.");
        }

        try {
            File file = new File(dir, "nichols.png");
            ClarifaiFileImage img = ClarifaiImage.of(file);
            ClarifaiInput input = ClarifaiInput.forImage(img);
            ClarifaiResponse response = client.getDefaultModels().generalModel().predict()
                    .withInputs(input)
                    .executeSync();

            List<ClarifaiOutput<Concept>> predictions = (List<ClarifaiOutput<Concept>>) response.get();
            String output = "Image contains ";
            List<Concept> data = predictions.get(0).data();
            for (int i = 0; i < data.size(); i++) {
                output += data.get(i).name() + " ";
            }
            outputTextView.setText(output);
        } catch (Exception e) {
            outputTextView.setText(e.getMessage());
        }
    }


    public void onClickPhoto(View v) {
        Intent redirect = new Intent(ImageClassificationActivity.this, PhotoActivity.class);
        startActivity(redirect);

    }

    public void onClickSpark(View v) {
        String url = "http://192.168.1.195:8080/get_custom";
        //ImageView image = (ImageView) findViewById(R.id.image_to_analyze);

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) image.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);

        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        try {
            while ((bytesRead = bis.read(buffer)) != -1) {
                bytearrayoutputstream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = bytearrayoutputstream.toByteArray();
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/x-markdown"), encodedString);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final JSONObject jsonResult;
                    final String result = response.body().string();

                    Log.d("okHttp", result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputTextView.setText(result);
                        }
                    });

                }
            });
        } catch (Exception ex) {
            outputTextView.setText(ex.getMessage());
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ImageClassification Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public boolean saveBitmapToFile(File dir, String filename, Bitmap bm, Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return true;
        }
        catch (IOException e) {
            Log.e("app",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());
        client2.disconnect();
    }


}
