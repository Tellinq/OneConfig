package org.polyfrost.oneconfig.internal.mixin.command;

//#if MC >= 1.16
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import net.minecraft.commands.synchronization.ArgumentTypes;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$
//$$ import java.util.Map;
//$$
//$$ @Mixin(ArgumentTypes.class)
//$$ public interface Mixin_ModernArgumentTypesAccessor {
//$$     @Accessor("BY_CLASS")
//$$     public static Map<Class<?>, Object> getArgumentTypes() {
//$$        throw new AssertionError();
//$$     }
//$$ }
//#endif