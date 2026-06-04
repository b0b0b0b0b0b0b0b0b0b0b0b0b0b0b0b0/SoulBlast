package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;

import java.util.List;

public final class PsPlaceholderResolver {

    private PsPlaceholderResolver() {
    }

    public static String joinLines(List<String> lines, PsBlockState state) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                builder.append('\n');
            }
            builder.append(apply(lines.get(index), state));
        }
        return builder.toString();
    }

    public static String apply(String template, PsBlockState state) {
        if (template == null) {
            return "";
        }
        return template
                .replace("%owner_name%", state.ownerName())
                .replace("%owner%", state.ownerName())
                .replace("%owner_prefix%", state.ownerPrefix())
                .replace("%owner_suffix%", state.ownerSuffix())
                .replace("%durability%", Integer.toString(Math.max(0, state.durability())))
                .replace("%durability_maximum%", Integer.toString(Math.max(1, state.maximum())))
                .replace("%radius_x%", Integer.toString(state.radiusX()))
                .replace("%radius_y%", Integer.toString(state.radiusY()))
                .replace("%radius_z%", Integer.toString(state.radiusZ()));
    }

}
