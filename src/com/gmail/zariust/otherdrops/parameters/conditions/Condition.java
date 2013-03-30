package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Parameter;

public abstract class Condition extends Parameter {

    String conditionName = "undefined";

    public final boolean check(CustomDrop drop, OccurredEvent occurrence) {
        boolean result = checkInstance(drop, occurrence);
        Log.logInfo("Condition '" + this.getClass().getSimpleName() + "' " + (result ? "passed" : "failed"), Verbosity.HIGHEST);
        return result;
    }

    protected abstract boolean checkInstance(CustomDrop drop, OccurredEvent occurrence);
    //protected abstract static List<Condition> parseInstance(ConfigurationNode node);
}
