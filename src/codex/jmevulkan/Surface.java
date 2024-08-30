/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkInstance;

/**
 *
 * @author codex
 */
public class Surface {
    
    private final PhysicalDevice physDevice;
    private final long surface;

    public Surface(PhysicalDevice physDevice, boolean glfw, long windowHandle) {
        this.physDevice = physDevice;
        if (glfw) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var surf = stack.mallocLong(1);
                GLFWVulkan.glfwCreateWindowSurface(getInstance(), windowHandle, null, surf);
                surface = surf.get(0);
            }
        } else {
            surface = windowHandle;
        }
    }
    
    public void cleanup() {
        KHRSurface.vkDestroySurfaceKHR(getInstance(), surface, null);
    }
    
    public final VkInstance getInstance() {
        return physDevice.getDevice().getInstance();
    }
    
    public long getSurface() {
        return surface;
    }
    
}
