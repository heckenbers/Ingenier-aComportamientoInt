package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

import static pacman.game.Constants.EAT_DISTANCE;
import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.GHOST_EAT_SCORE;
import static pacman.game.Constants.GHOST_SPEED_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.ucm.fdi.ici.Input;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Percepciones de Pac-Man. Se crea una vez por tick y todas las transiciones
 * y acciones leen de aquí.
 *
 * Modelo de amenaza: tFantasma(g, n) son los ticks que tarda el fantasma g en
 * ser peligroso en el nodo n, usando las distancias precalculadas con sentido
 * (los fantasmas no pueden invertir):
 *  - fantasma normal:     d
 *  - fantasma comestible: e + max(0, d - e*(R-1)/R)  (va a velocidad (R-1)/R hasta que se le acaba el susto)
 *  - fantasma en la casa: tCasa + D(salida, n)
 *
 * IMPORTANTE: Input llama a parseInput() desde su constructor, antes de que
 * se ejecuten los inicializadores de los atributos de esta subclase. Por eso
 * ningún atributo tiene inicializador: todo se asigna en parseInput().
 */
public class MsPacManInput extends Input {

    private static final int SIN_CALCULAR = Integer.MIN_VALUE;
    private static final int LEJOS = Integer.MAX_VALUE / 4;
    private static final GHOST[] FANTASMAS = GHOST.values();

    private MapaNivel mapa;
    private int numNodos;
    private int pacman;
    private MOVE ultimoMov;

    private int[] nodoF;
    private MOVE[] movF;
    private int[] comestibleF;
    private int[] carcelF;

    private int[][] tFantasma;      // caches perezosas, por tick
    private int[] amenazaTotal;
    private int[] amenazaActiva;
    private int[] distPacman;

    private int tSusto;
    private boolean poderRentable;

    private int[] pildoras;
    private int[] pildorasPoder;
    private int[] tamClusters;
    private int[] pildorasAisladas;
    private boolean pocasPildoras;

    private List<Ruta> rutas;
    private int mejorMargen;
    private int amenazaEnPacman;
    private int revertirEnPacman;

    private boolean salidaSegura;
    private boolean peligro;
    private boolean critico;
    private boolean porRevertir;
    private boolean hayComestibles;

    private GHOST presa;
    private int valorPlanCaza;

    private int pildoraCebo;
    private int valorCebo;
    private boolean ceboDisponible;

    public MsPacManInput(Game game) {
        super(game);
    }

    @Override
    public void parseInput() {
        mapa = MapaNivel.de(game);
        numNodos = game.getNumberOfNodes();

        pacman = game.getPacmanCurrentNodeIndex();
        MOVE m = game.getPacmanLastMoveMade();
        ultimoMov = (m == null) ? MOVE.NEUTRAL : m;

        int nf = FANTASMAS.length;
        nodoF = new int[nf];
        movF = new MOVE[nf];
        comestibleF = new int[nf];
        carcelF = new int[nf];
        hayComestibles = false;
        for (int i = 0; i < nf; i++) {
            GHOST f = FANTASMAS[i];
            nodoF[i] = game.getGhostCurrentNodeIndex(f);
            MOVE mf = game.getGhostLastMoveMade(f);
            movF[i] = (mf == null) ? MOVE.NEUTRAL : mf;
            comestibleF[i] = Math.max(0, game.getGhostEdibleTime(f));
            carcelF[i] = Math.max(0, game.getGhostLairTime(f));
            if (comestibleF[i] > 0 && carcelF[i] == 0)
                hayComestibles = true;
        }

        tFantasma = new int[nf][];
        amenazaTotal = vacio(numNodos);
        amenazaActiva = vacio(numNodos);
        distPacman = vacio(numNodos);

        // tiempo de susto de este nivel, el mismo cálculo que hace Game al comer la píldora
        tSusto = (int) (EDIBLE_TIME * Math.pow(EDIBLE_TIME_REDUCTION, game.getCurrentLevel() % LEVEL_RESET_REDUCTION));
        poderRentable = tSusto >= GHOST_SPEED_REDUCTION * Parametros.FACTOR_TAU_MIN * mapa.getRadio();

        pildoras = game.getActivePillsIndices();
        pildorasPoder = game.getActivePowerPillsIndices();
        int limitePocas = Math.max(Parametros.MIN_POCAS, (int) (Parametros.FRACCION_POCAS * game.getNumberOfPills()));
        pocasPildoras = pildoras.length <= limitePocas;

        if (pacman < 0) {                       // no debería pasar con información completa
            tamClusters = new int[pildoras.length];
            pildorasAisladas = new int[0];
            rutas = Collections.emptyList();
            mejorMargen = LEJOS;
            amenazaEnPacman = revertirEnPacman = LEJOS;
            salidaSegura = true;
            peligro = critico = porRevertir = false;
            presa = null;
            pildoraCebo = -1;
            return;
        }

        calcularClusters();

        rutas = Navegacion.explorarDesde(this, pacman, ultimoMov);
        mejorMargen = Integer.MIN_VALUE;
        for (Ruta r : rutas)
            mejorMargen = Math.max(mejorMargen, r.getMargen());
        if (rutas.isEmpty())
            mejorMargen = amenaza(pacman);

        amenazaEnPacman = amenazaActiva(pacman);
        revertirEnPacman = LEJOS;
        for (int i = 0; i < nf; i++)
            if (comestibleF[i] > 0 && carcelF[i] == 0)
                revertirEnPacman = Math.min(revertirEnPacman, tFantasma(i, pacman));

        salidaSegura = mejorMargen > Parametros.MARGEN_SEGURO;
        peligro = amenazaEnPacman <= Parametros.TAU_ALERTA || !salidaSegura;
        critico = amenazaEnPacman <= Parametros.TAU_CRIT || !salidaSegura;
        porRevertir = revertirEnPacman <= Parametros.TAU_ALERTA;

        calcularPlanCaza();
        calcularCebo();
    }

