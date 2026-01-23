package com.example.starter.repository;

import com.example.starter.model.BulkUpload;
import com.example.starter.model.BulkUploadError;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

public class BulkUploadRepository {

  public Single<BulkUpload> save(BulkUpload upload) {
    return Single.fromCallable(() -> {
      DB.save(upload);
      return upload;
    });
  }

  public Single<BulkUpload> findById(String id) {
    return Single.fromCallable(() -> DB.find(BulkUpload.class, id));
  }

  public void saveError(String uploadId, String email, String reason) {
    DB.execute(() -> {
      BulkUploadError error = new BulkUploadError();
      error.setUploadId(uploadId);
      error.setEmail(email);
      error.setReason(reason);
      DB.save(error);
    });
  }

  public Single<List<BulkUploadError>> findErrors(String uploadId) {
    return Single.fromCallable(() ->
      DB.find(BulkUploadError.class).where().eq("uploadId", uploadId).findList()
    );
  }
}
