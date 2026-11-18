package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.BloquearPPAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.CazarAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.CerrarPasoAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.DispersarseAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.HuidaEficienteAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.HuirDePPAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.InterceptarAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.RefugiarseAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions.VigilarPPAction;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import es.ucm.fdi.ici.fuzzy.FuzzyEngineObserver;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Fantasmas con razonamiento borroso: un FuzzyEngine independiente por fantasma
 * que carga el fichero .fcl indicado y selecciona la acción con mayor puntuación.
 */
public class Ghosts extends GhostController {

	public static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/grupoHLI_CSB_EJB/";
	public static final String FCL_CONCURSO = "ghosts-v6.fcl";

	private final EnumMap<GHOST, FuzzyEngine> motores = new EnumMap<>(GHOST.class);
	private final EnumMap<GHOST, UltimaAccion> ultimas = new EnumMap<>(GHOST.class);

	public Ghosts() {
		this(FCL_CONCURSO);
	}

	public Ghosts(String ficheroFcl) {
		setName("Ghosts grupoHLI_CSB_EJB");
		setTeam("grupoHLI_CSB_EJB");
		for (GHOST g : GHOST.values()) {
			Action[] acciones = accionesDe(g);
			FuzzyEngine motor = new FuzzyEngine(
					g.name(),
					FCL_PATH + ficheroFcl,
					"Ghost",
					new MaxActionSelector(acciones));
			UltimaAccion ultima = new UltimaAccion();
			motor.addObserver(ultima);
			motores.put(g, motor);
			ultimas.put(g, ultima);
		}
	}

	private static Action[] accionesDe(GHOST g) {
		return new Action[] {
				// Supervivencia (comestible) -> prioridad en caso de empate
				new HuidaEficienteAction(g),
				new RefugiarseAction(g),
				new DispersarseAction(g),
				new HuirAction(g),
				// Peligro por píldora de poder
				new BloquearPPAction(g),
				new HuirDePPAction(g),
				// Caza coordinada
				new CerrarPasoAction(g),
				new InterceptarAction(g),
				new CazarAction(g),
				new VigilarPPAction(g),
				new PerseguirAction(g)
		};
	}

	public FuzzyEngine getMotor(GHOST g) {
		return motores.get(g);
	}

	@Override
	public void preCompute(String opponent) {
		for (UltimaAccion u : ultimas.values()) u.olvidar();
	}

	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		EnumMap<GHOST, MOVE> movimientos = new EnumMap<>(GHOST.class);
		for (GHOST g : GHOST.values()) {
			if (!game.doesGhostRequireAction(g)) continue;
			GhostsInput input = new GhostsInput(game, g, ultimas.get(g).get());
			movimientos.put(g, motores.get(g).run(input.getFuzzyValues(), game));
		}
		return movimientos;
	}

	/** Observador interno que guarda el id de la última acción elegida en cada turno. */
	private static class UltimaAccion implements FuzzyEngineObserver {
		private String accion = "";

		@Override
		public void actionFired(String action, HashMap<String, Double> input, HashMap<String, Double> output,
				Collection<String> rules) {
			if (action != null) accion = action.toString(); // usa el nombre del primer parámetro generado
		}

		public String get() { return accion; }
		public void olvidar() { accion = ""; }

		@Override
		public String getObservedRules() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}