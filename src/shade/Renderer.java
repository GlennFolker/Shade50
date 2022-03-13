package shade;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;

import static shade.Shade50.*;

public class Renderer implements ApplicationListener{
    public Camera3D camera;
    public Seq<Container> containers = new Seq<>();

    public Shader screenspace;

    @Override
    public void init(){
        camera = new Camera3D();

        screenspace = new Shader("""
            in vec2 a_position;
            in vec2 a_texCoord0;
            
            out vec2 v_texCoords;
            
            void main(){
                gl_Position = vec4(a_position, 0.0, 1.0);
                v_texCoords = a_texCoord0;
            }
            """, """
            in vec2 v_texCoords;
            
            uniform sampler2D u_texture;
            
            void main(){
                gl_FragColor = texture2D(u_texture, v_texCoords);
            }
            """){
            @Override
            public void apply(){
                setUniformi("u_texture", 0);
            }
        };
    }

    @Override
    public void update(){
        Blending.disabled.apply();

        Gl.clearColor(0f, 0f, 0f, 0f);
        Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);

        Gl.depthMask(true);
        Gl.clear(Gl.depthBufferBit);
        Gl.enable(Gl.depthTest);

        Core.camera.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        Core.camera.update();
        camera.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        camera.update();

        for(Container cont : containers) cont.act();
        SFrameBuffer.resetAll();

