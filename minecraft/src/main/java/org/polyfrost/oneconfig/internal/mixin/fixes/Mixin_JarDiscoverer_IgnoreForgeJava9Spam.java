/*
 * This file is part of OneConfig.
 * OneConfig - Next Generation Config Library for Minecraft: Java Edition
 * Copyright (C) 2021~2024 Polyfrost.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
 * either version 1.0 of the Additional Terms, or (at your option) any later
 * version.
 *
 *   This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>. You should
 * have also received a copy of the Additional Terms Applicable
 * to OneConfig, as published by Polyfrost. If not, see
 * <https://polyfrost.org/legal/oneconfig/additional-terms>
 */

//#if FORGE && MC <= 1.12.2
package org.polyfrost.oneconfig.internal.mixin.fixes;

//#if MC >= 1.12.2
//$$ import org.apache.logging.log4j.Logger;
//#else
import org.apache.logging.log4j.Level;
//#endif

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = JarDiscoverer.class, remap = false)
public class Mixin_JarDiscoverer_IgnoreForgeJava9Spam {

    @WrapWithCondition(
            //#if MC > 1.8.9
            //$$ method = "findClassesASM",
            //#else
            method = "discover",
            //#endif
            at = @At(
                    value = "INVOKE",
                    //#if MC > 1.8.9
                    //$$ target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"
                    //#else
                    target = "Lnet/minecraftforge/fml/common/FMLLog;log(Lorg/apache/logging/log4j/Level;Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V"
                    //#endif
            )
    )
    private boolean ignoreException(
            //#if MC >= 1.12.2
            //$$ Logger instance,
            //$$ String s,
            //$$ Object o1,
            //$$ Object o2,
            //$$ Object ex
            //#else
            Level level,
            Throwable ex,
            String format,
            Object[] data
            //#endif
    ) {
        if (ex instanceof LoaderException) {
            LoaderException loaderException = (LoaderException) ex;
            return !(loaderException.getCause() instanceof IllegalArgumentException);
        }

        return true;
    }

}
//#endif