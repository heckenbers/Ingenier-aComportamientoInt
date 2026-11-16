package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.Arrays;

import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

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
public class MsPacManHLI_EJB_CSB extends PacmanController {

    private final double wComida, wAmenaza, wPresa;
    private final double wPoderAtrae, wPoderEvita;
    private final double caidaComida, caidaAmenaza, caidaPresa, caidaPoder;
    private double wPoder;                 // signo y peso de la capa de poder en este turno
    private double HOLGURA = 3;				// es el desempate para saber si llegamos o no antes que los fantasmas
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
    
    // ---- Cercania Fantasmas --------
    private double wAnticipacion; // Nuevo peso para la capa
    private double[] anticipacion;      // Nueva capa de influencia

    /** El que se entrega: todo sale de Constantes. */
    public MsPacManHLI_EJB_CSB() {
        this(Constantes.PESO_COMIDA, Constantes.PESO_AMENAZA, Constantes.PESO_PRESA,Constantes.PESO_PODER_ATRAE, Constantes.PESO_PODER_EVITA,
                Constantes.CAIDA_COMIDA, Constantes.CAIDA_AMENAZA, Constantes.CAIDA_PRESA, Constantes.CAIDA_PODER);
        this.wAnticipacion = Constantes.PESO_ANTICIPACION;
    }

    /** Sólo los tres pesos principales; el resto, de Constantes. */
    public MsPacManHLI_EJB_CSB(double wComida, double wAmenaza, double wPresa) {
        this(wComida, wAmenaza, wPresa, Constantes.PESO_PODER_ATRAE, Constantes.PESO_PODER_EVITA,
             Constantes.CAIDA_COMIDA, Constantes.CAIDA_AMENAZA, Constantes.CAIDA_PRESA, Constantes.CAIDA_PODER);
        this.wAnticipacion = Constantes.PESO_ANTICIPACION;
    }

    /** Todos los números afinables, para barrerlos desde Afinar sin recompilar. */
    public MsPacManHLI_EJB_CSB(double wComida, double wAmenaza, double wPresa, double wPoderAtrae, double wPoderEvita,
                    double caidaComida, double caidaAmenaza, double caidaPresa, double caidaPoder) {
        this.wComida = wComida;
        this.wAmenaza = wAmenaza;
        this.wPresa = wPresa;
        this.wPoderAtrae = wPoderAtrae;
        this.wPoderEvita = wPoderEvita;
        this.caidaComida = caidaComida;
        this.caidaAmenaza = caidaAmenaza;
        this.caidaPresa = caidaPresa;
        this.caidaPoder = caidaPoder;
		this.wAnticipacion = Constantes.PESO_ANTICIPACION;
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
        if (ops.length <= 1) {
            // en un pasillo sólo se puede seguir: no se pinta nada
            lastMove = ops.length == 1 ? ops[0] : MOVE.NEUTRAL;
            return lastMove;
        }

        // Patrón anytime: ya hay una respuesta decente si el reloj corta aquí
        lastMove = movimientoRapidoYSeguro(game, yo, ops);
        if (sinTiempo(timeDue)) {
            return lastMove;
        }

        pintarCapas(game, yo);
        elegirSignoDelPoder();

        // Una salida por cada movimiento legal; la mejor hasta ahora queda siempre apuntada
        MOVE mejor = null;
        double mejorValor = Double.NEGATIVE_INFINITY;
        for (MOVE m : ops) {
            if (mejor != null && sinTiempo(timeDue)) {
                break;                     // se acabó el tiempo: vale la mejor salida evaluada hasta ahora
            }
            double v = valorarTramo(game, yo, m);
            if (v > mejorValor) {
                mejorValor = v;
                mejor = m;
                lastMove = mejor;
            }
        }
        return lastMove;
    }

    private boolean sinTiempo(long timeDue) {
        return System.currentTimeMillis() >= timeDue - Constantes.MARGEN_TIEMPO_MS;
    }

    /**
     * Respuesta barata (sólo lecturas de tabla): la salida cuyo vecino queda más lejos del
     * fantasma peligroso más cercano. Sin peligro, la primera salida legal.
     */
    private MOVE movimientoRapidoYSeguro(Game game, int yo, MOVE[] ops) {
        MOVE mejor = ops[0];
        int mejorDistancia = -1;
        for (MOVE m : ops) {
            int vecino = game.getNeighbour(yo, m);
            if (vecino < 0) continue;
            int minima = Integer.MAX_VALUE;
            for (GHOST g : GHOST.values()) {
                if (game.getGhostLairTime(g) > 0 || game.getGhostEdibleTime(g) > 0) continue;
                int d = game.getShortestPathDistance(vecino, game.getGhostCurrentNodeIndex(g));
                if (d >= 0) minima = Math.min(minima, d);
            }
            if (minima > mejorDistancia) {
                mejorDistancia = minima;
                mejor = m;
            }
        }
        return mejor;
    }

