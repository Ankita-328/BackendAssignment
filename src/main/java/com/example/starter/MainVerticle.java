package com.example.starter;

import com.example.starter.config.AdminSeeder;
import com.example.starter.config.AuthConfig;
import com.example.starter.config.DbConfig;
import com.example.starter.controller.*;
import com.example.starter.repository.*;
import com.example.starter.service.AdminService;
import com.example.starter.service.AuthService;
import com.example.starter.service.UpdateService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import com.example.starter.service.KycService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.jackson.DatabindCodec;

public class MainVerticle extends AbstractVerticle {



  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    DbConfig.setup();
    AdminSeeder.seed();
    DatabindCodec.mapper().registerModule(new JavaTimeModule());

    JWTAuth jwtAuth = AuthConfig.createAuthProvider(vertx);
    JWTAuthHandler jwtHandler = JWTAuthHandler.create(jwtAuth);


    UserRepository userRepository = new UserRepository(vertx);

    AuthService authService = new AuthService(userRepository, jwtAuth);
    KycRepository kycRepository = new KycRepository(vertx);
    BulkUploadRepository bulkRepo = new BulkUploadRepository();
    StudentDetailsRepository studentDetailsRepo = new StudentDetailsRepository();
    TeacherDetailsRepository teacherDetailsRepo = new TeacherDetailsRepository();
    AdminService adminService = new AdminService(userRepository, kycRepository, bulkRepo);
    UpdateService updateService = new UpdateService(userRepository,teacherDetailsRepo,studentDetailsRepo);


    AuthController authController = new AuthController(authService);
    AdminController adminController = new AdminController(adminService);
    UpdateController updateController = new UpdateController(updateService);


    KycService kycService = new KycService(kycRepository, userRepository);
    KycController kycController = new KycController(kycService);


    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


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

    router.put("/api/student/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("STUDENT"))
      .handler(updateController::updateProfile);

    router.put("/api/teacher/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("TEACHER"))
      .handler(updateController::updateProfile);

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

//    router.post("/api/admin/bulk-upload")
//      .handler(jwtHandler)
//      .handler(RoleHandler.requireRole("ADMIN"))
//      .handler(ctx -> {
//        ctx.next();
//      })
//      .handler(adminController::bulkUpload);

    router.get("/api/admin/bulk-upload/template")
      .handler(adminController::downloadCsvTemplate);


    router.post("/api/admin/bulk-upload").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(ctx -> ctx.next()).handler(adminController::uploadCsv);
    router.get("/api/admin/bulk-upload/:id").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(adminController::getBulkStatus);
    router.get("/api/admin/bulk-upload/:id/errors").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(adminController::getBulkErrors);


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
      .onSuccess(id -> System.out.println(" Deployment Succeeded!"))
      .onFailure(err -> {
        System.err.println("Deployment FAILED. Here is the error:");
        err.printStackTrace();
      });
  }

}

