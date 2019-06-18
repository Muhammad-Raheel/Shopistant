package com.example.zar.shopistantcontent;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;


/**
 * Created by Zar on 4/17/2017.
 */

public class ProductsListAdapter extends FirebaseListAdapter<Product> {

    public ProductsListAdapter(Activity activity, Class<Product> modelClass, int modelLayout, Query ref){
        super(activity,modelClass,modelLayout,ref);
        this.mActivity=activity;
    }
    @Override
    protected void populateView(View v, Product model, int position) {
        TextView name= (TextView) v.findViewById(R.id.txt_product_name);
        TextView ailse= (TextView) v.findViewById(R.id.txt_aisle_num);
        TextView price= (TextView) v.findViewById(R.id.txt_aisle_price);
        TextView quantity= (TextView) v.findViewById(R.id.txt_aisle_qunatity);
        ImageView imageView=(ImageView) v.findViewById(R.id.img_item);
        if (model.getPrice()==null || model.getPrice().equals("")) {
            imageView.setVisibility(View.VISIBLE);
            price.setVisibility(View.GONE);
            quantity.setVisibility(View.GONE);
            name.setText(model.getDetails());
            Glide.with(mActivity).load(model.getImg())
                    .into(imageView);
            ailse.setText(" "+model.getAislePosition());
        }
        else {
            name.setText(model.getName());
            ailse.setText(" "+model.getAislePosition());
            price.setText("Price : "+model.getPrice());
            quantity.setText("Quantity : "+model.getQuantity());
        }

    }
}
