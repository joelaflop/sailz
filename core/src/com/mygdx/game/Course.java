package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import org.json.JSONArray;
import org.json.JSONObject;
import AppWarp.WarpController;

import java.util.ArrayList;

public class Course {

    private double windSpeed, currentSpeed;
    private static ArrayList<Sailboat> boats;
    private ArrayList<Mark> marks;
    private ArrayList<Committee> committees;
    private Sailboat localBoat, boat1;
    private float boatLength;
    int tickcnt;

    public Course(int w, int c){

        windSpeed = w;
        currentSpeed = c;

        boats = new ArrayList<Sailboat>();
        marks = new ArrayList<Mark>();
        committees= new ArrayList<Committee>();

        localBoat = new Sailboat("g1");
        boats.add(localBoat);

        //boat1 = new Sailboat("o1");
        //boats.add(boat1);

        boatLength = localBoat.getHeight();
        marks.add(new Mark(-6000, 0, "pin", boatLength));
        committees.add(new Committee(0, 0, "start", boatLength));

        marks.add(new Mark(-3000, 20000, "m1", boatLength));
        marks.add(new Mark(2400, 20000, "m2", boatLength));

        marks.add(new Mark(-3000, 5000, "m3", boatLength));
        marks.add(new Mark(2400, 5000, "m4", boatLength));

        boatLength = localBoat.getHeight();
        tickcnt = 0;

    }

    public void updateCourse(boolean playing){

        if (playing) {
            for (Sailboat b : boats) {
                if(b == null){
                    System.out.println("null boat b");
                }
                b.move(windSpeed, currentSpeed);
                for (Mark m : marks) {
                    if (b.hitMark(m)) {
                        b.penalize(1, tickcnt);
                    }
                }
                for (Committee c : committees) {
                    if (b.hitBoat(c)) {
                        b.penalize(1, tickcnt);
                    }
                }
                for (Sailboat bb : boats) {
                    System.out.println("null boat bb");
                    if (!b.equals(bb)) {
                        b.overlap(bb, tickcnt);
                        b.windward(bb);
                        if (b.hitBoat(bb)) {
                            assignPenalty(b, bb);
                        }
                    }
                }
            }
            tickcnt++;
        }

    }


    private void assignPenalty(Sailboat a, Sailboat b) {
        if ((a.isManuvering() && !b.isManuvering())) {
            a.penalize(2, tickcnt);
        } else if ((!a.isManuvering() && b.isManuvering())) {
            b.penalize(2, tickcnt);
        } else if (a.starboard() && !b.starboard()) {
            b.penalize(2, tickcnt);
        } else if (!a.starboard() && b.starboard()) {
            a.penalize(2, tickcnt);
        } else if (a.isWindward(b)) {
            a.penalize(2, tickcnt);
        } else if (b.isWindward(a)) {
            b.penalize(2, tickcnt);
        }

    }

    public void jibe(){
        localBoat.jibe(tickcnt);
    }

    public void luff(){
        localBoat.luff(tickcnt);
    }

    public void trim(){
        localBoat.trim();
    }

    public void ease(){
        localBoat.ease();
    }

    public void steer(float d){
        localBoat.adjustRudder(d);
    }

    public void clearRudder(){
        localBoat.clearRudder();
    }

    public Sailboat localBoat() { return localBoat; }

    public  void addBoat(Sailboat b){ boats.add(b); }

    public  void update(String n, float x, float y, float angle, float sailTrim, float rudderAngle, int id){
        for(Sailboat b : boats){
            if(b.id() == id){
                b.update(n,  x,  y,  angle,  sailTrim,  rudderAngle,  id);
            }
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapes){
        System.out.println(boats);
        for (Mark m : marks) {
            shapes.circle(m.getX(), m.getY(), boatLength * 2);
            batch.draw(m.img, m.getX() - m.getRadius(), m.getY() - m.getRadius(), m.getWidth(), m.getHeight());
        }
        for (Committee m : committees) {
            shapes.circle(m.getX(), m.getY(), boatLength * 2);
            batch.draw(m.img, m.getX() - m.getWidth()/2, m.getY() - m.getHeight()/2, m.getWidth(), m.getHeight());
            //m.drawOutline(shapes);
        }
        for (Sailboat b : boats) {
            if(b == null){
                System.out.println("NULL BOAT NULL BOAT NULL BOAT NULL BOAT NULL BOAT NULL BOAT");
            }
            batch.draw(b.img, b.getX(), b.getY(), b.getWidth() / 2, b.getHeight() / 2, b.getWidth(), b.getHeight(), 1, 1, (float) b.getAngle(), 0, 0, b.img.getWidth(), b.img.getHeight(), false, false);
            b.drawSail(shapes);
            b.drawWindshadow(shapes);
            //b.drawOutline(shapes);
            //shapes.setAutoShapeType(true);
            //shapes.set(ShapeRenderer.ShapeType.Filled);
            Vector2 portBow = b.getRotatedPoint(200, -500);
            if(b.penalties() > 0){
                shapes.setColor(com.badlogic.gdx.graphics.Color.RED);
                shapes.circle(portBow.x, portBow.y, 30);

            }
            if(b.overlaps() > 0){
                shapes.setColor(com.badlogic.gdx.graphics.Color.BLUE);
                shapes.circle(portBow.x, portBow.y+70, 30);
            }
            for(Sailboat bb: boats){
                if(!b.equals(bb)){
                    if(b.isWindward(bb)){
                        shapes.setColor(com.badlogic.gdx.graphics.Color.GREEN);
                        shapes.circle(portBow.x, portBow.y+140, 30);
                    }
                }
            }
            shapes.setColor(com.badlogic.gdx.graphics.Color.BLACK);
            //shapes.set(ShapeRenderer.ShapeType.Line);
            //shapes.setAutoShapeType(false);
        }

    }

    public void sendLocation(){
        //counter++;
        try {
                WarpController.getInstance().sendGameUpdate(localBoat.getArray().toString());
            //}
        } catch (Exception e) {
            // exception in sendLocation
        }
    }

    public void drawHUD(SpriteBatch batch, BitmapFont font){
        localBoat.hud(font, batch);
    }

}
