package arkanoid;

import java.awt.Color;
import java.awt.Graphics;

public class Paleta {
    private int x, y;
    private int vel = 0;
    private int speed = 10;
    private int width = 100;
    private int height = 20;
    private Color color = new Color(100, 100, 255);
    
    public Paleta() {
        resetPosition();
    }
    
    public void resetPosition() {
        x = Game.WIDTH / 2 - width / 2;
        y = Game.HEIGHT - 50;
        vel = 0;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    public void update(Pelota pelota) {
        x = Game.ensureRange(x + vel, 0, Game.WIDTH - width);
    }

    public void switchDirections(int direction) {
        vel = speed * direction;
    }

    public void stop() {
        vel = 0;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getVel() { return vel; }
}