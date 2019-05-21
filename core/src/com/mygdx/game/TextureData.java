package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class TextureData {
    float sailBoatWidth;
    float sailBoatHeight;
    float markWidth;
    float markHeight;
    float committeeWidth;
    float committeeHeight;
    Texture s, m, c;

    public TextureData(float sailBoatWidth, float sailBoatHeight, float markWidth, float markHeight, float committeeWidth, float committeeHeight, Texture ss, Texture mm, Texture cc){
        this.sailBoatWidth = sailBoatWidth;
        this.sailBoatHeight = sailBoatHeight;
        this.markWidth = markWidth;
        this.markHeight=  markHeight;
        this.committeeWidth = committeeWidth;
        this.committeeHeight = committeeHeight;
        this.s= ss;
        this.m = mm;
        this.c = cc;
    }
    float sailBoatWidth(){
        return sailBoatWidth;
    }
    float sailBoatHeight(){
        return sailBoatHeight;
    }
    float markWidth(){
        return markWidth;
    }
    float markHeight(){
        return markHeight;
    }
    float committeeWidth(){
        return committeeWidth;
    }
    float committeeHeight(){
        return committeeHeight;
    }
    Texture sailBoatTexture(){
        return s;

    }
    Texture markTexture(){
        return m;

    }Texture committeeTexture(){
        return c;

    }


}
