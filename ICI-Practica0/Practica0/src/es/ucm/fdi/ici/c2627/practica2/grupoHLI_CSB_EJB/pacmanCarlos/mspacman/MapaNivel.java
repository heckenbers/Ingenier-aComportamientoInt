package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

import pacman.game.Game;

/**
 * Preproceso de nivel: la acción de entrada de la raíz de la HFSM.
 * Como cada laberinto es distinto, todo lo que depende de la topología se
 * calcula aquí una sola vez y se reutiliza mientras no cambie el laberinto.
 *
 * - colgante(n): el nodo está en una rama sin ciclo (callejón).
 * - tunel(n): el nodo es un extremo de un túnel lateral.
 * - escape(n): nodos alcanzables en RADIO_ESCAPE pasos (lo "abierto" que es).
 * - diámetro y radio del grafo, para expresar umbrales relativos.
 */
public final class MapaNivel {

    private static final int MUESTRA_CRUCES = 24;
    private static MapaNivel actual;

    /** Devuelve el preproceso del laberinto actual, recalculándolo si ha cambiado. */
    public static MapaNivel de(Game game) {
        MapaNivel m = actual;
        if (m == null || m.laberinto != game.getMazeIndex() || m.numNodos != game.getNumberOfNodes()) {
            m = new MapaNivel(game);
            actual = m;
        }
        return m;
    }

    private final int laberinto;
    private final int numNodos;
    private final boolean[] colgante;
    private final boolean[] tunel;
    private final int[] escape;
    private final int diametro;
    private final int radio;

    private MapaNivel(Game game) {
        laberinto = game.getMazeIndex();
        numNodos = game.getNumberOfNodes();

        int[][] vecinos = new int[numNodos][];
        for (int i = 0; i < numNodos; i++) {
            int[] v = game.getNeighbouringNodes(i);
            vecinos[i] = (v == null) ? new int[0] : v;
        }

        colgante = calcularColgantes(vecinos);
        tunel = calcularTuneles(game, vecinos);
        escape = calcularEscape(vecinos);

        // diámetro y radio aproximados con las excentricidades de una muestra
        // de cruces (como mucho MUESTRA_CRUCES, para no pasarse del tiempo del tick)
        int[] cruces = game.getJunctionIndices();
        if (cruces == null || cruces.length == 0)
            cruces = new int[] { 0 };
        int paso = Math.max(1, cruces.length / MUESTRA_CRUCES);
        int dMax = 0, rMin = Integer.MAX_VALUE;
        for (int ic = 0; ic < cruces.length; ic += paso) {
            int c = cruces[ic];
            int ecc = 0;
            for (int i = 0; i < numNodos; i++)
                if (vecinos[i].length > 0)
                    ecc = Math.max(ecc, game.getShortestPathDistance(c, i));
            dMax = Math.max(dMax, ecc);
            rMin = Math.min(rMin, ecc);
        }
        diametro = Math.max(1, dMax);
        radio = Math.max(1, rMin == Integer.MAX_VALUE ? dMax : rMin);
    }

    /** Poda iterativa de hojas: lo que se poda está en ramas colgantes. */
    private boolean[] calcularColgantes(int[][] vecinos) {
        boolean[] res = new boolean[numNodos];
        int[] grado = new int[numNodos];
        int[] cola = new int[numNodos];
        int ini = 0, fin = 0;
        for (int i = 0; i < numNodos; i++) {
            grado[i] = vecinos[i].length;
            if (grado[i] == 1)
                cola[fin++] = i;
        }
        while (ini < fin) {
            int n = cola[ini++];
            if (res[n])
                continue;
            res[n] = true;
            for (int v : vecinos[n])
                if (!res[v] && --grado[v] == 1)
                    cola[fin++] = v;
        }
        return res;
    }

    /** Un túnel une nodos vecinos que en pantalla están muy separados. */
    private boolean[] calcularTuneles(Game game, int[][] vecinos) {
        boolean[] res = new boolean[numNodos];
        for (int i = 0; i < numNodos; i++)
            for (int v : vecinos[i]) {
                int dx = Math.abs(game.getNodeXCood(i) - game.getNodeXCood(v));
                int dy = Math.abs(game.getNodeYCood(i) - game.getNodeYCood(v));
                if (dx + dy > 4) {
                    res[i] = true;
                    res[v] = true;
                }
            }
        return res;
    }

    /** BFS limitado a RADIO_ESCAPE pasos desde cada nodo. */
    private int[] calcularEscape(int[][] vecinos) {
        int[] res = new int[numNodos];
        int[] marca = new int[numNodos];
        int[] prof = new int[numNodos];
        int[] cola = new int[numNodos];
        for (int s = 0; s < numNodos; s++) {
            if (vecinos[s].length == 0)
                continue;
            int sello = s + 1;
            int ini = 0, fin = 0, cuenta = 0;
            cola[fin++] = s;
            marca[s] = sello;
            prof[s] = 0;
            while (ini < fin) {
                int n = cola[ini++];
                cuenta++;
                if (prof[n] == Parametros.RADIO_ESCAPE)
                    continue;
                for (int v : vecinos[n])
                    if (marca[v] != sello) {
                        marca[v] = sello;
                        prof[v] = prof[n] + 1;
                        cola[fin++] = v;
                    }
            }
            res[s] = cuenta;
        }
        return res;
    }

    public boolean esColgante(int n) { return n >= 0 && n < numNodos && colgante[n]; }

    public boolean esTunel(int n) { return n >= 0 && n < numNodos && tunel[n]; }

    public int getEscape(int n) { return (n >= 0 && n < numNodos) ? escape[n] : 0; }

    /** escape(n) normalizado: ~1 en un pasillo recto, mayor cuanto más abierta es la zona. */
    public double getApertura(int n) {
        return getEscape(n) / (2.0 * Parametros.RADIO_ESCAPE + 1);
    }

    public int getDiametro() { return diametro; }

    public int getRadio() { return radio; }

    public int getNumNodos() { return numNodos; }
}
