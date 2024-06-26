package game;

import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SwingUtilities;


public class CelluleBouton extends JButton {
    private static final long serialVersionUID = -7090871842041181053L;
	 private final int row;
    private final int col;
    private final MotsCaches game;

    public CelluleBouton(char lettre, int row, int col, MotsCaches game) {
        super(String.valueOf(lettre).toUpperCase());
        this.row = row;
        this.col = col;
        this.game = game;
    }

    public int getRow() {
        return row; 
    }
 
    public int getCol() {
        return col;
    }  
    
    public void mousePressed(MouseEvent mouseEvent) {
        if (game.isResetting()) {
            System.out.println("Événement de la souris ignoré pendant la réinitialisation.");
            return;
        }
        if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
            game.handleLeftClick(this);
        } else if (SwingUtilities.isRightMouseButton(mouseEvent)) {
            game.handleRightClick(this);
        }
    }
}
