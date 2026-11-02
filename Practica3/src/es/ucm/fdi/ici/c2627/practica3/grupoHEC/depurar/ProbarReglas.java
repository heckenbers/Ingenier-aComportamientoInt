package es.ucm.fdi.ici.c2627.practica3.grupoHEC.depurar;

import java.util.Iterator;

import jess.Fact;
import jess.JessException;
import jess.Rete;

/**
 * Prueba las reglas sin jugar: se escribe una situación a mano, como hechos,
 * y se mira qué ACTION gana. Cada caso es una pregunta con su respuesta esperada.
 *
 * Cada .clp tiene sus propias plantillas: los casos de Ms. Pac-Man solo valen con su .clp y
 * los de los fantasmas con el suyo. PRUEBO_FANTASMAS elige qué se prueba.
 */
public class ProbarReglas {

    static final boolean PRUEBO_FANTASMAS = true;

    static final String RUTA = "es/ucm/fdi/ici/c2627/practica3/grupoHEC/";
    static final String REGLAS_PACMAN = RUTA + "mspacman-v1.clp";
    static final String REGLAS_FANTASMAS = RUTA + "ghosts-v5.clp";     // la versión que se prueba

    static int fallos = 0;

    public static void main(String[] args) throws JessException {
        Rete jess = new Rete();
        jess.batch(PRUEBO_FANTASMAS ? REGLAS_FANTASMAS : REGLAS_PACMAN);
        jess.eval("(watch rules)");           // Jess escribe cada regla que dispara

        if (PRUEBO_FANTASMAS) casosFantasmas(jess);
        else casosPacman(jess);

        System.out.println(fallos == 0 ? "\nTodos los casos bien." : "\n" + fallos + " casos MAL.");
    }

    // ==================================================================================
    // Fantasmas
    // ==================================================================================

