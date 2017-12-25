package com.example.zar.shopistantcontent;

import android.content.Intent;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Zar on 4/16/2017.
 */

public class Product implements Serializable  {

    private String name,aislePosition,price,details,img,promo;
    private int quantity;
    private HashMap<String,Object> rating;
    public Product(){}

    public Product(String name,int quantity,String aislePosition,String price,HashMap<String,Object> rating){
        this.name=name;
        this.quantity=quantity;
        this.aislePosition=aislePosition;
        this.price=price;
        this.rating=rating;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getAislePosition() {
        return aislePosition;
    }

    public String getPrice() {
        return price;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setAislePosition(String aislePosition) {
        this.aislePosition = aislePosition;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public HashMap<String, Object> getRating() {
        return rating;
    }
}

