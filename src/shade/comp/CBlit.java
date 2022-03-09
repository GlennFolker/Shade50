package shade.comp;

import arc.graphics.gl.*;
import arc.scene.ui.layout.*;
import shade.Renderer.*;

public class CBlit extends Container{
    public Shader shader;

    @Override
    public void act(){
        SFrameBuffer.blitCurrent(shader);
    }

    @Override
    public void dispose(){
        if(shader != null) shader.dispose();
    }

    @Override
    public void buildUI(Table table){

    }
}
