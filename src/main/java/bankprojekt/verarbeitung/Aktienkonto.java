package bankprojekt.verarbeitung;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Aktienkonto extends Konto {
    // Map des Depots mit wkn und Anzahl der gekauften Aktien
    private Map<String, Integer> depot;
    // Alle kaufbaren Aktien
    private Map<String, Aktie> aktienDatenbank;
    private final ExecutorService executorService;
    ScheduledExecutorService scheduler;


    public Aktienkonto() {
        super();
        this.depot = new HashMap<>();
        this.aktienDatenbank = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public Future<Double> kaufauftrag(String wkn, int anzahl, Geldbetrag hoechstpreis) {

        CompletableFuture<Double> future = new CompletableFuture<>();

        Aktie aktie = aktienDatenbank.get(wkn);
        if (aktie == null) {
            future.complete(0.0);
            return future;
        }

        scheduler.scheduleWithFixedDelay(() -> {
            if (aktie.getKurs() <= hoechstpreis.getBetrag()) {
                Geldbetrag gesamtpreis = new Geldbetrag(aktie.getKurs() * anzahl);
                if (getKontostand().compareTo(gesamtpreis) >= 0) {
                    try {
                        abheben(gesamtpreis);
                    } catch (GesperrtException e) {
                        throw new RuntimeException(e);
                    }
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

    public Future<Double> verkaufauftrag(String wkn, Geldbetrag minimalpreis) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        Integer anzahl = depot.get(wkn);
        if (anzahl == null || anzahl == 0) {
            future.complete(0.0);
            return future;
        }

        Aktie aktie = aktienDatenbank.get(wkn);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
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

    public static void main(String[] args) {
        // Aktien erstellen
        Aktie apple = new Aktie("AAPL", 150.0);
        Aktie microsoft = new Aktie("MSFT", 300.0);
        Aktie google = new Aktie("GOOGL", 2500.0);


        // Aktienkonto erstellen
        Aktienkonto konto = new Aktienkonto();
        konto.setAktienDatenbank(apple);
        konto.setAktienDatenbank(microsoft);
        konto.setAktienDatenbank(google);
        konto.einzahlen(new Geldbetrag(10000));

        // Kaufaufträge


        // Thread für Kursanzeige
        ScheduledExecutorService anzeige = Executors.newSingleThreadScheduledExecutor();
        anzeige.scheduleAtFixedRate(() -> {
            System.out.println("Aktuelle Kurse:");
            System.out.printf("AAPL: %.2f\n", apple.getKurs());
            System.out.printf("MSFT: %.2f\n", microsoft.getKurs());
            System.out.printf("GOOGL: %.2f\n", google.getKurs());
            System.out.println("---------------");
        }, 0, 2, TimeUnit.SECONDS);

        Future<Double> kauf1 = konto.kaufauftrag("AAPL", 10, new Geldbetrag(155.0));
        Future<Double> kauf2 = konto.kaufauftrag("MSFT", 5, new Geldbetrag(305.0));

        // Warte auf Kaufabschluss und zeige Ergebnisse
        try {
            System.out.println("Kaufpreis AAPL: " + kauf1.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Kaufpreis MSFT: " + kauf2.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Verkaufauftrag
        Future<Double> verkauf = konto.verkaufauftrag("AAPL", new Geldbetrag(160.0));
        try {
            System.out.println("Verkaufserlös AAPL: " + verkauf.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Aufräumen
        anzeige.shutdown();
    }
}