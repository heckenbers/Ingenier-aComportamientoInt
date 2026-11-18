package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman;

/**
 * Lo que Ms. Pac-Man recuerda de un tick a otro, para dos entradas del FCL:
 * cuántos ticks seguidos lleva esperando junto al cebo (espera) y cuántos lleva sin comerse
 * ninguna píldora (hambre, desde la v8). Una por controlador; nada estático.
 */
public class MemoriaPacMan {

    private int ultimoTickCebo;
    private int ticksEsperando;
    private int ultimaComida;       // tick en que se comió la última píldora (o empezó la vida)
    private int pildorasAntes;      // -1: todavía no se ha visto ningún tick

    public MemoriaPacMan() {
        reiniciar();
    }

    public void reiniciar() {
        ultimoTickCebo = -10;
        ticksEsperando = 0;
        pildorasAntes = -1;
    }

    /** Al principio de cada tick, lo llama el Contexto: si han bajado las píldoras, acaba de comer. */
    public void registrarTurno(int tick, int pildorasActivas) {
        if (pildorasAntes < 0 || pildorasActivas != pildorasAntes)
            ultimaComida = tick;
        pildorasAntes = pildorasActivas;
    }

    /** Ticks sin comer ninguna píldora (contando desde el principio de la vida o del nivel). */
    public int getTicksSinComer(int tick) {
        return tick - ultimaComida;
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
