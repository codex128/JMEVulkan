/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

/**
 *
 * @author codex
 */
public class ForwardRenderActivity {
    
    private final SwapChain swapchain;
    private final SwapChainRenderPass renderPass;
    private final VulkanFrameBuffer[] framebuffers;
    private final CommandBuffer[] commandbuffers;
    private final Fence[] fences;
    
    public ForwardRenderActivity(SwapChain swapchain, CommandPool pool) {
        this.swapchain = swapchain;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LogicalDevice device = swapchain.getDevice();
            VkExtent2D extent = swapchain.getExtent();
            ImageView[] views = swapchain.getViews();
            renderPass = new SwapChainRenderPass(swapchain);
            var attachments = stack.mallocLong(1);
            framebuffers = new VulkanFrameBuffer[views.length];
            for (int i = 0; i < views.length; i++) {
                attachments.put(0, views[i].getImage());
                framebuffers[i] = new VulkanFrameBuffer(device, extent.width(),
                        extent.height(), attachments, renderPass.getPass());
            }
            commandbuffers = new CommandBuffer[views.length];
            fences = new Fence[views.length];
            for (int i = 0; i < views.length; i++) {
                commandbuffers[i] = new CommandBuffer(pool, true, false);
                fences[i] = new Fence(device, true);
                recordCommandBuffer(stack, commandbuffers[i], framebuffers[i], extent.width(), extent.height());
            }
        }
    }
    
    private void recordCommandBuffer(MemoryStack stack, CommandBuffer cb, VulkanFrameBuffer fb, int width, int height) {
        var clearVals = VkClearValue.calloc(1, stack);
        clearVals.apply(0, v -> v.color().float32(0, 0.5f).float32(1, 0.7f).float32(0, 0.9f).float32(3, 1.0f));
        var passInfo = VkRenderPassBeginInfo.calloc(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .renderPass(renderPass.getPass())
                .pClearValues(clearVals)
                .renderArea(a -> a.extent().set(width, height))
                .framebuffer(fb.getFrameBuffer());
        cb.beginRecording();
        VK10.vkCmdBeginRenderPass(cb.getBuffer(), passInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
        VK10.vkCmdEndRenderPass(cb.getBuffer());
        cb.endRecording();
    }
    
    public void submit(CommandQueue queue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int i = swapchain.getCurrentFrame();
            var cb = commandbuffers[i];
            var fence = fences[i];
            fence.reset();
            SwapChain.SyncSemaphores s = swapchain.getSemaphores()[i];
            queue.submit(stack.pointers(cb.getBuffer()), stack.longs(s.imgAcquisition().getSemaphore()),
                    stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                    stack.longs(s.renderComplete().getSemaphore()), fence);
        }
    }
    public void waitForFence() {
        fences[swapchain.getCurrentFrame()].fenceWait();
    }
    
    public void cleanup() {
        for (var fb : framebuffers) {
            fb.cleanup();
        }
        renderPass.cleanup();
        for (var cb : commandbuffers) {
            cb.cleanup();
        }
        for (var f : fences) {
            f.cleanup();
        }
    }
    
}
