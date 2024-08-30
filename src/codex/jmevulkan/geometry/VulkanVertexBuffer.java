/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.geometry;

import codex.jmevulkan.LogicalDevice;
import codex.jmevulkan.utils.VulkanUtils;
import static codex.jmevulkan.utils.VulkanUtils.check;
import com.jme3.scene.VertexBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

/**
 *
 * @author codex
 */
public class VulkanVertexBuffer {
    
    private final LogicalDevice device;
    private final long requestedSize;
    private final long buffer;
    private final long allocationSize;
    private final long memory;
    private final PointerBuffer memPointer;
    private long mappedMemory = NULL;
    
    public VulkanVertexBuffer(LogicalDevice device, int size, int usage, int reqMask) {
        this.device = device;
        this.requestedSize = size;
        var vkDev = device.getLogicalDevice();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var bufferInfo = VkBufferCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(requestedSize)
                    .usage(usage)
                    .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
            var lBuf = stack.mallocLong(1);
            check(VK10.vkCreateBuffer(vkDev, bufferInfo, null, lBuf), "Failed to create buffer");
            buffer = lBuf.get(0);
            var memReqs = VkMemoryRequirements.malloc(stack);
            VK10.vkGetBufferMemoryRequirements(vkDev, buffer, memReqs);
            var memAlloc = VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memReqs.size())
                    .memoryTypeIndex(VulkanUtils.memoryTypeFromProperties(
                            device.getPhysicalDevice(), memReqs.memoryTypeBits(), reqMask));
            check(VK10.vkAllocateMemory(vkDev, memAlloc, null, lBuf), "Failed to allocate memory");
            allocationSize = memAlloc.allocationSize();
            memory = lBuf.get(0);
            memPointer = MemoryUtil.memAllocPointer(1);
            check(VK10.vkBindBufferMemory(vkDev, buffer, memory, 0), "Failed to bind buffer memory");
        }
    }
    
    public long map() {
        if (mappedMemory == NULL) {
            check(VK10.vkMapMemory(device.getLogicalDevice(), memory, 0, allocationSize, 0, memPointer),
                    "Failed to map buffer");
            mappedMemory = memPointer.get(0);
        }
        return mappedMemory;
    }
    public void unmap() {
        if (mappedMemory != NULL) {
            VK10.vkUnmapMemory(device.getLogicalDevice(), memory);
            mappedMemory = NULL;
        }
    }
    
    public void cleanup() {
        MemoryUtil.memFree(memPointer);
        VK10.vkDestroyBuffer(device.getLogicalDevice(), buffer, null);
        VK10.vkFreeMemory(device.getLogicalDevice(), memory, null);
    }
    
    public long getRequestedSize() {
        return requestedSize;
    }
    public long getBuffer() {
        return buffer;
    }
    
}
