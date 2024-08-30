/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan;

import codex.jmevulkan.app.SimpleVulkanApplication;
import com.jme3.system.AppSettings;

/**
 *
 * @author codex
 */
public class Main extends SimpleVulkanApplication {
    
    public static void main(String[] args) {
        Main app = new Main();
        var settings = new AppSettings(true);
        settings.setWidth(768);
        settings.setWidth(768);
        app.setSettings(settings);
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        
        renderManager.registerContext(VulkanContext.class, new VulkanContext(this).start());
        renderManager.setPipeline(new VulkanPipeline());
        
    }
    
}
