import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class LabyrintheGenerator extends JPanel {
    private static final int TAILLE = 15;
    private static final int CELL_SIZE = 30;
    private static final char MUR = '#';
    private static final char CHEMIN = '=';
    private static final char DEPART = 'S';
    private static final char ARRIVEE = 'E';
    private static final char CHEMIN_SOLUTION = '+';
    private static final char VISITE_TEMP = 'V';
    private static final char JOUEUR = 'P'; // Représentation du joueur
    private char[][] labyrinthe;
    private boolean modeDessin = false;
    private boolean modeJoueur = false; // Mode pour résoudre manuellement
    private Point positionJoueur; // Position actuelle du joueur
    private JLabel statsLabel;

    public LabyrintheGenerator() {
        genererLabyrintheAvecPrim();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (modeDessin) {
                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;
                    if (col < TAILLE && row < TAILLE) {
                        labyrinthe[row][col] = (labyrinthe[row][col] == MUR) ? CHEMIN : MUR;
                        repaint();
                    }
                }
            }
        });

        // Ajout d'un écouteur pour les touches fléchées
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (modeJoueur) {
                    deplacerJoueur(e.getKeyCode());
                }
            }
        });
        setFocusable(true); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Labyrinthe Solver");
            LabyrintheGenerator panel = new LabyrintheGenerator();
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            JButton btnBFS = new JButton("Résoudre avec BFS");
            JButton btnDFS = new JButton("Résoudre avec DFS");
            JButton btnGenerer = new JButton("Générer un labyrinthe");
            JButton btnReinitialiser = new JButton("Réinitialiser");
            JButton btnDessin = new JButton("Mode dessin");
            JButton btnCharger = new JButton("Charger un labyrinthe");
            JButton btnJoueur = new JButton("Résoudre moi-même");

            btnBFS.addActionListener(e -> panel.resoudreAvecBFS());
            btnDFS.addActionListener(e -> panel.resoudreAvecDFS());
            btnGenerer.addActionListener(e -> panel.genererLabyrintheAvecPrim());
            btnReinitialiser.addActionListener(e -> panel.reinitialiserLabyrinthe());
            btnDessin.addActionListener(e -> {
                panel.modeDessin = !panel.modeDessin;
                panel.modeJoueur = false; // Désactiver le mode joueur si mode dessin activé
            });
            btnCharger.addActionListener(e -> panel.chargerLabyrinthe());
            btnJoueur.addActionListener(e -> panel.activerModeJoueur());

            buttonPanel.add(btnBFS);
            buttonPanel.add(btnDFS);
            buttonPanel.add(btnGenerer);
            buttonPanel.add(btnReinitialiser);
            buttonPanel.add(btnDessin);
            buttonPanel.add(btnCharger);
            buttonPanel.add(btnJoueur);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            panel.statsLabel = new JLabel("Statistiques : ");
            frame.add(panel.statsLabel, BorderLayout.NORTH);

            frame.setSize(TAILLE * CELL_SIZE + 20, TAILLE * CELL_SIZE + 150);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private void genererLabyrintheAvecPrim() {
        labyrinthe = new char[TAILLE][TAILLE];
        for (char[] row : labyrinthe) Arrays.fill(row, MUR);
        Random rand = new Random();
        List<int[]> murs = new ArrayList<>();
        int x = 1, y = 1;
        labyrinthe[y][x] = CHEMIN;
        murs.add(new int[]{x, y});

        while (!murs.isEmpty()) {
            int[] mur = murs.remove(rand.nextInt(murs.size()));
            int cx = mur[0], cy = mur[1];
            int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
            Collections.shuffle(Arrays.asList(directions));

            for (int[] dir : directions) {
                int nx = cx + dir[0], ny = cy + dir[1];
                if (nx > 0 && ny > 0 && nx < TAILLE - 1 && ny < TAILLE - 1 && labyrinthe[ny][nx] == MUR) {
                    labyrinthe[cy + dir[1] / 2][cx + dir[0] / 2] = CHEMIN;
                    labyrinthe[ny][nx] = CHEMIN;
                    murs.add(new int[]{nx, ny});
                }
            }
        }
        labyrinthe[1][1] = DEPART;
        labyrinthe[TAILLE - 2][TAILLE - 2] = ARRIVEE;
        modeJoueur = false; // Désactiver le mode joueur lors de la génération
        System.out.println("Labyrinthe initial :");
        afficherLabyrintheTextuel();
        repaint();
    }

    // Réinitialiser le labyrinthe (effacer la solution sans générer un nouveau)
    private void reinitialiserLabyrinthe() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (labyrinthe[i][j] == CHEMIN_SOLUTION || labyrinthe[i][j] == VISITE_TEMP || labyrinthe[i][j] == JOUEUR) {
                    labyrinthe[i][j] = CHEMIN;
                }
            }
        }
        labyrinthe[1][1] = DEPART;
        labyrinthe[TAILLE - 2][TAILLE - 2] = ARRIVEE;
        modeJoueur = false; // Désactiver le mode joueur lors de la réinitialisation
        statsLabel.setText("Statistiques : ");
        System.out.println("Labyrinthe réinitialisé :");
        afficherLabyrintheTextuel();
        repaint();
    }

    // Charger un labyrinthe depuis un fichier
    private void chargerLabyrinthe() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                labyrinthe = new char[TAILLE][TAILLE];
                for (int i = 0; i < TAILLE; i++) {
                    String line = reader.readLine();
                    if (line == null || line.length() < TAILLE) {
                        throw new IOException("Fichier invalide : taille incorrecte");
                    }
                    for (int j = 0; j < TAILLE; j++) {
                        labyrinthe[i][j] = line.charAt(j);
                    }
                }
                modeJoueur = false; // Désactiver le mode joueur lors du chargement
                System.out.println("Labyrinthe chargé :");
                afficherLabyrintheTextuel();
                repaint();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors du chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

  
    private void activerModeJoueur() {
        reinitialiserLabyrinthe(); 
        modeJoueur = true;
        modeDessin = false; 
        positionJoueur = new Point(1, 1); 
        labyrinthe[1][1] = JOUEUR; 
        statsLabel.setText("Utilisez les touches fléchées pour vous déplacer !");
        repaint();
        requestFocusInWindow(); // S'assurer que le panneau peut recevoir les événements clavier
    }

    // Déplacer le joueur avec les touches fléchées
    private void deplacerJoueur(int keyCode) {
        if (!modeJoueur) return;

        int dx = 0, dy = 0;
        switch (keyCode) {
            case KeyEvent.VK_UP: dy = -1; break;
            case KeyEvent.VK_DOWN: dy = 1; break;
            case KeyEvent.VK_LEFT: dx = -1; break;
            case KeyEvent.VK_RIGHT: dx = 1; break;
            default: return;
        }

        int newX = positionJoueur.x + dx;
        int newY = positionJoueur.y + dy;

        // Vérifier si le déplacement est valide
        if (newX >= 0 && newX < TAILLE && newY >= 0 && newY < TAILLE && labyrinthe[newY][newX] != MUR) {
            // Restaurer la case actuelle (sauf si c'est l'arrivée)
            if (labyrinthe[positionJoueur.y][positionJoueur.x] != ARRIVEE) {
                labyrinthe[positionJoueur.y][positionJoueur.x] = CHEMIN;
            }
            // Mettre à jour la position du joueur
            positionJoueur.setLocation(newX, newY);
            // Vérifier si le joueur a atteint l'arrivée
            if (labyrinthe[newY][newX] == ARRIVEE) {
                modeJoueur = false;
                statsLabel.setText("Félicitations ! Vous avez atteint l'arrivée !");
                JOptionPane.showMessageDialog(this, "Vous avez gagné !", "Victoire", JOptionPane.INFORMATION_MESSAGE);
            } else {
                labyrinthe[newY][newX] = JOUEUR;
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                switch (labyrinthe[i][j]) {
                    case MUR:
                        g.setColor(Color.BLACK);
                        break;
                    case CHEMIN_SOLUTION:
                        g.setColor(Color.GREEN);
                        break;
                    case DEPART:
                        g.setColor(Color.BLUE);
                        break;
                    case ARRIVEE:
                        g.setColor(Color.RED);
                        break;
                    case VISITE_TEMP:
                        g.setColor(Color.YELLOW);
                        break;
                    case JOUEUR:
                        g.setColor(Color.ORANGE); // Couleur pour le joueur
                        break;
                    default:
                        g.setColor(Color.WHITE);
                        break;
                }
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void reinitialiserSolution() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (labyrinthe[i][j] == CHEMIN_SOLUTION || labyrinthe[i][j] == VISITE_TEMP || labyrinthe[i][j] == JOUEUR) {
                    labyrinthe[i][j] = CHEMIN;
                }
            }
        }
    }

    private void afficherLabyrintheTextuel() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                System.out.print(labyrinthe[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public void resoudreAvecBFS() {
        reinitialiserSolution();
        boolean[][] visite = new boolean[TAILLE][TAILLE];
        Map<Point, Point> parent = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(1, 1));
        visite[1][1] = true;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int etapes = 0;
        long debut = System.nanoTime();

        while (!queue.isEmpty()) {
            Point pos = queue.poll();
            int x = pos.x, y = pos.y;
            etapes++;

            if (labyrinthe[y][x] == ARRIVEE) {
                long fin = System.nanoTime();
                retracerChemin(parent, pos);
                System.out.println("Solution avec BFS :");
                afficherLabyrintheTextuel();
                statsLabel.setText("BFS - Étapes : " + etapes + ", Temps : " + (fin - debut) / 1_000_000.0 + " ms");
                return;
            }

            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1];
                if (nx > 0 && ny > 0 && nx < TAILLE - 1 && ny < TAILLE - 1 && !visite[ny][nx] && labyrinthe[ny][nx] != MUR) {
                    queue.add(new Point(nx, ny));
                    parent.put(new Point(nx, ny), pos);
                    visite[ny][nx] = true;
                    if (labyrinthe[ny][nx] != DEPART && labyrinthe[ny][nx] != ARRIVEE) {
                        labyrinthe[ny][nx] = VISITE_TEMP;
                    }
                    repaint();
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
        }
        statsLabel.setText("BFS - Aucun chemin trouvé.");
    }

    public void resoudreAvecDFS() {
        reinitialiserSolution();
        boolean[][] visite = new boolean[TAILLE][TAILLE];
        Stack<int[]> stack = new Stack<>();
        Map<Point, Point> parent = new HashMap<>();
        stack.push(new int[]{1, 1});
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int etapes = 0;
        long debut = System.nanoTime();

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0], y = pos[1];
            etapes++;

            if (labyrinthe[y][x] == ARRIVEE) {
                long fin = System.nanoTime();
                retracerChemin(parent, new Point(x, y));
                System.out.println("Solution avec DFS :");
                afficherLabyrintheTextuel();
                statsLabel.setText("DFS - Étapes : " + etapes + ", Temps : " + (fin - debut) / 1_000_000.0 + " ms");
                return;
            }

            if (!visite[y][x]) {
                visite[y][x] = true;
                for (int[] dir : directions) {
                    int nx = x + dir[0], ny = y + dir[1];
                    if (nx > 0 && ny > 0 && nx < TAILLE - 1 && ny < TAILLE - 1 && !visite[ny][nx] && labyrinthe[ny][nx] != MUR) {
                        stack.push(new int[]{nx, ny});
                        parent.put(new Point(nx, ny), new Point(x, y));
                        if (labyrinthe[ny][nx] != DEPART && labyrinthe[ny][nx] != ARRIVEE) {
                            labyrinthe[ny][nx] = VISITE_TEMP;
                        }
                        repaint();
                        try { Thread.sleep(50); } catch (InterruptedException e) {}
                    }
                }
            }
        }
        statsLabel.setText("DFS - Aucun chemin trouvé.");
    }

    private void retracerChemin(Map<Point, Point> parent, Point fin) {
        Point p = fin;
        while (p != null) {
            if (labyrinthe[p.y][p.x] != DEPART && labyrinthe[p.y][p.x] != ARRIVEE) {
                labyrinthe[p.y][p.x] = CHEMIN_SOLUTION;
            }
            p = parent.get(p);
        }
        repaint();
    }
}