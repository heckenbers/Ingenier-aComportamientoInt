package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * Condiciones que deciden cuándo la máquina cambia de estado. Cada una implementa la interfaz
 * Transition del motor y guarda en atributos lo que la distingue (umbral, modo). Las de salida con
 * histéresis se escriben con NotTransition sobre la de permanencia.
 */
public final class Transitions {

    private Transitions() {
    }

    /** Se cumple si se cumplen las dos transiciones. */
    public static class AndTransition implements Transition {

        private final Transition primera;
        private final Transition segunda;

        public AndTransition(Transition primera, Transition segunda) {
            this.primera = primera;
            this.segunda = segunda;
        }

        @Override
        public boolean evaluate(Input in) {
            return primera.evaluate(in) && segunda.evaluate(in);
        }

        @Override
        public String toString() {
            return "(" + primera + ") Y (" + segunda + ")";
        }
    }

    /** Hay un fantasma comestible a menos de 'umbral' nodos (o a 'umbral' o menos si es inclusivo). */
    public static class ComestibleCercaTransition implements Transition {

        private final int umbral;
        private final boolean inclusivo;

        public ComestibleCercaTransition(int umbral, boolean inclusivo) {
            this.umbral = umbral;
            this.inclusivo = inclusivo;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            if (m.getNearestEdible() == null) {
                return false;
            }
            int d = m.getDistNearestEdible();
            return inclusivo ? d <= umbral : d < umbral;
        }

        @Override
        public String toString() {
            return "fantasma comestible a " + (inclusivo ? "<= " : "< ") + umbral;
        }
    }

    /**
     * Se cumple cuando se lleva 'maxTurnos' turnos seguidos esperando. Solo la evalúa la máquina mientras
     * está en el estado de espera; si pasa más de un turno sin evaluarse (se salió y se volvió a entrar) o
     * empieza otra partida, la cuenta vuelve a cero. El estado es de la instancia, no estático.
     */
    public static class EsperaAgotadaTransition implements Transition {

        private final int maxTurnos;
        private int turnos;
        private int ultimoTurno;

        public EsperaAgotadaTransition(int maxTurnos) {
            this.maxTurnos = maxTurnos;
            this.turnos = 0;
            this.ultimoTurno = -1;
        }

        @Override
        public boolean evaluate(Input in) {
            int ahora = in.getGame().getTotalTime();
            if (ahora <= ultimoTurno || ahora - ultimoTurno > 1) {
                turnos = 0;
            }
            ultimoTurno = ahora;
            turnos++;
            if (turnos >= maxTurnos) {
                turnos = 0;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "espera de " + maxTurnos + " turnos agotada";
        }
    }

    /** Algún fantasma peligroso está a 'umbral' nodos o menos de la píldora de poder más cercana. */
    public static class FantasmaCercaDePildoraPoderTransition implements Transition {

        private final int umbral;

        public FantasmaCercaDePildoraPoderTransition(int umbral) {
            this.umbral = umbral;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            int pildora = m.getNearestPowerPill();
            return pildora >= 0 && m.distanciaMinimaFantasma(pildora) <= umbral;
        }

        @Override
        public String toString() {
            return "fantasma a <= " + umbral + " de la píldora de poder";
        }
    }

    /** Hay un fantasma no comestible a menos de 'umbral' nodos por camino. */
    public static class FantasmaPeligrosoCercaTransition implements Transition {

        private final int umbral;

        public FantasmaPeligrosoCercaTransition(int umbral) {
            this.umbral = umbral;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.getNearestDangerous() != null && m.getDistNearestDangerous() < umbral;
        }

        @Override
        public String toString() {
            return "fantasma no comestible a menos de " + umbral;
        }
    }

    /** Ms. Pac-Man está a 'radio' nodos o menos de la píldora de poder (y, si se exige, hay algún fantasma fuera). */
    public static class JuntoAPildoraPoderTransition implements Transition {

        private final int radio;
        private final boolean exigeFantasma;

        public JuntoAPildoraPoderTransition(int radio, boolean exigeFantasma) {
            this.radio = radio;
            this.exigeFantasma = exigeFantasma;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.getNearestPowerPill() >= 0 && m.getDistNearestPowerPill() <= radio
                    && (!exigeFantasma || m.hasActiveGhost());
        }

        @Override
        public String toString() {
            return "junto a la píldora de poder (<= " + radio + ")";
        }
    }

    /** Niega otra transición: sirve para las condiciones de salida con histéresis. */
    public static class NotTransition implements Transition {

        private final Transition transition;

        public NotTransition(Transition transition) {
            this.transition = transition;
        }

        @Override
        public boolean evaluate(Input in) {
            return !transition.evaluate(in);
        }

        @Override
        public String toString() {
            return "NO (" + transition + ")";
        }
    }

    /** Se cumple si se cumple al menos una de las dos transiciones. */
    public static class OrTransition implements Transition {

