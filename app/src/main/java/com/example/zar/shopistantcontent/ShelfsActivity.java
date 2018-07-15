package com.example.zar.shopistantcontent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class ShelfsActivity extends AppCompatActivity {

    private static final String TAG = "ShelfsActivity";
    ListView productsList;
    private String aislePosition;
    private int quantityInt=0;
    private FloatingActionButton fab,fab1,fab2;
    private Query databaseRef;
    private Boolean isFabOpen = false;
    private Uri mPromoImg;
    private ImageView mImageView;
    private ProductsListAdapter adapter;
    private RelativeLayout emptyView;
    private ProgressBar bar;
    private static final int GALLERY_REQUEST = 1;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelfs);
        Intent intent=getIntent();
        aislePosition=intent.getStringExtra("AISLE");
        initComponent();
      /*  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
    }

    public void initComponent(){
        bar= (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.VISIBLE);
        emptyView= (RelativeLayout) findViewById(R.id.empty_view);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(this,R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(this,R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(this,R.anim.rotate_backward);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPromotion();
            }
        });
        productsList= (ListView) findViewById(R.id.list_item);
        databaseRef= FirebaseDatabase.getInstance().getReference().child("products").orderByChild("aislePosition").equalTo(aislePosition);
        adapter=new ProductsListAdapter(this,Product.class,R.layout.product_list_item,databaseRef);
        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product=adapter.getItem(position);
                String itemId=adapter.getRef(position).getKey();
                if (product!=null)
                {
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("PRODUCT",product);
                    bundle.putString("ID",itemId);
                    DetailsFragment fragment=new DetailsFragment();
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();
                }
            }
        });
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                bar.setVisibility(View.GONE);
                super.onChanged();
            }

            @Override
            public void onInvalidated() {

                bar.setVisibility(View.GONE);
                super.onInvalidated();
            }
        });
        ValueEventListener listener =new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    productsList.setAdapter(adapter);
                }
                else {
                    bar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                databaseRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseRef.addValueEventListener(listener);
    }

    public void addProduct(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setMessage("Add Product");
        LayoutInflater inflater=this.getLayoutInflater();
        View dialogView=inflater.inflate(R.layout.add_product_dialog,null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(false);
        final EditText name= (EditText) dialogView.findViewById(R.id.add_dialog_edt_product_name);
        final EditText quantity= (EditText) dialogView.findViewById(R.id.add_dialog_edt_product_qunatitiy);
        final EditText price= (EditText) dialogView.findViewById(R.id.add_dialog_edt_product_price);
        final EditText trans= (EditText) dialogView.findViewById(R.id.edt_p_trans);
        Button  increase= (Button) dialogView.findViewById(R.id.add_dialog_btn_increase);
        Button decrease= (Button) dialogView.findViewById(R.id.add_dialog_btn_decrease);
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityInt <= 10) {
                    quantityInt++;
                    quantity.setText(""+quantityInt);
                }
            }
        });
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityInt > 0) {
                    quantityInt--;
                    quantity.setText(""+quantityInt);
                }
            }
        });


        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!name.getText().toString().equals("") && !price.getText().toString().equals("")){
                    String translation=trans.getText().toString();
                    HashMap<String, Object> rating = new HashMap<String, Object>();
                    rating.put("rating", 0);
                    rating.put("ratedByNum", 0);
                    Product product = new Product(name.getText().toString(), quantityInt, aislePosition, price.getText().toString(), rating);
                    if (!translation.equals("")) {
                        product.setTranslation(translation);
                    }
                    DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child("products").push();
                    allRef.setValue(product);

                }

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

    public void addPromotion() {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("uploading...");
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        LayoutInflater inflater=this.getLayoutInflater();
        final AlertDialog dialog=alertDialog.create();
        View dialogView=inflater.inflate(R.layout.dialog_promotions,null);
        dialog.setView(dialogView);
        dialog.show();
        dialog.setCancelable(false);
        final EditText edtDetails= (EditText) dialogView.findViewById(R.id.edt_promo);
        Button btnCreate= (Button) dialogView.findViewById(R.id.btn_create);
        Button btnCancel= (Button) dialogView.findViewById(R.id.btn_cancel);
        mImageView= (ImageView) dialogView.findViewById(R.id.img_promo);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String details=edtDetails.getText().toString();
                if (details.equals("")) {
                    edtDetails.setError("Can't be empty");
                }
                else if (mPromoImg==null) {
                    Toast.makeText(ShelfsActivity.this,"Please select promo image.",Toast.LENGTH_SHORT).show();
                }

                else {
                    progressDialog.show();
                    StorageReference imagePath = FirebaseStorage.getInstance().getReference().child("promoImages").child(String.valueOf(mPromoImg.getLastPathSegment()));
                    UploadTask uploadTask = imagePath.putFile(mPromoImg);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String url=taskSnapshot.getDownloadUrl().toString();
                            Product product=new Product();
                            product.setImg(url);
                            product.setPromo("true");
                            product.setAislePosition(aislePosition);
                            product.setDetails(details);
                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("products");
                            ref.push().setValue(product);
                            dialog.dismiss();
                            progressDialog.dismiss();
                            Toast.makeText(ShelfsActivity.this,"Promotions Added",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            dialog.dismiss();
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });


    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST) {
            Log.e("tag", "called");
            if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
                mPromoImg = data.getData();
                Glide.with(this).load(mPromoImg)
                        .into(mImageView);
                CropImage.activity(mPromoImg)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mPromoImg = result.getUri();
                    Glide.with(this).load(mPromoImg)
                            .into(mImageView);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.e("IMAGE", "Error: " + error);
                }
            }
        }
        else {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (data!=null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;

        }
    }
}
