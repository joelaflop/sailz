package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.*;

public class Sailboat extends CourseItem {

    private static Polar420 polar420;
    //private static SailPolar sailPolar;
    private static int sailBoatCount = 0;
    private float angle, omega, alpha, newAngle;
    private float rudderAngle;
    private float velocity, acceleration, newVelocity;
    private float sailTrim;
    private boolean tacking, jibing, slowing, spinning, skulling;
    private boolean starboard, luffing, properCourse;
    private int penalties, lastFoul, lastTrim, lastJibe;
    private Map<Sailboat, Integer> overlappedBoats;
    private Set<Sailboat> leewardBoats;
    private Map<String, Vector2> points;
    private Map<String, Vector2> windShadow;
    private final int id;
    private String name;

    public Sailboat(String n) {
        super(3000, -2500, n);
        this.name = n;
        id = sailBoatCount;
        sailBoatCount++;
        adjustPosition(1500*id,0);
        luffing = true;
        angle = 45;
        newAngle = 45;
        starboard = true;
        sailTrim = 45;
        alpha = 0.1f;
        polar420 = new Polar420();
        penalties = 0;
        lastFoul = 0;
        overlappedBoats = new HashMap<Sailboat, Integer>();
        leewardBoats = new HashSet<Sailboat>();

        points = new HashMap<String, Vector2>();
        windShadow = new HashMap<String, Vector2>();
        updatePoints();
    }

    public void update(String n, float x, float y, float angle, float sailTrim, float rudderAngle, int id) {

        //this.id = id;
        setPosition(x,y);
        luffing = true;
        this.angle = angle;
        this.sailTrim = sailTrim;
        //epenalties = 0;
        //lastFoul = 0;
        //overlappedBoats = new HashMap<Sailboat, Integer>();
        //leewardBoats = new HashSet<Sailboat>();

        points = new HashMap<String, Vector2>();
        windShadow = new HashMap<String, Vector2>();
        updatePoints();
    }

    public void move(double wind, double current) {
        double rad = Math.toRadians(getAngle());
        adjustPosition((float) (-velocity * Math.sin(rad)), (float) (-velocity * -Math.cos(rad)));
        updatePoints();
        //idealSailTrim();
        /*
        System.out.println("id:"+id +"  angle: " + getAngle() + "  velocity: " + velocity + "/" + newVelocity
                + "(" + velocity * Math.sin(rad) + "," + velocity * Math.cos(rad)
                + ")  acceleration: " + acceleration + "  starboard:" + starboard
                + "  properCourse:" + properCourse + "  luffing:" + luffing + "  slowing:" + slowing
                + "  rudder: "+rudderAngle);
                */
        if (starboard) {
            if (rudderAngle > 0) {
                headUp(rudderAngle / 90);
            } else if (rudderAngle < 0) {
                headDown(-rudderAngle / 90);
            }
        } else {
            if (rudderAngle > 0) {
                headUp(rudderAngle / 90);
            } else if (rudderAngle < 0) {
                headDown(-rudderAngle / 90);
            }
        }
        if (tacking) {
            if ((getAngle() > newAngle && omega > 0) || (getAngle() < newAngle && omega < 0)) {
                tacking = false;
                angle = newAngle;
                omega = 0;
            } else {
                double lastAngle = getAngle();
                adjustAngle(omega);
                if ((lastAngle < 60 && getAngle() > 300 || (lastAngle > 300 && getAngle() < 60))) {
                    starboard = !starboard;
                }
            }
            newVelocity = Math.max(3f, speedFromWind() / 2f);
        } else if (jibing) {
            if ((getAngle() > newAngle && omega > 0) || (getAngle() < newAngle && omega < 0)) {
                jibing = false;
                angle = newAngle;
                omega = 0;
            } else {
                double lastAngle = getAngle();
                adjustAngle(omega);
                if ((lastAngle < 180 && getAngle() > 180 || (lastAngle > 180 && getAngle() < 180))) {
                    starboard = !starboard;
                }
            }
            newVelocity = speedFromWind() * .6f;
        } else if (spinning) {
            newVelocity = Math.max(1.5f, speedFromWind() / 10f);
            //spinning = true;
            double lastAngle = getAngle();
            adjustAngle(omega);
            if ((lastAngle < 180 && getAngle() > 180 || (lastAngle > 180 && getAngle() < 180)) || ((lastAngle < 10 && getAngle() > 350) || (lastAngle > 350 && getAngle() < 10))) {
                starboard = !starboard;
            }
            // System.out.println(" angle: " + getAngle() + " " + newAngle);
            if (Math.abs(getAngle() - newAngle) < 3 && ((omega < 0 && getAngle() >= newAngle) || (omega > 0 && getAngle() <= newAngle))) {
                spinning = false;
                if (penalties > 0)
                    penalties--;
                omega = 0;
                angle = newAngle;
            }
        } else { //sailing = !luffing
            if (luffing) {
                if (getAngle() < 270 && getAngle() > 90) {
                    luffing = false;
                } else {
                    sailTrim = angleFromWind();
                    if (skulling) {
                        newVelocity = 2.5f;
                    } else {
                        newVelocity = 0;
                    }
                }
            } else {
                if (skulling) {
                        newVelocity = speedFromWind() / 3f;
                } else {
                        newVelocity = speedFromWind();
                }
            }
            if (properCourse) {
                getToProperCourse();
            }

        }
        if (!skulling) {
            //newVelocity -= (Math.abs(rudderAngle) / 30);
            if (newVelocity < 0) {
                //newVelocity = 0;
            }
        }
        accelerate();
        skulling = false;
    }

