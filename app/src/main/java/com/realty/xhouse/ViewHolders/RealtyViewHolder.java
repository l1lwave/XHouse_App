package com.realty.xhouse.ViewHolders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.realty.xhouse.Interface.ItemClickListner;
import com.realty.xhouse.R;

public class RealtyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView txtrealtyAddress, txtrealtyCity, txtrealtyRooms, txtrealtyPrice;
    public ImageView imageView;
    public ItemClickListner listner;
    public Button removeButton;

    public RealtyViewHolder(View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.realty_image);
        txtrealtyAddress = itemView.findViewById(R.id.realty_address);
        txtrealtyCity = itemView.findViewById(R.id.realty_city);
        txtrealtyRooms = itemView.findViewById(R.id.realty_rooms);
        txtrealtyPrice = itemView.findViewById(R.id.realty_price);
        removeButton = itemView.findViewById(R.id.remove_real_button);
    }

    public void setItemClickListner(ItemClickListner listner) {
        this.listner = listner;
    }

    @Override
    public void onClick(View view) {
        listner.onClick(view, getAdapterPosition(), false);
    }
}
