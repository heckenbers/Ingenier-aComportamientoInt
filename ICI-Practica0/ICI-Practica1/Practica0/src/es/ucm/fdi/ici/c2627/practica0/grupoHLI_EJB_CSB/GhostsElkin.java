package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import pacman.controllers.GhostController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsElkin extends GhostController {
	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
	private MOVE[] allMoves = MOVE.values();
    private Random rnd = new Random();
	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		
		int limit = 10;
		for(GHOST ghostype : GHOST.values()) {
			if(!game.doesGhostRequireAction(ghostype)) continue;
			double pacman = game.getPacmanCurrentNodeIndex();
			if(game.isGhostEdible(ghostype) || (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(), game.getActivePowerPillsIndices(), Constants.DM.EUCLID), Constants.DM.EUCLID) <= limit)) {
				moves.put(ghostype, game.getApproximateNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghostype), game.getGhostLastMoveMade(ghostype), Constants.DM.EUCLID));
			}
			else {
				Random rand = new Random();
				double random = rand.nextDouble();
				if(random < 0.9) {
					moves.put(ghostype, game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghostype), game.getGhostLastMoveMade(ghostype), Constants.DM.EUCLID));
					
				}
				else {
					moves.put(ghostype, allMoves[rnd.nextInt(allMoves.length)]);
				}
					
			}
			
		}
		return null;
	}
	
public class AdministradorFantasmas extends GhostController{
	