    public void accelerate() {
        if (!isManuvering() && !skulling) {
            if (velocity < newVelocity) {
                acceleration = speedFromWind() / 800f;
            } else if (velocity > newVelocity) {
                acceleration = (20 - speedFromWind()) / -700f;
            }
        } else {
            if (velocity < newVelocity) {
                acceleration = Math.max(0.01f, speedFromWind() / 1000f);
            } else if (velocity > newVelocity) {
                acceleration = Math.min(-0.01f, -speedFromWind() / 1000f);
            }
        }
        if (Math.abs(newVelocity - velocity) < Math.abs(acceleration)) {
            acceleration = 0;
            velocity = newVelocity;
            if (Math.abs(velocity) < Math.abs(acceleration)) {
                velocity = 0;
            }
        }
        velocity += acceleration;
    }

    public void headDown(float o) {
        if (!isManuvering()) {
            properCourse = false;
            if (starboard && getAngle() < 200) {
                adjustAngle(o);
                if (slowing && getAngle() > 200) {
                    adjustAngle(-o);
                }
            } else if (!starboard && getAngle() > 160) {
                adjustAngle(-o);
                if (slowing && getAngle() < 160) {
                    adjustAngle(o);
                }
            }
        } else {
            if (tacking && ((getAngle() > 20) && (getAngle() < 340))) {
                tacking = false;
                newAngle = angle;
                omega = 0;
            } else if (jibing) {
                jibing = false;
                newAngle = angle;
                omega = 0;
            }
        }
    }

    public void headUp(float o) {
        if (!isManuvering()) {
            properCourse = false;
            if (getAngle() >= 0 && getAngle() < 203 && starboard) {
                double oldAngle = getAngle();
                adjustAngle(-o);
                //System.out.println(oldAngle +"  " + getAngle() +  "starboardheadup");
                if (oldAngle < 5 && getAngle() > oldAngle) {
                    tack(false);
                }
                if (slowing && getAngle() < 160) {
                    adjustAngle(o);
                }
            } else if (getAngle() > 157 && getAngle() <= 360 && !starboard) {
                double oldAngle = getAngle();
                adjustAngle(o);
                //System.out.println(oldAngle +"  " + getAngle() +  "portheadup");
                if (oldAngle > 355 && getAngle() < oldAngle) {
                    tack(true);
                }
                if (slowing && getAngle() > 200) {
                    adjustAngle(-o);
                }
            }
        } else {
            if (tacking && ((getAngle() > 10) && (getAngle() < 350))) {
                tacking = false;
                newAngle = angle;
                omega = 0;
            } else if (jibing) {
                jibing = false;
                newAngle = angle;
                omega = 0;
            }
        }
    }

    public void getToProperCourse() {
        if (!luffing && !skulling && !slowing && !spinning) {
            if (getAngle() > 90 && getAngle() < 180) {
                newAngle = 160;
            } else if (getAngle() < 270 && getAngle() >= 180) {
                newAngle = 200;
            } else if (getAngle() >= 270 && getAngle() <= 360) {
                newAngle = 315;
            } else {
                newAngle = 45;
            }
            if (getAngle() < newAngle) {
                omega = 1;
            } else if (getAngle() > newAngle) {
                omega = -1;
            }
            adjustAngle(omega);
            if (Math.abs(getAngle() - newAngle) < omega) {
                properCourse = true;
                angle = newAngle;
                omega = 0;
            }
        }
    }

    public void adjustRudder(float a) {
        rudderAngle = a;
        properCourse = false;
        skulling = true;
        if (rudderAngle > 90) {
            rudderAngle = 90;
        } else if (rudderAngle < -90) {
            rudderAngle = -90;
        }
    }

    public void clearRudder() {
        rudderAngle = 0;
    }

    public void adjustAngle(double a) {
        angle += a * (Math.abs(velocity) + .15f) * .16f;
        skulling = true;
    }

    public float getAngle() {
        if (angle >= 0) {
            angle = (angle % 360);
        } else {
            angle = (angle % 360 + 360);
        }
        return angle;
    }

    public float angleFromWind() {
        if (starboard)
            return getAngle();
        else
            return 360 - getAngle();
    }

    public void trim() {
        luffing = false;
        sailTrim -= .3;
        if (sailTrim < 0) {
            sailTrim = 0;
        }
    }

    public void ease() {
        luffing = false;
        sailTrim += .3;
        if (sailTrim > 90) {
            sailTrim = 90;
        }
    }

    public float idealSailTrim() {
        return angleFromWind() / 2;
    }

    public float sailAngle() {
        float sailAng = angleFromWind() - sailTrim;
        if(sailAng > 180){
            return 360 - sailAng;
        }
        return sailAng;
    }

