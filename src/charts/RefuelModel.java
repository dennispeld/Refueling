package charts;

class RefuelModel {
    // Fuel name
    private String name;
    String getName() {
        return name;
    }

    // Fuel price
    private double price;
    double getPrice() {
        return price;
    }
    void addPrice(double newPrice) {
        price = price + newPrice;
    }

    // Fuel amount
    private double amount;
    double getAmount() {
        return amount;
    }
    void addAmount(double newAmount) {
        amount = amount + newAmount;
    }

    // Refueling month
    private int month;
    int getMonth() {
        return month;
    }

    // Constructor of RefuelModel, that initializes the object
    RefuelModel(String name, double price, double amount, int month)
    {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.month = month;
    }
}
