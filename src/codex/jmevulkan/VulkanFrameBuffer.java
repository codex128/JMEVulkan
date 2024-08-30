/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

/**
 *
 * @author codex
 */
public class VulkanFrameBuffer {
    
    private final LogicalDevice device;
    private final long fbo;
    
    public VulkanFrameBuffer(LogicalDevice device, int width, int height, LongBuffer attachments, long pass) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var fbInfo = VkFramebufferCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .pAttachments(attachments)
                    .width(width)
                    .height(height)
                    .layers(1)
                    .renderPass(pass);
            var longBuf = stack.mallocLong(1);
            check(VK10.vkCreateFramebuffer(device.getLogicalDevice(), fbInfo, null, longBuf),
                    "Failed to create framebuffer");
            fbo = longBuf.get(0);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroyFramebuffer(device.getLogicalDevice(), fbo, null);
    }
    
    public long getFrameBuffer() {
        return fbo;
    }
    
}
