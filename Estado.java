/*
Autor Original: Eduardo Hurtado
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Estructura de datos COMPARTIDA: un estado del puzzle 3x3.
 * fichas[i] = ficha en la casilla i (0..8, recorrido por filas). 0 = hueco.
 * Inmutable: todos los agentes pueden leerlo sin riesgo.
 */
public class Estado {

    public static final int N = 3;
    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    public final int[] fichas;

    public Estado(int[] fichas) {
        this.fichas = fichas.clone();
    }

    public static Estado meta() {
        return new Estado(META);
    }

    public int posVacio() {
        for (int i = 0; i < fichas.length; i++) {
            if (fichas[i] == 0) return i;
        }
        return -1;
    }

    public boolean esMeta() {
        return Arrays.equals(fichas, META);
    }

    /** Aristas del grafo: estados alcanzables moviendo el hueco una casilla. */
    public List<Estado> sucesores() {
        List<Estado> res = new ArrayList<>();
        int v = posVacio();
        int f = v / N, c = v % N;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nf = f + d[0], nc = c + d[1];
            if (nf < 0 || nf >= N || nc < 0 || nc >= N) continue;
            int p = nf * N + nc;
            int[] n = fichas.clone();
            n[v] = n[p];
            n[p] = 0;
            res.add(new Estado(n));
        }
        return res;
    }

    /** En un 3x3 es resoluble si el número de inversiones es par. */
    public boolean esResoluble() {
        int inv = 0;
        for (int i = 0; i < fichas.length; i++) {
            for (int j = i + 1; j < fichas.length; j++) {
                if (fichas[i] != 0 && fichas[j] != 0 && fichas[i] > fichas[j]) inv++;
            }
        }
        return inv % 2 == 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Estado && Arrays.equals(fichas, ((Estado) o).fichas);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fichas);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fichas.length; i++) {
            sb.append(fichas[i] == 0 ? "_" : String.valueOf(fichas[i]));
            sb.append(i % N == N - 1 ? (i == fichas.length - 1 ? "" : "\n") : " ");
        }
        return sb.toString();
    }
}