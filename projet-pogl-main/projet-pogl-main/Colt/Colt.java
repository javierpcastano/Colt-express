package Colt.Colt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;



/** Class énumerée utilisée pour garantir les directions des déplacements/tirs */
enum Direction {
    AVANT, ARRIERE, HAUT, BAS
}

/** Class énumerée utilisée pour reconnaître le êtat du jeu */
enum EtatJeu {
    PLANIFICATION, ACTION
}

/**
 * Interface des objets observateurs.
 */
interface Observer {
    /**
     * Un observateur doit posséder une méthode [update] déclenchant la mise à
     * jour.
     */
    public void update();
}

/**
 * Classe des objets pouvant être observés.
 */
abstract class Observable {
    private ArrayList<Observer> observers;
    public Observable() {
        this.observers = new ArrayList<Observer>();
    }
    public void addObserver(Observer o) {
        observers.add(o);
    }
    public void notifyObservers() {
        for(Observer o : observers) {
            o.update();
        }
    }
}
/** Fin du schéma observateur/observé. */


/** Ici on a la classe principale du programme */
public class Colt {

    public static void main(String[] args) {
        Modele modele = ConfigurationJeu.configurerJeu();
        if (modele != null) {
            Vue vue = new Vue(modele);
        } else {
            System.out.println("L'utilisateur a annulé la configuration.");
        }
    }
}
/** Fin de la classe principale. */


class Modele extends Observable {
    public static final int NB_WAGONS = 4;
    public static final int NB_ACTIONS = 4;
    public static final int NB_BANDITS = 2;
    public static final double NERVOSITE_MARSHALL = 0.3;
    public static final int NB_BALLES = 6;
    public static final int NB_MAX_TOURS = 10;

    public static final int NB_MAX_OBJETS = 4;

    /** Parametres pour fenetre graphique
     * C'est pour la fonctionnalité supplémentaire */
    private int nbWagons;
    private int nbActions;
    private int nbBandits;
    private int nbBalles;

    private int nbTour;

    /** Pour savoir quand le jeu doit finir */
    private int maxTour;

    /** Fin partie fonctionnalité supplémentaire */
    private ArrayList<Wagon> train;
    private ArrayList<Bandit> bandits;
    private Marshall marshall;

    /** Pour savoir à chaque fois dans quel etat du jeu on est */
    private EtatJeu etatActuel;




    /** Construction : on initialise les paramètres, le train, les bandits et le marshall. */
    public Modele(int nbwagons, int nbactions, int nbbandits, int nbballes, double nervosite, int maxTour, int nb_objets, ArrayList<String> nomBandits) {
        this.nbTour = 0;
        this.nbWagons = nbwagons;
        this.nbActions = nbactions;
        this.nbBandits = nbbandits;
        this.nbBalles = nbballes;
        this.maxTour = maxTour;
        this.marshall = new Marshall(nbWagons-1, "Marshall", nervosite);

        /** Initialise la liste de bandits avec les noms ajoutés dans les paramètres */
        this.bandits = new ArrayList<>();
        for (String nom: nomBandits) {
            bandits.add(new Bandit(nom, nbBalles));
        }

        this.etatActuel = EtatJeu.PLANIFICATION;

        /** Initialise les wagons du train en fonction des paramètres */
        this.train = new ArrayList<>(nbWagons);
        for (int i = 0; i < nbWagons; i++) {
            train.add(new Wagon(this, (i == nbWagons-1), (i == 0), this.bandits, i, nb_objets));
        }

    }


    /** Getters pour acceder aux attributs du modele */

    public ArrayList<Wagon> getTrain() {
        return train;
    }

    public ArrayList<Bandit> getBandits() {
        return bandits;
    }

    public Marshall getMarshall() {
        return marshall;
    }

    public EtatJeu getEtatActuel() {
        return etatActuel;
    }

    public int getNbTour() {return nbTour;}

    /** Méthodes qui renvoient booleans */

    public boolean planificationFini() {
        for (Bandit b : bandits) {
            if (b.getActions().size() < nbActions) {
                return false;
            }
        }
        return true;
    }

    public Boolean actionsFull(Bandit bandit) {
        return bandit.getActions().size() == nbActions;
    }

    /** Version "avancée" de bandit.addAction, utilisée pour prendre en compte le état du jeu*/
    public void addActionBandit(Bandit bandit, Action a) {
        bandit.addAction(a);
        System.out.println("Action ajoutée à " + bandit.getNom());
        if (planificationFini()) {
            etatActuel = EtatJeu.ACTION;
        }
        notifyObservers();
    }

    /** Methodes faites pour manipuler les actions des bandits/marshall*/

    /** Methode utilisée pour le deplacement des bandits */
    public void deplacer(Bandit bandit, Direction dir) {
        int currentIndex = bandit.getPos();
        Wagon wagon = train.get(currentIndex);
        switch (dir) {
            case ARRIERE:
                if (currentIndex > 0) {
                    Wagon wagonArriere = train.get(currentIndex-1);
                    bandit.setPos(currentIndex-1);
                    wagon.enleverBandit(bandit);
                    wagonArriere.ajouterBandit(bandit);
                    System.out.println(bandit.getNom() + " se deplace vers la gauche");
                } else {
                    System.out.println("Impossible, " + bandit.getNom() + " est deja dans le premier wagon.");
                }
                break;
            case AVANT:
                if (currentIndex < nbWagons-1) {
                    Wagon wagonAvant = train.get(currentIndex+1);
                    bandit.setPos(currentIndex+1);
                    wagon.enleverBandit(bandit);
                    wagonAvant.ajouterBandit(bandit);
                    System.out.println(bandit.getNom() + " se deplace vers la droite");
                    //System.out.println("Wagon " + wagonAvant.getIndex() + " contient les bandits: " + wagonAvant.getBandits());
                    //System.out.println("Wagon " + wagon.getIndex() + " contient les bandits: " + wagon.getBandits());
                    //System.out.println(bandit.getNom() + " se trouve dans le Wagon " + bandit.getPos());
                } else {
                    System.out.println("Impossible, " + bandit.getNom() + " est deja dans le dernier wagon.");
                }
                break;
            case HAUT:
                if (!bandit.getToit()) {
                    bandit.setToit(true);
                    System.out.println(bandit.getNom() + " grimpe sur le toit");
                } else {
                    System.out.println("Impossible, " + bandit.getNom() + " est deja dans le toit.");
                }
                break;
            case BAS:
                if (bandit.getToit()) {
                    bandit.setToit(false);
                    int n = bandit.getPos() + 1;
                    System.out.println(bandit.getNom() + " descend dans le wagon " + n);
                } else {
                    System.out.println("Impossible, " + bandit.getNom() + " est deja dans le wagon!");
                }
                break;
        }
    }

