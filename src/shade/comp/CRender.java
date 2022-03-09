package shade.comp;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.scene.ui.layout.*;
import shade.*;

public class CRender extends Container{
    public Mesh mesh;
    public Shader shader;
    public RenderType renderType = RenderType.triangles;

    @Override
    public void act(){
        if(mesh == null || shader == null) return;

        shader.bind();
        mesh.render(shader, renderType.primitiveType);
    }

    @Override
    public void dispose(){
        if(mesh != null) mesh.dispose();
        if(shader != null) shader.dispose();
    }

    @Override
    public void buildUI(Table table){

    }
}
