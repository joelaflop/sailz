package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public abstract class CourseItem {

    private float px;
    private float py;
    private String name;

    public CourseItem(float x, float y, String n) {
        this.px = x;
        this.py = y;
        this.name = n;
        //img = new Texture(Gdx.files.internal(n+".png"));
    }

    public void adjustPosition(float x, float y){
        px += x;
        py+= y;
    }

    public void setPosition(float x, float y){
        px = x;
        py= y;
    }


    public float getX() {
        return Math.round(px);
    }

    public float getY() {
        return Math.round(py);
    }

    /*
    public float getWidth() {
        return img.getWidth();
    }

    public float getHeight() {
        return img.getHeight();
    }
*/

    public String getName() {
        return name;
    }


}
