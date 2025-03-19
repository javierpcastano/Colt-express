package Colt.IG;

import javax.swing.*;


/**
 * Classe pour une zone de texte modifiable.
 */
public class Texte extends JLabel {


    /**
     * Constructeur paramétré par un texte initial.
     *
     * @param texte Texte initial
     */
    public Texte(String texte) {
        super(texte);
    }

    /**
     * Méthode pour la modification du texte.
     *
     * @param texte Nouveau texte
     */
    public void changeTexte(String texte) {
        this.setText(texte);
        this.repaint();
    }

}