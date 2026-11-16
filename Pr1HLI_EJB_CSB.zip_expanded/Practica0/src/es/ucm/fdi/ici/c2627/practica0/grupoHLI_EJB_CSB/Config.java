package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

/** Todos los umbrales de la HFSM. Los marcados (estimado) no están medidos: son el punto de partida a afinar. */
public final class Config {
    private Config() {
    }

    // ---- Seguridad
    /** δ: holgura sobre EAT_DISTANCE al comparar quién llega antes a un cruce. */
    public static final int DELTA = 1;
    /** δ_clear: margen a partir del cual Evade considera que ya está a salvo (estimado). */
    public static final int DELTA_CLEAR = 6;
    /** D: profundidad (en cruces) a la que se exige que exista una salida segura. */
    public static final int DEPTH = 2;
    /** Margen que se devuelve cuando ningún fantasma llega nunca. */
    public static final int MARGIN_CAP = 999;
    /** Margen máximo de un callejón sin salida: aceptable, pero un poco por debajo de "despejado" (estimado). */
    public static final int DEAD_END_MARGIN = 8;
    /** Margen por encima del cual el riesgo se considera 0 (estimado). */
    public static final int RISK_HORIZON = 12;
    /** Distancia de un fantasma a mis próximos cruces por debajo de la cual se pasa a Evade (estimado). */
    public static final int THREAT_DIST = 12;

    // ---- Píldora de poder
    /** E_min: ganancia esperada mínima de la cadena para gastar una píldora de poder = 200·(2² − 1). */
    public static final int E_MIN = 600;
    /** D_cadena: distancia máxima (desde la píldora) a la que un fantasma entra en la cadena (estimado). */
    public static final int D_CADENA = 25;
    /** Duración del miedo que se supone, porque la API no da el nivel (200·0,9^nivel; nivel ≈ 5) (estimado). */
    public static final int FEAR_ESTIMATE = 120;
    /** K: con este número de píldoras o menos, la fase es Endgame. */
    public static final int K_ENDGAME = 30;
    public static final double OPENING_FRACTION = 0.60;
    /** Ticks de pánico: tiempo que se mantiene la persecución de la píldora de poder (estimado). */
    public static final int PANIC_TICKS = 60;
    /** Ticks sin comer tras los cuales se permite gastar la última píldora de poder para acabar el nivel. */
    public static final int STALL_TICKS = 500;

    // ---- Estados
    /** T: ticks sin fantasmas agrupados tras los cuales Bait vuelve a Harvest (estimado). */
    public static final int T_BAIT = 40;
    /** t_ret: miedo restante por debajo del cual, con un normal cerca, se pasa a Retreat (estimado). */
    public static final int T_RET = 15;

    // ---- Pesos de edgeValue
    /** w_R: coste de un riesgo de 1,0 (una vida pesa mucho más que una píldora) (estimado). */
    public static final double W_R = 300;
    /** w_B: coste de volver al cruce del que vengo (estimado). */
    public static final double W_B = 8;
    /** w_F: peso del valor futuro del cruce destino (estimado). */
    public static final double W_F = 4;
    /** Coste de abandonar píldoras sueltas (pasillos cortos con comida) (estimado). */
    public static final double W_ISO = 1.5;
    public static final int ISO_MAX = 4;
    /** Coste de pasar por una píldora de poder que no conviene gastar (estimado). */
    public static final double W_WASTE = 60;
    /** Atracción hacia la píldora de poder en Bait, por nodo de distancia (estimado). */
    public static final double W_BAIT = 2;

    // ---- Fin de partida
    /** Ticks restantes para MAX_TIME a partir de los cuales se prioriza no perder vidas (cada una suma 800). */
    public static final int CAUTIOUS_TICKS_LEFT = 1500;
    public static final double CAUTIOUS_RISK_MULT = 3;
}