        Gl.disable(Gl.depthTest);
    }

    public static String copySource(String shaderSource){
        return shaderSource.substring(shaderSource.indexOf("#endif") + "#endif".length()).trim();
    }

    public static class SFrameBuffer extends FrameBuffer{
        private static final Seq<SFrameBuffer> buffers = new Seq<>();

        public SFrameBuffer(int width, int height){
            super(width, height, true);
            buffers.add(this);
        }

        @Override
        public void dispose(){
            super.dispose();
            buffers.remove(this);
        }

        public static void blitCurrent(Shader shader){
            if(currentBoundFramebuffer instanceof SFrameBuffer buffer){
                buffer.end();
                buffer.blit(shader);
            }
        }

        public static void resetAll(){
            unbind();
            currentBoundFramebuffer = null;
            bufferNesting = 0;

            Draw.flush();
            Gl.viewport(0, 0, Core.graphics.getBackBufferWidth(), Core.graphics.getBackBufferHeight());
            for(SFrameBuffer buffer : buffers) buffer.lastBoundFramebuffer = null;
        }
    }

    public abstract static class Container implements Disposable{
        public abstract void act();

        public abstract void buildUI(Table table);

        public static class CRender extends Container{
            public Mat3D transform = new Mat3D();
            public Mesh mesh;
            public Shader shader;
            public PrimitiveType primitiveType = PrimitiveType.triangles;

            public CRender(){}

            public CRender(Mat3D transform, Mesh mesh, Shader shader, PrimitiveType primitiveType){
                this.transform.set(transform);
                this.mesh = mesh;
                this.shader = shader;
                this.primitiveType = primitiveType;
            }

            @Override
            public void act(){
                if(mesh == null || shader == null) return;

                shader.bind();
                shader.setUniformMatrix4("u_proj", renderer.camera.combined.val);
                shader.setUniformMatrix4("u_trans", transform.val);
                mesh.render(shader, primitiveType.type);
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
                UI.defMesh(table.row(), "Mesh", mesh, m -> {
                    if(mesh != null) mesh.dispose();
                    mesh = m;
                }).growX().fillY().padTop(6f);
                UI.defShader(table.row(), "Shader", shader, s -> {
                    if(shader != null) shader.dispose();
                    shader = s;
                }).growX().fillY().padTop(6f);
                table.row().table(t -> UI.defField(t, "Primitive Type", () -> UI.defEnum(t, "Primitive Type", PrimitiveType.all, primitiveType, val -> primitiveType = val))).growX().fillY().padTop(6f);

                UI.defFooter(table.row(), this, null).growX().fillY().padTop(6f);
            }
        }

        public static class CBuffer extends Container{
            public SFrameBuffer buffer = new SFrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            public Vec2 scale = new Vec2(1f, 1f);
            public Color clear = new Color();

            @Override
            public void act(){
                buffer.resize(
                    Mathf.round(Core.graphics.getWidth() * scale.x),
                    Mathf.round(Core.graphics.getHeight() * scale.y)
                );

                buffer.begin(clear);
            }

            @Override
            public void dispose(){
                buffer.dispose();
            }

            @Override
            public void buildUI(Table table){
                UI.defHeader(table, "FrameBuffer Capture").growX().fillY();

                table.row().table(t -> {
                    UI.defField(t, "Scale", () -> UI.defVec2(t, null, scale, null));
                    UI.defField(t.row(), "Clear Color", () -> UI.defCol(t, null, clear, null));
                }).growX().fillY().padTop(6f);

                UI.defFooter(table.row(), this, null).growX().fillY().padTop(6f);
            }
        }

        public static class CBlit extends Container{
            public Shader shader = new Shader(
                copySource(renderer.screenspace.getVertexShaderSource()),
                copySource(renderer.screenspace.getFragmentShaderSource())
            );

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
                UI.defHeader(table, "FrameBuffer Blit").growX().fillY();

                table.row().table(t -> UI.defField(t, "Shader", () -> UI.defShader(t, null, shader, s -> {
                    if(shader != null) shader.dispose();
                    shader = s;
                }))).growX().fillY().padTop(6f);

                UI.defFooter(table.row(), this, null).growX().fillY().padTop(6f);
            }
        }

        public static class CBlend extends Container{
            public BlendFunc src = BlendFunc.srcAlpha, dst = BlendFunc.oneMinusSrcAlpha;

            public CBlend(){}

            public CBlend(BlendFunc src, BlendFunc dst){
                this.src = src;
                this.dst = dst;
            }

            @Override
            public void act(){
                Gl.enable(Gl.blend);
                Gl.blendFunc(src.func, dst.func);
            }

            @Override
            public void dispose(){}

            @Override
            public void buildUI(Table table){
                UI.defHeader(table, "Blending").growX().fillY();

                table.row().table(t -> {
                    UI.defField(t, "Source", () -> UI.defEnum(t, "Source Factor", BlendFunc.all, src, val -> src = val));
                    UI.defField(t.row(), "Destination", () -> UI.defEnum(t, "Destination Factor", BlendFunc.all, dst, val -> dst = val));
                }).growX().fillY().padTop(6f);

                UI.defFooter(table.row(), this, null).growX().fillY().padTop(6f);
            }
        }
    }

    public static class VertexAttributeBuilder{
        public int components = 1;
        public StructType type = StructType.glFloat;
        public boolean normalized;
        public String alias = "";

        public VertexAttributeBuilder(){}

        public VertexAttributeBuilder(VertexAttribute initial){
            components = initial.components;
            type = StructType.from(initial.type);
            normalized = initial.normalized;
            alias = initial.alias;
        }

        public VertexAttribute build(){
            return new VertexAttribute(components, type.type, normalized, alias);
        }

        public int size(){
            return switch(type){
                case glFloat, glFixed -> 4 * components;
                case glUByte, glByte -> components;
                case glUShort, glShort -> 2 * components;
            };
        }
    }

    public enum PrimitiveType{
        points(Gl.points, "GL_POINTS"),
        lines(Gl.lines, "GL_LINES"),
        lineLoop(Gl.lineLoop, "GL_LINE_LOOP"),
        triangles(Gl.triangles, "GL_TRIANGLES"),
        triangleStrip(Gl.triangleStrip, "GL_TRIANGLE_STRIP"),
        triangleFan(Gl.triangleFan, "GL_TRIANGLE_FAN");

        public static final PrimitiveType[] all = values();

        public final int type;
        public final String alias;

        PrimitiveType(int type, String alias){
            this.type = type;
            this.alias = alias;
        }

        @Override
        public String toString(){
            return alias;
        }
    }

    public enum StructType{
        glFloat(Gl.floatV, "GL_FLOAT"),
        glFixed(Gl.fixed, "GL_FIXED"),
        glUByte(Gl.unsignedByte, "GL_UNSIGNED_BYTE"),
        glByte(Gl.byteV, "GL_BYTE"),
        glUShort(Gl.unsignedShort, "GL_UNSIGNED_SHORT"),
        glShort(Gl.shortV, "GL_SHORT");

        public static final StructType[] all = values();

        public final int type;
        public final String alias;

        StructType(int type, String alias){
            this.type = type;
            this.alias = alias;
        }

        @Override
        public String toString(){
            return alias;
        }

        public static StructType from(int type){
            return switch(type){
                case Gl.floatV -> glFloat;
                case Gl.fixed -> glFixed;
                case Gl.unsignedByte -> glUByte;
                case Gl.byteV -> glByte;
                case Gl.unsignedShort -> glUShort;
                case Gl.shortV -> glShort;
                default -> throw new IllegalArgumentException("Invalid GL type: " + type);
            };
        }
    }

    public enum BlendFunc{
        zero(Gl.zero, "GL_ZERO"),
        one(Gl.one, "GL_ONE"),
        dstColor(Gl.dstColor, "GL_DST_COLOR"),
        oneMinusDstColor(Gl.oneMinusDstColor, "GL_ONE_MINUS_DST_COLOR"),
        srcColor(Gl.srcColor, "GL_SRC_COLOR"),
        oneMinusSrcColor(Gl.oneMinusSrcColor, "GL_ONE_MINUS_SRC_COLOR"),
        srcAlpha(Gl.srcAlpha, "GL_SRC_ALPHA"),
        oneMinusSrcAlpha(Gl.oneMinusSrcAlpha, "GL_ONE_MINUS_SRC_ALPHA");

        public static final BlendFunc[] all = values();

        public final int func;
        public final String alias;

        BlendFunc(int func, String alias){
            this.func = func;
            this.alias = alias;
        }

        public String toString(){
            return alias;
        }
    }

    public enum Bool{
        glFalse(Gl.falseV, "GL_FALSE"),
        glTrue(Gl.trueV, "GL_TRUE");

        public static final Bool[] all = values();

        public final int value;
        public final String alias;

        Bool(int value, String alias){
            this.value = value;
            this.alias = alias;
        }

        @Override
        public String toString(){
            return alias;
        }

        public static Bool from(boolean value){
            return value ? glTrue : glFalse;
        }
    }
}
