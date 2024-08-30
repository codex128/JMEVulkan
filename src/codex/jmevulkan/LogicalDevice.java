/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.*;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

/**
 *
 * @author codex
 */
public class LogicalDevice {
    
    private final PhysicalDevice physDevice;
    private final VkDevice device;
    
    public LogicalDevice(PhysicalDevice physDevice) {
        this.physDevice = physDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // todo: use extensions
            var extensions = getDeviceExtensions(stack);
            // todo: enable macos portability extension
            var reqExt = stack.mallocPointer(1);
            reqExt.put(stack.ASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
            reqExt.flip();
            var features = VkPhysicalDeviceFeatures.calloc(stack);
            var familyProps = physDevice.getFamilyProps();
            familyProps.rewind();
            var queueInfo = VkDeviceQueueCreateInfo.calloc(familyProps.remaining(), stack);
            queueInfo.rewind();
            int i = 0;
            while (familyProps.hasRemaining() && queueInfo.hasRemaining()) {
                var priorities = stack.callocFloat(familyProps.get().queueCount());
                System.out.println("add queue at family index: "+i);
                queueInfo.get().sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(i++)
                        .pQueuePriorities(priorities)
                        .flags(0);
            }
            familyProps.rewind();
            queueInfo.rewind();
            var deviceInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .ppEnabledExtensionNames(reqExt)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueInfo);
            var pBuf = stack.mallocPointer(1);
            check(VK10.vkCreateDevice(physDevice.getDevice(), deviceInfo, null, pBuf), "Failed to create device");
            device = new VkDevice(pBuf.get(0), physDevice.getDevice(), deviceInfo);
        }
    }
    
    private Set<String> getDeviceExtensions(MemoryStack stack) {
        var extensions = new HashSet<String>();
        var numExt = stack.callocInt(1);
        VK10.vkEnumerateDeviceExtensionProperties(physDevice.getDevice(), (String)null, numExt, null);
        var propsBuf = VkExtensionProperties.calloc(numExt.get(0), stack);
        VK10.vkEnumerateDeviceExtensionProperties(physDevice.getDevice(), (String)null, numExt, propsBuf);
        propsBuf.rewind();
        while (propsBuf.hasRemaining()) {
            extensions.add(propsBuf.get().extensionNameString());
        }
        return extensions;
    }
    
    public void cleanup() {
        VK10.vkDestroyDevice(device, null);
    }
    
    public PhysicalDevice getPhysicalDevice() {
        return physDevice;
    }
    
    public VkDevice getLogicalDevice() {
        return device;
    }
    
    public void waitIdle() {
        VK10.vkDeviceWaitIdle(device);
    }
    
}
