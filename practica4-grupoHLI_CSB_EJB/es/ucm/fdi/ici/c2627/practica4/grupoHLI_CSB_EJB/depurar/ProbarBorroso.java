package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.depurar;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Prueba un .fcl de Ms. Pac-Man sin jugar. Cada caso parte de "nada pasa" (fantasmas lejos,
 * huida holgada, sin comestibles ni cebo) y cambia sólo lo que importa. Dice si gana la acción
 * esperada, con el mismo desempate que MaxActionSelector (el orden del array de MsPacMan), y
 * avisa de EMPATE si las dos primeras salidas están a menos de 0,2, aunque el caso salga BIEN.
 *
 * Uso: ProbarBorroso [mspacman-vN.fcl]. Los casos de una versión se comprueban en ella y en
 * las siguientes, así que una versión nueva tiene que dejar todos los viejos en BIEN.
 */
public class ProbarBorroso {

    static final String RUTA = "bin/es/ucm/fdi/ici/c2627/practica4/grupoHLI_CSB_EJB/";
    static final String BLOQUE = "MsPacMan";
    /** El orden del array de acciones de MsPacMan: decide los empates. */
    static final String[] ORDEN = { "escapar", "territorio", "territorioComida", "territorioPoder", "evadir", "cazar", "comerCebo", "esperarCebo", "irAPildora", "rumbo", "recolectar" };
    static final double CASI_EMPATE = 0.2;

    static int fallos = 0, empates = 0, version;

