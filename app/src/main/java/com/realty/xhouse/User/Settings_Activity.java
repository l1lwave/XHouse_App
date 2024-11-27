package com.realty.xhouse.User;

import static com.realty.xhouse.R.id.settings_activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.realty.xhouse.Models.User;
import com.realty.xhouse.R;

import de.hdodenhof.circleimageview.CircleImageView;

import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class Settings_Activity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText lastNameEditText, firstNameEditText, middleNameEditText, userPhoneEditText, addressEditText;
    private TextView saveTextButton, closeTextBtn;
    private Uri imageUri;
    private String checker = "";
    FirebaseDatabase db;
    DatabaseReference users;
    private static final int GALLERYPICK = 1;
    private StorageReference storageProfilePictureRef;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(settings_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        profileImageView = findViewById(R.id.settings_account_image);
        lastNameEditText = findViewById(R.id.settings_last_name);
        firstNameEditText = findViewById(R.id.settings_first_name);
        middleNameEditText = findViewById(R.id.settings_middle_name);
        userPhoneEditText = findViewById(R.id.settings_phone);
        addressEditText = findViewById(R.id.settings_email);
        saveTextButton = findViewById(R.id.save_settings_tv);
        closeTextBtn = findViewById(R.id.close_settings_tv);

        closeTextBtn.setOnClickListener(view -> {
            Intent loginIntent = new Intent(Settings_Activity.this, Home_Activity.class);
            startActivity(loginIntent);
        });

        saveTextButton.setOnClickListener(view -> {
            if ("clicked".equals(checker)) {
                userInfoSaved();
            } else {
                updateOnlyUserInfo();
            }
        });

        profileImageView.setOnClickListener(view -> {
            checker = "clicked";
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, "Виберіть зображення"), GALLERYPICK);
        });

        loadUserData();

        Button rewritePasswordButton = findViewById(R.id.rewrite_password);
        rewritePasswordButton.setOnClickListener(view -> showChangePasswordDialog());

    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.change_pass_layout, null);
        builder.setView(dialogView);

        EditText currentPassword = dialogView.findViewById(R.id.current_password);
        EditText newPassword = dialogView.findViewById(R.id.new_password);

        builder.setPositiveButton("Зберегти", (dialog, which) -> {
            String currentPass = currentPassword.getText().toString();
            String newPass = newPassword.getText().toString();

            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)) {
                Toast.makeText(this, "Заповніть всі поля", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(currentPass, newPass);
        });

        builder.setNegativeButton("Відміна", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updatePassword(String currentPass, String newPass) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        users.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User existingUser = task.getResult().getValue(User.class);
                if (existingUser != null && currentPass.equals(existingUser.getPass())) {
                    existingUser.setPass(newPass);
                    users.child(userId).setValue(existingUser)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Пароль оновлено", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Помилка оновлення пароля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Поточний пароль неправильний", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Помилка отримання даних користувача", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    lastNameEditText.setText(user.getLastName());
                    firstNameEditText.setText(user.getFirstName());
                    middleNameEditText.setText(user.getMiddleName());
                    userPhoneEditText.setText(user.getPhone());
                    addressEditText.setText(user.getEmail());

                    String imageUrl = user.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImageView);
                    }
                }
            } else {
                Toast.makeText(this, "Неможливо завантажити дані користувача.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERYPICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));
            UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(500, 500)
                    .start(this);
        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageUri = resultUri;
                profileImageView.setImageURI(imageUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Помилка обрізки: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoSaved() {
        if (TextUtils.isEmpty(lastNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Заповніть прізвище.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(firstNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Заповніть ім'я.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(middleNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Заповніть побатькові.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
        {
            Toast.makeText(this, "Заполните адрес", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userPhoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Заполните номер", Toast.LENGTH_SHORT).show();
        }
        else if(checker.equals("clicked"))
        {
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Завантаження зображення...");
            progressDialog.show();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures").child(userId + ".jpg");

            uploadTask = storageProfilePictureRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageProfilePictureRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            updateUserInfo(imageUrl);
                            progressDialog.dismiss();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(Settings_Activity.this, "Помилка завантаження: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Виберіть зображення", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserInfo(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User existingUser = task.getResult().getValue(User.class);
                if (existingUser != null) {
                    String currentPassword = existingUser.getPass();

                    User user = new User();
                    user.setLastName(lastNameEditText.getText().toString());
                    user.setFirstName(firstNameEditText.getText().toString());
                    user.setMiddleName(middleNameEditText.getText().toString());
                    user.setEmail(addressEditText.getText().toString());
                    user.setPhone(userPhoneEditText.getText().toString());
                    user.setImage(imageUrl);
                    user.setPass(currentPassword);

                    users.child(userId).setValue(user)
                            .addOnSuccessListener(unused -> Snackbar.make(findViewById(R.id.main), "Дані користувача оновлено", Snackbar.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Неможливо завантажити дані користувача.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Помилка завантаження даних користувача.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateOnlyUserInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User existingUser = task.getResult().getValue(User.class);
                if (existingUser != null) {
                    String currentPassword = existingUser.getPass();
                    String currentImage = existingUser.getImage();

                    User user = new User();
                    user.setLastName(lastNameEditText.getText().toString());
                    user.setFirstName(firstNameEditText.getText().toString());
                    user.setMiddleName(middleNameEditText.getText().toString());
                    user.setEmail(addressEditText.getText().toString());
                    user.setPhone(userPhoneEditText.getText().toString());
                    user.setPass(currentPassword);
                    user.setImage(currentImage);

                    users.child(userId).setValue(user)
                            .addOnSuccessListener(unused -> Snackbar.make(findViewById(R.id.main), "Користувача оновлено", Snackbar.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Неможливо завантажити дані користувача.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Помилка завантаження даних користувача.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
