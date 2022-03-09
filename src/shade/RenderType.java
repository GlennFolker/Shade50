package shade;

import arc.graphics.*;

public enum RenderType{
    points(Gl.points),
    lines(Gl.lines),
    lineLoop(Gl.lineLoop),
    triangles(Gl.triangles),
    triangleStrip(Gl.triangleStrip),
    triangleFan(Gl.triangleFan);

    public final int primitiveType;

    RenderType(int primitiveType){
        this.primitiveType = primitiveType;
    }
}
