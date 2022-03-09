package shade.comp;

import arc.scene.ui.layout.*;
import arc.util.*;

public abstract class Container implements Disposable{
    public abstract void act();

    public abstract void buildUI(Table table);
}
