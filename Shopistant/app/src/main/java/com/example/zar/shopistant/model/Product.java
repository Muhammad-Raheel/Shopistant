package com.example.zar.shopistant.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Zar on 4/21/2017.
 */

public class Product implements Serializable {
    private String name,aislePosition,price,details,img;
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

    public void setDetails(String details) {
        this.details = details;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setRating(HashMap<String, Object> rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public String getDetails() {
        return details;
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
    public HashMap<String, Object> getRating() {
        return rating;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
