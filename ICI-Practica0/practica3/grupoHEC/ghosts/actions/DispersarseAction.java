package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import java.util.ArrayList;
import java.util.List;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * DISPERSARSE A ESQUINAS (comestible, con Ms. Pac-Man lejos): cada comestible va a una esquina
 * distinta, para que no pueda comérselos en cadena. Las esquinas se reparten igual en los cuatro
 * motores (mismo cálculo sobre el mismo juego): de todas las parejas (comestible, esquina libre),
 * primero la de más margen = lo que tarda ella en llegar a la esquina − lo que tarda él.
 */
public class DispersarseAction implements RulesAction {

	private final GHOST ghost;

	public DispersarseAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int esquina = esquinaDe(game, ghost);
		if (esquina < 0)
			return Movimientos.huidaEficiente(game, ghost,
					game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
		return Movimientos.irHacia(game, ghost, esquina);
	}

	@Override
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "dispersarse";
	}

	/** Reparto voraz de esquinas entre los comestibles; devuelve la de 'yo' o -1. */
	private static int esquinaDe(Game game, GHOST yo) {
		int[] esquinas = Movimientos.esquinas(game);
		boolean[] ocupada = new boolean[esquinas.length];
		List<GHOST> sinEsquina = new ArrayList<>();
		for (GHOST g : GHOST.values())
			if (game.getGhostLairTime(g) <= 0 && game.isGhostEdible(g)) sinEsquina.add(g);

		while (!sinEsquina.isEmpty()) {
			GHOST mejorG = null;
			int mejorE = -1;
			long mejorMargen = Long.MIN_VALUE;
			for (GHOST g : sinEsquina)
				for (int i = 0; i < esquinas.length; i++) {
					if (ocupada[i]) continue;
					long margen = (long) Movimientos.distanciaPacman(game, esquinas[i])
							- (long) GhostsConstants.FACTOR_LENTITUD_COMESTIBLE * Movimientos.distanciaFantasma(game, g, esquinas[i]);
					if (margen > mejorMargen) { mejorMargen = margen; mejorG = g; mejorE = i; }
				}
			if (mejorG == null) return -1;
			if (mejorG == yo) return esquinas[mejorE];
			ocupada[mejorE] = true;
			sinEsquina.remove(mejorG);
		}
		return -1;
	}
}