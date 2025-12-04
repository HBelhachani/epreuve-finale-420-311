package mv.sdd.model;

import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private static int nbCmd = 0;
    private final Client client;
    private EtatCommande etat = EtatCommande.EN_ATTENTE;
    private int tempsRestant; // en minutes simulées
    // TODO : ajouter l'attribut plats et son getter avec le bon type et le choix de la SdD adéquat
    private final List<Plat> plats = new ArrayList<>();

    // TODO : Ajout du ou des constructeur(s) nécessaires ou compléter au besoin
    public Commande(Client client, MenuPlat plat) {
        id = ++nbCmd;
        this.client = client;
        // À compléter

    }

    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public EtatCommande getEtat() {
        return etat;
    }

    public int getTempsRestant() {
        return tempsRestant;
    }

    public void setEtat(EtatCommande etat) {
        this.etat = etat;
    }

    // TODO : Ajoutez la méthode ajouterPlat
    public void ajouterPlat(Plat plat){
        if(plat != null){

            this.plats.add(plat);
        }
    }

    // TODO : Ajoutez la méthode demarrerPreparation

    // TODO : Ajoutez la méthode decrementerTempsRestant

    public void decrementeerTempsRestant(int temps){
        if(temps < 0) System.out.println("on ne peut pas décrémenter avec un temps négatif.");

        else if((tempsRestant -= temps) < 0){

            tempsRestant = 0;
        }
    }

    // TODO : Ajoutez la méthode estTermineeParTemps

    public boolean estTermineeParTemps(){
        return tempsRestant <= 0;
    }

    // TODO : Ajoutez la méthode calculerTempsPreparationTotal

    // TODO : Ajoutez la méthode calculerMontant
}
