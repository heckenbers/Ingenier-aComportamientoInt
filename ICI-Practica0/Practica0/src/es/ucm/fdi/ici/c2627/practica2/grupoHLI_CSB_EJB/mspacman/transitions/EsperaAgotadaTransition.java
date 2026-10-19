package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * Ms. Pac-Man lleva 'maxTurnos' turnos o más esperando junto a la píldora de poder. Se evalúa una vez por turno
 * mientras la máquina está en ESPERAR, así que dos evaluaciones separadas por más de un turno (o un reloj que
 * retrocede, partida nueva) significan que se ha vuelto a entrar y la cuenta empieza de cero. El estado es un
 * atributo de la instancia, no estático: cada controlador tiene el suyo.
 */
public class EsperaAgotadaTransition implements Transition {

    private final int maxTurnos;
    private int inicio = -1;
    private int ultimo = -1;

    public EsperaAgotadaTransition(int maxTurnos) {
        this.maxTurnos = maxTurnos;
    }

    @Override
    public boolean evaluate(Input in) {
        int ahora = in.getGame().getTotalTime();
        if (ultimo < 0 || ahora < ultimo || ahora - ultimo > 1) {
            inicio = ahora;
        }
        ultimo = ahora;
        return ahora - inicio >= maxTurnos;
    }

    @Override
    public String toString() {
        return "Espera de " + maxTurnos + " turnos agotada";
    }
}