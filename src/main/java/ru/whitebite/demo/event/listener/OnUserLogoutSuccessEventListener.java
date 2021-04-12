 
package ru.whitebite.demo.event.listener;

import ru.whitebite.demo.cache.LoggedOutJwtTokenCache;
import ru.whitebite.demo.event.OnUserLogoutSuccessEvent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnUserLogoutSuccessEventListener implements ApplicationListener<OnUserLogoutSuccessEvent> {

    private final LoggedOutJwtTokenCache tokenCache;
    private static final Logger logger = Logger.getLogger(OnUserLogoutSuccessEventListener.class);

    @Autowired
    public OnUserLogoutSuccessEventListener(LoggedOutJwtTokenCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    public void onApplicationEvent( OnUserLogoutSuccessEvent event) {
        if (event == null)
            return;
        logger.info(String.format("Log out success event received for user [%s]", event.getUserEmail()));
        tokenCache.markLogoutEventForToken(event);
    }
}
