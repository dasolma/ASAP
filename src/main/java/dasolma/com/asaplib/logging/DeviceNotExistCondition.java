package dasolma.com.asaplib.logging;

import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.user.Device;

/**
 * Created by dasolma on 18/05/15.
 */
public class DeviceNotExistCondition  implements ICondition  {

    @Override
    public boolean meet() {
        return !Factory.getLogger().exist("devices", "id", Device.instance().getId());
    }
}
