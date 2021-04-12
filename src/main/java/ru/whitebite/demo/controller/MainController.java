 
package ru.whitebite.demo.controller;

import ru.whitebite.demo.annotation.CurrentUser;
import ru.whitebite.demo.event.OnUserLogoutSuccessEvent;
import ru.whitebite.demo.exception.BankTransactionException;
import ru.whitebite.demo.exception.TokenRefreshException;
import ru.whitebite.demo.exception.UserLoginException;
import ru.whitebite.demo.exception.UserRegistrationException;
import ru.whitebite.demo.model.CustomUserDetails;
import ru.whitebite.demo.model.payload.*;
import ru.whitebite.demo.model.token.RefreshToken;
import ru.whitebite.demo.security.JwtTokenProvider;
import ru.whitebite.demo.service.AuthService;
import ru.whitebite.demo.service.PaymentService;
import ru.whitebite.demo.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Api(value = "Authorization Rest API", description = "Defines endpoints that can be hit only when the user is not logged in. It's not secured by default.")
@RequiredArgsConstructor
public class MainController {

  private static final Logger logger = Logger.getLogger(MainController.class);
  private final AuthService authService;
  private final JwtTokenProvider tokenProvider;
  private final ApplicationEventPublisher applicationEventPublisher;

  private final UserService userService;
  private final PaymentService paymentService;


  /**
   * Entry point for the user log in. Return the jwt auth token and the refresh token
   */
  @PostMapping("/login")
  @ApiOperation(value = "Logs the user in to the system and return the auth tokens")
  public ResponseEntity<?> authenticateUser(
      @ApiParam(value = "The LoginRequest payload") @Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authService.authenticateUser(loginRequest)
        .orElseThrow(() -> new UserLoginException("Couldn't login user [" + loginRequest + "]"));

    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    logger.info("Logged in User returned [API]: " + customUserDetails.getUsername());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    return authService.createAndPersistRefreshTokenForDevice(authentication, loginRequest)
        .map(RefreshToken::getToken)
        .map(refreshToken -> {
          String jwtToken = authService.generateToken(customUserDetails);
          return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken, refreshToken,
              tokenProvider.getExpiryDuration()));
        })
        .orElseThrow(() -> new UserLoginException(
            "Couldn't create refresh token for: [" + loginRequest + "]"));
  }

  /**
   * Entry point for the user registration process. On successful registration, publish an event to
   * generate email verification token
   */
  @PostMapping("/register")
  @ApiOperation(value = "Registers the user and publishes an event to generate the email verification")
  public ResponseEntity<?> registerUser(
      @ApiParam(value = "The RegistrationRequest payload") @Valid @RequestBody RegistrationRequest registrationRequest) {

    return authService.registerUser(registrationRequest)
        .map(user -> {
          logger.info("Registered User returned [API[: " + user);
          return ResponseEntity.ok(new ApiResponse(true,
              "User registered successfully."));
        })
        .orElseThrow(() -> new UserRegistrationException(registrationRequest.getUsername(),
            "Missing user object in database"));
  }

  /**
   * Refresh the expired jwt token using a refresh token for the specific device and return a new
   * token to the caller
   */
  @PostMapping("/refresh")
  @ApiOperation(value =
      "Refresh the expired jwt authentication by issuing a token refresh request and returns the" +
          "updated response tokens")
  public ResponseEntity<?> refreshJwtToken(
      @ApiParam(value = "The TokenRefreshRequest payload") @Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {

    return authService.refreshJwtToken(tokenRefreshRequest)
        .map(updatedToken -> {
          String refreshToken = tokenRefreshRequest.getRefreshToken();
          logger.info("Created new Jwt Auth token: " + updatedToken);
          return ResponseEntity.ok(new JwtAuthenticationResponse(updatedToken, refreshToken,
              tokenProvider.getExpiryDuration()));
        })
        .orElseThrow(() -> new TokenRefreshException(tokenRefreshRequest.getRefreshToken(),
            "Unexpected error during token refresh. Please logout and login again."));
  }


  /**
   * Log the user out from the app/device. Release the refresh token associated with the user
   * device.
   */
  @PostMapping("/logout")
  @ApiOperation(value = "Logs the specified user device and clears the refresh tokens associated with it")
  public ResponseEntity<?> logoutUser(@CurrentUser CustomUserDetails customUserDetails) {
    Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
    OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(
        customUserDetails.getUsername(), credentials.toString());
    applicationEventPublisher.publishEvent(logoutSuccessEvent);
    return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
  }

  @PostMapping("/sendMoney")
  @ApiOperation(value = "Withdraw money from the card")
  public ResponseEntity<?> processSendMoney(@CurrentUser CustomUserDetails customUserDetails) {
    try {
      paymentService.withdrawMoney(customUserDetails.getId(),1.1D);
    } catch (BankTransactionException | NotFoundException e) {
      return ResponseEntity.ok(new ApiResponse(false, e.getMessage()));
    }
    return ResponseEntity.ok(new ApiResponse(true, "Send success"));
  }
}
