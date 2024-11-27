package com.realty.xhouse.User;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.Query;
import com.realty.xhouse.Models.Realty;
import com.realty.xhouse.Models.User;
import com.realty.xhouse.R;
import com.realty.xhouse.ViewHolders.RealtyViewHolder;
import com.realty.xhouse.databinding.ActivityHomeBinding;
import com.realty.xhouse.Sing_in_window;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    DatabaseReference realtyRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    private EditText cityInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        realtyRef = FirebaseDatabase.getInstance().getReference().child("Realty");

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.client_materialToolbar);

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
               R.id.nav_add_realty, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.user_profile_name);
        CircleImageView profileImageView = headerView.findViewById(R.id.user_profile_image);

        loadUserData(userNameTextView, profileImageView);

        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        cityInput = findViewById(R.id.cityInput);
        cityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String city = cityInput.getText().toString().trim();
                filterRealtyByCity(city);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        updateRecyclerView(realtyRef);
    }

    private void showManagerDetailsDialog(Realty realty) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_manager_info);

        String managerId = realty.getId_manager();
        DatabaseReference managerRef = FirebaseDatabase.getInstance().getReference("Users").child(managerId);

        TextView managerNameText = dialog.findViewById(R.id.manager_full_name);
        TextView managerPhoneText = dialog.findViewById(R.id.manager_phone);

        managerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User manager = task.getResult().getValue(User.class);
                if (manager != null) {
                    String fullName = manager.getLastName() + " " + manager.getFirstName() + " " + manager.getMiddleName();
                    managerNameText.setText("Менеджер: " + fullName);
                    managerPhoneText.setText(manager.getPhone());
                } else {
                    managerNameText.setText("Менеджер: Не найден");
                    managerPhoneText.setText("Телефон: Не найден");
                }
            } else {
                managerNameText.setText("Менеджер: Ошибка загрузки");
                managerPhoneText.setText("Телефон: Ошибка загрузки");
            }
        });

        dialog.show();
    }

    private void showRealtyDetailsDialog(Realty realty) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.full_realty_layout);

        ImageView realtyImage = dialog.findViewById(R.id.realty_image);
        TextView cityText = dialog.findViewById(R.id.realty_city);
        TextView addressText = dialog.findViewById(R.id.realty_addres);
        TextView roomsText = dialog.findViewById(R.id.realty_rooms);
        TextView areaText = dialog.findViewById(R.id.realty_area);
        TextView floorText = dialog.findViewById(R.id.realty_floor);
        TextView aboutText = dialog.findViewById(R.id.realty_about);
        TextView priceText = dialog.findViewById(R.id.realty_price);

        Picasso.get().load(realty.getImage()).into(realtyImage);
        cityText.setText("Місто: " + realty.getCity());
        addressText.setText("Адруса: " + realty.getAddress());
        roomsText.setText("Кімнат: " + realty.getRooms());
        areaText.setText("Площа: " + realty.getArea() + " м²");
        floorText.setText("Поверх: " + realty.getFloor());
        aboutText.setText("Опис: " + realty.getAbout());
        priceText.setText(realty.getPrice() + " ₴");

        dialog.show();
    }

    private void filterRealtyByCity(String city) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Realty");

        if (!city.isEmpty()) {
            query = query.orderByChild("city").startAt(city).endAt(city + "\uf8ff");
        }

        updateRecyclerView(query);
    }

    private void updateRecyclerView(Query query) {
        FirebaseRecyclerOptions<Realty> options = new FirebaseRecyclerOptions.Builder<Realty>()
                .setQuery(query, Realty.class).build();

        FirebaseRecyclerAdapter<Realty, RealtyViewHolder> adapter = new FirebaseRecyclerAdapter<Realty, RealtyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RealtyViewHolder holder, int position, @NonNull Realty model) {
                holder.txtrealtyAddress.setText(model.getAddress());
                holder.txtrealtyCity.setText(model.getCity());
                holder.txtrealtyRooms.setText("Кімнат: " + model.getRooms());
                holder.txtrealtyPrice.setText(model.getPrice() + " ₴");
                Picasso.get().load(model.getImage()).into(holder.imageView);

                holder.imageView.setOnClickListener(v -> showRealtyDetailsDialog(model));

                Button managerButton = holder.itemView.findViewById(R.id.contact_manager_button);
                managerButton.setOnClickListener(v -> showManagerDetailsDialog(model));
            }

            @NonNull
            @Override
            public RealtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.realty_items_layout, parent, false);
                return new RealtyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    private void loadUserData(TextView userNameTextView, CircleImageView profileImageView) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    userNameTextView.setText(fullName);

                    String imageUrl = user.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImageView);
                    }
                }
            } else {
                Log.e("HomeActivity", "Помилка завантаження даних користувача: " + task.getException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_add_realty) {
            Intent addIntent = new Intent(Home_Activity.this, Add_realty_Activity.class);
            startActivity(addIntent);
        } else if (id == R.id.nav_show_my_realty) {
            Intent settingsIntent = new Intent(Home_Activity.this, MyReals_Activity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(Home_Activity.this, Settings_Activity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_logout) {
            Intent loginIntent = new Intent(Home_Activity.this, Sing_in_window.class);
            startActivity(loginIntent);
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}