    /** Methode utilisée pour le braquage des bandits */
    public void braquer(Bandit bandit, Wagon wagon) {
        if (!bandit.getToit()) {
            if (!wagon.getButins().isEmpty()) {
                Random rand = new Random();
                // On genere un index random entre 0 et le num de butins dans le wagon
                int randomIdx = rand.nextInt(wagon.getButins().size());
                // On prend cette index dans la liste des butins du wagon
                Butin randomButin = wagon.getButins().get(randomIdx);
                wagon.enleverButin(randomButin);
                bandit.addButin(randomButin);
                System.out.println(bandit.getNom() + " a braqué " + randomButin.getNom() + " de " + randomButin.getVal() + "€");
            } else {
                int n = wagon.getIndex() + 1;
                System.out.println("Le wagon " + n + " n'a plus de butin.");
            }
        } else { // le bandit est dans le toit
            System.out.println(bandit.getNom() + " ne peut pas braquer car il est dans le toit.");
        }
    }

    /** Methode utilisée pour le deplacement du Marshall */
    public void deplacerMarshall() {
        int currentIndex = marshall.getPos();
        Wagon currentWagon = train.get(currentIndex);
        Direction dir;
        Random random = new Random();
        if (random.nextDouble() < marshall.getNervosite()) { // Il se deplace
            // Il est forcé d'aller avant
            if (marshall.estDansSpawn())
                dir = Direction.AVANT;
                // Il est forcé d'aller en arriere
            else if (marshall.getPos() == nbWagons-1)
                dir = Direction.ARRIERE;
                // S'il est au milieu, c'est random
            else {
                if (random.nextBoolean()) { // 50% chances d'aller vers chaque coté
                    dir = Direction.AVANT;
                } else {
                    dir = Direction.ARRIERE;
                }
            }
            switch (dir) {
                case AVANT:
                    Wagon wagonAvant = train.get(currentIndex+1);
                    marshall.setPos(currentIndex+1);
                    currentWagon.enleverMarshall();
                    wagonAvant.ajouterMarshall();
                    System.out.println("Le marshall se deplace vers la droite.");
                    break;
                case ARRIERE:
                    Wagon wagonArriere = train.get(currentIndex-1);
                    marshall.setPos(currentIndex-1);
                    currentWagon.enleverMarshall();
                    wagonArriere.ajouterMarshall();
                    System.out.println("Le marshall se deplace vers la gauche.");
                    break;
            }
        } else {
            // Il ne se deplace pas
            System.out.println("Le marshall reste immobile.");
        }
    }

    /** Methode utilisée pour que à fin de chaque tour, le Marshall tire sur les bandits dans son wagon (si possible) */
    public void tirerMarshall() {
        int marshallIndex = marshall.getPos();
        for (Bandit bandit: bandits)  {
            int banditIndex = bandit.getPos();
            if (marshallIndex == banditIndex && !bandit.getToit()) {
                Wagon currentWagon = train.get(banditIndex);
                bandit.setToit(true);
                if (!bandit.getLoot().isEmpty()) {
                    Butin randomButin = bandit.butinRandom();
                    bandit.enleverButin(randomButin);
                    currentWagon.ajouterButin(randomButin);
                    int n = banditIndex+1;
                    System.out.println(bandit.getNom() + " a été tiré dessus par le Marshall");
                    System.out.println(bandit.getNom() + " a laissé un(e) " + randomButin.getNom() + " dans le Wagon " + n + " et grimpé au toit");
                } else { //Si son loot est vide
                    System.out.println(bandit.getNom() + " a été tiré dessus par le Marshall et a grimpé au toit");
                }
            }
        }
    }

