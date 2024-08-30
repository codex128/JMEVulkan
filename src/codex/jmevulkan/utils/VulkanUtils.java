/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.utils;

import codex.jmevulkan.PhysicalDevice;
import static org.lwjgl.vulkan.VK10.*;

/**
 *
 * @author codex
 */
public class VulkanUtils {
    
    public static final int FLOAT_LENGTH = 4;
    
    private VulkanUtils() {}
    
    public static void check(int err, String errMsg) {
        if (err != VK_SUCCESS) {
            throw new RuntimeException(errMsg + ": " + err);
        }
    }
    
    public static int memoryTypeFromProperties(PhysicalDevice device, int typeBits, int reqsMask) {
        var memTypes = device.getMemory().memoryTypes();
        for (int i = 0; i < VK_MAX_MEMORY_TYPES; i++) {
            if ((typeBits & 1) == 1 && (memTypes.get(i).propertyFlags() & reqsMask) == reqsMask) {
                return i;
            }
        }
        throw new RuntimeException("Failed to find memory type.");
    }
    
}
