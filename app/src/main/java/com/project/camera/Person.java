package com.project.camera;

import java.util.Comparator;

public class Person {

    int id;
    String name;
    String phone;

    public Person(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String toString() {
        String formattedPhone = phone.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
        return name+"  "+formattedPhone;
    }

    public static Comparator<Person> NameSort = (p1, p2) -> {
        String Name1 = p1.getName().toUpperCase();
        String Name2 = p2.getName().toUpperCase();

        //ascending order
        return Name1.compareTo(Name2);

        //descending order
        //return Name2.compareTo(Name1);
    };

    public static Comparator<Person> IdSort = (p1, p2) -> {

        int id1 = p1.getId();
        int id2 = p2.getId();

        /*For ascending order*/
        //return id1-id2;

        /*For descending order*/
        return id2-id1;
    };

    public static Comparator<Person> PhoneSort = (p1, p2) -> {

        String phone1 = p1.getPhone();
        String phone2 = p2.getPhone();

        /*For ascending order*/
        return phone1.compareTo(phone2);

        /*For descending order*/
        //phone2-phone1;
    };
}
