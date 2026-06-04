package bm.b0b0b0.SoulBlast.service.economy;

import org.bukkit.entity.Player;

public final class ExperienceCostService {

    public boolean canAfford(Player player, int cost) {
        if (cost <= 0) {
            return true;
        }
        return totalExperience(player) >= cost;
    }

    public boolean withdraw(Player player, int cost) {
        if (cost <= 0) {
            return true;
        }
        int total = totalExperience(player);
        if (total < cost) {
            return false;
        }
        player.setTotalExperience(total - cost);
        return true;
    }

    public int totalExperience(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        return levelExperience(level) + Math.round(progress * experienceForLevel(level));
    }

    private int levelExperience(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    private int experienceForLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if (level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

}
