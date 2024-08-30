/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

/**
 *
 * @author codex
 */
public class CommandPool {
    
    private final LogicalDevice device;
    private final long pool;
    
    public CommandPool(LogicalDevice device, int queueFamilyIndex) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queueFamilyIndex);
            var longBuf = stack.mallocLong(1);
            check(VK10.vkCreateCommandPool(device.getLogicalDevice(), poolInfo, null, longBuf),
                    "Failed to create command pool");
            pool = longBuf.get(0);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroyCommandPool(device.getLogicalDevice(), pool, null);
    }
    
    public LogicalDevice getDevice() {
        return device;
    }
    
    public long getPool() {
        return pool;
    }
    
}
