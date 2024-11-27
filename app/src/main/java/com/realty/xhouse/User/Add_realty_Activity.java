package com.realty.xhouse.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.realty.xhouse.Models.Realty;
import com.realty.xhouse.R;

public class Add_realty_Activity extends AppCompatActivity {

    private String cityInput, addressInput, aboutInput;
    private int roomsInput, floorInput;
    private double areaInput, priceInput;
    private ImageView realtyImage;
    private EditText city, address, rooms, area, floor, about, price;
    private Button ButtonAdd;
    private static final int GALLERYPICK = 1;
    private Uri imageUri;
    private StorageReference realtyImageRef;
    private String downloadImageUrl;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_realty);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addRealty), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        init();

        realtyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        ButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductData();
            }
        });
    }

    private void ValidateProductData() {
        cityInput = city.getText().toString();
        addressInput = address.getText().toString();
        try {
            roomsInput = Integer.parseInt(rooms.getText().toString());
            areaInput = Double.parseDouble(area.getText().toString());
            floorInput = Integer.parseInt(floor.getText().toString());
            priceInput = Double.parseDouble(price.getText().toString());
        } catch (NumberFormatException e) {
            roomsInput = 0;
            areaInput = 0;
            floorInput = 0;
            priceInput = 0;
        }
        aboutInput = about.getText().toString();

        if(imageUri == null){
            Toast.makeText(this, "Додайте фото нерухомості.", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(cityInput)){
            Toast.makeText(this, "Додайте місто.", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(addressInput)){
            Toast.makeText(this, "Додайте адресу.", Toast.LENGTH_SHORT).show();
        } else if(roomsInput <= 0){
            Toast.makeText(this, "Додайте кількість кімнат.", Toast.LENGTH_SHORT).show();
        } else if(areaInput <= 0){
            Toast.makeText(this, "Додайте площю нерухомості.", Toast.LENGTH_SHORT).show();
        } else if(floorInput <= 0){
            Toast.makeText(this, "Додайте поверх.", Toast.LENGTH_SHORT).show();
        } else if(priceInput <= 0){
            Toast.makeText(this, "Додайте ціну нерухомості.", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(aboutInput)){
            Toast.makeText(this, "Додайте опис.", Toast.LENGTH_SHORT).show();
        } else {
            StoreRealtyInfo();
        }

    }

    private void StoreRealtyInfo() {
        loadingBar.setTitle("Збереження нарухомості");
        loadingBar.setMessage("Будь ласка, почекайте...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        StorageReference filePath = realtyImageRef.child(imageUri.getLastPathSegment() + ".webm");

        final UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(Add_realty_Activity.this, "Помилка: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = filePath.getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();
                            SaveRealtyInfoToDatabase();
                        } else {
                            Toast.makeText(Add_realty_Activity.this, "Не вдалося отримати URL зображення.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    private void SaveRealtyInfoToDatabase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference realtyRef = FirebaseDatabase.getInstance().getReference().child("Realty");
        String realtyId = realtyRef.push().getKey();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(Add_realty_Activity.this, "Користувач не авторизований", Toast.LENGTH_SHORT).show();
            return;
        }

        Realty realty = new Realty(cityInput, addressInput, roomsInput, areaInput, floorInput, priceInput, aboutInput, downloadImageUrl, userId);

        realtyRef.child(realtyId).setValue(realty)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(Add_realty_Activity.this, "Дані збережено успішно.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Add_realty_Activity.this, "Помилка збереження даних.", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERYPICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERYPICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            realtyImage.setImageURI(imageUri);
        }
    }


    private void init(){
        realtyImage = findViewById(R.id.select_realty_image);
        city = findViewById(R.id.inputCityField);
        address = findViewById(R.id.inputAddressField);
        rooms = findViewById(R.id.inputRoomsField);
        area = findViewById(R.id.inputAresField);
        floor = findViewById(R.id.inputFloorField);
        about = findViewById(R.id.inputAboutField);
        ButtonAdd = findViewById(R.id.searchButton);
        price = findViewById(R.id.inputPriceField);
        realtyImageRef = FirebaseStorage.getInstance().getReference().child("realtyImages");
        loadingBar = new ProgressDialog(this);
    }
}