    private static int[] vacio(int n) {
        int[] a = new int[n];
        Arrays.fill(a, SIN_CALCULAR);
        return a;
    }

    // =====================================================================
    // Distancias y amenaza (O(1) gracias a las distancias precalculadas)
    // =====================================================================

    /** Ticks hasta que el fantasma i es peligroso en el nodo n. */
    public int tFantasma(int i, int n) {
        int[] fila = tFantasma[i];
        if (fila == null) {
            fila = vacio(numNodos);
            tFantasma[i] = fila;
        }
        if (fila[n] != SIN_CALCULAR)
            return fila[n];

        int t;
        if (carcelF[i] > 0 || nodoF[i] < 0) {
            t = carcelF[i] + game.getShortestPathDistance(game.getGhostInitialNodeIndex(), n);
        } else {
            int d;
            if (nodoF[i] == n)
                d = 0;
            else if (movF[i] == MOVE.NEUTRAL)
                d = game.getShortestPathDistance(nodoF[i], n);
            else
                d = game.getShortestPathDistance(nodoF[i], n, movF[i]);
            int e = comestibleF[i];
            t = (e > 0) ? e + Math.max(0, d - e * (GHOST_SPEED_REDUCTION - 1) / GHOST_SPEED_REDUCTION) : d;
        }
        fila[n] = t;
        return t;
    }

    /** Ticks hasta que algún fantasma (incluidos los comestibles cuando se les pase el susto) es peligroso en n. */
    public int amenaza(int n) {
        if (n < 0)
            return LEJOS;
        if (amenazaTotal[n] == SIN_CALCULAR) {
            int min = LEJOS;
            for (int i = 0; i < FANTASMAS.length; i++)
                min = Math.min(min, tFantasma(i, n));
            amenazaTotal[n] = min;
        }
        return amenazaTotal[n];
    }

    /** Igual que amenaza(n) pero solo con fantasmas no comestibles (incluidos los de la casa). */
    public int amenazaActiva(int n) {
        if (n < 0)
            return LEJOS;
        if (amenazaActiva[n] == SIN_CALCULAR) {
            int min = LEJOS;
            for (int i = 0; i < FANTASMAS.length; i++)
                if (comestibleF[i] == 0)
                    min = Math.min(min, tFantasma(i, n));
            amenazaActiva[n] = min;
        }
        return amenazaActiva[n];
    }

    /** Distancia de Pac-Man a n respetando que no puede invertir el sentido. */
    public int distanciaPacman(int n) {
        if (n < 0 || pacman < 0)
            return LEJOS;
        if (distPacman[n] == SIN_CALCULAR)
            distPacman[n] = (n == pacman) ? 0
                    : (ultimoMov == MOVE.NEUTRAL) ? game.getShortestPathDistance(pacman, n)
                    : game.getShortestPathDistance(pacman, n, ultimoMov);
        return distPacman[n];
    }

