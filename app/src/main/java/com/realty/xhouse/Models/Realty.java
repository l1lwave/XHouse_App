package com.realty.xhouse.Models;

public class Realty {
    private String city;
    private String address;
    private int rooms;
    private double area;
    private int floor;
    private double price;
    private String about;
    private String image;
    private String id_manager;

    public Realty(String city, String address, int rooms, double area, int floor, double price, String about, String image, String id_manager) {
        this.city = city;
        this.address = address;
        this.rooms = rooms;
        this.area = area;
        this.floor = floor;
        this.price = price;
        this.about = about;
        this.image = image;
        this.id_manager = id_manager;
    }

    public Realty(){}


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId_manager() {
        return id_manager;
    }

    public void setId_manager(String id_manager) {
        this.id_manager = id_manager;
    }
}
