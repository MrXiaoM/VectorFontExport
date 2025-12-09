package top.mrxiaom.vector.font.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ExportFontScreen extends Screen {
    public ExportFontScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        // TODO: 实现导出逻辑
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    public static ButtonWidget getButton(MinecraftClient client) {
        Text text = Text.of("导出字体");
        ButtonWidget.PressAction onPress = (button) -> client.setScreen(new ExportFontScreen());
        return ButtonWidget.builder(text, onPress).dimensions(0, 30, 98, 20).build();
    }
}
