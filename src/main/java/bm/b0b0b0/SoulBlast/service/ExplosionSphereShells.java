package bm.b0b0b0.SoulBlast.service;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public final class ExplosionSphereShells {

    private ExplosionSphereShells() {
    }

    public static List<ShellCell> collectShell(
            World world,
            int cx,
            int cy,
            int cz,
            int shell,
            double radiusSq,
            boolean onlyLoaded,
            IntPredicate includeY
    ) {
        double innerSq = shell == 0 ? -1.0 : (double) (shell - 1) * (shell - 1);
        double outerSq = Math.min((double) shell * shell, radiusSq);
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        List<ShellCell> cells = new ArrayList<>();
        for (int dx = -shell; dx <= shell; dx++) {
            for (int dy = -shell; dy <= shell; dy++) {
                for (int dz = -shell; dz <= shell; dz++) {
                    double distSq = (double) dx * dx + dy * dy + dz * dz;
                    if (distSq > outerSq || distSq <= innerSq) {
                        continue;
                    }
                    int x = cx + dx;
                    int y = cy + dy;
                    int z = cz + dz;
                    if (y < minY || y > maxY) {
                        continue;
                    }
                    if (!includeY.test(y)) {
                        continue;
                    }
                    if (onlyLoaded && !world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    cells.add(new ShellCell(x, y, z));
                }
            }
        }
        return cells;
    }

    public record ShellCell(int x, int y, int z) {
    }

}
