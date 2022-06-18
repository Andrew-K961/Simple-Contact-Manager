package com.project.camera;

import java.util.Comparator;

public class Item {

    private int id;
    private String name;
    private int quantity;
    private String location;
    public static boolean showQuantity;
    public static boolean showLocation;
    public static String quantityStr;
    public static String locationStr;

    public Item (int id, String name, int quantity, String location) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String toString (){
        boolean showQuantmp = quantity != -1 && showQuantity;
        boolean showLoctmp = !location.equals("-1") && showLocation;

        if (showQuantmp && !showLoctmp){
            return name + "\n" + quantityStr+ " " + quantity;
        } else if (showLoctmp && !showQuantmp) {
            return name + "\n" + locationStr+ " " + location;
        } else if (showLoctmp) {
            return name + "\n" + locationStr+ " " + location + "\n" + quantityStr + quantity;
        } else {
            return name;
        }
    }

    public static Comparator<Item> NameSort = (p1, p2) -> {
        String Name1 = p1.getName().toUpperCase();
        String Name2 = p2.getName().toUpperCase();

        //ascending order
        return Name1.compareTo(Name2);

        //descending order
        //return Name2.compareTo(Name1);
    };

    public static Comparator<Item> IdSort = (p1, p2) -> {

        int id1 = p1.getId();
        int id2 = p2.getId();

        /*For ascending order*/
        //return id1-id2;

        /*For descending order*/
        return id2-id1;
    };
}