    public static void main(String[] args) {
        String fichero = args.length > 0 ? args[0] : "mspacman-v0.fcl";
        version = Integer.parseInt(fichero.replaceAll("\\D", ""));
        FIS fis = FIS.load(RUTA + fichero, true);
        if (fis == null) { System.out.println("No encuentro " + RUTA + fichero); return; }
        FunctionBlock fb = fis.getFunctionBlock(BLOQUE);
        System.out.println("== " + fichero + " ==");

        // Cada caso dice en qué versiones vale: "0-" de la v0 en adelante, "1,2" sólo en esas.
        // Ramas: v0-v1-v4-v5 (v2, v3 descartadas) -> v5 da v6, v7-v8, v9, v10-v13 (descartadas)
        // y v14 (a concurso), de la que salen v15, v16 y v17 (descartadas).
        // (La v2 se descartó, así que la v4 sale de la v1 y no lleva sus casos.)
        // ---- v0: escapar, evadir, recolectar, cazar ----
        caso(fb, "0-8,10-", "nada pasa: recolecta",                        "recolectar", m());
        caso(fb, "0-", "fantasma a 10 ticks, huida con margen: evade", "evadir",    m("amenaza", 10, "margen", 15));
        caso(fb, "0-", "fantasma a 3 ticks: escapa",                  "escapar",    m("amenaza", 3, "margen", 15));
        caso(fb, "0-13", "sin salida segura (margen 2): escapa",         "escapar",    m("amenaza", 25, "margen", 2));
        caso(fb, "0-8,10-", "fantasma a 30 ticks: sigue recolectando",      "recolectar", m("amenaza", 30, "margen", 25));
        caso(fb, "0-", "dos comestibles a tiro, nadie cerca: caza",    "cazar",      m("caza", 2));
        caso(fb, "0-4", "uno a tiro, pero vuelve en 5 ticks: evade",   "evadir",     m("caza", 1, "revertir", 5, "amenaza", 40));
        caso(fb, "0-", "uno a tiro con fantasma a 3: escapa",          "escapar",    m("caza", 1, "amenaza", 3, "margen", 10));

        // ---- v1: el cebo ----
        caso(fb, "1-", "cebo de 2 fantasmas, recién llegada: espera",    "esperarCebo", m("cebo", 2));
        caso(fb, "1-", "cebo de 3 fantasmas: se lo come",                "comerCebo",   m("cebo", 3));
        caso(fb, "1-10,12-", "cebo de 2 tras 100 ticks esperando: se lo come", "comerCebo",   m("cebo", 2, "espera", 100));
        caso(fb, "1-8,10-", "cebo de 1 fantasma: no vale, recolecta",         "recolectar",  m("cebo", 1));
        caso(fb, "1-", "cebo de 3 con fantasma a 3: escapa",             "escapar",     m("cebo", 3, "amenaza", 3, "margen", 10));
        caso(fb, "1,2", "cebo de 2 con fantasma a 10: evade",            "evadir",      m("cebo", 2, "amenaza", 10, "margen", 15));

        // ---- v2 (descartada): con margen holgado, un fantasma cerca no hace huir ----
        caso(fb, "2,3", "fantasma a 10, margen 25: sigue recolectando",  "recolectar", m("amenaza", 10, "margen", 25));
        caso(fb, "2,3", "fantasma a 10, margen 10: evade",               "evadir",     m("amenaza", 10, "margen", 10));
        caso(fb, "4-", "fantasma a 10, margen 25: evade (como la v1)",   "evadir",     m("amenaza", 10, "margen", 25));

        // ---- v3, y v4 sobre la v1: el cebo, cuando el fantasma ya viene ----
        caso(fb, "3-", "cebo de 2 con fantasma a 10: se lo come",        "comerCebo",  m("cebo", 2, "amenaza", 10, "margen", 15));
        caso(fb, "3-", "cebo de 3 con fantasma a 10 y margen 25: come",  "comerCebo",  m("cebo", 3, "amenaza", 10, "margen", 25));
        caso(fb, "3-14,16-", "cebo de 1 con fantasma a 10: evade",             "evadir",     m("cebo", 1, "amenaza", 10, "margen", 10));

        // ---- v6: ir derecha a la píldora más cercana si está lejos ----
        caso(fb, "6", "píldora a 40 pasos, nada más: va a por ella",  "irAPildora", m("pildora", 40));
        caso(fb, "6", "píldora a 40 con fantasma a 10: evade",        "evadir",     m("pildora", 40, "amenaza", 10, "margen", 15));
        caso(fb, "6", "píldora a 40 y cebo de 2: espera",             "esperarCebo", m("pildora", 40, "cebo", 2));
        caso(fb, "6", "píldora a 40 y dos a tiro: caza",              "cazar",      m("pildora", 40, "caza", 2));
        if (en("6")) curva(fb, "pildora", 0, 60, 4, m());

        // ---- v7: con cebo, la regla 5 ya no sube recolectar (el hueco hacia los 80 ticks) ----
        // (en 80 es el cruce entre esperar y comer: ahí antes ganaba recolectar)
        caso(fb, "7,8", "cebo de 2 tras 70 ticks: sigue esperando",     "esperarCebo", m("cebo", 2, "espera", 70));
        caso(fb, "7,8", "cebo de 2 tras 90 ticks: se lo come",          "comerCebo",   m("cebo", 2, "espera", 90));

        // ---- v8: con hambre y sin peligro, el camino más corto ----
        caso(fb, "8", "150 ticks sin comer, nada más: rumbo",         "rumbo",      m("hambre", 150));
        caso(fb, "8", "150 sin comer con fantasma a 10: evade",       "evadir",     m("hambre", 150, "amenaza", 10, "margen", 15));
        caso(fb, "8", "150 sin comer y dos a tiro: caza",             "cazar",      m("hambre", 150, "caza", 2));
        caso(fb, "8", "150 sin comer y cebo de 3: se lo come",        "comerCebo",  m("hambre", 150, "cebo", 3));
        caso(fb, "8", "150 sin comer esperando cebo de 2: rumbo",     "rumbo",      m("hambre", 150, "cebo", 2, "espera", 30));
        caso(fb, "8", "40 ticks sin comer: recolecta",                "recolectar", m("hambre", 40));
        if (en("8")) curva(fb, "hambre", 0, 300, 20, m());

        // ---- v14: territorio sin ruta segura; v17: también si la zona libre se cierra ----
        caso(fb, "14-17", "sin ruta segura, fantasma a 25: territorio",  "territorio", m("amenaza", 25, "margen", 2));
        caso(fb, "14,15,17-", "fantasma a 3, sin ruta segura: escapa",        "escapar",    m("amenaza", 3, "margen", 2));
        caso(fb, "18", "sin ruta segura, fantasma a 25: territorioComida", "territorioComida", m("amenaza", 25, "margen", 2));
        caso(fb, "19", "sin ruta segura, fantasma a 25: territorioPoder",  "territorioPoder",  m("amenaza", 25, "margen", 2));
        caso(fb, "17", "zona libre de 30 nodos, fantasma a 10: territorio", "territorio", m("espacio", 30, "amenaza", 10, "margen", 10));
        caso(fb, "17", "zona libre de 30, fantasma a 40: recolecta",   "recolectar", m("espacio", 30, "amenaza", 40, "margen", 25));
        caso(fb, "17", "zona amplia, fantasma a 10: evade",            "evadir",     m("espacio", 300, "amenaza", 10, "margen", 10));
        if (en("17")) curva(fb, "espacio", 0, 300, 20, m("amenaza", 10, "margen", 10));

        if (en("2,3")) curva(fb, "margen", -10, 40, 3, m("amenaza", 10));
        if (en("3-")) curva(fb, "cebo", 0, 4, 0.25, m("amenaza", 10, "margen", 15));
        if (en("1-")) {
            curva(fb, "cebo", 0, 4, 0.25, m());
            curva(fb, "espera", 0, 120, 10, m("cebo", 2));
        }

        // La curva de control de amenaza, con huida holgada
        curva(fb, "amenaza", 0, 60, 4, m("margen", 25));
        // y la de margen, con el fantasma a 20 ticks
        curva(fb, "margen", -10, 40, 3, m("amenaza", 20));

        System.out.println(fallos == 0 ? "\nTodos los casos bien." : "\n" + fallos + " casos MAL.");
        if (empates > 0) System.out.println(empates + " casos con EMPATE (dos salidas a menos de " + CASI_EMPATE + ").");
    }

