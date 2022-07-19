package com.project.camera;

import java.util.Comparator;

public class Item {

    private int id;
    private String name;
    private int quantity;
    private String location;
    private String type;
    public static boolean showQuantity;
    public static boolean showLocation;
    public static boolean showType;
    public static String typeStr;
    public static String quantityStr;
    public static String locationStr;

    public Item (int id, String name, int quantity, String location, String type) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.location = location;
        this.type = type;
    }

    public static void setVars (boolean quant, boolean loc, boolean type, String quantStr, String locStr, String tStr){
        showQuantity = quant;
        showLocation = loc;
        showType = type;
        typeStr = tStr;
        quantityStr = quantStr;
        locationStr = locStr;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString (){
        StringBuilder result = new StringBuilder(name);
        //int spaces;
        boolean showQuant = quantity != -1 && showQuantity;
        boolean showLoc = !location.equals("-1") && showLocation;
        boolean showTyp = !type.equals("-1") && showType;

        /*if (!type.equals("-1")){
            spaces = 20 - type.length() - typeStr.length();
        } else {
            spaces = 20 - name.length();
        }*/

        if (showTyp){
            result.append("\n").append(typeStr).append(type);
        }
        /*if (showQuant && spaces > 0){
            for (int i = 0; i < spaces; i++){
                result.append(" ");
            }
            result.append(quantityStr).append(quantity);
        } else */if (showQuant){
            result.append("\n").append(quantityStr).append(quantity);
        }
        if (showLoc) {
            result.append("\n").append(locationStr).append(location);
        }

        return result.toString();
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

    public static Comparator<Item> LocationSort = (p1, p2) -> {
        String loc1 = p1.getLocation().toUpperCase();
        String loc2 = p2.getLocation().toUpperCase();

        if (loc1.equals("-1") && loc2.equals("-1")){
            return NameSort.compare(p1, p2);
        } else if (loc1.equals("-1")){
            return 1;
        } else if (loc2.equals("-1")){
            return -1;
        } else {
            return loc1.compareTo(loc2);
        }
    };

    public static Comparator<Item> TypeSort = (p1, p2) -> {
        String type1 = p1.getType().toUpperCase();
        String type2 = p2.getType().toUpperCase();

        if (type1.equals("-1") && type2.equals("-1")){
            return NameSort.compare(p1, p2);
        } else if (type1.equals("-1")){
            return 1;
        } else if (type2.equals("-1")){
            return -1;
        } else {
            return type1.compareTo(type2);
        }
    };
}
