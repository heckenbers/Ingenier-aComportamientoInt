package es.ucm.fdi.ici.c2627.practica2.grupoHEC;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.EncerradoAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible.DispersarseAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible.HuidaEficienteAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible.RefugiarseAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto.Papel;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal.InterceptarAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal.PatrullarPildorasAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal.VigilarPPAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.peligro.BloquearPPAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.peligro.HuirDePPAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible.PacManCercaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible.PacManLejosTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible.PuedeRefugiarseTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.EncerradoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.SaleDeCarcelTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.SeVuelveComestibleTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global.VuelveANormalTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.normal.TienePapelTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro.CercaDePPTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro.PuedeBloquearPPTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro.LlegaAntesAPPTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro.PacManSeAlejaDePPTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro.SeComePPTransition;
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
 *   ENCERRADO <-> NORMAL (PERSEGUIR, INTERCEPTAR, VIGILAR PP, PATRULLAR PÍLDORAS)
 *   NORMAL    <-> COMESTIBLE (HUIDA EFICIENTE, REFUGIARSE EN COMPAÑERO, DISPERSARSE A ESQUINAS)
 *   NORMAL    <-> PELIGRO POR PÍLDORA (HUIDA EFICIENTE, BLOQUEAR POWER PILL)
 *   PELIGRO    -> COMESTIBLE
 *   COMESTIBLE, PELIGRO -> ENCERRADO
 */
public class Ghosts extends GhostController {

	/** Máquina externa de cada fantasma (la que se ejecuta en cada turno). */
	private final EnumMap<GHOST, FSM> fsms = new EnumMap<>(GHOST.class);

	/** Máquinas internas de los estados compuestos: reset() de la externa no las reinicia. */
	private final List<FSM> internas = new ArrayList<>();

	/** Reparto del turno actual: lo necesita GhostsInput (histéresis) y algunas acciones (destinos). */
	private Reparto reparto = new Reparto();

	/**
	 * Las acciones solo reciben el Game, así que las que necesitan el reparto (refugio, esquina)
	 * lo leen a través de esto. Devuelve siempre el del turno en curso, porque getMove()
	 * actualiza 'reparto' antes de ejecutar las máquinas.
	 */
	private final Supplier<Reparto> repartoActual = () -> reparto;

	public Ghosts() {
		for (GHOST g : GHOST.values())
			fsms.put(g, crearMaquina(g));
	}

	/** Construye la máquina de un fantasma. Cada fantasma tiene sus propios estados y transiciones. */
	private FSM crearMaquina(GHOST g) {
		// Observadores de este fantasma: el primero es el de la externa, luego los de las internas
		List<GraphFSMObserver> observadores = new ArrayList<>();
		FSM fsm = nuevaMaquina(g.name(), observadores);

		CompoundState estadoNormal = crearNormal(g, observadores);
		CompoundState estadoComestible = crearComestible(g, observadores);
		CompoundState estadoPeligro = crearPeligro(g, observadores);
		SimpleState encerrado = new SimpleState("ENCERRADO", new EncerradoAction(g));

		// ---- Transiciones entre superestados ----
		// En cada estado, "es comido" se añade primero: es la que manda si coincide con otra.
		fsm.add(encerrado, new SaleDeCarcelTransition(g, "Termina tiempo de cárcel"), estadoNormal);

		fsm.add(estadoNormal, new EncerradoTransition(g, "Fantasma es comido"), encerrado);
		fsm.add(estadoNormal, new SeVuelveComestibleTransition(g, "Ms. Pacman se come PP"), estadoComestible);
		fsm.add(estadoNormal, new CercaDePPTransition(g, "Ms. Pacman está cerca de PP (*I)"), estadoPeligro);

		fsm.add(estadoComestible, new EncerradoTransition(g, "Fantasma comestible es comido"), encerrado);
		fsm.add(estadoComestible, new VuelveANormalTransition(g, "Deja de ser comestible o fuera de peligro (*II)"), estadoNormal);

		// Un fantasma en PELIGRO no es comestible: solo vuelve a la cárcel si muere Ms. Pac-Man o cambia de nivel
		fsm.add(estadoPeligro, new EncerradoTransition(g, "Muere Ms. Pacman o cambia de nivel"), encerrado);
		fsm.add(estadoPeligro, new SeComePPTransition(g, "Ms. Pacman se come la PP amenazada"), estadoComestible);
		fsm.add(estadoPeligro, new PacManSeAlejaDePPTransition("Ms. Pacman se aleja de la PP"), estadoNormal);

		fsm.ready(encerrado);
		mostrarVentana(g, observadores);
		return fsm;
	}

