package bm.b0b0b0.SoulBlast.service.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public final class VaultEconomyBridge {

    private final JavaPlugin plugin;
    private final boolean enabledInConfig;
    private Object economy;
    private Method getBalance;
    private Method withdrawMethod;
    private Method formatMethod;
    private boolean active;

    public VaultEconomyBridge(JavaPlugin plugin, boolean enabledInConfig) {
        this.plugin = plugin;
        this.enabledInConfig = enabledInConfig;
    }

    public void reload() {
        economy = null;
        getBalance = null;
        withdrawMethod = null;
        formatMethod = null;
        active = false;
        if (!enabledInConfig) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(economyClass);
            if (provider == null) {
                return;
            }
            economy = provider.getProvider();
            getBalance = economyClass.getMethod("getBalance", OfflinePlayer.class);
            withdrawMethod = economyClass.getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            formatMethod = economyClass.getMethod("format", double.class);
            active = economy != null;
        } catch (Throwable failure) {
            plugin.getLogger().log(Level.WARNING, "Vault economy unavailable", failure);
        }
    }

    public boolean isActive() {
        return active;
    }

    public double balance(OfflinePlayer player) {
        if (!active) {
            return Double.MAX_VALUE;
        }
        try {
            return ((Number) getBalance.invoke(economy, player)).doubleValue();
        } catch (ReflectiveOperationException exception) {
            return 0;
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!active || amount <= 0) {
            return true;
        }
        try {
            Object response = withdrawMethod.invoke(economy, player, amount);
            Method success = response.getClass().getMethod("transactionSuccess");
            return (boolean) success.invoke(response);
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    public String format(double amount) {
        if (!active) {
            return String.format("%.2f", amount);
        }
        try {
            return (String) formatMethod.invoke(economy, amount);
        } catch (ReflectiveOperationException exception) {
            return String.format("%.2f", amount);
        }
    }

    public double effectiveCost(double configuredCost) {
        if (configuredCost <= 0) {
            return 0;
        }
        if (!active) {
            return 0;
        }
        return configuredCost;
    }

}
