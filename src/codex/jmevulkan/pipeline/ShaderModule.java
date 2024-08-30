/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.pipeline;

/**
 *
 * @author codex
 */
public class ShaderModule {
    
    private final int stage;
    private final long shader;

    public ShaderModule(int stage, long handle) {
        this.stage = stage;
        this.shader = handle;
    }

    public int getStage() {
        return stage;
    }
    public long getShader() {
        return shader;
    }
    
}