        private final Transition primera;
        private final Transition segunda;

        public OrTransition(Transition primera, Transition segunda) {
            this.primera = primera;
            this.segunda = segunda;
        }

        @Override
        public boolean evaluate(Input in) {
            return primera.evaluate(in) || segunda.evaluate(in);
        }

        @Override
        public String toString() {
            return "(" + primera + ") O (" + segunda + ")";
        }
    }

    /** La píldora de poder está a 'distancia' nodos o menos y no hay fantasma comestible que cazar. */
    public static class PildoraPoderAlLadoTransition implements Transition {

        private final int distancia;

        public PildoraPoderAlLadoTransition(int distancia) {
            this.distancia = distancia;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.getNearestPowerPill() >= 0 && m.getNearestEdible() == null
                    && m.getDistNearestPowerPill() <= distancia;
        }

        @Override
        public String toString() {
            return "píldora de poder a <= " + distancia + " sin fantasma comestible";
        }
    }

    /** La píldora de poder más cercana está a 'limite' nodos o menos (y, si se exige, hay algún fantasma fuera de la cárcel). */
    public static class PildoraPoderCercaTransition implements Transition {

        private final int limite;
        private final boolean exigeFantasma;

        public PildoraPoderCercaTransition(int limite, boolean exigeFantasma) {
            this.limite = limite;
            this.exigeFantasma = exigeFantasma;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            if (m.getNearestPowerPill() < 0 || (exigeFantasma && !m.hasActiveGhost())) {
                return false;
            }
            return m.getDistNearestPowerPill() <= limite;
        }

        @Override
        public String toString() {
            return "píldora de poder a <= " + limite + (exigeFantasma ? " con fantasmas fuera" : "");
        }
    }

    /** Algún fantasma peligroso está a menos de 'umbral' nodos de la píldora común más cercana. */
    public static class PildoraVigiladaTransition implements Transition {

        private final int umbral;

        public PildoraVigiladaTransition(int umbral) {
            this.umbral = umbral;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            int pildora = m.getNearestPill();
            return pildora >= 0 && m.distanciaMinimaFantasma(pildora) < umbral;
        }

        @Override
        public String toString() {
            return "fantasma a menos de " + umbral + " de la píldora común más cercana";
        }
    }

    /** Quedan menos de 'n' píldoras comunes (y al menos una). */
    public static class PocasPildorasTransition implements Transition {

        private final int n;

        public PocasPildorasTransition(int n) {
            this.n = n;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.getNumPills() > 0 && m.getNumPills() < n;
        }

        @Override
        public String toString() {
            return "quedan menos de " + n + " píldoras";
        }
    }

    /** Hay menos de 'cuantos' fantasmas peligrosos a menos de 'distancia' nodos de Ms. Pac-Man. */
    public static class PocosFantasmasCercaTransition implements Transition {

        private final int cuantos;
        private final int distancia;

        public PocosFantasmasCercaTransition(int cuantos, int distancia) {
            this.cuantos = cuantos;
            this.distancia = distancia;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.contarFantasmasCerca(distancia) < cuantos;
        }

        @Override
        public String toString() {
            return "menos de " + cuantos + " fantasmas a menos de " + distancia;
        }
    }

    /** Un fantasma no comestible corta la ruta hacia la píldora de poder y está a menos de 'umbral' nodos de ella. */
    public static class RutaPoderInterceptadaTransition implements Transition {

        private final int umbral;

        public RutaPoderInterceptadaTransition(int umbral) {
            this.umbral = umbral;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            int pildora = m.getNearestPowerPill();
            if (pildora < 0) {
                return false;
            }
            int[] camino = m.caminoHasta(pildora);
            for (pacman.game.Constants.GHOST g : m.getDangerousGhosts()) {
                int d = m.distanciaDeFantasma(g, pildora);
                if (d >= 0 && d < umbral && m.fantasmaIntercepta(g, camino)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "fantasma corta la ruta a la píldora de poder a < " + umbral + " de ella";
        }
    }

    /** Se cumple siempre: es la última flecha de cada estado y lleva al estado por defecto. */
    public static class SiempreTransition implements Transition {

        @Override
        public boolean evaluate(Input in) {
            return true;
        }

        @Override
        public String toString() {
            return "siempre";
        }
    }

    /** Ms. Pac-Man llega a la píldora de poder con al menos 'margen' nodos de ventaja sobre el fantasma más cercano a ella, por una ruta que no pasa cerca de ninguno. */
    public static class VentajaEnPildoraPoderTransition implements Transition {

        private final int margen;

        public VentajaEnPildoraPoderTransition(int margen) {
            this.margen = margen;
        }

        @Override
        public boolean evaluate(Input in) {
            MsPacManInput m = (MsPacManInput) in;
            return m.getVentajaPildoraPoder() >= margen;
        }

        @Override
        public String toString() {
            return "ventaja >= " + margen + " en la píldora de poder";
        }
    }
}