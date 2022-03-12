package shade;

import arc.*;
import arc.backend.sdl.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.struct.*;
import arc.util.serialization.*;

public class Shade50 extends ApplicationCore{
    public static final Quat quat = new Quat();

    public static Renderer renderer;
    public static UI ui;
    public static Input input;
    public static StatedJson json;

    public static void main(String[] args){
        new SdlApplication(new Shade50(), new SdlConfig(){{
            title = "Shade-50";
            width = 800;
            height = 600;
            maximized = true;
            gl30 = true;
            disableAudio = true;
            depth = 8;
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
        add(input = new Input());

        Core.input.addProcessor(Core.scene);

        int[] insets = Core.graphics.getSafeInsets();
        Core.scene.marginLeft = insets[0];
        Core.scene.marginRight = insets[1];
        Core.scene.marginTop = insets[2];
        Core.scene.marginBottom = insets[3];

        json = new StatedJson(){{
            setSerializer(FloatSeq.class, new Serializer<>(){
                @Override
                public void write(Json json, FloatSeq object, Class knownType){
                    writeArrayStart();
                    float[] items = object.items;
                    int size = object.size;

                    for(int i = 0; i < size; i++) writeValue(items[i], float.class, null);
                    writeArrayEnd();
                }

                @Override
                public FloatSeq read(Json json, JsonValue data, Class type){
                    FloatSeq out = current();
                    out.clear();

                    for(JsonValue child = data.child; child != null; child = child.next) out.add(child.asFloat());
                    return out;
                }
            });

            setSerializer(ShortSeq.class, new Serializer<>(){
                @Override
                public void write(Json json, ShortSeq object, Class knownType){
                    writeArrayStart();
                    short[] items = object.items;
                    int size = object.size;

                    for(int i = 0; i < size; i++) writeValue(items[i], short.class, null);
                    writeArrayEnd();
                }

                @Override
                public ShortSeq read(Json json, JsonValue data, Class type){
                    ShortSeq out = current();
                    out.clear();

                    for(JsonValue child = data.child; child != null; child = child.next) out.add(child.asShort());
                    return out;
                }
            });
        }};
    }

    @Override
    public void dispose(){
        super.dispose();
        Core.batch.dispose();
        Core.atlas.dispose();
    }

    public static <T> T read(Class<T> type, Class<?> elementType, T ref, String input){
        json.current = ref;
        T res = json.fromJson(type, elementType, input);
        json.current = null;

        return res;
    }

    public static float parseFloat(String input){
        try{
            return Float.parseFloat(input);
        }catch(Throwable t){
            return 0f;
        }
    }

    public static int parseInt(String input){
        try{
            return Integer.parseInt(input);
        }catch(Throwable t){
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static class StatedJson extends Json{
        protected Object current;

        @Override
        protected Object newInstance(Class type){
            return (current != null && type.isAssignableFrom(current.getClass())) ? current : super.newInstance(type);
        }

        public <T> T current(){
            return (T)current;
        }
    }
}
