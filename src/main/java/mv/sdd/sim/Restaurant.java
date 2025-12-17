package mv.sdd.sim;

import mv.sdd.io.Action;
import mv.sdd.io.ActionType;
import mv.sdd.model.*;
import mv.sdd.sim.thread.Cuisinier;
import mv.sdd.utils.Constantes;
import mv.sdd.utils.Formatter;
import mv.sdd.utils.Logger;

import java.util.*;

public class Restaurant {
    private final Logger logger;

    private final Horloge horloge = new Horloge();
    private final Stats stats;
    private int dureeMaxService = 0;

    private final Map<Integer, Client> clients = new HashMap<>();
    private final Queue<Commande> commandesEnAttente = new LinkedList<>();
    private final List<Commande> commandesEnPreparation = new ArrayList<>();

    private Thread cuisinierThread;
    private Cuisinier cuisinier;
    private volatile boolean serviceEnCours = false;

    public List<Commande> getCommandesEnPreparation() {
        return commandesEnPreparation;
    }

    public Queue<Commande> getCommandesEnAttente() {
        return commandesEnAttente;
    }

    public boolean isServiceEnCours() {
        return serviceEnCours;
    }

    public Horloge getHorloge() {
        return horloge;
    }

    public Logger getLogger() {
        return logger;
    }

    public Restaurant(Logger logger) {
        this.logger = logger;
        this.stats = new Stats(horloge);
    }

    public void executerAction(Action action) {

        if (!serviceEnCours
                && action.getType() != ActionType.DEMARRER_SERVICE
                && action.getType() != ActionType.QUITTER) {
            return;
        }

        switch (action.getType()) {
            case DEMARRER_SERVICE:
                demarrerService(action.getParam1(), action.getParam2());
                break;
            case AJOUTER_CLIENT:
                // param1: id, param3: nom, param2: patience
                ajouterClient(action.getParam1(), action.getParam3(), action.getParam2());
                break;
            case PASSER_COMMANDE:
                // param1: idClient, param3: codePlat
                MenuPlat codePlat = MenuPlat.valueOf(action.getParam3());
                passerCommande(action.getParam1(), codePlat);
                break;
            case AVANCER_TEMPS:
                // param1: minutes
                avancerTemps(action.getParam1());
                break;
            case AFFICHER_ETAT:
                afficherEtat();
                break;
            case AFFICHER_STATS:
                afficherStatistiques();
                break;
            case QUITTER:
                arreterService();
                break;
        }
    }

    public void demarrerService(int dureeMax, int nbCuisiniers) {
        if (serviceEnCours) {
            logger.logLine("Le service est déjà en cours.");
            return;
        }

        this.dureeMaxService = dureeMax;

        logger.logLine(String.format(Constantes.DEMARRER_SERVICE, dureeMax, nbCuisiniers));

        cuisinier = new Cuisinier(this);
        cuisinierThread = new Thread(cuisinier, "Thread-Cuisinier-1");

        serviceEnCours = true;
        cuisinierThread.start();
    }

    public void avancerTemps(int minutes) {

        logger.logLine(Constantes.AVANCER_TEMPS + minutes);

        for (int i = 0; i < minutes; i++) {

            if (horloge.getTempsSimule() >= dureeMaxService) {

                logger.logLine("Temps max de service atteint (" + dureeMaxService + " min).");
                break;
            }

            horloge.avancerTempsSimule(1);
            tick();
        }
    }

