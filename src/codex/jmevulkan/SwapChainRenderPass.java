/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

/**
 *
 * @author codex
 */
public class SwapChainRenderPass {
    
    private final SwapChain swapchain;
    private final long pass;
    
    public SwapChainRenderPass(SwapChain swapchain) {
        this.swapchain = swapchain;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var attachments = VkAttachmentDescription.calloc(1, stack)
                    .format(swapchain.getImageFormat())
                    .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                    .initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
            var attachRef = VkAttachmentReference.calloc(1, stack)
                    .attachment(0)
                    .layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            var subpass = VkSubpassDescription.calloc(1, stack)
                    .pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(attachRef.remaining())
                    .pColorAttachments(attachRef);
            var dependencies = VkSubpassDependency.calloc(1, stack)
                    .srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            var passInfo = VkRenderPassCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subpass)
                    .pDependencies(dependencies);
            var longBuf = stack.mallocLong(1);
            check(VK10.vkCreateRenderPass(swapchain.getDevice().getLogicalDevice(), passInfo, null, longBuf),
                    "Failed to create render pass");
            pass = longBuf.get(0);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroyRenderPass(swapchain.getDevice().getLogicalDevice(), pass, null);
    }
    
    public long getPass() {
        return pass;
    }
    
}
