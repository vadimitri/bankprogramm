package bankprojekt.verarbeitung;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Aktienkonto extends Konto {
    // Map des Depots mit wkn und Anzahl der gekauften Aktien
    private Map<String, Integer> depot;
    // Alle kaufbaren Aktien
    private Map<String, Aktie> aktienDatenbank;
    ScheduledExecutorService scheduler;


    /**
     * Erstellt ein Aktiendepot, mit dem man Aktien kaufen kann.
     */
    public Aktienkonto() {
        super();
        this.depot = new HashMap<>();
        this.aktienDatenbank = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    /**
     * Erstellt einen Kaufauftrag fuer eine Aktie und kauft sobald ein bestimmter preis erreicht wurde
     * @param wkn Wertpapierkennnummer
     * @param anzahl Anzahl der zu kaufenden Aktien
     * @param hoechstpreis Preis, bei dem gekauft wird
     * @return Geldbetrag, der ausgegeben wurde
     */
    public Future<Double> kaufauftrag(String wkn, int anzahl, Geldbetrag hoechstpreis) {

        CompletableFuture<Double> future = new CompletableFuture<>();

        Aktie aktie = aktienDatenbank.get(wkn);
        if (aktie == null) {
            future.complete(0.0);
            return future;
        }

        scheduler.scheduleWithFixedDelay(() -> {
            // Wenn Aktie unter bestimmten hoechstpreis ist, wird gekauft
            if (aktie.getKurs() <= hoechstpreis.getBetrag()) {
                Geldbetrag gesamtpreis = new Geldbetrag(aktie.getKurs() * anzahl);
                if (getKontostand().compareTo(gesamtpreis) >= 0) {
                    try {
                        abheben(gesamtpreis);
                    } catch (GesperrtException e) {
                        throw new RuntimeException(e);
                    }
                    // Merge, falls mehrmals mehrere Aktien gekauft werden
                    depot.merge(wkn, anzahl, Integer::sum);
                    future.complete(gesamtpreis.getBetrag());
                } else {
                    future.complete(0.0);
                }
                scheduler.shutdown();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return future;
    }

    /**
     * Erstellt einen Verkaufsauftrag fuer eine Aktie und verkauft sobald ein bestimmter preis erreicht wurde
     * @param wkn Wertpapierkennnummer
     * @param minimalpreis Preis, bei dem verkauft wird
     * @return Geldbetrag in double, der als Erlös zurück kam
     */
    public Future<Double> verkaufsauftrag(String wkn, Geldbetrag minimalpreis) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        Integer anzahl = depot.get(wkn);
        if (anzahl == null || anzahl == 0) {
            future.complete(0.0);
            return future;
        }

        Aktie aktie = aktienDatenbank.get(wkn);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            // Wenn Aktie über bestimmten minimalwert ist, wird verkauft
            if (aktie.getKurs() >= minimalpreis.getBetrag()) {
                Geldbetrag gesamtgewinn = new Geldbetrag(aktie.getKurs() * anzahl);
                einzahlen(gesamtgewinn);
                depot.remove(wkn);
                future.complete(gesamtgewinn.getBetrag());
                scheduler.shutdown();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        return future;
    }

    /**
     * Methode, die Geld von Konto abhebt
     * @param betrag abzuhebender Betrag
     * @return ob abheben geklappt hat
     * @throws GesperrtException
     */
    @Override
    public boolean abheben(Geldbetrag betrag) throws GesperrtException{
        if (betrag == null || betrag.isNegativ()) {
            throw new IllegalArgumentException("Betrag ungültig");
        }
        if(this.isGesperrt())
            throw new GesperrtException(this.getKontonummer());
        if (!getKontostand().minus(betrag).isNegativ())
        {
            setKontostand(getKontostand().minus(betrag));
            return true;
        }
        else
            return false;
    }

    public void setAktienDatenbank(Aktie aktie) {
        aktienDatenbank.put(aktie.getWKN(), aktie);
    }

    public void shutdown() {
        aktienDatenbank.clear();
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        // Big Tech Aktien weil AI so boomed
        Aktie apple = new Aktie("865985", 150.0);
        Aktie microsoft = new Aktie("870747", 100.0);
        Aktie google = new Aktie("A14Y6F", 200.0);

        //  Aktien zu der Aktiendatenbank hinzugefügt
        Aktienkonto konto = new Aktienkonto();
        konto.setAktienDatenbank(apple);
        konto.setAktienDatenbank(microsoft);
        konto.setAktienDatenbank(google);
        // Sattes Startkapital
        konto.einzahlen(new Geldbetrag(100000));

        // Schleife, die immer aktuellen Kurs anzeigt
        ScheduledExecutorService anzeige = Executors.newSingleThreadScheduledExecutor();
        anzeige.scheduleAtFixedRate(() -> {
            System.out.println("Aktuelle Kurse:");
            System.out.printf("AAPL: %.2f\n", apple.getKurs());
            System.out.printf("MSFT: %.2f\n", microsoft.getKurs());
            System.out.printf("GOOGL: %.2f\n", google.getKurs());
            System.out.println("---------------");
        }, 0, 3, TimeUnit.SECONDS);

        // Kaufaufträge
        Future<Double> kauf1 = konto.kaufauftrag("865985", 3, new Geldbetrag(160.0));
        Future<Double> kauf2 = konto.kaufauftrag("870747", 2, new Geldbetrag(105.0));
        Future<Double> kauf3 = konto.kaufauftrag("A14Y6F", 1, new Geldbetrag(201.0));

        try {
            Double preis1 = kauf1.get();
            System.out.println("AAPL gekauft fuer: " + preis1);

            Double preis2 = kauf2.get();
            System.out.println("MSFT gekauft fuer: " + preis2);

            Double preis3 = kauf3.get();
            System.out.println("GOOGL gekauft fuer: " + preis3);

            // Verkaufsauftraege
            Future<Double> verkauf1 = konto.verkaufsauftrag("865985", new Geldbetrag(165.0));
            Future<Double> verkauf2 = konto.verkaufsauftrag("870747", new Geldbetrag(108.0));
            Future<Double> verkauf3 = konto.verkaufsauftrag("A14Y6F", new Geldbetrag(2014.0));
            Double verkaufspreis1 = verkauf1.get();
            Double verkaufspreis2 = verkauf2.get();
            Double verkaufspreis3 = verkauf3.get();
            System.out.println("Verkaufserlös AAPL: " + verkaufspreis1);
            System.out.println("Verkaufserlös MSFT: " + verkaufspreis2);
            System.out.println("Verkaufserlös GOOGL: " + verkaufspreis3);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Fehler bei der Ausführung: " + e.getMessage());
        } finally {
            // Aufräumen
            anzeige.shutdown();
            konto.shutdown();
        }
    }
}