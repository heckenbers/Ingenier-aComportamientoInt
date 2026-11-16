package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Prepara en cada turno los datos que necesitan las transiciones y las acciones.
 * Las distancias son siempre por los pasillos (DM.PATH) y respetan que nadie puede darse la vuelta:
 * la de Ms. Pac-Man usa su último movimiento y la de un fantasma usa el suyo. Un valor -1 significa
 * que no hay camino (por ejemplo, hacia la cárcel).
 * <p>
 * Ojo: el constructor de Input llama a parseInput() antes de que se inicialicen los atributos de esta
 * clase, así que ningún atributo tiene valor inicial en su declaración (se perdería): todo se inicia
 * dentro de parseInput().
 */
public class MsPacManInput extends Input {

    private int pacman;
    private MOVE lastMove;
    private MOVE[] possibleMoves;

    // Fantasmas que están fuera de la cárcel, separados por tipo
    private List<GHOST> dangerous;
    private GHOST nearestDangerous;
    private int distNearestDangerous;
    private GHOST nearestEdible;
    private int distNearestEdible;

    // Píldoras que siguen sin comer
    private int[] pills;
    private int[] powerPills;
    private int nearestPowerPill;
    private int distNearestPowerPill;

    // La píldora común más cercana se calcula solo si alguien la pide
    private boolean nearestPillReady;
    private int nearestPill;
    private int distNearestPill;

    public MsPacManInput(Game game) {
        super(game);
    }