	/**
	 * Crea una máquina y, con DEBUG, le pone su observador. Cada FSM necesita el suyo: el de
	 * la externa dibuja COMESTIBLE como un solo nodo, sin lo de dentro. El observador va justo
	 * después de new FSM(...) y antes de cualquier add(). No abre ventana: los paneles se
	 * juntan en una sola por fantasma en mostrarVentana().
	 */
	private FSM nuevaMaquina(String id, List<GraphFSMObserver> observadores) {
		FSM fsm = new FSM(id);
		if (GhostsConstants.DEBUG) {
			GraphFSMObserver observador = new GraphFSMObserver(id);
			fsm.addObserver(observador);
			observadores.add(observador);
		}
		return fsm;
	}

	/**
	 * Una ventana por fantasma: arriba la máquina externa y debajo, una al lado de otra, las
	 * internas de sus estados compuestos. Así se ve a la vez en qué superestado está y en qué
	 * subestado dentro de él.
	 */
	private static void mostrarVentana(GHOST g, List<GraphFSMObserver> observadores) {
		if (!GhostsConstants.DEBUG || observadores.isEmpty()) return;

		// Swing solo debe tocarse desde su propio hilo (EDT). Montar la ventana desde el hilo
		// principal puede bloquear el programa antes de que se abra la ventana del juego.
		SwingUtilities.invokeLater(() -> {
			try {
				JPanel principal = new JPanel(new BorderLayout());
				principal.add(observadores.get(0).getAsPanel(true, new Dimension(500, 300)), BorderLayout.CENTER);

				JPanel internas = new JPanel(new GridLayout(1, 0));      // una columna por máquina interna
				for (int i = 1; i < observadores.size(); i++)
					internas.add(observadores.get(i).getAsPanel(true, new Dimension(350, 250)));
				principal.add(internas, BorderLayout.SOUTH);

				JFrame ventana = new JFrame(g.name());
				ventana.getContentPane().add(principal);
				ventana.pack();
				ventana.setAutoRequestFocus(false);   // que no se ponga delante del juego
				ventana.setVisible(true);
			} catch (Exception e) {
				// Si falla la depuración, que la partida siga igualmente
				System.err.println("No se pudo mostrar la ventana de " + g + ":");
				e.printStackTrace();
			}
		});
	}

	/**
	 * NORMAL (compuesto), empieza en PERSEGUIR. Nota *V: cada fantasma hace el papel que le da
	 * el reparto de GhostsInput (perseguidor, interceptor, vigilante o patrulla). Desde cada
	 * estado sale una flecha "Le toca X" hacia cada uno de los otros tres: 12 flechas.
	 */
	private CompoundState crearNormal(GHOST g, List<GraphFSMObserver> observadores) {
		FSM normal = nuevaMaquina(g + " NORMAL", observadores);

		Papel[] papeles = { Papel.PERSEGUIR, Papel.INTERCEPTAR, Papel.VIGILAR, Papel.PATRULLAR };
		String[] nombres = { "PERSEGUIR", "INTERCEPTAR", "VIGILAR PP", "PATRULLAR PÍLDORAS" };
		SimpleState[] estados = {
				new SimpleState(nombres[0], new PerseguirAction(g)),
				new SimpleState(nombres[1], new InterceptarAction(g, repartoActual)),
				new SimpleState(nombres[2], new VigilarPPAction(g)),
				new SimpleState(nombres[3], new PatrullarPildorasAction(g)) };

		// El nombre lleva el origen porque no puede repetirse dentro de la máquina (el
		// observador lo usa como identificador de la arista)
		for (int i = 0; i < estados.length; i++)
			for (int j = 0; j < estados.length; j++)
				if (i != j)
					normal.add(estados[i],
							new TienePapelTransition(g, papeles[j], "Le toca " + nombres[j] + " (desde " + nombres[i] + ")"),
							estados[j]);

		normal.ready(estados[0]);                 // la interna, lista antes de envolverla
		internas.add(normal);
		return new CompoundState("NORMAL", normal);
	}

