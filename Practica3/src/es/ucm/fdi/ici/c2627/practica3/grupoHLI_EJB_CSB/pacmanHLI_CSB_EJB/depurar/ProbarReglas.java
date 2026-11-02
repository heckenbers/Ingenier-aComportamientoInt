package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.depurar;

import java.util.Iterator;

import jess.Fact;
import jess.JessException;
import jess.Rete;

/**
 * Prueba las reglas sin jugar: se escribe una situación a mano, como hechos,
 * y se mira qué ACTION gana. Cada caso es una pregunta con su respuesta esperada.
 */
public class ProbarReglas {

    /** Es texto: si cambia el paquete, cambiadlo a mano. Un argumento permite probar otra versión. */
    static final String REGLAS = "es/ucm/fdi/ici/c2627/practica3/grupoHLI_EJB_CSB/pacmanHLI_CSB_EJB/mspacman-v1.clp";
    static int fallos = 0;

    public static void main(String[] args) throws JessException {
        Rete jess = new Rete();
        String fichero = args.length > 0 ? args[0] : REGLAS;
        jess.batch(fichero);
        // cada bloque de casos se prueba solo con las versiones que tienen esas reglas
        int v = Integer.parseInt(fichero.replaceAll(".*mspacman-v(\\d+)\\.clp$", "$1"));
        boolean conCebo = v >= 1, conZona = v == 2, conFinal = v >= 3;
        int umbralFinal = (v >= 4) ? 120 : 70;      // UMBRAL_FINAL de cada versión
        System.out.println("Probando " + fichero);
        jess.eval("(watch rules)");           // Jess escribe cada regla que dispara

        // ---- v0: supervivencia, poder y recolección ----

        caso(jess, "nada cerca: recolecta", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "fantasma a 10 ticks, con salida: evade", "Evadir",
                "(MSPACMAN (amenaza 10) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "fantasma a 5 ticks: escape de emergencia", "EscapeDeEmergencia",
                "(MSPACMAN (amenaza 5) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima Evadir) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "sin salida segura aunque el fantasma esté lejos: escape", "EscapeDeEmergencia",
                "(MSPACMAN (amenaza 40) (margen 3) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "viene de huir y lleva 2 turnos sin peligro: sigue evadiendo", "Evadir",
                "(MSPACMAN (amenaza 40) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima Evadir) (ticksSinPeligro 2)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "viene de huir y lleva 3 turnos sin peligro: vuelve a recolectar", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 40) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima Evadir) (ticksSinPeligro 3)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "poder con presa PINKY: caza", "CazarFantasma",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 2) (presa PINKY) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "poder sin presa alcanzable: recolecta en poder", "RecolectarEnPoder",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 1) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "amenaza 13, justo fuera del umbral: recolecta", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 13) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "poder pero fantasma a 10: manda la supervivencia", "Evadir",
                "(MSPACMAN (amenaza 10) (margen 20) (revertir 536870911) (comestibles 2) (presa INKY) (ultima CazarFantasma) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "viene de huir pero hay poder: caza", "CazarFantasma",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 2) (presa INKY) (ultima Evadir) (ticksSinPeligro 1)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "amenaza 12, justo en el umbral: evade", "Evadir",
                "(MSPACMAN (amenaza 12) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        // ---- v1: el cebo ----
        if (conCebo) {

        caso(jess, "cebo que vale 600: patrulla junto a la píldora", "PrepararCebo/patrullar",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 600) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "cebo que vale 1400: se lo come ya", "PrepararCebo/comer",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 1400) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 5) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "80 turnos esperando y vale 600: se lo come", "PrepararCebo/comer",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 600) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 80) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "cebo que vale 400 sin pocas píldoras: no merece la pena", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 400) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "pocas píldoras y 80 turnos esperando aunque valga 200: se lo come", "PrepararCebo/comer",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 200) (poderRentable true) (pocasPildoras true)"
                + " (ticksEnCebo 80) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "cebo en enfriamiento: recolecta", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 800) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento true))");

        caso(jess, "ya en el cebo, el valor baja a 200 hace 10 turnos: sigue patrullando", "PrepararCebo/patrullar",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 200) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 30) (ticksSinValorCebo 10) (ceboEnfriamiento false))");

        caso(jess, "ya en el cebo, 40 turnos sin valor: lo abandona", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 200) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 60) (ticksSinValorCebo 40) (ceboEnfriamiento false))");

        caso(jess, "ya en el cebo y la píldora deja de ser candidata: lo abandona", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 20) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "cebo rentable pero fantasma a 10: evade", "Evadir",
                "(MSPACMAN (amenaza 10) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 1400) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        caso(jess, "se acaba de comer el cebo y hay presa: caza", "CazarFantasma",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 3) (presa BLINKY) (ultima PrepararCebo) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 600) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false))");

        }

        // ---- v2: la zona aislada (descartada: la v3 parte de la v1) ----
        if (conZona) {

        caso(jess, "zona aislada y fantasma a 40: va a limpiarla", "LimpiarZonaAislada",
                "(MSPACMAN (amenaza 40) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 6))");

        caso(jess, "zona aislada pero fantasma a 20 (margen no holgado): recolecta", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 20) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 6))");

        caso(jess, "ya limpiando y el fantasma baja a 20: sigue limpiando", "LimpiarZonaAislada",
                "(MSPACMAN (amenaza 20) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima LimpiarZonaAislada) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 4))");

        caso(jess, "ya limpiando y se acaba el clúster: recolecta", "IrAPildoraRentable",
                "(MSPACMAN (amenaza 40) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima LimpiarZonaAislada) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0))");

        caso(jess, "zona aislada y cebo a la vez, sin estar limpiando: gana el cebo", "PrepararCebo/patrullar",
                "(MSPACMAN (amenaza 40) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 600) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 6))");

        caso(jess, "limpiando y aparece un cebo: sigue limpiando", "LimpiarZonaAislada",
                "(MSPACMAN (amenaza 40) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima LimpiarZonaAislada) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 1400) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 4))");

        caso(jess, "limpiando con un fantasma a 10: evade", "Evadir",
                "(MSPACMAN (amenaza 10) (margen 30) (revertir 536870911) (comestibles 0) (presa NONE) (ultima LimpiarZonaAislada) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 4))");

        caso(jess, "limpiando y hay poder con presa: caza", "CazarFantasma",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 2) (presa SUE) (ultima LimpiarZonaAislada) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 4))");

        }

        // ---- v3: final de nivel ----
        if (conFinal) {
        caso(jess, "quedan UMBRAL_FINAL + 1 píldoras: aún recolecta en modo normal", "IrAPildoraRentable/patrullar",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0) (pildorasRestantes " + (umbralFinal + 1) + "))");

        caso(jess, "quedan UMBRAL_FINAL píldoras: recolecta en modo final", "IrAPildoraRentable/final",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0) (pildorasRestantes " + umbralFinal + "))");

        caso(jess, "final de nivel con un fantasma a 10: evade", "Evadir",
                "(MSPACMAN (amenaza 10) (margen 20) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0) (pildorasRestantes 30))");

        caso(jess, "final de nivel y cebo que vale 600: manda el cebo", "PrepararCebo/patrullar",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 0) (presa NONE) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo true) (valorCebo 600) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0) (pildorasRestantes 30))");

        caso(jess, "final de nivel con poder y presa: caza", "CazarFantasma",
                "(MSPACMAN (amenaza 100) (margen 50) (revertir 536870911) (comestibles 2) (presa PINKY) (ultima IrAPildoraRentable) (ticksSinPeligro 0)"
                + " (hayCebo false) (valorCebo 0) (poderRentable true) (pocasPildoras false)"
                + " (ticksEnCebo 0) (ticksSinValorCebo 0) (ceboEnfriamiento false) (aisladas 0) (pildorasRestantes 30))");

        }

        System.out.println(fallos == 0 ? "\nTodos los casos bien." : "\n" + fallos + " casos MAL.");
    }

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
        if (esperada.contains("/") && gana != null)             // acción con parámetro: id/modo
            id = id + "/" + gana.getSlotValue("modo").toString();
        boolean bien = id.equals(esperada);
        if (!bien) fallos++;
        System.out.printf("%s  gana %s, esperaba %s%n", bien ? "BIEN" : "MAL ", id, esperada);
    }
}
