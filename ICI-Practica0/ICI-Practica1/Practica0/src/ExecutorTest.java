import es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB.MsPacManHLI_EJB_CSB;
import es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB.GhostsHLI_EJB_CSB;
import es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB.GhostsElkin;

import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.PacmanController;
import pacman.game.util.Stats;

public class ExecutorTest {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setGhostPO(false)
                .setPacmanPO(false)
                .setGhostsMessage(false)
                .setVisual(true)
                .setScaleFactor(2.5)
                .build();

        //PacmanController pacMan = new MsPacManRandom();
        PacmanController pacMan = new MsPacManHLI_EJB_CSB();
        GhostController ghosts = new GhostsHLI_EJB_CSB();
        Stats[] s = executor.runExperiment(pacMan, ghosts, 30, "PacMan con Mapa de Influencia"); 
        System.out.println( 
            s[0]//last parameter defines speed
        	//executor.runGame(pacMan, ghosts, 10)
        );     
    }
	
}
