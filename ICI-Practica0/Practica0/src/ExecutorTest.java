import es.ucm.fdi.ici.c2627.practica0.grupoIndividual.Ghosts;

import es.ucm.fdi.ici.c2627.practica0.grupoIndividual.MsPacMan;

import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.PacmanController;

public class ExecutorTest {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(true)
                .setScaleFactor(2.5)
                .build();

        //PacmanController pacMan = new MsPacManRandom();
        PacmanController pacMan = new MsPacMan();
        GhostController ghosts = new Ghosts();
        
        System.out.println( 
            executor.runGame(pacMan, ghosts, 30) //last parameter defines speed
        );     
    }
	
}
