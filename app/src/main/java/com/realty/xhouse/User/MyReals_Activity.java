package com.realty.xhouse.User;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.realty.xhouse.Models.Realty;
import com.realty.xhouse.R;
import com.realty.xhouse.ViewHolders.RealtyViewHolder;
import com.realty.xhouse.databinding.ActivityHomeBinding;
import com.squareup.picasso.Picasso;

public class MyReals_Activity extends AppCompatActivity {

    DatabaseReference realtyRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseAuth mAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_reals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_my_reals), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        realtyRef = FirebaseDatabase.getInstance().getReference().child("Realty");

        recyclerView = findViewById(R.id.added_reals_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyReals_Activity.this, Home_Activity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUserId != null) {
            Query userRealsQuery = realtyRef.orderByChild("id_manager").equalTo(currentUserId);
            updateRecyclerView(userRealsQuery);
        }
    }

    private void updateRecyclerView(Query query) {
        FirebaseRecyclerOptions<Realty> options = new FirebaseRecyclerOptions.Builder<Realty>()
                .setQuery(query, Realty.class).build();

        FirebaseRecyclerAdapter<Realty, RealtyViewHolder> adapter = new FirebaseRecyclerAdapter<Realty, RealtyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RealtyViewHolder holder, int position, @NonNull Realty model) {
                holder.txtrealtyAddress.setText(model.getAddress());
                holder.txtrealtyCity.setText(model.getCity());
                holder.txtrealtyPrice.setText(model.getPrice() + " ₴");

                String realtyKey = getRef(position).getKey();

                holder.removeButton.setOnClickListener(view -> {
                    if (realtyKey != null) {
                        realtyRef.child(realtyKey).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Remove Realty", "Realty removed successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Remove Realty", "Failed to remove realty", e);
                                });
                    }
                });
            }

            @NonNull
            @Override
            public RealtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_real_layout, parent, false);
                return new RealtyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

}