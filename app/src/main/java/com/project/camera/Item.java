package com.project.camera;

import java.util.Comparator;

public class Item {

    int id;
    String name;

    public Item (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString (){
        return name;
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
