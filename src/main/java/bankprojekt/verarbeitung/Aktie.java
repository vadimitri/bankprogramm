package bankprojekt.verarbeitung;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Aktie {
    private static Map<String, Aktie> alleAktien = new HashMap<>();
    private String wkn;
    private Geldbetrag kurs;
    private final Random random;
    private final ScheduledExecutorService executor;
    private Condition kursHoch;
    private Condition kursRunter;
    private Lock aktienlock;

    /**
     * gibt die Aktie mit der gewünschten Wertpapierkennnummer zurück
     * @param wkn Wertpapierkennnummer
     * @return Aktie mit der angegebenen Wertpapierkennnummer oder null, wenn es diese WKN
     * 			nicht gibt.
     */
    public static Aktie getAktie(String wkn) {
        return alleAktien.get(wkn);
    }

    /**
     * erstellt eine neu Aktie mit den angegebenen Werten
     * @param wkn Wertpapierkennnummer
     * @param k aktueller Kurs
     * @throws IllegalArgumentException wenn einer der Parameter null bzw. negativ ist
     * 		                            oder es eine Aktie mit dieser WKN bereits gibt
     */
    public Aktie(String wkn, Geldbetrag k) {
        if(wkn == null || k == null || k.isNegativ() || alleAktien.containsKey(wkn))
            throw new IllegalArgumentException();
        this.wkn = wkn;
        this.kurs = k;
        this.random = new Random();
        this.aktienlock = new ReentrantLock();
        this.kursHoch = aktienlock.newCondition();
        this.kursRunter = aktienlock.newCondition();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        alleAktien.put(wkn, this);
        int zeit = random.nextInt(6) + 1;
        executor.scheduleAtFixedRate(this::kursaendern, 0, zeit, TimeUnit.SECONDS );


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
     * Wertpapierkennnummer
     * @return WKN der Aktie
     */
    public String getWkn() {
        return wkn;
    }

    /**
     * aktueller Kurs
     * @return Kurs der Aktie
     */
    public Geldbetrag getKurs() {
        return kurs;
    }


    public Condition getKursHoch() {
        return kursHoch;
    }

    public Condition getKursRunter() {
        return kursRunter;
    }

    public Lock getAktienLock() {
        return aktienlock;
    }

    public Map<String, Aktie> getAlleAktien() {
        return null;
    }

    public void shutdown() {
    }
}