    /** Casos de las reglas de ghosts-vN.clp. Todos desde el punto de vista de BLINKY. */
    static void casosFantasmas(Rete jess) throws JessException {
        String yo = "(YO (nombre BLINKY))";
        String ultima = "(ULTIMA (accion ninguna))";
        String pacmanLejosDePP = "(MSPACMAN (distPPill 100) (enPasillo false))";
        String pacmanJuntoAPP = "(MSPACMAN (distPPill 20) (enPasillo false))";

        // ---- v2 ----
        caso(jess, "G1 lejos de todo: perseguir", "perseguir",
                yo, ultima, pacmanLejosDePP, fantasma("BLINKY", false, 40, 120));

        caso(jess, "G2 comestible: huida eficiente", "huidaEficiente",
                yo, ultima, pacmanLejosDePP, fantasma("BLINKY", true, 15, 120));

        caso(jess, "G3 PP amenazada y yo cerca: huir de la PP", "huirDePP",
                yo, ultima, pacmanJuntoAPP, fantasma("BLINKY", false, 40, 30));

        caso(jess, "G3 PP amenazada pero yo lejos: perseguir", "perseguir",
                yo, ultima, pacmanJuntoAPP, fantasma("BLINKY", false, 40, 90));

        // ---- v3 ----
        caso(jess, "G4 llego con ventaja de sobra y soy el mejor: bloquear", "bloquearPP",
                yo, ultima, pacmanJuntoAPP, bloqueo("BLINKY", 6), bloqueo("PINKY", 2));

        caso(jess, "G4 llego con ventaja pero otro tiene mas: huir de la PP", "huirDePP",
                yo, ultima, pacmanJuntoAPP, bloqueo("BLINKY", 6), bloqueo("PINKY", 9));

        caso(jess, "G4 llego antes pero con poca ventaja: huir de la PP", "huirDePP",
                yo, ultima, pacmanJuntoAPP, bloqueo("BLINKY", 2));

        caso(jess, "G5 ya bloqueaba y sigo llegando antes: sigo", "bloquearPP",
                yo, "(ULTIMA (accion bloquearPP))", pacmanJuntoAPP, bloqueo("BLINKY", 1), bloqueo("PINKY", 9));

        caso(jess, "G5 ya bloqueaba pero ella llega antes: huir de la PP", "huirDePP",
                yo, "(ULTIMA (accion bloquearPP))", pacmanJuntoAPP, bloqueo("BLINKY", -2));

        // ---- v4 ----
        String libre = "(MSPACMAN (distPPill 150) (enPasillo false))";   // fuera de la ventana de vigilar (100)
        String enPasillo = "(MSPACMAN (distPPill 150) (enPasillo true))";

        caso(jess, "P+G1 soy el mas cercano: perseguir", "perseguir",
                yo, ultima, libre, caza("BLINKY", 30, -5, -1000), caza("PINKY", 50, -5, -1000));

        caso(jess, "G6 otro esta mas cerca y no puedo cortar: cazar por otro lado", "cazar",
                yo, ultima, libre, caza("BLINKY", 50, -5, -1000), caza("PINKY", 30, -5, -1000));

        caso(jess, "G7 otro persigue y yo me adelanto a ella: interceptar", "interceptar",
                yo, ultima, libre, caza("BLINKY", 50, 4, -1000), caza("PINKY", 30, -5, -1000));

        caso(jess, "G7 otro (no perseguidor) se adelanta mas: cazar", "cazar",
                yo, ultima, libre, caza("BLINKY", 50, 4, -1000), caza("PINKY", 30, -5, -1000),
                caza("INKY", 60, 7, -1000));

        caso(jess, "G8 ella en un pasillo y llego antes a la salida: cerrar paso", "cerrarPaso",
                yo, ultima, enPasillo, caza("BLINKY", 30, -5, 3), caza("PINKY", 50, -5, -1000));

        caso(jess, "G8 otro llega mejor a la salida: no cierro (persigo)", "perseguir",
                yo, ultima, enPasillo, caza("BLINKY", 30, -5, 3), caza("PINKY", 50, -5, 6));

        // ---- v5 (BLINKY comestible, con mucho tiempo salvo en G13) ----
        String dispersaba = "(ULTIMA (accion dispersarse))";
        String refugiaba = "(ULTIMA (accion refugiarse))";

        caso(jess, "G9 comestible y ella lejos: dispersarse", "dispersarse",
                yo, ultima, libre, comestible("BLINKY", 60, 150, "NINGUNO"));

        caso(jess, "G2 comestible, ella a media distancia y sin compañero: huida eficiente", "huidaEficiente",
                yo, ultima, libre, comestible("BLINKY", 35, 150, "NINGUNO"));

        caso(jess, "G11 ella a 25 y tengo compañero: refugiarse", "refugiarse",
                yo, ultima, libre, comestible("BLINKY", 25, 150, "PINKY"), caza("PINKY", 40, -5, -1000));

        caso(jess, "G10 ya me dispersaba y ella a 35: sigo dispersandome (aunque haya compañero)", "dispersarse",
                yo, dispersaba, libre, comestible("BLINKY", 35, 150, "PINKY"), caza("PINKY", 40, -5, -1000));

        caso(jess, "G10 ya me dispersaba pero ella a 25: refugiarse", "refugiarse",
                yo, dispersaba, libre, comestible("BLINKY", 25, 150, "PINKY"), caza("PINKY", 40, -5, -1000));

        caso(jess, "G12 ya me refugiaba y ella a 45: sigo refugiandome", "refugiarse",
                yo, refugiaba, libre, comestible("BLINKY", 45, 150, "PINKY"), caza("PINKY", 40, -5, -1000));

        caso(jess, "G12 ya me refugiaba pero ella a 55: dispersarse", "dispersarse",
                yo, refugiaba, libre, comestible("BLINKY", 55, 150, "PINKY"), caza("PINKY", 40, -5, -1000));

        caso(jess, "G13 me quedan 10 turnos y ella a 30 (> 2*10+4): volver a cazar", "cazar",
                yo, ultima, libre, comestible("BLINKY", 30, 10, "NINGUNO"));

        caso(jess, "G13 me quedan 10 turnos y ella a 20: aun no, huida eficiente", "huidaEficiente",
                yo, ultima, libre, comestible("BLINKY", 20, 10, "NINGUNO"));

        caso(jess, "G13 poco tiempo pero ella junto a otra PP: huida eficiente", "huidaEficiente",
                yo, ultima, pacmanJuntoAPP, comestible("BLINKY", 30, 10, "NINGUNO"));

        caso(jess, "G14 PINKY comestible se refugia en mi: escoltar", "refugiarse",
                yo, ultima, libre, caza("BLINKY", 30, -5, -1000), comestible("PINKY", 25, 150, "BLINKY"));

        caso(jess, "G14 PINKY se refugia en INKY, no en mi: perseguir", "perseguir",
                yo, ultima, libre, caza("BLINKY", 30, -5, -1000), caza("INKY", 50, -5, -1000),
                comestible("PINKY", 25, 150, "INKY"));

        // ---- v6 (PINKY, el más cercano, es el perseguidor salvo que se diga otra cosa) ----
        String haciaLaPP = "(MSPACMAN (distPPill 50) (enPasillo false))";
        String vigilaba = "(ULTIMA (accion vigilarPP))";

        caso(jess, "G15 ella va hacia la PP y yo llego antes que nadie: vigilar", "vigilarPP",
                yo, ultima, haciaLaPP, vigia("BLINKY", 50, 8), vigia("PINKY", 30, 1));

        caso(jess, "G15 otro (no perseguidor) llega con mas margen: cazar", "cazar",
                yo, ultima, haciaLaPP, vigia("BLINKY", 50, 8), vigia("PINKY", 30, 1), vigia("INKY", 60, 12));

        caso(jess, "G15 soy el perseguidor: perseguir (no vigilo)", "perseguir",
                yo, ultima, haciaLaPP, vigia("BLINKY", 30, 8), vigia("PINKY", 50, 2));

        caso(jess, "G15 ella lejos de su PP: cazar", "cazar",
                yo, ultima, libre, vigia("BLINKY", 50, 8), vigia("PINKY", 30, 1));

        caso(jess, "G15 ella llega antes que yo a la PP: cazar", "cazar",
                yo, ultima, haciaLaPP, vigia("BLINKY", 50, -3), vigia("PINKY", 30, 1));

        caso(jess, "G16 ya vigilaba, ella a 75 y otro tiene mas margen: sigo vigilando", "vigilarPP",
                yo, vigilaba, "(MSPACMAN (distPPill 75) (enPasillo false))",
                vigia("BLINKY", 50, 2), vigia("PINKY", 30, 1), vigia("INKY", 60, 12));

        caso(jess, "G16 ya vigilaba pero ella se aleja a 120: cazar", "cazar",
                yo, vigilaba, "(MSPACMAN (distPPill 120) (enPasillo false))",
                vigia("BLINKY", 50, 8), vigia("PINKY", 30, 1));

        // ---- v7 (la ventana de vigilar pasa de 70 a 100, y la de seguir de 80 a 110) ----
        caso(jess, "G15 ella a 90 de su PP (en v6 no, en v7 si): vigilar", "vigilarPP",
                yo, ultima, "(MSPACMAN (distPPill 90) (enPasillo false))",
                vigia("BLINKY", 50, 8), vigia("PINKY", 30, 1));

        caso(jess, "G16 ya vigilaba y ella a 105: sigo vigilando", "vigilarPP",
                yo, vigilaba, "(MSPACMAN (distPPill 105) (enPasillo false))",
                vigia("BLINKY", 50, 2), vigia("PINKY", 30, 1));

        caso(jess, "Relevo: vigilaba y ella entra a 30 de la PP: bloquear (G4)", "bloquearPP",
                yo, vigilaba, "(MSPACMAN (distPPill 30) (enPasillo false))",
                vigia("BLINKY", 50, 20), vigia("PINKY", 30, 1));
    }

