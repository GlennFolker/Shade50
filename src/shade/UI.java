package shade;

import arc.*;
import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.Tooltip.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import shade.comp.*;

import static shade.Shade50.*;

@SuppressWarnings("unchecked")
public class UI implements ApplicationListener{
    public static TextureRegionDrawable
        bgWhite, bgLight, bgMid, bgDark,
        icoPlus, icoMinus, icoReplay, icoPencil, icoBuffer, icoBufferPencil;

    public static Font font, fontLarge;

    public static ScrollPaneStyle defPane;
    public static ImageButtonStyle defImageBtn;
    public static LabelStyle defLabel, largeLabel;
    public static TextFieldStyle defField;

    protected Table submenu, instructions;
    protected ImageButton adder;

    @Override
    public void init(){
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Core.files.internal("fonts/font.ttf"));
        font = fontGen.generateFont(new FreeTypeFontParameter(){{ size = 16; }});
        fontLarge = fontGen.generateFont(new FreeTypeFontParameter(){{ size = 24; }});
        fontGen.dispose();

        bgWhite = new TextureRegionDrawable(Core.atlas.white());
        bgLight = (TextureRegionDrawable)bgWhite.tint(0.67f, 0.67f, 0.75f, 0.67f);
        bgMid = (TextureRegionDrawable)bgWhite.tint(0.33f, 0.33f, 0.45f, 0.5f);
        bgDark = (TextureRegionDrawable)bgWhite.tint(0.25f, 0.25f, 0.35f, 0.33f);

        icoPlus = new TextureRegionDrawable(Core.atlas.find("plus"));
        icoMinus = new TextureRegionDrawable(Core.atlas.find("minus"));
        icoReplay = new TextureRegionDrawable(Core.atlas.find("replay"));
        icoPencil = new TextureRegionDrawable(Core.atlas.find("pencil"));
        icoBuffer = new TextureRegionDrawable(Core.atlas.find("buffer"));
        icoBufferPencil = new TextureRegionDrawable(Core.atlas.find("buffer-pencil"));

        Core.scene.addStyle(ScrollPaneStyle.class, defPane = new ScrollPaneStyle(){{
            background = bgDark;
        }});

        Core.scene.addStyle(ImageButtonStyle.class, defImageBtn = new ImageButtonStyle(){{
            imageUpColor = Color.lightGray;
            imageOverColor = Color.white;
            imageDownColor = Color.gray;
            imageDisabledColor = Color.darkGray;
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

        Tooltips.getInstance().textProvider = str -> new Tooltip(t -> {
            t.setBackground(bgMid);
            t.add(str);
        });

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

        WidgetGroup group = new WidgetGroup();
        group.fillParent = true;

        Table global = new Table();
        global.fillParent = true;
        global.right().table(bgDark, cont -> {
            ScrollPane pane = cont.pane(t -> {
                instructions = t.top();
                instructions.defaults().padBottom(8f);
            }).grow().get();
            pane.setScrollingDisabled(true, false);

            submenu = new Table(bgMid);
            submenu.setClip(true);
            submenu.setTransform(true);
            submenu.defaults().size(32f).pad(4f);

            submenu.button(icoPencil, () -> {
                add(new CRender());
                hideSubmenu();
            }).tooltip("Mesh Renderer");
            submenu.row().button(icoBuffer, () -> {
                add(new CBuffer());
                hideSubmenu();
            }).tooltip("Framebuffer Capture");
            submenu.row().button(icoBufferPencil, () -> {
                add(new CBlit());
                hideSubmenu();
            }).tooltip("Framebuffer Blit");
            submenu.setSize(40f, 120f);

            submenu.visible = false;
            submenu.setScale(1f, 0f);

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
        }).growY().width(520f);

        group.addChild(global);
        Core.scene.add(group);
    }

    public void showSubmenu(){
        if(submenu.visible) return;

        submenu.visible = true;
        submenu.actions(Actions.scaleTo(1f, 1f, 0.06f, Interp.pow3Out));
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
    }

    public void add(Container cont){
        Table t = instructions.table(bgDark).growX().fillY().get();
        cont.buildUI(t);

        instructions.row();
        renderer.containers.add(cont);
    }

    public void remove(Container cont, Table t){
        Cell<Table> cell = instructions.getCell(t);
        if(cell != null){
            instructions.removeChild(t);
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

    public static Cell<Table> defVec3(Table table, String title, Vec3 pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            cont.add(title).growX().fillY().pad(4f);

            cont.row();
            cont.field(Float.toString(pointer.x), TextFieldFilter.floatsOnly, str -> {
                pointer.x = parseFloat(str);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).padLeft(8f).tooltip("X");
            cont.field(Float.toString(pointer.y), TextFieldFilter.floatsOnly, str -> {
                pointer.y = parseFloat(str);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).tooltip("Y");
            cont.field(Float.toString(pointer.z), TextFieldFilter.floatsOnly, str -> {
                pointer.z = parseFloat(str);
                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).tooltip("Z");
        });
    }

    public static Cell<Table> defQuat(Table table, String title, Quat pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            cont.add(title).growX().fillY().pad(4f);

            Vec3 euler = new Vec3();

            cont.row();
            cont.field(Float.toString(euler.x), TextFieldFilter.floatsOnly, str -> {
                euler.x = parseFloat(str);
                pointer.setEulerAngles(euler.x, euler.y, euler.z);

                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).padLeft(8f).tooltip("Yaw");
            cont.field(Float.toString(euler.y), TextFieldFilter.floatsOnly, str -> {
                euler.y = parseFloat(str);
                pointer.setEulerAngles(euler.x, euler.y, euler.z);

                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).tooltip("Pitch");
            cont.field(Float.toString(euler.z), TextFieldFilter.floatsOnly, str -> {
                euler.z = parseFloat(str);
                pointer.setEulerAngles(euler.x, euler.y, euler.z);

                if(changed != null) changed.run();
            }).growX().fillY().pad(4f).tooltip("Roll");
        });
    }

    public static Cell<Table> defMat4(Table table, String title, Mat3D pointer, Runnable changed){
        return table.table(bgDark, cont -> {
            cont.add(title).growX().fillY().pad(4f);

            Vec3 pos = new Vec3();
            Quat rot = new Quat();
            Vec3 scl = new Vec3(1f, 1f, 1f);

            cont.row();
            defVec3(cont, "Position", pos, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().padTop(4f).padBottom(4f).padLeft(8f);

            cont.row();
            defQuat(cont, "Orientation", rot, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().padTop(4f).padBottom(4f).padLeft(8f);

            cont.row();
            defVec3(cont, "Scale", scl, () -> {
                pointer.set(pos, rot, scl);
                if(changed != null) changed.run();
            }).growX().fillY().padTop(4f).padBottom(4f).padLeft(8f);
        });
    }

    public static Cell<Table> defHeader(Table table, String title){
        return table.table(bgMid, t -> t.add(title, largeLabel).grow().pad(4f));
    }

    public static Cell<Table> defFooter(Table table, Container cont, Runnable changed){
        return table.table(bgMid, t -> t.right().button(icoMinus, () -> {
            ui.remove(cont, table);
            if(changed != null) changed.run();
        }).size(32f).pad(4f).tooltip("Remove"));
    }
}
