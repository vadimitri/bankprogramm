package bankprojekt.verarbeitung;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Aktie {
    private String wkn;
    private double kurs;
    private final ScheduledExecutorService executor;
    private final Random random;
    private Condition kursHoch;
    private Condition kursRunter;
    private Lock aktienlock;

    /**
     * Konstruktor für eine Aktie.
     * @param wkn Wertpapierkennnummer
     * @param initialKurs Kurs, mit dem die Aktie "anfängt".
     */
    public Aktie(String wkn, double initialKurs) {

        this.wkn = wkn;
        this.kurs = initialKurs;
        this.random = new Random();
        this.aktienlock = new ReentrantLock();
        this.kursHoch = aktienlock.newCondition();
        this.kursRunter = aktienlock.newCondition();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        kusaenderung = executor.scheduleAtFixedRate(() -> this.kursaendern());

        startKursAenderungen();
    }

    public void kursaendern() {
        double veraenderung = Math.random()*6-3;
        aktienlock.lock();
        double kurs = this.kurs.getBetrag() * (100 + veraenderung)/100;
        this.kurs = new Geldbetrag(kurs, this.kurs.getWaehrung());
        if(veraenderung > 0) {
            kursHoch.signalAll();
        } if (veraenderung < 0) {
            kursRunter.signalAll();
        }
        aktienlock.unlock();
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
