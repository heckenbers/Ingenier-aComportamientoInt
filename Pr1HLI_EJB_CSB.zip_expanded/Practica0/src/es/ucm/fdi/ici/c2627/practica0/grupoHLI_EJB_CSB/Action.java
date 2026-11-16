package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import pacman.game.Constants.MOVE;

/** Lo que hace un estado. execute devuelve el movimiento elegido (las acciones de entrada/salida lo ignoran). */
public interface Action {
    MOVE execute(GameView in, Blackboard bb);

    default void onEnter(GameView in, Blackboard bb) {
    }

    default void onExit(GameView in, Blackboard bb) {
    }
}
