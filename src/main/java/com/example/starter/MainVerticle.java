package com.example.starter;

import com.example.starter.config.AdminSeeder;
import com.example.starter.config.AuthConfig;
import com.example.starter.config.DbConfig;
import com.example.starter.controller.*;
import com.example.starter.repository.*;
import com.example.starter.service.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.core.http.Cookie;

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


    AuthController.INSTANCE.init(authService);
    AdminController.INSTANCE.init(adminService);
    UpdateController.INSTANCE.init(updateService);

    io.vertx.rxjava3.core.Vertx rxVertx = io.vertx.rxjava3.core.Vertx.newInstance(vertx);
    AiService aiService = new AiService(rxVertx, kycRepository);

    KycService kycService = new KycService(kycRepository, userRepository,aiService);
    KycController.INSTANCE.init(kycService, kycRepository);





    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


    router.route().handler(ctx -> {
      Cookie cookie = ctx.request().getCookie("jwt");
      if (cookie != null) {
        String token = cookie.getValue();
        ctx.request().headers().set("Authorization", "Bearer " + token);
      }
      ctx.next();
    });


    router.post("/api/auth/login").handler(AuthController.INSTANCE::login);


    router.post("/api/admin/onboard/student")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::onboardStudent);

    router.post("/api/admin/onboard/teacher")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::onboardTeacher);

    router.get("/api/admin/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::getProfile);

    router.put("/api/admin/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::updateProfile);

    router.put("/api/student/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("STUDENT"))
      .handler(UpdateController.INSTANCE::updateProfile);

    router.put("/api/teacher/profile")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("TEACHER"))
      .handler(UpdateController.INSTANCE::updateProfile);

    router.get("/api/admin/users")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::listUsers);

    router.put("/api/admin/users/:userId/status")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::changeUserStatus);

    router.post("/api/kyc/submit")
      .handler(jwtHandler)
      .handler(KycController.INSTANCE::submitKyc);

    router.get("/api/kyc/status")
      .handler(jwtHandler)
      .handler(KycController.INSTANCE::getStatus);

    router.get("/api/admin/kyc")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::listKycs);

    router.put("/api/admin/kyc/:id/review")
      .handler(jwtHandler)
      .handler(RoleHandler.requireRole("ADMIN"))
      .handler(AdminController.INSTANCE::reviewKyc);



    router.get("/api/admin/bulk-upload/template")
      .handler(AdminController.INSTANCE::downloadCsvTemplate);


    router.post("/api/admin/bulk-upload").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(ctx -> ctx.next()).handler(AdminController.INSTANCE::uploadCsv);
    router.get("/api/admin/bulk-upload/:id").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(AdminController.INSTANCE::getBulkStatus);
    router.get("/api/admin/bulk-upload/:id/errors").handler(jwtHandler).handler(RoleHandler.requireRole("ADMIN")).handler(AdminController.INSTANCE::getBulkErrors);

    router.get("/api/admin/kyc/:id").handler(KycController.INSTANCE::getKycById);


    router.post("/api/auth/logout").handler(AuthController.INSTANCE::logout);
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

