/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;

/**
 *
 * @author codex
 */
public class PresentQueue extends CommandQueue {
    
    public PresentQueue(LogicalDevice device, Surface surface, int queueIndex) {
        super(device, getPresentQueueFamilyIndex(device, surface), queueIndex);
    }
    
    private static int getPresentQueueFamilyIndex(LogicalDevice device, Surface surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var phys = device.getPhysicalDevice();
            var propsBuf = phys.getFamilyProps();
            propsBuf.rewind();
            var iBuf = stack.mallocInt(1);
            for (int i = 0; propsBuf.hasRemaining(); i++) {
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(phys.getDevice(), i, surface.getSurface(), iBuf);
                if (iBuf.get(0) == VK10.VK_TRUE) {
                    return i;
                }
            }
        }
        throw new RuntimeException("Failed to get presentation queue family index.");
    }
    
}
