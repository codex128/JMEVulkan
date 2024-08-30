/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.*;
import java.nio.IntBuffer;
import java.util.LinkedList;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

/**
 *
 * @author codex
 */
public class PhysicalDevice {
    
    private VkPhysicalDevice device;
    private final VkExtensionProperties.Buffer extensions;
    private final VkPhysicalDeviceMemoryProperties memory;
    private final VkPhysicalDeviceFeatures features;
    private final VkPhysicalDeviceProperties properties;
    private final VkQueueFamilyProperties.Buffer familyProps;
    
    private PhysicalDevice(MemoryStack stack, VkPhysicalDevice device) {
        this.device = device;
        var intBuf = stack.mallocInt(1);
        properties = VkPhysicalDeviceProperties.calloc();
        VK10.vkGetPhysicalDeviceProperties(device, properties);
        check(VK10.vkEnumerateDeviceExtensionProperties(device, (String)null, intBuf, null),
                "Failed to fetch number of device extension properties");
        extensions = VkExtensionProperties.calloc(intBuf.get(0));
        check(VK10.vkEnumerateDeviceExtensionProperties(device, (String)null, intBuf, extensions),
                "Failed to fetch device extension properties");
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, intBuf, null);
        familyProps = VkQueueFamilyProperties.calloc(intBuf.get(0));
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, intBuf, familyProps);
        features = VkPhysicalDeviceFeatures.calloc();
        VK10.vkGetPhysicalDeviceFeatures(device, features);
        memory = VkPhysicalDeviceMemoryProperties.calloc();
        VK10.vkGetPhysicalDeviceMemoryProperties(device, memory);
    }
    
    public void cleanup() {
        memory.free();
        features.free();
        familyProps.free();
        extensions.free();
        properties.free();
    }
    
    public boolean hasKHRSwapChainExtension() {
        extensions.rewind();
        while (extensions.hasRemaining()) {
            String name = extensions.get().extensionNameString();
            if (KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME.equals(name)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasGraphicsQueueFamily() {
        familyProps.rewind();
        while (familyProps.hasRemaining()) {
            var props = familyProps.get();
            if ((props.queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
                return true;
            }
        }
        return false;
    }
    
    public String getName() {
        return properties.deviceNameString();
    }
    public VkPhysicalDevice getDevice() {
        return device;
    }
    public VkExtensionProperties.Buffer getExtensions() {
        return extensions;
    }
    public VkPhysicalDeviceMemoryProperties getMemory() {
        return memory;
    }
    public VkPhysicalDeviceFeatures getFeatures() {
        return features;
    }
    public VkPhysicalDeviceProperties getProperties() {
        return properties;
    }
    public VkQueueFamilyProperties.Buffer getFamilyProps() {
        return familyProps;
    }
    
    public static PhysicalDevice createDevice(VkInstance instance, String preferredDeviceName) {
        PhysicalDevice selected = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var devices = getPhysicalDevices(instance, stack);
            if (!devices.hasRemaining()) {
                throw new RuntimeException("No physical devices found.");
            }
            var usableDevices = new LinkedList<PhysicalDevice>();
            devices.rewind();
            while (devices.hasRemaining()) {
                VkPhysicalDevice vkDevice = new VkPhysicalDevice(devices.get(), instance);
                PhysicalDevice d = new PhysicalDevice(stack, vkDevice);
                if (d.hasGraphicsQueueFamily() && d.hasKHRSwapChainExtension()) {
                    if (preferredDeviceName != null && preferredDeviceName.equals(d.getName())) {
                        selected = d;
                    }
                    usableDevices.add(d);
                } else {
                    d.cleanup();
                }
            }
            if (selected == null && !usableDevices.isEmpty()) {
                selected = usableDevices.pollFirst();
            }
            return selected;
        }
    }
    
    protected static PointerBuffer getPhysicalDevices(VkInstance instance, MemoryStack stack) {
        IntBuffer intBuffer = stack.mallocInt(1);
        check(VK10.vkEnumeratePhysicalDevices(instance, intBuffer, null),
                "Failed to get number of physical devices");
        int numDevices = intBuffer.get(0);
        PointerBuffer pDevices = stack.mallocPointer(numDevices);
        check(VK10.vkEnumeratePhysicalDevices(instance, intBuffer, pDevices),
                "Failed to get physical devices");
        return pDevices;
    }
    
}
