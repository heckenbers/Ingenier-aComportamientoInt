package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Hay menos de 'cuantos' fantasmas no comestibles a 'distancia' nodos o menos de Ms. Pac-Man. Mientras se
 * cumple, merece la pena esperar junto a la píldora de poder a que se acerquen más fantasmas.
 */
public class PocosFantasmasCercaTransition implements Transition {

    private final int cuantos;
    private final int distancia;

    public PocosFantasmasCercaTransition(int cuantos, int distancia) {
        this.cuantos = cuantos;
        this.distancia = distancia;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.numFantasmasPeligrososA(distancia) < cuantos;
    }

    @Override
    public String toString() {
        return "Menos de " + cuantos + " fantasmas a <= " + distancia;
    }
}