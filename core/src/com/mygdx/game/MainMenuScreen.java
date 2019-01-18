package com.mygdx.game;

import AppWarp.WarpController;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

public class MainMenuScreen implements Screen {

    Game game;
    OrthographicCamera camera;
    SpriteBatch batch;
    ShapeRenderer shapes;
    Vector3 touchPoint;
    Rectangle playBounds;
    Rectangle helpBounds;
    Rectangle settingsBounds;
    Rectangle multiplayerBounds;

    public MainMenuScreen(Game game){
        this.game = game;
        camera = new OrthographicCamera(1600, 800);
        camera.position.set(800, 400, 0);
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        touchPoint = new Vector3();
        playBounds = new Rectangle(0,0, 400,400);
        settingsBounds = new Rectangle(400,0, 400,400);
        helpBounds = new Rectangle(0,800, 0,400);
        multiplayerBounds = new Rectangle(1200,0, 400,400);
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isTouched()){
            camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (multiplayerBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                WarpController.getInstance().startApp(getRandomHexString(10));
                game.setScreen(new StartMultiScreen(game));
                return;
            }
            if (playBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                game.setScreen(new CourseScreen(game, null));
                return;
            }
            if (settingsBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                //game.setScreen(new HighscoresScreen(game));
                return;
            }
            if (helpBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                //game.setScreen(new HelpScreen(game));
                return;
            }

        }
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        shapes.setAutoShapeType(true);
        shapes.begin();
        shapes.setColor(0,0,0,1);
        shapes.rect(multiplayerBounds.x, multiplayerBounds.y, multiplayerBounds.width, multiplayerBounds.height);
        shapes.rect(playBounds.x, playBounds.y, playBounds.width, playBounds.height);
        shapes.end();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, numchars);
    }
}
