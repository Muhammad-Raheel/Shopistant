package com.example.zar.shopistant;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.zar.shopistant.Adapters.SearchListAdapter;
import com.example.zar.shopistant.Adapters.ShoppingListAdapter;
import com.example.zar.shopistant.Utils.Utils;
import com.example.zar.shopistant.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    //global variables used by current context

    private AutoCompleteTextView mAutoCompleteTextView;
    private static Button btnLocate;
    private static TextView txtBilled;
    private ListView mListView;
    private RecyclerView recyclerView;
    private static View mEmptyView;
    private Query mReference;
    private SearchListAdapter mSearchListAdapter;
    private static ArrayList<Product> mShoppingList;
    private static ArrayList<String> mKeys;
    private ShoppingListAdapter mShoppingListAdapter;
    private Utils mUtils;
    private ProgressBar progressBar;
    private static final String TAG = "MainActivity";

    /**
    * Method override from AppCompactActivity part of activity life cycle to run initialize the screen and run all code.
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();
        searchProduct();
    }


    /**
    * Initializations of all global variable used in the current context MainActivity
    * */

    public void initComponent() {
        Toolbar toolbar=(Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
       /* mListView= (ListView) findViewById(R.id.list_item);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
      */btnLocate= (Button) findViewById(R.id.btn_locate);
        txtBilled= (TextView) findViewById(R.id.txt_billed);
        recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        mAutoCompleteTextView= (AutoCompleteTextView) findViewById(R.id.auto);
        mEmptyView=findViewById(R.id.empty_view);
        mShoppingList=new ArrayList<>();
        mKeys=new ArrayList<>();
        mUtils=new Utils(this);
        progressBar= (ProgressBar) findViewById(R.id.progress_bar);
        //mListView.setEmptyView(mEmptyView);
        mShoppingList=mUtils.getArrayListFromSf();
        mKeys=mUtils.getStringArrayListFromSF();
        if (mKeys.size()!=0) {
            btnLocate.setVisibility(View.VISIBLE);
            txtBilled.setVisibility(View.VISIBLE);
            int billed=0;
            for (int i=0; i<mShoppingList.size(); i++) {

                billed+=Integer.parseInt(mShoppingList.get(i).getPrice())*mShoppingList.get(i).getQuantity();
            }
            txtBilled.setText("Total bill: Rs. "+billed);
        }
        else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        mShoppingListAdapter=new ShoppingListAdapter(this,mShoppingList,mKeys);
        recyclerView.setAdapter(mShoppingListAdapter);
        //mListView.setAdapter(mShoppingListAdapter);
    }


    /**
    * searching products from firebase and adding into a list.
    * */

    public void searchProduct() {

        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchString=mAutoCompleteTextView.getText().toString();
                if (!searchString.equals("") && mUtils.checkNetwork(MainActivity.this)) {
                    progressBar.setVisibility(View.VISIBLE);
                    mReference = FirebaseDatabase.getInstance().getReference().child("products").orderByChild("name").startAt(searchString).endAt(searchString + "\uf8ff");
                    mSearchListAdapter=new SearchListAdapter(MainActivity.this,Product.class,R.layout.single_list_item,mReference);
                    mAutoCompleteTextView.setAdapter(mSearchListAdapter);
                    mAutoCompleteTextView.showDropDown();
                }

                else if (!mUtils.checkNetwork(MainActivity.this)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,"No network found",Toast.LENGTH_SHORT).show();
                }

                else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Adding an item selected from drop down list of searched item to Shopping list Array

        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(View.INVISIBLE);
                String name=mAutoCompleteTextView.getText().toString();
                //Log.e(TAG,mSearchListAdapter.getRef(position).getKey());
                final Query reference=FirebaseDatabase.getInstance().getReference().child("products").orderByChild("name").equalTo(name);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot childSnapShot: dataSnapshot.getChildren()) {
                            String key=childSnapShot.getKey();
                            Product product=childSnapShot.getValue(Product.class);
                            product.setQuantity(1);
                            boolean flag=true;
                            for (int i=0; i<mKeys.size(); i++) {
                                if (mKeys!=null && mKeys.get(i).equals(key)) {
                                    flag=false;
                                    break;
                                }
                            }
                            if (flag) {
                                mKeys.add(key);
                                mShoppingList.add(product);
                                mShoppingListAdapter = new ShoppingListAdapter(MainActivity.this, mShoppingList, mKeys);
                               // mListView.setAdapter(mShoppingListAdapter);
                                recyclerView.setAdapter(mShoppingListAdapter);
                                mEmptyView.setVisibility(View.GONE);
                                mAutoCompleteTextView.setText("");
                                mUtils.addArrayListToSf(mShoppingList);
                                mUtils.addStringArrayListSF(mKeys);
                                if (mKeys.size() != 0) {
                                    btnLocate.setVisibility(View.VISIBLE);
                                    txtBilled.setVisibility(View.VISIBLE);
                                    int billed = 0;
                                    for (int i = 0; i < mShoppingList.size(); i++) {

                                        billed += Integer.parseInt(mShoppingList.get(i).getPrice())*mShoppingList.get(i).getQuantity();
                                    }
                                    txtBilled.setText("Total bill: Rs. " + billed);

                                }
                            }
                            else {
                                mAutoCompleteTextView.setText("");
                                Toast.makeText(MainActivity.this,"Already exist in list",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    /**
    * updating U.I by data changed in ShoppingListAdapter Class.
    * */

    public static void onDataChanged(Activity context) {
        Log.e(TAG,"called");
        Utils utils=new Utils(context);
        mKeys=utils.getStringArrayListFromSF();
        mShoppingList=utils.getArrayListFromSf();
        if (mKeys.size()!=0) {
            btnLocate.setVisibility(View.VISIBLE);
            txtBilled.setVisibility(View.VISIBLE);
            int billed=0;
            for (int i=0; i<mShoppingList.size(); i++) {

                billed+=Integer.parseInt(mShoppingList.get(i).getPrice())*mShoppingList.get(i).getQuantity();
            }
            txtBilled.setText("Total bill: Rs. "+billed);
        }
        else {
            mEmptyView.setVisibility(View.VISIBLE);
            txtBilled.setVisibility(View.INVISIBLE);
            btnLocate.setVisibility(View.INVISIBLE);
        }

    }
}