    /** La función objetivo, nodo a nodo. */
    private double utilidad(int n) {
        return wComida * comida[n] 
             - wAmenaza * amenaza[n] 
             + wPresa * presa[n] 
             + wPoder * poder[n] 
             - wAnticipacion * anticipacion[n]; // <- Aquí se resta la anticipación
    }
    /**
     * Comprueba si Ms. Pac-Man llega al nodo del tramo antes que cualquier fantasma activo.
     */
    private boolean llegoAntes(Game game, int nodoDestino, int misPasos) {
        for (GHOST f : GHOST.values()) {
            if (game.isGhostEdible(f)) continue;          // Si es comestible, no es una amenaza letal
            if (game.getGhostLairTime(f) > 0) continue;   // Si está en la cárcel, no cuenta

            int nodoFantasma = game.getGhostCurrentNodeIndex(f);
            MOVE ultFantasma = game.getGhostLastMoveMade(f);
            int susPasos = game.getShortestPathDistance(nodoFantasma, nodoDestino, ultFantasma);

            // Si el fantasma llega antes o en el mismo tiempo (con holgura), no es seguro
            if (susPasos >= 0 && susPasos < misPasos + HOLGURA) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valora el tramo evaluando si Ms. Pac-Man llega antes a la intersección.
     * Si llega antes, respeta o premia el camino. Si el fantasma llega antes, penaliza suavemente.
     */
    private double valorarTramo(Game game, int desde, MOVE m) {
        int anterior = desde;
        int n = game.getNeighbour(desde, m);
        double peorUtilidadTramo = Double.POSITIVE_INFINITY;
        int pasosPacman = 0;
        
        boolean fantasmaGanaEnElTramo = false;

        for (int i = 0; i < Constantes.MAX_TRAMO && n >= 0; i++) {
            pasosPacman++;

            // Comprobamos si algún fantasma llega antes a este nodo del tramo
            for (GHOST f : GHOST.values()) {
                if (game.isGhostEdible(f) || game.getGhostLairTime(f) > 0) continue;

                int nodoFantasma = game.getGhostCurrentNodeIndex(f);
                MOVE ultFantasma = game.getGhostLastMoveMade(f);
                int susPasos = game.getShortestPathDistance(nodoFantasma, n, ultFantasma);

                // Si el fantasma llega antes o a la vez (con holgura de 1 paso)
                if (susPasos >= 0 && susPasos < pasosPacman + 1) {
                    fantasmaGanaEnElTramo = true;
                    break;
                }
            }

            double utilNodo = utilidad(n);
            
            // Si el fantasma nos gana en este punto, penalizamos la utilidad de este nodo específico
            if (fantasmaGanaEnElTramo) {
                utilNodo -= 2.5; // Penalización ajustable: suficiente para desincentivar, pero no un -INF total
            }

            peorUtilidadTramo = Math.min(peorUtilidadTramo, utilNodo);

            if (game.isJunction(n) || vecinos[n].length < 2) {
                break;
            }

            int siguiente = (vecinos[n][0] == anterior) ? vecinos[n][1] : vecinos[n][0];
            anterior = n;
            n = siguiente;
        }

        return peorUtilidadTramo == Double.POSITIVE_INFINITY ? Double.NEGATIVE_INFINITY : peorUtilidadTramo;
    }
    /**
     * Recorre el pasillo que sale de 'desde' por 'm' hasta el siguiente cruce, incluido,
     * y devuelve el peor U que encuentra: meterse en un pasillo es un compromiso.
     */
    /*private double valorarTramo(Game game, int desde, MOVE m) {
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
    }*/
    
    //Hace lo mismo que el anterior pero además mira si es que llego yo antes que el objetivo
    /*private double valorarTramo(Game game, int desde, MOVE m) {
        int anterior = desde;
        int n = game.getNeighbour(desde, m);
        double peor = Double.POSITIVE_INFINITY;
        int pasosPacman = 0;

        for (int i = 0; i < Constantes.MAX_TRAMO && n >= 0; i++) {
            pasosPacman++;

            // Validar anticipación: si el fantasma llega antes a este nodo, el tramo es mortal
            if (!llegoAntes(game, n, pasosPacman)) {
                return Double.NEGATIVE_INFINITY;
            }

            peor = Math.min(peor, utilidad(n));
            if (game.isJunction(n) || vecinos[n].length < 2) {
                return peor;
            }
            
            int siguiente = (vecinos[n][0] == anterior) ? vecinos[n][1] : vecinos[n][0];
            anterior = n;
            n = siguiente;
        }
        return peor == Double.POSITIVE_INFINITY ? Double.NEGATIVE_INFINITY : peor;
    }*/
    
    /**
     * Valora un tramo a dos niveles de profundidad:
     * 1. Recorre el primer tramo hasta el primer cruce (validando que Ms. Pac-Man llegue antes).
     * 2. Desde ese primer cruce, evalúa las salidas del segundo tramo.
     * Si el primer cruce no tiene ninguna salida segura posterior, el tramo entero se descarta.
     */
    /*private double valorarTramo(Game game, int desde, MOVE m) {
        // --- NIVEL 1: Primer tramo hasta el primer cruce ---
        int anterior = desde;
        int n = game.getNeighbour(desde, m);
        double peorNivel1 = Double.POSITIVE_INFINITY;
        int pasosPacman = 0;
        int primerCruce = -1;
        MOVE ultimaMovCruce1 = m;

        for (int i = 0; i < Constantes.MAX_TRAMO && n >= 0; i++) {
            pasosPacman++;

            // Validación de llegada antes en el primer tramo
            if (!llegoAntes(game, n, pasosPacman)) {
                return Double.NEGATIVE_INFINITY;
            }

            peorNivel1 = Math.min(peorNivel1, utilidad(n));

            // Si encontramos un cruce o un final de camino
            if (game.isJunction(n) || vecinos[n].length < 2) {
                primerCruce = n;
                break;
            }

            int siguiente = (vecinos[n][0] == anterior) ? vecinos[n][1] : vecinos[n][0];
            anterior = n;
            n = siguiente;
        }

        // Si no se encontró ningún cruce válido, devolvemos lo peor del primer tramo
        if (primerCruce == -1 || vecinos[primerCruce].length <= 1) {
            return peorNivel1 == Double.POSITIVE_INFINITY ? Double.NEGATIVE_INFINITY : peorNivel1;
        }

        // --- NIVEL 2: Evaluar las salidas desde el primer cruce ---
        MOVE[] opsNivel2 = game.getPossibleMoves(primerCruce, ultimaMovCruce1);
        if (opsNivel2.length == 0) {
            return Double.NEGATIVE_INFINITY; // Callejón sin salida real
        }

        double mejorPeorNivel2 = Double.NEGATIVE_INFINITY;
        boolean alMenosUnaSalidaSegura = false;

        for (MOVE m2 : opsNivel2) {
            int ant2 = primerCruce;
            int n2 = game.getNeighbour(primerCruce, m2);
            double peorEsteSegundoTramo = Double.POSITIVE_INFINITY;
            int pasosNivel2 = pasosPacman;
            boolean tramo2Valido = true;

            for (int j = 0; j < Constantes.MAX_TRAMO && n2 >= 0; j++) {
                pasosNivel2++;

                // Validar si los fantasmas interceptan en este segundo nivel
                if (!llegoAntes(game, n2, pasosNivel2)) {
                    tramo2Valido = false;
                    break;
                }

                peorEsteSegundoTramo = Math.min(peorEsteSegundoTramo, utilidad(n2));

                if (game.isJunction(n2) || vecinos[n2].length < 2) {
                    break;
                }

                int sig2 = (vecinos[n2][0] == ant2) ? vecinos[n2][1] : vecinos[n2][0];
                ant2 = n2;
                n2 = sig2;
            }

            if (tramo2Valido) {
                alMenosUnaSalidaSegura = true;
                mejorPeorNivel2 = Math.max(mejorPeorNivel2, peorEsteSegundoTramo);
            }
        }

        // Si desde el primer cruce TODAS las salidas llevan a ser atrapados, el tramo es una trampa
        if (!alMenosUnaSalidaSegura) {
            return Double.NEGATIVE_INFINITY;
        }

        // Combinamos de forma conservadora el peor caso del nivel 1 y el mejor de los peores del nivel 2
        return Math.min(peorNivel1, mejorPeorNivel2);
    }*/

    
    /**
     * Valora un tramo aplicando una penalización suave basada en la anticipación del fantasma,
     * en lugar de descartar el tramo por completo con Double.NEGATIVE_INFINITY.
     */
   /* private double valorarTramo(Game game, int desde, MOVE m) {
        int anterior = desde;
        int n = game.getNeighbour(desde, m);
        double peorUtilidadTramo = Double.POSITIVE_INFINITY;
        int pasosPacman = 0;
        double penalizacionAcumulada = 0.0;

        for (int i = 0; i < Constantes.MAX_TRAMO && n >= 0; i++) {
            pasosPacman++;

            // Obtenemos el factor de penalización suave para este nodo específico
            double penalizacionNodo = calcularPenalizacionAnticipacion(game, n, pasosPacman);
            penalizacionAcumulada += penalizacionNodo;

            // Calculamos la utilidad normal del nodo restándole el castigo por proximidad fantasma
            double utilNodo = utilidad(n) - penalizacionNodo;
            peorUtilidadTramo = Math.min(peorUtilidadTramo, utilNodo);

            if (game.isJunction(n) || vecinos[n].length < 2) {
                break;
            }

            int siguiente = (vecinos[n][0] == anterior) ? vecinos[n][1] : vecinos[n][0];
            anterior = n;
            n = siguiente;
        }

        return peorUtilidadTramo == Double.POSITIVE_INFINITY ? Double.NEGATIVE_INFINITY : peorUtilidadTramo;
    }*/

    /**
     * Calcula un valor de castigo proporcional a lo cerca que está el fantasma de llegar antes.
     * Si el fantasma gana por mucho, la penalización es enorme. Si Pac-Man gana o hay margen, es cero.
     */
    private double calcularPenalizacionAnticipacion(Game game, int nodoDestino, int misPasos) {
        double penalizacionTotal = 0.0;
        
        for (GHOST f : GHOST.values()) {
            if (game.isGhostEdible(f)) continue;          // Si es comestible, no penaliza
            if (game.getGhostLairTime(f) > 0) continue;   // Si está en la cárcel, no cuenta

            int nodoFantasma = game.getGhostCurrentNodeIndex(f);
            MOVE ultFantasma = game.getGhostLastMoveMade(f);
            int susPasos = game.getShortestPathDistance(nodoFantasma, nodoDestino, ultFantasma);

            if (susPasos < 0) continue;

            // Margen de seguridad deseado (ej. Queremos llegar al menos 2 o 3 pasos antes)
            int margenDeseado = 2; 
            int diferencia = misPasos + margenDeseado - susPasos;

            // Si el fantasma nos gana o nos pisa los talones (diferencia > 0)
            if (diferencia > 0) {
                // Penalización exponencial o lineal según lo grave que sea la intrusión
                penalizacionTotal += diferencia * 5.0; // Multiplicador ajustable según se quiera más o menos conservador
            }
        }
        return penalizacionTotal;
    }
    /**
     * La píldora de poder es un recurso: con fantasmas encima y cerca atrae; sin presión
     * repele (no se gasta de paso); y si la partida se estanca, se deja de evitar.
     */
    private void elegirSignoDelPoder() {
        if (amenazasCerca >= Constantes.FANTASMAS_PARA_PILDORA) {
            wPoder = wPoderAtrae;
        } else if (estancado) {
            wPoder = 0;
        } else {
            wPoder = -wPoderEvita;
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

    private void pintarCapas(Game game, int yo) {
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
            propagar(caidaComida, comida);
            normalizar(comida);
        }

        // Poder: capa propia, su signo se decide cada turno según la presión
        Arrays.fill(poder, 0);
        if (pills.length > 0 && powers.length > 0) {
            empezar();
            for (int p : powers) sembrar(p, 1.0);
            propagar(caidaPoder, poder);
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
                propagar(caidaPresa, presa);
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
                    sembrar(v, caidaAmenaza);
                }
                propagar(caidaAmenaza, amenaza);
            }
        }
        normalizar(amenaza);
        normalizar(presa);
     // --- Capa de Anticipación eficiente (por propagación de anchura) ---
        Arrays.fill(anticipacion, 0);
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0 || game.getGhostEdibleTime(g) > 0) continue;
            
            int nodoFantasma = game.getGhostCurrentNodeIndex(g);
            
            // Sembramos desde la posición del fantasma y propagamos su influencia de intercepción
            empezar();
            sembrar(nodoFantasma, 1.0);
            propagar(Constantes.CAIDA_AMENAZA, anticipacion); // Reutiliza la caída de amenaza o define una
        }
        normalizar(anticipacion);
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
        anticipacion = new double[n];
        cola = new int[n];
        vecinos = new int[n][];
        for (int i = 0; i < n; i++) {
            int[] v = game.getNeighbouringNodes(i);
            vecinos[i] = v == null ? new int[0] : v;
        }
        claveComida = -1;                  // hay que repintar la comida
    }
    
    @Override
    public String getName() {
        return "MsPacManHLI_EJB_CSB";
    }
}