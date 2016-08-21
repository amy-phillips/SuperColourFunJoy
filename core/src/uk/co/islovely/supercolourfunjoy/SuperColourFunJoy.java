package uk.co.islovely.supercolourfunjoy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Queue;

import java.io.ByteArrayOutputStream;

public class SuperColourFunJoy extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture splatTexture;
    private int score = 0;

    public enum Mode {
        normal,
        prepare,
        preview,
        takePicture,
        waitForPictureReady,
    }

    private Mode mode = Mode.normal;
    private Splat splats[] = new Splat[1000];
    private Sound splatSounds[] = new Sound[3];
    int splatSoundIndex = 0;
    private Sound errorSound;

    private final DeviceCameraControl deviceCameraControl;
    public SuperColourFunJoy(DeviceCameraControl cameraControl) {
        this.deviceCameraControl = cameraControl;
    }

	@Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.input.setInputProcessor(this);

        splatTexture = new Texture(Gdx.files.internal("splat_white.png"));

		batch = new SpriteBatch();

        splatSounds[0] = Gdx.audio.newSound(Gdx.files.internal("splat_01.mp3"));
        splatSounds[1] = Gdx.audio.newSound(Gdx.files.internal("splat_02.mp3"));
        splatSounds[2] = Gdx.audio.newSound(Gdx.files.internal("splat_03.mp3"));

        errorSound =  Gdx.audio.newSound(Gdx.files.internal("error.mp3"));
	}

    boolean firstTouch = true;

    @Override
    public void render() {

        if(mode == Mode.normal) {
            mode = Mode.prepare;
            if (deviceCameraControl != null) {
                deviceCameraControl.prepareCameraAsync();
            }
        }
        if (mode == Mode.prepare) {
            if (deviceCameraControl != null) {
                if (deviceCameraControl.isReady()) {
                    deviceCameraControl.startPreviewAsync();
                    mode = Mode.preview;
                }
            }
        }


        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
        for(int i=0; i<splats.length; ++i) {
            if(splats[i] == null)
                continue;

            splats[i].sprite.draw(batch);

            // update in render - urgh!
            splats[i].update();
            if(!splats[i].isValid())
                splats[i] = null;
        }
        batch.end();

        BitmapFont arial = new BitmapFont();

        Label.LabelStyle score_drop_style = new Label.LabelStyle(arial, Color.PURPLE);
        Label score_drop_label = new Label("Score: "+score, score_drop_style);
        score_drop_label.setFontScale(3);
        score_drop_label.setPosition(0, 10);

        Label.LabelStyle score_style = new Label.LabelStyle(arial, Color.YELLOW);
        Label score_label = new Label("Score: "+score, score_style);
        score_label.setFontScale(3);
        score_label.setPosition(0+6, 10+6);

        batch.begin();
        score_drop_label.draw(batch, 1.0f);
        score_label.draw(batch, 1.0f);
        batch.end();

        if(firstTouch) {
            Label.LabelStyle splash_drop_style = new Label.LabelStyle(arial, Color.BLACK);
            Label splash_drop_label = new Label("Cover grey stuff in paint!", splash_drop_style);
            splash_drop_label.setFontScale(6);
            splash_drop_label.setPosition(10, Gdx.graphics.getHeight()/2);

            Label.LabelStyle splash_style = new Label.LabelStyle(arial, Color.LIGHT_GRAY);
            Label splash_label = new Label("Cover grey stuff in paint!", splash_style);
            splash_label.setFontScale(6);
            splash_label.setPosition(10+6, Gdx.graphics.getHeight()/2+6);

            batch.begin();
            splash_drop_label.draw(batch, 1.0f);
            splash_label.draw(batch, 1.0f);
            batch.end();
        }


    }

    @Override
    public void resume() {
        super.resume();
        //theView.requestFocus();
        //theView.requestFocusFromTouch();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void dispose() {
        deviceCameraControl.stopPreviewAsync();
        mode = Mode.normal;

        batch.dispose();
        splatTexture.dispose();
        errorSound.dispose();
        for(int i=0; i<splatSounds.length; ++i) {
            splatSounds[i].dispose();
        }
    }

    boolean dragging;
    Color fillColours[] = {Color.FOREST, Color.TEAL, Color.SCARLET, Color.ROYAL, Color.GOLD, Color.PINK };
    int fillColourIndex = 0;
    @Override public boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0) return false;

        //floodFill(screenX, screenY, fillColour);
        dragging = true;
        return true;
    }

    @Override public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (!dragging) return false;

        firstTouch = false;
        ThrowPaint(screenX, screenY);
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) return false;

        firstTouch = false;
        ThrowPaint(screenX, screenY);
        dragging = false;
        return true;
    }

    @Override public boolean keyDown (int keycode) {
        return false;
    }

    @Override public boolean keyUp (int keycode) {
        return false;
    }

    @Override public boolean keyTyped (char character) {
        return false;
    }

    @Override public boolean scrolled (int amount) {
        return false;
    }

    private Color getFillColour() {
        fillColourIndex = (fillColourIndex + 1) % fillColours.length;
        return fillColours[fillColourIndex];
    }

    // todo scoring on thread

    long lastPaintTime = 0;
    // todo cooldown period to stop two simultaneous paints
    private void ThrowPaint(final int screenX, final int screenY) {
        long timeSinceLastPaint = System.currentTimeMillis() - lastPaintTime;
        if(timeSinceLastPaint < 100)
            return;

        lastPaintTime = System.currentTimeMillis();

        Color col = getFillColour();

        for(int i=0; i<splats.length; ++i) {
            if(splats[i] != null)
                continue;

            splats[i] = new Splat(screenX, Gdx.graphics.getHeight()-screenY, splatTexture, col);

            break;
        }

        // did we hit any grey?
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do something important here, asynchronously to the rendering thread
                final int scoreIncrement = deviceCameraControl.scoreHitOnCameraFeed(screenX, screenY);

                // post a Runnable to the rendering thread that processes the result
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // process the result, e.g. add it to an Array<Result> field of the ApplicationListener.
                        if(scoreIncrement>0) {
                            splatSounds[splatSoundIndex].play();
                            splatSoundIndex = (splatSoundIndex+1) % splatSounds.length;
                        } else {
                            errorSound.play();
                        }
                        score += scoreIncrement;
                    }
                });
            }
        }).start();



    }

}