    // =====================================================================
    // Clústeres de píldoras
    // =====================================================================

    private void calcularClusters() {
        int p = pildoras.length;
        int[] padre = new int[p];
        for (int i = 0; i < p; i++)
            padre[i] = i;
        for (int i = 0; i < p; i++)
            for (int j = i + 1; j < p; j++)
                if (game.getShortestPathDistance(pildoras[i], pildoras[j]) <= Parametros.ENLACE_CLUSTER)
                    unir(padre, i, j);

        int[] raiz = new int[p];
        int[] tam = new int[p];
        for (int i = 0; i < p; i++) {
            raiz[i] = buscar(padre, i);
            tam[raiz[i]]++;
        }
        tamClusters = new int[p];
        for (int i = 0; i < p; i++)
            tamClusters[i] = tam[raiz[i]];

        // distancia de cada clúster al resto
        int[] distOtros = new int[p];
        Arrays.fill(distOtros, LEJOS);
        for (int i = 0; i < p; i++)
            for (int j = i + 1; j < p; j++)
                if (raiz[i] != raiz[j]) {
                    int d = game.getShortestPathDistance(pildoras[i], pildoras[j]);
                    distOtros[raiz[i]] = Math.min(distOtros[raiz[i]], d);
                    distOtros[raiz[j]] = Math.min(distOtros[raiz[j]], d);
                }

        // clúster aislado más cercano a Pac-Man
        double umbral = Parametros.FRACCION_AISLADO * mapa.getDiametro();
        int mejorRaiz = -1, mejorDist = LEJOS;
        for (int i = 0; i < p; i++) {
            int r = raiz[i];
            if (tam[r] <= Parametros.MAX_TAM_AISLADO && distOtros[r] < LEJOS && distOtros[r] >= umbral) {
                int d = game.getShortestPathDistance(pacman, pildoras[i]);
                if (d < mejorDist) {
                    mejorDist = d;
                    mejorRaiz = r;
                }
            }
        }
        if (mejorRaiz < 0) {
            pildorasAisladas = new int[0];
        } else {
            pildorasAisladas = new int[tam[mejorRaiz]];
            int k = 0;
            for (int i = 0; i < p; i++)
                if (raiz[i] == mejorRaiz)
                    pildorasAisladas[k++] = pildoras[i];
        }
    }

    private static int buscar(int[] padre, int i) {
        while (padre[i] != i) {
            padre[i] = padre[padre[i]];
            i = padre[i];
        }
        return i;
    }

    private static void unir(int[] padre, int a, int b) {
        int ra = buscar(padre, a), rb = buscar(padre, b);
        if (ra != rb)
            padre[ra] = rb;
    }

    // =====================================================================
    // valor_cadena y plan_caza
    // =====================================================================

    /**
     * Mejor puntuación encadenando fantasmas desde pos, con t ticks ya gastados.
     * limite[i] = ticks de susto que le quedan al fantasma i (0 si no se puede comer).
     * Peor caso: el fantasma huye, así que Pac-Man se le acerca a velocidad 1/R
     * y tarda R * (d - EAT_DISTANCE). Con 4 fantasmas son como mucho 24 órdenes.
     */
    private int cadena(int pos, int t, int base, int usados, int[] limite) {
        int mejor = 0;
        for (int i = 0; i < FANTASMAS.length; i++) {
            if ((usados & (1 << i)) != 0 || limite[i] <= 0 || nodoF[i] < 0)
                continue;
            int d = game.getShortestPathDistance(pos, nodoF[i]);
            int tc = t + GHOST_SPEED_REDUCTION * Math.max(0, d - EAT_DISTANCE);
            if (tc + Parametros.HOLGURA_CAZA <= limite[i])
                mejor = Math.max(mejor, base + cadena(nodoF[i], tc, base * 2, usados | (1 << i), limite));
        }
        return mejor;
    }

