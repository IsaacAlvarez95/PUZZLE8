import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class AgenteManhattanConflictoLineal extends AgenteJugador {

    @Override
    protected int heuristica (Estado e){
        return Heuristicas.manhattanConflictoLineal(e);
    }

    @Override
    protected String nombreHeuristica(){
        return "Jugador ManConflicto";
    }

    public static void main(String[] args) throws Exception{
        Runtime rt = Runtime.instance();
        Profile perfil = new ProfileImpl();
        AgentContainer contenedor = rt.createMainContainer(perfil);
        Object[] argumentos = args.length > 0 ? new Object[]{args[0]} : null;

        contenedor.createNewAgent("h3", "AgenteManhattanConflictoLineal", argumentos).start();
    }

}
