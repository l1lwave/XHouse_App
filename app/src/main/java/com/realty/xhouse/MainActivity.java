package com.realty.xhouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.realty.xhouse.Models.User;
import com.realty.xhouse.User.Home_Activity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogIn, btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogIn = findViewById(R.id.btnLogIn);
        btnRegister = findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterWindow();
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSingInWindow();
            }
        });
    }

    private void showSingInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Увійти");
        dialog.setMessage("Введіть дані для входу");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sing_in_window = inflater.inflate(R.layout.activity_sing_in_window, null);
        dialog.setView(sing_in_window);

        final EditText email = sing_in_window.findViewById(R.id.emailField);
        final EditText password = sing_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Відмінити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Увійти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть email", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().length() < 5){
                    Snackbar.make(findViewById(R.id.main), "Введіть пароль, більший за 5 символів", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                               startActivity(new Intent(MainActivity.this, Home_Activity.class));

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(R.id.main), "Помилка авторизації: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        dialog.show();
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зареєструватися");
        dialog.setMessage("Введіть дані для реєстрації");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.activity_register_window, null);
        dialog.setView(register_window);

        final EditText lastName = register_window.findViewById(R.id.lastNameField);
        final EditText firstName = register_window.findViewById(R.id.firstNameField);
        final EditText middleName = register_window.findViewById(R.id.middleNameField);
        final EditText email = register_window.findViewById(R.id.emailField);
        final EditText phone = register_window.findViewById(R.id.phoneField);
        final EditText password = register_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Відмінити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Надіслати", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Проверка на пустые поля
                if(TextUtils.isEmpty(lastName.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть прізвище", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(firstName.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть ім'я", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(middleName.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть по-батькові", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть email", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(phone.getText().toString())){
                    Snackbar.make(findViewById(R.id.main), "Введіть номер телефону", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().length() < 5){
                    Snackbar.make(findViewById(R.id.main), "Введіть пароль, більший за 5 символів", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d("Register", "Пользователь успешно создан");

                                User user = new User();
                                user.setLastName(lastName.getText().toString());
                                user.setFirstName(firstName.getText().toString());
                                user.setMiddleName(middleName.getText().toString());
                                user.setEmail(email.getText().toString());
                                user.setPhone(phone.getText().toString());
                                user.setPass(password.getText().toString());
                                user.setImage(null);

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(findViewById(R.id.main), "Користувач додан", Snackbar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Register", "Ошибка при добавлении пользователя в базу данных: " + e.getMessage());
                                                Snackbar.make(findViewById(R.id.main), "Ошибка базы данных: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Register", "Ошибка регистрации пользователя: " + e.getMessage());
                                Snackbar.make(findViewById(R.id.main), "Ошибка регистрации: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        dialog.show();
    }
}
