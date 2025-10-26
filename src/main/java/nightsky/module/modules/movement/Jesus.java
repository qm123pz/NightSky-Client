package nightsky.module.modules.movement;

import nightsky.module.Module;
import nightsky.value.values.BooleanValue;
import nightsky.value.values.FloatValue;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Jesus extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
    public final FloatValue speed = new FloatValue("Speed", 2.5F, 0.0F, 3.0F);
    public final BooleanValue noPush = new BooleanValue("NoPush", true);
    public final BooleanValue groundOnly = new BooleanValue("GroundOnly", true);

    public Jesus() {
        super("Jesus", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{df.format(this.speed.getValue())};
    }
}
