package bm.b0b0b0.SoulBlast.gui.menu;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.util.TextUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MenuDynamiteSorter {

    private static final Comparator<DynamiteDefinition> BY_ID =
            Comparator.comparing((DynamiteDefinition d) -> d.id);

    public List<DynamiteDefinition> sort(List<DynamiteDefinition> source, MenuSortingType sorting) {
        List<DynamiteDefinition> copy = new ArrayList<>(source);
        copy.sort(comparatorFor(sorting));
        return copy;
    }

    private Comparator<DynamiteDefinition> comparatorFor(MenuSortingType sorting) {
        if (sorting == null) {
            return byPowerDescending();
        }
        return switch (sorting) {
            case LOWEST_POWER -> byPowerAscending();
            case SHORTEST_FUSE -> byFuseAscending();
            case LONGEST_FUSE -> byFuseDescending();
            case NAME_AZ -> byNameAscending();
            case NAME_ZA -> byNameDescending();
            default -> byPowerDescending();
        };
    }

    private Comparator<DynamiteDefinition> byPowerDescending() {
        return Comparator
                .comparingDouble((DynamiteDefinition d) -> d.explosion.power)
                .reversed()
                .thenComparing(BY_ID);
    }

    private Comparator<DynamiteDefinition> byPowerAscending() {
        return Comparator
                .comparingDouble((DynamiteDefinition d) -> d.explosion.power)
                .thenComparing(BY_ID);
    }

    private Comparator<DynamiteDefinition> byFuseAscending() {
        return Comparator
                .comparingInt((DynamiteDefinition d) -> d.fuseTicks)
                .thenComparing(BY_ID);
    }

    private Comparator<DynamiteDefinition> byFuseDescending() {
        return Comparator
                .comparingInt((DynamiteDefinition d) -> d.fuseTicks)
                .reversed()
                .thenComparing(BY_ID);
    }

    private Comparator<DynamiteDefinition> byNameAscending() {
        return Comparator.comparing(
                (DynamiteDefinition d) -> displayPlain(d).toLowerCase(Locale.ROOT)
        );
    }

    private Comparator<DynamiteDefinition> byNameDescending() {
        return byNameAscending().reversed();
    }

    private String displayPlain(DynamiteDefinition definition) {
        return TextUtil.colorize(definition.item.displayName).replaceAll("&[0-9a-fk-or]", "");
    }

}
