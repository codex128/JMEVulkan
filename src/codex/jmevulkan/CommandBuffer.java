/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;

/**
 *
 * @author codex
 */
public class CommandBuffer {
    
    private final CommandPool pool;
    private final VkCommandBuffer buffer;
    private final boolean oneTimeSubmit;
    private boolean primary;
    
    public CommandBuffer(CommandPool pool, boolean primary, boolean oneTimeSubmit) {
        this.pool = pool;
        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;
        var device = pool.getDevice().getLogicalDevice();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var allocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(pool.getPool())
                    .level(asPrimaryBuffer(primary))
                    .commandBufferCount(1);
            var pBuf = stack.mallocPointer(1);
            check(VK10.vkAllocateCommandBuffers(device, allocateInfo, pBuf),
                    "Failed to allocate command buffer");
            buffer = new VkCommandBuffer(pBuf.get(0), device);
        }
    }
    
    public void beginRecording() {
        beginRecording(null);
    }
    
    public void beginRecording(InheritanceInfo inheritance) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            if (oneTimeSubmit) {
                beginInfo.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            }
            if (!primary) {
                if (inheritance == null) {
                    throw new NullPointerException("Secondary buffer requires inheritance info.");
                }
                var inhInfo = VkCommandBufferInheritanceInfo.calloc(stack)
                        .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                        .renderPass(inheritance.pass)
                        .subpass(inheritance.subpass)
                        .framebuffer(inheritance.framebuffer);
                beginInfo.pInheritanceInfo(inhInfo);
                beginInfo.flags(VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            }
            check(VK10.vkBeginCommandBuffer(buffer, beginInfo), "Failed to begin command buffer");
        }
    }
    
    public void endRecording() {
        check(VK10.vkEndCommandBuffer(buffer), "Failed to end command buffer");
    }
    
    public void reset() {
        VK10.vkResetCommandBuffer(buffer, VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
    }
    
    public void cleanup() {
        VK10.vkFreeCommandBuffers(pool.getDevice().getLogicalDevice(), pool.getPool(), buffer);
    }
    
    public VkCommandBuffer getBuffer() {
        return buffer;
    }
    
    public record InheritanceInfo(long pass, long framebuffer, int subpass) {}
    
    private static int asPrimaryBuffer(boolean primary) {
        return primary ? VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
    }
    
}
