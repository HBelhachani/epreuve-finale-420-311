package mv.sdd.sim.thread;

import mv.sdd.model.Commande;
import mv.sdd.sim.Restaurant;
import mv.sdd.utils.Formatter;

import java.util.Queue;

public class Cuisinier implements Runnable {

    private final Restaurant restaurant;

    public Cuisinier(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void run() {

        Queue<Commande> commandesEnAttente = restaurant.getCommandesEnAttente();

        while (restaurant.isServiceEnCours()) {
            Commande commandeAPreparer = null;

            synchronized (commandesEnAttente) {
                try {

                    while (commandesEnAttente.isEmpty() && restaurant.isServiceEnCours()) {
                        commandesEnAttente.wait();
                    }

                    if (!restaurant.isServiceEnCours()) {
                        break;
                    }

                    commandeAPreparer = restaurant.retirerProchaineCommande();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (commandeAPreparer != null) {

                commandeAPreparer.demarrerPreparation();

                restaurant.getCommandesEnPreparation().add(commandeAPreparer);

                int tempsActuel = restaurant.getHorloge().getTempsSimule();

                restaurant.getLogger().logLine(
                        Formatter.eventCommandeDebut(
                                tempsActuel,
                                commandeAPreparer.getId(),
                                commandeAPreparer.getTempsRestant()
                        )
                );
            }
        }

        System.out.println("Thread Cuisinier terminé proprement.");
    }
}