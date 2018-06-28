package io.guaong.gesturemusic.model;

public class Color {

    private int bgColor;
    private int waterColor;

    public Color(int bgColor, int waterColor){
        this.bgColor = bgColor;
        this.waterColor = waterColor;
    }


    public int getBgColor() {
        return bgColor;
    }

    public int getWaterColor() {
        return waterColor;
    }
}
