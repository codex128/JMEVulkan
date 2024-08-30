/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import org.lwjgl.vulkan.VK10;

/**
 *
 * @author codex
 */
public class GraphicsQueue extends CommandQueue {
    
    public GraphicsQueue(LogicalDevice device, int queueIndex) {
        super(device, getGraphicsQueueFamilyIndex(device), queueIndex);
    }
    
    private static int getGraphicsQueueFamilyIndex(LogicalDevice device) {
        var physDevice = device.getPhysicalDevice();
        var propsBuf = physDevice.getFamilyProps();
        propsBuf.rewind();
        for (int i = 0; propsBuf.hasRemaining(); i++) {
            if ((propsBuf.get().queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
                return i;
            }
        }
        throw new RuntimeException("Failed to get graphics queue family index.");
    }
    
}
