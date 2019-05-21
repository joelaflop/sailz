package com.mygdx.game;

import AppWarp.WarpController;
import AppWarp.WarpListener;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.json.JSONObject;

public class CourseScreen implements Screen, WarpListener {

    Game game;
    OrthographicCamera camera, hudcam;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    FreeTypeFontGenerator generator;
    BitmapFont font, font24;
    //private Texture img;
    public boolean playing = true;
    Course course;
    private StartMultiScreen prevScreen;
    Rectangle backBounds;
    Vector3 touchPoint;
    boolean firstTime;
    TextureData t;


    public CourseScreen(Game game, StartMultiScreen prevScreen){
        firstTime = true;

        this.game = game;
        this.prevScreen = prevScreen;

        Texture sailboatTexture = new Texture(Gdx.files.internal("g1.png"));
        Texture markTexture = new Texture(Gdx.files.internal("m1.png"));
        Texture committeeTexture = new Texture(Gdx.files.internal("start.png"));

        t = new TextureData(sailboatTexture.getWidth(), sailboatTexture.getHeight(), markTexture.getWidth(), markTexture.getHeight(), committeeTexture.getWidth(), committeeTexture.getHeight(), sailboatTexture, markTexture, committeeTexture);

        course = new Course(5, 0, t);
        camera = new OrthographicCamera(16000, 8000);
        hudcam = new OrthographicCamera(1600, 800);
        backBounds = new Rectangle(0, 760, 40, 40);
        touchPoint = new Vector3();


        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        font = new BitmapFont(Gdx.files.internal("bitmapfont/Amble-Regular-26.fnt"));
        generator = new FreeTypeFontGenerator(Gdx.files.internal("truetypefont/Amble-Light.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        //parameter.borderWidth = 1;
        parameter.color = Color.BLACK;
        //parameter.shadowOffsetX = 3;
        //parameter.shadowOffsetY = 3;
        //parameter.shadowColor = new Color(0, 0.5f, 0, 0.75f);
        font24 = generator.generateFont(parameter); // font size 24 pixels
        generator.dispose();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font24;
        Label label2 = new Label("True Type Font (.ttf) - Gdx FreeType", labelStyle);
        label2.setSize(100, 100);
        label2.setPosition(50, 50);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16000, 8000);
        hudcam = new OrthographicCamera();
        hudcam.setToOrtho(false, 1600, 800);

        Gdx.input.setInputProcessor(new InputAdapter() {
            private Vector2 firstTouch = new Vector2();

            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                firstTouch = new Vector2(x, y);
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                course.steer(-((screenY - firstTouch.y)) / 2);
                return false;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                course.clearRudder();
                return false;
            }
        });
        WarpController.getInstance().setListener(this);
        //WarpClient.initialize("1e1541bdd6353bcbaa6786382bbf35c41376ffa43077dc4e0a95f94ae4f92a0d",
        //"d7a29066ee8b392aa5a6eaba080a7f19cb3383fdd34d69037980e9b0158c3dfc");


    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //System.out.println(camera.position);
        camera.position.set(course.localBoat().getX(), course.localBoat().getY(), 0);
        camera.update();
        hudcam.update();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        if (Gdx.input.justTouched()) {
            hudcam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                //Assets.playSound(Assets.clickSound);
                game.setScreen(new MainMenuScreen(game));
                WarpController.getInstance().handleLeave();
                return;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //course.localBoat().headDown(1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //course.localBoat().headUp(1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            course.jibe();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            course.localBoat().properCourse();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            course.luff();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            course.ease();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            course.trim();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) {
            course.localBoat().spin(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.COMMA)) {
            course.localBoat().spin(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            camera.zoom -= .01;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            camera.zoom += .01;
        }

        try {
            drawAll();
            course.updateCourse(playing);
        } catch(Exception e){
            System.out.println("---------------------------------there was an exception rendering" );
            e.printStackTrace();
            batch.end();
            shapes.end();
        }
        course.sendLocation();
    }

    private void drawAll() {
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        batch.begin();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        course.draw(batch, shapes);
        batch.setProjectionMatrix(hudcam.combined);
        course.drawHUD(batch, font24);
        batch.end();
        shapes.setProjectionMatrix(hudcam.combined);
        shapes.rect(backBounds.x,backBounds.y,backBounds.width,backBounds.height);
        shapes.end();
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
        batch.dispose();
        shapes.dispose();
    }

    @Override
    public void onWaitingStarted(String message) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onGameStarted(String message) {

    }

    @Override
    public void onGameFinished(int code, boolean isRemote) {

    }

    @Override
    public void onGameUpdateReceived(String message) {
        try {
            System.out.println("onGameUpdateReceived - CourseScreen");
            JSONObject data = new JSONObject(message);
             String n = data.getString("n");
             int id = data.getInt("id");
             float x = (float)data.getDouble("x");
             float y = (float)data.getDouble("y");
             float angle = (float)data.getDouble("angle");
             float sailTrim = (float)data.getDouble("sailTrim");
             float rudderAngle = (float)data.getDouble("rudderAngle");
             //float width = (float)data.getDouble("width");
             //float height = (float)data.getDouble("height");
            boolean star = data.getBoolean("starboard");
            System.out.println(id);
            course.update(n,  x,  y,  angle,  sailTrim,  rudderAngle,  id, star);
        } catch (Exception e) {
            System.out.println("caught an exception while updating courseScreen");
            // exception in onMoveNotificationReceived
        }

    }
}
