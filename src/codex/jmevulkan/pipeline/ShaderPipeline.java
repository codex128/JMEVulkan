/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.jmevulkan.pipeline;

import codex.jmevulkan.LogicalDevice;
import static codex.jmevulkan.utils.VulkanUtils.check;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;

/**
 *
 * @author codex
 */
public class ShaderPipeline {
    
    private final LogicalDevice device;
    private final long layout;
    private final long pipeline;
    
    public ShaderPipeline(PipelineCache cache, ShaderPipelineData data) {
        this.device = cache.getDevice();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var lBuf = stack.mallocLong(1);
            var name = stack.UTF8("main");
            var shaders = data.getProgram().getShaders();
            var stageInfo = VkPipelineShaderStageCreateInfo.calloc(shaders.length, stack);
            for (int i = 0; i < shaders.length; i++) {
                stageInfo.get(i)
                        .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                        .stage(shaders[i].getStage())
                        .module(shaders[i].getShader())
                        .pName(name);
            }
            var assemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            var viewportInfo = VkPipelineViewportStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .viewportCount(1)
                    .scissorCount(1);
            var rasterInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .polygonMode(VK10.VK_POLYGON_MODE_FILL)
                    .cullMode(VK10.VK_CULL_MODE_NONE)
                    .frontFace(VK10.VK_FRONT_FACE_CLOCKWISE)
                    .lineWidth(1.0f);
            var multisampleInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
            var blendState = VkPipelineColorBlendAttachmentState.calloc(data.getNumColorAttachments(), stack);
            for (int i = 0; i < data.getNumColorAttachments(); i++) {
                blendState.get(i).colorWriteMask(
                        VK10.VK_COLOR_COMPONENT_R_BIT
                      | VK10.VK_COLOR_COMPONENT_G_BIT
                      | VK10.VK_COLOR_COMPONENT_B_BIT
                      | VK10.VK_COLOR_COMPONENT_A_BIT);
            }
            var colorBlend = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .pAttachments(blendState);
            var dynamicInfo = VkPipelineDynamicStateCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT, VK10.VK_DYNAMIC_STATE_SCISSOR));
            var layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            check(VK10.vkCreatePipelineLayout(device.getLogicalDevice(), layoutInfo, null, lBuf),
                    "Failed to create pipeline layout");
            layout = lBuf.get(0);
            var pipelineBuf = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(stageInfo)
                    .pVertexInputState(data.getVertexState().getInputState())
                    .pInputAssemblyState(assemblyInfo)
                    .pViewportState(viewportInfo)
                    .pRasterizationState(rasterInfo)
                    .pMultisampleState(multisampleInfo)
                    .pColorBlendState(colorBlend)
                    .pDynamicState(dynamicInfo)
                    .layout(layout)
                    .renderPass(data.getRenderPass());
            check(VK10.vkCreateGraphicsPipelines(device.getLogicalDevice(), cache.getCache(), pipelineBuf, null, lBuf),
                    "Failed to create graphics pipeline(s)");
            pipeline = lBuf.get(0);
        }
    }
    
    public void cleanup() {
        VK10.vkDestroyPipelineLayout(device.getLogicalDevice(), layout, null);
        VK10.vkDestroyPipeline(device.getLogicalDevice(), pipeline, null);
    }
    
    public long getPipeline() {
        return pipeline;
    }
    
    public long getLayout() {
        return layout;
    }
    
}
