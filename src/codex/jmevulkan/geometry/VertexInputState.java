/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.geometry;

import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

/**
 *
 * @author codex
 */
public abstract class VertexInputState {
    
    protected VkPipelineVertexInputStateCreateInfo inputState;
    
    public VertexInputState() {}
    
    public void cleanup() {
        inputState.free();
    }
    
    public VkPipelineVertexInputStateCreateInfo getInputState() {
        return inputState;
    }
    
}
