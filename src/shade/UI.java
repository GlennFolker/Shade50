package shade;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.layout.*;

public class UI implements ApplicationListener{
    public static TextureRegionDrawable
        bgWhite, bgLight, bgMid, bgDark,
        icoPlus, icoMinus;

    protected Table instructions;

    @Override
    public void init(){
        Events.on(Shade50.class, e -> {
            bgWhite = new TextureRegionDrawable(Core.atlas.white());
            bgLight = (TextureRegionDrawable)bgWhite.tint(0.75f, 0.75f, 0.8f, 0.5f);
            bgMid = (TextureRegionDrawable)bgWhite.tint(0.5f, 0.5f, 0.55f, 0.5f);
            bgDark = (TextureRegionDrawable)bgWhite.tint(0.25f, 0.25f, 0.3f, 0.5f);

            Core.scene.addStyle(ScrollPaneStyle.class, new ScrollPaneStyle(){{
                background = bgDark;
                hScroll = vScroll = bgMid;
                hScrollKnob = vScrollKnob = bgLight;
            }});

            Core.scene.addStyle(ImageButtonStyle.class, new ImageButtonStyle(){{
                up = bgDark;
                imageUpColor = Color.lightGray;
                imageOverColor = Color.white;
                imageDownColor = Color.gray;
                imageDisabledColor = Color.darkGray;
            }});

            icoPlus = new TextureRegionDrawable(Core.atlas.find("plus"));
            icoMinus = new TextureRegionDrawable(Core.atlas.find("minus"));

            build();
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
        global.right().table(bgMid, cont -> {
            ScrollPane pane = cont.pane(t -> instructions = t).grow().get();
            pane.setScrollingDisabled(true, false);

            cont.row().table(buttons -> {
                buttons.right().button(icoPlus, () -> {}).size(32f);
            }).growX().fillY();
        }).growY().width(240f);

        group.addChild(global);
        Core.scene.add(group);
    }

    @Override
    public void update(){
        Core.scene.act();
        Core.scene.draw();
    }
}
