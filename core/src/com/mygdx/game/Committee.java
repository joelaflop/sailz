package com.mygdx.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.awt.*;

public class Committee extends CourseItem {
    private float boatlength;

    public Committee(int x, int y, String n, float b) {
        super(x, y, n);
        boatlength = b;
    }

    public void drawOutline(ShapeRenderer shapes){
        Vector2 c = new Vector2(getX(), getY());
        Vector2 bowtip = new Vector2(c.x, c.y + 520);
        Vector2 nearBowR = new Vector2(c.x +110, c.y+350);
        Vector2 nearBowL = new Vector2(c.x - 110, c.y+350);
        Vector2 midRight = new Vector2(c.x+190, c.y);
        Vector2 midLeft = new Vector2(c.x-190, c.y);
        Vector2 starboardBeam = new Vector2(c.x+200, c.y-300);
        Vector2 portBeam = new Vector2(c.x-200, c.y-300);
        Vector2 rightCorner = new Vector2(c.x+160, c.y-510);
        Vector2 leftCorner = new Vector2(c.x-160, c.y-510);

        shapes.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        shapes.line(bowtip.x, bowtip.y, nearBowR.x, nearBowR.y);
        shapes.line(bowtip.x, bowtip.y, nearBowL.x, nearBowL.y);
        shapes.line(midRight.x, midRight.y, nearBowR.x, nearBowR.y);
        shapes.line(midLeft.x, midLeft.y, nearBowL.x, nearBowL.y);
        shapes.line(midRight.x, midRight.y, starboardBeam.x, starboardBeam.y);
        shapes.line(midLeft.x, midLeft.y, portBeam.x, portBeam.y);
        shapes.line(rightCorner.x, rightCorner.y, starboardBeam.x, starboardBeam.y);
        shapes.line(leftCorner.x, leftCorner.y, portBeam.x, portBeam.y);
        shapes.line(leftCorner.x, leftCorner.y, rightCorner.x, rightCorner.y);
        shapes.setColor(com.badlogic.gdx.graphics.Color.BLACK);
    }

}
