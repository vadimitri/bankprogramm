package bankprojekt.verarbeitung;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Aktienkonto extends Konto {
    // Map des Depots mit wkn und Anzahl der gekauften Aktien
    private HashMap<String, Integer> depot;
    // Alle kaufbaren Aktien
    ScheduledExecutorService scheduler;
    // Konto lock
    private ReentrantLock kontolock = new ReentrantLock();


    /**
     * Erstellt ein Aktiendepot, mit dem man Aktien kaufen kann.
     */
    public Aktienkonto() {
        super();
        this.depot = new HashMap<>(); // ConcurrentHashMap
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    /**
     * Erstellt einen Kaufauftrag fuer eine Aktie und kauft sobald ein bestimmter preis erreicht wurde
     * @param wkn Wertpapierkennnummer
     * @param anzahl Anzahl der zu kaufenden Aktien
     * @param hoechstpreis Preis, bei dem gekauft wird
     * @return Geldbetrag, der ausgegeben wurde
     */
    public Future<Double> kaufauftrag(String wkn, int anzahl, Geldbetrag hoechstpreis) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        return scheduler.schedule(() -> {
                    Aktie aktie = Aktie.getAktie(wkn);
                    if (aktie == null)
                        return 0.0;
                    while (aktie.getKurs().compareTo(hoechstpreis) > 0) {
                        aktie.getAktienLock().lock();
                        aktie.getKursHoch().await();
                        aktie.getAktienLock().unlock();
                    }
                    Geldbetrag kaufKosten = new Geldbetrag(aktie.getKurs().getBetrag() * anzahl,
                            aktie.getKurs().getWaehrung());
                    depot.put(wkn, anzahl);
                    return abheben(kaufKosten) ? kaufKosten.getBetrag() : 0.0;
                },
                100, TimeUnit.MILLISECONDS);
    }

    /**
     * Erstellt einen Verkaufsauftrag fuer eine Aktie und verkauft sobald ein bestimmter preis erreicht wurde
     * @param wkn Wertpapierkennnummer
     * @param minimalpreis Preis, bei dem verkauft wird
     * @return Geldbetrag in double, der als Erlös zurück kam
     */
    public Future<Double> verkaufsauftrag(String wkn, Geldbetrag minimalpreis) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        return scheduler.schedule(() -> {

            Aktie aktie = Aktie.getAktie(wkn);
            if (!depot.containsKey(wkn) || depot.get(wkn) <= 0 || aktie == null) {
                return .0;
            }

            while (aktie.getKurs().compareTo(minimalpreis) < 0) {
                aktie.getAktienLock().lock();
                aktie.getKursRunter().await();
                aktie.getAktienLock().unlock();
            }
            kontolock.lock();
            Geldbetrag gewinn = new Geldbetrag(aktie.getKurs().getBetrag() * depot.get(wkn),
                    aktie.getKurs().getWaehrung());
            einzahlen(gewinn);
            depot.put(wkn, 0);
            kontolock.unlock();
            return gewinn.getBetrag();
        }, 0, TimeUnit.MILLISECONDS);
    }

    protected Geldbetrag getGrenzwert(Geldbetrag betrag) {
        return getKontostand();
    }



    public static void main(String[] args) {
        // Big Tech Aktien weil AI so boomed
        Aktie apple = new Aktie("865985", new Geldbetrag(150.0));
        Aktie microsoft = new Aktie("870747", new Geldbetrag(100.0));
        Aktie google = new Aktie("A14Y6F", new Geldbetrag(200.0));

        Aktienkonto konto = new Aktienkonto();
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
            Future<Double> verkauf1 = konto.verkaufsauftrag("865985", new Geldbetrag(154.0));
            Future<Double> verkauf2 = konto.verkaufsauftrag("870747", new Geldbetrag(101.0));
            Future<Double> verkauf3 = konto.verkaufsauftrag("A14Y6F", new Geldbetrag(200.0));
            Double verkaufspreis1 = verkauf1.get();
            Double verkaufspreis2 = verkauf2.get();
            Double verkaufspreis3 = verkauf3.get();
            System.out.println("Verkaufserlös AAPL: " + verkaufspreis1);
            System.out.println("Verkaufserlös MSFT: " + verkaufspreis2);
            System.out.println("Verkaufserlös GOOGL: " + verkaufspreis3);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Fehler: " + e.getMessage());
        } finally {
            anzeige.shutdown();
        }
    }
}