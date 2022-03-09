package shade;

import arc.*;
import arc.input.*;
import arc.math.geom.*;
import arc.util.*;

import static shade.Shade50.*;

public class Input implements InputProcessor{
    protected int lastX = -1, lastY = -1;

    @Override
    public boolean mouseMoved(int x, int y){
        if(Core.scene.getScrollFocus() != null) return false;

        if(lastX == -1) lastX = x;
        if(lastY == -1) lastY = y;

        Tmp.v1.set(x, y).sub(lastX, lastY);
        if(Tmp.v1.isZero()) return false;

        renderer.camera.direction.rotate(Vec3.Y, Tmp.v1.x).rotate(Vec3.X, Tmp.v1.y);
        return true;
    }
}