    private float speedFromWind() {
        //ang = Math.round(ang);
        float speed;
        speed = polar420.getSpeed(angleFromWind());
        speed *= 10;
        float idealTrim = idealSailTrim();
        float sailAngle = sailAngle();
        //System.out.print("angle:"+angleFromWind() +"  ideal:" + (idealTrim) + "  sailTrim:" + sailTrim + "  sailAng:" + sailAngle);
        if (sailAngle >=  idealTrim) { //overTrimmed
            speed *= (-Math.cos(((sailAngle+180-idealTrim*2)*Math.PI)/(180-idealTrim))+1)/2;
        } else if (sailAngle <  idealTrim && sailAngle >=0) { //underTrimmed
           speed *= (-Math.cos(sailAngle*Math.PI/idealTrim)+1)/2;
        } else{ //backwinding
            speed = polar420.getSpeed(180 - angleFromWind()) *5;
            //System.out.print("  preTrimSpeed: "+speed);
            idealTrim -= 90;
            speed *= -(-Math.cos(sailAngle*Math.PI/idealTrim)+1)/2;
        }
        return speed;
    }

    public void tack(boolean starb) {
        System.out.println("TACK");
        if (!isManuvering()) {
            if (starb) {
                newAngle = 45;
                omega = 1;
            } else {
                newAngle = 315;
                omega = -1;
            }
            newVelocity = 3;
            tacking = true;
            double lastAngle = getAngle();
            adjustAngle(omega);
            //System.out.println(lastAngle +"  "+getAngle());
            starboard = !starboard;

        }

    }

    public void jibe(int time) {
        if (time - lastJibe > 9) {
            lastJibe = time;
            if (!isManuvering()) {
                if ((getAngle() < 203 && getAngle() > 175 && starboard) || (getAngle() > 157 && getAngle() < 185 && !starboard)) {
                    newAngle = angle;
                    omega = 0;
                    starboard = !starboard;
                    jibing = false;
                } else {
                    newAngle = 360 - getAngle();
                    jibing = true;
                    //newVelocity = velocity * .85f;
                    if (getAngle() <= 180)
                        omega = 1;
                    else
                        omega = -1;
                }
            } else {
                if (tacking) {

                } else if (jibing) {
                    jibing = false;
                    newAngle = angle;
                    omega = 0;
                } else if (spinning) {

                }
            }
        }
    }

    public void spin(boolean t) {
        if (!isManuvering()) {
            newAngle = getAngle();
            if (starboard)
                if (t)
                    omega = -1;
                else
                    omega = 1;
            else {
                if (t)
                    omega = 1;
                else
                    omega = -1f;
            }
        }
        spinning = true;
        //adjustAngle(omega);
        // System.out.println(" angle: " + getAngle() + " " + newAngle);
    }

    public boolean isManuvering() {
        return tacking || jibing || spinning;
    }

    public boolean properCourse(){ return !luffing && properCourse; }

    public void luff(int time) {
        // System.out.println(getAngle() +" " +velocity +" "+velocity/2);
        if (time - lastTrim > 9) {
            lastTrim = time;
            if (getAngle() < 90 || getAngle() > 270) {
                luffing = true;
            }else {
                luffing = false;
            }
        }
    }

    public void penalize(int turns, int time) {
        if (time - lastFoul > 500) {
            penalties += turns;
            lastFoul = time;
        }
    }

    public void overlap(Sailboat b, int time) {
        if (Math.abs(getX() - b.getX()) > getHeight() * 3 || Math.abs(getX() - b.getX()) > getHeight() * 3)
            return;

        Vector2 rightCorner = getPoint("rightCorner");
        Vector2 leftCorner = getPoint("leftCorner");
        Vector2 bowtip = getPoint("bowtip");
        float sternSlope;
        if (rightCorner.x != leftCorner.x)
            sternSlope = (rightCorner.y - leftCorner.y) / (rightCorner.x - leftCorner.x);
        else
            sternSlope = Integer.MAX_VALUE;

        Vector2 brightCorner = b.getPoint("rightCorner");
        Vector2 bleftCorner = b.getPoint("leftCorner");
        Vector2 bbowtip = b.getPoint("bowtip");
        float bsternSlope;
        if (brightCorner.x != bleftCorner.x)
            bsternSlope = (brightCorner.y - bleftCorner.y) / (brightCorner.x - bleftCorner.x);
        else
            bsternSlope = Integer.MAX_VALUE;

        boolean bowInfrontBstern = (bsternSlope * (bowtip.x - brightCorner.x) + brightCorner.y
                - bowtip.y) >= 0;
        boolean bbowInfrontstern = (sternSlope * (bbowtip.x - rightCorner.x) + rightCorner.y
                - bbowtip.y) >= 0;
        boolean bbowDirection = (bsternSlope * (bbowtip.x - brightCorner.x) + brightCorner.y
                - bbowtip.y) >= 0;
        boolean bowDirection = (sternSlope * (bowtip.x - rightCorner.x) + rightCorner.y
                - bowtip.y) >= 0;

        //System.out.println(bowInfrontBstern+" " +bbowInfrontstern +" "+bbowDirection+" "+bowDirection);
        if (((bowInfrontBstern && bbowInfrontstern && bbowDirection && bowDirection)
                || (bowInfrontBstern && !bbowInfrontstern && bbowDirection && !bowDirection)
                || (!bowInfrontBstern && !bbowInfrontstern && !bbowDirection && !bowDirection)
                || (!bowInfrontBstern && bbowInfrontstern && !bbowDirection && bowDirection))
                && ((b.starboard && starboard) || (!b.starboard && !starboard)))
            overlappedBoats.put(b, time);
        else
            overlappedBoats.remove(b);
    }

