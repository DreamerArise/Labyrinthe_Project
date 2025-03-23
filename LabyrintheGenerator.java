import javax.swing.*; //Fournit les composants Swing (JFrame, JPanel, JButton, etc.) pour l'interface graphique
import java.awt.*;  //Fournit les classes pour le dessin graphique (Graphics, Color) et la gestion des evenements (MouseEvent, KeyEvent)
import java.awt.event.*; //Fournit les ecouteurs d'evenements (MouseAdapter, KeyAdapter) pour gerer les interactions utilisateur
import java.io.*; //Fournit les classes pour la lecture/ecriture de fichiers (File, BufferedReader, FileReader)
import java.util.*; //Fournit les classes pour la lecture de fichiers (BufferedReader, FileReader)
import java.util.List; //Fournit les structures de donnees (List, Queue, Stack, Map, Random) pour les algorithmes
import java.util.Queue; //Importations specifiques pour les interfaces List et Queue utilisees dans les algorithmes

public class LabyrintheGenerator extends JPanel {

                   /*INITIALISATION DES VARIABLES */
    private static final int TAILLE = 15; //taille grille 15x15
    private static final int CELL_SIZE = 30; //taille cellule 30x30 pour l'affichage graphique
    private static final char MUR = '#'; //caractere pour les murs
    private static final char CHEMIN = '='; //caractere pour les chemins
    private static final char DEPART = 'S'; //caractere pour le depart
    private static final char ARRIVEE = 'E'; //caractere pour l'arrivee
    private static final char CHEMIN_SOLUTION = '+'; //caractere pour le chemin solution
    private static final char VISITE_TEMP = 'V'; //caractere pour les cases visitees temporairement
    private static final char JOUEUR = 'P'; // Représentation du joueur
    private char[][] labyrinthe;
    private boolean modeDessin = false; // Mode pour dessiner manuellement il suffit de cliquer sur les cases baches ou noires pour ajouter des murs ou passages tu peux reconstituer le labyrinthe toi meme
    private boolean modeJoueur = false; // Mode pour résoudre manuellement
    private Point positionJoueur; // Position actuelle du joueur
    private JLabel statsLabel; // Label pour afficher les statistiques pour la comparaison des performances des algos BFS et DFS

                      /* CONSTRUCTEUR DE LA CLASSE */

