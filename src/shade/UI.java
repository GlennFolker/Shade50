package shade;

import arc.*;
import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.Dialog.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.Slider.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.Tooltip.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import shade.Renderer.Container.*;

import java.nio.*;

import static shade.Shade50.*;
import static shade.Renderer.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UI implements ApplicationListener{
    public static TextureRegionDrawable
        bgWhite, bgLight, bgMid, bgDark,
        icoPlus, icoMinus, icoReplay, icoPencil, icoBuffer, icoBufferPencil, icoBlend,
        icoUp, icoDown;

    public static Font font, fontLarge;

    public static ScrollPaneStyle defPane;
    public static ImageButtonStyle defImageBtn;
    public static TextButtonStyle defTextBtn;
    public static LabelStyle defLabel, largeLabel;
    public static TextFieldStyle defField;
    public static SliderStyle defSlide;
    public static DialogStyle defDialog;

    protected WidgetGroup group;
    protected Table submenu, instructions;
    protected ImageButton adder;

    @Override
    public void init(){
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Core.files.internal("fonts/font.ttf"));
        font = fontGen.generateFont(new FreeTypeFontParameter(){{ size = 14; }});
        fontLarge = fontGen.generateFont(new FreeTypeFontParameter(){{ size = 20; }});
        fontGen.dispose();

        bgWhite = new TextureRegionDrawable(Core.atlas.white());
        bgLight = (TextureRegionDrawable)bgWhite.tint(0.5f, 0.5f, 0.67f, 0.75f);
        bgMid = (TextureRegionDrawable)bgWhite.tint(0.33f, 0.33f, 0.45f, 0.67f);
        bgDark = (TextureRegionDrawable)bgWhite.tint(0.25f, 0.25f, 0.35f, 0.5f);

        icoPlus = new TextureRegionDrawable(Core.atlas.find("plus"));
        icoMinus = new TextureRegionDrawable(Core.atlas.find("minus"));
        icoReplay = new TextureRegionDrawable(Core.atlas.find("replay"));
        icoPencil = new TextureRegionDrawable(Core.atlas.find("pencil"));
        icoBuffer = new TextureRegionDrawable(Core.atlas.find("buffer"));
        icoBufferPencil = new TextureRegionDrawable(Core.atlas.find("buffer-pencil"));
        icoBlend = new TextureRegionDrawable(Core.atlas.find("blend"));
        icoUp = new TextureRegionDrawable(Core.atlas.find("up"));
        icoDown = new TextureRegionDrawable(Core.atlas.find("down"));

        Core.scene.addStyle(ScrollPaneStyle.class, defPane = new ScrollPaneStyle(){{
            background = bgDark;
        }});

        Core.scene.addStyle(ButtonStyle.class, new ButtonStyle());

        Core.scene.addStyle(ImageButtonStyle.class, defImageBtn = new ImageButtonStyle(){{
            imageUpColor = Color.lightGray;
            imageOverColor = Color.white;
            imageDownColor = Color.gray;
            imageDisabledColor = Color.darkGray;
        }});

        Core.scene.addStyle(TextButtonStyle.class, defTextBtn = new TextButtonStyle(){{
            font = UI.font;
            fontColor = Color.lightGray;
            overFontColor = Color.white;
            downFontColor = Color.gray;
            disabledFontColor = Color.darkGray;
        }});

        Core.scene.addStyle(LabelStyle.class, defLabel = new LabelStyle(){{
            font = UI.font;
        }});

        largeLabel = new LabelStyle(){{
            font = fontLarge;
        }};

        Core.scene.addStyle(TextFieldStyle.class, defField = new TextFieldStyle(){{
            background = bgLight;
            cursor = bgWhite.tint(0.25f, 0.25f, 0.35f, 1f);
            selection = bgWhite.tint(0.25f, 0.25f, 0.35f, 1f);
            font = UI.font;
            fontColor = Color.lightGray;
            focusedFontColor = Color.white;
            disabledFontColor = Color.darkGray;
        }});

        Core.scene.addStyle(SliderStyle.class, defSlide = new SliderStyle(){{
            background = bgMid;
            knob = new TextureRegionDrawable(bgLight){
                @Override
                public float getMinWidth(){
                    return 8f;
                }

                @Override
                public float getMinHeight(){
                    return 14f;
                }
            };
        }});

        Core.scene.addStyle(DialogStyle.class, defDialog = new DialogStyle(){{
            titleFont = fontLarge;
            background = bgDark;
        }});

        Tooltips.getInstance().textProvider = str -> new Tooltip(t -> {
            t.setBackground(bgDark);
            t.add(str);
        });

        Dialog.setShowAction(() -> Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f, Interp.pow3Out)));
        Dialog.setHideAction(() -> Actions.fadeOut(0.06f, Interp.pow3In));

        build();
        replay();
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
    }

    public void build(){
        renderer.containers.each(Disposable::dispose);
        renderer.containers.clear();
        Core.scene.clear();

        group = new WidgetGroup();
        group.fillParent = true;

        Table global = new Table();
        global.fillParent = true;
        global.right().table(bgDark, cont -> {
            cont.pane(t -> {
                instructions = t.top();
                instructions.defaults().padBottom(8f);
            }).grow().update(p -> {
                if(Core.scene.getScrollFocus() != p) return;
                Vec2 pos = Core.scene.screenToStageCoordinates(Core.input.mouse());

                Element hit = Core.scene.hit(pos.x, pos.y, false);
                if(hit == null || !hit.isDescendantOf(p)) Core.scene.setScrollFocus(null);
            }).get().setScrollingDisabled(true, false);

            submenu = new Table(bgMid){{
                setTransform(true);

                button(icoPencil, () -> {
                    ui.add(new CRender());
                    hideSubmenu();
                }).size(32f).pad(4f).tooltip("Mesh Renderer");
                row().button(icoBuffer, () -> {
                    ui.add(new CBuffer());
                    hideSubmenu();
                }).size(32f).pad(4f).tooltip("Framebuffer Capture");
                row().button(icoBufferPencil, () -> {
                    ui.add(new CBlit());
                    hideSubmenu();
                }).size(32f).pad(4f).tooltip("Framebuffer Blit");
                row().button(icoBlend, () -> {
                    ui.add(new CBlend());
                    hideSubmenu();
                }).size(32f).pad(4f).tooltip("Blending");
                setSize(40f, 160f);

                visible = false;
                setScale(1f, 0f);
            }};

            cont.row().table(bgLight, buttons -> {
                buttons.defaults().size(32f).pad(4f);

                buttons.right();
                buttons.button(icoReplay, () -> {
                    replay();
                    hideSubmenu();
                }).tooltip("Restart");
                adder = buttons.button(icoPlus, this::toggleSubmenu).tooltip("Add").get();
            }).growX().fillY().update(t -> {
                submenu.setPosition(adder.x - 4f, adder.y + adder.getHeight() + 4f);
                submenu.setOrigin(Align.bottomLeft);
            }).get().addChild(submenu);
        }).growY().fillX().minWidth(320f);

        group.addChild(global);
        Core.scene.add(group);
    }

    public void showSubmenu(){
        if(submenu.visible) return;

        submenu.visible = true;
        submenu.actions(Actions.scaleTo(1f, 1f, 0.1f, Interp.pow3Out));
    }

    public void hideSubmenu(){
        if(!submenu.visible) return;
        submenu.actions(Actions.scaleTo(1f, 0f, 0.06f, Interp.pow3In), Actions.visible(false));
    }

    public void toggleSubmenu(){
        if(submenu.visible){
            hideSubmenu();
        }else{
            showSubmenu();
        }
    }

    public void replay(){
        renderer.containers.each(Disposable::dispose);
        renderer.containers.clear();
        instructions.clear();

        add(new CBlend(BlendFunc.srcAlpha, BlendFunc.one));
        add(new CRender(
            new Mat3D(Tmp.v31.setZero(), new Quat(), Tmp.v32.set(4f, 4f, 4f)),
            new Mesh(false, true, 8, 36, VertexAttribute.position3, VertexAttribute.color){{
                setVertices(new float[]{
                    -1f, -1f, -1f, Color.red.toFloatBits(),
                     1f, -1f, -1f, Color.green.toFloatBits(),
                     1f,  1f, -1f, Color.blue.toFloatBits(),
                    -1f,  1f, -1f, Color.purple.toFloatBits(),

                    -1f, -1f,  1f, Color.cyan.toFloatBits(),
                     1f, -1f,  1f, Color.magenta.toFloatBits(),
                     1f,  1f,  1f, Color.yellow.toFloatBits(),
                    -1f,  1f,  1f, Color.lime.toFloatBits(),
                });

                setIndices(new short[]{
                    0, 1, 2, 2, 3, 0,
                    0, 1, 5, 5, 4, 0,
                    1, 2, 6, 6, 5, 1,
                    2, 3, 7, 7, 6, 2,
                    3, 0, 4, 4, 7, 3,
                    4, 5, 6, 6, 7, 4
                });
            }},
            new Shader("""
                in vec3 a_position;
                in vec4 a_color;
                
                out vec4 v_color;
                
                uniform mat4 u_proj;
                uniform mat4 u_trans;
                
                void main(){
                    gl_Position = u_proj * u_trans * vec4(a_position, 1.0);
                    
                    v_color = a_color;
                    v_color.a *= 255.0 / 254.0;
                }
                """, """
                in vec4 v_color;
                
                void main(){
                    gl_FragColor = v_color;
                }
                """),
            PrimitiveType.triangles
        ));

        Camera3D cam = renderer.camera;
        cam.position.set(8f, 6f, 8f);
        cam.direction.setZero().sub(cam.position);
    }

    public void add(Container cont){
        Table t = instructions.table(bgDark).growX().fillY().get();
        cont.buildUI(t);

        instructions.row();
        renderer.containers.add(cont);
    }

    public void moveUp(Container cont, Table t){
        Seq<Cell> cells = instructions.getCells();
        Cell<Table> cell = instructions.getCell(t);

        int c = cells.indexOf(cell);
        if(c > 0){
            cells.remove(c);
            cells.insert(c - 1, cell);
            instructions.invalidateHierarchy();
        }

        Seq<Container> conts = renderer.containers;
        int i = conts.indexOf(cont);
        if(i > 0){
            conts.remove(i);
            conts.insert(i - 1, cont);
        }
    }

    public void moveDown(Container cont, Table t){
        Seq<Cell> cells = instructions.getCells();
        Cell<Table> cell = instructions.getCell(t);

        int c = cells.indexOf(cell);
        if(c < cells.size - 1){
            cells.remove(c);
            cells.insert(c + 1, cell);
            instructions.invalidateHierarchy();
        }

        Seq<Container> conts = renderer.containers;
        int i = conts.indexOf(cont);
        if(i < conts.size - 1){
            conts.remove(i);
            conts.insert(i + 1, cont);
        }
    }

    public void remove(Container cont, Table t){
        Cell<Table> cell = instructions.getCell(t);
        if(cell != null){
            t.remove();
            instructions.getCells().remove(cell);
            instructions.invalidateHierarchy();
        }

        renderer.containers.remove(cont);
        cont.dispose();
    }

    @Override
    public void update(){
        Core.scene.act();
        Core.scene.draw();
    }

    public static Cell<Slider> defSlide(Table table, String tooltip, float min, float max, float step, float initial, Floatc setter){
        return table.add((Slider)new Slider(min, max, step, false){
            {
                setValue(initial);
                moved(setter);
            }

            @Override
            public float getPrefWidth(){
                return 40f;
            }

            @Override
            public float getPrefHeight(){
                return 14f;
            }
        }).tooltip(tooltip);
    }

    public static Cell<TextField> defString(Table table, String tooltip, String initial, Cons<String> setter){
        return table.add((TextField)new TextField(initial){
            {
                changed(() -> {
                    if(isValid()) setter.get(getText());
                });
            }

            @Override
            public float getPrefWidth(){
                return 40f;
            }
        }).tooltip(tooltip);
    }

    public static Cell<TextField> defFloat(Table table, String tooltip, float initial, Floatc setter){
        return defString(table, tooltip, Float.toString(initial), str -> setter.get(parseFloat(str))).with(t -> t.setFilter(TextFieldFilter.floatsOnly));
    }

    public static Cell<TextField> defInt(Table table, String tooltip, int initial, Intc setter){
        return defString(table, tooltip, Integer.toString(initial), str -> setter.get(parseInt(str))).with(t -> t.setFilter(TextFieldFilter.digitsOnly));
    }

    public static Cell<ImageButton> defEditor(Table table, String tooltip, Prov<String> initial, Boolf2<String, Table> setter){
        return table.button(icoPencil, () -> {
            Dialog dialog = new Dialog("Edit");
            dialog.fillParent = true;

            TextArea area = dialog.cont.area(initial.get(), str -> {}).grow().get();
            area.getStyle().background = bgMid;

            dialog.buttons.button("OK", () -> {
                Dialog err = new Dialog("Error");

                if(!setter.get(area.getText(), err.cont)){
                    err.buttons.button("OK", err::hide).size(128f, 48f).pad(0f).get().getStyle().up = bgLight;
                    err.show();
                }else{
                    dialog.hide();
                }
            }).size(128f, 48f).pad(0f).padBottom(3f).get().getStyle().up = bgLight;
            dialog.show();
        }).tooltip(tooltip);
    }

    public static <T extends Enum<T>> Cell<Table> defEnum(Table table, String tooltip, T[] values, T initial, Cons<T> setter){
        class State{
            ImageButton selector;
            StatedDrawable drawable;

            final Table selectMenu = new Table(bgMid){{
                visible = false;
                setTransform(true);
                setScale(1f, 0f);

                for(T value : values) button(value.toString(), () -> {
                    State.this.value = value;
                    setter.get(value);

                    close();
                }).growX().fillY().row();

                float w = 0f, h = 0f;
                for(Element e : getChildren()){
                    Label label = ((TextButton)e).getLabel();
                    label.setWrap(false);

                    w = Math.max(w, label.getPrefWidth());
                    h += label.getPrefHeight();
                }

                setSize(w, h);
            }};

            boolean opened;
            T value = initial;

            void toggle(){
                if(opened){
                    close();
                }else{
                    open();
                }

                selectMenu.toFront();
            }

            void open(){
                if(selectMenu.visible) return;

                opened = true;
                selectMenu.visible = true;
                selectMenu.actions(Actions.scaleTo(1f, 1f, 0.1f, Interp.pow3Out));
                drawable.update();
            }

            void close(){
                if(!selectMenu.visible) return;

                opened = false;
                selectMenu.actions(Actions.scaleTo(1f, 0f, 0.06f, Interp.pow3In), Actions.visible(false));
                drawable.update();
            }
        } State state = new State();

        ui.group.addChild(state.selectMenu);
        return table.add((Table)new Table(bgDark){
            {
                align(Align.center);

                float w = 0f;
                for(T value : values) w = Math.max(w, new Label(value.toString()).getPrefWidth());
                float width = w;

                label(() -> state.value.toString()).growX().fillY().tooltip(tooltip).self(c -> {
                    Label l = c.get();
                    l.getStyle().background = bgMid;

                    c.minWidth(width);
                }).get().getStyle().background = bgLight;
                table(bgMid, t -> state.selector = t.button(state.drawable = new StatedDrawable(() -> state.opened ? icoUp : icoDown), state::toggle).tooltip("Select").size(12f).pad(4f).get()).fillX().growY();
            }

            @Override
            protected void setScene(Scene scene){
                if(getScene() != null && scene == null) state.selectMenu.remove();
                super.setScene(scene);
            }
        }).update(t -> {
            Table menu = state.selectMenu;
            t.localToAscendantCoordinates(ui.group, Tmp.v1.set(t.getWidth() - menu.getWidth(), -menu.getHeight()));

            menu.setPosition(Tmp.v1.x, Tmp.v1.y);
            menu.setOrigin(Align.topLeft);
        });
    }

    public static <T> Cell<Table> defSeq(Table table, String title, Seq<T> pointer, Prov<T> provider, SeqSetter<T, Table> setter){
        class State{
            Table content;

            void add(T inst){
                content.table(e -> {
                    Seq<Cell> cells = content.getCells();
                    setter.get(inst, e, () -> cells.indexOf(content.getCell(e))).growX().fillY();

                    e.table(bgMid, b -> b.button(icoMinus, () -> {
                        int index = cells.indexOf(content.getCell(e));

                        e.remove();
                        pointer.remove(index);
                        cells.remove(index);
                        content.invalidateHierarchy();
                    }).size(32f).pad(4f).tooltip("Remove")).fillX().growY();
                }).growX().fillY().row();
            }
        } State state = new State();

        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.table(bgDark, arr -> {
                arr.table(bgDark, t -> state.content = t).growX().fillY();
                arr.row().table(bgMid, t -> t.right().button(icoPlus, () -> {
                    T inst = provider != null ? provider.get() : null;
                    state.add(inst);

                    pointer.add(inst);
                }).size(32f).pad(4f).tooltip("Add")).growX().fillY().padTop(6f);

                for(T value : pointer) state.add(value);
            }).growX().fillY().padTop(4f).padBottom(4f).padLeft(8f);
        });
    }

    public static Cell<Table> defFloats(Table table, String title, FloatSeq pointer, Runnable changed){
        FloatSeq buffer = new FloatSeq(pointer);
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.area(json.toJson(pointer, FloatSeq.class, float.class), str -> {
                pointer.clear();
                pointer.addAll(buffer);

                if(changed != null) changed.run();
            }).growX().fillY().minHeight(100f).pad(4f).padLeft(8f).valid(str -> {
                try{
                    read(FloatSeq.class, float.class, buffer, str);
                    return true;
                }catch(Throwable t){
                    return false;
                }
            });
        });
    }

    public static Cell<Table> defShorts(Table table, String title, ShortSeq pointer, Runnable changed){
        ShortSeq buffer = new ShortSeq(pointer);
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.area(json.toJson(pointer, ShortSeq.class, short.class), str -> {
                pointer.clear();
                pointer.addAll(buffer);

                if(changed != null) changed.run();
            }).growX().fillY().minHeight(100f).pad(4f).padLeft(8f).valid(str -> {
                try{
                    read(ShortSeq.class, float.class, buffer, str);
                    return true;
                }catch(Throwable t){
                    return false;
                }
            });
        });
    }

    public static Cell<Table> defVec2(Table table, String title, Vec2 pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.table(t -> {
                defFloat(t, "X", pointer.x, val -> {
                    pointer.x = val;
                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(8f);

                defFloat(t, "Y", pointer.y, val -> {
                    pointer.y = val;
                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(0f);
            }).growX().fillY();
        });
    }

    public static Cell<Table> defVec3(Table table, String title, Vec3 pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.table(t -> {
                defFloat(t, "X", pointer.x, val -> {
                    pointer.x = val;
                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(8f);

                defFloat(t, "Y", pointer.y, val -> {
                    pointer.y = val;
                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(0f);

                defFloat(t, "Z", pointer.z, val -> {
                    pointer.z = val;
                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(0f);
            }).growX().fillY();
        });
    }

    public static Cell<Table> defCol(Table table, String title, Color pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.table(t -> {
                t.image(bgWhite).update(i -> i.setColor(pointer)).width(16f).growY().pad(4f).padLeft(8f);

                t.table(col -> {
                    defSlide(col, "Red", 0f, 1f, 1f / 256f, pointer.r, val -> {
                        pointer.r = val;
                        if(changed != null) changed.run();
                    }).growX().fillY().pad(4f).padLeft(0f);

                    defSlide(col, "Green", 0f, 1f, 1f / 256f, pointer.g, val -> {
                        pointer.g = val;
                        if(changed != null) changed.run();
                    }).growX().fillY().pad(4f).padLeft(0f);

                    defSlide(col.row(), "Blue", 0f, 1f, 1f / 256f, pointer.b, val -> {
                        pointer.b = val;
                        if(changed != null) changed.run();
                    }).growX().fillY().pad(4f).padLeft(0f);

                    defSlide(col, "Alpha", 0f, 1f, 1f / 256f, pointer.a, val -> {
                        pointer.a = val;
                        if(changed != null) changed.run();
                    }).growX().fillY().pad(4f).padLeft(0f);
                }).growX().fillY();
            }).growX().fillY();
        });
    }

    public static Cell<Table> defQuat(Table table, String title, Quat pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            pointer.nor();
            Vec3 euler = new Vec3(pointer.getYaw(), pointer.getPitch(), pointer.getRoll());

            cont.table(t -> {
                defFloat(t, "Yaw", euler.x, val -> {
                    euler.x = val;
                    pointer.setEulerAngles(euler.x, euler.y, euler.z);

                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(8f);

                defFloat(t, "Pitch", euler.y, val -> {
                    euler.y = val;
                    pointer.setEulerAngles(euler.x, euler.y, euler.z);

                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(0f);

                defFloat(t, "Roll", euler.z, val -> {
                    euler.z = val;
                    pointer.setEulerAngles(euler.x, euler.y, euler.z);

                    if(changed != null) changed.run();
                }).growX().fillY().pad(4f).padLeft(0f);
            }).growX().fillY();
        });
    }

    public static Cell<Table> defMat4(Table table, String title, Mat3D pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            Vec3 pos = new Vec3();
            pointer.getTranslation(pos);

            Quat rot = new Quat();
            pointer.getRotation(rot);

            Vec3 scl = new Vec3();
            pointer.getScale(scl);

            defVec3(cont, "Position", pos, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).padLeft(8f);

            defQuat(cont.row(), "Rotation", rot, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).padLeft(8f);

            defVec3(cont.row(), "Scale", scl, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).padLeft(8f);
        });
    }

    public static Cell<Table> defVertAttrib(Table table, String title, VertexAttribute initial, Cons<VertexAttribute> setter){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            VertexAttributeBuilder builder = initial == null ? new VertexAttributeBuilder() : new VertexAttributeBuilder(initial);
            cont.table(t -> {
                defInt(t, "Components", builder.components, val -> {
                    builder.components = val;
                    setter.get(builder.build());
                }).growX().fillY().pad(4f).padLeft(8f);

                defEnum(t, "Type", StructType.all, builder.type, val -> {
                    builder.type = val;
                    setter.get(builder.build());
                }).growX().fillY().pad(4f).padLeft(0f);

                defString(t.row(), "Alias", builder.alias, val -> {
                    builder.alias = val;
                    setter.get(builder.build());
                }).growX().fillY().pad(4f).padLeft(8f);

                defEnum(t, "Normalized", Bool.all, Bool.from(builder.normalized), val -> {
                    builder.normalized = val.value == 0;
                    setter.get(builder.build());
                }).growX().fillY().pad(4f).padLeft(0f).disabled(n -> builder.type != StructType.glFixed);
            }).growX().fillY();
        });
    }

    public static Cell<Table> defMesh(Table table, String title, Mesh initial, Cons<Mesh> setter){
        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            Seq<VertexAttribute> attributes = new Seq<>(VertexAttribute.class);
            if(initial != null) attributes.set(initial.attributes);

            FloatSeq vertices = new FloatSeq();
            if(initial != null){
                FloatBuffer buf = initial.getVerticesBuffer();
                buf.clear();

                vertices.setSize(buf.limit());
                buf.get(vertices.items, 0, vertices.size);
            }

            ShortSeq indices = new ShortSeq();
            if(initial != null){
                ShortBuffer buf = initial.getIndicesBuffer();
                buf.clear();

                indices.setSize(buf.limit());
                buf.get(indices.items, 0, indices.size);
            }

            defSeq(cont, "Vertex Attributes", attributes, null, (val, t, index) -> defVertAttrib(t, null, val, v -> attributes.set(index.get(), v))).growX().fillY().pad(4f);
            defFloats(cont.row(), "Vertices", vertices, null).growX().fillY().pad(4f).padLeft(8f);
            defShorts(cont.row(), "Indices", indices, null).growX().fillY().pad(4f).padLeft(8f);

            cont.row().table(bgMid, t -> t.right().button(icoPencil, () -> {
                Mesh mesh = new Mesh(false, true, vertices.size / (attributes.sum(a -> a.size) / 4), indices.size, attributes.toArray());
                mesh.setVertices(vertices.items, 0, vertices.size);
                mesh.setIndices(indices.items, 0, indices.size);

                setter.get(mesh);
            }).size(32f).pad(4f).tooltip("Build Mesh")).growX().fillY().padTop(4f);
        });
    }

    public static Cell<Table> defShader(Table table, String title, Shader initial, Cons<Shader> setter){
        class State{
            String vertSource = initial == null ? "" : copySource(initial.getVertexShaderSource());
            String fragSource = initial == null ? "" : copySource(initial.getFragmentShaderSource());
        } State state = new State();

        return table.table(bgDark, cont -> {
            if(title != null && !title.isEmpty()) cont.add(title).growX().fillY().pad(4f).row();

            cont.table(bgDark, t -> {
                t.left().add("Vertex").fill().pad(4f).padLeft(8f).padRight(0f);
                t.add(": ").fill().pad(4f).padLeft(3f);
                defEditor(t, "Edit Shader", () -> state.vertSource, (val, e) -> {
                    state.vertSource = val;
                    return true;
                }).size(32f).pad(4f);

                t.row().add("Fragment").fill().pad(4f).padLeft(8f).padRight(0f);
                t.add(": ").fill().pad(4f).padLeft(3f);
                defEditor(t, "Edit Shader", () -> state.fragSource, (val, e) -> {
                    state.fragSource = val;
                    return true;
                });
            }).growX().fillY().pad(4f).padLeft(8f).padBottom(0f);

            cont.row().table(bgMid, t -> t.right().button(icoPencil, () -> {
                Shader shader = new Shader(state.vertSource, state.fragSource);
                setter.get(shader);
            }).size(32f).pad(4f)).tooltip("Compile Shader").growX().fillY();
        });
    }

    public static <T extends Element> Cell<T> defField(Table table, String fieldName, Prov<Cell<T>> cell){
        table.add(fieldName).fill().pad(4f).padRight(0f);
        table.add(": ").fill().pad(4f).padLeft(3f);
        return cell.get().growX().fillY().pad(4f).padLeft(0f);
    }

    public static Cell<Table> defHeader(Table table, String title){
        return table.table(bgMid, t -> t.add(title, largeLabel).grow().pad(4f));
    }

    public static Cell<Table> defFooter(Table table, Container cont, Runnable changed){
        return table.table(bgMid, t -> {
            t.right().button(icoUp, () -> {
                ui.moveUp(cont, table);
                if(changed != null) changed.run();
            }).size(32f).pad(4f).tooltip("Move Up");

            t.button(icoDown, () -> {
                ui.moveDown(cont, table);
                if(changed != null) changed.run();
            }).size(32f).pad(4f).tooltip("Move Down");

            t.button(icoMinus, () -> {
                ui.remove(cont, table);
                if(changed != null) changed.run();
            }).size(32f).pad(4f).tooltip("Remove");
        });
    }

    public static class StatedDrawable implements TransformDrawable{
        private final Prov<? extends Drawable> provider;
        private Drawable current;

        public StatedDrawable(Prov<? extends Drawable> provider){
            this.provider = provider;
            update();
        }

        public void update(){
            current = provider.get();
        }

        @Override
        public void draw(float x, float y, float width, float height){
            current.draw(x, y, width, height);
        }

        @Override
        public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
            if(current instanceof TransformDrawable draw){
                draw.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation);
            }else{
                current.draw(x, y, width, height);
            }
        }

        @Override
        public float getLeftWidth(){ return current.getLeftWidth(); }
        @Override
        public void setLeftWidth(float leftWidth){ current.setLeftWidth(leftWidth); }

        @Override
        public float getRightWidth(){ return current.getRightWidth(); }
        @Override
        public void setRightWidth(float rightWidth){ current.setRightWidth(rightWidth); }

        @Override
        public float getTopHeight(){ return current.getTopHeight(); }
        @Override
        public void setTopHeight(float topHeight){ current.setTopHeight(topHeight); }

        @Override
        public float getBottomHeight(){ return current.getBottomHeight(); }
        @Override
        public void setBottomHeight(float bottomHeight){ current.setBottomHeight(bottomHeight); }

        @Override
        public float getMinWidth(){ return current.getMinWidth(); }
        @Override
        public void setMinWidth(float minWidth){ current.setMinWidth(minWidth); }

        @Override
        public float getMinHeight(){ return current.getMinHeight(); }
        @Override
        public void setMinHeight(float minHeight){ current.setMinHeight(minHeight); }

        @Override
        public float imageSize(){ return current.imageSize(); }
    }

    interface SeqSetter<T, E extends Element>{
        Cell<E> get(T value, Table table, Intp index);
    }
}