    public void windward(Sailboat b) {
        if (Math.abs(getX() - b.getX()) > getHeight() * 3 || Math.abs(getX() - b.getX()) > getHeight() * 3)
            return;
        double a = Math.toDegrees(Math.atan2(((double) getY() - (double) b.getY()),
                ((double) getX() - (double) b.getX())));
        //System.out.println(b.id +"   "+a + "   " + getAngle() + "   ");
        if (starboard != b.starboard || !overlappedBoats.keySet().contains(b)) {
            leewardBoats.remove(b);
            //return false;
        } else if (starboard && b.starboard && (a + 90 >= getAngle() && a - 90 <= getAngle())) {
            leewardBoats.add(b);
            //return true;
        } else if (!starboard && !b.starboard && (a + 270 >= getAngle() && a + 90 <= getAngle())) {
            leewardBoats.add(b);
            //eturn true;
        } else {
            leewardBoats.remove(b);
            //return false;
        }
    }


    public boolean hitBoat(CourseItem b) {
        if (Math.abs(getX() - b.getX()) > getHeight() * 2 || Math.abs(getX() - b.getX()) > getHeight() * 2)
            return false;
        Vector2 bowtip = getPoint("bowtip");
        Vector2 nearBowR = getPoint("nearBowR");
        Vector2 nearBowL = getPoint("nearBowL");
        Vector2 midRight = getPoint("midRight");
        Vector2 midLeft = getPoint("midLeft");
        Vector2 starboardBeam = getPoint("starboardBeam");
        Vector2 portBeam = getPoint("portBeam");
        Vector2 rightCorner = getPoint("rightCorner");
        Vector2 leftCorner = getPoint("leftCorner");

        Vector2 bbowtip;
        Vector2 bnearBowR;
        Vector2 bnearBowL;
        Vector2 bmidRight;
        Vector2 bmidLeft;
        Vector2 bstarboardBeam;
        Vector2 bportBeam;
        Vector2 brightCorner;
        Vector2 bleftCorner;

        if (b instanceof Sailboat) {
            Sailboat bb = (Sailboat) b;
            bbowtip = bb.getPoint("bowtip");
            bnearBowR = bb.getPoint("nearBowR");
            bnearBowL = bb.getPoint("nearBowL");
            bmidRight = bb.getPoint("midRight");
            bmidLeft = bb.getPoint("midLeft");
            bstarboardBeam = bb.getPoint("starboardBeam");
            bportBeam = bb.getPoint("portBeam");
            brightCorner = bb.getPoint("rightCorner");
            bleftCorner = bb.getPoint("leftCorner");
        } else {
            Vector2 c = new Vector2(b.getX(), b.getY());
            bbowtip = new Vector2(c.x, c.y + 520);
            bnearBowR = new Vector2(c.x + 110, c.y + 350);
            bnearBowL = new Vector2(c.x - 110, c.y + 350);
            bmidRight = new Vector2(c.x + 190, c.y);
            bmidLeft = new Vector2(c.x - 190, c.y);
            bstarboardBeam = new Vector2(c.x + 200, c.y - 300);
            bportBeam = new Vector2(c.x - 200, c.y - 300);
            brightCorner = new Vector2(c.x + 160, c.y - 510);
            bleftCorner = new Vector2(c.x - 160, c.y - 510);
        }

        return intersectSegments(bowtip, nearBowR, bbowtip, bnearBowR)
                || intersectSegments(nearBowR, midRight, bnearBowR, bmidRight)
                || intersectSegments(midRight, starboardBeam, bmidRight, bstarboardBeam)
                || intersectSegments(starboardBeam, rightCorner, bstarboardBeam, brightCorner)
                || intersectSegments(rightCorner, leftCorner, bleftCorner, brightCorner)
                || intersectSegments(leftCorner, portBeam, bleftCorner, bportBeam)
                || intersectSegments(portBeam, midLeft, bportBeam, bmidLeft)
                || intersectSegments(midLeft, nearBowL, bmidLeft, bnearBowL)
                || intersectSegments(nearBowL, bowtip, bnearBowL, bbowtip) ||
                /*
                 * intersectSegments(bowtip, nearBowR, bnearBowR, bmidRight)||
                 * intersectSegments(nearBowR, midRight, bmidRight, bstarboardBeam)||
                 * intersectSegments(midRight, starboardBeam, bstarboardBeam, brightCorner)||
                 * intersectSegments(starboardBeam, rightCorner, bleftCorner, brightCorner)||
                 * intersectSegments(rightCorner, leftCorner,bleftCorner, bportBeam)||
                 * intersectSegments(leftCorner, portBeam, bportBeam, bmidLeft)||
                 * intersectSegments(portBeam, midLeft, bmidLeft, bnearBowL)||
                 * intersectSegments(midLeft, nearBowL, bnearBowL, bbowtip)||
                 * intersectSegments(nearBowL, bowtip, bbowtip, bnearBowR)||
                 */
                intersectSegments(bowtip, nearBowR, bmidRight, bstarboardBeam)
                || intersectSegments(nearBowR, midRight, bstarboardBeam, brightCorner)
                || intersectSegments(midRight, starboardBeam, bleftCorner, brightCorner)
                || intersectSegments(starboardBeam, rightCorner, bleftCorner, bportBeam)
                || intersectSegments(rightCorner, leftCorner, bportBeam, bmidLeft)
                || intersectSegments(leftCorner, portBeam, bmidLeft, bnearBowL)
                || intersectSegments(portBeam, midLeft, bnearBowL, bbowtip)
                || intersectSegments(midLeft, nearBowL, bbowtip, bnearBowR)
                || intersectSegments(nearBowL, bowtip, bnearBowR, bmidRight) ||

                intersectSegments(bowtip, nearBowR, bstarboardBeam, brightCorner)
                || intersectSegments(nearBowR, midRight, bleftCorner, brightCorner)
                || intersectSegments(midRight, starboardBeam, bleftCorner, bportBeam)
                || intersectSegments(starboardBeam, rightCorner, bportBeam, bmidLeft)
                || intersectSegments(rightCorner, leftCorner, bmidLeft, bnearBowL)
                || intersectSegments(leftCorner, portBeam, bnearBowL, bbowtip)
                || intersectSegments(portBeam, midLeft, bbowtip, bnearBowR)
                || intersectSegments(midLeft, nearBowL, bnearBowR, bmidRight)
                || intersectSegments(nearBowL, bowtip, bmidRight, bstarboardBeam) ||

                intersectSegments(bowtip, nearBowR, bleftCorner, brightCorner)
                || intersectSegments(nearBowR, midRight, bleftCorner, bportBeam)
                || intersectSegments(midRight, starboardBeam, bportBeam, bmidLeft)
                || intersectSegments(starboardBeam, rightCorner, bmidLeft, bnearBowL)
                || intersectSegments(rightCorner, leftCorner, bnearBowL, bbowtip)
                || intersectSegments(leftCorner, portBeam, bbowtip, bnearBowR)
                || intersectSegments(portBeam, midLeft, bnearBowR, bmidRight)
                || intersectSegments(midLeft, nearBowL, bmidRight, bstarboardBeam)
                || intersectSegments(nearBowL, bowtip, bstarboardBeam, brightCorner) ||

                intersectSegments(bowtip, nearBowR, bleftCorner, bportBeam)
                || intersectSegments(nearBowR, midRight, bportBeam, bmidLeft)
                || intersectSegments(midRight, starboardBeam, bmidLeft, bnearBowL)
                || intersectSegments(starboardBeam, rightCorner, bnearBowL, bbowtip)
                || intersectSegments(rightCorner, leftCorner, bbowtip, bnearBowR)
                || intersectSegments(leftCorner, portBeam, bnearBowR, bmidRight)
                || intersectSegments(portBeam, midLeft, bmidRight, bstarboardBeam)
                || intersectSegments(midLeft, nearBowL, bstarboardBeam, brightCorner)
                || intersectSegments(nearBowL, bowtip, bleftCorner, brightCorner) ||

                intersectSegments(bowtip, nearBowR, bportBeam, bmidLeft)
                || intersectSegments(nearBowR, midRight, bmidLeft, bnearBowL)
                || intersectSegments(midRight, starboardBeam, bnearBowL, bbowtip)
                || intersectSegments(starboardBeam, rightCorner, bbowtip, bnearBowR)
                || intersectSegments(rightCorner, leftCorner, bnearBowR, bmidRight)
                || intersectSegments(leftCorner, portBeam, bmidRight, bstarboardBeam)
                || intersectSegments(portBeam, midLeft, bstarboardBeam, brightCorner)
                || intersectSegments(midLeft, nearBowL, bleftCorner, brightCorner)
                || intersectSegments(nearBowL, bowtip, bleftCorner, bportBeam) ||

                intersectSegments(bowtip, nearBowR, bmidLeft, bnearBowL)
                || intersectSegments(nearBowR, midRight, bnearBowL, bbowtip)
                || intersectSegments(midRight, starboardBeam, bbowtip, bnearBowR)
                || intersectSegments(starboardBeam, rightCorner, bnearBowR, bmidRight)
                || intersectSegments(rightCorner, leftCorner, bmidRight, bstarboardBeam)
                || intersectSegments(leftCorner, portBeam, bstarboardBeam, brightCorner)
                || intersectSegments(portBeam, midLeft, bleftCorner, brightCorner)
                || intersectSegments(midLeft, nearBowL, bleftCorner, bportBeam)
                || intersectSegments(nearBowL, bowtip, bportBeam, bmidLeft);
        /*
         * intersectSegments(bowtip, nearBowR, bnearBowL, bbowtip)||
         * intersectSegments(nearBowR, midRight, bbowtip, bnearBowR)||
         * intersectSegments(midRight, starboardBeam, bnearBowR, bmidRight)||
         * intersectSegments(starboardBeam, rightCorner, bmidRight, bstarboardBeam)||
         * intersectSegments(rightCorner, leftCorner,bstarboardBeam, brightCorner)||
         * intersectSegments(leftCorner, portBeam, bleftCorner, brightCorner)||
         * intersectSegments(portBeam, midLeft,bleftCorner, bportBeam)||
         * intersectSegments(midLeft, nearBowL,bportBeam, bmidLeft)||
         * intersectSegments(nearBowL, bowtip, bmidLeft, bnearBowL)
         */
    }