    /** Methode utilisée pour les tires entre bandits */
    public void tirerBandit(Bandit bandit, Direction dir) {
        if (bandit.getBullets() > 0) {
            int indexBandit = bandit.getPos();
            Wagon currentWagon = train.get(indexBandit);
            bandit.useBullet();
            switch (dir) {
                case AVANT:
                    if (indexBandit < nbWagons-1) {
                        int indexVise = bandit.getPos() + 1;
                        Wagon wagonAvant = train.get(indexVise);
                        Bandit brandom = wagonAvant.choisirBanditRandom(bandit);
                        if (brandom != null) { // Si il y a un bandit dans le wagon
                            if (!brandom.getLoot().isEmpty()) { // Si le bandit a de loot disponible
                                Butin butin = brandom.butinRandom();
                                brandom.enleverButin(butin);
                                wagonAvant.ajouterButin(butin);
                                System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " qui a laché un(e) " + butin.getNom() + " de " + butin.getVal() + "€.");
                            } else { // Le bandit n'a pas de loot
                                System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " qui n'avait pas de butin.");
                            }
                        } else {
                            int n = indexVise + 1;
                            System.out.println("Tir de " + bandit.getNom() + " raté, Wagon " + n + " n'a pas d'objectifs disponibles pour tirer");
                        }
                    } else {
                        System.out.println(bandit.getNom() + " a tiré dans le vide.");
                    }
                    break;
                case ARRIERE:
                    if (indexBandit > 0) {
                        int indexVise = bandit.getPos() - 1;
                        Wagon wagonArriere = train.get(indexVise);
                        Bandit brandom = wagonArriere.choisirBanditRandom(bandit);
                        if (brandom != null) { // Si on peut tirer sur un bandit dans le wagon
                            if (!brandom.getLoot().isEmpty()) { // Si le bandit a de loot disponible
                                Butin butin = brandom.butinRandom();
                                brandom.enleverButin(butin);
                                wagonArriere.ajouterButin(butin);
                                System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " qui a laché un(e) " + butin.getNom() + " de " + butin.getVal() + "€.");
                            } else { // Le bandit n'a pas de loot
                                System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " qui n'avait pas de butin.");
                            }
                        } else {
                            int n = indexVise + 1;
                            System.out.println("Tir de " + bandit.getNom() + " raté, Wagon " + n + " n'a pas d'objectifs disponibles pour tirer");
                        }
                    } else {
                        System.out.println(bandit.getNom() + " a tiré dans le vide.");
                    }
                    break;
                case HAUT: // Il tire sur le toit de son wagon
                    boolean change = false;
                    if (!bandit.getToit()) {// Il est a l'interieur
                        bandit.setToit(true); // on le met dans le toit pour qu'il tire, apres on le redescend
                        change = true;
                    }
                    Bandit brandom = currentWagon.choisirBanditRandom(bandit);
                    if (brandom != null) { // Si on peut tirer sur un bandit dans le wagon
                        if (!brandom.getLoot().isEmpty()) { // Si le bandit a de loot disponible
                            Butin butin = brandom.butinRandom();
                            brandom.enleverButin(butin);
                            currentWagon.ajouterButin(butin);
                            System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " dans le toit, qui a laché un(e) " + butin.getNom() + " de " + butin.getVal() + "€.");
                        } else { // Le bandit n'a pas de loot
                            System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " dans le toit, qui n'avait pas de butin.");
                        }
                    } else {
                        int n = bandit.getPos() + 1;
                        System.out.println("Tir de " + bandit.getNom() + " raté, toit de Wagon " + n + " n'a pas d'objectifs disponibles pour tirer");
                    }
                    if (change) {bandit.setToit(false);} // Si on a changé sa surface pour tirer, on la remet
                    break;
                case BAS:
                    change = false;
                    if (bandit.getToit()) {// Il est au toit
                        bandit.setToit(false); // on le met dans l'interieur pour qu'il tire, apres on le remonte
                        change = true;
                    }
                    brandom = currentWagon.choisirBanditRandom(bandit);
                    if (brandom != null) { // Si on peut tirer sur un bandit dans le wagon
                        if (!brandom.getLoot().isEmpty()) { // Si le bandit a de loot disponible
                            Butin butin = brandom.butinRandom();
                            brandom.enleverButin(butin);
                            currentWagon.ajouterButin(butin);
                            System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " dans le wagon, qui a laché un(e) " + butin.getNom() + " de " + butin.getVal() + "€.");
                        } else { // Le bandit n'a pas de loot
                            System.out.println(bandit.getNom() + " a tiré sur " + brandom.getNom() + " dans le wagon, qui n'avait pas de butin.");
                        }
                    } else {
                        int n = bandit.getPos() + 1;
                        System.out.println("Tir de " + bandit.getNom() + " raté, Wagon " + n + " n'a pas d'objectifs disponibles pour tirer");
                    }
                    if (change) {bandit.setToit(true);} // Si on a changé sa surface pour tirer, on la remet
                    break;
            }
        } else {
            System.out.println("Impossible tirer, " + bandit.getNom() + " n'a plus de balles restantes.");
        }
    }

    /** Methode utilisée pour executer chaque tour (lors de Action!) */
    public void executerActions() {
        nbTour++;
        ArrayList<Bandit> banditsCopie = new ArrayList<>(bandits);
        deplacerMarshall();
        for (int i=0; i< nbActions; i++) {
            for (Bandit bandit: banditsCopie) {
                ArrayList<Action> actionsBandit = bandit.getActions();
                if (i < actionsBandit.size()) {
                    Action action = actionsBandit.get(i);
                    action.executer();
                }
            }
        }
        /** Apres éxécuter les actions, on les retire */
        for (Bandit bandit: banditsCopie) {
            bandit.clearActions();
        }

        tirerMarshall(); // Le marshall tire si possible
        etatActuel = EtatJeu.PLANIFICATION;
        notifyObservers();
        if (nbTour == maxTour) {
            finirJeu();
        }
    }

    /** Methode pour finir le jeu une fois le nombre de tours maximale dépasé */
    private void finirJeu() {
        afficherGagnant();
        System.exit(0);
    }

    /** Méthode pour calculer/afficher les gagnants du match */
    private void afficherGagnant() {
        ArrayList<Bandit> gagnants = new ArrayList<>();
        int maxLoot = 0;

        /** On cherche le(s) bandit(s) avec le plus d'argent */
        for (Bandit bandit : bandits) {
            int lootActuel = bandit.calculLoot();
            if (lootActuel > maxLoot) {
                maxLoot = lootActuel;
                gagnants.clear();
                gagnants.add(bandit);
            } else if (lootActuel == maxLoot) { /** Pour le cas d'égalité */
                gagnants.add(bandit);
            }
        }

        /** On construit le message */
        String message;
        if (!gagnants.isEmpty()) {
            if (gagnants.size() == 1) {
                message = "Le gagnant est le bandit " + gagnants.get(0).getNom() + " avec un butin de " + maxLoot + "€!";
            } else {
                /** Pour ne pas afficher la ',' avant le premier gagnant */
                int i = 0;
                message = "Egalité. Les gagnants sont les bandits: ";
                for (Bandit bandit : gagnants) {
                    if (i != 0) {
                        message += ", ";
                    }
                    message += bandit.getNom();
                    i++;
                }
            }
        } else {
            message = "Pas de gagnant!";
        }
        JOptionPane.showMessageDialog(null, message, "Fin du Jeu", JOptionPane.INFORMATION_MESSAGE);
    }
}

/** Fin de la classe Modele. */

/**
 * Définition d'une classe pour le wagon.
 */
class Wagon {
    /**
     * On conserve un pointeur vers la classe principale du modèle.
     */
    private Modele modele;

    private Integer index;
    private ArrayList<Butin> butins;
    private ArrayList<Personne> personnes;
    public static final int NB_OBJETS = 4;

