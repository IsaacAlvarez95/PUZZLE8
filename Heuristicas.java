/*
Autor Original: Eduardo Hurtado
 */
/** Las tres heurísticas (todas admisibles). Cada JugadorHx usa una. */
public final class Heuristicas {

    private Heuristicas() {}

    /** H1: número de fichas que no están en su casilla objetivo (sin contar el hueco). */
    public static int fichasMalColocadas(Estado e) {
        int c = 0;
        for (int i = 0; i < e.fichas.length; i++) {
            if (e.fichas[i] != 0 && e.fichas[i] != Estado.META[i]) c++;
        }
        return c;
    }

    /** H2: suma de distancias de Manhattan de cada ficha a su casilla objetivo. */
    public static int manhattan(Estado e) {
        int suma = 0;
        for (int i = 0; i < e.fichas.length; i++) {
            int t = e.fichas[i];
            if (t == 0) continue;
            int g = t - 1; // la ficha t va en la casilla t-1
            suma += Math.abs(i / 3 - g / 3) + Math.abs(i % 3 - g % 3);
        }
        return suma;
    }

    /** H3: Manhattan + 2 por cada conflicto lineal (fichas en su fila/columna pero invertidas). */
    public static int manhattanConflictoLineal(Estado e) {
        int total = manhattan(e);
        for (int r = 0; r < 3; r++) {
            int[] objetivo = new int[3];
            for (int c = 0; c < 3; c++) {
                int t = e.fichas[r * 3 + c];
                objetivo[c] = (t != 0 && (t - 1) / 3 == r) ? (t - 1) % 3 : -1;
            }
            total += 2 * conflictosEnLinea(objetivo);
        }
        for (int c = 0; c < 3; c++) {
            int[] objetivo = new int[3];
            for (int r = 0; r < 3; r++) {
                int t = e.fichas[r * 3 + c];
                objetivo[r] = (t != 0 && (t - 1) % 3 == c) ? (t - 1) / 3 : -1;
            }
            total += 2 * conflictosEnLinea(objetivo);
        }
        return total;
    }

    /** Fichas mínimas a sacar de la línea para eliminar todos los conflictos. */
    private static int conflictosEnLinea(int[] obj) {
        boolean[] quitada = new boolean[3];
        int total = 0;
        while (true) {
            int mejor = -1, max = 0;
            for (int i = 0; i < 3; i++) {
                if (quitada[i] || obj[i] < 0) continue;
                int c = 0;
                for (int j = 0; j < 3; j++) {
                    if (j == i || quitada[j] || obj[j] < 0) continue;
                    if ((i < j && obj[i] > obj[j]) || (i > j && obj[i] < obj[j])) c++;
                }
                if (c > max) {
                    max = c;
                    mejor = i;
                }
            }
            if (mejor < 0) break;
            quitada[mejor] = true;
            total++;
        }
        return total;
    }
}