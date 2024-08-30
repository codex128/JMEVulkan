/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.pipeline;

import codex.jmevulkan.LogicalDevice;
import static codex.jmevulkan.utils.VulkanUtils.check;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

/**
 *
 * @author codex
 */
public class ShaderProgram {
    
    private final LogicalDevice device;
    private final ShaderModule[] shaders;
    
    public ShaderProgram(LogicalDevice device, ShaderData[] shaderData) {
        try {
            this.device = device;
            int numShaders = shaderData != null ? shaderData.length : 0;
            shaders = new ShaderModule[numShaders];
            for (int i = 0; i < numShaders; i++) {
                // eventually, the asset manager will need to be used
                byte[] contents = Files.readAllBytes(new File(shaderData[i].getAsset()).toPath());
                long handle = createShader(contents);
                shaders[i] = new ShaderModule(shaderData[i].getStage(), handle);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading shader byte code.", ex);
        }
    }
    
    private long createShader(byte[] code) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var bytecode = stack.malloc(code.length).put(0, code);
            var shaderInfo = VkShaderModuleCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(bytecode);
            var lBuf = stack.mallocLong(1);
            check(VK10.vkCreateShaderModule(device.getLogicalDevice(), shaderInfo, null, lBuf),
                    "Failed to create shader");
            return lBuf.get(0);
        }
    }
    
    public ShaderModule[] getShaders() {
        return shaders;
    }
    
    public void cleanup() {
        for (var s : shaders) {
            VK10.vkDestroyShaderModule(device.getLogicalDevice(), s.getShader(), null);
        }
    }
    
}
