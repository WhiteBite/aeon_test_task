
package ru.whitebite.demo.service;

import ru.whitebite.demo.model.CustomUserDetails;
import ru.whitebite.demo.model.User;
import ru.whitebite.demo.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static ru.whitebite.demo.util.Util.getClientIP;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger logger = Logger.getLogger(CustomUserDetailsService.class);
  private final UserRepository userRepository;

  @Autowired
  private LoginAttemptService loginAttemptService;

  @Autowired
  private HttpServletRequest request;

  @Autowired
  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final String ip = getClientIP(request);
//    if (loginAttemptService.isBlocked(ip)) {
//      throw new RuntimeException("blocked");
//    }
    Optional<User> dbUser = userRepository.findByUsername(username);
    final UserDetails userDetails = loadUser(dbUser);
    return userDetails;
  }

  public UserDetails loadUserById(Long id) {
    Optional<User> dbUser = userRepository.findById(id);
    return loadUser(dbUser);
  }

  private UserDetails loadUser(Optional<User> dbUser) {
    final CustomUserDetails customUserDetails = dbUser.map(CustomUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException(
            "Couldn't find a matching user" ));
    return customUserDetails;
  }


}
