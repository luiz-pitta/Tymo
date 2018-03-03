package io.development.tymo.models;

public class FeedCubeModel {
    private int icon;
    private int photo;
    private int colorUpper;
    private int colorLower;
    private String title;
    private String description;

    public FeedCubeModel(int icon, int photo, int colorLower, int colorUpper, String title, String description) {
        this.icon = icon;
        this.photo = photo;
        this.colorUpper = colorUpper;
        this.colorLower = colorLower;
        this.title = title;
        this.description = description;
    }

    public int getIcon() {
        return icon;
    }

    public int getPhoto() {
        return photo;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public int getColorLower() {
        return colorLower;
    }

    public void setColorLower(int colorLower) {
        this.colorLower = colorLower;
    }

    public int getColorUpper() {
        return colorUpper;
    }

    public void setColorUpper(int colorUpper) {
        this.colorUpper = colorUpper;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}