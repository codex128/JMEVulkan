/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

/**
 *
 * @author codex
 */
public class CommandQueue {
    
    private final VkQueue queue;
    private final int queueFamilyIndex;
    
    public CommandQueue(LogicalDevice device, int queueFamilyIndex, int queueIndex) {
        this.queueFamilyIndex = queueFamilyIndex;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //var queueInfo = VkDeviceQueueCreateInfo.calloc(stack);
            var pBuf = stack.mallocPointer(1);
            System.out.println(queueFamilyIndex+" "+queueIndex);
            VK10.vkGetDeviceQueue(device.getLogicalDevice(), queueFamilyIndex, queueIndex, pBuf);
            long pQueue = pBuf.get(0);
            if (pQueue == NULL) {
                throw new NullPointerException("Failed to find device queue.");
            }
            queue = new VkQueue(pQueue, device.getLogicalDevice());
        }
    }
    
    public void submit(PointerBuffer commandBufs, LongBuffer waitSemaphores,
            IntBuffer stageMasks, LongBuffer signalSemaphores, Fence fence) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var submitInfo = VkSubmitInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pCommandBuffers(commandBufs)
                    .pSignalSemaphores(signalSemaphores);
            if (waitSemaphores != null) {
                submitInfo.waitSemaphoreCount(waitSemaphores.limit())
                        .pWaitSemaphores(waitSemaphores)
                        .pWaitDstStageMask(stageMasks);
            } else {
                submitInfo.waitSemaphoreCount(0);
            }
            long fHandle = fence != null ? fence.getFence() : VK10.VK_NULL_HANDLE;
            System.out.println(queue+" "+submitInfo+" "+fHandle+" "+VK10.VK_NULL_HANDLE);
            check(VK10.vkQueueSubmit(queue, submitInfo, fHandle), "Failed to submit command to queue");
        }
    }
    public void waitIdle() {
        VK10.vkQueueWaitIdle(queue);
    }
    
    public VkQueue getQueue() {
        return queue;
    }
    public int getQueueFamilyIndex() {
        return queueFamilyIndex;
    }
    
}