    public LabyrintheGenerator() {
        genererLabyrintheAvecPrim(); // Generer un labyrinthe initial
        // Ajout d'un écouteur pour le clic de souris c'est pour le mode dessin
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (modeDessin) {
                    int col = e.getX() / CELL_SIZE; //Pour calculer la colonne cliquee
                    int row = e.getY() / CELL_SIZE; //Pour calculer la ligne cliquee
                    // Verifier si les coordonnees sont valides
                    if (col < TAILLE && row < TAILLE) {
                        // Inverser la case (mur -> chemin, chemin -> mur)
                        labyrinthe[row][col] = (labyrinthe[row][col] == MUR) ? CHEMIN : MUR;
                        repaint(); // Redessiner le labyrinthe
                    }
                }
            }
        });

        // Ajout d'un écouteur pour les touches flechees
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (modeJoueur) {
                    deplacerJoueur(e.getKeyCode()); // Deplacer le joueur selon la touche pressee
                }
            }
        });
        setFocusable(true); // Permettre au panneau de recevoir les evenements clavier
    }

                  /* Méthode principale pour lancer l'application */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Labyrinthe Solver"); // Creer une fenetre JFrame la fenetre principale
            LabyrintheGenerator panel = new LabyrintheGenerator(); // Creer un panneau pour le labyrinthe
            frame.setLayout(new BorderLayout()); //
            frame.add(panel, BorderLayout.CENTER);

            // Creer un panneau pour les boutons 
            JPanel buttonPanel = new JPanel();
            JButton btnBFS = new JButton("Résoudre avec BFS");
            JButton btnDFS = new JButton("Résoudre avec DFS");
            JButton btnGenerer = new JButton("Générer un labyrinthe");
            JButton btnReinitialiser = new JButton("Réinitialiser");
            JButton btnDessin = new JButton("Mode dessin");
            JButton btnCharger = new JButton("Charger un labyrinthe");
            JButton btnJoueur = new JButton("Résoudre moi-même");

            // Ajouter des ecouteurs pour les boutons
            btnBFS.addActionListener(e -> panel.resoudreAvecBFS());
            btnDFS.addActionListener(e -> panel.resoudreAvecDFS());
            btnGenerer.addActionListener(e -> panel.genererLabyrintheAvecPrim());
            btnReinitialiser.addActionListener(e -> panel.reinitialiserLabyrinthe());
            // Activer/désactiver le mode dessin
            btnDessin.addActionListener(e -> {
                panel.modeDessin = !panel.modeDessin;
                panel.modeJoueur = false; // Desactiver le mode joueur si mode dessin active
            });
            btnCharger.addActionListener(e -> panel.chargerLabyrinthe()); // Charger un labyrinthe depuis un fichier
            btnJoueur.addActionListener(e -> panel.activerModeJoueur()); // Activer le mode joueur

            // Ajouter les boutons au panneau
            buttonPanel.add(btnBFS);
            buttonPanel.add(btnDFS);
            buttonPanel.add(btnGenerer);
            buttonPanel.add(btnReinitialiser);
            buttonPanel.add(btnDessin);
            buttonPanel.add(btnCharger);
            buttonPanel.add(btnJoueur);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            panel.statsLabel = new JLabel("Statistiques : ");// Ajouter un label pour les statistiques
            frame.add(panel.statsLabel, BorderLayout.NORTH); 

            frame.setSize(TAILLE * CELL_SIZE + 20, TAILLE * CELL_SIZE + 150); // Taille de la fenetre
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fermer l'application lors de la fermeture de la fenetre
            frame.setVisible(true); // Rendre la fenetre visible
        });
    }

    /*
      GENERER UN LABYRINTHE ALEAToIRE EN UTILISANT L'ALGORITHMA PRIM.
      Fonctionnement :
      - Initialise une grille pleine de murs.
      - Commence a la position (1,1) et marque cette cellule comme un passage.
      - Utilise une liste pour stocker les murs adjacents aux passages.
      - Tant qu'il y a des murs dans la liste :
        - Choisit un mur au hasard.
        - Explore les directions (haut, bas, gauche, droite) à une distance de 2 cellules.
        - Si la cellule cible est un mur, cree un passage entre le mur choisi et la cellule cible.
        - Ajoute la nouvelle cellule a la liste des murs.
      - Place le départ (S) à (1,1) et l'arrivee (E) à (TAILLE-2, TAILLE-2).
      - Affiche le labyrinthe dans la console et rafraîchit l'affichage graphique.
    */
    private void genererLabyrintheAvecPrim() {
        labyrinthe = new char[TAILLE][TAILLE];
        for (char[] row : labyrinthe) Arrays.fill(row, MUR); // Initialiser la grille pleine de murs
        Random rand = new Random(); // Gnrateur de nombres aléatoires
        List<int[]> murs = new ArrayList<>(); // Liste pour stocker les murs adjacents aux passages
        int x = 1, y = 1; // Position initiale
        labyrinthe[y][x] = CHEMIN; // Marquer la cellule de départ comme un passage
        murs.add(new int[]{x, y}); // Ajouter les murs adjacents à la cellule de départ

        // Tant qu'il y a des murs dans la liste
        while (!murs.isEmpty()) {
            int[] mur = murs.remove(rand.nextInt(murs.size())); // Choisir un mur au hasard
            int cx = mur[0], cy = mur[1]; 
            int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}}; // Directions (haut, bas, gauche, droite)
            Collections.shuffle(Arrays.asList(directions)); // Melanger les directions

            // Explorer les directions à une distance de 2 cellules
            for (int[] dir : directions) {
                int nx = cx + dir[0], ny = cy + dir[1]; // Nouvelles coordonnées

                // Si la cellule cible est un mur, creer un passage
                if (nx > 0 && ny > 0 && nx < TAILLE - 1 && ny < TAILLE - 1 && labyrinthe[ny][nx] == MUR) {
                    labyrinthe[cy + dir[1] / 2][cx + dir[0] / 2] = CHEMIN; 
                    labyrinthe[ny][nx] = CHEMIN; // Marquer la nouvelle cellule comme un passage
                    murs.add(new int[]{nx, ny}); // Ajouter les murs adjacents à la nouvelle cellule
                }
            }
        }
        labyrinthe[1][1] = DEPART; // Placer le départ
        labyrinthe[TAILLE - 2][TAILLE - 2] = ARRIVEE; // Placer l'arrivée
        modeJoueur = false; // Desactiver le mode joueur lors de la generation
        System.out.println("Labyrinthe initial :");
        afficherLabyrintheTextuel(); // Afficher le labyrinthe dans la console
        repaint(); // Rafraîchir l'affichage graphique
    }

    // Reinitialiser le labyrinthe (effacer la solution sans generer un nouveau)
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

    // Charger un labyrinthe depuis un fichier (notre principal probleme se trouve icic bien que le message d'erreur est affiche il y'a un bug qui fait planter l'application)
    private void chargerLabyrinthe() {
        JFileChooser fileChooser = new JFileChooser(); // Ouvrir une boîte de dialogue pour choisir un fichier

        // Filtre pour les fichiers texte
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile(); // Obtenir le fichier sélectionné

            // Lire le fichier et charger le labyrinthe
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                labyrinthe = new char[TAILLE][TAILLE]; // Initialiser le labyrinthe
                for (int i = 0; i < TAILLE; i++) {
                    String line = reader.readLine();
                    if (line == null || line.length() < TAILLE) {
                        throw new IOException("Fichier invalide : taille incorrecte"); // Gérer les erreurs de lecture 
                    }
                    for (int j = 0; j < TAILLE; j++) {
                        labyrinthe[i][j] = line.charAt(j); // Charger le labyrinthe depuis le fichier
                    }
                }
                modeJoueur = false; // Désactiver le mode joueur lors du chargement
                System.out.println("Labyrinthe chargé :");
                afficherLabyrintheTextuel();
                repaint();

                // Vérifier si le labyrinthe contient le départ et l'arrivée
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors du chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); // Afficher un message d'erreur
            }
        }
    }

    // Activer le mode joueur pour résoudre manuellement
    private void activerModeJoueur() {
        reinitialiserLabyrinthe(); // Réinitialiser le labyrinthe
        modeJoueur = true; // Activer le mode joueur
        modeDessin = false; // Désactiver le mode dessin
        positionJoueur = new Point(1, 1); // Position initiale du joueur c-a-d au depart
        labyrinthe[1][1] = JOUEUR;  //Placer le joueur dansla matrice
        statsLabel.setText("Utilisez les touches fléchées pour vous déplacer !");
        repaint();
        requestFocusInWindow(); // S'assurer que le panneau peut recevoir les evenements clavier
    }

    // Deplacer le joueur avec les touches flechees
    private void deplacerJoueur(int keyCode) {
        if (!modeJoueur) return; // Ignorer les touches si le mode joueur est désactivé

        int dx = 0, dy = 0; // Déplacement horizontal et vertical
        switch (keyCode) {
            case KeyEvent.VK_UP: dy = -1; break; // Déplacement vers le haut
            case KeyEvent.VK_DOWN: dy = 1; break; // Déplacement vers le bas
            case KeyEvent.VK_LEFT: dx = -1; break; // Déplacement vers la gauche
            case KeyEvent.VK_RIGHT: dx = 1; break; // Déplacement vers la droite
            default: return; // Ignorer les autres touches
        }

        // Calculer les nouvelles coordonnées
        int newX = positionJoueur.x + dx;  
        int newY = positionJoueur.y + dy; 

        // Vérifier si le deplacement est valide
        if (newX >= 0 && newX < TAILLE && newY >= 0 && newY < TAILLE && labyrinthe[newY][newX] != MUR) {
            // Restaurer la case actuelle (sauf si c'est l'arrivee)
            if (labyrinthe[positionJoueur.y][positionJoueur.x] != ARRIVEE) {
                labyrinthe[positionJoueur.y][positionJoueur.x] = CHEMIN;
            }
            // Mettre à jour la position du joueur
            positionJoueur.setLocation(newX, newY);
            // Verifier si le joueur a atteint l'arrive
            if (labyrinthe[newY][newX] == ARRIVEE) {
                modeJoueur = false;
                statsLabel.setText("Félicitations ! Vous avez atteint l'arrivée !"); // Afficher un message de victoire dans le label stats
                JOptionPane.showMessageDialog(this, "Vous avez gagné !", "Victoire", JOptionPane.INFORMATION_MESSAGE); // Afficher un message de victoire
            } else {
                labyrinthe[newY][newX] = JOUEUR; // Mettre à jour la position du joueur
            }
            repaint(); 
        }
    }

    @Override
    // Redessiner le labyrinthe
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Appeler la méthode paintComponent de la classe parent
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                switch (labyrinthe[i][j]) {
                    case MUR: // Afficher les murs en noir
                        g.setColor(Color.BLACK); 
                        break;
                    case CHEMIN_SOLUTION: // Afficher le chemin solution en bleu
                        g.setColor(Color.GREEN); 
                        break;
                    case DEPART: // Afficher le départ en bleu
                        g.setColor(Color.BLUE);
                        break;
                    case ARRIVEE: // Afficher l'arrive en rouge
                        g.setColor(Color.RED);
                        break;
                    case VISITE_TEMP: // Afficher les cases visitees temporairement en jaune
                        g.setColor(Color.YELLOW);
                        break;
                    case JOUEUR: // Afficher le joueur en orange
                        g.setColor(Color.ORANGE); 
                        break;
                    default: // Afficher les chemins en blanc
                        g.setColor(Color.WHITE);
                        break;
                }

                // Dessiner un rectangle pour chaque case pour l'affichage graphique
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE); // Dessiner un rectangle
                g.setColor(Color.GRAY); // Couleur des lignes
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE); // Dessiner un contour
            }
        }
    }

        // Reinitialiser la solution (effacer le chemin solution)
    private void reinitialiserSolution() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (labyrinthe[i][j] == CHEMIN_SOLUTION || labyrinthe[i][j] == VISITE_TEMP || labyrinthe[i][j] == JOUEUR) {
                    labyrinthe[i][j] = CHEMIN; 
                }
            }
        }
    }
        // Afficher le labyrinthe dans la console
    private void afficherLabyrintheTextuel() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                System.out.print(labyrinthe[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    // Resoudre le labyrinthe avec l'algorithme BFS (Breadth-First Search)
    public void resoudreAvecBFS() {
        reinitialiserSolution(); // Reinitialiser la solution
        boolean[][] visite = new boolean[TAILLE][TAILLE]; // Tableau pour marquer les cases visitees
        Map<Point, Point> parent = new HashMap<>(); // Tableau pour stocker les parents des cases visitees
        Queue<Point> queue = new LinkedList<>(); // File pour parcourir les cases
        queue.add(new Point(1, 1)); // Ajouter la case de départ à la file
        visite[1][1] = true; // Marquer la case de départ comme visitee
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}; // Directions (haut, bas, gauche, droite)
        int etapes = 0; // Compteur d'etapes (cases visitees) pour les statistiques
        long debut = System.nanoTime(); // Temps de debut de l'algorithme BFS (en nanosecondes) pour les statistiques 

        // Parcourir les cases jusqu'à ce que la file soit vide
        while (!queue.isEmpty()) {
            Point pos = queue.poll(); // Obtenir la case actuelle de la file
            int x = pos.x, y = pos.y; // Coordonnees de la case actuelle
            etapes++; // Incrementer le compteur d'etapes

            // Verifier si on a atteint l'arrivee
            if (labyrinthe[y][x] == ARRIVEE) {
                long fin = System.nanoTime(); // Temps de fin de l'algorithme BFS (en nanosecondes) pour les statistiques
                retracerChemin(parent, pos); // Retracer le chemin solution
                System.out.println("Solution avec BFS :");
                afficherLabyrintheTextuel(); // Afficher le labyrinthe dans la console
                statsLabel.setText("BFS - Étapes : " + etapes + ", Temps : " + (fin - debut) / 1_000_000.0 + " ms"); // Afficher les statistiques dans le label stats
                return;
            }

            // Parcourir les directions (haut, bas, gauche, droite)
            for (int[] dir : directions) {
                int nx = x + dir[0], ny = y + dir[1]; // Nouvelles coordonnees

                // Verifier si la case est valide et non visitee
                if (nx > 0 && ny > 0 && nx < TAILLE - 1 && ny < TAILLE - 1 && !visite[ny][nx] && labyrinthe[ny][nx] != MUR) {
                    queue.add(new Point(nx, ny)); 
                    parent.put(new Point(nx, ny), pos); 
                    visite[ny][nx] = true; 
                    if (labyrinthe[ny][nx] != DEPART && labyrinthe[ny][nx] != ARRIVEE) {
                        labyrinthe[ny][nx] = VISITE_TEMP; 
                    }
                    repaint();
                    // Ajouter un delai pour l'affichage graphique
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
        }
        // Afficher un message si aucun chemin n'est trouve
        statsLabel.setText("BFS - Aucun chemin trouve.");
    }

    // Resoudre le labyrinthe avec l'algorithme DFS (Depth-First Search)
    public void resoudreAvecDFS() {
        reinitialiserSolution(); // Reinitialiser la solution
        boolean[][] visite = new boolean[TAILLE][TAILLE]; 
        Stack<int[]> stack = new Stack<>(); // Pile pour parcourir les cases
        Map<Point, Point> parent = new HashMap<>(); // Tableau pour stocker les parents des cases visitees
        stack.push(new int[]{1, 1}); // Ajouter la case de départ à la pile
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int etapes = 0;
        long debut = System.nanoTime();

        while (!stack.isEmpty()) {
            int[] pos = stack.pop(); // Obtenir la case actuelle de la pile
            int x = pos[0], y = pos[1]; // Coordonnees de la case actuelle
            etapes++;

            // Verifier si on a atteint l'arrivee
            if (labyrinthe[y][x] == ARRIVEE) {
                long fin = System.nanoTime(); // Temps de fin de l'algorithme DFS (en nanosecondes) pour les statistiques
                retracerChemin(parent, new Point(x, y)); // Retracer le chemin solution
                System.out.println("Solution avec DFS :");
                afficherLabyrintheTextuel();
                statsLabel.setText("DFS - Étapes : " + etapes + ", Temps : " + (fin - debut) / 1_000_000.0 + " ms");
                return;
            }

                // Parcourir les directions (haut, bas, gauche, droite)
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

    // Retracer le chemin solution à partir de l'arrivee jusqu'au depart
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