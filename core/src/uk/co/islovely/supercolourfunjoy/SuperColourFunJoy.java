package uk.co.islovely.supercolourfunjoy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;

public class SuperColourFunJoy extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture splatTexture;

    public enum Mode {
        normal,
        prepare,
        preview,
        takePicture,
        waitForPictureReady,
    }

    private Mode mode = Mode.normal;

    private Splat splats[] = new Splat[20];

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

	}

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
    }

    boolean dragging;
    Color fillColours[] = {Color.FOREST, Color.SCARLET, Color.ROYAL, Color.GOLD, Color.PINK };
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

        ThrowPaint(screenX, screenY);
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        //if (button != Input.Buttons.LEFT || pointer > 0) return false;

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

    private void ThrowPaint(int screenX, int screenY) {

        Color col = getFillColour();

        for(int i=0; i<splats.length; ++i) {
            if(splats[i] != null)
                continue;

            splats[i] = new Splat(screenX, Gdx.graphics.getHeight()-screenY, splatTexture, col);

            break;
        }

        deviceCameraControl.ThrowPaint(screenX, screenY, col);
    }

}
