package com.mygdx.game;

import AppWarp.WarpController;
import AppWarp.WarpListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.text.DecimalFormat;
import java.util.Arrays;

public class StartMultiScreen implements Screen, WarpListener {
    Game game;
    OrthographicCamera camera;
    SpriteBatch batcher;
    ShapeRenderer shapes;
    Rectangle backBounds;
    Vector3 touchPoint;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    FreeTypeFontGenerator generator;
    BitmapFont font, font24;

    float xOffset = 0;

    private final String[] tryingToConnect = {"Connecting","to AppWarp"};
    private final String[] waitForOtherUser = {"Waiting for","other user"};
    private final String[] errorInConnection = {"Error in","Connection", "Go Back"};

    private final String[] game_win = {"Congrats You Win!", "Enemy Defeated"};
    private final String[] game_loose = {"Oops You Loose!","Target Achieved","By Enemy"};
    private final String[] enemy_left = {"Congrats You Win!", "Enemy Left the Game"};

    private String[] msg = tryingToConnect;

    public StartMultiScreen (Game game) {
        this.game = game;

        camera = new OrthographicCamera(1600, 800);
        camera.position.set(800, 400, 0);
        backBounds = new Rectangle(0, 800-200, 200, 200);
        touchPoint = new Vector3();
        batcher = new SpriteBatch();
        shapes = new ShapeRenderer();
        xOffset = 80;
        WarpController.getInstance().setListener(this);

        font = new BitmapFont(Gdx.files.internal("bitmapfont/Amble-Regular-26.fnt"));
    }

    @Override
    public void render(float delta) {
        update();
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batcher.setProjectionMatrix(camera.combined);
        batcher.disableBlending();
        batcher.begin();
        printMessage(font, batcher);
        //batcher.draw(Assets.backgroundRegion, 0, 0, 320, 480);
        batcher.end();

        batcher.enableBlending();
        batcher.begin();

        shapes.setProjectionMatrix(camera.combined);
        shapes.setAutoShapeType(true);
        shapes.begin();
        shapes.rect(backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        shapes.end();


        float y = 230;
        for (int i = msg.length-1; i >= 0; i--) {
            //float width = Assets.font.getBounds(msg[i]).width;
            //Assets.font.draw(batcher, msg[i], 160-width/2, y);
            //y += Assets.font.getLineHeight();
        }

        //batcher.draw(Assets.arrow, 0, 0, 64, 64);
        batcher.end();

    }

    public void update(){
        if (Gdx.input.justTouched()) {
            camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                game.setScreen(new MainMenuScreen(game));
                WarpController.getInstance().handleLeave();
                return;
            }
        }
    }

    public void printMessage(BitmapFont font, SpriteBatch hud){
        //System.out.println(Arrays.toString(msg));
        font.draw(hud, Arrays.toString(msg), 100 , 200);
    }

    @Override
    public void onWaitingStarted(String message) {
        this.msg = waitForOtherUser;
        update();
    }

    @Override
    public void onError(String message) {
        this.msg = errorInConnection;
        update();
    }

    @Override
    public void onGameStarted(String message) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                game.setScreen(new CourseScreen(game, StartMultiScreen.this));
            }
        });
    }

    @Override
    public void onGameFinished(int code, boolean isRemote) {
        if(code==WarpController.GAME_WIN){
            this.msg = game_loose;
        }else if(code==WarpController.GAME_LOOSE){
            this.msg = game_win;
        }else if(code==WarpController.ENEMY_LEFT){
            this.msg = enemy_left;
        }
        //update();
        game.setScreen(this);
    }

    @Override
    public void onGameUpdateReceived(String message) {

    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

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
}
