package io.development.tymo.models;

public class CubeModel {
    private int icon;
    private int colorUpper;
    private int colorLower;

    public CubeModel(int icon, int colorId1, int colorId2 ) {
        this.icon = icon;
        this.colorUpper = colorId1;
        this.colorLower = colorId2;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getColorLower() {
        return colorLower;
    }

    public void setColorUpper(int colorUpper) {
        this.colorUpper = colorUpper;
    }

    public int getColorUpper() {
        return colorUpper;
    }

    public void setColorLower(int colorLower) {
        this.colorLower = colorLower;
    }
}