    private Integer nb_objets;
    private Boolean locomotive; // True si c'est la locomotive (dernier wagon)

    private Boolean spawn; // True si c'est le premier wagon


    /**
     * Constructeur de la class Wagon
     */
    public Wagon(Modele modele, Boolean locomotive, Boolean spawn, ArrayList<Bandit> bandits, Integer index, Integer nb_objets) {
        this.modele = modele;
        this.index = index;
        this.locomotive = locomotive;
        this.butins = new ArrayList<>();
        this.spawn = spawn;
        /** On ajoute les objets dans le wagon d'une forme aleatoire*/
        Random random = new Random();
        for (int i = 0; i < nb_objets; i++) {
            if (random.nextDouble() < 0.8) { /** 80% de chances d'ajouter un objet dans le wagon*/
                if (random.nextDouble() < 0.7) { /** 70% de chances que le butin soit une bourse*/
                    butins.add(new Bourse());
                } else { /** 30% de chances que le butin sout un bijou*/
                    butins.add(new Bijou());
                }
            }
        }
        /** Ajoute les bandits si c'est le spawn (1ere wagon) */
        if (spawn) {this.personnes = new ArrayList<>(bandits);} else {this.personnes = new ArrayList<>();}
        /** On ajoute le marshall et le magot si c'est la locomotive */
        if (locomotive) { this.personnes.add(modele.getMarshall()); butins.add(new Magot());}
    }

    /** Getters */

    public ArrayList<Butin> getButins() {
        return this.butins;
    }

    public ArrayList<Personne> getPersonnes() {
        return this.personnes;
    }

    public Integer getIndex() {
        return this.index;
    }



    /** Methodes pour ajouter */
    public void ajouterBandit(Bandit bandit) {
        this.personnes.add(bandit);
    }

    public void ajouterMarshall() {
        this.personnes.add(modele.getMarshall());
    }

    public void ajouterButin(Butin b) {this.butins.add(b); }

    /** Methodes pour enlever */
    public void enleverBandit(Bandit bandit) {
        if (this.personnes.contains(bandit)) {
            this.personnes.remove(bandit);
        } else { // Cas d'erreur
            int n = index+1;
            System.out.println(bandit.getNom() + " n'est pas dans Wagon " + n);
        }
    }
    public void enleverMarshall() {
        if (this.personnes.contains(modele.getMarshall())) {
            this.personnes.remove(modele.getMarshall());
        } else { // Cas d'erreur
            int n = index+1;
            System.out.println("Marshall n'est pas dans Wagon " + n);
        }
    }

    public void enleverButin(Butin b) {
        if (this.butins.contains(b)) {
            this.butins.remove(b);
        } else { // Cas d'erreur
            int n = index+1;
            System.out.println(b.getNom() + " n'est pas dans Wagon " + n);
        }
    }

    /** Autres méthodes */
    public Boolean contientMarshall() {
        return personnes.contains(modele.getMarshall());
    }

    public Boolean contientBandit() {
        for (Personne p: personnes) {
            // Teste s'il y a un bandit dans la liste de personnes
            if (p instanceof Bandit) {
                return true;
            }
        }
        return false;
    }

    /** Methode pour tirer dans un bandit random (sauf le tireur) dans le wagon */
    public Bandit choisirBanditRandom(Bandit bandit1) {
        ArrayList<Bandit> banditsfiltres = new ArrayList<>();
        for (Personne personne : personnes) {
            // Filtre le marshall
            if (personne instanceof Bandit) {
                Bandit bandit2 = (Bandit) personne;
                // Si ils sont a la meme surface (toit/interieur)
                if (bandit1.getToit() == bandit2.getToit()) {
                    banditsfiltres.add(bandit2);
                }
            }
        }
        // On filtre le bandit qui tire
        banditsfiltres.remove(bandit1);
        // Une fois tout filtré, on extrait un bandit des disponibles
        if (!banditsfiltres.isEmpty()) {
            Random random = new Random();
            return banditsfiltres.get(random.nextInt(banditsfiltres.size()));
        } else { // Si aucun bandit est disponible, on renvoie null
            return null;
        }
    }
}

/** On crée une classe abstraite Personne, d'où on va créer les classes Bandit et Marshall*/
abstract class Personne {

    protected String nom;

    /** Ici on gardera l'index du wagon où la personne est actuellement */
    protected int position;

    /** Getters */
    public String getNom() {
        return nom;
    }
    public int getPos() {
        return position;
    }

    /** Setters */
    public void setPos(int newpos) {
        this.position = newpos;
    }

    /** Methodes de boolean */
    public Boolean estDansSpawn() {
        return (position == 0);
    }

}
/**
 * Définition d'une classe pour les bandits.
 */
class Bandit extends Personne {
    /** Pour savoir si le bandit est dans le sol ou dans le toit, on utilisera ce bool */
    private Boolean toit;
    private ArrayList<Butin> loot;

    /** Attribut où on gardera la list d'actions du bandit dans chaque tour */
    private ArrayList<Action> actions;

    private Integer bullets;

    /** Constructeur de bandit */
    public Bandit(String nom, int bullets) {
        this.nom = nom;
        this.position = 0;
        this.toit = false;
        this.loot =  new ArrayList<>();
        this.actions = new ArrayList<>();
        this.bullets = bullets;
    }


    /** Getters */
    public Boolean getToit() {
        return toit;
    }

