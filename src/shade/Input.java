package shade;

import arc.*;
import arc.graphics.g3d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.util.*;

import static shade.Shade50.*;

public class Input implements ApplicationListener{
    public float sensitivity = 0.2f;

    @Override
    public void update(){
        int dx = Core.input.deltaX(), dy = -Core.input.deltaY();

        Camera3D cam = renderer.camera;
        Vec3 dir = cam.direction, pos = cam.position;
        if(Core.scene.getScrollFocus() == null){
            // TODO broken
            if(Core.input.keyDown(KeyCode.mouseRight)) dir.rotate(Vec3.Y, dx * sensitivity).rotate(Vec3.X, dy * sensitivity);
        }

        if(Core.scene.getKeyboardFocus() == null){
            Tmp.v31.setZero();
            if(Core.input.keyDown(KeyCode.w)) Tmp.v31.z += 1f;
            if(Core.input.keyDown(KeyCode.s)) Tmp.v31.z -= 1f;
            if(Core.input.keyDown(KeyCode.a)) Tmp.v31.x += 1f;
            if(Core.input.keyDown(KeyCode.d)) Tmp.v31.x -= 1f;
            if(Core.input.keyDown(KeyCode.q)) Tmp.v31.y -= 1f;
            if(Core.input.keyDown(KeyCode.e)) Tmp.v31.y += 1f;
            pos.add(Tmp.v31.rotate(Vec3.Y, Tmp.v1.set(dir.x, dir.z).angle() - 90f));
        }
    }
}