	/**
	 * COMESTIBLE (compuesto), empieza en HUIDA EFICIENTE:
	 *   HUIDA       -> REFUGIARSE  "Puede refugiarse (*IV)"   (se añade primero: tiene prioridad)
	 *   REFUGIARSE  -> HUIDA       "No puede refugiarse"
	 *   HUIDA       -> DISPERSARSE "Ms. Pacman está lejos"
	 *   DISPERSARSE -> REFUGIARSE  "Puede refugiarse desde la esquina (*IV)" (antes que la de cerca: prioridad)
	 *   DISPERSARSE -> HUIDA       "Ms. Pacman está cerca"
	 *
	 * Los nombres de las flechas no se pueden repetir dentro de una máquina: GraphFSMObserver
	 * usa el nombre como identificador de la arista, y dos iguales dan error al crearla.
	 */
	private CompoundState crearComestible(GHOST g, List<GraphFSMObserver> observadores) {
		FSM comestible = nuevaMaquina(g + " COMESTIBLE", observadores);

		SimpleState huida = new SimpleState("HUIDA EFICIENTE", new HuidaEficienteAction(g));
		SimpleState refugiarse = new SimpleState("REFUGIARSE EN COMPAÑERO", new RefugiarseAction(g, repartoActual));
		SimpleState dispersarse = new SimpleState("DISPERSARSE A ESQUINAS", new DispersarseAction(g, repartoActual));

		comestible.add(huida, new PuedeRefugiarseTransition(g, false, "Puede refugiarse (*IV)"), refugiarse);
		comestible.add(refugiarse, new PuedeRefugiarseTransition(g, true, "No puede refugiarse"), huida);
		comestible.add(huida, new PacManLejosTransition(g, "Ms. Pacman está lejos"), dispersarse);
		comestible.add(dispersarse, new PuedeRefugiarseTransition(g, false, "Puede refugiarse desde la esquina (*IV)"), refugiarse);
		comestible.add(dispersarse, new PacManCercaTransition(g, "Ms. Pacman está cerca"), huida);

		comestible.ready(huida);
		internas.add(comestible);
		return new CompoundState("COMESTIBLE", comestible);
	}

	/**
	 * PELIGRO POR PÍLDORA (compuesto), empieza en HUIDA EFICIENTE:
	 *   HUIDA   -> BLOQUEAR "Llega antes a la PP (*III)"   (con ventaja extra y siendo el más cercano)
	 *   BLOQUEAR -> HUIDA   "Ya no llega antes a la PP"
	 */
	private CompoundState crearPeligro(GHOST g, List<GraphFSMObserver> observadores) {
		FSM peligro = nuevaMaquina(g + " PELIGRO POR PÍLDORA", observadores);

		SimpleState huida = new SimpleState("HUIDA EFICIENTE", new HuirDePPAction(g));
		SimpleState bloquear = new SimpleState("BLOQUEAR POWER PILL", new BloquearPPAction(g));

		peligro.add(huida, new PuedeBloquearPPTransition(g, "Llega antes a la PP (*III)"), bloquear);
		peligro.add(bloquear, new LlegaAntesAPPTransition(g, true, "Ya no llega antes a la PP"), huida);

		peligro.ready(huida);
		internas.add(peligro);
		return new CompoundState("PELIGRO POR PÍLDORA", peligro);
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
		reparto = input.getReparto();                          // el de este turno: lo leen las acciones y el siguiente input

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