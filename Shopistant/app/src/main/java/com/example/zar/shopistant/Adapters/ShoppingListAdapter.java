package com.example.zar.shopistant.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zar.shopistant.MainActivity;
import com.example.zar.shopistant.Utils.Utils;
import com.example.zar.shopistant.model.Product;
import com.example.zar.shopistant.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Zar on 4/29/2017.
 */

public class ShoppingListAdapter extends ArrayAdapter<Product> {
    private static final String TAG = "ShoppingListAdapter";
    private ArrayList<Product> shoppingList;
    private ArrayList<String> keys;
    private double mAvgRating;
    private double mRatingFromDb;
    private static final String DEVICE_ID= Build.SERIAL;

    Activity mContext;

    public ShoppingListAdapter(Activity context, ArrayList<Product> shoppingList, ArrayList<String> keys){
        super(context,0,shoppingList);
        this.shoppingList=shoppingList;
        this.keys=keys;
        this.mContext=context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.single_shopping_list_item, parent, false);
        }

        final Product item=getItem(position);
        Log.e(TAG,item.getPrice());
        ViewHolder h=new ViewHolder();
        h.txtName= (TextView) listItemView.findViewById(R.id.text_view_active_list_item_name);
        h.txtBill= (TextView) listItemView.findViewById(R.id.text_view_price);
        h.txtRate= (TextView) listItemView.findViewById(R.id.txt_rate_b);
        h.txtQuantity= (TextView) listItemView.findViewById(R.id.txt_quantity);
        h.btnInc= (Button) listItemView.findViewById(R.id.btn_inc);
        h.btnDec= (Button) listItemView.findViewById(R.id.btn_dec);
        h.rateLayout= (LinearLayout) listItemView.findViewById(R.id.lv_rate);
        h.deleteLayout= (LinearLayout) listItemView.findViewById(R.id.lv_delete);
        h.txtCategory= (TextView) listItemView.findViewById(R.id.txt_category);
        char[] array=item.getName().toCharArray();
        int count=0;

        for (int i=0; i<array.length; i++) {
            String current=""+array[i];
            if (current.equals(" ")) {
                count=i;
                break;
            }
        }
        HashMap<String,Object> rating=new HashMap<>();
        rating=item.getRating();

        if (rating.size()!=0) {
            String rates=rating.get("rating").toString();
            String ratedByNum=rating.get("ratedByNum").toString();
            if (rates.contains(".") && ratedByNum.contains(".")) {
                String ratesParse=""+round(Double.parseDouble(rates),1);
                h.txtRate.setText(ratesParse+"/"+(ratedByNum.substring(0,ratedByNum.indexOf("."))));
            }
            else if (rates.contains(".") && !ratedByNum.contains(".")) {
                String ratesParse=""+round(Double.parseDouble(rates),1);
                h.txtRate.setText(ratesParse+"/"+(ratedByNum));
            }
            else {
                h.txtRate.setText(rating.get("rating").toString()+"/"+(ratedByNum));
            }

        }
        h.txtQuantity.setText(""+item.getQuantity());
        h.txtCategory.setText(item.getName().substring(0,count));
        h.txtName.setText(item.getName().substring(count));
        h.txtBill.setText("Rs. "+(Integer.parseInt(item.getPrice()))*item.getQuantity());
        h.deleteLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext(),R.style.CustomTheme_Dialog)
                        .setTitle(getContext().getString(R.string.remove_item_option))
                        .setMessage(getContext().getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(position);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                AlertDialog alertDialog=alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        h.rateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateAndReview(keys.get(position),item,position);
            }
        });

        h.btnInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               inc(item,position);
            }
        });

        h.btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dec(item,position);
            }
        });

        return listItemView;
    }

    /**
     * removing an item from list
     * */

    private void removeItem(int id) {
        Utils mUtils=new Utils(mContext);
        shoppingList.remove(id);
        keys.remove(id);
        mUtils.addArrayListToSf(shoppingList);
        mUtils.addStringArrayListSF(keys);
        notifyDataSetChanged();
        ((MainActivity)mContext).onDataChanged(mContext);
    }

    /**
     * Quantity increment
     * */

    public void inc(Product product,int id) {
        if (product.getQuantity()>=1 && product.getQuantity()<6) {
            product.setQuantity(product.getQuantity()+1);
            Utils utils=new Utils(mContext);
            shoppingList.remove(id);
            shoppingList.add(id,product);
            utils.addArrayListToSf(shoppingList);
            notifyDataSetChanged();
            ((MainActivity)mContext).onDataChanged(mContext);
        }
    }

    /**
    * Quantity decrement
    * */

    public void dec(Product product,int id) {
        if (product.getQuantity()>1) {
            product.setQuantity(product.getQuantity()-1);
            Utils utils=new Utils(mContext);
            shoppingList.remove(id);
            shoppingList.add(id,product);
            utils.addArrayListToSf(shoppingList);
            notifyDataSetChanged();
            ((MainActivity)mContext).onDataChanged(mContext);
        }
    }

    /**
    * Holding U.I element for single item
    * */
    public static class ViewHolder {
        LinearLayout rateLayout,deleteLayout;
        Button btnInc,btnDec;
        TextView txtQuantity,txtRate,txtBill,txtName,txtCategory;
    }

    /**
    * Dialog for adding rating item selected from shopping list
    * */

    public void rateAndReview(final String productKey, final Product item, final int id){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
        final Utils mUtils=new Utils(mContext);
        LayoutInflater inflater=mContext.getLayoutInflater();
        View dialogView=inflater.inflate(R.layout.rate_review_dailogue,null);
        alertDialog.setView(dialogView);
        final RatingBar ratingBar= (RatingBar) dialogView.findViewById(R.id.rating_bar);
        final DatabaseReference referenceSet=mUtils.getDatabase().getReference().child("rating").child(productKey);
        final DatabaseReference reference=referenceSet.child("productRated");
        ValueEventListener listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    mRatingFromDb= dataSnapshot.getValue(Double.class);
                    reference.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } ;
        reference.addListenerForSingleValueEvent(listener);

        alertDialog.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final double rating=ratingBar.getRating();
                averageRate(rating);
                final DatabaseReference ref=mUtils.getDatabase().getReference().child("rating").child(productKey).child("ratedBy").child(DEVICE_ID);
                ValueEventListener listener=new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String,Object> ratedMap=(HashMap<String,Object>) dataSnapshot.getValue();
                        if (ratedMap!=null && ratedMap.size()!=0) {
                            Toast.makeText(mContext,"You already rated this product",Toast.LENGTH_LONG).show();
                        }
                        else {
                            HashMap<String,Object> ratingHash=new HashMap<>();
                            ratingHash.put("productRated",mAvgRating);
                            referenceSet.updateChildren(ratingHash);
                            productRatedBy(DEVICE_ID,productKey,mAvgRating,item,id);
                            Toast.makeText(mContext,"Product Rated",Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }
                        ref.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                ref.addListenerForSingleValueEvent(listener);

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        alertDialog.show();
    }

    /**
    * Average rate calculations
    * */

    public void averageRate(double rating) {
        if (mRatingFromDb!=0.0) {
            mAvgRating=(rating+mRatingFromDb);
            Log.e(TAG,"average Rate :"+mAvgRating);
        }

        else {
            mAvgRating=rating;
        }
    }

    /**
     *  Adding unique device id for the product rated by the user once only.
     * */

    public void productRatedBy(String deviceId, final String key, final double rate, final Product item, final int id) {
        final Utils mUtils=new Utils(mContext);
        final DatabaseReference ref=mUtils.getDatabase().getReference().child("rating").child(key).child("ratedBy").child(deviceId);
        ValueEventListener listener=new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {

                    HashMap<String,Object> rated=new HashMap<>();
                    rated.put("rated","true");
                    ref.setValue(rated);
                    final DatabaseReference reference=mUtils.getDatabase().getReference().child("products").child(key).child("rating");
                    final Query referenceCount=mUtils.getDatabase().getReference().child("rating").child(key).child("ratedBy");

                    ValueEventListener listener1=new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            long count=dataSnapshot.getChildrenCount();
                            HashMap<String,Object> rating=new HashMap<>();
                            //rate=mAvgRating/(count+1);
                            rating.put("rating",mAvgRating/(count));
                            rating.put("ratedByNum",count);
                            reference.updateChildren(rating);
                            item.setRating(rating);
                            shoppingList.remove(id);
                            shoppingList.add(id,item);
                            notifyDataSetChanged();
                            mUtils.addArrayListToSf(shoppingList);
                            referenceCount.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    referenceCount.addListenerForSingleValueEvent(listener1);

                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addListenerForSingleValueEvent(listener);
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
