package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Catálogo de acciones de Ms. Pac-Man: cada una es un comportamiento puro que recibe el Game y
 * devuelve un movimiento legal. Ninguna decide cambiar a otra: eso es cosa de las transiciones de
 * la máquina. MsPacManAction es la base común; las demás son los diez estados simples.
 */
public final class Actions {

    private Actions() {
    }

    /**
     * Base de las acciones de Ms. Pac-Man: prepara el MsPacManInput del turno, devuelve el movimiento
     * forzado si Ms. Pac-Man está en un pasillo (el motor haría lo mismo: seguir recto) y, si hay
     * decisión que tomar, delega en decide(). Ninguna acción decide cambiar a otra: eso es cosa de
     * las transiciones de la máquina.
     */
    public abstract static class MsPacManAction implements Action {

        private final String id;

        protected MsPacManAction(String id) {
            this.id = id;
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
            return decide(m);
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
         * De los movimientos legales, el que deja a Ms. Pac-Man más lejos del fantasma peligroso que antes
         * llegaría a su nuevo nodo (cada fantasma con su último movimiento: no puede darse la vuelta, así
         * que la parte de atrás de un fantasma es la parte libre). Con la misma seguridad gana el que más
         * acerca a la píldora común más cercana. Si 'evitarPoder', los movimientos que pisan una píldora
         * de poder solo se eligen si no hay otro.
         */
        protected static MOVE movimientoSeguro(MsPacManInput m, boolean evitarPoder) {
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
                int distFantasma = Math.min(m.distanciaMinimaFantasma(siguiente), Constantes.DISTANCIA_SEGURA);
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

    /** Persigue al fantasma comestible más cercano por la ruta más corta. */
    public static class ComerFantasmaAction extends MsPacManAction {

        public ComerFantasmaAction() {
            super("ComerFantasma");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            GHOST g = m.getNearestEdible();
            if (g == null) {
                return movimientoSeguro(m, false);
            }
            MOVE mov = legal(m, m.movimientoHacia(m.getGhostNode(g)));
            return mov != null ? mov : movimientoSeguro(m, false);
        }
    }

    /**
     * Va hacia la píldora común más cercana por la ruta más corta, sin pasar por una píldora de poder
     * (para no gastarla) y sin que ningún fantasma corte la ruta; si la ruta está cortada, recalcula con
     * la siguiente píldora más cercana.
     */
    public static class ComerPildoraAction extends MsPacManAction {

        public ComerPildoraAction() {
            super("ComerPildora");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            // Si no quedan píldoras comunes, las de poder son lo único que falta para acabar el nivel
            boolean quedanComunes = m.getActivePills().length > 0;
            int[] objetivos = quedanComunes ? m.getActivePills() : m.getActivePowerPills();
            for (int objetivo : m.masCercanas(objetivos, Constantes.NUM_CANDIDATAS_PILDORA)) {
                int[] camino = m.caminoHasta(objetivo);
                if (camino.length == 0 || m.rutaInterceptada(camino)) {
                    continue;
                }
                if (quedanComunes && m.contienePildoraPoder(camino)) {
                    continue;
                }
                MOVE mov = legal(m, m.movimientoPorCamino(camino));
                if (mov != null) {
                    return mov;
                }
            }
            return movimientoSeguro(m, quedanComunes);
        }
    }

    /**
     * Espera junto a la píldora de poder sin pisarla, hasta que se acerquen suficientes fantasmas para que
     * valga la pena comérsela. Ms. Pac-Man no puede darse la vuelta, así que "esperar" es moverse sin
     * alejarse más de ESPERA_RADIO nodos de la píldora y sin acercarse a los fantasmas.
     */
    public static class EsperarJuntoPildoraPoderAction extends MsPacManAction {

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
                int alPoder = m.distanciaEntre(siguiente, pildora);
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

    /**
     * Hay una píldora de poder cerca pero ningún fantasma comestible que cazar: se elige un movimiento
     * que no la pise, para no perder el beneficio del poder. Entre los demás gana el más seguro.
     */
    public static class EsquivarPildoraPoderAction extends MsPacManAction {

        public EsquivarPildoraPoderAction() {
            super("EsquivarPildoraPoder");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            return movimientoSeguro(m, true);
        }
    }

    /**
     * Cambia de trayectoria hacia un cruce alternativo: de los cruces, los que menos rodeo suponen para
     * llegar a la píldora de poder (Ms. Pac-Man hasta el cruce más el cruce hasta la píldora), y se queda
     * con el primero cuya ruta no corte ningún fantasma.
     */
    public static class EvitarCaminoAction extends MsPacManAction {

        public EvitarCaminoAction() {
            super("EvitarCamino");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            int pildora = m.getNearestPowerPill();
            if (pildora < 0) {
                return movimientoSeguro(m, false);
            }
            int[] cruces = m.getGame().getJunctionIndices();
            int[] candidatos = new int[cruces.length];
            int[] rodeo = new int[cruces.length];
            int n = 0;
            for (int cruce : cruces) {
                if (cruce == m.getPacman()) {
                    continue;
                }
                int hastaCruce = m.distanciaPacman(cruce);
                int desdeCruce = m.distanciaEntre(cruce, pildora);
                if (hastaCruce < 0 || desdeCruce < 0) {
                    continue;
                }
                candidatos[n] = cruce;
                rodeo[n] = hastaCruce + desdeCruce;
                n++;
            }
            // Selección parcial: se sacan de uno en uno los de menor rodeo
            for (int r = 0; r < Constantes.NUM_CANDIDATOS_DESVIO && r < n; r++) {
                int mejor = r;
                for (int i = r + 1; i < n; i++) {
                    if (rodeo[i] < rodeo[mejor]) {
                        mejor = i;
                    }
                }
                int tmpNodo = candidatos[r];
                candidatos[r] = candidatos[mejor];
                candidatos[mejor] = tmpNodo;
                int tmpRodeo = rodeo[r];
                rodeo[r] = rodeo[mejor];
                rodeo[mejor] = tmpRodeo;

                int[] camino = m.caminoHasta(candidatos[r]);
                if (camino.length == 0 || m.rutaInterceptada(camino)) {
                    continue;
                }
                MOVE mov = legal(m, m.movimientoPorCamino(camino));
                if (mov != null) {
                    return mov;
                }
            }
            return movimientoSeguro(m, false);
        }
    }

    /**
     * Huye al cruce más lejano del fantasma peligroso más cercano (getFarthestNodeIndexFromNodeIndex con
     * DM.PATH sobre getJunctionIndices). Si el camino hasta él lo cortase algún fantasma, elige el
     * movimiento más seguro.
     */
    public static class HuirACruceAction extends MsPacManAction {

        public HuirACruceAction() {
            super("HuirACruce");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            GHOST g = m.getNearestDangerous();
            if (g == null) {
                return movimientoSeguro(m, true);
            }
            int nodoFantasma = m.getGhostNode(g);
            if (nodoFantasma >= 0) {
                Game game = m.getGame();
                int objetivo = game.getFarthestNodeIndexFromNodeIndex(nodoFantasma, game.getJunctionIndices(), DM.PATH);
                if (objetivo >= 0 && objetivo != m.getPacman()) {
                    int[] camino = m.caminoHasta(objetivo);
                    if (camino.length > 0 && !m.rutaInterceptada(camino)) {
                        MOVE mov = legal(m, m.movimientoPorCamino(camino));
                        if (mov != null) {
                            return mov;
                        }
                    }
                }
            }
            return movimientoSeguro(m, true);
        }
    }

    /**
     * Huye hacia la píldora de poder cuando Ms. Pac-Man llega antes que los fantasmas: si el camino no
     * pasa cerca de ninguno, va por él (la transición ya lo ha comprobado, aquí se vuelve a hacer por
     * seguridad); si no, el movimiento más seguro.
     */
    public static class HuirHaciaPildoraPoderAction extends MsPacManAction {

        public HuirHaciaPildoraPoderAction() {
            super("HuirHaciaPildoraPoder");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            int pildora = m.getNearestPowerPill();
            if (pildora >= 0) {
                int[] camino = m.caminoHasta(pildora);
                if (camino.length > 0 && !m.rutaInterceptada(camino)
                        && m.distanciaMinimaFantasmaEnRuta(camino) >= Constantes.HUIR_PODER_DISTANCIA_RUTA) {
                    MOVE mov = legal(m, m.movimientoPorCamino(camino));
                    if (mov != null) {
                        return mov;
                    }
                }
            }
            return movimientoSeguro(m, false);
        }
    }

    /** Va hacia la píldora de poder activa más cercana por la ruta más corta. */
    public static class IrPildoraPoderAction extends MsPacManAction {

        public IrPildoraPoderAction() {
            super("IrPildoraPoder");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            int pildora = m.getNearestPowerPill();
            if (pildora < 0) {
                return movimientoSeguro(m, false);
            }
            MOVE mov = legal(m, m.movimientoHacia(pildora));
            return mov != null ? mov : movimientoSeguro(m, false);
        }
    }

    /**
     * Quedan pocas píldoras: de las más cercanas elige la que, sumando lo que cuesta llegar y lo que
     * queda desde ella hasta la siguiente píldora, da el recorrido más corto, entre las que ningún
     * fantasma corta. Evita las píldoras de poder mientras queden comunes.
     */
    public static class LimpiarUltimasAction extends MsPacManAction {

        public LimpiarUltimasAction() {
            super("LimpiarUltimas");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            boolean quedanComunes = m.getActivePills().length > 0;
            int[] objetivos = quedanComunes ? m.getActivePills() : m.getActivePowerPills();
            int mejorObjetivo = -1;
            long mejorCoste = Long.MAX_VALUE;
            for (int candidato : m.masCercanas(objetivos, Constantes.NUM_CANDIDATAS_PILDORA)) {
                int[] camino = m.caminoHasta(candidato);
                if (camino.length == 0 || m.rutaInterceptada(camino)) {
                    continue;
                }
                if (quedanComunes && m.contienePildoraPoder(camino)) {
                    continue;
                }
                int hasta = m.distanciaPacman(candidato);
                if (hasta < 0) {
                    continue;
                }
                int siguiente = Constantes.SIN_DISTANCIA;
                for (int otro : objetivos) {
                    if (otro == candidato) {
                        continue;
                    }
                    int d = m.distanciaEntre(candidato, otro);
                    if (d >= 0 && d < siguiente) {
                        siguiente = d;
                    }
                }
                long coste = (long) hasta + (siguiente == Constantes.SIN_DISTANCIA ? 0 : siguiente);
                if (coste < mejorCoste) {
                    mejorCoste = coste;
                    mejorObjetivo = candidato;
                }
            }
            if (mejorObjetivo >= 0) {
                MOVE mov = legal(m, m.movimientoPorCamino(m.caminoHasta(mejorObjetivo)));
                if (mov != null) {
                    return mov;
                }
            }
            return movimientoSeguro(m, quedanComunes);
        }
    }

    /**
     * La píldora más cercana está vigilada por un fantasma: se patrulla la zona segura. Entre los cruces a
     * los que Ms. Pac-Man llega con ventaja sobre todos los fantasmas, va al que queda más lejos de ellos
     * (en caso de empate, al más cercano). Si no hay ninguno, el movimiento más seguro.
     */
    public static class PatrullarAction extends MsPacManAction {

        public PatrullarAction() {
            super("Patrullar");
        }

        @Override
        protected MOVE decide(MsPacManInput m) {
            int mejorCruce = -1;
            int mejorSeguridad = -1;
            int mejorDistancia = Constantes.SIN_DISTANCIA;
            for (int cruce : m.getGame().getJunctionIndices()) {
                if (cruce == m.getPacman()) {
                    continue;
                }
                int mia = m.distanciaPacman(cruce);
                if (mia <= 0) {
                    continue;
                }
                int fantasma = m.distanciaMinimaFantasma(cruce);
                if (fantasma != Constantes.SIN_DISTANCIA && fantasma < mia + Constantes.PATRULLA_VENTAJA) {
                    continue;                                  // un fantasma llega antes o casi a la vez
                }
                int seguridad = Math.min(fantasma, Constantes.DISTANCIA_SEGURA);
                if (seguridad > mejorSeguridad || (seguridad == mejorSeguridad && mia < mejorDistancia)) {
                    mejorSeguridad = seguridad;
                    mejorDistancia = mia;
                    mejorCruce = cruce;
                }
            }
            if (mejorCruce >= 0) {
                int[] camino = m.caminoHasta(mejorCruce);
                if (camino.length > 0 && !m.rutaInterceptada(camino)) {
                    MOVE mov = legal(m, m.movimientoPorCamino(camino));
                    if (mov != null) {
                        return mov;
                    }
                }
            }
            return movimientoSeguro(m, true);
        }
    }
}