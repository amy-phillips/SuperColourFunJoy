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
            pixmap.setColor(Color.RED);
            pixmap.fill();

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
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        sprite.setPosition(0, 0);
        sprite.draw(batch);
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

        floodFill(screenX, screenY, fillColour);
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) return false;

        floodFill(screenX, screenY, fillColour);
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

    private void floodFill(int x, int y, Color newC) {

        int prevC = pixmap.getPixel(x, y);
        Gdx.app.log("MyTag", "prevC:"+prevC);
        Gdx.app.log("MyTag", "Color(prevC):"+new Color(prevC));
        Gdx.app.log("MyTag", "newC:"+newC);
        if(Color.rgba8888(newC) == prevC)
            return;

        pixmap.setColor(newC);

        //simple flood fill with loop https://en.wikipedia.org/wiki/Flood_fill#Alternative_implementations
        Queue<Vector2> to_fill = new Queue<Vector2>();
        to_fill.addLast(new Vector2(x,y));
        while(to_fill.size > 0) {
            Vector2 pop = to_fill.removeFirst();
            int curx = (int)pop.x;
            int cury = (int)pop.y;

            int w = curx;
            int e = curx;

            //Move w to the west until the color of the node to the west of w no longer matches target-color.
            while(w>1 && (pixmap.getPixel(w-1, cury) == prevC)) {
                w = w-1;
            }
            //Move e to the east until the color of the node to the east of e no longer matches target-color.
            while(e<pixmap.getWidth()-2 && pixmap.getPixel(e+1, cury) == prevC) {
                e = e+1;
            }

            //For each node n between w and e:
            for(curx=w; curx<=e; ++curx) {
                //optimisation - did we already do this one?
                if(pixmap.getPixel(curx, cury) != prevC)
                    continue;

                //Set the color of n to replacement-color.
                pixmap.drawPixel(curx, cury);

                //If the color of the node to the north of n is target-color, add that node to Q.
                if(cury>0 && pixmap.getPixel(curx, cury-1)==prevC) {
                    Vector2 to_add = new Vector2(curx, cury - 1);

                    if (to_fill.indexOf(to_add, true) == -1)
                        to_fill.addLast(to_add);

                }

                //If the color of the node to the south of n is target-color, add that node to Q.
                if(cury<pixmap.getHeight()-1 && pixmap.getPixel(curx, cury+1)==prevC) {
                    Vector2 to_add = new Vector2(curx, cury+1);

                    if (to_fill.indexOf(to_add, true) == -1)
                        to_fill.addLast(to_add);
                }
            }
        }

        texture = new Texture(pixmap);
        sprite = new Sprite(texture);
    }

}
