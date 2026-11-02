package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman;

import pacman.game.Game;

/**
 * El MsPacManInput del turno en curso, compartido entre el controlador y las
 * acciones. El RuleEngine llama a RulesAction.execute(Game), que solo recibe
 * el Game; si cada acción construyera su propio MsPacManInput se calcularían
 * dos veces por turno los clústeres, las rutas y el plan de caza.
 *
 * Es un objeto por controlador (no static), así cada partida en paralelo
 * tiene el suyo.
 */
public class Turno {

    private MsPacManInput input;

    public void set(MsPacManInput input) {
        this.input = input;
    }

    /** El Input del turno; null si aún no se ha calculado o es de otro Game. */
    public MsPacManInput get(Game game) {
        MsPacManInput in = input;
        if (in == null || in.getGame().getTotalTime() != game.getTotalTime())
            return null;
        return in;
    }
}
