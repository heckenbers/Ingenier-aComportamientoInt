package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Espera junto a la píldora de poder sin pisarla, hasta que se acerquen suficientes fantasmas para que valga
 * la pena comérsela. Ms. Pac-Man no puede darse la vuelta, así que "esperar" es moverse sin alejarse más de
 * ESPERA_RADIO nodos de la píldora y sin acercarse a los fantasmas.
 */
public class EsperarJuntoPildoraPoderAction extends MsPacManAction {

    public EsperarJuntoPildoraPoderAction() {
        super("EsperarJuntoPildoraPoder");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        int pildora = m.getNearestPowerPill();
        if (pildora < 0) {
            return movimientoSeguro(m, false);
        }
        MOVE[] posibles = m.getPossibleMoves();
        Game game = m.getGame();
        MOVE mejor = null;
        long mejorPuntos = Long.MIN_VALUE;
        for (MOVE mov : posibles) {
            int siguiente = game.getNeighbour(m.getPacman(), mov);
            if (siguiente < 0 || siguiente == pildora) {
                continue;                                  // no se pisa la píldora
            }
            long puntos = (long) Math.min(m.distanciaMinimaFantasma(siguiente), Constantes.DISTANCIA_SEGURA)
                    * Constantes.PESO_SEGURIDAD;
            // Distancia de vuelta a la píldora tras este movimiento: sin poder darse la vuelta, seguir recto aleja
            int alPoder = m.distanciaConSentido(siguiente, pildora, mov);
            if (alPoder < 0 || alPoder > Constantes.ESPERA_RADIO) {
                puntos -= Constantes.PENALIZACION_FUERA_DE_RADIO * Constantes.PESO_SEGURIDAD;
            } else {
                puntos -= alPoder;                         // a igualdad, quedarse lo más cerca posible
            }
            if (puntos > mejorPuntos) {
                mejorPuntos = puntos;
                mejor = mov;
            }
        }
        return mejor != null ? mejor : movimientoSeguro(m, true);
    }
}