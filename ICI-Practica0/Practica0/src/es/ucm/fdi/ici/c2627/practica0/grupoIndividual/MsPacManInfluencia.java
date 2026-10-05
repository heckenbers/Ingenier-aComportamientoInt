package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

import java.awt.Color;
import java.util.Arrays;

import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 * Ms. Pac-Man que decide con un mapa de influencia. Cuatro capas, un número por nodo:
 * comida (atrae), amenaza (repele), presa (atrae) y poder (atrae o repele según la
 * situación). Cada capa se pinta con una anchura que va perdiendo fuerza con la distancia
 * por el pasillo, se normaliza a [0, 1] y se mezcla en
 *
 *   U(n) = wComida·comida − wAmenaza·amenaza + wPresa·presa ± wPoder·poder
 *
 * La decisión sólo ocurre en los cruces: para cada salida se recorre el pasillo hasta el
 * siguiente cruce y se queda con el peor U del tramo; gana la salida cuyo peor nodo es el
 * menos malo. El conocimiento del juego no está en un if: está pintado en el mapa.
 */
public class MsPacManInfluencia extends PacmanController {

    private double wComida, wAmenaza, wPresa;
    private double wPoder;                 // signo y peso de la capa de poder en este turno

    // ---- Las capas: un número por nodo -------------------------------------
    private double[] comida, amenaza, presa, poder;

    // ---- Lo que se reserva una vez por laberinto ---------------------------
    private int laberinto = -1;            // para saber cuándo cambia
    private int[][] vecinos;               // vecinos[n]: los nodos a un paso de n
    private int claveComida = -1;          // la capa de comida sólo cambia si cambia esto

    // ---- La cola de la anchura, reutilizada --------------------------------
    private int[] cola;
    private double[] valor;                // valor con el que entra cada nodo en la cola
    private boolean[] visto;
    private int ini, fin;

    // ---- Estado dentro de la partida (recordar, no aprender) ---------------
    private int amenazasCerca;             // fantasmas peligrosos a menos de CERCA
    private int pildorasPrevias = -1;
    private int ultimoComer = 0;
    private boolean estancado = false;

    public MsPacManInfluencia() {
        this(Constantes.PESO_COMIDA, Constantes.PESO_AMENAZA, Constantes.PESO_PRESA);
    }

    /** Constructor con pesos, para probar varios juegos de pesos sin recompilar. */
    public MsPacManInfluencia(double wComida, double wAmenaza, double wPresa) {
        this.wComida = wComida;
        this.wAmenaza = wAmenaza;
        this.wPresa = wPresa;
    }

    // ------------------------------------------------------------------ Decisión

    @Override
    public MOVE getMove(Game game, long timeDue) {
        if (game.getMazeIndex() != laberinto) {
            reservar(game);
        }
        actualizarProgreso(game);

        int yo = game.getPacmanCurrentNodeIndex();
        MOVE ult = game.getPacmanLastMoveMade();
        MOVE[] ops = game.getPossibleMoves(yo, ult);
        boolean decide = ops.length > 1;   // la pregunta buena: cuántas opciones legales me quedan
        MOVE porDefecto = ops.length == 1 ? ops[0] : MOVE.NEUTRAL;

        if (!decide && !Constantes.DEBUG_PACMAN) {
            return porDefecto;             // en un pasillo sólo se puede seguir: no se pinta nada
        }

        pintarCapas(game, yo, ult);
        elegirSignoDelPoder();
        if (Constantes.DEBUG_PACMAN) {
            depurar(game);
        }
        if (!decide) {
            return porDefecto;
        }

        // Una salida por cada movimiento legal; gana la que tenga mejor tramo
        MOVE mejor = ops[0];
        double mejorValor = Double.NEGATIVE_INFINITY;
        for (MOVE m : ops) {
            double v = valorarTramo(game, yo, m);
            if (v > mejorValor) {
                mejorValor = v;
                mejor = m;
            }
        }
        return mejor;
    }

    /** La función objetivo, nodo a nodo. */
    private double utilidad(int n) {
        return wComida * comida[n] - wAmenaza * amenaza[n] + wPresa * presa[n] + wPoder * poder[n];
    }

    /**
     * Recorre el pasillo que sale de 'desde' por 'm' hasta el siguiente cruce, incluido,
     * y devuelve el peor U que encuentra: meterse en un pasillo es un compromiso.
     */
    private double valorarTramo(Game game, int desde, MOVE m) {
        int anterior = desde;
        int n = game.getNeighbour(desde, m);
        double peor = Double.POSITIVE_INFINITY;
        for (int i = 0; i < Constantes.MAX_TRAMO && n >= 0; i++) {
            peor = Math.min(peor, utilidad(n));
            if (game.isJunction(n) || vecinos[n].length < 2) {
                return peor;
            }
            // un nodo de pasillo tiene dos vecinos: el siguiente es el que no es el anterior
            int siguiente = (vecinos[n][0] == anterior) ? vecinos[n][1] : vecinos[n][0];
            anterior = n;
            n = siguiente;
        }
        return peor == Double.POSITIVE_INFINITY ? Double.NEGATIVE_INFINITY : peor;
    }

    /**
     * La píldora de poder es un recurso: con fantasmas encima y cerca atrae; sin presión
     * repele (no se gasta de paso); y si la partida se estanca, se deja de evitar.
     */
    private void elegirSignoDelPoder() {
        if (amenazasCerca >= Constantes.FANTASMAS_PARA_PILDORA) {
            wPoder = Constantes.PESO_PODER_ATRAE;
        } else if (estancado) {
            wPoder = 0;
        } else {
            wPoder = -Constantes.PESO_PODER_EVITA;
        }
    }

