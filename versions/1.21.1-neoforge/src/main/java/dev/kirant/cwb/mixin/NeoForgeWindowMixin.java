package dev.kirant.cwb.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.kirant.cwb.CWB;
import dev.kirant.cwb.util.MinecraftWindow;
import dev.kirant.cwb.util.OS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
abstract class NeoForgeWindowMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;refreshFramebufferSize()V"))
    private void configureFramebuffer(CallbackInfo ci) {
        if (OS.isMacOS()) {
            MinecraftWindow.MacOS.setUseScaledFramebuffer((Window)(Object)this, CWB.CONFIG.getUseScaledFramebuffer());
        }
    }
}
