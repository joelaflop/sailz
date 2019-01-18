package com.mygdx.game;

//import com.shephertz.app42.gaming.multiplayer.client.WarpClient;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class Sailx extends Game {
    FPSLogger fps;

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
        fps = new FPSLogger();
    }

    @Override
    public void render() {
        super.render();
        //fps.log();
    }

    @Override
    public void dispose() {
        super.dispose();
        getScreen().dispose();
    }

}