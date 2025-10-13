//package com.hotelbooking.account.grpc;
//
//import com.hotelbooking.account.entity.Account;
//import com.hotelbooking.account.repository.AccountRepository;
//import com.hotelbooking.account.service.JwtService;
//import com.hotelbooking.account.service.RefreshTokenService;
//import com.hotelbooking.account.proto.*;
//import io.grpc.stub.StreamObserver;
//import net.devh.boot.grpc.server.service.GrpcService;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@GrpcService
//public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase {
//
//    private final AccountRepository accountRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//    private final RefreshTokenService refreshTokenService;
//
//    public AccountGrpcService(AccountRepository accountRepository,
//                              PasswordEncoder passwordEncoder,
//                              JwtService jwtService,
//                              RefreshTokenService refreshTokenService) {
//        this.accountRepository = accountRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtService = jwtService;
//        this.refreshTokenService = refreshTokenService;
//    }
//
//    @Override
//    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
//        var userOpt = accountRepository.findByEmail(request.getEmail());
//        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
//            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED.withDescription("Invalid credentials").asRuntimeException());
//            return;
//        }
//        Account user = userOpt.get();
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", user.getUsername());
//        claims.put("email", user.getEmail());
//        claims.put("role", user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("user"));
//        String token = jwtService.generateToken(String.valueOf(user.getId()), claims);
//        String refresh = refreshTokenService.issueRefreshToken(String.valueOf(user.getId()));
//
//        LoginResponse resp = LoginResponse.newBuilder()
//                .setToken(token)
//                .setRefreshToken(refresh)
//                .setUser(toProtoUser(user))
//                .build();
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
//        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
//            responseObserver.onError(io.grpc.Status.ALREADY_EXISTS.withDescription("Email already exists").asRuntimeException());
//            return;
//        }
//
//        Account account = new Account();
//        account.setUsername(request.getUsername());
//        account.setEmail(request.getEmail());
//        account.setPhone(request.getPhone());
//        account.setPassword(passwordEncoder.encode(request.getPassword()));
//        account = accountRepository.save(account);
//
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", account.getUsername());
//        claims.put("email", account.getEmail());
//        claims.put("role", "user");
//        String token = jwtService.generateToken(String.valueOf(account.getId()), claims);
//        String refresh = refreshTokenService.issueRefreshToken(String.valueOf(account.getId()));
//
//        RegisterResponse resp = RegisterResponse.newBuilder()
//                .setToken(token)
//                .setRefreshToken(refresh)
//                .setUser(toProtoUser(account))
//                .build();
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void refreshToken(RefreshTokenRequest request, StreamObserver<RefreshTokenResponse> responseObserver) {
//        String userId = refreshTokenService.validateRefreshToken(request.getRefreshToken());
//        if (userId == null) {
//            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED.withDescription("Invalid refresh token").asRuntimeException());
//            return;
//        }
//        var user = accountRepository.findById(UUID.fromString(userId)).orElse(null);
//        if (user == null) {
//            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
//            return;
//        }
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", user.getUsername());
//        claims.put("email", user.getEmail());
//        claims.put("role", user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("user"));
//        String token = jwtService.generateToken(String.valueOf(user.getId()), claims);
//        String newRefresh = refreshTokenService.issueRefreshToken(String.valueOf(user.getId()));
//
//        RefreshTokenResponse resp = RefreshTokenResponse.newBuilder()
//                .setToken(token)
//                .setRefreshToken(newRefresh)
//                .build();
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
//        var user = accountRepository.findById(UUID.fromString(request.getUserId())).orElse(null);
//        if (user == null) {
//            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
//            return;
//        }
//        GetProfileResponse resp = GetProfileResponse.newBuilder()
//                .setUser(toProtoUser(user))
//                .build();
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//    private User toProtoUser(Account a) {
//        return User.newBuilder()
//                .setId(String.valueOf(a.getId()))
//                .setUsername(a.getUsername())
//                .setEmail(a.getEmail())
//                .setPhone(a.getPhone() == null ? "" : a.getPhone())
//                .setRole(a.getRoles().stream().findFirst().map(r -> r.getName()).orElse("user"))
//                .setCreatedAt(a.getCreatedAt().toString())
//                .build();
//    }
//}
