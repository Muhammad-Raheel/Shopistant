package com.example.zar.shopistant.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.zar.shopistant.R;

/**
 * Created by Amir on 12/31/2017.
 */

public class ItemsViewHolder extends RecyclerView.ViewHolder {

    LinearLayout rateLayout,deleteLayout,transLayout;
    Button btnInc,btnDec;
    TextView txtQuantity,txtRate,txtBill,txtName,txtCategory;

    public ItemsViewHolder(View listItemView) {
        super(listItemView);
        txtName= (TextView) listItemView.findViewById(R.id.text_view_active_list_item_name);
        txtBill= (TextView) listItemView.findViewById(R.id.text_view_price);
        txtRate= (TextView) listItemView.findViewById(R.id.txt_rate_b);
        txtQuantity= (TextView) listItemView.findViewById(R.id.txt_quantity);
        btnInc= (Button) listItemView.findViewById(R.id.btn_inc);
        btnDec= (Button) listItemView.findViewById(R.id.btn_dec);
        rateLayout= (LinearLayout) listItemView.findViewById(R.id.lv_rate);
        deleteLayout= (LinearLayout) listItemView.findViewById(R.id.lv_delete);
        txtCategory= (TextView) listItemView.findViewById(R.id.txt_category);
        transLayout= (LinearLayout) listItemView.findViewById(R.id.lv_trans);
    }
}
