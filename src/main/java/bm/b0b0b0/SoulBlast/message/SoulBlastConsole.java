package bm.b0b0b0.SoulBlast.message;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public final class SoulBlastConsole {

    public static final String PREFIX = "\u001B[37m[\u001B[90mSoulBlast\u001B[37m]\u001B[0m ";

    private final ConsoleCommandSender console;

    public SoulBlastConsole() {
        this.console = Bukkit.getConsoleSender();
    }

    public void blank() {
        console.sendMessage(" ");
    }

    public void line(String message) {
        console.sendMessage(PREFIX + message);
    }

    public void info(String message) {
        line(message);
    }

    public void detail(String message) {
        line("\u001B[90m" + message + "\u001B[0m");
    }

    public void ok(String message) {
        line("\u001B[32m" + message + "\u001B[0m");
    }

    public void warn(String message) {
        line("\u001B[33m" + message + "\u001B[0m");
    }

    public void error(String message) {
        line("\u001B[31m" + message + "\u001B[0m");
    }

    public void separator() {
        line("==============================");
    }

}