    public boolean hitMark(Mark m) {
        if (Math.abs(getX() - m.getX()) > getHeight() || Math.abs(getX() - m.getX()) > getHeight())
            return false;

        Vector2 bowtip = getPoint("bowtip");
        Vector2 nearBowR = getPoint("nearBowR");
        Vector2 nearBowL = getPoint("nearBowL");
        Vector2 midRight = getPoint("midRight");
        Vector2 midLeft = getPoint("midLeft");
        Vector2 starboardBeam = getPoint("starboardBeam");
        Vector2 portBeam = getPoint("portBeam");
        Vector2 rightCorner = getPoint("rightCorner");
        Vector2 leftCorner = getPoint("leftCorner");

        return (Math.abs(m.getX() - bowtip.x) - m.getRadius() < 0 && Math.abs(m.getY() - bowtip.y) - m.getRadius() < 0)
                || (Math.abs(m.getX() - starboardBeam.x) - m.getRadius() < -4 && Math.abs(m.getY() - starboardBeam.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - portBeam.x) - m.getRadius() < -1 && Math.abs(m.getY() - portBeam.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - bowtip.x) - m.getRadius() < -1 && Math.abs(m.getY() - bowtip.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - nearBowR.x) - m.getRadius() < -1 && Math.abs(m.getY() - nearBowR.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - nearBowL.x) - m.getRadius() < -1 && Math.abs(m.getY() - nearBowL.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - midRight.x) - m.getRadius() < -1 && Math.abs(m.getY() - midRight.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - midLeft.x) - m.getRadius() < -1 && Math.abs(m.getY() - midLeft.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - rightCorner.x) - m.getRadius() < -1 && Math.abs(m.getY() - rightCorner.y) - m.getRadius() < -1)
                || (Math.abs(m.getX() - leftCorner.x) - m.getRadius() < -1 && Math.abs(m.getY() - leftCorner.y) - m.getRadius() < -1);
    }

