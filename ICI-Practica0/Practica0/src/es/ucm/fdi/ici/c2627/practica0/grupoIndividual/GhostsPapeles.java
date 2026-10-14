package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Fantasmas con papeles distintos: Blinky persigue, Pinky intercepta el próximo
 * cruce de Ms. Pac-Man, Inky corta el segundo cruce (o cubre la píldora de poder),
 * Sue persigue con un poco de ruido. Además: dispersión al empezar, anticipación de
 * la píldora de poder, huida con criterio y dejar de huir a tiempo.
 */
public class GhostsPapeles extends GhostController {

    private final Random rnd = new Random();

    // Predicción de los dos próximos cruces de Ms. Pac-Man (una vez por turno)
    private long claveTurno = -1;
    private final int[] cruces = new int[2];

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        for (GHOST f : GHOST.values()) {
            if (!game.doesGhostRequireAction(f)) continue;   // sin entrada: el motor sigue recto
            moves.put(f, decidir(game, f));
        }
        return moves;
    }

    // ------------------------------------------------------------- Decisión

    private MOVE decidir(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        MOVE ult = game.getGhostLastMoveMade(f);
        int pac = game.getPacmanCurrentNodeIndex();
        int dPac = distancia(game, nodo, pac, ult);

        // 1. Dispersión al empezar el nivel
        if (game.getCurrentLevelTime() < Constantes.TURNOS_DISPERSION
                && (dPac < 0 || dPac > Constantes.DIST_FIN_DISPERSION)) {
            return haciaNodo(game, nodo, ult, esquina(game, f), pac);
        }

        // 2. Comestible: huir, salvo que ya no me pueda comer antes de que acabe el tiempo
        int tiempo = game.getGhostEdibleTime(f);
        if (tiempo > 0) {
            boolean dejarDeHuir = dPac >= 0 && 3 * tiempo < 2 * dPac;   // me alcanza en 2d/3
            return dejarDeHuir ? segunSuPapel(game, f, nodo, ult, pac)
                               : huir(game, f, nodo, ult, pac);
        }

        // 3. Anticipar la píldora de poder: taponarla si llego antes, dispersarme si no
        MOVE ante = anteLaPildoraDePoder(game, nodo, ult, pac);
        if (ante != null) return ante;

        // 4. Perseguir según el papel
        return segunSuPapel(game, f, nodo, ult, pac);
    }

    private MOVE anteLaPildoraDePoder(Game game, int nodo, MOVE ult, int pac) {
        int[] powers = game.getActivePowerPillsIndices();
        if (powers.length == 0) return null;
        int pill = game.getClosestNodeIndexFromNodeIndex(pac, powers, DM.PATH);
        int dPacPill = game.getShortestPathDistance(pac, pill);
        if (dPacPill < 0 || dPacPill > Constantes.ALERTA_PODER) return null;
        int dMia = distancia(game, nodo, pill, ult);
        if (dMia >= 0 && dMia < dPacPill) {
            return haciaNodo(game, nodo, ult, pill, pac);        // taponar
        }
        return huir(game, null, nodo, ult, pac);                 // dispersarme antes de ser comestible
    }

    // --------------------------------------------------------------- Papeles

    private MOVE segunSuPapel(Game game, GHOST f, int nodo, MOVE ult, int pac) {
        switch (f) {
            case BLINKY:    // persigue por el camino más corto
                return haciaNodo(game, nodo, ult, pac, pac);

            case PINKY:     // intercepta: va al próximo cruce de Ms. Pac-Man
                return haciaNodo(game, nodo, ult, cruceDePacman(game, 0), pac);

            case INKY: {    // cubre la píldora de poder cercana a Ms. Pac-Man; si no, corta el 2º cruce
                int[] powers = game.getActivePowerPillsIndices();
                if (powers.length > 0) {
                    int guardar = game.getClosestNodeIndexFromNodeIndex(pac, powers, DM.PATH);
                    return haciaNodo(game, nodo, ult, guardar, pac);
                }
                return haciaNodo(game, nodo, ult, cruceDePacman(game, 1), pac);
            }

            default: {      // SUE: persigue, con un poco de ruido para romper bucles
                if (rnd.nextDouble() < Constantes.RUIDO) {
                    return movimientoAleatorioLegal(game, nodo, ult);
                }
                return haciaNodo(game, nodo, ult, pac, pac);
            }
        }
    }

    // ------------------------------------------------------------------ Huida

    /**
     * Huye sin ir en fila con los demás: descarta la salida por la que viene Ms. Pac-Man y,
     * entre las otras, elige la que más la aleja y más separa de otros comestibles.
     */
    private MOVE huir(Game game, GHOST yoMismo, int nodo, MOVE ult, int pac) {
        MOVE[] ops = game.getPossibleMoves(nodo, ult);
        if (ops.length == 0) return MOVE.NEUTRAL;
        MOVE porDondeViene = game.getNextMoveTowardsTarget(nodo, pac, ult, DM.PATH);

        MOVE mejor = ops[0];
        double mejorValor = Double.NEGATIVE_INFINITY;
        for (MOVE m : ops) {
            if (m == porDondeViene && ops.length > 1) continue;
            int vecino = game.getNeighbour(nodo, m);
            if (vecino < 0) continue;
            int d = game.getShortestPathDistance(vecino, pac);
            double valor = (d < 0 ? 0 : d) + Constantes.PESO_SEPARACION * distanciaAOtrosComestibles(game, yoMismo, vecino);
            if (valor > mejorValor) {
                mejorValor = valor;
                mejor = m;
            }
        }
        return mejor;
    }

    private int distanciaAOtrosComestibles(Game game, GHOST yoMismo, int desde) {
        int min = -1;
        for (GHOST g : GHOST.values()) {
            if (g == yoMismo || game.getGhostLairTime(g) > 0 || game.getGhostEdibleTime(g) <= 0) continue;
            int d = game.getShortestPathDistance(desde, game.getGhostCurrentNodeIndex(g));
            if (d >= 0 && (min < 0 || d < min)) min = d;
        }
        return min < 0 ? 0 : min;
    }

    // ------------------------------------------------------------- Predicción

    /** Próximo cruce (nivel 0) o segundo cruce (nivel 1) de Ms. Pac-Man. */
    private int cruceDePacman(Game game, int nivel) {
        long clave = game.getCurrentLevelTime() * 100000L + game.getPacmanCurrentNodeIndex();
        if (clave != claveTurno) {
            claveTurno = clave;
            predecir(game);
        }
        return cruces[nivel];
    }

    /** Sigue el pasillo de Ms. Pac-Man hasta el cruce; en el cruce supone que va hacia la comida. */
    private void predecir(Game game) {
        int n = game.getPacmanCurrentNodeIndex();
        MOVE m = game.getPacmanLastMoveMade();
        cruces[0] = n;
        cruces[1] = n;
        for (int nivel = 0; nivel < 2; nivel++) {
            MOVE[] ops = game.getPossibleMoves(n, m);
            if (ops.length == 0) break;
            MOVE salida = ops.length == 1 ? ops[0] : salidaHaciaComida(game, n, m, ops);
            int c = n;
            MOVE cm = salida;
            for (int i = 0; i < Constantes.MAX_TRAMO; i++) {
                int sig = game.getNeighbour(c, cm);
                if (sig < 0) break;
                c = sig;
                if (game.isJunction(c)) break;
                MOVE[] o = game.getPossibleMoves(c, cm);
                if (o.length == 0) break;
                cm = o[0];
            }
            n = c;
            m = cm;
            cruces[nivel] = n;
            if (nivel == 0) cruces[1] = n;
        }
    }

    private MOVE salidaHaciaComida(Game game, int n, MOVE m, MOVE[] ops) {
        int[] pills = game.getActivePillsIndices();
        if (pills.length == 0) return ops[0];
        int objetivo = game.getClosestNodeIndexFromNodeIndex(n, pills, DM.PATH);
        MOVE hacia = game.getNextMoveTowardsTarget(n, objetivo, m, DM.PATH);
        for (MOVE o : ops) {
            if (o == hacia) return hacia;
        }
        return ops[0];
    }

    // ---------------------------------------------------------------- Auxiliares

    /** Una esquina por fantasma: las píldoras de poder están en las cuatro esquinas. */
    private int esquina(Game game, GHOST f) {
        int[] powers = game.getPowerPillIndices();
        return powers[f.ordinal() % powers.length];
    }

    /** Distancia respetando que el fantasma no puede darse la vuelta. -1 si no llega. */
    private int distancia(Game game, int a, int b, MOVE ult) {
        if (b < 0) return -1;
        if (ult == null || ult == MOVE.NEUTRAL) return game.getShortestPathDistance(a, b);
        return game.getShortestPathDistance(a, b, ult);
    }

    /** Primer paso del camino hacia 'destino'. Si no sirve, se persigue a Ms. Pac-Man. */
    private MOVE haciaNodo(Game game, int nodo, MOVE ult, int destino, int pac) {
        if (destino < 0 || destino == nodo) destino = pac;
        int[] camino = (ult == null || ult == MOVE.NEUTRAL)
                ? game.getShortestPath(nodo, destino)
                : game.getShortestPath(nodo, destino, ult);
        return primerMovimientoDe(game, camino);
    }

    private MOVE primerMovimientoDe(Game game, int[] camino) {
        if (camino == null || camino.length < 2) return MOVE.NEUTRAL;
        MOVE m = game.getMoveToMakeToReachDirectNeighbour(camino[0], camino[1]);
        return m == null ? MOVE.NEUTRAL : m;
    }

    private MOVE movimientoAleatorioLegal(Game game, int nodo, MOVE ult) {
        MOVE[] ops = game.getPossibleMoves(nodo, ult);
        return ops.length == 0 ? MOVE.NEUTRAL : ops[rnd.nextInt(ops.length)];
    }

    @Override
    public String getName() {
        return "GhostsPapeles";
    }
}