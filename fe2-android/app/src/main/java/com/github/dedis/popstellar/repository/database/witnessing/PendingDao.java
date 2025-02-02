package com.github.dedis.popstellar.repository.database.witnessing;

import androidx.room.*;

import com.github.dedis.popstellar.model.objects.security.MessageID;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface PendingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  Completable insert(PendingEntity pendingEntity);

  @Query("SELECT * FROM pending_objects WHERE lao_id = :laoId")
  Single<List<PendingEntity>> getPendingObjectsFromLao(String laoId);

  @Query("DELETE FROM pending_objects WHERE id = :messageID")
  Completable removePendingObject(MessageID messageID);
}
