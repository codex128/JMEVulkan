/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.app;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.profile.AppStep;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codex
 */
public abstract class SimpleVulkanApplication extends VulkanApplication {
    public static final String INPUT_MAPPING_EXIT = "SIMPLEAPP_Exit";
    public static final String INPUT_MAPPING_CAMERA_POS = DebugKeysAppState.INPUT_MAPPING_CAMERA_POS;
    public static final String INPUT_MAPPING_MEMORY = DebugKeysAppState.INPUT_MAPPING_MEMORY;
    public static final String INPUT_MAPPING_HIDE_STATS = "SIMPLEAPP_HideStats";

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    private final AppActionListener actionListener = new AppActionListener();

    private class AppActionListener implements ActionListener {

        @Override
        public void onAction(String name, boolean value, float tpf) {
            if (!value) {
                return;
            }

            if (name.equals(INPUT_MAPPING_EXIT)) {
                stop();
            } else if (name.equals(INPUT_MAPPING_HIDE_STATS)) {
                if (stateManager.getState(VulkanStatsState.class) != null) {
                    stateManager.getState(VulkanStatsState.class).toggleStats();
                }
            }
        }
    }

    public SimpleVulkanApplication() {
        this(new VulkanStatsState(), new FlyCamAppState(), new AudioListenerState(), new DebugKeysAppState(),
                new ConstantVerifierState());
    }

    public SimpleVulkanApplication(AppState... initialStates) {
        super(initialStates);
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }

    /**
     * Returns the application's speed.
     *
     * @return The speed of the application.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Changes the application's speed. 0.0f prevents the application from updating.
     * @param speed The speed to set.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Retrieves flyCam
     * @return flyCam Camera object
     *
     */
    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * Retrieves guiNode
     * @return guiNode Node object
     *
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Retrieves rootNode
     * @return rootNode Node object
     *
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     * @param showSettings Sets true/false
     *
     */
    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    /**
     *  Creates the font that will be set to the guiFont field
     *  and subsequently set as the font for the stats text.
     *
     * @return the loaded BitmapFont
     */
    protected BitmapFont loadGuiFont() {
        return assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    @Override
    public void initialize() {
        super.initialize();

        // Several things rely on having this
        guiFont = loadGuiFont();

        guiNode.setQueueBucket(RenderQueue.Bucket.Gui);
        guiNode.setCullHint(Spatial.CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null) {

            // We have to special-case the FlyCamAppState because too
            // many SimpleApplication subclasses expect it to exist in
            // simpleInit().  But at least it only gets initialized if
            // the app state is added.
            if (stateManager.getState(FlyCamAppState.class) != null) {
                flyCam = new FlyByCamera(cam);
                flyCam.setMoveSpeed(1f); // odd to set this here but it did it before
                FlyCamAppState camState = stateManager.getState(FlyCamAppState.class);
                try {
                    Field f = FlyCamAppState.class.getField("flyCam");
                    f.setAccessible(true);
                    f.set(camState, flyCam);
                } catch (SecurityException | IllegalAccessException
                        | IllegalArgumentException | NoSuchFieldException ex) {
                    Logger.getLogger(SimpleVulkanApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (context.getType() == JmeContext.Type.Display) {
                inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
            }

            if (stateManager.getState(VulkanStatsState.class) != null) {
                inputManager.addMapping(INPUT_MAPPING_HIDE_STATS, new KeyTrigger(KeyInput.KEY_F5));
                inputManager.addListener(actionListener, INPUT_MAPPING_HIDE_STATS);
            }

            inputManager.addListener(actionListener, INPUT_MAPPING_EXIT);
        }

        if (stateManager.getState(VulkanStatsState.class) != null) {
            // Some tests rely on having access to fpsText
            // for quick display.  Maybe a different way would be better.
            stateManager.getState(VulkanStatsState.class).setFont(guiFont);
            fpsText = stateManager.getState(VulkanStatsState.class).getFpsText();
        }

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (prof != null) {
            prof.appStep(AppStep.BeginFrame);
        }

        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;

        // update states
        if (prof != null) {
            prof.appStep(AppStep.StateManagerUpdate);
        }
        stateManager.update(tpf);

        // simple update and root node
        simpleUpdate(tpf);

        if (prof != null) {
            prof.appStep(AppStep.SpatialUpdate);
        }
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);

        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        if (prof != null) {
            prof.appStep(AppStep.StateManagerRender);
        }
        stateManager.render(renderManager);

        if (prof != null) {
            prof.appStep(AppStep.RenderFrame);
        }
        renderManager.render(tpf, context.isRenderable());
        simpleRender(renderManager);
        stateManager.postRender();

        if (prof != null) {
            prof.appStep(AppStep.EndFrame);
        }
    }

    public void setDisplayFps(boolean show) {
        if (stateManager.getState(VulkanStatsState.class) != null) {
            stateManager.getState(VulkanStatsState.class).setDisplayFps(show);
        }
    }

    public void setDisplayStatView(boolean show) {
        if (stateManager.getState(VulkanStatsState.class) != null) {
            stateManager.getState(VulkanStatsState.class).setDisplayStatView(show);
        }
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf) {
    }

    public void simpleRender(RenderManager rm) {
    }
}
