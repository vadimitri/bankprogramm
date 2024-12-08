package bankprojekt.verarbeitung;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Aktienkonto extends Konto {
    // Map des Depots mit wkn und Anzahl der gekauften Aktien
    private Map<String, Integer> depot;
    // Alle kaufbaren Aktien
    private Map<String, Aktie> aktienDatenbank;
    private final ScheduledExecutorService scheduler;

    public Aktienkonto() {
        super();
        this.depot = new HashMap<>();
        this.aktienDatenbank = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public Future<Double> kaufauftrag(String wkn, int anzahl, Geldbetrag hoechstpreis) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        Aktie aktie = aktienDatenbank.get(wkn);
        if (aktie == null) {
            future.complete(0.0);
            return future;
        }

        AtomicBoolean transactionCompleted = new AtomicBoolean(false);
        ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(() -> {
            // Überprüfe, ob die Transaktion bereits abgeschlossen ist
            if (transactionCompleted.get()) {
                return;
            }

            try {
                if (aktie.getKurs() <= hoechstpreis.getBetrag()) {
                    Geldbetrag gesamtpreis = new Geldbetrag(aktie.getKurs() * anzahl);
                    
                    // Führe die Transaktion nur aus, wenn sie noch nicht abgeschlossen ist
                    if (getKontostand().compareTo(gesamtpreis) >= 0 && !transactionCompleted.get()) {
                        synchronized (this) {
                            if (!transactionCompleted.get()) {
                                abheben(gesamtpreis);
                                depot.merge(wkn, anzahl, Integer::sum);
                                future.complete(gesamtpreis.getBetrag());
                                transactionCompleted.set(true);
                            }
                        }
                    } else if (!transactionCompleted.get()) {
                        future.complete(0.0);
                        transactionCompleted.set(true);
                    }
                }
            } catch (GesperrtException e) {
                if (!transactionCompleted.get()) {
                    future.completeExceptionally(e);
                    transactionCompleted.set(true);
                }
            } catch (Exception e) {
                if (!transactionCompleted.get()) {
                    future.completeExceptionally(e);
                    transactionCompleted.set(true);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        // Registriere einen Callback für das Aufräumen
        future.whenComplete((result, ex) -> {
            scheduledTask.cancel(false);
        });

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
        AtomicBoolean transactionCompleted = new AtomicBoolean(false);
        ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(() -> {
            if (transactionCompleted.get()) {
                return;
            }

            if (aktie.getKurs() >= minimalpreis.getBetrag()) {
                synchronized (this) {
                    if (!transactionCompleted.get()) {
                        Geldbetrag gesamtgewinn = new Geldbetrag(aktie.getKurs() * anzahl);
                        einzahlen(gesamtgewinn);
                        depot.remove(wkn);
                        future.complete(gesamtgewinn.getBetrag());
                        transactionCompleted.set(true);
                    }
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        future.whenComplete((result, ex) -> {
            scheduledTask.cancel(false);
        });

        return future;
    }

    @Override
    public boolean abheben(Geldbetrag betrag) throws GesperrtException {
        if (betrag == null || betrag.isNegativ()) {
            throw new IllegalArgumentException("Betrag ungültig");
        }
        if (this.isGesperrt())
            throw new GesperrtException(this.getKontonummer());
        if (!getKontostand().minus(betrag).isNegativ()) {
            setKontostand(getKontostand().minus(betrag));
            return true;
        } else
            return false;
    }

    public void setAktienDatenbank(Aktie aktie) {
        aktienDatenbank.put(aktie.getWKN(), aktie);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
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

        // Thread für Kursanzeige
        ScheduledExecutorService anzeige = Executors.newSingleThreadScheduledExecutor();
        anzeige.scheduleAtFixedRate(() -> {
            System.out.println("Aktuelle Kurse:");
            System.out.printf("AAPL: %.2f\n", apple.getKurs());
            System.out.printf("MSFT: %.2f\n", microsoft.getKurs());
            System.out.printf("GOOGL: %.2f\n", google.getKurs());
            System.out.println("---------------");
        }, 0, 2, TimeUnit.SECONDS);

        // Kaufaufträge
        Future<Double> kauf1 = konto.kaufauftrag("AAPL", 10, new Geldbetrag(155.0));
        Future<Double> kauf2 = konto.kaufauftrag("MSFT", 5, new Geldbetrag(305.0));

        try {
            Double preis1 = kauf1.get(10, TimeUnit.SECONDS);
            System.out.println("Kaufpreis AAPL: " + preis1);
            
            Double preis2 = kauf2.get(10, TimeUnit.SECONDS);
            System.out.println("Kaufpreis MSFT: " + preis2);

            // Verkaufauftrag
            Future<Double> verkauf = konto.verkaufauftrag("AAPL", new Geldbetrag(160.0));
            Double verkaufspreis = verkauf.get(10, TimeUnit.SECONDS);
            System.out.println("Verkaufserlös AAPL: " + verkaufspreis);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Fehler bei der Ausführung: " + e.getMessage());
        } finally {
            // Aufräumen
            anzeige.shutdown();
            konto.shutdown();
        }
    }
}