    public ArrayList<Butin> getLoot() {
        return loot;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public Integer getBullets() {return bullets;}

    /** Setters */

    public void setToit(Boolean t) {
        this.toit = t;
    }

    /** Methodes pour ajouter */
    public void addAction(Action a) {
        this.actions.add(a);
    }

    public void addButin(Butin b) {
        this.loot.add(b);
    }
    /** Methodes pour enlever */

    public void enleverButin(Butin b) {
        if (this.loot.contains(b)) {
            this.loot.remove(b);
        } else { // Cas d'erreur
            System.out.println(getNom() + " n'a pas le butin " + b.getNom());
        }
    }

    public void useBullet() {
        this.bullets--;
    }
    public void clearActions() {
        this.actions.clear();
    }

    /** Autres methodes */

    /** Calcule l'argent porté par le bandit en fonction de ses butins */
    public Integer calculLoot() {
        int total = 0;
        for (Butin butin : loot) {
            total += butin.getVal();
        }
        return total;
    }
    /** Prend un butin random du loot du bandit */
    public Butin butinRandom() {
        if (!loot.isEmpty()) {
            Random rand = new Random();
            // On genere un index random entre 0 et le num de butins dans le wagon
            int randomIdx = rand.nextInt(loot.size());
            // On prend cette index dans la liste des butins du wagon
            return loot.get(randomIdx);
        } else { // cas d'erreur
            System.out.println(nom + " n'a pas de butin dans son inventaire!");
            return null;
        }
    }
}

/**
 * Définition d'une classe pour le marshall.
 */
class Marshall extends Personne {
    private double nervosite;

    /** Constructeur du marshall */
    public Marshall(int posInit, String nom, double nerv) {
        this.nervosite = nerv;
        this.position = posInit;
        this.nom = nom;

    }

    /** Getters */
    public double getNervosite() {
        return nervosite;
    }

    /** Setters */
    public void setNervosite(double n) {
        this.nervosite = n;
    }
}

/** Creation de la classe de Butin, d'où on fera des extends pour chaque objet */
class Butin {
    private String nom;
    private Integer valeur;

    /** Constructeur de la class Butin */
    public Butin(String nom, Integer val) {
        this.nom = nom;
        this.valeur = val;
    }

    /** Methodes de Butin */
    public String getNom() {
        return this.nom;
    }

    public Integer getVal() {
        return this.valeur;
    }

}

/** On crée les types de Butin */

/** Création du type Bourse */
class Bourse extends Butin {
    public Bourse() {
        super("Bourse", generateRandomVal());
    }

    private static int generateRandomVal() {
        Random random  = new Random();
        /** On gènére la valeur du sac de 0 à 500$ */
        int randomVal = random.nextInt(501);
        return randomVal;
    }
}

/** Création du type Bijoue */
class Bijou extends Butin {
    public Bijou() {
        super("Bijou", 500);
    }
}

/** Création du type Magot */
class Magot extends Butin {
    public Magot() {
        super("Magot", 1000);
    }
}

/**
 * Définition d'une classe abstraite pour les actions.
 * D'ici on fera chaque action disponible pour les bandits
 * Lesquels on ajoutera a la list d'actions de chaque bandit
 * Qui seront executées à la fin du tour
 */
abstract class Action {
    protected Bandit bandit;
    protected Modele modele;

    /** Constructeur d'Action */
    public Action(Bandit bandit, Modele modele) {
        this.bandit = bandit;
        this.modele = modele;
    }

    /** Tous les actions pourront s'executer */
    public abstract void executer();
}

class Deplacer extends Action {
    /** Un deplacement a besoin d'une direction */
    private Direction dir;

    /** Constructeur de Deplacer */
    public Deplacer(Modele modele, Bandit b, Direction d) {
        super(b, modele);
        this.dir = d;
    }

    @Override
    public void executer() {
        modele.deplacer(bandit, dir);
    }

    /** Getters */
    public Direction getDir() {
        return dir;
    }
}

class Braquer extends Action {
    /** Un braquage a besoin d'un Wagon */
    private Wagon wagon;

    /** Constructeur de Braquer */
    public Braquer(Modele modele, Bandit b, Wagon w) {
        super(b, modele);
        this.wagon = w;
    }
    @Override
    public void executer() {
        modele.braquer(bandit, wagon);
    }
}

class Tirer extends Action {
    /** Un tir a besoin d'une direction */
    private Direction dir;

    /** Constructeur de tirer */
    public Tirer(Modele modele, Bandit b, Direction d) {
        super(b, modele);
        this.dir = d;
    }

    @Override
    public void executer() { modele.tirerBandit(bandit, dir); }
}


class Vue {
    private JFrame frame;

    /** Ces 4 attributs sont les 4 parties de l'interface graphique:
     * VueTrain pour l'affichage des wagons
     * VueCommandes pour l'affichage des commandes (boutons)
     * VueBandits pour l'affichage des informations des bandits
     * VueEtat pour l'affichage de l'etat du jeu */
    private VueTrain train;
    private VueCommandes commandes;
    private VueBandits bandits;
    private VueEtat etat;

