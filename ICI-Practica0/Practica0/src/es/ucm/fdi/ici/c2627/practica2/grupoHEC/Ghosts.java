package es.ucm.fdi.ici.c2627.practica2.grupoHEC;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.EncerradoAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.EncerradoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.SaleDeCarcelTransition;
import es.ucm.fdi.ici.fsm.CompoundState;
import es.ucm.fdi.ici.fsm.FSM;
import es.ucm.fdi.ici.fsm.SimpleState;
import es.ucm.fdi.ici.fsm.observers.GraphFSMObserver;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Controlador de los fantasmas: una máquina de estados por fantasma.
 *
 * Esqueleto (paso 3): ENCERRADO <-> NORMAL, con NORMAL como estado compuesto que de
 * momento solo contiene PERSEGUIR. Los demás superestados y subestados se añaden después
 * sin cambiar esta estructura.
 */
public class Ghosts extends GhostController {

	/** Máquina externa de cada fantasma (la que se ejecuta en cada turno). */
	private final EnumMap<GHOST, FSM> fsms = new EnumMap<>(GHOST.class);

	/** Máquinas internas de los estados compuestos: reset() de la externa no las reinicia. */
	private final List<FSM> internas = new ArrayList<>();

	/** Reparto del turno anterior: lo necesita GhostsInput para la histéresis de papeles y esquinas. */
	private Reparto reparto = new Reparto();

	public Ghosts() {
		for (GHOST g : GHOST.values())
			fsms.put(g, crearMaquina(g));
	}

	/** Construye la máquina de un fantasma. Cada fantasma tiene sus propios estados y transiciones. */
	private FSM crearMaquina(GHOST g) {
		FSM fsm = new FSM(g.name());
		if (GhostsConstants.DEBUG) {
			// El observador va justo después de new FSM(...) y antes de cualquier add()
			GraphFSMObserver observador = new GraphFSMObserver(g.name());
			fsm.addObserver(observador);
			observador.showInFrame(new Dimension(500, 350));
		}

		// ---- NORMAL (compuesto): de momento solo PERSEGUIR ----
		FSM normal = new FSM(g + " NORMAL");
		SimpleState perseguir = new SimpleState(new PerseguirAction(g));
		normal.ready(perseguir);                  // la interna, lista antes de envolverla
		internas.add(normal);
		CompoundState estadoNormal = new CompoundState("NORMAL", normal);

		// ---- ENCERRADO ----
		SimpleState encerrado = new SimpleState("ENCERRADO", new EncerradoAction(g));

		// ---- Transiciones entre superestados ----
		fsm.add(encerrado, new SaleDeCarcelTransition(g, "Termina tiempo de cárcel"), estadoNormal);
		fsm.add(estadoNormal, new EncerradoTransition(g, "Fantasma es comido"), encerrado);

		fsm.ready(encerrado);
		return fsm;
	}

	/** Al empezar cada partida: todas las máquinas (externas e internas) al estado inicial. */
	@Override
	public void preCompute(String opponent) {
		for (FSM fsm : fsms.values()) fsm.reset();
		for (FSM fsm : internas) fsm.reset();
		reparto = new Reparto();
	}

	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		EnumMap<GHOST, MOVE> result = new EnumMap<>(GHOST.class);

		GhostsInput input = new GhostsInput(game, reparto);   // uno por turno, compartido
		reparto = input.getReparto();                          // se guarda para el turno siguiente

		for (GHOST g : GHOST.values()) {
			MOVE m = fsms.get(g).run(input);          // todas las máquinas, en todos los turnos
			if (game.doesGhostRequireAction(g))       // pero solo se ordena en los cruces
				result.put(g, m);
		}
		return result;
	}

	@Override
	public String getName() {
		return "Ghosts grupoHEC";
	}
}