    private boolean intersectSegments(Vector2 a, Vector2 b, Vector2 c, Vector2 d) {
        //System.out.println(a +" " + b +" " + c+" "+ d);
        float denominator = ((b.x - a.x) * (d.y - c.y)) - ((b.y - a.y) * (d.x - c.x));
        float numerator1 = ((a.y - c.y) * (d.x - c.x)) - ((a.x - c.x) * (d.y - c.y));
        float numerator2 = ((a.y - c.y) * (b.x - a.x)) - ((a.x - c.x) * (b.y - a.y));

        if (denominator == 0)
            return false;
        else {
            float r = numerator1 / denominator;
            float s = numerator2 / denominator;
            return (r >= 0 && r <= 1) && (s >= 0 && s <= 1);
        }
    }

    public boolean starboard() {
        return starboard;
    }

    public boolean isWindward(Sailboat b) {
        return (leewardBoats.contains(b));
    }

    public int penalties() {
        return penalties;
    }

    public int overlaps() {
        return overlappedBoats.size();
    }

    public Vector2 getPoint(String n) {
        return points.get(n);
    }

    public int id(){ return id; }


    public void updatePoints() {
        points.clear();
        points.put("bowtip", getRotatedPoint(0, -618));
        points.put("nearBowR", getRotatedPoint(100, -369));
        points.put("nearBowL", getRotatedPoint(-100, -369));
        points.put("midRight", getRotatedPoint(155, -127));
        points.put("midLeft", getRotatedPoint(-155, -127));
        points.put("starboardBeam", getRotatedPoint(180, 194));
        points.put("portBeam", getRotatedPoint(-180, 194));
        points.put("rightCorner", getRotatedPoint(136, 617));
        points.put("leftCorner", getRotatedPoint(-136, 617));
        windShadow.put("mastStep", getRotatedPoint(0, -400));
        Vector2 mastStep = windShadow.get("mastStep");
        if (starboard) {
            windShadow.put("idealEndBoom",
                    new Vector2(mastStep.x + (float) (1000) * (float) Math.sin(Math.toRadians(angleFromWind() / 2)),
                            mastStep.y - (1000) * (float) Math.cos(Math.toRadians(angleFromWind() / 2))));
            if(luffing) {
                windShadow.put("endBoom",
                        new Vector2(mastStep.x, mastStep.y-1000));
            } else{
                windShadow.put("endBoom",
                        new Vector2(mastStep.x - 1000 * (float) Math.cos(Math.toRadians(90 - sailTrim + angleFromWind())),
                                mastStep.y - 1000 * (float) Math.sin(Math.toRadians(90 - sailTrim + angleFromWind()))));
            }
        } else {
            windShadow.put("idealEndBoom",
                    new Vector2(mastStep.x - (float) (1000) * (float) Math.sin(Math.toRadians(angleFromWind() / 2)),
                            mastStep.y - (float) (1000) * (float) Math.cos(Math.toRadians(angleFromWind() / 2))));
            if(luffing) {
                windShadow.put("endBoom",
                        new Vector2(mastStep.x, mastStep.y-1000));
            } else {
                windShadow.put("endBoom",
                        new Vector2(mastStep.x + 1000 * (float) Math.cos(Math.toRadians(90 - sailTrim + angleFromWind())),
                                mastStep.y - 1000 * (float) Math.sin(Math.toRadians(90 - sailTrim + angleFromWind()))));
            }
        }

    }

