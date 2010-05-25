package org.jbei.ice.services.blazeds;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.models.Account;

public class BaseService {
    public static final String BASE_SERVICE_NAME = "BlazeDS";

    protected Account getAccountBySessionId(String sessionId) {
        Account account = null;

        try {
            account = AccountController.getAccountBySessionKey(sessionId);
        } catch (ControllerException e) {
            Logger.error("Failed to get account by sessionId: " + sessionId, e);

            return null;
        }

        if (account == null) {
            Logger.warn(getServiceName() + "User by sessionId doesn't exist: " + sessionId);

            return null;
        }

        return account;
    }

    protected String getServiceName() {
        return BASE_SERVICE_NAME;
    }

    protected String getLoggerPrefix() {
        return getServiceName() + ": ";
    }

    protected void logInfo(String message) {
        UsageLogger.info(getLoggerPrefix() + message);
    }
}