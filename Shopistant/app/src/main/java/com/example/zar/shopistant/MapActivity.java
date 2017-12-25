package com.example.zar.shopistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.zar.shopistant.Utils.Utils;
import com.example.zar.shopistant.model.Point;
import com.example.zar.shopistant.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    // global variables used by current context

    private ArrayList<Product> mShoppingList ;
    private ArrayList<Product> mPromotionList;
    private Utils mUtils;
    private ImageView promoImage;
    private TextView txtDetails;
    private AlertDialog dialog;
    private Button btnCancel,btnTrack;
    boolean isLeftSide=false;
    boolean isRightSide=false;
    int mMinOfLeft=0;
    int mMinOfRight=0;
    ArrayList<Point> points;
    ImageView routeView;
    Path path;
    Paint paint;

    private static final String TAG = "MapActivity";

    /**
    * Method override from AppCompactActivity part of activity life cycle to run initialize the screen and run all code.
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initComponent();
        switchChases();
        generateRoute();
        Log.e(TAG,String.valueOf("Right side "+isRightSide+" Left side "+isLeftSide
        +" Min Right"+mMinOfRight+" Min Left"+mMinOfLeft));
    }

    /**
    * Initialization of all components used in current context
    * */

    public void initComponent() {
        mPromotionList=new ArrayList<>();

        final Query query= FirebaseDatabase.getInstance().getReference()
                .child("products").orderByChild("promo").equalTo("true");

        ValueEventListener listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    Log.e("tag",""+dataSnapshot.getChildrenCount());
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()) {
                        Product product=snapshot.getValue(Product.class);
                        mPromotionList.add(product);
                        //Log.e("tag", product.getDetails());
                    }
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(listener);

        new CountDownTimer(600000, 10000) {
            int i=0;
            public void onTick(long millisUntilFinished) {

                if (mPromotionList.size()>i) {
                    Product p=mPromotionList.get(i);
                    if (dialog!=null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showPromo(p);
                    i++;
                }
            }

            public void onFinish() {

            }
        }.start();

        points=new ArrayList<>();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        Log.e(TAG,String.valueOf(h+" "+w));
        mUtils=new Utils(this);
        mShoppingList=mUtils.getArrayListFromSf();
        routeView= (ImageView) findViewById(R.id.route_view);
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        path = new Path();
        float[] intervals = new float[]{50.0f, 20.0f};
        float phase = 0;
        DashPathEffect dashPathEffect =
                new DashPathEffect(intervals, phase);

        paint.setPathEffect(dashPathEffect);

    }

    /**
     * showing promotion dialog
     * */

    public void showPromo(final Product p) {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("uploading...");
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        LayoutInflater inflater=this.getLayoutInflater();
        dialog=alertDialog.create();
        View dialogView=inflater.inflate(R.layout.dialog_promotion,null);
        dialog.setView(dialogView);

        dialog.show();
        btnCancel= (Button) dialog.findViewById(R.id.btn_cancel);
        btnTrack= (Button) dialog.findViewById(R.id.btn_track);
        promoImage= (ImageView) dialog.findViewById(R.id.dialog_img);
        txtDetails= (TextView) dialog.findViewById(R.id.dialog_details);
        Glide.with(MapActivity.this).load(p.getImg())
                .into(promoImage);
        txtDetails.setText(p.getDetails());
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShoppingList.add(p);
                points.clear();
                isLeftSide=false;
                isRightSide=false;
                mMinOfLeft=0;
                mMinOfRight=0;
                path.reset();
                switchChases();
                generateRoute();
                dialog.dismiss();
                Log.e("tag","clicked");
            }
        });
    }

    /**
    * pin point positions configurations
    * */

    public void switchChases() {
        for (int i=0; i<mShoppingList.size(); i++){

            String aisle=mShoppingList.get(i).getAislePosition();
            switch (aisle){
                case "a01":
                    ImageView a01= (ImageView) findViewById(R.id.pin_a01);
                    ImageView a01_p= (ImageView) findViewById(R.id.pin_p_a01);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a01_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a01.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    mMinOfRight=1;
                    break;
                case "a02":
                    ImageView a02= (ImageView) findViewById(R.id.pin_a02);
                    a02.setVisibility(View.VISIBLE);
                    ImageView a02_p= (ImageView) findViewById(R.id.pin_p_a02);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a02_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a02.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    if (mMinOfRight==0 || mMinOfRight>2) {
                        mMinOfRight=2;
                    }
                    break;
                case "a03":
                    ImageView a03= (ImageView) findViewById(R.id.pin_a03);
                    isRightSide=true;
                    ImageView a03_p= (ImageView) findViewById(R.id.pin_p_a03);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a03_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a03.setVisibility(View.VISIBLE);
                    }
                    if (mMinOfRight==0 || mMinOfRight>3) {
                        mMinOfRight=3;
                    }
                    break;
                case "a04":
                    ImageView a04= (ImageView) findViewById(R.id.pin_a04);
                    ImageView a04_p= (ImageView) findViewById(R.id.pin_p_a04);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a04_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a04.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    if (mMinOfRight==0 || mMinOfRight>4) {
                        mMinOfRight=4;
                    }
                    break;
                case "a05":
                    ImageView a05= (ImageView) findViewById(R.id.pin_a05);
                    ImageView a05_p= (ImageView) findViewById(R.id.pin_p_a05);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a05_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a05.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    if (mMinOfRight==0 || mMinOfRight>5) {
                        mMinOfRight=5;
                    }
                    break;
                case "a06":
                    ImageView a06= (ImageView) findViewById(R.id.pin_a06);
                    ImageView a06_p= (ImageView) findViewById(R.id.pin_p_a06);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a06_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a06.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    if (mMinOfRight==0 || mMinOfRight>6) {
                        mMinOfRight=6;
                    }
                    break;
                case "a07":
                    ImageView a07= (ImageView) findViewById(R.id.pin_a07);
                    ImageView a07_p= (ImageView) findViewById(R.id.pin_p_a07);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a07_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a07.setVisibility(View.VISIBLE);
                    }
                    isRightSide=true;
                    if (mMinOfRight==0 || mMinOfRight>7) {
                        mMinOfRight=7;
                    }
                    break;
                case "a08":
                    ImageView a08= (ImageView) findViewById(R.id.pin_a08);
                    ImageView a08_p= (ImageView) findViewById(R.id.pin_p_a08);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a08_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a08.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    mMinOfLeft=8;
                    break;
                case "a09":
                    ImageView a09= (ImageView) findViewById(R.id.pin_a09);
                    ImageView a09_p= (ImageView) findViewById(R.id.pin_p_a09);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a09_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a09.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>9) {
                        mMinOfLeft=9;
                    }

                    break;
                case "a10":
                    ImageView a10= (ImageView) findViewById(R.id.pin_a10);
                    ImageView a10_p= (ImageView) findViewById(R.id.pin_p_a10);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a10_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a10.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>10) {
                        mMinOfLeft=10;
                    }
                    break;
                case "a11":
                    ImageView a11= (ImageView) findViewById(R.id.pin_a11);
                    ImageView a11_p= (ImageView) findViewById(R.id.pin_p_a11);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a11_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a11.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>11) {
                        mMinOfLeft=11;
                    }
                    break;
                case "a12":
                    ImageView a12= (ImageView) findViewById(R.id.pin_a12);
                    ImageView a12_p= (ImageView) findViewById(R.id.pin_p_a12);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a12_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a12.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>12) {
                        mMinOfLeft=12;
                    }
                    break;
                case "a13":
                    ImageView a13= (ImageView) findViewById(R.id.pin_a13);
                    ImageView a13_p= (ImageView) findViewById(R.id.pin_p_a13);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a13_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a13.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>13) {
                        mMinOfLeft=13;
                    }
                    break;
                case "a14":
                    ImageView a14= (ImageView) findViewById(R.id.pin_a14);
                    ImageView a14_p= (ImageView) findViewById(R.id.pin_p_a14);
                    if (mShoppingList.get(i).getImg()!=null) {
                        a14_p.setVisibility(View.VISIBLE);
                    }
                    else {
                        a14.setVisibility(View.VISIBLE);
                    }
                    isLeftSide=true;
                    if (mMinOfLeft==0 || mMinOfLeft>14) {
                        mMinOfLeft=14;
                    }

                    break;
                default:
                    Log.e(TAG,"default switch aisle");
                    break;
            }
        }
    }

    /**
    * generating shortest route possible for the pin points marked according to shopping list.
    * */

    public void generateRoute() {

        if (isLeftSide && isRightSide) {
            if ( mMinOfLeft>=11 && mMinOfRight>=4) {

                //not round path
                Log.e(TAG,"not round true");
                pathReturn();
            }
            else  {
                //round path
                Log.e(TAG,"all true");
                roundPath();
            }
        }

        else if (isRightSide && !isLeftSide) {
                //only right path
            Log.e(TAG,"only right true");
            if(mMinOfRight!=1) {
                onlyRightPath();
            }
            else {
                roundPath();
            }
        }

        else if (isLeftSide && !isRightSide) {
            //only left path
            Log.e(TAG,"only left true");
            onlyLeftPath();
        }
    }

    /**
    * drawing route on canvas by given points
    * */

    public void pointListOnRoute(ArrayList<Point> list) {
        for (int i=0; i<list.size(); i++) {
            if (i==0) {
                path.moveTo(convertPixelsToDp(list.get(i).getX(),this),convertPixelsToDp(list.get(i).getY(),this));
            }
            else {
                path.lineTo(convertPixelsToDp(list.get(i).getX(),this),convertPixelsToDp(list.get(i).getY(),this));
            }

        }

        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        routeView.setImageBitmap(bitmap);
        //routeView.animate().translationXBy(1000f).setDuration(2000);
        canvas.drawPath(path,paint);

    }

    /**
    * when there is pinpoints on both sides of aisle this path will be drown.
    * */

    public void pathReturn() {
        Point p1=new Point(50,585);
        points.add(p1);
        Point p2=new Point(235,585);
        points.add(p2);

        if (mMinOfRight==7) {
            Point p3=new Point(235,485);
            points.add(p3);
            Point p4=new Point(255,485);
            points.add(p4);
            Point p5=new Point(255,565);
            points.add(p5);
            Point p6=new Point(115,565);
            points.add(p6);
        }

        else if (mMinOfRight==6) {
            Point p3=new Point(235,425);
            points.add(p3);
            Point p4=new Point(255,425);
            points.add(p4);
            Point p5=new Point(255,565);
            points.add(p5);
            Point p6=new Point(115,565);
            points.add(p6);
        }

        else if (mMinOfRight==5) {
            Point p3=new Point(235,385);
            points.add(p3);
            Point p4=new Point(255,385);
            points.add(p4);
            Point p5=new Point(255,565);
            points.add(p5);
            Point p6=new Point(115,565);
            points.add(p6);
        }

        else if (mMinOfRight==4) {
            Point p3=new Point(235,305);
            points.add(p3);
            Point p4=new Point(255,305);
            points.add(p4);
            Point p5=new Point(255,565);
            points.add(p5);
            Point p6=new Point(115,565);
            points.add(p6);
        }

        if (mMinOfLeft==14) {
            Point p7=new Point(115,485);
            points.add(p7);
            Point p8=new Point(50,485);
            points.add(p8);
        }

        else if (mMinOfLeft==13) {
            Point p7=new Point(115,425);
            points.add(p7);
            Point p8=new Point(90,425);
            points.add(p8);
            Point p9=new Point(90,485);
            points.add(p9);
            Point p10=new Point(50,485);
            points.add(p10);
        }

        else if (mMinOfLeft==12) {
            Point p7=new Point(115,385);
            points.add(p7);
            Point p8=new Point(90,385);
            points.add(p8);
            Point p9=new Point(90,485);
            points.add(p9);
            Point p10=new Point(50,485);
            points.add(p10);
        }
        else if (mMinOfLeft==11) {
            Point p7=new Point(115,305);
            points.add(p7);
            Point p8=new Point(90,305);
            points.add(p8);
            Point p9=new Point(90,485);
            points.add(p9);
            Point p10=new Point(50,485);
            points.add(p10);
        }
        pointListOnRoute(points);
    }

    /**
    * This is the complete path followed around aisle.
    * */

    public void roundPath() {
        Point p1=new Point(50,585);
        points.add(p1);
        Point p2=new Point(250,585);
        points.add(p2);
        Point p3=new Point(250,55);
        points.add(p3);
        Point p4=new Point(90,55);
        points.add(p4);
        Point p5=new Point(90,510);
        points.add(p5);
        Point p6=new Point(50,510);
        points.add(p6);
        pointListOnRoute(points);

    }

    /**
    * when pinpoints only marked on right side this path will be follow.
    * */

    public void onlyRightPath() {
        Point p1=new Point(50,585);
        points.add(p1);
        Point p2=new Point(235,585);
        points.add(p2);
        switch (mMinOfRight) {
            case 7:
                rightPathPoints(485);
                break;
            case 6:
                rightPathPoints(425);
                break;
            case 5:
                rightPathPoints(385);
                break;
            case 4:
                rightPathPoints(305);
                break;
            case 3:
                rightPathPoints(265);
                break;
            case 2:
                rightPathPoints(185);
                break;
            default:
                Log.e(TAG,"default switch");
                break;
        }
    }

    /**
    * when pinpoints only marked on left side this path will be follow.
    * */

    public void onlyLeftPath() {

        switch (mMinOfLeft) {
            case 14:
                leftPathPoints(485);
                break;
            case 13:
                leftPathPoints(425);
                break;
            case 12:
                leftPathPoints(385);
                break;
            case 11:
                leftPathPoints(305);
                break;
            case 10:
                leftPathPoints(265);
                break;
            case 9:
                leftPathPoints(165);
                break;
            case 8:
                leftPathPoints(125);
                break;
            default:
                Log.e(TAG,"default switch");
                break;
        }
    }

    /**
    * The points which will be going to select for right side path only.
    * */

    public void rightPathPoints(int y) {
        Point p3=new Point(235,y);
        points.add(p3);
        Point p4=new Point(255,y);
        points.add(p4);
        Point p5=new Point(255,565);
        points.add(p5);
        Point p6=new Point(115,565);
        points.add(p6);
        Point p7=new Point(115,485);
        points.add(p7);
        Point p8=new Point(50,485);
        points.add(p8);
        pointListOnRoute(points);
    }

    /**
    * The points which will be going to select for left side path only.
    * */

    public void leftPathPoints(int y) {
        Point p1=new Point(50,585);
        points.add(p1);
        Point p2=new Point(115,585);
        points.add(p2);
        Point p3=new Point(115,y);
        points.add(p3);
        if (y==485) {
            Point p4=new Point(50,y);
            points.add(p4);
        }
        else {
            Point p4=new Point(90,y);
            points.add(p4);
            Point p5=new Point(90,485);
            points.add(p5);
            Point p6=new Point(50,485);
            points.add(p6);
        }
        pointListOnRoute(points);
    }

    /**
    * This method used to convert the pixels into dp (Display independent pixels). To make it responsive for kind of screen sizes.
    * */

    public float convertPixelsToDp(float px, Context context){
        Resources r = context.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        float dp = px * (metrics.densityDpi / 160f);
        Log.e("tag",String.valueOf(metrics.densityDpi));
        return dp;
    }
 }
