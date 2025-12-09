package top.mrxiaom.vector.font.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.mrxiaom.vector.font.gui.ExportFontScreen;

@Mixin(TitleScreen.class)
public class MixinMainScreen extends Screen {
    protected MixinMainScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addButtons(CallbackInfo ci) {
        addDrawableChild(ExportFontScreen.getButton(client));
    }
}
