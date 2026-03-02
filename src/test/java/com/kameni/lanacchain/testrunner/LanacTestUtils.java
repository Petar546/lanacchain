package com.kameni.lanacchain.testrunner;

import com.kameni.lanacchain.lanac.data.LanacData;
import com.kameni.lanacchain.lanac.data.SignedAction;

import java.lang.reflect.Constructor;

public class LanacTestUtils {
    /**
     * Creates a SignedAction using the private constructor via Reflection.
     * This allows us to simulate a "Hacker" node without making the constructor public.
     */
    public static SignedAction createTamperedAction(LanacData data, String address, byte[] badSignature) throws Exception {
        // Look up the private constructor: SignedAction(LanacData, String, byte[])
        Constructor<SignedAction> constructor = SignedAction.class.getDeclaredConstructor(
                LanacData.class, String.class, byte[].class
        );

        constructor.setAccessible(true);
        return constructor.newInstance(data, address, badSignature);
    }
}
