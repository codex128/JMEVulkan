/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import com.jme3.renderer.ViewPort;

/**
 *
 * @author codex
 */
public class VulkanRenderer {
    
    private final VulkanContext context;
    private final LogicalDevice logicDevice;
    private final GraphicsQueue graphicsQueue;
    private final PhysicalDevice physDevice;
    private final Surface surface;
    private final SwapChain swapchain;
    private final CommandPool commandPool;
    private final PresentQueue presentQueue;
    private final ForwardRenderActivity fwdActivity;
    
    public VulkanRenderer(VulkanContext context, int requestedImages, boolean vsync) {
        this.context = context;
        physDevice = PhysicalDevice.createDevice(context.getInstance(), context.getSettings().getPreferredDevice());
        logicDevice = new LogicalDevice(physDevice);
        surface = new Surface(physDevice, context.isWindowGlfw(), context.getWindowHandle());
        graphicsQueue = new GraphicsQueue(logicDevice, 0);
        presentQueue = new PresentQueue(logicDevice, surface, 0);
        swapchain = new SwapChain(context, logicDevice, surface, requestedImages,
                vsync, presentQueue, new CommandQueue[] {graphicsQueue});
        commandPool = new CommandPool(logicDevice, graphicsQueue.getQueueFamilyIndex());
        fwdActivity = new ForwardRenderActivity(swapchain, commandPool);
    }
    
    public void renderViewPort(ViewPort viewPort) {
        fwdActivity.waitForFence();
        int imgIndex = swapchain.acquireNextImage();
        if (imgIndex < 0) {
            return;
        }
        fwdActivity.submit(graphicsQueue);
        swapchain.presentImage(presentQueue, imgIndex);
    }
    
    public void cleanup() {
        surface.cleanup();
        logicDevice.cleanup();
        physDevice.cleanup();
        swapchain.cleanup();
    }
    
}
