package mv.sdd.model;

import mv.sdd.utils.Constantes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Commande {
    private int id;
    private static int nbCmd = 0;
    private final Client client;
    private EtatCommande etat = EtatCommande.EN_ATTENTE;
    private int tempsRestant; // en minutes simulées
    // TODO : ajouter l'attribut plats et son getter avec le bon type et le choix de la SdD adéquat
    private final HashMap<MenuPlat, Plat> plats = new HashMap<>();

    // TODO : Ajout du ou des constructeur(s) nécessaires ou compléter au besoin
    public Commande(Client client, MenuPlat plat) {
        id = ++nbCmd;
        this.client = client;

        ajouterPlat(plat);

    }

    public HashMap<MenuPlat, Plat> getPlats() {
        return plats;
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
    public void ajouterPlat(MenuPlat menuPlat){
        if(menuPlat != null){

            Plat plat = Constantes.MENU.get(menuPlat);

            this.plats.put(menuPlat, plat);
        }
    }

    // TODO : Ajoutez la méthode demarrerPreparation

    public void demarrerPreparation(){
        setEtat(EtatCommande.EN_PREPARATION);
    }

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

    public int calculerTempsPreparationTotal(){

        int sum = 0;

        for(Plat p : plats.values()){
            sum += p.getTempsPreparation();
        }

        return sum;
    }

    // TODO : Ajoutez la méthode calculerMontant

    public double calculerMontant(){
        double sum = 0;

        for(Plat p : plats.values()){
            sum += p.getPrix();
        }

        return sum;
    }
}
