package dasolma.com.asaplib.logging;

import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.user.User;

/**
 * Created by dasolma on 15/05/15.
 */
public class UserNotExistCondition implements ICondition {
    @Override
    public boolean meet() {
        return !Factory.getLogger().exist("users", "id", User.instance().getId());
    }
}
