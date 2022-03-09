package shade;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import shade.comp.*;

@SuppressWarnings("unchecked")
public class UI implements ApplicationListener{
    public static TextureRegionDrawable
        bgWhite, bgLight, bgMid, bgDark,
        icoPlus, icoMinus, icoReplay, icoPencil, icoBuffer, icoBufferPencil;

    protected Table instructions;
    protected ImageButton adder;

    @Override
    public void init(){
        bgWhite = new TextureRegionDrawable(Core.atlas.white());
        bgLight = (TextureRegionDrawable)bgWhite.tint(0.75f, 0.75f, 0.85f, 0.5f);
        bgMid = (TextureRegionDrawable)bgWhite.tint(0.5f, 0.5f, 0.6f, 0.5f);
        bgDark = (TextureRegionDrawable)bgWhite.tint(0.25f, 0.25f, 0.35f, 0.5f);

        icoPlus = new TextureRegionDrawable(Core.atlas.find("plus"));
        icoMinus = new TextureRegionDrawable(Core.atlas.find("minus"));
        icoReplay = new TextureRegionDrawable(Core.atlas.find("replay"));
        icoPencil = new TextureRegionDrawable(Core.atlas.find("pencil"));
        icoBuffer = new TextureRegionDrawable(Core.atlas.find("buffer"));
        icoBufferPencil = new TextureRegionDrawable(Core.atlas.find("buffer-pencil"));

        Core.scene.addStyle(ScrollPaneStyle.class, new ScrollPaneStyle(){{
            background = bgDark;
            hScroll = vScroll = bgMid;
            hScrollKnob = vScrollKnob = bgLight;
        }});

        Core.scene.addStyle(ImageButtonStyle.class, new ImageButtonStyle(){{
            imageUpColor = Color.lightGray;
            imageOverColor = Color.white;
            imageDownColor = Color.gray;
            imageDisabledColor = Color.darkGray;
        }});

        build();
        replay();
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
    }

    public void build(){
        Core.scene.clear();

        WidgetGroup group = new WidgetGroup();
        group.fillParent = true;

        Table global = new Table();
        global.fillParent = true;
        global.right().table(bgDark, cont -> {
            ScrollPane pane = cont.pane(t -> instructions = t).grow().get();
            pane.setScrollingDisabled(true, false);

            Table submenu = new Table(bgMid);
            submenu.defaults().size(32f).pad(4f);
            submenu.button(icoPencil, () -> {
                add(new CRender());
                submenu.visible = false;
            });
            submenu.row().button(icoBuffer, () -> {
                add(new CBuffer());
                submenu.visible = false;
            });
            submenu.row().button(icoBufferPencil, () -> {
                add(new CBlit());
                submenu.visible = false;
            });
            submenu.setSize(40f, 120f);
            submenu.visible = false;

            cont.row().table(bgMid, buttons -> {
                buttons.defaults().size(32f).pad(4f);

                buttons.center().right();
                buttons.button(icoReplay, () -> {
                    replay();
                    submenu.visible = false;
                });
                adder = buttons.button(icoPlus, () -> submenu.visible = !submenu.visible).get();
            }).growX().fillY().update(t -> {
                submenu.setPosition(adder.x - 4f, adder.y + adder.getHeight() + 4f);
                submenu.setOrigin(Align.bottomLeft);
            }).get().addChild(submenu);
        }).growY().width(240f);

        group.addChild(global);
        Core.scene.add(group);
    }

    public void replay(){
        instructions.clear();
    }

    public void add(Container cont){
        Table t = instructions.table().growX().fillY().get();
        cont.buildUI(t);

        t.row();
    }

    public void remove(Container cont, Table t){
        Cell<Table> cell = instructions.getCell(t);
        if(cell != null){
            instructions.removeChild(t);
            instructions.getCells().remove(cell);
            instructions.invalidateHierarchy();
        }

        cont.dispose();
    }

    @Override
    public void update(){
        Core.scene.act();
        Core.scene.draw();
    }
}
