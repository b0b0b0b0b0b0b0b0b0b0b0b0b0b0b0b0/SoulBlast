package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class EconomySettings {

    @Comment(@CommentValue("Использовать Vault, если плагин установлен"))
    public boolean useVaultIfPresent = true;

    @Comment(@CommentValue("Символ валюты в лore меню"))
    public String currencySymbol = "⛃";

}
