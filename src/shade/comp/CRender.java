package shade.comp;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import shade.*;

public class CRender extends Container{
    public Mat3D transform = new Mat3D();
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
        UI.defHeader(table, "Mesh Render").growX().fillY();
        UI.defMat4(table.row(), "Transform", transform, null).growX().fillY().padTop(6f);
        UI.defFooter(table.row(), this, null).growX().fillY().padTop(6f);
    }
}
