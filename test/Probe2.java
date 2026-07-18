import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class Probe2 {
    public static void main(String[] args) throws Exception {
        ClassLoader cl = Probe2.class.getClassLoader();
        Class<?> psClass = Class.forName("dev.espi.protectionstones.ProtectionStones", false, cl);
        Class<?> regionClass = Class.forName("dev.espi.protectionstones.PSRegion", false, cl);
        Class<?> protectBlockClass = Class.forName("dev.espi.protectionstones.PSProtectBlock", false, cl);
        Class<?> blockUtilClass = Class.forName("dev.espi.protectionstones.utils.BlockUtil", false, cl);
        Class<?> locationClass = Class.forName("org.bukkit.Location", false, cl);
        Class<?> blockClass = Class.forName("org.bukkit.block.Block", false, cl);
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        check("PSRegion.fromLocation", () -> lookup.findStatic(regionClass, "fromLocation", MethodType.methodType(regionClass, locationClass)));
        check("PSRegion.fromLocationGroup", () -> lookup.findStatic(regionClass, "fromLocationGroup", MethodType.methodType(regionClass, locationClass)));
        check("PSRegion.getType", () -> lookup.findVirtual(regionClass, "getType", MethodType.methodType(String.class)));
        check("PSRegion.getTypeOptions", () -> lookup.findVirtual(regionClass, "getTypeOptions", MethodType.methodType(protectBlockClass)));
        check("PSRegion.getOwners", () -> lookup.findVirtual(regionClass, "getOwners", MethodType.methodType(ArrayList.class)));
        check("PSRegion.getProtectBlock", () -> lookup.findVirtual(regionClass, "getProtectBlock", MethodType.methodType(blockClass)));
        check("PSRegion.deleteRegion", () -> lookup.findVirtual(regionClass, "deleteRegion", MethodType.methodType(boolean.class, boolean.class)));
        check("ProtectionStones.getInstance", () -> lookup.findStatic(psClass, "getInstance", MethodType.methodType(psClass)));
        check("ProtectionStones.getConfiguredBlocks", () -> lookup.findVirtual(psClass, "getConfiguredBlocks", MethodType.methodType(List.class)));
        check("ProtectionStones.isProtectBlock", () -> lookup.findStatic(psClass, "isProtectBlock", MethodType.methodType(boolean.class, blockClass)));
        check("BlockUtil.getProtectBlockType", () -> lookup.findStatic(blockUtilClass, "getProtectBlockType", MethodType.methodType(String.class, blockClass)));
        check("PSProtectBlock field alias", () -> protectBlockClass.getField("alias"));
        check("PSProtectBlock field xRadius", () -> protectBlockClass.getField("xRadius"));
    }

    interface ThrowingSupplier {
        Object get() throws Throwable;
    }

    static void check(String name, ThrowingSupplier action) {
        try {
            action.get();
            System.out.println(name + " OK");
        } catch (Throwable t) {
            System.out.println(name + " FAIL: " + t);
        }
    }
}