    /** Construction d'une vue attachée à un modèle. */
    public Vue(Modele modele) {
        /** Définition de la fenêtre principale. */
        frame = new JFrame();
        frame.setTitle("Colt Express");
        frame.setLayout(new BorderLayout());

        /** Définition des quatre vues et ajout à la fenêtre. */
        etat = new VueEtat(modele);
        frame.add(etat, BorderLayout.NORTH);
        train = new VueTrain(modele);
        frame.add(train, BorderLayout.CENTER);
        commandes = new VueCommandes(modele);
        frame.add(commandes, BorderLayout.EAST);
        bandits = new VueBandits(modele);
        frame.add(bandits, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}


/**
 * Une classe pour représenter la zone d'affichage des contenus des wagons
 */
class VueTrain extends JPanel implements Observer {
    /** On maintient une référence vers le modèle. */
    private Modele modele;
    /** Définition des tailles pour l'affichage du train */
    private int TAILLE_X;
    private int TAILLE_Y;

    /** Constructeur */
    public VueTrain(Modele modele) {
        this.modele = modele;
        int nbWagons = modele.getTrain().size();
        this.TAILLE_X = 1700
                /nbWagons;
        this.TAILLE_Y = 80*modele.getBandits().size();
        /** On enregistre la vue [this] en tant qu'observateur de [modele]. */
        modele.addObserver(this);

        /** On éxécute la méthode qui construit VueTrain*/
        construireVueTrain();
    }

    private void construireVueTrain() {
        int nbWagons = modele.getTrain().size();
        JPanel train = new JPanel(new GridLayout(1, nbWagons));
        train.setPreferredSize(new Dimension(TAILLE_X*nbWagons, TAILLE_Y));

        for (int i = 0; i < nbWagons; i++) {
            Wagon wagon = modele.getTrain().get(i);
            JPanel wagonPanel = new JPanel();
            wagonPanel.setLayout(new BoxLayout(wagonPanel, BoxLayout.PAGE_AXIS));
            if (i+1 == nbWagons)
                wagonPanel.setBorder(BorderFactory.createTitledBorder("Locomotive"));
            else
                wagonPanel.setBorder(BorderFactory.createTitledBorder("Wagon " + (i + 1)));



            /** Creation d'un JPanel interne pour contenir les butins et personnes dans chaque wagon */
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

            /** Creation d'une etiquete pour le butin */
            JLabel labelButin = new JLabel("Butin: ");

            /** Couleur pour le rendre plus joli */
            labelButin.setForeground(Color.BLUE);

            /** Si le wagon est vide, on crée une label rien */
            if (wagon.getButins().isEmpty()) {
                JLabel labelRien = new JLabel("Rien");
                contentPanel.add(labelButin , labelRien);
            }
            /** Sinon on affiche la liste des noms de chaque butin */
            else {
                contentPanel.add(labelButin);
                for (Butin b : wagon.getButins()) {
                    contentPanel.add(new JLabel("- " + b.getNom()));
                }
            }

            /** Création d'une nouvelle etiquette pour les personnes dans le wagon */
            JLabel labelPers = new JLabel("Personne: ");
            labelPers.setForeground(Color.MAGENTA);
            contentPanel.add(labelPers);
            /** On ajoute chaque personne dans le wagon correspondant */
            for (Personne p : wagon.getPersonnes()) {
                contentPanel.add(new JLabel("- " + p.getNom()));
            }

            /** On ajoute le panel de contenu au panel du wagon */
            wagonPanel.add(contentPanel);
            /** On ajoute le panel du wagon dans le panel principal */
            train.add(wagonPanel);
        }
        /** On ajoute le panel du train dans le panel globale */
        this.add(train);
    }

    /** Pour faire update, on a crée une fonction rewrite() qui refait à chaque tour l'affichage de VueTrain */
    public void update() { rewrite(); }

    private void rewrite() {
        /** On vide le contenu actuel */
        this.removeAll();

        /** On le reconstruit */
        construireVueTrain();

        /** On raffraîchit l'affichage */
        this.revalidate();
        this.repaint();
    }
}

/**
 * Une classe pour représenter la zone d'affichage des contenus des bandits
 */
class VueBandits extends JPanel implements Observer {
    private Modele modele;
    private int TAILLE_X;

    /** Définition d'une taille verticale fixé pour l'affichage des bandits. */
    private final static int TAILLE_Y = 300;

    /** Constructeur */
    public VueBandits(Modele modele) {
        this.modele = modele;
        int tailleBandits = modele.getBandits().size();
        this.TAILLE_X = 600/tailleBandits;
        /** On enregistre la vue [this] en tant qu'observateur de [modele]. */
        modele.addObserver(this);

        /** On éxécute la méthode qui construit VueBandits */
        construireVueBandits();
    }

    private void construireVueBandits() {
        int tailleBandits = modele.getBandits().size();
        JPanel bandits = new JPanel(new GridLayout(1, tailleBandits));
        bandits.setPreferredSize(new Dimension(TAILLE_X*tailleBandits, TAILLE_Y));

        for (int i = 0; i < tailleBandits; i++) {
            Bandit bandit = modele.getBandits().get(i);
            JPanel banditPanel = new JPanel();

            banditPanel.setLayout(new BoxLayout(banditPanel, BoxLayout.PAGE_AXIS));
            banditPanel.setBorder(BorderFactory.createTitledBorder(bandit.getNom()));

            /** Creation d'un JPanel interne pour contenir les informations de chaque bandit */
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

            /** Création d'une etiquette pour le butin */
            JLabel labelButin = new JLabel("Butin: ");
            labelButin.setForeground(Color.BLUE);

            if (bandit.getLoot().isEmpty()) {
                JLabel labelButinVide = new JLabel("Butin: Rien");
                labelButinVide.setForeground(Color.BLUE);
                contentPanel.add(labelButinVide);
            }
            else {
                contentPanel.add(labelButin);
                for (Butin b : bandit.getLoot()) {
                    contentPanel.add(new JLabel("- " + b.getNom() + " : " + b.getVal() + "€"));
                }
            }

            /** Création d'une étiquette pour préciser "l'hauteur" du bandit */
            String t = bandit.getToit() ? "Dans le Toit" : "Dans le wagon";
            contentPanel.add(new JLabel(t));

            /** Création d'une étiquette pour afficher l'argent du bandit */
            Integer total = bandit.calculLoot();
            contentPanel.add(new JLabel("Total: " + total + "€"));

            /** Création d'une étiquette pour afficher la position du bandit */
            Integer pos = bandit.getPos() + 1;
            contentPanel.add(new JLabel("Position: Wagon " + pos));

            /** Création d'une étiquette pour afficher les balles du bandit */
            Integer balles = bandit.getBullets();
            contentPanel.add(new JLabel("Balles restantes: " + balles));

            /** Ajout de ces informations au JPanel principaux */
            banditPanel.add(contentPanel);
            bandits.add(banditPanel);
        }
        this.add(bandits);
    }

    public void update() { rewrite(); }

    private void rewrite() {
        /** On vide le contenu actuel */
        this.removeAll();

        /** On le reconstruit */
        construireVueBandits();

        /** On raffraîchit l'affichage */
        this.revalidate();
        this.repaint();
    }
}

/**
 * Une classe pour représenter la zone contenant les boutons.
 * */
class VueCommandes extends JPanel {
    /**
     * Pour que le bouton puisse transmettre ses ordres, on garde une
     * référence au modèle.
     */
    private Modele modele;

    /** Constructeur */
    public VueCommandes(Modele modele) {
        this.modele = modele;
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /** Création des contraintes du GridBagLayout */
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Apres avoir placé ce composant, l'autre sera dans la ligne suivante
        gbc.anchor = GridBagConstraints.WEST; // Le composant sera aligné à gauche de la cellule
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 5, 0, 5); // Largeur de 5 pixels à droite at à gauche du composant

        /** On ajoute les boutons */
        JButton boutonDeplacer = new JButton("Deplacer");
        JButton boutonBraquer = new JButton("Braquer");
        JButton boutonTirer = new JButton("Tirer");
        JButton boutonAction = new JButton("Action!");
        JButton boutonUp = new JButton("^");
        JButton boutonDown = new JButton("v");
        JButton boutonDevant = new JButton(">");
        JButton boutonArriere = new JButton("<");


        add(boutonBraquer, gbc);
        add(boutonDeplacer, gbc);
        add(boutonTirer, gbc);
        add(boutonUp, gbc);
        add(boutonDown, gbc);
        add(boutonDevant, gbc);
        add(boutonArriere, gbc);
        add(boutonAction, gbc);

        Controleur ctrl = new Controleur(modele);
        /** Enregistrement du contrôleur comme auditeur du bouton. */
        boutonAction.addActionListener(ctrl);
        boutonBraquer.addActionListener(ctrl);
        boutonTirer.addActionListener(ctrl);
        boutonUp.addActionListener(ctrl);
        boutonDown.addActionListener(ctrl);
        boutonDevant.addActionListener(ctrl);
        boutonArriere.addActionListener(ctrl);
        boutonDeplacer.addActionListener(ctrl);

    }
}

/**
 * Une classe pour représenter la zone d'affichage de l'état du jeu
 */
class VueEtat extends JPanel implements Observer {
    private Modele modele;

