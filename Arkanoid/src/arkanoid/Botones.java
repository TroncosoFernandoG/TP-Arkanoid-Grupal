package arkanoid;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Botones extends KeyAdapter {
    private Paleta paleta;
    private boolean left = false;
    private boolean right = false;
    private Game game;
    
    public Botones(Game game, Paleta paleta) {
        this.game = game;
        this.paleta = paleta;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (!game.isgameStarted && key == KeyEvent.VK_SPACE) {
            game.isgameStarted = true; 
        }
        
        if (game.isgameStarted) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                paleta.switchDirections(-1);
                left = true;
            }
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                paleta.switchDirections(1);
                right = true;
            }
            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            left = false;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            right = false;
        }

        if (!left && !right) {
            paleta.stop();
        }
    }
}