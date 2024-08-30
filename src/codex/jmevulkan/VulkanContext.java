/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import codex.jmevulkan.app.LwjglVulkanWindow;
import codex.jmevulkan.app.VulkanSettings;
import static codex.jmevulkan.utils.VulkanUtils.*;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.pipeline.PipelineContext;
import com.jme3.system.AWTContext;
import com.jme3.system.JmeContext;
import com.jme3.system.NullContext;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.lwjgl.LwjglCanvas;
import com.jme3.system.lwjgl.LwjglDisplay;
import com.jme3.system.lwjgl.LwjglOffscreenBuffer;
import java.awt.AWTException;
import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugUtils;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRXcbSurface;
import static org.lwjgl.vulkan.VK11.*;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.awt.AWTVK;

/**
 *
 * @author codex
 */
public class VulkanContext extends BaseAppState implements PipelineContext {

    private static final Logger LOG = Logger.getLogger(VulkanContext.class.getName());
    
    private final Application app;
    private VulkanRenderer renderer;
    private VkInstance instance;
    private VulkanSettings settings;
    private VkDebugUtilsMessengerCreateInfoEXT debugUtils;
    private long windowHandle = VK_NULL_HANDLE;
    private long debugHandle = VK_NULL_HANDLE;
    private int windowWidth, windowHeight;
    private boolean windowGlfw;
    private boolean rendered = false;
    
    
    public VulkanContext(Application app) {
        this.app = app;
        this.settings = new VulkanSettings();
    }
    
    @Override
    public boolean startViewPortRender(RenderManager rm, ViewPort vp) {
        if (instance == null) {
            throw new IllegalStateException("Context has not been started.");
        }
        return rendered;
    }
    @Override
    public void endViewPortRender(RenderManager rm, ViewPort vp) {
        rendered = true;
    }
    @Override
    public void endContextRenderFrame(RenderManager rm) {
        rendered = false;
    }
    @Override
    protected void initialize(Application app) {}
    @Override
    protected void cleanup(Application app) {
        stop();
    }
    @Override
    protected void onEnable() {}
    @Override
    protected void onDisable() {}
    
