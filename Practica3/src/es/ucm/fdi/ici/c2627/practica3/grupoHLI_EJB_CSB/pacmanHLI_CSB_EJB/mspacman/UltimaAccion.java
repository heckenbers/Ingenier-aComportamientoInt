package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman;

import java.util.Collection;

import es.ucm.fdi.ici.rules.RuleEngineObserver;

/**
 * Memoria entre turnos. El RuleEngine hace reset en cada turno, así que lo
 * que tenga que sobrevivir vive aquí, en Java, y vuelve a Jess como slots
 * del hecho MSPACMAN:
 *  - ultima: id del ACTION que ganó el turno anterior;
 *  - ticksSinPeligro: turnos seguidos huyendo en los que las reglas NO
 *    concluyeron PELIGRO. Sustituye al contador de SinPeligroTransition.
 *
 * Se entera de ambas cosas como observador del RuleEngine: actionFired recibe
 * el ACTION ganador y todos los hechos que quedaron en memoria tras run().
 * Así el umbral de peligro solo está en el .clp, no repetido en Java.
 *
 * Misma idea que UltimaAccion de la plantilla, más el contador.
 */
public class UltimaAccion implements RuleEngineObserver {

    private static final String NADA = "nada";
    private static final String HECHO_PELIGRO = "MAIN::PELIGRO";

    private String ultima;
    private int ticksSinPeligro;

    public UltimaAccion() {
        olvidar();
    }

    /** Al empezar cada partida, cada nivel y tras cada muerte. */
    public void olvidar() {
        ultima = NADA;
        ticksSinPeligro = 0;
    }

    public String get() {
        return ultima;
    }

    public int getTicksSinPeligro() {
        return ticksSinPeligro;
    }

    @Override
    public void ruleEngineReset() {
        // nada: el reset de Jess ocurre en cada turno
    }

    /**
     * Igual que SinPeligroTransition de la Práctica 2: el contador vale 0 al
     * entrar en supervivencia y cuenta los turnos SEGUIDOS huyendo (Evadir o
     * EscapeDeEmergencia) en los que las reglas no concluyeron PELIGRO.
     */
    @Override
    public void actionFired(String action, Collection<String> allFacts) {
        ultima = slot(action, "id", NADA);
        boolean huyendo = ultima.equals("Evadir") || ultima.equals("EscapeDeEmergencia");
        boolean peligro = false;
        for (String f : allFacts)
            if (f.equals("(" + HECHO_PELIGRO + ")")) {
                peligro = true;
                break;
            }
        ticksSinPeligro = (!huyendo || peligro) ? 0 : ticksSinPeligro + 1;
    }

    /** Valor de un slot en un hecho escrito como texto: "(... (id Evadir) ...)" -> Evadir. */
    private static String slot(String hecho, String nombre, String porDefecto) {
        String clave = "(" + nombre + " ";
        int i = hecho.indexOf(clave);
        if (i < 0)
            return porDefecto;
        int ini = i + clave.length();
        int fin = hecho.indexOf(')', ini);
        return (fin < 0) ? porDefecto : hecho.substring(ini, fin).trim();
    }
}
