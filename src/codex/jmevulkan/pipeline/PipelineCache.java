/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.pipeline;

import codex.jmevulkan.LogicalDevice;
import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;

/**
 *
 * @author codex
 */
public class PipelineCache {
    
    private final LogicalDevice device;
    private final long cache;
    
    public PipelineCache(LogicalDevice device) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var cacheInfo = VkPipelineCacheCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO);
            var lBuf = stack.mallocLong(1);
            check(VK10.vkCreatePipelineCache(device.getLogicalDevice(), cacheInfo, null, lBuf),
                    "Failed to create pipeline cache");
            cache = lBuf.get(1);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroyPipelineCache(device.getLogicalDevice(), cache, null);
    }
    
    public long getCache() {
        return cache;
    }
    
    public LogicalDevice getDevice() {
        return device;
    }
    
}
