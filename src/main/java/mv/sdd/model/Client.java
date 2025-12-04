package mv.sdd.model;

import java.util.Objects;

public class Client {
    private final int id;
    private final String nom;
    private int patience;
    private EtatClient etat;
    private Commande commande;

    public Client(int id, String nom, int patienceInitiale) {
        this.id = id;
        this.nom = nom;
        this.patience = patienceInitiale;
        this.etat = EtatClient.EN_ATTENTE;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public int getPatience() {
        return patience;
    }

    public void diminuerPatience(int minutes) {
        // TODO: diminuer patience et passer à PARTI_FACHE si <= 0

        if(minutes < 0) System.out.println("impossible de diminuer la patience avec un temps négatif.");

        else if ((patience -= minutes) <= 0){
            setEtat(EtatClient.PARTI_FACHE);

            patience = 0;
        }

    }

    public EtatClient getEtat() {
        return etat;
    }

    public void setEtat(EtatClient etat) {
        this.etat = etat;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
