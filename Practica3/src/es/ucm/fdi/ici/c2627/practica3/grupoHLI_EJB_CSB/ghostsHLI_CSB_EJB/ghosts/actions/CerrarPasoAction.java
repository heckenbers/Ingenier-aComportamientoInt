package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * CERRAR PASO: Ms. Pac-Man está en un pasillo y no puede darse la vuelta: tiene que salir por el
 * cruce del final. El fantasma va a ese cruce y, al llegar (o si ya está dentro del pasillo),
 * entra de frente hacia ella. Si ella ya no está en un pasillo, va a por ella.
 */
public class CerrarPasoAction implements RulesAction {

	private final GHOST ghost;

	public CerrarPasoAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pacman = game.getPacmanCurrentNodeIndex();
		Movimientos.Tramo pasillo = Movimientos.tramoPacman(game);
		if (pasillo == null) return Movimientos.irHacia(game, ghost, pacman);

		int yo = game.getGhostCurrentNodeIndex(ghost);
		if (yo == pasillo.cruce || pasillo.nodos.contains(yo))
			return Movimientos.irHacia(game, ghost, pacman);       // ya está delante de ella: de frente

		return Movimientos.irHacia(game, ghost, pasillo.cruce);
	}

	@Override
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "cerrarPaso";
	}
}