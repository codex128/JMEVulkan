/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.pipeline;

import codex.jmevulkan.geometry.VertexInputState;

/**
 *
 * @author codex
 */
public class ShaderPipelineData {
    
    private final long renderPass;
    private final ShaderProgram program;
    private final int numColorAttachments;
    private final VertexInputState vertexState;

    public ShaderPipelineData(long renderPass, ShaderProgram program, int numColorAttachments, VertexInputState vertexState) {
        this.renderPass = renderPass;
        this.program = program;
        this.numColorAttachments = numColorAttachments;
        this.vertexState = vertexState;
    }

    public void cleanup() {
        
    }
    
    public long getRenderPass() {
        return renderPass;
    }
    public ShaderProgram getProgram() {
        return program;
    }
    public int getNumColorAttachments() {
        return numColorAttachments;
    }
    public VertexInputState getVertexState() {
        return vertexState;
    }
    
}
