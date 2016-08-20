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
    private Pixmap pixmap = null;
    private Texture texture;
    private Sprite sprite;

    public enum Mode {
        normal,
        prepare,
        preview,
        takePicture,
        waitForPictureReady,
    }

    private Mode mode = Mode.normal;

    private final DeviceCameraControl deviceCameraControl;
    public SuperColourFunJoy(DeviceCameraControl cameraControl) {
        this.deviceCameraControl = cameraControl;
    }

	@Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.input.setInputProcessor(this);



		batch = new SpriteBatch();

        if(pixmap == null) {
            // A Pixmap is basically a raw image in memory as repesented by pixels
            // We create one 256 wide, 128 height using 8 bytes for Red, Green, Blue and Alpha channels
            pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);

            //Fill it red
            //pixmap.setColor(Color.RED);
            //pixmap.fill();

            //Draw two lines forming an X
            pixmap.setColor(Color.BLACK);
            pixmap.drawLine(0, 0, pixmap.getWidth()-1, pixmap.getHeight()-1);
            pixmap.drawLine(0, pixmap.getHeight()-1, pixmap.getWidth()-1, 0);

            //Draw a circle about the middle
            pixmap.setColor(Color.YELLOW);
            pixmap.drawCircle(pixmap.getWidth()/2, pixmap.getHeight()/2, pixmap.getHeight()/2 - 1);
        }

		texture = new Texture(pixmap);

		//It's the textures responsibility now... get rid of the pixmap
		//pixmap.dispose();

		sprite = new Sprite(texture);
	}

    @Override
    public void render() {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        sprite.setPosition(0, 0);
        sprite.draw(batch);
        batch.end();

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
        texture.dispose();
        pixmap.dispose();
    }

    boolean dragging;
    Color fillColour = Color.FOREST;
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

        deviceCameraControl.ThrowPaint(screenX, screenY, fillColour);
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) return false;

        deviceCameraControl.ThrowPaint(screenX, screenY, fillColour);
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

}
