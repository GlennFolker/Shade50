package shade;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import shade.comp.*;

public class Renderer implements ApplicationListener{
    public Camera3D camera;
    public Seq<Container> containers = new Seq<>();

    @Override
    public void init(){
        camera = new Camera3D();
    }

    @Override
    public void update(){
        Gl.clearColor(0f, 0f, 0f, 0f);
        Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);

        Core.camera.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        Core.camera.update();
        camera.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        camera.update();

        for(Container cont : containers) cont.act();
        SFrameBuffer.resetAll();
    }

    public static class SFrameBuffer extends FrameBuffer{
        private static final Seq<SFrameBuffer> buffers = new Seq<>();

        public SFrameBuffer(int width, int height){
            super(width, height);
            buffers.add(this);
        }

        @Override
        public void dispose(){
            super.dispose();
            buffers.remove(this);
        }

        public static void blitCurrent(Shader shader){
            if(currentBoundFramebuffer instanceof SFrameBuffer buffer){
                buffer.end();
                buffer.blit(shader);
            }
        }

        public static void resetAll(){
            unbind();
            currentBoundFramebuffer = null;
            bufferNesting = 0;

            Draw.flush();
            Gl.viewport(0, 0, Core.graphics.getBackBufferWidth(), Core.graphics.getBackBufferHeight());
            for(SFrameBuffer buffer : buffers) buffer.lastBoundFramebuffer = null;
        }
    }
}
