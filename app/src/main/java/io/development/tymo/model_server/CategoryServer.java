package io.development.tymo.model_server;

public class CategoryServer {

    private String name;
    private int iconsQty = 0, position = 0;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIconsQty(int iconsQty) {
        this.iconsQty = iconsQty;
    }

    public int getIconsQty() {
        return iconsQty;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }


}
