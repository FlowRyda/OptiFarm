package at.ac.tuwien.ase.groupphase.backend.security;

import java.util.List;

public interface JwtTokenizer {
    String getAuthToken(String user, Long id, List<String> roles);
}
