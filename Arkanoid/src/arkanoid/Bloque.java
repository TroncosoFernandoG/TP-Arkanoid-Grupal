package arkanoid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Bloque {
    public int x, y;
    public int width, height;
    public Color color;
    private int tipo;
    private int resistencia;
    private int resistenciaMaxima;
    private Color colorOriginal;
    
    public Bloque(int x, int y, int width, int height, Color color, int tipo) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.colorOriginal = color;
        this.tipo = tipo;
        
        switch (tipo) {
            case 1: 
                resistencia = 3;
                resistenciaMaxima = 3;
                break;
            case 2: 
                resistencia = 1;
                resistenciaMaxima = 1;
                break;
            default: 
                resistencia = 2;
                resistenciaMaxima = 2;
        }
    }
    
    public void setResistencia(int resistencia) {
        this.resistencia = resistencia;
        this.resistenciaMaxima = resistencia;
        actualizarColor();
    }
    
    private void actualizarColor() {
        if (resistenciaMaxima > 1) {
            float factor = (float) resistencia / resistenciaMaxima;
            int r = (int) (colorOriginal.getRed() * factor + 50 * (1 - factor));
            int g = (int) (colorOriginal.getGreen() * factor + 50 * (1 - factor));
            int b = (int) (colorOriginal.getBlue() * factor + 50 * (1 - factor));
            this.color = new Color(
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255)
            );
        }
    }
    
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        
        // Mostrar número de resistencia si es mayor a 1
        if (resistenciaMaxima > 1) {
            g.setColor(resistencia > 1 ? Color.BLACK : Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            String resistenciaStr = String.valueOf(resistencia);
            int strWidth = g.getFontMetrics().stringWidth(resistenciaStr);
            g.drawString(resistenciaStr, x + width/2 - strWidth/2, y + height/2 + 5);
        }
    }
    
    public boolean golpear() {
        resistencia--;
        actualizarColor();
        return resistencia <= 0;
    }
    
    public int getPuntos() {
        int puntosBase;
        switch (tipo) {
            case 1: puntosBase = 30; break;
            case 2: puntosBase = 50; break;
            default: puntosBase = 20;
        }
        return puntosBase * resistenciaMaxima; // Más puntos por bloques resistentes
    }
    
    public int getResistencia() {
        return resistencia;
    }
}