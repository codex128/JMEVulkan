/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.check;
import java.util.Arrays;
import java.util.LinkedList;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

/**
 *
 * @author codex
 */
public class SwapChain {
    
    private final LogicalDevice device;
    private final ImageView[] views;
    private final SurfaceFormat format;
    private final VkExtent2D extent;
    private final SyncSemaphores[] semaphores;
    private final long swapchain;
    private int currentFrame = 0;
    
    public SwapChain(VulkanContext context, LogicalDevice device, Surface surface, int requestedImages,
            boolean vsync, PresentQueue presentQueue, CommandQueue[] concurrentQueues) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDevice phys = device.getPhysicalDevice().getDevice();
            var surfCaps = VkSurfaceCapabilitiesKHR.calloc(stack);
            check(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(phys, surface.getSurface(), surfCaps),
                    "Failed to get surface capabilities");
            int numImages = calcNumImages(surfCaps, requestedImages);
            format = calcSurfaceFormat(stack, device.getPhysicalDevice(), surface);
            extent = calcSwapChainExtent(context, surfCaps);
            var chainInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface.getSurface())
                    .minImageCount(numImages)
                    .imageFormat(format.format)
                    .imageColorSpace(format.colorSpace)
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    .imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(surfCaps.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .clipped(true);
            if (vsync) {
                chainInfo.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
            } else {
                chainInfo.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
            }
            int numQueues = concurrentQueues != null ? concurrentQueues.length : 0;
            var indices = new LinkedList<Integer>();
            for (int i = 0; i < numQueues; i++) {
                var q = concurrentQueues[i];
                if (q.getQueueFamilyIndex() != presentQueue.getQueueFamilyIndex()) {
                    indices.add(q.getQueueFamilyIndex());
                }
            }
            if (!indices.isEmpty()) {
                var iBuf = stack.mallocInt(indices.size() + 1);
                indices.forEach(i -> iBuf.put(i));
                iBuf.put(presentQueue.getQueueFamilyIndex()).flip();
                chainInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT)
                        .queueFamilyIndexCount(iBuf.capacity())
                        .pQueueFamilyIndices(iBuf);
            } else {
                chainInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
            }
            semaphores = new SyncSemaphores[numImages];
            Arrays.setAll(semaphores, i -> new SyncSemaphores(device));
            var chainBuf = stack.mallocLong(1);
            check(KHRSwapchain.vkCreateSwapchainKHR(device.getLogicalDevice(), chainInfo, null, chainBuf),
                    "Failed to create swapchain");
            swapchain = chainBuf.get(0);
            views = createImageViews(stack, device, swapchain, format.format);
        }
    }
    
    private int calcNumImages(VkSurfaceCapabilitiesKHR surfCaps, int requestedImages) {
        int max = surfCaps.maxImageCount();
        if (max != 0) {
            return Math.min(requestedImages, max);
        } else {
            return Math.max(requestedImages, surfCaps.minImageCount());
        }
    }
    private SurfaceFormat calcSurfaceFormat(MemoryStack stack, PhysicalDevice phys, Surface surface) {
        var intBuf = stack.mallocInt(1);
        check(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(phys.getDevice(), surface.getSurface(), intBuf, null),
                "Failed to get number of surface formats");
        int numFormats = intBuf.get(0);
        if (numFormats <= 0) {
            throw new RuntimeException("No surface formats found.");
        }
        var formatBuf = VkSurfaceFormatKHR.calloc(numFormats, stack);
        check(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(phys.getDevice(), surface.getSurface(), intBuf, formatBuf),
                "Failed to get surface formats");
        int imgFormat = VK10.VK_FORMAT_B8G8R8A8_SRGB;
        int colorSpace = formatBuf.get(0).colorSpace();
        formatBuf.rewind();
        while (formatBuf.hasRemaining()) {
            var f = formatBuf.get();
            if (f.format() == VK10.VK_FORMAT_B8G8R8_SRGB
                    && f.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                imgFormat = f.format();
                colorSpace = f.colorSpace();
                break;
            }
        }
        return new SurfaceFormat(imgFormat, colorSpace);
    }
    private VkExtent2D calcSwapChainExtent(VulkanContext context, VkSurfaceCapabilitiesKHR surfCaps) {
        var result = VkExtent2D.calloc();
        if (surfCaps.currentExtent().width() == 0xFFFFFFFF) {
            int width = Math.min(context.getWindowWidth(), surfCaps.maxImageExtent().width());
            width = Math.max(width, surfCaps.minImageExtent().width());
            int height = Math.min(context.getWindowHeight(), surfCaps.maxImageExtent().height());
            height = Math.max(height, surfCaps.minImageExtent().height());
            result.width(width);
            result.height(height);
        } else {
            result.set(surfCaps.currentExtent());
        }
        return result;
    }
    private ImageView[] createImageViews(MemoryStack stack, LogicalDevice device, long swapchain, int format) {
        var intBuf = stack.mallocInt(1);
        check(KHRSwapchain.vkGetSwapchainImagesKHR(device.getLogicalDevice(), swapchain, intBuf, null),
                "Failed to get number of swapchain images");
        var images = stack.mallocLong(intBuf.get(0));
        check(KHRSwapchain.vkGetSwapchainImagesKHR(device.getLogicalDevice(), swapchain, intBuf, images),
                "Failed to get swapchain images");
        images.rewind();
        var result = new ImageView[images.remaining()];
        var data = ImageView.createData().format(format).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
        for (int i = 0; images.hasRemaining(); i++) {
            result[i] = new ImageView(device, images.get(), data);
        }
        return result;
    }
    
    public int acquireNextImage() {
        long timeout = 1000000000L;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var iBuf = stack.mallocInt(1);
            int err = KHRSwapchain.vkAcquireNextImageKHR(device.getLogicalDevice(), swapchain, timeout,
                    semaphores[currentFrame].imgAcquisition.getSemaphore(), MemoryUtil.NULL, iBuf);
            if (err == VK10.VK_TIMEOUT) {
                throw new RuntimeException("Image acquisition timed out on image "+currentFrame);
            } else if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                // window has been resized
                return -1;
            } else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                // image not optimal but can be used
            } else if (err != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to acquire image: " + err);
            }
            return iBuf.get(0);
        }
    }
    public boolean presentImage(CommandQueue queue, int imageIndex) {
        boolean resize = false;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var present = VkPresentInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(semaphores[currentFrame].renderComplete.getSemaphore()))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(swapchain))
                    .pImageIndices(stack.ints(imageIndex));
            int err = KHRSwapchain.vkQueuePresentKHR(queue.getQueue(), present);
            if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                // need to resize window
                resize = true;
            } else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                // not optimal but can still be used
            } else if (err != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to present KHR: " + err);
            }
        }
        if (++currentFrame >= views.length) {
            currentFrame = 0;
        }
        return resize;
    }
    
    public void cleanup() {
        extent.free();
        for (ImageView v : views) {
            v.cleanup();
        }
        for (SyncSemaphores s : semaphores) {
            s.cleanup();
        }
        KHRSwapchain.vkDestroySwapchainKHR(device.getLogicalDevice(), swapchain, null);
    }
    
    public LogicalDevice getDevice() {
        return device;
    }
    public VkExtent2D getExtent() {
        return extent;
    }
    public ImageView[] getViews() {
        return views;
    }
    public SyncSemaphores[] getSemaphores() {
        return semaphores;
    }
    public int getImageFormat() {
        return format.format;
    }
    public int getImageColorSpace() {
        return format.colorSpace;
    }
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    private record SurfaceFormat(int format, int colorSpace) {}
    public record SyncSemaphores(Semaphore imgAcquisition, Semaphore renderComplete) {
        
        public SyncSemaphores(LogicalDevice device) {
            this(new Semaphore(device), new Semaphore(device));
        }
        
        public void cleanup() {
            imgAcquisition.cleanup();
            renderComplete.cleanup();
        }
        
    }
    
}
