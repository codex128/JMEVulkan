/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.app;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkLayerProperties;

/**
 *
 * @author codex
 */
public class VulkanSettings {
    
    public static final VulkanSettings DEFAULT = new VulkanSettings();
    
    private final LinkedList<String> requestedValidationLayers = new LinkedList<>();
    private String preferredDevice;
    
    public void requestValidationLayer(String layer) {
        requestedValidationLayers.add(layer);
    }
    
    public List<String> getSupportedValidationLayers() {
        if (requestedValidationLayers.isEmpty()) {
            return new LinkedList<>();
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer layerBuf = stack.callocInt(1);
            vkEnumerateInstanceLayerProperties(layerBuf, null);
            int numLayers = layerBuf.get(0);
            VkLayerProperties.Buffer propsBuf = VkLayerProperties.calloc(numLayers, stack);
            vkEnumerateInstanceLayerProperties(layerBuf, propsBuf);
            LinkedList<String> layers = new LinkedList<>();
            for (int i = 0; i < numLayers; i++) {
                VkLayerProperties props = propsBuf.get(i);
                layers.add(props.layerNameString());
            }
            // todo: add fallback validation layers
            return requestedValidationLayers.stream().filter(layers::contains).toList();
        }
    }
    public Set<String> getInstanceExtensions() {
        Set<String> extensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtBuf = stack.callocInt(1);
            vkEnumerateInstanceExtensionProperties((String)null, numExtBuf, null);
            int numExt = numExtBuf.get(0);
            VkExtensionProperties.Buffer extProps = VkExtensionProperties.calloc(VK_TRUE, stack);
            vkEnumerateInstanceExtensionProperties((String)null, numExtBuf, extProps);
            for (int i = 0; i < numExt; i++) {
                extensions.add(extProps.get(i).extensionNameString());
            }
        }
        return extensions;
    }
    public String getPreferredDevice() {
        return preferredDevice;
    }
    
}