    /**
     * Un hecho FANTASMA con TODOS sus slots. Se rellenan todos porque un slot numérico que
     * falta vale nil, y una comparación como (<= nil 50) hace fallar la regla.
     * Los que no importan en estos casos llevan valores "neutros" (no puede bloquear, ni
     * cortar, ni cerrar el pasillo, ni refugiarse).
     */
    static String fantasma(String nombre, boolean comestible, int distPeligro, int distPP) {
        return String.format("(FANTASMA (nombre %s) (comestible %s) (enCarcel false) (tiempoComestible %d)"
                + " (distPacman %d) (distPeligro %d) (distPP %d) (margenBloqueo -10) (margenCorte -10)"
                + " (margenCierre -1000) (companero NINGUNO))",
                nombre, comestible, comestible ? 100 : 0, distPeligro, distPeligro, distPP);
    }

    /** Un fantasma no comestible a 30 de la PP con el margen de bloqueo indicado (el resto, neutro). */
    static String bloqueo(String nombre, int margenBloqueo) {
        return String.format("(FANTASMA (nombre %s) (comestible false) (enCarcel false) (tiempoComestible 0)"
                + " (distPacman 40) (distPeligro 40) (distPP 30) (margenBloqueo %d) (margenCorte -10)"
                + " (margenCierre -1000) (companero NINGUNO))", nombre, margenBloqueo);
    }