    @Override
    public void parseInput() {
        dangerous = new ArrayList<>();
        nearestDangerous = null;
        distNearestDangerous = Constantes.SIN_DISTANCIA;
        nearestEdible = null;
        distNearestEdible = Constantes.SIN_DISTANCIA;
        nearestPowerPill = -1;
        distNearestPowerPill = Constantes.SIN_DISTANCIA;
        nearestPillReady = false;
        nearestPill = -1;
        distNearestPill = Constantes.SIN_DISTANCIA;

        pacman = game.getPacmanCurrentNodeIndex();
        MOVE ultimo = game.getPacmanLastMoveMade();
        lastMove = (ultimo == null) ? MOVE.NEUTRAL : ultimo;
        if (pacman < 0) {
            possibleMoves = new MOVE[0];
            pills = new int[0];
            powerPills = new int[0];
            return;
        }
        possibleMoves = game.getPossibleMoves(pacman, lastMove);

        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) {
                continue;                              // en la cárcel: no está en el laberinto
            }
            int nodo = game.getGhostCurrentNodeIndex(g);
            if (nodo < 0) {
                continue;
            }
            Boolean comestible = game.isGhostEdible(g);
            if (comestible != null && comestible) {
                int d = distanciaPacman(nodo);
                if (d >= 0 && d < distNearestEdible) {
                    distNearestEdible = d;
                    nearestEdible = g;
                }
            } else {
                int d = distanciaDeFantasma(g, pacman);
                if (d >= 0) {
                    dangerous.add(g);
                    if (d < distNearestDangerous) {
                        distNearestDangerous = d;
                        nearestDangerous = g;
                    }
                }
            }
        }

        pills = game.getActivePillsIndices();
        powerPills = game.getActivePowerPillsIndices();
        int[] cercana = masCercanas(powerPills, 1);
        if (cercana.length > 0) {
            nearestPowerPill = cercana[0];
            distNearestPowerPill = distanciaPacman(cercana[0]);
        }
    }

    // ------------------------------------------------------------------ Datos del turno

    public int getPacman() {
        return pacman;
    }

    public MOVE getLastMove() {
        return lastMove;
    }

    public MOVE[] getPossibleMoves() {
        return possibleMoves;
    }

    /** @return el único movimiento legal si Ms. Pac-Man está en un pasillo, o null si hay más de uno */
    public MOVE getForcedMove() {
        return possibleMoves.length == 1 ? possibleMoves[0] : null;
    }

    public List<GHOST> getDangerousGhosts() {
        return dangerous;
    }

    /** @return el fantasma no comestible más cercano, o null si no hay ninguno fuera de la cárcel */
    public GHOST getNearestDangerous() {
        return nearestDangerous;
    }

    public int getDistNearestDangerous() {
        return distNearestDangerous;
    }

    /** @return el fantasma comestible más cercano, o null si no hay ninguno fuera de la cárcel */
    public GHOST getNearestEdible() {
        return nearestEdible;
    }

    public int getDistNearestEdible() {
        return distNearestEdible;
    }

    /** @return el nodo de ese fantasma, o -1 si no se puede saber */
    public int getGhostNode(GHOST g) {
        return game.getGhostCurrentNodeIndex(g);
    }

    public int[] getActivePills() {
        return pills;
    }

    public int[] getActivePowerPills() {
        return powerPills;
    }

    /** @return el nodo de la píldora de poder más cercana, o -1 si no queda ninguna */
    public int getNearestPowerPill() {
        return nearestPowerPill;
    }

    public int getDistNearestPowerPill() {
        return distNearestPowerPill;
    }

    /** @return el nodo de la píldora común más cercana, o -1 si no queda ninguna */
    public int getNearestPill() {
        prepararPildoraCercana();
        return nearestPill;
    }

    public int getDistNearestPill() {
        prepararPildoraCercana();
        return distNearestPill;
    }

    private void prepararPildoraCercana() {
        if (nearestPillReady) {
            return;
        }
        nearestPillReady = true;
        int[] cercana = masCercanas(pills, 1);
        if (cercana.length > 0) {
            nearestPill = cercana[0];
            distNearestPill = distanciaPacman(cercana[0]);
        }
    }

    /** @return true si en ese nodo hay una píldora de poder sin comer */
    public boolean esPildoraPoder(int nodo) {
        for (int p : powerPills) {
            if (p == nodo) {
                return true;
            }
        }
        return false;
    }

    /** @return true si algún nodo del camino tiene una píldora de poder sin comer */
    public boolean contienePildoraPoder(int[] camino) {
        for (int nodo : camino) {
            if (esPildoraPoder(nodo)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ Distancias

    /** Distancia por pasillos de Ms. Pac-Man a 'destino' sin darse la vuelta; -1 si no hay camino. */
    public int distanciaPacman(int destino) {
        if (pacman < 0 || destino < 0) {
            return -1;
        }
        if (pacman == destino) {
            return 0;
        }
        try {
            return game.getShortestPathDistance(pacman, destino, lastMove);
        } catch (RuntimeException e) {
            return game.getShortestPathDistance(pacman, destino);
        }
    }

    /** Distancia por pasillos que recorrería el fantasma 'g' hasta 'destino' sin darse la vuelta; -1 si no hay camino. */
    public int distanciaDeFantasma(GHOST g, int destino) {
        int nodo = game.getGhostCurrentNodeIndex(g);
        if (nodo < 0 || destino < 0) {
            return -1;
        }
        if (nodo == destino) {
            return 0;
        }
        MOVE ultimo = game.getGhostLastMoveMade(g);
        if (ultimo == null) {
            ultimo = MOVE.NEUTRAL;
        }
        try {
            return game.getShortestPathDistance(nodo, destino, ultimo);
        } catch (RuntimeException e) {
            return game.getShortestPathDistance(nodo, destino);
        }
    }

    /** Distancia por pasillos entre dos nodos cualesquiera (sin tener en cuenta sentidos); -1 si no hay camino. */
    public int distanciaEntre(int a, int b) {
        if (a < 0 || b < 0) {
            return -1;
        }
        if (a == b) {
            return 0;
        }
        return game.getShortestPathDistance(a, b);
    }

    /**
     * Distancia por pasillos de 'desde' a 'hasta' para quien llega a 'desde' moviéndose en el sentido 'sentido':
     * no puede darse la vuelta, así que seguir de frente puede alejarle mucho más de lo que dice la distancia
     * simétrica. -1 si no hay camino.
     */
    public int distanciaConSentido(int desde, int hasta, MOVE sentido) {
        if (desde < 0 || hasta < 0) {
            return -1;
        }
        if (desde == hasta) {
            return 0;
        }
        try {
            return game.getShortestPathDistance(desde, hasta, sentido);
        } catch (RuntimeException e) {
            return game.getShortestPathDistance(desde, hasta);
        }
    }

    /**
     * Si Ms. Pac-Man entra en 'siguiente' (vecino suyo) con el movimiento 'mov' y sigue por el pasillo sin poder
     * darse la vuelta: {primer cruce al que llega, pasos que da hasta él contando el paso a 'siguiente'}.
     * Si 'siguiente' ya es un cruce, es él con 1 paso. Se corta a los PASILLO_MAX pasos o si el pasillo no sigue.
     */
    public int[] finDePasillo(int siguiente, MOVE mov) {
        int nodo = siguiente;
        MOVE sentido = mov;
        int pasos = 1;
        while (!game.isJunction(nodo) && pasos < Constantes.PASILLO_MAX) {
            MOVE[] salidas = game.getPossibleMoves(nodo, sentido);
            if (salidas.length == 0) {
                break;
            }
            int vecino = game.getNeighbour(nodo, salidas[0]);
            if (vecino < 0) {
                break;
            }
            nodo = vecino;
            sentido = salidas[0];
            pasos++;
        }
        return new int[] {nodo, pasos};
    }

    /** Menor distancia de un fantasma peligroso a 'nodo', o SIN_DISTANCIA si no hay ninguno. */
    public int distanciaMinimaFantasma(int nodo) {
        int minima = Constantes.SIN_DISTANCIA;
        for (GHOST g : dangerous) {
            int d = distanciaDeFantasma(g, nodo);
            if (d >= 0 && d < minima) {
                minima = d;
            }
        }
        return minima;
    }

    /** Cuántos fantasmas no comestibles están a 'distancia' nodos o menos de Ms. Pac-Man (cada uno con su último movimiento). */
    public int numFantasmasPeligrososA(int distancia) {
        int cuantos = 0;
        for (GHOST g : dangerous) {
            int d = distanciaDeFantasma(g, pacman);
            if (d >= 0 && d <= distancia) {
                cuantos++;
            }
        }
        return cuantos;
    }

    /**
     * Menor distancia de un fantasma peligroso a cualquier nodo de los primeros NODOS_REVISADOS_RUTA del camino,
     * o SIN_DISTANCIA si no hay fantasmas. Mide lo cerca que pasa la ruta de ellos.
     */
    public int distanciaMinimaFantasmaEnRuta(int[] camino) {
        int minima = Constantes.SIN_DISTANCIA;
        int limite = Math.min(camino.length, Constantes.NODOS_REVISADOS_RUTA);
        for (int i = 0; i < limite; i++) {
            if (camino[i] == pacman) {
                continue;
            }
            minima = Math.min(minima, distanciaMinimaFantasma(camino[i]));
        }
        return minima;
    }

    /** Los 'k' nodos de la lista más cercanos a Ms. Pac-Man, de menor a mayor distancia (los inalcanzables se ignoran). */
    public int[] masCercanas(int[] nodos, int k) {
        int[] mejores = new int[k];
        int[] distancias = new int[k];
        int n = 0;
        for (int nodo : nodos) {
            int d = distanciaPacman(nodo);
            if (d < 0) {
                continue;
            }
            if (n == k && d >= distancias[k - 1]) {
                continue;
            }
            int pos = (n < k) ? n : k - 1;
            while (pos > 0 && distancias[pos - 1] > d) {
                distancias[pos] = distancias[pos - 1];
                mejores[pos] = mejores[pos - 1];
                pos--;
            }
            distancias[pos] = d;
            mejores[pos] = nodo;
            if (n < k) {
                n++;
            }
        }
        return Arrays.copyOf(mejores, n);
    }

    // ------------------------------------------------------------------ Rutas

    /** Secuencia de nodos hasta 'destino' sin darse la vuelta; array vacío si no hay camino. */
    public int[] caminoHasta(int destino) {
        if (pacman < 0 || destino < 0 || destino == pacman) {
            return new int[0];
        }
        int[] camino;
        try {
            camino = game.getShortestPath(pacman, destino, lastMove);
        } catch (RuntimeException e) {
            camino = null;
        }
        return camino == null ? new int[0] : camino;
    }

    /** Primer nodo del camino distinto del que ocupa Ms. Pac-Man (el camino puede o no incluir el de partida); -1 si no hay. */
    public int siguienteNodo(int[] camino) {
        for (int n : camino) {
            if (n != pacman) {
                return n;
            }
        }
        return -1;
    }

    /** Movimiento que lleva al primer paso del camino, o null si no se puede calcular. */
    public MOVE movimientoPorCamino(int[] camino) {
        int siguiente = siguienteNodo(camino);
        if (siguiente < 0 || pacman < 0) {
            return null;
        }
        return game.getMoveToMakeToReachDirectNeighbour(pacman, siguiente);
    }

    /** Movimiento hacia 'destino': por el camino y, si falla, con getNextMoveTowardsTarget. Puede ser null. */
    public MOVE movimientoHacia(int destino) {
        MOVE m = movimientoPorCamino(caminoHasta(destino));
        if (m != null) {
            return m;
        }
        if (pacman < 0 || destino < 0 || destino == pacman) {
            return null;
        }
        try {
            return game.getNextMoveTowardsTarget(pacman, destino, lastMove, DM.PATH);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * true si el fantasma peligroso 'g' llega a algún nodo del camino antes, o con menos de
     * MARGEN_INTERCEPCION nodos de retraso, que Ms. Pac-Man.
     */
    public boolean fantasmaIntercepta(GHOST g, int[] camino) {
        int limite = Math.min(camino.length, Constantes.NODOS_REVISADOS_RUTA);
        int paso = 0;
        for (int i = 0; i < limite; i++) {
            int nodo = camino[i];
            if (nodo == pacman) {
                continue;
            }
            paso++;
            int d = distanciaDeFantasma(g, nodo);
            if (d >= 0 && d <= paso + Constantes.MARGEN_INTERCEPCION) {
                return true;
            }
        }
        return false;
    }

    /** true si algún fantasma peligroso corta el camino. */
    public boolean rutaInterceptada(int[] camino) {
        for (GHOST g : dangerous) {
            if (fantasmaIntercepta(g, camino)) {
                return true;
            }
        }
        return false;
    }
}