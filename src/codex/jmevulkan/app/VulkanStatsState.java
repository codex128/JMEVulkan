/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.app;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsView;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author codex
 */
public class VulkanStatsState extends AbstractAppState {
    
    private Application app;
    protected StatsView statsView;
    protected boolean showSettings = true;
    private boolean showFps = true;
    private boolean showStats = true;
    private boolean darkenBehind = true;

    protected Node guiNode;
    protected float secondCounter = 0.0f;
    protected int frameCounter = 0;
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected Geometry darkenFps;
    protected Geometry darkenStats;

    public VulkanStatsState() {
    }

    public VulkanStatsState(Node guiNode, BitmapFont guiFont) {
        this.guiNode = guiNode;
        this.guiFont = guiFont;
    }

    /**
     *  Called by SimpleApplication to provide an early font
     *  so that the fpsText can be created before init.  This
     *  is because several applications expect to directly access
     *  fpsText... unfortunately.
     *
     * @param guiFont the desired font (not null, alias created)
     */
    public void setFont(BitmapFont guiFont) {
        this.guiFont = guiFont;
        this.fpsText = new BitmapText(guiFont);
    }

    public BitmapText getFpsText() {
        return fpsText;
    }

    public StatsView getStatsView() {
        return statsView;
    }

    public float getSecondCounter() {
        return secondCounter;
    }

    public void toggleStats() {
        setDisplayFps(!showFps);
        setDisplayStatView(!showStats);
    }

    public void setDisplayFps(boolean show) {
        showFps = show;
        if (fpsText != null) {
            fpsText.setCullHint(show ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            if (darkenFps != null) {
                darkenFps.setCullHint(showFps && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            }

        }
    }

    public void setDisplayStatView(boolean show) {
        showStats = show;
        if (statsView != null) {
            statsView.setEnabled(show);
            statsView.setCullHint(show ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            if (darkenStats != null) {
                darkenStats.setCullHint(showStats && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            }
        }
    }

    public void setDarkenBehind(boolean darkenBehind) {
        this.darkenBehind = darkenBehind;
        setEnabled(isEnabled());
    }

    public boolean isDarkenBehind() {
        return darkenBehind;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;

        if (app instanceof SimpleVulkanApplication) {
            SimpleVulkanApplication simpleApp = (SimpleVulkanApplication) app;
            if (guiNode == null) {
                guiNode = simpleApp.guiNode;
            }
            if (guiFont == null) {
                guiFont = simpleApp.guiFont;
            }
        }

        if (guiNode == null) {
            throw new RuntimeException("No guiNode specific and cannot be automatically determined.");
        }

        if (guiFont == null) {
            guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        }

        loadFpsText();
        loadStatsView();
        loadDarken();
    }

    /**
     * Attaches FPS statistics to guiNode and displays it on the screen.
     *
     */
    public void loadFpsText() {
        if (fpsText == null) {
            fpsText = new BitmapText(guiFont);
        }

        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Frames per second");
        fpsText.setCullHint(showFps ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        guiNode.attachChild(fpsText);

    }

    /**
     * Attaches Statistics View to guiNode and displays it on the screen
     * above FPS statistics line.
     *
     */
    public void loadStatsView() {
        statsView = new StatsView("Statistics View",
                app.getAssetManager(),
                app.getRenderer().getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        statsView.setEnabled(showStats);
        statsView.setCullHint(showStats ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        guiNode.attachChild(statsView);
    }

    public void loadDarken() {
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.5f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        darkenFps = new Geometry("StatsDarken", new Quad(200, fpsText.getLineHeight()));
        darkenFps.setMaterial(mat);
        darkenFps.setLocalTranslation(0, 0, -1);
        darkenFps.setCullHint(showFps && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        guiNode.attachChild(darkenFps);

        darkenStats = new Geometry("StatsDarken", new Quad(200, statsView.getHeight()));
        darkenStats.setMaterial(mat);
        darkenStats.setLocalTranslation(0, fpsText.getHeight(), -1);
        darkenStats.setCullHint(showStats && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        guiNode.attachChild(darkenStats);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            fpsText.setCullHint(showFps ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            darkenFps.setCullHint(showFps && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            statsView.setEnabled(showStats);
            statsView.setCullHint(showStats ? Spatial.CullHint.Never : Spatial.CullHint.Always);
            darkenStats.setCullHint(showStats && darkenBehind ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        } else {
            fpsText.setCullHint(Spatial.CullHint.Always);
            darkenFps.setCullHint(Spatial.CullHint.Always);
            statsView.setEnabled(false);
            statsView.setCullHint(Spatial.CullHint.Always);
            darkenStats.setCullHint(Spatial.CullHint.Always);
        }
    }

    @Override
    public void update(float tpf) {
        if (showFps) {
            secondCounter += app.getTimer().getTimePerFrame();
            frameCounter++;
            if (secondCounter >= 1.0f) {
                int fps = (int) (frameCounter / secondCounter);
                fpsText.setText("Frames per second: " + fps);
                secondCounter = 0.0f;
                frameCounter = 0;
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        guiNode.detachChild(statsView);
        guiNode.detachChild(fpsText);
        guiNode.detachChild(darkenFps);
        guiNode.detachChild(darkenStats);
    }
}
