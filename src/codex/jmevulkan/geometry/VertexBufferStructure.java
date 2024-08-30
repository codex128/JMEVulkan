/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.geometry;

import com.jme3.scene.VertexBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

/**
 *
 * @author codex
 */
public class VertexBufferStructure extends VertexInputState {
    
    private static final int NUM_ATTRIBUTES = 1;
    private static final int POSITION_COMPONENTS = 3;
    
    private final VkVertexInputAttributeDescription.Buffer attribute;
    private final VkVertexInputBindingDescription.Buffer binding;
    
    public VertexBufferStructure() {
        attribute = VkVertexInputAttributeDescription.calloc(NUM_ATTRIBUTES);
        binding = VkVertexInputBindingDescription.calloc(1);
        inputState = VkPipelineVertexInputStateCreateInfo.calloc();
        int i = 0;
        attribute.get(i)
                .binding(0)
                .location(i)
                .format(VK10.VK_FORMAT_R32G32B32_SFLOAT)
                .offset(0);
        binding.get(0)
                .binding(0)
                .stride(POSITION_COMPONENTS * VertexBuffer.Format.Float.getComponentSize())
                .inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
        inputState.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexAttributeDescriptions(attribute)
                .pVertexBindingDescriptions(binding);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        attribute.free();
        binding.free();
    }
    
}