	private EnumMap<GHOST, MOVE> mapaDeOrdenes = new EnumMap<GHOST, MOVE>(GHOST.class);
		
	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue){
		
		//Se limpia el mapa en cada tick
		mapaDeOrdenes.clear();
		
		//Empezamos con la asignacion de tareas espeficas para cada fantasma
		
		//Fantasma 1 = Blinky encargado de perseguir a pacman
		if (game.doesGhostRequireAction(GHOST.BLINKY)){
			
			MOVE movimientoBlinky = calcularRutaBFSRastreador(game, GHOST.BLINKY);
			mapaDeOrdenes.put(GHOST.BLINKY, movimientoBlinky);
			
			int nodoPacman = game.getPacmanCurrentNodeIndex(); // ¿Dónde está Pacman?
			int miNodo = game.getGhostCurrentNodeIndex(GHOST.BLINKY); // ¿Dónde estoy yo?

			if (nodoPacman != -1) {
			    // ESTADO 1: ¡LO VEO EN MI UMBRAL! ATAQUE DIRECTO
			    // No gastamos procesamiento en BFS. Persecución pura en línea recta hasta alcanzarlo.
			    MOVE ataque = game.getApproximateNextMoveTowardsTarget(
			        nodoPacman, 
			        miNodo, 
			        game.getGhostLastMoveMade(GHOST.BLINKY), 
			        Constants.DM.EUCLID
			    );
			    moves.put(GHOST.BLINKY, ataque);

			} else {
			    // ESTADO 2: ¡LO PERDÍ DE VISTA! ACTIVAR SABUESO
			    MOVE nodoRastro = calcularRutaBFSRastreador(game, GHOST.BLINKY);
			    
			    if (nodoRastro != MOVE.NEUTRAL) {
			        // Si el BFS encontró rastro, le asignamos directamente ese movimiento
			        moves.put(GHOST.BLINKY, nodoRastro);
			    } else {
			        // Si no hay rastro, movimiento por defecto para patrullar o al azar
			        moves.put(GHOST.BLINKY, allMoves[rnd.nextInt(allMoves.length)]);
			    }
			}
			
		}
		//Fantasma 2 = pinky intercetar caminos
		if (game.doesGhostRequireAction(GHOST.PINKY)) {
		    
		    MOVE interceptarPinky = interceptarPaso(game, GHOST.PINKY);
		    
		    // Cambiamos null por GHOST.PINKY para guardar su orden oficialmente
		    mapaDeOrdenes.put(GHOST.PINKY, interceptarPinky);
		}
		
		//Fantasma3 = Inky encargado de interceptar los 4 tuneles
		if (game.doesGhostRequireAction(GHOST.INKY)) {
		    
		    MOVE movimientoInky = vigilarPortales(game, GHOST.INKY);
		    
		    if (movimientoInky != MOVE.NEUTRAL) {
		        mapaDeOrdenes.put(GHOST.INKY, movimientoInky);
		    } else {
		        // Si Pacman no es visible por PO, patrulla de forma segura al azar
		        mapaDeOrdenes.put(GHOST.INKY, allMoves[rnd.nextInt(allMoves.length)]);
		    }
		}
		
		//Fantasma 4 = Sue encargado de vigilar pildora de poder
		
		if (game.doesGhostRequireAction(GHOST.SUE)) {
		    
		    MOVE movimientoSue = vigilarPildora(game, GHOST.SUE);
		    
		    if (movimientoSue != MOVE.NEUTRAL) {
		        mapaDeOrdenes.put(GHOST.SUE, movimientoSue);
		    } else {
		        // Si no hay píldoras activas o Pacman no se ve, patrulla al azar
		        mapaDeOrdenes.put(GHOST.SUE, allMoves[rnd.nextInt(allMoves.length)]);
		    }
		}
		
		return null;
	}
	
	//Funcion BFS para el primer fantasma
	
	private MOVE calcularRutaBFSRastreador(Game game, GHOST fantasma) {
        Queue<Integer> frontera = new LinkedList<>();
        HashSet<Integer> visitados = new HashSet<>();
        
        int nodoInicial = game.getGhostCurrentNodeIndex(fantasma);
        frontera.add(nodoInicial);
        visitados.add(nodoInicial);
        
        int nodoDestinoFinal = -1;
        
        while(!frontera.isEmpty()) {
            int nodoActual = frontera.poll();
            
            // Si el nodo actual cumple la condición de ser el rastro de comida comida
            if (!game.isPillStillAvailable(nodoActual)) { 
                nodoDestinoFinal = nodoActual;
                break; 
            }
            
            // Expandir vecinos
            for (int vecino : game.getNeighbouringNodes(nodoActual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    frontera.add(vecino);
                }
            }
        }
        
        // Si encontramos rastro, convertimos esa posición en una orden física (MOVE)
        if (nodoDestinoFinal != -1) {
            return game.getApproximateNextMoveTowardsTarget(
                nodoDestinoFinal, 
                nodoInicial, 
                game.getGhostLastMoveMade(fantasma), 
                pacman.game.Constants.DM.EUCLID
            );
        }
        
        return MOVE.NEUTRAL; // Dirección por defecto si no encuentra rastro
    }
	
	//Funcion del fantasma 2 para interceptar caminos y ayudar a acorralar a pacman
	private MOVE interceptarPaso(Game game, GHOST fantasma) {
        int nodoPacman = game.getPacmanCurrentNodeIndex();
        int miNodo = game.getGhostCurrentNodeIndex(fantasma);
        
        // Si no vemos a Pacman (PO), no hay datos para proyectar el cruce, devolvemos NEUTRAL
        if (nodoPacman == -1) {
            return MOVE.NEUTRAL;
        }
        
        Queue<Integer> frontera = new LinkedList<>();
        HashSet<Integer> visitados = new HashSet<>();
        
        // El BFS de Pinky arranca desde Pacman para mapear su trayectoria futura
        frontera.add(nodoPacman);
        visitados.add(nodoPacman);
        
        int nodoCruceObjetivo = -1;
        
        while (!frontera.isEmpty()) {
            int nodoActual = frontera.poll();
            
            // CONDICIÓN DE INTERCEPTACIÓN: Encontrar el cruce más cercano en la ruta de Pacman
            if (game.isJunction(nodoActual) && nodoActual != nodoPacman) {
                nodoCruceObjetivo = nodoActual;
                break; // Paramos el cálculo, encontramos el punto estratégico
            }
            
            for (int vecino : game.getNeighbouringNodes(nodoActual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    frontera.add(vecino);
                }
            }
        }
        
        // Si encontramos el cruce del futuro, le ordenamos a Pinky que corra hacia allá
        if (nodoCruceObjetivo != -1) {
            return game.getApproximateNextMoveTowardsTarget(nodoCruceObjetivo, miNodo, game.getGhostLastMoveMade(fantasma), Constants.DM.EUCLID);
        }
        
        return MOVE.NEUTRAL;
    }
	
	private MOVE vigilarPortales(Game game, GHOST fantasma) {
	    int nodoPacman = game.getPacmanCurrentNodeIndex();
	    int miNodo = game.getGhostCurrentNodeIndex(fantasma);
	    
	    // Lista fija de los 4 nodos de los portales en el mapa (ejemplo conceptual de índices)
	    int[] nodosPortales = {0, 10, 20, 30}; // suponemos estos indices (verificar los índices en nuestro  mapa)
	    int umbralPortal = 5; // Distancia límite para activar la alarma
	    
	    // Si no vemos a Pacman por la visibilidad limitada (PO), pasamos directo a vigilar el portal más cercano
	    if (nodoPacman != -1) {
	        
	        // Buscamos si Pacman está cerca de alguno de los 4 portales
	        for (int portal : nodosPortales) {
	            int distanciaPacmanAlPortal = game.getShortestPathDistance(nodoPacman, portal);
	            
	            // COMPUERTA 1: Si Pacman entra al umbral de un portal -> ¡A bloquear el camino!
	            if (distanciaPacmanAlPortal <= umbralPortal) {
	                return game.getApproximateNextMoveTowardsTarget(
	                    portal, 
	                    miNodo, 
	                    game.getGhostLastMoveMade(fantasma), 
	                    Constants.DM.EUCLID
	                );
	            }
	        }
	    }
	    
	    // COMPUERTA 2 (ELSE): Si Pacman está lejos o no se ve, Inky patrulla el portal que le quede más cerca
	    int portalMasCercanoAInky = game.getClosestNodeIndexFromNodeIndex(miNodo, nodosPortales, Constants.DM.EUCLID);
	    
	    return game.getApproximateNextMoveTowardsTarget(
	        portalMasCercanoAInky, 
	        miNodo, 
	        game.getGhostLastMoveMade(fantasma), 
	        Constants.DM.EUCLID
	    );
	}

	
}
	
