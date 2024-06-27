package game ; 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MotsCaches extends JFrame {
    private static final long serialVersionUID = -1384767520433009233L;
	private static final int TAILLE_GRILLE = 20;
    private char[][] grille = new char[TAILLE_GRILLE][TAILLE_GRILLE];
    private List<String> mots = new ArrayList<>();
    private Set<Point> lettresSelectionnees = new HashSet<>();
    private JPanel grillePanel = new JPanel(new GridLayout(TAILLE_GRILLE, TAILLE_GRILLE));
    private JList<String> listeMots;
    private DefaultListModel<String> listeModel;
    private Set<String> motsTrouves = new HashSet<>();
    private boolean isResetting = false;
    
    public MotsCaches() {
        setTitle("Jeu de Mots Cachés");  
        setSize(1920, 1080); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
        	  @Override
        	  public void keyPressed(KeyEvent e) {
        	    if (!isResetting && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	      try {
        	        resetGrille();
        	      } catch (IOException ioException) {
        	        ioException.printStackTrace();
        	      }
        	    }
        	  }
        	});

        try {
            mots = GenerateurMots.genererMotsAleatoires();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des mots. Veuillez réessayer.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        initialiserGrille();
        ajouterMots();
        remplirEspacesVides();
        afficherGrille();

        listeModel = new DefaultListModel<>();
        mots.forEach(mot -> listeModel.addElement(mot));
        listeMots = new JList<>(listeModel);
        add(new JScrollPane(listeMots), BorderLayout.EAST);
    }

    private void resetGrille() throws IOException {
        if (isResetting) {
            System.out.println("Réinitialisation déjà en cours, attente...");
            return;
        }
        verifierMotTrouve();
        isResetting = true;
        System.out.println("Réinitialisation de la grille...");
        try {
            mots = GenerateurMots.genererMotsAleatoires();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des mots. Veuillez réessayer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            isResetting = false;
            return;
        }
        lettresSelectionnees.clear();
        remplirEspacesVides();
        listeModel.clear();
        mots.forEach(mot -> listeModel.addElement(mot));
        afficherGrille();
        isResetting = false;
        System.out.println("Grille réinitialisée.");
    }



    private void initialiserGrille() {
        for (int i = 0; i < TAILLE_GRILLE; i++) {
            for (int j = 0; j < TAILLE_GRILLE; j++) {
                grille[i][j] = ' ';
            }
        }
    }

    private void ajouterMots() {
        if (mots == null || mots.isEmpty()) return;

        Random rand = new Random();
        for (String mot : mots) {
            boolean place = false;
            while (!place) {
                int direction = rand.nextInt(3);
                int row = rand.nextInt(TAILLE_GRILLE);
                int col = rand.nextInt(TAILLE_GRILLE);
                mot = mot.toUpperCase();
                if (direction == 0 && col + mot.length() <= TAILLE_GRILLE) {
                    if (peutPlacerMot(mot, row, col, 0, 1)) {
                        placerMot(mot, row, col, 0, 1);
                        place = true;
                    }
                } else if (direction == 1 && row + mot.length() <= TAILLE_GRILLE) {
                    if (peutPlacerMot(mot, row, col, 1, 0)) {
                        placerMot(mot, row, col, 1, 0);
                        place = true;
                    }
                } else if (direction == 2 && row + mot.length() <= TAILLE_GRILLE && col + mot.length() <= TAILLE_GRILLE) {
                    if (peutPlacerMot(mot, row, col, 1, 1)) {
                        placerMot(mot, row, col, 1, 1);
                        place = true;
                    }
                }
            }
        }
    }

    private boolean peutPlacerMot(String mot, int row, int col, int rowInc, int colInc) {
        for (int k = 0; k < mot.length(); k++) {
            if (grille[row + k * rowInc][col + k * colInc] != ' ' && grille[row + k * rowInc][col + k * colInc] != mot.charAt(k)) {
                return false;
            }
        }
        return true;
    }

    private void placerMot(String mot, int row, int col, int rowInc, int colInc) {
        for (int k = 0; k < mot.length(); k++) {
            grille[row + k * rowInc][col + k * colInc] = mot.charAt(k);
        }
    }

    private void remplirEspacesVides() {
        Random rand = new Random();
        for (int i = 0; i < TAILLE_GRILLE; i++) {
            for (int j = 0; j < TAILLE_GRILLE; j++) {
                if (grille[i][j] == ' ') {
                    grille[i][j] = (char) ('A' + rand.nextInt(26));
                }
            }
        }
    }

    private void afficherGrille() {
        grillePanel.removeAll();
        for (int i = 0; i < TAILLE_GRILLE; i++) {
            for (int j = 0; j < TAILLE_GRILLE; j++) {
                CelluleBouton bouton = new CelluleBouton(grille[i][j], i, j, this);
                bouton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (isResetting) {
                            System.out.println("Événement de la souris ignoré pendant la réinitialisation.");
                            return;
                        }
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            bouton.setBackground(Color.YELLOW);
                            lettresSelectionnees.add(new Point(bouton.getRow(), bouton.getCol()));
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            bouton.setBackground(null);
                            lettresSelectionnees.remove(new Point(bouton.getRow(), bouton.getCol()));
                        }
                        try {
                            verifierMotTrouve();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                grillePanel.add(bouton);
            }
        }
        add(grillePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

//    private boolean sontTousMotsTrouves() {
//        return motsTrouves.size() == mots.size();
//    }

    private void verifierMotTrouve() throws IOException {
    	  if (isResetting) {
    	    System.out.println("Vérification du mot ignorée pendant la réinitialisation.");
    	    return;
    	  }

    	  StringBuilder sb = new StringBuilder();
    	  for (Point point : lettresSelectionnees) {
    	    sb.append(grille[point.x][point.y]);
    	  }
    	  String sequence = sb.toString();

    	  boolean wordFound = false;
    	  for (String mot : mots) {
    	    if (mot.equals(sequence)) {
    	      motsTrouves.add(mot);
    	      wordFound = true;
    	      lettresSelectionnees.clear(); 
    	      mettreAJourListeMots();
    	      break;
    	    }
    	  }
    	  if (!wordFound) {
    	    lettresSelectionnees.clear(); 
    	  }
    	  mettreAJourListeMots();
    	}

    private void mettreAJourListeMots() {
        listeModel.clear();
        for (String mot : mots) {
            if (motsTrouves.contains(mot)) {
                listeModel.addElement("[X] " + mot);
            } else {
                listeModel.addElement(mot);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MotsCaches frame = new MotsCaches();  
            frame.setVisible(true);
        });
    } 
 
    public void handleLeftClick(CelluleBouton celluleBouton) {
    	  if (isResetting) {
    	    System.out.println("Événement de la souris ignoré pendant la réinitialisation.");
    	    return;
    	  }
    	  lettresSelectionnees.add(new Point(celluleBouton.getRow(), celluleBouton.getCol()));
    	  celluleBouton.setBackground(Color.RED);
    	}

    	public void handleRightClick(CelluleBouton celluleBouton) {
    	  if (isResetting) {
    	    System.out.println("Événement de la souris ignoré pendant la réinitialisation.");
    	    return;
    	  }
    	  lettresSelectionnees.remove(new Point(celluleBouton.getRow(), celluleBouton.getCol()));
    	  celluleBouton.setBackground(null); 
    	}

    public boolean isResetting() {
        return isResetting;
    }
    
}



