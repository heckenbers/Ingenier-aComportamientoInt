package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman;

/**
 * Lo único que Ms. Pac-Man recuerda de un tick a otro: cuántos ticks seguidos lleva
 * esperando junto al cebo. Es una entrada del FCL (espera), así que se calcula en Java.
 * Una por controlador; nada estático.
 */
public class MemoriaPacMan {

    private int ultimoTickCebo;
    private int ticksEsperando;

    public MemoriaPacMan() {
        reiniciar();
    }

    public void reiniciar() {
        ultimoTickCebo = -10;
        ticksEsperando = 0;
    }

    /** Lo llama EsperarCeboAction cada tick que se ejecuta. */
    public void registrarEspera(int tick) {
        ticksEsperando = (tick == ultimoTickCebo + 1) ? ticksEsperando + 1 : 1;
        ultimoTickCebo = tick;
    }

    /** Ticks seguidos esperando junto al cebo hasta el tick anterior (0 si ayer no esperaba). */
    public int getTicksEsperando(int tick) {
        return (tick <= ultimoTickCebo + 1) ? ticksEsperando : 0;
    }
}
