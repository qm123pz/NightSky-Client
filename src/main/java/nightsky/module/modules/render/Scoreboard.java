package nightsky.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import nightsky.events.Render2DEvent;
import nightsky.event.EventTarget;
import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.IntValue;

import java.util.Collection;
import java.util.List;

public class Scoreboard extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    
    public final BooleanValue hide = new BooleanValue("Hide", false);
    public final IntValue offsetX = new IntValue("OffsetX", 0, -1000, 200);
    public final IntValue offsetY = new IntValue("OffsetY", 0, -600, 200);
    
    public static Scoreboard instance;
    private boolean isRenderingCustom = false;
    
    public Scoreboard() {
        super("Scoreboard", false);
        instance = this;
    }
    
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        
        if (hide.getValue()) {
            return;
        }
        
        if (offsetX.getValue() != 0 || offsetY.getValue() != 0) {
            if (!isRenderingCustom) {
                isRenderingCustom = true;
                renderCustomScoreboard();
                isRenderingCustom = false;
            }
        }
    }
    
    public boolean shouldHideOriginalScoreboard() {
        return this.isEnabled() && (hide.getValue() || (offsetX.getValue() != 0 || offsetY.getValue() != 0));
    }
    
    private void renderCustomScoreboard() {
        if (mc.theWorld == null || mc.gameSettings.showDebugInfo) return;
        
        ScoreObjective scoreobjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
        if (scoreobjective != null) {
            net.minecraft.scoreboard.Scoreboard scoreboard = scoreobjective.getScoreboard();
            Collection<Score> collection = scoreboard.getSortedScores(scoreobjective);
            List<Score> list = com.google.common.collect.Lists.newArrayList(com.google.common.collect.Iterables.filter(collection, new com.google.common.base.Predicate<Score>() {
                public boolean apply(Score score) {
                    return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
                }
            }));
            
            if (list.size() > 15) {
                collection = com.google.common.collect.Lists.newArrayList(com.google.common.collect.Iterables.skip(list, collection.size() - 15));
            } else {
                collection = list;
            }
            
            int maxWidth = mc.fontRendererObj.getStringWidth(scoreobjective.getDisplayName());
            
            for (Score score : collection) {
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
                maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(s));
            }
            
            int listSize = collection.size();
            int height = listSize * mc.fontRendererObj.FONT_HEIGHT;
            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int screenWidth = scaledresolution.getScaledWidth();
            int screenHeight = scaledresolution.getScaledHeight();
            int posX = screenWidth - maxWidth - 3 + offsetX.getValue();
            int posY = screenHeight / 2 + height / 3 + offsetY.getValue();
            
            int backgroundColor = 0x90505050;
            int headerColor = 0x90FF0000;
            
            GlStateManager.enableBlend();
            Gui.drawRect(posX - 2, posY - mc.fontRendererObj.FONT_HEIGHT - 1, posX + maxWidth, posY + height, backgroundColor);
            Gui.drawRect(posX - 2, posY - 1, posX + maxWidth, posY, headerColor);
            
            mc.fontRendererObj.drawString(scoreobjective.getDisplayName(), posX + maxWidth / 2 - mc.fontRendererObj.getStringWidth(scoreobjective.getDisplayName()) / 2, posY - mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
            
            int index = 0;
            for (Score score : collection) {
                ++index;
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
                String scoreValue = "" + EnumChatFormatting.RED + score.getScorePoints();
                int yPos = posY + (listSize - index) * mc.fontRendererObj.FONT_HEIGHT;
                
                mc.fontRendererObj.drawString(playerName, posX, yPos, 0xFFFFFF);
                mc.fontRendererObj.drawString(scoreValue, posX + maxWidth - mc.fontRendererObj.getStringWidth(scoreValue), yPos, 0xFFFFFF);
            }
            GlStateManager.disableBlend();
        }
    }
}