    private void calcularPlanCaza() {
        presa = null;
        valorPlanCaza = 0;
        int[] limite = new int[FANTASMAS.length];
        for (int i = 0; i < FANTASMAS.length; i++)
            limite[i] = (carcelF[i] == 0) ? comestibleF[i] : 0;

        int base = game.getGhostCurrentEdibleScore();
        int mejorDist = LEJOS;
        for (int i = 0; i < FANTASMAS.length; i++) {
            if (limite[i] <= 0 || nodoF[i] < 0)
                continue;
            int d = distanciaPacman(nodoF[i]);
            int tc = GHOST_SPEED_REDUCTION * Math.max(0, d - EAT_DISTANCE);
            if (tc + Parametros.HOLGURA_CAZA > limite[i])
                continue;
            int v = base + cadena(nodoF[i], tc, base * 2, 1 << i, limite);
            if (v > valorPlanCaza || (v == valorPlanCaza && d < mejorDist)) {
                valorPlanCaza = v;
                mejorDist = d;
                presa = FANTASMAS[i];
            }
        }
    }

    /** valor_cadena(p): puntos esperados si Pac-Man come ahora la píldora de poder p. */
    public int valorCadena(int p) {
        int[] limite = new int[FANTASMAS.length];
        for (int i = 0; i < FANTASMAS.length; i++)
            limite[i] = (carcelF[i] == 0 && nodoF[i] >= 0) ? tSusto : 0;
        return cadena(p, 0, GHOST_EAT_SCORE, 0, limite);
    }

    private void calcularCebo() {
        pildoraCebo = -1;
        valorCebo = 0;
        int mejorDist = LEJOS;
        double distMax = Parametros.DIST_MAX_CEBO * mapa.getDiametro();
        for (int p : pildorasPoder) {
            if (mapa.esColgante(p))
                continue;                       // nunca cebar en un callejón
            int tp = distanciaPacman(p);
            if (amenaza(p) - tp <= Parametros.MARGEN_SEGURO)
                continue;
            if (tp > distMax && !pocasPildoras)
                continue;
            int v = valorCadena(p);
            if (v > valorCebo || (v == valorCebo && tp < mejorDist)) {
                valorCebo = v;
                mejorDist = tp;
                pildoraCebo = p;
            }
        }
        // si quedan pocas píldoras hay que comerse las de poder igualmente para acabar el nivel
        if (pildoraCebo < 0 && pocasPildoras)
            for (int p : pildorasPoder) {
                int tp = distanciaPacman(p);
                if (tp < mejorDist) {
                    mejorDist = tp;
                    pildoraCebo = p;
                    valorCebo = valorCadena(p);
                }
            }
        ceboDisponible = poderRentable && pildoraCebo >= 0 && valorCebo >= Parametros.VALOR_MIN_CEBO;
    }

    // =====================================================================
    // Getters
    // =====================================================================

    public MapaNivel getMapa() { return mapa; }

    public int getPacman() { return pacman; }

    public MOVE getUltimoMov() { return ultimoMov; }

    public int getTSusto() { return tSusto; }

    public boolean isPoderRentable() { return poderRentable; }

    public int[] getPildoras() { return pildoras; }

    public int[] getPildorasPoder() { return pildorasPoder; }

    /** Tamaño del clúster de cada píldora (mismo orden que getPildoras()). */
    public int[] getTamClusters() { return tamClusters; }

    /** Píldoras del clúster aislado más cercano (vacío si no hay). */
    public int[] getPildorasAisladas() { return pildorasAisladas; }

    public boolean isPocasPildoras() { return pocasPildoras; }

    public List<Ruta> getRutas() { return rutas; }

    public int getMejorMargen() { return mejorMargen; }

    /** Ticks que tarda el fantasma no comestible más cercano en llegar a Pac-Man. */
    public int getAmenazaEnPacman() { return amenazaEnPacman; }

    public boolean isSalidaSegura() { return salidaSegura; }

    public boolean isPeligro() { return peligro; }

    public boolean isCritico() { return critico; }

    public boolean isPorRevertir() { return porRevertir; }

    public boolean isHayComestibles() { return hayComestibles; }

    /** Primer fantasma del plan de caza, o null si no hay ninguno alcanzable. */
    public GHOST getPresa() { return presa; }

    public int getValorPlanCaza() { return valorPlanCaza; }

    public int getPildoraCebo() { return pildoraCebo; }

    public int getValorCebo() { return valorCebo; }

    public boolean isCeboDisponible() { return ceboDisponible; }
}
