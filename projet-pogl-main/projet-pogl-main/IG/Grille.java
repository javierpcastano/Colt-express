package Colt.IG;



import javax.swing.*;
import java.awt.*;

/**
 * Classe pour un ensemble d'éléments graphiques disposés en grille.
 */
public class Grille extends JPanel {

    /**
     * Constructeur, prend en paramètres les dimensions de la grille.
     *
     * @param hauteur Nombre de lignes de la grille.
     * @param largeur Nombre de colonnes de la grille.
     */
    public Grille (int hauteur, int largeur) {
        setLayout(new GridLayout(hauteur, largeur, 5, 5));
    }

    /**
     * Ajouter un élément dans la grille.
     *
     * @param element Élément à ajouter.
     */
    public void ajouteElement (JComponent element) {
        this.add(element);
    }

}
