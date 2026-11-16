package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Ms. Pac-Man controlada por la HFSM. Toda la lógica está en HFSMFactory y Heuristics. */
public class HFSMPacman extends PacmanController {

    private final HFSM hfsm = HFSMFactory.build(new Blackboard());

    @Override
    public MOVE getMove(Game game, long timeDue) {
        MOVE m;
        try {
            m = hfsm.update(game);
        } catch (RuntimeException e) {
            // Última red de seguridad: un fallo imprevisto no debe dejar a Ms. Pac-Man sin respuesta.
            MOVE[] ops = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
            m = ops != null && ops.length > 0 ? ops[0] : MOVE.NEUTRAL;
        }
        lastMove = m;
        return m;
    }

    public String getName() {
        return "HFSMPacman_HLI_EJB_CSB";
    }

    /** Estado hoja activo, para trazas. */
    public String estado() {
        return hfsm.currentLeaf();
    }
}
