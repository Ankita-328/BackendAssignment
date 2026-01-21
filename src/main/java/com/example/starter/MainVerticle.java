package com.example.starter;

import com.example.starter.config.AdminSeeder;
import com.example.starter.config.AuthConfig;
import com.example.starter.config.DbConfig;
import com.example.starter.controller.AdminController;
import com.example.starter.controller.AuthController;
import com.example.starter.controller.RoleHandler;
import com.example.starter.repository.UserRepository;
import com.example.starter.service.AdminService;
import com.example.starter.service.AuthService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import com.example.starter.repository.KycRepository;
import com.example.starter.service.KycService;
import com.example.starter.controller.KycController;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    DbConfig.setup();
    AdminSeeder.seed();

    // 2. Setup Security (JWT)
    JWTAuth jwtAuth = AuthConfig.createAuthProvider(vertx);
    JWTAuthHandler jwtHandler = JWTAuthHandler.create(jwtAuth);

    // 3. Initialize Layers (Repository -> Service -> Controller)
    // We pass 'vertx' to the repository so it can do async DB calls.
    UserRepository userRepository = new UserRepository(vertx);

    AuthService authService = new AuthService(userRepository, jwtAuth);
    KycRepository kycRepository = new KycRepository(vertx);
    AdminService adminService = new AdminService(userRepository,kycRepository);

    AuthController authController = new AuthController(authService);
    AdminController adminController = new AdminController(adminService);


    KycService kycService = new KycService(kycRepository, userRepository);
    KycController kycController = new KycController(kycService);


    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


    router.route().handler(BodyHandler.create());

    router.post("/api/auth/login").handler(authController::login);


    router.post("/api/admin/onboard/student")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::onboardStudent);

    router.post("/api/admin/onboard/teacher")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::onboardTeacher);

    router.get("/api/admin/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::getProfile);

    router.put("/api/admin/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::updateProfile);

    router.get("/api/admin/users")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::listUsers);

    router.put("/api/admin/users/:userId/status")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::changeUserStatus);

    router.post("/api/kyc/submit")
      .handler(jwtHandler)
      .handler(kycController::submitKyc);

    router.get("/api/kyc/status")
      .handler(jwtHandler)
      .handler(kycController::getStatus);

    router.get("/api/admin/kyc")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::listKycs);

    router.put("/api/admin/kyc/:id/review")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(adminController::reviewKyc);


    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8000, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println(" Server started on port 8000");
        } else {
          startPromise.fail(http.cause());
        }
      });
  }
  public static void main(String[] args) {
    io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
    vertx.deployVerticle(new MainVerticle())
      .onSuccess(id -> System.out.println("✅ Deployment Succeeded!"))
      .onFailure(err -> {
        System.err.println("Deployment FAILED. Here is the error:");
        err.printStackTrace(); // This prints the REAL reason
      });
  }

}

