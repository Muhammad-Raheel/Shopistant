package com.example.zar.shopistantcontent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Zar on 4/10/2017.
 */

public class DetailsFragment extends Fragment {
    private  int quantity=0;
    private  String itemId;
    private Product product;
    private EditText edtName,edtPrice,edtQun,edtDetails;
    private ImageView imageView;
    private Button btnInc,btnDec,btnUpdate,btnRemove;
    private String aislePosition;
    private boolean isPromo=false;
    private LinearLayout l;
    private Uri imgUri;
    private ProgressDialog dialog;
    private static final int GALLERY_REQUEST = 2;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_products,container,false);
        initComp(view);
        return view;
    }

    public void initComp(View view) {
        dialog=new ProgressDialog(getActivity());
        dialog.setMessage("Updating...");
        product= (Product) getArguments().getSerializable("PRODUCT");
        String name=product.getName();
        final String price=product.getPrice();
        quantity=product.getQuantity();
        itemId=getArguments().getString("ID");
        aislePosition=product.getAislePosition();
        l= (LinearLayout) view.findViewById(R.id.l_edt);
        imageView= (ImageView) view.findViewById(R.id.img_promo_edit);
        edtDetails= (EditText) view.findViewById(R.id.edt_promo_edit);
        edtName= (EditText) view.findViewById(R.id.fragment_product_name);
        edtPrice= (EditText) view.findViewById(R.id.fragment_product_price);
        edtQun= (EditText) view.findViewById(R.id.fragment_edt_product_qunatitiy);
        btnInc= (Button) view.findViewById(R.id.fragment_btn_increase);
        btnDec= (Button) view.findViewById(R.id.fragment_btn_decrease);
        btnUpdate= (Button) view.findViewById(R.id.fragment_btn_update);
        btnRemove= (Button) view.findViewById(R.id.fragment_btn_remove);
        if (product.getPrice()==null) {
            isPromo=true;
            imageView.setVisibility(View.VISIBLE);
            edtDetails.setVisibility(View.VISIBLE);
            edtName.setVisibility(View.GONE);
            edtPrice.setVisibility(View.GONE);
            l.setVisibility(View.GONE);
            Glide.with(this).load(product.getImg())
                    .into(imageView);
            edtDetails.setText(product.getDetails());

        }
        else {
            if (!name.equals("") && !price.equals("")){
                edtName.setText(name);
                edtPrice.setText(price);
                edtQun.setText(""+quantity);
            }

        }

        btnInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity<=10){
                    quantity++;
                    edtQun.setText(""+quantity);
                }
            }
        });
        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity>0){
                    quantity--;
                    edtQun.setText(""+quantity);
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if (isPromo) {
                    if (imgUri!=null) {
                        StorageReference deleteRef= FirebaseStorage.getInstance().getReferenceFromUrl(product.getImg());
                        deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               StorageReference updateRef=FirebaseStorage.getInstance().getReference().child("promoImages")
                                       .child(imgUri.getLastPathSegment());
                                UploadTask uploadTask=updateRef.putFile(imgUri);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String url=taskSnapshot.getDownloadUrl().toString();
                                        HashMap<String, Object> itemUpdated = new HashMap<String, Object>();
                                        itemUpdated.put("details", edtDetails.getText().toString());
                                        itemUpdated.put("img",url);
                                        DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child("products").child(itemId);
                                        allRef.updateChildren(itemUpdated);
                                        dialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(),"failed",Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(),"failed",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            }
                        });
                    }
                    else {
                        HashMap<String, Object> itemUpdated = new HashMap<String, Object>();
                        itemUpdated.put("details", edtDetails.getText().toString());
                        DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child("products").child(itemId);
                        allRef.updateChildren(itemUpdated);
                        dialog.dismiss();
                    }
                }
                else {
                    HashMap<String, Object> itemUpdated = new HashMap<String, Object>();
                    itemUpdated.put("name", edtName.getText().toString());
                    itemUpdated.put("price", edtPrice.getText().toString());
                    itemUpdated.put("quantity", quantity);
                    DatabaseReference allRef = FirebaseDatabase.getInstance().getReference().child("products").child(itemId);
                    allRef.updateChildren(itemUpdated);
                    dialog.dismiss();
                }
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (product.getImg()!=null) {
                StorageReference ref=FirebaseStorage.getInstance().getReferenceFromUrl(product.getImg());
                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference allRef=FirebaseDatabase.getInstance().getReference().child("products").child(itemId);
                        allRef.removeValue();
                    }
                });
                }
                else {
                    DatabaseReference allRef=FirebaseDatabase.getInstance().getReference().child("products").child(itemId);
                    allRef.removeValue();
                }
                edtName.setVisibility(View.GONE);
                edtPrice.setVisibility(View.GONE);
                edtQun.setVisibility(View.GONE);
                btnDec.setVisibility(View.GONE);
                btnInc.setVisibility(View.GONE);
                btnRemove.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.GONE);
                edtDetails.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                Toast.makeText(getActivity(),"Product Removed",Toast.LENGTH_SHORT).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                getActivity().startActivityForResult(intent, GALLERY_REQUEST);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("tag",""+GALLERY_REQUEST+""+RESULT_OK);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imgUri = data.getData();
            Glide.with(this).load(imageView)
                    .into(imageView);
            CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getActivity());
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgUri = result.getUri();
                Glide.with(this).load(imgUri)
                        .into(imageView);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("IMAGE", "Error: " + error);
            }
        }
    }

}