    /** Constructeur */
    public VueEtat(Modele modele) {
        this.modele = modele;

        modele.addObserver(this);

        construireVueEtat();
    }

    private void construireVueEtat() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        String text;
        if (!modele.planificationFini()) {text = "Etat de planification";}
        else {text = "Etat de action";}
        JLabel etat = new JLabel(text);
        etat.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nbTour = new JLabel("Tour n° " + modele.getNbTour());
        nbTour.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(etat);
        this.add(nbTour);
    }

    public void update() { rewrite();}

    private void rewrite() {
        /** On vide le contenu actuel */
        this.removeAll();

        /** On le reconstruit */
        construireVueEtat();

        /** On raffraîchit l'affichage */
        this.revalidate();
        this.repaint();
    }
}
/** Fin de la vue. */


/**
 * Classe pour notre contrôleur rudimentaire.
 */
class Controleur implements ActionListener {
    /**
     * On garde un pointeur vers le modèle, car le contrôleur doit
     * provoquer un appel de méthode du modèle.
     */
    Modele modele;
    String actionEnAttente; // Pour garder les actions Deplacer/Tirer en attente

    /** Attribut ajouté pour jouer à plusieurs bandits
     * ça sert à savoir quelle bandit est en train de faire ses actions */
    Integer indexActuel;
    public Controleur(Modele modele) { this.modele = modele; this.actionEnAttente = ""; this.indexActuel = 0;}
    /**
     * Action effectuée à réception d'un événement : appeler la
     * méthode [avance] du modèle.
     */
    public void actionPerformed(ActionEvent e) {
        /** On extrait la commande */
        String commande = e.getActionCommand();
        ArrayList<Bandit> bandits = modele.getBandits();
        /** On extrait le bandit qui fait l'action */
        Bandit bandit = bandits.get(indexActuel);

        if (commande.equals("Deplacer")) {
            /** Pour chaque action, on teste si le jeu est en etat de planification
             * et affiche impossible sinon */
            if (modele.getEtatActuel() == EtatJeu.PLANIFICATION) {
                actionEnAttente = "Deplacer";
            } else {
                System.out.println("Deplacer impossible, le jeu est en etat d'action.");
            }
        } else if (commande.equals("Braquer")) {
            actionEnAttente = "";
            if (modele.getEtatActuel() == EtatJeu.PLANIFICATION) {
                int i = 0;
                /** On avait eu des problèmes pour braquer apres que le bandit se déplace dans
                 * le même tour, donc on a fait ça */
                for (Action action : bandit.getActions()) {
                    if (action instanceof Deplacer) {
                        Deplacer deplace = (Deplacer) action;
                        if (deplace.getDir() == Direction.AVANT) {
                            i++;
                        } else if (deplace.getDir() == Direction.ARRIERE) {
                            i--;
                        }
                    }
                }
                Wagon currentWagon = modele.getTrain().get(bandit.getPos() + i);
                Braquer braquage = new Braquer(modele, bandit, currentWagon);
                modele.addActionBandit(bandit, braquage);
            } else {
                System.out.println("Braquer impossible, le jeu est en etat d'action.");
            }
        } else if (commande.equals("Tirer")) {
            if (modele.getEtatActuel() == EtatJeu.PLANIFICATION) {
                actionEnAttente = "Tirer";
            } else {
                System.out.println("Tirer impossible, le jeu est en etat d'action.");
            }
        } else if (commande.equals("Action!")) {
            actionEnAttente = "";
            if (modele.getEtatActuel() == EtatJeu.ACTION) {
                modele.executerActions();
            } else {
                System.out.println("Action impossible, le jeu est en etat de planification.");
            }
        } else if (commande.equals("^") || commande.equals("v") || commande.equals(">") || commande.equals("<")) {
            if (modele.getEtatActuel() == EtatJeu.PLANIFICATION) {
                /** Executer l'action en attente */
                if (!actionEnAttente.isEmpty()) {
                    Direction dir = convertirEnDirection(commande);
                    if (actionEnAttente.equals("Deplacer")) {
                        Deplacer deplacement = new Deplacer(modele, bandit, dir);
                        modele.addActionBandit(bandit, deplacement);
                    } else if (actionEnAttente.equals("Tirer")) {
                        Tirer tir = new Tirer(modele, bandit, dir);
                        modele.addActionBandit(bandit, tir);
                    }
                    actionEnAttente = "";
                } else {
                    System.out.println("Pas d'action en attente!");
                }
            } else {
                System.out.println("Action impossible, le jeu est en etat d'action.");
            }
        }
        /** Si le bandit a fini de faire ses actions, on passe au suivant bandit */
        if (modele.actionsFull(bandit)) {
            this.indexActuel++;
        }
        /** Si le tour a fini pour tout les bandits, on reinitalise le index */
        if (modele.planificationFini()) {
            this.indexActuel = 0;
        }
    }

