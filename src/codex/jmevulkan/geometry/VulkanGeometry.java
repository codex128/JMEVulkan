/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.geometry;

import codex.jmevulkan.LogicalDevice;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import java.util.HashMap;

/**
 *
 * @author codex
 */
public class VulkanGeometry {
    
    private final LogicalDevice device;
    private final Geometry geometry;
    private final HashMap<VertexBuffer.Type, VertexTransferChannel> vertexChannels = new HashMap<>();
    
    public VulkanGeometry(LogicalDevice device, Geometry geometry) {
        this.device = device;
        this.geometry = geometry;
    }
    
    public void update() {
        var mesh = geometry.getMesh();
        if (mesh == null) {
            throw new NullPointerException("Geometry mesh cannot be null.");
        }
        for (var vb : mesh.getBufferList()) {
            var channel = vertexChannels.get(vb.getBufferType());
            if (channel == null) {
                channel = new VertexTransferChannel(vb.getBufferType(), device);
                vertexChannels.put(vb.getBufferType(), channel);
            }
            if (channel.isDataUpdateNeeded()) {
                channel.updateBuffers(vb);
            }
        }
    }
    
}