    /** Un fantasma no comestible lejos de la PP, con lo que importa para cazar (el resto, neutro). */
    static String caza(String nombre, int distPacman, int margenCorte, int margenCierre) {
        return String.format("(FANTASMA (nombre %s) (comestible false) (enCarcel false) (tiempoComestible 0)"
                + " (distPacman %d) (distPeligro %d) (distPP 120) (margenBloqueo -10) (margenCorte %d)"
                + " (margenCierre %d) (companero NINGUNO))", nombre, distPacman, distPacman, margenCorte, margenCierre);
    }

    /** Un no comestible para vigilar: su distancia a ella y su margen para llegar a la PP antes que ella. */
    static String vigia(String nombre, int distPacman, int margenBloqueo) {
        return String.format("(FANTASMA (nombre %s) (comestible false) (enCarcel false) (tiempoComestible 0)"
                + " (distPacman %d) (distPeligro %d) (distPP 60) (margenBloqueo %d) (margenCorte -5)"
                + " (margenCierre -1000) (companero NINGUNO))", nombre, distPacman, distPacman, margenBloqueo);
    }

    /** Un fantasma comestible: lo cerca que está ella, los turnos que le quedan y su refugio. */
    static String comestible(String nombre, int distPeligro, int tiempo, String companero) {
        return String.format("(FANTASMA (nombre %s) (comestible true) (enCarcel false) (tiempoComestible %d)"
                + " (distPacman %d) (distPeligro %d) (distPP 120) (margenBloqueo -10) (margenCorte -10)"
                + " (margenCierre -1000) (companero %s))", nombre, tiempo, distPeligro, distPeligro, companero);
    }

    // ==================================================================================
    // Ms. Pac-Man (los casos originales del ejemplo, sin cambios)
    // ==================================================================================

    static void casosPacman(Rete jess) throws JessException {
        caso(jess, "nada cerca: come", "comer",
                "(MSPACMAN (distFantasma 80) (distPPill 40) (ultima comer))");

        caso(jess, "fantasma a 15: huye", "huir",
                "(MSPACMAN (distFantasma 15) (distPPill 40) (ultima comer))");

        caso(jess, "a 25 sin venir huyendo: come", "comer",
                "(MSPACMAN (distFantasma 25) (distPPill 40) (ultima comer))");

        caso(jess, "a 25 viniendo de huir: sigue huyendo", "huir",
                "(MSPACMAN (distFantasma 25) (distPPill 40) (ultima huir))");

        caso(jess, "fantasma a 15 y PPill a 5: a la PPill", "irAPPill",
                "(MSPACMAN (distFantasma 15) (distPPill 5) (ultima huir))");

        caso(jess, "comestible a 30 pero un peligroso a 15: huye", "huir",
                "(FANTASMA (nombre PINKY) (comestible true) (distancia 30) (tiempo 100))",
                "(FANTASMA (nombre SUE) (comestible false) (distancia 15) (tiempo 0))",
                "(MSPACMAN (distFantasma 15) (distPPill 1000) (ultima comer))");

        caso(jess, "comestible a 30 con tiempo: lo caza", "perseguir",
                "(MSPACMAN (distFantasma 1000) (distPPill 1000) (ultima comer))",
                "(FANTASMA (nombre PINKY) (comestible true) (distancia 30) (tiempo 100))");

        caso(jess, "comestible a 5 y otro peligroso a 15: lo remata", "perseguir",
                "(MSPACMAN (distFantasma 15) (distPPill 1000) (ultima huir))",
                "(FANTASMA (nombre PINKY) (comestible true) (distancia 5) (tiempo 100))",
                "(FANTASMA (nombre SUE) (comestible false) (distancia 15) (tiempo 0))");
    }

    // ==================================================================================

    static void caso(Rete jess, String nombre, String esperada, String... hechos) throws JessException {
        System.out.println("\n--- " + nombre);
        jess.reset();
        for (String h : hechos) jess.assertString(h);
        jess.run();

        Fact gana = null;                       // lo mismo que hace RuleEngine: la de más priority
        for (Iterator<?> it = jess.listFacts(); it.hasNext(); ) {
            Fact f = (Fact) it.next();
            if (f.getName().equals("MAIN::ACTION") && (gana == null
                    || f.getSlotValue("priority").intValue(null) > gana.getSlotValue("priority").intValue(null)))
                gana = f;
        }
        String id = gana == null ? "(ninguna)" : gana.getSlotValue("id").toString();
        boolean bien = id.equals(esperada);
        if (!bien) fallos++;
        System.out.printf("%s  gana %s, esperaba %s%n", bien ? "BIEN" : "MAL ", id, esperada);
    }
}