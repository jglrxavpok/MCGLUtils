package org.jglrxavpok.shady;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jglrxavpok.shady.cpalette.DefaultColorPalettes;
import org.jglrxavpok.shady.gui.GuiIconButton;
import org.jglrxavpok.shady.gui.GuiShadyOptions;
import org.jglrxavpok.shady.shaders.PassRegistry;
import org.jglrxavpok.shady.shaders.ShaderBatch;
import org.jglrxavpok.shady.shaders.ShaderPass;
import org.jglrxavpok.shady.shaders.passes.LowResPass;
import org.jglrxavpok.shady.shaders.passes.NotchPass;
import org.jglrxavpok.shady.shaders.passes.PhosphorPass;
import org.jglrxavpok.shady.shaders.passes.VanillaPass;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;

@Mod(modid = ShadyMod.MODID, version = ShadyMod.VERSION, name = "Shady")
public class ShadyMod
{
    public static final String MODID   = "shady";
    public static final String VERSION = "Pre-0.1";

    @Instance(MODID)
    public static ShadyMod     instance;
    private ShadyResManager    resManager;
    private Configuration      config;
    private String             paletteID;
    private boolean            enabled;
    private ShaderBatch        batch;
    private File               lastBatchFile;
    public File                batchesFolder;

    public ShadyResManager getResourceManager()
    {
        return resManager;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if(event.getSide().isServer())
        {
            throw new IllegalStateException("The Shady mod can't be loaded on servers");
        }

        resManager = new ShadyResManager(Minecraft.getMinecraft().getResourceManager());
        MinecraftForge.EVENT_BUS.register(this);
        config = new Configuration(event.getSuggestedConfigurationFile());
        enabled = config.getBoolean("enabled", Configuration.CATEGORY_GENERAL, true, "Are shaders enabled?");
        paletteID = config.getString("palette", Configuration.CATEGORY_GENERAL, "none", "The id of the palette");
        config.save();

        batchesFolder = new File(Minecraft.getMinecraft().mcDataDir, "batches");
        if(!batchesFolder.exists())
            batchesFolder.mkdirs();
        lastBatchFile = new File(batchesFolder, "current.batch");

        DefaultColorPalettes.init();
        registerVanillaPasses();
        PassRegistry.register("lowres", new LowResPass());
    }

    private void registerVanillaPasses()
    {
        try
        {
            for(ResourceInfo info : ClassPath.from(Thread.currentThread().getContextClassLoader()).getResources())
            {
                if(info.getResourceName().startsWith("assets/minecraft/shaders/program/") && info.getResourceName().endsWith(".json"))
                {
                    String[] parts = info.getResourceName().split("/");
                    String file = parts[parts.length - 1];
                    String name = file.substring(0, file.indexOf("."));
                    if(name.equals("phosphor"))
                    {
                        PassRegistry.register(name, new PhosphorPass());
                    }
                    else if(name.equals("notch"))
                    {
                        PassRegistry.register(name, new NotchPass());
                    }
                    else
                        PassRegistry.register(name, new VanillaPass(name));
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void activateBatch()
    {
        if(batch != null)
        {
            if(OpenGlHelper.shadersSupported)
            {
                try
                {
                    ShaderGroup theShaderGroup = batch.toShaderGroup(resManager);
                    theShaderGroup.createBindFramebuffers(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                    ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, theShaderGroup, 51);
                    ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, true, 55);
                }
                catch(JsonException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disactiveBatch()
    {
        updateConfig();
        ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, null, 51);
        ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, false, 55);
    }

    private void updateConfig()
    {
        config.get(Configuration.CATEGORY_GENERAL, "enabled", true).set(enabled);
        config.get(Configuration.CATEGORY_GENERAL, "palette", "none").set(paletteID);
        config.save();
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent event)
    {
        if(event.gui instanceof GuiOptions)
        {
            if(event.button.id == 0x42)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiShadyOptions(event.gui));
            }
        }
    }

    @SubscribeEvent
    public void onGuiInit(DrawScreenEvent event)
    {
        if(event.gui instanceof GuiMainMenu)
        {
            if(!OpenGlHelper.shadersSupported)
            {
                String t = "you won't be able to enjoy ShadyMod and its children ;(";
                FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
                Gui.drawRect(0, 0, font.getStringWidth(t), 20, 0x90000000);
                font.drawStringWithShadow("Shaders are not supported by your graphical card,", 0, 0, 0xFFFF0707);
                font.drawStringWithShadow(t, 0, 10, 0xFFFF0707);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onGuiInit(InitGuiEvent event)
    {
        if(event.gui instanceof GuiOptions)
        {
            int x = event.gui.width / 2 + 5 + 150 + 5;
            int y = event.gui.height / 6 + 48 - 6;
            event.buttonList.add(new GuiIconButton(0x42, x, y, new ResourceLocation(MODID, "textures/gui/palette.png")));
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean flag)
    {
        this.enabled = flag;
    }

    public void setBatch(ShaderBatch batch)
    {
        this.batch = batch;
    }

    public ShaderBatch getBatch()
    {
        return batch;
    }

    public void saveBatch() throws IOException
    {
        saveBatch(batch, lastBatchFile);
    }

    public void saveBatch(ShaderBatch batch, File batchFile) throws IOException
    {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(batchFile));
        if(batch == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(batch.getPasses().size());
            for(int i = 0; i < batch.getPasses().size(); i++ )
            {
                ShaderPass pass = batch.getPasses().get(i);
                String passName = batch.getNames().get(i);
                out.writeUTF(passName);
                out.writeUTF(PassRegistry.getID(pass));
            }
        }
        out.flush();
        out.close();
    }

    public ShaderBatch loadBatch(File file) throws IOException
    {
        ShaderBatch batch = new ShaderBatch();
        DataInputStream input = new DataInputStream(new FileInputStream(file));

        int passesCount = input.readInt();
        for(int i = 0; i < passesCount; i++ )
        {
            String name = input.readUTF();
            String passID = input.readUTF();
            ShaderPass pass = PassRegistry.getFromID(passID);
            batch.addPass(name, pass);
        }

        input.close();
        return batch;
    }
}
