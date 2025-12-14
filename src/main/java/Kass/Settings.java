package Kass;

public class Settings {
private String Font;
    private int SizeFontBtn = 18;
    private int SizeFontLabels = 24;
    private int SizeFontTables = 18;
    private String Color = "Black";
    private String Orientation;

    public String getFont() {
        return Font;
    }

    public void setFont(String font) {
        Font = font;
    }

    public int getSizeFontBtn() {
        return SizeFontBtn;
    }

    public void setSizeFontBtn(int sizeFontBtn) {
        SizeFontBtn = sizeFontBtn;
    }

    public int getSizeFontLabels() {
        return SizeFontLabels;
    }

    public void setSizeFontLabels(int sizeFontLabels) {
        SizeFontLabels = sizeFontLabels;
    }

    public int getSizeFontTables() {
        return SizeFontTables;
    }

    public void setSizeFontTables(int sizeFontTables) {
        SizeFontTables = sizeFontTables;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public String getOrientation() {
        return Orientation;
    }

    public void setOrientation(String orientation) {
        Orientation = orientation;
    }


}