private MOVE vigilarPildora(Game game, GHOST fantasma) {
    int nodoPacman = game.getPacmanCurrentNodeIndex();
    int miNodo = game.getGhostCurrentNodeIndex(fantasma);
    
    // Obtener los índices de las píldoras de poder que TODAVÍA están activas
    int[] pildorasActivas = game.getActivePowerPillsIndices();
    
    // Si Pacman ya se las comió todas, no hay nada que vigilar, devolvemos NEUTRAL
    if (pildorasActivas.length == 0) {
        return MOVE.NEUTRAL;
    }
    
    int umbralAlerta = 5; // Tu umbral de 5 nodos para la alerta
    
    // COMPUERTA 1: Si vemos a Pacman (PO), revisamos si hay peligro inminente cerca de las píldoras
    if (nodoPacman != -1) {
        for (int pildora : pildorasActivas) {
            int distanciaPacmanAPildora = game.getShortestPathDistance(nodoPacman, pildora);
            int distanciaSueAPildora = game.getShortestPathDistance(miNodo, pildora);
            
            // Si Pacman está a menos de 5 pasos de la píldora Y Sue también está cerca, ¡ALERTA!
            if (distanciaPacmanAPildora <= umbralAlerta && distanciaSueAPildora <= umbralAlerta) {
                // Sue arremete en dirección CONTRARIA a Pacman para evitar ser comido
                return game.getApproximateNextMoveAwayFromTarget(
                    nodoPacman, 
                    miNodo, 
                    game.getGhostLastMoveMade(fantasma), 
                    Constants.DM.EUCLID
                );
            }
        }
    }
    
    // COMPUERTA 2 (ELSE): Si no hay alerta, sigue merodeando/vigilando la píldora activa más cercana
    int pildoraMasCercana = game.getClosestNodeIndexFromNodeIndex(miNodo, pildorasActivas, Constants.DM.EUCLID);
    
    return game.getApproximateNextMoveTowardsTarget(
        pildoraMasCercana, 
        miNodo, 
        game.getGhostLastMoveMade(fantasma), 
        Constants.DM.EUCLID
    );
}


}