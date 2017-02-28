package edu.umkc.anonymous.cs5542project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

    public class PhotoActivity extends AppCompatActivity {
        int TAKE_PHOTO_CODE = 0;
        ImageView userPhoto;
        Bitmap photo;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_photo);

            Button takePhoto = (Button) findViewById(R.id.button_take_photo);
            userPhoto = (ImageView)  findViewById(R.id.user_photo);
            takePhoto.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK ) {
                photo = (Bitmap) data.getExtras().get("data");
                userPhoto.setImageBitmap(photo);
                //ImageView userIcon = (ImageView) findViewById(R.id.signup_imageView);
                //userIcon.setImageBitmap(photo);
            }
        }

        public void redirect(View v) {
            Intent redirect = new Intent(PhotoActivity.this, ImageClassificationActivity.class);
            redirect.putExtra("Image", photo);
            startActivity(redirect);
        }
    }
