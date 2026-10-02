package at.ac.tuwien.ase.groupphase.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.invoke.MethodHandles;

public class JwtUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static JwtUser getActiveJwtUser() {
        LOGGER.trace("Getting authenticated JwtUser");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (JwtUser) authentication.getPrincipal();
    }
}
