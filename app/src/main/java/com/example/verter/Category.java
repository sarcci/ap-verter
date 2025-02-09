package com.example.verter;

public class Category {
    String name;
    double limit, spent;

    String Id;

    public Category() {
    }
    public Category(String name, double limit, double spent) {
        this.name = name;
        this.limit = limit;
        this.spent = spent;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", limit=" + limit +
                ", spent=" + spent +
                '}';
    }
}
