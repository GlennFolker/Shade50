package shade;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.layout.*;
import arc.util.*;

public class UI implements ApplicationListener{
    public static TextureRegionDrawable
        bgWhite, bgLight, bgMid, bgDark,
        icoPlus, icoMinus, icoReplay, icoPencil, icoBuffer;

    protected Table instructions;
    protected ImageButton adder;

    @Override
    public void init(){
        Events.on(Shade50.class, e -> {
            bgWhite = new TextureRegionDrawable(Core.atlas.white());
            bgLight = (TextureRegionDrawable)bgWhite.tint(0.75f, 0.75f, 0.8f, 0.5f);
            bgMid = (TextureRegionDrawable)bgWhite.tint(0.5f, 0.5f, 0.55f, 0.5f);
            bgDark = (TextureRegionDrawable)bgWhite.tint(0.25f, 0.25f, 0.3f, 0.5f);

            icoPlus = new TextureRegionDrawable(Core.atlas.find("plus"));
            icoMinus = new TextureRegionDrawable(Core.atlas.find("minus"));
            icoReplay = new TextureRegionDrawable(Core.atlas.find("replay"));
            icoPencil = new TextureRegionDrawable(Core.atlas.find("pencil"));
            icoBuffer = new TextureRegionDrawable(Core.atlas.find("buffer"));

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
        });
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
    }

    public void build(){
        if(!Core.assets.isFinished()) return;

        Core.scene.clear();

        WidgetGroup group = new WidgetGroup();
        group.fillParent = true;

        Table global = new Table();
        global.fillParent = true;
        global.right().table(bgDark, cont -> {
            ScrollPane pane = cont.pane(t -> instructions = t).grow().get();
            pane.setScrollingDisabled(true, false);

            Table submenu = new Table(bgMid);
            submenu.button(icoPencil, () -> submenu.visible = false).size(32f).pad(4f);
            submenu.row().button(icoBuffer, () -> submenu.visible = false).size(32f).pad(4f);
            submenu.setSize(40f, 76f);
            submenu.visible = false;

            cont.row().table(bgMid, buttons -> {
                buttons.center().right();
                buttons.button(icoReplay, () -> {}).size(32f).pad(4f);
                adder = buttons.button(icoPlus, () -> submenu.visible = !submenu.visible).size(32f).pad(4f).get();
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

    @Override
    public void update(){
        Core.scene.act();
        Core.scene.draw();
    }
}
