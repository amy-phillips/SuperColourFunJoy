package uk.co.islovely.supercolourfunjoy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by Amy on 21/08/2016.
 */
public class Splat {

    public final int DEAD = -1;
    public int timeToLive = DEAD;

    public int posX;
    public int posY;

    public Color colour;
    public Sprite sprite;

    public boolean isValid() { return timeToLive != DEAD; }
    public Splat(int posx, int posy, Texture tex, Color col) {
        posX = posx - tex.getWidth()/2;
        posY = posy - tex.getHeight()/2;
        colour = col;
        timeToLive = 60;
        sprite = new Sprite(tex);
        //sprite.setOriginCenter();
        sprite.setPosition(posX, posY);
        sprite.setColor(colour);
        sprite.setScale(0.8f);
    }

    public void update() {
        if(!isValid())
            return;

        timeToLive--;

        // clear up old sprite
        if(!isValid()) {
            sprite=null;
        }
    }
}
