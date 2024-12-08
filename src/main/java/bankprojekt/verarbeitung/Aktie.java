package bankprojekt.verarbeitung;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Aktie {
    private String wkn;
    private double kurs;
    private final ScheduledExecutorService executor;
    private final Random random;

    /**
     * Konstruktor für eine Aktie.
     * @param wkn Wertpapierkennnummer
     * @param initialKurs Kurs, mit dem die Aktie "anfängt".
     */
    public Aktie(String wkn, double initialKurs) {
        this.wkn = wkn;
        this.kurs = initialKurs;
        this.random = new Random();
        this.executor = Executors.newSingleThreadScheduledExecutor();

        // Starte periodische Kursänderungen
        startKursAenderungen();
    }

    /**
     * Methode, die den Kurs auf unvorhersehbare Weise ändert
     */
    private void startKursAenderungen() {
        int zeit = random.nextInt(6) + 1;

        executor.scheduleAtFixedRate(() -> {
            double kursaenderung = random.nextDouble() * 6 - 3;
            kurs = kurs * (1 + kursaenderung / 100);
        }, 0, zeit, TimeUnit.SECONDS);
    }

    public String getWKN() {
        return wkn;
    }

    public double getKurs() {
        return kurs;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
