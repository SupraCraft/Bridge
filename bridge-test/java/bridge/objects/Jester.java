package bridge.objects;

import bridge.Adopt;
import bridge.Synthetic;
import bridge.Unchecked;

@Synthetic
@Adopt(clean = true)
class Jester extends Super implements Adoptable {
    @Synthetic
    CharSequence cs;

    @Synthetic
    Jester(CharSequence cs) {
        super(cs);
    }

    @Synthetic
    @Override
    public Void v_method() {
        return null;
    }

    @Synthetic
    static void sneak() {
        throw new Unchecked(new NonException());
    }
}
