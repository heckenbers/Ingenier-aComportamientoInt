package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.Action;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Base de las acciones de Ms. Pac-Man: prepara el MsPacManInput del turno, devuelve el movimiento forzado si
 * Ms. Pac-Man está en un pasillo (el motor haría lo mismo: seguir recto) y, si hay decisión que tomar, delega
 * en decide(). Ninguna acción decide cambiar a otra: eso es cosa de las transiciones de la máquina.
 */
public abstract class MsPacManAction implements Action {

    private final String id;
    private boolean seguridadEnPasillo;        // V1: el movimiento seguro mide el cruce al que lleva el pasillo, no el nodo de al lado

    protected MsPacManAction(String id) {
        this.id = id;
    }

    /** V0 no lo llama (falso). V1 lo pone a true en todas sus acciones: es su único cambio. */
    public final void setSeguridadEnPasillo(boolean valor) {
        this.seguridadEnPasillo = valor;
    }

    @Override
    public final String getActionId() {
        return id;
    }

    @Override
    public final MOVE execute(Game game) {
        MsPacManInput m = new MsPacManInput(game);
        MOVE unico = m.getForcedMove();
        if (unico != null) {
            return unico;
        }
        MOVE mov = decide(m);
        return mov == null ? MOVE.NEUTRAL : mov;
    }

    @Override
    public String toString() {
        return id;
    }

    /** Elige el movimiento en un cruce. Nunca devuelve null. */
    protected abstract MOVE decide(MsPacManInput m);

    /** @return 'mov' si es uno de los movimientos legales de este turno; si no, null */
    protected static MOVE legal(MsPacManInput m, MOVE mov) {
        if (mov == null) {
            return null;
        }
        for (MOVE posible : m.getPossibleMoves()) {
            if (posible == mov) {
                return mov;
            }
        }
        return null;
    }

    /**
     * De los movimientos legales, el que deja a Ms. Pac-Man más lejos del fantasma peligroso que antes llegaría
     * a su nuevo nodo (cada fantasma con su último movimiento: no puede darse la vuelta, así que la parte de
     * atrás de un fantasma es la parte libre). Con la misma seguridad gana el que más acerca a la píldora
     * común más cercana. Si 'evitarPoder', los movimientos que pisan una píldora de poder solo se eligen si no
     * hay otro igual de seguro.
     * <p>
     * V0 mide la seguridad en el nodo vecino. V1 (seguridadEnPasillo) la mide en el primer cruce al que lleva ese
     * movimiento: Ms. Pac-Man no puede darse la vuelta en un pasillo, así que entrar en él la compromete a llegar
     * a ese cruce, y lo que importa es cuánta ventaja le saca a los fantasmas allí (distancia del fantasma al cruce
     * menos pasos de Ms. Pac-Man hasta él; negativa si un fantasma llega antes).
     */
    protected MOVE movimientoSeguro(MsPacManInput m, boolean evitarPoder) {
        MOVE[] posibles = m.getPossibleMoves();
        if (posibles.length == 0) {
            return MOVE.NEUTRAL;
        }
        int pildora = m.getNearestPill();
        Game game = m.getGame();
        MOVE mejor = posibles[0];
        long mejorPuntos = Long.MIN_VALUE;
        for (MOVE mov : posibles) {
            int siguiente = game.getNeighbour(m.getPacman(), mov);
            if (siguiente < 0) {
                continue;
            }
            int distFantasma;
            if (seguridadEnPasillo) {
                int[] fin = m.finDePasillo(siguiente, mov);        // {cruce, pasos hasta él}
                long ventaja = (long) m.distanciaMinimaFantasma(fin[0]) - fin[1];
                distFantasma = (int) Math.min(ventaja, Constantes.DISTANCIA_SEGURA);
            } else {
                distFantasma = Math.min(m.distanciaMinimaFantasma(siguiente), Constantes.DISTANCIA_SEGURA);
            }
            int distPildora = pildora >= 0 ? m.distanciaEntre(siguiente, pildora) : 0;
            if (distPildora < 0) {
                distPildora = Constantes.DISTANCIA_PILDORA_INALCANZABLE;
            }
            long puntos = distFantasma * Constantes.PESO_SEGURIDAD - distPildora;
            if (evitarPoder && m.esPildoraPoder(siguiente)) {
                puntos -= Constantes.PESO_SEGURIDAD * Constantes.DISTANCIA_SEGURA;
            }
            if (puntos > mejorPuntos) {
                mejorPuntos = puntos;
                mejor = mov;
            }
        }
        return mejor;
    }
}