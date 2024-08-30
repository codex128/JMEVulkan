/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFenceCreateInfo;

/**
 *
 * @author codex
 */
public class Fence {
    
    private final LogicalDevice device;
    private final long fence;
    
    public Fence(LogicalDevice device, boolean signaled) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var fenceInfo = VkFenceCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0);
            var lBuf = stack.mallocLong(1);
            check(VK10.vkCreateFence(device.getLogicalDevice(), fenceInfo, null, lBuf),
                    "Failed to create fence");
            fence = lBuf.get(0);
        }
    }
    
    public void fenceWait() {
        int err = VK10.vkWaitForFences(device.getLogicalDevice(), fence, true, 1000L);
        if (err == VK10.VK_TIMEOUT) {
            throw new RuntimeException("Fence timed out.");
        }
    }
    
    public void reset() {
        VK10.vkResetFences(device.getLogicalDevice(), fence);
    }
    
    public void cleanup() {
        VK10.vkDestroyFence(device.getLogicalDevice(), fence, null);
    }
    
    public long getFence() {
        return fence;
    }
    
}
