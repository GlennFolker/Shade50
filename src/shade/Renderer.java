package shade;

import arc.*;
import arc.graphics.*;
import arc.graphics.g3d.*;

public class Renderer implements ApplicationListener{
    public Camera3D camera;

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
    }
}
