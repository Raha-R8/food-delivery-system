package Entities;


public class Item {
    private int id;
    private String name;
    private float price;
    private String ingredients;
    private String category;

    public Item(int id, String name, float price, String ingredients, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getCategory() {
        return category;
    }
}