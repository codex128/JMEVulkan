/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

/**
 *
 * @author codex
 */
public class Semaphore {
    
    private final LogicalDevice device;
    private final long semaphore;
    
    public Semaphore(LogicalDevice device) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var createInfo = VkSemaphoreCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            var lBuf = stack.mallocLong(1);
            check(VK10.vkCreateSemaphore(device.getLogicalDevice(), createInfo, null, lBuf),
                    "Failed to create semaphore");
            semaphore = lBuf.get(0);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroySemaphore(device.getLogicalDevice(), semaphore, null);
    }
    
    public long getSemaphore() {
        return semaphore;
    }
    
}