    /** Methode pour convertir le bouton en Direction */
    private Direction convertirEnDirection(String commande) {
        switch (commande) {
            case "^":
                return Direction.HAUT;
            case "v":
                return Direction.BAS;
            case ">":
                return Direction.AVANT;
            case "<":
                return Direction.ARRIERE;
            default:
                return null;
        }
    }

}
/** Fin du contrôleur. */


/** Fonctionnalité supplementaire */
class ConfigurationJeu {
    public static Modele configurerJeu() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));

        /** Création des JTextFields où l'utilisateur écrira les paramètres du jeu
         * Chacun de ces JTextFields aura une default value, notée sur la class Modele
         * */
        JTextField nbWagonsField = new JTextField(String.valueOf(Modele.NB_WAGONS), 5);
        JTextField nbActionsField = new JTextField(String.valueOf(Modele.NB_ACTIONS), 5);
        JTextField nbBanditsField = new JTextField(String.valueOf(Modele.NB_BANDITS), 5);
        JTextField nbNervositeField = new JTextField(String.valueOf(Modele.NERVOSITE_MARSHALL), 5);
        JTextField nbBallesField = new JTextField(String.valueOf(Modele.NB_BALLES), 5);
        JTextField nbToursMaxField = new JTextField(String.valueOf(Modele.NB_MAX_TOURS), 5);
        JTextField nbObjetsMaxField = new JTextField(String.valueOf(Modele.NB_MAX_OBJETS), 5);

        /** Ajout des demandes dans chaque JTextField */
        configPanel.add(new JLabel("Nombre de wagons:"));
        configPanel.add(nbWagonsField);
        configPanel.add(new JLabel("Nombre d'objets max par wagon:"));
        configPanel.add(nbObjetsMaxField);
        configPanel.add(new JLabel("Nombre d'actions par bandit:"));
        configPanel.add(nbActionsField);
        configPanel.add(new JLabel("Nombre de bandits:"));
        configPanel.add(nbBanditsField);
        configPanel.add(new JLabel("Nervosité du marshall:"));
        configPanel.add(nbNervositeField);
        configPanel.add(new JLabel("Nombre de balles par bandit:"));
        configPanel.add(nbBallesField);
        configPanel.add(new JLabel("Nombre de tours maximale:"));
        configPanel.add(nbToursMaxField);

        /** Création de l'interface secondaire pour afficher ces JTextFields */
        JOptionPane.showMessageDialog(null, configPanel, "Configuration initiale", JOptionPane.PLAIN_MESSAGE);

        // Collecte des noms des bandits en fonction du nombre spécifié
        /** On collecte le nombre des bandits (si possible) */
        int nbBandits;
        try {
            nbBandits = Integer.parseInt(nbBanditsField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Le nombre de bandits n'est pas valide. Les valeurs par défaut seront utilisées.");
            nbBandits = Modele.NB_BANDITS;
        }

        /** Creation d'une autre interface secondaire pour écrire les noms des bandits */
        JPanel banditsPanel = new JPanel();
        banditsPanel.setLayout(new BoxLayout(banditsPanel, BoxLayout.PAGE_AXIS));
        JTextField[] banditFields = new JTextField[nbBandits];
        for (int i = 0; i < nbBandits; i++) {
            JTextField banditField = new JTextField(20); // Taille horizontale du JTextField
            banditFields[i] = banditField;
            JPanel row = new JPanel();
            row.add(new JLabel("Nom du bandit " + (i + 1) + ":"));
            row.add(banditField);
            banditsPanel.add(row);
        }

        /** Enregistre l'éléction de bouton de l'utilisateur
         * (Si OK, on continue avec le jeu, sinon on annule)
         * */
        int result = JOptionPane.showConfirmDialog(null, banditsPanel, "Entrez les noms des bandits", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            ArrayList<String> nomsBandits = new ArrayList<>();
            for (JTextField field : banditFields) {
                nomsBandits.add(field.getText().trim());
            }

            /** Création du modèle avec les paramètres configurés */
            try {
                /** Extraction des nouvelles parametres du jeu */
                int nbWagons = Integer.parseInt(nbWagonsField.getText());
                int nbActions = Integer.parseInt(nbActionsField.getText());
                double nervositeMarshall = Double.parseDouble(nbNervositeField.getText());
                int nbBalles = Integer.parseInt(nbBallesField.getText());
                int nb_obj_max = Integer.parseInt(nbObjetsMaxField.getText());
                int nbToursMax = Integer.parseInt(nbToursMaxField.getText());
                return new Modele(nbWagons, nbActions, nbBandits, nbBalles, nervositeMarshall, nbToursMax, nb_obj_max,nomsBandits);
            } catch (NumberFormatException e) {
                /** Affichage de message d'erreur à l'utilisateur */
                JOptionPane.showMessageDialog(null, "Une ou plusieurs valeurs numériques sont invalides. Le jeu utilisera les valeurs par défaut.");

                /** Ajoute les noms des bandits dans un tableau qu'on mettra en parametre du Modele */
                ArrayList<String> nomsBanditsParDefaut = new ArrayList<>();
                for (int i = 0; i < Modele.NB_BANDITS; i++) {
                    nomsBanditsParDefaut.add("Bandit " + (i + 1));
                }
                /** Crée et retourne une nouvelle instance de Modele avec les valeurs par défaut */
                return new Modele(Modele.NB_WAGONS, Modele.NB_ACTIONS, Modele.NB_BANDITS, Modele.NB_BALLES, Modele.NERVOSITE_MARSHALL, Modele.NB_MAX_TOURS, Modele.NB_MAX_OBJETS, nomsBanditsParDefaut);
            }
        } else {
            /** L'utilisateur a annulé la configuartion */
            return null;
        }
    }
}