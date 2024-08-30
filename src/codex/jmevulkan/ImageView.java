/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import static codex.jmevulkan.utils.VulkanUtils.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

/**
 *
 * @author codex
 */
public class ImageView {
    
    private final LogicalDevice device;
    private final long image;
    private final ImageViewData data;
    
    public ImageView(LogicalDevice device, long imageId, ImageViewData data) {
        this.device = device;
        this.data = data;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var imgBuf = stack.mallocLong(1);
            var viewInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(imageId)
                    .viewType(data.viewType)
                    .format(data.format)
                    .subresourceRange(this::setupSubresourceRange);
            check(VK10.vkCreateImageView(device.getLogicalDevice(), viewInfo, null, imgBuf),
                    "Failed to create image view.");
            image = imgBuf.get(0);
        }
    }
    
    private void setupSubresourceRange(VkImageSubresourceRange sub) {
        sub.aspectMask(data.aspectMask)
           .baseMipLevel(data.mipLevels)
           .baseArrayLayer(data.baseArrayLayer)
           .layerCount(data.layerCount);
    }
    
    public void cleanup() {
        VK10.vkDestroyImageView(device.getLogicalDevice(), image, null);
    }
    
    public long getImage() {
        return image;
    }
    
    public static ImageViewData createData() {
        return new ImageViewData();
    }
    public static class ImageViewData {
        
        private int aspectMask;
        private int baseArrayLayer = 0;
        private int format;
        private int layerCount = 1;
        private int mipLevels = 1;
        private int viewType = VK10.VK_IMAGE_VIEW_TYPE_2D;
        
        public ImageViewData() {}
        
        public ImageViewData aspectMask(int aspectMask) {
            this.aspectMask = aspectMask;
            return this;
        }
        
        public ImageViewData baseArrayLayer(int baseArrayLayer) {
            this.baseArrayLayer = baseArrayLayer;
            return this;
        }
        
        public ImageViewData format(int format) {
            this.format = format;
            return this;
        }
        
        public ImageViewData layerCount(int layerCount) {
            this.layerCount = layerCount;
            return this;
        }
        
        public ImageViewData mipLevels(int mipLevels) {
            this.mipLevels = mipLevels;
            return this;
        }
        
        public ImageViewData viewType(int viewType) {
            this.viewType = viewType;
            return this;
        }
        
    }
    
}
