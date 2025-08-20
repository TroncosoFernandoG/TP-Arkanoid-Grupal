package arkanoid;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private boolean running = false;
    private Thread gameThread;
    public boolean isgameStarted = false;
    
    private Paleta paleta;
    private Pelota pelota;
    private List<Bloque> bloques = new ArrayList<>();
    private int nivelActual = 1;
    private int vidas = 3;
    private int puntuacion = 0;
    private Botones botones;
    
    public Game() {
        canvasSetup();
        new Ventana("Arkanoid", this);
        
        paleta = new Paleta();
        botones = new Botones(this, paleta);
        this.addKeyListener(botones);
        this.setFocusable(true);
        
        inicializarNivel(1);
    }

    private void canvasSetup() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
    }

    private void inicializarNivel(int nivel) {
        paleta.resetPosition();
        pelota = new Pelota(this);
        bloques.clear();
        crearBloquesParaNivel(nivel);
    }
    
    private void crearBloquesParaNivel(int nivel) {
        int filas = 4 + nivel;
        int columnas = 10;
        int bloqueWidth = 70;
        int bloqueHeight = 25;
        int espacio = 5;
        int margenSuperior = 50;
        
        Color[] colores = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA};
        
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                int x = col * (bloqueWidth + espacio) + espacio;
                int y = fila * (bloqueHeight + espacio) + margenSuperior;
                
                int tipo = 0;
                if (fila == 0 && col % 3 == 0) tipo = 1;
                if (fila == 2 && col % 4 == 0) tipo = 2;
                
                Color color = colores[(fila + nivel) % colores.length];
                bloques.add(new Bloque(x, y, bloqueWidth, bloqueHeight, color, tipo));
            }
        }
    }
    
    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if (delta >= 1) {
                if (isgameStarted) {
                    update();
                }
                draw();
                delta--;
            }
        }
        stop();
    }
    
    public synchronized void start() {
        gameThread = new Thread(this);
        gameThread.start();
        running = true;
    }

    public void stop() {
        try {
            gameThread.join();
            running = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void update() {
        pelota.update(paleta, bloques);
        paleta.update(pelota);
        
        if (bloques.isEmpty()) {
            nivelActual++;
            if (nivelActual > 10) {
                isgameStarted = false;
            } else {
                pelota.incrementarVelocidad(0.5);
                inicializarNivel(nivelActual);
            }
        }
    }
    
    public void draw() {
        BufferStrategy buffer = this.getBufferStrategy();
        if (buffer == null) {
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = buffer.getDrawGraphics();
        
        if (!isgameStarted) {
            drawWelcomeScreen(g);
        } else {
            drawBackground(g);
            paleta.draw(g);
            pelota.draw(g);
            
            for (Bloque bloque : bloques) {
                bloque.draw(g);
            }
            
            drawGameInfo(g);
        }
        
        g.dispose();
        buffer.show();
    }
    
    private void drawWelcomeScreen(Graphics g) {
        g.setColor(new Color(0, 0, 51));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 60));
        String title = "ARKANOID";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (WIDTH / 2) - (titleWidth / 2), 150);
        
        g.setFont(new Font("Roboto", Font.PLAIN, 30));
        String startMessage = "Presione ESPACIO para jugar";
        int messageWidth = g.getFontMetrics().stringWidth(startMessage);
        g.drawString(startMessage, (WIDTH / 2) - (messageWidth / 2), HEIGHT / 2 + 50);
    }
    
    private void drawBackground(Graphics g) {
        g.setColor(new Color(0, 0, 80));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
    }
    
    private void drawGameInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 20));
        g.drawString("Puntos: " + puntuacion, 20, 30);
        g.drawString("Vidas: " + vidas, WIDTH - 100, 30);
        g.drawString("Nivel: " + nivelActual, WIDTH / 2 - 30, 30);
    }
    
    public static int ensureRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
    
    public void incrementarPuntuacion(int puntos) {
        puntuacion += puntos;
    }
    
    public void perderVida() {
        vidas--;
        if (vidas <= 0) {
            isgameStarted = false;
        } else {
            pelota.reset();
        }
    }
    
    public void eliminarBloque(Bloque bloque) {
        bloques.remove(bloque);
    }
    
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}