    public void arreterService() {

        if (!serviceEnCours) return;

        logger.logLine("Commande QUITTER reçue. Arrêt du service...");
        serviceEnCours = false;

        synchronized (commandesEnAttente) {

            commandesEnAttente.notifyAll();
        }

        try {
            if (cuisinierThread != null && cuisinierThread.isAlive()) {
                cuisinierThread.join(5000); // Attendre max 5s
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.logLine(Constantes.FOOTER_APP);
    }

    public void tick() {

        synchronized (this) {

            faireProgresserPreparation();

            diminuerPatienceClients();
        }
    }

    public Client ajouterClient(int id, String nom, int patienceInitiale) {

        if (clients.containsKey(id)) {

            logger.logLine("Erreur: Client avec ID " + id + " existe déjà.");
            return null;
        }

        Client client = new Client(id, nom, patienceInitiale);
        clients.put(id, client);
        stats.incrementerTotalClients();

        logger.logLine(Formatter.eventArriveeClient(horloge.getTempsSimule(), client));
        return client;
    }

    public Commande passerCommande(int idClient, MenuPlat codePlat) {

        Client client = clients.get(idClient);

        if (client == null || client.getEtat() != EtatClient.EN_ATTENTE) {
            logger.logLine("Erreur: Client " + idClient + " introuvable ou n'est plus en attente.");
            return null;
        }

        Commande commande;

        synchronized (commandesEnAttente) {
            boolean premiereCommande = client.getCommande() == null;

            if (premiereCommande) {

                commande = new Commande(client, codePlat);
                client.setCommande(commande);
                commandesEnAttente.offer(commande);
                commandesEnAttente.notifyAll();

                logger.logLine(Formatter.eventCommandeCree(horloge.getTempsSimule(), commande.getId(), client, codePlat));
            } else {

                commande = client.getCommande();
                Plat plat = Constantes.MENU.get(codePlat);

                commande.ajouterPlat(plat.getCode());

                logger.logLine(String.format("[%s t=%d] Cmd #%d (%s) → ajoute %s",
                        Constantes.EMO_FILE_CMD, horloge.getTempsSimule(), commande.getId(), client.getNom(), Formatter.emojiPlat(codePlat)));
            }
        }
        return commande;
    }

    public Commande retirerProchaineCommande() {

        synchronized (commandesEnAttente) {

            return commandesEnAttente.poll();
        }
    }

    public void marquerCommandeTerminee(Commande commande) {
        synchronized (this) {

            commandesEnPreparation.remove(commande);

            Client client = commande.getClient();

            if (client.getEtat() == EtatClient.EN_ATTENTE) {

                client.setEtat(EtatClient.SERVI);
                commande.setEtat(EtatCommande.LIVREE);

                stats.incrementerNbServis();

                stats.incrementerChiffreAffaires(commande.calculerMontant());
                for (Plat plat : commande.getPlats().values()) {
                    stats.incrementerVentesParPlat(plat.getCode());
                }

                logger.logLine(Formatter.eventCommandeTerminee(horloge.getTempsSimule(), commande.getId(), client));
            } else {
                commande.setEtat(EtatCommande.PERDUE);
            }
        }
    }

    public void afficherEtat() {
        // Calcul des statistiques pour l'affichage de l'état avec les stream
        long nbServis = clients.values().stream().filter(c -> c.getEtat() == EtatClient.SERVI).count();
        long nbFaches = clients.values().stream().filter(c -> c.getEtat() == EtatClient.PARTI_FACHE).count();
        long nbClients = clients.size();

        logger.logLine(Formatter.resumeEtat(
                horloge.getTempsSimule(),
                (int) nbClients,
                (int) nbServis,
                (int) nbFaches,
                commandesEnAttente.size(),
                commandesEnPreparation.size()
        ));

        for (Client client : clients.values()) {
            logger.logLine(Formatter.clientLine(client));
        }
    }

    public void afficherStatistiques() {
        logger.logLine(Constantes.HEADER_AFFICHER_STATS);
        logger.logLine(stats.toString());
    }

    private void diminuerPatienceClients() {
        List<Client> clientsPartisFaches = new ArrayList<>();

        for (Client client : clients.values()) {
            if (client.getEtat() == EtatClient.EN_ATTENTE) {

                client.diminuerPatience(1);

                if (client.getEtat() == EtatClient.PARTI_FACHE) {
                    clientsPartisFaches.add(client);
                }
            }
        }

        for (Client client : clientsPartisFaches) {
            stats.incrementerNbFaches();
            logger.logLine(Formatter.eventClientFache(horloge.getTempsSimule(), client));

            if (client.getCommande() != null) {
                client.getCommande().setEtat(EtatCommande.PERDUE);
            }
        }
    }

    private void faireProgresserPreparation() {
        List<Commande> commandesTerminees = new ArrayList<>();

        for (Commande commande : commandesEnPreparation) {
            commande.decrementeerTempsRestant(1);

            if (commande.estTermineeParTemps()) {
                commandesTerminees.add(commande);
            }
        }


        for (Commande commande : commandesTerminees) {

            marquerCommandeTerminee(commande);
        }
    }
}