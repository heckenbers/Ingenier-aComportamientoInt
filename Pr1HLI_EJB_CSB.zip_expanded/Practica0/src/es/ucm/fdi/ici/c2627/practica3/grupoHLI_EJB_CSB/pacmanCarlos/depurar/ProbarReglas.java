package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.depurar;

import java.util.Iterator;

import jess.Fact;
import jess.JessException;
import jess.Rete;

/**
 * Prueba las reglas sin lanzar el juego: cada caso escribe a mano el hecho
 * MSPACMAN de un turno y comprueba qué ACTION gana (el de mayor priority,
 * como hace RuleEngine). Imprime BIEN o MAL por caso y el total de fallos.
 *
 * Se ejecuta como cualquier main. Conservad los casos antiguos: detectan en
 * un segundo cuándo una regla nueva rompe otra.
 */
public class ProbarReglas {

    /** Ruta del .clp en el classpath. Es texto: si cambia el paquete, cambiadla a mano. */
    private static final String REGLAS =
            "es/ucm/fdi/ici/c2627/practica3/grupoHLI_EJB_CSB/pacmanCarlos/mspacman-v0.clp";

    /** El LEJOS de MsPacManInput ("no hay"). */
    private static final int LEJOS = Integer.MAX_VALUE / 4;

    private int fallos = 0;
    private int casos = 0;

    public static void main(String[] args) throws JessException {
        new ProbarReglas().probar((args.length > 0) ? args[0] : REGLAS);
    }

    private void probar(String fichero) throws JessException {
        Rete jess = new Rete();
        jess.batch(fichero);
        jess.executeCommand("(watch rules)");

        caso(jess, "1. nada cerca", "IrAPildoraRentable", null,
                mspacman(100, 50, LEJOS, 0, "NONE", "IrAPildoraRentable", 0));
        caso(jess, "2. peligro: fantasma a 10 ticks, con salida", "Evadir", null,
                mspacman(10, 20, LEJOS, 0, "NONE", "IrAPildoraRentable", 0));
        caso(jess, "3. crítico: fantasma a 5 ticks", "EscapeDeEmergencia", null,
                mspacman(5, 20, LEJOS, 0, "NONE", "Evadir", 0));
        caso(jess, "4. sin salida segura aunque el fantasma esté lejos", "EscapeDeEmergencia", null,
                mspacman(40, 3, LEJOS, 0, "NONE", "IrAPildoraRentable", 0));
        caso(jess, "5. histéresis: viene de huir y lleva 2 turnos sin peligro", "Evadir", null,
                mspacman(40, 20, LEJOS, 0, "NONE", "Evadir", 2));
        caso(jess, "6. histéresis cumplida: 3 turnos sin peligro, vuelve a comer", "IrAPildoraRentable", null,
                mspacman(40, 20, LEJOS, 0, "NONE", "Evadir", 3));
        caso(jess, "7. poder con presa PINKY", "CazarFantasma", "PINKY",
                mspacman(100, 50, LEJOS, 2, "PINKY", "IrAPildoraRentable", 0));
        caso(jess, "8. poder sin presa alcanzable", "RecolectarEnPoder", null,
                mspacman(100, 50, LEJOS, 1, "NONE", "IrAPildoraRentable", 0));
        caso(jess, "9. justo fuera del umbral de alerta (13): no es peligro", "IrAPildoraRentable", null,
                mspacman(13, 20, LEJOS, 0, "NONE", "IrAPildoraRentable", 0));
        caso(jess, "10. poder pero con peligro: manda la supervivencia", "Evadir", null,
                mspacman(10, 20, LEJOS, 2, "INKY", "CazarFantasma", 0));
        caso(jess, "11. histéresis no impide cazar si hay poder", "CazarFantasma", "INKY",
                mspacman(100, 50, LEJOS, 2, "INKY", "Evadir", 1));
        caso(jess, "12. justo en el umbral de alerta (12) es peligro", "Evadir", null,
                mspacman(12, 20, LEJOS, 0, "NONE", "IrAPildoraRentable", 0));

        System.out.println();
        System.out.println(fallos == 0 ? "TODOS BIEN (" + casos + " casos)"
                : fallos + " MAL de " + casos + " casos");
    }

    /** El hecho MSPACMAN tal y como lo escribe MsPacManInput.getFacts(). */
    private static String mspacman(int amenaza, int margen, int revertir, int comestibles,
            String presa, String ultima, int ticksSinPeligro) {
        return String.format(
                "(MSPACMAN (amenaza %d) (margen %d) (revertir %d) (comestibles %d) (presa %s) (ultima %s) (ticksSinPeligro %d))",
                amenaza, margen, revertir, comestibles, presa, ultima, ticksSinPeligro);
    }

    private void caso(Rete jess, String nombre, String esperada, String fantasmaEsperado,
            String... hechos) throws JessException {
        casos++;
        System.out.println("--- " + nombre);
        jess.reset();
        for (String h : hechos)
            jess.assertString(h);
        jess.run();

        Fact ganador = null;
        int max = Integer.MIN_VALUE;
        for (Iterator<?> it = jess.listFacts(); it.hasNext();) {
            Fact f = (Fact) it.next();
            if (f.getName().equals("MAIN::ACTION")) {
                int p = f.getSlotValue("priority").intValue(null);
                if (p > max) {
                    max = p;
                    ganador = f;
                }
            }
        }
        String id = (ganador == null) ? "(ninguno)" : ganador.getSlotValue("id").toString();
        boolean bien = id.equals(esperada);
        String extra = "";
        if (bien && fantasmaEsperado != null) {
            String fantasma = ganador.getSlotValue("fantasma").toString();
            bien = fantasma.equals(fantasmaEsperado);
            extra = " (fantasma " + fantasma + ", esperaba " + fantasmaEsperado + ")";
        }
        if (!bien)
            fallos++;
        System.out.println((bien ? "BIEN" : "MAL") + " gana " + id + ", esperaba " + esperada + extra);
    }
}
