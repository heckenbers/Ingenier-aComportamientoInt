import es.ucm.fdi.ici.c2627.practica2.grupoHEC.Ghosts;   // nuestros fantasmas (práctica 2)
import es.ucm.fdi.ici.rivales.*;                         // MsPacManBasico, MsPacManIntermedio, MsPacManAvanzado
import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.controllers.PacmanController;
import pacman.game.util.Stats;

public class ExecutorTest {

    // true  -> una partida visual (para ver cómo se mueven MsPacMan y los fantasmas)
    // false -> experimento de TRIALS partidas sin ventana, con la media de puntos
    private static final boolean VISUAL = false;

    private static final int TRIALS = 500;  // partidas del experimento
    private static final int DELAY = 70;    // ms entre ticks en modo visual (más alto = más lento)

    public static void main(String[] args) {
        PacmanController pacMan = new MsPacManAvanzado();   // o new MsPacManBasico() / new MsPacManAvanzado()
        GhostController ghosts = new Ghosts();

        if (VISUAL) runVisual(pacMan, ghosts);
        else        runExperiment(pacMan, ghosts);
    }

    // Una sola partida con ventana
    private static void runVisual(PacmanController pacMan, GhostController ghosts) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(true)
                .setScaleFactor(2.5)
                .build();

        int score = executor.runGame(pacMan, ghosts, DELAY);
        System.out.println("Puntuación: " + score);
    }

    // TRIALS partidas seguidas sin ventana y estadísticas
    private static void runExperiment(PacmanController pacMan, GhostController ghosts) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(false)
                .build();

        String rival = pacMan.getClass().getSimpleName();

        // stats[0] = puntuaciones, stats[1] = ticks del último nivel de cada partida
        Stats[] stats = executor.runExperiment(pacMan, ghosts, TRIALS, rival + " vs Ghosts grupoHEC");
        Stats score = stats[0];
        Stats ticks = stats[1];

        System.out.println();
        System.out.println("===== " + rival + " vs Ghosts grupoHEC (" + score.getN() + " partidas) =====");
        System.out.printf("Puntuación media:   %.2f%n", score.getAverage());
        System.out.printf("Desviación típica:  %.2f%n", score.getStandardDeviation());
        System.out.printf("Mínimo / Máximo:    %.0f / %.0f%n", score.getMin(), score.getMax());
        System.out.printf("Ticks medios:       %.2f%n", ticks.getAverage());
        System.out.printf("Tiempo total:       %d ms%n", score.getMsTaken());
    }
}