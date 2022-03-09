package shade.comp;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import shade.Renderer.*;

public class CBuffer extends Container{
    public SFrameBuffer buffer = new SFrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
    public float scaleX = 1f;
    public float scaleY = 1f;
    public Color clear = new Color();

    @Override
    public void act(){
        buffer.resize(
            Mathf.round(Core.graphics.getWidth() * scaleX),
            Mathf.round(Core.graphics.getHeight() * scaleY)
        );

        buffer.begin(clear);
    }

    @Override
    public void dispose(){
        buffer.dispose();
    }

    @Override
    public void buildUI(Table table){

    }
}
