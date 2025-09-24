package arkanoid;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Pelota {
    public static final int SIZE = 16;
    private static final double VELOCIDAD_INICIAL = 7.0;
    private static final double VELOCIDAD_MAXIMA = 8.0;
    
    private int x, y;
    private double xVel, yVel;
    private double speed;
    private Game game;
    private List<Bloque> bloquesGolpeadosEsteFrame;
    
    public Pelota(Game game) {
        this.game = game;
        this.bloquesGolpeadosEsteFrame = new ArrayList<>();
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
        speed = Math.min(speed + incremento, VELOCIDAD_MAXIMA);
    }

    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillOval(x, y, SIZE, SIZE);
    }

    public void update(Paleta paleta, List<Bloque> bloques) {
        bloquesGolpeadosEsteFrame.clear();
        
        // Guardar posición anterior para detectar colisiones
        int prevX = x;
        int prevY = y;
        
        // Movimiento
        x += xVel * speed;
        y += yVel * speed;

        // Colisiones con bordes
        if (x <= 0) {
            x = 0;
            xVel = Math.abs(xVel);
        } else if (x + SIZE >= Game.WIDTH) {
            x = Game.WIDTH - SIZE;
            xVel = -Math.abs(xVel);
        }
        
        if (y <= 0) {
            y = 0;
            yVel = Math.abs(yVel);
        } else if (y + SIZE >= Game.HEIGHT) {
            game.perderVida();
            return; // Salir temprano para evitar más procesamiento
        }
        
        // Colisión con la paleta (prioridad alta)
        if (colisionConPaleta(paleta)) {
            manejarColisionPaleta(paleta, prevX, prevY);
        }
        
        // Colisión con bloques
        for (int i = 0; i < bloques.size(); i++) {
            Bloque bloque = bloques.get(i);
            if (!bloquesGolpeadosEsteFrame.contains(bloque) && 
                colisionConBloque(bloque)) {
                manejarColisionBloque(bloque, prevX, prevY);
            }
        }
    }
    
    private boolean colisionConPaleta(Paleta paleta) {
        // Detección más precisa considerando la forma circular
        int centroX = x + SIZE/2;
        int centroY = y + SIZE/2;
        int radio = SIZE/2;
        
        // Encontrar el punto más cercano en el rectángulo al centro del círculo
        int closestX = clamp(centroX, paleta.getX(), paleta.getX() + paleta.getWidth());
        int closestY = clamp(centroY, paleta.getY(), paleta.getY() + paleta.getHeight());
        
        // Calcular distancia entre el centro y el punto más cercano
        int distanceX = centroX - closestX;
        int distanceY = centroY - closestY;
        
        // Si la distancia es menor que el radio, hay colisión
        return (distanceX * distanceX + distanceY * distanceY) < (radio * radio);
    }
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private void manejarColisionPaleta(Paleta paleta, int prevX, int prevY) {
        // Método más preciso para determinar el punto de impacto
        int centroPelotaX = x + SIZE/2;
        int centroPelotaY = y + SIZE/2;
        
        // Calcular punto de impacto relativo en la paleta (0 = izquierda, 1 = derecha)
        double impactoRelativo = (centroPelotaX - paleta.getX()) / (double)paleta.getWidth();
        impactoRelativo = Math.max(0, Math.min(1, impactoRelativo)); // Asegurar entre 0 y 1
        
        // Calcular nuevo ángulo basado en el punto de impacto
        // -π/3 (60°) a la izquierda, 0 en el centro, π/3 (60°) a la derecha
        double nuevoAngulo = Math.PI/3 * (2 * impactoRelativo - 1);
        
        // Aplicar nuevo vector de dirección
        xVel = Math.sin(nuevoAngulo);
        yVel = -Math.cos(nuevoAngulo); // Siempre hacia arriba
        
        // Influencia del movimiento de la paleta
        int paletaVel = paleta.getVel();
        if (paletaVel != 0) {
            xVel += paletaVel * 0.02; // Influencia muy suave
        }
        
        // Normalizar velocidad
        normalizarVelocidad();
        
        // Ajustar posición para evitar colisiones múltiples
        y = paleta.getY() - SIZE;
        
        // Pequeño ajuste adicional
        y -= 2;
    }
    
    private boolean colisionConBloque(Bloque bloque) {
        // Detección de colisión círculo-rectángulo más precisa
        int centroX = x + SIZE/2;
        int centroY = y + SIZE/2;
        int radio = SIZE/2;
        
        // Encontrar el punto más cercano en el rectángulo al centro del círculo
        int closestX = clamp(centroX, bloque.x, bloque.x + bloque.width);
        int closestY = clamp(centroY, bloque.y, bloque.y + bloque.height);
        
        // Calcular distancia entre el centro y el punto más cercano
        int distanceX = centroX - closestX;
        int distanceY = centroY - closestY;
        
        // Si la distancia es menor que el radio, hay colisión
        return (distanceX * distanceX + distanceY * distanceY) < (radio * radio);
    }
    
    private void manejarColisionBloque(Bloque bloque, int prevX, int prevY) {
        // Determinar dirección de colisión basada en la posición anterior
        boolean colisionHorizontal = false;
        boolean colisionVertical = false;
        
        // Calcular penetración en cada eje
        int penetracionX = calcularPenetracionX(bloque, prevX);
        int penetracionY = calcularPenetracionY(bloque, prevY);
        
        // La colisión ocurre en el eje con menor penetración
        if (Math.abs(penetracionX) < Math.abs(penetracionY)) {
            colisionHorizontal = true;
        } else {
            colisionVertical = true;
        }
        
        // Ajustar posición basada en la colisión
        if (colisionHorizontal) {
            x += penetracionX;
            xVel *= -1;
        }
        
        if (colisionVertical) {
            y += penetracionY;
            yVel *= -1;
        }
        
        // Registrar bloque golpeado y procesar
        bloquesGolpeadosEsteFrame.add(bloque);
        if (bloque.golpear()) {
            game.eliminarBloque(bloque);
            game.incrementarPuntuacion(bloque.getPuntos());
        }
    }
    
    private int calcularPenetracionX(Bloque bloque, int prevX) {
        if (xVel > 0) { // Moviéndose a la derecha
            return bloque.x - (x + SIZE);
        } else { // Moviéndose a la izquierda
            return (bloque.x + bloque.width) - x;
        }
    }
    
    private int calcularPenetracionY(Bloque bloque, int prevY) {
        if (yVel > 0) { // Moviéndose hacia abajo
            return bloque.y - (y + SIZE);
        } else { // Moviéndose hacia arriba
            return (bloque.y + bloque.height) - y;
        }
    }
    
    private void normalizarVelocidad() {
        double magnitud = Math.sqrt(xVel * xVel + yVel * yVel);
        
        if (magnitud > 0) {
            xVel = (xVel / magnitud) * (speed / VELOCIDAD_INICIAL);
            yVel = (yVel / magnitud) * (speed / VELOCIDAD_INICIAL);
        }
        
        // Limitar velocidad horizontal
        double maxXVel = 1.2;
        if (xVel > maxXVel) xVel = maxXVel;
        if (xVel < -maxXVel) xVel = -maxXVel;
        
        // Asegurar velocidad vertical mínima
        double minYVel = 0.8;
        if (Math.abs(yVel) < minYVel) {
            yVel = yVel > 0 ? minYVel : -minYVel;
            // Renormalizar después del ajuste
            normalizarVelocidad();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
}