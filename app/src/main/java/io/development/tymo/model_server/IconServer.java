package io.development.tymo.model_server;

public class IconServer {

    private String url, category, name;
    private boolean selectable, colorful;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean getSelectable() {
        return selectable;
    }

    public void setColorful(boolean colorful) {
        this.colorful = colorful;
    }

    public boolean getColorful() {
        return colorful;
    }


}