    public Vector2 getRotatedPoint(float x, float y) {
        Vector2 v = new Vector2(
                (int) (Math.cos(Math.toRadians(180 + getAngle())) * (x)
                        - (y) * Math.sin(Math.toRadians(180 + getAngle())) + getX() + getWidth() / 2),
                (int) (Math.sin(Math.toRadians(180 + getAngle())) * (x)
                        + (y) * Math.cos(Math.toRadians(180 + getAngle()))) + getY() + getHeight() / 2);
        return v;
    }

    public void drawOutline(ShapeRenderer shapes) {
        Vector2 bowtip = getPoint("bowtip");
        Vector2 nearBowR = getPoint("nearBowR");
        Vector2 nearBowL = getPoint("nearBowL");
        Vector2 midRight = getPoint("midRight");
        Vector2 midLeft = getPoint("midLeft");
        Vector2 starboardBeam = getPoint("starboardBeam");
        Vector2 portBeam = getPoint("portBeam");
        Vector2 rightCorner = getPoint("rightCorner");
        Vector2 leftCorner = getPoint("leftCorner");
        shapes.line(bowtip.x, bowtip.y, nearBowR.x, nearBowR.y);
        shapes.line(bowtip.x, bowtip.y, nearBowL.x, nearBowL.y);
        shapes.line(midRight.x, midRight.y, nearBowR.x, nearBowR.y);
        shapes.line(midLeft.x, midLeft.y, nearBowL.x, nearBowL.y);
        shapes.line(midRight.x, midRight.y, starboardBeam.x, starboardBeam.y);
        shapes.line(midLeft.x, midLeft.y, portBeam.x, portBeam.y);
        shapes.line(rightCorner.x, rightCorner.y, starboardBeam.x, starboardBeam.y);
        shapes.line(leftCorner.x, leftCorner.y, portBeam.x, portBeam.y);
        shapes.line(leftCorner.x, leftCorner.y, rightCorner.x, rightCorner.y);
    }

    public void drawWindshadow(ShapeRenderer shapes) {
        if (!luffing) {
            Vector2 a = new Vector2(windShadow.get("mastStep").x, windShadow.get("mastStep").y - 3000);
            Vector2 b = new Vector2(windShadow.get("endBoom").x, windShadow.get("endBoom").y - 3000);
            Vector2 c, d;
            if (starboard) {
                c = new Vector2(windShadow.get("mastStep").x + 2000, windShadow.get("mastStep").y - 3000);
                d = new Vector2(windShadow.get("endBoom").x + 2000, windShadow.get("endBoom").y - 3000);

            } else {
                c = new Vector2(windShadow.get("mastStep").x - 2000, windShadow.get("mastStep").y - 3000);
                d = new Vector2(windShadow.get("endBoom").x - 2000, windShadow.get("endBoom").y - 3000);

            }
            windShadow.put("mastStepDown", a);
            windShadow.put("endBoomDown", b);
            windShadow.put("mastStepDownback", c);
            windShadow.put("endBoomDownback", d);

            shapes.setColor(com.badlogic.gdx.graphics.Color.GREEN);
            if (getAngle() < 80 || getAngle() > 280) {
                Vector2 l1, l2;
                if (starboard) {
                    l1 = new Vector2(windShadow.get("mastStep").x + 1500, windShadow.get("mastStep").y - 600);
                    l2 = new Vector2(windShadow.get("endBoom").x + 1000, windShadow.get("endBoom").y - 1000);
                } else {
                    l1 = new Vector2(windShadow.get("mastStep").x - 1500, windShadow.get("mastStep").y - 600);
                    l2 = new Vector2(windShadow.get("endBoom").x - 1000, windShadow.get("endBoom").y - 1000);
                }
                windShadow.put("leebowBackMast", l1);
                windShadow.put("leebowBackBoom", l2);
                shapes.setColor(com.badlogic.gdx.graphics.Color.GREEN);
                shapes.line(windShadow.get("mastStep"), l1);
                shapes.line(l1, l2);
                shapes.line(windShadow.get("endBoom"), l2);
            }

            shapes.setColor(com.badlogic.gdx.graphics.Color.GRAY);
            shapes.line(windShadow.get("mastStep"), a);
            shapes.line(a, b);
            shapes.line(b, d);
            shapes.line(c, d);
            shapes.line(c, windShadow.get("endBoom"));

            shapes.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        }
    }

