/*
Autores: Eduardo Hurtado e Isaac Alvarez
 */
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public abstract class AgenteJugador extends Agent {

    protected Estado estadoActual = null;

    /*
    --------> METODOS NECESARIOS PARA LA HEURISTICA
     */
    protected abstract int heuristica(Estado e);//Heuristica
    protected abstract String nombreHeuristica();//Nombre del jugador

    /*
    -------->Configuracion Inicial, Ciclo de vida
     */
    @Override
    protected void setup() {
        System.out.println("Hola Mi Nombre Es " + getLocalName() + "[" + nombreHeuristica() + "]\n");

        //I. DEMO DE ARRANQUE
        final Estado inicio = definirEstadoInicial();
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("Estado inicial:\n" + inicio + "\n");
                if (!inicio.esResoluble()) {
                    System.out.println("Este estado no tiene solución.");
                    return;
                }
                System.out.println("h(inicio) = " + heuristica(inicio) + "\n");
                mostrarSolucion(resolver(inicio));
            }
        });

        //II. COMUNICACION ENTRE AGENTES
        addBehaviour(new CyclicBehaviour() {
            private final MessageTemplate plantilla = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

            @Override
            public void action() {
                ACLMessage mensaje = receive(plantilla);
                if (mensaje == null) {
                    block();
                    return;
                }

                String cont = mensaje.getContent();
                ACLMessage resp = mensaje.createReply();

                if (cont != null && cont.startsWith("RESOLVER:")) {
                    Estado e = parsear(cont.substring("RESOLVER:".length()));
                    if (e == null) {
                        resp.setPerformative(ACLMessage.FAILURE);
                        resp.setContent("ESTADO_INVALIDO");
                    } else if (!e.esResoluble()) {
                        resp.setPerformative(ACLMessage.FAILURE);
                        resp.setContent("ESTADO_NO_RESOLUBLE");
                    } else {
                        estadoActual = e;
                        Solucion s = resolver(estadoActual);
                        mostrarSolucion(s);
                        resp.setPerformative(ACLMessage.INFORM);
                        resp.setContent(codificarSolucion(s));
                    }
                } else {
                    resp.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    resp.setContent("COMANDO_DESCONOCIDO");
                }

                send(resp);
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.printf("Agente jugador [%s] finalizando.%n", getLocalName());
    }


    /*
     -Define el estado inicial o pone un ejemplo
     ej. h2:JugadorH2(813402765)
      */
    protected Estado definirEstadoInicial() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            Estado e = parsear(String.valueOf(args[0]));
            if (e != null) return e;
        }
        return new Estado(new int[]{8, 1, 3, 4, 0, 2, 7, 6, 5});
    }


    /*
    -> "813402765" a un estado de 9 digitos (0-8) si es valido
     */
    public static Estado parsear(String texto) {
        String d = texto.replaceAll("[^0-9]", "");
        if (d.length() != 9) return null;
        int[] fichas = new int[9];
        boolean[] vistos = new boolean[9];
        for (int i = 0; i < 9; i++) {
            int v = d.charAt(i) - '0';
            if (v > 8 || vistos[v]) return null;
            vistos[v] = true;
            fichas[i] = v;
        }
        return new Estado(fichas);
    }


    /*
    -> Solucion a Texto de INFORM
     */
    public static String codificarSolucion(Solucion s) {
        return String.format(Locale.US, "PASOS:%d;EXPANDIDOS:%d;GENERADOS:%d;TIEMPO_MS:%.2f;CAMINO:%s",
                s.pasos(), s.nodosExpandidos, s.generados, s.tiempoMs, codificarCamino(s.camino));
    }

    /*
    -> Estado de 9 digitos (0-8) a "813402765"
    */
    public static String codificarEstado(Estado e) {
        StringBuilder sb = new StringBuilder();
        for (int f : e.fichas) sb.append(f);
        return sb.toString();
    }

    /*
    -> Genera un camino "813402765,813042765,..."
    */
    public static String codificarCamino(List<Estado> camino) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(codificarEstado(camino.get(i)));
        }
        return sb.toString();
    }
    /*
    -> "813402765,813042765,..." a una Lista de estados
    */
    public static List<Estado> decodificarCamino(String texto) {
        List<Estado> camino = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return camino;
        for (String parte : texto.split(",")) {
            Estado e = parsear(parte);
            if (e != null) camino.add(e);
        }
        return camino;
    }

    public void notificarAvance(Estado estadoActual) {
        ACLMessage msgProgreso = new ACLMessage(ACLMessage.INFORM);
        msgProgreso.addReceiver(new jade.core.AID("AgenteGUI", jade.core.AID.ISLOCALNAME));
        msgProgreso.setContent(String.format("PROGRESO:%s:%s",
                getLocalName(), codificarEstado(estadoActual)));
        send(msgProgreso);
        //System.out.println(codificarEstado(estadoActual));
    }

    /*
    -> Metodo A estrella para todos los agentes
    */
    public void mostrarSolucion(Solucion s) {
        System.out.println("=== " + nombreHeuristica() + " ===");
        if (!s.resuelto()) {
            System.out.println("Sin solución");
            return;
        }
        System.out.printf(Locale.US, "Pasos: %d | Expandidos: %d | Generados: %d | Tiempo: %.2f ms%n",
                s.pasos(), s.nodosExpandidos, s.generados, s.tiempoMs);
        System.out.println();

        final int POR_FILA = 6;
        for (int ini = 0; ini < s.camino.size(); ini += POR_FILA) {
            int fin = Math.min(ini + POR_FILA, s.camino.size());
            StringBuilder titulo = new StringBuilder();
            StringBuilder[] filas = {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            for (int i = ini; i < fin; i++) {
                titulo.append(String.format("%-10s", "Paso " + i));
                Estado e = s.camino.get(i);
                for (int r = 0; r < 3; r++) {
                    String linea = "";
                    for (int c = 0; c < 3; c++) {
                        int f = e.fichas[r * 3 + c];
                        linea += (f == 0 ? "_" : String.valueOf(f)) + (c < 2 ? " " : "");
                    }
                    filas[r].append(String.format("%-10s", linea));
                }
            }
            System.out.println(titulo);
            for (StringBuilder fila : filas) System.out.println(fila);
            System.out.println();
        }
    }

    /*
    -> Presentacion de los valores
    */
    public Solucion resolver(Estado inicio) {
        long tiempo = System.nanoTime();
        PriorityQueue<Nodo> nodosAbiertos = new PriorityQueue<>(Comparator.comparingInt(a -> a.h));
        Map<Estado, Integer> nodosExpandidos = new HashMap<>();

        nodosAbiertos.add(new Nodo(inicio, null, heuristica(inicio)));
        nodosExpandidos.put(inicio, heuristica(inicio));

        int expandidos = 0, generados = 1;

        while (!nodosAbiertos.isEmpty()) {
            Nodo n = nodosAbiertos.poll();

            if (n.h > nodosExpandidos.get(n.estado)) continue;

            expandidos++;
            /*
            NOTIFICA A LA GUI EL ESTADO ACTUAL.
             */
            notificarAvance(n.estado);

            if (n.estado.esMeta()) {
                List<Estado> camino = new ArrayList<>();
                for (Nodo x = n; x != null; x = x.padre) camino.add(x.estado);
                Collections.reverse(camino);
                return new Solucion(camino, expandidos, generados, (System.nanoTime() - tiempo) / 1e6);
            }

            for (Estado s : n.estado.sucesores()) {
                int hSucesor = heuristica(s);
                Integer previoH = nodosExpandidos.get(s);

                if (previoH == null || hSucesor < previoH) {
                    nodosExpandidos.put(s, hSucesor);
                    nodosAbiertos.add(new Nodo(s, n, hSucesor));
                    generados++;
                }
            }
        }
        return new Solucion(new ArrayList<>(), expandidos, generados, (System.nanoTime() - tiempo) / 1e6);
    }

    /*
    -> Clase Nodo
    */
    public class Nodo implements Comparable<Nodo> {
        Estado estado;
        Nodo padre;
        int h;

        Nodo(Estado estado, Nodo padre, int h) {
            this.estado = estado;
            this.padre = padre;
            this.h = h;
        }

        @Override
        public int compareTo(Nodo o) {
            return Integer.compare(this.h, o.h);
        }
    }

    /*
    -> Clase Solucion
    */
    public static class Solucion {
        public final List<Estado> camino;
        public final int nodosExpandidos;
        public final int generados;
        public final double tiempoMs;

        Solucion(List<Estado> camino, int nodosExpandidos, int generados, double tiempoMs) {
            this.camino = camino;
            this.nodosExpandidos = nodosExpandidos;
            this.generados = generados;
            this.tiempoMs = tiempoMs;
        }

        public boolean resuelto() {
            return !camino.isEmpty();
        }

        public int pasos() {
            return camino.size() - 1;
        }
    }
}