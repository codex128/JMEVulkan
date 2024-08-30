/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.pipeline.RenderPipeline;

/**
 *
 * @author codex
 */
public class VulkanPipeline implements RenderPipeline<VulkanContext> {

    private boolean rendered = false;
    
    @Override
    public VulkanContext fetchPipelineContext(RenderManager rm) {
        return rm.getContext(VulkanContext.class);
    }
    @Override
    public boolean hasRenderedThisFrame() {
        return rendered;
    }
    @Override
    public void startRenderFrame(RenderManager rm) {}
    @Override
    public void pipelineRender(RenderManager rm, VulkanContext context, ViewPort vp, float tpf) {
        context.getRenderer().renderViewPort(vp);
        rendered = true;
    }
    @Override
    public void endRenderFrame(RenderManager rm) {
        rendered = false;
    }
    
}
