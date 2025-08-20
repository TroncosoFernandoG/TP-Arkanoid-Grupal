package arkanoid;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Pelota {
    public static final int SIZE = 16;
    private static final double VELOCIDAD_INICIAL = 4.0;
    private static final double VELOCIDAD_MAXIMA = 10.0;
    
    private int x, y;
    private double xVel, yVel;
    private double speed;
    private Game game;
    
    public Pelota(Game game) {
        this.game = game;
        reset();
    }

    public void reset() {
        x = Game.WIDTH / 2 - SIZE / 2;
        y = Game.HEIGHT / 2 - SIZE / 2;
        
        double angulo = Math.random() * Math.PI/2 + Math.PI/4;
        xVel = Math.cos(angulo);
        yVel = -Math.sin(angulo);
        speed = VELOCIDAD_INICIAL;
    }
    
    public void incrementarVelocidad(double incremento) {
        speed += incremento;
        if (speed > VELOCIDAD_MAXIMA) {
            speed = VELOCIDAD_MAXIMA;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillOval(x, y, SIZE, SIZE);
    }

    public void update(Paleta paleta, List<Bloque> bloques) {
        x += xVel * speed;
        y += yVel * speed;

        if (x <= 0 || x + SIZE >= Game.WIDTH) {
            xVel *= -1;
        }
        
        if (y <= 0) {
            yVel *= -1;
        }
        
        if (y + SIZE >= Game.HEIGHT) {
            game.perderVida();
        }
        
        for (int i = 0; i < bloques.size(); i++) {
            Bloque bloque = bloques.get(i);
            if (colisionConBloque(bloque)) {
                boolean golpeLateral = (x < bloque.x || x > bloque.x + bloque.width);
                
                if (golpeLateral) {
                    xVel *= -1;
                } else {
                    yVel *= -1;
                }
                
                if (bloque.golpear()) {
                    game.eliminarBloque(bloque);
                    game.incrementarPuntuacion(bloque.getPuntos());
                }
                break;
            }
        }
        
        // Colisión con la paleta
        if (y + SIZE >= paleta.getY() && 
            x + SIZE >= paleta.getX() && 
            x <= paleta.getX() + paleta.getWidth()) {
            
            // Cambiamos dirección vertical
            yVel = -Math.abs(yVel);
            
            // Influencia del movimiento de la paleta
            int paletaVel = paleta.getVel();
            if (paletaVel != 0) {
                int centroPaleta = paleta.getX() + paleta.getWidth() / 2;
                double posRelativa = (x - centroPaleta) / (double) (paleta.getWidth() / 2);
                double influencia = paletaVel * 0.05 + posRelativa * 0.5;
                xVel += influencia;
                
                // Limitar velocidad horizontal
                double maxXVel = 2.0;
                if (xVel > maxXVel) xVel = maxXVel;
                if (xVel < -maxXVel) xVel = -maxXVel;
            }
        }
    }
    
    private boolean colisionConBloque(Bloque bloque) {
        return x + SIZE >= bloque.x && 
               x <= bloque.x + bloque.width && 
               y + SIZE >= bloque.y && 
               y <= bloque.y + bloque.height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}