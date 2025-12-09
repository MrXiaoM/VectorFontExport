package top.mrxiaom.vector.font.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import top.mrxiaom.vector.font.VectorFontExport;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportFontScreen extends Screen {

    private final Screen parent;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private boolean executing = false;

    public ExportFontScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        MinecraftClient client = this.client;
        if (client == null) return;

        Text exportText = Text.of("导出字体");
        Text exportTextExecuting = Text.of("正在导出字体…");
        ButtonWidget.PressAction onExportPress = (btn) -> {
            executing = true;
            btn.setMessage(exportTextExecuting);
            btn.active = false;
            executor.execute(() -> {
                try {
                    FontManager manager = client.fontManager;
                    NbtList fonts = new NbtList();
                    JsonArray fontsReversed = new JsonArray();
                    for (Map.Entry<Identifier, FontStorage> entry : manager.fontStorages.entrySet()) {
                        Identifier fontKey = entry.getKey();
                        String key = fontKey.toString();
                        // 引用字体不进行扫描
                        if (fontKey.getNamespace().equals("minecraft") && fontKey.getPath().startsWith("include/")) {
                            VectorFontExport.LOGGER.info("忽视默认的引用 FontStorage: '{}'", key);
                            continue;
                        }
                        FontStorage fontStorage = entry.getValue();
                        VectorFontExport.LOGGER.info("正在导出 FontStorage: '{}'，一共有 {} 个 Font", key, fontStorage.fonts.size());

                        JsonObject obj = new JsonObject();
                        Map<Float, JsonArray> advancesReversedJson = new HashMap<>();
                        NbtCompound advances = new NbtCompound();
                        Set<Integer> addedCodePoints = new HashSet<>();
                        for (Font font : fontStorage.fonts) {
                            VectorFontExport.LOGGER.info("  正在扫描 Font 中的字符长度: '{}'", font.getClass().getName());
                            for (int codePoint : font.getProvidedGlyphs()) {
                                if (addedCodePoints.contains(codePoint)) continue;
                                Glyph glyph = font.getGlyph(codePoint);
                                if (glyph == null) continue;
                                addedCodePoints.add(codePoint);

                                float advance = glyph.getAdvance();
                                // 正向存入: 以 codePoint 为键，宽度 为值
                                advances.putFloat(String.valueOf(codePoint), advance);
                                // 反向存入: 以 宽度 为键，codePoint 为值
                                JsonArray codes = advancesReversedJson.computeIfAbsent(advance, k -> new JsonArray());
                                codes.add(codePoint);
                                advancesReversedJson.put(advance, codes);
                            }
                        }

                        NbtCompound fontCompound = new NbtCompound();
                        fontCompound.putString("id", key);
                        fontCompound.put("advances", advances);
                        fonts.add(fontCompound);

                        obj.addProperty("id", key);
                        JsonObject advancesObj = new JsonObject();
                        for (Map.Entry<Float, JsonArray> entryA : advancesReversedJson.entrySet()) {
                            String advStr = entryA.getKey().toString();
                            // 按 JSON 储存
                            advancesObj.add(advStr, entryA.getValue());
                        }
                        obj.add("advances", advancesObj);
                        fontsReversed.add(obj);
                    }
                    File fileNbt1 = new File(client.runDirectory, "fonts.nbt");
                    File fileJson = new File(client.runDirectory, "fonts.json");
                    NbtCompound nbt = new NbtCompound();
                    nbt.put("fonts", fonts);
                    NbtIo.writeCompressed(nbt, fileNbt1.toPath());
                    FileUtils.write(fileJson, fontsReversed.toString(), StandardCharsets.UTF_8);
                } catch (Throwable t) {
                    VectorFontExport.LOGGER.warn("导出字体时出现异常", t);
                }
                btn.active = true;
                btn.setMessage(exportText);
            });
        };
        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> client.setScreen(this.parent)).dimensions(0, 10, 100, 20).build());
        ButtonWidget btn = ButtonWidget.builder(exportText, onExportPress).dimensions(0, 50, 100, 20).build();
        if (executing) {
            btn.active = false;
            btn.setMessage(exportTextExecuting);
        }
        addDrawableChild(btn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        int centerY = height / 2;
        context.drawBorder(centerX - 100, centerY - 50, 200, 100, -1);
    }

    public static ButtonWidget getButton(MinecraftClient client) {
        Text text = Text.of("导出字体");
        ButtonWidget.PressAction onPress = (button) -> client.setScreen(new ExportFontScreen(client.currentScreen));
        return ButtonWidget.builder(text, onPress).dimensions(0, 30, 100, 20).build();
    }
}