    public void drawSail(ShapeRenderer shapes) {
        if(shapes == null){
            System.out.println("NULL SHAPES NULL SHAPES NULL SHAPES NULL SHAPES NULL SHAPES NULL SHAPES NULL SHAPES NULL SHAPES ");
        }
        Vector2 mastStep = windShadow.get("mastStep");
        if(mastStep == null){
            System.out.println("NULL mastStep NULL mastStep NULL mastStep NULL mastStep NULL mastStep NULL mastStep NULL mastStep NULL mastStep ");
        }
        Vector2 endBoom = windShadow.get("endBoom");
        if(endBoom == null){
            System.out.println("NULL endBoom NULL endBoom NULL endBoom NULL endBoom NULL endBoom NULL endBoom NULL endBoom NULL endBoom ");
        }
        Vector2 idealEndBoom = windShadow.get("idealEndBoom");
        shapes.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        shapes.line(mastStep, endBoom);
        shapes.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        shapes.line(mastStep, idealEndBoom);

        /*
        if (!luffing && !slowing) {
            if (starboard) {
                shapes.line(mastStep.x, mastStep.y,
                        mastStep.x + (float) (1000) * (float) Math.sin(Math.toRadians(180 - getAngle() / 2)),
                        mastStep.y + (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2)));
                windShadow.put("endBoom", new Vector2 (mastStep.x + (float) (1000) * (float) Math.sin(Math.toRadians
                (180 - getAngle() / 2)),
                        mastStep.y + (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2))));
            } else {
                shapes.line(mastStep.x, mastStep.y,
                        mastStep.x - (float) (1000) * (float) Math.sin(Math.toRadians(180 - getAngle() / 2)),
                        mastStep.y - (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2)));
                windShadow.put("endBoom", new Vector2 (mastStep.x - (float) (1000) * (float) Math.sin(Math.toRadians
                (180 - getAngle() / 2)),
                        mastStep.y - (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2))));
            }
        } else {
            if (getAngle() < 110 || getAngle() > 250) {
                shapes.line(mastStep.x, mastStep.y, mastStep.x,
                        (int) (mastStep.y - 1000));
                windShadow.put("endBoom", new Vector2 (mastStep.x,
                        (int) (mastStep.y - 1000)));

            } else if (getAngle() > 160 && getAngle() < 200) {
                shapes.line(mastStep.x, mastStep.y, mastStep.x,
                        (int) (mastStep.y + 1000));
                windShadow.put("endBoom", new Vector2 (mastStep.x,
                        (int) (mastStep.y + 1000)));
            } else {
                if (starboard()) {
                    shapes.line(mastStep.x, mastStep.y,
                            mastStep.x + (float) (1000) * (float) Math.sin(Math.toRadians(180 - getAngle() / 2)),
                            mastStep.y + (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2)));
                    windShadow.put("endBoom", new Vector2 (mastStep.x + (float) (1000) * (float) Math.sin(Math
                    .toRadians(180 - getAngle() / 2)),
                            mastStep.y + (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2))));

                } else {
                    shapes.line(mastStep.x, mastStep.y,
                            mastStep.x - (float) (1000) * (float) Math.sin(Math.toRadians(180 - getAngle() / 2)),
                            mastStep.y - (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2)));
                    windShadow.put("endBoom", new Vector2 (mastStep.x - (float) (1000) * (float) Math.sin(Math
                    .toRadians(180 - getAngle() / 2)),
                            mastStep.y - (float) (1000) * (float) Math.cos(Math.toRadians(180 - getAngle() / 2))));
                }
            }
        }
        */
    }

    public JSONObject getArray(){
        //counter++;
        try {
            JSONObject data = new JSONObject();
            data.put("n", name);
            data.put("id", id);
            data.put("x", getX());
            data.put("y", getY());
            data.put("angle", angle);
            data.put("sailTrim", sailTrim);
            data.put("rudderAngle", rudderAngle);
            data.put("width", 300);
            data.put("height", 300);
            //System.out.println(data);
            //if(counter%10==0){
            //    counter=0;
            //}
            return data;
        } catch (Exception e) {
            // exception in sendLocation
            return null;
        }

    }

    public void hud(BitmapFont font, SpriteBatch hud){
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(3);
        df.setMaximumIntegerDigits(3);
        df.setMinimumIntegerDigits(3);
        font.draw(hud, (
                  "angle: " + df.format(angleFromWind()) +
                  "  velocity: " + df.format(velocity) + "/" + df.format(newVelocity)
                + "  acceleration: " + df.format(acceleration)
                //+ "  luffing:" + luffing
                + "  rudder: "+rudderAngle), 50 , 50);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        else {
            if (this == o)
                return true;
            else {
                if (o instanceof Sailboat) {
                    if (this.id == ((Sailboat) o).id) {
                        return true;
                    } else {
                        return false;
                    }
                } else
                    return false;
            }
        }
    }

    @Override
    public int hashCode() {
        return id % 50;
    }

}