    public VulkanContext start() {
        app.getStateManager().attach(this);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer appName = stack.UTF8("JMEVulkan");
            ByteBuffer engineName = stack.UTF8("JMonkeyEngine");
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(appName)
                    .applicationVersion(1)
                    .pEngineName(engineName)
                    .apiVersion(VK_API_VERSION_1_1);
            List<String> layerNames = settings.getSupportedValidationLayers();
            PointerBuffer reqLayers = null;
            if (!layerNames.isEmpty()) {
                reqLayers = stack.mallocPointer(layerNames.size());
                for (String l : layerNames) {
                    LOG.log(Level.INFO, "Using validation layer: {0}", l);
                    reqLayers.put(stack.ASCII(l));
                }
            }
            Window window = getWindow(app.getContext());
            windowGlfw = window.isGLFW();
            PointerBuffer windowExt;
            if (windowGlfw) {
                if (!GLFWVulkan.glfwVulkanSupported()) {
                    throw new IllegalStateException("GLFW window not compatible with Vulkan");
                }
                windowExt = GLFWVulkan.glfwGetRequiredInstanceExtensions();
                if (windowExt == null) {
                    throw new NullPointerException("Failed to get GLFW extensions.");
                }
            } else {
                windowExt = stack.pointers(
                        stack.UTF8(KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME),
                        stack.UTF8(AWTVK.getSurfaceExtensionName()));
            }
            int numExt = windowExt.remaining();
            PointerBuffer reqExt;
            // todo: add extensions for macos
            if (!layerNames.isEmpty()) {
                ByteBuffer vkDebugUtils = stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
                reqExt = stack.mallocPointer(numExt + 2);
                reqExt.put(vkDebugUtils);
            } else {
                reqExt = stack.mallocPointer(numExt + 1);
            }
            reqExt.put(windowExt).put(stack.UTF8(KHRXcbSurface.VK_KHR_XCB_SURFACE_EXTENSION_NAME));
            reqExt.flip();
            debugUtils = null;
            long extension = MemoryUtil.NULL;
            if (!layerNames.isEmpty()) {
                debugUtils = createDebugCallBack();
                extension = debugUtils.address();
            }
            VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(extension)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(reqLayers)
                    .ppEnabledExtensionNames(reqExt);
            PointerBuffer instBuf = stack.mallocPointer(1);
            check(vkCreateInstance(info, null, instBuf), "Error creating instance");
            instance = new VkInstance(instBuf.get(0), info);
            if (!layerNames.isEmpty()) {
                LongBuffer buf = stack.mallocLong(1);
                check(vkCreateDebugUtilsMessengerEXT(instance, debugUtils, null, buf), "Error creating debug utils");
                debugHandle = buf.get(0);
            }
            windowHandle = window.getHandle(instance);
            windowWidth = app.getContext().getSettings().getWidth();
            windowHeight = app.getContext().getSettings().getHeight();
            renderer = new VulkanRenderer(this, 2, app.getContext().getSettings().isVSync());
        } catch (AWTException ex) {
            throw new RuntimeException("Failed to get AWT window handle.", ex);
        }
        return this;
    }
    public void stop() {
        if (debugHandle != VK_NULL_HANDLE) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugHandle, null);
        }
        if (debugUtils != null) {
            debugUtils.pfnUserCallback();
            debugUtils.free();
        }
        vkDestroyInstance(instance, null);
        renderer.cleanup();
    }
    
    public VulkanRenderer getRenderer() {
        return renderer;
    }
    public VkInstance getInstance() {
        return instance;
    }
    public VulkanSettings getSettings() {
        return settings;
    }
    public long getWindowHandle() {
        return windowHandle;
    }
    public int getWindowWidth() {
        return windowWidth;
    }
    public int getWindowHeight() {
        return windowHeight;
    }
    public boolean isWindowGlfw() {
        return windowGlfw;
    }
    
    private static Window getWindow(JmeContext context) throws AWTException {
        // LWJGL vulkan window
        if (context instanceof LwjglVulkanWindow) {
            return new Window(((LwjglVulkanWindow)context).getWindowHandle(), null);
        }
        // LWJGL window
        if (context instanceof LwjglDisplay) {
            return new Window(((LwjglDisplay)context).getWindowHandle(), null);
        }
        // AWT
        if (context instanceof AwtPanelsContext) {
            return getWindow(getField(context, "actualContext"));
        } else if (context instanceof AWTContext) {
            return getWindow(getField(context, "backgroundContext"));
        } else if (context instanceof LwjglCanvas) {
            return new Window(-1, ((LwjglCanvas)context).getCanvas());
        }
        // todo: LWJGL offscreen
        if (context instanceof LwjglOffscreenBuffer) {
            throw new UnsupportedOperationException("LWJGL offscreen buffer is not yet supported.");
        }
        // todo: headless
        if (context instanceof NullContext) {
            throw new UnsupportedOperationException("Headless mode is not yet supported.");
        }
        throw new NullPointerException("Failed to retrieve window.");
    }
    private static <T> T getField(Object object, String field) {
        try {
            var f = object.getClass().getField(field);
            f.setAccessible(true);
            return (T)f.get(object);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException("Error accessing field \"" + field + "\" of " + object, ex);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Error retrieving value of \"" + field + "\" in " + object, ex);
        }
    }
    private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallBack() {
        VkDebugUtilsMessengerCreateInfoEXT result = VkDebugUtilsMessengerCreateInfoEXT.calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                //.messageSeverity(MESSAGE_SEVERITY_BITMASK)
                //.messageType(MESSAGE_TYPE_BITMASK)
                .pfnUserCallback((messageSeverity, messageTypes, pCallbackData, pUserData) -> {
                    VkDebugUtilsMessengerCallbackDataEXT callbackData =
                            VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
                    if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
                        LOG.log(Level.INFO, "VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
                        LOG.log(Level.WARNING, "VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
                        LOG.log(Level.SEVERE, "VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else {
                        LOG.log(Level.FINE, "VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    }
                    return VK_FALSE;
                });
        return result;
    }
    
    private static class Window {
        
        private long handle;
        public final Canvas canvas;

        public Window(long handle, Canvas canvas) {
            this.handle = handle;
            this.canvas = canvas;
        }
        
        public long getHandle(VkInstance instance) throws AWTException {
            if (canvas != null) {
                fromCanvas(instance);
            }
            return handle;
        }
        public long fromCanvas(VkInstance instance) throws AWTException {
            return (handle = AWTVK.create(canvas, instance));
        }
        
        public boolean isGLFW() {
            return canvas == null;
        }
        
    }
    
}
