package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class CraftingSettings {

    @Comment(@CommentValue("Включить рецепт в верстаке"))
    public boolean enabled = false;

    @Comment(@CommentValue("Ключ рецепта (NamespacedKey)"))
    public String recipeKey = "soulblast_example";

    @Comment(@CommentValue("Форма: до 3 строк по 3 символа (материал или #)"))
    public List<String> shape = new ArrayList<>(List.of(
            "TST",
            "SGS",
            "TST"
    ));

    @Comment(@CommentValue("Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)"))
    public List<String> ingredients = new ArrayList<>(List.of(
            "T=TNT",
            "S=SAND",
            "G=GUNPOWDER"
    ));

    @Comment(@CommentValue("Количество в результате"))
    public int resultAmount = 1;

}
