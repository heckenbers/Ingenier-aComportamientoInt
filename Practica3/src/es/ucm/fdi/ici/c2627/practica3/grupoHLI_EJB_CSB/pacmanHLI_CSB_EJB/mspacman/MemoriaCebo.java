package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman;

import java.util.Collection;

import es.ucm.fdi.ici.rules.RuleEngineObserver;

/**
 * Memoria del cebo entre turnos (v1). Es la parte "cebo" de la MemoriaPacMan
 * de la Práctica 2, pero ahora se entera de todo como observador del
 * RuleEngine en vez de que la acción y la transición la modifiquen:
 *
 *  - si gana PrepararCebo (patrullando o comiéndose el cebo), cuenta turnos
 *    esperando y turnos seguidos con valorCebo < VALOR_MIN_CEBO;
 *  - si las reglas concluyen CEBO_AGOTADO, abandona el cebo y empieza el
 *    enfriamiento (en la P2 lo hacía CeboAgotadoTransition.evaluate()).
 *
 * Los turnos se cuentan con su propio contador: uno por decisión.
 */
public class MemoriaCebo implements RuleEngineObserver {

    private static final String ACCION_CEBO = "PrepararCebo";
    private static final String HECHO_AGOTADO = "(MAIN::CEBO_AGOTADO)";
    private static final String HECHO_MSPACMAN = "(MAIN::MSPACMAN ";

    private int turno;                  // número de la próxima decisión
    private int ultimoTurnoCebo;
    private int ticksEsperando;
    private int ticksSinValor;
    private int ultimoAbandono;

    public MemoriaCebo() {
        olvidar();
    }

    /** Al empezar cada partida, cada nivel y tras cada muerte (como MemoriaPacMan.reiniciar). */
    public void olvidar() {
        turno = 0;
        ultimoTurnoCebo = -10;
        ticksEsperando = 0;
        ticksSinValor = 0;
        ultimoAbandono = Integer.MIN_VALUE / 2;
    }

    /** Turnos seguidos en cebo hasta el turno anterior (0 si el anterior no fue cebo). */
    public int getTicksEsperando() {
        return (turno <= ultimoTurnoCebo + 1) ? ticksEsperando : 0;
    }

    /** Turnos seguidos en cebo con valorCebo por debajo de VALOR_MIN_CEBO. */
    public int getTicksSinValor() {
        return (turno <= ultimoTurnoCebo + 1) ? ticksSinValor : 0;
    }

    public boolean enEnfriamiento() {
        return turno - ultimoAbandono < Parametros.ENFRIAMIENTO_CEBO;
    }

    @Override
    public void ruleEngineReset() {
    }

    @Override
    public void actionFired(String action, Collection<String> facts) {
        boolean agotado = false;
        int valor = 0;
        for (String f : facts) {
            if (f.equals(HECHO_AGOTADO))
                agotado = true;
            else if (f.startsWith(HECHO_MSPACMAN))
                valor = entero(f, "valorCebo");
        }

        if (agotado) {                          // CeboAgotadoTransition: abandonar y enfriar
            ultimoAbandono = turno;
            ultimoTurnoCebo = -10;
        } else if (action.contains("(id " + ACCION_CEBO + ")")) {
            if (turno != ultimoTurnoCebo + 1) { // se acaba de entrar en el cebo
                ticksEsperando = 0;
                ticksSinValor = 0;
            }
            ticksEsperando++;
            ticksSinValor = (valor < Parametros.VALOR_MIN_CEBO) ? ticksSinValor + 1 : 0;
            ultimoTurnoCebo = turno;
        }
        turno++;
    }

    private static int entero(String hecho, String slot) {
        String clave = "(" + slot + " ";
        int i = hecho.indexOf(clave);
        if (i < 0)
            return 0;
        int ini = i + clave.length();
        int fin = hecho.indexOf(')', ini);
        try {
            return Integer.parseInt(hecho.substring(ini, fin).trim());
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
