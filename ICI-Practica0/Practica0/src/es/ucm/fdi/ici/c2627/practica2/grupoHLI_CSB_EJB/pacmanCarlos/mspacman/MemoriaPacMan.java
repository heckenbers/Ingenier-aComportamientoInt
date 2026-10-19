package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

/**
 * Lo que la HFSM tiene que recordar entre ticks (el Input se crea de nuevo
 * en cada tick): qué superestado estaba activo, para la historia superficial,
 * y cuánto lleva esperando en el cebo.
 */
public class MemoriaPacMan {

    private String superestadoActivo;

    private int ultimoTickCebo;
    private int ticksEsperando;
    private int ticksSinValor;
    private int ultimoAbandonoCebo;

    public MemoriaPacMan() {
        reiniciar();
    }

    public void reiniciar() {
        superestadoActivo = null;
        ultimoTickCebo = -10;
        ticksEsperando = 0;
        ticksSinValor = 0;
        ultimoAbandonoCebo = Integer.MIN_VALUE / 2;
    }

    // ---------- historia ----------

    public String getSuperestadoActivo() { return superestadoActivo; }

    public void setSuperestadoActivo(String id) { this.superestadoActivo = id; }

    // ---------- cebo ----------

    /** Lo llama PrepararCeboAction en cada tick que está activo. */
    public void registrarCebo(int tick, int valorCadena) {
        if (tick != ultimoTickCebo + 1) {       // se acaba de entrar en el cebo
            ticksEsperando = 0;
            ticksSinValor = 0;
        }
        ticksEsperando++;
        ticksSinValor = (valorCadena < Parametros.VALOR_MIN_CEBO) ? ticksSinValor + 1 : 0;
        ultimoTickCebo = tick;
    }

    public int getTicksEsperando(int tick) {
        return (tick <= ultimoTickCebo + 1) ? ticksEsperando : 0;
    }

    public int getTicksSinValor(int tick) {
        return (tick <= ultimoTickCebo + 1) ? ticksSinValor : 0;
    }

    public void abandonarCebo(int tick) {
        ultimoAbandonoCebo = tick;
        ultimoTickCebo = -10;
    }

    public boolean ceboEnEnfriamiento(int tick) {
        return tick - ultimoAbandonoCebo < Parametros.ENFRIAMIENTO_CEBO;
    }

    @Override
    public String toString() {
        return "superestado=" + superestadoActivo + ", ticks en cebo=" + ticksEsperando;
    }
}
