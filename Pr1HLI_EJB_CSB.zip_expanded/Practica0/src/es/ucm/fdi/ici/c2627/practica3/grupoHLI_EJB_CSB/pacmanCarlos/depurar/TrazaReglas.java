package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.depurar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RuleEngineObserver;
import jess.Activation;
import jess.Defrule;
import jess.JessEvent;
import jess.JessException;
import jess.JessListener;
import jess.Rete;

/**
 * Traza de reglas para DEPURAR (no se entrega enganchada).
 *
 * Se engancha a un RuleEngine:
 *  - como JessListener del Rete, para saber qué reglas se disparan cada turno
 *    (también las que solo asiertan hechos intermedios, como PELIGRO);
 *  - como RuleEngineObserver, para saber qué ACTION gana.
 *
 * resumen() imprime cuántas veces se ha disparado cada regla del fichero
 * (marcando NUNCA las que no) y cuántas veces gana cada acción.
 */
public class TrazaReglas implements JessListener, RuleEngineObserver {

    private final String nombre;
    private final boolean imprimirCambios;
    private final Map<String, Integer> disparos = new LinkedHashMap<>();
    private final Map<String, Integer> victorias = new LinkedHashMap<>();
    private final List<String> reglasTurno = new ArrayList<>();
    private String ultimaGanadora = "";
    private int turnos = 0;

    /**
     * @param imprimirCambios true: imprime una línea cada vez que cambia la acción ganadora.
     */
    public TrazaReglas(String nombre, RuleEngine motor, boolean imprimirCambios) {
        this.nombre = nombre;
        this.imprimirCambios = imprimirCambios;
        Rete rete = reteDe(motor);
        for (Iterator<?> it = rete.listDefrules(); it.hasNext();)
            disparos.put(sinModulo(((Defrule) it.next()).getName()), 0);
        rete.addJessListener(this);
        rete.setEventMask(rete.getEventMask() | JessEvent.DEFRULE_FIRED | JessEvent.RESET);
        motor.addObserver(this);
    }

    @Override
    public void eventHappened(JessEvent e) throws JessException {
        int tipo = e.getType() & ~JessEvent.REMOVED;
        if (tipo == JessEvent.RESET) {
            turnos++;
            reglasTurno.clear();
        } else if (tipo == JessEvent.DEFRULE_FIRED) {
            String regla = sinModulo(((Activation) e.getObject()).getRule().getName());
            reglasTurno.add(regla);
            disparos.merge(regla, 1, Integer::sum);
        }
    }

    @Override
    public void actionFired(String action, Collection<String> facts) {
        String id = slot(action, "id");
        victorias.merge(id, 1, Integer::sum);
        if (imprimirCambios && !id.equals(ultimaGanadora))
            System.out.println(String.format("[%s t=%d] %s gana %s (%s)",
                    nombre, turnos, reglasTurno, id, slot(action, "info")));
        ultimaGanadora = id;
    }

    @Override
    public void ruleEngineReset() {
    }

    public void resumen() {
        System.out.println("===== Resumen de reglas " + nombre + ": " + turnos + " turnos =====");
        for (Map.Entry<String, Integer> e : disparos.entrySet())
            System.out.println(String.format("%8d  %s%s", e.getValue(), e.getKey(),
                    e.getValue() == 0 ? "   <-- NUNCA" : ""));
        System.out.println("----- Acciones ganadoras -----");
        for (Map.Entry<String, Integer> e : victorias.entrySet())
            System.out.println(String.format("%8d  %s", e.getValue(), e.getKey()));
    }

    /** Número de veces que se ha disparado una regla (para comprobaciones). */
    public Map<String, Integer> getDisparos() {
        return disparos;
    }

    /** El atributo jess de RuleEngine no tiene getter: se lee por reflexión, sin modificar el motor. */
    private static Rete reteDe(RuleEngine motor) {
        try {
            Field f = RuleEngine.class.getDeclaredField("jess");
            f.setAccessible(true);
            return (Rete) f.get(motor);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener el Rete del RuleEngine", e);
        }
    }

    private static String sinModulo(String s) {
        return s.startsWith("MAIN::") ? s.substring(6) : s;
    }

    private static String slot(String hecho, String nombre) {
        String clave = "(" + nombre + " ";
        int i = hecho.indexOf(clave);
        if (i < 0)
            return "";
        int ini = i + clave.length();
        if (ini < hecho.length() && hecho.charAt(ini) == '"') {
            int fin = hecho.indexOf('"', ini + 1);
            return (fin < 0) ? "" : hecho.substring(ini + 1, fin);
        }
        int fin = hecho.indexOf(')', ini);
        return (fin < 0) ? "" : hecho.substring(ini, fin).trim();
    }
}
