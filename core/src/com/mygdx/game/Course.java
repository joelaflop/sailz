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
    private static int sailBoatCount = 0;
    private ArrayList<Mark> marks;
    private ArrayList<Committee> committees;
    private Sailboat localBoat, boat1;
    private float boatLength;
    TextureData t;
    private int tickcnt;

    public Course(int w, int c, TextureData t){

        windSpeed = w;
        currentSpeed = c;

        boats = new ArrayList<Sailboat>();
        marks = new ArrayList<Mark>();
        committees= new ArrayList<Committee>();

        localBoat = new Sailboat(sailBoatCount, "g1", t);
        sailBoatCount++;
        boats.add(localBoat);

        this.t = t;

        //boat1 = new Sailboat("o1");
        //boats.add(boat1);

        boatLength = t.sailBoatHeight;
        marks.add(new Mark(-6000, 0, "pin", boatLength));
        committees.add(new Committee(0, 0, "start", boatLength));

        marks.add(new Mark(-3000, 20000, "m1", boatLength));
        marks.add(new Mark(2400, 20000, "m2", boatLength));

        marks.add(new Mark(-3000, 5000, "m3", boatLength));
        marks.add(new Mark(2400, 5000, "m4", boatLength));

        tickcnt = 0;

    }

    public void updateCourse(boolean playing){

        if (playing) {
            for (Sailboat b : boats) {
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

    public void clearRudder(){ localBoat.clearRudder(); }

    public Sailboat localBoat() { return localBoat; }

    public  void update(String n, float x, float y, float angle, float sailTrim, float rudderAngle, int id, boolean star){
        System.out.println("attempting to update boats,");
        boolean found = false;
        for(Sailboat b : boats){
            System.out.println("compare: "+b.id() +" " + id);
            if(b.id() == id){
                System.out.println("boat update collision - good");
                b.update(n,  x,  y,  angle,  sailTrim,  rudderAngle,  id, star);
                found = true;
            }
        }
        if(!found){
            Sailboat s = new Sailboat(sailBoatCount, n, t);
            sailBoatCount++;
            s.update(n,x,y,angle,sailTrim,rudderAngle,id, star);
            boats.add(s);
            System.out.println("boat added, boats size:"+boats.size());
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapes){
        //System.out.println(boats);
        for (Mark m : marks) {
            shapes.circle(m.getX(), m.getY(), boatLength*2);
            batch.draw(t.markTexture(), m.getX() - t.markWidth()/2, m.getY() - t.markWidth()/2, t.markWidth(), t.markWidth());
        }
        for (Committee m : committees) {
            shapes.circle(m.getX(), m.getY(), boatLength * 2);
            batch.draw(t.committeeTexture(), m.getX() - t.committeeWidth()/2, m.getY() - t.committeeHeight()/2, t.committeeWidth(), t.committeeHeight());
            //m.drawOutline(shapes);
        }
        for (Sailboat b : boats) {
            if(b == null){
                System.out.println("NULL BOAT NULL BOAT NULL BOAT NULL BOAT NULL BOAT NULL BOAT");
            }
            batch.draw(t.sailBoatTexture(), b.getX(), b.getY(), t.sailBoatWidth() / 2, t.sailBoatHeight()/ 2, t.sailBoatWidth(), t.sailBoatHeight(), 1, 1, (float) b.getAngle(), 0, 0, (int) t.sailBoatWidth(), (int) t.sailBoatHeight(), false, false);
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
        //System.out.print("sending location:  ");
        try {
                WarpController.getInstance().sendGameUpdate(localBoat.getArray().toString());
        } catch (Exception e) {
            System.out.println("exception sending location (Course)");
        }
    }

    public void drawHUD(SpriteBatch batch, BitmapFont font){
        localBoat.hud(font, batch);
    }

}