    /** Final de nivel: si hace mucho que no se come, se deja de evitar la píldora de poder. */
    private void actualizarProgreso(Game game) {
        int tiempo = game.getCurrentLevelTime();
        int quedan = game.getNumberOfActivePills() + game.getNumberOfActivePowerPills();
        if (quedan != pildorasPrevias || tiempo < ultimoComer) {
            pildorasPrevias = quedan;
            ultimoComer = tiempo;
        }
        estancado = tiempo - ultimoComer > Constantes.TURNOS_ESTANCADO;
    }

    // ------------------------------------------------------------ Pintar capas

    private void pintarCapas(Game game, int yo, MOVE ult) {
        int[] pills = game.getActivePillsIndices();
        int[] powers = game.getActivePowerPillsIndices();

        // Comida: una sola anchura con todas las píldoras en la cola a la vez. Las de poder
        // sólo entran aquí cuando no queda otra cosa que comer (si no, el nivel no acabaría).
        int clave = game.getCurrentLevel() * 10000 + pills.length + powers.length;
        if (clave != claveComida) {
            claveComida = clave;
            Arrays.fill(comida, 0);
            empezar();
            int[] fuentes = pills.length > 0 ? pills : powers;
            for (int p : fuentes) sembrar(p, 1.0);
            propagar(Constantes.CAIDA_COMIDA, comida);
            normalizar(comida);
        }

        // Poder: capa propia, su signo se decide cada turno según la presión
        Arrays.fill(poder, 0);
        if (pills.length > 0 && powers.length > 0) {
            empezar();
            for (int p : powers) sembrar(p, 1.0);
            propagar(Constantes.CAIDA_PODER, poder);
            normalizar(poder);
        }

        // Amenaza y presa: una anchura por fantasma, sumadas
        Arrays.fill(amenaza, 0);
        Arrays.fill(presa, 0);
        amenazasCerca = 0;
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) {
                continue;                          // en la cárcel: no está en el laberinto
            }
            int nodo = game.getGhostCurrentNodeIndex(g);
            int d = game.getShortestPathDistance(yo, nodo);
            if (game.isGhostEdible(g)) {
                // sólo es presa si da tiempo a alcanzarla: cota 2d (va a media velocidad)
                if (2 * d + Constantes.MARGEN_CAZA >= game.getGhostEdibleTime(g)) {
                    continue;
                }
                empezar();
                sembrar(nodo, 1.0);
                propagar(Constantes.CAIDA_PRESA, presa);
            } else {
                if (d <= Constantes.CERCA) {
                    amenazasCerca++;
                }
                // No puede darse la vuelta: la primera ola sólo sale hacia delante
                empezar();
                visto[nodo] = true;
                amenaza[nodo] += 1.0;
                int[] delante = game.getNeighbouringNodes(nodo, game.getGhostLastMoveMade(g));
                if (delante == null) {
                    delante = vecinos[nodo];       // sin sentido conocido: hacia todos lados
                }
                for (int v : delante) {
                    sembrar(v, Constantes.CAIDA_AMENAZA);
                }
                propagar(Constantes.CAIDA_AMENAZA, amenaza);
            }
        }
        normalizar(amenaza);
        normalizar(presa);
    }

    // ---- La anchura con caída ----------------------------------------------

    private void empezar() {
        Arrays.fill(visto, false);
        ini = 0;
        fin = 0;
    }

    private void sembrar(int nodo, double v) {
        if (!visto[nodo]) {
            visto[nodo] = true;
            valor[nodo] = v;
            cola[fin++] = nodo;
        }
    }

    /** Cada nodo suma su valor a la capa y pasa valor × caída a sus vecinos. */
    private void propagar(double caida, double[] capa) {
        while (ini < fin) {
            int u = cola[ini++];
            capa[u] += valor[u];
            for (int v : vecinos[u]) {
                sembrar(v, valor[u] * caida);
            }
        }
    }

    /** Divide por el máximo, para que los pesos se puedan comparar entre capas. */
    private static void normalizar(double[] capa) {
        double max = 0;
        for (double x : capa) max = Math.max(max, x);
        if (max > 0) {
            for (int i = 0; i < capa.length; i++) capa[i] /= max;
        }
    }

    /** Todo lo que depende del laberinto se reserva aquí, y sólo aquí. */
    private void reservar(Game game) {
        laberinto = game.getMazeIndex();
        int n = game.getNumberOfNodes();
        comida = new double[n];
        amenaza = new double[n];
        presa = new double[n];
        poder = new double[n];
        valor = new double[n];
        visto = new boolean[n];
        cola = new int[n];
        vecinos = new int[n][];
        for (int i = 0; i < n; i++) {
            int[] v = game.getNeighbouringNodes(i);
            vecinos[i] = v == null ? new int[0] : v;
        }
        claveComida = -1;                  // hay que repintar la comida
    }

    // ---------------------------------------------------------------- Depurar

    /** U(n) en cinco tramos: amarillo lo que suma, rojo lo que resta, más opaco cuanto más lejos del cero. */
    private void depurar(Game game) {
        double escala = Math.max(wComida + wPresa + Constantes.PESO_PODER_ATRAE, wAmenaza);
        for (int n = 0; n < comida.length; n++) {
            double u = utilidad(n) / escala;
            if (Math.abs(u) < 0.02) continue;
            int tramo = (int) Math.min(4, Math.abs(u) * 5);
            int alfa = 40 + 45 * tramo;
            Color c = (u > 0) ? new Color(245, 180, 0, alfa) : new Color(220, 40, 30, alfa);
            GameView.addPoints(game, c, n);
        }
    }

    @Override
    public String getName() {
        return "MsPacManInfluencia";
    }
}