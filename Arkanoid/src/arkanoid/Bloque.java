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
    
    public Bloque(int x, int y, int width, int height, Color color, int tipo) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.tipo = tipo;
        
        switch (tipo) {
            case 1: resistencia = 2; break;
            case 2: resistencia = 1; break;
            default: resistencia = 1;
        }
    }
    
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        
        if (tipo == 1 && resistencia > 1) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("" + resistencia, x + width/2 - 5, y + height/2 + 5);
        }
    }
    
    public boolean golpear() {
        resistencia--;
        return resistencia <= 0;
    }
    
    public int getPuntos() {
        switch (tipo) {
            case 1: return 20;
            case 2: return 50;
            default: return 10;
        }
    }
}