    /** "Nada pasa" más los cambios: pares nombre, valor. */
    static Map<String, Double> m(Object... cambios) {
        Map<String, Double> v = new LinkedHashMap<>();
        v.put("amenaza", 60.0); v.put("margen", 40.0); v.put("caza", 0.0); v.put("revertir", 60.0);
        v.put("cebo", 0.0); v.put("espera", 0.0); v.put("quedan", 80.0); v.put("pildora", 3.0); v.put("hambre", 0.0); v.put("espacio", 300.0);
        for (int i = 0; i < cambios.length; i += 2)
            v.put((String) cambios[i], ((Number) cambios[i + 1]).doubleValue());
        return v;
    }

    /** ¿Vale para esta versión? "0-": de la 0 en adelante; "0-4": de la 0 a la 4; "1,2": sólo esas. */
    static boolean en(String versiones) {
        for (String p : versiones.split(",")) {
            p = p.trim();
            if (p.endsWith("-") && version >= Integer.parseInt(p.substring(0, p.length() - 1))) return true;
            if (p.contains("-") && !p.endsWith("-")) {
                String[] r = p.split("-");
                if (version >= Integer.parseInt(r[0]) && version <= Integer.parseInt(r[1])) return true;
            } else if (!p.endsWith("-") && version == Integer.parseInt(p)) return true;
        }
        return false;
    }

    static void caso(FunctionBlock fb, String versiones, String nombre, String esperada, Map<String, Double> e) {
        if (!en(versiones)) return;
        evaluar(fb, e);
        String gana = gana(fb);
        boolean bien = gana.equals(esperada);
        if (!bien) fallos++;
        boolean empate = margenGanadora(fb) < CASI_EMPATE;
        if (empate) empates++;
        System.out.printf("%s  %-46s gana %-11s (esperaba %s)%s%n      salidas: %s%n      reglas: %s%n",
                bien ? "BIEN" : "MAL ", nombre, gana, esperada, empate ? "  EMPATE" : "", salidas(fb), activas(fb));
    }

    static void curva(FunctionBlock fb, String var, double desde, double hasta, double paso, Map<String, Double> base) {
        System.out.printf("%n%-8s salidas -> gana   (resto: %s)%n", var, base);
        for (double x = desde; x <= hasta + 1e-9; x += paso) {
            base.put(var, x);
            evaluar(fb, base);
            System.out.printf("%6.2f   %s -> %s%s%n", x, salidas(fb), gana(fb), margenGanadora(fb) < CASI_EMPATE ? "  EMPATE" : "");
        }
    }

    static void evaluar(FunctionBlock fb, Map<String, Double> e) {
        for (Map.Entry<String, Double> x : e.entrySet())     // todas las entradas, cada vez
            fb.setVariable(x.getKey(), x.getValue());
        fb.evaluate();
    }

    static double valor(FunctionBlock fb, String salida) {
        Variable v = fb.getVariable(salida);
        return v == null ? Double.NEGATIVE_INFINITY : v.getLatestDefuzzifiedValue();
    }

    /** Como MaxActionSelector: la más alta, y en un empate la que va antes en ORDEN. */
    static String gana(FunctionBlock fb) {
        String gana = "(ninguna)";
        double max = Double.NEGATIVE_INFINITY;
        for (String s : ORDEN)
            if (valor(fb, s) > max) { max = valor(fb, s); gana = s; }
        return gana;
    }

    static double margenGanadora(FunctionBlock fb) {
        double a = Double.NEGATIVE_INFINITY, b = Double.NEGATIVE_INFINITY;
        for (String s : ORDEN) {
            double v = valor(fb, s);
            if (v > a) { b = a; a = v; } else if (v > b) b = v;
        }
        return a - b;
    }

    static String salidas(FunctionBlock fb) {
        StringBuilder sb = new StringBuilder();
        for (String s : ORDEN)
            if (fb.getVariable(s) != null) sb.append(String.format("%s=%.1f ", s, valor(fb, s)));
        return sb.toString().trim();
    }

    static String activas(FunctionBlock fb) {
        StringBuilder sb = new StringBuilder();
        for (Rule r : fb.getRuleBlocks().values().iterator().next().getRules())
            if (r.getDegreeOfSupport() > 0) sb.append(String.format(" R%s:%.2f", r.getName(), r.getDegreeOfSupport()));
        return sb.toString();
    }
}
