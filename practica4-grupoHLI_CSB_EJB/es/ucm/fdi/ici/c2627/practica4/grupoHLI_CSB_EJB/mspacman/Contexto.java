package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman;

import pacman.game.Constants;
import pacman.game.Game;

/**
 * Lo que comparten el controlador, el Input y las acciones dentro de un mismo controlador:
 * la percepción de este tick (para no calcularla dos veces), los mapas de los laberintos y la
 * memoria. Uno por controlador, en un atributo: ningún estado estático.
 */
public class Contexto {

    /**
     * El preproceso de los cuatro laberintos, hecho en el constructor: hacerlo en el primer
     * tick de cada nivel costaba ~470 ms, muy por encima de los 40 ms de un turno.
     */
    private final MapaNivel[] mapas = new MapaNivel[Constants.NUM_MAZES];
    private MapaNivel mapa;
    private Percepcion percepcion;
    private final MemoriaPacMan memoria = new MemoriaPacMan();

    public Contexto() {
        for (int i = 0; i < mapas.length; i++)
            mapas[i] = MapaNivel.para(new Game(0, i), null);
    }

    /** Al principio de cada tick: recalcula la percepción con el mapa del laberinto actual. */
    public Percepcion actualizar(Game game) {
        int m = game.getMazeIndex();
        MapaNivel previo = (m >= 0 && m < mapas.length) ? mapas[m] : mapa;
        mapa = MapaNivel.para(game, previo);        // por si acaso no coincide: lo rehace
        percepcion = new Percepcion(game, mapa);
        memoria.registrarTurno(game.getTotalTime(), game.getNumberOfActivePills());
        return percepcion;
    }

    public Percepcion getPercepcion() { return percepcion; }

    public MemoriaPacMan getMemoria() { return memoria; }

    public void reiniciar() {
        memoria.reiniciar();
    }
}
