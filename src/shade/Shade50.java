package shade;

import arc.*;
import arc.backend.sdl.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;

public class Shade50 extends ApplicationCore{
    public static Renderer renderer;
    public static UI ui;
    public static Input input;

    public static void main(String[] args){
        new SdlApplication(new Shade50(), new SdlConfig(){{
            title = "Shade50";
            width = 800;
            height = 600;
            maximized = true;
            gl30 = true;
            disableAudio = true;
            depth = 8;
            stencil = 8;
        }});
    }

    @Override
    public void setup(){
        Core.atlas = new TextureAtlas("sprites/sprites.aatls");
        Core.camera = new Camera();
        Core.batch = new SpriteBatch();
        Core.scene = new Scene();

        add(renderer = new Renderer());
        add(ui = new UI());

        Core.input.addProcessor(Core.scene);
        Core.input.addProcessor(input = new Input());

        int[] insets = Core.graphics.getSafeInsets();
        Core.scene.marginLeft = insets[0];
        Core.scene.marginRight = insets[1];
        Core.scene.marginTop = insets[2];
        Core.scene.marginBottom = insets[3];
    }

    @Override
    public void dispose(){
        super.dispose();
        Core.batch.dispose();
        Core.atlas.dispose();
    }
}
