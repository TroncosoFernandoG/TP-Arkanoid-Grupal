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
        int filas = 2 + (nivel - 1); // Primer nivel: 2 filas
        int columnas = 10;
        int bloqueWidth = 70;
        int bloqueHeight = 25;
        int espacio = 5;
        int margenSuperior = 50;
        
        Color[] colores = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA};
        
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                if (!debeTenerBloque(nivel, fila, col, filas, columnas)) {
                    continue;
                }
                
                int x = col * (bloqueWidth + espacio) + espacio;
                int y = fila * (bloqueHeight + espacio) + margenSuperior;
                
                int tipo = determinarTipo(nivel, fila, col);
                int resistencia = determinarResistencia(nivel, fila, col);
                
                Color color = colores[(fila + nivel) % colores.length];
                Bloque bloque = new Bloque(x, y, bloqueWidth, bloqueHeight, color, tipo);
                bloque.setResistencia(resistencia);
                bloques.add(bloque);
            }
        }
    }
    
    private boolean debeTenerBloque(int nivel, int fila, int col, int totalFilas, int totalColumnas) {
        // Crear diferentes patrones según el nivel
        switch (nivel % 5) { // Ciclo de patrones cada 5 niveles
            case 1: // Patrón: columnas alternas
                return col % 2 == fila % 2;
                
            case 2: // Patrón: marco exterior
                return fila == 0 || fila == totalFilas - 1 || 
                       col == 0 || col == totalColumnas - 1;
                
            case 3: // Patrón: pirámide
                int centro = totalColumnas / 2;
                int distancia = Math.abs(col - centro);
                return distancia <= fila && fila < totalFilas / 2 + 1;
                
            case 4: // Patrón: rombo
                int centroFila = totalFilas / 2;
                int centroCol = totalColumnas / 2;
                int distFila = Math.abs(fila - centroFila);
                int distCol = Math.abs(col - centroCol);
                return distFila + distCol <= Math.min(centroFila, centroCol) + 1;
                
            case 0: // Patrón: ajedrez
                return (fila + col) % 2 == 0;
                
            default:
                return true;
        }
    }
    
    private int determinarTipo(int nivel, int fila, int col) {
        // Bloques especiales basados en posición y nivel
        if (fila == 0 && col % 3 == 0) return 1; // Bloques resistentes
        if (fila == Math.min(2, nivel) && col % 4 == 0) return 2; // Bloques de puntos extra
        return 0; // Bloque normal
    }
    
    private int determinarResistencia(int nivel, int fila, int col) {
        int resistenciaBase = 1;
        
        // Aumentar resistencia según nivel
        if (nivel >= 3) resistenciaBase++;
        if (nivel >= 6) resistenciaBase++;
        if (nivel >= 9) resistenciaBase++;
        
        // Bloques en posiciones estratégicas son más resistentes
        if (fila == 0) resistenciaBase++; // Primera fila más resistente
        if (col % 2 == 0) resistenciaBase++; // Columnas pares más resistentes
        
        return Math.min(resistenciaBase, 5); // Máximo 5 de resistencia
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