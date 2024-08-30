/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.geometry;

import codex.jmevulkan.CommandBuffer;
import codex.jmevulkan.LogicalDevice;
import com.jme3.scene.VertexBuffer;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;

/**
 * Transfers a type of vertex data to vulkan space.
 * 
 * @author codex
 */
public class VertexTransferChannel {
    
    private final VertexBuffer.Type type;
    private final LogicalDevice device;
    private VulkanVertexBuffer source, target;
    private boolean dataUpdateFlag = true;
    
    public VertexTransferChannel(VertexBuffer.Type type, LogicalDevice device) {
        this.type = type;
        this.device = device;
    }
    
    public void updateBuffers(VertexBuffer vb) {
        if (vb.getBufferType() != type) {
            throw new IllegalArgumentException("Expected "+type+" vertex buffer,"
                    + " got "+vb.getBufferType()+" vertex buffer instead.");
        }
        int size = vb.getData().capacity() * vb.getFormat().getComponentSize();
        if (source == null || source.getRequestedSize() < size) {
            if (source != null) {
                source.cleanup();
                target.cleanup();
            }
            source = new VulkanVertexBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            target = new VulkanVertexBuffer(device, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                    | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        }
        fillSourceBuffer(vb, source.map(), (int)source.getRequestedSize());
        source.unmap();
        dataUpdateFlag = false;
    }
    
    public void recordTransfer(CommandBuffer command) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var copyRegion = VkBufferCopy.calloc(1, stack)
                    .srcOffset(0)
                    .dstOffset(0)
                    .size(source.getRequestedSize());
            VK10.vkCmdCopyBuffer(command.getBuffer(), source.getBuffer(), target.getBuffer(), copyRegion);
        }
    }
    
    public void setDataUpdateNeeded() {
        dataUpdateFlag = true;
    }
    
    public boolean isDataUpdateNeeded() {
        return dataUpdateFlag;
    }
    
    private static Buffer fillSourceBuffer(VertexBuffer vb, long address, int size) {
        return switch (vb.getFormat()) {
            case Int -> MemoryUtil.memIntBuffer(address, size).put((IntBuffer)vb.getData());
            case Float -> MemoryUtil.memFloatBuffer(address, size).put((FloatBuffer)vb.getData());
            default -> throw new UnsupportedOperationException("Vertex buffer format "+vb.getFormat()+" is not yet supported.");
        };
    }
    
}
