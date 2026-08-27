/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.Element;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.SlowlyStyle;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.UiUtils;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClickGui extends GuiScreen {

    public final List<Panel> panels = new ArrayList<>();
    private final ResourceLocation hudIcon = new ResourceLocation(LiquidBounce.CLIENT_NAME.toLowerCase() + "/custom_hud_icon.png");
    public Style style = new SlowlyStyle();
    private Panel clickedPanel;
    private int mouseX;
    private int mouseY;
    private int scrollY = 0;

    public double slide, progress = 0;

    public long lastMS = System.currentTimeMillis();

    public ClickGui() {
        final int width = 100;
        final int height = 18;

        int yPos = 5;
        for (final ModuleCategory category : ModuleCategory.values()) {
            panels.add(new Panel(category.getDisplayName(), 100, yPos, width, height, false) {

                @Override
                public void setupItems() {
                    for (Module module : LiquidBounce.moduleManager.getModules())
                        if (module.getCategory() == category)
                            getElements().add(new ModuleElement(module));
                }
            });

            yPos += 20;
        }

        yPos += 20;

        panels.add(new Panel("Targets", 100, yPos, width, height, false) {

            @Override
            public void setupItems() {
                getElements().add(new ButtonElement("Players") {

                    @Override
                    public void createButton(String displayName) {
                        color = EntityUtils.targetPlayer ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        super.createButton(displayName);
                    }

                    @Override
                    public String getDisplayName() {
                        displayName = "Players";
                        color = EntityUtils.targetPlayer ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        return super.getDisplayName();
                    }

                    @Override
                    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible()) {
                            EntityUtils.targetPlayer = !EntityUtils.targetPlayer;
                            displayName = "Players";
                            color = EntityUtils.targetPlayer ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                    }
                });

                getElements().add(new ButtonElement("Mobs") {

                    @Override
                    public void createButton(String displayName) {
                        color = EntityUtils.targetMobs ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        super.createButton(displayName);
                    }

                    @Override
                    public String getDisplayName() {
                        displayName = "Mobs";
                        color = EntityUtils.targetMobs ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        return super.getDisplayName();
                    }

                    @Override
                    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible()) {
                            EntityUtils.targetMobs = !EntityUtils.targetMobs;
                            displayName = "Mobs";
                            color = EntityUtils.targetMobs ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                    }
                });

                getElements().add(new ButtonElement("Animals") {

                    @Override
                    public void createButton(String displayName) {
                        color = EntityUtils.targetAnimals ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        super.createButton(displayName);
                    }

                    @Override
                    public String getDisplayName() {
                        displayName = "Animals";
                        color = EntityUtils.targetAnimals ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        return super.getDisplayName();
                    }

                    @Override
                    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible()) {
                            EntityUtils.targetAnimals = !EntityUtils.targetAnimals;
                            displayName = "Animals";
                            color = EntityUtils.targetAnimals ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                    }
                });

                getElements().add(new ButtonElement("Invisible") {

                    @Override
                    public void createButton(String displayName) {
                        color = EntityUtils.targetInvisible ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        super.createButton(displayName);
                    }

                    @Override
                    public String getDisplayName() {
                        displayName = "Invisible";
                        color = EntityUtils.targetInvisible ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        return super.getDisplayName();
                    }

                    @Override
                    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible()) {
                            EntityUtils.targetInvisible = !EntityUtils.targetInvisible;
                            displayName = "Invisible";
                            color = EntityUtils.targetInvisible ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                    }
                });

                getElements().add(new ButtonElement("Dead") {

                    @Override
                    public void createButton(String displayName) {
                        color = EntityUtils.targetDead ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        super.createButton(displayName);
                    }

                    @Override
                    public String getDisplayName() {
                        displayName = "Dead";
                        color = EntityUtils.targetDead ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                        return super.getDisplayName();
                    }

                    @Override
                    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
                        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible()) {
                            EntityUtils.targetDead = !EntityUtils.targetDead;
                            displayName = "Dead";
                            color = EntityUtils.targetDead ? ClickGUI.generateColor().getRGB() : Integer.MAX_VALUE;
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void initGui() {
        slide = progress = 0;
        lastMS = System.currentTimeMillis();
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (progress < 1) progress = (float)(System.currentTimeMillis() - lastMS) / 500F; // fully fps async
        else progress = 1;

        switch (((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).animationValue.get().toLowerCase()) {
            case "slidebounce":
            case "zoombounce":
            slide = EaseUtils.easeOutBack(progress);
            break;
            case "slide":
            case "zoom":
            case "azura":
            slide = EaseUtils.easeOutQuart(progress);
            break;
            case "none":
            slide = 1;
            break;
        }

        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50)
            mc.displayGuiScreen(new GuiHudDesigner());

        // Enable DisplayList optimization
        AWTFontRenderer.Companion.setAssumeNonVolatile(true);

        final double scale = ((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        switch (((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).backgroundValue.get()) {
        case "Default":
            drawDefaultBackground();
            break;
        case "Gradient":
            drawGradientRect(0, 0, width, height, ColorUtils.reAlpha(ClickGUI.generateColor(), 40).getRGB(), ClickGUI.generateColor().getRGB());
            break;
        default:
            break;
        }

        GlStateManager.disableAlpha();
        RenderUtils.drawImage(hudIcon, 9, height - 41, 32, 32);
        GlStateManager.enableAlpha();

        switch (((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).animationValue.get().toLowerCase()) {
            case "azura":
            GlStateManager.translate(0, (1.0 - slide) * height * 2.0, 0);
            GlStateManager.scale(scale, scale + (1.0 - slide) * 2.0, scale);
            break;
            case "slide":
            case "slidebounce":
            GlStateManager.translate(0, (1.0 - slide) * height * 2.0, 0);
            GlStateManager.scale(scale, scale, scale);
            break;
            case "zoom":
            GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), (1.0 - slide) * (width / 2.0));
            GlStateManager.scale(scale * slide, scale * slide, scale * slide);
            break;
            case "zoombounce":
            GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), 0);
            GlStateManager.scale(scale * slide, scale * slide, scale * slide);
            break;
            case "none":
            GlStateManager.scale(scale, scale, scale);
            break;
        }

        for (final Panel panel : panels) {
            panel.updateFade(RenderUtils.deltaTime);
            int originalY = panel.y;
            panel.y += scrollY;
            panel.drawScreen(mouseX, mouseY, partialTicks);
            panel.y = originalY;
        }

        for (final Panel panel : panels) {
            for (final Element element : panel.getElements()) {
                if (element instanceof ModuleElement) {
                    final ModuleElement moduleElement = (ModuleElement) element;

                    if (mouseX != 0 && mouseY != 0 && moduleElement.isHovering(mouseX, mouseY) && moduleElement.isVisible() && element.getY() <= panel.getY() + panel.getFade())
                        style.drawDescription(mouseX, mouseY, moduleElement.getModule().getDescription());
                }
            }
        }

        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();

            boolean handled = false;
            for (int i = panels.size() - 1; i >= 0; i--) {
                Panel panel = panels.get(i);
                int adjustedY = panel.getY() + scrollY;
                if (mouseX >= panel.getX() && mouseX <= panel.getX() + panel.getWidth()
                    && mouseY >= adjustedY && mouseY <= adjustedY + 19 + panel.getElementsHeight()) {
                    if (panels.get(i).handleScroll(mouseX, mouseY - scrollY, wheel)) {
                        handled = true;
                        break;
                    }
                }
            }

            if (!handled) {
                scrollY += wheel > 0 ? 20 : -20;
                int minY = Integer.MAX_VALUE;
                for (Panel panel : panels) {
                    if (panel.getY() + scrollY < minY)
                        minY = panel.getY() + scrollY;
                }
                if (minY > 10)
                    scrollY = 10 - (minY - scrollY);
                int maxY = 0;
                for (Panel panel : panels) {
                    int panelBottom = panel.getY() + scrollY + 20 + panel.getElementsHeight();
                    if (panelBottom > maxY)
                        maxY = panelBottom;
                }
                if (maxY < height - 10)
                    scrollY += (height - 10) - maxY;
            }
        }

        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();

        switch (((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).animationValue.get().toLowerCase()) {
            case "azura":
            GlStateManager.translate(0, (1.0 - slide) * height * -2.0, 0);
            break;
            case "slide":
            case "slidebounce":
            GlStateManager.translate(0, (1.0 - slide) * height * -2.0, 0);
            break;
            case "zoom":
            GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), -1 * (1.0 - slide) * (width / 2.0));
            break;
            case "zoombounce":
            GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), 0);
            break;
        }
        GlStateManager.scale(1, 1, 1);

        AWTFontRenderer.Companion.setAssumeNonVolatile(false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        final double scale = ((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        for (final Panel panel : panels) {
            int adjustedY = panel.getY() + scrollY;
            panel.mouseClicked(mouseX, mouseY - scrollY, mouseButton);

            panel.drag = false;

            if (mouseButton == 0 && mouseX >= panel.getX() && mouseX <= panel.getX() + panel.getWidth()
                && mouseY >= adjustedY && mouseY <= adjustedY + panel.getHeight())
                clickedPanel = panel;
        }

        if (clickedPanel != null) {
            clickedPanel.x2 = clickedPanel.x - mouseX;
            clickedPanel.y2 = clickedPanel.getY() + scrollY - mouseY;
            clickedPanel.drag = true;

            panels.remove(clickedPanel);
            panels.add(clickedPanel);
            clickedPanel = null;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        final double scale = ((ClickGUI) Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class))).scaleValue.get();

        mouseX /= scale;
        mouseY /= scale;

        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        for (final Panel panel : panels) {
            for (final Element element : panel.getElements()) {
                if (element instanceof ButtonElement) {
                    final ButtonElement buttonElement = (ButtonElement) element;

                    if (buttonElement.isHovering(mouseX, mouseY)) {
                        if (buttonElement.hoverTime < 7)
                            buttonElement.hoverTime++;
                    } else if (buttonElement.hoverTime > 0)
                        buttonElement.hoverTime--;
                }

                if (element instanceof ModuleElement) {
                    if (((ModuleElement) element).getModule().getState()) {
                        if (((ModuleElement) element).slowlyFade < 255)
                            ((ModuleElement) element).slowlyFade += 50;
                    } else if (((ModuleElement) element).slowlyFade > 0)
                        ((ModuleElement) element).slowlyFade -= 50;

                    if (((ModuleElement) element).slowlyFade > 255)
                        ((ModuleElement) element).slowlyFade = 255;

                    if (((ModuleElement) element).slowlyFade < 0)
                        ((ModuleElement) element).slowlyFade = 0;
                }
            }
        }
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.clickGuiConfig);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
